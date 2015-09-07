package net.crsr.ashurbanipal.reader;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

/**
 * Reader that skips the extra text added by Project Gutenberg for advertising and licensing. 
 * 
 * <p>
 * This class is a port of the Python utility by Clemens Wolff which can be found at:
 * <a href="https://github.com/c-w/Gutenberg">https://github.com/c-w/Gutenberg</a>
 * 
 * <p>
 * The file there includes this note:
 * 
 * <p>
 *  Remove lines that are part of the Project Gutenberg header or footer.
 *  Note: this function is a port of the C++ utility by Johannes Krugel. The
 *  original version of the code can be found at:
 *  <a href="http://www14.in.tum.de/spp1307/src/strip_headers.cpp">http://www14.in.tum.de/spp1307/src/strip_headers.cpp</a>
 *
 */
public class GutenbergLicenseReader extends Reader {
  
  private static final List<String> TEXT_START_MARKERS = Arrays.asList(
      "*END*THE SMALL PRINT",
      "*** START OF THE PROJECT GUTENBERG",
      "*** START OF THIS PROJECT GUTENBERG",
      "This etext was prepared by",
      "E-text prepared by",
      "Produced by",
      "Distributed Proofreading Team",
      "*END THE SMALL PRINT",
      "***START OF THE PROJECT GUTENBERG",
      "This etext was produced by",
      "*** START OF THE COPYRIGHTED",
      "The Project Gutenberg",
      "http://gutenberg.spiegel.de/ erreichbar.",
      "Project Runeberg publishes",
      "Beginning of this Project Gutenberg",
      "Project Gutenberg Online Distributed",
      "Gutenberg Online Distributed",
      "the Project Gutenberg Online Distributed",
      "Project Gutenberg TEI",
      "This eBook was prepared by",
      "http://gutenberg2000.de erreichbar.",
      "This Etext was prepared by",
      "Gutenberg Distributed Proofreaders",
      "the Project Gutenberg Online Distributed Proofreading Team",
      "**The Project Gutenberg",
      "*SMALL PRINT!",
      "More information about this book is at the top of this file.",
      "tells you about restrictions in how the file may be used.",
      "l'authorization à les utilizer pour preparer ce texte.",
      "of the etext through OCR.",
      "*****These eBooks Were Prepared By Thousands of Volunteers!*****",
      "SERVICE THAT CHARGES FOR DOWNLOAD",
      "We need your donations more than ever!",
      " *** START OF THIS PROJECT GUTENBERG",
      "****     SMALL PRINT!"
      );
      
  private static final List<String> TEXT_END_MARKERS = Arrays.asList(
      "*** END OF THE PROJECT GUTENBERG",
      "*** END OF THIS PROJECT GUTENBERG",
      "***END OF THE PROJECT GUTENBERG",
      "End of the Project Gutenberg",
      "End of The Project Gutenberg",
      "Ende dieses Project Gutenberg",
      "by Project Gutenberg",
      "End of Project Gutenberg",
      "End of this Project Gutenberg",
      "Ende dieses Projekt Gutenberg",
      "        ***END OF THE PROJECT GUTENBERG",
      "*** END OF THE COPYRIGHTED",
      "End of this is COPYRIGHTED",
      "Ende dieses Etextes ",
      "Ende dieses Project Gutenber",
      "Ende diese Project Gutenberg",
      "**This is a COPYRIGHTED Project Gutenberg Etext, Details Above**",
      "Fin de Project Gutenberg",
      "The Project Gutenberg Etext of ",
      "Ce document fut presente en lecture",
      "Ce document fut présenté en lecture",
      "More information about this book is at the top of this file.",
      "We need your donations more than ever!",
      "<<THIS ELECTRONIC VERSION OF",
      "END OF PROJECT GUTENBERG",
      " End of the Project Gutenberg",
      " *** END OF THIS PROJECT GUTENBERG"
      );

  private String string;
  private int offset;
  private int mark;
  private int end;

  /**
   * Attempt to skip the Project Gutenberg file header and footer, only
   * returning the text contents of the file.
   */
  public GutenbergLicenseReader(Reader reader) throws IOException {
    try {
      // Read the entire damn file.
      final StringBuilder sb = new StringBuilder();
      final char[] charBuffer = new char[1024 * 1024];
      int length = reader.read(charBuffer);
      while (length >= 0) {
        sb.append(new String(charBuffer, 0, length));
        length = reader.read(charBuffer);
      }
      string = sb.toString();
      // Find the start and end of the text.
      int lines = lineCount(string);
      offset = maximalLocationOfStart(string, lineN(string, Math.min(600, lines / 3)));
      mark = offset;
      end = minimalLocationOfEnd(string, Math.max(offset, lineN(string, 100)));
      System.err.println("Reader " + offset + " " + end);
    } finally {
      reader.close();
    }
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    final int copySize = Math.min(len, end - offset);
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
    mark = 0;
    end = 0;
  }

  @Override
  public boolean ready() throws IOException {
    return offset < end;
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
  
  static int lineCount(String text) {
    int count = 0;
    for (int i = 0; i < text.length(); ++i) {
      if (text.charAt(i) == '\n') {
        count++;
      }
    }
    return count;
  }
  
  /**
   * Return the location of line n+1 in text.
   * 
   * @param text
   * @param n
   * @return a character offset.
   */
  static int lineN(String text, int n) {
    int count = 0;
    int i = 0;
    for (; i < text.length(); ++i) {
      if (text.charAt(i) == '\n') {
        count++;
        if (count == n) { return i; }
      }
    }
    return i;
  }
  
  static int maximalLocationOfStart(String text, int prefixLength) {
    System.err.println("prefix " + prefixLength);
    int loc = 0;
    for (String marker : TEXT_START_MARKERS) {
      final int i = text.lastIndexOf('\n' + marker);
      System.err.println("start " + i);
      if (i > loc && i < prefixLength) {
        loc = i + marker.length();
      }
    }
    System.err.println("endStart: " + loc);
    while (loc < text.length() && text.charAt(loc) != '\n') {
      loc++;
    }
    return loc;
  }
  
  static int minimalLocationOfEnd(String text, int prefixLength) {
    int loc = text.length();
    for (String marker : TEXT_END_MARKERS) {
      final int i = text.indexOf('\n' + marker);
      if (i >= 0 && i < loc && i > prefixLength) {
        loc = i;
      }
    }
    return loc;
  }

}
