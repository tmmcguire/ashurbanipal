package net.crsr.ashurbanipal.reader;

import java.io.IOException;
import java.io.Reader;
import java.util.regex.Pattern;

public class GutenbergLicenseReader extends Reader {

  final Pattern startPattern = Pattern.compile("^\\*\\*\\* *START OF", Pattern.MULTILINE);
  final Pattern endPattern = Pattern.compile("^\\*\\*\\* *END OF", Pattern.MULTILINE);

  private String string;
  private int offset = 0;
  private int mark = 0;

  /**
   * Attempt to skip the Project Gutenberg file header and footer, only
   * returning the text contents of the file.
   */
  public GutenbergLicenseReader(Reader reader) throws IOException {
    try {
      final StringBuilder sb = new StringBuilder();
      final char[] charBuffer = new char[1024 * 1024];
      int length = reader.read(charBuffer);
      while (length >= 0) {
        sb.append(new String(charBuffer, 0, length));
        length = reader.read(charBuffer);
      }
      String[] suffix = startPattern.split(sb.toString(), 2);
      String[] prefix = endPattern.split(suffix[suffix.length - 1], 2);
      // System.out.println(suffix[0].length() + " - " + prefix[0].length() +
      // " - " + (prefix.length > 1 ? prefix[1].length() : 0));
      string = prefix[0];
    } finally {
      reader.close();
    }
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    final int copySize = Math.min(len, string.length() - offset);
    for (int i = offset; i < offset + copySize; ++i) {
      cbuf[off++] = string.charAt(i);
    }
    offset += copySize;
    return copySize > 0 ? copySize : -1;
  }

  @Override
  public void close() throws IOException {
    string = "";
    offset = 0;
  }

  @Override
  public boolean ready() throws IOException {
    return offset < string.length();
  }

  @Override
  public void reset() throws IOException {
    offset = mark;
  }

  @Override
  public boolean markSupported() {
    return true;
  }

  @Override
  public void mark(int readAheadLimit) throws IOException {
    mark = offset;
  }
}
