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
 * INTERRUPTION) HOWEVER CAD AND ON ANY THEORY OF LIABILITY, WHETHER IN
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

import org.scijava.AbstractContextual;
import org.scijava.plugin.Plugin;

/**
 * @author Barry DeZonia
 */
@Plugin(type = DataType.class)
public class DataTypeUnboundedInteger extends AbstractContextual implements
	DataType<UnboundedIntegerType>
{

	private UnboundedIntegerType type = new UnboundedIntegerType();

	@Override
	public UnboundedIntegerType getType() {
		return type;
	}

	@Override
	public String name() {
		return "Unbounded integer";
	}

	@Override
	public String description() {
		return "An integer whose size is unrestricted";
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
		return false;
	}

	@Override
	public boolean isBoundedBelow() {
		return false;
	}

	@Override
	public boolean isBoundedAbove() {
		return false;
	}

	@Override
	public void lowerBound(UnboundedIntegerType dest) {
		throw new UnsupportedOperationException("This data type is unbounded");
	}

	@Override
	public void upperBound(UnboundedIntegerType dest) {
		throw new UnsupportedOperationException("This data type is unbounded");
	}

	@Override
	public int bitCount() {
		throw new UnsupportedOperationException("This data type is unbounded");
	}

	@Override
	public UnboundedIntegerType createVariable() {
		return new UnboundedIntegerType();
	}

	@Override
	public BigDecimal asBigDecimal(UnboundedIntegerType val) {
		return new BigDecimal(val.get());
	}

	@Override
	public void cast(long val, UnboundedIntegerType dest) {
		dest.set(BigInteger.valueOf(val));
	}

	@Override
	public void cast(double val, UnboundedIntegerType dest) {
		dest.set(BigInteger.valueOf((long) val));
	}

	@Override
	public void cast(BigDecimal val, UnboundedIntegerType dest) {
		dest.set(val.toBigInteger());
	}

}
