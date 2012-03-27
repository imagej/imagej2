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

package imagej.ext.ui.swing;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.swing.SpinnerNumberModel;

/**
 * Abstract superclass for {@link SpinnerNumberModel} implementations for
 * non-primitive {@link Number} types, such as {@link BigInteger} and
 * {@link BigDecimal}.
 * 
 * @author Curtis Rueden
 * @see SpinnerBigIntegerModel
 * @see SpinnerBigDecimalModel
 */
public abstract class SpinnerTypedNumberModel<T extends Number> extends
	SpinnerNumberModel
{

	private final Class<T> type;

	private T value;
	private Comparable<T> min;
	private Comparable<T> max;
	private T stepSize;

	public SpinnerTypedNumberModel(final Class<T> type, final T value,
		final Comparable<T> min, final Comparable<T> max, final T stepSize)
	{
		super(value, min, max, stepSize);
		this.type = type;
		this.value = value;
		this.min = min;
		this.max = max;
		this.stepSize = stepSize;
	}

	// -- SpinnerTypedNumberModel methods --

	protected abstract T stepUp();

	protected abstract T stepDown();

	// -- SpinnerNumberModel methods --

	@Override
	public Comparable<T> getMaximum() {
		return max;
	}

	@Override
	public Comparable<T> getMinimum() {
		return min;
	}

	@Override
	public T getNextValue() {
		final T newValue = stepUp();
		if (max != null && max.compareTo(newValue) < 0) return null;
		return newValue;
	}

	@Override
	public T getNumber() {
		return value;
	}

	@Override
	public T getPreviousValue() {
		final T newValue = stepDown();
		if (min != null && min.compareTo(newValue) > 0) return null;
		return newValue;
	}

	@Override
	public T getStepSize() {
		return stepSize;
	}

	@Override
	public T getValue() {
		return value;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setMaximum(final Comparable maximum) {
		max = maximum;
		super.setMaximum(maximum);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setMinimum(final Comparable minimum) {
		min = minimum;
		super.setMinimum(minimum);
	}

	@Override
	public void setStepSize(final Number stepSize) {
		if (stepSize == null || !type.isInstance(stepSize)) {
			throw new IllegalArgumentException("illegal value");
		}
		@SuppressWarnings("unchecked")
		final T typedStepSize = (T) stepSize;
		this.stepSize = typedStepSize;
		super.setStepSize(stepSize);
	}

	@Override
	public void setValue(final Object value) {
		if (value == null || !type.isInstance(value)) {
			throw new IllegalArgumentException("illegal value");
		}
		@SuppressWarnings("unchecked")
		final T typedValue = (T) value;
		this.value = typedValue;
		super.setValue(value);
	}

}
