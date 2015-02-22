package net.crsr.ashurbanipal.store;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class MetadataStore extends AbstractFileStore implements Map<Integer,Map<String,List<String>>> {
	
	public static int getEtextNumber(Map<String,List<String>> metadata) {
		return Integer.valueOf( metadata.get("etext_no").get(0) );
	}

	private static final List<String> columns = new ArrayList<>(
			Arrays.asList("etext_no", "link", "title", "author", "subject", "language", "release_date", "loc_class", "note", "copyright_status")
			);

	private final Map<Integer,Map<String,List<String>>> metadataTable = new TreeMap<>();

	public MetadataStore(String filename) throws IOException {
		super(filename);
	}

	@Override
	protected void readData(BufferedReader r) throws IOException {
		// skip column headers
		r.readLine();
		String line = r.readLine();
		while (line != null) {
			final String[] values = line.split("\\t");
			final Map<String,List<String>> data = new HashMap<>();
			for (int i = 0; i < values.length; ++i) {
				data.put(columns.get(i), Arrays.asList( values[i].split("; ") ));
			}
			metadataTable.put(getEtextNumber(data), data);
			line = r.readLine();
		}
	}

	@Override
	protected void writeData(OutputStream w) throws IOException {
		final StringBuilder sb = new StringBuilder().append(columns.get(0));
		for (String column : columns.subList(1, columns.size())) {
			sb.append('\t').append(column);
		}
		sb.append('\n');
		for (Map<String,List<String>> entry : metadataTable.values()) {
			formatEntry(sb, entry);
		}
		w.write(sb.toString().getBytes());
	}
	
	public void append(Map<String,List<String>> metadata) throws IOException {
		OutputStream os = null;
		try {
			os = new FileOutputStream(file.getAbsoluteFile(), true);
			final StringBuilder sb = new StringBuilder();
			this.formatEntry(sb, metadata);
			os.write(sb.toString().getBytes());
		} finally {
			if (os != null) { os.close(); }
		}
	}
	
	private void formatEntry(StringBuilder sb, Map<String,List<String>> metadata) {
		sb.append( String.join("; ", metadata.get(columns.get(0))) );
		for (String column : columns.subList(1, columns.size())) {
			final List<String> value = metadata.get(column);
			sb.append('\t').append( value != null ? String.join("; ", value) : "" );
		}
		sb.append('\n');
	}

	@Override
	public int size() {
		return metadataTable.size();
	}

	@Override
	public boolean isEmpty() {
		return metadataTable.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return metadataTable.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return metadataTable.containsValue(value);
	}

	@Override
	public Map<String, List<String>> get(Object key) {
		return metadataTable.get(key);
	}

	@Override
	public Map<String, List<String>> put(Integer key, Map<String, List<String>> value) {
		final Map<String, List<String>> result = metadataTable.put(key, value);
		return result;
	}

	@Override
	public Map<String, List<String>> remove(Object key) {
		final Map<String, List<String>> result = metadataTable.remove(key);
		return result;
	}

	@Override
	public void putAll(Map<? extends Integer, ? extends Map<String, List<String>>> m) {
		metadataTable.putAll(m);
	}

	@Override
	public void clear() {
		metadataTable.clear();
	}

	@Override
	public Set<Integer> keySet() {
		return metadataTable.keySet();
	}

	@Override
	public Collection<Map<String, List<String>>> values() {
		return metadataTable.values();
	}

	@Override
	public Set<java.util.Map.Entry<Integer, Map<String, List<String>>>> entrySet() {
		return metadataTable.entrySet();
	}

}
