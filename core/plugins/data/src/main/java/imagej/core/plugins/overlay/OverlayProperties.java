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

package imagej.core.plugins.overlay;

import imagej.data.Data;
import imagej.data.display.DataView;
import imagej.data.display.ImageDisplay;
import imagej.data.overlay.Overlay;
import imagej.data.overlay.Overlay.ArrowStyle;
import imagej.data.overlay.Overlay.LineStyle;
import imagej.ext.menu.MenuConstants;
import imagej.ext.module.ui.WidgetStyle;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.ext.plugin.PreviewPlugin;
import imagej.options.OptionsService;
import imagej.options.plugins.OptionsOverlay;
import imagej.util.ColorRGB;

import java.util.ArrayList;
import java.util.List;

/**
 * A plugin to change the properties (e.g., line color, line width) of the
 * selected overlays.
 * 
 * @author Curtis Rueden
 * @author Lee Kamentsky
 */
@Plugin(menu = {
	@Menu(label = MenuConstants.IMAGE_LABEL, weight = MenuConstants.IMAGE_WEIGHT,
		mnemonic = MenuConstants.IMAGE_MNEMONIC),
	@Menu(label = "Overlay", mnemonic = 'o'),
	@Menu(label = "Properties...", mnemonic = 'p') }, headless = true)
public class OverlayProperties implements ImageJPlugin, PreviewPlugin {

	static final protected String solidLineStyle = "Solid";
	static final protected String dashLineStyle = "Dash";
	static final protected String dotLineStyle = "Dot";
	static final protected String dotDashLineStyle = "Dot-dash";
	static final protected String noLineStyle = "None";
	static final protected String arrowLineDecoration = "Arrow";
	static final protected String noLineDecoration = "None";

	@Parameter
	private ImageDisplay display;

	@Parameter(label = "Line color", persist = false)
	private ColorRGB lineColor;

	@Parameter(label = "Line width", persist = false, min = "0.1")
	private double lineWidth;

	@Parameter(label = "Line style", persist = false, choices = { solidLineStyle,
		dashLineStyle, dotLineStyle, dotDashLineStyle, noLineStyle })
	private String lineStyle = "Solid";

	@Parameter(label = "Fill color", persist = false)
	private ColorRGB fillColor;

	@Parameter(label = "Alpha", description = "The opacity or alpha of the "
		+ "interior of the overlay (0=transparent, 255=opaque)", persist = false,
		style = WidgetStyle.NUMBER_SCROLL_BAR, min = "0", max = "255")
	private int alpha;

	@Parameter(
		label = "Line start arrow style",
		description = "The arrow style at the starting point of a line or other path",
		persist = false, choices = { noLineDecoration, arrowLineDecoration })
	private String startLineArrowStyle;

	@Parameter(label = "Line end arrow style",
		description = "The arrow style at the end point of a line or other path",
		persist = false, choices = { noLineDecoration, arrowLineDecoration })
	private String endLineArrowStyle;

	@Parameter(label = "Update default overlay settings", persist = false)
	private boolean updateDefaults = false;

	@Parameter(persist = false)
	private OptionsService os;

	public OverlayProperties() {
		// set default values to match the first selected overlay
		final List<Overlay> selected = getSelectedOverlays();
		if (selected.size() > 0) {
			final Overlay overlay = selected.get(0);
			lineColor = overlay.getLineColor();
			lineWidth = overlay.getLineWidth();
			fillColor = overlay.getFillColor();
			alpha = overlay.getAlpha();
			switch (overlay.getLineStyle()) {
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
			switch (overlay.getLineStartArrowStyle()) {
				case NONE:
					startLineArrowStyle = noLineDecoration;
					break;
				case ARROW:
					startLineArrowStyle = arrowLineDecoration;
					break;
			}
			switch (overlay.getLineEndArrowStyle()) {
				case NONE:
					endLineArrowStyle = noLineDecoration;
					break;
				case ARROW:
					endLineArrowStyle = arrowLineDecoration;
					break;
			}
		}
	}

	@Override
	public void run() {
		// change properties of all selected overlays
		final List<Overlay> selected = getSelectedOverlays();
		for (final Overlay overlay : selected) {
			overlay.setLineColor(getLineColor());
			overlay.setLineWidth(getLineWidth());
			overlay.setFillColor(getFillColor());
			overlay.setAlpha(getAlpha());
			overlay.setLineStyle(getLineStyle());
			overlay.setLineStartArrowStyle(getStartLineArrowStyle());
			overlay.setLineEndArrowStyle(getEndLineArrowStyle());
			overlay.update();
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

	private List<Overlay> getSelectedOverlays() {
		final ArrayList<Overlay> result = new ArrayList<Overlay>();
		if (display == null) return result;
		for (final DataView view : display) {
			if (!view.isSelected()) continue;
			final Data data = view.getData();
			if (!(data instanceof Overlay)) continue;
			final Overlay overlay = (Overlay) data;
			result.add(overlay);
		}
		return result;
	}

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
