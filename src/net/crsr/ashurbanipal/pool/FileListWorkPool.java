package net.crsr.ashurbanipal.pool;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.crsr.ashurbanipal.utility.Triple;

public class FileListWorkPool<R> extends WorkPool<R> {

  protected final Set<Integer> seen = new HashSet<Integer>();

  public FileListWorkPool(Collection<Integer> seenEtextNos) {
    seen.addAll(seenEtextNos);
  }
  
  public int submit(List<Triple<Integer,String,File>> todoList, ProcessorSupplier<R> processorSupplier) {
    int count = 0;
    for (Triple<Integer,String,File> text : todoList) {
      if (!seen.contains(text.a)) {
        executorCompletionService.submit(processorSupplier.getProcessor(text.a, text.b, text.c));
        count++;
      }
    }
    return count;
  }
  
  @SuppressWarnings("serial")
  public static class Exception extends java.lang.Exception {
    public Exception(String message, Throwable cause) { super(message, cause); }
  }
}
