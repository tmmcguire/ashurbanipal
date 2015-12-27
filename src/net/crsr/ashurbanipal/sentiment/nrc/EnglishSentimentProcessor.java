package net.crsr.ashurbanipal.sentiment.nrc;

import net.crsr.ashurbanipal.sentiment.LexiconSentimentProcessor;

public class EnglishSentimentProcessor extends LexiconSentimentProcessor {

  // See http://saifmohammad.com/WebPages/lexicons.html
  //
  // Or the Syuzhet package.
  
  public EnglishSentimentProcessor() {
    super("/net/crsr/ashurbanipal/sentiment/nrc/nrc.txt");
  }

}
