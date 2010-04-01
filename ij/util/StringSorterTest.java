package ij.util;

import java.util.*;

import static org.junit.Assert.*;

import org.junit.Test;

public class StringSorterTest {

	// make sure that StringSorter's implementation matches a known good sort algo: Java's Collections.sort
	private void testThisStringArray(String[] strings)
	{
	    String[] dupes = strings.clone();

		// create the baseline case from Java's API 
	    List<String> baselineList = Arrays.asList(strings);
		Collections.sort(baselineList);
		
		// create the IJ case from StringSorter
		StringSorter.sort(dupes);
		List<String> dupesList = Arrays.asList(dupes);
		
		// compare the two lists using equals()
		assertTrue(dupesList.equals(baselineList));
	}
	
	@Test
	public void testSortStringArray() {

		// zero items
		testThisStringArray(new String[] {});
		
		// one item
		testThisStringArray(new String[] {"1"});
		
		// some two item tests
		testThisStringArray(new String[] {"1","2"});
		testThisStringArray(new String[] {"2","1"});
		
		// some three item tests
		testThisStringArray(new String[] {"1","2","3"});
		testThisStringArray(new String[] {"1","3","2"});
		testThisStringArray(new String[] {"2","1","3"});
		testThisStringArray(new String[] {"2","3","1"});
		testThisStringArray(new String[] {"3","1","2"});
		testThisStringArray(new String[] {"3","2","1"});
		
		// some four item tests
		testThisStringArray(new String[] {"a","a","a","a"});
		testThisStringArray(new String[] {"1","0","1","0"});
		testThisStringArray(new String[] {"0","0","1","1"});
		testThisStringArray(new String[] {"1","1","0","0"});
		testThisStringArray(new String[] {"","","",""});
		
		// a random String array
		testThisStringArray(new String[] {"d4","--","81q","11x", "ASDF","#$%^","\\][|}{","PO23aa!"});
	}

}
