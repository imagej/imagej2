package ij.plugin;
import ijx.gui.IjxWindow;
import ijx.IjxApplication;
import ijx.IjxImagePlus;
import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.io.*;
import ij.plugin.frame.*;
import ij.text.TextWindow;
import ij.macro.Interpreter;
import java.awt.Frame;
import java.io.File;
import java.applet.Applet;
	
/**	Runs File and Window menu commands. */
public class Commands implements PlugIn {
	
	public void run(String cmd) {
		if (cmd.equals("new"))
			new NewImage();
		else if (cmd.equals("open")) {
			if (Prefs.useJFileChooser && !IJ.macroRunning())
				new Opener().openMultiple();
			else
				new Opener().open();
		} else if (cmd.equals("close"))
			close();
		else if (cmd.equals("save"))
			save();
		else if (cmd.equals("ij")) {
			Frame ij = IJ.getTopComponentFrame();
			if (ij!=null) ij.toFront();
		} else if (cmd.equals("tab"))
			WindowManager.putBehind();
		else if (cmd.equals("quit")) {
			IjxApplication ij = IJ.getInstance();
			if (ij!=null) ij.quit();
		} else if (cmd.equals("revert"))
			revert();
		else if (cmd.equals("undo"))
			undo();
		else if (cmd.equals("startup"))
			openStartupMacros();
    }
    
    void revert() {
    	IjxImagePlus imp = WindowManager.getCurrentImage();
		if (imp!=null)
			imp.revert();
		else
			IJ.noImage();
	}

    void save() {
    	IjxImagePlus imp = WindowManager.getCurrentImage();
		if (imp!=null)
			new FileSaver(imp).save();
		else
			IJ.noImage();
	}
	
    void undo() {
    	IjxImagePlus imp = WindowManager.getCurrentImage();
		if (imp!=null)
			Undo.undo();
		else
			IJ.noImage();
	}

	void close() {
    	IjxImagePlus imp = WindowManager.getCurrentImage();
		IjxWindow frame = WindowManager.getFrontWindow();
		if (frame==null || (Interpreter.isBatchMode() && frame instanceof ImageWindow))
			closeImage(imp);
		else if (frame instanceof PlugInFrame)
			((PlugInFrame)frame).close();
		else if (frame instanceof TextWindow)
			((TextWindow)frame).close();
		else
			closeImage(imp);
	}

	void closeImage(IjxImagePlus imp) {
		if (imp==null) {
			IJ.noImage();
			return;
		}
		imp.close();
		if (Recorder.record) {
			Recorder.record("close");
			Recorder.setCommand(null); // don't record run("Close")
		}
	}
	
	// Plugins>Macros>Open Startup Macros command
	void openStartupMacros() {
		Applet applet = IJ.getApplet();
		if (applet!=null) {
			IJ.run("URL...", "url="+IJ.URL+"/applet/StartupMacros.txt");
		} else {
			String path = IJ.getDirectory("macros")+"/StartupMacros.txt";
			File f = new File(path);
			if (!f.exists())
				IJ.error("\"StartupMacros.txt\" not found in ImageJ/macros/");
			else
				IJ.open(path);
		}
	}
	
}



