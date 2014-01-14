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

import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.AbstractContextual;
import org.scijava.plugin.Plugin;

/**
 * {@link DataType} definition for 64-bit floats.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = DataType.class)
public class DataType64BitSignedFloat extends AbstractContextual implements
	DataType<DoubleType>
{

	private final DoubleType type = new DoubleType();

	@Override
	public DoubleType getType() {
		return type;
	}

	@Override
	public String shortName() {
		return "64-bit float";
	}

	@Override
	public String longName() {
		return "64-bit signed float";
	}

	@Override
	public String description() {
		return "A float data type ranging between " + (-Double.MAX_VALUE) +
			" and " + Double.MAX_VALUE;
	}

	@Override
	public boolean isComplex() {
		return false;
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
		return true;
	}

	@Override
	public void lowerBound(DoubleType dest) {
		dest.set(-Double.MAX_VALUE);
	}

	@Override
	public void upperBound(DoubleType dest) {
		dest.set(Double.MAX_VALUE);
	}

	@Override
	public int bitCount() {
		return 64;
	}

	@Override
	public DoubleType createVariable() {
		return new DoubleType();
	}

	@Override
	public void cast(DoubleType val, BigComplex dest) {
		dest.setReal(val.get());
		dest.setImag(BigDecimal.ZERO);
	}

	@Override
	public void cast(BigComplex val, DoubleType dest) {
		setDouble(dest, val.getReal().doubleValue());
	}

	@Override
	public boolean hasDoubleRepresentation() {
		return true;
	}

	@Override
	public boolean hasLongRepresentation() {
		return false;
	}

	@Override
	public double asDouble(DoubleType val) {
		return val.get();
	}

	@Override
	public long asLong(DoubleType val) {
		// no - data loss possible
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDouble(DoubleType val, double v) {
		val.set(v);
	}

	@Override
	public void setLong(DoubleType val, long v) {
		setDouble(val, v);
	}
}
