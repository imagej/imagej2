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

package imagej.data.sampler;

import imagej.data.display.ImageDisplay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.imglib2.meta.AxisType;
import net.imglib2.ops.Tuple2;
import net.imglib2.ops.Tuple3;


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
	private List<Long> indices;

	// -- private base constructor --
	
	private AxisSubrange() {
		this.err = null;
		this.indices = new ArrayList<Long>();
	}
	
	// -- public interface --
	
	public String getError() { return err; }
	
	public List<Long> getIndices() {
		return Collections.unmodifiableList(indices);
	}

	// -- public constructors --

	/**
	 * Create an AxisSubrange from a single value.
	 * 
	 * @param pos The single index position
	 */
	public AxisSubrange(long pos) {
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
	public AxisSubrange(long pos1, long pos2) {
		this();
		int numElements = (int) (Math.max(pos1, pos2) - Math.min(pos1, pos2) + 1);
		if (numElements > Integer.MAX_VALUE)
			throw new IllegalArgumentException(
				"the number of axis elements cannot exceed "+Integer.MAX_VALUE);
		int inc;
		if (pos1 <= pos2)
			inc = 1; 
		else 
			inc = -1;
		for (long l = pos1; l <= pos2; l += inc) {
			indices.add(l);
		}
	}
	
	/**
	 * Create an AxisSubrange from one position to another position stepping by a
	 * given value. The order of the two positions does matter. The "by" step
	 * must not be 0 but can be negative.
	 *  
	 * 
	 * @param pos1 The first index position
	 * @param pos2 The second index position
	 * @param by The amount to step by between positions
	 */
	public AxisSubrange(long pos1, long pos2, long by) {
		this();
		if (by == 0)
			throw new IllegalArgumentException("increment must not be 0");
		int numElements = (int) ((Math.max(pos1, pos2) - Math.min(pos1, pos2) + 1) / Math.abs(by));
		if (numElements > Integer.MAX_VALUE)
			throw new IllegalArgumentException(
				"the number of axis elements cannot exceed "+Integer.MAX_VALUE);
		long startPos = pos1, endPos = pos2;
		for (long l = startPos; l >= endPos; l += by) {
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
	 * <li>"1-10-2,20-30-3" : planes 1 through 10 by 2 and planes 20 through 30 by 3</li>
	 * <li>"1,3-5,12-60-6" : an example combining all three formats </li>
	 * </ul>
	 * 
	 * @param display The ImageDisplay used to set legal bounds on dimension sizes
	 * @param axis The AxisType within the ImageDisplay that defines bounds
	 * @param definition The textual description of the subrange (detailed elsewhere)
	 * @param originOne A boolean specifying whether the origin within the String
	 * 	language should be treated as 0 based or 1 based.
	 */
	public AxisSubrange(ImageDisplay display, AxisType axis, String definition,
			boolean originOne)
	{
		this();
		int axisIndex = display.getAxisIndex(axis);
		long min, max;
		if (originOne) {
			min = 1;
			max = display.dimension(axisIndex);
		}
		else { // origin zero
			min = 0;
			max = display.dimension(axisIndex) - 1;
		}
		if (!parseAxisDefinition(min, max, definition))
			throw new IllegalArgumentException(getError());
	}

	// -- private helpers --
	
	/**
	 * Parses a textual description of an axis definition
	 * 
	 * @param min The min value of the language (0 or 1)
	 * @param max The max value of an axis (dim-1 or dim)
	 * @param description The textual description of the axis subrange
	 * @return True if parsed successfully. Otherwise returns false and sets the
	 * error state of the AxisSubrange.
	 */
	private boolean parseAxisDefinition(long min, long max, String description)
	{
		String[] terms = description.split(",");
		for (int i = 0; i < terms.length; i++) {
			terms[i] = terms[i].trim();
		}
		for (String term : terms) {
			Long num = number(term);
			Tuple2<Long, Long> numDashNum = numberDashNumber(term);
			Tuple3<Long, Long, Long> numDashNumDashNum =
					numberDashNumberDashNumber(term);
			AxisSubrange subrange = null;
			if (num != null) {
				long pos = num - min;
				if ((pos < min) && (pos > max))
					err = "Dimension out of bounds ("+min+","+max+") : "+pos+" in "+description;
				else
					subrange = new AxisSubrange(num-min);
			}
			else if (numDashNum != null) {
				long start = numDashNum.get1();
				long end = numDashNum.get2();
				long pos1 = start - min;
				long pos2 = end - min;
				if ((pos1 < min) && (pos1 > max))
					err = "Dimension out of bounds ("+min+","+max+") : "+pos1+" in "+description;
				else if ((pos2 < min) && (pos2 > max))
					err = "Dimension out of bounds ("+min+","+max+") : "+pos2+" in "+description;
				else
					subrange = new AxisSubrange(pos1, pos2);
			}
			else if (numDashNumDashNum != null) {
				long start = numDashNumDashNum.get1();
				long end = numDashNumDashNum.get2();
				long by = numDashNumDashNum.get3();
				long pos1 = start - min;
				long pos2 = end - min;
				if ((pos1 < min) && (pos1 > max))
					err = "Dimension out of bounds ("+min+","+max+") : "+pos1+" in "+description;
				else if ((pos2 < min) && (pos2 > max))
					err = "Dimension out of bounds ("+min+","+max+") : "+pos2+" in "+description;
				else if ((by == 0) && (pos1 != pos2))
					err = "Step by value cannot be 0 in "+description;
				else
					subrange = new AxisSubrange(pos1, pos2, by);
			}
			else {
				err = "Could not parse definition: "+description;
			}
			if (err != null) {
				return false;
			}
			for (long l : subrange.getIndices()) { // invalid warning here
				if (indices.contains(l)) continue;
				indices.add(l);
			}
		}
		Collections.sort(indices);
		return true;
	}

	/**
	 * Tries to match a single number from the input term. Returns a Long of that
	 * value if successful otherwise returns null.
	 */
	private Long number(String term) {
		Matcher matcher = Pattern.compile("\\d+").matcher(term);
		if (!matcher.matches()) return null;
		return Long.parseLong(term);
	}
	
	/**
	 * Tries to match number-number from the input term. Returns a Tuple of Longs
	 * that contain the two values if successful otherwise returns null.
	 */
	private Tuple2<Long,Long> numberDashNumber(String term) {
		Matcher matcher = Pattern.compile("\\d+-\\d+").matcher(term);
		if (!matcher.matches()) return null;
		String[] values = term.split("-");
		Long start = Long.parseLong(values[0]);
		Long end = Long.parseLong(values[1]);
		if (end < start) return null;
		return new Tuple2<Long,Long>(start, end);
	}
	
	/**
	 * Tries to match number-number-number from the input term. Returns a Tuple of
	 * Longs that contain the three values if successful otherwise returns null.
	 */
	private Tuple3<Long,Long,Long> numberDashNumberDashNumber(String term) {
		Matcher matcher = Pattern.compile("\\d+-\\d+-\\d+").matcher(term);
		if (!matcher.matches()) return null;
		String[] values = term.split("-");
		Long start = Long.parseLong(values[0]);
		Long end = Long.parseLong(values[1]);
		Long by = Long.parseLong(values[2]);
		if (end < start) return null;
		if (by <= 0) return null;
		return new Tuple3<Long,Long,Long>(start, end, by);
	}
	
}

