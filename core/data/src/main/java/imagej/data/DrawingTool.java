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

package imagej.data;

import imagej.util.RealRect;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

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
	private final RandomAccess<? extends RealType<?>> accessor;
	private long lineWidth;
	private long u0, v0;
	private long maxU, maxV;
	private ChannelCollection channels;
	private double intensity;

	private TextRenderer textRenderer;

	public enum FontFamily {
		MONOSPACED, SERIF, SANS_SERIF
	}

	public enum FontStyle {
		PLAIN, BOLD, ITALIC, BOLD_ITALIC
	}

	public enum TextJustification {
		LEFT, CENTER, RIGHT
	}

	// -- constructor --

	/**
	 * Creates a DrawingTool to modify a specified Dataset. Will draw pixel values
	 * using current channel values (default all zero). After construction the
	 * default U axis will be the first non-channel axis. The default V axis will
	 * be the second non-channel axis.
	 */
	public DrawingTool(final Dataset ds) {
		this.dataset = ds;
		this.accessor = ds.getImgPlus().randomAccess();
		this.channels = new ChannelCollection();
		this.lineWidth = 1;
		this.intensity = 1;
		// FIXME - initialize renderer externally later. For now this works.
		this.textRenderer = new AWTTextRenderer();
		this.u0 = 0;
		this.v0 = 0;
		initAxisVariables();
	}

	// -- public interface --

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
		long numChan = 1;
		if (channelAxis != -1) numChan = dataset.dimension(channelAxis);
		for (long c = 0; c < numChan; c++) {
			final double value = intensity * channels.getChannelValue(c);
			if (channelAxis != -1) accessor.setPosition(c, channelAxis);
			accessor.get().setReal(value);
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
				drawPixel(u, v);
	}

	/**
	 * Fills a subset of the current UV plane.
	 */
	public void fill(final RealRect rect) {
		for (long u = (long) rect.x; u < rect.x + rect.width; u++)
			for (long v = (long) rect.y; v < rect.y + rect.height; v++)
				drawPixel(u, v);
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
		channelAxis = dataset.getAxisIndex(Axes.CHANNEL);
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

	/**
	 * Basic text renderer interface. Implementers render text into an int[]
	 * buffer. Values range from 0 to 255 (for now) and represent grayscale
	 * intensities. The buffer is then available afterwards including its
	 * dimensions. Users can set font attributes before rendering.
	 */
	public interface TextRenderer {

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
		
		//void setTextOutlineWidth(float width);

		//float getTextOutlineWidth();
	}

	/**
	 * The AWT implementation of the TextRenderer interface. TODO - relocate to
	 * some other subproject to remove AWT dependencies from this subproject.
	 * 
	 * @author Barry DeZonia
	 * @author Curtis Rueden
	 */
	private class AWTTextRenderer implements TextRenderer {

		private static final int EXTRA_SPACE = 10;
		
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
		//private float outlineWidth;

		public AWTTextRenderer() {
			fontFamily = Font.SANS_SERIF;
			fontStyle = Font.PLAIN;
			fontSize = 12;
			antialiasing = false;
			//outlineWidth = 0;
			buildFont();
			initTextBuffer("42 is my favorite number");
		}

		@Override
		public void renderText(final String text) {
			initTextBuffer(text);
			final Graphics2D g = textBuffer.createGraphics();
			setAntialiasedText(g, antialiasing);
			g.setFont(font);
			final int x = EXTRA_SPACE, y = bufferSizeV / 2;
			//if (outlineWidth > 0)
			//	drawTextOutline(g, text, Color.DARK_GRAY, x, y, outlineWidth);
			g.drawString(text, x, y);
			g.dispose();
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
			if (pixels != null && pixels.length != bufferSizeU * bufferSizeV) {
				pixels = null;
			}
			pixels = textRaster.getPixels(0, 0, bufferSizeU, bufferSizeV, pixels);
			return pixels;
		}

		@Override
		public void setFontFamily(final FontFamily family) {
			final String familyString;
			switch (family) {
				case MONOSPACED:
					familyString = Font.MONOSPACED;
					break;
				case SERIF:
					familyString = Font.SERIF;
					break;
				case SANS_SERIF:
					familyString = Font.SANS_SERIF;
					break;
				default:
					throw new IllegalArgumentException("unknown font family: " + family);
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
			throw new IllegalArgumentException("unknown font family: " + fontFamily);
		}

		@Override
		public void setFontStyle(final FontStyle style) {
			final int styleInt;
			switch (style) {
				case PLAIN:
					styleInt = Font.PLAIN;
					break;
				case BOLD:
					styleInt = Font.BOLD;
					break;
				case ITALIC:
					styleInt = Font.ITALIC;
					break;
				case BOLD_ITALIC:
					styleInt = Font.BOLD | Font.ITALIC;
					break;
				default:
					throw new IllegalArgumentException("unknown font style: " + style);
			}
			if (font.getStyle() == styleInt) return;
			this.fontStyle = styleInt;
			buildFont();
		}

		@Override
		public FontStyle getFontStyle() {
			switch (fontStyle) {
				case Font.PLAIN:
					return FontStyle.PLAIN;
				case Font.BOLD:
					return FontStyle.BOLD;
				case Font.ITALIC:
					return FontStyle.ITALIC;
				case (Font.BOLD | Font.ITALIC):
					return FontStyle.BOLD_ITALIC;
				default:
					throw new IllegalArgumentException("unknown font style: " + fontStyle);
			}
		}

		@Override
		public void setFontSize(final int size) {
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
		public void setAntialiasing(final boolean val) {
			antialiasing = val;
		}

		@Override
		public boolean getAntialiasing() {
			return antialiasing;
		}

		//@Override
		//public void setTextOutlineWidth(float width) {
		//	outlineWidth = width;
		//}

		//@Override
		//public float getTextOutlineWidth() {
		//	return outlineWidth;
		//}

		// -- private helpers --

		private void buildFont() {
			font = new Font(fontFamily, fontStyle, fontSize);
		}

		private void initTextBuffer(final String text) {
			// the first time we call this method the buffer could be null.
			// need to allocate an arbitrary one because calcTextSize() uses it
			if (textBuffer == null) {
				this.bufferSizeU = 200;
				this.bufferSizeV = 20;
				this.textBuffer =
					new BufferedImage(bufferSizeU, bufferSizeV,
						BufferedImage.TYPE_BYTE_GRAY);
				this.textRaster = textBuffer.getRaster();
			}

			// determine extents of text to be drawn
			final Dimension extents = calcTextSize(text);

			// if extents are bigger than existing buffer then allocate a new buffer
			if ((extents.width > textBuffer.getWidth()) ||
				(extents.height > textBuffer.getHeight()))
			{
				this.bufferSizeU = extents.width;
				this.bufferSizeV = extents.height;
				this.textBuffer =
					new BufferedImage(bufferSizeU, bufferSizeV,
						BufferedImage.TYPE_BYTE_GRAY);
				this.textRaster = textBuffer.getRaster();
			}
			else // use existing buffer but prepare for drawing into it
			clearTextBuffer();
		}

		private void clearTextBuffer() {
			for (int u = 0; u < bufferSizeU; u++)
				for (int v = 0; v < bufferSizeV; v++)
					textRaster.setSample(u, v, 0, 0);
		}

		private Dimension calcTextSize(final String txt) {
			final Graphics g = textBuffer.getGraphics();
			final FontMetrics metrics = g.getFontMetrics(font);
			g.dispose();
			final int width = metrics.charsWidth(txt.toCharArray(), 0, txt.length());
			final Dimension extents = new Dimension();
			extents.width = width + EXTRA_SPACE;  // avoid front clipping
			extents.height = metrics.getHeight() + 2*EXTRA_SPACE; // avoid top clipping
			return extents;
		}

		private void setAntialiasedText(final Graphics2D g,
			final boolean antialiasedText)
		{
			final Object antialias =
				antialiasedText ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON
					: RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, antialias);
		}

		/*
		private void drawTextOutline(final Graphics2D g, final String text,
			final Color c, final int x, final int y, final float width)
		{
			final FontRenderContext frc = g.getFontRenderContext();
			final TextLayout textLayout = new TextLayout(text, font, frc);
			final AffineTransform transform = new AffineTransform();
			transform.setToTranslation(x, y);
			final Shape shape = textLayout.getOutline(transform);
			final Color oldColor = g.getColor();
			g.setStroke(new BasicStroke(width));
			g.setColor(c);
			g.draw(shape);
			g.setColor(oldColor);
		}
		*/
	}

}
