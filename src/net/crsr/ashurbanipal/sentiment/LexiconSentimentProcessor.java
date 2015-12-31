package net.crsr.ashurbanipal.sentiment;

import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import net.crsr.ashurbanipal.sentiment.afinn.EnglishSentimentProcessor;
import net.crsr.ashurbanipal.sentiment.lingpipe.EnglishSentenceIterator;
import net.crsr.ashurbanipal.utility.IOUtilities;

public abstract class LexiconSentimentProcessor extends SentimentProcessor {

  protected final Map<String,Double> lexicon = new HashMap<>();
  
  protected LexiconSentimentProcessor(String lexiconFile) {
    BufferedReader br = null;
    try {
      br = new BufferedReader( new InputStreamReader( EnglishSentimentProcessor.class.getResourceAsStream(lexiconFile) ) );
      String line = br.readLine();
      while (line != null) {
        final String[] values = line.split("\\t");
        lexicon.put(values[0], Double.valueOf(values[1]));
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
        score += lexicon.getOrDefault(word, 0.0);
      }
      scoresValues.add(score);
      if (score < 0) {
        classesValues.add(-1.0);
      } else if (score > 0) {
        classesValues.add(1.0);
      } else {
        classesValues.add(0.0);
      }
    }
  }

}