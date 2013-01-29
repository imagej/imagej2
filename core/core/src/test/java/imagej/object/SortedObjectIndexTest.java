/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.List;

import org.junit.Test;

/**
 * Tests {@link SortedObjectIndex}.
 * 
 * @author Curtis Rueden
 */
public class SortedObjectIndexTest {

	@Test
	public void testGetAllSorted() {
		final SortedObjectIndex<String> objectIndex =
			new SortedObjectIndex<String>(String.class);
		final String o1 = "quick";
		final String o2 = "brown";
		final String o3 = "fox";
		objectIndex.add(o1);
		objectIndex.add(o2);
		objectIndex.add(o3);
		final List<String> all = objectIndex.getAll();
		assertEquals(3, all.size());
		assertSame(o2, all.get(0));
		assertSame(o3, all.get(1));
		assertSame(o1, all.get(2));
	}

	@Test
	public void testDuplicates() {
		final SortedObjectIndex<String> objectIndex =
			new SortedObjectIndex<String>(String.class);
		final String o1 = "quick";
		final String o2 = "brown";
		final String o3 = "fox";
		objectIndex.add(o1);
		objectIndex.add(o2);
		objectIndex.add(o3);
		objectIndex.add(o2);
		objectIndex.add(o3);
		objectIndex.add(o1);
		objectIndex.add(o3);
		objectIndex.remove(o3);
		final List<String> all = objectIndex.getAll();
		assertEquals(6, all.size());
		assertSame(o2, all.get(0));
		assertSame(o2, all.get(1));
		assertSame(o3, all.get(2));
		assertSame(o3, all.get(3));
		assertSame(o1, all.get(4));
		assertSame(o1, all.get(5));
	}

}
