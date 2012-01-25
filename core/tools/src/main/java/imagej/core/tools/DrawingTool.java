//
// DrawingTool.java
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

package imagej.core.tools;

import imagej.data.Dataset;
import imagej.util.ColorRGB;
import imagej.util.Colors;
import net.imglib2.RandomAccess;
import net.imglib2.meta.Axes;
import net.imglib2.type.numeric.RealType;


/**
 * Draws data in an orthoplane of a Dataset
 * 
 * @author Barry DeZonia
 *
 */
public class DrawingTool {

	private final Dataset dataset;
	private int uAxis;
	private int vAxis;
	private final int colorAxis;
	private RandomAccess<? extends RealType<?>> accessor;
	private long lineWidth;
	private ColorRGB colorValue;
	private double grayValue;
	private long u0, v0;
	private long maxU, maxV;
	
	public DrawingTool(Dataset ds) {
		this.dataset = ds;
		if (ds.isRGBMerged())
			this.colorAxis = ds.getAxisIndex(Axes.CHANNEL);
		else
			this.colorAxis = -1;
		this.accessor = ds.getImgPlus().randomAccess();
		this.lineWidth = 1;
		this.grayValue = ds.getType().getMinValue();
		this.colorValue = Colors.BLACK;
		this.uAxis = 0;
		this.vAxis = 1;
		this.maxU = ds.dimension(0) - 1;
		this.maxV = ds.dimension(1) - 1;
		this.u0 = 0;
		this.v0 = 0;
	}

	public Dataset getDataset() {
		return dataset;
	}
	
	public void setUAxis(int axisNum) {
		uAxis = axisNum;
		maxU = dataset.dimension(uAxis) - 1;
	}
	
	public void setVAxis(int axisNum) {
		vAxis = axisNum;
		maxV = dataset.dimension(vAxis) - 1;
	}
	
	/**
	 * Sets this DrawingHelper's internal position. Useful for changing plane
	 * position. Also useful when changing U or V axes.
	 */
	public void setPosition(long[] position) {
		accessor.setPosition(position);
	}
	
	public void setLineWidth(long lineWidth) {
		this.lineWidth = lineWidth;
	}

	// for gray data. note: cannot always represent 64-bit int exactly
	public void setGrayValue(double value) {
		this.grayValue = value;
	}

	// for color data.
	public void setColorValue(ColorRGB color) {
		this.colorValue = color;
	}
	
	public void drawPixel(long u, long v) {
		if (u < 0) return;
		if (v < 0) return;
		if (u > maxU) return;
		if (v > maxV) return;
		accessor.setPosition(u, uAxis);
		accessor.setPosition(v, vAxis);
		// gray data?
		if (!dataset.isRGBMerged()) {
			accessor.get().setReal(grayValue);
		}
		else { // color data
			accessor.setPosition(0, colorAxis);
			accessor.get().setReal(colorValue.getRed());
			accessor.setPosition(1, colorAxis);
			accessor.get().setReal(colorValue.getGreen());
			accessor.setPosition(2, colorAxis);
			accessor.get().setReal(colorValue.getBlue());
		}
	}
	
	public void drawDot(long u, long v) {
		if (lineWidth == 1)
			drawPixel(u,v);
		else if (lineWidth == 2) {
			drawPixel(u,v);
			drawPixel(u,v-1);
			drawPixel(u-1,v);
			drawPixel(u-1,v-1);
		}
		else { // 3 or more pixels wide
			drawCircle(u,v);
		}
	}

	public void moveTo(long u, long v) {
		u0 = u;
		v0 = v;
	}
	
	public void lineTo(long u1, long v1) {
		long du = u1-u0;
		long dv = v1-v0;
		long absdu = du >= 0 ? du : -du;
		long absdv = dv >= 0 ? dv : -dv;
		long n = absdv > absdu ? absdv : absdu;
		double uinc = (double)du/n;
		double vinc = (double)dv/n;
		double u = u0;
		double v = v0;
		n++;
		u0 = u1;
		v0 = v1;
		// old IJ1 code - still relevant?
		// if (n>1000000) return;
		do {
			drawDot(Math.round(u), Math.round(v));
			u += uinc;
			v += vinc;
		} while (--n>0);
	}
		
	/** Draws a line from (x1,y1) to (x2,y2). */
	public void drawLine(long x1, long y1, long x2, long y2) {
		moveTo(x1, y1);
		lineTo(x2, y2);
	}

	// FIXME - this is a computationally expensive version adapted from IJ1.
	
	public void drawCircle(long uc, long vc) {
		double r = lineWidth / 2.0;
		long umin = (long) (uc - r + 0.5);
		long vmin = (long) (vc - r + 0.5);
		long umax = umin + lineWidth;
		long vmax = vmin + lineWidth;
		double r2 = r * r;
		r -= 0.5;
		double uoffset = umin + r;
		double voffset = vmin + r;
		double uu, vv;
		for (long v = vmin; v < vmax; v++) {
			for (long u = umin; u < umax; u++) {
				uu = u - uoffset;
				vv = v - voffset;
				if ((uu*uu + vv*vv) <= r2)
					drawPixel(u, v);
			}
		}
	}
}
