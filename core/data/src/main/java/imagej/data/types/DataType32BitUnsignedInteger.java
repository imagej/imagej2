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

import net.imglib2.type.numeric.integer.UnsignedIntType;

import org.scijava.AbstractContextual;
import org.scijava.plugin.Plugin;

/**
 * @author Barry DeZonia
 */
@Plugin(type = DataType.class)
public class DataType32BitUnsignedInteger extends AbstractContextual implements
	DataType<UnsignedIntType>
{

	private final UnsignedIntType type = new UnsignedIntType();

	@Override
	public UnsignedIntType getType() {
		return type;
	}

	@Override
	public String name() {
		return "32-bit unsigned integer";
	}

	@Override
	public String description() {
		return "An integer data type ranging between 0 and " + 0xffffffffL;
	}

	@Override
	public boolean isFloat() {
		return false;
	}

	@Override
	public boolean isSigned() {
		return false;
	}

	@Override
	public boolean isBoundedFully() {
		return true;
	}

	@Override
	public boolean isBoundedBelow() {
		return true;
	}

	@Override
	public boolean isBoundedAbove() {
		return true;
	}

	@Override
	public void lowerBound(UnsignedIntType dest) {
		dest.set(0);
	}

	@Override
	public void upperBound(UnsignedIntType dest) {
		dest.set(0xffffffffL);
	}

	@Override
	public int bitCount() {
		return 32;
	}

	@Override
	public UnsignedIntType createVariable() {
		return new UnsignedIntType();
	}

	@Override
	public BigComplex asBigComplex(UnsignedIntType val) {
		return new BigComplex(BigDecimal.valueOf(val.get()), BigDecimal.ZERO);
	}

	@Override
	public void cast(long val, UnsignedIntType dest) {
		// TODO - is this broken? boundary cases impossible or applied too often?
		if (val < 0) dest.set(0);
		else if (val > 0xffffffffL) dest.set(0xffffffffL);
		else dest.set(val & 0xffffffffL);
	}

	@Override
	public void cast(double val, UnsignedIntType dest) {
		cast((long) val, dest);
	}

	@Override
	public void cast(BigComplex val, UnsignedIntType dest) {
		cast(val.getReal().longValue(), dest);
	}

}
