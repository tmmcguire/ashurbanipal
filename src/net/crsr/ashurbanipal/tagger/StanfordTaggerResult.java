package net.crsr.ashurbanipal.tagger;

import java.util.Map;

import net.crsr.ashurbanipal.utility.Triple;

public class StanfordTaggerResult extends Triple<String,Map<String,Double>,Map<String,Map<String,Integer>>> {
  public StanfordTaggerResult(String a, Map<String,Double> b, Map<String,Map<String,Integer>> c) {
    super(a, b, c);
  }
}
