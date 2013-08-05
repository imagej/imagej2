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

import net.imglib2.type.numeric.complex.ComplexDoubleType;

import org.scijava.AbstractContextual;

//TODO - uncomment when we are ready to support
//@Plugin(type = DataType.class)
/**
 * @author Barry DeZonia
 */
public class DataType128BitSignedComplexFloat extends AbstractContextual
	implements DataType<ComplexDoubleType>
{

	// -- fields --

	private ComplexDoubleType type = new ComplexDoubleType();

	// -- DataType methods --

	@Override
	public ComplexDoubleType getType() {
		return type;
	}

	@Override
	public String shortName() {
		return "128-bit complex";
	}

	@Override
	public String longName() {
		return "128-bit complex float";
	}

	@Override
	public String description() {
		return "A complex floating data type with 64-bit double subcomponents";
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
	public void lowerBound(ComplexDoubleType dest) {
		throw new UnsupportedOperationException("complex numbers are unbounded");
	}

	@Override
	public void upperBound(ComplexDoubleType dest) {
		throw new UnsupportedOperationException("complex numbers are unbounded");
	}

	@Override
	public int bitCount() {
		return 128;
	}

	@Override
	public ComplexDoubleType createVariable() {
		return new ComplexDoubleType();
	}

	@Override
	public void cast(ComplexDoubleType val, BigComplex dest) {
		dest.setReal(val.getRealDouble());
		dest.setImag(val.getImaginaryDouble());
	}

	@Override
	public void cast(BigComplex val, ComplexDoubleType dest) {
		dest.setReal(val.getReal().doubleValue());
		dest.setImaginary(val.getImag().doubleValue());
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
	public double asDouble(ComplexDoubleType val) {
		throw new UnsupportedOperationException();
	}

	@Override
	public long asLong(ComplexDoubleType val) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDouble(ComplexDoubleType val, double v) {
		val.setReal(v);
		val.setImaginary(0);
	}

	@Override
	public void setLong(ComplexDoubleType val, long v) {
		val.setReal(v);
		val.setImaginary(0);
	}
}
