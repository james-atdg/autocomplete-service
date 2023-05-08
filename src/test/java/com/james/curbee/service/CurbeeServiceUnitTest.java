package com.james.curbee.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CurbeeServiceUnitTest {

	private Logger logger = LoggerFactory.getLogger(CurbeeServiceUnitTest.class);
	
	@Autowired
	CurbeeService service;
	
	final long maxDeviationShortSearchStringMs = 500;
	final long maxDeviationLongSearchStringMs = 1000;
	
	@Test
	public void givenADictionaryOfWords_whenDictionaryIsIndexed_thenValidateAllWordsArePresentInIndex() {
		List<String> missingWords = new ArrayList<String>();
		Set<String> indexWords = new HashSet<String>();
		service.getIndex().entrySet().forEach(entry -> indexWords.addAll(entry.getValue()));
		service.getWordList().forEach(word -> {
			if (!indexWords.contains(word)) missingWords.add(word);
		});
		
		assertEquals(0, missingWords.size());
	}
	
	@Test
	public void givenDictionaryIndex_whenQueriedForShortSearchStrings_thenResponseTimesShouldBeConsistent() {
		long minShortQueryTime = 0L; //short search strings are those < than service.maxSearchStringLength
		long maxShortQueryTime = 0L;
		for (String key : service.getIndexKeys()) {
			Long start = Instant.now().toEpochMilli();
			boolean shortPrefix = key.length() < service.maxSearchStringLength;
			if (shortPrefix) {
				@SuppressWarnings("unused")
				List<String> matches = service.searchWords(key);
				Long duration = Instant.now().toEpochMilli() - start;
				
				if (duration < minShortQueryTime) minShortQueryTime = duration;
				if (duration > maxShortQueryTime) maxShortQueryTime = duration;
			}
		}
		
		logger.info("Query time deviation for search strings < " + service.maxSearchStringLength + " chars: " + (maxShortQueryTime - minShortQueryTime));
		
		assertTrue(maxShortQueryTime - minShortQueryTime < maxDeviationShortSearchStringMs); //deviation (ms) for short search string matches 
	}
	
	@Test
	public void givenDictionaryIndex_whenQueriedForLongSearchStrings_thenResponseTimesShouldBeConsistent() {
		long minLongQueryTime = 0L; //long strings are those >= than service.maxSearchStringLength which requires additional comparisons
		long maxLongQueryTime = 0L;
		
		for (String key : service.getIndexKeys()) {
			Long start = Instant.now().toEpochMilli();
			boolean longPrefix = key.length() <= service.maxSearchStringLength;
			if (longPrefix)  {
				@SuppressWarnings("unused")
				List<String> matches = service.searchWords(key);
				Long duration = Instant.now().toEpochMilli() - start;
				
				if (duration < minLongQueryTime) minLongQueryTime = duration;
				if (duration > maxLongQueryTime) maxLongQueryTime = duration;
			}
		}
		
		logger.info("Query time deviation for search strings >= " + service.maxSearchStringLength + " chars: " + (maxLongQueryTime - minLongQueryTime));
		
		assertTrue(maxLongQueryTime - minLongQueryTime < maxDeviationLongSearchStringMs); //deviation (ms) for long search strings matches
	}
	
}
