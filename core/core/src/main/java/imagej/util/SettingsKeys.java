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
}
