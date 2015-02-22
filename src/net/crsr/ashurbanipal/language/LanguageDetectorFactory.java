package net.crsr.ashurbanipal.language;

import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

public class LanguageDetectorFactory {

  static {
    try {
      DetectorFactory.loadProfile(getProfiles());
    } catch (LangDetectException | IOException e) {
      throw new IOError(e);
    }
  }

  public static Detector create() throws LangDetectException {
    return DetectorFactory.create();
  }

  private static List<String> getProfiles() throws IOException {
    BufferedReader r = null;
    try {
      final InputStream stream = LanguageDetectorFactory.class.getClassLoader().getResourceAsStream("net/crsr/ashurbanipal/language/profiles/combined-profiles");
      r = new BufferedReader(new InputStreamReader(stream));
      final List<String> result = new ArrayList<String>();
      String line = r.readLine();
      while (line != null) {
        result.add(line);
        line = r.readLine();
      }
      return result;
    } finally {
      if (r != null) {
        r.close();
      }
    }
  }
}
