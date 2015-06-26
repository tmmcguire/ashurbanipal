# ashurbanipal
Exploring Project Guteberg with natural language processing

## DESCRIPTION

This project digs through the Project Gutenberg 2010 DVD, assembling
the information needed to make recommendations based on the texts. The
information includes:

* Metadata about the texts in the corpus, including title, author(s),
  subject(s), etext numbers, URLs within the Project Gutenberg
  repository, etc.

* Part-of-speech data about each English text in the corpus that is
  provided in a text/plain format (for initial ease of
  development). This part-of-speech data is collected by parsing the
  texts with the [Stanford Part-Of-Speech
  Tagger](http://nlp.stanford.edu/software/tagger.shtml) and counting
  the number of uses of each part-of-speech in the [Penn Treebank tag
  set](http://www.comp.leeds.ac.uk/amalgam/tagsets/upenn.html)
  extended to include numbers and other punctuation. (Basically, what
  the SPOST comes up with using the
  `english-left3words-distsim.tagger` model.

  Parts-of-speech for each word in the text are counted, then divided
  by the total number of words in the text to produce a vector of POS
  usage for each text. Style recommendations are based on Euclidian
  distance between two such vectors.

* Most-common-noun data about each English text in the corpus. While
  tagging, the nouns are lemmatized (reduced to a base form),
  accumulated, and counted, and the 200 most common are retained for
  each text. This set of nouns is assumed to describe the topics
  contained in the text.

  Topic recommendations are based on the overlap between two text's
  sets of common nouns, using the [Jaccard
  distance](https://en.wikipedia.org/wiki/Jaccard_index) metric in the
  web services. (This approach may be changed at some point; it
  consumes a great deal of memory and is slower than other suggested
  approaches.)

If you are interested in a starting point in the code, likely the best
would be the TagDirectory.java program, used to produce the raw
datasets.

**Note:** this mess needs cleaning badly. For several reasons,
including the use of the `langdetect` library to detect the language a
text is written in, despite that information being available from the
metadata. You can probably see the order in which things were done
here.

Further, the Stanford POS Tagger is used because it seems to be more
accurate than the other likely candidate, the [Apache
OpenNLP](https://opennlp.apache.org) project. However, OpenNLP seems
to be faster and to use less memory and it is unclear if the accuracy
difference is important. As well, the Stanford POS Tagger likes to
blow up on large texts.

## SEE ALSO

* [ashurbanipal.web.ui](https://github.com/tmmcguire/ashurbanipal.web.ui): Javascript client UI to the ashurbanipal.web interfaces.

* [ashurbanipal.web](https://github.com/tmmcguire/ashurbanipal.web): Java Servlet-based interface to Ashurbanipal data.

## AUTHOR

Tommy M. McGuire wrote this.

## LICENSE

GNU GPLv2 or later.
