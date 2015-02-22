package net.crsr.ashurbanipal;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.crsr.ashurbanipal.store.FormatStore;
import net.crsr.ashurbanipal.store.MetadataStore;
import net.crsr.ashurbanipal.store.PosStore;
import net.crsr.ashurbanipal.store.WordStore;
import net.crsr.ashurbanipal.utility.Pair;
import static net.crsr.ashurbanipal.LookupUtilites.*;

public class CombinedLookup {

  public static void main(String[] args) {
    try {

      final int etextNo = Integer.parseInt(args[4]);

      final FormatStore formatStore = new FormatStore(args[0]);
      formatStore.read();
      final List<Pair<String,String>> formats = formatStore.get(etextNo);
      final Map<String,Integer> etextLookupMap = formatStore.asEtextNoLookupMap();

      final MetadataStore metadataStore = new MetadataStore(args[1]);
      metadataStore.read();
      final Map<String,List<String>> metadata = metadataStore.get(etextNo);

      final WordStore nounStore = new WordStore(args[2]);
      nounStore.read();

      final PosStore posStore = new PosStore(args[3]);
      posStore.read();

      final List<Pair<Map<String,Double>,Map<String,Integer>>> matches = matchingPosAndNouns(posStore, nounStore, formats);
      for (Pair<Map<String,Double>,Map<String,Integer>> match : matches) {
        printHeader();
        printMetadata(metadata);
        for (Pair<Double,String> neighbors : nearestNeighbors(posStore, nounStore, match)) {
          int neighborNo = etextLookupMap.get(neighbors.r);
          if (neighborNo != etextNo) {
            printMetadata(neighbors.l, metadataStore.get(neighborNo));
          }
        }
        System.out.println();
      }

    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("Usage: StyleLookup format-store metadata-store noun-store pos-store etext-no");
    } catch (IOException e) {
      throw new IOError(e);
    }
  }

  public static List<Pair<Map<String,Double>,Map<String,Integer>>> matchingPosAndNouns(PosStore posStore, WordStore nouns,
      List<Pair<String,String>> formats) {
    final List<Pair<Map<String,Double>,Map<String,Integer>>> result = new ArrayList<>();
    for (Pair<String,String> format : formats) {
      if (format.l.contains("text/plain") && posStore.containsKey(format.r) && nouns.containsKey(format.r)) {
        final Map<String,Double> posMap = posStore.get(format.r);
        final Map<String,Integer> nounMap = nouns.get(format.r);
        result.add(Pair.pair(posMap, nounMap));
      }
    }
    return result;
  }

  public static List<Pair<Double,String>> nearestNeighbors(PosStore posStore, WordStore nounStore,
      Pair<Map<String,Double>,Map<String,Integer>> initial) {
    final Map<String,Double> style = new HashMap<>();
    for (Pair<Double,String> elt : StyleLookup.nearestNeighbors(posStore, initial.l)) {
      style.put(elt.r, elt.l);
    }
    final List<Pair<Double,String>> results = new ArrayList<>();
    final List<Pair<Double,String>> topic = TopicLookup.nearestNeighbors(nounStore, initial.r);
    for (Pair<Double,String> elt : topic) {
      final Double styleDistance = style.get(elt.r);
      if (styleDistance != null) {
        // Drastically enhance the topic power to get the two whiskey books close together.
        results.add(Pair.pair(Math.pow(elt.l, 6) * styleDistance, elt.r));
      } else {
        System.err.format("%s: style information not found\n", elt.r);
      }
    }
    results.sort(new Comparator<Pair<Double,String>>() {
      @Override
      public int compare(Pair<Double,String> left, Pair<Double,String> right) {
        return Double.compare(left.l, right.l);
      }
    });
    return results;
  }
}
