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

package imagej.data.overlay;

import imagej.ImageJ;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.meta.Axes;

/**
 * Represents an angle having a center point and two end points.
 * 
 * @author Barry DeZonia
 */
public class AngleOverlay extends AbstractOverlay {

	private static final long serialVersionUID = 1L;

	private RealPoint ctrPoint;
	private RealPoint endPoint1;
	private RealPoint endPoint2;

	public AngleOverlay(final ImageJ context) {
		super(context);
		ctrPoint = new RealPoint(2);
		endPoint1 = new RealPoint(2);
		endPoint2 = new RealPoint(2);
		this.setAxis(Axes.X, 0);
		this.setAxis(Axes.Y, 1);
	}

	public AngleOverlay(final ImageJ context, final RealLocalizable ctr,
		final RealLocalizable end1, final RealLocalizable end2)
	{
		super(context);
		assert ctr.numDimensions() == end1.numDimensions();
		assert ctr.numDimensions() == end2.numDimensions();
		this.ctrPoint = new RealPoint(ctr);
		this.endPoint1 = new RealPoint(end1);
		this.endPoint2 = new RealPoint(end2);
	}

	public RealLocalizable getCenterPoint() {
		return ctrPoint;
	}
	
	public RealLocalizable getEndPoint1() {
		return endPoint1;
	}

	public RealLocalizable getEndPoint2() {
		return endPoint2;
	}

	public void setCenterPoint(final RealLocalizable pt) {
		ctrPoint.setPosition(pt);
	}

	public void setEndPoint1(final RealLocalizable pt) {
		endPoint1.setPosition(pt);
	}

	public void setEndPoint2(final RealLocalizable pt) {
		endPoint2.setPosition(pt);
	}

	/* (non-Javadoc)
	 * @see imagej.data.roi.AbstractOverlay#numDimensions()
	 */
	@Override
	public int numDimensions() {
		return ctrPoint.numDimensions();
	}

	/* (non-Javadoc)
	 * @see imagej.data.roi.AbstractOverlay#writeExternal(java.io.ObjectOutput)
	 */
	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeInt(this.numDimensions());
		for (final RealLocalizable pt : new RealLocalizable[] { ctrPoint, endPoint1, endPoint2 }) {
			for (int i = 0; i < numDimensions(); i++) {
				out.writeDouble(pt.getDoublePosition(i));
			}
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
		final RealPoint[] pts = new RealPoint[3];
		final double[] position = new double[nDimensions];
		for (int i = 0; i < pts.length; i++) {
			for (int j = 0; j < nDimensions; j++) {
				position[j] = in.readDouble();
			}
			pts[i] = new RealPoint(position);
		}
		ctrPoint = pts[0];
		endPoint1 = pts[1];
		endPoint2 = pts[2];
	}
}
