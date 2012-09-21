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

import java.lang.reflect.Array;

import java.util.Iterator;

/**
 * Abstract base class for primitive-type extensible arrays.
 * 
 * <p>
 * For primitive type arrays, {@link java.util.ArrayList} is not a good choice
 * because it uses boxing and unboxing to store the elements, leading to a large
 * memory footprint as well as performance penalties.
 * </p> 
 * 
 * <p>
 * This class makes it easy to implement extensible arrays backed by fixed-size
 * primitive type arrays, re-allocating and copying data as needed. To
 * avoid frequent re-allocation, the fixed-size array will be doubled in size by
 * default when running out of space.
 * </p>
 * 
 * <p>
 * For example, a ByteArray would look like this:
 * <pre>
 * public class ByteArray extends PrimitiveArray&lt;byte[], Byte&gt;
 * {
 *     protected byte[] array;
 * 
 *     public ByteArray(int size) {
 *         super(size, Byte.TYPE);
 *     }
 * 
 *     @Override
 *     protected void setArray(byte[] array) {
 *         this.array = array;
 *     }
 * 
 *     @Override
 *     protected byte[] getArray() {
 *         return array;
 *     }
 * 
 *     @Override
 *     protected Byte valueOf(int index) {
 *         return Byte.valueOf(array[index]);
 *     }
 * 
 *     // these methods make this class useful
 * 
 *     public byte get(int index) {
 *         return array[index];
 *     }
 * 
 *     public void set(int index, byte value) {
 *         array[index] = value;
 *     }
 * 
 *     public void add(byte value) {
 *         set(getAddIndex(), value);
 *     }
 * }
 * </pre>
 * </p>
 * 
 * @author Johannes Schindelin
 *
 * @param <ArrayType>
 * @param <BaseType>
 */
public abstract class PrimitiveArray<ArrayType, BaseType>  implements Iterable<BaseType> {

	/** The class boxing the element type. */
	protected Class<BaseType> type;
	/** The size of the array. */
	protected int actualSize;
	/** The size of the backing array. */
	protected int allocated;
	/** The maximal growth step */
	protected int maximumGrowth;

	/**
	 * Constructs an extensible array of primitive type elements, backed by a fixed-size array.
	 * 
	 * @param size the initial size
	 * @param growth maximal step size by which to grow the fixed-size arrays when running out of space
	 * @param type the class of the primitive type
	 */
	public PrimitiveArray(int size, int growth, Class<BaseType> type) {
		this.type = type;
		@SuppressWarnings("unchecked")
		ArrayType array = (ArrayType)Array.newInstance(type, size);
		allocated = size;
		maximumGrowth = growth;
		setArray(array);
	}

	/**
	 * Constructs an extensible array of primitive type elements, backed by a fixed-size array.
	 * 
	 * @param size the initial size
	 * @param type the class of the primitive type
	 */
	public PrimitiveArray(int size, Class<BaseType> type) {
		this(size, Integer.MAX_VALUE, type);
	}

	/**
	 * Gets the fixed-size array backing this instance.
	 * 
	 * @return the array
	 */
	protected abstract ArrayType getArray();

	/**
	 * Sets the fixed-size array backing this instance.
	 * 
	 * @param array
	 */
	protected abstract void setArray(ArrayType array);

	/**
	 * Returns the (boxed) element at a given index.
	 * 
	 * @param index the index
	 * @return the value
	 */
	protected abstract BaseType valueOf(int index);

	/**
	 * Makes sure the backing array at least a specified capacity.
	 * 
	 * After calling this method, <code>valueOf(required - 1)<code> is valid, but
	 * <code>valueOf(required)</code> might not be.
	 * 
	 * @param required the size
	 */
	public void ensureCapacity(int required) {
		if (required <= actualSize)
			return;
		if (required <= allocated) {
			actualSize = required;
			return;
		}
		ArrayType base = getArray();
		int size = Math.max(required, allocated + Math.min(allocated, maximumGrowth));
		@SuppressWarnings("unchecked")
		ArrayType grown = (ArrayType)Array.newInstance(type, size);
		if (actualSize > 0)
			System.arraycopy(base, 0, grown, 0, actualSize);
		allocated = size;
		actualSize = required;
		setArray(grown);
	}

	/**
	 * Gets the next add position for appending, increasing size if needed.
	 * 
	 * @return the index
	 */
	protected int getAddIndex() {
		int result = actualSize;
		ensureCapacity(actualSize + 1);
		return result;
	}

	/**
	 * Makes room to insert a value at a specified index.
	 * 
	 * @param index the index
	 */
	protected void makeInsertSpace(int index) {
		if (index < 0)
			throw new ArrayIndexOutOfBoundsException("Invalid index value");
		ensureCapacity(actualSize + 1);
		if (index < actualSize) {
			ArrayType array = getArray();
			System.arraycopy(array, index, array, index + 1, actualSize - index - 1);
		}
	}

	/**
	 * Removes a value from the array.
	 * 
	 * @param index the index
	 */
	public void remove(int index) {
		if (index < 0 || index >= actualSize)
			throw new ArrayIndexOutOfBoundsException("Invalid index value: " + index);
		if (index < --actualSize) {
			ArrayType array = getArray();
			System.arraycopy(array, index + 1, array, index, actualSize - index);
		}
	}

	/**
	 * Sets the size to zero.
	 */
	public void clear() {
		setSize(0);
	}

	/**
	 * Gets the number of elements in this array.
	 * 
	 * @return the size
	 */
	public int size() {
		return actualSize;
	}

	/**
	 * Sets the size of the current array.
	 * 
	 * @param size the size
	 */
	public void setSize(int size) {
		if (size > allocated)
			ensureCapacity(size);
		actualSize = size;
	}

	/**
	 * Returns the array as a primitive-type array.
	 * 
	 * The returned array is guaranteed to have {@link PrimitiveArray#size()} elements.
	 * 
	 * @return the fixed-size array
	 */
	public ArrayType buildArray() {
		@SuppressWarnings("unchecked")
		ArrayType copy = (ArrayType)Array.newInstance(type, actualSize);
		System.arraycopy(getArray(), 0, copy, 0, actualSize);
		return copy;
	}

	/**
	 * Returns an iterator of the elements in this array.
	 * 
	 * Each element will be boxed, therefore this method is a not very performant
	 * convenience method.
	 */
	@Override
	public Iterator<BaseType> iterator() {
		return new Iterator<BaseType>() {
			int counter = 0;

			@Override
			public boolean hasNext() {
				return counter < actualSize;
			}

			@Override
			public BaseType next() {
				return valueOf(counter++);
			}

			@Override
			public void remove() {
				PrimitiveArray.this.remove(--counter);
			}
		};
	}
}
