package ijx.plugin;
import ijx.plugin.api.PlugIn;
import ijx.gui.dialog.GenericDialog;
import imagej.util.StringSorter;
import ijx.io.PluginClassLoader;
import ijx.Menus;
import ijx.IJ;
import ijx.IjxMenus;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;

/** Implements the Plugins/Shortcuts/Install... command.
Finds plugin in the plugins dir and any sub-directories (only one-level down)
 */
public class Installer implements PlugIn {

	private static String[] menus = {"Shortcuts", "Plugins", "Import", "Save As",
		"Filters", "Tools", "Utilities"};
	private static final String TITLE = "Installer";
	private static String command = "";
	private static String shortcut = "";
	private static String defaultPlugin = "";
	private static String menuStr = menus[0];

	public void run(String arg) {
		installPlugin();
	}

	void installPlugin() {
		String[] plugins = getPlugins();
		if (plugins==null || plugins.length==0) {
			IJ.error("No plugins found");
			return;
		}
		GenericDialog gd = new GenericDialog("Install Plugin", IJ.getTopComponentFrame());
		gd.addChoice("Plugin:", plugins, defaultPlugin);
		gd.addChoice("Menu:", menus, menuStr);
		gd.addStringField("Command:", command, 16);
		gd.addStringField("Shortcut:", shortcut, 3);
		gd.addStringField("Argument:", "", 12);
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		String plugin = gd.getNextChoice();
		menuStr = gd.getNextChoice();
		command = gd.getNextString();
		shortcut = gd.getNextString();
		String argument = gd.getNextString();
		IJ.register(Installer.class);
		defaultPlugin = plugin;
		if (command.equals("")) {
			IJ.showMessage(TITLE, "Command required");
			return;
		}
		if (shortcut.length()>1)
			shortcut = shortcut.replace('f','F');
		char menu = ' ';
		if (menuStr.equals(menus[0]))
			menu = IjxMenus.SHORTCUTS_MENU;
		else if (menuStr.equals(menus[1]))
			menu = IjxMenus.PLUGINS_MENU;
		else if (menuStr.equals(menus[2]))
			menu = IjxMenus.IMPORT_MENU;
		else if (menuStr.equals(menus[3]))
			menu = IjxMenus.SAVE_AS_MENU;
		else if (menuStr.equals(menus[4]))
			menu = IjxMenus.FILTERS_MENU;
		else if (menuStr.equals(menus[5]))
			menu = IjxMenus.TOOLS_MENU;
		else if (menuStr.equals(menus[6]))
			menu = IjxMenus.UTILITIES_MENU;
		if (!argument.equals(""))
			plugin += "(\"" + argument +"\")";
		int err = Menus.installPlugin(plugin,menu,command,shortcut,IJ.getInstance());
		switch (err) {
			case IjxMenus.COMMAND_IN_USE:
				IJ.showMessage(TITLE, "The command \""+command+"\" \nis already being used.");
				return;
			case IjxMenus.INVALID_SHORTCUT:
				IJ.showMessage(TITLE, "The shortcut must be a single character or \"F1\"-\"F12\".");
				return;
			case IjxMenus.SHORTCUT_IN_USE:
				IJ.showMessage("The \""+shortcut+"\" shortcut is already being used.");
				return;
			default:
				command = "";
				shortcut = "";
				break;
		}
		if (!plugin.endsWith(")"))
			installAbout(plugin);
	}
	
	void installAbout(String plugin) {
		boolean hasShowAboutMethod=false;
		PluginClassLoader loader = new PluginClassLoader(Menus.getPlugInsPath());
		try {
			Class c = loader.loadClass(plugin);
			Method m = c.getDeclaredMethod("showAbout", new Class[0]);
			if (m!=null)
				hasShowAboutMethod = true;
		}
		catch (Exception e) {}
		//IJ.write("installAbout: "+plugin+" "+hasShowAboutMethod);
		if (hasShowAboutMethod)
			Menus.installPlugin(plugin+"(\"about\")",IjxMenus.ABOUT_MENU,plugin+"...","",IJ.getInstance());
	}
	
	String[] getPlugins() {
		String path = Menus.getPlugInsPath();
		if (path==null)
			return null;
		File f = new File(path);
		String[] list = f.list();
		if (list==null) return null;
		Vector v = new Vector();
		for (int i=0; i<list.length; i++) {
			String className = list[i];
			boolean isClassFile = className.endsWith(".class");
			if (className.indexOf('_')>=0 && isClassFile && className.indexOf('$')<0 ) {
				className = className.substring(0, className.length()-6); 
				v.addElement(className);
			} else {
				if (!isClassFile)
					getSubdirectoryPlugins(path, className, v);
			}
		}
		list = new String[v.size()];
		v.copyInto((String[])list);
		StringSorter.sort(list);
		return list;
	}
	
	/** Looks for plugins in a subdirectorie of the plugins directory. */
	void getSubdirectoryPlugins(String path, String dir, Vector v) {
		//IJ.write("getSubdirectoryPlugins: "+path+dir);
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
			if (name.indexOf('_')>=0 && name.endsWith(".class") && name.indexOf('$')<0 ) {
				name = name.substring(0, name.length()-6); // remove ".class"
				v.addElement(name);
				//IJ.write("File: "+f+"/"+name);
			}
		}
	}

}
