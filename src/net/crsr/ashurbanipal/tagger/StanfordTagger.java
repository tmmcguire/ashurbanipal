package net.crsr.ashurbanipal.tagger;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

/**
 * Process texts through the MaxentTagger, generating a proportional tag map and a set of noun/verb word counts.
 */
public class StanfordTagger {
	
	private final TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "asciiQuotes,untokenizable=noneKeep");
	private final MaxentTagger tagger = new MaxentTagger("net/crsr/ashurbanipal/tagger/english-left3words-distsim.tagger");
	private final Morphology morphology = new Morphology();
	
	public StanfordTaggerResult process(String filename, Reader text) {
	    final DocumentPreprocessor documentPreprocessor = new DocumentPreprocessor(text);
	    documentPreprocessor.setTokenizerFactory(tokenizerFactory);

	    double words = 0.0;
	    final Map<String,Double> tagCounts = new TreeMap<String, Double>();
	    final Map<String,Map<String,Integer>> wordBags = new TreeMap<>();
	    for (List<HasWord> sentence : documentPreprocessor) {
	    	for (TaggedWord word : tagger.tagSentence(sentence)) {
	    		// word count
	    		words++;

	    		// tag counts
	    		final String tag = word.tag();
	        	tagCounts.put(tag, tagCounts.getOrDefault(tag, 0.0) + 1.0);
	        	
	        	// noun/verb word bags
	        	if ("NN".equals(tag) || "NNS".equals(tag) || tag.startsWith("VB")) {
	        		// get base form of word
	        		String lemma = morphology.stem(word).toString();
	        		if (lemma == null) {
	        			lemma = word.toString();
	        		}
	        		// get bag for words of this POS
	        		Map<String,Integer> wordBag = wordBags.get(tag);
	        		if (wordBag == null) {
	        			wordBag = new HashMap<>();
	        			wordBags.put(tag, wordBag);
	        		}
	        		// increment count
	        		wordBag.put(lemma, wordBag.getOrDefault(lemma, 0) + 1);
	        	}
	        }
	    }
		return new StanfordTaggerResult(filename, tagProportions(words, tagCounts), wordBags);
	}
	
	private static Map<String,Double> tagProportions(double words, Map<String,Double> result) {
		for (Entry<String,Double> entry : result.entrySet()) {
			entry.setValue( entry.getValue() / words );
		}
		return result;
	}
}
