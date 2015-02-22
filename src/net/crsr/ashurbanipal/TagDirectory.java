package net.crsr.ashurbanipal;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import net.crsr.ashurbanipal.pool.DirectoryWalker;
import net.crsr.ashurbanipal.store.PosStore;
import net.crsr.ashurbanipal.store.WordStore;
import net.crsr.ashurbanipal.tagger.StanfordTaggerResult;

public class TagDirectory {

  public static void main(String[] args) {
    try {
      final String directory = args[0];
      final String posFile = args[1];
      final String nounStoreFile = args[2];
      final String verbStoreFile = args[3];

      final String nThreads = System.getProperty("ashurbanipal.threads", Integer.toString(Runtime.getRuntime().availableProcessors() - 1));

      final ExecutorService executorService = Executors.newFixedThreadPool(Integer.valueOf(nThreads));
      final ExecutorCompletionService<StanfordTaggerResult> tecs = new ExecutorCompletionService<>(executorService);

      final PosStore posStore = new PosStore(directory, posFile);
      posStore.read();
      final WordStore nounStore = new WordStore(nounStoreFile);
      nounStore.read();
      final WordStore verbStore = new WordStore(verbStoreFile);
      verbStore.read();

      final DirectoryWalker walker = new DirectoryWalker(tecs, posStore);
      final int count = walker.walk(directory);

      for (int i = 0; i < count; ++i) {
        try {
          final StanfordTaggerResult result = tecs.take().get();
          if (result == null) { /* unrecognized language? */
            continue;
          }
          final String filename = result.a.startsWith(directory) ? result.a.substring(directory.length()) : result.a;
          posStore.put(filename, result.b);
          posStore.write();
          final Map<String,Integer> nouns = new HashMap<>();
          final Map<String,Integer> verbs = new HashMap<>();
          for (Entry<String,Map<String,Integer>> entry : result.c.entrySet()) {
            if (entry.getKey().startsWith("NN")) {
              updateWordCounts(nouns, entry.getValue());
            } else if (entry.getKey().startsWith("VB")) {
              updateWordCounts(verbs, entry.getValue());
            }
          }
          nounStore.append(filename, nouns);
          verbStore.append(filename, verbs);
        } catch (CancellationException | ExecutionException | InterruptedException e) {
          e.printStackTrace();
        }
      }

      executorService.shutdown();
      executorService.awaitTermination(2, TimeUnit.MINUTES);

    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("Usage: TagDirectory directory POS-file Noun-file Verb-file");
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private static void updateWordCounts(Map<String,Integer> counts, Map<String,Integer> updates) {
    for (Entry<String,Integer> entry : updates.entrySet()) {
      counts.put(entry.getKey(), counts.getOrDefault(entry.getKey(), 0) + entry.getValue());
    }
  }
}
