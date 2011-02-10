package ijx.plugin.readwrite;
import ijx.WindowManager;
import ijx.IJ;

import ijx.plugin.api.PlugIn;
import ijx.text.*;
import ijx.plugin.frame.Editor;
import ijx.gui.IjxWindow;

/** This plugin implements the File/Save As/Text command. What it does
	is save the contents of TextWindows (e.g., "Log" and "Results"). */
public class TextWriter implements PlugIn {
    
	public void run(String arg) {
		saveText();
	}
	
	void saveText() {
		IjxWindow frame = WindowManager.getFrontWindow();
		if (frame!=null && (frame instanceof TextWindow)) {
			TextPanel tp = ((TextWindow)frame).getTextPanel();
			tp.saveAs("");
		} else if (frame!=null && (frame instanceof Editor)) {
			Editor ed = (Editor)frame;
			ed.saveAs();
		} else {
			IJ.error("Save As Text",
				"This command requires a TextWindow, such\n"
				+ "as the \"Log\" window, or an Editor window. Use\n"
				+ "File>Save>Text Image to save an image as text.");
		}
	}
	
}

