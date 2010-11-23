package ij;

import ij.util.*;

import ij.plugin.MacroInstaller;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.awt.event.*;
import java.util.zip.*;

/**
This class installs and updates ImageJ's menus. Note that menu labels,
even in submenus, must be unique. This is because ImageJ uses a single
hash table for all menu labels. If you look closely, you will see that
File->Import->Text Image... and File->Save As->Text Image... do not use
the same label. One of the labels has an extra space.

@see ImageJ
*/

public class Menus {

	public static final char PLUGINS_MENU = 'p';
	public static final char IMPORT_MENU = 'i';
	public static final char SAVE_AS_MENU = 's';
	public static final char SHORTCUTS_MENU = 'h'; // 'h'=hotkey
	public static final char ABOUT_MENU = 'a';
	public static final char FILTERS_MENU = 'f';
	public static final char TOOLS_MENU = 't';
	public static final char UTILITIES_MENU = 'u';
		
	public static final int WINDOW_MENU_ITEMS = 5; // fixed items at top of Window menu
	
	public static final int NORMAL_RETURN = 0;
	public static final int COMMAND_IN_USE = -1;
	public static final int INVALID_SHORTCUT = -2;
	public static final int SHORTCUT_IN_USE = -3;
	public static final int NOT_INSTALLED = -4;
	public static final int COMMAND_NOT_FOUND = -5;
	
	public static final int MAX_OPEN_RECENT_ITEMS = 15;
	
	private static MenuBar mbar;
	private static CheckboxMenuItem gray8Item,gray16Item,gray32Item,
			color256Item,colorRGBItem,RGBStackItem,HSBStackItem;
	private static PopupMenu popup;

	private static ImageJ ij;
	private static ImageJApplet applet;
	private static Hashtable demoImagesTable = new Hashtable();
	private static String pluginsPath, macrosPath;
	private static String[] pluginsPaths;
	private static Menu pluginsMenu, importMenu, saveAsMenu, shortcutsMenu, 
		aboutMenu, filtersMenu, toolsMenu, utilitiesMenu, macrosMenu, optionsMenu;
	private static Hashtable pluginsTable;
	
	static Menu window, openRecentMenu;
	static int nPlugins, nMacros;
	private static Hashtable shortcuts = new Hashtable();
	private static Hashtable macroShortcuts;
	private static Vector pluginsPrefs = new Vector(); // commands saved in IJ_Prefs
	static int windowMenuItems2; // non-image windows listed in Window menu + separator
	private static String error;
	private String jarError;
	private String pluginError;
    private boolean isJarErrorHeading;
	private boolean installingJars, duplicateCommand;
	private static Vector jarFiles;  // JAR files in plugins folder with "_" in their name
	private static Vector macroFiles;  // Macro files in plugins folder with "_" in their name
	private int importCount, saveAsCount, toolsCount, optionsCount;
	private static Hashtable menusTable; // Submenus of Plugins menu
	private int userPluginsIndex; // First user plugin or submenu in Plugins menu
	private boolean addSorted;
	private  static int fontSize = Prefs.getInt(Prefs.MENU_SIZE, 14);
	private static Font menuFont;
	static boolean jnlp; // true when using Java WebStart
		
	Menus(ImageJ ijInstance, java.applet.Applet appletInstance) { }
	Menus(ImageJ ijInstance, ImageJApplet appletInstance) {
		ij = ijInstance;
		applet = appletInstance;
	}

	String addMenuBar() {
		error = null;
		pluginsTable = new Hashtable();

		addPlugInItem("Open...", "ij.plugin.Commands(\"open\")", KeyEvent.VK_O, false);
		addPlugInItem("Open Next", "ij.plugin.NextImageOpener", KeyEvent.VK_O, true);
		addPlugInItem("Close", "ij.plugin.Commands(\"close\")", KeyEvent.VK_W, false);
		addPlugInItem("Save", "ij.plugin.Commands(\"save\")", KeyEvent.VK_S, false);
		addPlugInItem("Revert", "ij.plugin.Commands(\"revert\")", KeyEvent.VK_R,  false);
		addPlugInItem("Page Setup...", "ij.plugin.filter.Printer(\"setup\")", 0, false);
		addPlugInItem("Print...", "ij.plugin.filter.Printer(\"print\")", KeyEvent.VK_P, false);
		addPlugInItem("Quit", "ij.plugin.Commands(\"quit\")", 0, false);
		
		addPlugInItem("Undo", "ij.plugin.Commands(\"undo\")", KeyEvent.VK_Z, false);
		addPlugInItem("Cut", "ij.plugin.Clipboard(\"cut\")", KeyEvent.VK_X, false);
		addPlugInItem("Copy", "ij.plugin.Clipboard(\"copy\")", KeyEvent.VK_C, false);
		addPlugInItem("Copy to System", "ij.plugin.Clipboard(\"scopy\")", 0, false);
		addPlugInItem("Paste", "ij.plugin.Clipboard(\"paste\")", KeyEvent.VK_V, false);
		addPlugInItem("Paste Control...", "ij.plugin.frame.PasteController", 0, false);
		addPlugInItem("Clear", "ij.plugin.filter.Filler(\"clear\")", 0, false);
		addPlugInItem("Clear Outside", "ij.plugin.filter.Filler(\"outside\")", 0, false);
		addPlugInItem("Fill", "ij.plugin.filter.Filler(\"fill\")", KeyEvent.VK_F, false);
		addPlugInItem("Draw", "ij.plugin.filter.Filler(\"draw\")", KeyEvent.VK_D, false);
		addPlugInItem("Invert", "ij.plugin.filter.Filters(\"invert\")", KeyEvent.VK_I, true);
		
		addCheckboxItem("8-bit", "ij.plugin.Converter(\"8-bit\")");
		addCheckboxItem("16-bit", "ij.plugin.Converter(\"16-bit\")");
		addCheckboxItem("32-bit", "ij.plugin.Converter(\"32-bit\")");
		addCheckboxItem("8-bit Color", "ij.plugin.Converter(\"8-bit Color\")");
		addCheckboxItem("RGB Color", "ij.plugin.Converter(\"RGB Color\")");
		addCheckboxItem("RGB Stack", "ij.plugin.Converter(\"RGB Stack\")");
		addCheckboxItem("HSB Stack", "ij.plugin.Converter(\"HSB Stack\")");
			
		addPlugInItem("Show Info...", "ij.plugin.filter.Info", KeyEvent.VK_I, false);
		addPlugInItem("Properties...", "ij.plugin.filter.ImageProperties", KeyEvent.VK_P, true);
		addPlugInItem("Crop", "ij.plugin.filter.Resizer(\"crop\")", KeyEvent.VK_X, true);
		addPlugInItem("Duplicate...", "ij.plugin.filter.Duplicater", KeyEvent.VK_D, true);
		addPlugInItem("Rename...", "ij.plugin.SimpleCommands(\"rename\")", 0, false);
		addPlugInItem("Scale...", "ij.plugin.Scaler", KeyEvent.VK_E, false);
		
		addPlugInItem("Smooth", "ij.plugin.filter.Filters(\"smooth\")", KeyEvent.VK_S, true);
		addPlugInItem("Sharpen", "ij.plugin.filter.Filters(\"sharpen\")", 0, false);
		addPlugInItem("Find Edges", "ij.plugin.filter.Filters(\"edge\")", 0, false);
		addPlugInItem("Enhance Contrast", "ij.plugin.ContrastEnhancer", 0, false);
		addPlugInItem("Image Calculator...", "ij.plugin.ImageCalculator", 0, false);
		addPlugInItem("Subtract Background...", "ij.plugin.filter.BackgroundSubtracter", 0, false);
		
		addPlugInItem("Measure", "ij.plugin.filter.Analyzer", KeyEvent.VK_M, false);
		addPlugInItem("Analyze Particles...", "ij.plugin.filter.ParticleAnalyzer", 0, false);
		addPlugInItem("Summarize", "ij.plugin.filter.Analyzer(\"sum\")", 0, false);
		addPlugInItem("Distribution...", "ij.plugin.Distribution", 0, false);
		addPlugInItem("Label", "ij.plugin.filter.Filler(\"label\")", 0, false);
		addPlugInItem("Clear Results", "ij.plugin.filter.Analyzer(\"clear\")", 0, false);
		addPlugInItem("Set Measurements...", "ij.plugin.filter.Analyzer(\"set\")", 0, false);
		addPlugInItem("Set Scale...", "ij.plugin.filter.ScaleDialog", 0, false);
		addPlugInItem("Calibrate...", "ij.plugin.filter.Calibrator", 0, false);
		addPlugInItem("Histogram", "ij.plugin.Histogram", KeyEvent.VK_H, false);
		addPlugInItem("Plot Profile", "ij.plugin.filter.Profiler(\"plot\")", KeyEvent.VK_K, false);
		addPlugInItem("Surface Plot...", "ij.plugin.SurfacePlotter", 0, false);

		addPlugInItem("Show All", "ij.plugin.WindowOrganizer(\"show\")", KeyEvent.VK_F, true);
		addPlugInItem("Put Behind [tab]", "ij.plugin.Commands(\"tab\")", 0, false);
		addPlugInItem("Cascade", "ij.plugin.WindowOrganizer(\"cascade\")", 0, false);
		addPlugInItem("Tile", "ij.plugin.WindowOrganizer(\"tile\")", 0, false);

		addPlugInItem("ImageJA Web Site...", "ij.plugin.BrowserLauncher", 0, false);
		addPlugInItem("Online Docs...", "ij.plugin.BrowserLauncher(\"online\")", 0, false);
		addPlugInItem("About ImageJA...", "ij.plugin.AboutBoxJA", 0, false);
				
		addSubMenu("New");
		addSubMenu("Open Samples");
		addSubMenu("Import");
		addSubMenu("Save As");
		addSubMenu("Selection");
		addSubMenu("Options");
		addSubMenu("Adjust");
		addSubMenu("Color");
		addSubMenu("Stacks");
		addSubMenu("Rotate");
		addSubMenu("Zoom");
		addSubMenu("Lookup Tables");
		addSubMenu("Noise");
		addSubMenu("Shadows");
		addSubMenu("Binary");
		addSubMenu("Math");
		addSubMenu("FFT");
		addSubMenu("Filters");
		addSubMenu("Gels");
		addSubMenu("Tools");
		addSubMenu("About Plugins");

		installPlugins();
		
		if (pluginError!=null)
			error = error!=null?error+="\n"+pluginError:pluginError;
		if (jarError!=null)
			error = error!=null?error+="\n"+jarError:jarError;
		return error;
	}
	
	void addPlugInItem(String label, String className, int shortcut, boolean shift) {
		pluginsTable.put(label, className);
		nPlugins++;
	}

	void addCheckboxItem(String label, String className) {
		pluginsTable.put(label, className);
		nPlugins++;
	}

	void addSubMenu(String name) {
		String value;
		String key = name.toLowerCase(Locale.US);
		int index;
		index = key.indexOf(' ');
		if (index>0)
			key = key.substring(0, index);
 		for (int count=1; count<100; count++) {
			value = Prefs.getString(key + (count/10)%10 + count%10);
			if (value==null)
				break;
			addPluginItem(value);
		}
	}

	void addPluginItem(String s) {
		if (s.startsWith("\"-\"")) {
			// add menu separator if command="-"
			return;
		}
		int lastComma = s.lastIndexOf(',');
		if (lastComma<=0)
			return;
		String command = s.substring(1,lastComma-1);
		int keyCode = 0;
		boolean shift = false;
		if (command.endsWith("]")) {
			int openBracket = command.lastIndexOf('[');
			if (openBracket>0) {
				String shortcut = command.substring(openBracket+1,command.length()-1);
				keyCode = convertShortcutToCode(shortcut);
				boolean functionKey = keyCode>=KeyEvent.VK_F1 && keyCode<=KeyEvent.VK_F12;
				if (keyCode>0 && !functionKey)
					command = command.substring(0,openBracket);
				//IJ.write(command+": "+shortcut);
			}
		}
		if (keyCode>=KeyEvent.VK_F1 && keyCode<=KeyEvent.VK_F12) {
			shortcuts.put(new Integer(keyCode),command);
			keyCode = 0;
		} else if (keyCode>=265 && keyCode<=290) {
			keyCode -= 200;
			shift = true;
		}
		while(s.charAt(lastComma+1)==' ' && lastComma+2<s.length())
			lastComma++; // remove leading spaces
		String className = s.substring(lastComma+1,s.length());
		//IJ.log(command+"  "+className);
		if (installingJars)
			duplicateCommand = pluginsTable.get(command)!=null;
		pluginsTable.put(command, className);
		nPlugins++;
	}

	void checkForDuplicate(String command) {
		if (pluginsTable.get(command)!=null) {
		}
	}
	
	void addPluginsMenu() {
		String value,label,className;
		int index;
		for (int count=1; count<100; count++) {
			value = Prefs.getString("plug-in" + (count/10)%10 + count%10);
			if (value==null)
				break;
			char firstChar = value.charAt(0);
			if (firstChar=='-')
				;
			else if (firstChar=='>') {
				String submenu = value.substring(2,value.length()-1);
				addSubMenu(submenu);
			} else
				addPluginItem(value);
		}
		userPluginsIndex = 0;
	}

	/** Install plugins using "pluginxx=" keys in IJ_Prefs.txt.
		Plugins not listed in IJ_Prefs are added to the end
		of the Plugins menu. */
	void installPlugins() {
		String value, className;
		char menuCode;
		String[] plugins = getPlugins();
		String[] plugins2 = null;
		Hashtable skipList = new Hashtable();
 		for (int index=0; index<100; index++) {
			value = Prefs.getString("plugin" + (index/10)%10 + index%10);
			if (value==null)
				break;
			menuCode = value.charAt(0);
			String prefsValue = value;
			value = value.substring(2,value.length()); //remove menu code and coma
			className = value.substring(value.lastIndexOf(',')+1,value.length());
			boolean found = className.startsWith("ij.");
			if (!found && plugins!=null) { // does this plugin exist?
				if (plugins2==null)
					plugins2 = getStrippedPlugins(plugins);
				for (int i=0; i<plugins2.length; i++) {
					if (className.startsWith(plugins2[i])) {
						found = true;
						break;
					}
				}
			}
			if (found) {
				addPluginItem(value);
				pluginsPrefs.addElement(prefsValue);
				if (className.endsWith("\")")) { // remove any argument
					int argStart = className.lastIndexOf("(\"");
					if (argStart>0)
						className = className.substring(0, argStart);
				}
				skipList.put(className, "");
			}
		}
		if (plugins!=null) {
			for (int i=0; i<plugins.length; i++) {
				if (!skipList.containsKey(plugins[i]))
					installUserPlugin(plugins[i]);
			}
		}
		installJarPlugins();
		installMacros();
	}
	
	/** Installs macro files that are located in the plugins folder and have a "_" in their name. */
	void installMacros() {
		if (macroFiles==null)
			return;
		for (int i=0; i<macroFiles.size(); i++) {
			String name = (String)macroFiles.elementAt(i);
			installMacro(name);
		}		
	}

	/** Installs a macro in the Plugins menu, or submenu, with
		with underscores in the file name replaced by spaces. */
	void installMacro(String name) {
		String dir = null;
		int slashIndex = name.indexOf('/');
		if (slashIndex>0) {
			dir = name.substring(0, slashIndex);
			name = name.substring(slashIndex+1, name.length());
		}
		String command = name.replace('_',' ');
		command = command.substring(0, command.length()-4); //remove ".txt" or ".ijm"
		command.trim();
		if (pluginsTable.get(command)!=null) // duplicate command?
			command = command + " Macro";
		String path = (dir!=null?dir+File.separator:"") + name;
		pluginsTable.put(command, "ij.plugin.Macro_Runner(\""+path+"\")");
		nMacros++;
	}


	/** Install plugins located in JAR files. */
	void installJarPlugins() {
		if (jarFiles==null)
			return;
		installingJars = true;
		for (int i=0; i<jarFiles.size(); i++) {
            isJarErrorHeading = false;
			String jar = (String)jarFiles.elementAt(i);
			InputStream is = getConfigurationFile(jar);
            if (is==null) continue;
            LineNumberReader lnr = new LineNumberReader(new InputStreamReader(is));
            try {
                while(true) {
                    String s = lnr.readLine();
                    if (s==null) break;
                    installJarPlugin(jar, s);
                }
            }
            catch (IOException e) {}
			finally {
				try {if (lnr!=null) lnr.close();}
				catch (IOException e) {}
			}
		}		
	}
    
    /** Install a plugin located in a JAR file. */
    void installJarPlugin(String jar, String s) {
		//IJ.log(s);
		if (s.length()<3) return;
		char firstChar = s.charAt(0);
		if (firstChar=='#') return;
		addSorted = false;
        if (s.startsWith("Plugins>")) {
			int firstComma = s.indexOf(',');
			if (firstComma==-1 || firstComma<=8)
				;
			else {
        		String name = s.substring(8, firstComma);
			}
        } else if (firstChar=='"' || s.startsWith("Plugins")) {
        	String name = getSubmenuName(jar);
            addJarErrorHeading(jar);
			jarError += "    Invalid menu: " + s + "\n";
			return;
		}
		int firstQuote = s.indexOf('"');
		if (firstQuote==-1)
			return;
		s = s.substring(firstQuote, s.length()); // remove menu
		if (duplicateCommand) {
			if (jarError==null) jarError = "";
            addJarErrorHeading(jar);
			jarError += "    Duplicate command: " + s + "\n";
		}
		addPluginItem(s);
		duplicateCommand = false;
    }
    
    void addJarErrorHeading(String jar) {
        if (!isJarErrorHeading) {
		if (jarError == null)
			jarError = "";
                if (!jarError.equals(""))
                    jarError += " \n";
                jarError += "Plugin configuration error: " + jar + "\n";
                isJarErrorHeading = true;
            }
    }

	String getSubmenuName(String jarPath) {
		//IJ.log("getSubmenuName: \n"+jarPath+"\n"+pluginsPath);
		for (int i = 0; i < pluginsPaths.length; i++)
			if (jarPath.startsWith(pluginsPaths[i])) {
				jarPath = jarPath.substring(pluginsPaths[i].length() - 1);			break;
			}
		int index = jarPath.lastIndexOf(File.separatorChar);
		if (index<0) return null;
		String name = jarPath.substring(0, index);
		index = name.lastIndexOf(File.separatorChar);
		if (index<0) return null;
		name = name.substring(index+1);
		if (name.equals("plugins")) return null;
		return name;
    }

    /** Opens the configuration file ("plugins.txt") from a JAR file and returns it as an InputStream. */
	InputStream getConfigurationFile(String jar) {
		try {
			ZipFile jarFile = new ZipFile(jar);
			Enumeration entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
        		if (entry.getName().endsWith("plugins.config"))
					return jarFile.getInputStream(entry);
			}
		}
    	catch (Exception e) {}
		return autoGenerateConfigFile(jar);
	}
	
    /** Creates a configuration file for JAR/ZIP files that do not have one. */
	InputStream autoGenerateConfigFile(String jar) {
		StringBuffer sb = null;
		try {
			ZipFile jarFile = new ZipFile(jar);
			Enumeration entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				String name = entry.getName();
				if (name.endsWith(".class") && name.indexOf("_")>0 && name.indexOf("$")==-1
				&& name.indexOf("/_")==-1 && !name.startsWith("_")) {
					if (sb==null) sb = new StringBuffer();
					String className = name.substring(0, name.length()-6);
					int slashIndex = className.lastIndexOf('/');
					String plugins = "Plugins";
					if (slashIndex >= 0) {
						plugins += ">" + className.substring(0, slashIndex).replace('/', '>').replace('_', ' ');
						name = className.substring(slashIndex + 1);
					} else
						name = className;
					name = name.replace('_', ' ');
					className = className.replace('/', '.');
					sb.append(plugins + ", \""+name+"\", "+className+"\n");
				}
			}
		}
    	catch (Exception e) {}
		//IJ.log(""+(sb!=null?sb.toString():"null"));
		if (sb==null)
			return null;
		else
    		return new ByteArrayInputStream(sb.toString().getBytes());
	}

	/** Returns a list of the plugins with directory names removed. */
	String[] getStrippedPlugins(String[] plugins) {
		String[] plugins2 = new String[plugins.length];
		int slashPos;
		for (int i=0; i<plugins2.length; i++) {
			plugins2[i] = plugins[i];
			slashPos = plugins2[i].lastIndexOf('/');
			if (slashPos>=0)
				plugins2[i] = plugins[i].substring(slashPos+1,plugins2[i].length());
		}
		return plugins2;
	}

	/** Returns a list of the plugins in the plugins menu. */
	public static synchronized String[] getPlugins() {
		String homeDir = Prefs.getHomeDir();
		if (homeDir==null)
			return null;
		if (homeDir.endsWith("plugins"))
			pluginsPath = homeDir+Prefs.separator;
		else {
			String property = System.getProperty("plugins.dir");
			if (property!=null && (property.endsWith("/")||property.endsWith("\\")))
				property = property.substring(0, property.length()-1);
			String pluginsDir = property;
			if (pluginsDir==null)
				pluginsDir = homeDir;
			else if (pluginsDir.equals("user.home")) {
				pluginsDir = System.getProperty("user.home");
				if (!(new File(pluginsDir+Prefs.separator+"plugins")).isDirectory())
					pluginsDir = pluginsDir + Prefs.separator + "ImageJ";
				property = null;
				// needed to run plugins when ImageJ launched using Java WebStart
				if (applet==null) System.setSecurityManager(null);
				jnlp = true;
			}
			pluginsPath = pluginsDir+Prefs.separator+"plugins"+Prefs.separator;
			if (property!=null&&!(new File(pluginsPath)).isDirectory())
				pluginsPath = pluginsDir + Prefs.separator;
			macrosPath = pluginsDir+Prefs.separator+"macros"+Prefs.separator;
		}
		File f = macrosPath!=null?new File(macrosPath):null;
		if (f!=null && !f.isDirectory())
			macrosPath = null;

		pluginsPaths = Tools.splitPathList(pluginsPath);
		Vector v = new Vector();
		for (int i = 0; i < pluginsPaths.length; i++)
			getPlugins(v, pluginsPaths[i]);
		if (v.size() == 0)
			return null;

		String[] list = new String[v.size()];
		v.copyInto((String[])list);
		StringSorter.sort(list);
		return list;
	}

	public static void getPlugins(Vector v, String pluginsPath) {
		File f = pluginsPath!=null?new File(pluginsPath):null;
		if (f==null || (f!=null && !f.isDirectory())) {
			//error = "Plugins folder not found at "+pluginsPath;
			pluginsPath = null;
			return;
		}
		String[] list = f.list();
		if (list==null)
			return;
		jarFiles = null;
		macroFiles = null;
		for (int i=0; i<list.length; i++) {
			String name = list[i];
			boolean isClassFile = name.endsWith(".class");
			boolean hasUnderscore = name.indexOf('_')>=0;
			if (hasUnderscore && isClassFile && name.indexOf('$')<0 ) {
				name = name.substring(0, name.length()-6); // remove ".class"
				v.addElement(name);
			} else if (hasUnderscore && (name.endsWith(".jar") || name.endsWith(".zip"))) {
				if (jarFiles==null) jarFiles = new Vector();
				jarFiles.addElement(pluginsPath + name);
			} else if (hasUnderscore && (name.endsWith(".txt")||name.endsWith(".ijm"))) {
				if (macroFiles==null) macroFiles = new Vector();
				macroFiles.addElement(name);
			} else {
				if (!isClassFile)
					checkSubdirectory(pluginsPath, name, v);
			}
		}
	}
	
	/** Looks for plugins and jar files in a subdirectory of the plugins directory. */
	static void checkSubdirectory(String path, String dir, Vector v) {
		if (dir.endsWith(".java"))
			return;
		File f = new File(path, dir);
		if (!f.isDirectory())
			return;
		String[] list = f.list();
		if (list==null)
			return;
		dir += "/";
		for (int i=0; i<list.length; i++) {
			String name = list[i];
			boolean hasUnderscore = name.indexOf('_')>=0;
			if (hasUnderscore && name.endsWith(".class") && name.indexOf('$')<0) {
				name = name.substring(0, name.length()-6); // remove ".class"
				v.addElement(dir+name);
				//IJ.write("File: "+f+"/"+name);
			} else if (hasUnderscore && (name.endsWith(".jar") || name.endsWith(".zip"))) {
				if (jarFiles==null) jarFiles = new Vector();
				jarFiles.addElement(f.getPath() + File.separator + name);
			} else if (hasUnderscore && (name.endsWith(".txt")||name.endsWith(".ijm"))) {
				if (macroFiles==null) macroFiles = new Vector();
				macroFiles.addElement(dir + name);
			}
		}
	}
	
	static String submenuName;

	/** Installs a plugin in the Plugins menu using the class name,
		with underscores replaced by spaces, as the command. */
	void installUserPlugin(String className) {
		installUserPlugin(className, false);
	}

	public static void installUserPlugin(String className, boolean force) {
		int slashIndex = className.indexOf('/');
		String command = className;
		if (slashIndex>0) {
			String dir = className.substring(0, slashIndex);
			command = className.substring(slashIndex+1, className.length());
			//className = className.replace('/', '.');
		//IJ.write(dir + "  " + className);
		}
		command = command.replace('_',' ');

		command.trim();
		boolean itemExists = (pluginsTable.get(command)!=null);
		if(force && itemExists)
			return;

		if (!force && itemExists)  // duplicate command?
			command = command + " Plugin";
		pluginsTable.put(command, className.replace('/', '.'));
		nPlugins++;
	}

	void installPopupMenu(ImageJ ij) {
	}
	
	public static MenuBar getMenuBar() {
		return mbar;
	}
		
	public static Menu getMacrosMenu() {
		return macrosMenu;
	}
		
	static final int RGB_STACK=10, HSB_STACK=11;
	
	/** Updates the Image/Type and Window menus. */
	public static void updateMenus() {
		IjxImagePlus imp = WindowManager.getCurrentImage();
		if (imp==null)
			return;
    	int type = imp.getType();
     	if (imp.getStackSize()>1) {
    		IjxImageStack stack = imp.getStack();
    		if (stack.isRGB()) type = RGB_STACK;
    		else if (stack.isHSB()) type = HSB_STACK;
    	}
    	if (type==IjxImagePlus.GRAY8) {
			ImageProcessor ip = imp.getProcessor();
    		if (ip!=null && ip.getMinThreshold()==ImageProcessor.NO_THRESHOLD && ip.isColorLut()) {
    			type = IjxImagePlus.COLOR_256;
	    		if (!ip.isPseudoColorLut())
	    			imp.setType(IjxImagePlus.COLOR_256);
    		}
    	}

	}
	
	static boolean isColorLut(IjxImagePlus imp) {
		ImageProcessor ip = imp.getProcessor();
    	IndexColorModel cm = (IndexColorModel)ip.getColorModel();
    	if (cm==null) return false;
		int mapSize = cm.getMapSize();
		byte[] reds = new byte[mapSize];
		byte[] greens = new byte[mapSize];
		byte[] blues = new byte[mapSize];	
		cm.getReds(reds); 
		cm.getGreens(greens); 
		cm.getBlues(blues);
		boolean isColor = false;
		for (int i=0; i<mapSize; i++) {
			if ((reds[i] != greens[i]) || (greens[i] != blues[i])) {
				isColor = true;
				break;
			}
		}
		return isColor;
	}

	
	/** Returns the path to the user plugins directory or
		null if the plugins directory was not found. */
	public static String getPlugInsPath() {
		return pluginsPath;
	}

	/** Returns the path to the macros directory or
		null if the macros directory was not found. */
	public static String getMacrosPath() {
		return macrosPath;
	}
        
	/** Returns the hashtable that associates commands with plugins. */
	public static Hashtable getCommands() {
		return pluginsTable;
	}
        
	/** Returns the hashtable that associates shortcuts with commands. The keys
		in the hashtable are Integer keycodes, or keycode+200 for uppercase. */
	public static Hashtable getShortcuts() {
		return shortcuts;
	}
        
	/** Returns the hashtable that associates keyboard shortcuts with macros. The keys
		in the hashtable are Integer keycodes, or keycode+200 for uppercase. */
	public static Hashtable getMacroShortcuts() {
		if (macroShortcuts==null) macroShortcuts = new Hashtable();
		return macroShortcuts;
	}
        
	/** Returns the hashtable that associates menu names with menus. */
	//public static Hashtable getMenus() {
	//	return menusTable;
	//}

	/** Inserts one item (a non-image window) into the Window menu. */
	static synchronized void insertWindowMenuItem(Frame win) {
		if (ij==null || win==null)
			return;
		CheckboxMenuItem item = new CheckboxMenuItem(win.getTitle());
		item.addItemListener(ij);
		int index = WINDOW_MENU_ITEMS+windowMenuItems2;
		if (windowMenuItems2>=2)
			index--;
		window.insert(item, index);
		windowMenuItems2++;
		if (windowMenuItems2==1) {
			window.insertSeparator(WINDOW_MENU_ITEMS+windowMenuItems2);
			windowMenuItems2++;
		}
		//IJ.write("insertWindowMenuItem: "+windowMenuItems2);
	}

	/** Adds one image to the end of the Window menu. */
	static synchronized void addWindowMenuItem(IjxImagePlus imp) {
		//IJ.log("addWindowMenuItem: "+imp);
		if (ij==null) return;
		String name = imp.getTitle();
		int size = (imp.getWidth()*imp.getHeight()*imp.getStackSize())/1024;
		switch (imp.getType()) {
			case IjxImagePlus.GRAY32: case IjxImagePlus.COLOR_RGB: // 32-bit
				size *=4;
				break;
			case IjxImagePlus.GRAY16:  // 16-bit
				size *= 2;
				break;
			default: // 8-bit
				;
		}
		CheckboxMenuItem item = new CheckboxMenuItem(name + " " + size + "K");
		window.add(item);
		item.addItemListener(ij);
	}
	
	/** Removes the specified item from the Window menu. */
	static synchronized void removeWindowMenuItem(int index) {
		//IJ.log("removeWindowMenuItem: "+index+" "+windowMenuItems2+" "+window.getItemCount());
		if (ij==null)
			return;
		if (index>=0 && index<window.getItemCount()) {
			window.remove(WINDOW_MENU_ITEMS+index);
			if (index<windowMenuItems2) {
				windowMenuItems2--;
				if (windowMenuItems2==1) {
					window.remove(WINDOW_MENU_ITEMS);
					windowMenuItems2 = 0;
				}
			}
		}
	}

	/** Changes the name of an item in the Window menu. */
	public static synchronized void updateWindowMenuItem(String oldLabel, String newLabel) {
		if (oldLabel.equals(newLabel))
			return;
		int first = WINDOW_MENU_ITEMS;
		int last = window.getItemCount()-1;
		//IJ.write("updateWindowMenuItem: "+" "+first+" "+last+" "+oldLabel+" "+newLabel);
		try {  // workaround for Linux/Java 5.0/bug
			for (int i=first; i<=last; i++) {
				MenuItem item = window.getItem(i);
				//IJ.write(i+" "+item.getLabel()+" "+newLabel);
				String label = item.getLabel();
				if (item!=null && label.startsWith(oldLabel)) {
					if (label.endsWith("K")) {
						int index = label.lastIndexOf(' ');
						if (index>-1)
							newLabel += label.substring(index, label.length());
					}
					item.setLabel(newLabel);
					return;
				}
			}
		} catch (NullPointerException e) {}
	}
	
	/** Adds a file path to the beginning of the File/Open Recent submenu. */
	public static synchronized void addOpenRecentItem(String path) {
		if (ij==null) return;
		int count = openRecentMenu.getItemCount();
		if (count>0 && openRecentMenu.getItem(0).getLabel().equals(path))
			return;
		if (count==MAX_OPEN_RECENT_ITEMS)
			openRecentMenu.remove(MAX_OPEN_RECENT_ITEMS-1);
		MenuItem item = new MenuItem(path);
		openRecentMenu.insert(item, 0);
		item.addActionListener(ij);
	}

	public static PopupMenu getPopupMenu() {
		return popup;
	}
	
	public static Menu getSaveAsMenu() {
		return saveAsMenu;
	}
	
	/** Adds a plugin based command to the end of a specified menu.
	* @param plugin			the plugin (e.g. "Inverter_", "Inverter_("arg")")
	* @param menuCode		PLUGINS_MENU, IMPORT_MENU, SAVE_AS_MENU or HOT_KEYS
	* @param command		the menu item label (set to "" to uninstall)
	* @param shortcut		the keyboard shortcut (e.g. "y", "Y", "F1")
	* @param ij				ImageJ (the action listener)
	*
	* @return				returns an error code(NORMAL_RETURN,COMMAND_IN_USE_ERROR, etc.)
	*/
	public static int installPlugin(String plugin, char menuCode, String command, String shortcut, ImageJ ij) {
		if (command.equals("")) { //uninstall
			//Object o = pluginsPrefs.remove(plugin);
			//if (o==null)
			//	return NOT_INSTALLED;
			//else
				return NORMAL_RETURN;
		}
		
		if (commandInUse(command))
			return COMMAND_IN_USE;
		if (!validShortcut(shortcut))
			return INVALID_SHORTCUT;
		if (shortcutInUse(shortcut))
			return SHORTCUT_IN_USE;
			
		Menu menu;
		switch (menuCode) {
			case PLUGINS_MENU: menu = pluginsMenu; break;
			case IMPORT_MENU: menu = importMenu; break;
			case SAVE_AS_MENU: menu = saveAsMenu; break;
			case SHORTCUTS_MENU: menu = shortcutsMenu; break;
			case ABOUT_MENU: menu = aboutMenu; break;
			case FILTERS_MENU: menu = filtersMenu; break;
			case TOOLS_MENU: menu = toolsMenu; break;
			case UTILITIES_MENU: menu = utilitiesMenu; break;
			default: return 0;
		}
		int code = convertShortcutToCode(shortcut);
		MenuItem item;
		boolean functionKey = code>=KeyEvent.VK_F1 && code<=KeyEvent.VK_F12;
		if (code==0)
			item = new MenuItem(command);
		else if (functionKey) {
			command += " [F"+(code-KeyEvent.VK_F1+1)+"]";
			shortcuts.put(new Integer(code),command);
			item = new MenuItem(command);
		}else {
			shortcuts.put(new Integer(code),command);
			int keyCode = code;
			boolean shift = false;
			if (keyCode>=265 && keyCode<=290) {
				keyCode -= 200;
				shift = true;
			}
			item = new MenuItem(command, new MenuShortcut(keyCode, shift));
		}
		menu.add(item);
		item.addActionListener(ij);
		pluginsTable.put(command, plugin);
		shortcut = code>0 && !functionKey?"["+shortcut+"]":"";
		//IJ.write("installPlugin: "+menuCode+",\""+command+shortcut+"\","+plugin);
		pluginsPrefs.addElement(menuCode+",\""+command+shortcut+"\","+plugin);
		return NORMAL_RETURN;
	}
	
	/** Deletes a command installed by installPlugin. */
	public static int uninstallPlugin(String command) {
		boolean found = false;
		for (Enumeration en=pluginsPrefs.elements(); en.hasMoreElements();) {
			String cmd = (String)en.nextElement();
			if (cmd.indexOf(command)>0) {
				pluginsPrefs.removeElement((Object)cmd);
				found = true;
				break;
			}
		}
		if (found)
			return NORMAL_RETURN;
		else
			return COMMAND_NOT_FOUND;

	}
	
	public static boolean commandInUse(String command) {
		if (pluginsTable.get(command)!=null)
			return true;
		else
			return false;
	}

	public static int convertShortcutToCode(String shortcut) {
		int code = 0;
		int len = shortcut.length();
		if (len==2 && shortcut.charAt(0)=='F') {
			code = KeyEvent.VK_F1+(int)shortcut.charAt(1)-49;
			if (code>=KeyEvent.VK_F1 && code<=KeyEvent.VK_F9)
				return code;
			else
				return 0;
		}
		if (len==3 && shortcut.charAt(0)=='F') {
			code = KeyEvent.VK_F10+(int)shortcut.charAt(2)-48;
			if (code>=KeyEvent.VK_F10 && code<=KeyEvent.VK_F12)
				return code;
			else
				return 0;
		}
		if (len==2 && shortcut.charAt(0)=='N') { // numeric keypad
			code = KeyEvent.VK_NUMPAD0+(int)shortcut.charAt(1)-48;
			if (code>=KeyEvent.VK_NUMPAD0 && code<=KeyEvent.VK_NUMPAD9)
				return code;
			switch (shortcut.charAt(1)) {
				case '/': return KeyEvent.VK_DIVIDE;
				case '*': return KeyEvent.VK_MULTIPLY;
				case '-': return KeyEvent.VK_SUBTRACT;
				case '+': return KeyEvent.VK_ADD;
				case '.': return KeyEvent.VK_DECIMAL;
				default: return 0;
			}
		}
		if (len!=1)
			return 0;
		int c = (int)shortcut.charAt(0);
		if (c>=65&&c<=90) //A-Z
			code = KeyEvent.VK_A+c-65 + 200;
		else if (c>=97&&c<=122) //a-z
			code = KeyEvent.VK_A+c-97;
		else if (c>=48&&c<=57) //0-9
			code = KeyEvent.VK_0+c-48;
		else {
			switch (c) {
				case 43: code = KeyEvent.VK_PLUS; break;
				case 45: code = KeyEvent.VK_MINUS; break;
				//case 92: code = KeyEvent.VK_BACK_SLASH; break;
				default: return 0;
			}
		}
		return code;
	}
	
	void installStartupMacroSet() {
		if (applet!=null) {
			String docBase = ""+applet.getDocumentBase();
			if (!docBase.endsWith("/")) {
				int index = docBase.lastIndexOf("/");
				if (index!=-1)
					docBase = docBase.substring(0, index+1);
			}
			IJ.runPlugIn("ij.plugin.URLOpener", docBase+"StartupMacros.txt");
			return;
		}
		if (macrosPath==null) {
			(new MacroInstaller()).installFromIJJar("/macros/StartupMacros.txt");
			return;
		}
		String path = macrosPath + "StartupMacros.txt";
		File f = new File(path);
		if (!f.exists()) {
			path = macrosPath + "StartupMacros.ijm";
			f = new File(path);
			if (!f.exists()) {
				(new MacroInstaller()).installFromIJJar("/macros/StartupMacros.txt");
				return;
			}
		}
		String libraryPath = macrosPath + "Library.txt";
		f = new File(libraryPath);
		boolean isLibrary = f.exists();
		try {
			MacroInstaller mi = new MacroInstaller();
			if (isLibrary) mi.installLibrary(libraryPath);
			mi.installFile(path);
			nMacros += mi.getMacroCount();
		} catch (Exception e) {}
	}
	
	static boolean validShortcut(String shortcut) {
		int len = shortcut.length();
		if (shortcut.equals(""))
			return true;
		else if (len==1)
			return true;
		else if (shortcut.startsWith("F") && (len==2 || len==3))
			return true;
		else
			return false;
	}

	public static boolean shortcutInUse(String shortcut) {
		int code = convertShortcutToCode(shortcut);
		if (shortcuts.get(new Integer(code))!=null)
			return true;
		else
			return false;
	}
	
	/** Set the size (in points) used for the fonts in ImageJ menus. 
		Set the size to 0 to use the Java default size. */
	public static void setFontSize(int size) {
		if (size<9 && size!=0) size = 9;
		if (size>24) size = 24;
		fontSize = size;
	}
	
	/** Returns the size (in points) used for the fonts in ImageJ menus. Returns
		0 if the default font size is being used or if this is a Macintosh. */
	public static int getFontSize() {
		return IJ.isMacintosh()?0:fontSize;
	}
	
	public static Font getFont() {
		if (menuFont==null)
			menuFont =  new Font("SanSerif", Font.PLAIN, fontSize==0?12:fontSize);
		return menuFont;
	}

	/** Called once when ImageJ quits. */
	public static void savePreferences(Properties prefs) {
		int index = 0;
		for (Enumeration en=pluginsPrefs.elements(); en.hasMoreElements();) {
			String key = "plugin" + (index/10)%10 + index%10;
			String value = (String)en.nextElement();
			prefs.put(key, value);
			index++;
		}
		int n = openRecentMenu.getItemCount();
		for (int i=0; i<n; i++) {
			String key = ""+i;
			if (key.length()==1) key = "0"+key;
			key = "recent"+key;
			prefs.put(key, openRecentMenu.getItem(i).getLabel());
		}
		prefs.put(Prefs.MENU_SIZE, Integer.toString(fontSize));
	}

}
