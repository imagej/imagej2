package fiji.scripting;

import ij.io.OpenDialog;

import ij.plugin.PlugIn;

public class ShowPluginChanges implements PlugIn {
	public void run(String arg) {
		FileFunctions fileFunctions = new FileFunctions(null);
		if (arg == null || "".equals(arg)) {
			OpenDialog dialog = new OpenDialog("Which Fiji component",
				fileFunctions.ijDir + "plugins", "");
			if (dialog.getDirectory() == null)
				return;
			arg = dialog.getDirectory() + dialog.getFileName();
		}
		if (arg.startsWith(fileFunctions.ijDir))
			arg = arg.substring(fileFunctions.ijDir.length());
		fileFunctions.showPluginChangesSinceUpload(arg);
	}
}