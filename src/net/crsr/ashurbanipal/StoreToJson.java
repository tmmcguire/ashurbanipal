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

import org.json.simple.JSONObject;

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
      final Map<String,Set<String>> nouns = nounStore.asSetsOfWords();

      writeMetadata(metadataArray(metadataStore, formatStore));
      writePOS(posVectors(filenameToEtextNo, posStore));
      writeNouns(wordSetsForFiles(filenameToEtextNo, nouns));

    } catch (IOException e) {
      throw new IOError(e);
    }
  }

  private static List<Map<String,List<String>>> metadataArray(MetadataStore metadata, Map<Integer,?> formatStore) {
    final List<Map<String,List<String>>> array = new ArrayList<>();
    for (Entry<Integer,Map<String,List<String>>> entry : metadata.entrySet()) {
      final Integer etextNo = entry.getKey();
      final Map<String,List<String>> data = entry.getValue();
      if (formatStore.containsKey(etextNo)) {
        array.add(data);
      }
    }
    return array;
  }

  private static List<Double> dataVector(List<String> columns, Map<String,Double> metadata) {
    final List<Double> vector = new ArrayList<>();
    for (String column : columns.subList(1, columns.size())) { vector.add(metadata.get(column)); }
    return vector;
  }

  private static List<Map<String,Object>> posVectors(Map<String,Integer> fileToEtext, PosStore posStore) {
    final List<String> columns = posStore.columns();
    final List<Map<String,Object>> vectors = new ArrayList<>();
    for (Entry<String,Map<String,Double>> entry : posStore.entrySet()) {
      final Map<String,Object> row = new HashMap<>();
      row.put("vector", dataVector(columns, entry.getValue()));
      row.put("etext_no", fileToEtext.get(entry.getKey()));
      vectors.add(row);
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

  private static List<Map<String,Object>> wordSetsForFiles(Map<String,Integer> fileToEtext, Map<String,Set<String>> words) {
    final Map<String,Integer> wordIds = wordToId(words);
    final List<Map<String,Object>> wordsets = new ArrayList<>();
    for (Entry<String,Set<String>> entry : words.entrySet()) {
      final Map<String,Object> row = new HashMap<>();
      row.put("etext_no", fileToEtext.get(entry.getKey()));
      row.put("wordset", fileToBitset(wordIds, entry.getValue()));
      wordsets.add(row);
    }
    return wordsets;
  }

  private static void writeMetadata(List<Map<String,List<String>>> metadata) throws IOException {
    // put the result in the format Ext wants
    final Map<String,Object> object = new HashMap<>();
    object.put("rows", metadata);
    final File newFile = new File("metadata.json");
    OutputStream w = null;
    try {
      w = new FileOutputStream(newFile);
      w.write(JSONObject.toJSONString(object).getBytes());
      w.write("\n".getBytes());
    } finally {
      if (w != null) {
        w.close();
      }
    }
  }

  private static void writePOS(List<Map<String,Object>> rows) throws IOException {
    final Map<String,Object> object = new HashMap<>();
    object.put("rows", rows);
    final File newFile = new File("styledata.json");
    OutputStream w = null;
    try {
      w = new FileOutputStream(newFile);
      w.write(JSONObject.toJSONString(object).getBytes());
      w.write("\n".getBytes());
    } finally {
      if (w != null) {
        w.close();
      }
    }
  }

  private static void writeNouns(List<Map<String,Object>> wordsets) throws IOException {
    final Map<String,Object> object = new HashMap<>();
    object.put("rows", wordsets);
    final File newFile = new File("topicdata.json");
    OutputStream w = null;
    try {
      w = new FileOutputStream(newFile);
      w.write(JSONObject.toJSONString(object).getBytes());
      w.write("\n".getBytes());
    } finally {
      if (w != null) {
        w.close();
      }
    }
  }

}
