/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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
 * #L%
 */

package imagej.data.types;

import java.math.BigDecimal;

import net.imglib2.type.numeric.ComplexType;

/**
 * A fixed point complex float type. Its real and imaginary components are
 * {@link PreciseFixedFloatType}s.
 * 
 * @author Barry DeZonia
 */
public class PreciseFixedComplexFloatType implements
	ComplexType<PreciseFixedComplexFloatType>
{

	// -- fields --

	private PreciseFixedFloatType real;
	private PreciseFixedFloatType imag;

	// -- constructors --

	public PreciseFixedComplexFloatType() {
		real = new PreciseFixedFloatType();
		imag = new PreciseFixedFloatType();
	}

	public PreciseFixedComplexFloatType(double r, double i) {
		real = new PreciseFixedFloatType(r);
		imag = new PreciseFixedFloatType(i);
	}

	public PreciseFixedComplexFloatType(BigDecimal r, BigDecimal i) {
		real = new PreciseFixedFloatType(r);
		imag = new PreciseFixedFloatType(i);
	}

	public PreciseFixedComplexFloatType(String r, String i) {
		real = new PreciseFixedFloatType(r);
		imag = new PreciseFixedFloatType(i);
	}

	public PreciseFixedComplexFloatType(PreciseFixedComplexFloatType other) {
		this();
		set(other);
	}

	// -- accessors --

	public PreciseFixedFloatType getReal() {
		return real;
	}

	public PreciseFixedFloatType getImag() {
		return imag;
	}

	@Override
	public void setReal(double r) {
		real.set(r);
	}

	public void setImag(double i) {
		imag.set(i);
	}

	public void setReal(BigDecimal r) {
		real.set(r);
	}

	public void setImag(BigDecimal i) {
		imag.set(i);
	}

	public void setReal(PreciseFixedFloatType r) {
		real.set(r);
	}

	public void setImag(PreciseFixedFloatType i) {
		imag.set(i);
	}

	@Override
	public void set(PreciseFixedComplexFloatType other) {
		setReal(other.getReal());
		setImag(other.getImag());
	}

	// -- ComplexType methods --

	public void
		add(PreciseFixedComplexFloatType a, PreciseFixedComplexFloatType b)
	{
		real.add(a.getReal(), b.getReal());
		imag.add(a.getImag(), b.getImag());
	}

	@Override
	public void add(PreciseFixedComplexFloatType c) {
		add(this, c);
	}

	public void
		sub(PreciseFixedComplexFloatType a, PreciseFixedComplexFloatType b)
	{
		real.sub(a.getReal(), b.getReal());
		imag.sub(a.getImag(), b.getImag());
	}

	@Override
	public void sub(PreciseFixedComplexFloatType c) {
		sub(this, c);
	}

	// TODO - bad : tons of object creation.destruction per multiply. We should
	// allocate temps for each instance.

	public void
		mul(PreciseFixedComplexFloatType a, PreciseFixedComplexFloatType b)
	{
		PreciseFixedFloatType t1 = new PreciseFixedFloatType();
		PreciseFixedFloatType t2 = new PreciseFixedFloatType();
		PreciseFixedFloatType sum1 = new PreciseFixedFloatType();
		PreciseFixedFloatType sum2 = new PreciseFixedFloatType();
		t1.mul(a.getReal(), b.getReal());
		t2.mul(a.getImag(), b.getImag());
		sum1.sub(t1, t2);
		t1.mul(a.getImag(), b.getReal());
		t2.mul(a.getReal(), b.getImag());
		sum2.add(t1, t2);
		setReal(sum1);
		setImag(sum2);
	}

	@Override
	public void mul(PreciseFixedComplexFloatType c) {
		mul(this, c);
	}

	// TODO - bad : tons of object creation.destruction per divide. We should
	// allocate temps for each instance.

	public void
		div(PreciseFixedComplexFloatType a, PreciseFixedComplexFloatType b)
	{
		PreciseFixedFloatType denom = new PreciseFixedFloatType();
		PreciseFixedFloatType sum1 = new PreciseFixedFloatType();
		PreciseFixedFloatType sum2 = new PreciseFixedFloatType();
		sum1.mul(b.getReal(), b.getReal());
		sum2.mul(b.getImag(), b.getImag());
		denom.add(sum1, sum2);
		sum1.mul(a.getReal(), b.getReal());
		sum2.mul(a.getImag(), b.getImag());
		real.add(sum1, sum2);
		real.div(denom);
		sum1.mul(a.getImag(), b.getReal());
		sum2.mul(a.getReal(), b.getImag());
		imag.sub(sum1, sum2);
		imag.div(denom);
	}

	@Override
	public void div(PreciseFixedComplexFloatType c) {
		div(this, c);
	}

	@Override
	public void setZero() {
		real.setZero();
		imag.setZero();
	}

	@Override
	public void setOne() {
		real.setOne();
		imag.setZero();
	}

	@Override
	public void mul(float c) {
		mul((double) c);
	}

	@Override
	public void mul(double c) {
		real.mul(c);
		imag.mul(c);
	}

	@Override
	public PreciseFixedComplexFloatType createVariable() {
		return new PreciseFixedComplexFloatType();
	}

	@Override
	public PreciseFixedComplexFloatType copy() {
		return new PreciseFixedComplexFloatType(this);
	}

	@Override
	public double getRealDouble() {
		return real.getRealDouble();
	}

	@Override
	public float getRealFloat() {
		return real.getRealFloat();
	}

	@Override
	public double getImaginaryDouble() {
		return imag.getRealDouble();
	}

	@Override
	public float getImaginaryFloat() {
		return imag.getRealFloat();
	}

	@Override
	public void setReal(float f) {
		setReal((double) f);
	}

	@Override
	public void setImaginary(float f) {
		setImag(f);
	}

	@Override
	public void setImaginary(double f) {
		setImag(f);
	}

	@Override
	public void setComplexNumber(float r, float i) {
		setReal(r);
		setImag(i);
	}

	@Override
	public void setComplexNumber(double r, double i) {
		setReal(r);
		setImag(i);
	}

	@Override
	public float getPowerFloat() {
		return (float) getPowerDouble();
	}

	@Override
	public double getPowerDouble() {
		return getPower().getRealDouble();
	}

	public PreciseFixedFloatType getPower() {
		PreciseFixedFloatType a = new PreciseFixedFloatType();
		PreciseFixedFloatType b = new PreciseFixedFloatType();
		PreciseFixedFloatType sum = new PreciseFixedFloatType();
		a.mul(real, real);
		b.mul(imag, imag);
		sum.add(a, b);
		return PreciseFixedFloatType.sqrt(sum);
	}

	@Override
	public float getPhaseFloat() {
		return (float) getPhaseDouble();
	}

	@Override
	public double getPhaseDouble() {
		return getPhase().getRealDouble();
	}

	public PreciseFixedFloatType getPhase() {
		return PreciseFixedFloatType.atan2(imag, real);
	}

	@Override
	public void complexConjugate() {
		imag.negate();
	}
}
