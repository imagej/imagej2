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
 * Draws data in an orthoplane of a {@link Dataset}. Many methods adapted from
 * ImageJ1's ImageProcessor methods. Internally the drawing routines work in a
 * UV plane. U and V can be specified from existing coord axes (i.e UV can equal
 * XY or ZT or any other combination of Dataset axes).
 * 
 * @author Barry DeZonia
 */
public class DrawingTool {

	// -- instance variables --

	private final Dataset dataset;
	private int uAxis;
	private int vAxis;
	private final int colorAxis;
	private final RandomAccess<? extends RealType<?>> accessor;
	private long lineWidth;
	private ColorRGB colorValue;
	private double grayValue;
	private long u0, v0;
	private long maxU, maxV;

	// -- constructor --

	/**
	 * Creates a DrawingTool to modify a specified Dataset.
	 */
	public DrawingTool(final Dataset ds) {
		this.dataset = ds;
		if (ds.isRGBMerged()) this.colorAxis = ds.getAxisIndex(Axes.CHANNEL);
		else this.colorAxis = -1;
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

	// -- public interface --

	/** Return the Dataset associated with this DrawingTool. */
	public Dataset getDataset() {
		return dataset;
	}

	/** Sets the U axis index this DrawingTool will work in. */
	public void setUAxis(final int axisNum) {
		uAxis = axisNum;
		maxU = dataset.dimension(uAxis) - 1;
	}

	/** Returns the index of the U axis of this Drawing Tool. */
	public int getUAxis() {
		return uAxis;
	}

	/** Sets the V axis index this DrawingTool will work in. */
	public void setVAxis(final int axisNum) {
		vAxis = axisNum;
		maxV = dataset.dimension(vAxis) - 1;
	}

	/** Returns the index of the V axis of this Drawing Tool. */
	public int getVAxis() {
		return vAxis;
	}

	/**
	 * Sets this DrawingHelper's current drawing position. Usually specified once
	 * before a series of drawing operations are done. Useful for changing the
	 * drawing plane position quickly. Also useful when changing U or V axes.
	 */
	public void setPosition(final long[] position) {
		accessor.setPosition(position);
	}

	/** Gets this DrawingHelper's current drawing position. */
	public void getPosition(final long[] position) {
		for (int i = 0; i < accessor.numDimensions(); i++)
			position[i] = accessor.getLongPosition(i);
	}

	/**
	 * Sets the current drawing line width. This affects how other methods draw
	 * such as lines, circles, dots, etc.
	 */
	public void setLineWidth(final long lineWidth) {
		this.lineWidth = lineWidth;
	}

	/** Gets the current drawing line width. */
	public long getLineWidth() {
		return lineWidth;
	}

	// note: we cannot represent 64-bit integer data exactly in all cases

	/**
	 * Sets the current drawing gray value. Any subsequent drawing operations use
	 * this gray value for gray level Datasets.
	 */
	public void setGrayValue(final double value) {
		this.grayValue = value;
	}

	/** Gets the current drawing gray value. */
	public double getGrayValue() {
		return grayValue;
	}

	/**
	 * Sets the current drawing color value. Any subsequent drawing operations use
	 * this color value for color Datasets.
	 */
	public void setColorValue(final ColorRGB color) {
		this.colorValue = color;
	}

	/** Gets the current drawing color value. */
	public ColorRGB getColorValue() {
		return colorValue;
	}

	/** Draws a pixel in the current UV plane at specified UV coordinates. */
	public void drawPixel(final long u, final long v) {
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

	/**
	 * Draws a dot in the current UV plane at specified UV coordinates. The size
	 * of the dot is determined by the current line width.
	 */
	public void drawDot(final long u, final long v) {
		if (lineWidth == 1) drawPixel(u, v);
		else if (lineWidth == 2) {
			drawPixel(u, v);
			drawPixel(u, v - 1);
			drawPixel(u - 1, v);
			drawPixel(u - 1, v - 1);
		}
		else { // 3 or more pixels wide
			drawCircle(u, v);
		}
	}

	/**
	 * Moves the drawing origin of the current UV plane to the specified
	 * coordinates.
	 */
	public void moveTo(final long u, final long v) {
		u0 = u;
		v0 = v;
	}

	/**
	 * Draws a line in the current UV plane from the current origin to the
	 * specified coordinate.
	 */
	public void lineTo(final long u1, final long v1) {
		final long du = u1 - u0;
		final long dv = v1 - v0;
		final long absdu = du >= 0 ? du : -du;
		final long absdv = dv >= 0 ? dv : -dv;
		long n = absdv > absdu ? absdv : absdu;
		final double uinc = (double) du / n;
		final double vinc = (double) dv / n;
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
		}
		while (--n > 0);
	}

	/** Draws a line from (u1,v1) to (u2,v2). */
	public void drawLine(final long u1, final long v1, final long u2,
		final long v2)
	{
		moveTo(u1, v1);
		lineTo(u2, v2);
	}

	// TODO - performance improve drawCircle? Necessary? Test.
	// TODO - make a version that draws the outline only. That version would need
	// user to provide radius. Line width would be the width of the outline.
	// TODO - make an ellipse method. have drawCircle call it.

	/**
	 * Draws a filled circle in the current UV plane centered at the specified UV
	 * coordinates. The radius of the circle is equals the current line width.
	 */
	public void drawCircle(final long uc, final long vc) {
		double r = lineWidth / 2.0;
		final long umin = (long) (uc - r + 0.5);
		final long vmin = (long) (vc - r + 0.5);
		final long umax = umin + lineWidth;
		final long vmax = vmin + lineWidth;
		final double r2 = r * r;
		r -= 0.5;
		final double uoffset = umin + r;
		final double voffset = vmin + r;
		double uu, vv;
		for (long v = vmin; v < vmax; v++) {
			for (long u = umin; u < umax; u++) {
				uu = u - uoffset;
				vv = v - voffset;
				if ((uu * uu + vv * vv) <= r2) drawPixel(u, v);
			}
		}
	}

}
