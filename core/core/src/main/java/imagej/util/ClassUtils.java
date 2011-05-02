//
// ClassUtils.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.util;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * TODO
 * 
 * @author Curtis Rueden
 */
public final class ClassUtils {

	private ClassUtils() {
		// prevent instantiation of utility class
	}

	/** Converts the given string to a {@link Number}. */
	public static Number toNumber(final String num, final Class<?> type) {
		if (num == null || num.isEmpty()) return null;
		if (isByte(type)) return new Byte(num);
		if (isShort(type)) return new Short(num);
		if (isInteger(type)) return new Integer(num);
		if (isLong(type)) return new Long(num);
		if (isFloat(type)) return new Float(num);
		if (isDouble(type)) return new Double(num);
		if (BigInteger.class.isAssignableFrom(type)) return new BigInteger(num);
		if (BigDecimal.class.isAssignableFrom(type)) return new BigDecimal(num);
		return null;
	}

	public static Number getMinimumNumber(final Class<?> type) {
		if (isByte(type)) return Byte.MIN_VALUE;
		if (isShort(type)) return Short.MIN_VALUE;
		if (isInteger(type)) return Integer.MIN_VALUE;
		if (isLong(type)) return Long.MIN_VALUE;
		if (isFloat(type)) return -Float.MAX_VALUE;
		if (isDouble(type)) return -Double.MAX_VALUE;
		return Double.NEGATIVE_INFINITY;
	}

	public static Number getMaximumNumber(final Class<?> type) {
		if (isByte(type)) return Byte.MAX_VALUE;
		if (isShort(type)) return Short.MAX_VALUE;
		if (isInteger(type)) return Integer.MAX_VALUE;
		if (isLong(type)) return Long.MAX_VALUE;
		if (isFloat(type)) return Float.MAX_VALUE;
		if (isDouble(type)) return Double.MAX_VALUE;
		return Double.POSITIVE_INFINITY;
	}

	public static boolean isBoolean(final Class<?> type) {
		return Boolean.class.isAssignableFrom(type) ||
			boolean.class.isAssignableFrom(type);
	}

	public static boolean isByte(final Class<?> type) {
		return Byte.class.isAssignableFrom(type) ||
			byte.class.isAssignableFrom(type);
	}

	public static boolean isCharacter(final Class<?> type) {
		return Character.class.isAssignableFrom(type) ||
			char.class.isAssignableFrom(type);
	}

	public static boolean isDouble(final Class<?> type) {
		return Double.class.isAssignableFrom(type) ||
			double.class.isAssignableFrom(type);
	}

	public static boolean isFloat(final Class<?> type) {
		return Float.class.isAssignableFrom(type) ||
			float.class.isAssignableFrom(type);
	}

	public static boolean isInteger(final Class<?> type) {
		return Integer.class.isAssignableFrom(type) ||
			int.class.isAssignableFrom(type);
	}

	public static boolean isLong(final Class<?> type) {
		return Long.class.isAssignableFrom(type) ||
			long.class.isAssignableFrom(type);
	}

	public static boolean isShort(final Class<?> type) {
		return Short.class.isAssignableFrom(type) ||
			short.class.isAssignableFrom(type);
	}

	public static boolean isNumber(final Class<?> type) {
		return Number.class.isAssignableFrom(type) ||
			byte.class.isAssignableFrom(type) ||
			double.class.isAssignableFrom(type) ||
			float.class.isAssignableFrom(type) ||
			int.class.isAssignableFrom(type) ||
			long.class.isAssignableFrom(type) ||
			short.class.isAssignableFrom(type);
	}

	public static boolean isText(final Class<?> type) {
		return String.class.isAssignableFrom(type) || isCharacter(type);
	}

}
