package ij.util;

import java.util.*;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class StringSorterTest {

	private void testThisStringArray(String[] strings)
	{
	    List<String> baselineList = Arrays.asList(strings);
		Collections.sort(baselineList);
		
	    String[] dupes = strings.clone();
		StringSorter.sort(dupes);
		List<String> dupesList = Arrays.asList(dupes);
		
		assertTrue(dupesList.equals(baselineList));
	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSortStringArray() {

		testThisStringArray(new String[] {});
		testThisStringArray(new String[] {"1"});
		testThisStringArray(new String[] {"1","2"});
		testThisStringArray(new String[] {"2","1"});
		testThisStringArray(new String[] {"1","2","3"});
		testThisStringArray(new String[] {"1","3","2"});
		testThisStringArray(new String[] {"2","1","3"});
		testThisStringArray(new String[] {"2","3","1"});
		testThisStringArray(new String[] {"3","1","2"});
		testThisStringArray(new String[] {"3","2","1"});
		testThisStringArray(new String[] {"a","a","a","a"});
		testThisStringArray(new String[] {"1","0","1","0"});
		testThisStringArray(new String[] {"0","0","1","1"});
		testThisStringArray(new String[] {"1","1","0","0"});
		// Collections.sort() fails on the following code
		// testThisStringArray(new String[] {null,null,null,null});
		testThisStringArray(new String[] {"","","",""});
	}

}
