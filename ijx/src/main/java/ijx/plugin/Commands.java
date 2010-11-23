package ijx.plugin;
import ijx.plugin.frame.Recorder;
import ijx.plugin.api.PlugIn;
import ijx.plugin.api.PlugInFrame;
import ijx.gui.NewImage;
import ijx.gui.dialog.GenericDialog;
import ijx.io.Opener;
import ijx.io.FileSaver;
import ijx.Undo;
import ijx.Prefs;
import ijx.WindowManager;
import ijx.IJ;

import ij.plugin.frame.*;
import ijx.text.TextWindow;
import ijx.macro.Interpreter;
import ijx.IjxImagePlus;
import ijx.app.IjxApplication;
import ijx.gui.IjxImageWindow;
import ijx.gui.IjxWindow;
import java.io.File;
import java.applet.Applet;
	
/**	Runs miscellaneous File and Window menu commands. */
public class Commands implements PlugIn {
	
	public void run(String cmd) {
		if (cmd.equals("new"))
			new NewImage().openImage();
		else if (cmd.equals("open")) {
			if (Prefs.useJFileChooser && !IJ.macroRunning())
				new Opener().openMultiple();
			else
				new Opener().open();
		} else if (cmd.equals("close"))
			close();
		else if (cmd.equals("close-all"))
			closeAll();
		else if (cmd.equals("save"))
			save();
		else if (cmd.equals("ij")) {
            IjxWindow ij = IJ.getTopComponent();
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
		if (imp!=null) {
			boolean unlockedImage = imp.getStackSize()==1&&!imp.isLocked();
			if (unlockedImage) imp.lock();
			new FileSaver(imp).save();
			if (unlockedImage) imp.unlock();
		} else
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
		if (frame==null || (Interpreter.isBatchMode() && frame instanceof IjxImageWindow))
			closeImage(imp);
		else if (frame instanceof PlugInFrame)
			((PlugInFrame)frame).close();
		else if (frame instanceof TextWindow)
			((TextWindow)frame).close();
		else
			closeImage(imp);
	}

	void closeAll() {
    	int[] list = WindowManager.getIDList();
    	if (list!=null) {
    		int imagesWithChanges = 0;
			for (int i=0; i<list.length; i++) {
				IjxImagePlus imp = WindowManager.getImage(list[i]);
				if (imp!=null && imp.isChanged()) imagesWithChanges++;
			}
			if (imagesWithChanges>0 && !IJ.macroRunning()) {
				GenericDialog gd = new GenericDialog("Close All");
				String msg = null;
				if (imagesWithChanges==1)
					msg = "There is one image";
				else
					msg = "There are "+imagesWithChanges+" images";
				gd.addMessage(msg+" with unsaved changes. If you\nclick \"OK\" they will be closed without being saved.");
				gd.showDialog();
				if (gd.wasCanceled()) return;
			}
			for (int i=0; i<list.length; i++) {
				IjxImagePlus imp = WindowManager.getImage(list[i]);
				if (imp!=null) {
					imp.setChanged(false);
					imp.close();
				}
			}
    	}
    	//Frame[] windows = WindowManager.getNonImageWindows();
    	//for (int i=0; i<windows.length; i++) {
    	//	if ((windows[i] instanceof PlugInFrame) && !(windows[i] instanceof Editor))
    	//		((PlugInFrame)windows[i]).close();
    	//}
	}

	void closeImage(IjxImagePlus imp) {
		if (imp==null) {
			IJ.noImage();
			return;
		}
		imp.close();
		if (Recorder.record && !IJ.isMacro()) {
			if (Recorder.scriptMode())
				Recorder.recordCall("imp.close();");
			else
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
			if (IJ.runPlugIn("fiji.scripting.Script_Editor", path) != null)
				return;
			File f = new File(path);
			if (!f.exists())
				IJ.error("\"StartupMacros.txt\" not found in ImageJ/macros/");
			else
				IJ.open(path);
		}
	}
	
}



