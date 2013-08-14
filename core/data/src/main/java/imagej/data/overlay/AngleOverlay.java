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
import net.imglib2.meta.DefaultCalibratedAxis;
import net.imglib2.roi.AngleRegionOfInterest;

import org.scijava.Context;

/**
 * Represents an angle having a center point and two end points.
 * 
 * @author Barry DeZonia
 */
public class AngleOverlay extends AbstractROIOverlay<AngleRegionOfInterest> {

	private static final long serialVersionUID = 1L;

	// default constructor for use by serialization code
	//   (see AbstractOverlay::duplicate())
	public AngleOverlay() {
		super(new AngleRegionOfInterest(new double[2], new double[2], new double[2]));
	}
	
	public AngleOverlay(final Context context) {
		super(context, new AngleRegionOfInterest(new double[2], new double[2], new double[2]));
		this.setAxis(new DefaultCalibratedAxis(Axes.X), 0);
		this.setAxis(new DefaultCalibratedAxis(Axes.Y), 1);
	}

	public AngleOverlay(final Context context, double[] ctr,
		final double[] end1, final double[] end2)
	{
		super(context,
			new AngleRegionOfInterest(
				new double[ctr.length],
				new double[end1.length],
				new double[end2.length]));
		assert ctr.length == end1.length;
		assert ctr.length == end2.length;
		AngleRegionOfInterest roi = getRegionOfInterest();
		roi.setCenter(ctr);
		roi.setPoint1(end1);
		roi.setPoint2(end2);
		this.setAxis(new DefaultCalibratedAxis(Axes.X), 0);
		this.setAxis(new DefaultCalibratedAxis(Axes.Y), 1);
	}

	public void getCenter(double[] pt) {
		getRegionOfInterest().getCenter(pt);
	}
	
	public void getPoint1(double[] pt) {
		getRegionOfInterest().getPoint1(pt);
	}

	public void getPoint2(double[] pt) {
		getRegionOfInterest().getPoint2(pt);
	}

	public void setCenter(final double[] pt) {
		getRegionOfInterest().setCenter(pt);
	}

	public void setPoint1(final double[] pt) {
		getRegionOfInterest().setPoint1(pt);
	}

	public void setPoint2(final double[] pt) {
		getRegionOfInterest().setPoint2(pt);
	}

	public double getCenter(int dim) {
		return getRegionOfInterest().getCenter(dim);
	}
	
	public double getPoint1(int dim) {
		return getRegionOfInterest().getPoint1(dim);
	}

	public double getPoint2(int dim) {
		return getRegionOfInterest().getPoint2(dim);
	}

	public void setCenter(double val, int dim) {
		getRegionOfInterest().setCenter(val, dim);
	}

	public void setPoint1(double val, int dim) {
		getRegionOfInterest().setPoint1(val, dim);
	}

	public void setPoint2(double val, int dim) {
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
		AngleRegionOfInterest roi = getRegionOfInterest();
		int numDims = roi.numDimensions();
		out.writeInt(numDims);
		for (int i = 0; i < numDims; i++)
			out.writeDouble(roi.getPoint1(i));
		for (int i = 0; i < numDims; i++)
			out.writeDouble(roi.getCenter(i));
		for (int i = 0; i < numDims; i++)
			out.writeDouble(roi.getPoint2(i));
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
		final double[][] pts = new double[3][nDimensions];
		for (int i = 0; i < pts.length; i++) {
			for (int j = 0; j < nDimensions; j++) {
				pts[i][j] = in.readDouble();
			}
		}
		AngleRegionOfInterest roi = getRegionOfInterest();
		roi.setPoint1(pts[0]);
		roi.setCenter(pts[1]);
		roi.setPoint2(pts[2]);
	}

	@Override
	public void move(double[] deltas) {
		getRegionOfInterest().move(deltas);
	}

}
