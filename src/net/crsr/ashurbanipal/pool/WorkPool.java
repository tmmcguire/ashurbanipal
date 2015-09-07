package net.crsr.ashurbanipal.pool;

import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WorkPool<R> {
  final String nThreads = System.getProperty("ashurbanipal.threads", Integer.toString(Runtime.getRuntime().availableProcessors() - 1));
  final ExecutorService executorService = Executors.newFixedThreadPool(Integer.valueOf(nThreads));
  final protected ExecutorCompletionService<R> executorCompletionService = new ExecutorCompletionService<>(executorService);

  public void shutdown() throws InterruptedException {
    executorService.shutdown();
    executorService.awaitTermination(2, TimeUnit.MINUTES);
  }
}
