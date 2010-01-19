/**Implements the Edit/Undo command.*/

package ij;
import ij.process.*;
import java.awt.*;
import java.awt.image.*;
import ij.gui.*;

/** This class consists of static methods and
	fields that implement ImageJ's Undo command. */
public class Undo {

	public static final int NOTHING = 0;
	public static final int FILTER = 1;
	public static final int TYPE_CONVERSION = 2;
	public static final int PASTE = 3;
	public static final int COMPOUND_FILTER = 4;
	public static final int COMPOUND_FILTER_DONE = 5;
	public static final int TRANSFORM = 6;
	public static final int OVERLAY_ADDITION = 7;
	
	private static int whatToUndo = NOTHING;
	private static int imageID;
	private static ImageProcessor ipCopy = null;
	private static ImagePlus impCopy;
	
	public static void setup(int what, ImagePlus imp) {
		if (imp==null) {
			whatToUndo = NOTHING;
			reset();
			return;
		}
		//IJ.log(imp.getTitle() + ": set up undo (" + what + ")");
		if (what==FILTER && whatToUndo==COMPOUND_FILTER)
				return;
		if (what==COMPOUND_FILTER_DONE) {
			if (whatToUndo==COMPOUND_FILTER)
				whatToUndo = what;
			return;
		}
		whatToUndo = what;
		imageID = imp.getID();
		if (what==TYPE_CONVERSION)
			ipCopy = imp.getProcessor();
		else if (what==TRANSFORM) {			
			impCopy = new ImagePlus(imp.getTitle(), imp.getProcessor().duplicate());
			Object fht  = imp.getProperty("FHT");
			if (fht!=null) {
				fht = new FHT((ImageProcessor)fht); // duplicate
				impCopy.setProperty("FHT", fht);
			}
		} else if (what==COMPOUND_FILTER) {
			ImageProcessor ip = imp.getProcessor();
			if (ip!=null)
				ipCopy = ip.duplicate();
			else
				ipCopy = null;
		} else if (what==OVERLAY_ADDITION) {
			impCopy = null;
			ipCopy = null;
		} else
			ipCopy = null;
	}
	
	
	public static void reset() {
		if (whatToUndo==COMPOUND_FILTER || whatToUndo==OVERLAY_ADDITION)
			return;
		whatToUndo = NOTHING;
		imageID = 0;
		ipCopy = null;
		impCopy = null;
		//IJ.log("Undo: reset");
	}
	

	public static void undo() {
		ImagePlus imp = WindowManager.getCurrentImage();
		//IJ.log(imp.getTitle() + ": undo (" + whatToUndo + ")  "+(imageID!=imp.getID()));
		if (imp==null || imageID!=imp.getID()) {
			reset();
			return;
		}
		switch (whatToUndo) {
			case FILTER:
				ImageProcessor ip = imp.getProcessor();
				if (ip!=null) {
					ip.reset();
					imp.updateAndDraw();
				}
	    		break;
			case TYPE_CONVERSION:
			case COMPOUND_FILTER:
			case COMPOUND_FILTER_DONE:
				if (ipCopy!=null)
					imp.setProcessor(null, ipCopy);
	    		break;
			case TRANSFORM:
				if (impCopy!=null) {
					imp.setProcessor(impCopy.getTitle(), impCopy.getProcessor());
					Object fht  = impCopy.getProperty("FHT");
					if (fht!=null)
						imp.setProperty("FHT", fht);
					else if (imp.getProperty("FHT")!=null)
							imp.getProperties().remove("FHT");
				}
	    		break;
			case PASTE:
				Roi roi = imp.getRoi();
				if (roi!=null)
					roi.abortPaste();
	    		break;
			case OVERLAY_ADDITION:
				Overlay overlay = imp.getOverlay();
				if (overlay==null) 
					{IJ.beep(); return;}
				int size = overlay.size();
				if (size>0) {
					overlay.remove(size-1);
					imp.draw();
				} else {
					IJ.beep();
					return;
				}
	    		return; //don't reset
    	}
    	reset();
	}
	
}
