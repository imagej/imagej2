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
 * Miscellaneous utility methods. Every project needs a class like this, right?
 * 
 * @author Curtis Rueden
 */
public final class MiscUtils {

	private MiscUtils() {
		// prevent instantiation of utility class
	}

	/**
	 * Compares two comparable objects, even if one or both of them are null.
	 * <p>
	 * By convention, nulls are considered less than non-nulls (and will hence
	 * will appear first on sorted lists).
	 * </p>
	 * 
	 * @param o1 The first object to compare.
	 * @param o2 The second object to compare.
	 * @return The result of {@code o1.compareTo(o2)} unless one or both of them
	 *         is null, in which case nulls are considered less than non-null
	 *         objects.
	 */
	public static <T extends Comparable<? super T>> int compare(final T o1,
		final T o2)
	{
		return compare(o1, o2, false);
	}

	/**
	 * Compares two comparable objects, even if one or both of them are null.
	 * 
	 * @param o1 The first object to compare.
	 * @param o2 The second object to compare.
	 * @param heavyNulls If true, nulls will be treated as greater than non-nulls,
	 *          and hence "sink to the bottom" of e.g. sorted lists.
	 * @return The result of {@code o1.compareTo(o2)} unless one or both of them
	 *         is null, in which case the null strategy defined by the
	 *         {@code heavyNulls} flag is used, to define nulls as either less
	 *         than, or greater than, non-null objects.
	 */
	public static <T extends Comparable<? super T>> int compare(final T o1,
		final T o2, final boolean heavyNulls)
	{
		if (o1 == null && o2 == null) return 0;
		if (o1 == null) return heavyNulls ? 1 : -1;
		if (o2 == null) return heavyNulls ? -1 : 1;
		return o1.compareTo(o2);
	}

	/**
	 * Compares two objects for equality, even if one or both of them are null.
	 * 
	 * @param o1 The first object to compare.
	 * @param o2 The second object to compare.
	 * @return True if the two objects are both null, or both are non-null and
	 *         {@code o1.equals(o2)} holds.
	 */
	public static boolean equal(final Object o1, final Object o2) {
		if (o1 == null && o2 == null) return true;
		if (o1 == null || o2 == null) return false;
		return o1.equals(o2);
	}

}
