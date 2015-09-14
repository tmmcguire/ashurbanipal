package net.crsr.ashurbanipal;

import static net.crsr.ashurbanipal.utility.LookupUtilites.printHeader;
import static net.crsr.ashurbanipal.utility.LookupUtilites.printMetadata;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.crsr.ashurbanipal.store.MetadataStore;
import net.crsr.ashurbanipal.store.PosStore;
import net.crsr.ashurbanipal.store.WordStore;
import net.crsr.ashurbanipal.utility.Pair;

public class CombinedLookup {

  public static void main(String[] args) {
    try {

      final int etextNo = Integer.parseInt(args[3]);

      final MetadataStore metadataStore = new MetadataStore(args[0]);
      metadataStore.read();
      final Map<String,List<String>> metadata = metadataStore.get(etextNo);

      final WordStore nounStore = new WordStore(args[1]);
      nounStore.read();

      final PosStore posStore = new PosStore(args[2]);
      posStore.read();

      Map<String,Double> pos = posStore.get(etextNo);
      Map<String,Integer> nouns = nounStore.get(etextNo);

      printHeader();
      printMetadata(metadata);
      for (Pair<Double,Integer> neighbors : nearestNeighbors(posStore, nounStore, pos, nouns)) {
        if (neighbors.r != etextNo) {
          printMetadata(neighbors.l, metadataStore.get(neighbors.r));
        }
      }
      System.out.println();

    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("Usage: StyleLookup metadata-store noun-store pos-store etext-no");
    } catch (IOException e) {
      throw new IOError(e);
    }
  }

  public static List<Pair<Double,Integer>> nearestNeighbors(PosStore posStore, WordStore nounStore, Map<String,Double> pos, Map<String,Integer> nouns) {
    final Map<Integer,Double> style = new HashMap<>();
    for (Pair<Double,Integer> elt : StyleLookup.nearestNeighbors(posStore, pos)) {
      style.put(elt.r, elt.l);
    }
    final List<Pair<Double,Integer>> results = new ArrayList<>();
    final List<Pair<Double,Integer>> topic = TopicLookup.nearestNeighbors(nounStore, nouns);
    for (Pair<Double,Integer> elt : topic) {
      final Double styleDistance = style.get(elt.r);
      if (styleDistance != null) {
        // Drastically enhance the topic power to get the two whiskey books close together.
        // results.add(Pair.pair(Math.pow(elt.l, 6) * styleDistance, elt.r));
        results.add(Pair.pair(elt.l * styleDistance, elt.r));
      } else {
        System.err.format("%s: style information not found\n", elt.r);
      }
    }
    results.sort(new Comparator<Pair<Double,Integer>>() {
      @Override
      public int compare(Pair<Double,Integer> left, Pair<Double,Integer> right) {
        return Double.compare(left.l, right.l);
      }
    });
    return results;
  }
}
