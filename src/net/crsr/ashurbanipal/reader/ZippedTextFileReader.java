package net.crsr.ashurbanipal.reader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;

public class ZippedTextFileReader extends Reader {

  private final ArchiveInputStream archiveInputStream;
  private final InputStreamReader inputStream;

  public ZippedTextFileReader(File file) throws Exception {
    try {
      
      InputStreamReader temp = null;
      archiveInputStream = new ArchiveStreamFactory().createArchiveInputStream(new BufferedInputStream(new FileInputStream(file)));
      ArchiveEntry entry = archiveInputStream.getNextEntry();
      while (entry != null) {
        if (!entry.isDirectory() && entry.getName().endsWith((".txt"))) {
          temp = new InputStreamReader(archiveInputStream);
          break;
        }
        entry = archiveInputStream.getNextEntry();
      }
      inputStream = temp;

    } catch (ArchiveException e) {
      throw new Exception("error reading archive: " + e.toString(), e);
    } catch (FileNotFoundException e) {
      throw new Exception("error opening archive: " + e.toString(), e);
    } catch (IOException e) {
      throw new Exception("error reading archive entry: " + e.toString(), e);
    }
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
    archiveInputStream.close();
  }
  
  @SuppressWarnings("serial")
  public static class Exception extends java.lang.Exception {
    public Exception(String message, Throwable cause) {
      super(message, cause);
    }
  }

}
