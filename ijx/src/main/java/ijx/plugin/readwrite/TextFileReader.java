package ijx.plugin.readwrite;

//import java.awt.*;
//import java.awt.image.*;
//import java.io.*;

import ijx.plugin.api.PlugIn;
//
//


/** This plugin displays the contents of a text file in a window. */
public class TextFileReader implements PlugIn {
	
	public void run(String arg) {
		new ijx.text.TextWindow(arg,400,450);
	}

}
