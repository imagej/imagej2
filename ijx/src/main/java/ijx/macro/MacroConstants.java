package ijx.macro;

public interface MacroConstants {

	// In the tokenized macro, the token and optional symbol table address 
	// are packed into a single 32-bit int. The token is stored in the right-most 
	// 12 bits and the address in the remaining 24 bits. 
	static final int TOK_SHIFT=12, TOK_MASK=0xfff;

	static final int PLUS_PLUS=1, MINUS_MINUS=2, EQ=3, NEQ=4, GT=5, GTE=6, LT=7, LTE=8,
		PLUS_EQUAL=9, MINUS_EQUAL=10, MUL_EQUAL=11, DIV_EQUAL=12, LOGICAL_AND=13, LOGICAL_OR=14,
		SHIFT_RIGHT=15, SHIFT_LEFT=16;

	// Token types
	public static final int EOF=128, WORD=129, NUMBER=130, NOP=131, /*EOL=132,*/ STRING_CONSTANT=133, 
		PREDEFINED_FUNCTION=134, NUMERIC_FUNCTION=135, STRING_FUNCTION=136, ARRAY_FUNCTION=137, 
		USER_FUNCTION=138, ARRAY=139;

	// Keywords
	static final String[] keywords = {"macro", "var", "if", "else", "while", "do", "for", "function",
		"return", "true", "false", "PI", "NaN"};
	public static final int MACRO=200, VAR=201, IF=202, ELSE=203, WHILE=204, DO=205, FOR=206, FUNCTION=207,
		RETURN=208, TRUE=209, FALSE=210, PI=211, NaN=212;;
	static final int[] keywordIDs = {MACRO, VAR, IF, ELSE, WHILE, DO, FOR, FUNCTION,
		RETURN, TRUE, FALSE, PI, NaN};

	// Functions that don't return a value
	static final int RUN=300, INVERT=301, SELECT=302, WAIT=303, BEEP=304, RESET_MIN_MAX=305, RESET_THRESHOLD=306,
		PRINT=307, WRITE=308, DO_WAND=309, SET_MIN_MAX=310, SET_THRESHOLD=311, SET_TOOL=312,
		SET_FOREGROUND=313, SET_BACKGROUND=314, MAKE_LINE=315, MAKE_OVAL=316, MAKE_RECTANGLE=317,
		DUMP=318, MOVE_TO=319, LINE_TO=320, DRAW_LINE=321, REQUIRES=322, AUTO_UPDATE=323, UPDATE_DISPLAY=324, DRAW_STRING=325,
		SET_PASTE_MODE=326, DO_COMMAND=327, SHOW_STATUS=328, SHOW_PROGRESS=329, SHOW_MESSAGE=330, PUT_PIXEL=331, SET_PIXEL=332,
		SNAPSHOT=333, RESET=334, FILL=335, SET_COLOR=336, SET_LINE_WIDTH=337, CHANGE_VALUES=338, SELECT_IMAGE=339, EXIT=340,
		SET_LOCATION=341, GET_CURSOR_LOC=342, GET_LINE=343, GET_VOXEL_SIZE=344, GET_HISTOGRAM=345, GET_STATISTICS=346,
		GET_BOUNDING_RECT=347, GET_LUT=348, SET_LUT=349, GET_COORDINATES=350, SHOW_MESSAGE_WITH_CANCEL=351,
		MAKE_SELECTION=352, SET_RESULT=353, UPDATE_RESULTS=354, SET_BATCH_MODE=355, PLOT=356, SET_JUSTIFICATION=357,
		SET_Z_COORDINATE=358, GET_THRESHOLD=359, GET_PIXEL_SIZE=360, SETUP_UNDO=361, SAVE_SETTINGS=362, RESTORE_SETTINGS=363,
		SET_KEY_DOWN=364, OPEN=365, SET_FONT=366, GET_MIN_AND_MAX=367, CLOSE=368, SET_SLICE=369,
		NEW_IMAGE=370, SAVE_AS=371, SAVE=372, SET_AUTO_THRESHOLD=373, RENAME=374, GET_BOUNDS=375, FILL_RECT=376,
		GET_RAW_STATISTICS=377, FLOOD_FILL=378, RESTORE_PREVIOUS_TOOL=379, SET_VOXEL_SIZE=380, GET_LOCATION_AND_SIZE=381,
		GET_DATE_AND_TIME=382, SET_METADATA=383, CALCULATOR=384, SET_RGB_WEIGHTS=385, MAKE_POLYGON=386, SET_SELECTION_NAME=387,
		DRAW_RECT=388, DRAW_OVAL=389, FILL_OVAL=390, SET_OPTION=391, SHOW_TEXT=392, SET_SELECTION_LOC=393, GET_DIMENSIONS=394,
		WAIT_FOR_USER=395, MAKE_POINT=396, MAKE_TEXT=397;
	static final String[] functions = {"run","invert","selectWindow","wait", "beep", "resetMinAndMax", "resetThreshold",
		"print", "write", "doWand", "setMinAndMax", "setThreshold", "setTool",
		"setForegroundColor", "setBackgroundColor", "makeLine", "makeOval", "makeRectangle",
		"dump", "moveTo", "lineTo", "drawLine", "requires", "autoUpdate", "updateDisplay", "drawString",
		"setPasteMode", "doCommand", "showStatus", "showProgress", "showMessage", "putPixel", "setPixel",
		"snapshot", "reset", "fill", "setColor", "setLineWidth", "changeValues", "selectImage", "exit",
		"setLocation", "getCursorLoc", "getLine", "getVoxelSize", "getHistogram", "getStatistics",
		"getBoundingRect", "getLut", "setLut", "getSelectionCoordinates", "showMessageWithCancel",
		"makeSelection", "setResult", "updateResults", "setBatchMode", "Plot", "setJustification",
		"setZCoordinate", "getThreshold", "getPixelSize", "setupUndo", "saveSettings", "restoreSettings",
		"setKeyDown", "open", "setFont", "getMinAndMax", "close", "setSlice",
		"newImage", "saveAs", "save", "setAutoThreshold", "rename", "getSelectionBounds", "fillRect",
		"getRawStatistics", "floodFill", "restorePreviousTool", "setVoxelSize", "getLocationAndSize",
		"getDateAndTime", "setMetadata", "imageCalculator", "setRGBWeights", "makePolygon", "setSelectionName",
		"drawRect", "drawOval", "fillOval", "setOption", "showText", "setSelectionLocation", "getDimensions",
		"waitForUser", "makePoint", "makeText"};
	static final int[] functionIDs = {RUN, INVERT, SELECT, WAIT, BEEP, RESET_MIN_MAX, RESET_THRESHOLD,
		PRINT, WRITE,	 DO_WAND, SET_MIN_MAX, SET_THRESHOLD, SET_TOOL,
		SET_FOREGROUND, SET_BACKGROUND, MAKE_LINE, MAKE_OVAL, MAKE_RECTANGLE,
		DUMP, MOVE_TO, LINE_TO, DRAW_LINE, REQUIRES, AUTO_UPDATE, UPDATE_DISPLAY, DRAW_STRING,
		SET_PASTE_MODE, DO_COMMAND, SHOW_STATUS, SHOW_PROGRESS, SHOW_MESSAGE, PUT_PIXEL, SET_PIXEL,
		SNAPSHOT, RESET, FILL, SET_COLOR, SET_LINE_WIDTH, CHANGE_VALUES, SELECT_IMAGE, EXIT,
		SET_LOCATION, GET_CURSOR_LOC, GET_LINE, GET_VOXEL_SIZE, GET_HISTOGRAM, GET_STATISTICS,
		GET_BOUNDING_RECT, GET_LUT, SET_LUT, GET_COORDINATES, SHOW_MESSAGE_WITH_CANCEL,
		MAKE_SELECTION, SET_RESULT, UPDATE_RESULTS, SET_BATCH_MODE, PLOT, SET_JUSTIFICATION,
		SET_Z_COORDINATE, GET_THRESHOLD, GET_PIXEL_SIZE, SETUP_UNDO, SAVE_SETTINGS, RESTORE_SETTINGS,
		SET_KEY_DOWN, OPEN, SET_FONT, GET_MIN_AND_MAX, CLOSE, SET_SLICE,
		NEW_IMAGE, SAVE_AS, SAVE, SET_AUTO_THRESHOLD, RENAME, GET_BOUNDS, FILL_RECT,
		GET_RAW_STATISTICS, FLOOD_FILL, RESTORE_PREVIOUS_TOOL, SET_VOXEL_SIZE, GET_LOCATION_AND_SIZE,
		GET_DATE_AND_TIME, SET_METADATA, CALCULATOR, SET_RGB_WEIGHTS, MAKE_POLYGON, SET_SELECTION_NAME,
		DRAW_RECT, DRAW_OVAL, FILL_OVAL, SET_OPTION, SHOW_TEXT, SET_SELECTION_LOC, GET_DIMENSIONS,
		WAIT_FOR_USER, MAKE_POINT, MAKE_TEXT};

	// Numeric functions
	static final int GET_PIXEL=1000, ABS=1001, COS=1002, EXP=1003, FLOOR=1004, LOG=1005, MAX_OF=1006, MIN_OF=1007, POW=1008,
		ROUND=1009, SIN=1010, SQRT=1011, TAN=1012, GET_TIME=1013, GET_WIDTH=1014, GET_HEIGHT=1015, RANDOM=1016,
		GET_RESULT=1017, GET_COUNT=1018, GET_NUMBER=1019, NIMAGES=1020, NSLICES=1021,
		LENGTH_OF=1022, NRESULTS=1023, GET_ID=1024, BIT_DEPTH=1025, SELECTION_TYPE=1026, ATAN=1027, IS_OPEN=1028, 
		IS_ACTIVE=1029, INDEX_OF=1030, LAST_INDEX_OF=1031, CHAR_CODE_AT=1032, GET_BOOLEAN=1033,
		STARTS_WITH=1034, ENDS_WITH=1035, ATAN2=1036, IS_NAN=1037, GET_ZOOM=1038, PARSE_INT=1039, PARSE_FLOAT=1040,
		IS_KEY_DOWN=1041, GET_SLICE_NUMBER=1042, SCREEN_WIDTH=1043, SCREEN_HEIGHT=1044, CALIBRATE=1045,
		ASIN=1046, ACOS=1047, ROI_MANAGER=1048, TOOL_ID=1049, IS=1050, GET_VALUE=1051, STACK=1052, MATCHES=1053,
		GET_STRING_WIDTH=1054, FIT=1055, OVERLAY=1056;
	static final String[] numericFunctions = { "getPixel", "abs", "cos", "exp", "floor", "log", "maxOf", "minOf", "pow",
		"round", "sin", "sqrt", "tan", "getTime", "getWidth", "getHeight", "random",
		"getResult", "getResultsCount", "getNumber", "nImages", "nSlices", 
		"lengthOf", "nResults", "getImageID", "bitDepth", "selectionType", "atan", "isOpen",
		"isActive", "indexOf", "lastIndexOf", "charCodeAt", "getBoolean",
		"startsWith", "endsWith", "atan2", "isNaN", "getZoom", "parseInt", "parseFloat",
		"isKeyDown", "getSliceNumber", "screenWidth", "screenHeight", "calibrate",
		"asin", "acos", "roiManager", "toolID", "is", "getValue", "Stack", "matches",
		"getStringWidth", "Fit", "Overlay"};
	static final int[] numericFunctionIDs = {GET_PIXEL, ABS, COS, EXP, FLOOR, LOG, MAX_OF, MIN_OF, POW,
		ROUND, SIN, SQRT, TAN, GET_TIME, GET_WIDTH, GET_HEIGHT, RANDOM,
		GET_RESULT, GET_COUNT, GET_NUMBER, NIMAGES, NSLICES,
		LENGTH_OF, NRESULTS, GET_ID, BIT_DEPTH, SELECTION_TYPE, ATAN, IS_OPEN, 
		IS_ACTIVE, INDEX_OF, LAST_INDEX_OF, CHAR_CODE_AT, GET_BOOLEAN,
		STARTS_WITH, ENDS_WITH, ATAN2, IS_NAN, GET_ZOOM, PARSE_INT, PARSE_FLOAT,
		IS_KEY_DOWN, GET_SLICE_NUMBER, SCREEN_WIDTH, SCREEN_HEIGHT, CALIBRATE,
		ASIN, ACOS, ROI_MANAGER, TOOL_ID, IS, GET_VALUE, STACK, MATCHES,
		GET_STRING_WIDTH, FIT, OVERLAY};

	// String functions
	static final int D2S=2000, TO_HEX=2001, TO_BINARY=2002, GET_TITLE=2003, GET_STRING=2004, SUBSTRING=2005,
		FROM_CHAR_CODE=2006, GET_INFO=2007, GET_DIRECTORY=2008, GET_ARGUMENT=2009, GET_IMAGE_INFO=2010,
		TO_LOWER_CASE=2011, TO_UPPER_CASE=2012, RUN_MACRO=2013, EVAL=2014, TO_STRING=2015, REPLACE=2016,
		DIALOG=2017, GET_METADATA=2018, FILE=2019, SELECTION_NAME=2020, GET_VERSION=2021, GET_RESULT_LABEL=2022,
		CALL=2023, STRING=2024, EXT=2025, EXEC=2026, LIST=2027, DEBUG=2028, IJ_CALL=2029;
	static final String[] stringFunctions = {"d2s", "toHex", "toBinary", "getTitle", "getString", "substring",
		"fromCharCode", "getInfo", "getDirectory", "getArgument", "getImageInfo", 
		"toLowerCase", "toUpperCase", "runMacro", "eval", "toString", "replace",
		"Dialog", "getMetadata", "File", "selectionName", "getVersion", "getResultLabel",
		"call", "String", "Ext", "exec", "List", "debug", "IJ"};
	static final int[] stringFunctionIDs = {D2S, TO_HEX, TO_BINARY, GET_TITLE, GET_STRING, SUBSTRING,
		FROM_CHAR_CODE, GET_INFO, GET_DIRECTORY, GET_ARGUMENT, GET_IMAGE_INFO,
		TO_LOWER_CASE, TO_UPPER_CASE, RUN_MACRO, EVAL, TO_STRING, REPLACE,
		DIALOG, GET_METADATA, FILE, SELECTION_NAME, GET_VERSION, GET_RESULT_LABEL,
		CALL, STRING, EXT, EXEC, LIST, DEBUG, IJ_CALL};

	// Array functions
	static final int GET_PROFILE=3000, NEW_ARRAY=3001, SPLIT=3002, GET_FILE_LIST=3003,
		GET_FONT_LIST=3004, NEW_MENU=3005, GET_LIST=3006, ARRAY_FUNC=3007;
	static final String[] arrayFunctions = {"getProfile", "newArray", "split", "getFileList",
		"getFontList", "newMenu", "getList", "Array"};
	static final int[] arrayFunctionIDs = {GET_PROFILE, NEW_ARRAY, SPLIT, GET_FILE_LIST,
		GET_FONT_LIST, NEW_MENU, GET_LIST, ARRAY_FUNC};

}  // interface MacroConstants
