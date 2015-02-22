package net.crsr.ashurbanipal;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.crsr.ashurbanipal.store.FormatStore;
import net.crsr.ashurbanipal.store.MetadataStore;
import net.crsr.ashurbanipal.store.PosStore;
import net.crsr.ashurbanipal.utility.Pair;

import static net.crsr.ashurbanipal.LookupUtilites.*;

public class StyleLookup {

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

      final PosStore posStore = new PosStore(args[2]);
      posStore.read();
      final List<Map<String,Double>> matches = matchingPos(posStore, formats);
      for (Map<String,Double> match : matches) {
        printHeader();
        printMetadata(metadata);
        for (Pair<Double,String> neighbors : nearestNeighbors(posStore, match).subList(0, 10)) {
          int neighborNo = etextLookupMap.get(neighbors.r);
          if (neighborNo != etextNo) {
            printMetadata(neighbors.l, metadataStore.get(neighborNo));
          }
        }
        System.out.println();
      }

    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("Usage: StyleLookup format-store metadata-store pos-store etext-no");
    } catch (IOException e) {
      throw new IOError(e);
    }

  }

  public static List<Map<String,Double>> matchingPos(PosStore posStore, List<Pair<String,String>> formats) {
    final List<Map<String,Double>> result = new ArrayList<>();
    for (Pair<String,String> format : formats) {
      if (format.l.contains("text/plain") && posStore.containsKey(format.r)) {
        result.add(posStore.get(format.r));
      }
    }
    return result;
  }

  public static List<Pair<Double,String>> nearestNeighbors(PosStore posStore, Map<String,Double> initial) {
    final List<Pair<Double,String>> results = new ArrayList<>();

    for (Entry<String,Map<String,Double>> entry : posStore.entrySet()) {
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

  public static double computeDistance(Map<String,Double> left, Map<String,Double> right) {
    double distance = 0.0;
    for (String key : left.keySet()) {
      distance += Math.pow(left.get(key) - right.get(key), 2.0);
    }
    return Math.sqrt(distance);
  }
}
