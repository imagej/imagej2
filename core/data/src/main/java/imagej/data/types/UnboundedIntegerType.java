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
import java.math.BigInteger;

import net.imglib2.type.numeric.NumericType;

/**
 * @author Barry DeZonia
 */
// TODO - implement IntegerType or RealType too
public class UnboundedIntegerType implements NumericType<UnboundedIntegerType> {

	// -- fields --

	private BigInteger v = BigInteger.ZERO;

	// -- accessors --

	public BigInteger get() {
		return v;
	}

	public void set(BigInteger val) {
		v = val;
	}

	// -- NumericType methods --

	@Override
	public UnboundedIntegerType createVariable() {
		return new UnboundedIntegerType();
	}

	@Override
	public UnboundedIntegerType copy() {
		UnboundedIntegerType copy = new UnboundedIntegerType();
		copy.v = this.v;
		return copy;
	}

	@Override
	public void set(UnboundedIntegerType c) {
		this.v = c.v;
	}

	@Override
	public void add(UnboundedIntegerType c) {
		this.v = v.add(c.v);
	}

	@Override
	public void sub(UnboundedIntegerType c) {
		this.v = v.subtract(c.v);
	}

	@Override
	public void mul(UnboundedIntegerType c) {
		this.v = v.multiply(c.v);
	}

	@Override
	public void div(UnboundedIntegerType c) {
		this.v = v.divide(c.v);
	}

	@Override
	public void setZero() {
		this.v = BigInteger.ZERO;
	}

	@Override
	public void setOne() {
		this.v = BigInteger.ONE;
	}

	@Override
	public void mul(float c) {
		doMul(new BigDecimal(c));
	}

	@Override
	public void mul(double c) {
		doMul(new BigDecimal(c));
	}

	// -- helpers --

	private void doMul(BigDecimal factor) {
		BigDecimal val = new BigDecimal(this.v);
		BigDecimal result = val.multiply(factor);
		this.v = result.toBigInteger();
	}
}
