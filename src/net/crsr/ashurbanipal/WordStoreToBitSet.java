package net.crsr.ashurbanipal;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOError;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.crsr.ashurbanipal.store.WordStore;

public class WordStoreToBitSet {

  public static void main(String[] args) {
    try {

      final WordStore wordStore = new WordStore(args[0]);
      wordStore.read();

      // Read all of the words in the store, counting the
      // number of files using them.
      final Map<String,Integer> words = new TreeMap<>();
      for (Map<String,Integer> map : wordStore.values()) {
        for (String word : map.keySet()) {
          words.put(word, words.getOrDefault(word, 0) + 1);
        }
      }

      // Create a mapping of words (used by more than one file
      // and all alphabetic) to identifying numbers.
      final Map<String,Integer> wordIds = new HashMap<>();
      int i = 0;
      for (Entry<String,Integer> word : words.entrySet()) {
        if (word.getValue() > 1 && isAlpha(word.getKey())) {
          wordIds.put(word.getKey(), i);
          ++i;
        }
      }
      System.out.println(wordIds.size() + " words");

      // For each text, write the etext_no and the id of each word to a line.
      final Writer writer = new BufferedWriter(new FileWriter(args[1]));
      for (Integer etext_no : wordStore.keySet()) {
        final StringBuilder sb = new StringBuilder().append(etext_no);
        for (String word : wordStore.get(etext_no).keySet()) {
          final Integer bit = wordIds.get(word);
          if (bit != null) {
            sb.append('\t').append(bit);
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
