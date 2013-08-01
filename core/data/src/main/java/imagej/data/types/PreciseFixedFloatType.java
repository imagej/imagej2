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
import java.math.BigInteger;

import net.imglib2.type.numeric.RealType;

/**
 * A fixed point floating numeric type. Currently 25 decimal places of accuracy.
 * 
 * @author Barry DeZonia
 */
public class PreciseFixedFloatType implements RealType<PreciseFixedFloatType> {

	// TODO - use FloatingType rather than RealType but not yet merged to Imglib.
	// Once merged then implement the exponential and trig methods to a fixed
	// number of decimal places. Can take example code from BigComplex and then
	// dumb that class down to only be used for castings.

	// -- constants --

	private static final int DECIMAL_PLACES = 25; // TODO - make user configurable

	// -- fields --

	private BigInteger scale;
	private BigInteger amount;

	// -- constructors --

	public PreciseFixedFloatType() {
		scale = BigInteger.TEN.pow(DECIMAL_PLACES);
		amount = BigInteger.ZERO;
	}

	public PreciseFixedFloatType(PreciseFixedFloatType other) {
		this.scale = other.scale;
		this.amount = other.amount;
	}

	public PreciseFixedFloatType(long v) {
		this();
		set(v);
	}

	public PreciseFixedFloatType(double v) {
		this();
		set(v);
	}

	public PreciseFixedFloatType(BigInteger v) {
		this();
		set(v);
	}

	public PreciseFixedFloatType(BigDecimal v) {
		this();
		set(v);
	}

	// -- RealType methods --

	public BigDecimal get() {
		BigDecimal numer = new BigDecimal(amount);
		BigDecimal denom = new BigDecimal(scale);
		return numer.divide(denom);
	}

	@Override
	public double getRealDouble() {
		return get().doubleValue();
	}

	@Override
	public float getRealFloat() {
		return get().floatValue();
	}

	@Override
	public double getImaginaryDouble() {
		return 0;
	}

	@Override
	public float getImaginaryFloat() {
		return 0;
	}

	@Override
	public void setReal(float v) {
		set(v);
	}

	@Override
	public void setReal(double v) {
		set(v);
	}

	@Override
	public void setImaginary(float v) {
		// do nothing
	}

	@Override
	public void setImaginary(double v) {
		// do nothing
	}

	@Override
	public void setComplexNumber(float r, float i) {
		set(r);
	}

	@Override
	public void setComplexNumber(double r, double i) {
		set(r);
	}

	@Override
	public float getPowerFloat() {
		return getRealFloat();
	}

	@Override
	public double getPowerDouble() {
		return getRealDouble();
	}

	@Override
	public float getPhaseFloat() {
		return 0;
	}

	@Override
	public double getPhaseDouble() {
		return 0;
	}

	@Override
	public void complexConjugate() {
		// do nothing
	}

	@Override
	public void add(PreciseFixedFloatType v) {
		amount = amount.add(v.amount);
	}

	@Override
	public void sub(PreciseFixedFloatType v) {
		amount = amount.subtract(v.amount);
	}

	@Override
	public void mul(PreciseFixedFloatType v) {
		amount = amount.multiply(v.amount);
	}

	@Override
	public void div(PreciseFixedFloatType v) {
		amount = amount.divide(v.amount);
	}

	@Override
	public void setZero() {
		amount = BigInteger.ZERO;
	}

	@Override
	public void setOne() {
		this.amount = scale;
	}

	@Override
	public void mul(float v) {
		mul(BigDecimal.valueOf(v));
	}

	@Override
	public void mul(double v) {
		mul(BigDecimal.valueOf(v));
	}

	public void mul(BigInteger v) {
		amount = amount.multiply(v);
	}

	public void mul(BigDecimal v) {
		BigDecimal integer = new BigDecimal(amount);
		BigDecimal number = integer.multiply(v);
		amount = number.toBigInteger();
	}

	@Override
	public PreciseFixedFloatType createVariable() {
		return new PreciseFixedFloatType();
	}

	@Override
	public PreciseFixedFloatType copy() {
		return new PreciseFixedFloatType(this);
	}

	@Override
	public void set(PreciseFixedFloatType other) {
		this.scale = other.scale;
		this.amount = other.amount;
	}

	public void set(double v) {
		BigDecimal d = BigDecimal.valueOf(v);
		BigDecimal factor = new BigDecimal(scale);
		BigDecimal number = d.multiply(factor);
		amount = number.toBigInteger();
	}

	public void set(long v) {
		amount = BigInteger.valueOf(v).multiply(scale);
	}

	public void set(BigInteger v) {
		amount = v.multiply(scale);
	}

	public void set(BigDecimal v) {
		BigDecimal scaled = v.multiply(new BigDecimal(scale));
		amount = scaled.toBigInteger();
	}

	@Override
	public int compareTo(PreciseFixedFloatType other) {
		return amount.compareTo(other.amount);
	}

	@Override
	public void inc() {
		amount = amount.add(scale);
	}

	@Override
	public void dec() {
		amount = amount.subtract(scale);
	}

	@Override
	public double getMaxValue() {
		return Double.MAX_VALUE; // TODO - narrowing!
	}

	@Override
	public double getMinValue() {
		return -Double.MAX_VALUE; // TODO - narrowing!
	}

	@Override
	public double getMinIncrement() {
		return 1.0 / Math.pow(10, DECIMAL_PLACES); // TODO - prone to precision loss
	}

	@Override
	public int getBitsPerPixel() {
		return 1024; // TODO - a WAG : nothing makes sense here. Use DataType.
	}

	@Override
	public String toString() {
		return get().toString();
	}
}
