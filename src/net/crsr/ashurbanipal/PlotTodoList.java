package net.crsr.ashurbanipal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import net.crsr.ashurbanipal.pool.FileListWorkPool;
import net.crsr.ashurbanipal.pool.ProcessorSupplier;
import net.crsr.ashurbanipal.sentiment.SentimentProcessorCallable;
import net.crsr.ashurbanipal.sentiment.SentimentResult;
import net.crsr.ashurbanipal.store.PlotFrequencyStore;
import net.crsr.ashurbanipal.utility.IOUtilities;
import net.crsr.ashurbanipal.utility.Triple;

public class PlotTodoList {

  public static void main(String[] args) {
    try {
      final String todoListFile = args[0];
      final String baseDirectory = args[1];
      final PlotFrequencyStore scores = new PlotFrequencyStore(args[2]);
      scores.read();
      scores.write();
      final PlotFrequencyStore classes = new PlotFrequencyStore(args[3]);
      classes.read();
      classes.write();

      final List<Triple<Integer,String,File>> todoList = IOUtilities.readTodoList(baseDirectory, todoListFile);
      
      FileListWorkPool<SentimentResult> pool = null;
      try {
        pool = new FileListWorkPool<>(Collections.<Integer> emptyList());
        final int count = pool.submit(todoList, new ProcessorSupplier<SentimentResult>() {
          @Override
          public Callable<SentimentResult> getProcessor(Integer etext_no, String language, File file) {
            return new SentimentProcessorCallable(etext_no, language, file);
          }
        });

        for (int i = 0; i < count; ++i) {
          storeResult(pool, scores, classes);
          if (i % 50 == 0) {
            scores.write();
            classes.write();
          }
        }
        scores.write();
        classes.write();
      }
      finally {
        if (pool != null) { pool.shutdown(); }
      }
      
    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("Usage: PlotTodoList todo-list base-directory scores-file classes-file");
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

  }
  
  private static void storeResult(FileListWorkPool<SentimentResult> pool, PlotFrequencyStore scores, PlotFrequencyStore classes) {
    try {

      final SentimentResult sentimentResult = pool.getResult();
      if (sentimentResult == null) {
        return;
      } else {
        scores.append(sentimentResult.etext_no, sentimentResult.scores);
        classes.append(sentimentResult.etext_no, sentimentResult.classes);
      }

    } catch (Throwable e) {
      PrintWriter bw = null;
      try {
        bw = new PrintWriter(new FileOutputStream("PlotTodoList.errors", true));
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

}
