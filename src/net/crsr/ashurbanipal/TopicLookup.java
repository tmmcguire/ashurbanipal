package net.crsr.ashurbanipal;

import static net.crsr.ashurbanipal.utility.LookupUtilites.printHeader;
import static net.crsr.ashurbanipal.utility.LookupUtilites.printMetadata;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.crsr.ashurbanipal.store.MetadataStore;
import net.crsr.ashurbanipal.store.WordStore;
import net.crsr.ashurbanipal.utility.Pair;

public class TopicLookup {

  public static void main(String[] args) {
    try {

      final int etextNo = Integer.parseInt(args[2]);

      final MetadataStore metadataStore = new MetadataStore(args[0]);
      metadataStore.read();
      final WordStore nounStore = new WordStore(args[1]);
      nounStore.read();

      printHeader();
      printMetadata(metadataStore.get(etextNo));
      for (Pair<Double,Integer> neighbors : nearestNeighbors(nounStore, nounStore.get(etextNo))) {
        if (neighbors.r != etextNo) {
          printMetadata(neighbors.l, metadataStore.get(neighbors.r));
        }
        System.out.println();
      }

    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("Usage: TopicLookup metadata-store noun-store etext-no");
    } catch (IOException e) {
      throw new IOError(e);
    }
  }

  public static List<Pair<Double,Integer>> nearestNeighbors(WordStore posStore, Map<String,Integer> initial) {
    final List<Pair<Double,Integer>> results = new ArrayList<>();

    for (Entry<Integer,Map<String,Integer>> entry : posStore.entrySet()) {
      results.add(Pair.pair(computeDistance(initial, entry.getValue()), entry.getKey()));
    }
    results.sort(new Comparator<Pair<Double,Integer>>() {
      @Override
      public int compare(Pair<Double,Integer> left, Pair<Double,Integer> right) {
        return Double.compare(left.l, right.l);
      }
    });

    return results;
  }

  public static double computeDistance(Map<String,Integer> left, Map<String,Integer> right) {
    final Set<String> intersection = new HashSet<>(left.keySet());
    intersection.retainAll(right.keySet());
    return 1.0 - ((double) intersection.size()) / (((double) left.size()) + ((double) right.size()));
  }
}
