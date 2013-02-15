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

package imagej.data.overlay;


import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import net.imglib2.meta.Axes;
import net.imglib2.roi.LineRegionOfInterest;

import org.scijava.Context;

/**
 * Represents a line going from here to there, possibly with arrows on one end,
 * the other or both.
 * 
 * @author Lee Kamentsky
 * @author Barry DeZonia
 */
public class LineOverlay extends AbstractROIOverlay<LineRegionOfInterest> {

	// default constructor for use by serialization code
	//   (see AbstractOverlay::duplicate())
	public LineOverlay() {
		super(new LineRegionOfInterest(new double[2], new double[2]));
	}
	
	public LineOverlay(final Context context) {
		super(context, new LineRegionOfInterest(new double[2], new double[2]));
		this.setAxis(Axes.X, 0);
		this.setAxis(Axes.Y, 1);
	}

	public LineOverlay(final Context context, final double[] ptStart,
		final double[] ptEnd)
	{
		super(context, new LineRegionOfInterest(ptStart,ptEnd));
		assert ptStart.length == ptEnd.length;
		this.setAxis(Axes.X, 0);
		this.setAxis(Axes.Y, 1);
	}

	public void getLineStart(double[] pt) {
		getRegionOfInterest().getPoint1(pt);
	}

	public void getLineEnd(double[] pt) {
		getRegionOfInterest().getPoint2(pt);
	}

	public void setLineStart(final double[] pt) {
		getRegionOfInterest().setPoint1(pt);
	}

	public void setLineEnd(final double[] pt) {
		getRegionOfInterest().setPoint2(pt);
	}

	public double getLineStart(int dim) {
		return getRegionOfInterest().getPoint1(dim);
	}

	public double getLineEnd(int dim) {
		return getRegionOfInterest().getPoint2(dim);
	}

	public void setLineStart(double val, int dim) {
		getRegionOfInterest().setPoint1(val, dim);
	}

	public void setLineEnd(double val, int dim) {
		getRegionOfInterest().setPoint2(val, dim);
	}

	/* (non-Javadoc)
	 * @see imagej.data.roi.AbstractOverlay#numDimensions()
	 */
	@Override
	public int numDimensions() {
		return getRegionOfInterest().numDimensions();
	}

	/* (non-Javadoc)
	 * @see imagej.data.roi.AbstractOverlay#writeExternal(java.io.ObjectOutput)
	 */
	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		super.writeExternal(out);
		LineRegionOfInterest roi = getRegionOfInterest();
		int numDims = roi.numDimensions();
		out.writeInt(numDims);
		for (int i = 0; i < numDims; i++) {
			out.writeDouble(roi.getPoint1(i));
		}
		for (int i = 0; i < numDims; i++) {
			out.writeDouble(roi.getPoint2(i));
		}
	}

	/* (non-Javadoc)
	 * @see imagej.data.roi.AbstractOverlay#readExternal(java.io.ObjectInput)
	 */
	@Override
	public void readExternal(final ObjectInput in) throws IOException,
		ClassNotFoundException
	{
		super.readExternal(in);
		final int nDimensions = in.readInt();
		final double[] pt1 = new double[nDimensions];
		final double[] pt2 = new double[nDimensions];
		for (int i = 0; i < nDimensions; i++) {
			pt1[i] = in.readDouble();
		}
		for (int i = 0; i < nDimensions; i++) {
			pt2[i] = in.readDouble();
		}
		LineRegionOfInterest roi = getRegionOfInterest();
		roi.setPoint1(pt1);
		roi.setPoint1(pt2);
	}

	@Override
	public void move(double[] deltas) {
		getRegionOfInterest().move(deltas);
	}

}
