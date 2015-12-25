package net.crsr.ashurbanipal.sentiment.afinn;

import net.crsr.ashurbanipal.sentiment.LexiconSentimentProcessor;

public class EnglishSentimentProcessor extends LexiconSentimentProcessor {

  // Word list from: http://www2.imm.dtu.dk/pubdb/views/publication_details.php?id=6010
  //
  // See also: http://arxiv.org/pdf/1103.2903v1.pdf
  // or the Syuzhet package.

  public EnglishSentimentProcessor() {
    super("/net/crsr/ashurbanipal/sentiment/afinn/AFINN-111.txt");
  }
}
