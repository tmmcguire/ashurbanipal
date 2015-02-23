package net.crsr.ashurbanipal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.crsr.ashurbanipal.store.FormatStore;
import net.crsr.ashurbanipal.store.MetadataStore;
import net.crsr.ashurbanipal.store.PosStore;
import net.crsr.ashurbanipal.store.WordStore;

import org.json.simple.JSONArray;

public class StoreToJson {

  public static void main(String[] args) {
    try {

      FormatStore formatStore = new FormatStore(args[0]);
      formatStore.read();
      MetadataStore metadataStore = new MetadataStore(args[1]);
      metadataStore.read();
      PosStore posStore = new PosStore(args[2]);
      posStore.read();
      WordStore nounStore = new WordStore(args[3]);
      nounStore.read();
      
      final Map<String,Integer> filenameToEtextNo = formatStore.asEtextNoLookupMap();
      final Map<String,Integer> filenameToFileId = fileIdMapping(filenameToEtextNo);
      
      final Map<String,Set<String>> nouns = nounStore.asSetsOfWords();

      writeFileEtextMap(etextLookup(filenameToFileId, filenameToEtextNo));
      writeMetadata(metadataArray(metadataStore));
      writePOS(posVectors(filenameToFileId, posStore));
      writeNouns(wordSetsForFiles(filenameToFileId, nouns));

    } catch (IOException e) {
      throw new IOError(e);
    }
  }

  private static Map<String,Integer> fileIdMapping(Map<String,?> map) {
    int count = 0;
    final Map<String,Integer> result = new HashMap<>();
    for (String filename : map.keySet()) {
      result.put(filename, count++);
    }
    return result;
  }

  private static List<Integer> etextLookup(Map<String,Integer> fileIds, Map<String,Integer> etextLookup) {
    List<Integer> array = new ArrayList<>(etextLookup.size());
    for (int i = 0; i < etextLookup.size(); ++i) { array.add(null); }
    for (Entry<String,Integer> entry : etextLookup.entrySet()) {
      final Integer fileId = fileIds.get(entry.getKey());
      final Integer etextNo = entry.getValue();
      array.set(fileId, etextNo);
    }
    return array;
  }
  
  private static Integer maxOfSet(Set<Integer> ints) {
    Integer max = null;
    for (Integer i : ints) {
      if (max == null || max < i) { max = i; }
    }
    return max;
  }

  private static List<Map<String,List<String>>> metadataArray(MetadataStore metadata) {
    int nRows = maxOfSet(metadata.keySet()) + 1;
    final List<Map<String,List<String>>> array = new ArrayList<>(nRows);
    for (int i = 0; i < nRows; ++i) { array.add(null); }
    for (Entry<Integer,Map<String,List<String>>> entry : metadata.entrySet()) {
      final Integer etextNo = entry.getKey();
      final Map<String,List<String>> data = entry.getValue();
      array.set(etextNo, data);
    }
    return array;
  }
  
  private static List<Double> dataVector(List<String> columns, Map<String,Double> metadata) {
    final List<Double> vector = new ArrayList<>();
    for (String column : columns.subList(1, columns.size())) { vector.add(metadata.get(column)); }
    return vector;
  }
  
  private static List<List<Double>> posVectors(Map<String,Integer> fileIds, PosStore posStore) {
    final List<String> columns = posStore.columns();
    final List<List<Double>> vectors = new ArrayList<>(fileIds.size());
    for (int i = 0; i < fileIds.size(); ++i) { vectors.add(null); }
    for (Entry<String,Map<String,Double>> entry : posStore.entrySet()) {
      int fileId = fileIds.get(entry.getKey());
      List<Double> vector = dataVector(columns, entry.getValue());
      vectors.set(fileId, vector);
    }
    return vectors;
  }
  
  private static Map<String,Integer> wordToId(Map<?,Set<String>> wordsByFile) {
    final Set<String> words = new HashSet<>();
    for (Set<String> fromFile : wordsByFile.values()) {
      words.addAll(fromFile);
    }
    final Map<String,Integer> result = new HashMap<>();
    int i = 0;
    for (String word : words) {
      result.put(word, i++);
    }
    return result;
  }
  
  private static List<Integer> fileToBitset(Map<String,Integer> wordId, Set<String> words) {
    int nBits = wordId.size();
    int nBitmaps = nBits / 32 + 1;
    final List<Integer> bitset = new ArrayList<>(nBitmaps);
    for (int i = 0; i < nBitmaps; ++i) { bitset.add(0); }
    for (String word : words) {
      int bit = wordId.get(word);
      bitset.set(bit / 32, ((int) bitset.get(bit / 32)) | 1 << bit % 32);
    }
    return bitset;
  }
  
  private static List<List<Integer>> wordSetsForFiles(Map<String,Integer> fileIds, Map<String,Set<String>> words) {
    final Map<String,Integer> wordIds = wordToId(words);
    final List<List<Integer>> wordsets = new ArrayList<>(fileIds.size());
    for (int i = 0; i < fileIds.size(); ++i) { wordsets.add(null); }
    for (Entry<String,Set<String>> entry : words.entrySet()) {
      final int fileId = fileIds.get(entry.getKey());
      final List<Integer> wordset = fileToBitset(wordIds, entry.getValue());
      wordsets.set(fileId, wordset);
    }
    return wordsets;
  }

  private static void writeFileEtextMap(List<Integer> idToEtext) throws IOException {
    final File newFile = new File("FileToEtext.js");
    OutputStream w = null;
    try {
      w = new FileOutputStream(newFile);
      w.write("// This array maps file identifiers to etext-numbers.\n".getBytes());
      w.write("fileToEtext = ".getBytes());
      w.write(JSONArray.toJSONString(idToEtext).getBytes());
      w.write("\n".getBytes());
    } finally {
      if (w != null) {
        w.close();
      }
    }
  }
  
  private static void writeMetadata(List<Map<String,List<String>>> metadata) throws IOException {
    final File newFile = new File("Metadata.js");
    OutputStream w = null;
    try {
      w = new FileOutputStream(newFile);
      w.write("// This array maps etext numbers to etext metadata.\n".getBytes());
      w.write("metadata = ".getBytes());
      w.write(JSONArray.toJSONString(metadata).getBytes());
      w.write("\n".getBytes());
    } finally {
      if (w != null) {
        w.close();
      }
    }
  }
  
  private static void writePOS(List<List<Double>> pos) throws IOException {
    final File newFile = new File("StyleData.js");
    OutputStream w = null;
    try {
      w = new FileOutputStream(newFile);
      w.write("// This array maps file ids to etext style data vectors.\n".getBytes());
      w.write("styleData = ".getBytes());
      w.write(JSONArray.toJSONString(pos).getBytes());
      w.write("\n".getBytes());
    } finally {
      if (w != null) {
        w.close();
      }
    }
  }
  
  private static void writeNouns(List<List<Integer>> bitsets) throws IOException {
    final File newFile = new File("TopicData.js");
    OutputStream w = null;
    try {
      w = new FileOutputStream(newFile);
      w.write("// This array maps file ids to sets of topic words.\n".getBytes());
      w.write("topicData = ".getBytes());
      w.write(JSONArray.toJSONString(bitsets).getBytes());
      w.write("\n".getBytes());
    } finally {
      if (w != null) {
        w.close();
      }
    }
  }

}
