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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Useful methods for working with {@link Class} objects and primitive types.
 * 
 * @author Curtis Rueden
 */
public final class ClassUtils {

	private ClassUtils() {
		// prevent instantiation of utility class
	}

	// -- Type conversion and casting --

	/**
	 * Converts the given object to an object of the specified type. The object is
	 * casted directly if possible, or else a new object is created using the
	 * destination type's public constructor that takes the original object as
	 * input (except when converting to {@link String}, which uses the
	 * {@link Object#toString()} method instead). In the case of primitive types,
	 * returns an object of the corresponding wrapped type. If the destination
	 * type does not have an appropriate constructor, returns null.
	 * 
	 * @param <T> Type to which the object should be converted.
	 * @param value The object to convert.
	 * @param type Type to which the object should be converted.
	 */
	public static <T> T convert(final Object value, final Class<T> type) {
		if (value == null) return getNullValue(type);

		// ensure type is well-behaved, rather than a primitive type
		final Class<T> saneType = getNonprimitiveType(type);

		// cast the existing object, if possible
		if (canCast(value, saneType)) return cast(value, saneType);

		// special cases for strings
		if (value instanceof String) {
			// source type is String
			final String s = (String) value;
			if (s.isEmpty()) {
				// return null for empty strings
				return getNullValue(type);
			}

			// use first character when converting to Character
			if (saneType == Character.class) {
				final Character c = new Character(s.charAt(0));
				@SuppressWarnings("unchecked")
				final T result = (T) c;
				return result;
			}
		}
		if (saneType == String.class) {
			// destination type is String; use Object.toString() method
			final String sValue = value.toString();
			@SuppressWarnings("unchecked")
			final T result = (T) sValue;
			return result;
		}

		// wrap the original object with one of the new type, using a constructor
		try {
			final Constructor<T> ctor = saneType.getConstructor(value.getClass());
			return ctor.newInstance(value);
		}
		catch (final Exception exc) {
			// no known way to convert
			return null;
		}
	}

	/**
	 * Checks whether objects of the given class can be converted to the specified
	 * type.
	 * 
	 * @see #convert(Object, Class)
	 */
	public static boolean canConvert(final Class<?> c, final Class<?> type) {
		// ensure type is well-behaved, rather than a primitive type
		final Class<?> saneType = getNonprimitiveType(type);

		// OK if the existing object can be casted
		if (canCast(c, saneType)) return true;

		// OK if string
		if (saneType == String.class) return true;

		// OK if source type is string and destination type is character
		// (in this case, the first character of the string would be used)
		if (String.class.isAssignableFrom(c) && saneType == Character.class) {
			return true;
		}

		// OK if appropriate wrapper constructor exists
		try {
			saneType.getConstructor(c);
			return true;
		}
		catch (final Exception exc) {
			// no known way to convert
			return false;
		}
	}

	/**
	 * Checks whether the given object can be converted to the specified type.
	 * 
	 * @see #convert(Object, Class)
	 */
	public static boolean canConvert(final Object value, final Class<?> type) {
		if (value == null) return true;
		return canConvert(value.getClass(), type);
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

	/**
	 * Checks whether objects of the given class can be cast to the specified
	 * type.
	 * 
	 * @see #cast(Object, Class)
	 */
	public static boolean canCast(final Class<?> c, final Class<?> type) {
		return type.isAssignableFrom(c);
	}

	/**
	 * Checks whether the given object can be cast to the specified type.
	 * 
	 * @see #cast(Object, Class)
	 */
	public static boolean canCast(final Object obj, final Class<?> type) {
		return canCast(obj.getClass(), type);
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
	 * <li>void.class becomes Void.class</li>
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
		else if (type == void.class) destType = Void.class;
		else destType = type;
		@SuppressWarnings("unchecked")
		final Class<T> result = (Class<T>) destType;
		return result;
	}

	/**
	 * Gets the "null" value for the given type. For non-primitives, this will
	 * actually be null. For primitives, it will be zero for numeric types, false
	 * for boolean, and the null character for char.
	 */
	public static <T> T getNullValue(final Class<T> type) {
		final Object defaultValue;
		if (type == boolean.class) defaultValue = false;
		else if (type == byte.class) defaultValue = (byte) 0;
		else if (type == char.class) defaultValue = '\0';
		else if (type == double.class) defaultValue = 0d;
		else if (type == float.class) defaultValue = 0f;
		else if (type == int.class) defaultValue = 0;
		else if (type == long.class) defaultValue = 0L;
		else if (type == short.class) defaultValue = (short) 0;
		else defaultValue = null;
		@SuppressWarnings("unchecked")
		final T result = (T) defaultValue;
		return result;
	}

	// -- Class loading, querying and reflection --

	/** Loads the class with the given name, or null if it cannot be loaded. */
	public static Class<?> loadClass(final String className) {
		return loadClass(className, null);
	}

	/** Loads the class with the given name, or null if it cannot be loaded. */
	public static Class<?> loadClass(final String className,
		final ClassLoader classLoader)
	{
		try {
			if (classLoader == null) return Class.forName(className);
			return classLoader.loadClass(className);
		}
		catch (final ClassNotFoundException e) {
			return null;
		}
	}

	/** Checks whether a class with the given name exists. */
	public static boolean hasClass(final String className) {
		return hasClass(className, null);
	}

	/** Checks whether a class with the given name exists. */
	public static boolean hasClass(final String className,
		final ClassLoader classLoader)
	{
		try {
			if (classLoader == null) Class.forName(className);
			else classLoader.loadClass(className);
			return true;
		}
		catch (final ClassNotFoundException e) {
			return false;
		}
	}

	/**
	 * Gets the base location of the given class.
	 * <p>
	 * If the class is directly on the file system (e.g.,
	 * "/path/to/my/package/MyClass.class") then it will return the base directory
	 * (e.g., "/path/to").
	 * </p>
	 * <p>
	 * If the class is within a JAR file (e.g.,
	 * "/path/to/my-jar.jar!/my/package/MyClass.class") then it will return the
	 * path to the JAR (e.g., "/path/to/my-jar.jar").
	 * </p>
	 * 
	 * @param className The name of the class whose location is desired.
	 * @see FileUtils#urlToFile(URL) to convert the result to a {@link File}.
	 */
	public static URL getLocation(final String className) {
		return getLocation(className, null);
	}

	/**
	 * Gets the base location of the given class.
	 * <p>
	 * If the class is directly on the file system (e.g.,
	 * "/path/to/my/package/MyClass.class") then it will return the base directory
	 * (e.g., "/path/to").
	 * </p>
	 * <p>
	 * If the class is within a JAR file (e.g.,
	 * "/path/to/my-jar.jar!/my/package/MyClass.class") then it will return the
	 * path to the JAR (e.g., "/path/to/my-jar.jar").
	 * </p>
	 * 
	 * @param className The name of the class whose location is desired.
	 * @param classLoader The class loader to use when loading the class.
	 * @see FileUtils#urlToFile(URL) to convert the result to a {@link File}.
	 */
	public static URL getLocation(final String className,
		final ClassLoader classLoader)
	{
		final Class<?> c = loadClass(className, classLoader);
		return getLocation(c);
	}

	/**
	 * Gets the base location of the given class.
	 * <p>
	 * If the class is directly on the file system (e.g.,
	 * "/path/to/my/package/MyClass.class") then it will return the base directory
	 * (e.g., "file:/path/to").
	 * </p>
	 * <p>
	 * If the class is within a JAR file (e.g.,
	 * "/path/to/my-jar.jar!/my/package/MyClass.class") then it will return the
	 * path to the JAR (e.g., "file:/path/to/my-jar.jar").
	 * </p>
	 * 
	 * @param c The class whose location is desired.
	 * @see FileUtils#urlToFile(URL) to convert the result to a {@link File}.
	 */
	public static URL getLocation(final Class<?> c) {
		if (c == null) return null; // could not load the class

		// try the easy way first
		try {
			final URL codeSourceLocation =
				c.getProtectionDomain().getCodeSource().getLocation();
			if (codeSourceLocation != null) return codeSourceLocation;
		}
		catch (SecurityException e) {
			// NB: Cannot access protection domain.
		}
		catch (NullPointerException e) {
			// NB: Protection domain or code source is null.
		}

		// NB: The easy way failed, so we try the hard way. We ask for the class
		// itself as a resource, then strip the class's path from the URL string,
		// leaving the base path.

		// get the class's raw resource path
		final URL classResource = c.getResource(c.getSimpleName() + ".class");
		if (classResource == null) return null; // cannot find class resource

		final String url = classResource.toString();
		final String suffix = c.getCanonicalName().replace('.', '/') + ".class";
		if (!url.endsWith(suffix)) return null; // weird URL

		// strip the class's path from the URL string
		final String base = url.substring(0, url.length() - suffix.length());

		String path = base;

		// remove the "jar:" prefix and "!/" suffix, if present
		if (path.startsWith("jar:")) path = path.substring(4, path.length() - 2);

		try {
			return new URL(path);
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Gets the given class's {@link Field}s marked with the annotation of the
	 * specified class.
	 * <p>
	 * Unlike {@link Class#getFields()}, the result will include any non-public
	 * fields, including fields defined in supertypes of the given class.
	 * </p>
	 * 
	 * @param c The class to scan for annotated fields.
	 * @param annotationClass The type of annotation for which to scan.
	 * @return A new list containing all fields with the requested annotation.
	 */
	public static <A extends Annotation> List<Field> getAnnotatedFields(
		final Class<?> c, final Class<A> annotationClass)
	{
		final ArrayList<Field> fields = new ArrayList<Field>();
		getAnnotatedFields(c, annotationClass, fields);
		return fields;
	}

	/**
	 * Gets the given class's {@link Field}s marked with the annotation of the
	 * specified class.
	 * <p>
	 * Unlike {@link Class#getFields()}, the result will include any non-public
	 * fields, including fields defined in supertypes of the given class.
	 * </p>
	 * 
	 * @param c The class to scan for annotated fields.
	 * @param annotationClass The type of annotation for which to scan.
	 * @param fields The list to which matching fields will be added.
	 */
	public static <A extends Annotation> void
		getAnnotatedFields(final Class<?> c, final Class<A> annotationClass,
			final List<Field> fields)
	{
		if (c == null) return;

		// check supertypes for annotated fields first
		getAnnotatedFields(c.getSuperclass(), annotationClass, fields);
		for (final Class<?> iface : c.getInterfaces()) {
			getAnnotatedFields(iface, annotationClass, fields);
		}

		for (final Field f : c.getDeclaredFields()) {
			final A ann = f.getAnnotation(annotationClass);
			if (ann != null) fields.add(f);
		}
	}

	/**
	 * Gets the specified field of the given class, or null if it does not exist.
	 */
	public static Field getField(final String className, final String fieldName)
	{
		return getField(loadClass(className), fieldName);
	}

	/**
	 * Gets the specified field of the given class, or null if it does not exist.
	 */
	public static Field getField(final Class<?> c, final String fieldName) {
		if (c == null) return null;
		try {
			return c.getDeclaredField(fieldName);
		}
		catch (final NoSuchFieldException e) {
			return null;
		}
	}

	/**
	 * Gets the given field's value of the specified object instance, or null if
	 * the value cannot be obtained.
	 */
	public static Object getValue(final Field field, final Object instance) {
		try {
			field.setAccessible(true);
			return field.get(instance);
		}
		catch (final IllegalAccessException e) {
			return null;
		}
	}

	/**
	 * Sets the given field's value of the specified object instance.
	 * 
	 * @throws IllegalArgumentException if the value cannot be set.
	 */
	public static void setValue(final Field field, final Object instance,
		final Object value)
	{
		try {
			field.setAccessible(true);
			field.set(instance, convert(value, field.getType()));
		}
		catch (final IllegalAccessException e) {
			throw new IllegalArgumentException("No access to field: " +
				field.getName(), e);
		}
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

	// -- Comparison --

	/**
	 * Compares two {@link Class} objects using their fully qualified names.
	 * <p>
	 * Note: this method provides a natural ordering that may be inconsistent with
	 * equals. Specifically, two unequal classes may return 0 when compared in
	 * this fashion if they represent the same class loaded using two different
	 * {@link ClassLoader}s. Hence, if this method is used as a basis for
	 * implementing {@link Comparable#compareTo} or
	 * {@link java.util.Comparator#compare}, that implementation may want to
	 * impose logic beyond that of this method, for breaking ties, if a total
	 * ordering consistent with equals is always required.
	 * </p>
	 * 
	 * @see imagej.Priority#compare(imagej.Prioritized, imagej.Prioritized)
	 */
	public static int compare(final Class<?> c1, final Class<?> c2) {
		if (c1 == c2) return 0;
		final String name1 = c1 == null ? null : c1.getName();
		final String name2 = c2 == null ? null : c2.getName();
		return MiscUtils.compare(name1, name2);
	}

}
