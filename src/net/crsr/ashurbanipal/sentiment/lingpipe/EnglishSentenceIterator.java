package net.crsr.ashurbanipal.sentiment.lingpipe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.aliasi.sentences.IndoEuropeanSentenceModel;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;

public class EnglishSentenceIterator implements Iterable<String> {

  private final String[] tokens;
  final int[] sentenceBoundaries;
  
  public EnglishSentenceIterator(String text) {
    final Tokenizer tokenizer = IndoEuropeanTokenizerFactory.INSTANCE.tokenizer(text.toCharArray(), 0, text.length());
    final List<String> tokenList = new ArrayList<>();
    final List<String> whiteList = new ArrayList<>();
    tokenizer.tokenize(tokenList,whiteList);
    tokens = tokenList.toArray(new String[tokenList.size()]);
    final String[] whites = whiteList.toArray(new String[whiteList.size()]);
    sentenceBoundaries = new IndoEuropeanSentenceModel().boundaryIndices(tokens, whites);
  }

  @Override
  public Iterator<String> iterator() {
    return new iterator();
  }

  private class iterator implements Iterator<String> {

    private int idx = 0;
    
    @Override
    public boolean hasNext() {
      return idx < sentenceBoundaries.length;
    }

    @Override
    public String next() {
      final int sStart = idx == 0 ? 0 : sentenceBoundaries[idx-1] + 1;
      final int sEnd = sentenceBoundaries[idx];
      idx += 1;
      final StringBuilder sb = new StringBuilder();
      for (int i = sStart; i <= sEnd; ++i) {
        sb.append(tokens[i]).append(' ');
      }
      return sb.toString();
    }
    
  }

}
