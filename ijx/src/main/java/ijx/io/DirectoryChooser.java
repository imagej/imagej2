package ijx.io;
import ijx.Macro;
import ijx.Prefs;
import ijx.IJ;


import ijx.plugin.frame.Recorder;
import ijx.util.Java2;
import java.awt.*;
import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.*;

/** This class displays a dialog box that allows the user can select a directory. */ 
 public class DirectoryChooser {
 	private String directory;
 	private static String defaultDir;
 	private String title;
 
 	/** Display a dialog using the specified title. */
 	public DirectoryChooser(String title) {
 		this.title = title;
 		if (IJ.isMacOSX())
			getDirectoryUsingFileDialog(title);
 		else {
			String macroOptions = Macro.getOptions();
			if (macroOptions!=null)
				directory = Macro.getValue(macroOptions, title, null);
			if (directory==null) {
 				if (EventQueue.isDispatchThread())
 					getDirectoryUsingJFileChooserOnThisThread(title);
 				else
 					getDirectoryUsingJFileChooser(title);
 			}
 		}
 	}
 	
	// runs JFileChooser on event dispatch thread to avoid possible thread deadlocks
 	void getDirectoryUsingJFileChooser(final String title) {
		Java2.setSystemLookAndFeel();
		try {
			EventQueue.invokeAndWait(new Runnable() {
				public void run() {
					JFileChooser chooser = new JFileChooser();
					if (defaultDir!=null) 
						chooser.setCurrentDirectory(new File(defaultDir));
					chooser.setDialogTitle(title);
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					chooser.setApproveButtonText("Select");
					if (chooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION) {
						File dir = chooser.getCurrentDirectory();
						File file = chooser.getSelectedFile();
						directory = dir.getPath();
						if (!directory.endsWith(File.separator))
							directory += File.separator;
						if (directory!=null)
							defaultDir = directory;
						String fileName = file.getName();
						if (fileName.indexOf(":\\")!=-1)
							directory = defaultDir = fileName;
						else
							directory += fileName+File.separator;
					}
				}
			});
		} catch (Exception e) {}
	}
 
	// Choose a directory using JFileChooser on the current thread
 	void getDirectoryUsingJFileChooserOnThisThread(final String title) {
		Java2.setSystemLookAndFeel();
		try {
			JFileChooser chooser = new JFileChooser();
			if (defaultDir!=null) {
				chooser.setCurrentDirectory(new File(defaultDir));
			}
			chooser.setDialogTitle(title);
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setApproveButtonText("Select");
			if (chooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION) {
				File dir = chooser.getCurrentDirectory();
				File file = chooser.getSelectedFile();
				directory = dir.getPath();
				if (!directory.endsWith(File.separator))
					directory += File.separator;
					if (directory!=null)
						defaultDir = directory;
				String fileName = file.getName();
				if (fileName.indexOf(":\\")!=-1)
					directory = defaultDir = fileName;
				else
					directory += fileName+File.separator;
			}
		} catch (Exception e) {}
	}

 	// On Mac OS X, we can select directories using the native file open dialog
 	void getDirectoryUsingFileDialog(String title) {
 		boolean saveUseJFC = Prefs.useJFileChooser;
 		Prefs.useJFileChooser = false;
		System.setProperty("apple.awt.fileDialogForDirectories", "true");
		OpenDialog od = new OpenDialog(title, defaultDir, null);
		if (od.getDirectory()==null)
			directory = null;
		else
			directory = od.getDirectory() + od.getFileName() + "/";
		if (directory!=null)
			defaultDir = (new File(directory)).getParent();
		System.setProperty("apple.awt.fileDialogForDirectories", "false");
 		Prefs.useJFileChooser = saveUseJFC;
	}

 	/** Returns the directory selected by the user. */
 	public String getDirectory() {
 		//IJ.log("getDirectory: "+directory);
		if (Recorder.record && !IJ.isMacOSX())
			Recorder.recordPath(title, directory);
 		return directory;
 	}
 	
    /** Sets the default directory presented in the dialog. */
    public static void setDefaultDirectory(String dir) {
    	if (dir==null || (new File(dir)).isDirectory())
        	defaultDir = dir;
    }

}
