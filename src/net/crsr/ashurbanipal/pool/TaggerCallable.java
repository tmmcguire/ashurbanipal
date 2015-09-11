package net.crsr.ashurbanipal.pool;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import net.crsr.ashurbanipal.reader.FragmentingReader;
import net.crsr.ashurbanipal.reader.GutenbergLicenseReader;
import net.crsr.ashurbanipal.reader.ZippedTextFileReader;
import net.crsr.ashurbanipal.tagger.Tagger;
import net.crsr.ashurbanipal.tagger.TaggerResult;

public class TaggerCallable implements Callable<TaggerResult> {

  private static final ThreadLocal<Map<String,Integer>> threadTaggerUseCount = ThreadLocal.withInitial(new Supplier<Map<String,Integer>>() {
    @Override public Map<String,Integer> get() { return new HashMap<>(); }
  });

  private static final ThreadLocal<Map<String,Tagger>> threadTagger = ThreadLocal.withInitial(new Supplier<Map<String,Tagger>>() {
    @Override public Map<String,Tagger> get() { return new HashMap<>(); }
  });

  private final int etextNo;
  private final File file;
  private final String lang;

  public TaggerCallable(int etextNo, String lang, File file) {
    this.etextNo = etextNo;
    this.file = file;
    this.lang = lang;
  }

  @Override
  public TaggerResult call() throws Exception {
    FragmentingReader fragments = null;
    try {

      Tagger tagger = threadTagger.get().get(lang);
      Integer useCount = threadTaggerUseCount.get().get(lang);
      if (useCount == null || tagger == null || useCount > 32) {
        tagger = Tagger.getTaggerFor(lang, file);
        if (tagger == null) {
          return null;
        }
        threadTagger.get().put(lang, tagger);
        useCount = 0;
      }
      threadTaggerUseCount.get().put(lang, useCount+1);

      // Process text in fragments, then coalesce data from fragments.
      fragments = new FragmentingReader(new GutenbergLicenseReader(new ZippedTextFileReader(file)), 10240);
      int nWords = 0;
      final Map<String,Double> posData = new HashMap<>();
      final Map<String,Map<String,Integer>> wordBags = new HashMap<>();
      while (fragments.hasFragments()) {
        final TaggerResult taggerResult = tagger.process(etextNo, fragments.nextFragment());
        // Total number of words.
        nWords += taggerResult.nWords;
        // Total POS data.
        for (Entry<String,Double> entry : taggerResult.posData.entrySet()) {
          posData.put(entry.getKey(), posData.getOrDefault(entry.getKey(), 0.0) + entry.getValue());
        }
        // Total word collections.
        for (Entry<String,Map<String,Integer>> entry : taggerResult.wordCounts.entrySet()) {
          Map<String,Integer> wordCounts = wordBags.get(entry.getKey());
          if (wordCounts == null) {
            wordCounts = new TreeMap<>();
            wordBags.put(entry.getKey(), wordCounts);
          }
          for (Entry<String,Integer> subEntry : entry.getValue().entrySet()) {
            wordCounts.put(subEntry.getKey(), wordCounts.getOrDefault(subEntry.getKey(), 0) + subEntry.getValue());
          }
        }
      }
      return new TaggerResult(etextNo, tagProportions(nWords, posData), wordBags, nWords);
      
    } catch (ZippedTextFileReader.Exception e) {
      throw new Exception("error reading text " + etextNo + ": " + file.getAbsolutePath(), e);
    } catch (IOException e) {
      throw new Exception("error reading text " + etextNo + ": " + file.getAbsolutePath(), e);
    } finally {
      if (fragments != null) { try { fragments.close(); } catch (Throwable t) { } }
    }
  }

  private static Map<String,Double> tagProportions(int words, Map<String,Double> result) {
    for (Entry<String,Double> entry : result.entrySet()) {
      entry.setValue(entry.getValue() / words);
    }
    return result;
  }
  
  @SuppressWarnings("serial")
  public static class Exception extends java.lang.Exception {
    public Exception(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
