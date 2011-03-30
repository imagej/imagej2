//
// Prefs.java
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

import java.util.prefs.Preferences;

/**
 * Simple utility class that stores and retrieves user preferences.
 * 
 * @author Curtis Rueden
 */
public final class Prefs {

	private Prefs() {
		// prevent instantiation of utility class
	}

	// -- Global preferences --

	public static String get(final String name) {
		return get((Class<?>) null, name);
	}

	public static String get(final String name, final String defaultValue) {
		return get(null, name, defaultValue);
	}

	public static boolean getBoolean(final String name,
		final boolean defaultValue)
	{
		return getBoolean(null, name, defaultValue);
	}

	public static double getDouble(final String name, final double defaultValue)
	{
		return getDouble(null, name, defaultValue);
	}

	public static float getFloat(final String name, final float defaultValue) {
		return getFloat(null, name, defaultValue);
	}

	public static int getInt(final String name, final int defaultValue) {
		return getInt(null, name, defaultValue);
	}

	public static long getLong(final String name, final long defaultValue) {
		return getLong(null, name, defaultValue);
	}

	public static void put(final String name, final String value) {
		put(null, name, value);
	}

	public static void put(final String name, final boolean value) {
		put(null, name, value);
	}

	public static void put(final String name, final double value) {
		put(null, name, value);
	}

	public static void put(final String name, final float value) {
		put(null, name, value);
	}

	public static void put(final String name, final int value) {
		put(null, name, value);
	}

	public static void put(final String name, final long value) {
		put(null, name, value);
	}

	// -- Class-specific preferences --

	public static String get(final Class<?> c, final String name) {
		return get(c, name, null);
	}

	public static String get(final Class<?> c, final String name,
		final String defaultValue)
	{
		return prefs(c).get(key(c, name), defaultValue);
	}

	public static boolean getBoolean(final Class<?> c, final String name,
		final boolean defaultValue)
	{
		return prefs(c).getBoolean(key(c, name), defaultValue);
	}

	public static double getDouble(final Class<?> c, final String name,
		final double defaultValue)
	{
		return prefs(c).getDouble(key(c, name), defaultValue);
	}

	public static float getFloat(final Class<?> c, final String name,
		final float defaultValue)
	{
		return prefs(c).getFloat(key(c, name), defaultValue);
	}

	public static int getInt(final Class<?> c, final String name,
		final int defaultValue)
	{
		return prefs(c).getInt(key(c, name), defaultValue);
	}

	public static long getLong(final Class<?> c, final String name,
		final long defaultValue)
	{
		return prefs(c).getLong(key(c, name), defaultValue);
	}

	public static void put(final Class<?> c, final String name,
		final String value)
	{
		prefs(c).put(key(c, name), value);
	}

	public static void put(final Class<?> c, final String name,
		final boolean value)
	{
		prefs(c).putBoolean(key(c, name), value);
	}

	public static void put(final Class<?> c, final String name,
		final double value)
	{
		prefs(c).putDouble(key(c, name), value);
	}

	public static void
		put(final Class<?> c, final String name, final float value)
	{
		prefs(c).putFloat(key(c, name), value);
	}

	public static void put(final Class<?> c, final String name, final int value)
	{
		prefs(c).putInt(key(c, name), value);
	}

	public static void
		put(final Class<?> c, final String name, final long value)
	{
		prefs(c).putLong(key(c, name), value);
	}

	// -- Helper methods --

	private static Preferences prefs(final Class<?> c) {
		return Preferences.userNodeForPackage(c == null ? Prefs.class : c);
	}

	private static String key(final Class<?> c, final String name) {
		return c == null ? name : c.getSimpleName() + "." + name;
	}

}
