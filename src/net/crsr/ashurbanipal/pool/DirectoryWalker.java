package net.crsr.ashurbanipal.pool;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorCompletionService;

import net.crsr.ashurbanipal.tagger.StanfordTaggerResult;

public class DirectoryWalker {

  private final ExecutorCompletionService<StanfordTaggerResult> executorCompletionService;
  private final Map<String,?> seen;

  public DirectoryWalker(ExecutorCompletionService<StanfordTaggerResult> ecs, Map<String,?> seen) {
    this.executorCompletionService = ecs;
    this.seen = seen;
  }

  public int walk(String directory) {
    // create a to-do list
    List<File> todoList = new ArrayList<>();
    // walk the directory hierarchy adding all unseen .zip files to the to-do
    // list
    final Queue<File> queue = new ArrayDeque<File>();
    queue.add(new File(directory));
    while (!queue.isEmpty()) {
      final File current = queue.remove();
      final File[] zipFiles = current.listFiles(new FileFilter() {
        @Override
        public boolean accept(File f) {
          return f.isFile() && f.getName().endsWith(".zip");
        }
      });
      for (File f : zipFiles) {
        if (!seen.containsKey(f.getAbsolutePath())) {
          todoList.add(f);
        }
      }
      final File[] subs = current.listFiles(new FileFilter() {
        @Override
        public boolean accept(File f) {
          return f.isDirectory();
        }
      });
      queue.addAll(Arrays.asList(subs));
    }
    // sort the to-do list by length
    // (handling shorter files should keep the tagger from running out of memory
    // as long as possible)
    todoList.sort(new Comparator<File>() {
      @Override
      public int compare(File left, File right) {
        return Long.compare(left.length(), right.length());
      }
    });
    // sumbit jobs for each file
    for (File zipFile : todoList) {
      executorCompletionService.submit(new StanfordTaggerCallable(zipFile));
    }
    return todoList.size();
  }

}
