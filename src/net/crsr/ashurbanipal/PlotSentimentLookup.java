package net.crsr.ashurbanipal;

import static net.crsr.ashurbanipal.utility.LookupUtilites.printHeader;
import static net.crsr.ashurbanipal.utility.LookupUtilites.printMetadata;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import net.crsr.ashurbanipal.store.MetadataStore;
import net.crsr.ashurbanipal.store.PlotFrequencyStore;
import net.crsr.ashurbanipal.utility.Complex;
import net.crsr.ashurbanipal.utility.Pair;

public class PlotSentimentLookup {

  public static void main(String[] args) {
    try {

      final int etextNo = Integer.parseInt(args[2]);

      final MetadataStore metadataStore = new MetadataStore(args[0]);
      metadataStore.read();
      final PlotFrequencyStore frequencyStore = new PlotFrequencyStore(args[1]);
      frequencyStore.read();
      
      final List<Complex<Double>> match = frequencyStore.get(etextNo);
      printHeader();
      printMetadata(metadataStore.get(etextNo));
      for (Pair<Double,Integer> neighbors : nearestNeighbors(frequencyStore, match)) {
        if (neighbors.r != etextNo) {
          printMetadata(neighbors.l, metadataStore.get(neighbors.r));
        }
      }
      System.out.println();

    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("Usage: PlotSentimentLookup metadata-store plot-frequency-store etext-no");
    } catch (IOException e) {
      throw new IOError(e);
    }

  }

  public static List<Pair<Double,Integer>> nearestNeighbors(PlotFrequencyStore posStore, List<Complex<Double>> initial) {
    final List<Pair<Double,Integer>> results = new ArrayList<>();
    for (Entry<Integer,List<Complex<Double>>> entry : posStore.entrySet()) {
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

  public static double computeDistance(List<Complex<Double>> left, List<Complex<Double>> right) {
    double distance = 0.0;
    final int length = Math.min(left.size(), right.size());
    for (int i = 0; i < length; ++i) {
      final Complex<Double> l = left.get(i);
      final Complex<Double> r = right.get(i);
      distance += l.distance(r) * Math.pow(0.99, i);
    }
    return distance;
  }

}
