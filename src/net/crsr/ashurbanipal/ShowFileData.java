package net.crsr.ashurbanipal;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import net.crsr.ashurbanipal.reader.GutenbergLicenseReader;
import net.crsr.ashurbanipal.reader.ZippedTextFileReader;

public class ShowFileData {

  public static void main(String[] args) {

    if (args.length < 1) {

      System.err.println("Usage: ShowFileData filename | directory metadata formats");
    
    } else if (args.length == 1) {
      
      BufferedReader br = null;
      try {
        final File file = new File(args[0]);
        if (!file.canRead()) {
          System.err.println("cannot read: " + file.getPath());
        }
        br = new BufferedReader(new GutenbergLicenseReader(new ZippedTextFileReader(file))); // new GutenbergLicenseReader(new ZippedTextFileReader(file))
        String line = br.readLine();
        while (line != null) {
          System.out.println(line);
          line = br.readLine();
        }
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        if (br != null) { try { br.close(); } catch (Exception e) { } }
      }

    } else {
      final String directory = args[0];
      final String metadata = args[1];
      final String formats = args[2];
    }

  }
}
