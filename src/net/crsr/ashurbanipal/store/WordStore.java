package net.crsr.ashurbanipal.store;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Word count storage file.
 * 
 * <p>
 * This file contains the entry for one text per line, with the columns being
 * the etext number followed by (word, count) pairs for the <code>setSize</code>
 * most common words in the text. Tabs separate columns and a space separates the
 * word and count.
 * 
 * <p>
 * If <code>setSize</code> is 0, all words inserted into the {@link WordStore} are kept when the file is written.
 * If <code>setSize</code> is greater than 0, only the <code>setSize</code> most numerous words are kept.
 * The default for <code>setSize</code> is 200.
 */
public class WordStore extends AbstractFileStore implements Map<Integer,Map<String,Integer>> {

  private final Map<Integer,Map<String,Integer>> wordStore = new TreeMap<>();
  private final int setSize;

  public WordStore(String filename) throws IOException {
    super(filename);
    this.setSize = 200;
  }

  public WordStore(String filename, int setSize) throws IOException {
    super(filename);
    this.setSize = setSize;
  }

  @Override
  protected void readData(BufferedReader r) throws IOException {
    String line = r.readLine();
    while (line != null) {
      final String[] values = line.split("\\t");
      final Map<String,Integer> data = new TreeMap<>();
      wordStore.put(Integer.valueOf(values[0]), data);
      for (int i = 1; i < values.length; ++i) {
        final String[] pair = values[i].split(" ", 2);
        data.put(pair[0], Integer.valueOf(pair[1]));
      }
      line = r.readLine();
    }
  }

  @Override
  protected void writeData(OutputStream w) throws IOException {
    final StringBuilder sb = new StringBuilder();
    for (Entry<Integer,Map<String,Integer>> entry : wordStore.entrySet()) {
      formatEntry(sb, entry.getKey(), entry.getValue());
    }
    w.write(sb.toString().getBytes());
  }

  public void append(Integer etextNo, Map<String,Integer> wordCounts) throws IOException {
    OutputStream os = null;
    try {
      os = new FileOutputStream(file.getAbsoluteFile(), true);
      final StringBuilder sb = new StringBuilder();
      this.formatEntry(sb, etextNo, wordCounts);
      os.write(sb.toString().getBytes());
    } finally {
      if (os != null) {
        os.close();
      }
    }
  }
  
  public Map<Integer,Set<String>> asSetsOfWords() {
    final Map<Integer,Set<String>> result = new HashMap<>();
    for (Entry<Integer,Map<String,Integer>> entry : this.entrySet()) {
      result.put(entry.getKey(), entry.getValue().keySet());
    }
    return result;
  }

  private void formatEntry(final StringBuilder sb, Integer etextNo, Map<String,Integer> wordCounts) {
    sb.append(etextNo).append('\t');

    final List<Entry<String,Integer>> values = new ArrayList<>(wordCounts.entrySet());
    values.sort(new Comparator<Entry<String,Integer>>() {
      @Override
      public int compare(Entry<String,Integer> left, Entry<String,Integer> right) {
        return -Integer.compare(left.getValue(), right.getValue());
      }
    });
    final int setSizeLimit = setSize > 0 ? Integer.min(setSize, values.size()) : values.size();
    for (Entry<String,Integer> subentry : values.subList(0, setSizeLimit)) {
      sb.append(subentry.getKey()).append(' ').append(subentry.getValue()).append('\t');
    }
    sb.append('\n');
  }

  @Override
  public int size() {
    return wordStore.size();
  }

  @Override
  public boolean isEmpty() {
    return wordStore.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return wordStore.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return wordStore.containsValue(value);
  }

  @Override
  public Map<String,Integer> get(Object key) {
    return wordStore.get(key);
  }

  @Override
  public Map<String,Integer> put(Integer key, Map<String,Integer> value) {
    return wordStore.put(key, value);
  }

  @Override
  public Map<String,Integer> remove(Object key) {
    return wordStore.remove(key);
  }

  @Override
  public void putAll(Map<? extends Integer,? extends Map<String,Integer>> m) {
    wordStore.putAll(m);
  }

  @Override
  public void clear() {
    wordStore.clear();
  }

  @Override
  public Set<Integer> keySet() {
    return wordStore.keySet();
  }

  @Override
  public Collection<Map<String,Integer>> values() {
    return wordStore.values();
  }

  @Override
  public Set<java.util.Map.Entry<Integer,Map<String,Integer>>> entrySet() {
    return wordStore.entrySet();
  }

}
