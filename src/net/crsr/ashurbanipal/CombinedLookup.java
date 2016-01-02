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
import net.crsr.ashurbanipal.store.PlotFrequencyStore;
import net.crsr.ashurbanipal.store.PosStore;
import net.crsr.ashurbanipal.store.WordStore;
import net.crsr.ashurbanipal.utility.Pair;

public class CombinedLookup {

  public static void main(String[] args) {
    try {

      final int etextNo = Integer.parseInt(args[4]);

      final MetadataStore metadataStore = new MetadataStore(args[0]);
      metadataStore.read();
      final Map<String,List<String>> metadata = metadataStore.get(etextNo);

      final WordStore nounStore = new WordStore(args[1]);
      nounStore.read();

      final PosStore posStore = new PosStore(args[2]);
      posStore.read();
      
      final PlotFrequencyStore sentimentStore = new PlotFrequencyStore(args[3]);
      sentimentStore.read();

      printHeader();
      printMetadata(metadata);
      for (Pair<Double,Integer> neighbors : nearestNeighbors(posStore, nounStore, sentimentStore, etextNo)) {
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

  public static List<Pair<Double,Integer>> nearestNeighbors(PosStore posStore, WordStore nounStore, PlotFrequencyStore sentimentStore, int etextNo) {
    final Map<Integer,Double> style = computeStyleMap(posStore, etextNo);
    final Map<Integer,Double> sentiments = computeSentimentMap(sentimentStore, etextNo);
    final List<Pair<Double,Integer>> topic = TopicLookup.nearestNeighbors(nounStore, etextNo);
    final List<Pair<Double,Integer>> results = new ArrayList<>();
    for (Pair<Double,Integer> elt : topic) {
      final Double styleDistance = style.get(elt.r);
      final Double sentimentDistance = sentiments.get(elt.r);
      double distance = elt.l;
      if (styleDistance != null) {
        distance *= styleDistance;
      } else {
        System.err.format("%s: style information not found\n", elt.r);
      }
      if (sentimentDistance != null) {
        distance *= sentimentDistance;
      } else {
        // System.err.format("%s: sentiment information not found\n", elt.r);
      }
      results.add(Pair.pair(distance, elt.r));
    }
    results.sort(new Comparator<Pair<Double,Integer>>() {
      @Override
      public int compare(Pair<Double,Integer> left, Pair<Double,Integer> right) {
        return Double.compare(left.l, right.l);
      }
    });
    return results;
  }

  private static Map<Integer,Double> computeStyleMap(PosStore posStore, int etextNo) {
    final List<Pair<Double,Integer>> distances = StyleLookup.nearestNeighbors(posStore, etextNo);
    final Pair<Double,Double> minMax = minMaxDistance(distances);
    final Map<Integer,Double> style = new HashMap<>();
    for (Pair<Double,Integer> elt : distances) {
      style.put(elt.r, scale(minMax, elt.l));
    }
    return style;
  }

  private static Map<Integer,Double> computeSentimentMap(PlotFrequencyStore frequencyStore, int etextNo) {
    final List<Pair<Double,Integer>> distances = PlotSentimentLookup.nearestNeighbors(frequencyStore, etextNo);
    final Pair<Double,Double> minMax = minMaxDistance(distances);
    final Map<Integer,Double> sentimentMap = new HashMap<>();
    for (Pair<Double,Integer> elt : distances) {
      sentimentMap.put(elt.r, scale(minMax, elt.l));
    }
    return sentimentMap;
  }
  
  private static Pair<Double,Double> minMaxDistance(List<Pair<Double,Integer>> distances) {
    double min = Double.POSITIVE_INFINITY;
    double max = Double.NEGATIVE_INFINITY;
    for (Pair<Double,Integer> distance : distances) {
      if (distance.l < min) { min = distance.l; }
      if (distance.l > max) { max = distance.l; }
    }
    return new Pair<>(min,max);
  }
  
  private static double scale(Pair<Double,Double> minMax, double value) {
    double range = minMax.r - minMax.l;
    return (value - minMax.l) / range;
  }
}
