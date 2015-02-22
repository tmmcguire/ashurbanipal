package net.crsr.ashurbanipal;

import java.io.IOError;
import java.io.IOException;
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
import net.crsr.ashurbanipal.utility.Pair;

public class DataCleaner {

  public static void main(String[] args) {
    try {

      final MetadataStore ms = cleanMetadata(args[0]);
      final FormatStore fs = cleanFormats(ms, args[1]);
      final WordStore ws1 = cleanWordStore(ms, fs, args[2]);
      System.out.println(invert( ws1.asSetsOfWords() ).keySet().size() + " unique words in " + args[2]);
      final WordStore ws2 = cleanWordStore(ms, fs, args[3]);
      System.out.println(invert( ws2.asSetsOfWords() ).keySet().size() + " unique words in " + args[3]);
      cleanPos(fs, args[4]);

    } catch (IOException e) {
      throw new IOError(e);
    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("Usage: DataCleaner metadata-file formats-file words-file1 words-file2 pos-file");
    }
  }

  private static MetadataStore cleanMetadata(String filename) throws IOException {
    final MetadataStore metadataStore = new MetadataStore(filename);
    metadataStore.read();
    final MetadataStore cleanedMetadata = new MetadataStore("c-" + filename);
    for (Entry<Integer,Map<String,List<String>>> elt : metadataStore.entrySet()) {
      // filter out non-English works
      for (String lang : elt.getValue().get("language")) {
        if (lang.contains("English")) {
          cleanedMetadata.put(elt.getKey(), elt.getValue());
        }
      }
    }
    cleanedMetadata.write();
    return cleanedMetadata;
  }
  
  private static FormatStore cleanFormats(MetadataStore metadata, String filename) throws IOException {
    final FormatStore formatStore = new FormatStore(filename);
    formatStore.read();
    final FormatStore cleanedFormats = new FormatStore("c-" + filename);
    for (Entry<Integer,List<Pair<String,String>>> elt : formatStore.entrySet()) {
      final Integer etext = elt.getKey();
      // filter out files for which there is no metadata (i.e. non-English texts)
      if (! metadata.containsKey(etext)) { continue; }
      final List<Pair<String,String>> cleanedFiles = new ArrayList<>();
      // filter out non-plain text files
      for (Pair<String,String> file : elt.getValue()) {
        if (file.l.contains("text/plain")) {
          cleanedFiles.add(file);
        }
      }
      cleanedFormats.put(etext, cleanedFiles);
    }
    cleanedFormats.write();
    return cleanedFormats;
  }
  
  private static PosStore cleanPos(FormatStore formats, String filename) throws IOException {
    final Map<String,Integer> etextLookup = formats.asEtextNoLookupMap();
    final PosStore posStore = new PosStore(filename);
    posStore.read();
    final PosStore cleanedPos = new PosStore("c-" + filename);
    for (Entry<String,Map<String,Double>> entry : posStore.entrySet()) {
      // filter out entries which do not have a matching file record
      final String file = entry.getKey();
      if (!etextLookup.containsKey(file)) { continue; }
      cleanedPos.put(entry.getKey(), entry.getValue());
    }
    cleanedPos.write();
    return cleanedPos;
  }

  private static WordStore cleanWordStore(MetadataStore metadata, FormatStore formats, String filename) throws IOException {
    final Map<String,Integer> etextLookup = formats.asEtextNoLookupMap();
    final WordStore wordStore = new WordStore(filename);
    wordStore.read();
    final Map<String,Set<String>> wordsToFiles = invert( wordStore.asSetsOfWords() );
    final WordStore cleanedWordStore = new WordStore("c-" + filename);
    for (Entry<String,Map<String,Integer>> entry : wordStore.entrySet()) {
      // filter out entries which do not have a matching file record
      final String file = entry.getKey();
      if (!etextLookup.containsKey(file)) { continue; }
      final Map<String,Integer> words = new HashMap<>();
      for (String word : entry.getValue().keySet()) {
        // filter out non-alphabetic words
        if (!containsAlphabetic(word)) { continue; }
        // filter out words only used in one file
        if (wordsToFiles.get(word).size() < 2) { continue; }
        words.put(word, entry.getValue().get(word));
      }
      cleanedWordStore.put(file, words);
    }
    cleanedWordStore.write();
    return cleanedWordStore;
  }

  private static boolean containsAlphabetic(String word) {
    final int length = word.length();
    int i = 0;
    while (i < length) {
      if (Character.isAlphabetic(word.codePointAt(i))) { break; }
      ++i;
    }
    return i < length;
  }
  
  private static <A,B> Map<B,Set<A>> invert(Map<A,Set<B>> map) {
    Map<B,Set<A>> result = new HashMap<>();
    for (Entry<A,Set<B>> entry : map.entrySet()) {
      for (B b : entry.getValue()) {
        Set<A> values = result.get(b);
        if (values == null) {
          values = new HashSet<>();
          result.put(b, values);
        }
        values.add(entry.getKey());
      }
    }
    return result;
  }
}
