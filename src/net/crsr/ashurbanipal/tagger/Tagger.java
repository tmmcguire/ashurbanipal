package net.crsr.ashurbanipal.tagger;

import java.io.File;
import java.io.Reader;

public abstract class Tagger {

  public abstract TaggerResult process(Integer etextNo, Reader text);
  
  public static Tagger getTaggerFor(String lang, File file) {
    switch (lang) {
      case "English":
        return new EnglishTagger();
      default:
        System.out.println("unknown language: " + lang + " for " + file.getAbsolutePath());
        return null;
    }
  }

}