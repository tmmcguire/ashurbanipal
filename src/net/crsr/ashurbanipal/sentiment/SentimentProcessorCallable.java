package net.crsr.ashurbanipal.sentiment;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import net.crsr.ashurbanipal.reader.FragmentingReader;
import net.crsr.ashurbanipal.reader.GutenbergLicenseReader;
import net.crsr.ashurbanipal.reader.ZippedTextFileReader;

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

  public SentimentProcessorCallable(int etext_no, String language, File file) {
    this.etext_no = etext_no;
    this.file = file;
    this.language = language;
  }
  
  @Override
  public SentimentResult call() throws Exception {
    System.out.println("processing" + etext_no);
    FragmentingReader fragments = null;
    try {
      SentimentProcessor processor = threadProcessor.get().get(language);
      Integer useCount = threadProcessorUseCount.get().get(language);
      if (useCount == null || processor == null || useCount > 32) {
        processor = SentimentProcessor.getProcessorFor(language, file);
        if (processor == null) {
          return null;
        }
        threadProcessor.get().put(language, processor);
        useCount = 0;
      }
      threadProcessorUseCount.get().put(language, useCount+1);

      // Process text in fragments, then coalesce data from fragments.
      processor.clear(etext_no);
      fragments = new FragmentingReader(new GutenbergLicenseReader(new ZippedTextFileReader(file)), 10240);
      while (fragments.hasFragments()) {
        processor.process(etext_no, fragments.nextFragment());
      }
      return processor.reduce();
      
    } finally {
      if (fragments != null) { try { fragments.close(); } catch (Throwable t) { } }
    }
  }

}
