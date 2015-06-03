package net.crsr.ashurbanipal;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOError;
import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.crsr.ashurbanipal.store.FormatStore;
import net.crsr.ashurbanipal.store.WordStore;

public class TopicToBitSet {

  public static void main(String[] args) {
    try {
      
      final WordStore wordStore = new WordStore(args[0]);
      wordStore.read();
      
      final FormatStore formatStore = new FormatStore(args[1]);
      formatStore.read();
      final Map<String,Integer> fileToEtext = formatStore.asEtextNoLookupMap();

      final Map<String,Integer> words = new TreeMap<>();
      for (Map<String,Integer> map : wordStore.values()) {
        for (String word : map.keySet()) {
          words.put(word, words.getOrDefault(word, 0) + 1);
        }
      }
      
      final Map<String,Integer> wordIds = new HashMap<>();
      int i = 0;
      for (Entry<String,Integer> word : words.entrySet()) {
        if (word.getValue() > 1 && isAlpha(word.getKey())) {
          wordIds.put(word.getKey(), i);
          ++i;
        }
      }
      System.out.println(wordIds.size() + " words");
      
      Writer writer = new BufferedWriter(new FileWriter(args[2]));
      for (String file : wordStore.keySet()) {
        final Integer etext_no = fileToEtext.get(file);
        final StringBuilder sb = new StringBuilder().append(etext_no);
//        System.out.println(etext_no);
//        BigInteger bitSet = BigInteger.valueOf(0);
        for (String word : wordStore.get(file).keySet()) {
          final Integer bit = wordIds.get(word);
          if (bit != null) {
            sb.append('\t').append(bit);
//            bitSet = bitSet.setBit( bit );
          }
        }
        sb.append('\n');
        writer.write(sb.toString());;
      }
      writer.close();

    } catch (IOException e) {
      throw new IOError(e);
    }

  }
  
  private static boolean isAlpha(String s) {
    for (char ch : s.toCharArray()) {
      if (!Character.isLetter(ch)) {
        return false;
      }
    }
    return true;
  }

}
