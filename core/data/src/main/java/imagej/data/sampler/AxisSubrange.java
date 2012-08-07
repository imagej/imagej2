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
 * A set of position indices might look like this: 1, 3, 6, 7, 8, 25, 44.
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
	
	public AxisSubrange(long pos) {
		this();
		indices.add(pos);
	}
	
	public AxisSubrange(long pos1, long pos2) {
		this();
		long startPos, endPos;
		if (pos1 < pos2) {
			startPos = pos1;
			endPos = pos2;
		}
		else {
			startPos = pos2;
			endPos = pos1;
		}
		if (endPos - startPos + 1 > Integer.MAX_VALUE)
			throw new IllegalArgumentException(
				"the number of axis elements cannot exceed "+Integer.MAX_VALUE);
		for (long l = startPos; l <= endPos; l++) {
			indices.add(l);
		}
	}
	
	public AxisSubrange(long pos1, long pos2, long by) {
		this();
		long startPos, endPos;
		if (by == 0) {
			if (pos1 == pos2) {
				indices.add(pos1);
				return;
			}
			throw new IllegalArgumentException("increment must not be 0");
		}
		else if (by < 0) {
			startPos = Math.max(pos1, pos2);
			endPos = Math.min(pos1, pos2);
			if ((endPos - startPos + 1) / by > Integer.MAX_VALUE)
				throw new IllegalArgumentException(
					"the number of axis elements cannot exceed "+Integer.MAX_VALUE);
			for (long l = startPos; l >= endPos; l += by) {
				indices.add(l);
			}
		}
		else { // by > 0
			startPos = Math.min(pos1, pos2);
			endPos = Math.max(pos1, pos2);
			if ((endPos - startPos + 1) / by > Integer.MAX_VALUE)
				throw new IllegalArgumentException(
					"the number of axis elements cannot exceed "+Integer.MAX_VALUE);
			for (long l = startPos; l <= endPos; l += by) {
				indices.add(l);
			}
		}
	}
	
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
	
	// min = origin (like 0 or 1)
	// max = largest legal dim (like size-1 or size)
	private boolean parseAxisDefinition(long min, long max, String fieldValue)
	{
		String[] terms = fieldValue.split(",");
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
					err = "Dimension out of bounds ("+min+","+max+") : "+pos+" in "+fieldValue;
				else
					subrange = new AxisSubrange(num-min);
			}
			else if (numDashNum != null) {
				long start = numDashNum.get1();
				long end = numDashNum.get2();
				long pos1 = start - min;
				long pos2 = end - min;
				if ((pos1 < min) && (pos1 > max))
					err = "Dimension out of bounds ("+min+","+max+") : "+pos1+" in "+fieldValue;
				else if ((pos2 < min) && (pos2 > max))
					err = "Dimension out of bounds ("+min+","+max+") : "+pos2+" in "+fieldValue;
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
					err = "Dimension out of bounds ("+min+","+max+") : "+pos1+" in "+fieldValue;
				else if ((pos2 < min) && (pos2 > max))
					err = "Dimension out of bounds ("+min+","+max+") : "+pos2+" in "+fieldValue;
				else if ((by == 0) && (pos1 != pos2))
					err = "Step by value cannot be 0 in "+fieldValue;
				else
					subrange = new AxisSubrange(pos1, pos2, by);
			}
			else {
				err = "Could not parse definition: "+fieldValue;
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

	private Long number(String term) {
		Matcher matcher = Pattern.compile("\\d+").matcher(term);
		if (!matcher.matches()) return null;
		return Long.parseLong(term);
	}
	
	private Tuple2<Long,Long> numberDashNumber(String term) {
		Matcher matcher = Pattern.compile("\\d+-\\d+").matcher(term);
		if (!matcher.matches()) return null;
		String[] values = term.split("-");
		Long start = Long.parseLong(values[0]);
		Long end = Long.parseLong(values[1]);
		if (end < start) return null;
		return new Tuple2<Long,Long>(start, end);
	}
	
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

