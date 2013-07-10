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

import net.imglib2.type.numeric.RealType;

/**
 * @author Barry DeZonia
 */
public class UnboundedFloatType implements RealType<UnboundedFloatType> {

	// -- fields --

	private BigDecimal v = BigDecimal.ZERO;

	// -- accessors --

	public BigDecimal get() {
		return v;
	}

	public void set(BigDecimal val) {
		v = val;
	}

	// -- NumericType methods --

	@Override
	public UnboundedFloatType createVariable() {
		return new UnboundedFloatType();
	}

	@Override
	public UnboundedFloatType copy() {
		UnboundedFloatType copy = new UnboundedFloatType();
		copy.v = this.v;
		return copy;
	}

	@Override
	public void set(UnboundedFloatType c) {
		this.v = c.v;
	}

	@Override
	public void add(UnboundedFloatType c) {
		this.v = v.add(c.v);
	}

	@Override
	public void sub(UnboundedFloatType c) {
		this.v = v.subtract(c.v);
	}

	@Override
	public void mul(UnboundedFloatType c) {
		this.v = v.multiply(c.v);
	}

	@Override
	public void div(UnboundedFloatType c) {
		this.v = v.divide(c.v);
	}

	@Override
	public void setZero() {
		this.v = BigDecimal.ZERO;
	}

	@Override
	public void setOne() {
		this.v = BigDecimal.ONE;
	}

	@Override
	public void mul(float c) {
		this.v = this.v.multiply(new BigDecimal(c));
	}

	@Override
	public void mul(double c) {
		this.v = this.v.multiply(new BigDecimal(c));
	}

	// -- RealType methods --

	// Note that most of them are precision losing methods

	@Override
	public double getRealDouble() {
		return this.v.doubleValue();
	}

	@Override
	public float getRealFloat() {
		return this.v.floatValue();
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
	public void setReal(float f) {
		this.v = BigDecimal.valueOf(f);
	}

	@Override
	public void setReal(double f) {
		this.v = BigDecimal.valueOf(f);
	}

	@Override
	public void setImaginary(float f) {
		// do nothing
		// TODO - throw except if f != 0 ?
	}

	@Override
	public void setImaginary(double f) {
		// do nothing
		// TODO - throw except if f != 0 ?
	}

	@Override
	public void setComplexNumber(float r, float i) {
		setReal(r);
		// TODO - throw except if i != 0 ?
	}

	@Override
	public void setComplexNumber(double r, double i) {
		setReal(r);
		// TODO - throw except if i != 0 ?
	}

	@Override
	public float getPowerFloat() {
		return this.v.floatValue();
	}

	@Override
	public double getPowerDouble() {
		return this.v.doubleValue();
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
	public int compareTo(UnboundedFloatType o) {
		return this.v.compareTo(o.v);
	}

	@Override
	public void inc() {
		this.v = this.v.add(BigDecimal.ONE);
	}

	@Override
	public void dec() {
		this.v = this.v.subtract(BigDecimal.ONE);
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
		return Double.MIN_VALUE; // TODO - narrowing!
	}

	@Override
	public int getBitsPerPixel() {
		return 1024; // TODO arbitrary!
	}

}
