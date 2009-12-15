package ij.plugin;
import ijx.IjxImagePlus;
import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.measure.Calibration;
import ij.macro.Interpreter;
import ij.io.FileInfo;
import ijx.IjxImageStack;
import java.awt.image.ColorModel;



/** Implements the "Stack to HyperStack", "RGB to HyperStack" 
	and "HyperStack to Stack" commands. */
public class HyperStackConverter implements PlugIn {
	static final int C=0, Z=1, T=2;
	static final int CZT=0, CTZ=1, ZCT=2, ZTC=3, TCZ=4, TZC=5;
    static final String[] orders = {"xyczt(default)", "xyctz", "xyzct", "xyztc", "xytcz", "xytzc"};
    static int order = CZT;

	public void run(String arg) {
		if (arg.equals("new"))
		{newHyperStack(); return;}
		IjxImagePlus imp = IJ.getImage();
    	if (arg.equals("stacktohs"))
    		convertStackToHS(imp);
    	else if (arg.equals("hstostack"))
    		convertHSToStack(imp);
	}
	
	/** Displays the current stack in a HyperStack window. Based on the 
		Stack_to_Image5D class in Joachim Walter's Image5D plugin. */
	void convertStackToHS(IjxImagePlus imp) {
        int nChannels = imp.getNChannels();
        int nSlices = imp.getNSlices();
        int nFrames = imp.getNFrames();
		int stackSize = imp.getImageStackSize();
		if (stackSize==1) {
			IJ.error("Stack to HyperStack", "Stack required");
			return;
		}
		if (imp.getBitDepth()==24) {
			new CompositeConverter().run("color");
			return;
		}
		String[] modes = {"Composite", "Color", "Grayscale"};
		GenericDialog gd = new GenericDialog("Convert to HyperStack");
		gd.addChoice("Order:", orders, orders[order]);
		gd.addNumericField("Channels (c):", nChannels, 0);
		gd.addNumericField("Slices (z):", nSlices, 0);
		gd.addNumericField("Frames (t):", nFrames, 0);
		gd.addChoice("Display Mode:", modes, modes[1]);
		gd.showDialog();
		if (gd.wasCanceled()) return;
		order = gd.getNextChoiceIndex();
		nChannels = (int) gd.getNextNumber();
		nSlices = (int) gd.getNextNumber();
		nFrames = (int) gd.getNextNumber();
		int mode = gd.getNextChoiceIndex();
		if (nChannels*nSlices*nFrames!=stackSize) {
			IJ.error("HyperStack Converter", "channels x slices x frames <> stack size");
			return;
		}
		imp.setDimensions(nChannels, nSlices, nFrames);
		if (order!=CZT && imp.getStack().isVirtual())
			IJ.error("HyperStack Converter", "Virtual stacks must by in XYCZT order.");
		else {
			shuffle(imp, order);
			IjxImagePlus imp2 = imp;
			if (nChannels>1 && imp.getBitDepth()!=24) {
				imp2 = new CompositeImage(imp, mode+1);
			}
			imp2.setOpenAsHyperStack(true);
			new StackWindow(imp2);
			if (imp!=imp2) imp.hide();
		}
	}

	/** Changes the dimension order of a 4D or 5D stack from 
		the specified order (CTZ, ZCT, ZTC, TCZ or TZC) to 
		the XYCZT order used by ImageJ. */
	public void shuffle(IjxImagePlus imp, int order) {
        int nChannels = imp.getNChannels();
        int nSlices = imp.getNSlices();
        int nFrames = imp.getNFrames();
		int first=C, middle=Z, last=T;
		int nFirst=nChannels, nMiddle=nSlices, nLast=nFrames;
		switch (order) {
			case CTZ: first=C; middle=T; last=Z;
				nFirst=nChannels; nMiddle=nFrames; nLast=nSlices;
				break;
			case ZCT: first=Z; middle=C; last=T;
				nFirst=nSlices; nMiddle=nChannels; nLast=nFrames;
				break;
			case ZTC: first=Z; middle=T; last=C;
				nFirst=nSlices; nMiddle=nFrames; nLast=nChannels;
				break;
			case TCZ: first=T; middle=C; last=Z;
				nFirst=nFrames; nMiddle=nChannels; nLast=nSlices;
				break;
			case TZC: first=T; middle=Z; last=C;
				nFirst=nFrames; nMiddle=nSlices; nLast=nChannels;
				break;
		}
		if (order!=CZT) {
			Object[] images1 = imp.getImageStack().getImageArray();
			Object[] images2 = new Object[images1.length];
			System.arraycopy(images1, 0, images2, 0, images1.length);
			int[] index = new int[3];
			for (index[2]=0; index[2]<nFrames; ++index[2]) {
				for (index[1]=0; index[1]<nSlices; ++index[1]) {
					for (index[0]=0; index[0]<nChannels; ++index[0]) {
						int dstIndex = index[0] + index[1]*nChannels + index[2]*nChannels*nSlices;
						int srcIndex = index[first] + index[middle]*nFirst + index[last]*nFirst*nMiddle;
						images1[dstIndex] = images2[srcIndex];
					}
				}
			}
		}
	}

	void convertHSToStack(IjxImagePlus imp) {
		if (!imp.isHyperStack()) return;
		IjxImagePlus imp2 = imp;
		if (imp.isComposite()) {
			IjxImageStack stack = imp.getStack();
			imp2 = imp.createImagePlus();
			imp2.setStack(imp.getTitle(), stack);
			int[] dim = imp.getDimensions();
			imp2.setDimensions(dim[2], dim[3], dim[4]);
			ImageProcessor ip2 = imp2.getProcessor();
			ip2.setColorModel(ip2.getDefaultColorModel());
		}
		imp2.setOpenAsHyperStack(false);
		new StackWindow(imp2);
		if (imp!=imp2) imp.hide();
	}
	
	void newHyperStack() {
		IJ.runMacroFile("ij.jar:HyperStackMaker", "");
	}
	
}

