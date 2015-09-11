package net.crsr.ashurbanipal.tagger;

import java.util.Map;

public class TaggerResult {
  
  public final int etext_no;
  public final Map<String,Double> posData;
  public final Map<String,Map<String,Integer>> wordCounts;
  public final int nWords;
  
  public TaggerResult(Integer etextNo, Map<String,Double> posData, Map<String,Map<String,Integer>> wordCounts, int wordCount) {
    this.etext_no = etextNo;
    this.posData = posData;
    this.wordCounts = wordCounts;
    this.nWords = wordCount;
  }

}
