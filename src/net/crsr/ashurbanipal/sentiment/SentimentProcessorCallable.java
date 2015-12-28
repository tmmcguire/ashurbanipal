package net.crsr.ashurbanipal.sentiment;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import net.crsr.ashurbanipal.reader.FragmentingReader;
import net.crsr.ashurbanipal.reader.GutenbergLicenseReader;
import net.crsr.ashurbanipal.reader.ZippedTextFileReader;
import net.crsr.ashurbanipal.sentiment.SentimentProcessor.Processors;

public class SentimentProcessorCallable implements Callable<SentimentResult> {

  private static final ThreadLocal<Map<String,Integer>> threadProcessorUseCount = ThreadLocal.withInitial(new Supplier<Map<String,Integer>>() {
    @Override public Map<String,Integer> get() { return new HashMap<>(); }
  });

  private static final ThreadLocal<Map<String,SentimentProcessor>> threadProcessor = ThreadLocal.withInitial(new Supplier<Map<String,SentimentProcessor>>() {
    @Override public Map<String,SentimentProcessor> get() { return new HashMap<>(); }
  });

  private final int etext_no;
  private final File file;
  private final String language;
  private final Processors processor;

  public SentimentProcessorCallable(int etext_no, String language, File file) {
    this.etext_no = etext_no;
    this.file = file;
    this.language = language;
    this.processor = Processors.LINGPIPE;
  }

  public SentimentProcessorCallable(int etext_no, String language, File file, Processors processor) {
    this.etext_no = etext_no;
    this.file = file;
    this.language = language;
    this.processor = processor;
  }
  
  @Override
  public SentimentResult call() throws Exception {
    System.out.println("processing " + etext_no);
    FragmentingReader fragments = null;
    try {
      SentimentProcessor sentimentProcessor = threadProcessor.get().get(language);
      Integer useCount = threadProcessorUseCount.get().get(language);
      if (useCount == null || sentimentProcessor == null || useCount > 32) {
        sentimentProcessor = SentimentProcessor.getProcessorFor(language, processor, file);
        if (sentimentProcessor == null) {
          return null;
        }
        threadProcessor.get().put(language, sentimentProcessor);
        useCount = 0;
      }
      threadProcessorUseCount.get().put(language, useCount+1);

      // Process text in fragments, then coalesce data from fragments.
      sentimentProcessor.clear(etext_no);
      fragments = new FragmentingReader(new GutenbergLicenseReader(new ZippedTextFileReader(file)), 10240);
      while (fragments.hasFragments()) {
        sentimentProcessor.process(etext_no, fragments.nextFragment());
      }
      return sentimentProcessor.reduce();
      
    } finally {
      if (fragments != null) { try { fragments.close(); } catch (Throwable t) { } }
    }
  }

}
