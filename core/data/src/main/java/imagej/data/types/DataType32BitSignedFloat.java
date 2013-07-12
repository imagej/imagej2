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

import net.imglib2.type.numeric.real.FloatType;

import org.scijava.AbstractContextual;
import org.scijava.plugin.Plugin;

/**
 * @author Barry DeZonia
 */
@Plugin(type = DataType.class)
public class DataType32BitSignedFloat extends AbstractContextual implements
	DataType<FloatType>
{

	private final FloatType type = new FloatType();

	@Override
	public FloatType getType() {
		return type;
	}

	@Override
	public String name() {
		return "32-bit signed float";
	}

	@Override
	public String description() {
		return "A floating data type ranging from " + (-Float.MAX_VALUE) + " to " +
			Float.MAX_VALUE;
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
	public void lowerBound(FloatType dest) {
		dest.set(-Float.MAX_VALUE);
	}

	@Override
	public void upperBound(FloatType dest) {
		dest.set(Float.MAX_VALUE);
	}

	@Override
	public int bitCount() {
		return 32;
	}

	@Override
	public FloatType createVariable() {
		return new FloatType();
	}

	@Override
	public void cast(FloatType val, BigComplex dest) {
		dest.setReal(BigDecimal.valueOf(val.get()));
		dest.setImag(BigDecimal.ZERO);
	}

	@Override
	public void cast(BigComplex val, FloatType dest) {
		dest.set(val.getReal().floatValue());
	}

}
