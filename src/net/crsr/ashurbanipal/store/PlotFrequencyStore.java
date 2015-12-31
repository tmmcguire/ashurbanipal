package net.crsr.ashurbanipal.store;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.crsr.ashurbanipal.utility.Complex;

/**
 * 
 * @author mcguire
 *
 */
public class PlotFrequencyStore extends AbstractFileStore implements Map<Integer,List<Complex>> {

  private final Map<Integer,List<Complex>> store = new TreeMap<>();
  private final int setSize;
  
  public PlotFrequencyStore(String filename) throws IOException {
    super(filename);
    this.setSize = 20;
  }

  public PlotFrequencyStore(String filename, int setSize) throws IOException {
    super(filename);
    this.setSize = setSize;
  }

  @Override
  protected void readData(BufferedReader r) throws IOException {
    // Header line
    r.readLine();
    // Entries
    String line = r.readLine();
    while (line != null) {
      final String[] values = line.split("\\t");
      final Integer etextNo = Integer.valueOf(values[0]);
      final List<Complex> data = new ArrayList<>(setSize);
      store.put(etextNo, data);
      data.add( new Complex(Double.valueOf(values[1]), 0.0) );
      for (int i = 2; i < values.length; i += 2) {
        if (i < values.length - 1) {
          data.add( new Complex(Double.valueOf(values[i]), Double.valueOf(values[i+1])) );          
        } else {
          data.add( new Complex(Double.valueOf(values[i]), 0.0) );
        }
      }
      line = r.readLine();
    }
  }

  @Override
  protected void writeData(OutputStream w) throws IOException {
    // Header line
    final StringBuilder sb = new StringBuilder().append("etext_no\tdata\n");
    // Entries
    for (Entry<Integer,List<Complex>> entry : store.entrySet()) {
      formatEntry(sb, entry.getKey(), entry.getValue());
    }
    w.write(sb.toString().getBytes());
  }
  
  public void append(Integer etextNo, List<Complex> data) throws IOException {
    List<Complex> keptData = data.subList(0, setSize != 0 ? Math.min(setSize, data.size()) : data.size());
    store.put(etextNo, keptData);
    final StringBuilder sb = new StringBuilder();
    this.formatEntry(sb, etextNo, keptData);
    this.appendString(sb.toString());
  }
  
  private void formatEntry(StringBuilder sb, Integer etextNo, List<Complex> data) {
    sb.append(etextNo)
    .append('\t')
    .append(data.get(0).real);
    for (Complex value : data.subList(1, data.size())) {
      sb.append('\t').append(value.real).append('\t').append(value.imag);
    }
    sb.append('\n');
  }
  
  // Delegate methods

  public int size() {
    return store.size();
  }

  public boolean isEmpty() {
    return store.isEmpty();
  }

  public boolean containsKey(Object key) {
    return store.containsKey(key);
  }

  public boolean containsValue(Object value) {
    return store.containsValue(value);
  }

  public List<Complex> get(Object key) {
    return store.get(key);
  }

  public List<Complex> put(Integer key, List<Complex> value) {
    return store.put(key, value);
  }

  public List<Complex> remove(Object key) {
    return store.remove(key);
  }

  public void putAll(Map<? extends Integer,? extends List<Complex>> m) {
    store.putAll(m);
  }

  public void clear() {
    store.clear();
  }

  public Set<Integer> keySet() {
    return store.keySet();
  }

  public Collection<List<Complex>> values() {
    return store.values();
  }

  public Set<java.util.Map.Entry<Integer,List<Complex>>> entrySet() {
    return store.entrySet();
  }

  public boolean equals(Object o) {
    return store.equals(o);
  }

  public int hashCode() {
    return store.hashCode();
  }

  public List<Complex> getOrDefault(Object key, List<Complex> defaultValue) {
    return store.getOrDefault(key, defaultValue);
  }

  public void forEach(BiConsumer<? super Integer,? super List<Complex>> action) {
    store.forEach(action);
  }

  public void replaceAll(BiFunction<? super Integer,? super List<Complex>,? extends List<Complex>> function) {
    store.replaceAll(function);
  }

  public List<Complex> putIfAbsent(Integer key, List<Complex> value) {
    return store.putIfAbsent(key, value);
  }

  public boolean remove(Object key, Object value) {
    return store.remove(key, value);
  }

  public boolean replace(Integer key, List<Complex> oldValue, List<Complex> newValue) {
    return store.replace(key, oldValue, newValue);
  }

  public List<Complex> replace(Integer key, List<Complex> value) {
    return store.replace(key, value);
  }

  public List<Complex> computeIfAbsent(Integer key, Function<? super Integer,? extends List<Complex>> mappingFunction) {
    return store.computeIfAbsent(key, mappingFunction);
  }

  public List<Complex> computeIfPresent(Integer key,
      BiFunction<? super Integer,? super List<Complex>,? extends List<Complex>> remappingFunction) {
    return store.computeIfPresent(key, remappingFunction);
  }

  public List<Complex> compute(Integer key,
      BiFunction<? super Integer,? super List<Complex>,? extends List<Complex>> remappingFunction) {
    return store.compute(key, remappingFunction);
  }

  public List<Complex> merge(Integer key, List<Complex> value,
      BiFunction<? super List<Complex>,? super List<Complex>,? extends List<Complex>> remappingFunction) {
    return store.merge(key, value, remappingFunction);
  }

}
