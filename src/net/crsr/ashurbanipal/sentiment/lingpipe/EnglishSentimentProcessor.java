package net.crsr.ashurbanipal.sentiment.lingpipe;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

import net.crsr.ashurbanipal.sentiment.SentimentProcessor;
import net.crsr.ashurbanipal.utility.IOUtilities;

import com.aliasi.classify.JointClassification;
import com.aliasi.classify.LMClassifier;
import com.aliasi.util.AbstractExternalizable;

public class EnglishSentimentProcessor extends SentimentProcessor {

  private static final List<String> categories = Arrays.asList("neg", "neu", "pos");
  
  // Frozen classifier from
  // http://processingdeveloper.altervista.org/classifier.txt
  // courtesy of
  // http://www.gioviz.com/2013/05/how-to-sentiment-analysis-of-tweets.html
  
  private final LMClassifier<?,?> classifier;

  public EnglishSentimentProcessor() {
    try {
      final File model = new File( EnglishSentimentProcessor.class.getResource("/net/crsr/ashurbanipal/sentiment/lingpipe/classifier.txt").getFile() );
      classifier = (LMClassifier<?,?>) AbstractExternalizable.readObject(model);
    } catch (ClassNotFoundException | IOException e) {
      throw new IOError(e);
    }
  }

  @Override
  public void process(Integer etextNo, Reader text) {
    for (String sentence : new EnglishSentenceIterator( IOUtilities.readText(text)) ) {
      final JointClassification classification = classifier.classify(sentence);
      double score = -2.0;
      for (int i = 0; i < categories.size(); ++i) {
        score += (i + 1) * classification.conditionalProbability( categories.get(i) );
      }
      // 1 <= score <= categories.size()
      scoresValues.add(score);
      switch (classification.bestCategory()) {
        case "neg":
          classesValues.add(-1.0);
          break;
        case "neu":
          classesValues.add(0.0);
          break;
        case "pos":
          classesValues.add(1.0);
          break;
        default:
          throw new Error("unknown sentiment: " + classification.bestCategory());
      }
    }
  }

}
