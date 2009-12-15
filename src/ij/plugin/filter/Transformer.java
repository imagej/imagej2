package ij.plugin.filter;
import ijx.IjxImagePlus;
import ij.*;
import ij.process.*;
import ij.measure.Calibration;
import ijx.IjxImageStack;

/** Implements the flip and rotate commands in the Image/Transformations submenu. */
public class Transformer implements IjxPlugInFilter {
	
	IjxImagePlus imp;
	String arg;

	public int setup(String arg, IjxImagePlus imp) {
		this.arg = arg;
		this.imp = imp;
		if (arg.equals("fliph") || arg.equals("flipv"))
			return IJ.setupDialog(imp, DOES_ALL+NO_UNDO);
		else
			return DOES_ALL+NO_UNDO+NO_CHANGES;
	}

	public void run(ImageProcessor ip) {
		if (arg.equals("fliph")) {
			ip.flipHorizontal();
			return;
		}
		if (arg.equals("flipv")) {
			ip.flipVertical();
			return;
		}
		if (arg.equals("right") || arg.equals("left")) {
	    	StackProcessor sp = new StackProcessor(imp.getStack(), ip);
	    	IjxImageStack s2 = null;
			if (arg.equals("right"))
	    		s2 = sp.rotateRight();
	    	else
	    		s2 = sp.rotateLeft();
	    	Calibration cal = imp.getCalibration();
	    	imp.setStack(null, s2);
	    	double pixelWidth = cal.pixelWidth;
	    	cal.pixelWidth = cal.pixelHeight;
	    	cal.pixelHeight = pixelWidth;
			return;
		}
	}

}
