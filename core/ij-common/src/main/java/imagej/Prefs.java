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

package imagej;

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

	public static String get(final Class<?> c, final String key) {
		return get(c, key, null);
	}

	public static String get(final Class<?> c, final String key,
		final String defaultValue)
	{
		final Preferences prefs = Preferences.userNodeForPackage(c);
		return prefs.get(key, defaultValue);
	}

	public static boolean getBoolean(final Class<?> c, final String key,
		final boolean defaultValue)
	{
		final Preferences prefs = Preferences.userNodeForPackage(c);
		return prefs.getBoolean(key, defaultValue);
	}

	public static double getDouble(final Class<?> c, final String key,
		final double defaultValue)
	{
		final Preferences prefs = Preferences.userNodeForPackage(c);
		return prefs.getDouble(key, defaultValue);
	}

	public static float getFloat(final Class<?> c, final String key,
		final float defaultValue)
	{
		final Preferences prefs = Preferences.userNodeForPackage(c);
		return prefs.getFloat(key, defaultValue);
	}

	public static int getInt(final Class<?> c, final String key,
		final int defaultValue)
	{
		final Preferences prefs = Preferences.userNodeForPackage(c);
		return prefs.getInt(key, defaultValue);
	}

	public static long getLong(final Class<?> c, final String key,
		final long defaultValue)
	{
		final Preferences prefs = Preferences.userNodeForPackage(c);
		return prefs.getLong(key, defaultValue);
	}

	public static void
		put(final Class<?> c, final String key, final String value)
	{
		final Preferences prefs = Preferences.userNodeForPackage(c);
		prefs.put(key, value);
	}

	public static void put(final Class<?> c, final String key,
		final boolean value)
	{
		final Preferences prefs = Preferences.userNodeForPackage(c);
		prefs.putBoolean(key, value);
	}

	public static void
		put(final Class<?> c, final String key, final double value)
	{
		final Preferences prefs = Preferences.userNodeForPackage(c);
		prefs.putDouble(key, value);
	}

	public static void
		put(final Class<?> c, final String key, final float value)
	{
		final Preferences prefs = Preferences.userNodeForPackage(c);
		prefs.putFloat(key, value);
	}

	public static void put(final Class<?> c, final String key, final int value) {
		final Preferences prefs = Preferences.userNodeForPackage(c);
		prefs.putInt(key, value);
	}

	public static void put(final Class<?> c, final String key, final long value)
	{
		final Preferences prefs = Preferences.userNodeForPackage(c);
		prefs.putLong(key, value);
	}

}
