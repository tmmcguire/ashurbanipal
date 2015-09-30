package net.crsr.ashurbanipal;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import net.crsr.ashurbanipal.store.MetadataStore;
import net.crsr.ashurbanipal.store.PosStore;
import net.crsr.ashurbanipal.store.WordStore;

public class EnforceConguence {

  public static void main(String[] args) {

    try {
      
      final String metadataFile = args[0];
      final String nounFile = args[1];
      final String posFile = args[2];

      final MetadataStore metadataStore = new MetadataStore(metadataFile);
      metadataStore.read();
      final WordStore nounStore = new WordStore(nounFile);
      nounStore.read();
      final PosStore posStore = new PosStore(posFile);
      posStore.read();
      
      final Set<Integer> etext_nos = new TreeSet<>(metadataStore.keySet());
      etext_nos.retainAll(nounStore.keySet());
      etext_nos.retainAll(posStore.keySet());

      new File("clean").mkdir();
      final String cleanMetadataFile = "clean/" + metadataFile;
      final String cleanNounFile = "clean/" + nounFile;
      final String cleanPosFile = "clean/" + posFile;
      
      final MetadataStore cleanMetadataStore = new MetadataStore(cleanMetadataFile);
      final WordStore cleanNounStore = new WordStore(cleanNounFile);
      final PosStore cleanPosStore = new PosStore(cleanPosFile);
      
      for (Integer etext_no : etext_nos) {
        cleanMetadataStore.put(etext_no, metadataStore.get(etext_no));
        cleanNounStore.put(etext_no, nounStore.get(etext_no));
        cleanPosStore.put(etext_no, posStore.get(etext_no));
      }
      
      cleanMetadataStore.write();
      cleanNounStore.write();
      cleanPosStore.write();

    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("Usage: EnforceCongruence metadata-store noun-store pos-store");
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

}
