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

package imagej.data.types;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test code for the {@link BigComplex} type.
 * 
 * @author Barry DeZonia
 */
public class BigComplexTest {

	private static final double PRECISION = 0.0000000000001;

	@Test
	public void testAdd() {
		BigComplex a = new BigComplex();
		BigComplex b = new BigComplex();

		a.setComplexNumber(5, 10);
		b.setComplexNumber(-3, 2);
		a.add(b);
		assertEquals(2, a.getRealDouble(), 0);
		assertEquals(12, a.getImaginaryDouble(), 0);
	}

	@Test
	public void testSub() {
		BigComplex a = new BigComplex();
		BigComplex b = new BigComplex();

		a.setComplexNumber(5, 10);
		b.setComplexNumber(-3, 2);
		a.sub(b);
		assertEquals(8, a.getRealDouble(), 0);
		assertEquals(8, a.getImaginaryDouble(), 0);
	}

	@Test
	public void testMul() {
		BigComplex a = new BigComplex();
		BigComplex b = new BigComplex();

		a.setComplexNumber(5, 0);
		b.setComplexNumber(-3, 0);
		a.mul(b);
		assertEquals(-15, a.getRealDouble(), 0);
		assertEquals(0, a.getImaginaryDouble(), 0);
		
		a.setComplexNumber(4, 3);
		b.setComplexNumber(9, 1);
		a.mul(b);
		assertEquals(33, a.getRealDouble(), 0);
		assertEquals(31, a.getImaginaryDouble(), 0);
	}

	@Test
	public void testDiv() {
		BigComplex a = new BigComplex();
		BigComplex b = new BigComplex();

		a.setComplexNumber(5, 0);
		b.setComplexNumber(-2, 0);
		a.div(b);
		assertEquals(-2.5, a.getRealDouble(), 0);
		assertEquals(0, a.getImaginaryDouble(), 0);

		a.setComplexNumber(4, 3);
		b.setComplexNumber(9, 1);
		a.div(b);
		assertEquals(39 / 82.0, a.getRealDouble(), 0);
		assertEquals(23 / 82.0, a.getImaginaryDouble(), 0);
	}

	@Test
	public void testPhase() {
		BigComplex val;

		// the 90s

		val = new BigComplex(1, 0);
		assertEquals(0, val.getPhaseDouble(), PRECISION);
		val = new BigComplex(0, 1);
		assertEquals(Math.PI / 2, val.getPhaseDouble(), PRECISION);
		val = new BigComplex(-1, 0);
		assertEquals(Math.PI, val.getPhaseDouble(), PRECISION);
		val = new BigComplex(0, -1);
		assertEquals(3 * Math.PI / 2, val.getPhaseDouble(), PRECISION);

		// the 45s

		val = new BigComplex(1, 1);
		assertEquals(Math.PI / 4, val.getPhaseDouble(), PRECISION);
		val = new BigComplex(-1, 1);
		assertEquals(3 * Math.PI / 4, val.getPhaseDouble(), PRECISION);
		val = new BigComplex(-1, -1);
		assertEquals(5 * Math.PI / 4, val.getPhaseDouble(), PRECISION);
		val = new BigComplex(1, -1);
		assertEquals(7 * Math.PI / 4, val.getPhaseDouble(), PRECISION);

		// the 30s

		double half = 0.5;
		double root3over2 = Math.sqrt(3) / 2;

		val = new BigComplex(half, root3over2);
		assertEquals(2 * Math.PI / 6, val.getPhaseDouble(), PRECISION);
		val = new BigComplex(-half, root3over2);
		assertEquals(4 * Math.PI / 6, val.getPhaseDouble(), PRECISION);
		val = new BigComplex(half, -root3over2);
		assertEquals(10 * Math.PI / 6, val.getPhaseDouble(), PRECISION);
		val = new BigComplex(-half, -root3over2);
		assertEquals(8 * Math.PI / 6, val.getPhaseDouble(), PRECISION);

		val = new BigComplex(root3over2, half);
		assertEquals(Math.PI / 6, val.getPhaseDouble(), PRECISION);
		val = new BigComplex(-root3over2, half);
		assertEquals(5 * Math.PI / 6, val.getPhaseDouble(), PRECISION);
		val = new BigComplex(root3over2, -half);
		assertEquals(11 * Math.PI / 6, val.getPhaseDouble(), PRECISION);
		val = new BigComplex(-root3over2, -half);
		assertEquals(7 * Math.PI / 6, val.getPhaseDouble(), PRECISION);

		val = new BigComplex(7, -3);
		assertEquals(2 * Math.PI - Math.atan(3.0 / 7), val.getPhaseDouble(),
			PRECISION);
	}

	@Test
	public void testPower() {
		BigComplex val;

		val = new BigComplex(1, 0);
		assertEquals(1, val.getPowerDouble(), PRECISION);

		val = new BigComplex(2, 0);
		assertEquals(2, val.getPowerDouble(), PRECISION);

		val = new BigComplex(3, 0);
		assertEquals(3, val.getPowerDouble(), PRECISION);

		val = new BigComplex(4, 0);
		assertEquals(4, val.getPowerDouble(), PRECISION);

		val = new BigComplex(0, 1);
		assertEquals(1, val.getPowerDouble(), PRECISION);

		val = new BigComplex(0, 2);
		assertEquals(2, val.getPowerDouble(), PRECISION);

		val = new BigComplex(0, 3);
		assertEquals(3, val.getPowerDouble(), PRECISION);

		val = new BigComplex(0, 4);
		assertEquals(4, val.getPowerDouble(), PRECISION);

		val = new BigComplex(1, 1);
		assertEquals(Math.sqrt(2), val.getPowerDouble(), PRECISION);

		val = new BigComplex(2, 2);
		assertEquals(2 * Math.sqrt(2), val.getPowerDouble(), PRECISION);

		val = new BigComplex(3, 3);
		assertEquals(3 * Math.sqrt(2), val.getPowerDouble(), PRECISION);

		val = new BigComplex(4, 4);
		assertEquals(4 * Math.sqrt(2), val.getPowerDouble(), PRECISION);

		val = new BigComplex(7, -3);
		assertEquals(Math.sqrt(58), val.getPowerDouble(), PRECISION);
	}
}
