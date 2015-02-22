package net.crsr.ashurbanipal;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.crsr.ashurbanipal.store.FormatStore;
import net.crsr.ashurbanipal.store.MetadataStore;
import net.crsr.ashurbanipal.store.WordStore;
import net.crsr.ashurbanipal.utility.Pair;

import static net.crsr.ashurbanipal.LookupUtilites.*;

public class TopicLookup {

  public static void main(String[] args) {
    try {

      final int etextNo = Integer.parseInt(args[3]);

      final FormatStore formatStore = new FormatStore(args[0]);
      formatStore.read();
      final List<Pair<String,String>> formats = formatStore.get(etextNo);
      final Map<String,Integer> etextLookupMap = formatStore.asEtextNoLookupMap();

      final MetadataStore metadataStore = new MetadataStore(args[1]);
      metadataStore.read();
      final Map<String,List<String>> metadata = metadataStore.get(etextNo);

      final WordStore nounStore = new WordStore(args[2]);
      nounStore.read();

      final List<Map<String,Integer>> matches = matchingNouns(nounStore, formats);
      for (Map<String,Integer> match : matches) {
        printHeader();
        printMetadata(metadata);
        for (Pair<Double,String> neighbors : nearestNeighbors(nounStore, match)) {
          int neighborNo = etextLookupMap.get(neighbors.r);
          if (neighborNo != etextNo) {
            printMetadata(neighbors.l, metadataStore.get(neighborNo));
          }
        }
        System.out.println();
      }

    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("Usage: TopicLookup format-store metadata-store noun-store etext-no");
    } catch (IOException e) {
      throw new IOError(e);
    }
  }

  public static List<Map<String,Integer>> matchingNouns(WordStore nounStore, List<Pair<String,String>> formats) {
    final List<Map<String,Integer>> result = new ArrayList<>();
    for (Pair<String,String> format : formats) {
      if (format.l.contains("text/plain") && nounStore.containsKey(format.r)) {
        result.add(nounStore.get(format.r));
      }
    }
    return result;
  }

  public static List<Pair<Double,String>> nearestNeighbors(WordStore posStore, Map<String,Integer> initial) {
    final List<Pair<Double,String>> results = new ArrayList<>();

    for (Entry<String,Map<String,Integer>> entry : posStore.entrySet()) {
      results.add(Pair.pair(computeDistance(initial, entry.getValue()), entry.getKey()));
    }
    results.sort(new Comparator<Pair<Double,String>>() {
      @Override
      public int compare(Pair<Double,String> left, Pair<Double,String> right) {
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
