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

package imagej.render;

/**
 * Basic text renderer interface, which renders text into an {@code int[]}
 * buffer. Values range from 0 to 255 (for now) and represent grayscale
 * intensities. The buffer is then available afterwards including its
 * dimensions. Users can set font attributes before rendering.
 * 
 * @author Barry DeZonia
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

//	void setTextOutlineWidth(float width);

//	float getTextOutlineWidth();

	public enum FontFamily {
		MONOSPACED, SERIF, SANS_SERIF
	}

	public enum FontStyle {
		PLAIN, BOLD, ITALIC, BOLD_ITALIC
	}

	public enum TextJustification {
		LEFT, CENTER, RIGHT
	}

}
