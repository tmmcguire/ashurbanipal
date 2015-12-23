package net.crsr.ashurbanipal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import net.crsr.ashurbanipal.pool.FileListWorkPool;
import net.crsr.ashurbanipal.pool.ProcessorSupplier;
import net.crsr.ashurbanipal.store.PosStore;
import net.crsr.ashurbanipal.store.WordStore;
import net.crsr.ashurbanipal.tagger.TaggerCallable;
import net.crsr.ashurbanipal.tagger.TaggerResult;
import net.crsr.ashurbanipal.utility.IOUtilities;
import net.crsr.ashurbanipal.utility.Triple;

public class TagTodoList {

  public static void main(String[] args) {
    try {

      final String todoListFile = args[0];
      final String baseDirectory = args[1];
      final String posFile = args[2];
      final String nounStoreFile = args[3];

      final PosStore posStore = new PosStore(posFile);
      posStore.read();
      posStore.write();
      final WordStore nounStore = new WordStore(nounStoreFile);
      nounStore.read();
      nounStore.write();
      
      final List<Triple<Integer,String,File>> todoList = IOUtilities.readTodoList(baseDirectory, todoListFile);
      final FileListWorkPool<TaggerResult> workPool = new FileListWorkPool<>(posStore.keySet());
      final int count = workPool.submit(todoList, new ProcessorSupplier<TaggerResult>() {
        @Override public Callable<TaggerResult> getProcessor(Integer etext_no, String language, File file) {
          return new TaggerCallable(etext_no, language, file);
        }
      });
      
      for (int i = 0; i < count; ++i) {
        storeResult(workPool, posStore, nounStore);
        if (i % 50 == 0) {
          posStore.write();
          nounStore.write();
        }
      }
      
      workPool.shutdown();
      
    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("Usage: TagTodoList todo-list base-directory POS-file Noun-file");
    } catch (IOException e) {
      System.out.println("error: " + e.toString());
      e.printStackTrace();
    } catch (InterruptedException e) {
      System.out.println("warning: " + e.toString());
      e.printStackTrace();
    }

  }

  private static void storeResult(final FileListWorkPool<TaggerResult> pool, final PosStore posStore, final WordStore nounStore) throws IOException {
    try {

      final TaggerResult taggerResult = pool.getResult();
      if (taggerResult == null) {
        return;
      } else {
        posStore.append(taggerResult.etext_no, taggerResult.posData);
        nounStore.append(taggerResult.etext_no, mergeWordCounts(taggerResult.wordCounts.get("NN"), taggerResult.wordCounts.get("NNS")));
      }

    } catch (Throwable e) {
      PrintWriter bw = null;
      try {
        bw = new PrintWriter(new FileOutputStream("TagTodoList.errors", true));
        bw.format("Error with task: " + e.toString());
        e.printStackTrace(bw);
      } catch (FileNotFoundException e1) {
        throw new Error("cannot write error log", e);
      } finally {
        if (bw != null) { try { bw.close(); } catch (Throwable t) { } }
      }
      System.err.println("Error with task: " + e.toString());
      e.printStackTrace(System.err);
    }
  }
  
  @SafeVarargs
  private static Map<String,Integer> mergeWordCounts(Map<String,Integer>... maps) {
    final Map<String,Integer> results = new TreeMap<>();
    for (Map<String,Integer> map : maps) {
      if (map == null) { continue; }
      for (Entry<String,Integer> entry : map.entrySet()) {
        final String key = entry.getKey();
        results.put(key, results.getOrDefault(key, 0) + entry.getValue());
      }
    }
    return results;
  }
}
