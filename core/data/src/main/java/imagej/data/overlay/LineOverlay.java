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
import net.imglib2.roi.RectangleRegionOfInterest;

/**
 * Represents a line going from here to there, possibly with arrows on one end,
 * the other or both.
 * 
 * @author Lee Kamentsky
 */
public class LineOverlay extends AbstractROIOverlay<RectangleRegionOfInterest> {

	private RealPoint ptStart;
	private RealPoint ptEnd;

	// default constructor for use by serialization code
	//   (see AbstractOverlay::duplicate())
	public LineOverlay() {
		super(new RectangleRegionOfInterest(new double[2], new double[2]));
		ptStart = new RealPoint(2);
		ptEnd = new RealPoint(2);
	}
	
	public LineOverlay(final ImageJ context) {
		super(context, new RectangleRegionOfInterest(new double[2], new double[2]));
		ptStart = new RealPoint(2);
		ptEnd = new RealPoint(2);
		this.setAxis(Axes.X, 0);
		this.setAxis(Axes.Y, 1);
	}

	public LineOverlay(final ImageJ context, final RealLocalizable ptStart,
		final RealLocalizable ptEnd)
	{
		super(context, new RectangleRegionOfInterest(new double[2], new double[2]));
		assert ptStart.numDimensions() == ptEnd.numDimensions();
		this.ptStart = new RealPoint(ptStart);
		this.ptEnd = new RealPoint(ptEnd);
		this.setAxis(Axes.X, 0);
		this.setAxis(Axes.Y, 1);
		updateRegionOfInterest();
	}

	public RealLocalizable getLineStart() {
		return ptStart;
	}

	public RealLocalizable getLineEnd() {
		return ptEnd;
	}

	public void setLineStart(final RealLocalizable pt) {
		ptStart.setPosition(pt);
		updateRegionOfInterest();
	}

	public void setLineEnd(final RealLocalizable pt) {
		ptEnd.setPosition(pt);
		updateRegionOfInterest();
	}

	/* (non-Javadoc)
	 * @see imagej.data.roi.AbstractOverlay#numDimensions()
	 */
	@Override
	public int numDimensions() {
		return ptStart.numDimensions();
	}

	/* (non-Javadoc)
	 * @see imagej.data.roi.AbstractOverlay#writeExternal(java.io.ObjectOutput)
	 */
	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeInt(this.numDimensions());
		for (final RealLocalizable pt : new RealLocalizable[] { ptStart, ptEnd }) {
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
		final RealPoint[] pts = new RealPoint[2];
		final double[] position = new double[nDimensions];
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < nDimensions; j++) {
				position[j] = in.readDouble();
			}
			pts[i] = new RealPoint(position);
		}
		ptStart = pts[0];
		ptEnd = pts[1];
		updateRegionOfInterest();
	}

	/*
	@Override
	public Overlay duplicate() {
		LineOverlay overlay = new LineOverlay(getContext());
		RealLocalizable origPt1 = getLineStart();
		RealLocalizable origPt2 = getLineEnd();
		overlay.setLineStart(origPt1);
		overlay.setLineEnd(origPt2);
		overlay.setAlpha(getAlpha());
		overlay.setAxis(Axes.X, Axes.X.ordinal());
		overlay.setAxis(Axes.Y, Axes.Y.ordinal());
		overlay.setFillColor(getFillColor());
		overlay.setLineColor(getLineColor());
		overlay.setLineEndArrowStyle(getLineEndArrowStyle());
		overlay.setLineStartArrowStyle(getLineStartArrowStyle());
		overlay.setLineStyle(getLineStyle());
		overlay.setLineWidth(getLineWidth());
		overlay.setName(getName());
		return overlay;
	}
	*/
	
	@Override
	public void move(double[] deltas) {
		for (int i = 0; i < deltas.length; i++) {
			double currPos = ptStart.getDoublePosition(i);
			ptStart.setPosition(currPos+deltas[i], i);
			currPos = ptEnd.getDoublePosition(i);
			ptEnd.setPosition(currPos+deltas[i], i);
		}
		updateRegionOfInterest();
	}
	
	@Override
	public long max(int d) {
		double v1 = getLineStart().getDoublePosition(d);
		double v2 = getLineEnd().getDoublePosition(d);
		return Math.round(Math.max(v1, v2));
	}
	
	@Override
	public long min(int d) {
		double v1 = getLineStart().getDoublePosition(d);
		double v2 = getLineEnd().getDoublePosition(d);
		return (long) Math.floor(Math.min(v1, v2));
	}

	private void updateRegionOfInterest() {
		double minX = myMin(0);
		double minY = myMin(1);
		double maxX = myMax(0);
		double maxY = myMax(1);
		getRegionOfInterest().setOrigin(new double[]{minX, minY});
		getRegionOfInterest().setExtent(new double[]{maxX-minX, maxY-minY});
	}

	private double myMax(int d) {
		double v1 = ptStart.getDoublePosition(d);
		double v2 = ptEnd.getDoublePosition(d);
		return Math.max(v1, v2);
	}
	
	private double myMin(int d) {
		double v1 = ptStart.getDoublePosition(d);
		double v2 = ptEnd.getDoublePosition(d);
		return Math.min(v1, v2);
	}

}
