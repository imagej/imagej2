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
import java.math.RoundingMode;

/**
 * A complex number that stores values in BigDecimal (arbitrary) precision. This
 * class is useful for supporting DataType translations with minimal data loss.
 * 
 * @author Barry DeZonia
 */
public class BigComplex {

	private BigDecimal r, i;

	public BigComplex() {
		setZero();
	}

	public BigComplex(long r, long i) {
		setReal(r);
		setImag(i);
	}

	public BigComplex(double r, double i) {
		setReal(r);
		setImag(i);
	}

	public BigComplex(BigInteger r, BigInteger i) {
		setReal(r);
		setImag(i);
	}

	public BigComplex(BigDecimal r, BigDecimal i) {
		setReal(r);
		setImag(i);
	}

	public BigComplex(String r, String i) {
		setReal(r);
		setImag(i);
	}

	public BigDecimal getReal() {
		return r;
	}

	public BigDecimal getImag() {
		return i;
	}

	public void setZero() {
		r = BigDecimal.ZERO;
		i = BigDecimal.ZERO;
	}

	public BigComplex createVariable() {
		return new BigComplex();
	}

	public BigComplex copy() {
		return new BigComplex(r, i);
	}

	public void set(BigComplex other) {
		this.r = other.r;
		this.i = other.i;
	}

	public void set(long r, long i) {
		setReal(r);
		setImag(i);
	}

	public void set(double r, double i) {
		setReal(r);
		setImag(i);
	}

	public void set(BigInteger r, BigInteger i) {
		setReal(r);
		setImag(i);
	}

	public void set(BigDecimal r, BigDecimal i) {
		setReal(r);
		setImag(i);
	}

	public void set(String r, String i) {
		setReal(r);
		setImag(i);
	}

	public void setReal(long r) {
		this.r = BigDecimal.valueOf(r);
	}

	public void setReal(double r) {
		this.r = BigDecimal.valueOf(r);
	}

	public void setReal(BigInteger r) {
		this.r = new BigDecimal(r);
	}

	public void setReal(BigDecimal r) {
		this.r = r;
	}

	public void setReal(String r) {
		this.r = new BigDecimal(r);
	}

	public void setImag(long i) {
		this.i = BigDecimal.valueOf(i);
	}

	public void setImag(double i) {
		this.i = BigDecimal.valueOf(i);
	}

	public void setImag(BigInteger i) {
		this.i = new BigDecimal(i);
	}

	public void setImag(BigDecimal i) {
		this.i = i;
	}

	public void setImag(String i) {
		this.i = new BigDecimal(i);
	}

	public void add(BigComplex other) {
		r = r.add(other.r);
		i = i.add(other.i);
	}

	public void div(long divisor) {
		// precision loss possible: divide to twenty places of accuracy
		r = r.divide(BigDecimal.valueOf(divisor), 20, RoundingMode.HALF_UP);
	}

}