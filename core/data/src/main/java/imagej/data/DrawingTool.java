/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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

package imagej.data;

import imagej.render.RenderingService;
import imagej.render.TextRenderer;
import imagej.render.TextRenderer.FontFamily;
import imagej.render.TextRenderer.FontStyle;
import imagej.render.TextRenderer.TextJustification;
import net.imglib2.RandomAccess;
import net.imglib2.meta.Axes;
import net.imglib2.type.numeric.RealType;

// TODO
// - text drawing uses shades of the current color. what is to be done when
//     you're drawing into a gray dataset? (LATER - is this comment obsolete?)

/**
 * Draws data across all channels in an orthoplane of a {@link Dataset}. Many
 * methods adapted from ImageJ1's ImageProcessor methods. Internally the drawing
 * routines work in a UV plane. U and V can be specified from existing coord
 * axes (i.e UV can equal XY or ZT or any other combination of Dataset axes that
 * do not involve the channel axis). It is the user's responsibility to avoid
 * using a single axis to specify both the U and V axes.
 * 
 * @author Barry DeZonia
 */
public class DrawingTool {

	// -- instance variables --

	private final Dataset dataset;
	private int uAxis;
	private int vAxis;
	private int channelAxis;
	private long preferredChannel;
	private final RandomAccess<? extends RealType<?>> accessor;
	private long lineWidth;
	private long u0, v0;
	private long maxU, maxV;
	private ChannelCollection channels;
	private double intensity;

	private TextRenderer textRenderer;

	// -- constructor --

	/**
	 * Creates a DrawingTool to modify a specified Dataset. Will draw pixel values
	 * using current channel values (default all zero). After construction the
	 * default U axis will be the first non-channel axis. The default V axis will
	 * be the second non-channel axis.
	 */
	public DrawingTool(final Dataset ds, RenderingService service) {
		this.dataset = ds;
		this.accessor = ds.getImgPlus().randomAccess();
		this.channels = new ChannelCollection();
		this.lineWidth = 1;
		this.intensity = 1;
		// FIXME - initialize renderer externally later. For now this works.
		this.textRenderer = service.getTextRenderer();
		this.u0 = 0;
		this.v0 = 0;
		this.preferredChannel = -1;
		initAxisVariables();
	}

	// -- public interface --

	/**
	 * Set the preferred channel to draw on. By default drawing takes place
	 * across all channels of the current plane. If you specify a nonnegative
	 * channel number with this method drawing will be restricted to that channel.
	 * If the channel number specified is negative then drawing is done on all
	 * channels. In general one should specify setPreferredChannel(-1) to undo
	 * any specification of a preferred channel.
	 * 
	 * @param channelNumber
	 */
	public void setPreferredChannel(long channelNumber) {
		if (channelNumber > 0) {
			boolean invalid = channelAxis < 0;
			if (!invalid)
				invalid = channelNumber >= dataset.dimension(channelAxis);
			if (invalid)
				throw new IllegalArgumentException(
					"preferred channel outside valid range");
		}
		this.preferredChannel = channelNumber;
	}
	
	/** Return the Dataset associated with this DrawingTool. */
	public Dataset getDataset() {
		return dataset;
	}

	/** Sets the U axis index this DrawingTool will work in. */
	public void setUAxis(final int axisNum) {
		checkAxisValid(axisNum);
		uAxis = axisNum;
		maxU = dataset.dimension(uAxis) - 1;
	}

	/** Returns the index of the U axis of this Drawing Tool. */
	public int getUAxis() {
		return uAxis;
	}

	/** Sets the V axis index this DrawingTool will work in. */
	public void setVAxis(final int axisNum) {
		checkAxisValid(axisNum);
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

	public ChannelCollection getChannels() {
		return channels;
	}

	public void setChannels(ChannelCollection chans) {
		channels = chans;
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

	public void setTextRenderer(final TextRenderer renderer) {
		this.textRenderer = renderer;
	}

	/**
	 * Sets the family name of the drawing font.
	 */
	public void setFontFamily(final FontFamily family) {
		textRenderer.setFontFamily(family);
	}

	/**
	 * Gets the family name of the drawing font.
	 */
	public FontFamily getFontFamily() {
		return textRenderer.getFontFamily();
	}

	/**
	 * Sets the style of the drawing font.
	 */
	public void setFontStyle(final FontStyle style) {
		textRenderer.setFontStyle(style);
	}

	/**
	 * Gets the style of the drawing font.
	 */
	public FontStyle getFontStyle() {
		return textRenderer.getFontStyle();
	}

	/**
	 * Sets the size of the drawing font.
	 */
	public void setFontSize(final int size) {
		textRenderer.setFontSize(size);
	}

	/**
	 * Gets the size of the drawing font.
	 */
	public int getFontSize() {
		return textRenderer.getFontSize();
	}

	/**
	 * Turns antialiasing of drawn text on (true) or off (false)
	 */
	public void setTextAntialiasing(boolean value) {
		textRenderer.setAntialiasing(value);
	}

	/**
	 * Get the drawn text antialiasing value: on (true) or off (false)
	 */
	public boolean getTextAntialiasing() {
		return textRenderer.getAntialiasing();
	}

	///**
	// * One can draw text in outlined form. Specify the width in pixels of the 
	// * outline. Specifying a width of 0 means no outline desired.
	// */
	//public void setTextOutlineWidth(float width) {
	//	if (width < 0)
	//		throw new IllegalArgumentException("text outline width must be >= 0");
	//	textRenderer.setTextOutlineWidth(width);
	//}
	
	///**
	// * One can draw text in outlined form. Get the width in pixels of the 
	// * outline. A return value of 0 means no outline.
	// */
	//public float getTextOutlineWidth() {
	//	return textRenderer.getTextOutlineWidth();
	//}
	
	/** Draws a pixel in the current UV plane at specified UV coordinates. */
	public void drawPixel(final long u, final long v) {
		if (u < 0) return;
		if (v < 0) return;
		if (u > maxU) return;
		if (v > maxV) return;
		accessor.setPosition(u, uAxis);
		accessor.setPosition(v, vAxis);
		// draw in single channel mode
		if (preferredChannel >= 0) {
			final double value = intensity * channels.getChannelValue(preferredChannel);
			if (channelAxis != -1) accessor.setPosition(preferredChannel, channelAxis);
			accessor.get().setReal(value);
		}
		else { // draw across all channels
			long numChannels = 1;
			if (channelAxis != -1) numChannels = dataset.dimension(channelAxis);
			for (long c = 0; c < numChannels; c++) {
				final double value = intensity * channels.getChannelValue(c);
				if (channelAxis != -1) accessor.setPosition(c, channelAxis);
				accessor.get().setReal(value);
			}
		}
		dataset.setDirty(true);
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
			fillCircle(u, v);
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

	// TODO - performance improve fillCircle? Necessary? Test.
	// TODO - make a version that draws the outline only. That version would need
	// user to provide radius. Line width would be the width of the outline.
	// TODO - make an ellipse method. have fillCircle call it.

	/**
	 * Draws a filled circle in the current UV plane centered at the specified UV
	 * coordinates. The radius of the circle is equals the current line width.
	 */
	public void fillCircle(final long uc, final long vc) {
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

	/**
	 * Draws the outline of a rectangle in the current UV plane. Uses given
	 * width, height, and origin.
	 */
	public void drawRect(long uOrg, long vOrg, long w, long h) {
		drawLine(uOrg,     vOrg,     uOrg,     vOrg+h-1);
		drawLine(uOrg,     vOrg,     uOrg+w-1, vOrg);
		drawLine(uOrg+w-1, vOrg+h-1, uOrg,     vOrg+h-1);
		drawLine(uOrg+w-1, vOrg+h-1, uOrg+w-1, vOrg);
	}
	
	/**
	 * Draws a filled rectangle in the current UV plane. Uses given width,
	 * height, and origin.
	 */
	public void fillRect(long uOrigin, long vOrigin, long w, long h) {
		for (long du = 0; du < w; du++) {
			for (long dv = 0; dv < h; dv++) {
				drawPixel(uOrigin+du, vOrigin+dv);
			}
		}
	}

	/**
	 * Fills the current UV plane.
	 */
	public void fill() {
		fillRect(0, 0, maxU+1, maxV+1);
	}

	/**
	 * Draws a line of text along the U axis
	 */
	public void drawText(final long anchorU, final long anchorV,
		final String text, final TextJustification just)
	{
		// render into buffer
		textRenderer.renderText(text);

		// get extents of drawn text in buffer
		final int bufferSizeU = textRenderer.getPixelsWidth();
		final int bufferSizeV = textRenderer.getPixelsHeight();
		final int[] buffer = textRenderer.getPixels();
		int minu = Integer.MAX_VALUE;
		int minv = Integer.MAX_VALUE;
		int maxu = Integer.MIN_VALUE;
		int maxv = Integer.MIN_VALUE;
		for (int u = 0; u < bufferSizeU; u++) {
			for (int v = 0; v < bufferSizeV; v++) {
				final int index = v * bufferSizeU + u;
				// only worry about nonzero pixels
				if (buffer[index] != 0) {
					if (u < minu) minu = u;
					if (u > maxu) maxu = u;
					if (v < minv) minv = v;
					if (v > maxv) maxv = v;
				}
			}
		}

		// determine drawing origin based on justification
		long originU, originV;
		switch (just) {
			case CENTER:
				originU = anchorU - (maxu - minu + 1) / 2;
				originV = anchorV - (maxv - minv + 1) / 2;
				break;
			case RIGHT:
				originU = anchorU - (maxu - minu + 1);
				originV = anchorV - (maxv - minv + 1);
				break;
			default: // LEFT
				originU = anchorU;
				originV = anchorV;
				break;
		}

		// draw pixels in dataset as needed
		for (int u = minu; u <= maxu; u++) {
			for (int v = minv; v <= maxv; v++) {
				final int index = v * bufferSizeU + u;
				// only render nonzero pixels
				if (buffer[index] != 0) {
					final double pixVal = buffer[index] & 0xff;
					intensity = pixVal / 255.0;
					drawPixel(originU + u - minu, originV + v - minv);
				}
			}
		}

		intensity = 1;
	}

	// -- private helpers --

	private void initAxisVariables() {
		channelAxis = dataset.dimensionIndex(Axes.CHANNEL);
		uAxis = -1;
		vAxis = -1;
		for (int i = 0; i < dataset.numDimensions(); i++) {
			if (i == channelAxis) continue;
			if (uAxis == -1) uAxis = i;
			else if (vAxis == -1) vAxis = i;
		}
		if (uAxis == -1 || vAxis == -1) {
			throw new IllegalArgumentException(
				"DrawingTool cannot find appropriate default UV axes");
		}
		maxU = dataset.dimension(uAxis) - 1;
		maxV = dataset.dimension(vAxis) - 1;
	}

	private void checkAxisValid(final int axisNum) {
		if (axisNum == channelAxis) {
			throw new IllegalArgumentException("DrawingTool misconfiguration. "
				+ "The tool fills multiple channels at once. "
				+ "Cannot use a channel plane as working plane.");
		}
	}


}
