//
// PointOverlay.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
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
 * Represents a user specified point
 * 
 * @author Barry DeZonia
 */
public class PointOverlay extends AbstractOverlay {

	private static final long serialVersionUID = 1L;

	private RealPoint point;

	public PointOverlay(final ImageJ context) {
		super(context);
		point = new RealPoint(2);
		this.setAxis(Axes.X, 0);
		this.setAxis(Axes.Y, 1);
	}

	public PointOverlay(final ImageJ context, final RealLocalizable pt) {
		super(context);
		this.point = new RealPoint(pt);
	}

	public RealLocalizable getPoint() {
		return point;
	}
	
	public void setPoint(final RealLocalizable pt) {
		point.setPosition(pt);
	}

	/* (non-Javadoc)
	 * @see imagej.data.roi.AbstractOverlay#numDimensions()
	 */
	@Override
	public int numDimensions() {
		return point.numDimensions();
	}

	/* (non-Javadoc)
	 * @see imagej.data.roi.AbstractOverlay#writeExternal(java.io.ObjectOutput)
	 */
	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeInt(this.numDimensions());
		for (int i = 0; i < numDimensions(); i++) {
			out.writeDouble(point.getDoublePosition(i));
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
		final double[] position = new double[nDimensions];
		for (int j = 0; j < nDimensions; j++) {
			position[j] = in.readDouble();
		}
		point = new RealPoint(position);
	}
}
