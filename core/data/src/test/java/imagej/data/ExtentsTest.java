/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
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

package imagej.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for {@link Extents}.
 * 
 * @author Barry DeZonia
 */
public class ExtentsTest {

	private Extents ext;

	@Test
	public void testExtents() {
		ext = new Extents(new long[] {});
		ext = new Extents(new long[] { 1 });
		ext = new Extents(new long[] { 1, 2 });
		ext = new Extents(new long[] { 1, 2, 3 });
		ext = new Extents(new long[] { 1, 2, 3, 4 });
		assertTrue(true);
	}

	@Test
	public void testCreatePosition() {
		ext = new Extents(new long[] { 1, 2, 3 });
		final Position pos = ext.createPosition();
		assertEquals(ext.numDimensions(), pos.numDimensions());
	}

	@Test
	public void testNumDimensions() {
		ext = new Extents(new long[] {});
		assertEquals(0, ext.numDimensions());
		ext = new Extents(new long[] { 1 });
		assertEquals(1, ext.numDimensions());
		ext = new Extents(new long[] { 1, 2 });
		assertEquals(2, ext.numDimensions());
		ext = new Extents(new long[] { 1, 2, 3 });
		assertEquals(3, ext.numDimensions());
		ext = new Extents(new long[] { 1, 2, 3, 4 });
		assertEquals(4, ext.numDimensions());
	}

	@Test
	public void testDimension() {
		ext = new Extents(new long[] { 1, 2, 3, 4 });
		assertEquals(1, ext.dimension(0));
		assertEquals(2, ext.dimension(1));
		assertEquals(3, ext.dimension(2));
		assertEquals(4, ext.dimension(3));
	}

	@Test
	public void testDimensions() {
		ext = new Extents(new long[] { 1, 2, 3, 4 });
		final long[] dims = new long[ext.numDimensions()];
		ext.dimensions(dims);
		assertEquals(4, dims.length);
		assertEquals(1, dims[0]);
		assertEquals(2, dims[1]);
		assertEquals(3, dims[2]);
		assertEquals(4, dims[3]);
	}

	@Test
	public void testNumElements() {
		ext = new Extents(new long[] {});
		assertEquals(0, ext.numElements());
		ext = new Extents(new long[] { 7 });
		assertEquals(7, ext.numElements());
		ext = new Extents(new long[] { 7, 5 });
		assertEquals(35, ext.numElements());
		ext = new Extents(new long[] { 7, 5, 3 });
		assertEquals(105, ext.numElements());
		ext = new Extents(new long[] { 7, 5, 3, 1 });
		assertEquals(105, ext.numElements());
	}

}
