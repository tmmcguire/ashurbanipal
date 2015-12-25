package net.crsr.ashurbanipal.sentiment.afinn;

import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import net.crsr.ashurbanipal.sentiment.SentimentProcessor;
import net.crsr.ashurbanipal.sentiment.lingpipe.EnglishSentenceIterator;
import net.crsr.ashurbanipal.utility.IOUtilities;

public class EnglishSentimentProcessor extends SentimentProcessor {

  // Word list from: http://www2.imm.dtu.dk/pubdb/views/publication_details.php?id=6010
  //
  // See also: http://arxiv.org/pdf/1103.2903v1.pdf
  // or the Syuzhet package.
  
  private static final Map<String,Double> valence = new HashMap<>();
  
  static {
    BufferedReader br = null;
    try {
      br = new BufferedReader( new InputStreamReader( EnglishSentimentProcessor.class.getResourceAsStream("/net/crsr/ashurbanipal/sentiment/afinn/AFINN-111.txt") ) );
      String line = br.readLine();
      while (line != null) {
        final String[] values = line.split("\\t");
        valence.put(values[0], Double.valueOf(values[1]));
        line = br.readLine();
      }
    } catch (IOException e) {
      throw new IOError(e);
    } finally {
      if (br != null) { try { br.close(); } catch (IOException e) { } }
    }
  }
  
  @Override
  public void process(Integer etextNo, Reader text) {
    for (String sentence : new EnglishSentenceIterator( IOUtilities.readText(text)) ) {
      double score = 0.0;
      for (String word : sentence.split(" +")) {
        score += valence.getOrDefault(word, 0.0);
      }
      scoresValues.add(score);
      if (score < -1) {
        classesValues.add(-1.0);
      } else if (score > 1) {
        classesValues.add(1.0);
      } else {
        classesValues.add(0.0);
      }
    }
  }

}
