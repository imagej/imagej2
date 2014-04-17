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
 * #L%
 */

package imagej.data.options;

import imagej.data.display.OverlayService;
import imagej.data.overlay.Overlay.ArrowStyle;
import imagej.data.overlay.Overlay.LineStyle;
import imagej.data.overlay.OverlaySettings;
import imagej.menu.MenuConstants;
import imagej.options.OptionsPlugin;
import imagej.widget.NumberWidget;

import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.ColorRGB;
import org.scijava.util.Colors;

/**
 * Runs the Edit::Options::Overlay... dialog.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = OptionsPlugin.class,
	menu = {
		@Menu(label = MenuConstants.IMAGE_LABEL,
			weight = MenuConstants.IMAGE_WEIGHT,
			mnemonic = MenuConstants.IMAGE_MNEMONIC),
		@Menu(label = "Overlay", mnemonic = 'o'),
		@Menu(label = "Overlay Options...") }, label = "Default Overlay Settings")
public class OptionsOverlay extends OptionsPlugin {

	// -- Constants --

	public static final String solidLineStyle = "Solid";
	public static final String dashLineStyle = "Dash";
	public static final String dotLineStyle = "Dot";
	public static final String dotDashLineStyle = "Dot-dash";
	public static final String noLineStyle = "None";
	public static final String arrowLineDecoration = "Arrow";
	public static final String noLineDecoration = "None";

	// -- Parameters  --

	@Parameter(required = false)
	private OverlayService overlayService;

	@Parameter(label = "Line color")
	private ColorRGB lineColor = Colors.YELLOW;

	@Parameter(label = "Line width", min = "0.1")
	private double lineWidth = 1;

	@Parameter(label = "Line style", choices = { solidLineStyle, dashLineStyle,
		dotLineStyle, dotDashLineStyle, noLineStyle })
	private String lineStyle = solidLineStyle;

	@Parameter(label = "Fill color")
	private ColorRGB fillColor = Colors.YELLOW;

	@Parameter(label = "Fill opacity",
		description = "The opacity or alpha of the "
			+ "interior of the overlay (0=transparent, 255=opaque)",
		style = NumberWidget.SCROLL_BAR_STYLE, min = "0", max = "255",
		persistKey = "alpha.1")
	private int alpha = 63;

	@Parameter(
		label = "Line start arrow style",
		description = "The arrow style at the starting point of a line or other path",
		choices = { noLineDecoration, arrowLineDecoration })
	private String startLineArrowStyle = noLineDecoration;

	@Parameter(label = "Line end arrow style",
		description = "The arrow style at the end point of a line or other path",
		choices = { noLineDecoration, arrowLineDecoration })
	private String endLineArrowStyle = noLineDecoration;

	// -- public interface --

	@Override
	public void run() {
		if (overlayService != null) {
			updateSettings(overlayService.getDefaultSettings());
		}
		super.run();
	}

	/** Updates the given overlay settings to match these options. */
	public void updateSettings(final OverlaySettings settings) {
		settings.setLineColor(getLineColor());
		settings.setLineWidth(getLineWidth());
		settings.setLineStyle(getLineStyle());
		settings.setFillColor(getFillColor());
		settings.setAlpha(getAlpha());
		settings.setStartArrowStyle(getStartArrowStyle());
		settings.setEndArrowStyle(getEndArrowStyle());
	}

	public void setLineColor(final ColorRGB color) {
		lineColor = color;
	}

	public ColorRGB getLineColor() {
		return lineColor;
	}

	public void setLineWidth(final double width) {
		if (width < 0.1) this.lineWidth = 0.1;
		else this.lineWidth = width;
	}

	public double getLineWidth() {
		// Be defensive here. I have come across cases where lineWidth is 0.
		if (lineWidth < 0.1) return 0.1;
		return lineWidth;
	}

	public void setLineStyle(final LineStyle style) {
		switch (style) {
			case DASH:
				lineStyle = dashLineStyle;
				break;
			case DOT:
				lineStyle = dotLineStyle;
				break;
			case DOT_DASH:
				lineStyle = dotDashLineStyle;
				break;
			case SOLID:
				lineStyle = solidLineStyle;
				break;
			default:
				lineStyle = noLineStyle;
				break;
		}
	}

	public LineStyle getLineStyle() {
		if (lineStyle.equals(dashLineStyle)) return LineStyle.DASH;
		if (lineStyle.equals(dotLineStyle)) return LineStyle.DOT;
		if (lineStyle.equals(dotDashLineStyle)) return LineStyle.DOT_DASH;
		if (lineStyle.equals(solidLineStyle)) return LineStyle.SOLID;
		return LineStyle.NONE;
	}

	public void setFillColor(final ColorRGB color) {
		fillColor = color;
	}

	public ColorRGB getFillColor() {
		return fillColor;
	}

	public void setAlpha(final int alpha) {
		this.alpha = alpha;
	}

	public int getAlpha() {
		return alpha;
	}

	public void setStartArrowStyle(final ArrowStyle style) {
		if (style == ArrowStyle.ARROW) startLineArrowStyle = arrowLineDecoration;
		else startLineArrowStyle = noLineDecoration;
	}

	public ArrowStyle getStartArrowStyle() {
		if (startLineArrowStyle.equals(arrowLineDecoration)) return ArrowStyle.ARROW;
		return ArrowStyle.NONE;
	}

	public void setEndArrowStyle(final ArrowStyle style) {
		if (style == ArrowStyle.ARROW) endLineArrowStyle = arrowLineDecoration;
		else endLineArrowStyle = noLineDecoration;
	}

	public ArrowStyle getEndArrowStyle() {
		if (endLineArrowStyle.equals(arrowLineDecoration)) return ArrowStyle.ARROW;
		return ArrowStyle.NONE;
	}
}
