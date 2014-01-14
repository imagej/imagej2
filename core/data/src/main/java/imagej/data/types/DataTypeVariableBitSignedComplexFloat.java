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

import org.scijava.AbstractContextual;

//TODO - uncomment when we are ready to support
//@Plugin(type = DataType.class)
/**
 * {@link DataType} definition for variable bit complex numbers.
 * 
 * @author Barry DeZonia
 */
public class DataTypeVariableBitSignedComplexFloat extends AbstractContextual
	implements DataType<BigComplex>
{

	// -- fields --

	private BigComplex type = new BigComplex();

	// -- DataType methods --

	@Override
	public BigComplex getType() {
		return type;
	}

	@Override
	public String shortName() {
		return "Unbounded complex";
	}

	@Override
	public String longName() {
		return "Unbounded complex float";
	}

	@Override
	public String description() {
		return "A complex data type whose size is unrestricted and precise to 50 decimal places";
	}

	@Override
	public boolean isComplex() {
		return true;
	}

	@Override
	public boolean isFloat() {
		return true;
	}

	@Override
	public boolean isSigned() {
		return true;
	}

	@Override
	public boolean isBounded() {
		return false;
	}

	@Override
	public void lowerBound(BigComplex dest) {
		throw new UnsupportedOperationException("complex numbers are unbounded");
	}

	@Override
	public void upperBound(BigComplex dest) {
		throw new UnsupportedOperationException("complex numbers are unbounded");
	}

	@Override
	public int bitCount() {
		return -1;
	}

	@Override
	public BigComplex createVariable() {
		return new BigComplex();
	}

	@Override
	public void cast(BigComplex val, BigComplex dest) {
		dest.setReal(val.getReal());
		dest.setImag(val.getImag());
	}

	@Override
	public boolean hasDoubleRepresentation() {
		return false;
	}

	@Override
	public boolean hasLongRepresentation() {
		return false;
	}

	@Override
	public double asDouble(BigComplex val) {
		throw new UnsupportedOperationException();
	}

	@Override
	public long asLong(BigComplex val) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDouble(BigComplex val, double v) {
		val.setReal(v);
		val.setImag(BigDecimal.ZERO);
	}

	@Override
	public void setLong(BigComplex val, long v) {
		val.setReal(v);
		val.setImag(BigDecimal.ZERO);
	}
}
