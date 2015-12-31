package net.crsr.ashurbanipal.sentiment;

import java.util.List;

import net.crsr.ashurbanipal.utility.Complex;

public class SentimentResult {

  public final int etext_no;
  public final List<Complex> scores;
  public final List<Complex> classes;
  
  public SentimentResult(int etext_no, List<Complex> scores, List<Complex> classes) {
    this.etext_no = etext_no;
    this.scores = scores;
    this.classes = classes;
  }

  @Override
  public String toString() {
    return "SentimentResult [etext_no=" + etext_no + ", scoreFreqs=" + scores + ", classFreqs=" + classes + "]";
  }
}
