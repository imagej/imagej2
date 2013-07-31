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

import net.imglib2.type.numeric.NumericType;

import org.scijava.Contextual;
import org.scijava.plugin.SingletonPlugin;

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
	String shortName();

	/**
	 * A long name useful for displaying to user.
	 */
	String longName();

	/**
	 * A fuller description that elaborates on the capabilities of the type.
	 */
	String description();

	/**
	 * Returns true if this DataType is a complex number (imaginary component is
	 * changeable).
	 */
	boolean isComplex();

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
	boolean isBounded();

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

	/**
	 * Sets a BigComplex representation from a given variable of the underlying
	 * NumericType.
	 */
	void cast(T val, BigComplex dest);

	/**
	 * Sets a given variable of the underlying NumericType to a value that is
	 * closest to a given BigComplex number.
	 */
	void cast(BigComplex val, T dest);

	/**
	 * Returns true if data can be encoded in a double with no precision loss.
	 */
	boolean hasDoubleRepresentation();

	/**
	 * Returns true if data can be encoded in a long with no precision loss.
	 */
	boolean hasLongRepresentation();

	/**
	 * Converts value to a double. If this DataType has no double representation
	 * throws UnsupportedOperationException.
	 */
	double asDouble(T val);

	/**
	 * Converts value to a long. If this DataType has no long representation
	 * throws UnsupportedOperationException.
	 */
	long asLong(T val);

	/**
	 * Sets a given value to a given double. It is best to test appropriateness of
	 * such a call by checking if this DataType has a double representation. All
	 * DataTypes implement setDouble. No care is taken to avoid precision loss.
	 */
	void setDouble(T val, double v);

	/**
	 * Sets a given value to a given long. It is best to test appropriateness of
	 * such a call by checking if this DataType has a long representation. All
	 * DataTypes implement setLong. No care is taken to avoid precision loss.
	 */
	void setLong(T val, long v);

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
