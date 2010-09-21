package ij.plugin;
import ij.*;
import ij.plugin.filter.*;
import ij.process.*;
import ij.gui.*;
import imagej.plugin.AbstractPlugIn;
import imagej.plugin.Parameter;

import java.awt.*;
import java.util.ArrayList;

/** This plugin implements the Image/Adjust/Canvas Size command.
	It changes the canvas size of an image or stack without resizing the image.
	The border is filled with the current background color.
	@author Jeffrey Kuhn (jkuhn at ccwf.cc.utexas.edu)
*/
public class CanvasResizer extends AbstractPlugIn 
{
	
	String[] positionTypes = {"Top-Left", "Top-Center", "Top-Right", 
		"Center-Left", "Center", "Center-Right",
		"Bottom-Left", "Bottom-Center", "Bottom-Right" };
	
	@Parameter( label = "Zero Fill", persist = "resizer.zero" )
    public boolean zeroFill = false;
    @Parameter( label = "Width", digits = 0 )
    public int width = 0;    
    @Parameter( label = "Height", digits = 0 )
    public int height = 0;
    @Parameter(label = "Position", choices = {
			"Top-Left", "Top-Center", "Top-Right", 
			"Center-Left", "Center", "Center-Right",
			"Bottom-Left", "Bottom-Center", "Bottom-Right"
		} )
    public String sPositions = "Center";
    
    private int wOld;
    private int hOld;
    private boolean fIsStack;
    
    public CanvasResizer() {
    	fIsStack = false;
    	
		ImagePlus imp = IJ.getImage();
		
		width = wOld = imp.getWidth();
		height = hOld = imp.getHeight();

		ImageStack stackOld = imp.getStack();
		if ((stackOld != null) && (stackOld.getSize() > 1))
			fIsStack = true;
		
		String strTitle = fIsStack ? "Resize Stack Canvas" : "Resize Image Canvas";
		
	}

    
	public void run() {
		
		//TODO:temp.
		int iPos = 0;

		ImagePlus imp = IJ.getImage();
		ImageStack stackOld = imp.getStack();
		
		int xOff, yOff;
		int xC = (width - wOld)/2;	// offset for centered
		int xR = (width - wOld);		// offset for right
		int yC = (height - hOld)/2;	// offset for centered
		int yB = (height - hOld);		// offset for bottom
		
		ArrayList<String> arrayList = new ArrayList<String>();
		for( String string : positionTypes )
		{
			arrayList.add(string);
		}
		
		switch( arrayList.indexOf(sPositions) ) {
		case 0:	// TL
			xOff=0;	yOff=0; break;
		case 1:	// TC
			xOff=xC; yOff=0; break;
		case 2:	// TR
			xOff=xR; yOff=0; break;
		case 3: // CL
			xOff=0; yOff=yC; break;
		case 4: // C
			xOff=xC; yOff=yC; break;
		case 5:	// CR
			xOff=xR; yOff=yC; break;
		case 6: // BL
			xOff=0; yOff=yB; break;
		case 7: // BC
			xOff=xC; yOff=yB; break;
		case 8: // BR
			xOff=xR; yOff=yB; break;
		default: // center
			xOff=xC; yOff=yC; break;
		}
		
		if (fIsStack) {
			ImageStack stackNew = expandStack(stackOld, width, height, xOff, yOff);
			imp.setStack(null, stackNew);
		} else {
			if (!IJ.macroRunning())
				Undo.setup(Undo.COMPOUND_FILTER, imp);
			ImageProcessor newIP = expandImage(imp.getProcessor(), width, height, xOff, yOff);
			imp.setProcessor(null, newIP);
			if (!IJ.macroRunning())
				Undo.setup(Undo.COMPOUND_FILTER_DONE, imp);
		}
	}
	
	public ImageStack expandStack(ImageStack stackOld, int wNew, int hNew, int xOff, int yOff) {
		int nFrames = stackOld.getSize();
		ImageProcessor ipOld = stackOld.getProcessor(1);
		java.awt.Color colorBack = Toolbar.getBackgroundColor();
		
		ImageStack stackNew = new ImageStack(wNew, hNew, stackOld.getColorModel());
		ImageProcessor ipNew;
		
		for (int i=1; i<=nFrames; i++) {
			IJ.showProgress((double)i/nFrames);
			ipNew = ipOld.createProcessor(wNew, hNew);
			if ( zeroFill )
				ipNew.setValue(0.0);
			else 
				ipNew.setColor(colorBack);
			ipNew.fill();
			ipNew.insert(stackOld.getProcessor(i), xOff, yOff);
			stackNew.addSlice(stackOld.getSliceLabel(i), ipNew);
		}
		return stackNew;
	}
	
	public ImageProcessor expandImage(ImageProcessor ipOld, int wNew, int hNew, int xOff, int yOff) {
		ImageProcessor ipNew = ipOld.createProcessor(wNew, hNew);
		if (zeroFill)
			ipNew.setValue(0.0);
		else 
			ipNew.setColor(Toolbar.getBackgroundColor());
		ipNew.fill();
		ipNew.insert(ipOld, xOff, yOff);
		return ipNew;
	}

	

}

