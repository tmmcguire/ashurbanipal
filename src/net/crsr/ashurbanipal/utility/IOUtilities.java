package net.crsr.ashurbanipal.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IOUtilities {

  /**
   * The file format is a header line followed by multiple lines of the following format:
   * <code>etext_no{tab}language{tab}content_type{tab}filename</code>
   * 
   * @param todoListFile name of file containing todo list.
   * @throws IOException if an error occurs.
   */
  public static List<Triple<Integer,String,File>> readTodoList(String baseDirectory, String todoListFile) throws IOException {
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
  
  /**
   * Read the entire contents of a text from a {@link Reader}, returning it as a string.
   * 
   * @param reader A {@link Reader} containing interesting text.
   * @return The text, as a string.
   */
  public static String readText(Reader reader) {
    BufferedReader br = null;
    try {
      br = new BufferedReader(reader);
      return br.lines().collect(Collectors.joining("\n"));
    } finally {
      if (br != null) { try { br.close(); } catch (Exception e) { } }
    }
  }

}
