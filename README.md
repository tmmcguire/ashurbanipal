# ashurbanipal
Exploring Project Guteberg with natural language processing

## DESCRIPTION

This project digs through the Project Gutenberg 2010 DVD, assembling
the information needed to make recommendations based on the texts. The
information includes:

* Metadata about the texts in the corpus, including title, author(s),
  subject(s), etext numbers, URLs within the Project Gutenberg
  repository, etc.

  Another part of the metadata relates to the multiple formats in
  which Project Gutenberg texts are stored. Information is collected
  about the MIME format and location of each file for each text.

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
  
* Sentiment-over-time data about each text, following the lead of
  [Matthew Jockers'](http://www.matthewjockers.net/)
  [Syuzhet](http://www.matthewjockers.net/2015/02/02/syuzhet/) package
  (available from github as
  [mjockers/syuzhet](https://github.com/mjockers/syuzhet)).
  
  Ashurbanipal collects a sentiment score associated with each sentence in the
  text, in order, performs a FFT on that data, and then records the
  low-frequencies of the data to get an idea of the overall "plot" of the text.

If you are interested in a starting point in the code, likely the best
would be the [TagTodoList.java program][1], used to produce the raw
datasets.

[1]: https://github.com/tmmcguire/ashurbanipal/blob/master/src/net/crsr/ashurbanipal/TagTodoList.java

The Stanford POS Tagger is used because it seems to be more accurate
than the other likely candidate, the
[Apache OpenNLP](https://opennlp.apache.org) project. However, OpenNLP
seems to be faster and to use less memory and it is unclear if the
accuracy difference is important. As well, the Stanford POS Tagger
likes to blow up on large texts.

Unfortunately, the Stanford
[CoreNLP](http://stanfordnlp.github.io/CoreNLP/index.html) sentiment annotator
seems to be drastically too slow to use on this data set. The current
alternatives are [LingPipe](http://alias-i.com/lingpipe/) and dictionary look-up
based approaches from the Syuzhet package. (LingPipe doesn't currently work.)

In order to handle the last problem, each text is broken into smaller
chunks on paragraph boundaries (or, at least, empty lines) and the
results combined. Currently, this enables processing of the entire
Project Gutenberg 2010 DVD in something like eight hours on my laptop.

There are number of scripts in the root directory for either running
the Java programs correctly or intermediate processing of the data.

* `run-html-metadata`: given a directory of HTML Project Gutenberg
  metadata files (such as is on the DVD), create metadata and formats
  files.

* `run-tag-todolist`: process a to-do list, collecting the
  part-of-speech and noun data.

* `show-data`: using ashurbanipal's text-handling code, show the
  contents of a text file. (The Project Gutenberg header and footer
  should be stripped off.)

* `show-text`: using command-line zip utilities, show the contents of
  a text file.

* `style-lookup`: compute a recommendation list based on the
  part-of-speech data.

* `topic-lookup`: using the noun data, compute a recommendation list.

* `combined-lookup`: combine the results of both `style-lookup` and
  `topic-lookup` to produce an overall recommendation.

* `clean-data`: create a copy of the data set containing only complete
  information.

* `wordstore-to-bitset`: Convert the noun (word store) data to a
  bitset-style data file for use by the ashurbanipal.web project.

* `pick-content-type.awk`: given a raw to-do list file, pick out the
  "best" entries as far as content type goes.

* `join-tabs`: call the `join` command line utility with the magic
  necessary to operate on tab-separated files.

## SEE ALSO

* [ashurbanipal.web.ui](https://github.com/tmmcguire/ashurbanipal.web.ui):
  Javascript client UI to the ashurbanipal.web interfaces.

* [ashurbanipal.web](https://github.com/tmmcguire/ashurbanipal.web):
  Java Servlet-based interface to Ashurbanipal data. This is obsolete
  in favor of:

* [rust_ashurbanipal_web](https://github.com/tmmcguire/rust_ashurbanipal_web):
  Rust-based HTTP interface to Ashurbanipal data.

## AUTHOR

Tommy M. McGuire wrote this.

## LICENSE

GNU GPLv2 or later.
