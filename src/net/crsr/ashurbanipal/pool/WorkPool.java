package net.crsr.ashurbanipal.pool;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import net.crsr.ashurbanipal.pool.FileListWorkPool.Exception;

public class WorkPool<R> {

  final protected String nThreads = System.getProperty("ashurbanipal.threads", Integer.toString(Runtime.getRuntime().availableProcessors() - 1));
  final protected ExecutorService executorService = Executors.newFixedThreadPool(Integer.valueOf(nThreads));
  final protected ExecutorCompletionService<R> executorCompletionService = new ExecutorCompletionService<>(executorService);
  
  public R getResult() throws Exception {
    try {
      return executorCompletionService.take().get();
    } catch (InterruptedException e) {
      throw new Exception("Task interrupted", e);
    } catch (ExecutionException e) {
      throw new Exception("Task threw exception", e);
    }
  }

  public void shutdown() throws InterruptedException {
    executorService.shutdown();
    executorService.awaitTermination(2, TimeUnit.MINUTES);
  }
}
