package net.crsr.ashurbanipal.pool;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import net.crsr.ashurbanipal.tagger.TaggerResult;
import net.crsr.ashurbanipal.utility.Triple;

public class FileListProcessor extends WorkPool<TaggerResult> {

  private final Set<Integer> seen;

  public FileListProcessor(Set<Integer> seenEtextNos) {
    this.seen = seenEtextNos;
  }
  
  public int walk(List<Triple<Integer,String,File>> todoList) {
    int count = 0;
    for (Triple<Integer,String,File> text : todoList) {
      if (!seen.contains(text.a)) {
        executorCompletionService.submit(new TaggerCallable(text.a, text.b, text.c));
        count++;
      }
    }
    return count;
  }
  
  public TaggerResult get() throws Exception {
    try {
      return executorCompletionService.take().get();
    } catch (InterruptedException e) {
      throw new Exception("Task interrupted", e);
    } catch (ExecutionException e) {
      throw new Exception("Task threw exception", e);
    }
  }
  
  @SuppressWarnings("serial")
  public static class Exception extends java.lang.Exception {
    public Exception(String message, Throwable cause) { super(message, cause); }
  }
}
