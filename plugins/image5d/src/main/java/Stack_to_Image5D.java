import i5d.Image5D;
import ij.*;
import ij.gui.*;
import ij.plugin.*;
/*
 * Created on 07.04.2005
 */

/** Converts an ImageStack to an Image5D. NChannels, nSlices and nFrames are taken
 * as default values.
 * @author Joachim Walter
 */
public class Stack_to_Image5D implements PlugIn {

    private static final int CH=0;
    private static final int Z=1;
    private static final int T=2;

	public Stack_to_Image5D() {
	}


	public void run(String arg) {
	    if (IJ.versionLessThan("1.34p")) return;
	    
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp == null) {
			IJ.noImage();
			return;
		}
		if (imp instanceof Image5D) {
			IJ.error("Image is already an Image5D");
			return;
		}
		int width = imp.getWidth();
		int height = imp.getHeight();
        int nChannels = imp.getNChannels();
        int nSlices = imp.getNSlices();
        int nFrames = imp.getNFrames();
		int stackSize = imp.getStackSize();
		String title = imp.getTitle();
		int type = imp.getType();
		
		if (type != ImagePlus.GRAY8 && type != ImagePlus.GRAY16 && type != ImagePlus.GRAY32) {
			IJ.error("Wrong data type");
			return;
		}

		// The choices that are initially displayed:
		// ch, z, t and the respective dimension sizes.
		int first=CH;
		int middle=Z;
		int last=T;
		int nFirst=nChannels;
		int nMiddle=nSlices;
		int nLast=nFrames;
		boolean assignColor;
        
        // Different choices, if only one dimension is >1
        if (nChannels<=1 && nSlices<=1 && nFrames>1) {
            first = T; middle = Z; last = CH;
            nFirst = stackSize; nMiddle = 1; nLast = 1;
        } else if (nChannels<=1 && nFrames<=1 && nSlices>1) {
            first = Z; middle = CH; last = T;
            nFirst = stackSize; nMiddle = 1; nLast = 1;
        }
		
		
		String[] dimensions = new String[] {"ch", "z", "t"};
		boolean goOn = true;
		do {
		    goOn = true;
		    
			GenericDialog gd = new GenericDialog("Convert stack to Image5D");
			gd.addMessage("Stack has "+stackSize+" slices.");
			gd.addChoice("3rd dimension", dimensions, dimensions[first]);
			gd.addChoice("4th dimension", dimensions, dimensions[middle]);
			gd.addNumericField("3rd_dimension_size", nFirst, 0, 8, "");
			gd.addNumericField("4th_dimension_size", nMiddle, 0, 8, "");
			gd.addCheckbox("Assign default color", true);
			gd.showDialog();
			
			if (gd.wasCanceled()) {
				return;
			}
	
			first = gd.getNextChoiceIndex();
			middle = gd.getNextChoiceIndex();
			nFirst = (int) gd.getNextNumber();
			nMiddle = (int) gd.getNextNumber();
			assignColor = gd.getNextBoolean();
			
			if (first==middle) {
				IJ.error("Please do not select two dimensions equal!");
				goOn = false;
                continue;
			}
            
            // Determine type of third dimension.
            boolean[] thirdChoice = {true, true, true};
            thirdChoice[first] = false;
            thirdChoice[middle] = false;
            for (int i=0; i<3; i++) {
                if (thirdChoice[i]) {
                    last = i;
                    break;
                }
            }

            double dLast = (double) stackSize / (double) nFirst / (double) nMiddle;
			nLast = (int) dLast;
			if (nLast != dLast) {
				IJ.error("channels*slices*frames!=stackSize");
				goOn = false;
                continue;
			}
		
		} while(goOn == false);
		
		
		nChannels=1;
        nSlices=1;
        nFrames=1;
		switch (first) {
			case 0: nChannels = nFirst; break;
			case 1: nSlices = nFirst; break;
			case 2: nFrames = nFirst; break;
		}		
		switch (middle) {
			case 0: nChannels = nMiddle; break;
			case 1: nSlices = nMiddle; break;
			case 2: nFrames = nMiddle; break;
		}		
		switch (last) {
			case 0: nChannels = nLast; break;
			case 1: nSlices = nLast; break;
			case 2: nFrames = nLast; break;
		}
		
		
		Image5D img5d = new Image5D(title, type, width, height, nChannels, nSlices, nFrames, false);
	
		int[] index = new int[3];
		for (index[2]=0; index[2]<nFrames; ++index[2]) {
			for (index[1]=0; index[1]<nSlices; ++index[1]) {
				for (index[0]=0; index[0]<nChannels; ++index[0]) {
					img5d.setCurrentPosition(0, 0, index[0], index[1], index[2]);
					int stackPosition = 1 + index[first] + index[middle]*nFirst + index[last]*nFirst*nMiddle;
					img5d.setPixels(imp.getStack().getPixels(stackPosition));
				}
			}
		}
        img5d.setDefaultChannelNames();
        
        if(assignColor) {
            img5d.setDefaultColors();
        }
		
		img5d.setCurrentPosition(0,0,0,0,0);       
        img5d.setCalibration(imp.getCalibration().copy());

		img5d.show();
        
		imp.changes = false;        
        if (imp.getWindow() != null)
            imp.getWindow().close();
        
        if(img5d.getWindow() != null)
            WindowManager.setCurrentWindow(img5d.getWindow());
	}

	
}
