package net.crsr.ashurbanipal.sentiment;

import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import net.crsr.ashurbanipal.utility.Complex;
import net.crsr.ashurbanipal.utility.FFT;

public abstract class SentimentProcessor {
  
  protected int etext_no;
  protected final List<Double> scoresValues = new ArrayList<>();
  protected final List<Double> classesValues = new ArrayList<>();

  public enum Processors {
    // English
    NRC,
    BING,
    AFINN,
    LINGPIPE,
    STANFORD,
  }
  
  public abstract void process(Integer etextNo, Reader text);

  public SentimentResult reduce() {
    final List<Complex> scores = FFT.fft(scoresValues);
    final List<Complex> classes = FFT.fft(classesValues);
    return new SentimentResult(etext_no, scores, classes);
  }
  
  public void clear(int etext_no) {
    this.etext_no = etext_no;
    scoresValues.clear();
    classesValues.clear();
  }
  
  public static SentimentProcessor getProcessorFor(String lang, Processors processor, File file) {
    switch (lang) {
      case "English":
        synchronized (SentimentProcessor.class) {
          switch (processor) {
            case NRC:
              return new net.crsr.ashurbanipal.sentiment.nrc.EnglishSentimentProcessor();
            case BING:
              return new net.crsr.ashurbanipal.sentiment.bing.EnglishSentimentProcessor();
            case AFINN:
              return new net.crsr.ashurbanipal.sentiment.afinn.EnglishSentimentProcessor();
            case LINGPIPE:
              return new net.crsr.ashurbanipal.sentiment.lingpipe.EnglishSentimentProcessor();
            case STANFORD:
              return new net.crsr.ashurbanipal.sentiment.stanford.EnglishSentimentProcessor();
          }
        }
      default:
        System.out.println("unknown language: " + lang + " for " + file.getAbsolutePath());
        return null;
    }
  }

}
