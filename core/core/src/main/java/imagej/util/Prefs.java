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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
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

	public static void clear(final Class<?> c) {
		try {
			prefs(c).clear();
		}
		catch (final BackingStoreException e) {
			// do nothing
		}
	}

	public static void clearAll() {
		try {
			final String[] childNames = Preferences.userRoot().childrenNames();
			for (final String name : childNames)
				Preferences.userRoot().node(name).removeNode();
		}
		catch (final BackingStoreException e) {
			// do nothing
		}
	}

	// -- Helper methods --

	private static Preferences prefs(final Class<?> c) {
		return Preferences.userNodeForPackage(c == null ? Prefs.class : c);
	}

	private static String key(final Class<?> c, final String name) {
		return c == null ? name : c.getSimpleName() + "." + name;
	}
	
// public class PrefsUtil {
/**
 * Utility class for prefs
 * from http://www.java2s.com/Code/Java/Development-Class/Utilityclassforpreferences.htm
 * Copyright Javelin Software, All rights reserved.
 * @author Robin Sharp
 */
	/**
	 * Clear all the node
	 */
	public static void clear(String key) {
		clear(prefs(null), key);
	}
	
	public static void clear(Preferences preferences, String key) {
		try {
			if (preferences.nodeExists(key)) {
				preferences.node(key).clear();
			}
		} catch (BackingStoreException bse) {
			bse.printStackTrace();
		}
	}

	/**
	 * Remove the node
	 */
	public static void remove(Preferences preferences, String key) {
		try {
			if (preferences.nodeExists(key)) {
				preferences.node(key).removeNode();
			}
		} catch (BackingStoreException bse) {
			bse.printStackTrace();
		}
	}

	/**
	 * Puts a list into the preferences.
	 */
	public static void putMap(Map map, String key) {
		putMap(prefs(null), map, key);
	}
	
	public static void putMap(Preferences preferences, Map map, String key) {
		putMap(preferences.node(key), map);
	}

	/**
	 * Puts a list into the preferences.
	 */
	public static void putMap(Preferences preferences, Map map) {
		if (preferences == null) {
			throw new IllegalArgumentException("Preferences not set.");
		}
		for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			Object value = entry.getValue();
			preferences.put(entry.getKey().toString(), value == null ? null : value.toString());
		}
	}

	/**
	 * Gets a Map from the preferences.
	 */
	public static Map getMap(String key) {
		return getMap(prefs(null), key);
	}
	
	public static Map getMap(Preferences preferences, String key) {
		return getMap(preferences.node(key));
	}

	/**
	 * Gets a Map from the preferences.
	 */
	public static Map getMap(Preferences preferences) {
		if (preferences == null) {
			throw new IllegalArgumentException("Preferences not set.");
		}
		Map map = new HashMap();
		try {
			String[] keys = preferences.keys();
			for (int index = 0; index < keys.length; index++) {
				map.put(keys[index], preferences.get(keys[index], null));
			}
		} catch (BackingStoreException bse) {
			bse.printStackTrace();
		}
		return map;
	}

	/**
	 * Puts a list into the preferences starting with "0" then "1"
	 */
	public static void putList(List list, String key) {
		putList(prefs(null), list, key);
	}
	
	public static void putList(Preferences preferences, List list, String key) {
		putList(preferences.node(key), list);
	}

	/**
	 * Puts a list into the preferences starting with "0" then "1"
	 */
	public static void putList(Preferences preferences, List list) {
		if (preferences == null) {
			throw new IllegalArgumentException("Preferences not set.");
		}
		//System.out.println( "LIST=" + list );
		for (int index = 0; list != null && index < list.size(); index++) {
			Object value = list.get(index);
			preferences.put("" + index, value == null ? null : value.toString());
		}
	}

	/**
	 * Gets a List from the preferences, starting with "0", then "1" etc
	 */
	public static List getList(String key) {
		return getList(prefs(null), key);
	}

	public static List getList(Preferences preferences, String key) {
		return getList(preferences.node(key));
	}

	/**
	 * Gets a List from the preferences, starting with "0", then "1" etc
	 * Returns an empty ArrayList if nothing in prefs.
	 */
	public static List getList(Preferences preferences) {
		if (preferences == null) {
			throw new IllegalArgumentException("Preferences not set.");
		}
		List list = new ArrayList();
		for (int index = 0; index < 1000; index++) {
			String value = preferences.get("" + index, null);
			if (value == null) {
				break;
			}
			//System.out.println( ""+index+ " " + value );
			list.add(value);
		}
		return list;
	}

	// Tests for Map and List... 
	
	public static void main(String[] args) {
		try {
			Preferences prefs = Preferences.userNodeForPackage(String.class);
			// Map...
			Map map = new HashMap();
			map.put("0", "A");
			map.put("1", "B");
			map.put("2", "C");
			map.put("3", "D");
			map.put("5", "f");
			final String MAP_KEY = "MapKey";
			putMap(prefs, map, MAP_KEY);
			System.out.println(getMap(prefs, MAP_KEY));
			clear(prefs, MAP_KEY);
			// List...
			String RECENT_FILES = "RecentFiles";
			List recentFiles = new  ArrayList(); 
			//List recentFiles = PrefsUtil.getList(prefs, RECENT_FILES);
			recentFiles.add("some/path1");
			recentFiles.add("some/path2");
			recentFiles.add("some/path3");
			putList(prefs, recentFiles, RECENT_FILES);
			System.out.println(getList(prefs, RECENT_FILES));
			clear(prefs, RECENT_FILES );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
//}	

}
