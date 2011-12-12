//
// UnitUtilsTest.java
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
		assertEquals("9.8KB", UnitUtils.getAbbreviatedByteLabel(1e4));
		assertEquals("97.7KB", UnitUtils.getAbbreviatedByteLabel(1e5));
		assertEquals("976.6KB", UnitUtils.getAbbreviatedByteLabel(1e6));
		assertEquals("9.5MB", UnitUtils.getAbbreviatedByteLabel(1e7));
		assertEquals("95.4MB", UnitUtils.getAbbreviatedByteLabel(1e8));
		assertEquals("953.7MB", UnitUtils.getAbbreviatedByteLabel(1e9));
		assertEquals("9.3GB", UnitUtils.getAbbreviatedByteLabel(1e10));
		assertEquals("93.1GB", UnitUtils.getAbbreviatedByteLabel(1e11));
		assertEquals("931.3GB", UnitUtils.getAbbreviatedByteLabel(1e12));
		assertEquals("9.1TB", UnitUtils.getAbbreviatedByteLabel(1e13));
		assertEquals("90.9TB", UnitUtils.getAbbreviatedByteLabel(1e14));
		assertEquals("909.5TB", UnitUtils.getAbbreviatedByteLabel(1e15));
		assertEquals("8.9PB", UnitUtils.getAbbreviatedByteLabel(1e16));
		assertEquals("88.8PB", UnitUtils.getAbbreviatedByteLabel(1e17));
		assertEquals("888.2PB", UnitUtils.getAbbreviatedByteLabel(1e18));
		assertEquals("8.7EB", UnitUtils.getAbbreviatedByteLabel(1e19));
		assertEquals("86.7EB", UnitUtils.getAbbreviatedByteLabel(1e20));
		assertEquals("867.4EB", UnitUtils.getAbbreviatedByteLabel(1e21));
		assertEquals("8.5ZB", UnitUtils.getAbbreviatedByteLabel(1e22));
		assertEquals("84.7ZB", UnitUtils.getAbbreviatedByteLabel(1e23));
		assertEquals("847.0ZB", UnitUtils.getAbbreviatedByteLabel(1e24));
		assertEquals("8.3YB", UnitUtils.getAbbreviatedByteLabel(1e25));
		assertEquals("82.7YB", UnitUtils.getAbbreviatedByteLabel(1e26));
		assertEquals("827.2YB", UnitUtils.getAbbreviatedByteLabel(1e27));
		assertEquals("8271.8YB", UnitUtils.getAbbreviatedByteLabel(1e28));
		assertEquals("82718.1YB", UnitUtils.getAbbreviatedByteLabel(1e29));
		assertEquals("827180.6YB", UnitUtils.getAbbreviatedByteLabel(1e30));
	}

}
