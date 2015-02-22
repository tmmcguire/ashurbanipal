package net.crsr.ashurbanipal.store;

import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Part of speech proportions file.
 * 
 * <p>
 * The file contains the entry for one text per line, with the columns being the
 * text name followed by the proportion of the text that consists of each given
 * part of speech. Tabs separate the columns. A header line is first in the
 * file.
 * 
 * <p>
 * See {@link AbstractFileStore} for more information.
 * 
 * <p>
 * This class also implements {@code Map<String,Map<String,Double>>} to allow
 * the data in the file to be manipulated.
 */
public class PosStore extends AbstractFileStore implements Map<String,Map<String,Double>> {

  private final Map<String,Map<String,Double>> posTable = new TreeMap<>();
  private final List<String> columns = new ArrayList<>();
  private final String prefix;

  public PosStore(String filename) throws IOException {
    super(filename);
    this.prefix = null;
  }

  public PosStore(String prefix, String filename) throws IOException {
    super(filename);
    this.prefix = prefix;
  }

  @Override
  protected void readData(BufferedReader r) throws IOException {
    final String columnNamesLine = r.readLine();
    if (columnNamesLine == null) {
      this.valid = false;
      return;
    }
    columns.addAll(Arrays.asList(columnNamesLine.split("\\t")));

    String line = r.readLine();
    while (line != null) {
      final String[] values = line.split("\\t");
      // remove the leading directory prefix from the filename key
      final String filename = prepareFilename(values[0]);
      final Map<String,Double> data = new TreeMap<>();
      posTable.put(filename, data);
      for (int i = 1; i < values.length; ++i) {
        data.put(columns.get(i), Double.valueOf(values[i]));
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
    final StringBuilder sb = new StringBuilder().append("name");
    for (String column : columns) {
      sb.append('\t').append(column);
    }
    sb.append('\n');
    for (Entry<String,Map<String,Double>> entry : posTable.entrySet()) {
      sb.append(entry.getKey());
      for (String column : columns) {
        final Double value = entry.getValue().get(column);
        sb.append('\t').append(value != null ? value : 0.0);
      }
      sb.append('\n');
    }
    w.write(sb.toString().getBytes());
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
  public Map<String,Double> put(String key, Map<String,Double> value) {
    return posTable.put(prepareFilename(key), value);
  }

  @Override
  public Map<String,Double> remove(Object key) {
    return posTable.remove(key);
  }

  @Override
  public void putAll(Map<? extends String,? extends Map<String,Double>> m) {
    posTable.putAll(m);
  }

  @Override
  public void clear() {
    posTable.clear();
  }

  @Override
  public Set<String> keySet() {
    return posTable.keySet();
  }

  @Override
  public Collection<Map<String,Double>> values() {
    return posTable.values();
  }

  @Override
  public Set<java.util.Map.Entry<String,Map<String,Double>>> entrySet() {
    return posTable.entrySet();
  }

  private String prepareFilename(final String filename) {
    return prefix == null || !filename.startsWith(prefix) ? filename : filename.substring(prefix.length());
  }
}
