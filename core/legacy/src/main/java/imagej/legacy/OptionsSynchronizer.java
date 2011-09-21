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
import imagej.options.OptionsService;

import java.awt.Color;
import java.awt.Font;

/**
 * The options synchronizer bidirectionally synchronizes IJ2 options with
 * IJ1 settings and preferences. 
 * 
 * @author Barry DeZonia
 *
 */
public class OptionsSynchronizer {

	private OptionsService optionsService;

	public OptionsSynchronizer(final OptionsService optionsService) {
		this.optionsService = optionsService;
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
		setOptionsFromPublicStatics();
	}
	
	// -- helpers --
	
	private String getString(String className, String fieldName) {
		return (String) optionsService.getOption(className, fieldName);
	}
	
	private Boolean getBoolean(String className, String fieldName) {
		return (Boolean) optionsService.getOption(className, fieldName);
	}
	
	private Integer getInteger(String className, String fieldName) {
		return (Integer) optionsService.getOption(className, fieldName);
	}
	
	private Double getDouble(String className, String fieldName) {
		return (Double) optionsService.getOption(className, fieldName);
	}
	
	private Color getColor(String className, String fieldName, Color defaultColor) {
		String colorName = getString(className, fieldName);
		return Colors.getColor(colorName, defaultColor);
	}

	private void setOptionValue(String className, String fieldName, Object value) {
		optionsService.setOption(className, fieldName, value);
	}
	
	private void appearenceOptions() {	
		ij.Prefs.antialiasedText = false;
		
		ij.Prefs.antialiasedTools = getBoolean("imagej.options.plugins.OptionsAppearance", "antialiasedToolIcons");
		
		ij.Prefs.blackCanvas = getBoolean("imagej.options.plugins.OptionsAppearance", "blackCanvas");
		
		ij.Prefs.open100Percent = getBoolean("imagej.options.plugins.OptionsAppearance", "fullZoomImages");
		
		ij.Prefs.interpolateScaledImages = getBoolean("imagej.options.plugins.OptionsAppearance", "interpZoomedImages");
		
		ij.Prefs.noBorder = getBoolean("imagej.options.plugins.OptionsAppearance", "noImageBorder");
		
		ij.Prefs.useInvertingLut = getBoolean("imagej.options.plugins.OptionsAppearance", "useInvertingLUT");
		
		// TODO
		// this one needs to have code applied to IJ2. Nothing to set for IJ1.
		//Prefs.get(SettingsKeys.OPTIONS_APPEARANCE_MENU_FONT_SIZE);
	}

	private void arrowOptions() {	
		// TODO - for this next setting there is nothing to synchronize. Changing
		// this setting runs some code in IJ1's UI. Might need some code on the IJ2
		// side that mirrors the behavior. 
		//String color = getString(SettingsKeys.OPTIONS_ARROW_COLOR);
		boolean doubleHeaded = getBoolean("imagej.options.plugins.OptionsArrowTool","arrowDoubleHeaded");
		boolean outline = getBoolean("imagej.options.plugins.OptionsArrowTool","arrowOutline");
		int size = getInteger("imagej.options.plugins.OptionsArrowTool","arrowSize");
		String style = getString("imagej.options.plugins.OptionsArrowTool","arrowStyle");
		int width = getInteger("imagej.options.plugins.OptionsArrowTool","arrowWidth");

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
		Toolbar.setForegroundColor(getColor("imagej.options.plugins.OptionsColors","fgColor", Color.white));
		Toolbar.setBackgroundColor(getColor("imagej.options.plugins.OptionsColors","bgColor", Color.black));
		Roi.setColor(getColor("imagej.options.plugins.OptionsColors","selColor",Color.yellow));
	}
	
	private void compilerOptions() {
		String version = getString("imagej.options.plugins.OptionsCompiler","targetJavaVersion");
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
		ImageConverter.setDoScaling(getBoolean("imagej.options.plugins.OptionsConversions","scaleWhenConverting"));
		ij.Prefs.weightedColor = getBoolean("imagej.options.plugins.OptionsConversions","weightedRgbConversions");
		if (!ij.Prefs.weightedColor)
			ColorProcessor.setWeightingFactors(1d/3d, 1d/3d, 1d/3d);
		else if (ij.Prefs.weightedColor && !weighted)
			ColorProcessor.setWeightingFactors(0.299, 0.587, 0.114);
	}
	
	private void dicomOptions() {	
		ij.Prefs.openDicomsAsFloat = getBoolean("imagej.options.plugins.OptionsDicom","openAs32bitFloat");
		ij.Prefs.flipXZ = getBoolean("imagej.options.plugins.OptionsDicom","rotateXZ");
		ij.Prefs.rotateYZ = getBoolean("imagej.options.plugins.OptionsDicom","rotateYZ");
	}
	
	private void fontOptions() {	
		String fontName = getString("imagej.options.plugins.OptionsFont","font");
		int fontSize = getInteger("imagej.options.plugins.OptionsFont","fontSize");
		String styleName = getString("imagej.options.plugins.OptionsFont","fontStyle");
		boolean smooth = getBoolean("imagej.options.plugins.OptionsFont","fontSmooth"); 
		
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
		ij.Prefs.copyColumnHeaders = getBoolean("imagej.options.plugins.OptionsInputOutput","copyColumnHeaders");
		ij.Prefs.noRowNumbers = !getBoolean("imagej.options.plugins.OptionsInputOutput","copyRowNumbers");
		String extension = getString("imagej.options.plugins.OptionsInputOutput","tableFileExtension");
		if (extension == null) extension = ".txt";
		ij.Prefs.set("options.ext", extension);
		FileSaver.setJpegQuality(getInteger("imagej.options.plugins.OptionsInputOutput","jpegQuality"));
		ij.Prefs.dontSaveHeaders = !getBoolean("imagej.options.plugins.OptionsInputOutput","saveColumnHeaders");
		ij.Prefs.intelByteOrder = getBoolean("imagej.options.plugins.OptionsInputOutput","saveOrderIntel");
		ij.Prefs.dontSaveRowNumbers = !getBoolean("imagej.options.plugins.OptionsInputOutput","saveRowNumbers");
		ij.Prefs.setTransparentIndex(getInteger("imagej.options.plugins.OptionsInputOutput","transparentIndex"));
		ij.Prefs.useJFileChooser = getBoolean("imagej.options.plugins.OptionsInputOutput","useJFileChooser");
	}
	
	private void lineWidthOptions() {	
		Line.setWidth(getInteger("imagej.options.plugins.OptionsLineWidth","lineWidth"));
	}	

	private void memoryAndThreadsOptions() {	
		ij.Prefs.keepUndoBuffers = getBoolean("imagej.options.plugins.OptionsMemoryAndThreads","multipleBuffers");
		ij.Prefs.noClickToGC = !getBoolean("imagej.options.plugins.OptionsMemoryAndThreads","runGcOnClick");
		ij.Prefs.setThreads(getInteger("imagej.options.plugins.OptionsMemoryAndThreads","stackThreads"));
		// TODO
		// nothing to set in this next case. Need IJ2 to fire some code as appropriate
		//Prefs.get(SettingsKeys.OPTIONS_MEMORYTHREADS_MAX_MEMORY);
	}
	
	private void miscOptions() {	
		String divValue = getString("imagej.options.plugins.OptionsMisc","divByZeroVal");
		IJ.debugMode = getBoolean("imagej.options.plugins.OptionsMisc","debugMode");
		IJ.hideProcessStackDialog = getBoolean("imagej.options.plugins.OptionsMisc","hideProcessStackDialog");
		ij.Prefs.moveToMisc = getBoolean("imagej.options.plugins.OptionsMisc","moveIsolatedPlugins");
		ij.Prefs.usePointerCursor = getBoolean("imagej.options.plugins.OptionsMisc","usePtrCursor");
		ij.Prefs.requireControlKey = getBoolean("imagej.options.plugins.OptionsMisc","requireCommandKey");
		ij.Prefs.runSocketListener = getBoolean("imagej.options.plugins.OptionsMisc","runSingleInstanceListener");
		
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
		ij.Prefs.pointAddToManager = getBoolean("imagej.options.plugins.OptionsPointTool","addToRoiMgr");
		ij.Prefs.pointAutoMeasure = getBoolean("imagej.options.plugins.OptionsPointTool","autoMeasure");
		ij.Prefs.pointAutoNextSlice = getBoolean("imagej.options.plugins.OptionsPointTool","autoNextSlice");
		ij.Prefs.noPointLabels = !getBoolean("imagej.options.plugins.OptionsPointTool","labelPoints");
		Analyzer.markWidth = getInteger("imagej.options.plugins.OptionsPointTool","markWidth");
		Roi.setColor(getColor("imagej.options.plugins.OptionsPointTool","selectionColor",Color.yellow));
	}

	private void profilePlotOptions() {	
		ij.gui.PlotWindow.autoClose = getBoolean("imagej.options.plugins.OptionsProfilePlot","autoClose");
		ij.gui.PlotWindow.saveXValues = !getBoolean("imagej.options.plugins.OptionsProfilePlot","noSaveXValues");
		ij.gui.PlotWindow.noGridLines = !getBoolean("imagej.options.plugins.OptionsProfilePlot","drawGridLines");
		boolean fixedScale = getBoolean("imagej.options.plugins.OptionsProfilePlot","yFixedScale");
		ij.gui.PlotWindow.plotHeight = getInteger("imagej.options.plugins.OptionsProfilePlot","height");
		ij.gui.PlotWindow.interpolate = getBoolean("imagej.options.plugins.OptionsProfilePlot","interpLineProf");
		ij.gui.PlotWindow.listValues = getBoolean("imagej.options.plugins.OptionsProfilePlot","listValues");
		double yMax = getDouble("imagej.options.plugins.OptionsProfilePlot","maxY");
		double yMin = getDouble("imagej.options.plugins.OptionsProfilePlot","minY");
		ij.Prefs.verticalProfile = getBoolean("imagej.options.plugins.OptionsProfilePlot","vertProfile");
		ij.gui.PlotWindow.plotWidth = getInteger("imagej.options.plugins.OptionsProfilePlot","width");
		
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
		String server = getString("imagej.options.plugins.OptionsProxy","proxyServer");
		if (server != null) {
			ij.Prefs.set("proxy.server", server);
			ij.Prefs.set("proxy.port", getInteger("imagej.options.plugins.OptionsProxy","port"));
		}
	}
	
	private void roundRectOptions() {
		int crnDiam = getInteger("imagej.options.plugins.OptionsRoundedRectangleTool","cornerDiameter");
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
	
	private void setOptionsFromPublicStatics() {
		setOptionValue("imagej.options.plugins.OptionsAppearance", "antialiasedToolIcons", ij.Prefs.antialiasedTools);
		setOptionValue("imagej.options.plugins.OptionsAppearance", "blackCanvas", ij.Prefs.blackCanvas);
		setOptionValue("imagej.options.plugins.OptionsAppearance", "fullZoomImages",ij.Prefs.open100Percent);
		setOptionValue("imagej.options.plugins.OptionsAppearance", "interpZoomedImages",ij.Prefs.interpolateScaledImages);
		setOptionValue("imagej.options.plugins.OptionsAppearance", "noImageBorder",ij.Prefs.noBorder);
		setOptionValue("imagej.options.plugins.OptionsAppearance", "useInvertingLUT",ij.Prefs.useInvertingLut);
		boolean arrowTwoHeads = Arrow.getDefaultDoubleHeaded();
		setOptionValue("imagej.options.plugins.OptionsArrowTool","arrowDoubleHeaded", arrowTwoHeads);
		boolean arrowOutline = Arrow.getDefaultOutline();
		setOptionValue("imagej.options.plugins.OptionsArrowTool","arrowOutline", arrowOutline);
		int arrowSize = (int)Arrow.getDefaultHeadSize();
		setOptionValue("imagej.options.plugins.OptionsArrowTool","arrowSize", arrowSize);
		int arrowStyle = Arrow.getDefaultStyle();
		String arrowStyleName;
		if (arrowStyle == 1) arrowStyleName = "Notched";
		else if (arrowStyle == 2) arrowStyleName = "Open";
		else if (arrowStyle == 3) arrowStyleName = "Headless";
		else arrowStyleName = "Filled";
		setOptionValue("imagej.options.plugins.OptionsArrowTool","arrowStyle", arrowStyleName);
		int arrowWidth = (int) Arrow.getDefaultWidth();
		setOptionValue("imagej.options.plugins.OptionsArrowTool","arrowWidth",arrowWidth);
		setOptionValue("imagej.options.plugins.OptionsColors","fgColor", Toolbar.getForegroundColor());
		setOptionValue("imagej.options.plugins.OptionsColors","bgColor", Toolbar.getBackgroundColor());
		setOptionValue("imagej.options.plugins.OptionsColors","selColor",Roi.getColor());
		setOptionValue("imagej.options.plugins.OptionsConversions","scaleWhenConverting", ImageConverter.getDoScaling());
		setOptionValue("imagej.options.plugins.OptionsConversions","weightedRgbConversions",ij.Prefs.weightedColor);
		setOptionValue("imagej.options.plugins.OptionsDicom","openAs32bitFloat",ij.Prefs.openDicomsAsFloat);
		setOptionValue("imagej.options.plugins.OptionsDicom","rotateXZ",ij.Prefs.flipXZ);
		setOptionValue("imagej.options.plugins.OptionsDicom","rotateYZ",ij.Prefs.rotateYZ);
		setOptionValue("imagej.options.plugins.OptionsFont","font", TextRoi.getFont());
		setOptionValue("imagej.options.plugins.OptionsFont","fontSize", TextRoi.getSize());
		String fontStyleString;
		int tmp = TextRoi.getStyle();
		if (tmp == Font.BOLD + Font.ITALIC)
			fontStyleString = "Bold+Italic";
		else if (tmp == Font.BOLD)
			fontStyleString = "Bold";
		else if (tmp == Font.ITALIC)
			fontStyleString = "Italic";
		else
			fontStyleString = "";
		setOptionValue("imagej.options.plugins.OptionsFont","fontStyle", fontStyleString);
		setOptionValue("imagej.options.plugins.OptionsInputOutput","copyColumnHeaders",ij.Prefs.copyColumnHeaders);
		setOptionValue("imagej.options.plugins.OptionsInputOutput","copyRowNumbers",!ij.Prefs.noRowNumbers);
		setOptionValue("imagej.options.plugins.OptionsInputOutput","jpegQuality", FileSaver.getJpegQuality());
		setOptionValue("imagej.options.plugins.OptionsInputOutput","saveColumnHeaders", !ij.Prefs.dontSaveHeaders);
		setOptionValue("imagej.options.plugins.OptionsInputOutput","saveOrderIntel",ij.Prefs.intelByteOrder);
		setOptionValue("imagej.options.plugins.OptionsInputOutput","saveRowNumbers", !ij.Prefs.dontSaveRowNumbers);
		setOptionValue("imagej.options.plugins.OptionsInputOutput","transparentIndex", ij.Prefs.getTransparentIndex());
		setOptionValue("imagej.options.plugins.OptionsInputOutput","useJFileChooser",ij.Prefs.useJFileChooser);
		setOptionValue("imagej.options.plugins.OptionsLineWidth","lineWidth", Line.getWidth());
		setOptionValue("imagej.options.plugins.OptionsMemoryAndThreads","multipleBuffers",ij.Prefs.keepUndoBuffers);
		setOptionValue("imagej.options.plugins.OptionsMemoryAndThreads","runGcOnClick",!ij.Prefs.noClickToGC);
		setOptionValue("imagej.options.plugins.OptionsMemoryAndThreads","stackThreads", ij.Prefs.getThreads());
		String dbzString = new Float(FloatBlitter.divideByZeroValue).toString();
		setOptionValue("imagej.options.plugins.OptionsMisc","divByZeroVal", dbzString);
		setOptionValue("imagej.options.plugins.OptionsMisc","debugMode",IJ.debugMode);
		setOptionValue("imagej.options.plugins.OptionsMisc","hideProcessStackDialog",IJ.hideProcessStackDialog);
		setOptionValue("imagej.options.plugins.OptionsMisc","moveIsolatedPlugins",ij.Prefs.moveToMisc);
		setOptionValue("imagej.options.plugins.OptionsMisc","usePtrCursor",ij.Prefs.usePointerCursor);
		setOptionValue("imagej.options.plugins.OptionsMisc","requireCommandKey",ij.Prefs.requireControlKey);
		setOptionValue("imagej.options.plugins.OptionsMisc","runSingleInstanceListener",ij.Prefs.runSocketListener);
		setOptionValue("imagej.options.plugins.OptionsPointTool","addToRoiMgr",ij.Prefs.pointAddToManager);
		setOptionValue("imagej.options.plugins.OptionsPointTool","autoMeasure",ij.Prefs.pointAutoMeasure);
		setOptionValue("imagej.options.plugins.OptionsPointTool","autoNextSlice",ij.Prefs.pointAutoNextSlice);
		setOptionValue("imagej.options.plugins.OptionsPointTool","labelPoints",!ij.Prefs.noPointLabels);
		setOptionValue("imagej.options.plugins.OptionsPointTool","markWidth",Analyzer.markWidth);
		setOptionValue("imagej.options.plugins.OptionsPointTool","selectionColor",Roi.getColor());
		setOptionValue("imagej.options.plugins.OptionsProfilePlot","autoClose",ij.gui.PlotWindow.autoClose);
		setOptionValue("imagej.options.plugins.OptionsProfilePlot","noSaveXValues",!ij.gui.PlotWindow.saveXValues);
		setOptionValue("imagej.options.plugins.OptionsProfilePlot","drawGridLines",!ij.gui.PlotWindow.noGridLines);
		setOptionValue("imagej.options.plugins.OptionsProfilePlot","height",ij.gui.PlotWindow.plotHeight);
		setOptionValue("imagej.options.plugins.OptionsProfilePlot","interpLineProf",ij.gui.PlotWindow.interpolate);
		setOptionValue("imagej.options.plugins.OptionsProfilePlot","listValues",ij.gui.PlotWindow.listValues);
		double yMin = ProfilePlot.getFixedMin();
		double yMax = ProfilePlot.getFixedMax();
		setOptionValue("imagej.options.plugins.OptionsProfilePlot","maxY",yMax);
		setOptionValue("imagej.options.plugins.OptionsProfilePlot","minY",yMin);
		setOptionValue("imagej.options.plugins.OptionsProfilePlot","vertProfile",ij.Prefs.verticalProfile);
		setOptionValue("imagej.options.plugins.OptionsProfilePlot","width",ij.gui.PlotWindow.plotWidth);
		int crnDiam = Toolbar.getRoundRectArcSize();
		setOptionValue("imagej.options.plugins.OptionsRoundedRectangleTool","cornerDiameter", crnDiam);
	}
}
