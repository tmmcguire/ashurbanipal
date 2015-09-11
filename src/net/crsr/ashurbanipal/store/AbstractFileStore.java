package net.crsr.ashurbanipal.store;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/*
 * Base file storage for processed data.
 * 
 * The file is written in such a way as to attempt to preserve a backup of
 * the previous version and the validity of the current version.
 * 
 * The file should be read (if possible) when the object is created.
 */
abstract public class AbstractFileStore {

  protected final File file;
  protected boolean valid = false;

  abstract protected void readData(BufferedReader r) throws IOException;

  abstract protected void writeData(OutputStream w) throws IOException;

  protected AbstractFileStore(String filename) throws IOException {
    this.file = new File(filename);
  }
  
  protected String escape(String str) {
    final StringBuilder sb = new StringBuilder(str);
    for (int i = 0; i < sb.length(); ++i) {
      switch (sb.charAt(i)) {
        case ';':
        case '\t':
        case '\\':
          sb.insert(i, "\\");
          i++;
          break;
        default:
          break;
      }
    }
    return sb.toString();
  }
  
  protected String escape(List<String> strs) {
    if (strs == null) {
      return "";
    }
    final List<String> results = new ArrayList<>(strs);
    for (int i = 0; i < results.size(); ++i) {
      results.set(i, escape(results.get(i)));
    }
    return String.join(";", results);
  }
  
  protected String unescape(String str) {
    final StringBuilder sb = new StringBuilder(str);
    for (int i = 0; i < sb.length(); ++i) {
      switch (sb.charAt(i)) {
        case '\\':
          sb.delete(i, i+1);
          break;
        default:
          break;
      }
    }
    return sb.toString();
  }
  
  protected List<String> unescapeOnSemicolon(String str) {
    final List<String> results = new ArrayList<>();
    int i = 0;
    while (i < str.length()) {
      int j = i+1;
      while (j < str.length() && str.charAt(j) != ';') {
        j += (str.charAt(j) == '\\') ? 2 : 1;
      }
      results.add( unescape(str.substring(i, j)) );
      i = (j < str.length() && str.charAt(j) == ';') ? j + 1 : j;
    }
    
    return results;
  }

  public void read() throws IOException {
    if (!file.exists()) { return; }
    BufferedReader r = null;
    try {
      r = new BufferedReader(new InputStreamReader(new FileInputStream(this.file)));
      this.readData(r);
    } finally {
      if (r != null) {
        r.close();
      }
    }
  }

  public void write() throws IOException {
    final File newFile = new File(this.file.getAbsolutePath() + ".new");
    OutputStream w = null;
    try {
      w = new FileOutputStream(newFile);
      this.writeData(w);
      this.valid = true;
    } finally {
      if (w != null) {
        w.close();
      }
    }
    if (this.file.exists()) {
      this.file.renameTo(new File(file.getAbsolutePath() + ".bak"));
    }
    newFile.renameTo(file);
  }

  public void appendString(String string) throws IOException {
    OutputStream os = null;
    try {
      os = new FileOutputStream(file.getAbsoluteFile(), true);
      os.write(string.getBytes());
    } finally {
      if (os != null) {
        os.close();
      }
    }
  }
}
