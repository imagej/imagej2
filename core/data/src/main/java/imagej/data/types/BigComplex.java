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

import java.math.BigDecimal;
import java.math.RoundingMode;

import net.imglib2.type.numeric.ComplexType;


/**
 * A complex number that stores values in BigDecimal (arbitrary) precision. This
 * class is useful for supporting DataType translations with minimal data loss.
 * 
 * @author Barry DeZonia
 */
public class BigComplex implements ComplexType<BigComplex> {

	// TODO - if we implement FloatingType interface then this class will be
	// tricky to implement. acos, tanh, sin, atanh, etc.

	private BigDecimal r, i;

	public BigComplex() {
		setZero();
	}

	public BigComplex(BigDecimal r, BigDecimal i) {
		setReal(r);
		setImag(i);
	}

	public BigComplex(double r, double i) {
		setReal(BigDecimal.valueOf(r));
		setImag(BigDecimal.valueOf(i));
	}

	public BigDecimal getReal() {
		return r;
	}

	public BigDecimal getImag() {
		return i;
	}

	public void setReal(BigDecimal r) {
		this.r = r;
	}

	public void setImag(BigDecimal i) {
		this.i = i;
	}

	@Override
	public void add(BigComplex c) {
		r = r.add(c.r);
		i = i.add(c.i);
	}

	@Override
	public void sub(BigComplex c) {
		r = r.subtract(c.r);
		i = i.subtract(c.i);
	}

	@Override
	public void mul(BigComplex c) {
		BigDecimal a = r.multiply(c.r);
		BigDecimal b = i.multiply(c.i);
		BigDecimal sum1 = a.subtract(b);
		a = i.multiply(c.r);
		a = r.multiply(c.i);
		BigDecimal sum2 = a.add(b);
		r = sum1;
		i = sum2;
	}

	@Override
	public void div(BigComplex c) {
		BigDecimal a = c.r.multiply(c.r);
		BigDecimal b = c.r.multiply(c.r);
		BigDecimal denom = a.add(b);
		a = r.multiply(c.r);
		b = i.multiply(c.i);
		BigDecimal sum1 = a.add(b);
		a = i.multiply(c.r);
		b = r.multiply(c.i);
		BigDecimal sum2 = a.subtract(b);
		r = sum1.divide(denom);
		i = sum2.divide(denom);
	}

	@Override
	public void setZero() {
		r = BigDecimal.ZERO;
		i = BigDecimal.ZERO;
	}

	@Override
	public void setOne() {
		r = BigDecimal.ONE;
		i = BigDecimal.ZERO;
	}

	@Override
	public void mul(float c) {
		mul(new BigComplex(BigDecimal.valueOf(c), BigDecimal.ZERO));
	}

	@Override
	public void mul(double c) {
		mul(new BigComplex(BigDecimal.valueOf(c), BigDecimal.ZERO));
	}

	@Override
	public BigComplex createVariable() {
		return new BigComplex();
	}

	@Override
	public BigComplex copy() {
		return new BigComplex(r, i);
	}

	@Override
	public void set(BigComplex c) {
		this.r = c.r;
		this.i = c.i;
	}

	@Override
	public void complexConjugate() {
		i = i.negate();
	}

	// -- narrowing methods --

	@Override
	public double getRealDouble() {
		return r.doubleValue();
	}

	@Override
	public float getRealFloat() {
		return r.floatValue();
	}

	@Override
	public double getImaginaryDouble() {
		return i.doubleValue();
	}

	@Override
	public float getImaginaryFloat() {
		return i.floatValue();
	}

	@Override
	public void setReal(float f) {
		r = BigDecimal.valueOf(f);
	}

	@Override
	public void setReal(double f) {
		r = BigDecimal.valueOf(f);
	}

	@Override
	public void setImaginary(float f) {
		i = BigDecimal.valueOf(f);
	}

	@Override
	public void setImaginary(double f) {
		i = BigDecimal.valueOf(f);
	}

	@Override
	public void setComplexNumber(float r, float i) {
		setReal(r);
		setImaginary(i);
	}

	@Override
	public void setComplexNumber(double r, double i) {
		setReal(r);
		setImaginary(i);
	}

	@Override
	public float getPowerFloat() {
		return modulus().floatValue();
	}

	@Override
	public double getPowerDouble() {
		return modulus().doubleValue();
	}

	@Override
	public float getPhaseFloat() {
		return phase().floatValue();
	}

	@Override
	public double getPhaseDouble() {
		return phase().doubleValue();
	}

	// -- helpers --

	private BigDecimal modulus() {
		BigDecimal a = r.multiply(r);
		BigDecimal b = i.multiply(i);
		BigDecimal sum = a.add(b);
		return bigSqrt(sum);
	}

	private BigDecimal phase() {
		return atan2(i, r);
	}

	private static final BigDecimal SQRT_DIG = new BigDecimal(150);
	private static final BigDecimal SQRT_PRE = new BigDecimal(10).pow(SQRT_DIG
		.intValue());

	/**
	 * Uses Newton Raphson to compute the square root of a BigDecimal.
	 * 
	 * @author Luciano Culacciatti
	 * @url 
	 *      http://www.codeproject.com/Tips/257031/Implementing-SqrtRoot-in-BigDecimal
	 * @param c
	 * @return
	 */
	private static BigDecimal bigSqrt(BigDecimal c) {
		return sqrtNewtonRaphson(c, BigDecimal.ONE, BigDecimal.ONE.divide(SQRT_PRE));
	}
	
	/**
	 * Private utility method used to compute the square root of a BigDecimal.
	 * 
	 * @author Luciano Culacciatti 
	 * @url http://www.codeproject.com/Tips/257031/Implementing-SqrtRoot-in-BigDecimal
	 * @param c
	 * @param xn
	 * @param precision
	 * @return
	 */
	private static BigDecimal sqrtNewtonRaphson(BigDecimal c, BigDecimal xn,
		BigDecimal precision)
	{
	    BigDecimal fx = xn.pow(2).add(c.negate());
	    BigDecimal fpx = xn.multiply(new BigDecimal(2));
		BigDecimal xn1 =
			fx.divide(fpx, 2 * SQRT_DIG.intValue(), RoundingMode.HALF_DOWN);
	    xn1 = xn.add(xn1.negate());
	    BigDecimal currentSquare = xn1.pow(2);
	    BigDecimal currentPrecision = currentSquare.subtract(c);
	    currentPrecision = currentPrecision.abs();
	    if (currentPrecision.compareTo(precision) <= -1){
	        return xn1;
	    }
	    return sqrtNewtonRaphson(c, xn1, precision);
	}

	private static final BigDecimal TWO = new BigDecimal(2);
	private static final BigDecimal PI = new BigDecimal(
		"3.14159265358979323846264338327950288419716939937510");

	// NB - PI limited to 50 decimal places of precision so narrowing possible

	private BigDecimal atan2(BigDecimal y, BigDecimal x) {
		int xStatus = x.compareTo(BigDecimal.ZERO);
		int yStatus = y.compareTo(BigDecimal.ZERO);
		if (xStatus > 0) {
			return atan(y.divide(x));
		}
		else if (xStatus < 0) {
			if (yStatus >= 0) return atan(y.divide(x)).add(PI);
			return atan(y.divide(x)).subtract(PI);
		}
		else { // xStatus == 0
			if (yStatus > 0) return PI.divide(TWO);
			else if (yStatus < 0) return PI.divide(TWO).negate();
			else
				throw new IllegalArgumentException("atan2 undefined when x & y both zero");
		}
	}

	private BigDecimal atan(BigDecimal val) {
		throw new UnsupportedOperationException(
			"TODO - maybe use a cordic algorithm");
	}
}
