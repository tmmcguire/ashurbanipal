package net.crsr.ashurbanipal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.ejml.simple.SimpleMatrix;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

public class SentimentTest {
  private static final String text
  = "The soft beauty of the Latin word touched with an enchanting touch the " +
      "dark of the evening, with a touch fainter and more persuading than the " +
      "touch of music or of a woman's hand. The strife of their minds was " +
      "quelled. The figure of a woman as she appears in the liturgy of the " +
      "church passed silently through the darkness: a white-robed figure, " +
      "small and slender as a boy, and with a falling girdle. Her voice, frail " +
      "and high as a boy's, was heard intoning from a distant choir the first " +
      "words of a woman which pierce the gloom and clamour of the first " +
      "chanting of the passion:";

  //  String text = IOUtils.slurpFileNoExceptions(filename);
  //  Annotation annotation = new Annotation(text);
  //  tokenizer.annotate(annotation);
  //  List<Annotation> annotations = Generics.newArrayList();
  //  for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
  //    Annotation nextAnnotation = new Annotation(sentence.get(CoreAnnotations.TextAnnotation.class));
  //    nextAnnotation.set(CoreAnnotations.SentencesAnnotation.class, Collections.singletonList(sentence));
  //    annotations.add(nextAnnotation);
  //  }
  //  return annotations;

  public static void main(String[] args) {
    try {
      String text = IOUtils.slurpFile(args[0]);

      Properties tokenizerProperties = new Properties();
      tokenizerProperties.setProperty("annotators", "tokenize, ssplit");
      StanfordCoreNLP tokenizer = new StanfordCoreNLP(tokenizerProperties);

      Properties pipelineProperties = new Properties();
      pipelineProperties.setProperty("annotators", "parse, sentiment");
      pipelineProperties.setProperty("enforceRequirements", "false");
      StanfordCoreNLP pipeline = new StanfordCoreNLP(pipelineProperties);

      Annotation tokens = new Annotation(text);
      tokenizer.annotate(tokens);

      List<Annotation> annotations = new ArrayList<>();
      for (CoreMap sentence : tokens.get(CoreAnnotations.SentencesAnnotation.class)) {
        Annotation nextAnnotation = new Annotation(sentence.get(CoreAnnotations.TextAnnotation.class));
        nextAnnotation.set(CoreAnnotations.SentencesAnnotation.class, Collections.singletonList(sentence));
        pipeline.annotate(nextAnnotation);
        annotations.add(nextAnnotation);
//      }

//      for (Annotation annotation : annotations) {
        for (CoreMap sentence2 : nextAnnotation.get(CoreAnnotations.SentencesAnnotation.class)) {
          final Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
          //          System.out.println(tree);
          SimpleMatrix vector = RNNCoreAnnotations.getPredictions(tree);
          double score = 0.0;
          for (int i = 0; i < 5; ++i) {
            score += (i + 1) * vector.get(i);
          }
          score -= 3.0;
          //          System.out.println(vector);
          final String sentimentClass = sentence2.get(SentimentCoreAnnotations.SentimentClass.class);
          int cls = 0;
          switch (sentimentClass.toLowerCase()) {
            case "very negative":
              cls = -2;
              break;
            case "negative":
              cls = -1;
              break;
            case "neutral":
              cls = 0;
              break;
            case "positive":
              cls = 1;
              break;
            case "very positive":
              cls = 2;
              break;
            default:
              System.out.println(sentimentClass);
              break;
          }
          System.out.format("%s %s\n", cls, score);
        }
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
