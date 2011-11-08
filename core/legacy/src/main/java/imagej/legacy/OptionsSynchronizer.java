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
 * The options synchronizer bidirectionally synchronizes IJ2 options with IJ1
 * settings and preferences.
 * 
 * @author Barry DeZonia
 */
public class OptionsSynchronizer {

	private final OptionsService optionsService;

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

	private <O extends OptionsPlugin> Color getColor(final String colorName,
		final Color defaultColor)
	{
		return Colors.getColor(colorName, defaultColor);
	}

	private void appearanceOptions() {
		final OptionsAppearance optionsAppearance =
			optionsService.getOptions(OptionsAppearance.class);

		Prefs.antialiasedText = false;
		Prefs.antialiasedTools = optionsAppearance.isAntialiasedToolIcons();
		Prefs.blackCanvas = optionsAppearance.isBlackCanvas();
		Prefs.open100Percent = optionsAppearance.isFullZoomImages();
		Prefs.interpolateScaledImages = optionsAppearance.isInterpZoomedImages();
		Prefs.noBorder = optionsAppearance.isNoImageBorder();
		Prefs.useInvertingLut = optionsAppearance.isUseInvertingLUT();

		// TODO
		// this one needs to have code applied to IJ2. Nothing to set for IJ1.
		// Prefs.get(SettingsKeys.OPTIONS_APPEARANCE_MENU_FONT_SIZE);
	}

	private void arrowOptions() {
		final OptionsArrowTool optionsArrowTool =
			optionsService.getOptions(OptionsArrowTool.class);

		// TODO - for this next setting there is nothing to synchronize. Changing
		// this setting runs some code in IJ1's UI. Might need some code on the IJ2
		// side that mirrors the behavior.
		// String color = optionsArrowTool.getArrowColor();
		final boolean doubleHeaded = optionsArrowTool.isArrowDoubleHeaded();
		final boolean outline = optionsArrowTool.isArrowOutline();
		final int size = optionsArrowTool.getArrowSize();
		String style = optionsArrowTool.getArrowStyle();
		final int width = optionsArrowTool.getArrowWidth();

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
		final OptionsColors optionsColors =
			optionsService.getOptions(OptionsColors.class);

		Toolbar
			.setForegroundColor(getColor(optionsColors.getFgColor(), Color.white));
		Toolbar
			.setBackgroundColor(getColor(optionsColors.getBgColor(), Color.black));
		Roi.setColor(getColor(optionsColors.getSelColor(), Color.yellow));
	}

	private void compilerOptions() {
		final OptionsCompiler optionsCompiler =
			optionsService.getOptions(OptionsCompiler.class);

		String version = optionsCompiler.getTargetJavaVersion();
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
		final OptionsConversions optionsConversions =
			optionsService.getOptions(OptionsConversions.class);

		final double[] weights = ColorProcessor.getWeightingFactors();
		final boolean weighted =
			!(weights[0] == 1d / 3d && weights[1] == 1d / 3d && weights[2] == 1d / 3d);
		ImageConverter.setDoScaling(optionsConversions.isScaleWhenConverting());
		Prefs.weightedColor = optionsConversions.isWeightedRgbConversions();
		if (!Prefs.weightedColor) ColorProcessor.setWeightingFactors(1d / 3d,
			1d / 3d, 1d / 3d);
		else if (Prefs.weightedColor && !weighted) ColorProcessor
			.setWeightingFactors(0.299, 0.587, 0.114);
	}

	private void dicomOptions() {
		final OptionsDicom optionsDicom =
			optionsService.getOptions(OptionsDicom.class);

		Prefs.openDicomsAsFloat = optionsDicom.isOpenAs32bitFloat();
		Prefs.flipXZ = optionsDicom.isRotateXZ();
		Prefs.rotateYZ = optionsDicom.isRotateYZ();
	}

	private void fontOptions() {
		final OptionsFont optionsFont =
			optionsService.getOptions(OptionsFont.class);

		final String fontName = optionsFont.getFont();
		final int fontSize = optionsFont.getFontSize();
		String styleName = optionsFont.getFontStyle();
		final boolean smooth = optionsFont.isFontSmooth();

		if (styleName == null) styleName = "";
		int fontStyle = Font.PLAIN;
		if (styleName.equals("Bold")) fontStyle = Font.BOLD;
		else if (styleName.equals("Italic")) fontStyle = Font.ITALIC;
		else if (styleName.equals("Bold+Italic")) fontStyle =
			Font.BOLD + Font.ITALIC;
		TextRoi.setFont(fontName, fontSize, fontStyle, smooth);
	}

	private void ioOptions() {
		final OptionsInputOutput optionsInputOutput =
			optionsService.getOptions(OptionsInputOutput.class);

		Prefs.copyColumnHeaders = optionsInputOutput.isCopyColumnHeaders();
		Prefs.noRowNumbers = !optionsInputOutput.isCopyRowNumbers();
		String extension = optionsInputOutput.getTableFileExtension();
		if (extension == null) extension = ".txt";
		Prefs.set("options.ext", extension);
		FileSaver.setJpegQuality(optionsInputOutput.getJpegQuality());
		Prefs.dontSaveHeaders = !optionsInputOutput.isSaveColumnHeaders();
		Prefs.intelByteOrder = optionsInputOutput.isSaveOrderIntel();
		Prefs.dontSaveRowNumbers = !optionsInputOutput.isSaveRowNumbers();
		Prefs.setTransparentIndex(optionsInputOutput.getTransparentIndex());
		Prefs.useJFileChooser = optionsInputOutput.isUseJFileChooser();
	}

	private void lineWidthOptions() {
		final OptionsLineWidth optionsLineWidth =
			optionsService.getOptions(OptionsLineWidth.class);

		Line.setWidth(optionsLineWidth.getLineWidth());
	}

	private void memoryAndThreadsOptions() {
		final OptionsMemoryAndThreads optionsMemoryAndThreads =
			optionsService.getOptions(OptionsMemoryAndThreads.class);

		Prefs.keepUndoBuffers = optionsMemoryAndThreads.isMultipleBuffers();
		Prefs.noClickToGC = !optionsMemoryAndThreads.isRunGcOnClick();
		Prefs.setThreads(optionsMemoryAndThreads.getStackThreads());
		// TODO
		// nothing to set in this next case. Need IJ2 to fire some code as
		// appropriate
		// Prefs.get(SettingsKeys.OPTIONS_MEMORYTHREADS_MAX_MEMORY);
	}

	private void miscOptions() {
		final OptionsMisc optionsMisc =
			optionsService.getOptions(OptionsMisc.class);

		String divValue = optionsMisc.getDivByZeroVal();
		IJ.debugMode = optionsMisc.isDebugMode();
		IJ.hideProcessStackDialog = optionsMisc.isHideProcessStackDialog();
		Prefs.moveToMisc = optionsMisc.isMoveIsolatedPlugins();
		Prefs.usePointerCursor = optionsMisc.isUsePtrCursor();
		Prefs.requireControlKey = optionsMisc.isRequireCommandKey();
		Prefs.runSocketListener = optionsMisc.isRunSingleInstanceListener();

		if (divValue == null) divValue = "infinity";
		if (divValue.equalsIgnoreCase("infinity") ||
			divValue.equalsIgnoreCase("infinite")) FloatBlitter.divideByZeroValue =
			Float.POSITIVE_INFINITY;
		else if (divValue.equalsIgnoreCase("NaN")) FloatBlitter.divideByZeroValue =
			Float.NaN;
		else if (divValue.equalsIgnoreCase("max")) FloatBlitter.divideByZeroValue =
			Float.MAX_VALUE;
		else {
			Float f;
			try {
				f = new Float(divValue);
			}
			catch (final NumberFormatException e) {
				f = null;
			}
			if (f != null) FloatBlitter.divideByZeroValue = f.floatValue();
		}
	}

	private void pointOptions() {
		final OptionsPointTool optionsPointTool =
			optionsService.getOptions(OptionsPointTool.class);

		Prefs.pointAddToManager = optionsPointTool.isAddToRoiMgr();
		Prefs.pointAutoMeasure = optionsPointTool.isAutoMeasure();
		Prefs.pointAutoNextSlice = optionsPointTool.isAutoNextSlice();
		Prefs.noPointLabels = !optionsPointTool.isLabelPoints();
		Analyzer.markWidth = optionsPointTool.getMarkWidth();
		Roi.setColor(getColor(optionsPointTool.getSelectionColor(), Color.yellow));
	}

	private void profilePlotOptions() {
		final OptionsProfilePlot optionsProfilePlot =
			optionsService.getOptions(OptionsProfilePlot.class);

		ij.gui.PlotWindow.autoClose = optionsProfilePlot.isAutoClose();
		ij.gui.PlotWindow.saveXValues = !optionsProfilePlot.isNoSaveXValues();
		ij.gui.PlotWindow.noGridLines = !optionsProfilePlot.isDrawGridLines();
		boolean fixedScale = optionsProfilePlot.isYFixedScale();
		ij.gui.PlotWindow.plotHeight = optionsProfilePlot.getHeight();
		ij.gui.PlotWindow.interpolate = optionsProfilePlot.isInterpLineProf();
		ij.gui.PlotWindow.listValues = optionsProfilePlot.isListValues();
		double yMax = optionsProfilePlot.getMaxY();
		double yMin = optionsProfilePlot.getMinY();
		Prefs.verticalProfile = optionsProfilePlot.isVertProfile();
		ij.gui.PlotWindow.plotWidth = optionsProfilePlot.getWidth();

		if (!fixedScale && (yMin != 0.0 || yMax != 0.0)) fixedScale = true;
		if (!fixedScale) {
			yMin = 0.0;
			yMax = 0.0;
		}
		else if (yMin > yMax) {
			final double tmp = yMin;
			yMin = yMax;
			yMax = tmp;
		}
		ProfilePlot.setMinAndMax(yMin, yMax);
	}

	private void proxyOptions() {
		final OptionsProxy optionsProxy =
			optionsService.getOptions(OptionsProxy.class);

		// TODO
		// This next setting affects IJ1 dialog. Nothing programmatic can be set.
		// Need pure IJ2 plugins when this setting is utilized.
		// Prefs.get(SettingsKeys.OPTIONS_PROXY_AUTHENTICATE);
		final String server = optionsProxy.getProxyServer();
		if (server != null) {
			Prefs.set("proxy.server", server);
			Prefs.set("proxy.port", optionsProxy.getPort());
		}
		ij.Prefs.useSystemProxies = optionsProxy.isUseSystemProxy();
	}

	private void roundRectOptions() {
		final OptionsRoundedRectangleTool optionsRoundedRectangleTool =
			optionsService.getOptions(OptionsRoundedRectangleTool.class);

		final int crnDiam = optionsRoundedRectangleTool.getCornerDiameter();
		Toolbar.setRoundRectArcSize(crnDiam);
		// TODO
		// IJ1 RectToolOptions does not manipulate Prefs much. It fires
		// code to change behavior when dialog entries changed. No programmatic
		// way to make our settings affect IJ1. Need pure IJ2 support elsewhere.
		// Prefs.get(SettingsKeys.OPTIONS_ROUND_RECT_FILL_COLOR, none); ?how to
		// handle "none"?
		// Prefs.get(SettingsKeys.OPTIONS_ROUND_RECT_STROKE_COLOR, Color.black);
		// Prefs.get(SettingsKeys.OPTIONS_ROUND_RECT_STROKE_WIDTH, 2);
	}

	private void wandToolOptions() {
		// TODO
		// IJ1 WandToolOptions does not mess with preferences in any way. There
		// is no way in IJ1 to set values without running dialog. Programmatic
		// approach is out. Can't synchronize unless Wayne adds code. May not be
		// needed because Wand is not ever active in IJ2. Right?
		// Prefs.get(SettingsKeys.OPTIONS_WAND_MODE, "Legacy");
		// Prefs.get(SettingsKeys.OPTIONS_WAND_TOLERANCE, 0.0);
	}

	private void setOptionsFromPublicStatics() {
		final OptionsAppearance optionsAppearance =
			optionsService.getOptions(OptionsAppearance.class);
		optionsAppearance.setAntialiasedToolIcons(Prefs.antialiasedTools);
		optionsAppearance.setBlackCanvas(Prefs.blackCanvas);
		optionsAppearance.setFullZoomImages(Prefs.open100Percent);
		optionsAppearance.setInterpZoomedImages(Prefs.interpolateScaledImages);
		optionsAppearance.setNoImageBorder(Prefs.noBorder);
		optionsAppearance.setUseInvertingLUT(Prefs.useInvertingLut);

		final OptionsArrowTool optionsArrowTool =
			optionsService.getOptions(OptionsArrowTool.class);
		final boolean arrowTwoHeads = Arrow.getDefaultDoubleHeaded();
		optionsArrowTool.setArrowDoubleHeaded(arrowTwoHeads);
		final boolean arrowOutline = Arrow.getDefaultOutline();
		optionsArrowTool.setArrowOutline(arrowOutline);
		final int arrowSize = (int) Arrow.getDefaultHeadSize();
		optionsArrowTool.setArrowSize(arrowSize);
		final int arrowStyle = Arrow.getDefaultStyle();
		String arrowStyleName;
		if (arrowStyle == 1) arrowStyleName = "Notched";
		else if (arrowStyle == 2) arrowStyleName = "Open";
		else if (arrowStyle == 3) arrowStyleName = "Headless";
		else arrowStyleName = "Filled";
		optionsArrowTool.setArrowStyle(arrowStyleName);
		final int arrowWidth = (int) Arrow.getDefaultWidth();
		optionsArrowTool.setArrowWidth(arrowWidth);

		final OptionsColors optionsColors =
			optionsService.getOptions(OptionsColors.class);
		optionsColors.setFgColor(Toolbar.getForegroundColor().toString());
		optionsColors.setBgColor(Toolbar.getBackgroundColor().toString());
		optionsColors.setSelColor(Roi.getColor().toString());

		final OptionsConversions optionsConversions =
			optionsService.getOptions(OptionsConversions.class);
		optionsConversions.setScaleWhenConverting(ImageConverter.getDoScaling());
		optionsConversions.setWeightedRgbConversions(Prefs.weightedColor);

		final OptionsDicom optionsDicom =
			optionsService.getOptions(OptionsDicom.class);
		optionsDicom.setOpenAs32bitFloat(Prefs.openDicomsAsFloat);
		optionsDicom.setRotateXZ(Prefs.flipXZ);
		optionsDicom.setRotateYZ(Prefs.rotateYZ);

		final OptionsFont optionsFont =
			optionsService.getOptions(OptionsFont.class);
		optionsFont.setFont(TextRoi.getFont());
		optionsFont.setFontSize(TextRoi.getSize());
		String fontStyleString;
		final int tmp = TextRoi.getStyle();
		if (tmp == Font.BOLD + Font.ITALIC) fontStyleString = "Bold+Italic";
		else if (tmp == Font.BOLD) fontStyleString = "Bold";
		else if (tmp == Font.ITALIC) fontStyleString = "Italic";
		else fontStyleString = "";
		optionsFont.setFontStyle(fontStyleString);

		final OptionsInputOutput optionsInputOutput =
			optionsService.getOptions(OptionsInputOutput.class);
		optionsInputOutput.setCopyColumnHeaders(Prefs.copyColumnHeaders);
		optionsInputOutput.setCopyRowNumbers(!Prefs.noRowNumbers);
		optionsInputOutput.setJpegQuality(FileSaver.getJpegQuality());
		optionsInputOutput.setSaveColumnHeaders(!Prefs.dontSaveHeaders);
		optionsInputOutput.setSaveOrderIntel(Prefs.intelByteOrder);
		optionsInputOutput.setSaveRowNumbers(!Prefs.dontSaveRowNumbers);
		optionsInputOutput.setTransparentIndex(Prefs.getTransparentIndex());
		optionsInputOutput.setUseJFileChooser(Prefs.useJFileChooser);

		final OptionsLineWidth optionsLineWidth =
			optionsService.getOptions(OptionsLineWidth.class);
		optionsLineWidth.setLineWidth(Line.getWidth());

		final OptionsMemoryAndThreads optionsMemoryAndThreads =
			optionsService.getOptions(OptionsMemoryAndThreads.class);
		optionsMemoryAndThreads.setMultipleBuffers(Prefs.keepUndoBuffers);
		optionsMemoryAndThreads.setRunGcOnClick(!Prefs.noClickToGC);
		optionsMemoryAndThreads.setStackThreads(Prefs.getThreads());

		final OptionsMisc optionsMisc =
			optionsService.getOptions(OptionsMisc.class);
		final String dbzString =
			new Float(FloatBlitter.divideByZeroValue).toString();
		optionsMisc.setDivByZeroVal(dbzString);
		optionsMisc.setDebugMode(IJ.debugMode);
		optionsMisc.setHideProcessStackDialog(IJ.hideProcessStackDialog);
		optionsMisc.setMoveIsolatedPlugins(Prefs.moveToMisc);
		optionsMisc.setUsePtrCursor(Prefs.usePointerCursor);
		optionsMisc.setRequireCommandKey(Prefs.requireControlKey);
		optionsMisc.setRunSingleInstanceListener(Prefs.runSocketListener);

		final OptionsPointTool optionsPointTool =
			optionsService.getOptions(OptionsPointTool.class);
		optionsPointTool.setAddToRoiMgr(Prefs.pointAddToManager);
		optionsPointTool.setAutoMeasure(Prefs.pointAutoMeasure);
		optionsPointTool.setAutoNextSlice(Prefs.pointAutoNextSlice);
		optionsPointTool.setLabelPoints(!Prefs.noPointLabels);
		optionsPointTool.setMarkWidth(Analyzer.markWidth);
		optionsPointTool.setSelectionColor(Roi.getColor().toString());

		final OptionsProfilePlot optionsProfilePlot =
			optionsService.getOptions(OptionsProfilePlot.class);
		optionsProfilePlot.setAutoClose(ij.gui.PlotWindow.autoClose);
		optionsProfilePlot.setNoSaveXValues(!ij.gui.PlotWindow.saveXValues);
		optionsProfilePlot.setDrawGridLines(!ij.gui.PlotWindow.noGridLines);
		optionsProfilePlot.setHeight(ij.gui.PlotWindow.plotHeight);
		optionsProfilePlot.setInterpLineProf(ij.gui.PlotWindow.interpolate);
		optionsProfilePlot.setListValues(ij.gui.PlotWindow.listValues);
		final double yMin = ProfilePlot.getFixedMin();
		final double yMax = ProfilePlot.getFixedMax();
		optionsProfilePlot.setMaxY(yMax);
		optionsProfilePlot.setMinY(yMin);
		optionsProfilePlot.setVertProfile(Prefs.verticalProfile);
		optionsProfilePlot.setWidth(ij.gui.PlotWindow.plotWidth);

		final OptionsProxy optionsProxy =
			optionsService.getOptions(OptionsProxy.class);
		optionsProxy.setUseSystemProxy(ij.Prefs.useSystemProxies);

		final OptionsRoundedRectangleTool optionsRoundedRectangleTool =
			optionsService.getOptions(OptionsRoundedRectangleTool.class);
		final int crnDiam = Toolbar.getRoundRectArcSize();
		optionsRoundedRectangleTool.setCornerDiameter(crnDiam);
	}
}
