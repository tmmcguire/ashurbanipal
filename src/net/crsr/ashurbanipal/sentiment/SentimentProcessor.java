package net.crsr.ashurbanipal.sentiment;

import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import net.crsr.ashurbanipal.utility.CollectionsUtilities;
import net.crsr.ashurbanipal.utility.Complex;

import org.jtransforms.fft.DoubleFFT_1D;

public abstract class SentimentProcessor {
  
  protected int etext_no;
  protected final List<Double> scoresValues = new ArrayList<>();
  protected final List<Double> classesValues = new ArrayList<>();
  
  public abstract void process(Integer etextNo, Reader text);
  
  public SentimentResult reduce() {
    final double[] scores = CollectionsUtilities.asArray(scoresValues, 2);
    final double[] classes = CollectionsUtilities.asArray(classesValues, 2);
    final DoubleFFT_1D fft = new DoubleFFT_1D(scores.length);
    fft.realForward(scores);
    fft.realForward(classes);
    return new SentimentResult(etext_no, toComplexList(scores), toComplexList(classes));
  }
  
  /**
   * Convert an array of doubles in the format produced by {@link DoubleFFT_1D#realForward(double[])} into a list of Complex<Double>.
   * 
   * The format of the input array should be:
   * <pre>
   *  a[2*k]   = Re[k], 0<=k<n/2
   *  a[2*k+1] = Im[k], 0<k<n/2
   *  a[1]     = Re[n/2]
   * </pre>
   * 
   * @param values Array of doubles, in the format produced by {@link DoubleFFT_1D#realForward(double[])}.
   * @return List of Complex<Double>.
   */
  private List<Complex<Double>> toComplexList(double[] values) {
    final List<Complex<Double>> result = new ArrayList<>();
    final int boundary = values.length / 2;
    result.add( new Complex<>(values[0], 0.0) );
    for (int k = 1; k < boundary; ++k) {
      result.add( new Complex<>(values[2*k], values[2*k + 1]) );
    }
    result.add( new Complex<>(values[boundary], 0.0) );
    return result;
  }
  
  public void clear(int etext_no) {
    this.etext_no = etext_no;
    scoresValues.clear();
    classesValues.clear();
  }
  
  public static SentimentProcessor getProcessorFor(String lang, File file) {
    switch (lang) {
      case "English":
        synchronized (SentimentProcessor.class) {
          return new net.crsr.ashurbanipal.sentiment.afinn.EnglishSentimentProcessor();
          // return new net.crsr.ashurbanipal.sentiment.lingpipe.EnglishSentimentProcessor();
          // return new net.crsr.ashurbanipal.sentiment.stanford.EnglishSentimentProcessor();
        }
      default:
        System.out.println("unknown language: " + lang + " for " + file.getAbsolutePath());
        return null;
    }
  }

}
