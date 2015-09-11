package net.crsr.ashurbanipal.utility;

import java.util.List;
import java.util.Map;

public class LookupUtilites {

  public static void printHeader() {
    System.out.format("%10s %10s  %50s  %30s\n", "distance", "etext_no", "title", "author");
  }

  public static void printMetadata(Map<String,List<String>> md) {
    System.out.format("           %10s  %50s  %30s\n", combine(md.get("etext_no")), combine(md.get("title")), combine(md.get("author")));
  }

  public static void printMetadata(Double distance, Map<String,List<String>> md) {
    System.out.format("%10f %10s  %50s  %30s\n", distance, combine(md.get("etext_no")), combine(md.get("title")), combine(md.get("author")));
  }

  public static String combine(List<String> strings) {
    return String.join("; ", strings);
  }

}
