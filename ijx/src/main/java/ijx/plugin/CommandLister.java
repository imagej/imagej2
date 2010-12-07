package ijx.plugin;
import ijx.plugin.api.PlugIn;
import ijx.util.StringSorter;
import ijx.Menus;
import ijx.text.*;
import java.util.*;
import java.awt.event.*;

/** Lists ImageJ commands or keyboard shortcuts in a text window. */
public class CommandLister implements PlugIn {

	public void run(String arg) {
		if (arg.equals("shortcuts"))
			listShortcuts();
		else
			listCommands();
	}
	
	public void listCommands() {
		Hashtable commands = Menus.getCommands();
		Vector v = new Vector();
		for (Enumeration en=commands.keys(); en.hasMoreElements();) {
			String command = (String)en.nextElement();
			v.addElement(command+"\t"+(String)commands.get(command));
		}
		showList("Commands", "Command\tPlugin", v);

        String[]  pluginsArray = Menus.getPlugins();
		Vector v2 = new Vector();
        for (int i = 0; i < pluginsArray.length; i++) {
            String string = pluginsArray[i];
			v2.addElement(string);
		}
		showList("Plugin", "nothing", v2);


	}

	public void listShortcuts() {
		Hashtable shortcuts = Menus.getShortcuts();
		Vector v = new Vector();
		addShortcutsToVector(shortcuts, v);
		Hashtable macroShortcuts = Menus.getMacroShortcuts();
		addShortcutsToVector(macroShortcuts, v);
		showList("Keyboard Shortcuts", "Hot Key\tCommand", v);
	}
	
	void addShortcutsToVector(Hashtable shortcuts, Vector v) {
		for (Enumeration en=shortcuts.keys(); en.hasMoreElements();) {
			Integer key = (Integer)en.nextElement();
			int keyCode = key.intValue();
			boolean upperCase = false;
			if (keyCode>=200+65 && keyCode<=200+90) {
				upperCase = true;
				keyCode -= 200;
			}
			String shortcut = KeyEvent.getKeyText(keyCode);
			if (!upperCase && shortcut.length()==1) {
				char c = shortcut.charAt(0);
				if (c>=65 && c<=90)
					c += 32;
				char[] chars = new char[1];
				chars[0] = c;
				shortcut = new String(chars);
			}
			if (shortcut.length()>1)
				shortcut = " " + shortcut; 
			v.addElement(shortcut+"\t"+(String)shortcuts.get(key));
		}
	}

	void showList(String title, String headings, Vector v) {
		String[] list = new String[v.size()];
		v.copyInto((String[])list);
		StringSorter.sort(list);
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<list.length; i++) {
			sb.append(list[i]);
			sb.append("\n");
		}
		TextWindow tw = new TextWindow(title, headings, sb.toString(), 600, 500);
	}
}
