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

package imagej.data;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import imagej.util.RealRect;
import net.imglib2.RandomAccess;
import net.imglib2.meta.Axes;
import net.imglib2.type.numeric.RealType;

// TODO
// - move awt code out of here to avoid a dependency
// - black color draws in weird blue
//     Ok, the problem is the image is 3 channel composite and not RGBMerged.
//     So the draw gray code is called. Which draws on a single channel (the
//     first?). Need code like elsewhere that draws to all channels if its a
//     composite image. And maybe track channel fill values in draw tool rather
//     than color/gray split.
// - text drawing uses shades of the current color. what is to be done when
//     you're drawing into a gray dataset?

/**
 * Draws data across all channels in an orthoplane of a {@link Dataset}. Many
 * methods adapted from ImageJ1's ImageProcessor methods. Internally the drawing
 * routines work in a UV plane. U and V can be specified from existing coord
 * axes (i.e UV can equal XY or ZT or any other combination of Dataset axes).
 * 
 * @author Barry DeZonia
 */
public class DrawingTool {

	// -- instance variables --

	private final Dataset dataset;
	private int uAxis;
	private int vAxis;
	private final int channelAxis;
	private final RandomAccess<? extends RealType<?>> accessor;
	private long lineWidth;
	private long u0, v0;
	private long maxU, maxV;
	private ChannelCollection channels;
	private double intensity;
	
	private TextRenderer textRenderer;
	
	public enum FontFamily {MONOSPACED, SERIF, SANS_SERIF}
	public enum FontStyle {PLAIN, BOLD, ITALIC, BOLD_ITALIC}
	public enum TextJustification {LEFT, CENTER, RIGHT}

	// -- constructor --

	/**
	 * Creates a DrawingTool to modify a specified Dataset.
	 */
	public DrawingTool(final Dataset ds, ChannelCollection fillValues) {
		this.dataset = ds;
		this.channelAxis = ds.getAxisIndex(Axes.CHANNEL);
		this.accessor = ds.getImgPlus().randomAccess();
		this.lineWidth = 1;
		this.channels = new ChannelCollection(fillValues);
		this.uAxis = 0;
		this.vAxis = 1;
		this.maxU = ds.dimension(0) - 1;
		this.maxV = ds.dimension(1) - 1;
		this.u0 = 0;
		this.v0 = 0;
		this.intensity = 1;
		this.textRenderer = new AWTTextRenderer();  // FIXME - do elsewhere
		textRenderer.setAntialiasing(true);
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

	public ChannelCollection getChannels() {
		return channels;
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

	public void setTextRenderer(TextRenderer renderer) {
		this.textRenderer = renderer;
	}
	
	/**
	 * Sets the family name of the drawing font.
	 */
	public void setFontFamily(FontFamily family) {
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
	public void setFontStyle(FontStyle style) {
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
	public void setFontSize(int size) {
		textRenderer.setFontSize(size);
	}
	
	/**
	 * Gets the size of the drawing font.
	 */
	public int getFontSize() {
		return textRenderer.getFontSize();
	}
	
	/** Draws a pixel in the current UV plane at specified UV coordinates. */
	public void drawPixel(final long u, final long v) {
		if (u < 0) return;
		if (v < 0) return;
		if (u > maxU) return;
		if (v > maxV) return;
		accessor.setPosition(u, uAxis);
		accessor.setPosition(v, vAxis);
		if (channelAxis == -1) { // no channel axis - already in position
			double value = intensity * channels.getChannelValue(0);
			accessor.get().setReal(value);
		}
		else { // channelAxis >= 0
			long numChan = dataset.dimension(channelAxis);
			for (long i = 0; i < numChan; i++) {
				accessor.setPosition(i, channelAxis);
				double value = intensity * channels.getChannelValue(i);
				accessor.get().setReal(value);
			}
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

	// TODO - IJ1 fill() applies to the ROI rectangle bounds. Do we want to mirror
	// this behavior in IJ2? For now implement two fill methods. But perhaps in
	// the future the DrawingTool might track a region of interest within a plane.
	
	/**
	 * Fills the current UV plane.
	 */
	public void fill() {
		for (long u = 0; u <= maxU; u++)
			for (long v = 0; v <= maxV; v++)
				drawPixel(u,v);
	}

	/**
	 * Fills a subset of the current UV plane.
	 */
	public void fill(RealRect rect) {
		for (long u = (long)rect.x; u < rect.x+rect.width; u++)
			for (long v = (long)rect.y; v < rect.y+rect.height; v++)
				drawPixel(u,v);
	}
	
	/**
	 * Draws a line of text along the U axis
	 */
	public void drawText(long anchorU, long anchorV, String text,
												TextJustification just)
	{
		// render into buffer
		textRenderer.renderText(text);
		
		// get extents of drawn text in buffer
		int bufferSizeU = textRenderer.getPixelsWidth();
		int bufferSizeV = textRenderer.getPixelsHeight();
		int[] buffer = textRenderer.getPixels();
		int minu = Integer.MAX_VALUE;
		int minv = Integer.MAX_VALUE;
		int maxu = Integer.MIN_VALUE;
		int maxv = Integer.MIN_VALUE;
		for (int u = 0; u < bufferSizeU; u++) {
			for (int v = 0; v < bufferSizeV; v++) {
				int index = v*bufferSizeU + u;
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
				int index = v*bufferSizeU + u;
				if (buffer[index] != 0) {
					double pixVal = buffer[index] & 0xff;
					intensity = pixVal / 255.0;
					drawPixel(originU+u-minu, originV+v-minv);
				}
			}
		}
		
		intensity = 1;
	}

	// -- private helpers --

	private interface TextRenderer {
		void renderText(String text);
		int getPixelsWidth();
		int getPixelsHeight();
		int[] getPixels();
		void setFontFamily(FontFamily family);
		FontFamily getFontFamily();
		void setFontStyle(FontStyle style);
		FontStyle getFontStyle();
		void setFontSize(int size);
		int getFontSize();
		void setAntialiasing(boolean val);
		boolean getAntialiasing();
	}
	
	private class AWTTextRenderer implements TextRenderer {
		private int bufferSizeU;
		private int bufferSizeV;
		private BufferedImage textBuffer;
		private WritableRaster textRaster;
		private String fontFamily;
		private int fontStyle;
		private int fontSize;
		private Font font;
		private int[] pixels;
		private boolean antialiasing;

		public AWTTextRenderer() {
			fontFamily = Font.SANS_SERIF;
			fontStyle = Font.PLAIN;
			fontSize = 12;
			antialiasing = false;
			buildFont();
			initTextBuffer("42 is my favorite number");
		}
		
		@Override
		public void renderText(String text) {
			initTextBuffer(text);
			Graphics g = textBuffer.getGraphics();
			setAntialiasedText(g, antialiasing);
			g.setFont(font);
			g.drawString(text, 0, bufferSizeV/2);
		}
		
		@Override
		public int getPixelsWidth() {
			return bufferSizeU;
		}

		@Override
		public int getPixelsHeight() {
			return bufferSizeV;
		}

		@Override
		public int[] getPixels() {
			if (pixels != null)
				if (pixels.length != (bufferSizeU*bufferSizeV))
					pixels = null;
			pixels = textRaster.getPixels(0, 0, bufferSizeU, bufferSizeV, pixels);
			return pixels;
		}
		
		
		@Override
		public void setFontFamily(FontFamily family) {
			final String familyString;
			switch (family) {
				case MONOSPACED: familyString = Font.MONOSPACED; break;
				case SERIF: familyString = Font.SERIF; break;
				case SANS_SERIF: familyString = Font.SANS_SERIF; break;
				default:
					throw new IllegalArgumentException("unknown font family: "+family);
			}
			if (font.getFamily().equalsIgnoreCase(familyString)) return;
			this.fontFamily = familyString;
			buildFont();
		}
		
		@Override
		public FontFamily getFontFamily() {
			if (fontFamily.equals(Font.MONOSPACED)) return FontFamily.MONOSPACED;
			if (fontFamily.equals(Font.SERIF)) return FontFamily.SERIF;
			if (fontFamily.equals(Font.SANS_SERIF)) return FontFamily.SANS_SERIF;
			throw new IllegalArgumentException("unknown font family: "+fontFamily);
		}
		
		@Override
		public void setFontStyle(FontStyle style) {
			final int styleInt;
			switch (style) {
				case PLAIN: styleInt = Font.PLAIN; break;
				case BOLD: styleInt = Font.BOLD; break;
				case ITALIC: styleInt = Font.ITALIC; break;
				case BOLD_ITALIC: styleInt = Font.BOLD | Font.ITALIC; break;
				default:
					throw new IllegalArgumentException("unknown font style: "+style);
			}
			if (font.getStyle() == styleInt) return;
			this.fontStyle = styleInt;
			buildFont();
		}

		@Override
		public FontStyle getFontStyle() {
			switch (fontStyle) {
				case Font.PLAIN: return FontStyle.PLAIN;
				case Font.BOLD: return FontStyle.BOLD;
				case Font.ITALIC: return FontStyle.ITALIC;
				case (Font.BOLD | Font.ITALIC): return FontStyle.BOLD_ITALIC;
				default:
					throw new IllegalArgumentException("unknown font style: "+fontStyle);
			}
		}
		
		@Override
		public void setFontSize(int size) {
			if (size <= 0) return;
			if (font.getSize() == size) return;
			this.fontSize = size;
			buildFont();
		}

		@Override
		public int getFontSize() {
			return fontSize;
		}
		

		@Override
		public void setAntialiasing(boolean val) {
			antialiasing = val;
		}
		
		@Override
		public boolean getAntialiasing() {
			return antialiasing;
		}
		
		// -- private helpers --
		
		private void buildFont() {
			font = new Font(fontFamily, fontStyle, fontSize);
		}
		
		private void initTextBuffer(String text) {
			// the first time we call this method the buffer could be null.
			// need to allocate an arbitrary one because calcTextSize() uses it
			if (textBuffer == null) {
				this.bufferSizeU = 200;
				this.bufferSizeV = 20;
				this.textBuffer =	new BufferedImage(bufferSizeU, bufferSizeV,
																						BufferedImage.TYPE_BYTE_GRAY);
				this.textRaster = textBuffer.getRaster();
			}
			
			// determine extents of text to be drawn
			final Rectangle extents = calcTextSize(text);
			
			// if extents are bigger than existing buffer then allocate a new buffer
			if ((extents.width > textBuffer.getWidth()) ||
					(extents.height > textBuffer.getHeight()))
			{
				this.bufferSizeU = extents.width;
				this.bufferSizeV = extents.height;
				this.textBuffer =	new BufferedImage(bufferSizeU, bufferSizeV,
																						BufferedImage.TYPE_BYTE_GRAY);
				this.textRaster = textBuffer.getRaster();
			}
			else  // use existing buffer but prepare for drawing into it
				clearTextBuffer();
		}

		private void clearTextBuffer() {
			for (int u = 0; u < bufferSizeU; u++)
				for (int v = 0; v < bufferSizeV; v++)
					textRaster.setSample(u, v, 0, 0);
		}

		private Rectangle calcTextSize(String txt) {
			final FontMetrics metrics = textBuffer.getGraphics().getFontMetrics(font);
			final int width = metrics.charsWidth(txt.toCharArray(), 0, txt.length());
			final Rectangle extents = new Rectangle();
			extents.x = 0;
			extents.y = 0;
			extents.width = width;
			extents.height = metrics.getHeight() + 10;
			return extents;
		}
		
		private void setAntialiasedText(Graphics g, boolean antialiasedText) {
			Graphics2D g2d = (Graphics2D)g;
			if (antialiasedText)
				g2d.setRenderingHint(
					RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			else
				g2d.setRenderingHint(
					RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		}
	}
}
