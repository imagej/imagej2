/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
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

package imagej.util;

import java.util.List;

/**
 * Interface for primitive-type extensible arrays, modeled after
 * {@link java.util.ArrayList}, but more performant.
 * <p>
 * For primitive type arrays, {@link java.util.ArrayList} is not a good choice
 * because it uses boxing and unboxing to store the elements, leading to a large
 * memory footprint as well as performance penalties.
 * </p>
 * 
 * @author Johannes Schindelin
 * @author Curtis Rueden
 * @param <ArrayType> Type of the primitive array; e.g., {@code double[]}.
 * @param <BaseType> Boxed type of the array element; e.g., {@code Double}.
 */
public interface PrimitiveArray<ArrayType, BaseType> extends List<BaseType>,
	Sizable
{

	/**
	 * Gets the fixed-size array backing this instance.
	 * 
	 * @return the backing array
	 */
	ArrayType getArray();

	/**
	 * Sets the fixed-size array backing this instance.
	 * 
	 * @param array the new backing array
	 */
	void setArray(ArrayType array);

	/**
	 * Gets the maximal step size by which to grow the fixed-size array when
	 * running out of space.
	 */
	int getMaximumGrowth();

	/**
	 * Sets the maximal step size by which to grow the fixed-size array when
	 * running out of space.
	 */
	void setMaximumGrowth(int growth);

	/**
	 * Returns a copy of the primitive-array array.
	 * <p>
	 * The returned array is guaranteed to have {@link PrimitiveArray#size()}
	 * elements.
	 * </p>
	 * 
	 * @return the fixed-size array
	 */
	ArrayType copyArray();

	/** Gets the current capacity of the backing array. */
	int capacity();

	/**
	 * Makes sure the backing array at least a specified capacity.
	 * <p>
	 * After calling this method, the internal array will have at least
	 * {@code minCapacity} elements.
	 * </p>
	 * 
	 * @param minCapacity the minimum capacity
	 */
	void ensureCapacity(int minCapacity);

	/**
	 * Shifts the array to insert space at a specified index.
	 * 
	 * @param index the index where the space should be inserted
	 * @param count the number of values to insert
	 */
	void insert(int index, int count);

	/**
	 * Shifts the array to delete space starting at a specified index.
	 * 
	 * @param index the index where the space should be deleted
	 * @param count the number of values to delete
	 */
	void delete(int index, int count);

}
