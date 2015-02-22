package net.crsr.ashurbanipal;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.crsr.ashurbanipal.store.FormatStore;
import net.crsr.ashurbanipal.store.MetadataStore;
import net.crsr.ashurbanipal.utility.Pair;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

// Metadata keys
// 999   note
// 7193  contributor (added to author)
// 19284 loc_class
// 29550 release_date
// 29556 base_directory (ignored)
// 29556 copyright_status
// 29556 etext_no
// 29556 link
// 29653 language
// 29719 author
// 29755 title
// 33376 subject

// Available formats/files
// 34450 file_name
// 34450 format

public class HTMLMetadata {

  public static void main(String[] args) {
    try {

      final String directory = args[0];
      final String metadata = args[1];
      final String formats = args[2];

      final MetadataStore metadataStore = new MetadataStore(metadata);
      metadataStore.read();
      metadataStore.write();
      final FormatStore formatStore = new FormatStore(formats);
      formatStore.read();
      formatStore.write();

      final File dir = new File(directory);
      for (File file : dir.listFiles()) {
        handleFile(file, metadataStore, formatStore);
      }

    } catch (IOException e) {
      throw new IOError(e);
    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("Usage: HTMLMetadata directory metadata-file format-file");
    }
  }

  private static void handleFile(final File file, MetadataStore metadataStore, FormatStore formatStore) throws IOException {
    Document doc = Jsoup.parse(file, Charset.defaultCharset().displayName(), "file://" + file.getAbsolutePath());

    final Elements tables = doc.getElementsByTag("table");
    assert tables.size() == 2;
    final Map<String,List<String>> metadata = new HashMap<>();
    final List<Pair<String,String>> formats = new ArrayList<>();
    for (Element table : tables) {
      final String caption = getFirstElementText(table, "caption");
      switch (caption) {
        case "Bibliographic Record":
          for (Element row : table.getElementsByTag("tr")) {
            String key = makeKey(getFirstElementText(row, "th"));
            switch (key) {
              case "contributor":
                key = "author";
              case "base_directory":
                continue;
              default:
                break;
            }
            List<String> values = metadata.get(key);
            if (values == null) {
              values = new ArrayList<>();
              metadata.put(key, values);
            }
            values.add(getFirstElementText(row, "td"));
          }
          break;
        case "EBook Files":
          final Elements rows = table.getElementsByTag("tr");
          final List<String> headers = new ArrayList<>();
          for (Element header : rows.get(0).getElementsByTag("th")) {
            headers.add(makeKey(header.text()));
          }
          for (Element row : rows.subList(1, rows.size())) {
            final Elements columns = row.getElementsByTag("td");
            String contentType = null;
            String filename = null;
            for (int i = 0; i < columns.size(); ++i) {
              final String value = columns.get(i).text();
              switch (headers.get(i)) {
                case "format":
                  contentType = value;
                  break;
                case "file_name":
                  filename = value;
                  break;
              }
            }
            formats.add(Pair.pair(contentType, filename));
          }
          break;
        default:
          throw new IllegalStateException("unrecognized table caption: '" + caption + "'");
      }
    }

    List<String> links = new ArrayList<>();
    metadata.put("link", links);
    for (Element link : doc.select("a[href^=http://www.gutenberg.org/")) {
      links.add(link.attr("href"));
    }

    final int etextNo = MetadataStore.getEtextNumber(metadata);
    if (!metadataStore.containsKey(etextNo)) {
      metadataStore.append(metadata);
    }
    if (!formatStore.containsKey(etextNo)) {
      formatStore.append(etextNo, formats);
    }
  }

  private static String getFirstElementText(Element elt, String tag) {
    final Elements captions = elt.getElementsByTag(tag);
    if (captions.size() == 1) {
      return captions.get(0).text();
    } else {
      throw new IllegalStateException("mulitple " + tag + " tags in " + elt.tagName());
    }
  }

  private static String makeKey(String text) {
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < text.length(); ++i) {
      final int ch = text.codePointAt(i);
      if (Character.isAlphabetic(ch)) {
        sb.appendCodePoint(Character.toLowerCase(ch));
      } else if (Character.isSpaceChar(ch) || ch == '-') {
        sb.append('_');
      }
    }
    return sb.toString();
  }

}
