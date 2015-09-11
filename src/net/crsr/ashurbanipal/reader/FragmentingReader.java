package net.crsr.ashurbanipal.reader;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;


public class FragmentingReader {
  
  private String string;
  private final int fragmentSize;
  private int offset = 0;

  public FragmentingReader(Reader reader, int fragmentSize) throws IOException {
    try {
      final StringBuilder sb = new StringBuilder();
      final char[] charBuffer = new char[1024 * 1024];
      int length = reader.read(charBuffer);
      while (length >= 0) {
        sb.append(new String(charBuffer, 0, length));
        length = reader.read(charBuffer);
      }
      this.string = sb.toString();
    } finally {
      try { reader.close(); } catch (Throwable t) { }
    }
    this.fragmentSize = fragmentSize;
  }
  
  public boolean hasFragments() {
    return offset < string.length();
  }
  
  public Reader nextFragment() {
    final int end = Math.min(string.length(), nextParagraph(offset + fragmentSize));
    final StringReader reader = new StringReader(string.substring(offset, end));
    offset = end;
    return reader;
  }
  
  public void close() {
    this.string = "";
    this.offset = 0;
  }
  
  private int nextParagraph(int off) {
    while (off < string.length()) {
      if (string.charAt(off-2) == '\n' && string.charAt(off-1) == '\n') {
        break;
      } else if (string.charAt(off-2) == '\r' && string.charAt(off-1) == '\r') {
          break;
      } else if (string.charAt(off-4) == '\r' && string.charAt(off-3) == '\n' && string.charAt(off-2) == '\r' && string.charAt(off-1) == '\n') {
        break;
      }
      ++off;
    }
    return off;
  }
}
