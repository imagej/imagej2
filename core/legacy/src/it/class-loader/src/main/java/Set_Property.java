

import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

public class Set_Property implements PlugIn {
	@Override
	public void run(final String arg) {
		final GenericDialog gd = new GenericDialog("Set Property");
		gd.addStringField("key", "hello");
		gd.addStringField("value", "world");
		gd.showDialog();
		if (gd.wasCanceled()) return;

		final String key = gd.getNextString();
		final String value = gd.getNextString();
		System.setProperty(key, value);
	}
}
