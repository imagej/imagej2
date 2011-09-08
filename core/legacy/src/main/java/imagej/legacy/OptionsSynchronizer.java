//
// OptionsSynchronizer.java
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

package imagej.legacy;

import ij.IJ;
import ij.gui.Arrow;
import ij.gui.Line;
import ij.gui.ProfilePlot;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.gui.Toolbar;
import ij.io.FileSaver;
import ij.plugin.Colors;
import ij.plugin.filter.Analyzer;
import ij.process.ColorProcessor;
import ij.process.FloatBlitter;
import ij.process.ImageConverter;
import imagej.util.Prefs;
import imagej.util.SettingsKeys;

import java.awt.Color;
import java.awt.Font;

/**
 * The {@link OptionsSynchronizer} sets IJ1 settings and preferences to reflect
 * values set within IJ2 Options dialogs. 
 * 
 * @author Barry DeZonia
 *
 */
public class OptionsSynchronizer {

	/**
	 * Updates IJ1 settings and preferences to reflect values set in IJ2 dialogs.
	 */ 
	public void update() {
		appearenceOptions();
		arrowOptions();
		colorOptions();
		compilerOptions();
		conversionsOptions();
		dicomOptions();
		fontOptions();
		ioOptions();
		lineWidthOptions();
		memoryAndThreadsOptions();
		miscOptions();
		pointOptions();
		profilePlotOptions();
		proxyOptions();
		roundRectOptions();
		wandToolOptions();
	}
		
	// -- helpers --
	
	private String getString(String key, String defaultValue) {
		return Prefs.get(key, defaultValue);
	}

	private boolean getBoolean(String key, boolean defaultValue) {
		String defaultString = (defaultValue ? "true" : "false");
		String value = Prefs.get(key, defaultString);
		if ((value == null) || (value == ""))
			value = defaultString;
		return value.equalsIgnoreCase("true");
	}

	private int getInteger(String key, int defaultValue) {
		String defaultString = "" + defaultValue;
		String value = Prefs.get(key, defaultString);
		if (value.equals(""))
			value = defaultString;
		return Integer.parseInt(value);
	}

	private double getDouble(String key, double defaultValue) {
		String defaultString = "" + defaultValue;
		String value = Prefs.get(key, defaultString);
		if (value.equals(""))
			value = defaultString;
		return Double.parseDouble(value);
	}
	
	private Color getColor(String key, Color defaultValue) {
		String defaultString = defaultValue.toString();
		String value = Prefs.get(key, defaultString);
		if (value.equals(""))
			value = defaultString;
		return Colors.getColor(value, defaultValue);
	}

	private void appearenceOptions() {	
		ij.Prefs.antialiasedTools = getBoolean(SettingsKeys.OPTIONS_APPEARANCE_ANTIALIASED_TOOL_ICONS, true);
		ij.Prefs.blackCanvas = getBoolean(SettingsKeys.OPTIONS_APPEARANCE_BLACK_CANVAS, false);
		ij.Prefs.open100Percent = getBoolean(SettingsKeys.OPTIONS_APPEARANCE_FULL_ZOOMED_IMAGES, false);
		ij.Prefs.interpolateScaledImages = getBoolean(SettingsKeys.OPTIONS_APPEARANCE_INTERPOLATE_ZOOMED_IMAGES, false);
		// TODO
		// this one needs to have code applied to IJ2. Nothing to set for IJ1.
		//Prefs.get(SettingsKeys.OPTIONS_APPEARANCE_MENU_FONT_SIZE);
		ij.Prefs.noBorder = getBoolean(SettingsKeys.OPTIONS_APPEARANCE_NO_IMAGE_BORDER, false);
		ij.Prefs.useInvertingLut = getBoolean(SettingsKeys.OPTIONS_APPEARANCE_USE_INVERTING_LUT, false);
	}
	
	private void arrowOptions() {	
		// TODO - for this next setting there is nothing to synchronize. Changing
		// this setting runs some code in IJ1's UI. Might need some code on the IJ2
		// side that mirrors the behavior. 
		//String color = getString(SettingsKeys.OPTIONS_ARROW_COLOR);
		boolean doubleHeaded = getBoolean(SettingsKeys.OPTIONS_ARROW_DOUBLEHEADED, false);
		boolean outline = getBoolean(SettingsKeys.OPTIONS_ARROW_OUTLINE, false);
		int size = getInteger(SettingsKeys.OPTIONS_ARROW_SIZE, 10);
		String style = getString(SettingsKeys.OPTIONS_ARROW_STYLE, "Filled");
		int width = getInteger(SettingsKeys.OPTIONS_ARROW_WIDTH, 2);

		if (style == null) style = "Filled";
		int styleIndex = 0;
		if (style.equals("Filled")) styleIndex = 0;
		else if (style.equals("Notched")) styleIndex = 1;
		else if (style.equals("Open")) styleIndex = 2;
		else if (style.equals("Headless")) styleIndex = 3;

		ij.Prefs.set(Arrow.STYLE_KEY, style);
		ij.Prefs.set(Arrow.WIDTH_KEY, width);
		ij.Prefs.set(Arrow.SIZE_KEY, size);
		ij.Prefs.set(Arrow.OUTLINE_KEY, outline);
		ij.Prefs.set(Arrow.DOUBLE_HEADED_KEY, doubleHeaded);

		Arrow.setDefaultStyle(styleIndex);
		Arrow.setDefaultWidth(width);
		Arrow.setDefaultHeadSize(size);
		Arrow.setDefaultOutline(outline);
		Arrow.setDefaultDoubleHeaded(doubleHeaded);
	}
	
	private void colorOptions() {
		Toolbar.setForegroundColor(getColor(SettingsKeys.OPTIONS_COLORS_FOREGROUND, Color.black));
		Toolbar.setBackgroundColor(getColor(SettingsKeys.OPTIONS_COLORS_BACKGROUND, Color.white));
		Roi.setColor(getColor(SettingsKeys.OPTIONS_COLORS_SELECTION, Color.yellow));
	}
	
	private void compilerOptions() {
		// TODO
		// this next key has no way to be set programmatically in IJ1
		// Prefs.get(SettingsKeys.OPTIONS_COMPILER_DEBUG_INFO, false);
		String version = getString(SettingsKeys.OPTIONS_COMPILER_VERSION, "1.5");
		if (version == null) version = "1.5";
		if (version.equals("1.4")) ij.Prefs.set("javac.target", 0);
		else if (version.equals("1.5")) ij.Prefs.set("javac.target", 1);
		else if (version.equals("1.6")) ij.Prefs.set("javac.target", 2);
		else if (version.equals("1.7")) ij.Prefs.set("javac.target", 3);
	}
	
	private void conversionsOptions() {	
		double[] weights = ColorProcessor.getWeightingFactors();
		boolean weighted = !(weights[0]==1d/3d && weights[1]==1d/3d && weights[2]==1d/3d);
		ImageConverter.setDoScaling(getBoolean(SettingsKeys.OPTIONS_CONVERSIONS_SCALE, true));
		ij.Prefs.weightedColor = getBoolean(SettingsKeys.OPTIONS_CONVERSIONS_WEIGHTED, false);
		if (!ij.Prefs.weightedColor)
			ColorProcessor.setWeightingFactors(1d/3d, 1d/3d, 1d/3d);
		else if (ij.Prefs.weightedColor && !weighted)
			ColorProcessor.setWeightingFactors(0.299, 0.587, 0.114);
	}
	
	private void dicomOptions() {	
		ij.Prefs.openDicomsAsFloat = getBoolean(SettingsKeys.OPTIONS_DICOM_OPEN_FLOAT32, false);
		ij.Prefs.flipXZ = getBoolean(SettingsKeys.OPTIONS_DICOM_ROTATE_XZ, false);
		ij.Prefs.rotateYZ = getBoolean(SettingsKeys.OPTIONS_DICOM_ROTATE_YZ, false);
	}
	
	private void fontOptions() {	
		String fontName = getString(SettingsKeys.OPTIONS_FONT_NAME, "SansSerif");  // TODO - bad default name?
		int fontSize = getInteger(SettingsKeys.OPTIONS_FONT_SIZE, 18);
		String styleName = getString(SettingsKeys.OPTIONS_FONT_STYLE, "");
		boolean smooth = getBoolean(SettingsKeys.OPTIONS_FONT_SMOOTHING, true); 
		
		if (styleName == null) styleName = "";
		int fontStyle = Font.PLAIN;
		if (styleName.equals("Bold"))
			fontStyle = Font.BOLD;
		else if (styleName.equals("Italic"))
			fontStyle = Font.ITALIC;
		else if (styleName.equals("Bold+Italic"))
			fontStyle = Font.BOLD+Font.ITALIC;
		TextRoi.setFont(fontName, fontSize, fontStyle, smooth);
	}
	
	private void ioOptions() {	
		ij.Prefs.copyColumnHeaders = getBoolean(SettingsKeys.OPTIONS_IO_COPY_COLUMNS, false);
		ij.Prefs.noRowNumbers = !getBoolean(SettingsKeys.OPTIONS_IO_COPY_ROWS, true);
		String extension = getString(SettingsKeys.OPTIONS_IO_FILE_EXT, ".txt");
		if (extension == null) extension = ".TXT";
		ij.Prefs.set("options.ext", extension);
		FileSaver.setJpegQuality(getInteger(SettingsKeys.OPTIONS_IO_JPEG_QUALITY, 85));
		ij.Prefs.dontSaveHeaders = !getBoolean(SettingsKeys.OPTIONS_IO_SAVE_COLUMNS, true);
		ij.Prefs.intelByteOrder = getBoolean(SettingsKeys.OPTIONS_IO_SAVE_INTEL, false);
		ij.Prefs.dontSaveRowNumbers = !getBoolean(SettingsKeys.OPTIONS_IO_SAVE_ROWS, true);
		ij.Prefs.setTransparentIndex(getInteger(SettingsKeys.OPTIONS_IO_TRANSPARENT_INDEX, -1));
		ij.Prefs.useJFileChooser = getBoolean(SettingsKeys.OPTIONS_IO_USE_JFILECHOOSER, false);
	}
	
	private void lineWidthOptions() {	
		Line.setWidth(getInteger(SettingsKeys.OPTIONS_LINEWIDTH_WIDTH, 1));
	}	

	private void memoryAndThreadsOptions() {	
		// TODO
		// nothing to set in this next case. Need IJ2 to fire some code as appropriate
		//Prefs.get(SettingsKeys.OPTIONS_MEMORYTHREADS_MAX_MEMORY);
		ij.Prefs.keepUndoBuffers = getBoolean(SettingsKeys.OPTIONS_MEMORYTHREADS_MULTIPLE_UNDO_BUFFERS, false);
		ij.Prefs.noClickToGC = !getBoolean(SettingsKeys.OPTIONS_MEMORYTHREADS_RUN_GC, false);
		ij.Prefs.setThreads(getInteger(SettingsKeys.OPTIONS_MEMORYTHREADS_STACK_THREADS, 2));
	}
	
	private void miscOptions() {	
		String divValue = getString(SettingsKeys.OPTIONS_MISC_DBZ_VALUE,"infinity");
		IJ.debugMode = getBoolean(SettingsKeys.OPTIONS_MISC_DEBUG_MODE, false);
		IJ.hideProcessStackDialog = getBoolean(SettingsKeys.OPTIONS_MISC_HIDE_STACK_MSG, false);
		ij.Prefs.moveToMisc = getBoolean(SettingsKeys.OPTIONS_MISC_MOVE_PLUGINS, false);
		ij.Prefs.usePointerCursor = getBoolean(SettingsKeys.OPTIONS_MISC_POINTER_CURSOR, false);
		ij.Prefs.requireControlKey = getBoolean(SettingsKeys.OPTIONS_MISC_REQUIRE_COMMAND, false);
		ij.Prefs.runSocketListener = getBoolean(SettingsKeys.OPTIONS_MISC_SINGLE_INSTANCE, false);
		
		if (divValue == null) divValue = "infinity";
		if (divValue.equalsIgnoreCase("infinity") || divValue.equalsIgnoreCase("infinite"))
			FloatBlitter.divideByZeroValue = Float.POSITIVE_INFINITY;
		else if (divValue.equalsIgnoreCase("NaN"))
			FloatBlitter.divideByZeroValue = Float.NaN;
		else if (divValue.equalsIgnoreCase("max"))
			FloatBlitter.divideByZeroValue = Float.MAX_VALUE;
		else {
			Float f;
			try {f = new Float(divValue);}
			catch (NumberFormatException e) {f = null;}
			if (f!=null)
				FloatBlitter.divideByZeroValue = f.floatValue();
		}
	}
	
	private void pointOptions() {	
		ij.Prefs.pointAddToManager = getBoolean(SettingsKeys.OPTIONS_POINT_ADD_ROI, false);
		ij.Prefs.pointAutoMeasure = getBoolean(SettingsKeys.OPTIONS_POINT_AUTO_MEASURE, false);
		ij.Prefs.pointAutoNextSlice = getBoolean(SettingsKeys.OPTIONS_POINT_AUTOSLICE, false);
		ij.Prefs.noPointLabels = !getBoolean(SettingsKeys.OPTIONS_POINT_LABEL_POINTS, true);
		Analyzer.markWidth = getInteger(SettingsKeys.OPTIONS_POINT_MARK_WIDTH, 0);
		Roi.setColor(getColor(SettingsKeys.OPTIONS_POINT_SELECTION_COLOR, Color.yellow));
	}
	
	private void profilePlotOptions() {	
		ij.gui.PlotWindow.autoClose = getBoolean(SettingsKeys.OPTIONS_PROFILEPLOT_AUTOCLOSE, false);
		ij.gui.PlotWindow.saveXValues = !getBoolean(SettingsKeys.OPTIONS_PROFILEPLOT_DISCARD_X, false);
		ij.gui.PlotWindow.noGridLines = !getBoolean(SettingsKeys.OPTIONS_PROFILEPLOT_DRAW_GRID, true);
		boolean fixedScale = getBoolean(SettingsKeys.OPTIONS_PROFILEPLOT_FIXED_YSCALE, false);
		ij.gui.PlotWindow.plotHeight = getInteger(SettingsKeys.OPTIONS_PROFILEPLOT_HEIGHT, 200);
		ij.gui.PlotWindow.interpolate = getBoolean(SettingsKeys.OPTIONS_PROFILEPLOT_INTERPOLATE, true);
		ij.gui.PlotWindow.listValues = getBoolean(SettingsKeys.OPTIONS_PROFILEPLOT_LIST_VALUES, false);
		double yMax = getDouble(SettingsKeys.OPTIONS_PROFILEPLOT_MAX_Y, 0);
		double yMin = getDouble(SettingsKeys.OPTIONS_PROFILEPLOT_MIN_Y, 0);
		ij.Prefs.verticalProfile = getBoolean(SettingsKeys.OPTIONS_PROFILEPLOT_VERTICAL, false);
		ij.gui.PlotWindow.plotWidth = getInteger(SettingsKeys.OPTIONS_PROFILEPLOT_WIDTH, 450);
		
		if (!fixedScale && (yMin!=0.0 || yMax!=0.0))
			fixedScale = true;
		if (!fixedScale) {
			yMin = 0.0;
			yMax = 0.0;
		}
		else if (yMin>yMax) {
			double tmp = yMin;
			yMin = yMax;
			yMax = tmp;
		}
		ProfilePlot.setMinAndMax(yMin, yMax);
	}
	
	private void proxyOptions() {	
		// TODO
		// This next setting affects IJ1 dialog. Nothing programmatic can be set.
		// Need pure IJ2 plugins when this setting is utilized.
		//Prefs.get(SettingsKeys.OPTIONS_PROXY_AUTHENTICATE);
		String server = getString(SettingsKeys.OPTIONS_PROXY_SERVER, null);
		if (server != null) {
			ij.Prefs.set("proxy.server", server);
			ij.Prefs.set("proxy.port", getInteger(SettingsKeys.OPTIONS_PROXY_PORT, 8080));
		}
	}
	
	private void roundRectOptions() {
		int crnDiam = getInteger(SettingsKeys.OPTIONS_ROUND_RECT_CORNER_DIAMETER, 20);
		Toolbar.setRoundRectArcSize(crnDiam);
		// TODO
		// IJ1 RectToolOptions does not manipulate Prefs much. It fires
		// code to change behavior when dialog entries changed. No programmatic
		// way to make our settings affect IJ1. Need pure IJ2 support elsewhere.
		//Prefs.get(SettingsKeys.OPTIONS_ROUND_RECT_FILL_COLOR, none);  ?how to handle "none"?
		//Prefs.get(SettingsKeys.OPTIONS_ROUND_RECT_STROKE_COLOR, Color.black);
		//Prefs.get(SettingsKeys.OPTIONS_ROUND_RECT_STROKE_WIDTH, 2);
	}
	
	private void wandToolOptions() {	
		// TODO
		// IJ1 WandToolOptions does not mess with preferences in any way. There
		// is no way in IJ1 to set values without running dialog. Programmatic
		// approach is out. Can't synchronize unless Wayne adds code. May not be
		// needed because Wand is not ever active in IJ2. Right?
		//Prefs.get(SettingsKeys.OPTIONS_WAND_MODE, "Legacy");
		//Prefs.get(SettingsKeys.OPTIONS_WAND_TOLERANCE, 0.0);
	}

}
