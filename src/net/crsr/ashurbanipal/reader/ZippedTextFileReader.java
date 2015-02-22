package net.crsr.ashurbanipal.reader;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class ZippedTextFileReader extends Reader {

  private final ZipFile zipFile;
  private final InputStreamReader inputStream;

  public ZippedTextFileReader(File file) throws ZipException, IOException {
    InputStreamReader temp = null;
    zipFile = new ZipFile(file);
    final Enumeration<? extends ZipEntry> entries = zipFile.entries();
    while (entries.hasMoreElements()) {
      final ZipEntry entry = entries.nextElement();
      if (!entry.isDirectory() && entry.getName().endsWith(".txt")) {
        temp = new InputStreamReader(zipFile.getInputStream(entry));
        break;
      }
    }
    inputStream = temp;
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    if (inputStream == null) {
      return -1;
    } else {
      return inputStream.read(cbuf, off, len);
    }
  }

  @Override
  public void close() throws IOException {
    if (inputStream != null) {
      inputStream.close();
    }
    zipFile.close();
  }

}
