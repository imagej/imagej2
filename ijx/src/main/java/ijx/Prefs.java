package ijx;
import ijx.plugin.filter.Filters;
import ijx.gui.ImageCanvasHelper;
import ijx.gui.IjxToolbar;
import ijx.roi.Roi;
import ijx.io.OpenDialog;
import ijx.io.FileSaver;
import ijx.IJ;
import java.io.*;
import java.util.*;
import java.net.URL;
import java.awt.*;
import java.applet.Applet;
import ij.io.*;
import ijx.util.Tools;

import ij.plugin.filter.*;
import ijx.plugin.Animator;
import ijx.process.FloatBlitter;
import ijx.process.ColorProcessor;
import ijx.text.TextWindow;
import ijx.CentralLookup;
import ijx.SavesPrefs;
import org.openide.util.Lookup;

/**
This class contains the ImageJ preferences, which are 
loaded from the "IJ_Props.txt" and "IJ_Prefs.txt" files.
@see ij.ImageJ
*/
public class Prefs {

	public static final String PROPS_NAME = "IJ_Props.txt";
	public static final String PREFS_NAME = "IJ_Prefs.txt";
	public static final String DIR_IMAGE = "dir.image";
	public static final String FCOLOR = "fcolor";
	public static final String BCOLOR = "bcolor";
	public static final String ROICOLOR = "roicolor";
	public static final String SHOW_ALL_COLOR = "showcolor";
	public static final String JPEG = "jpeg";
	public static final String FPS = "fps";
    public static final String DIV_BY_ZERO_VALUE = "div-by-zero";
    public static final String NOISE_SD = "noise.sd";
    public static final String MENU_SIZE = "menu.size";
    public static final String THREADS = "threads";
    public static final String ENABLE_RMI = "enable.rmi.listener";
	public static final String KEY_PREFIX = ".";
 
	private static final int USE_POINTER=1<<0, ANTIALIASING=1<<1, INTERPOLATE=1<<2, ONE_HUNDRED_PERCENT=1<<3,
		BLACK_BACKGROUND=1<<4, JFILE_CHOOSER=1<<5, UNUSED=1<<6, BLACK_CANVAS=1<<7, WEIGHTED=1<<8, 
		AUTO_MEASURE=1<<9, REQUIRE_CONTROL=1<<10, USE_INVERTING_LUT=1<<11, ANTIALIASED_TOOLS=1<<12,
		INTEL_BYTE_ORDER=1<<13, DOUBLE_BUFFER=1<<14, NO_POINT_LABELS=1<<15, NO_BORDER=1<<16,
		SHOW_ALL_SLICE_ONLY=1<<17, COPY_HEADERS=1<<18, NO_ROW_NUMBERS=1<<19,
		MOVE_TO_MISC=1<<20, ADD_TO_MANAGER=1<<21, RUN_SOCKET_LISTENER=1<<22,
		MULTI_POINT_MODE=1<<23, ROTATE_YZ=1<<24, FLIP_XZ=1<<25,
		DONT_SAVE_HEADERS=1<<26, DONT_SAVE_ROW_NUMBERS=1<<27; 
    public static final String OPTIONS = "prefs.options";
    
	public static final String vistaHint = "\n \nOn Windows Vista, ImageJ must be installed in a directory that\nthe user can write to, such as \"Desktop\" or \"Documents\"";

	/** file.separator system property */
	public static String separator = System.getProperty("file.separator");
	/** Use pointer cursor instead of cross */
	public static boolean usePointerCursor;
	/** No longer used */
	public static boolean antialiasedText;
	/** Display images scaled <100% using bilinear interpolation */
	public static boolean interpolateScaledImages;
	/** Open images at 100% magnification*/
	public static boolean open100Percent;
	/** Backgound is black in binary images*/
	public static boolean blackBackground;
	/** Use JFileChooser instead of FileDialog to open and save files. */
	public static boolean useJFileChooser;
	/** Color to grayscale conversion is weighted (0.299, 0.587, 0.114) if the variable is true. */
	public static boolean weightedColor;
	/** Use black image border. */
	public static boolean blackCanvas;
	/** Point tool auto-measure mode. */
	public static boolean pointAutoMeasure;
	/** Point tool auto-next slice mode (not saved in IJ_Prefs). */
	public static boolean pointAutoNextSlice;
	/** Require control or command key for keybaord shortcuts. */
	public static boolean requireControlKey;
	/** Open 8-bit images with inverting LUT so 0 is white and 255 is black. */
	public static boolean useInvertingLut;
	/** Draw tool icons using antialiasing. */
	public static boolean antialiasedTools = true;
	/** Export TIFF and Raw using little-endian byte order. */
	public static boolean intelByteOrder;
	/** Double buffer display of selections and overlays. */
	public static boolean doubleBuffer = true;
	/** Do not label multiple points created using point tool. */
	public static boolean noPointLabels = true;
	/** Disable Edit/Undo command. */
	public static boolean disableUndo;
	/** Do not draw black border around image. */
	public static boolean noBorder;
	/** Only show ROIs associated with current slice in Roi Manager "Show All" mode. */
	public static boolean showAllSliceOnly;
	/** Include column headers when copying tables to clipboard. */
	public static boolean copyColumnHeaders;
	/** Do not include row numbers when copying tables to clipboard. */
	public static boolean noRowNumbers;
	/** Move isolated plugins to Miscellaneous submenu. */
	public static boolean moveToMisc;
	/** Add points to ROI Manager. */
	public static boolean pointAddToManager;
	/** Extend the borders to foreground for binary erosions and closings. */
	public static boolean padEdges;
	/** Run the RMIListener (-1 unspecified, 0 no, 1 yes). */
	public static int enableRMIListener = -1;
	/** Only for backwards compatibility */
	public static boolean runSocketListener;
	/** Use MultiPoint tool. */
	public static boolean multiPointMode;
	/** Open DICOMs as 32-bit float images */
	public static boolean openDicomsAsFloat;
	/** Plot rectangular selectons vertically */
	public static boolean verticalProfile;
	/** Rotate YZ orthogonal views 90 degrees */
	public static boolean rotateYZ;
	/** Rotate XZ orthogonal views 180 degrees */
	public static boolean flipXZ;
	/** Don't save Results table column headers */
	public static boolean dontSaveHeaders;
	/** Don't save Results table row numbers */
	public static boolean dontSaveRowNumbers;


	static Properties ijPrefs = new Properties();
	static Properties props = new Properties(ijPrefs);
	static String prefsDir;
	static String imagesURL;
	static String homeDir; // ImageJ folder
	static int threads;
	static int transparentIndex = -1;
	static boolean commandLineMacro;

	/** Finds and loads the ImageJ configuration file, "IJ_Props.txt".
		@return	an error message if "IJ_Props.txt" not found.
	*/
	public static String load(Object ij, Applet applet) {
		InputStream f = (ij instanceof Class ? (Class)ij : ij.getClass()).getResourceAsStream("/"+PROPS_NAME);
		if (applet!=null)
			return loadAppletProps(f, applet);
		if (homeDir==null)
			homeDir = System.getProperty("user.dir");
		String userHome = System.getProperty("user.home");
		prefsDir = userHome;
		if (!IJ.isWindows()) {
			if (IJ.isMacOSX())
				prefsDir += "/Library/Preferences";
			else
				prefsDir += "/.imagej";
		} 
		if (f==null) {
			try {f = new FileInputStream(homeDir+"/"+PROPS_NAME);}
			catch (FileNotFoundException e) {f=null;}
		}
		if (f==null)
			return PROPS_NAME+" not found in ij.jar or in "+homeDir;
		f = new BufferedInputStream(f);
		try {props.load(f); f.close();}
		catch (IOException e) {return("Error loading "+PROPS_NAME);}
		imagesURL = props.getProperty("images.location");
		loadPreferences();
		loadOptions();
		return null;
	}

	static String loadAppletProps(InputStream f, Applet applet) {
		if (f==null)
			return PROPS_NAME+" not found in ij.jar";
		try {
			props.load(f);
			f.close();
		}
		catch (IOException e) {return("Error loading "+PROPS_NAME);}
		try {
			URL url = new URL(applet.getDocumentBase(), "images/");
			imagesURL = url.toString();
		}
		catch (Exception e) {}
		return null;
	}

	/** Returns the URL of the directory that contains the ImageJ sample images. */
	public static String getImagesURL() {
		return imagesURL;
	}

	/** Sets the URL of the directory that contains the ImageJ sample images. */
	public static void setImagesURL(String url) {
		imagesURL = url;
	}

	/** Returns the path to the ImageJ directory. */
	public static String getHomeDir() {
		return homeDir;
	}

	/** Gets the path to the directory where the 
		preferences file (IJPrefs.txt) is saved. */
	public static String getPrefsDir() {
		return prefsDir;
	}

	/** Sets the path to the ImageJ directory. */
	public static void setHomeDir(String path) {
		if (path.endsWith(File.separator))
			path = path.substring(0, path.length()-1);
		homeDir = path;
	}

	/** Returns the default directory, if any, or null. */
	public static String getDefaultDirectory() {
		if (commandLineMacro)
			return null;
		else
			return getString(DIR_IMAGE);
	}

	/** Finds an string in IJ_Props or IJ_Prefs.txt. */
	public static String getString(String key) {
		return props.getProperty(key);
	}

	/** Finds an string in IJ_Props or IJ_Prefs.txt. */
	public static String getString(String key, String defaultString) {
		if (props==null)
			return defaultString;
		String s = props.getProperty(key);
		if (s==null)
			return defaultString;
		else
			return s;
	}

	/** Finds a boolean in IJ_Props or IJ_Prefs.txt. */
	public static boolean getBoolean(String key, boolean defaultValue) {
		if (props==null) return defaultValue;
		String s = props.getProperty(key);
		if (s==null)
			return defaultValue;
		else
			return s.equals("true");
	}

	/** Finds an int in IJ_Props or IJ_Prefs.txt. */
	public static int getInt(String key, int defaultValue) {
		if (props==null) //workaround for Netscape JIT bug
			return defaultValue;
		String s = props.getProperty(key);
		if (s!=null) {
			try {
				return Integer.decode(s).intValue();
			} catch (NumberFormatException e) {IJ.write(""+e);}
		}
		return defaultValue;
	}

	/** Looks up a real number in IJ_Props or IJ_Prefs.txt. */
	public static double getDouble(String key, double defaultValue) {
		if (props==null)
			return defaultValue;
		String s = props.getProperty(key);
		Double d = null;
		if (s!=null) {
			try {d = new Double(s);}
			catch (NumberFormatException e){d = null;}
			if (d!=null)
				return(d.doubleValue());
		}
		return defaultValue;
	}

	/** Finds a color in IJ_Props or IJ_Prefs.txt. */
	public static Color getColor(String key, Color defaultColor) {
		int i = getInt(key, 0xaaa);
		if (i == 0xaaa)
			return defaultColor;
		return new Color((i >> 16) & 0xFF, (i >> 8) & 0xFF, i & 0xFF);
	}

	/** Returns the file.separator system property. */
	public static String getFileSeparator() {
		return separator;
	}

	/** Opens the IJ_Prefs.txt file. */
	static void loadPreferences() {
		String path = prefsDir+separator+PREFS_NAME;
		boolean ok =  loadPrefs(path);
		if (!ok && !IJ.isWindows()) {
			path = System.getProperty("user.home")+separator+PREFS_NAME;
			ok = loadPrefs(path); // look in home dir
			if (ok) new File(path).delete();
		}

	}

	static boolean loadPrefs(String path) {
		try {
			InputStream is = new BufferedInputStream(new FileInputStream(path));
			ijPrefs.load(is);
			is.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/** Saves user preferences in the IJ_Prefs.txt properties file. */
	public static void savePreferences() {
		try {
			Properties prefs = new Properties();
			String dir = OpenDialog.getDefaultDirectory();
			if (dir!=null)
				prefs.put(DIR_IMAGE, dir);
			prefs.put(ROICOLOR, Tools.c2hex(Roi.getColor()));
			prefs.put(SHOW_ALL_COLOR, Tools.c2hex(ImageCanvasHelper.getShowAllColor()));
            IjxToolbar toolbar = ((IjxToolbar) CentralLookup.getDefault().lookup(IjxToolbar.class));
			prefs.put(FCOLOR, Tools.c2hex(toolbar.getForegroundColor()));
			prefs.put(BCOLOR, Tools.c2hex(toolbar.getBackgroundColor()));
			prefs.put(JPEG, Integer.toString(FileSaver.getJpegQuality()));
			prefs.put(FPS, Double.toString(Animator.getFrameRate()));
			prefs.put(DIV_BY_ZERO_VALUE, Double.toString(FloatBlitter.divideByZeroValue));
			prefs.put(NOISE_SD, Double.toString(Filters.getSD()));
			if (threads>1) prefs.put(THREADS, Integer.toString(threads));
			if (IJ.isMacOSX()) useJFileChooser = false;
			saveOptions(prefs);
			savePluginPrefs(prefs);
			IJ.getInstance().savePreferences(prefs);
                        // IjX: New dynamic approach to calling classes that save preferences
            // Invoke savePrefs(prefs) on all implementors of SavesPrefs interface
            Collection<? extends SavesPrefs> s = Lookup.getDefault().lookupAll(SavesPrefs.class);
            for (Iterator<? extends SavesPrefs> it = s.iterator(); it.hasNext();) {
                SavesPrefs savesPrefs = it.next();
                System.out.println("SavesPrefs: " + savesPrefs.toString());
                savesPrefs.savePreferences(prefs);
            }
            // these have been changed to @ServiceProvider(SavesPrefs.class)...
            //Menus.savePreferences(prefs);
            //ParticleAnalyzer.savePreferences(prefs);
            //Analyzer.savePreferences(prefs);
            //ImportDialog.savePreferences(prefs);
            //PlotWindow.savePreferences(prefs);
            //NewImage.savePreferences(prefs);
			String path = prefsDir+separator+PREFS_NAME;
			if (prefsDir.endsWith(".imagej")) {
				File f = new File(prefsDir);
				if (!f.exists()) f.mkdir(); // create .imagej directory
			}
			savePrefs(prefs, path);
		} catch (Throwable t) {
			String msg = t.getMessage();
			if (msg==null) msg = ""+t;
			int delay = 4000;
			if (IJ.isVista()) {
				msg += vistaHint;
				delay = 1000;
			}
			try {
				new TextWindow("Error Saving Preferences", msg, 500, 200);
				IJ.wait(delay);
			} catch (Throwable t2) {}
		}
	}

	static void loadOptions() {
		int options = getInt(OPTIONS, ANTIALIASING);
		usePointerCursor = (options&USE_POINTER)!=0;
		//antialiasedText = (options&ANTIALIASING)!=0;
		antialiasedText = false;
		interpolateScaledImages = (options&INTERPOLATE)!=0;
		open100Percent = (options&ONE_HUNDRED_PERCENT)!=0;
		open100Percent = (options&ONE_HUNDRED_PERCENT)!=0;
		blackBackground = (options&BLACK_BACKGROUND)!=0;
		useJFileChooser = (options&JFILE_CHOOSER)!=0;
		weightedColor = (options&WEIGHTED)!=0;
		if (weightedColor)
			ColorProcessor.setWeightingFactors(0.299, 0.587, 0.114);
		blackCanvas = (options&BLACK_CANVAS)!=0;
		pointAutoMeasure = (options&AUTO_MEASURE)!=0;
		requireControlKey = (options&REQUIRE_CONTROL)!=0;
		useInvertingLut = (options&USE_INVERTING_LUT)!=0;
		antialiasedTools = (options&ANTIALIASED_TOOLS)!=0;
		intelByteOrder = (options&INTEL_BYTE_ORDER)!=0;
		// doubleBuffer = (options&DOUBLE_BUFFER)!=0; // always double buffer
		noPointLabels = (options&NO_POINT_LABELS)!=0;
		noBorder = (options&NO_BORDER)!=0;
		showAllSliceOnly = (options&SHOW_ALL_SLICE_ONLY)!=0;
		copyColumnHeaders = (options&COPY_HEADERS)!=0;
		noRowNumbers = (options&NO_ROW_NUMBERS)!=0;
		moveToMisc = (options&MOVE_TO_MISC)!=0;
		pointAddToManager = (options&ADD_TO_MANAGER)!=0;
		enableRMIListener = getInt(ENABLE_RMI, -1);
		multiPointMode = (options&MULTI_POINT_MODE)!=0;
		rotateYZ = (options&ROTATE_YZ)!=0;
		flipXZ = (options&FLIP_XZ)!=0;
		dontSaveHeaders = (options&DONT_SAVE_HEADERS)!=0;
		dontSaveRowNumbers = (options&DONT_SAVE_ROW_NUMBERS)!=0;
	}

	static void saveOptions(Properties prefs) {
		int options = (usePointerCursor?USE_POINTER:0) + (antialiasedText?ANTIALIASING:0)
			+ (interpolateScaledImages?INTERPOLATE:0) + (open100Percent?ONE_HUNDRED_PERCENT:0)
			+ (blackBackground?BLACK_BACKGROUND:0) + (useJFileChooser?JFILE_CHOOSER:0)
			+ (blackCanvas?BLACK_CANVAS:0) + (weightedColor?WEIGHTED:0) 
			+ (pointAutoMeasure?AUTO_MEASURE:0) + (requireControlKey?REQUIRE_CONTROL:0)
			+ (useInvertingLut?USE_INVERTING_LUT:0) + (antialiasedTools?ANTIALIASED_TOOLS:0)
			+ (intelByteOrder?INTEL_BYTE_ORDER:0) + (doubleBuffer?DOUBLE_BUFFER:0)
			+ (noPointLabels?NO_POINT_LABELS:0) + (noBorder?NO_BORDER:0)
			+ (showAllSliceOnly?SHOW_ALL_SLICE_ONLY:0) + (copyColumnHeaders?COPY_HEADERS:0)
			+ (noRowNumbers?NO_ROW_NUMBERS:0) + (moveToMisc?MOVE_TO_MISC:0)
			+ (pointAddToManager?ADD_TO_MANAGER:0)
			+ (multiPointMode?MULTI_POINT_MODE:0) + (rotateYZ?ROTATE_YZ:0)
			+ (flipXZ?FLIP_XZ:0) + (dontSaveHeaders?DONT_SAVE_HEADERS:0)
			+ (dontSaveRowNumbers?DONT_SAVE_ROW_NUMBERS:0);
		prefs.put(OPTIONS, Integer.toString(options));
		if (enableRMIListener >= 0)
			prefs.put(ENABLE_RMI, Integer.toString(enableRMIListener));
	}

	/** Saves the value of the string <code>text</code> in the preferences
		file using the keyword <code>key</code>. This string can be 
		retrieved using the appropriate <code>get()</code> method. */
	public static void set(String key, String text) {
		if (key.indexOf('.')<1)
			throw new IllegalArgumentException("Key must have a prefix");
		ijPrefs.put(KEY_PREFIX+key, text);
	}

	/** Saves <code>value</code> in the preferences file using 
		the keyword <code>key</code>. This value can be retrieved 
		using the appropriate <code>getPref()</code> method. */
	public static void set(String key, int value) {
		set(key, Integer.toString(value));
	}

	/** Saves <code>value</code> in the preferences file using 
		the keyword <code>key</code>. This value can be retrieved 
		using the appropriate <code>getPref()</code> method. */
	public static void set(String key, double value) {
		set(key, ""+value);
	}

	/** Saves the boolean variable <code>value</code> in the preferences
		 file using the keyword <code>key</code>. This value can be retrieved 
		using the appropriate <code>getPref()</code> method. */
	public static void set(String key, boolean value) {
		set(key, ""+value);
	}

	/** Uses the keyword <code>key</code> to retrieve a string from the
		preferences file. Returns <code>defaultValue</code> if the key
		is not found. */
	public static String get(String key, String defaultValue) {
		String value = ijPrefs.getProperty(KEY_PREFIX+key);
		if (value == null)
			return defaultValue;
		else
			return value;
	}

	/** Uses the keyword <code>key</code> to retrieve a number from the
		preferences file. Returns <code>defaultValue</code> if the key
		is not found. */
	public static double get(String key, double defaultValue) {
		String s = ijPrefs.getProperty(KEY_PREFIX+key);
		Double d = null;
		if (s!=null) {
			try {d = new Double(s);}
			catch (NumberFormatException e) {d = null;}
			if (d!=null)
				return(d.doubleValue());
		}
		return defaultValue;
	}

	/** Uses the keyword <code>key</code> to retrieve a boolean from
		the preferences file. Returns <code>defaultValue</code> if
		the key is not found. */
	public static boolean get(String key, boolean defaultValue) {
		String value = ijPrefs.getProperty(KEY_PREFIX+key);
		if (value==null)
			return defaultValue;
		else
			return value.equals("true");
	}

	/** Saves the Point <code>loc</code> in the preferences
		 file as a string using the keyword <code>key</code>. */
	public static void saveLocation(String key, Point loc) {
		set(key, loc.x+","+loc.y);
	}

	/** Uses the keyword <code>key</code> to retrieve a location
		from the preferences file. Returns null if the
		key is not found or the location is not valid (e.g., offscreen). */
	public static Point getLocation(String key) {
		String value = ijPrefs.getProperty(KEY_PREFIX+key);
		if (value==null) return null;
		int index = value.indexOf(",");
		if (index==-1) return null;
		double xloc = Tools.parseDouble(value.substring(0, index));
		if (Double.isNaN(xloc) || index==value.length()-1) return null;
		double yloc = Tools.parseDouble(value.substring(index+1));
		if (Double.isNaN(yloc)) return null;
		Point p = new Point((int)xloc, (int)yloc);
		Dimension screen = IJ.getScreenSize();
		if (p.x>screen.width-100 || p.y>screen.height-40)
			return null;
		else
			return p;
	}

	/** Save plugin preferences. */
	static void savePluginPrefs(Properties prefs) {
		Enumeration e = ijPrefs.keys();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			if (key.indexOf(KEY_PREFIX) == 0)
				prefs.put(key, ijPrefs.getProperty(key));
		}
	}

	public static void savePrefs(Properties prefs, String path) throws IOException{
		FileOutputStream fos = new FileOutputStream(path);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		prefs.store(bos, IJ.getInstance().getTitle() + " "+IJ.getInstance().getVersion()+" Preferences");
		bos.close();
	}
	
	/** Returns the number of threads used by PlugInFilters to process stacks. */
	public static int getThreads() {
		if (threads==0) {
			threads = getInt(THREADS, 0);
			int processors = Runtime.getRuntime().availableProcessors();
			if (threads<1 || threads>processors) threads = processors;
		}
		return threads;
	}
	
	/** Sets the number of threads (1-32) used by PlugInFilters to process stacks. */
	public static void setThreads(int n) {
		if (n<1) n = 1;
		if (n>32) n = 32;
		threads = n;
	}
	
	/** Sets the transparent index (0-255), or set to -1 to disable transparency. */
	public static void setTransparentIndex(int index) {
		if (index<-1 || index>255) index = -1;
		transparentIndex = index;
	}

	/** Returns the transparent index (0-255), or -1 if transparency is disabled. */
	public static int getTransparentIndex() {
		return transparentIndex;
	}



}

