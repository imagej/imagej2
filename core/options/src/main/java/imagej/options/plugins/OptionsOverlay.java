//
// OptionsOverlay.java
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

package imagej.options.plugins;

import imagej.data.roi.Overlay.ArrowStyle;
import imagej.data.roi.Overlay.LineStyle;
import imagej.ext.module.ui.WidgetStyle;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.options.OptionsPlugin;
import imagej.util.ColorRGB;
import imagej.util.JavaColorMap;

// TODO - this dialog has limited color support. Users can assign any color
// via rgb but the dialog can only show a few due to the existence of
// "choices" arrays. Ideally this dialog would just display the color with a
// color wheel and we'd get rid of the choices array. Then user could set
// colors to anything.

/**
 * Runs the Edit::Options::Overlay... dialog.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = OptionsPlugin.class, menu = {
	@Menu(label = "Edit", mnemonic = 'e'),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "Overlay...", weight = 16)}, label="Default Overlay Settings")
public class OptionsOverlay extends OptionsPlugin {

	// -- public statics --
	
	static final public String solidLineStyle = "Solid";
	static final public String dashLineStyle = "Dash";
	static final public String dotLineStyle = "Dot";
	static final public String dotDashLineStyle = "Dot-dash";
	static final public String noLineStyle = "None";
	static final public String arrowLineDecoration = "Arrow";
	static final public String noLineDecoration = "None";

	// -- private statics --
	
	static final private String noColorStr = "none";
	static final private String redStr = "red";
	static final private String greenStr = "green";
	static final private String blueStr = "blue";
	static final private String cyanStr = "cyan";
	static final private String magentaStr = "magenta";
	static final private String yellowStr = "yellow";
	static final private String pinkStr = "pink";
	static final private String orangeStr = "orange";
	static final private String blackStr = "black";
	static final private String grayStr = "gray";
	static final private String whiteStr = "white";
	
	@Parameter(label = "Line color", choices = {redStr, greenStr, blueStr,
		cyanStr, magentaStr, yellowStr, pinkStr, orangeStr, blackStr,
		grayStr, whiteStr}
	)
	private String lineColor = yellowStr;

	@Parameter(label = "Line width", min = "0.1")
	private double lineWidth = 1;

	@Parameter(label = "Line style", choices = { solidLineStyle,
		dashLineStyle, dotLineStyle, dotDashLineStyle, noLineStyle })
	private String lineStyle = solidLineStyle;

	@Parameter(label = "Fill color", choices = {noColorStr, redStr, greenStr,
		blueStr, cyanStr, magentaStr, yellowStr, pinkStr, orangeStr, blackStr,
		grayStr, whiteStr}
	)
	private String fillColor = noColorStr;

	@Parameter(label = "Alpha", description = "The opacity or alpha of the "
		+ "interior of the overlay (0=transparent, 255=opaque)",
		style = WidgetStyle.NUMBER_SCROLL_BAR, min = "0", max = "255")
	private int alpha = 255;

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

	public OptionsOverlay() {
		load(); // NB: Load persisted values *after* field initialization.
	}

	// NB - to be compatible with dialog UI must restrict input values
	
	public void setLineColor(ColorRGB color) {
		lineColor = getColorName(color);
		if (lineColor == null) lineColor = yellowStr;
	}
	
	public ColorRGB getLineColor() {
		return getColor(lineColor);
	}
	
	public void setLineWidth(double width) {
		this.lineWidth = width;
	}
	
	public double getLineWidth() {
		return lineWidth;
	}
	
	public void setLineStyle(LineStyle style) {
		switch (style) {
			case DASH: lineStyle = dashLineStyle; break;
			case DOT: lineStyle = dotLineStyle; break;
			case DOT_DASH: lineStyle = dotDashLineStyle; break;
			case SOLID: lineStyle = solidLineStyle; break;
			default: lineStyle = noLineStyle; break;
		}
	}
	
	public LineStyle getLineStyle() {
		if (lineStyle.equals(dashLineStyle)) return LineStyle.DASH;
		if (lineStyle.equals(dotLineStyle)) return LineStyle.DOT;
		if (lineStyle.equals(dotDashLineStyle)) return LineStyle.DOT_DASH;
		if (lineStyle.equals(solidLineStyle)) return LineStyle.SOLID;
		return LineStyle.NONE;
	}
	
	// NB - to be compatible with dialog UI must restrict input values

	public void setFillColor(ColorRGB color) {
		fillColor = getColorName(color);
		if (fillColor == null) fillColor = noColorStr;
	}
	
	public ColorRGB getFillColor() {
		return getColor(fillColor);
	}
	
	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}
	
	public int getAlpha() {
		return alpha;
	}
	
	public void setStartArrowStyle(ArrowStyle style) {
		if (style == ArrowStyle.ARROW)
			startLineArrowStyle = arrowLineDecoration;
		else
			startLineArrowStyle = noLineDecoration;
	}
	
	public ArrowStyle getStartArrowStyle() {
		if (startLineArrowStyle.equals(arrowLineDecoration))
			return ArrowStyle.ARROW;
		return ArrowStyle.NONE;
	}
	
	public void setEndArrowStyle(ArrowStyle style) {
		if (style == ArrowStyle.ARROW)
			endLineArrowStyle = arrowLineDecoration;
		else
			endLineArrowStyle = noLineDecoration;
	}
	
	public ArrowStyle getEndArrowStyle() {
		if (endLineArrowStyle.equals(arrowLineDecoration))
			return ArrowStyle.ARROW;
		return ArrowStyle.NONE;
	}

	// -- private helpers --
	
	private String getColorName(ColorRGB color) {
		if (color.equals(JavaColorMap.BASIC_BLACK)) return blackStr;
		if (color.equals(JavaColorMap.BASIC_BLUE)) return blueStr;
		if (color.equals(JavaColorMap.BASIC_CYAN)) return cyanStr;
		if (color.equals(JavaColorMap.BASIC_GRAY)) return grayStr;
		if (color.equals(JavaColorMap.BASIC_GREEN)) return greenStr;
		if (color.equals(JavaColorMap.BASIC_MAGENTA)) return magentaStr;
		if (color.equals(JavaColorMap.BASIC_ORANGE)) return orangeStr;
		if (color.equals(JavaColorMap.BASIC_PINK)) return pinkStr;
		if (color.equals(JavaColorMap.BASIC_RED)) return redStr;
		if (color.equals(JavaColorMap.BASIC_WHITE)) return whiteStr;
		if (color.equals(JavaColorMap.BASIC_YELLOW)) return yellowStr;
		return null;
	}
	
	private ColorRGB getColor(String colorName) {
		if (colorName.equals(blackStr)) return JavaColorMap.BASIC_BLACK;
		if (colorName.equals(blueStr)) return JavaColorMap.BASIC_BLUE;
		if (colorName.equals(cyanStr)) return JavaColorMap.BASIC_CYAN;
		if (colorName.equals(grayStr)) return JavaColorMap.BASIC_GRAY;
		if (colorName.equals(greenStr)) return JavaColorMap.BASIC_GREEN;
		if (colorName.equals(magentaStr)) return JavaColorMap.BASIC_MAGENTA;
		if (colorName.equals(orangeStr)) return JavaColorMap.BASIC_ORANGE;
		if (colorName.equals(pinkStr)) return JavaColorMap.BASIC_PINK;
		if (colorName.equals(redStr)) return JavaColorMap.BASIC_RED;
		if (colorName.equals(whiteStr)) return JavaColorMap.BASIC_WHITE;
		if (colorName.equals(yellowStr)) return JavaColorMap.BASIC_YELLOW;
		return null;
	}
}
