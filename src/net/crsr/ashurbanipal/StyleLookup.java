package net.crsr.ashurbanipal;

import static net.crsr.ashurbanipal.utility.LookupUtilites.printHeader;
import static net.crsr.ashurbanipal.utility.LookupUtilites.printMetadata;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.crsr.ashurbanipal.store.MetadataStore;
import net.crsr.ashurbanipal.store.PosStore;
import net.crsr.ashurbanipal.utility.Pair;

public class StyleLookup {

  public static void main(String[] args) {
    try {

      final int etextNo = Integer.parseInt(args[3]);

      final MetadataStore metadataStore = new MetadataStore(args[1]);
      metadataStore.read();
      final PosStore posStore = new PosStore(args[2]);
      posStore.read();
      
      final Map<String,Double> match = posStore.get(etextNo);
      printHeader();
      printMetadata(metadataStore.get(etextNo));
      for (Pair<Double,Integer> neighbors : nearestNeighbors(posStore, match)) {
        if (neighbors.r != etextNo) {
          printMetadata(neighbors.l, metadataStore.get(neighbors.r));
        }
      }
      System.out.println();

    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("Usage: StyleLookup format-store metadata-store pos-store etext-no");
    } catch (IOException e) {
      throw new IOError(e);
    }

  }

  public static List<Pair<Double,Integer>> nearestNeighbors(PosStore posStore, Map<String,Double> initial) {
    final List<Pair<Double,Integer>> results = new ArrayList<>();
    for (Entry<Integer,Map<String,Double>> entry : posStore.entrySet()) {
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

  public static double computeDistance(Map<String,Double> left, Map<String,Double> right) {
    double distance = 0.0;
    for (String key : left.keySet()) {
      distance += Math.pow(left.get(key) - right.get(key), 2.0);
    }
    return Math.sqrt(distance);
  }
}
