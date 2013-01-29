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

package imagej.util;

import static org.junit.Assert.assertEquals;

/**
 * Abstract superclass for {@link PrimitiveArray}-based tests.
 * 
 * @author Curtis Rueden
 * @author Johannes Schindelin
 */
public abstract class PrimitiveArrayTest {

	/** Tests {@link PrimitiveArray#insert(int, int)}. */
	protected void testInsert(final PrimitiveArray<?, ?> array) {
		final int size = array.size();
		final Object e0 = array.get(0);
		final Object e2 = array.get(2);
		final Object e3 = array.get(3);
		final Object eN = array.get(size - 1);
		
		array.insert(size, 3);
		assertEquals(size + 3, array.size());
		assertEquals(e0, array.get(0));
		assertEquals(eN, array.get(size - 1));
		
		array.insert(3, 7);
		assertEquals(size + 10, array.size());
		assertEquals(e0, array.get(0));
		assertEquals(e3, array.get(10));
		assertEquals(eN, array.get(size + 6));

		array.insert(0, 5);
		assertEquals(size + 15, array.size());
		assertEquals(e0, array.get(5));
		assertEquals(e2, array.get(7));
		assertEquals(e3, array.get(15));
		assertEquals(eN, array.get(size + 11));
	}

	/** Tests {@link PrimitiveArray#delete(int, int)}. */
	protected void testDelete(final PrimitiveArray<?, ?> array) {
		final Object[] a = array.toArray();

		array.delete(a.length - 2, 2);
		assertEquals(a.length - 2, array.size());
		for (int i = 0; i < a.length - 2; i++) {
			assertEquals("@" + i, a[i], array.get(i));
		}
		
		array.delete(0, 2);
		assertEquals(a.length - 4, array.size());
		for (int i = 0; i < a.length - 4; i++) {
			assertEquals("@" + i, a[i + 2], array.get(i));
		}
	}

}
