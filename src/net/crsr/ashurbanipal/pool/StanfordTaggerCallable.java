package net.crsr.ashurbanipal.pool;

import java.io.File;
import java.io.Reader;
import java.util.concurrent.Callable;

import net.crsr.ashurbanipal.language.LanguageDetectorFactory;
import net.crsr.ashurbanipal.reader.GutenbergLicenseReader;
import net.crsr.ashurbanipal.reader.ZippedTextFileReader;
import net.crsr.ashurbanipal.tagger.StanfordTagger;
import net.crsr.ashurbanipal.tagger.StanfordTaggerResult;
import net.crsr.ashurbanipal.utility.Pair;

import com.cybozu.labs.langdetect.Detector;

public class StanfordTaggerCallable implements Callable<StanfordTaggerResult> {

  private static final ThreadLocal<Pair<StanfordTagger,Integer>> threadTagger = new ThreadLocal<>();

  private final File file;

  public StanfordTaggerCallable(File file) {
    this.file = file;
  }

  @Override
  public StanfordTaggerResult call() throws Exception {
    Reader text = null;
    try {
      text = new GutenbergLicenseReader(new ZippedTextFileReader(file));

      // skip into the text to avoid PG header, if necessary
      long skipped = text.skip(1 * 1024);
      if (skipped < 1 * 1024) { /* file too short */
        return null;
      }
      final Detector detector = LanguageDetectorFactory.create();
      detector.append(text);
      final String lang = detector.detect();
      // reset text and tag
      text.reset();
      switch (lang) {
        case "en": {
          Pair<StanfordTagger,Integer> p = threadTagger.get();
          if (p == null || p.r > 32) {
            p = Pair.pair(new StanfordTagger(), 0);
          } else {
            p = Pair.pair(p.l, p.r + 1);
          }
          threadTagger.set(p);
          return p.l.process(file.getAbsolutePath(), text);
        }
        default: {
          System.out.println("unknown language: " + lang + " for " + file.getAbsolutePath());
          return null;
        }
      }
    } finally {
      if (text != null) {
        text.close();
      }
    }
  }
}
