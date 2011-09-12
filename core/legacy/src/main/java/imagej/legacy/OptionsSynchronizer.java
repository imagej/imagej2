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
import imagej.ImageJ;
import imagej.ext.options.OptionsPlugin;
import imagej.ext.options.OptionsService;

import java.awt.Color;
import java.awt.Font;
import java.util.List;

/**
 * The {@link OptionsSynchronizer} sets IJ1 settings and preferences to reflect
 * values set within IJ2 Options dialogs. 
 * 
 * @author Barry DeZonia
 *
 */
public class OptionsSynchronizer {

	private static OptionsService optionsService = ImageJ.get(OptionsService.class);
	
	public OptionsSynchronizer() {
		// make sure OptionsPlugin fields are initialized
		List<OptionsPlugin> optionsPlugins =
				ImageJ.get(OptionsService.class).getOptions();
		for (OptionsPlugin plugin : optionsPlugins)
			plugin.load();
	}
	
	/**
	 * Updates IJ1 settings and preferences to reflect values set in IJ2 dialogs.
	 */ 
	public void updateIJ1SettingsFromIJ2() {
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
		
	/**
	 * Updates IJ2 options dialog settings to reflect values set by IJ1 plugins.
	 */
	public void updateIJ2SettingsFromIJ1() {
		
	}
	
	// -- helpers --
	
	private static String getString(String className, String fieldName) {
		return (String) optionsService.getOption(className, fieldName);
	}
	
	private static Boolean getBoolean(String className, String fieldName) {
		return (Boolean) optionsService.getOption(className, fieldName);
	}
	
	private static Integer getInteger(String className, String fieldName) {
		return (Integer) optionsService.getOption(className, fieldName);
	}
	
	private static Double getDouble(String className, String fieldName) {
		return (Double) optionsService.getOption(className, fieldName);
	}
	
	private static Color getColor(String className, String fieldName, Color defaultColor) {
		String colorName = getString(className, fieldName);
		return Colors.getColor(colorName, defaultColor);
	}

	/*
	private static void setOption(String className, String fieldName, Object value) {
		Field field = ClassUtils.getField(className, fieldName);
		OptionsPlugin instance = optionsService.getInstance(className);
		ClassUtils.setValue(field, instance, value);
	}
	*/
	
	private void appearenceOptions() {	
		ij.Prefs.antialiasedText = false;
		
		ij.Prefs.antialiasedTools = getBoolean("imagej.core.plugins.options.OptionsAppearance", "antialiasedToolIcons");
		
		ij.Prefs.blackCanvas = getBoolean("imagej.core.plugins.options.OptionsAppearance", "blackCanvas");
		
		ij.Prefs.open100Percent = getBoolean("imagej.core.plugins.options.OptionsAppearance", "fullZoomImages");
		
		ij.Prefs.interpolateScaledImages = getBoolean("imagej.core.plugins.options.OptionsAppearance", "interpZoomedImages");
		
		ij.Prefs.noBorder = getBoolean("imagej.core.plugins.options.OptionsAppearance", "noImageBorder");
		
		ij.Prefs.useInvertingLut = getBoolean("imagej.core.plugins.options.OptionsAppearance", "useInvertingLUT");
		
		// TODO
		// this one needs to have code applied to IJ2. Nothing to set for IJ1.
		//Prefs.get(SettingsKeys.OPTIONS_APPEARANCE_MENU_FONT_SIZE);
	}

	private void arrowOptions() {	
		// TODO - for this next setting there is nothing to synchronize. Changing
		// this setting runs some code in IJ1's UI. Might need some code on the IJ2
		// side that mirrors the behavior. 
		//String color = getString(SettingsKeys.OPTIONS_ARROW_COLOR);
		boolean doubleHeaded = getBoolean("imagej.core.plugins.options.OptionsArrowTool","arrowDoubleHeaded");
		boolean outline = getBoolean("imagej.core.plugins.options.OptionsArrowTool","arrowOutline");
		int size = getInteger("imagej.core.plugins.options.OptionsArrowTool","arrowSize");
		String style = getString("imagej.core.plugins.options.OptionsArrowTool","arrowStyle");
		int width = getInteger("imagej.core.plugins.options.OptionsArrowTool","arrowWidth");

		if (style == null) style = "Filled";
		int styleIndex = 0;
		if (style.equals("Filled")) styleIndex = 0;
		else if (style.equals("Notched")) styleIndex = 1;
		else if (style.equals("Open")) styleIndex = 2;
		else if (style.equals("Headless")) styleIndex = 3;
		
		ij.Prefs.set(Arrow.STYLE_KEY, styleIndex);
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
		Toolbar.setForegroundColor(getColor("imagej.core.plugins.options.OptionsColors","fgColor", Color.white));
		Toolbar.setBackgroundColor(getColor("imagej.core.plugins.options.OptionsColors","bgColor", Color.black));
		Roi.setColor(getColor("imagej.core.plugins.options.OptionsColors","selColor",Color.yellow));
	}
	
	private void compilerOptions() {
		String version = getString("imagej.core.plugins.options.OptionsCompiler","targetJavaVersion");
		if (version == null) version = "1.5";
		if (version.equals("1.4")) ij.Prefs.set("javac.target", 0);
		else if (version.equals("1.5")) ij.Prefs.set("javac.target", 1);
		else if (version.equals("1.6")) ij.Prefs.set("javac.target", 2);
		else if (version.equals("1.7")) ij.Prefs.set("javac.target", 3);
		// TODO
		// this next key has no way to be set programmatically in IJ1
		// Prefs.get(SettingsKeys.OPTIONS_COMPILER_DEBUG_INFO, false);
	}
	
	private void conversionsOptions() {	
		double[] weights = ColorProcessor.getWeightingFactors();
		boolean weighted = !(weights[0]==1d/3d && weights[1]==1d/3d && weights[2]==1d/3d);
		ImageConverter.setDoScaling(getBoolean("imagej.core.plugins.options.OptionsConversions","scaleWhenConverting"));
		ij.Prefs.weightedColor = getBoolean("imagej.core.plugins.options.OptionsConversions","weightedRgbConversions");
		if (!ij.Prefs.weightedColor)
			ColorProcessor.setWeightingFactors(1d/3d, 1d/3d, 1d/3d);
		else if (ij.Prefs.weightedColor && !weighted)
			ColorProcessor.setWeightingFactors(0.299, 0.587, 0.114);
	}
	
	private void dicomOptions() {	
		ij.Prefs.openDicomsAsFloat = getBoolean("imagej.core.plugins.options.OptionsDicom","openAs32bitFloat");
		ij.Prefs.flipXZ = getBoolean("imagej.core.plugins.options.OptionsDicom","rotateXZ");
		ij.Prefs.rotateYZ = getBoolean("imagej.core.plugins.options.OptionsDicom","rotateYZ");
	}
	
	private void fontOptions() {	
		String fontName = getString("imagej.core.plugins.options.OptionsFont","font");
		int fontSize = getInteger("imagej.core.plugins.options.OptionsFont","fontSize");
		String styleName = getString("imagej.core.plugins.options.OptionsFont","fontStyle");
		boolean smooth = getBoolean("imagej.core.plugins.options.OptionsFont","fontSmooth"); 
		
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
		ij.Prefs.copyColumnHeaders = getBoolean("imagej.core.plugins.options.OptionsInputOutput","copyColumnHeaders");
		ij.Prefs.noRowNumbers = !getBoolean("imagej.core.plugins.options.OptionsInputOutput","copyRowNumbers");
		String extension = getString("imagej.core.plugins.options.OptionsInputOutput","tableFileExtension");
		if (extension == null) extension = ".txt";
		ij.Prefs.set("options.ext", extension);
		FileSaver.setJpegQuality(getInteger("imagej.core.plugins.options.OptionsInputOutput","jpegQuality"));
		ij.Prefs.dontSaveHeaders = !getBoolean("imagej.core.plugins.options.OptionsInputOutput","saveColumnHeaders");
		ij.Prefs.intelByteOrder = getBoolean("imagej.core.plugins.options.OptionsInputOutput","saveOrderIntel");
		ij.Prefs.dontSaveRowNumbers = !getBoolean("imagej.core.plugins.options.OptionsInputOutput","saveRowNumbers");
		ij.Prefs.setTransparentIndex(getInteger("imagej.core.plugins.options.OptionsInputOutput","transparentIndex"));
		ij.Prefs.useJFileChooser = getBoolean("imagej.core.plugins.options.OptionsInputOutput","useJFileChooser");
	}
	
	private void lineWidthOptions() {	
		Line.setWidth(getInteger("imagej.core.plugins.options.OptionsLineWidth","lineWidth"));
	}	

	private void memoryAndThreadsOptions() {	
		ij.Prefs.keepUndoBuffers = getBoolean("imagej.core.plugins.options.OptionsMemoryAndThreads","multipleBuffers");
		ij.Prefs.noClickToGC = !getBoolean("imagej.core.plugins.options.OptionsMemoryAndThreads","runGcOnClick");
		ij.Prefs.setThreads(getInteger("imagej.core.plugins.options.OptionsMemoryAndThreads","stackThreads"));
		// TODO
		// nothing to set in this next case. Need IJ2 to fire some code as appropriate
		//Prefs.get(SettingsKeys.OPTIONS_MEMORYTHREADS_MAX_MEMORY);
	}
	
	private void miscOptions() {	
		String divValue = getString("imagej.core.plugins.options.OptionsMisc","divByZeroVal");
		IJ.debugMode = getBoolean("imagej.core.plugins.options.OptionsMisc","debugMode");
		IJ.hideProcessStackDialog = getBoolean("imagej.core.plugins.options.OptionsMisc","hideProcessStackDialog");
		ij.Prefs.moveToMisc = getBoolean("imagej.core.plugins.options.OptionsMisc","moveIsolatedPlugins");
		ij.Prefs.usePointerCursor = getBoolean("imagej.core.plugins.options.OptionsMisc","usePtrCursor");
		ij.Prefs.requireControlKey = getBoolean("imagej.core.plugins.options.OptionsMisc","requireCommandKey");
		ij.Prefs.runSocketListener = getBoolean("imagej.core.plugins.options.OptionsMisc","runSingleInstanceListener");
		
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
		ij.Prefs.pointAddToManager = getBoolean("imagej.core.plugins.options.OptionsPointTool","addToRoiMgr");
		ij.Prefs.pointAutoMeasure = getBoolean("imagej.core.plugins.options.OptionsPointTool","autoMeasure");
		ij.Prefs.pointAutoNextSlice = getBoolean("imagej.core.plugins.options.OptionsPointTool","autoNextSlice");
		ij.Prefs.noPointLabels = !getBoolean("imagej.core.plugins.options.OptionsPointTool","labelPoints");
		Analyzer.markWidth = getInteger("imagej.core.plugins.options.OptionsPointTool","markWidth");
		Roi.setColor(getColor("imagej.core.plugins.options.OptionsPointTool","selectionColor",Color.yellow));
	}

	private void profilePlotOptions() {	
		ij.gui.PlotWindow.autoClose = getBoolean("imagej.core.plugins.options.OptionsProfilePlot","autoClose");
		ij.gui.PlotWindow.saveXValues = !getBoolean("imagej.core.plugins.options.OptionsProfilePlot","noSaveXValues");
		ij.gui.PlotWindow.noGridLines = !getBoolean("imagej.core.plugins.options.OptionsProfilePlot","drawGridLines");
		boolean fixedScale = getBoolean("imagej.core.plugins.options.OptionsProfilePlot","yFixedScale");
		ij.gui.PlotWindow.plotHeight = getInteger("imagej.core.plugins.options.OptionsProfilePlot","height");
		ij.gui.PlotWindow.interpolate = getBoolean("imagej.core.plugins.options.OptionsProfilePlot","interpLineProf");
		ij.gui.PlotWindow.listValues = getBoolean("imagej.core.plugins.options.OptionsProfilePlot","listValues");
		double yMax = getDouble("imagej.core.plugins.options.OptionsProfilePlot","maxY");
		double yMin = getDouble("imagej.core.plugins.options.OptionsProfilePlot","minY");
		ij.Prefs.verticalProfile = getBoolean("imagej.core.plugins.options.OptionsProfilePlot","vertProfile");
		ij.gui.PlotWindow.plotWidth = getInteger("imagej.core.plugins.options.OptionsProfilePlot","width");
		
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
		String server = getString("imagej.core.plugins.options.OptionsProxy","proxyServer");
		if (server != null) {
			ij.Prefs.set("proxy.server", server);
			ij.Prefs.set("proxy.port", getInteger("imagej.core.plugins.options.OptionsProxy","port"));
		}
	}
	
	private void roundRectOptions() {
		int crnDiam = getInteger("imagej.core.plugins.options.OptionsRoundedRectangleTool","cornerDiameter");
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
