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

package imagej.ui.swing.tools.overlay;

import imagej.ImageJ;
import imagej.data.display.OverlayService;
import imagej.data.display.OverlayView;
import imagej.data.overlay.Overlay;
import imagej.data.overlay.OverlaySettings;
import imagej.ext.tool.AbstractTool;
import imagej.ui.swing.overlay.IJHotDrawOverlayAdapter;
import imagej.util.ColorRGB;
import imagej.util.ColorRGBA;
import imagej.util.awt.AWTColors;

import java.awt.Color;

import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.decoration.ArrowTip;

/**
 * An abstract class that gives default behavior for the IJHotDrawOverlayAdapter
 * interface.
 * 
 * @author Lee Kamentsky
 */
public abstract class AbstractJHotDrawOverlayAdapter<O extends Overlay>
	extends AbstractTool implements IJHotDrawOverlayAdapter
{

	// NB: The line styles here are taken from
	// org.jhotdraw.draw.action.ButtonFactory.
	// Copyright (c) 1996-2010 by the original authors of
	// JHotDraw and all its contributors. All rights reserved.

	static final protected double[] solidLineStyle = null;
	static final protected double[] dashLineStyle = { 4, 4 };
	static final protected double[] dotLineStyle = { 1, 2 };
	static final protected double[] dotDashLineStyle = { 6, 2, 1, 2 };

	private int priority;
	
	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public void setPriority(final int priority) {
		this.priority = priority;
	}

	@Override
	public void updateFigure(final OverlayView overlay, final Figure figure) {
		final ColorRGB lineColor = overlay.getData().getLineColor();
		if (overlay.getData().getLineStyle() != Overlay.LineStyle.NONE) {
			figure.set(AttributeKeys.STROKE_COLOR, AWTColors.getColor(lineColor));
			
			// FIXME - is this next line dangerous for drawing attributes? width could
			// conceivably need to always stay 0.
			figure.set(AttributeKeys.STROKE_WIDTH, overlay.getData().getLineWidth());
			double[] dash_pattern;
			switch (overlay.getData().getLineStyle()) {
				case SOLID:
					dash_pattern = null;
					break;
				case DASH:
					dash_pattern = dashLineStyle;
					break;
				case DOT:
					dash_pattern = dotLineStyle;
					break;
				case DOT_DASH:
					dash_pattern = dotDashLineStyle;
					break;
				default:
					throw new UnsupportedOperationException("Unsupported line style: " +
						overlay.getData().getLineStyle().toString());
			}
			figure.set(AttributeKeys.STROKE_DASHES, dash_pattern);
		}
		else {
			// Render a "NONE" line style as alpha = transparent.
			figure.set(AttributeKeys.STROKE_COLOR, new Color(0, 0, 0, 0));
		}
		final ColorRGB fillColor = overlay.getData().getFillColor();
		figure.set(AttributeKeys.FILL_COLOR, AWTColors.getColor(fillColor, overlay
			.getData().getAlpha()));
		switch (overlay.getData().getLineStartArrowStyle()) {
			case ARROW:
				figure.set(AttributeKeys.START_DECORATION, new ArrowTip());
				break;
			case NONE:
				figure.set(AttributeKeys.START_DECORATION, null);
		}
		switch (overlay.getData().getLineEndArrowStyle()) {
			case ARROW:
				figure.set(AttributeKeys.END_DECORATION, new ArrowTip());
				break;
			case NONE:
				figure.set(AttributeKeys.END_DECORATION, null);
				break;
		}
	}

	@Override
	public void updateOverlay(final Figure figure, final OverlayView overlay) {
		final Color strokeColor = figure.get(AttributeKeys.STROKE_COLOR);
		overlay.getData().setLineColor(AWTColors.getColorRGB(strokeColor));
		// The line style is intentionally omitted here because it is ambiguous and
		// because there is no UI for setting it by the JHotDraw UI.
		
		// FIXME - is this next line dangerous for drawing attributes? width could
		// conceivably be 0.
		overlay.getData().setLineWidth(figure.get(AttributeKeys.STROKE_WIDTH));
		final Color fillColor = figure.get(AttributeKeys.FILL_COLOR);
		final ColorRGBA imageJColor = AWTColors.getColorRGBA(fillColor);
		overlay.getData().setFillColor(imageJColor);
		overlay.getData().setAlpha(imageJColor.getAlpha());
	}

	public Color getDefaultStrokeColor() {
		// TODO - eliminate deprecated use. Note that simply using getContext() is
		// not sufficient. The figure adapters do not initialize their ImageJ
		// context. So its possible getContext() would return null here and a NPE
		// can get thrown. Happens when you run a legacy plugin if getContext() used
		// here.
		OverlaySettings settings =
				ImageJ.get(OverlayService.class).getDefaultSettings();
		ColorRGB color = settings.getLineColor();
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		return new Color(r,g,b,255);
	}
	
	public Color getDefaultFillColor() {
		// TODO - eliminate deprecated use. Note that simply using getContext() is
		// not sufficient. The figure adapters do not initialize their ImageJ
		// context. So its possible getContext() would return null here and a NPE
		// can get thrown. Happens when you run a legacy plugin if getContext() used
		// here.
		OverlaySettings settings =
				ImageJ.get(OverlayService.class).getDefaultSettings();
		ColorRGB color = settings.getFillColor();
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		int a = settings.getAlpha();
		return new Color(r,g,b,a);
	}
}
