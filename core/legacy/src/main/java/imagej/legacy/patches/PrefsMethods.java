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

import imagej.ImageJ;
import imagej.ext.options.OptionsService;
import imagej.legacy.LegacyInjector;

import java.util.HashMap;
import java.util.Map;

/**
 * These class methods are used by the {@link LegacyInjector} to replace
 * code in IJ1 that sets values in it's Prefs class to update IJ2's
 * internal options as needed.
 * 
 * @author Barry DeZonia
 */
public class PrefsMethods {

	// -- static variables and initialization --
	
	private static class OptionKey {
		public String pluginName;
		public String fieldName;
		
		public OptionKey(String pName, String fName) {
			pluginName = pName;
			fieldName = fName;
		}
	}
	
	private static Map<String,OptionKey> keyMap;
	
	static {
		keyMap = new HashMap<String,OptionKey>();
		mapPrefKeys();
	}
	
	private static final OptionsService OPTIONS_SERVICE = ImageJ.get(OptionsService.class);
	
	// -- public interface --
	
	public static void set(String ij1Key, String text) {
		OptionKey ij2Key = getIJ2Key(ij1Key);
		// is there a parallel settings value in IJ2?
		if (ij2Key != null) {
			// do some special case code if necessary
			if (ij1Key.equals("arrow.style")) {
				int styleIndex = Integer.parseInt(text);
				String styleName;
				switch (styleIndex) {
					case 1: styleName = "Notched"; break;
					case 2: styleName = "Open"; break;
					case 3: styleName = "Headless"; break;
					default: styleName = "Filled"; break;
				}
				OPTIONS_SERVICE.setOption(ij2Key.pluginName, ij2Key.fieldName, styleName);
			}
			else // this key requires no special case code
				OPTIONS_SERVICE.setOption(ij2Key.pluginName, ij2Key.fieldName, text);
		}
	}
	
	// -- private interface --
	
	private static OptionKey getIJ2Key(String ij1Key) {
		return keyMap.get(ij1Key);
	}
	
	private static void mapPrefKeys() {
		keyMap.put(ij.Prefs.JPEG, new OptionKey("imagej.core.plugins.options.OptionsInputOutput","jpegQuality"));
		keyMap.put(ij.Prefs.DIV_BY_ZERO_VALUE, new OptionKey("imagej.core.plugins.options.OptionsMisc","divByZeroVal"));
		keyMap.put(ij.Prefs.THREADS, new OptionKey("imagej.core.plugins.options.OptionsMemoryAndThreads","stackThreads"));
		keyMap.put(ij.Prefs.MENU_SIZE, new OptionKey("imagej.core.plugins.options.OptionsAppearence","menuFontSize"));
		keyMap.put("arrow.double", new OptionKey("imagej.core.plugins.options.OptionsArrowTool","arrowDoubleHeaded"));
		keyMap.put("arrow.outline", new OptionKey("imagej.core.plugins.options.OptionsArrowTool","arrowOutline"));
		keyMap.put("arrow.size", new OptionKey("imagej.core.plugins.options.OptionsArrowTool","arrowSize"));
		keyMap.put("arrow.style", new OptionKey("imagej.core.plugins.options.OptionsArrowTool","arrowStyle"));
		keyMap.put("arrow.width", new OptionKey("imagej.core.plugins.options.OptionsArrowTool","arrowWidth"));
		keyMap.put("javac.target", new OptionKey("imagej.core.plugins.options.OptionsCompiler","targetJavaVersion"));
		keyMap.put("options.ext", new OptionKey("imagej.core.plugins.options.OptionsInputOutput","tableFileExtension"));
		keyMap.put("pp.max", new OptionKey("imagej.core.plugins.options.OptionsProfilePlot","maxY"));
		keyMap.put("pp.min", new OptionKey("imagej.core.plugins.options.OptionsProfilePlot","minY"));
		keyMap.put("pp.width", new OptionKey("imagej.core.plugins.options.OptionsProfilePlot","width"));
		keyMap.put("pp.height", new OptionKey("imagej.core.plugins.options.OptionsProfilePlot","height"));
		keyMap.put("proxy.server", new OptionKey("imagej.core.plugins.options.OptionsProxy","proxyServer"));
		keyMap.put("proxy.port", new OptionKey("imagej.core.plugins.options.OptionsProxy","port"));
		keyMap.put("toolbar.arc.size", new OptionKey("imagej.core.plugins.options.OptionsRoundedRectangleTool","cornerDiameter"));
	}

	@SuppressWarnings("unused")
	private static void prefsGraveyard() {
		
		// These are IJ1 prefs that do not have an IJ2 OptionsPlugin associated
		// with them. Keep around for now in case we do something with them.
		
		// not a options plugin field
		//keyMap.put("ij.x",);
		// not a options plugin field
		//keyMap.put("ij.y",);

		// not a options plugin field
		//keyMap.put("proxy.user", null);
		// not a options plugin field
		//keyMap.put(TextWindow.WIDTH_KEY,);
		// not a options plugin field
		//keyMap.put(TextWindow.HEIGHT_KEY,);
		// not a options plugin field
		//keyMap.put("tw.font.anti",);
		// not a options plugin field
		//keyMap.put("tw.font.size",);

		// next one is a animator dialog preference - no direct IJ2 mapping
		//keyMap.put(ij.Prefs.FPS,);
		// next one is a noise option dialog preference - no direct IJ2 mapping
		//keyMap.put(ij.Prefs.NOISE_SD,);

		//keyMap.put("ap.options",);
		//keyMap.put("mark.width",);
		//keyMap.put("measurements",);
		//keyMap.put("new.type",);
		//keyMap.put("new.fill",);
		//keyMap.put("new.width",);
		//keyMap.put("new.height",);
		//keyMap.put("new.slices",);
		//keyMap.put("precision",);
		//keyMap.put("raw.type",);
		//keyMap.put("raw.width",);
		//keyMap.put("raw.height",);
		//keyMap.put("raw.offset",);
		//keyMap.put("raw.n",);
		//keyMap.put("raw.gap",);
		//keyMap.put("raw.options",);

		// Next one relies on value of OpenDialog directory. We never invoke.
		//keyMap.put(ij.Prefs.DIR_IMAGE,);
		// Next one used to draw IJ1 Rois. Since we don't draw we can ignore
		//keyMap.put(ij.Prefs.ROICOLOR,);
		// Next one only used by ImageCanvas which we don't draw to
		//keyMap.put(ij.Prefs.SHOW_ALL_COLOR,);
		// Next one only used by Toolbar which we don't instantiate
		//keyMap.put(ij.Prefs.FCOLOR,);
		// Next one only used by Toolbar which we don't instantiate
		//keyMap.put(ij.Prefs.BCOLOR,);

		// TODO
		// This next one is tricky. IJ1 encodes some settings together as an int
		// in ij.Prefs -> saveOptions() and loadOptions(). If we change a setting
		// in IJ2, the IJ1 variables should get set. But the ability of an IJ1
		// prefs user to get the right values is predicated on the last time IJ1's
		// Prefs.saveOptions() was run. Maybe the OptionsSynchronizer has to fire
		// something in IJ1 land so that saveOptions() is called often enough.
		// keyMap.put(ij.Prefs.OPTIONS,);

		// TODO - what about this one? encoded from many fields in IJ1
		//keyMap.put("pp.options",);

		//keyMap.put("batch.format",);
		//keyMap.put("batch.input",);
		//keyMap.put("batch.output",);
		//keyMap.put("bs.background",);

		//keyMap.put("command-finder.close", SettingsKeys.SETTINGS_COMMAND_FINDER_CLOSE);
		//keyMap.put("cthresholder.dark", SettingsKeys.SETTINGS_COLOR_THRESHOLDER_DARK);
		//keyMap.put("editor.case-sensitive", SettingsKeys.SETTINGS_EDITOR_CASE_SENSITIVE);
		//keyMap.put("editor.font.mono", SettingsKeys.SETTINGS_EDITOR_FONT_MONO);
		//keyMap.put("editor.font.size", SettingsKeys.SETTINGS_EDITOR_FONT_SIZE);
		//keyMap.put("gel.hscale", SettingsKeys.SETTINGS_GEL_ANALYZER_HSCALE);
		//keyMap.put("gel.options", SettingsKeys.SETTINGS_GEL_ANALYZER_OPTIONS); 
		//keyMap.put("gel.vscale", SettingsKeys.SETTINGS_GEL_ANALYZER_VSCALE);
		//keyMap.put("label.format", SettingsKeys.SETTINGS_STACKLABELER_LABEL_FORMAT);
		//keyMap.put("math.macro", SettingsKeys.SETTINGS_IMAGE_MATH_MACRO_VALUE);
		//keyMap.put("recorder.mode", SettingsKeys.SETTINGS_RECORDER_MODE);
		//keyMap.put("resizer.zero", SettingsKeys.SETTINGS_CANVAS_RESIZER_ZERO);
		//keyMap.put("threshold.dark", SettingsKeys.SETTINGS_THRESHOLD_ADJUSTER_DARK_BACKGROUND);
		//keyMap.put("threshold.mode", SettingsKeys.SETTINGS_THRESHOLD_ADJUSTER_MODE_KEY);
		//keyMap.put("toolbar.brush.size", SettingsKeys.SETTINGS_TOOLBAR_LAST_BRUSH_SIZE);
		//keyMap.put("zproject.method",SettingsKeys.SETTINGS_ZPROJECTOR_METHOD);
	}
}
