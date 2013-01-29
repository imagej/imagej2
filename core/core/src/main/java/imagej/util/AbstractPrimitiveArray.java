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

package imagej.util;

import java.lang.reflect.Array;
import java.util.AbstractList;

/**
 * Abstract base class for primitive-type extensible arrays.
 * <p>
 * This class makes it easy to implement extensible arrays backed by fixed-size
 * primitive type arrays, re-allocating and copying data as needed. To avoid
 * frequent re-allocation, by default, the fixed-size array will be expanded by
 * 50% when running out of space.
 * </p>
 * 
 * @author Johannes Schindelin
 * @author Curtis Rueden
 * @param <ArrayType> Type of the primitive array; e.g., {@code double[]}.
 * @param <BaseType> Boxed type of the array element; e.g., {@code Double}.
 */
public abstract class AbstractPrimitiveArray<ArrayType, BaseType> extends
	AbstractList<BaseType> implements PrimitiveArray<ArrayType, BaseType>
{

	/** The class boxing the element type. */
	private final Class<BaseType> type;

	/**
	 * The current size of the list.
	 * <p>
	 * Note that internally, the actual array length may be greater than this
	 * value. The size represents the number of actual elements in the collection.
	 * </p>
	 */
	private int size;

	/** The maximal growth step. */
	private int maximumGrowth = Integer.MAX_VALUE;

	/**
	 * Constructs an extensible array of primitive type elements, backed by a
	 * fixed-size array.
	 * 
	 * @param type the class of the primitive type
	 */
	public AbstractPrimitiveArray(final Class<BaseType> type) {
		this(type, 0);
	}

	/**
	 * Constructs an extensible array of primitive type elements, backed by a
	 * fixed-size array.
	 * 
	 * @param type the class of the primitive type
	 * @param size the initial size
	 */
	public AbstractPrimitiveArray(final Class<BaseType> type, final int size) {
		this.type = type;
		this.size = size;
		ensureCapacity(size);
	}

	/**
	 * Constructs an extensible array of primitive type elements, backed by the
	 * given fixed-size array.
	 * 
	 * @param array the array to wrap
	 * @param type the class of the primitive type
	 */
	public AbstractPrimitiveArray(final Class<BaseType> type,
		final ArrayType array)
	{
		this.type = type;
		setArray(array);
		size = capacity();
	}

	// -- PrimitiveArray methods --

	@Override
	public int getMaximumGrowth() {
		return maximumGrowth;
	}

	@Override
	public void setMaximumGrowth(final int growth) {
		if (maximumGrowth < 1) {
			throw new IllegalArgumentException("Invalid growth value: " + growth);
		}
		maximumGrowth = growth;
	}

	@Override
	public ArrayType copyArray() {
		return copyArray(size());
	}

	/** Gets the current capacity of the backing array. */
	@Override
	public int capacity() {
		final ArrayType array = getArray();
		return array == null ? 0 : Array.getLength(array);
	}

	@Override
	public void ensureCapacity(final int minCapacity) {
		final int oldCapacity = capacity();
		if (minCapacity <= oldCapacity) return; // no need to grow

		// grow the array by up to 50% (plus a small constant)
		final int growth = Math.min(oldCapacity / 2 + 16, maximumGrowth);
		final int newCapacity;
		if (growth > Integer.MAX_VALUE - oldCapacity) {
			// growth would push array over the maximum array size
			newCapacity = Integer.MAX_VALUE;
		}
		else newCapacity = oldCapacity + growth;
		// ensure the array grows by at least the requested minimum capacity
		final int newLength = Math.max(minCapacity, newCapacity);

		// copy the data into a new array
		setArray(copyArray(newLength));
	}

	/**
	 * Shifts the array to insert space at a specified index.
	 * 
	 * @param index the index where the space should be inserted
	 * @param count the number of values to insert
	 */
	@Override
	public void insert(final int index, final int count) {
		int oldSize = size();
		if (index < 0 || index > oldSize) {
			throw new ArrayIndexOutOfBoundsException("Invalid index value");
		}
		if (count > Integer.MAX_VALUE - oldSize) {
			// insertion would push array over the maximum size
			throw new IllegalArgumentException("Too many elements");
		}
		if (count <= 0) {
			throw new IllegalArgumentException("Count must be positive");
		}
		setSize(oldSize + count);
		if (index < oldSize) {
			final ArrayType array = getArray();
			System.arraycopy(array, index, array, index + count, oldSize - index);
		}
	}

	/**
	 * Shifts the array to delete space starting at a specified index.
	 * 
	 * @param index the index where the space should be deleted
	 * @param count the number of values to delete
	 */
	@Override
	public void delete(final int index, final int count) {
		int oldSize = size();
		if (index < 0 || index > oldSize) {
			throw new ArrayIndexOutOfBoundsException("Invalid index value");
		}
		if (index < 0 || index + count > oldSize) {
			throw new IllegalArgumentException("Invalid range: index=" + index +
				", count=" + count + ", size=" + oldSize);
		}
		setSize(oldSize - count);
		if (index + count < oldSize) {
			final ArrayType array = getArray();
			System.arraycopy(array, index + count, array, index, oldSize - index -
				count);
		}
	}

	// -- List methods --

	// NB: We override and declare abstract to force reimplementation.
	@Override
	public abstract BaseType set(final int index, final BaseType element);

	// NB: We override and declare abstract to force reimplementation.
	@Override
	public abstract void add(final int index, final BaseType element);

	@Override
	public BaseType remove(final int index) {
		final BaseType removed = get(index);
		delete(index, 1);
		return removed;
	}

	// -- Collection methods --

	@Override
	public int size() {
		return size;
	}

	// NB: Overridden for performance.
	@Override
	public void clear() {
		setSize(0);
	}

	// -- Sizable methods --

	@Override
	public void setSize(final int size) {
		ensureCapacity(size);
		this.size = size;
	}

	// -- Internal methods --

	/** Checks that the index is less than the size of the array. */
	protected void checkBounds(final int index) {
		if (index >= size()) {
			throw new ArrayIndexOutOfBoundsException("Invalid index value");
		}
	}

	// -- Helper methods --

	private ArrayType copyArray(final int newLength) {
		@SuppressWarnings("unchecked")
		final ArrayType copy = (ArrayType) Array.newInstance(type, newLength);
		final ArrayType oldArray = getArray();
		if (oldArray != null) System.arraycopy(oldArray, 0, copy, 0, size());
		return copy;
	}

}
