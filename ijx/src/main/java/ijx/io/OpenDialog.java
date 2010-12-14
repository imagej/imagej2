package ijx.io;
import ijx.Macro;
import ijx.Prefs;
import ijx.IJ;


import ijx.plugin.frame.Recorder;
import ijx.util.Java2;
import ijx.macro.Interpreter;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import javax.swing.JFileChooser;

/** This class displays a dialog window from 
	which the user can select an input file. */ 
 public class OpenDialog {

	private String dir;
	private String name;
	private boolean recordPath;
	private static String defaultDirectory;
	private static Frame sharedFrame;
	private String title;
	private static String lastDir, lastName;

	
	/** Displays a file open dialog with 'title' as
		the title. If 'path' is non-blank, it is
		used and the dialog is not displayed. Uses
		and updates the ImageJ default directory. */
	public OpenDialog(String title, String path) {
		String macroOptions = Macro.getOptions();
		if (macroOptions!=null && (path==null||path.equals(""))) {
			path = Macro.getValue(macroOptions, title, path);
			if (path==null || path.equals(""))
				path = Macro.getValue(macroOptions, "path", path);
			if ((path==null || path.equals("")) && title!=null && title.equals("Open As String"))
				path = Macro.getValue(macroOptions, "OpenAsString", path);
			if (path!=null && path.indexOf(".")==-1 && !((new File(path)).exists())) {
				// Is 'path' a macro variable?
				if (path.startsWith("&")) path=path.substring(1);
				Interpreter interp = Interpreter.getInstance();
				String path2 = interp!=null?interp.getStringVariable(path):null;
				if (path2!=null) path = path2;
			}
		}
		if (path==null || path.equals("")) {
			if (Prefs.useJFileChooser)
				jOpen(title, getDefaultDirectory(), null);
			else
				open(title, getDefaultDirectory(), null);
			if (name!=null) defaultDirectory = dir;
			this.title = title;
			recordPath = true;
		} else {
			decodePath(path);
			recordPath = IJ.macroRunning();
		}
		IJ.register(OpenDialog.class);
	}
	
	/** Displays a file open dialog, using the specified 
		default directory and file name. */
	public OpenDialog(String title, String defaultDir, String defaultName) {
		String path = null;
		String macroOptions = Macro.getOptions();
		if (macroOptions!=null)
			path = Macro.getValue(macroOptions, title, path);
		if (path!=null)
			decodePath(path);
		else {
			if (Prefs.isUseJFileChooser())
				jOpen(title, defaultDir, defaultName);
			else
				open(title, defaultDir, defaultName);
			this.title = title;
			recordPath = true;
		}
	}
	
	// Uses JFileChooser to display file open dialog box.
	void jOpen(String title, String path, String fileName) {
		Java2.setSystemLookAndFeel();
		if (EventQueue.isDispatchThread())
			jOpenDispatchThread(title, path, fileName);
		else
			jOpenInvokeAndWait(title, path, fileName);
	}
		
	// Uses the JFileChooser class to display the dialog box.
	// Assumes we are running on the event dispatch thread
	void jOpenDispatchThread(String title, String path, final String fileName) {
		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle(title);
		File fdir = null;
		if (path!=null)
			fdir = new File(path);
		if (fdir!=null)
			fc.setCurrentDirectory(fdir);
		if (fileName!=null)
			fc.setSelectedFile(new File(fileName));
		int returnVal = fc.showOpenDialog(IJ.getTopComponentFrame());
		if (returnVal!=JFileChooser.APPROVE_OPTION)
			{Macro.abort(); return;}
		File file = fc.getSelectedFile();
		if (file==null)
			{Macro.abort(); return;}
		name = file.getName();
		dir = fc.getCurrentDirectory().getPath()+File.separator;
	}

	// Run JFileChooser on event dispatch thread to avoid deadlocks
	void jOpenInvokeAndWait(final String title, final String path, final String fileName) {
		try {
			EventQueue.invokeAndWait(new Runnable() {
				public void run() {
				JFileChooser fc = new JFileChooser();
				fc.setDialogTitle(title);
				File fdir = null;
				if (path!=null)
					fdir = new File(path);
				if (fdir!=null)
					fc.setCurrentDirectory(fdir);
				if (fileName!=null)
					fc.setSelectedFile(new File(fileName));
				int returnVal = fc.showOpenDialog(IJ.getTopComponentFrame());
				if (returnVal!=JFileChooser.APPROVE_OPTION)
					{Macro.abort(); return;}
				File file = fc.getSelectedFile();
				if (file==null)
					{Macro.abort(); return;}
				name = file.getName();
				dir = fc.getCurrentDirectory().getPath()+File.separator;
				}
			});
		} catch (Exception e) {}
	}
	
	// Uses the AWT FileDialog class to display the dialog box
	void open(String title, String path, String fileName) {
		Frame parent = IJ.getTopComponentFrame();
		if (parent==null) {
			if (sharedFrame==null) sharedFrame = new Frame();
			parent = sharedFrame;
		}
		FileDialog fd = new FileDialog(parent, title);
		if (path!=null)
			fd.setDirectory(path);
		if (fileName!=null)
			fd.setFile(fileName);
		//GUI.center(fd);
		fd.show();
		name = fd.getFile();
		if (name==null) {
			if (IJ.isMacOSX())
				System.setProperty("apple.awt.fileDialogForDirectories", "false");
			Macro.abort();
		} else
			dir = fd.getDirectory();
	}

	void decodePath(String path) {
		int i = path.lastIndexOf('/');
		if (i==-1)
			i = path.lastIndexOf('\\');
		if (i>0) {
			dir = path.substring(0, i+1);
			name = path.substring(i+1);
		} else {
			dir = "";
			name = path;
		}
	}

	/** Returns the selected directory. */
	public String getDirectory() {
		lastDir = dir;
		return dir;
	}
	
	/** Returns the selected file name. */
	public String getFileName() {
		if (Recorder.record && recordPath)
			Recorder.recordPath(title, dir+name);
		lastName = name;
		return name;
	}
		
	/** Returns the current working directory, which may be null. The
		returned string always ends with the separator character ("/" or "\").*/
	public static String getDefaultDirectory() {
		if (defaultDirectory==null)
			defaultDirectory = Prefs.getDefaultDirectory();
		return defaultDirectory;
	}

	/** Sets the current working directory. */
	public static void setDefaultDirectory(String defaultDir) {
		defaultDirectory = defaultDir;
		if (!defaultDirectory.endsWith(File.separator))
			defaultDirectory = defaultDirectory + File.separator;
	}
	
	/** Returns the path to the last directory opened by the user
		using a file open or file save dialog, or using drag and drop. 
		Returns null if the users has not opened a file. */
	public static String getLastDirectory() {
		return lastDir;
	}
		
	/** Sets the path to the directory containing the last file opened by the user. */
	public static void setLastDirectory(String dir) {
		lastDir = dir;
	}

	/** Returns the name of the last file opened by the user
		using a file open or file save dialog, or using drag and drop.
		Returns null if the users has not opened a file. */
	public static String getLastName() {
		return lastName;
	}

	/** Sets the name of the last file opened by the user. */
	public static void setLastName(String name) {
		lastName = name;
	}

}
