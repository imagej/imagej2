package ij.plugin;
import ijx.IjxImagePlus;
import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.measure.*;
import ij.plugin.frame.Recorder;
import ij.plugin.filter.PlugInFilter;
import ijx.IjxImageStack;
import java.awt.*;

/** This plugin implements the Process/Binary/Make Binary 
	and Convert to Mask commands. */
public class Thresholder implements PlugIn, Measurements {
	
	private double minThreshold;
	private double maxThreshold;
	boolean autoThreshold;
	boolean skipDialog;
	static boolean fill1 = true;
	static boolean fill2 = true;
	static boolean useBW = true;
	static boolean useLocal = true;
	boolean convertToMask;

	public void run(String arg) {
		convertToMask = arg.equals("mask");
		skipDialog = arg.equals("skip") || convertToMask;
		IjxImagePlus imp = WindowManager.getCurrentImage();
		if (imp==null)
			{IJ.noImage(); return;}
		if (imp.getStackSize()==1) {
			Undo.setup(Undo.COMPOUND_FILTER, imp);
			applyThreshold(imp);
			Undo.setup(Undo.COMPOUND_FILTER_DONE, imp);
		} else
			convertStack(imp);
	}
	
	void convertStack(IjxImagePlus imp) {
		if (!(imp.getProcessor().getMinThreshold()==ImageProcessor.NO_THRESHOLD))
			useLocal = false;		
		GenericDialog gd = new GenericDialog("Convert to Mask");
		gd.addMessage("Convert all images in stack to binary?");
		gd.addCheckbox("Calculate Threshold for Each Image", useLocal);
		gd.addCheckbox("Black Background", Prefs.blackBackground);
		gd.showDialog();
		if (gd.wasCanceled()) return;
		useLocal = gd.getNextBoolean();
		Prefs.blackBackground = gd.getNextBoolean();
		Undo.reset();
		if (useLocal)
			convertStackToBinary(imp);
		else
			applyThreshold(imp);
	}

	void applyThreshold(IjxImagePlus imp) {
		imp.killRoi();
		ImageProcessor ip = imp.getProcessor();
		ip.resetBinaryThreshold();
		int type = imp.getType();
		if (type==IjxImagePlus.GRAY16 || type==IjxImagePlus.GRAY32) {
			applyShortOrFloatThreshold(imp);
			return;
		}
		if (!imp.lock()) return;
		double saveMinThreshold = ip.getMinThreshold();
		double saveMaxThreshold = ip.getMaxThreshold();
		autoThreshold = saveMinThreshold==ImageProcessor.NO_THRESHOLD;
					
		boolean useBlackAndWhite = true;
		if (skipDialog)
			fill1 = fill2 = useBlackAndWhite = true;
		else if (!autoThreshold) {
			GenericDialog gd = new GenericDialog("Make Binary");
			gd.addCheckbox("Thresholded pixels to foreground color", fill1);
			gd.addCheckbox("Remaining pixels to background color", fill2);
			gd.addMessage("");
			gd.addCheckbox("Black foreground, white background", useBW);
			gd.showDialog();
			if (gd.wasCanceled())
				{imp.unlock(); return;}
			fill1 = gd.getNextBoolean();
			fill2 = gd.getNextBoolean();
			useBW = useBlackAndWhite = gd.getNextBoolean();
		} else {
			fill1 = fill2 = true;
			convertToMask = true;
		}

		if (type!=IjxImagePlus.GRAY8)
			convertToByte(imp);
		ip = imp.getProcessor();
		
		if (autoThreshold)
			autoThreshold(ip);
		else {
			if (Recorder.record && (!IJ.isMacro()||Recorder.recordInMacros))
				Recorder.record("setThreshold", (int)saveMinThreshold, (int)saveMaxThreshold);
 			minThreshold = saveMinThreshold;
 			maxThreshold = saveMaxThreshold;
		}

		if (convertToMask && ip.isColorLut())
			ip.setColorModel(ip.getDefaultColorModel());
		int fcolor, bcolor;
		ip.resetThreshold();
		int savePixel = ip.getPixel(0,0);
		if (useBlackAndWhite)
 			ip.setColor(Color.black);
 		else
 			ip.setColor(Toolbar.getForegroundColor());
		ip.drawPixel(0,0);
		fcolor = ip.getPixel(0,0);
		if (useBlackAndWhite)
 			ip.setColor(Color.white);
 		else
 			ip.setColor(Toolbar.getBackgroundColor());
		ip.drawPixel(0,0);
		bcolor = ip.getPixel(0,0);
		ip.setColor(Toolbar.getForegroundColor());
		ip.putPixel(0,0,savePixel);

		int[] lut = new int[256];
		for (int i=0; i<256; i++) {
			if (i>=minThreshold && i<=maxThreshold)
				lut[i] = fill1?fcolor:(byte)i;
			else {
				lut[i] = fill2?bcolor:(byte)i;
			}
		}
		if (imp.getStackSize()>1)
			new StackProcessor(imp.getStack(), ip).applyTable(lut);
		else
			ip.applyTable(lut);
		if (convertToMask) {
			if (!imp.isInvertedLut()) {
				setInvertedLut(imp);
				fcolor = 255 - fcolor;
				bcolor = 255 - bcolor;
			}
			if (Prefs.blackBackground)
				ip.invertLut();
		}
		if (fill1=true && fill2==true && ((fcolor==0&&bcolor==255)||(fcolor==255&&bcolor==0)))
			imp.getProcessor().setThreshold(fcolor, fcolor, ImageProcessor.NO_LUT_UPDATE);
		imp.updateAndRepaintWindow();
		imp.unlock();
	}
	
	void applyShortOrFloatThreshold(IjxImagePlus imp) {
		if (!imp.lock()) return;
		int width = imp.getWidth();
		int height = imp.getHeight();
		int size = width*height;
		boolean isFloat = imp.getType()==IjxImagePlus.GRAY32;
		int nSlices = imp.getStackSize();
		IjxImageStack stack1 = imp.getStack();
		IjxImageStack stack2 = IJ.getFactory().newImageStack(width, height);
		ImageProcessor ip = imp.getProcessor();
		float t1 = (float)ip.getMinThreshold();
		float t2 = (float)ip.getMaxThreshold();
		if (t1==ImageProcessor.NO_THRESHOLD) {
			//ip.resetMinAndMax();
			double min = ip.getMin();
			double max = ip.getMax();
			ip = ip.convertToByte(true);
			autoThreshold(ip);
			t1 = (float)(min + (max-min)*(minThreshold/255.0));
			t2 = (float)(min + (max-min)*(maxThreshold/255.0));
		}
		float value;
		ImageProcessor ip1, ip2;
		IJ.showStatus("Converting to mask");
		for(int i=1; i<=nSlices; i++) {
			IJ.showProgress(i, nSlices);
			String label = stack1.getSliceLabel(i);
			ip1 = stack1.getProcessor(i);
			ip2 = new ByteProcessor(width, height);
			for (int j=0; j<size; j++) {
				value = ip1.getf(j);
				if (value>=t1 && value<=t2)
					ip2.set(j, 255);
				else
					ip2.set(j, 0);
			}
			stack2.addSlice(label, ip2);
		}
		imp.setStack(null, stack2);
		IjxImageStack stack = imp.getStack();
		stack.setColorModel(LookUpTable.createGrayscaleColorModel(!Prefs.blackBackground));
		imp.setStack(null, stack);
		if (imp.isComposite()) {
			CompositeImage ci = (CompositeImage)imp;
			ci.setMode(CompositeImage.GRAYSCALE);
			ci.resetDisplayRanges();
			ci.updateAndDraw();
		}
		imp.getProcessor().setThreshold(255, 255, ImageProcessor.NO_LUT_UPDATE);
		IJ.showStatus("");
		imp.unlock();
	}

	void convertStackToBinary(IjxImagePlus imp) {
		int nSlices = imp.getStackSize();
		if ((imp.getBitDepth()!=8)) {
			IJ.showStatus("Converting to byte");
			IjxImageStack stack1 = imp.getStack();
			IjxImageStack stack2 = IJ.getFactory().newImageStack(imp.getWidth(), imp.getHeight());
			for(int i=1; i<=nSlices; i++) {
				IJ.showProgress(i, nSlices);
				String label = stack1.getSliceLabel(i);
				ImageProcessor ip = stack1.getProcessor(i);
				ip.resetMinAndMax();
				stack2.addSlice(label, ip.convertToByte(true));
			}
			imp.setStack(null, stack2);
		}
		IjxImageStack stack = imp.getStack();
		IJ.showStatus("Auto-thresholding");
		for(int i=1; i<=nSlices; i++) {
			IJ.showProgress(i, nSlices);
			ImageProcessor ip = stack.getProcessor(i);
			autoThreshold(ip);
			int[] lut = new int[256];
			for (int j=0; j<256; j++) {
				if (j>=minThreshold && j<=maxThreshold)
					lut[j] = (byte)255;
				else
					lut[j] = 0;
			}
			ip.applyTable(lut);
		}
		stack.setColorModel(LookUpTable.createGrayscaleColorModel(!Prefs.blackBackground));
		imp.setStack(null, stack);
		imp.getProcessor().setThreshold(255, 255, ImageProcessor.NO_LUT_UPDATE);
		if (imp.isComposite()) {
			CompositeImage ci = (CompositeImage)imp;
			ci.setMode(CompositeImage.GRAYSCALE);
			ci.resetDisplayRanges();
			ci.updateAndDraw();
		}
		IJ.showStatus("");
	}

	void convertToByte(IjxImagePlus imp) {
		ImageProcessor ip;
		int currentSlice =  imp.getCurrentSlice();
		IjxImageStack stack1 = imp.getStack();
		IjxImageStack stack2 = imp.createEmptyStack();
		int nSlices = imp.getStackSize();
		String label;
		for(int i=1; i<=nSlices; i++) {
			label = stack1.getSliceLabel(i);
			ip = stack1.getProcessor(i);
			ip.setMinAndMax(0, 255);
			stack2.addSlice(label, ip.convertToByte(true));
		}
		imp.setStack(null, stack2);
		imp.setSlice(currentSlice);
		imp.setCalibration(imp.getCalibration()); //update calibration
	}
	
	void setInvertedLut(IjxImagePlus imp) {
		ImageProcessor ip = imp.getProcessor();
		ip.invertLut();
		int nImages = imp.getStackSize();
		if (nImages==1)
			ip.invert();
		else {
			IjxImageStack stack = imp.getStack();
			for (int slice=1; slice<=nImages; slice++)
				stack.getProcessor(slice).invert();
			stack.setColorModel(ip.getColorModel());
		}
	}

	void autoThreshold(ImageProcessor ip) {
		ip.setAutoThreshold(ImageProcessor.ISODATA2, ImageProcessor.NO_LUT_UPDATE);
		minThreshold = ip.getMinThreshold();
		maxThreshold = ip.getMaxThreshold();
 	}

}
