package net.crsr.ashurbanipal.store;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Part of speech proportions file.
 * 
 * <p>
 * The file contains the entry for one text per line, with the columns being the
 * etext number followed by the proportion of the text that consists of each given
 * part of speech. Tabs separate the columns. A header line is first in the
 * file.
 * 
 * <p>
 * See {@link AbstractFileStore} for more information.
 * 
 * <p>
 * This class also implements {@code Map<Integer,Map<String,Double>>} to allow
 * the data in the file to be manipulated.
 */
public class PosStore extends AbstractFileStore implements Map<Integer,Map<String,Double>> {

  private final Map<Integer,Map<String,Double>> posTable = new HashMap<>();
  private final List<String> columns = new ArrayList<>();
//  private final String prefix;

  public PosStore(String filename) throws IOException {
    super(filename);
//    this.prefix = null;
  }

//  public PosStore(String prefix, String filename) throws IOException {
//    super(filename);
//    this.prefix = prefix;
//  }

  @Override
  protected void readData(BufferedReader r) throws IOException {
    // Header line
    final String headerLine = r.readLine();
    if (headerLine == null) {
      this.valid = false;
      return;
    }
    final List<String> header = Arrays.asList(headerLine.split("\\t"));
    if (header.size() > 1) {
      columns.addAll(header.subList(1, header.size()));
    }
    // Entries
    String line = r.readLine();
    while (line != null) {
      final String[] values = line.split("\\t");
      final Integer etextNo = Integer.valueOf(values[0]);
      final Map<String,Double> data = new HashMap<>();
      posTable.put(etextNo, data);
      for (int i = 1; i < values.length; ++i) {
        data.put(columns.get(i-1), Double.valueOf(values[i]));
      }
      line = r.readLine();
    }
  }

  @Override
  protected void writeData(OutputStream w) throws IOException {
    final TreeSet<String> columns = new TreeSet<>();
    for (Map<String,Double> data : posTable.values()) {
      columns.addAll(data.keySet());
    }
    // Header line
    final StringBuilder sb = new StringBuilder().append("etext_no");
    for (String column : columns) {
      sb.append('\t').append(column);
    }
    sb.append('\n');
    // Entries
    for (Entry<Integer,Map<String,Double>> entry : posTable.entrySet()) {
      formatEntry(sb, entry.getKey(), entry.getValue());
    }
    w.write(sb.toString().getBytes());
  }
  
  public void append(Integer etextNo, Map<String,Double> data) throws IOException {
    posTable.put(etextNo, data);
    final Set<String> newColumns = data.keySet();
    if (newColumns.size() > columns.size()) {
      columns.clear();
      columns.addAll(newColumns);
      this.write();
    } else {
      final StringBuilder sb = new StringBuilder();
      this.formatEntry(sb, etextNo, data);
      this.appendString(sb.toString());
    }
  }
  
  private void formatEntry(StringBuilder sb, Integer etextNo, Map<String,Double> data) {
    sb.append(etextNo);
    for (String column : columns) {
      final Double value = data.get(column);
      sb.append('\t').append(value != null ? value : 0.0);
    }
    sb.append('\n');
  }

  public List<String> columns() {
    return Collections.unmodifiableList(columns);
  }

  @Override
  public int size() {
    return posTable.size();
  }

  @Override
  public boolean isEmpty() {
    return posTable.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return posTable.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return posTable.containsValue(value);
  }

  @Override
  public Map<String,Double> get(Object key) {
    return posTable.get(key);
  }

  @Override
  public Map<String,Double> put(Integer key, Map<String,Double> value) {
    return posTable.put(key, value);
  }

  @Override
  public Map<String,Double> remove(Object key) {
    return posTable.remove(key);
  }

  @Override
  public void putAll(Map<? extends Integer,? extends Map<String,Double>> m) {
    posTable.putAll(m);
  }

  @Override
  public void clear() {
    posTable.clear();
  }

  @Override
  public Set<Integer> keySet() {
    return posTable.keySet();
  }

  @Override
  public Collection<Map<String,Double>> values() {
    return posTable.values();
  }

  @Override
  public Set<java.util.Map.Entry<Integer,Map<String,Double>>> entrySet() {
    return posTable.entrySet();
  }

//  private String prepareFilename(final String filename) {
//    return prefix == null || !filename.startsWith(prefix) ? filename : filename.substring(prefix.length());
//  }
}
