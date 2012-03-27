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

package imagej.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests {@link UnitUtils}.
 * 
 * @author Curtis Rueden
 */
public class UnitUtilsTest {

	@Test
	public void testGetAbbreviatedByteLabel() {
		assertEquals("0B", UnitUtils.getAbbreviatedByteLabel(0));
		assertEquals("1B", UnitUtils.getAbbreviatedByteLabel(1));
		assertEquals("123B", UnitUtils.getAbbreviatedByteLabel(123));

		assertEquals("10B", UnitUtils.getAbbreviatedByteLabel(1e1));
		assertEquals("100B", UnitUtils.getAbbreviatedByteLabel(1e2));
		assertEquals("1000B", UnitUtils.getAbbreviatedByteLabel(1e3));
		assertEquals("9.8KiB", UnitUtils.getAbbreviatedByteLabel(1e4));
		assertEquals("97.7KiB", UnitUtils.getAbbreviatedByteLabel(1e5));
		assertEquals("976.6KiB", UnitUtils.getAbbreviatedByteLabel(1e6));
		assertEquals("9.5MiB", UnitUtils.getAbbreviatedByteLabel(1e7));
		assertEquals("95.4MiB", UnitUtils.getAbbreviatedByteLabel(1e8));
		assertEquals("953.7MiB", UnitUtils.getAbbreviatedByteLabel(1e9));
		assertEquals("9.3GiB", UnitUtils.getAbbreviatedByteLabel(1e10));
		assertEquals("93.1GiB", UnitUtils.getAbbreviatedByteLabel(1e11));
		assertEquals("931.3GiB", UnitUtils.getAbbreviatedByteLabel(1e12));
		assertEquals("9.1TiB", UnitUtils.getAbbreviatedByteLabel(1e13));
		assertEquals("90.9TiB", UnitUtils.getAbbreviatedByteLabel(1e14));
		assertEquals("909.5TiB", UnitUtils.getAbbreviatedByteLabel(1e15));
		assertEquals("8.9PiB", UnitUtils.getAbbreviatedByteLabel(1e16));
		assertEquals("88.8PiB", UnitUtils.getAbbreviatedByteLabel(1e17));
		assertEquals("888.2PiB", UnitUtils.getAbbreviatedByteLabel(1e18));
		assertEquals("8.7EiB", UnitUtils.getAbbreviatedByteLabel(1e19));
		assertEquals("86.7EiB", UnitUtils.getAbbreviatedByteLabel(1e20));
		assertEquals("867.4EiB", UnitUtils.getAbbreviatedByteLabel(1e21));
		assertEquals("8.5ZiB", UnitUtils.getAbbreviatedByteLabel(1e22));
		assertEquals("84.7ZiB", UnitUtils.getAbbreviatedByteLabel(1e23));
		assertEquals("847.0ZiB", UnitUtils.getAbbreviatedByteLabel(1e24));
		assertEquals("8.3YiB", UnitUtils.getAbbreviatedByteLabel(1e25));
		assertEquals("82.7YiB", UnitUtils.getAbbreviatedByteLabel(1e26));
		assertEquals("827.2YiB", UnitUtils.getAbbreviatedByteLabel(1e27));
		assertEquals("8271.8YiB", UnitUtils.getAbbreviatedByteLabel(1e28));
		assertEquals("82718.1YiB", UnitUtils.getAbbreviatedByteLabel(1e29));
		assertEquals("827180.6YiB", UnitUtils.getAbbreviatedByteLabel(1e30));
	}

}
