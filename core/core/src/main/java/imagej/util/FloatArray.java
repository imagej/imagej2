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

/**
 * An extensible array of float elements.
 * 
 * @author Johannes Schindelin
 */
public class FloatArray extends PrimitiveArray<float[], Float> {

	/**
	 * The backing array.
	 */
	protected float[] baseArray;

	/**
	 * Constructs the variable-size array.
	 * 
	 * @param size the initial size
	 * @param growth the maximal growth
	 */
	public FloatArray(int size, int growth) {
		super(size, growth, Float.TYPE);
	}

	/**
	 * Constructs the variable-size array.
	 * 
	 * @param size the initial size
	 */
	public FloatArray(int size) {
		super(size, Float.TYPE);
	}

	/**
	 * Constructs the variable-size array.
	 */
	public FloatArray() {
		super(0, Float.TYPE);
	}

	/**
	 * Returns the backing array.
	 */
	@Override
	protected float[] getArray() {
		return baseArray;
	}

	/**
	 * Sets the backing array.
	 * 
	 * The caller needs to ensure that actualSize is valid after this call.
	 */
	@Override
	protected void setArray(float[] array) {
		baseArray = array;
	}

	/**
	 * Returns one (boxed) element of the array.
	 */
	@Override
	protected Float valueOf(int index) {
		return Float.valueOf(baseArray[index]);
	}

	/**
	 * Appends a value to the collection.
	 * 
	 * @param value the value
	 * @return the index at which the value was inserted
	 */
	public int add(float value) {
		int index = getAddIndex();
		baseArray[index] = value;
		return index;
	}

	/**
	 * Inserts a value into the collection.
	 * 
	 * @param index the indest
	 * @param value the value
	 * @return the index at which the value was inserted
	 */
	public int insert(int index, float value) {
		if (index < 0 || index > actualSize)
			throw new ArrayIndexOutOfBoundsException("Invalid index value");
		makeInsertSpace(index);
		baseArray[index] = value;
		return index;
	}

	/**
	 * Returns the element at the given index.
	 * 
	 * @param index the index
	 * @return the value
	 */
	public float get(int index) {
		if (index < 0 || index >= actualSize)
			throw new ArrayIndexOutOfBoundsException("Invalid index value");
		return baseArray[index];
	}

	/**
	 * Sets the value at a give position.
	 * 
	 * @param index the index
	 * @param value the value
	 */
	public void set(int index, float value) {
		if (index < 0 || index >= actualSize)
			throw new ArrayIndexOutOfBoundsException("Invalid index value");
		baseArray[index] = value;
	}

	/**
	 * Checks whether the array contains a given value.
	 * 
	 * @param value the value
	 * @return whether the array contains the value 
	 */
	public boolean contains(float value) {
		for (int i = 0; i < actualSize; i++)
			if (baseArray[i] == value)
				return true;
		return false;
	}

	/**
	 * Returns a {@link String} representation of the array.
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		String delimiter = "";
		for (int i = 0; i < actualSize; i++) {
			result.append(delimiter).append(baseArray[i]);
			delimiter = ", ";
		}
		return "[ " + result.toString() + " ]";
	}
}
