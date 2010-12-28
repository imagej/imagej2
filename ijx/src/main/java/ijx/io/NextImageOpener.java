/**
This plugin, written by Jon Harmon, implements the File/Open Next command.
It opens the "next" image in a directory, where "next" can be the
succeeding or preceeding image in the directory list.
Press shift-o to open the succeeding image or 
alt-shift-o to open the preceeding image.
It can leave the previous file open, or close it.
You may contact the author at Jonathan_Harman at yahoo.com
This code was modified from Image_Browser by Albert Cardona
*/

package ijx.io;
import ijx.plugin.api.PlugIn;
import ijx.gui.dialog.YesNoCancelDialog;
import ijx.io.FileSaver;
import ijx.io.Opener;
import ijx.io.FileInfo;
import ijx.IJ;

import ijx.IjxImagePlus;
import ijx.gui.IjxImageWindow;
import java.io.File;

public class NextImageOpener implements PlugIn {

	boolean forward = true; // default browse direction is forward
	boolean closeCurrent = true; //default behavior is to close current window
	IjxImagePlus imp0;
	
	public void run(String arg) {
		/* get changes to defaults */
		if (arg.equals("backward") || IJ.altKeyDown()) forward = false;
		if (arg.equals("backwardsc")) {
			forward = false;
			closeCurrent = false;
		}
		if (arg.equals("forwardsc")) {
			forward = true;
			closeCurrent = false;
		}
				
		// get current image; displays error and aborts if no image is open
 		imp0 = IJ.getImage();
 		// get current image directory
 		String currentPath = IJ.getDirectory("image");
		if (IJ.debugMode) IJ.log("OpenNext.currentPath:" + currentPath);
		if (currentPath==null) {
			IJ.error("Next Image", "Directory information for \""+imp0.getTitle()+"\" not found.");
			return;
		}
		// get the next name (full path)
		//long start = System.currentTimeMillis();
		String nextPath = getNext(currentPath, getName(imp0), forward);
		//IJ.log("time: "+(System.currentTimeMillis()-start));
		if (IJ.debugMode) IJ.log("OpenNext.nextPath:" + nextPath);
		// open
		if (nextPath != null) {
			String rtn = open(nextPath);
			if (rtn==null)
				open(getNext(currentPath, (new File(nextPath)).getName(), forward));
		}
	}
	
	String getName(IjxImagePlus imp) {
		String name = imp.getTitle();
		FileInfo fi = imp.getOriginalFileInfo();
		if (fi!=null && fi.fileName!=null)
			name = fi.fileName;
		return name;
	}
	
	String open(String nextPath) {
		IjxImagePlus imp2 = IJ.openImage(nextPath);
		if (imp2==null) return null;
		String newTitle = imp2.getTitle();
		if (imp0.isChanged()) {
			String msg;
			String name = imp0.getTitle();
			if (name.length()>22)
				msg = "Save changes to\n" + "\"" + name + "\"?";
			else
				msg = "Save changes to \"" + name + "\"?";
			YesNoCancelDialog d = new YesNoCancelDialog(IJ.getTopComponentFrame(), "ImageJ", msg);
			if (d.cancelPressed())
				return "Canceled";
			else if (d.yesPressed()) {
				FileSaver fs = new FileSaver(imp0);
				if (!fs.save())
					return "Canceled";
			}
			imp0.setChanged(false);
		}
		if (imp2.isComposite() || imp2.isHyperStack()) {
			imp2.show();
			imp0.close();
			imp0 = imp2;
		} else {
			imp0.setStack(newTitle, imp2.getStack());
			imp0.setCalibration(imp2.getCalibration());
			imp0.setFileInfo(imp2.getOriginalFileInfo());
			imp0.setProperty ("Info", imp2.getProperty ("Info"));
			IjxImageWindow win = imp0.getWindow();
			if (win!=null) win.repaint();
		}
		return "ok";
	}

	/** gets the next image name in a directory list */
	String getNext(String path, String imageName, boolean forward) {
		File dir = new File(path);
		if (!dir.isDirectory()) return null;
		String[] names = dir.list();
		imagej.util.StringSorter.sort(names);
		int thisfile = -1;
		for (int i=0; i<names.length; i++) {
			if (names[i].equals(imageName)) {
				thisfile = i;
				break;
			}
		}
		if (IJ.debugMode) IJ.log("OpenNext.thisfile:" + thisfile);
		if(thisfile == -1) return null;// can't find current image
		
		// make candidate the index of the next file
		int candidate = thisfile + 1;
		if (!forward) candidate = thisfile - 1;
		if (candidate<0) candidate = names.length - 1;
		if (candidate==names.length) candidate = 0;
		// keep on going until an image file is found or we get back to beginning
		while (candidate!=thisfile) {
			String nextPath = path + names[candidate];
			if (IJ.debugMode) IJ.log("OpenNext: "+ candidate + "  " + names[candidate]);
			File nextFile = new File(nextPath);
			boolean canOpen = true;
			if (names[candidate].startsWith(".") || nextFile.isDirectory())
				canOpen = false;
			if (canOpen) {
				Opener o = new Opener();
				int type = o.getFileType(nextPath);
				if (type==Opener.UNKNOWN || type==Opener.JAVA_OR_TEXT
				||  type==Opener.ROI ||  type==Opener.TEXT)
					canOpen = false;
			}
			if (canOpen)
					return nextPath;
			else {// increment again
				if (forward)
					candidate = candidate + 1;
				else
					candidate = candidate - 1;
				if (candidate<0) candidate = names.length - 1;
				if (candidate == names.length) candidate = 0;
			}
			
		}
		if (IJ.debugMode) IJ.log("OpenNext: Search failed");
		return null;
	}

}
