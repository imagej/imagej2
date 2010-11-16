package ij.process;

import java.awt.*;
import java.awt.image.*;
import ij.*;
import ij.gui.*;
import ij.measure.*;
import ijx.IjxImagePlus;
import ijx.IjxImageStack;

/** This class does stack type conversions. */
public class StackConverter {
	IjxImagePlus imp;
	int type, nSlices, width, height;

	public StackConverter(IjxImagePlus imp) {
		this.imp = imp;
		type = imp.getType();
		nSlices = imp.getStackSize();
		if (nSlices<2)
			throw new IllegalArgumentException("Stack required");
		width = imp.getWidth();
		height = imp.getHeight();
	}
	
	/** Converts this Stack to 8-bit grayscale. */
	public void convertToGray8() {
		IjxImageStack stack1 = imp.getStack();
		int currentSlice =  imp.getCurrentSlice();
		ImageProcessor ip = imp.getProcessor();
		boolean colorLut = ip.isColorLut();
		boolean pseudoColorLut = colorLut && ip.isPseudoColorLut();
		if (type==IjxImagePlus.GRAY8 && pseudoColorLut) {
			boolean invertedLut = ip.isInvertedLut();
			ip.setColorModel(LookUpTable.createGrayscaleColorModel(invertedLut));
			stack1.setColorModel(ip.getColorModel());
	    	imp.updateAndDraw();
			return;
		}
		if (type==IjxImagePlus.COLOR_RGB || type==IjxImagePlus.COLOR_256 || colorLut) {
			convertRGBToGray8();
			imp.setSlice(currentSlice);
			return;
		}
		
		IjxImageStack stack2 = IJ.getFactory().newImageStack(width, height);
		Image img;
		String label;
		double min = ip.getMin();
		double max = ip.getMax();
	    int inc = nSlices/20;
	    if (inc<1) inc = 1;
	    LUT[] luts = imp.isComposite()?((CompositeImage)imp).getLuts():null;
		for(int i=1; i<=nSlices; i++) {
			label = stack1.getSliceLabel(1);
			ip = stack1.getProcessor(1);
			stack1.deleteSlice(1);
			if (luts!=null) {
				int index = (i-1)%luts.length;
				min = luts[index].min;
				max = luts[index].max;
			}
			ip.setMinAndMax(min, max);
			boolean scale = ImageConverter.getDoScaling();
			stack2.addSlice(label, ip.convertToByte(scale));
			if ((i%inc)==0) {
				IJ.showProgress((double)i/nSlices);
				IJ.showStatus("Converting to 8-bits: "+i+"/"+nSlices);
			}
		}
		imp.setStack(null, stack2);
		imp.setCalibration(imp.getCalibration()); //update calibration
		if (imp.isComposite()) {
			((CompositeImage)imp).resetDisplayRanges();
			((CompositeImage)imp).updateAllChannelsAndDraw();
		}
		imp.setSlice(currentSlice);
		IJ.showProgress(1.0);
	}

	/** Converts an RGB or 8-bit color stack to 8-bit grayscale. */
	void convertRGBToGray8() {
		IjxImageStack stack1 = imp.getStack();
		IjxImageStack stack2 = IJ.getFactory().newImageStack(width, height);
		ImageProcessor ip;
		Image img;
		String label;
	    int inc = nSlices/20;
	    if (inc<1) inc = 1;
		for(int i=1; i<=nSlices; i++) {
			label = stack1.getSliceLabel(1);
			ip = stack1.getProcessor(1);
			stack1.deleteSlice(1);
			if (ip instanceof ByteProcessor)
				ip = new ColorProcessor(ip.createImage());
			boolean scale = ImageConverter.getDoScaling();
			stack2.addSlice(label, ip.convertToByte(scale));
			if ((i%inc)==0) {
				IJ.showProgress((double)i/nSlices);
				IJ.showStatus("Converting to 8-bits: "+i+"/"+nSlices);
			}
		}
		imp.setStack(null, stack2);
		IJ.showProgress(1.0);
	}

	/** Converts this Stack to 16-bit grayscale. */
	public void convertToGray16() {
		if (type==IjxImagePlus.GRAY16)
			return;
		if (!(type==IjxImagePlus.GRAY8 || type==IjxImagePlus.GRAY32))
			throw new IllegalArgumentException("Unsupported conversion");
		IjxImageStack stack1 = imp.getStack();
		IjxImageStack stack2 = IJ.getFactory().newImageStack(width, height);
		String label;
	    int inc = nSlices/20;
	    if (inc<1) inc = 1;
	    boolean scale = type==IjxImagePlus.GRAY32 && ImageConverter.getDoScaling();
	    ImageProcessor ip1, ip2;
		for(int i=1; i<=nSlices; i++) {
			label = stack1.getSliceLabel(1);
			ip1 = stack1.getProcessor(1);
			ip2 = ip1.convertToShort(scale);
			stack1.deleteSlice(1);
			stack2.addSlice(label, ip2);
			if ((i%inc)==0) {
				IJ.showProgress((double)i/nSlices);
				IJ.showStatus("Converting to 16-bits: "+i+"/"+nSlices);
			}
		}
		IJ.showProgress(1.0);
		imp.setStack(null, stack2);
	}

	/** Converts this Stack to 32-bit (float) grayscale. */
	public void convertToGray32() {
		if (type==IjxImagePlus.GRAY32)
			return;
		if (!(type==IjxImagePlus.GRAY8||type==IjxImagePlus.GRAY16))
			throw new IllegalArgumentException("Unsupported conversion");
		IjxImageStack stack1 = imp.getStack();
		IjxImageStack stack2 = IJ.getFactory().newImageStack(width, height);
		String label;
	    int inc = nSlices/20;
	    if (inc<1) inc = 1;
	    ImageProcessor ip1, ip2;
	    Calibration cal = imp.getCalibration();
		for(int i=1; i<=nSlices; i++) {
			label = stack1.getSliceLabel(1);
			ip1 = stack1.getProcessor(1);
			ip1.setCalibrationTable(cal.getCTable());
			ip2 = ip1.convertToFloat();
			stack1.deleteSlice(1);
			stack2.addSlice(label, ip2);
			if ((i%inc)==0) {
				IJ.showProgress((double)i/nSlices);
				IJ.showStatus("Converting to 32-bits: "+i+"/"+nSlices);
			}
		}
		IJ.showProgress(1.0);
		imp.setStack(null, stack2);
		imp.setCalibration(imp.getCalibration()); //update calibration
	}

	/** Converts the Stack to RGB. */
	public void convertToRGB() {
		if (imp.isComposite())
			throw new IllegalArgumentException("Use Image>Color>Stack to RGB");
		IjxImageStack stack1 = imp.getStack();
		IjxImageStack stack2 = IJ.getFactory().newImageStack(width, height);
		String label;
	    int inc = nSlices/20;
	    if (inc<1) inc = 1;
	    ImageProcessor ip1, ip2;
	    Calibration cal = imp.getCalibration();
		for(int i=1; i<=nSlices; i++) {
			label = stack1.getSliceLabel(i);
			ip1 = stack1.getProcessor(i);
			ip2 = ip1.convertToRGB();
			stack2.addSlice(label, ip2);
			if ((i%inc)==0) {
				IJ.showProgress((double)i/nSlices);
				IJ.showStatus("Converting to RGB: "+i+"/"+nSlices);
			}
		}
		IJ.showProgress(1.0);
		imp.setStack(null, stack2);
		imp.setCalibration(imp.getCalibration()); //update calibration
	}

	/** Converts the stack (which must be RGB) to a 
		3 channel (red, green and blue) hyperstack. */
	public void convertToRGBHyperstack() {
		if (type!=IjxImagePlus.COLOR_RGB)
			throw new IllegalArgumentException("RGB stack required");
		new ij.plugin.CompositeConverter().run("composite");
	}

	/** Converts the stack (which must be RGB) to a 3 channel 
		(hue, saturation and brightness) hyperstack. */
	public void convertToHSBHyperstack() {
		if (type!=IjxImagePlus.COLOR_RGB)
			throw new IllegalArgumentException("RGB stack required");
		IjxImageStack stack1 = imp.getStack();
		IjxImageStack stack2 = IJ.getFactory().newImageStack(width,height);
		int nSlices = stack1.getSize();
		Calibration cal = imp.getCalibration();
		int inc = nSlices/20;
		if (inc<1) inc = 1;
		for(int i=1; i<=nSlices; i++) {
			String label = stack1.getSliceLabel(i);
			ColorProcessor cp = (ColorProcessor)stack1.getProcessor(i);
			IjxImageStack stackHSB = cp.getHSBStack();
			stack2.addSlice(label,stackHSB.getProcessor(1));
			stack2.addSlice(label,stackHSB.getProcessor(2));
			stack2.addSlice(label,stackHSB.getProcessor(3));
			if ((i%inc)==0) {
				IJ.showProgress((double)i/nSlices);
				IJ.showStatus("Converting to HSB: "+i+"/"+nSlices);
			}
		}
		IJ.showProgress(1.0);
		imp.setStack(null,stack2);
		imp.setCalibration(cal);
		imp.setDimensions(3, nSlices, 1);
		CompositeImage ci = new CompositeImage(imp, CompositeImage.GRAYSCALE);
		ci.show();
		imp.hide();
	}

	/** Converts the stack to 8-bits indexed color. 'nColors' must
		be greater than 1 and less than or equal to 256. */
	public void convertToIndexedColor(int nColors) {
		if (type!=IjxImagePlus.COLOR_RGB)
			throw new IllegalArgumentException("RGB stack required");
		IjxImageStack stack = imp.getStack();
		int size = stack.getSize();
		ImageProcessor montage = new ColorProcessor(width*size, height);
        for (int i=0; i<size; i++)
            montage.insert(stack.getProcessor(i+1), i*width, 0);
        MedianCut mc = new MedianCut((ColorProcessor)montage);
        montage = mc.convertToByte(nColors);
        IjxImageStack stack2 = IJ.getFactory().newImageStack(width, height);
        for (int i=0; i<size; i++) {
            montage.setRoi(i*width, 0, width, height);
            stack2.addSlice(null, montage.crop());
        }
		imp.setStack(null, stack2);
	}

}
