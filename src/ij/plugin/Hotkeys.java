package ij.plugin;
import ij.*;
import ij.gui.*;
import ij.util.*;
import java.awt.*;
import java.io.*;
import java.util.*;

/** Implements the Plugins/Hotkeys/Install and Remove commands. */
public class Hotkeys implements PlugIn {

	private static final String TITLE = "Hotkeys";
	private static String command = "";
	private static String shortcut = "";

	public void run(String arg) {
		if (arg.equals("install"))
			installHotkey();
		else if (arg.equals("remove"))
			removeHotkey();
		else {
			Executer e = new Executer(arg);
			e.run();
		}
		IJ.register(Hotkeys.class);
	}

	void installHotkey() {
		String[] commands = getAllCommands();
		GenericDialog gd = new GenericDialog("Create Shortcut");
		gd.addChoice("Command:", commands, command);
		gd.addStringField("Shortcut:", shortcut, 3);
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		command = gd.getNextChoice();
		shortcut = gd.getNextString();
		if (shortcut.equals("")) {
			IJ.showMessage(TITLE, "Shortcut required");
			return;
		}
		if (shortcut.length()>1)
			shortcut = shortcut.replace('f','F');
		String plugin = "ij.plugin.Hotkeys("+"\""+command+"\")";
		int err = Menus.installPlugin(plugin,Menus.SHORTCUTS_MENU,"*"+command,shortcut,IJ.getInstance());
		switch (err) {
			case Menus.INVALID_SHORTCUT:
				IJ.showMessage(TITLE, "The shortcut must be a single character or F1-F12.");
				break;
			case Menus.SHORTCUT_IN_USE:
				IJ.showMessage("The \""+shortcut+"\" shortcut is already being used.");
				break;
			default:
				shortcut = "";
				break;
		}
	}
	
	void removeHotkey() {
		String[] commands = getInstalledCommands();
		if (commands==null) {
			IJ.showMessage("Remove...", "No installed commands found.");
			return;
		}
		GenericDialog gd = new GenericDialog("Remove");
		gd.addChoice("Command:", commands, "");
		gd.addMessage("The command is not removed\nuntil ImageJ is restarted.");
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		command = gd.getNextChoice();
		int err = Menus.uninstallPlugin(command);
		boolean removed = true;
		if(err==Menus.COMMAND_NOT_FOUND)
			removed = deletePlugin(command);
		if (removed) {
			IJ.showStatus("\""+command + "\" removed; ImageJ restart required");
		} else
			IJ.showStatus("\""+command + "\" not removed");
	}

	boolean deletePlugin(String command) {
		String plugin = (String)Menus.getCommands().get(command);
		String name = plugin+".class";
		File file = new File(Menus.getPlugInsPath(), name);
		if (file==null || !file.exists())
			return false;
		else
			return IJ.showMessageWithCancel("Delete Plugin?", "Permanently delete \""+name+"\"?");
	}
	
	String[] getAllCommands() {
		Vector v = new Vector();
		for (Enumeration en=Menus.getCommands().keys(); en.hasMoreElements();) {
			String cmd = (String)en.nextElement();
			if (!cmd.startsWith("*"))
				v.addElement(cmd);
		}
		String[] list = new String[v.size()];
		v.copyInto((String[])list);
		StringSorter.sort(list);
		return list;
	}
	
	String[] getInstalledCommands() {
		Vector v = new Vector();
		Hashtable commandTable = Menus.getCommands();
		for (Enumeration en=commandTable.keys(); en.hasMoreElements();) {
			String cmd = (String)en.nextElement();
			if (cmd.startsWith("*"))
				v.addElement(cmd);
			else {
				String plugin = (String)commandTable.get(cmd);
				if (plugin.indexOf("_")>=0 && !plugin.startsWith("ij."))
					v.addElement(cmd);
 			}
		}
		if (v.size()==0)
			return null;
		String[] list = new String[v.size()];
		v.copyInto((String[])list);
		StringSorter.sort(list);
		return list;
	}
	
}
