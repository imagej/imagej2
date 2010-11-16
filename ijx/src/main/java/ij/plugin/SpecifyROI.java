package ij.plugin;
import java.awt.*;
import java.util.*;
import ij.*;
import ij.gui.*;
import ij.measure.Calibration;
import ijx.IjxImagePlus;

/**
 *      This plugin implements the Edit/Selection/Specify command.<p>

 *      New update, correctly handling existing oval ROIs, the case that 
 *      "Centered" is already selected when the plugin starts, and always 
 *      restoring the original ROI when the dialog is cancelled (JW, 2008/02/22)
 *
 *      Enhancing the original plugin created by Jeffrey Kuhn, this one takes,
 *      in addition to width and height and the option to have an oval ROI from 
 *      the original program, x & y coordinates, slice number, and the option to have
 *      the x & y coordinates centered or in default top left corner of ROI.
 *      The original creator is Jeffrey Kuhn, The University of Texas at Austin,
 *	    jkuhn@ccwf.cc.utexas.edu
 *
 *      @author Joachim Wesner
 *      @author Leica Microsystems CMS GmbH
 *      @author joachim.wesner@leica-microsystems.com
 *
 *      @author Anthony Padua
 *      @author Duke University Medical Center, Department of Radiology
 *      @author padua001@mc.duke.edu
 *      
 */
public class SpecifyROI implements PlugIn, DialogListener {
    static double xRoi, yRoi, width, height;
    static boolean  oval;
    static boolean  centered;
    static boolean scaledUnits;
    static Rectangle prevRoi;
    static double prevPixelWidth = 1.0;
    int iSlice;
    boolean bAbort;
    IjxImagePlus imp;
    Vector fields, checkboxes;
    int stackSize;

	public void run(String arg) {
		imp = IJ.getImage();
		stackSize = imp!=null?imp.getStackSize():0;
		Roi roi = imp.getRoi();
		Calibration cal = imp.getCalibration();
		if (roi!=null && roi.getBounds().equals(prevRoi) && cal.pixelWidth==prevPixelWidth)
			roi = null;
		if (roi!=null) {
    		boolean rectOrOval = roi!=null && (roi.getType()==Roi.RECTANGLE||roi.getType()==Roi.OVAL);
    		oval = rectOrOval && (roi.getType()==Roi.OVAL);	// Handle existing oval ROI
			Rectangle r = roi.getBounds();
			width = r.width;
			height = r.height;
			xRoi = r.x;
			yRoi = r.y;
			if (scaledUnits && cal.scaled()) {
				xRoi = xRoi*cal.pixelWidth;
				yRoi = yRoi*cal.pixelHeight;
				width = width*cal.pixelWidth;
				height = height*cal.pixelHeight;
			}
    	} else if (!validDialogValues()) {
			width = imp.getWidth()/2;
			height = imp.getHeight()/2;
			xRoi = width/2;
			yRoi = height/2; 
		}
		if (centered) {	// Make xRoi and yRoi consistent when centered mode is active
			xRoi += width/2.0;
			yRoi += height/2.0; 
		}
		iSlice = imp.getCurrentSlice();
		showDialog();
	}
	
	boolean validDialogValues() {
		Calibration cal = imp.getCalibration();
		double pw=cal.pixelWidth, ph=cal.pixelHeight;
		if (width/pw<1.5 || height/ph<1.5)
			return false;
		if (xRoi/pw>=imp.getWidth() || yRoi/ph>=imp.getHeight())
			return false;
		return true;
	}

    /**
     *	Creates a dialog box, allowing the user to enter the requested
     *	width, height, x & y coordinates, slice number for a Region Of Interest,
     *  option for oval, and option for whether x & y coordinates to be centered.
     */
    void showDialog() {
    	Calibration cal = imp.getCalibration();
    	int digits = 0;
    	if (scaledUnits && cal.scaled())
    		digits = 2;
    	Roi roi = imp.getRoi();
    	if (roi==null)
    		drawRoi();
        GenericDialog gd = new GenericDialog("Specify");
        gd.addNumericField("Width:", width, digits);
        gd.addNumericField("Height:", height, digits);
        gd.addNumericField("X Coordinate:", xRoi, digits);
        gd.addNumericField("Y Coordinate:", yRoi, digits);
        if (stackSize>1)
        	gd.addNumericField("Slice:", iSlice, 0);
        gd.addCheckbox("Oval", oval);
        gd.addCheckbox("Centered",centered);
        if (cal.scaled())
            gd.addCheckbox("Scaled Units ("+cal.getUnits()+")", scaledUnits);
        fields = gd.getNumericFields();
        gd.addDialogListener(this);
        gd.showDialog();
        if (gd.wasCanceled()) {
        	 if (roi==null)
        		imp.killRoi();
        	 else // *ALWAYS* restore initial ROI when cancelled
        		imp.setRoi(roi);
        }
    }
    
	void drawRoi() {
		int iX = (int)xRoi;
		int iY = (int)yRoi;
		if (centered) {
			iX = (int)(xRoi - (width/2));
			iY = (int)(yRoi - (height/2));
		}
		int iWidth = (int)width;
		int iHeight = (int)height;
		Calibration cal = imp.getCalibration();
		if (scaledUnits && cal.scaled()) {
			iX = (int)Math.round(xRoi/cal.pixelWidth);
			iY = (int)Math.round(yRoi/cal.pixelHeight);
			iWidth = (int)Math.round(width/cal.pixelWidth);
			iHeight = (int)Math.round(height/cal.pixelHeight);
			prevPixelWidth = cal.pixelWidth;
		}
		Roi roi;
		if (oval)
			roi = new OvalRoi(iX, iY, iWidth, iHeight, imp);
		else
			roi = new Roi(iX, iY, iWidth, iHeight);
		imp.setRoi(roi);
		prevRoi = roi.getBounds();
		prevPixelWidth = cal.pixelWidth;
	}
    	
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		if (IJ.isMacOSX()) IJ.wait(100);
		width = gd.getNextNumber();
		height = gd.getNextNumber();
		xRoi = gd.getNextNumber();	
		yRoi = gd.getNextNumber();
		if (stackSize>1)	
			iSlice = (int) gd.getNextNumber(); 
		oval = gd.getNextBoolean();
		centered = gd.getNextBoolean();
		if (imp.getCalibration().scaled())
			scaledUnits = gd.getNextBoolean();
		if (gd.invalidNumber())
			return false;
		else {
			if (stackSize>1 && iSlice>0 && iSlice<=stackSize)
			    imp.setSlice(iSlice);
			drawRoi();
			return true;
		}
    }

}
