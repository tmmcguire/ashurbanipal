package net.crsr.ashurbanipal.graphics;

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
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class PlotSentimentGraphForTexts {

  public static void main(String[] args) {
    try {
      final MetadataStore metadataStore = new MetadataStore(args[0]);
      metadataStore.read();
      final PlotFrequencyStore frequencyStore = new PlotFrequencyStore(args[1]);
      frequencyStore.read();

      final XYSeriesCollection dataset = new XYSeriesCollection();
      
      for (int i = 2; i < args.length; ++i) {
        final int etextNo = Integer.parseInt(args[i]);

        final List<Double> data = FFT.inverseFFT( frequencyStore.get(etextNo), 100, 3 );

        final XYSeries dataSeries = new XYSeries(metadataStore.get(etextNo).get("title").get(0));
        for (int j = 0; j < data.size(); ++j) {
          dataSeries.add(j, data.get(j));
        }
        dataset.addSeries(dataSeries);
      }
      
      JFreeChart chart = ChartFactory.createXYLineChart(frequencyStore.file().getName() + " Comparison", "Time", "Sentiment", dataset);
      ChartUtilities.saveChartAsPNG(new File(frequencyStore.file().getName() + ".png"), chart, 600, 300);
      
    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("Usage: PlotSentimentGraphForTexts metadata-store plot-frequency-store etext-no...");
    } catch (IOException e) {
      throw new IOError(e);
    }

  }
}
