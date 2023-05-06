package com.james.curbee.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.james.curbee.exception.ErrorType;
import com.james.curbee.exception.ServiceException;

import jakarta.annotation.PostConstruct;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Date;

/**
 *  The {@code CurbeeService} class provides auto-complete for user supplied strings 
 *  against a known dictionary.
 *  <p>
 *  This implementation utilizes an indexing strategy to optimize the performance of 
 *  locating words in a given dictionary that start with a user supplied string.  To build 
 *  the index, at initialization time the service predetermines every possible starting sequence 
 *  up to a given maximum length (10 characters).  It then stores each in a Map 
 *  along with a Set containing each word having the starting sequence.  When a request is received, 
 *  the service determines if the string length is less than the maximum and, if so, 
 *  retrieves the Set associated with the starting sequence from the index.  If not, the service 
 *  shortens the string to the maximum length, retrieves the Set associated with the 
 *  shortened string, and then finds all words in this Set containing the full string. 
 *  </p>
 *  <p>
 *  This two tier strategy fully optimizes response times for more common use cases of 
 *  search strings up to 10 characters while still providing optimized performance as search 
 *  strings exceed the threshold.  Testing demonstrates highly consistent, very fast (<5ms generally) 
 *  response times regardless of search string length up to the threshold.  Response times remain 
 *  consistent and fast (<20ms) as the size increases above the threshold as well.  (Testing 
 *  performed on a MacBook Apple M2 w/ 8GB RAM. Results will vary depending on environment.)
 *  </p>
 */
@Service
public class CurbeeService {

	private Logger logger = LoggerFactory.getLogger(CurbeeService.class);
	
	@Value("classpath:dictionaries/scrabble.txt")
	Resource resourceFile;
	
    final List<String> words = new ArrayList<String>();
    final Map<String, HashSet<String>> index = new HashMap<>();
    final int maxMatches = 10;
    final int maxSearchStringLength = 10;
    
    @PostConstruct
    private void initialize() throws IOException {
    	InputStream resource = resourceFile.getInputStream();
	    try ( BufferedReader reader = new BufferedReader(
	      new InputStreamReader(resource)) ) {
	        reader.lines().forEach(line -> words.add(line));
	    }
	    buildIndex();
    }
    
    public int countWords() {
    	return words.size();
    }
    
    /**
     * Finds all words starting with the input string.
     *
     * @param pattern the string to search for
     */
    public List<String> searchWords(String pattern){
    	HashSet<String> potentialMatches = index.getOrDefault(pattern, new HashSet<String>());
    	if (potentialMatches.size() == 0) throw new ServiceException(ErrorType.I_AM_A_TEAPOT, "No suggestions found");
    	
    	if (pattern.length() < maxSearchStringLength)
    		return potentialMatches.stream()
    								.limit(maxMatches)
    								.collect(Collectors.toList());
    	else 
    		return potentialMatches.stream()
									.filter(word -> word.startsWith(pattern))
									.limit(maxMatches)
									.collect(Collectors.toList());
    }
    
    public Map<String, HashSet<String>> getIndex(){
    	return index;
    }
    
    public Set<String> getIndexKeys(){
    	return index.keySet();
    }
    
    public List<String> getWordList(){
    	return words;
    }
    
    /**
     * Builds the <em>index</em> index consisting of all possible
     * starting sequences up to <em>maxSearchStringLength</em> mapped to all <em>words</em>
     * starting with a given string.
     */
    private void buildIndex() {
    	Date start = new Date();
    	long totalRecordsInIndex = 0;
    	for (int prefixLength = 2; prefixLength <= maxSearchStringLength; prefixLength++) {
		    String currentPrefix = null;
		    for (String word : words) {
		    	if (word.length() >= prefixLength) {
			    	if (currentPrefix == null || !currentPrefix.equals(word.substring(0, prefixLength))) {
			    		currentPrefix = word.substring(0, prefixLength);
			    		index.put(currentPrefix, new HashSet<>(Arrays.asList(word)));
			    		totalRecordsInIndex++;
			    	}
			    	else {
			    		index.get(currentPrefix).add(word);
			    		totalRecordsInIndex++;
			    	}
		    	}
		    }
	    }
    	logger.info("Index initialization took (ms): " + ((new Date()).getTime() - start.getTime()));
    	logger.info("Dictionary record count: " + words.size());
    	logger.info("Index record count: " + totalRecordsInIndex);
    	
    }
    
}
