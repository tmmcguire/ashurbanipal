package net.crsr.ashurbanipal;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.util.List;

import net.crsr.ashurbanipal.store.MetadataStore;
import net.crsr.ashurbanipal.store.PlotFrequencyStore;
import net.crsr.ashurbanipal.utility.FFT;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class PlotSentimentGraph {

  public static void main(String[] args) {
    try {
      final MetadataStore metadataStore = new MetadataStore(args[0]);
      metadataStore.read();
      final int etextNo = Integer.parseInt(args[1]);

      final String filename = args[2];
      final PlotFrequencyStore frequencyStore = new PlotFrequencyStore(filename);
      frequencyStore.read();
      
      List<Double> data = FFT.inverseFFT( frequencyStore.get(etextNo), 100 );
      
      final XYSeries dataSeries = new XYSeries(filename);
      for (int i = 0; i < data.size(); ++i) {
        dataSeries.add(i, data.get(i));
      }
      final XYDataset dataset = new XYSeriesCollection(dataSeries);
      JFreeChart chart = ChartFactory.createXYLineChart(filename, "Time", "Valence", dataset);
      ChartUtilities.saveChartAsPNG(new File("1.png"), chart, 600, 300);
      
    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("Usage: PlotSentimentGraph metadata-store etext-no plot-frequency-store...");
    } catch (IOException e) {
      throw new IOError(e);
    }

  }
}
