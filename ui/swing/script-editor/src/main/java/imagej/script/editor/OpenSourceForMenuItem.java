package fiji.scripting;

import common.RefreshScripts;

import fiji.IJ_Alt_Key_Listener;

import fiji.util.MenuItemDiverter;

import ij.IJ;
import ij.Menus;

import ij.plugin.BrowserLauncher;

import javax.swing.JOptionPane;

public class OpenSourceForMenuItem extends MenuItemDiverter {
	@Override
	public void setActions() {
		IJ.getInstance().toFront();
		super.setActions();
		try {
			IJ_Alt_Key_Listener.getOpener().run();
		} catch (NullPointerException e) {
			/* no menu opener available */
		}
	}

	@Override
	protected String getTitle() {
		return "Menu Item Source";
	}

	@Override
	protected void action(String arg) {
		String action = (String)Menus.getCommands().get(arg);
		if (action != null) try {
			IJ.showStatus("Opening source for " + arg);
			String path = getScriptPath(action);
			if (path == null) {
				int paren = action.indexOf('(');
				if (paren > 0)
					action = action.substring(0, paren);
				path = new FileFunctions(Script_Editor.getInstance()).getSourcePath(action);
			}
			if (path != null) {
				new Script_Editor().run(path);
				return;
			}
			else {
				String url = new FileFunctions(Script_Editor.getInstance()).getSourceURL(action);
				new BrowserLauncher().run(url);
				return;
			}
		} catch (Exception e) { e.printStackTrace(); /* fallthru */ }
		error("Could not get source for '" + arg + "'");
	}

	protected String getScriptPath(String action) {
		int paren = action.indexOf("(\"");
		if (paren < 0 || !action.endsWith("\")"))
			return null;
		try {
			if (RefreshScripts.class.isAssignableFrom(getClass().getClassLoader().loadClass(action.substring(0, paren))))
				return action.substring(paren + 2, action.length() - 2);
		} catch (Exception e) { /* ignore */ }
		return null;
	}

	protected void error(String message) {
		JOptionPane.showMessageDialog(null, message);
	}
}