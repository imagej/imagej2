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

package imagej.ui.common.awt;

import imagej.render.TextRenderer;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * The AWT implementation of the TextRenderer interface. TODO - relocate to
 * some other subproject to remove AWT dependencies from this subproject.
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
public class AWTTextRenderer implements TextRenderer {

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
