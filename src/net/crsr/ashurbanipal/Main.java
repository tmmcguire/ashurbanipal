package net.crsr.ashurbanipal;

import java.io.File;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.crsr.ashurbanipal.reader.GutenbergLicenseReader;
import net.crsr.ashurbanipal.reader.ZippedTextFileReader;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class Main {

	public static void main(String[] args) {
		try {

			final MaxentTagger tagger = new MaxentTagger("net/crsr/ashurbanipal/tagger/english-left3words-distsim.tagger");
			final Morphology morphology = new Morphology();

			final Reader ztfr = new ZippedTextFileReader(new File(args[0]));
			final Reader reader = new GutenbergLicenseReader(ztfr);
			
		    final DocumentPreprocessor documentPreprocessor = new DocumentPreprocessor(reader);
			final TokenizerFactory<CoreLabel> ptbTokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "asciiQuotes,untokenizable=noneKeep");
		    documentPreprocessor.setTokenizerFactory(ptbTokenizerFactory);
		    
		    final Map<String,Map<String,Integer>> wordbags = new TreeMap<>();
		    double words = 0;
		    final Map<String,Integer> nouns = new HashMap<>();
		    
		    for (List<HasWord> sentence : documentPreprocessor) {
		    	final List<TaggedWord> tSentence = tagger.tagSentence(sentence);
		        for (TaggedWord word : tSentence) {
		        	words++;
		        	
		        	String lemma = morphology.stem(word).toString();
		        	if (lemma == null) {
		        		lemma = word.toString();
		        	}
		        	
		        	String tag = word.tag();
		        	
		        	Map<String,Integer> wordbag = wordbags.get(tag);
		        	if (wordbag == null) {
		        		wordbag = new TreeMap<>();
		        		wordbags.put(tag, wordbag);
		        	}
		        	Integer count = wordbag.get(lemma);
	        		wordbag.put(lemma, (count != null) ? count + 1 : 1);
	        		
	        		if (tag.startsWith("NN") && !tag.equals("NNP") && !tag.equals("NNPS")) {
	        			count = nouns.get(lemma);
	        			nouns.put(lemma, (count != null) ? count + 1 : 1);
	        		}
		        }
		    }
		    
		    for (Entry<String, Map<String,Integer>> entry : wordbags.entrySet()) {
		    	System.out.format("%s\n", entry.getKey());
		    	double posCount = 0;
		    	for (Entry<String,Integer> subentry : entry.getValue().entrySet()) {
		    		System.out.format("  %s\t%d\n", subentry.getKey(), subentry.getValue());
		    		posCount += subentry.getValue();
		    	}
		    	System.out.format("%s\t%f\n", entry.getKey(), posCount / words);
		    }
		    
		    System.out.println("NOUNS");
		    for (Entry<String, Integer> entry : nouns.entrySet()) {
		    	System.out.format("%d\t%s\n", entry.getValue(), entry.getKey());
		    }

		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace(System.err);
		}
	}

}
