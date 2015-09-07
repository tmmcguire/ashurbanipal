package net.crsr.ashurbanipal.tagger;

import java.util.Map;

import net.crsr.ashurbanipal.utility.Triple;

public class TaggerResult extends Triple<Integer,Map<String,Double>,Map<String,Map<String,Integer>>> {
  
  public TaggerResult(Integer etextNo, Map<String,Double> posData, Map<String,Map<String,Integer>> wordCounts) {
    super(etextNo, posData, wordCounts);
  }

}
