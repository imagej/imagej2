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

import imagej.data.display.OverlayService;
import imagej.data.overlay.Overlay.ArrowStyle;
import imagej.data.overlay.Overlay.LineStyle;
import imagej.util.ColorRGB;
import imagej.util.Colors;

/**
 * Utility class that gives access to default overlay settings such as fill,
 * transparency, arrow styles, etc. There is one OverlaySettings class per
 * {@link OverlayService} instance.
 * 
 * @author Barry DeZonia
 */
public class OverlaySettings {

	// -- instance variables --

	private double lineWidth = 1.0;
	private LineStyle lineStyle = LineStyle.SOLID;
	private ColorRGB lineColor = Colors.YELLOW;
	private ColorRGB fillColor = Colors.LIME;
	private int alpha = 0;
	private ArrowStyle startArrowStyle = ArrowStyle.NONE;
	private ArrowStyle endArrowStyle = ArrowStyle.NONE;

	// -- accessors --

	public double getLineWidth() {
		return lineWidth;
	}

	public void setLineWidth(final double width) {
		lineWidth = width;
	}

	public LineStyle getLineStyle() {
		return lineStyle;
	}

	public void setLineStyle(final LineStyle style) {
		lineStyle = style;
	}

	public ColorRGB getLineColor() {
		return lineColor;
	}

	public void setLineColor(final ColorRGB c) {
		lineColor = c;
	}

	public ColorRGB getFillColor() {
		return fillColor;
	}

	public void setFillColor(final ColorRGB c) {
		fillColor = c;
	}

	public int getAlpha() {
		return alpha;
	}

	public void setAlpha(final int alpha) {
		this.alpha = alpha;
	}

	public ArrowStyle getStartArrowStyle() {
		return startArrowStyle;
	}

	public void setStartArrowStyle(final ArrowStyle style) {
		startArrowStyle = style;
	}

	public ArrowStyle getEndArrowStyle() {
		return endArrowStyle;
	}

	public void setEndArrowStyle(final ArrowStyle style) {
		endArrowStyle = style;
	}

}
