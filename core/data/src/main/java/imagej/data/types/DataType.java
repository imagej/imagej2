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

import imagej.plugin.SingletonPlugin;

import java.math.BigDecimal;

import net.imglib2.type.numeric.NumericType;

import org.scijava.Contextual;

/**
 * DataType represents information about pixel types. These types can be
 * discovered at runtime making ImageJ user extensible.
 * 
 * @author Barry DeZonia
 */
public interface DataType<T extends NumericType<T>> extends Contextual,
	SingletonPlugin
{

	// TODO - this class also tries to fix limitations in the Imglib type system

	/**
	 * Provides an example of the underlying NumericType.
	 */
	T getType();

	/**
	 * A short name useful for displaying to user.
	 */
	String name();

	/**
	 * A longer description that elaborates on the capabilities of the type.
	 */
	String description();

	/**
	 * Returns true if this DataType represents a floating point number.
	 */
	boolean isFloat();

	/**
	 * Returns true if this DataType represents a signed number.
	 */
	boolean isSigned();

	/**
	 * Returns true if DataType is fully bounded (bounded below and bounded
	 * above).
	 */
	boolean isBoundedFully();

	/**
	 * Returns true if DataType is bounded below.
	 */
	boolean isBoundedBelow();

	/**
	 * Returns true if DataType is bounded above.
	 */
	boolean isBoundedAbove();

	/**
	 * Sets a given variable to the lower bound of the underlying NumericType. If
	 * this DataType is not bounded an UnsupportedOperationException is thrown.
	 */
	void lowerBound(T dest);

	/**
	 * Sets a given variable to the upper bound of the underlying NumericType. If
	 * this DataType is not bounded an UnsupportedOperationException is thrown.
	 */
	void upperBound(T dest);

	/**
	 * Number of bits of information encoded by the type. If this DataType is not
	 * fully bounded an UnsupportedOperationException is thrown.
	 */
	int bitCount();

	/**
	 * Returns a variable of the underlying NumericType wrapped by this DataType.
	 * Its value is zero.
	 */
	T createVariable();

	// TODO - not great for complex numbers but works. And NumericType has set()
	// for imaginary value support.

	/**
	 * Returns a BigDecimal representation of a given variable in the underlying
	 * NumericType.
	 */
	BigDecimal asBigDecimal(T val);

	// TODO - not great for complex numbers but works. And NumericType has set()
	// for imaginary value support.

	/**
	 * Sets a given variable of the underlying NumericType to a value that is
	 * closest to a given long number.
	 */
	void cast(long val, T dest);

	/**
	 * Sets a given variable of the underlying NumericType to a value that is
	 * closest to a given double number.
	 */
	void cast(double val, T dest);

	/**
	 * Sets a given variable of the underlying NumericType to a value that is
	 * closest to a given BigDecimal number.
	 */
	void cast(BigDecimal val, T dest);

// This is here because NumericType has a reliance on float/double which can
// cause accuracy problems. All other basic ops are supported by NumericType
// and are not included here.
//
//	/**
//	 * Multiplies a given variable of the underlying type by a given BigDecimal
//	 * constant.
//	 */
//	void mul(BigDecimal decimal, T var);

// TODO - not well defined for complex numbers
//	/**
//	 * Returns the minimum positive increment between two numbers in the
//	 * underlying NumericType.
//	 */
//	T step();

}
