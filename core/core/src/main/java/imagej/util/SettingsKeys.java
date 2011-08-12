//
// SettingsKeys.java
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

package imagej.util;

/**
 * This class holds key value constants use for saving and retrieving Preferences.
 * 
 * @author Barry DeZonia
 *
 */
public class SettingsKeys
{
	private SettingsKeys()
	{
		// uninstantiable constructor
	}
	
	// from the Options Appearence dialog
	public static final String OPTIONS_APPEARANCE_INTERPOLATE_ZOOMED_IMAGES = "settings.appearance.InterpolateZoomedImages";
	public static final String OPTIONS_APPEARANCE_FULL_ZOOMED_IMAGES = "settings.appearance.FullZoomedImages";
	public static final String OPTIONS_APPEARANCE_BLACK_CANVAS = "settings.appearance.BlackCanvas";
	public static final String OPTIONS_APPEARANCE_NO_IMAGE_BORDER = "settings.appearance.NoImageBorder";
	public static final String OPTIONS_APPEARANCE_USE_INVERTING_LUT = "settings.appearance.UseInvertingLut";
	public static final String OPTIONS_APPEARANCE_ANTIALIASED_TOOL_ICONS = "settings.appearance.AntialiasedToolIcons";
	public static final String OPTIONS_APPEARANCE_MENU_FONT_SIZE = "settings.appearance.MenuFontSize";

	// from the Options Arrow dialog
	public static final String OPTIONS_ARROW_WIDTH = "settings.arrow.Width";
	public static final String OPTIONS_ARROW_SIZE = "settings.arrow.Size";
	public static final String OPTIONS_ARROW_COLOR = "settings.arrow.Color";
	public static final String OPTIONS_ARROW_STYLE = "settings.arrow.Style";
	public static final String OPTIONS_ARROW_OUTLINE = "settings.arrow.Outline";
	public static final String OPTIONS_ARROW_DOUBLEHEADED = "settings.arrow.DoubleHeaded";

	// from the Options Colors dialog
	public static final String OPTIONS_COLORS_FOREGROUND = "settings.color.Foreground";
	public static final String OPTIONS_COLORS_BACKGROUND = "settings.color.Background";
	public static final String OPTIONS_COLORS_SELECTION = "settings.color.Selection";

	// from the Options Compiler dialog
	public static final String OPTIONS_COMPILER_VERSION = "settings.compiler.Version";
	public static final String OPTIONS_COMPILER_DEBUG_INFO = "settings.compiler.DebugInfo";

	// from the Options Conversions dialog
	public static final String OPTIONS_CONVERSIONS_SCALE = "settings.conversions.Scale";
	public static final String OPTIONS_CONVERSIONS_WEIGHTED = "settings.conversions.Weighted";

	// from the Options DICOM dialog
	public static final String OPTIONS_DICOM_OPEN_FLOAT32 = "settings.dicom.OpenAs32bitFloat";
	public static final String OPTIONS_DICOM_ROTATE_YZ = "settings.dicom.RotateYZ";
	public static final String OPTIONS_DICOM_ROTATE_XZ = "settings.dicom.RotateXZ";

	// from the Options Font dialog
	public static final String OPTIONS_FONT_NAME = "settings.font.Name";
	public static final String OPTIONS_FONT_SIZE = "settings.font.Size";
	public static final String OPTIONS_FONT_STYLE = "settings.font.Style";
	public static final String OPTIONS_FONT_SMOOTHING = "settings.font.Smoothing";

	// from the Options IO dialog
	public static final String OPTIONS_IO_JPEG_QUALITY = "settings.io.JpegQuality";
	public static final String OPTIONS_IO_TRANSPARENT_INDEX = "settings.io.TransparentIndex";
	public static final String OPTIONS_IO_FILE_EXT = "settings.io.FileExtension";
	public static final String OPTIONS_IO_USE_JFILECHOOSER = "settings.io.UseJFileChooser";
	public static final String OPTIONS_IO_SAVE_INTEL = "settings.io.SaveInIntelByteOrder";
	public static final String OPTIONS_IO_COPY_COLUMNS = "settings.io.CopyColumns";
	public static final String OPTIONS_IO_COPY_ROWS = "settings.io.CopyRows";
	public static final String OPTIONS_IO_SAVE_COLUMNS = "settings.io.SaveColumns";
	public static final String OPTIONS_IO_SAVE_ROWS = "settings.io.SaveRows";

	// from the Options Linewidth dialog
	public static final String OPTIONS_LINEWIDTH_WIDTH = "settings.linewidth.LineWidth";

	// from the Options Memory/Threads dialog
	public static final String OPTIONS_MEMORYTHREADS_MAX_MEMORY = "settings.memorythreads.MaxMemory";
	public static final String OPTIONS_MEMORYTHREADS_STACK_THREADS = "settings.memorythreads.StackThreads";
	public static final String OPTIONS_MEMORYTHREADS_MULTIPLE_UNDO_BUFFERS = "settings.memorythreads.MultipleUndoBuffers";
	public static final String OPTIONS_MEMORYTHREADS_RUN_GC = "settings.memorythreads.RunGc";

	// from the Options Misc dialog
	public static final String OPTIONS_MISC_DBZ_VALUE = "settings.misc.DivideByZeroValue";
	public static final String OPTIONS_MISC_POINTER_CURSOR = "settings.misc.UsePointerCursor";
	public static final String OPTIONS_MISC_HIDE_STACK_MSG = "settings.misc.HideStackMessage";
	public static final String OPTIONS_MISC_REQUIRE_COMMAND = "settings.misc.RequireCommandKey";
	public static final String OPTIONS_MISC_MOVE_PLUGINS = "settings.misc.MoveIsolatedCommands";
	public static final String OPTIONS_MISC_SINGLE_INSTANCE = "settings.misc.SingleInstanceListener";
	public static final String OPTIONS_MISC_DEBUG_MODE = "settings.misc.DebugMode";

	// from the Options Point dialog
	public static final String OPTIONS_POINT_MARK_WIDTH = "settings.point.MarkWidth";
	public static final String OPTIONS_POINT_AUTO_MEASURE = "settings.point.AutoMeasure";
	public static final String OPTIONS_POINT_AUTOSLICE = "settings.point.AutoSlice";
	public static final String OPTIONS_POINT_ADD_ROI = "settings.point.AddToRoiManager";
	public static final String OPTIONS_POINT_LABEL_POINTS = "settings.point.LabelPoints";
	public static final String OPTIONS_POINT_SELECTION_COLOR = "settings.point.SelectionColor";

	// from the Options Profile Plot dialog
	public static final String OPTIONS_PROFILEPLOT_WIDTH = "settings.profileplot.Width";
	public static final String OPTIONS_PROFILEPLOT_HEIGHT = "settings.profileplot.Height";
	public static final String OPTIONS_PROFILEPLOT_MIN_Y = "settings.profileplot.MinY";
	public static final String OPTIONS_PROFILEPLOT_MAX_Y = "settings.profileplot.MaxY";
	public static final String OPTIONS_PROFILEPLOT_FIXED_YSCALE = "settings.profileplot.FixedYScale";
	public static final String OPTIONS_PROFILEPLOT_DISCARD_X = "settings.profileplot.DiscardXValues";
	public static final String OPTIONS_PROFILEPLOT_AUTOCLOSE = "settings.profileplot.AutoClose";
	public static final String OPTIONS_PROFILEPLOT_VERTICAL = "settings.profileplot.VerticalProfile";
	public static final String OPTIONS_PROFILEPLOT_LIST_VALUES = "settings.profileplot.ListValues";
	public static final String OPTIONS_PROFILEPLOT_INTERPOLATE = "settings.profileplot.Interpolate";
	public static final String OPTIONS_PROFILEPLOT_DRAW_GRID = "settings.profileplot.DrawGrid";

	// from the Options Proxy dialog
	public static final String OPTIONS_PROXY_SERVER = "settings.proxy.ProxyServer";
	public static final String OPTIONS_PROXY_PORT = "settings.proxy.ProxyPort";
	public static final String OPTIONS_PROXY_AUTHENTICATE = "settings.proxy.AuthenticationRequired";

	// from the Options Round Rect Tool dialog
	public static final String OPTIONS_ROUND_RECT_STROKE_WIDTH = "settings.roundrect.StrokeWidth";
	public static final String OPTIONS_ROUND_RECT_CORNER_DIAMETER = "settings.roundrect.CornerDiameter";
	public static final String OPTIONS_ROUND_RECT_STROKE_COLOR = "settings.roundrect.StrokeColor";
	public static final String OPTIONS_ROUND_RECT_FILL_COLOR = "settings.roundrect.FillColor";
	
	// from the Options Wand dialog
	public static final String OPTIONS_WAND_MODE = "settings.wand.Mode";
	public static final String OPTIONS_WAND_TOLERANCE = "settings.wand.Tolerance";
	
	// Scripting Options
	public static final String SCRIPT_LANG = "settings.script.lang";
	
	// miscellaneous settings from IJ1
	public static final String SETTINGS_FILTERS_NOISE_SD = "settings.ij1.filters.NoiseSD";
	public static final String SETTINGS_ANIMATOR_FPS = "settings.ij1.animator.FPS";
	public static final String SETTINGS_PROXY_USER = "settings.ij1.proxy.User";
	public static final String SETTINGS_TEXTWINDOW_WIDTH = "settings.ij1.textwindow.Width";
	public static final String SETTINGS_TEXTWINDOW_HEIGHT = "settings.ij1.textwindow.Height";
	public static final String SETTINGS_TEXTWINDOW_FONT_ANTIALIASED = "settings.ij1.textwindow.font.Antialiased";
	public static final String SETTINGS_TEXTWINDOW_FONT_SIZE = "settings.ij1.textwindow.font.Size";
	public static final String SETTINGS_PREFERRED_X_LOC = "settings.ij1.preferred.location.X";
	public static final String SETTINGS_PREFERRED_Y_LOC = "settings.ij1.preferred.location.Y";
	public static final String SETTINGS_PARTICLE_ANALYZER_OPTIONS = "settings.ij1.particle.analyzer.Options";
	public static final String SETTINGS_ANALYZER_MEASUREMENTS = "settings.ij1.analyzer.Measurements";
	public static final String SETTINGS_ANALYZER_MARK_WIDTH = "settings.ij1.analyzer.MarkWidth";
	public static final String SETTINGS_ANALYZER_PRECISION = "settings.ij1.analyzer.Precision";
	public static final String SETTINGS_IMPORT_TYPE = "settings.ij1.import.raw.Type";
	public static final String SETTINGS_IMPORT_WIDTH = "settings.ij1.import.raw.Width";
	public static final String SETTINGS_IMPORT_HEIGHT = "settings.ij1.import.raw.Height";
	public static final String SETTINGS_IMPORT_OFFSET = "settings.ij1.import.raw.Offset";
	public static final String SETTINGS_IMPORT_N = "settings.ij1.import.raw.N";
	public static final String SETTINGS_IMPORT_GAP = "settings.ij1.import.raw.Gap";
	public static final String SETTINGS_IMPORT_OPTIONS = "settings.ij1.import.raw.Options";
	public static final String SETTINGS_PLOTWINDOW_MIN = "settings.ij1.plotwindow.Min";
	public static final String SETTINGS_PLOTWINDOW_MAX = "settings.ij1.plotwindow.Max";
	public static final String SETTINGS_PLOTWINDOW_PLOT_WIDTH = "settings.ij1.plotwindow.Width";
	public static final String SETTINGS_PLOTWINDOW_PLOT_HEIGHT = "settings.ij1.plotwindow.Height";
	public static final String SETTINGS_PLOTWINDOW_OPTIONS = "settings.ij1.plotwindow.Options";
	public static final String SETTINGS_NEWIMAGE_TYPE = "settings.ij1.newimage.Type";
	public static final String SETTINGS_NEWIMAGE_FILL = "settings.ij1.newimage.Fill";
	public static final String SETTINGS_NEWIMAGE_WIDTH = "settings.ij1.newimage.Width";
	public static final String SETTINGS_NEWIMAGE_HEIGHT = "settings.ij1.newimage.Height";
	public static final String SETTINGS_NEWIMAGE_SLICES = "settings.ij1.newimage.Slices";
	public static final String SETTINGS_PREFS_OPTIONS = "settings.ij1.prefs.Options";
	public static final String SETTINGS_TOOLBAR_LAST_BRUSH_SIZE = "settings.ij1.toolbar.LastBrushSize";
	public static final String SETTINGS_BACKGROUND_SUBTRACTER_BACKGROUND = "settings.ij1.background.subtracter.Background";
	public static final String SETTINGS_IMAGE_MATH_MACRO_VALUE = "settings.ij1.imagemath.MacroValue";
	public static final String SETTINGS_STACKLABELER_LABEL_FORMAT = "settings.ij1.stacklabeler.LabelFormat";
	public static final String SETTINGS_COLOR_THRESHOLDER_DARK = "settings.ij1.colorthresholder.Dark";
	public static final String SETTINGS_EDITOR_FONT_SIZE = "settings.ij1.editor.FontSize";
	public static final String SETTINGS_EDITOR_FONT_MONO = "settings.ij1.editor.FontMono";
	public static final String SETTINGS_EDITOR_CASE_SENSITIVE = "settings.ij1.editor.CaseSensitive";
	public static final String SETTINGS_RECORDER_MODE = "settings.ij1.recorder.Mode";
	public static final String SETTINGS_THRESHOLD_ADJUSTER_MODE_KEY = "settings.ij1.threshold.adjuster.ModeKey";
	public static final String SETTINGS_THRESHOLD_ADJUSTER_DARK_BACKGROUND = "settings.ij1.threshold.adjuster.DarkBackground";
	public static final String SETTINGS_BATCH_INPUT = "settings.ij1.batch.Input";
	public static final String SETTINGS_BATCH_OUTPUT = "settings.ij1.batch.Output";
	public static final String SETTINGS_BATCH_FORMAT = "settings.ij1.batch.Format";
	public static final String SETTINGS_CANVAS_RESIZER_ZERO = "settings.ij1.canvas.resizer.Zero";
	public static final String SETTINGS_COMMAND_FINDER_CLOSE = "settings.ij1.command.finder.Close";
	public static final String SETTINGS_GEL_ANALYZER_OPTIONS = "settings.ij1.gel.analyzer.Options"; 
	public static final String SETTINGS_GEL_ANALYZER_VSCALE = "settings.ij1.gel.analyzer.VScale";
	public static final String SETTINGS_GEL_ANALYZER_HSCALE = "settings.ij1.gel.analyzer.HScale";
	public static final String SETTINGS_ZPROJECTOR_METHOD = "settings.ij1.zprojector.Method";
}
