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

package imagej.data.sampler;

import imagej.data.display.ImageDisplay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.imglib2.meta.AxisType;
import net.imglib2.ops.util.Tuple2;
import net.imglib2.ops.util.Tuple3;

/**
 * An AxisSubrange defines a set of position indices using various constructors.
 * A set of position indices might look like this: 1, 3, 6, 7, 8, 25, 44. These
 * indices are used to iterate over a subset of an image.
 * 
 * @author Barry DeZonia
 */
public class AxisSubrange {

	// -- instance variables --

	private String err;
	private final List<Long> indices;

	// -- private base constructor --

	private AxisSubrange() {
		this.err = null;
		this.indices = new ArrayList<Long>();
	}

	// -- public interface --

	public String getError() {
		return err;
	}

	public List<Long> getIndices() {
		return Collections.unmodifiableList(indices);
	}

	// -- public constructors --

	/**
	 * Create an AxisSubrange from a single value.
	 * 
	 * @param pos The single index position
	 */
	public AxisSubrange(final long pos) {
		this();
		indices.add(pos);
	}

	/**
	 * Create an AxisSubrange from one position to another position and everything
	 * in between. Whether position 1 is less or greater than position 2 does not
	 * matter.
	 * 
	 * @param pos1 The first index position of the range
	 * @param pos2 The second index position of the range
	 */
	public AxisSubrange(final long pos1, final long pos2) {
		this();
		final long numElements = Math.max(pos1, pos2) - Math.min(pos1, pos2) + 1;
		if (numElements > Integer.MAX_VALUE) {
			err = "AxisSubrange: the number of axis elements cannot exceed " + Integer.MAX_VALUE;
			return;
		}
		int by;
		if (pos1 <= pos2) by = 1;
		else by = -1;
		for (long l = pos1; (by > 0) ? l <= pos2: l >= pos2; l += by) {
			indices.add(l);
		}
	}

	/**
	 * Create an AxisSubrange from one position to another position stepping by a
	 * given value. The order of the two positions does matter. The "by" step must
	 * not be 0 but can be negative.
	 * 
	 * @param pos1 The first index position
	 * @param pos2 The second index position
	 * @param by The amount to step by between positions
	 */
	public AxisSubrange(final long pos1, final long pos2, final long by) {
		this();
		if (by == 0) {
			err = "AxisSubrange: increment by must not be 0";
			return;
		}
		final long numElements =
				(Math.max(pos1, pos2) - Math.min(pos1, pos2) + 1) / Math.abs(by);
		if (numElements > Integer.MAX_VALUE) {
			err = "AxisSubrange: the number of axis elements cannot exceed " + Integer.MAX_VALUE;
			return;
		}
		for (long l = pos1; (by > 0) ? l <= pos2 : l >= pos2; l += by) {
			indices.add(l);
		}
	}

	/**
	 * Create an AxisSubrange from a String definition. The definition is a
	 * textual language that allows one or more positions to be defined by one or
	 * more comma separated values. Some examples:
	 * <p>
	 * <ul>
	 * <li>"1" : plane 1</li>
	 * <li>"3,5" : planes 3 and 5</li>
	 * <li>"1-10" : planes 1 through 10</li>
	 * <li>"1-10,20-30" : planes 1 through 10 and 20 through 30</li>
	 * <li>"1-10-2,20-30-3" : planes 1 through 10 by 2 and planes 20 through 30 by
	 * 3</li>
	 * <li>"1,3-5,12-60-6" : an example combining all three formats</li>
	 * </ul>
	 * 
	 * @param display The ImageDisplay used to set legal bounds on dimension sizes
	 * @param axis The AxisType within the ImageDisplay that defines bounds
	 * @param definition The textual description of the subrange (detailed
	 *          elsewhere)
	 * @param originOne A boolean specifying whether the origin within the String
	 *          language should be treated as 0 based or 1 based.
	 */
	public AxisSubrange(final ImageDisplay display, final AxisType axis,
		final String definition, final boolean originOne)
	{
		this();
		final int axisIndex = display.dimensionIndex(axis);
		long min, max;
		if (originOne) {
			min = 1;
			max = display.dimension(axisIndex);
		}
		else { // origin zero
			min = 0;
			max = display.dimension(axisIndex) - 1;
		}
		parseAxisDefinition(min, max, definition);
	}

	// -- private helpers --

	/**
	 * Parses a textual description of an axis definition
	 * 
	 * @param min The min value of the language (0 or 1)
	 * @param max The max value of an axis (dim-1 or dim)
	 * @param description The textual description of the axis subrange
	 */
	private void parseAxisDefinition(final long min, final long max,
		final String description)
	{
		final String[] terms = description.split(",");
		if (terms.length == 0) {
			err = "AxisSubrange: description string is empty";
			return;
		}
		for (int i = 0; i < terms.length; i++) {
			terms[i] = terms[i].trim();
		}
		for (final String term : terms) {
			final Long num = number(term);
			final Tuple2<Long, Long> numDashNum = numberDashNumber(term);
			final Tuple3<Long, Long, Long> numDashNumDashNum =
				numberDashNumberDashNumber(term);
			AxisSubrange subrange = null;
			if (num != null) {
				if ((num < min) || (num > max)) {
					err =
							"AxisSubrange: dimension out of bounds (" + min + "," + max +
							") : " + num + " in " + description;
				}
				else {
					subrange = new AxisSubrange(num - min);
				}
			}
			else if (numDashNum != null) {
				long start = numDashNum.get1();
				long end = numDashNum.get2();
				if (end < start) {  // allow them to be order reversed
					long tmp = end;
					end = start;
					start = tmp;
				}
				if ((start < min) || (start > max)) {
					err =
							"AxisSubrange: dimension out of bounds (" + min + "," + max +
							") : " + start + " in " + description;
				}
				else if ((end < min) || (end > max)) {
					err =
							"AxisSubrange: dimension out of bounds (" + min + "," + max +
							") : " + end + " in " + description;
				}
				else {
					subrange = new AxisSubrange(start-min, end-min);
				}
			}
			else if (numDashNumDashNum != null) {
				final long start = numDashNumDashNum.get1();
				final long end = numDashNumDashNum.get2();
				final long by = numDashNumDashNum.get3();
				if ((start < min) || (start > max)) {
					err =
							"AxisSubrange: dimension out of bounds (" + min + "," + max +
							") : " + start + " in " + description;
				}
				else if ((end < min) || (end > max)) {
					err =
							"AxisSubrange: dimension out of bounds (" + min + "," + max +
							") : " + end + " in " + description;
				}
				else if (by == 0) {
					err = "AxisSubrange: step by value cannot be 0 in " + description;
				}
				else if ((by < 0 && start < end) || (by > 0 && end < start)) {
					err = "AxisSubrange: empty interval specified in " + description;
				}
				else {
					subrange = new AxisSubrange(start-min, end-min, by);
				}
			}
			else { // not num or numDashNum or numDashNumDashNum
				err = "AxisSubrange: could not parse definition: " + description;
			}
			if (err != null) return;
			for (final long l : subrange.getIndices()) { // invalid warning here
				if (indices.contains(l)) continue;
				indices.add(l);
			}
		}
		Collections.sort(indices);
	}

	/**
	 * Tries to match a single number from the input term. Returns a Long of that
	 * value if successful otherwise returns null.
	 */
	private Long number(final String term) {
		final Matcher matcher = Pattern.compile("\\d+").matcher(term);
		if (!matcher.matches()) return null;
		return Long.parseLong(term);
	}

	/**
	 * Tries to match number-number from the input term. Returns a Tuple of Longs
	 * that contain the two values if successful otherwise returns null.
	 */
	private Tuple2<Long, Long> numberDashNumber(final String term) {
		final Matcher matcher = Pattern.compile("\\d+-\\d+").matcher(term);
		if (!matcher.matches()) return null;
		final String[] values = term.split("-");
		final Long start = Long.parseLong(values[0]);
		final Long end = Long.parseLong(values[1]);
		return new Tuple2<Long, Long>(start, end);
	}

	/**
	 * Tries to match number-number-number from the input term. Returns a Tuple of
	 * Longs that contain the three values if successful otherwise returns null.
	 */
	private Tuple3<Long, Long, Long>
		numberDashNumberDashNumber(final String term)
	{
		final Matcher matcher = Pattern.compile("\\d+-\\d+-\\d+").matcher(term);
		if (!matcher.matches()) return null;
		final String[] values = term.split("-");
		final Long start = Long.parseLong(values[0]);
		final Long end = Long.parseLong(values[1]);
		final Long by = Long.parseLong(values[2]);
		return new Tuple3<Long, Long, Long>(start, end, by);
	}

}
