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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.imglib2.meta.Axes;
import net.imglib2.meta.axis.DefaultLinearAxis;
import net.imglib2.roi.RectangleRegionOfInterest;

import org.scijava.Context;

/**
 * Represents a user specified collection of points
 * 
 * @author Barry DeZonia
 */
public class PointOverlay extends AbstractROIOverlay<RectangleRegionOfInterest> {

	private static final long serialVersionUID = 1L;

	private List<double[]> points = new ArrayList<double[]>();
	
	// default constructor for use by serialization code
	//   (see AbstractOverlay::duplicate())
	public PointOverlay() {
		super(new RectangleRegionOfInterest(new double[2], new double[2]));
	}
	
	public PointOverlay(final Context context) {
		super(context, new RectangleRegionOfInterest(new double[2], new double[2]));
		this.setAxis(new DefaultLinearAxis(Axes.X), 0);
		this.setAxis(new DefaultLinearAxis(Axes.Y), 1);
	}

	public PointOverlay(final Context context, final List<double[]> pts) {
		this(context);
		setPoints(pts);
	}

	public PointOverlay(final Context context, final double[] pt) {
		this(context, Arrays.asList(pt));
	}

	public List<double[]> getPoints() {
		return points;
	}

	public void setPoints(List<double[]> pts) {
		points.clear();
		for (double[] pt : pts) {
			points.add(pt.clone());
		}
		calcRegion();
	}

	public void setPoint(int i, final double[] pt) {
		double[] p = points.get(i);
		p[0] = pt[0];
		p[1] = pt[1];
		calcRegion();
	}

	public double[] getPoint(int i) {
		return points.get(i);
	}
	
	/* (non-Javadoc)
	 * @see imagej.data.roi.AbstractOverlay#numDimensions()
	 */
	@Override
	public int numDimensions() {
		return 2;
	}

	/* (non-Javadoc)
	 * @see imagej.data.roi.AbstractOverlay#writeExternal(java.io.ObjectOutput)
	 */
	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeInt(points.size());
		out.writeInt(this.numDimensions());
		for (int p = 0; p < points.size(); p++) {
			double[] pt = points.get(p);
			for (int i = 0; i < numDimensions(); i++) {
				out.writeDouble(pt[i]);
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
		points.clear();
		final int numP = in.readInt();
		final int nDimensions = in.readInt();
		final double[] position = new double[nDimensions];
		for (int p = 0; p < numP; p++) {
			for (int j = 0; j < nDimensions; j++) {
				position[j] = in.readDouble();
			}
			points.add(position.clone());
		}
		calcRegion();
	}

	@Override
	public void move(double[] deltas) {
		getRegionOfInterest().move(deltas);
		for (int p = 0; p < points.size(); p++) {
			double[] pt = points.get(p);
			for (int i = 0; i < deltas.length; i++) {
				pt[i] += deltas[i];
			}
		}
	}

	private void calcRegion() {
		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		for (double[] pt : points) {
			if (pt[0] < minX) minX = pt[0];
			if (pt[0] > maxX) maxX = pt[0];
			if (pt[1] < minY) minY = pt[1];
			if (pt[1] > maxY) maxY = pt[1];
		}
		getRegionOfInterest().setOrigin(minX, 0);
		getRegionOfInterest().setOrigin(minY, 1);
		getRegionOfInterest().setExtent(maxX - minX, 0);
		getRegionOfInterest().setExtent(maxY - minY, 1);
	}
}
