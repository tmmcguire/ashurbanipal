package net.crsr.ashurbanipal;

import java.io.IOError;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.crsr.ashurbanipal.store.MetadataStore;

public class DataCleaner {

  public static void main(String[] args) {
    try {

      cleanMetadata(args);

    } catch (IOException e) {
      throw new IOError(e);
    }
  }

  private static void cleanMetadata(String[] args) throws IOException {
    final MetadataStore metadataStore = new MetadataStore(args[0]);
    metadataStore.read();
    final MetadataStore cleanedMetadata = new MetadataStore("c-" + args[0]);
    for (Entry<Integer,Map<String,List<String>>> elt : metadataStore.entrySet()) {
      for (String lang : elt.getValue().get("language")) {
        if ("English".equalsIgnoreCase(lang)) {
          cleanedMetadata.put(elt.getKey(), elt.getValue());
        }
      }
    }
    cleanedMetadata.write();
  }

}
