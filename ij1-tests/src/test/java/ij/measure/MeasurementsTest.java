//
// MeasurementsTest.java
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

package ij.measure;

import static org.junit.Assert.*;

import org.junit.Test;

public class MeasurementsTest {
	@Test
	public void testConstants()
	{
		assertEquals(1,Measurements.AREA);
		assertEquals(2,Measurements.MEAN);
		assertEquals(4,Measurements.STD_DEV);
		assertEquals(8,Measurements.MODE);
		assertEquals(16,Measurements.MIN_MAX);
		assertEquals(32,Measurements.CENTROID);
		assertEquals(64,Measurements.CENTER_OF_MASS);
		assertEquals(128,Measurements.PERIMETER);
		assertEquals(256,Measurements.LIMIT);
		assertEquals(512,Measurements.RECT);
		assertEquals(1024,Measurements.LABELS);
		assertEquals(2048,Measurements.ELLIPSE);
		assertEquals(4096,Measurements.INVERT_Y);
		assertEquals(8192,Measurements.CIRCULARITY);
		assertEquals(8192,Measurements.SHAPE_DESCRIPTORS);
		assertEquals(16384,Measurements.FERET);
		assertEquals(0x8000,Measurements.INTEGRATED_DENSITY);
		assertEquals(0x10000,Measurements.MEDIAN);
		assertEquals(0x20000,Measurements.SKEWNESS);
		assertEquals(0x40000,Measurements.KURTOSIS);
		assertEquals(0x80000,Measurements.AREA_FRACTION); 
		assertEquals(0x100000,Measurements.SLICE);
		assertEquals(0x100000,Measurements.STACK_POSITION);
		assertEquals(0x200000,Measurements.SCIENTIFIC_NOTATION);
		assertEquals(20,Measurements.MAX_STANDARDS);
	}
}
