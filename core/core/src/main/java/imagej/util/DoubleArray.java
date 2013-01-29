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

import java.util.Collection;

/**
 * An extensible array of {@code double} elements.
 * 
 * @author Johannes Schindelin
 * @author Curtis Rueden
 */
public class DoubleArray extends AbstractPrimitiveArray<double[], Double> {

	/** The backing array. */
	private double[] array;

	/**
	 * Constructs an extensible array of doubles, backed by a fixed-size array.
	 */
	public DoubleArray() {
		super(Double.TYPE);
	}

	/**
	 * Constructs an extensible array of doubles, backed by a fixed-size array.
	 * 
	 * @param size the initial size
	 */
	public DoubleArray(final int size) {
		super(Double.TYPE, size);
	}

	/**
	 * Constructs an extensible array of doubles, backed by the given fixed-size
	 * array.
	 * 
	 * @param array the array to wrap
	 */
	public DoubleArray(final double[] array) {
		super(Double.TYPE, array);
	}

	// -- DoubleArray methods --

	public void addValue(final double value) {
		addValue(size(), value);
	}

	public boolean removeValue(final double value) {
		final int index = indexOf(value);
		if (index < 0) return false;
		delete(index, 1);
		return true;
	}

	public double getValue(final int index) {
		checkBounds(index);
		return array[index];
	}

	public double setValue(final int index, final double value) {
		checkBounds(index);
		final double oldValue = getValue(index);
		array[index] = value;
		return oldValue;
	}

	public void addValue(final int index, final double value) {
		insert(index, 1);
		array[index] = value;
	}

	public int indexOf(final double value) {
		for (int i = 0; i < size(); i++) {
			if (array[i] == value) return i;
		}
		return -1;
	}

	public int lastIndexOf(final double value) {
		for (int i = size() - 1; i >= 0; i--) {
			if (array[i] == value) return i;
		}
		return -1;
	}

	public boolean contains(final double value) {
		return indexOf(value) >= 0;
	}

	// -- PrimitiveArray methods --

	@Override
	public double[] getArray() {
		return array;
	}

	@Override
	public void setArray(final double[] array) {
		if (array.length < size()) {
			throw new IllegalArgumentException("Array too small");
		}
		this.array = array;
	}

	// -- List methods --

	@Override
	public Double get(final int index) {
		return getValue(index);
	}

	@Override
	public Double set(final int index, final Double element) {
		return setValue(index, element == null ? defaultValue() : element);
	}

	@Override
	public void add(final int index, final Double element) {
		addValue(index, element);
	}

	// NB: Overridden for performance.
	@Override
	public int indexOf(final Object o) {
		if (!(o instanceof Double)) return -1;
		final double value = (Double) o;
		return indexOf(value);
	}

	// NB: Overridden for performance.
	@Override
	public int lastIndexOf(final Object o) {
		if (!(o instanceof Double)) return -1;
		final double value = (Double) o;
		return lastIndexOf(value);
	}

	// -- Collection methods --

	// NB: Overridden for performance.
	@Override
	public boolean contains(final Object o) {
		if (!(o instanceof Double)) return false;
		final double value = (Double) o;
		return contains(value);
	}

	// NB: Overridden for performance.
	@Override
	public boolean remove(final Object o) {
		if (!(o instanceof Double)) return false;
		final double value = (Double) o;
		return removeValue(value);
	}

	// NB: Overridden for performance.
	@Override
	public boolean containsAll(final Collection<?> c) {
		for (final Object o : c) {
			if (!(o instanceof Double)) return false;
			final double value = (Double) o;
			if (indexOf(value) < 0) return false;
		}
		return true;
	}

	// NB: Overridden for performance.
	@Override
	public boolean addAll(final int index, final Collection<? extends Double> c) {
		if (c.size() == 0) return false;
		insert(index, c.size());
		int i = index;
		for (final double e : c) {
			setValue(i++, e);
		}
		return true;
	}

	// NB: Overridden for performance.
	@Override
	public boolean removeAll(final Collection<?> c) {
		boolean changed = false;
		for (final Object o : c) {
			if (!(o instanceof Double)) continue;
			final double value = (Double) o;
			final boolean result = removeValue(value);
			if (result) changed = true;
		}
		return changed;
	}

	@Override
	public Double defaultValue() {
		return 0.0;
	}
}
