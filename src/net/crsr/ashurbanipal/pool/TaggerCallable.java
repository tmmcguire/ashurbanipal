package net.crsr.ashurbanipal.pool;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

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
    Reader text = null;
    try {

      text = new GutenbergLicenseReader(new ZippedTextFileReader(file));
      // reset text and tag
      text.reset();
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
      System.out.println("Processing " + etextNo);
      return tagger.process(etextNo, text);
      
    } catch (ZippedTextFileReader.Exception e) {
      throw new Exception("error reading text " + etextNo + ": " + file.getAbsolutePath(), e);
    } catch (IOException e) {
      throw new Exception("error reading text " + etextNo + ": " + file.getAbsolutePath(), e);
    } finally {
      if (text != null) { try { text.close(); } catch (Throwable t) { } }
    }
  }
  
  @SuppressWarnings("serial")
  public static class Exception extends java.lang.Exception {
    public Exception(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
