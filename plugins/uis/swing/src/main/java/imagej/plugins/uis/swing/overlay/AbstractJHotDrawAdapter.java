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

package imagej.plugins.uis.swing.overlay;

import imagej.data.display.ImageCanvas;
import imagej.data.display.ImageDisplay;
import imagej.data.display.OverlayService;
import imagej.data.display.OverlayView;
import imagej.data.overlay.Overlay;
import imagej.data.overlay.OverlaySettings;
import imagej.display.Display;
import imagej.util.awt.AWTColors;

import java.awt.Color;
import java.awt.Shape;

import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.decoration.ArrowTip;
import org.scijava.plugin.AbstractRichPlugin;
import org.scijava.plugin.Parameter;
import org.scijava.util.ColorRGB;
import org.scijava.util.IntCoords;
import org.scijava.util.MiscUtils;
import org.scijava.util.RealCoords;

/**
 * An abstract class that gives default behavior for the {@link JHotDrawAdapter}
 * interface.
 * 
 * @author Lee Kamentsky
 */
public abstract class AbstractJHotDrawAdapter<O extends Overlay, F extends Figure>
	extends AbstractRichPlugin implements JHotDrawAdapter<F>
{

	// NB: The line styles here are taken from
	// org.jhotdraw.draw.action.ButtonFactory.
	// Copyright (c) 1996-2010 by the original authors of
	// JHotDraw and all its contributors. All rights reserved.

	protected static final double[] SOLID_LINE_STYLE = null;
	protected static final double[] DASH_LINE_STYLE = { 4, 4 };
	protected static final double[] DOT_LINE_STYLE = { 1, 2 };
	protected static final double[] DOT_DASH_LINE_STYLE = { 6, 2, 1, 2 };

	@Parameter
	private OverlayService overlayService;

	/**
	 * Mouse position in <em>data</em> coordinates of the last mouse press
	 * recorded by {@link #mouseDown(Display, int, int)}. This information is
	 * recorded for potential status reporting by certain tools.
	 */
	private RealCoords down;

	/**
	 * Mouse position in <em>data</em> coordinates of the last mouse drag recorded
	 * by {@link #mouseDrag(Display, int, int)}. This information is recorded for
	 * potential status reporting by certain tools.
	 */
	private RealCoords drag;

	// -- AbstractJHotDrawAdapter methods --

	/** Converts a figure into an AWT Shape. */
	public abstract Shape toShape(final F figure);

	// -- JHotDrawAdapter methods --

	@Override
	public void updateFigure(final OverlayView view, final F figure) {
		final Overlay overlay = view.getData();
		final ColorRGB lineColor = overlay.getLineColor();
		if (overlay.getLineStyle() != Overlay.LineStyle.NONE) {
			set(figure, AttributeKeys.STROKE_COLOR, AWTColors.getColor(lineColor));

			// FIXME - is this next line dangerous for drawing attributes? width could
			// conceivably need to always stay 0.
			set(figure, AttributeKeys.STROKE_WIDTH, overlay.getLineWidth());
			double[] dash_pattern;
			switch (overlay.getLineStyle()) {
				case SOLID:
					dash_pattern = SOLID_LINE_STYLE;
					break;
				case DASH:
					dash_pattern = DASH_LINE_STYLE;
					break;
				case DOT:
					dash_pattern = DOT_LINE_STYLE;
					break;
				case DOT_DASH:
					dash_pattern = DOT_DASH_LINE_STYLE;
					break;
				default:
					throw new UnsupportedOperationException("Unsupported line style: " +
						overlay.getLineStyle());
			}
			set(figure, AttributeKeys.STROKE_DASHES, dash_pattern);
		}
		else {
			// Render a "NONE" line style as alpha = transparent.
			set(figure, AttributeKeys.STROKE_COLOR, new Color(0, 0, 0, 0));
		}
		final ColorRGB fillColor = overlay.getFillColor();
		final int alpha = overlay.getAlpha();
		set(figure, AttributeKeys.FILL_COLOR, AWTColors.getColor(fillColor, alpha));
		switch (overlay.getLineStartArrowStyle()) {
			case ARROW:
				set(figure, AttributeKeys.START_DECORATION, new ArrowTip());
				break;
			case NONE:
				set(figure, AttributeKeys.START_DECORATION, null);
		}
		switch (overlay.getLineEndArrowStyle()) {
			case ARROW:
				set(figure, AttributeKeys.END_DECORATION, new ArrowTip());
				break;
			case NONE:
				set(figure, AttributeKeys.END_DECORATION, null);
				break;
		}
	}

	@Override
	public void updateOverlay(final F figure, final OverlayView view) {
		final Color strokeColor = figure.get(AttributeKeys.STROKE_COLOR);
		final Overlay overlay = view.getData();
		overlay.setLineColor(AWTColors.getColorRGB(strokeColor));
		// The line style is intentionally omitted here because it is ambiguous and
		// because there is no UI for setting it by the JHotDraw UI.

		// FIXME - is this next line dangerous for drawing attributes? width could
		// conceivably be 0.
		overlay.setLineWidth(figure.get(AttributeKeys.STROKE_WIDTH));
		final Color fillColor = figure.get(AttributeKeys.FILL_COLOR);
		overlay.setFillColor(AWTColors.getColorRGB(fillColor));
		overlay.setAlpha(fillColor.getAlpha());
	}

	@Override
	public void mouseDown(final Display<?> d, final int x, final int y) {
		down = getDataCoords(d, x, y);
	}

	@Override
	public void mouseDrag(final Display<?> d, final int x, final int y) {
		drag = getDataCoords(d, x, y);
		report(down, drag);
	}

	@Override
	public void report(final RealCoords p1, final RealCoords p2) {
		// NB: Subclasses can override to report something.
	}

	// -- Internal methods --

	protected void initDefaultSettings(final F figure) {
		final OverlaySettings settings = overlayService.getDefaultSettings();
		set(figure, AttributeKeys.STROKE_WIDTH, getDefaultLineWidth(settings));
		set(figure, AttributeKeys.FILL_COLOR, getDefaultFillColor(settings));
		set(figure, AttributeKeys.STROKE_COLOR, getDefaultStrokeColor(settings));
		// Avoid IllegalArgumentException: miter limit < 1 on the EDT
		set(figure, AttributeKeys.IS_STROKE_MITER_LIMIT_FACTOR, false);
	}

	// -- Helper methods --

	private double getDefaultLineWidth(final OverlaySettings settings) {
		return settings.getLineWidth();
	}

	private Color getDefaultStrokeColor(final OverlaySettings settings) {
		final ColorRGB color = settings.getLineColor();
		final int r = color.getRed();
		final int g = color.getGreen();
		final int b = color.getBlue();
		return new Color(r, g, b, 255);
	}

	private Color getDefaultFillColor(final OverlaySettings settings) {
		final ColorRGB color = settings.getFillColor();
		final int r = color.getRed();
		final int g = color.getGreen();
		final int b = color.getBlue();
		final int a = settings.getAlpha();
		return new Color(r, g, b, a);
	}

	/**
	 * Gets the coordinates in <em>data</em> space for the given (x, y) pixel
	 * coordinates.
	 */
	private RealCoords getDataCoords(final Display<?> d, final int x,
		final int y)
	{
		if (!(d instanceof ImageDisplay)) return null;
		final ImageDisplay imageDisplay = (ImageDisplay) d;
		final ImageCanvas canvas = imageDisplay.getCanvas();
		return canvas.panelToDataCoords(new IntCoords(x, y));
	}

	private <T> void set(final F fig, final AttributeKey<T> key, final T value) {
		if (MiscUtils.equal(value, fig.get(key))) {
			// NB: Do not trigger an attribute change event if value already matches.
			return;
		}
		fig.set(key, value);
	}

}
