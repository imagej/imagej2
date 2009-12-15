package ij.plugin;

import java.awt.*;
import java.io.*;
import ij.*;
import ij.io.*;

/** This plugin implements the File/Import/Raw command. */
public class Raw implements PlugIn {

	private static String defaultDirectory = null;

	public void run(String arg) {
		OpenDialog od = new OpenDialog("Open Raw...", arg);
		String directory = od.getDirectory();
		String fileName = od.getFileName();
		if (fileName==null)
			return;
		ImportDialog d = new ImportDialog(fileName, directory);
		d.openImage();
	}
	
}
