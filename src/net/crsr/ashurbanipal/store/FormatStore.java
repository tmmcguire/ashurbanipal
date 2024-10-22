package net.crsr.ashurbanipal.store;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.crsr.ashurbanipal.utility.Pair;

/**
 * Mapping from etext numbers to pairs of (filename, content-type).
 */
public class FormatStore extends AbstractFileStore implements Map<Integer,List<Pair<String,String>>> {

  private final Map<Integer,List<Pair<String,String>>> formatsTable = new HashMap<>();

  public FormatStore(String filename) throws IOException {
    super(filename);
  }

  @Override
  protected void readData(BufferedReader r) throws IOException {
    // skip column headers
    r.readLine();
    String line = r.readLine();
    while (line != null) {
      final String[] values = line.split("\\t");
      final int etextNo = Integer.valueOf(values[0]);
      List<Pair<String,String>> entry = formatsTable.get(etextNo);
      if (entry == null) {
        entry = new ArrayList<>();
        formatsTable.put(etextNo, entry);
      }
      entry.add(Pair.pair(unescape(values[1]), unescape(values[2])));
      line = r.readLine();
    }
  }

  @Override
  protected void writeData(OutputStream w) throws IOException {
    final StringBuilder sb = new StringBuilder();
    sb.append("etext_no").append('\t').append("content_type").append('\t').append("filename").append('\n');
    for (Entry<Integer,List<Pair<String,String>>> entry : formatsTable.entrySet()) {
      this.formatEntry(sb, entry.getKey(), entry.getValue());
    }
    w.write(sb.toString().getBytes());
  }
  
  public void append(int etextNo, List<Pair<String,String>> formats) throws IOException {
      final StringBuilder sb = new StringBuilder();
      this.formatEntry(sb, etextNo, formats);
      this.appendString(sb.toString());
  }

  public Map<String,Integer> asEtextNoLookupMap() {
    final Map<String,Integer> map = new HashMap<>();
    for (Entry<Integer,List<Pair<String,String>>> entry : formatsTable.entrySet()) {
      for (Pair<String,String> subentry : entry.getValue()) {
        map.put(subentry.r, entry.getKey());
      }
    }
    return map;
  }
  
  public Map<Integer,String> asFileLookupMap() {
    // This assumes the FormatStore contains only one file for each etext.
    final Map<Integer,String> result = new TreeMap<>();
    for (Entry<Integer,List<Pair<String,String>>> entry : formatsTable.entrySet()) {
      if (entry.getValue().size() != 1) {
        throw new IllegalStateException("etext " + entry.getKey() + " has multiple files");
      }
      result.put(entry.getKey(), entry.getValue().get(0).r);
    }
    return result;
  }

  @Override
  public int size() {
    return formatsTable.size();
  }

  @Override
  public boolean isEmpty() {
    return formatsTable.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return formatsTable.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return formatsTable.containsValue(value);
  }

  @Override
  public List<Pair<String,String>> get(Object key) {
    return formatsTable.get(key);
  }

  @Override
  public List<Pair<String,String>> put(Integer key, List<Pair<String,String>> value) {
    return formatsTable.put(key, value);
  }

  @Override
  public List<Pair<String,String>> remove(Object key) {
    return formatsTable.remove(key);
  }

  @Override
  public void putAll(Map<? extends Integer,? extends List<Pair<String,String>>> m) {
    formatsTable.putAll(m);
  }

  @Override
  public void clear() {
    formatsTable.clear();
  }

  @Override
  public Set<Integer> keySet() {
    return formatsTable.keySet();
  }

  @Override
  public Collection<List<Pair<String,String>>> values() {
    return formatsTable.values();
  }

  @Override
  public Set<java.util.Map.Entry<Integer,List<Pair<String,String>>>> entrySet() {
    return formatsTable.entrySet();
  }

  private void formatEntry(StringBuilder sb, int etextNo, List<Pair<String,String>> formats) {
    for (Pair<String,String> elt : formats) {
      sb
      .append( etextNo ).append('\t')
      .append( elt.l ).append('\t')
      .append( elt.r ).append('\n');
    }
  }

}
