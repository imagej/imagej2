//
// StringSorterTest.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package ij.util;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

/**
 * Unit tests for {@link StringSorter}.
 *
 * @author Barry DeZonia
 */
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
