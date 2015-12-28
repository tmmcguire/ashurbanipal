package net.crsr.ashurbanipal.sentiment.stanford;

import java.io.Reader;
import java.util.Collections;
import java.util.Properties;

import net.crsr.ashurbanipal.sentiment.SentimentProcessor;
import net.crsr.ashurbanipal.utility.IOUtilities;

import org.ejml.simple.SimpleMatrix;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

public class EnglishSentimentProcessor extends SentimentProcessor {
  
  private final StanfordCoreNLP tokenizerPipeline;
  private final StanfordCoreNLP sentimentPipeline;
  
  public EnglishSentimentProcessor() {
    final Properties tokenizerProperties = new Properties();
    tokenizerProperties.setProperty("annotators", "tokenize, ssplit");
    tokenizerPipeline = new StanfordCoreNLP(tokenizerProperties);
    
    final Properties sentimentProperties = new Properties();
    sentimentProperties.setProperty("annotators", "parse, sentiment");
    sentimentProperties.setProperty("enforceRequirements", "false");
    sentimentPipeline = new StanfordCoreNLP(sentimentProperties);
  }

  @Override
  public void process(Integer etext_no, Reader reader) {
    final Annotation tokens = new Annotation( IOUtilities.readText(reader) );
    tokenizerPipeline.annotate(tokens);
    for (CoreMap sentence : tokens.get(CoreAnnotations.SentencesAnnotation.class)) {
      final Annotation annotation = annotateSentiment(sentence);
      for (CoreMap subAnnotation : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
        // Given that only one sentence is passed to annotateSentiment, there should be
        // only one sentence subAnnotation here.
        recordSentimentForSentence(subAnnotation);
      }
    }
  }

  private Annotation annotateSentiment(CoreMap sentence) {
    final Annotation sentenceAnnotation = new Annotation(sentence.get(CoreAnnotations.TextAnnotation.class));
    sentenceAnnotation.set(CoreAnnotations.SentencesAnnotation.class, Collections.singletonList(sentence));
    sentimentPipeline.annotate(sentenceAnnotation);
    return sentenceAnnotation;
  }

  private void recordSentimentForSentence(final CoreMap sentenceAnnotation) {
    final Tree tree = sentenceAnnotation.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
    final SimpleMatrix matrix = RNNCoreAnnotations.getPredictions( tree );
    double score = 0.0;
    double cls = 0;
    double maxColValue = Double.NEGATIVE_INFINITY;
    for (int i = 0; i < 5; ++i) {
      final double colValue = matrix.get(i);
      score += (i + 1) * colValue;
      if (colValue > maxColValue) {
        cls = i;
        maxColValue = colValue;
      }
    }
    // 1 <= score <= 5
    scoresValues.add(score - 3.0);
    // 0 <= cls <= 4
    classesValues.add(cls - 2.0);
  }
}
