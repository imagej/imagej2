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
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author Barry DeZonia
 */
public class PreciseFixedFloatTypeTest {

	private static final double PRECISION = 0.000000000001;

	@Test
	public void testAdd() {
		PreciseFixedFloatType a = new PreciseFixedFloatType();
		PreciseFixedFloatType b = new PreciseFixedFloatType();

		a.set(5);
		b.set(3);
		a.add(b);
		assertEquals(8, a.get().doubleValue(), 0);

		a.set(5);
		b.set(-3);
		a.add(b);
		assertEquals(2, a.get().doubleValue(), 0);

		a.set(-5);
		b.set(3);
		a.add(b);
		assertEquals(-2, a.get().doubleValue(), 0);

		a.set(-5);
		b.set(-3);
		a.add(b);
		assertEquals(-8, a.get().doubleValue(), 0);
	}

	@Test
	public void testSub() {
		PreciseFixedFloatType a = new PreciseFixedFloatType();
		PreciseFixedFloatType b = new PreciseFixedFloatType();

		a.set(5);
		b.set(3);
		a.sub(b);
		assertEquals(2, a.get().doubleValue(), 0);

		a.set(5);
		b.set(-3);
		a.sub(b);
		assertEquals(8, a.get().doubleValue(), 0);

		a.set(-5);
		b.set(3);
		a.sub(b);
		assertEquals(-8, a.get().doubleValue(), 0);

		a.set(-5);
		b.set(-3);
		a.sub(b);
		assertEquals(-2, a.get().doubleValue(), 0);
	}

	@Test
	public void testMul() {
		PreciseFixedFloatType a = new PreciseFixedFloatType();
		PreciseFixedFloatType b = new PreciseFixedFloatType();

		a.set(5);
		b.set(3);
		a.mul(b);
		assertEquals(15, a.get().doubleValue(), 0);

		a.set(5);
		b.set(-3);
		a.mul(b);
		assertEquals(-15, a.get().doubleValue(), 0);

		a.set(-5);
		b.set(3);
		a.mul(b);
		assertEquals(-15, a.get().doubleValue(), 0);

		a.set(-5);
		b.set(-3);
		a.mul(b);
		assertEquals(15, a.get().doubleValue(), 0);
	}

	@Test
	public void testDiv() {
		PreciseFixedFloatType a = new PreciseFixedFloatType();
		PreciseFixedFloatType b = new PreciseFixedFloatType();

		a.set(5);
		b.set(2);
		a.div(b);
		assertEquals(2.5, a.get().doubleValue(), 0);

		a.set(5);
		b.set(-2);
		a.div(b);
		assertEquals(-2.5, a.get().doubleValue(), 0);

		a.set(-5);
		b.set(2);
		a.div(b);
		assertEquals(-2.5, a.get().doubleValue(), 0);

		a.set(-5);
		b.set(-2);
		a.div(b);
		assertEquals(2.5, a.get().doubleValue(), 0);
	}

	@Test
	public void testPow() {
		PreciseFixedFloatType a = new PreciseFixedFloatType();

		a.set(1);
		a.pow(0);
		assertEquals(1, a.get().doubleValue(), 0);

		a.set(1);
		a.pow(1);
		assertEquals(1, a.get().doubleValue(), 0);

		a.set(1);
		a.pow(2);
		assertEquals(1, a.get().doubleValue(), 0);

		a.set(3);
		a.pow(0);
		assertEquals(1, a.get().doubleValue(), 0);

		a.set(3);
		a.pow(1);
		assertEquals(3, a.get().doubleValue(), 0);

		a.set(3);
		a.pow(2);
		assertEquals(9, a.get().doubleValue(), 0);

		a.set(-3);
		a.pow(0);
		assertEquals(1, a.get().doubleValue(), 0);

		a.set(-3);
		a.pow(1);
		assertEquals(-3, a.get().doubleValue(), 0);

		a.set(-3);
		a.pow(2);
		assertEquals(9, a.get().doubleValue(), 0);

		a.set(0.1);
		a.pow(-1);
		assertEquals(10, a.get().doubleValue(), 0);

		a.set(0.1);
		a.pow(-2);
		assertEquals(100, a.get().doubleValue(), 0);

		a.set(0.1);
		a.pow(-3);
		assertEquals(1000, a.get().doubleValue(), 0);

		a.set(10);
		a.pow(-3);
		assertEquals(0.001, a.get().doubleValue(), 0);
	}

	@Test
	public void testAbs() {

		PreciseFixedFloatType input = new PreciseFixedFloatType();
		PreciseFixedFloatType refVal = new PreciseFixedFloatType();

		refVal.set(4.5);
		input.set(4.5);
		input.abs();
		assertTrue(input.compareTo(refVal) == 0);

		refVal.set(4.5);
		input.set(-4.5);
		input.abs();
		assertTrue(input.compareTo(refVal) == 0);
	}

	@Test
	public void testNegate() {
		PreciseFixedFloatType input = new PreciseFixedFloatType();
		PreciseFixedFloatType refVal = new PreciseFixedFloatType();

		refVal.set(4.5);
		input.set(-4.5);
		input.negate();
		assertTrue(input.compareTo(refVal) == 0);

		refVal.set(-4.5);
		input.set(4.5);
		input.negate();
		assertTrue(input.compareTo(refVal) == 0);
	}

	@Test
	public void testAtan2() {

		PreciseFixedFloatType y = new PreciseFixedFloatType();
		PreciseFixedFloatType x = new PreciseFixedFloatType();
		PreciseFixedFloatType val;

		// the 90s

		y.set(0);
		x.set(1);
		val = PreciseFixedFloatType.atan2(y, x);
		assertEquals(0, val.getPowerDouble(), PRECISION);

		y.set(1);
		x.set(0);
		val = PreciseFixedFloatType.atan2(y, x);
		assertEquals(Math.PI / 2, val.getPowerDouble(), PRECISION);

		y.set(0);
		x.set(-1);
		val = PreciseFixedFloatType.atan2(y, x);
		assertEquals(Math.PI, val.getPowerDouble(), PRECISION);

		y.set(-1);
		x.set(0);
		val = PreciseFixedFloatType.atan2(y, x);
		assertEquals(3 * Math.PI / 2, val.getPowerDouble(), PRECISION);

		// the 45s

		y.set(1);
		x.set(1);
		val = PreciseFixedFloatType.atan2(y, x);
		assertEquals(Math.PI / 4, val.getPowerDouble(), PRECISION);

		y.set(1);
		x.set(-1);
		val = PreciseFixedFloatType.atan2(y, x);
		assertEquals(3 * Math.PI / 4, val.getPowerDouble(), PRECISION);

		y.set(-1);
		x.set(-1);
		val = PreciseFixedFloatType.atan2(y, x);
		assertEquals(5 * Math.PI / 4, val.getPowerDouble(), PRECISION);

		y.set(-1);
		x.set(1);
		val = PreciseFixedFloatType.atan2(y, x);
		assertEquals(7 * Math.PI / 4, val.getPowerDouble(), PRECISION);

		// the 30s

		double half = 0.5;
		double root3over2 = Math.sqrt(3) / 2;

		y.set(root3over2);
		x.set(half);
		val = PreciseFixedFloatType.atan2(y, x);
		assertEquals(2 * Math.PI / 6, val.getPowerDouble(), PRECISION);

		y.set(root3over2);
		x.set(-half);
		val = PreciseFixedFloatType.atan2(y, x);
		assertEquals(4 * Math.PI / 6, val.getPowerDouble(), PRECISION);

		y.set(-root3over2);
		x.set(half);
		val = PreciseFixedFloatType.atan2(y, x);
		assertEquals(10 * Math.PI / 6, val.getPowerDouble(), PRECISION);

		y.set(-root3over2);
		x.set(-half);
		val = PreciseFixedFloatType.atan2(y, x);
		assertEquals(8 * Math.PI / 6, val.getPowerDouble(), PRECISION);

		y.set(half);
		x.set(root3over2);
		val = PreciseFixedFloatType.atan2(y, x);
		assertEquals(Math.PI / 6, val.getPowerDouble(), PRECISION);

		y.set(half);
		x.set(-root3over2);
		val = PreciseFixedFloatType.atan2(y, x);
		assertEquals(5 * Math.PI / 6, val.getPowerDouble(), PRECISION);

		y.set(-half);
		x.set(root3over2);
		val = PreciseFixedFloatType.atan2(y, x);
		assertEquals(11 * Math.PI / 6, val.getPowerDouble(), PRECISION);

		y.set(-half);
		x.set(-root3over2);
		val = PreciseFixedFloatType.atan2(y, x);
		assertEquals(7 * Math.PI / 6, val.getPowerDouble(), PRECISION);
	}

	@Test
	public void testSqrt() {

		PreciseFixedFloatType val;

		val = new PreciseFixedFloatType(1);
		assertEquals(1, PreciseFixedFloatType.sqrt(val).getPowerDouble(), PRECISION);

		val = new PreciseFixedFloatType(4);
		assertEquals(2, PreciseFixedFloatType.sqrt(val).getPowerDouble(), PRECISION);

		val = new PreciseFixedFloatType(9);
		assertEquals(3, PreciseFixedFloatType.sqrt(val).getPowerDouble(), PRECISION);

		val = new PreciseFixedFloatType(16);
		assertEquals(4, PreciseFixedFloatType.sqrt(val).getPowerDouble(), PRECISION);

		val = new PreciseFixedFloatType(2);
		assertEquals(Math.sqrt(2),
			PreciseFixedFloatType.sqrt(val).getPowerDouble(), PRECISION);

		val = new PreciseFixedFloatType(8);
		assertEquals(2 * Math.sqrt(2), PreciseFixedFloatType.sqrt(val)
			.getPowerDouble(), PRECISION);

		val = new PreciseFixedFloatType(18);
		assertEquals(3 * Math.sqrt(2), PreciseFixedFloatType.sqrt(val)
			.getPowerDouble(), PRECISION);

		val = new PreciseFixedFloatType(32);
		assertEquals(4 * Math.sqrt(2), PreciseFixedFloatType.sqrt(val)
			.getPowerDouble(), PRECISION);
	}

}
