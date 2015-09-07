package net.crsr.ashurbanipal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.crsr.ashurbanipal.pool.FileListProcessor;
import net.crsr.ashurbanipal.pool.FileListProcessor.Exception;
import net.crsr.ashurbanipal.store.PosStore;
import net.crsr.ashurbanipal.store.WordStore;
import net.crsr.ashurbanipal.tagger.TaggerResult;
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
      
      final List<Triple<Integer,String,File>> todoList = readTodoList(baseDirectory, todoListFile);
      final FileListProcessor fileListProcessor = new FileListProcessor(posStore.keySet());
      final int count = fileListProcessor.walk(todoList);
      
      for (int i = 0; i < count; ++i) {
        try {
          
          final TaggerResult taggerResult = fileListProcessor.get();
          if (taggerResult == null) {
            // unrecognized language?
            continue;
          } else {
            posStore.append(taggerResult.a, taggerResult.b);
            nounStore.append(taggerResult.a, mergeWordCounts(taggerResult.c.get("NN"), taggerResult.c.get("NNS")));
          }

        } catch (Exception e) {
          System.err.println("Error with task: " + e.toString());
          e.printStackTrace(System.err);
        }
      }
      
      fileListProcessor.shutdown();
      
    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("Usage: TagTodoList todo-list base-directory POS-file Noun-file");
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      System.out.println("warning: " + e.toString());
    }

  }

  /**
   * The file format is a header line followed by multiple lines of the following format:
   * <code>etext_no{tab}language{tab}content_type{tab}filename</code>
   * 
   * @param todoListFile name of file containing todo list.
   * @throws IOException if an error occurs.
   */
  private static List<Triple<Integer,String,File>> readTodoList(String baseDirectory, String todoListFile) throws IOException {
    BufferedReader r = null;
    try {
      r = new BufferedReader(new InputStreamReader(new FileInputStream(todoListFile)));
      // Skip header
      r.readLine();
      final List<Triple<Integer,String,File>> todoList = new ArrayList<>();
      String line = r.readLine();
      while (line != null) {
        final String[] values = line.split("\\t");
        todoList.add( new Triple<>(Integer.valueOf(values[0]), values[1], new File(baseDirectory + values[3])) );
        line = r.readLine();
      }
      return todoList;
    } finally {
      if (r != null) {
        try { r.close(); } catch (IOException e) { }
      }
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
