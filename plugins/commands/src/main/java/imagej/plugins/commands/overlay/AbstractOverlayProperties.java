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

package imagej.plugins.commands.overlay;

import imagej.command.ContextCommand;
import imagej.command.Previewable;
import imagej.data.options.OptionsOverlay;
import imagej.data.overlay.Overlay;
import imagej.data.overlay.Overlay.ArrowStyle;
import imagej.data.overlay.Overlay.LineStyle;
import imagej.options.OptionsService;
import imagej.widget.NumberWidget;

import java.util.ArrayList;
import java.util.List;

import org.scijava.plugin.Parameter;
import org.scijava.util.ColorRGB;
import org.scijava.util.Colors;

/**
 * A plugin to change the properties (e.g., line color, line width) of a given
 * set of overlays.
 * 
 * @author Curtis Rueden
 * @author Lee Kamentsky
 */
public abstract class AbstractOverlayProperties
	extends ContextCommand implements Previewable
{
	static final private String solidLineStyle = "Solid";
	static final private String dashLineStyle = "Dash";
	static final private String dotLineStyle = "Dot";
	static final private String dotDashLineStyle = "Dot-dash";
	static final private String noLineStyle = "None";
	static final private String arrowLineDecoration = "Arrow";
	static final private String noLineDecoration = "None";

	@Parameter(label = "Line color", persist = false)
	private ColorRGB lineColor = Colors.YELLOW;

	@Parameter(label = "Line width", persist = false, min = "0.1")
	private double lineWidth = 1;

	@Parameter(label = "Line style", persist = false, choices = { solidLineStyle,
		dashLineStyle, dotLineStyle, dotDashLineStyle, noLineStyle })
	private String lineStyle = solidLineStyle;

	@Parameter(label = "Fill color", persist = false)
	private ColorRGB fillColor = null;

	@Parameter(label = "Alpha", description = "The opacity or alpha of the "
		+ "interior of the overlay (0=transparent, 255=opaque)", persist = false,
		style = NumberWidget.SCROLL_BAR_STYLE, min = "0", max = "255")
	private int alpha = 0;

	@Parameter(
		label = "Line start arrow style",
		description = "The arrow style at the starting point of a line or other path",
		persist = false, choices = { noLineDecoration, arrowLineDecoration })
	private String startLineArrowStyle = noLineDecoration;

	@Parameter(label = "Line end arrow style",
		description = "The arrow style at the end point of a line or other path",
		persist = false, choices = { noLineDecoration, arrowLineDecoration })
	private String endLineArrowStyle = noLineDecoration;

	@Parameter(label = "Update default overlay settings", persist = false)
	private boolean updateDefaults = false;

	@Parameter
	private OptionsService os;

	// -- instance variables --
	
	private List<Overlay> overlays = new ArrayList<Overlay>();

	// -- protected helper interface --
	
	protected void setOverlays(List<Overlay> overlays) {
		this.overlays = overlays;
	}
	
	/**
	 * Updates all elements from the first overlay within the list of overlays.
	 */
	protected void updateValues() {
		// set default values to match the first overlay
		if (overlays.size() > 0) {
			final Overlay o = overlays.get(0);
			lineColor = o.getLineColor();
			lineWidth = o.getLineWidth();
			fillColor = o.getFillColor();
			alpha = o.getAlpha();
			switch (o.getLineStyle()) {
				case SOLID:
					lineStyle = solidLineStyle;
					break;
				case DASH:
					lineStyle = dashLineStyle;
					break;
				case DOT:
					lineStyle = dotLineStyle;
					break;
				case DOT_DASH:
					lineStyle = dotDashLineStyle;
					break;
				case NONE:
					lineStyle = noLineStyle;
					break;
			}
			switch (o.getLineStartArrowStyle()) {
				case NONE:
					startLineArrowStyle = noLineDecoration;
					break;
				case ARROW:
					startLineArrowStyle = arrowLineDecoration;
					break;
			}
			switch (o.getLineEndArrowStyle()) {
				case NONE:
					endLineArrowStyle = noLineDecoration;
					break;
				case ARROW:
					endLineArrowStyle = arrowLineDecoration;
					break;
			}
		}
	}

	// -- public interface --
	
	@Override
	public void run() {
		// change properties of all selected overlays
		for (final Overlay o : overlays) {
			o.setLineColor(getLineColor());
			o.setLineWidth(getLineWidth());
			o.setFillColor(getFillColor());
			o.setAlpha(getAlpha());
			o.setLineStyle(getLineStyle());
			o.setLineStartArrowStyle(getStartLineArrowStyle());
			o.setLineEndArrowStyle(getEndLineArrowStyle());
			o.update();
		}
		if (updateDefaults) updateDefaults();
	}

	@Override
	public void preview() {
		run();
	}

	@Override
	public void cancel() {
		// TODO
	}

	public ColorRGB getLineColor() {
		return lineColor;
	}

	public double getLineWidth() {
		return lineWidth;
	}

	public ColorRGB getFillColor() {
		return fillColor;
	}

	public int getAlpha() {
		return alpha;
	}

	public Overlay.LineStyle getLineStyle() {
		return decodeLineStyle(lineStyle);
	}

	public Overlay.ArrowStyle getStartLineArrowStyle() {
		return decodeArrowStyle(startLineArrowStyle);
	}

	public Overlay.ArrowStyle getEndLineArrowStyle() {
		return decodeArrowStyle(endLineArrowStyle);
	}

	public boolean isUpdateDefaults() {
		return updateDefaults;
	}

	public void setUpdateDefaults(final boolean updateDefaults) {
		this.updateDefaults = updateDefaults;
	}

	// -- private helpers --

	private void updateDefaults() {
		final OptionsOverlay options = os.getOptions(OptionsOverlay.class);
		options.setLineWidth(getLineWidth());
		options.setLineColor(getLineColor());
		options.setLineStyle(getLineStyle());
		options.setFillColor(getFillColor());
		options.setAlpha(getAlpha());
		options.setStartArrowStyle(getStartLineArrowStyle());
		options.setEndArrowStyle(getEndLineArrowStyle());
		options.run();
	}

	private Overlay.LineStyle decodeLineStyle(final String style) {
		
		if (style == null) return LineStyle.SOLID;
		
		if (style.equals(solidLineStyle)) {
			return LineStyle.SOLID;
		}
		else if (style.equals(dashLineStyle)) {
			return LineStyle.DASH;
		}
		else if (style.equals(dotLineStyle)) {
			return LineStyle.DOT;
		}
		else if (style.equals(dotDashLineStyle)) {
			return LineStyle.DOT_DASH;
		}
		else if (style.equals(noLineStyle)) {
			return LineStyle.NONE;
		}
		else {
			throw new UnsupportedOperationException("Unimplemented style: " + style);
		}
	}

	private Overlay.ArrowStyle decodeArrowStyle(final String style) {
		
		if (style == null) return ArrowStyle.NONE;
		
		if (style.equals(arrowLineDecoration)) {
			return ArrowStyle.ARROW;
		}
		else if (style.equals(noLineDecoration)) {
			return ArrowStyle.NONE;
		}
		else {
			throw new UnsupportedOperationException("Unimplemented style: " + style);
		}
	}
	
}
