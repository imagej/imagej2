package ijx.plugin.readwrite;

//import java.awt.*;
//import java.awt.image.*;
//import java.io.*;
import ij.*;
import ijx.plugin.api.PlugIn;
//import ij.io.*;
//


/** This plugin displays the contents of a text file in a window. */
public class TextFileReader implements PlugIn {
	
	public void run(String arg) {
		new ijx.text.TextWindow(arg,400,450);
	}

}
