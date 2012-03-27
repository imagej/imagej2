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
 * Useful methods for working with {@link Number} objects.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public final class NumberUtils {

	private NumberUtils() {
		// prevent instantiation of utility class
	}

	/**
	 * Converts the given object to a {@link Number} of the specified type, or
	 * null if the types are incompatible.
	 */
	public static Number toNumber(final Object value, final Class<?> type) {
		final Object num = ClassUtils.convert(value, type);
		return num == null ? null : ClassUtils.cast(num, Number.class);
	}

	public static Number getMinimumNumber(final Class<?> type) {
		if (ClassUtils.isByte(type)) return Byte.MIN_VALUE;
		if (ClassUtils.isShort(type)) return Short.MIN_VALUE;
		if (ClassUtils.isInteger(type)) return Integer.MIN_VALUE;
		if (ClassUtils.isLong(type)) return Long.MIN_VALUE;
		if (ClassUtils.isFloat(type)) return -Float.MAX_VALUE;
		if (ClassUtils.isDouble(type)) return -Double.MAX_VALUE;
		return null;
	}

	public static Number getMaximumNumber(final Class<?> type) {
		if (ClassUtils.isByte(type)) return Byte.MAX_VALUE;
		if (ClassUtils.isShort(type)) return Short.MAX_VALUE;
		if (ClassUtils.isInteger(type)) return Integer.MAX_VALUE;
		if (ClassUtils.isLong(type)) return Long.MAX_VALUE;
		if (ClassUtils.isFloat(type)) return Float.MAX_VALUE;
		if (ClassUtils.isDouble(type)) return Double.MAX_VALUE;
		return null;
	}

	public static Number getDefaultValue(final Number min, final Number max,
		final Class<?> type)
	{
		if (min != null) return min;
		if (max != null) return max;
		return toNumber("0", type);
	}

	public static Number clampToRange(final Class<?> type, final Number value,
		final Number min, final Number max)
	{
		if (value == null) return getDefaultValue(min, max, type);
		if (Comparable.class.isAssignableFrom(type)) {
			@SuppressWarnings("unchecked")
			final Comparable<Number> cValue = (Comparable<Number>) value;
			if (min != null && cValue.compareTo(min) < 0) return min;
			if (max != null && cValue.compareTo(max) > 0) return max;
		}
		return value;
	}

}
