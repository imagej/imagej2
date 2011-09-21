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
import ij.Prefs;
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
import imagej.options.OptionsPlugin;
import imagej.options.OptionsService;
import imagej.options.plugins.OptionsAppearance;
import imagej.options.plugins.OptionsArrowTool;
import imagej.options.plugins.OptionsColors;
import imagej.options.plugins.OptionsCompiler;
import imagej.options.plugins.OptionsConversions;
import imagej.options.plugins.OptionsDicom;
import imagej.options.plugins.OptionsFont;
import imagej.options.plugins.OptionsInputOutput;
import imagej.options.plugins.OptionsLineWidth;
import imagej.options.plugins.OptionsMemoryAndThreads;
import imagej.options.plugins.OptionsMisc;
import imagej.options.plugins.OptionsPointTool;
import imagej.options.plugins.OptionsProfilePlot;
import imagej.options.plugins.OptionsProxy;
import imagej.options.plugins.OptionsRoundedRectangleTool;

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
		appearanceOptions();
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
	
	private <O extends OptionsPlugin> String getString(Class<O> optionsClass, String fieldName) {
		return (String) optionsService.getOption(optionsClass, fieldName);
	}
	
	private <O extends OptionsPlugin> Boolean getBoolean(Class<O> optionsClass, String fieldName) {
		return (Boolean) optionsService.getOption(optionsClass, fieldName);
	}
	
	private <O extends OptionsPlugin> Integer getInteger(Class<O> optionsClass, String fieldName) {
		return (Integer) optionsService.getOption(optionsClass, fieldName);
	}
	
	private <O extends OptionsPlugin> Double getDouble(Class<O> optionsClass, String fieldName) {
		return (Double) optionsService.getOption(optionsClass, fieldName);
	}
	
	private <O extends OptionsPlugin> Color getColor(Class<O> optionsClass, String fieldName, Color defaultColor) {
		String colorName = getString(optionsClass, fieldName);
		return Colors.getColor(colorName, defaultColor);
	}

	private void appearanceOptions() {	
		Prefs.antialiasedText = false;
		
		Prefs.antialiasedTools = getBoolean(OptionsAppearance.class, "antialiasedToolIcons");
		
		Prefs.blackCanvas = getBoolean(OptionsAppearance.class, "blackCanvas");
		
		Prefs.open100Percent = getBoolean(OptionsAppearance.class, "fullZoomImages");
		
		Prefs.interpolateScaledImages = getBoolean(OptionsAppearance.class, "interpZoomedImages");
		
		Prefs.noBorder = getBoolean(OptionsAppearance.class, "noImageBorder");
		
		Prefs.useInvertingLut = getBoolean(OptionsAppearance.class, "useInvertingLUT");
		
		// TODO
		// this one needs to have code applied to IJ2. Nothing to set for IJ1.
		//Prefs.get(SettingsKeys.OPTIONS_APPEARANCE_MENU_FONT_SIZE);
	}

	private void arrowOptions() {	
		// TODO - for this next setting there is nothing to synchronize. Changing
		// this setting runs some code in IJ1's UI. Might need some code on the IJ2
		// side that mirrors the behavior. 
		//String color = getString(SettingsKeys.OPTIONS_ARROW_COLOR);
		boolean doubleHeaded = getBoolean(OptionsArrowTool.class,"arrowDoubleHeaded");
		boolean outline = getBoolean(OptionsArrowTool.class,"arrowOutline");
		int size = getInteger(OptionsArrowTool.class,"arrowSize");
		String style = getString(OptionsArrowTool.class,"arrowStyle");
		int width = getInteger(OptionsArrowTool.class,"arrowWidth");

		if (style == null) style = "Filled";
		int styleIndex = 0;
		if (style.equals("Filled")) styleIndex = 0;
		else if (style.equals("Notched")) styleIndex = 1;
		else if (style.equals("Open")) styleIndex = 2;
		else if (style.equals("Headless")) styleIndex = 3;
		
		Prefs.set(Arrow.STYLE_KEY, styleIndex);
		Prefs.set(Arrow.WIDTH_KEY, width);
		Prefs.set(Arrow.SIZE_KEY, size);
		Prefs.set(Arrow.OUTLINE_KEY, outline);
		Prefs.set(Arrow.DOUBLE_HEADED_KEY, doubleHeaded);

		Arrow.setDefaultStyle(styleIndex);
		Arrow.setDefaultWidth(width);
		Arrow.setDefaultHeadSize(size);
		Arrow.setDefaultOutline(outline);
		Arrow.setDefaultDoubleHeaded(doubleHeaded);
	}
	
	private void colorOptions() {
		Toolbar.setForegroundColor(getColor(OptionsColors.class,"fgColor", Color.white));
		Toolbar.setBackgroundColor(getColor(OptionsColors.class,"bgColor", Color.black));
		Roi.setColor(getColor(OptionsColors.class,"selColor",Color.yellow));
	}
	
	private void compilerOptions() {
		String version = getString(OptionsCompiler.class,"targetJavaVersion");
		if (version == null) version = "1.5";
		if (version.equals("1.4")) Prefs.set("javac.target", 0);
		else if (version.equals("1.5")) Prefs.set("javac.target", 1);
		else if (version.equals("1.6")) Prefs.set("javac.target", 2);
		else if (version.equals("1.7")) Prefs.set("javac.target", 3);
		// TODO
		// this next key has no way to be set programmatically in IJ1
		// Prefs.get(SettingsKeys.OPTIONS_COMPILER_DEBUG_INFO, false);
	}
	
	private void conversionsOptions() {	
		double[] weights = ColorProcessor.getWeightingFactors();
		boolean weighted = !(weights[0]==1d/3d && weights[1]==1d/3d && weights[2]==1d/3d);
		ImageConverter.setDoScaling(getBoolean(OptionsConversions.class,"scaleWhenConverting"));
		Prefs.weightedColor = getBoolean(OptionsConversions.class,"weightedRgbConversions");
		if (!Prefs.weightedColor)
			ColorProcessor.setWeightingFactors(1d/3d, 1d/3d, 1d/3d);
		else if (Prefs.weightedColor && !weighted)
			ColorProcessor.setWeightingFactors(0.299, 0.587, 0.114);
	}
	
	private void dicomOptions() {	
		Prefs.openDicomsAsFloat = getBoolean(OptionsDicom.class,"openAs32bitFloat");
		Prefs.flipXZ = getBoolean(OptionsDicom.class,"rotateXZ");
		Prefs.rotateYZ = getBoolean(OptionsDicom.class,"rotateYZ");
	}
	
	private void fontOptions() {	
		String fontName = getString(OptionsFont.class,"font");
		int fontSize = getInteger(OptionsFont.class,"fontSize");
		String styleName = getString(OptionsFont.class,"fontStyle");
		boolean smooth = getBoolean(OptionsFont.class,"fontSmooth"); 
		
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
		Prefs.copyColumnHeaders = getBoolean(OptionsInputOutput.class,"copyColumnHeaders");
		Prefs.noRowNumbers = !getBoolean(OptionsInputOutput.class,"copyRowNumbers");
		String extension = getString(OptionsInputOutput.class,"tableFileExtension");
		if (extension == null) extension = ".txt";
		Prefs.set("options.ext", extension);
		FileSaver.setJpegQuality(getInteger(OptionsInputOutput.class,"jpegQuality"));
		Prefs.dontSaveHeaders = !getBoolean(OptionsInputOutput.class,"saveColumnHeaders");
		Prefs.intelByteOrder = getBoolean(OptionsInputOutput.class,"saveOrderIntel");
		Prefs.dontSaveRowNumbers = !getBoolean(OptionsInputOutput.class,"saveRowNumbers");
		Prefs.setTransparentIndex(getInteger(OptionsInputOutput.class,"transparentIndex"));
		Prefs.useJFileChooser = getBoolean(OptionsInputOutput.class,"useJFileChooser");
	}
	
	private void lineWidthOptions() {	
		Line.setWidth(getInteger(OptionsLineWidth.class,"lineWidth"));
	}	

	private void memoryAndThreadsOptions() {	
		Prefs.keepUndoBuffers = getBoolean(OptionsMemoryAndThreads.class,"multipleBuffers");
		Prefs.noClickToGC = !getBoolean(OptionsMemoryAndThreads.class,"runGcOnClick");
		Prefs.setThreads(getInteger(OptionsMemoryAndThreads.class,"stackThreads"));
		// TODO
		// nothing to set in this next case. Need IJ2 to fire some code as appropriate
		//Prefs.get(SettingsKeys.OPTIONS_MEMORYTHREADS_MAX_MEMORY);
	}
	
	private void miscOptions() {	
		String divValue = getString(OptionsMisc.class,"divByZeroVal");
		IJ.debugMode = getBoolean(OptionsMisc.class,"debugMode");
		IJ.hideProcessStackDialog = getBoolean(OptionsMisc.class,"hideProcessStackDialog");
		Prefs.moveToMisc = getBoolean(OptionsMisc.class,"moveIsolatedPlugins");
		Prefs.usePointerCursor = getBoolean(OptionsMisc.class,"usePtrCursor");
		Prefs.requireControlKey = getBoolean(OptionsMisc.class,"requireCommandKey");
		Prefs.runSocketListener = getBoolean(OptionsMisc.class,"runSingleInstanceListener");
		
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
		Prefs.pointAddToManager = getBoolean(OptionsPointTool.class,"addToRoiMgr");
		Prefs.pointAutoMeasure = getBoolean(OptionsPointTool.class,"autoMeasure");
		Prefs.pointAutoNextSlice = getBoolean(OptionsPointTool.class,"autoNextSlice");
		Prefs.noPointLabels = !getBoolean(OptionsPointTool.class,"labelPoints");
		Analyzer.markWidth = getInteger(OptionsPointTool.class,"markWidth");
		Roi.setColor(getColor(OptionsPointTool.class,"selectionColor",Color.yellow));
	}

	private void profilePlotOptions() {	
		ij.gui.PlotWindow.autoClose = getBoolean(OptionsProfilePlot.class,"autoClose");
		ij.gui.PlotWindow.saveXValues = !getBoolean(OptionsProfilePlot.class,"noSaveXValues");
		ij.gui.PlotWindow.noGridLines = !getBoolean(OptionsProfilePlot.class,"drawGridLines");
		boolean fixedScale = getBoolean(OptionsProfilePlot.class,"yFixedScale");
		ij.gui.PlotWindow.plotHeight = getInteger(OptionsProfilePlot.class,"height");
		ij.gui.PlotWindow.interpolate = getBoolean(OptionsProfilePlot.class,"interpLineProf");
		ij.gui.PlotWindow.listValues = getBoolean(OptionsProfilePlot.class,"listValues");
		double yMax = getDouble(OptionsProfilePlot.class,"maxY");
		double yMin = getDouble(OptionsProfilePlot.class,"minY");
		Prefs.verticalProfile = getBoolean(OptionsProfilePlot.class,"vertProfile");
		ij.gui.PlotWindow.plotWidth = getInteger(OptionsProfilePlot.class,"width");
		
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
		String server = getString(OptionsProxy.class,"proxyServer");
		if (server != null) {
			Prefs.set("proxy.server", server);
			Prefs.set("proxy.port", getInteger(OptionsProxy.class,"port"));
		}
	}
	
	private void roundRectOptions() {
		int crnDiam = getInteger(OptionsRoundedRectangleTool.class,"cornerDiameter");
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
		optionsService.setOption(OptionsAppearance.class, "antialiasedToolIcons", Prefs.antialiasedTools);
		optionsService.setOption(OptionsAppearance.class, "blackCanvas", Prefs.blackCanvas);
		optionsService.setOption(OptionsAppearance.class, "fullZoomImages",Prefs.open100Percent);
		optionsService.setOption(OptionsAppearance.class, "interpZoomedImages",Prefs.interpolateScaledImages);
		optionsService.setOption(OptionsAppearance.class, "noImageBorder",Prefs.noBorder);
		optionsService.setOption(OptionsAppearance.class, "useInvertingLUT",Prefs.useInvertingLut);
		boolean arrowTwoHeads = Arrow.getDefaultDoubleHeaded();
		optionsService.setOption(OptionsArrowTool.class,"arrowDoubleHeaded", arrowTwoHeads);
		boolean arrowOutline = Arrow.getDefaultOutline();
		optionsService.setOption(OptionsArrowTool.class,"arrowOutline", arrowOutline);
		int arrowSize = (int)Arrow.getDefaultHeadSize();
		optionsService.setOption(OptionsArrowTool.class,"arrowSize", arrowSize);
		int arrowStyle = Arrow.getDefaultStyle();
		String arrowStyleName;
		if (arrowStyle == 1) arrowStyleName = "Notched";
		else if (arrowStyle == 2) arrowStyleName = "Open";
		else if (arrowStyle == 3) arrowStyleName = "Headless";
		else arrowStyleName = "Filled";
		optionsService.setOption(OptionsArrowTool.class,"arrowStyle", arrowStyleName);
		int arrowWidth = (int) Arrow.getDefaultWidth();
		optionsService.setOption(OptionsArrowTool.class,"arrowWidth",arrowWidth);
		optionsService.setOption(OptionsColors.class,"fgColor", Toolbar.getForegroundColor());
		optionsService.setOption(OptionsColors.class,"bgColor", Toolbar.getBackgroundColor());
		optionsService.setOption(OptionsColors.class,"selColor",Roi.getColor());
		optionsService.setOption(OptionsConversions.class,"scaleWhenConverting", ImageConverter.getDoScaling());
		optionsService.setOption(OptionsConversions.class,"weightedRgbConversions",Prefs.weightedColor);
		optionsService.setOption(OptionsDicom.class,"openAs32bitFloat",Prefs.openDicomsAsFloat);
		optionsService.setOption(OptionsDicom.class,"rotateXZ",Prefs.flipXZ);
		optionsService.setOption(OptionsDicom.class,"rotateYZ",Prefs.rotateYZ);
		optionsService.setOption(OptionsFont.class,"font", TextRoi.getFont());
		optionsService.setOption(OptionsFont.class,"fontSize", TextRoi.getSize());
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
		optionsService.setOption(OptionsFont.class,"fontStyle", fontStyleString);
		optionsService.setOption(OptionsInputOutput.class,"copyColumnHeaders",Prefs.copyColumnHeaders);
		optionsService.setOption(OptionsInputOutput.class,"copyRowNumbers",!Prefs.noRowNumbers);
		optionsService.setOption(OptionsInputOutput.class,"jpegQuality", FileSaver.getJpegQuality());
		optionsService.setOption(OptionsInputOutput.class,"saveColumnHeaders", !Prefs.dontSaveHeaders);
		optionsService.setOption(OptionsInputOutput.class,"saveOrderIntel",Prefs.intelByteOrder);
		optionsService.setOption(OptionsInputOutput.class,"saveRowNumbers", !Prefs.dontSaveRowNumbers);
		optionsService.setOption(OptionsInputOutput.class,"transparentIndex", Prefs.getTransparentIndex());
		optionsService.setOption(OptionsInputOutput.class,"useJFileChooser",Prefs.useJFileChooser);
		optionsService.setOption(OptionsLineWidth.class,"lineWidth", Line.getWidth());
		optionsService.setOption(OptionsMemoryAndThreads.class,"multipleBuffers",Prefs.keepUndoBuffers);
		optionsService.setOption(OptionsMemoryAndThreads.class,"runGcOnClick",!Prefs.noClickToGC);
		optionsService.setOption(OptionsMemoryAndThreads.class,"stackThreads", Prefs.getThreads());
		String dbzString = new Float(FloatBlitter.divideByZeroValue).toString();
		optionsService.setOption(OptionsMisc.class,"divByZeroVal", dbzString);
		optionsService.setOption(OptionsMisc.class,"debugMode",IJ.debugMode);
		optionsService.setOption(OptionsMisc.class,"hideProcessStackDialog",IJ.hideProcessStackDialog);
		optionsService.setOption(OptionsMisc.class,"moveIsolatedPlugins",Prefs.moveToMisc);
		optionsService.setOption(OptionsMisc.class,"usePtrCursor",Prefs.usePointerCursor);
		optionsService.setOption(OptionsMisc.class,"requireCommandKey",Prefs.requireControlKey);
		optionsService.setOption(OptionsMisc.class,"runSingleInstanceListener",Prefs.runSocketListener);
		optionsService.setOption(OptionsPointTool.class,"addToRoiMgr",Prefs.pointAddToManager);
		optionsService.setOption(OptionsPointTool.class,"autoMeasure",Prefs.pointAutoMeasure);
		optionsService.setOption(OptionsPointTool.class,"autoNextSlice",Prefs.pointAutoNextSlice);
		optionsService.setOption(OptionsPointTool.class,"labelPoints",!Prefs.noPointLabels);
		optionsService.setOption(OptionsPointTool.class,"markWidth",Analyzer.markWidth);
		optionsService.setOption(OptionsPointTool.class,"selectionColor",Roi.getColor());
		optionsService.setOption(OptionsProfilePlot.class,"autoClose",ij.gui.PlotWindow.autoClose);
		optionsService.setOption(OptionsProfilePlot.class,"noSaveXValues",!ij.gui.PlotWindow.saveXValues);
		optionsService.setOption(OptionsProfilePlot.class,"drawGridLines",!ij.gui.PlotWindow.noGridLines);
		optionsService.setOption(OptionsProfilePlot.class,"height",ij.gui.PlotWindow.plotHeight);
		optionsService.setOption(OptionsProfilePlot.class,"interpLineProf",ij.gui.PlotWindow.interpolate);
		optionsService.setOption(OptionsProfilePlot.class,"listValues",ij.gui.PlotWindow.listValues);
		double yMin = ProfilePlot.getFixedMin();
		double yMax = ProfilePlot.getFixedMax();
		optionsService.setOption(OptionsProfilePlot.class,"maxY",yMax);
		optionsService.setOption(OptionsProfilePlot.class,"minY",yMin);
		optionsService.setOption(OptionsProfilePlot.class,"vertProfile",Prefs.verticalProfile);
		optionsService.setOption(OptionsProfilePlot.class,"width",ij.gui.PlotWindow.plotWidth);
		int crnDiam = Toolbar.getRoundRectArcSize();
		optionsService.setOption(OptionsRoundedRectangleTool.class,"cornerDiameter", crnDiam);
	}
}
