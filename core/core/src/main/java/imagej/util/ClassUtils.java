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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * Useful methods for working with {@link Class} objects, primitive types and
 * {@link Number}s.
 * 
 * @author Curtis Rueden
 */
public final class ClassUtils {

	private ClassUtils() {
		// prevent instantiation of utility class
	}

	// -- Class loading --

	/** Loads the class with the given name, or null if it cannot be loaded. */
	public static Class<?> loadClass(final String className) {
		try {
			return Class.forName(className);
		}
		catch (final ClassNotFoundException e) {
			Log.warn("Could not load class: " + className, e);
			return null;
		}
	}

	// -- Type conversion and casting --

	/**
	 * Converts the given object to an object of the specified type. The object is
	 * casted directly if possible, or else a new object is created using the
	 * destination type's public constructor that takes the original object as
	 * input. In the case of primitive types, returns an object of the
	 * corresponding wrapped type. If the destination type does not have an
	 * appropriate constructor, returns null.
	 * 
	 * @param <T> Type to which the object should be converted.
	 * @param value The object to convert.
	 * @param type Type to which the object should be converted.
	 */
	public static <T> T convert(final Object value, final Class<T> type) {
		if (value == null) return null;

		// ensure type is well-behaved, rather than a primitive type
		final Class<T> saneType = getNonprimitiveType(type);

		// cast the existing object, if possible
		if (canCast(value, saneType)) return cast(value, saneType);

		// special cases for strings
		if (value instanceof String) {
			final String s = (String) value;
			// return null for empty strings
			if (s.isEmpty()) return null;

			// use first character when converting to Character
			if (saneType == Character.class) {
				final Character c = new Character(s.charAt(0));
				@SuppressWarnings("unchecked")
				final T result = (T) c;
				return result;
			}
		}

		// wrap the original object with one of the new type, using a constructor
		try {
			final Constructor<T> ctor = saneType.getConstructor(value.getClass());
			return ctor.newInstance(value);
		}
		catch (final Exception e) {
			// NB: Many types of exceptions; simpler to handle them all the same.
			Log.warn("Cannot convert '" + value + "' to " + type.getName(), e);
		}
		return null;
	}

	/**
	 * Casts the given object to the specified type, or null if the types are
	 * incompatible.
	 */
	public static <T> T cast(final Object obj, final Class<T> type) {
		if (!canCast(obj, type)) return null;
		@SuppressWarnings("unchecked")
		final T result = (T) obj;
		return result;
	}

	/** Checks whether the given object can be cast to the specified type. */
	public static boolean canCast(final Object obj, final Class<?> type) {
		return (type.isAssignableFrom(obj.getClass()));
	}

	/**
	 * Returns the non-primitive {@link Class} closest to the given type.
	 * <p>
	 * Specifically, the following type conversions are done:
	 * <ul>
	 * <li>boolean.class becomes Boolean.class</li>
	 * <li>byte.class becomes Byte.class</li>
	 * <li>char.class becomes Character.class</li>
	 * <li>double.class becomes Double.class</li>
	 * <li>float.class becomes Float.class</li>
	 * <li>int.class becomes Integer.class</li>
	 * <li>long.class becomes Long.class</li>
	 * <li>short.class becomes Short.class</li>
	 * </ul>
	 * All other types are unchanged.
	 * </p>
	 */
	public static <T> Class<T> getNonprimitiveType(final Class<T> type) {
		final Class<?> destType;
		if (type == boolean.class) destType = Boolean.class;
		else if (type == byte.class) destType = Byte.class;
		else if (type == char.class) destType = Character.class;
		else if (type == double.class) destType = Double.class;
		else if (type == float.class) destType = Float.class;
		else if (type == int.class) destType = Integer.class;
		else if (type == long.class) destType = Long.class;
		else if (type == short.class) destType = Short.class;
		else destType = type;
		@SuppressWarnings("unchecked")
		final Class<T> result = (Class<T>) destType;
		return result;
	}

	// -- Reflection --

	/**
	 * Gets the given field's value of the specified object instance, or null if
	 * the value cannot be obtained.
	 */
	public static Object getValue(final Field field, final Object instance) {
		if (instance == null) return null;
		try {
			return field.get(instance);
		}
		catch (final IllegalArgumentException e) {
			Log.error(e);
			return null;
		}
		catch (final IllegalAccessException e) {
			Log.error(e);
			return null;
		}
	}

	/**
	 * Sets the given field's value of the specified object instance. Does nothing
	 * if the value cannot be set.
	 */
	public static void setValue(final Field field, final Object instance,
		final Object value)
	{
		if (instance == null) return;
		try {
			field.set(instance, convert(value, field.getType()));
		}
		catch (final IllegalArgumentException e) {
			Log.error(e);
			assert false;
		}
		catch (final IllegalAccessException e) {
			Log.error(e);
			assert false;
		}
	}

	// -- Numbers --

	/**
	 * Converts the given object to a {@link Number} of the specified type, or
	 * null if the types are incompatible.
	 */
	public static Number toNumber(final Object value, final Class<?> type) {
		final Object num = convert(value, type);
		return num == null ? null : cast(num, Number.class);
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

	// -- Type querying --

	public static boolean isBoolean(final Class<?> type) {
		return type == boolean.class || Boolean.class.isAssignableFrom(type);
	}

	public static boolean isByte(final Class<?> type) {
		return type == byte.class || Byte.class.isAssignableFrom(type);
	}

	public static boolean isCharacter(final Class<?> type) {
		return type == char.class || Character.class.isAssignableFrom(type);
	}

	public static boolean isDouble(final Class<?> type) {
		return type == double.class || Double.class.isAssignableFrom(type);
	}

	public static boolean isFloat(final Class<?> type) {
		return type == float.class || Float.class.isAssignableFrom(type);
	}

	public static boolean isInteger(final Class<?> type) {
		return type == int.class || Integer.class.isAssignableFrom(type);
	}

	public static boolean isLong(final Class<?> type) {
		return type == long.class || Long.class.isAssignableFrom(type);
	}

	public static boolean isShort(final Class<?> type) {
		return type == short.class || Short.class.isAssignableFrom(type);
	}

	public static boolean isNumber(final Class<?> type) {
		return Number.class.isAssignableFrom(type) || type == byte.class ||
			type == double.class || type == float.class || type == int.class ||
			type == long.class || type == short.class;
	}

	public static boolean isText(final Class<?> type) {
		return String.class.isAssignableFrom(type) || isCharacter(type);
	}

}
