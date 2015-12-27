package net.crsr.ashurbanipal.sentiment.bing;

import net.crsr.ashurbanipal.sentiment.LexiconSentimentProcessor;

public class EnglishSentimentProcessor extends LexiconSentimentProcessor {

  // Word list from:
  // https://www.cs.uic.edu/~liub/FBS/sentiment-analysis.html#lexicon
  //
  // See also the Syuzhet package.
  
  public EnglishSentimentProcessor() {
    super("/net/crsr/ashurbanipal/sentiment/bing/bing-words.txt");
  }

}
