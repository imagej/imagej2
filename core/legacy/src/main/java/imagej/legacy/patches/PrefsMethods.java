//
// PrefsMethods.java
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

package imagej.legacy.patches;

import ij.text.TextWindow;
import imagej.legacy.LegacyInjector;
import imagej.util.Log;
import imagej.util.SettingsKeys;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

/**
 * These class methods are used by the {@link LegacyInjector} to replace
 * code in IJ1 that sets values in it's Prefs class to instead use IJ2's
 * Prefs class.
 * 
 * @author Barry DeZonia
 */
public class PrefsMethods {

	// -- static variables and initialization --
	
	private static Map<String,String> keyMap;
	
	static {
		keyMap = new HashMap<String,String>();
		mapBasePrefNames();
		mapOptionsPrefNames();
		mapPluginPrefNames();
		mapImageJInstancePrefNames();
		mapMenuPrefNames();
		mapParticleAnalyzerPrefNames();
		mapAnalyzerPrefNames();
		mapImportDialogPrefNames();
		mapPlotWindowPrefNames();
		mapNewImagePrefNames();
		mapMiscPrefNames();
	}
	
	// -- public interface --
	
	public static String get(final String ij1Key, final String defaultValue)
	{
		String ij2Key = getIJ2Key(ij1Key);
		return imagej.util.Prefs.get(ij2Key, defaultValue);
	}

	public static boolean get(final String ij1Key, final boolean defaultValue)
	{
		String ij2Key = getIJ2Key(ij1Key);
		return imagej.util.Prefs.getBoolean(ij2Key, defaultValue);
	}

	public static double get(final String ij1Key, final double defaultValue)
	{
		String ij2Key = getIJ2Key(ij1Key);
		return imagej.util.Prefs.getDouble(ij2Key, defaultValue);
	}

	public static void set(String ij1Key, String text) {
		String ij2Key = getIJ2Key(ij1Key);
		imagej.util.Prefs.put(ij2Key, text);
	}
	
	public static void set(String ij1Key, int value) {
		String ij2Key = getIJ2Key(ij1Key);
		imagej.util.Prefs.put(ij2Key, value);
	}
	
	public static void set(String ij1Key, double value) {
		String ij2Key = getIJ2Key(ij1Key);
		imagej.util.Prefs.put(ij2Key, value);
	}
	
	public static void set(String ij1Key, boolean value) {
		String ij2Key = getIJ2Key(ij1Key);
		imagej.util.Prefs.put(ij2Key, value);
	}
	
	// TODO - I don't think I need getLocation(). It is used by Frame oriented
	//   classes and we never instantiate those in IJ2. Right?
	
	public static Point getLocation(final String key) {
		return null;
	}
	
	// TODO
	// Need getInt(), getString(), etc. also? Don't think so. Prefs live in
	//   Prefs' class variable "ijPrefs". Properties live in Prefs' class
	//   variable "props". Those methods use "props".
	
	// -- private interface --
	
	private static void warn(String key) {
		// TODO - might want to disable this
		Log.warn("Legacy preferences key map does not contain IJ1 key: "+key);
	}

	private static String getIJ2Key(String ij1Key) {
		String ij2Key = keyMap.get(ij1Key);
		if (ij2Key == null) {
			warn(ij1Key);
			ij2Key = ij.Prefs.KEY_PREFIX + "legacy.unknown." + ij1Key;
		}
		return ij2Key;
	}
	
	private static void mapBasePrefNames() {
		// Next one relies on value of OpenDialog directory. We never invoke.
		//keyMap.put(ij.Prefs.DIR_IMAGE, null);
		// Next one used to draw IJ1 Rois. Since we don't draw we can ignore
		//keyMap.put(ij.Prefs.ROICOLOR, null);
		// Next one only used by ImageCanvas which we don't draw to
		//keyMap.put(ij.Prefs.SHOW_ALL_COLOR, null);
		// Next one only used by Toolbar which we don't instantiate
		//keyMap.put(ij.Prefs.FCOLOR, null);
		// Next one only used by Toolbar which we don't instantiate
		//keyMap.put(ij.Prefs.BCOLOR, null);
		keyMap.put(ij.Prefs.JPEG, SettingsKeys.OPTIONS_IO_JPEG_QUALITY);
		keyMap.put(ij.Prefs.FPS, SettingsKeys.SETTINGS_ANIMATOR_FPS);
		keyMap.put(ij.Prefs.DIV_BY_ZERO_VALUE, SettingsKeys.OPTIONS_MISC_DBZ_VALUE);
		keyMap.put(ij.Prefs.NOISE_SD, SettingsKeys.SETTINGS_FILTERS_NOISE_SD);
		keyMap.put(ij.Prefs.THREADS, SettingsKeys.OPTIONS_MEMORYTHREADS_STACK_THREADS);
	}

	private static void mapOptionsPrefNames() {
		keyMap.put(ij.Prefs.OPTIONS, SettingsKeys.SETTINGS_PREFS_OPTIONS);
	}
	
	private static void mapPluginPrefNames() {
		// TODO - it seems like there is nothing to do
	}
	
	private static void mapImageJInstancePrefNames() {
		keyMap.put("ij.x", SettingsKeys.SETTINGS_PREFERRED_X_LOC);
		keyMap.put("ij.y", SettingsKeys.SETTINGS_PREFERRED_Y_LOC);
		keyMap.put("proxy.server", SettingsKeys.OPTIONS_PROXY_SERVER);
		keyMap.put("proxy.port", SettingsKeys.OPTIONS_PROXY_PORT);
		keyMap.put("proxy.user", SettingsKeys.SETTINGS_PROXY_USER);
		keyMap.put(TextWindow.WIDTH_KEY, SettingsKeys.SETTINGS_TEXTWINDOW_WIDTH);
		keyMap.put(TextWindow.HEIGHT_KEY, SettingsKeys.SETTINGS_TEXTWINDOW_HEIGHT);
		keyMap.put("tw.font.anti", SettingsKeys.SETTINGS_TEXTWINDOW_FONT_ANTIALIASED);
		keyMap.put("tw.font.size", SettingsKeys.SETTINGS_TEXTWINDOW_FONT_SIZE);
	}
	
	private static void mapMenuPrefNames() {
		keyMap.put(ij.Prefs.MENU_SIZE, SettingsKeys.OPTIONS_APPEARANCE_MENU_FONT_SIZE);
	}
	
	private static void mapParticleAnalyzerPrefNames() {
		keyMap.put("ap.options", SettingsKeys.SETTINGS_PARTICLE_ANALYZER_OPTIONS);
	}
	
	private static void mapAnalyzerPrefNames() {
		keyMap.put("measurements",SettingsKeys.SETTINGS_ANALYZER_MEASUREMENTS);
		keyMap.put("mark.width",SettingsKeys.SETTINGS_ANALYZER_MARK_WIDTH);
		keyMap.put("precision",SettingsKeys.SETTINGS_ANALYZER_PRECISION);
	}
	
	private static void mapImportDialogPrefNames() {
		keyMap.put("raw.type", SettingsKeys.SETTINGS_IMPORT_TYPE);
		keyMap.put("raw.width", SettingsKeys.SETTINGS_IMPORT_WIDTH);
		keyMap.put("raw.height", SettingsKeys.SETTINGS_IMPORT_HEIGHT);
		keyMap.put("raw.offset", SettingsKeys.SETTINGS_IMPORT_OFFSET);
		keyMap.put("raw.n", SettingsKeys.SETTINGS_IMPORT_N);
		keyMap.put("raw.gap", SettingsKeys.SETTINGS_IMPORT_GAP);
		keyMap.put("raw.options", SettingsKeys.SETTINGS_IMPORT_OPTIONS);
	}
	
	private static void mapPlotWindowPrefNames() {
		keyMap.put("pp.max", SettingsKeys.SETTINGS_PLOTWINDOW_MAX);
		keyMap.put("pp.min", SettingsKeys.SETTINGS_PLOTWINDOW_MIN);
		keyMap.put("pp.options", SettingsKeys.SETTINGS_PLOTWINDOW_OPTIONS);
		keyMap.put("pp.width", SettingsKeys.SETTINGS_PLOTWINDOW_PLOT_WIDTH);
		keyMap.put("pp.height", SettingsKeys.SETTINGS_PLOTWINDOW_PLOT_HEIGHT);
	}
	
	private static void mapNewImagePrefNames() {
		keyMap.put("new.type", SettingsKeys.SETTINGS_NEWIMAGE_TYPE);
		keyMap.put("new.fill", SettingsKeys.SETTINGS_NEWIMAGE_FILL);
		keyMap.put("new.width", SettingsKeys.SETTINGS_NEWIMAGE_WIDTH);
		keyMap.put("new.height", SettingsKeys.SETTINGS_NEWIMAGE_HEIGHT);
		keyMap.put("new.slices", SettingsKeys.SETTINGS_NEWIMAGE_SLICES);
	}
	
	// next method populated by searching IJ1 source for all calls to Prefs.get()
	// and seeing what we may have missed
	
	private static void mapMiscPrefNames() {
		keyMap.put("arrow.double", SettingsKeys.OPTIONS_ARROW_DOUBLEHEADED);
		keyMap.put("arrow.outline", SettingsKeys.OPTIONS_ARROW_OUTLINE);
		keyMap.put("arrow.size", SettingsKeys.OPTIONS_ARROW_SIZE);
		keyMap.put("arrow.style", SettingsKeys.OPTIONS_ARROW_STYLE);
		keyMap.put("arrow.width", SettingsKeys.OPTIONS_ARROW_WIDTH);
		keyMap.put("batch.format", SettingsKeys.SETTINGS_BATCH_FORMAT);
		keyMap.put("batch.input", SettingsKeys.SETTINGS_BATCH_INPUT);
		keyMap.put("batch.output", SettingsKeys.SETTINGS_BATCH_OUTPUT);
		keyMap.put("bs.background", SettingsKeys.SETTINGS_BACKGROUND_SUBTRACTER_BACKGROUND);
		keyMap.put("command-finder.close", SettingsKeys.SETTINGS_COMMAND_FINDER_CLOSE);
		keyMap.put("cthresholder.dark", SettingsKeys.SETTINGS_COLOR_THRESHOLDER_DARK);
		keyMap.put("editor.case-sensitive", SettingsKeys.SETTINGS_EDITOR_CASE_SENSITIVE);
		keyMap.put("editor.font.mono", SettingsKeys.SETTINGS_EDITOR_FONT_MONO);
		keyMap.put("editor.font.size", SettingsKeys.SETTINGS_EDITOR_FONT_SIZE);
		keyMap.put("gel.hscale", SettingsKeys.SETTINGS_GEL_ANALYZER_HSCALE);
		keyMap.put("gel.options", SettingsKeys.SETTINGS_GEL_ANALYZER_OPTIONS); 
		keyMap.put("gel.vscale", SettingsKeys.SETTINGS_GEL_ANALYZER_VSCALE);
		keyMap.put("javac.target", SettingsKeys.OPTIONS_COMPILER_VERSION);
		keyMap.put("label.format", SettingsKeys.SETTINGS_STACKLABELER_LABEL_FORMAT);
		keyMap.put("math.macro", SettingsKeys.SETTINGS_IMAGE_MATH_MACRO_VALUE);
		keyMap.put("options.ext", SettingsKeys.OPTIONS_IO_FILE_EXT);
		keyMap.put("recorder.mode", SettingsKeys.SETTINGS_RECORDER_MODE);
		keyMap.put("resizer.zero", SettingsKeys.SETTINGS_CANVAS_RESIZER_ZERO);
		keyMap.put("threshold.dark", SettingsKeys.SETTINGS_THRESHOLD_ADJUSTER_DARK_BACKGROUND);
		keyMap.put("threshold.mode", SettingsKeys.SETTINGS_THRESHOLD_ADJUSTER_MODE_KEY);
		keyMap.put("toolbar.arc.size", SettingsKeys.OPTIONS_ROUND_RECT_CORNER_DIAMETER);
		keyMap.put("toolbar.brush.size", SettingsKeys.SETTINGS_TOOLBAR_LAST_BRUSH_SIZE);
		keyMap.put("zproject.method",SettingsKeys.SETTINGS_ZPROJECTOR_METHOD);
	}
}
