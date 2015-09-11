package net.crsr.ashurbanipal;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import net.crsr.ashurbanipal.reader.FragmentingReader;
import net.crsr.ashurbanipal.reader.GutenbergLicenseReader;
import net.crsr.ashurbanipal.reader.ZippedTextFileReader;

public class ShowFileData {

  public static void main(String[] args) {

    if (args.length < 1) {

      System.err.println("Usage: ShowFileData filename | directory metadata formats");
    
    } else if (args.length == 1) {
      
      FragmentingReader fr = null;
      BufferedReader br = null;
      try {
        final File file = new File(args[0]);
        if (!file.canRead()) {
          System.err.println("cannot read: " + file.getPath());
        }
        
        fr = new FragmentingReader(new GutenbergLicenseReader(new ZippedTextFileReader(file)), 100);
        while (fr.hasFragments()) {
          br = new BufferedReader( fr.nextFragment() );
          String line = br.readLine();
          while (line != null) {
            System.out.println(line);
            line = br.readLine();
          }
        }
        
      } catch (IOException e) {
        e.printStackTrace();
      } catch (net.crsr.ashurbanipal.reader.ZippedTextFileReader.Exception e) {
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
