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

import java.awt.Color;
import java.awt.Font;

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
	
	private String getString(String key) {
		return Prefs.get(key);
	}

	private boolean getBoolean(String key) {
		String value = Prefs.get(key);
		if (value == null)
			return false;
		return value.equalsIgnoreCase("true");
	}

	private int getInteger(String key) {
		String value = Prefs.get(key);
		if (value == null)
			return 0;
		return Integer.parseInt(value);
	}

	private double getDouble(String key) {
		String value = Prefs.get(key);
		if (value == null)
			return 0;
		return Double.parseDouble(value);
	}
	
	private Color getColor(String key) {
		String value = Prefs.get(key);
		return Colors.getColor(value, Color.black);
	}

	private void appearenceOptions() {	
		ij.Prefs.antialiasedTools = getBoolean(SettingsKeys.OPTIONS_APPEARANCE_ANTIALIASED_TOOL_ICONS);
		ij.Prefs.blackCanvas = getBoolean(SettingsKeys.OPTIONS_APPEARANCE_BLACK_CANVAS);
		ij.Prefs.open100Percent = getBoolean(SettingsKeys.OPTIONS_APPEARANCE_FULL_ZOOMED_IMAGES);
		ij.Prefs.interpolateScaledImages = getBoolean(SettingsKeys.OPTIONS_APPEARANCE_INTERPOLATE_ZOOMED_IMAGES);
		// TODO
		// this one needs to have code applied to IJ2. Nothing to set for IJ1.
		//Prefs.get(SettingsKeys.OPTIONS_APPEARANCE_MENU_FONT_SIZE);
		ij.Prefs.noBorder = getBoolean(SettingsKeys.OPTIONS_APPEARANCE_NO_IMAGE_BORDER);
		ij.Prefs.useInvertingLut = getBoolean(SettingsKeys.OPTIONS_APPEARANCE_USE_INVERTING_LUT);
	}
	
	private void arrowOptions() {	
		// TODO - for this next setting there is nothing to synchronize. Changing
		// this setting runs some code in IJ1's UI. Might need some code on the IJ2
		// side that mirrors the behavior. 
		//String color = getString(SettingsKeys.OPTIONS_ARROW_COLOR);
		boolean doubleHeaded = getBoolean(SettingsKeys.OPTIONS_ARROW_DOUBLEHEADED);
		boolean outline = getBoolean(SettingsKeys.OPTIONS_ARROW_OUTLINE);
		int size = getInteger(SettingsKeys.OPTIONS_ARROW_SIZE);
		String style = getString(SettingsKeys.OPTIONS_ARROW_STYLE);
		int width = getInteger(SettingsKeys.OPTIONS_ARROW_WIDTH);

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
		Toolbar.setForegroundColor(getColor(SettingsKeys.OPTIONS_COLORS_FOREGROUND));
		Toolbar.setBackgroundColor(getColor(SettingsKeys.OPTIONS_COLORS_BACKGROUND));
		Roi.setColor(getColor(SettingsKeys.OPTIONS_COLORS_SELECTION));
	}
	
	private void compilerOptions() {
		// TODO
		// this next key has no way to be set programmatically in IJ1
		// Prefs.get(SettingsKeys.OPTIONS_COMPILER_DEBUG_INFO);
		String version = getString(SettingsKeys.OPTIONS_COMPILER_VERSION);
		if (version == null) version = "1.5";
		if (version.equals("1.4")) ij.Prefs.set("javac.target", 0);
		else if (version.equals("1.5")) ij.Prefs.set("javac.target", 1);
		else if (version.equals("1.6")) ij.Prefs.set("javac.target", 2);
		else if (version.equals("1.7")) ij.Prefs.set("javac.target", 3);
	}
	
	private void conversionsOptions() {	
		double[] weights = ColorProcessor.getWeightingFactors();
		boolean weighted = !(weights[0]==1d/3d && weights[1]==1d/3d && weights[2]==1d/3d);
		ImageConverter.setDoScaling(getBoolean(SettingsKeys.OPTIONS_CONVERSIONS_SCALE));
		ij.Prefs.weightedColor = getBoolean(SettingsKeys.OPTIONS_CONVERSIONS_WEIGHTED);
		if (!ij.Prefs.weightedColor)
			ColorProcessor.setWeightingFactors(1d/3d, 1d/3d, 1d/3d);
		else if (ij.Prefs.weightedColor && !weighted)
			ColorProcessor.setWeightingFactors(0.299, 0.587, 0.114);
	}
	
	private void dicomOptions() {	
		ij.Prefs.openDicomsAsFloat = getBoolean(SettingsKeys.OPTIONS_DICOM_OPEN_FLOAT32);
		ij.Prefs.flipXZ = getBoolean(SettingsKeys.OPTIONS_DICOM_ROTATE_XZ);
		ij.Prefs.rotateYZ = getBoolean(SettingsKeys.OPTIONS_DICOM_ROTATE_YZ);
	}
	
	private void fontOptions() {	
		String fontName = getString(SettingsKeys.OPTIONS_FONT_NAME);
		int fontSize = getInteger(SettingsKeys.OPTIONS_FONT_SIZE);
		boolean smooth = getBoolean(SettingsKeys.OPTIONS_FONT_SMOOTHING); 
		String styleName = getString(SettingsKeys.OPTIONS_FONT_STYLE);
		
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
		ij.Prefs.copyColumnHeaders = getBoolean(SettingsKeys.OPTIONS_IO_COPY_COLUMNS);
		ij.Prefs.noRowNumbers = !getBoolean(SettingsKeys.OPTIONS_IO_COPY_ROWS);
		String extension = getString(SettingsKeys.OPTIONS_IO_FILE_EXT);
		if (extension == null) extension = ".TXT";
		ij.Prefs.set("options.ext", extension);
		FileSaver.setJpegQuality(getInteger(SettingsKeys.OPTIONS_IO_JPEG_QUALITY));
		ij.Prefs.dontSaveHeaders = !getBoolean(SettingsKeys.OPTIONS_IO_SAVE_COLUMNS);
		ij.Prefs.intelByteOrder = getBoolean(SettingsKeys.OPTIONS_IO_SAVE_INTEL);
		ij.Prefs.dontSaveRowNumbers = !getBoolean(SettingsKeys.OPTIONS_IO_SAVE_ROWS);
		ij.Prefs.setTransparentIndex(getInteger(SettingsKeys.OPTIONS_IO_TRANSPARENT_INDEX));
		ij.Prefs.useJFileChooser = getBoolean(SettingsKeys.OPTIONS_IO_USE_JFILECHOOSER);
	}
	
	private void lineWidthOptions() {	
		Line.setWidth(getInteger(SettingsKeys.OPTIONS_LINEWIDTH_WIDTH));
	}	

	private void memoryAndThreadsOptions() {	
		// TODO
		// nothing to set in this next case. Need IJ2 to fire some code as appropriate
		//Prefs.get(SettingsKeys.OPTIONS_MEMORYTHREADS_MAX_MEMORY);
		ij.Prefs.keepUndoBuffers = getBoolean(SettingsKeys.OPTIONS_MEMORYTHREADS_MULTIPLE_UNDO_BUFFERS);
		ij.Prefs.noClickToGC = !getBoolean(SettingsKeys.OPTIONS_MEMORYTHREADS_RUN_GC);
		ij.Prefs.setThreads(getInteger(SettingsKeys.OPTIONS_MEMORYTHREADS_STACK_THREADS));
	}
	
	private void miscOptions() {	
		String divValue = getString(SettingsKeys.OPTIONS_MISC_DBZ_VALUE);
		IJ.debugMode = getBoolean(SettingsKeys.OPTIONS_MISC_DEBUG_MODE);
		IJ.hideProcessStackDialog = getBoolean(SettingsKeys.OPTIONS_MISC_HIDE_STACK_MSG);
		ij.Prefs.moveToMisc = getBoolean(SettingsKeys.OPTIONS_MISC_MOVE_PLUGINS);
		ij.Prefs.usePointerCursor = getBoolean(SettingsKeys.OPTIONS_MISC_POINTER_CURSOR);
		ij.Prefs.requireControlKey = getBoolean(SettingsKeys.OPTIONS_MISC_REQUIRE_COMMAND);
		ij.Prefs.runSocketListener = getBoolean(SettingsKeys.OPTIONS_MISC_SINGLE_INSTANCE);
		
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
		ij.Prefs.pointAddToManager = getBoolean(SettingsKeys.OPTIONS_POINT_ADD_ROI);
		ij.Prefs.pointAutoMeasure = getBoolean(SettingsKeys.OPTIONS_POINT_AUTO_MEASURE);
		ij.Prefs.pointAutoNextSlice = getBoolean(SettingsKeys.OPTIONS_POINT_AUTOSLICE);
		ij.Prefs.noPointLabels = !getBoolean(SettingsKeys.OPTIONS_POINT_LABEL_POINTS);
		Analyzer.markWidth = getInteger(SettingsKeys.OPTIONS_POINT_MARK_WIDTH);
		Roi.setColor(getColor(SettingsKeys.OPTIONS_POINT_SELECTION_COLOR));
	}
	
	private void profilePlotOptions() {	
		ij.gui.PlotWindow.autoClose = getBoolean(SettingsKeys.OPTIONS_PROFILEPLOT_AUTOCLOSE);
		ij.gui.PlotWindow.saveXValues = !getBoolean(SettingsKeys.OPTIONS_PROFILEPLOT_DISCARD_X);
		ij.gui.PlotWindow.noGridLines = !getBoolean(SettingsKeys.OPTIONS_PROFILEPLOT_DRAW_GRID);
		boolean fixedScale = getBoolean(SettingsKeys.OPTIONS_PROFILEPLOT_FIXED_YSCALE);
		ij.gui.PlotWindow.plotHeight = getInteger(SettingsKeys.OPTIONS_PROFILEPLOT_HEIGHT);
		ij.gui.PlotWindow.interpolate = getBoolean(SettingsKeys.OPTIONS_PROFILEPLOT_INTERPOLATE);
		ij.gui.PlotWindow.listValues = getBoolean(SettingsKeys.OPTIONS_PROFILEPLOT_LIST_VALUES);
		double yMax = getDouble(SettingsKeys.OPTIONS_PROFILEPLOT_MAX_Y);
		double yMin = getDouble(SettingsKeys.OPTIONS_PROFILEPLOT_MIN_Y);
		ij.Prefs.verticalProfile = getBoolean(SettingsKeys.OPTIONS_PROFILEPLOT_VERTICAL);
		ij.gui.PlotWindow.plotWidth = getInteger(SettingsKeys.OPTIONS_PROFILEPLOT_WIDTH);
		
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
		String server = getString(SettingsKeys.OPTIONS_PROXY_SERVER);
		if (server != null) {
			ij.Prefs.set("proxy.server", server);
			ij.Prefs.set("proxy.port", getInteger(SettingsKeys.OPTIONS_PROXY_PORT));
		}
	}
	
	private void roundRectOptions() {
		int crnDiam = getInteger(SettingsKeys.OPTIONS_ROUND_RECT_CORNER_DIAMETER);
		Toolbar.setRoundRectArcSize(crnDiam);
		// TODO
		// IJ1 RectToolOptions does not manipulate Prefs much. It fires
		// code to change behavior when dialog entries changed. No programmatic
		// way to make our settings affect IJ1. Need pure IJ2 support elsewhere.
		//Prefs.get(SettingsKeys.OPTIONS_ROUND_RECT_FILL_COLOR);
		//Prefs.get(SettingsKeys.OPTIONS_ROUND_RECT_STROKE_COLOR);
		//Prefs.get(SettingsKeys.OPTIONS_ROUND_RECT_STROKE_WIDTH);
	}
	
	private void wandToolOptions() {	
		// TODO
		// IJ1 WandToolOptions does not mess with preferences in any way. There
		// is no way in IJ1 to set values without running dialog. Programmatic
		// approach is out. Can't synchronize unless Wayne adds code. May not be
		// needed because Wand is not ever active in IJ2. Right?
		//Prefs.get(SettingsKeys.OPTIONS_WAND_MODE);
		//Prefs.get(SettingsKeys.OPTIONS_WAND_TOLERANCE);
	}

}
