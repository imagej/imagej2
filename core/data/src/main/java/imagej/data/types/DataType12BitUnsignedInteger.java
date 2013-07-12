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

import net.imglib2.type.numeric.integer.Unsigned12BitType;

import org.scijava.AbstractContextual;
import org.scijava.plugin.Plugin;

/**
 * @author Barry DeZonia
 */
@Plugin(type = DataType.class)
public class DataType12BitUnsignedInteger extends AbstractContextual implements
	DataType<Unsigned12BitType>
{

	private final Unsigned12BitType type = new Unsigned12BitType();

	@Override
	public Unsigned12BitType getType() {
		return type;
	}

	@Override
	public String name() {
		return "12-bit unsigned integer";
	}

	@Override
	public String description() {
		return "An integer data type ranging between 0 and " + 0xfff;
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
	public boolean isBounded() {
		return true;
	}

	@Override
	public void lowerBound(Unsigned12BitType dest) {
		dest.set((short) 0);
	}

	@Override
	public void upperBound(Unsigned12BitType dest) {
		dest.set((short) 0xfff);
	}

	@Override
	public int bitCount() {
		return 12;
	}

	@Override
	public Unsigned12BitType createVariable() {
		return new Unsigned12BitType();
	}

	@Override
	public void cast(Unsigned12BitType val, BigComplex dest) {
		dest.setReal(BigDecimal.valueOf(val.get()));
		dest.setImag(BigDecimal.ZERO);
	}

	@Override
	public void cast(BigComplex val, Unsigned12BitType dest) {
		long v = val.getReal().longValue();
		if (v < 0) dest.set((short) 0);
		else if (v > 0xfff) dest.set((short) 0xfff);
		else dest.set((short) v);
	}

}
