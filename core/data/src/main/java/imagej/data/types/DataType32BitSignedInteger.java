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

import net.imglib2.type.numeric.integer.IntType;

import org.scijava.AbstractContextual;
import org.scijava.plugin.Plugin;

/**
 * @author Barry DeZonia
 */
@Plugin(type = DataType.class)
public class DataType32BitSignedInteger extends AbstractContextual implements
	DataType<IntType>
{

	private final IntType type = new IntType();

	@Override
	public IntType getType() {
		return type;
	}

	@Override
	public String name() {
		return "32-bit signed integer";
	}

	@Override
	public String description() {
		return "An integer data type ranging between " + Integer.MIN_VALUE +
			" and " + Integer.MAX_VALUE;
	}

	@Override
	public boolean isFloat() {
		return false;
	}

	@Override
	public boolean isSigned() {
		return true;
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
	public void lowerBound(IntType dest) {
		dest.set(Integer.MIN_VALUE);
	}

	@Override
	public void upperBound(IntType dest) {
		dest.set(Integer.MAX_VALUE);
	}

	@Override
	public int bitCount() {
		return 32;
	}

	@Override
	public IntType createVariable() {
		return new IntType();
	}

	@Override
	public BigDecimal asBigDecimal(IntType val) {
		return BigDecimal.valueOf(val.get());
	}

	@Override
	public void cast(long val, IntType dest) {
		if (val < Integer.MIN_VALUE) dest.set(Integer.MIN_VALUE);
		else if (val > Integer.MAX_VALUE) dest.set(Integer.MAX_VALUE);
		else dest.set((int) val);
	}

	@Override
	public void cast(double val, IntType dest) {
		cast((long) val, dest);
	}

	@Override
	public void cast(BigDecimal val, IntType dest) {
		cast(val.longValue(), dest);
	}

}
