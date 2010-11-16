package ij.plugin;
import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.measure.*;
import ij.util.Tools;
import ijx.IjxImagePlus;
import ijx.IjxImageStack;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/** This plugin implements the Edit/Crop and Image/Adjust/Size commands. */
public class Resizer implements PlugIn, TextListener, ItemListener  {
	public static final int IN_PLACE=16, SCALE_T=32;
    private static int newWidth;
    private static int newHeight;
    private static boolean constrain = true;
    private static boolean averageWhenDownsizing = true;
	private static int interpolationMethod = ImageProcessor.BILINEAR;
	private String[] methods = ImageProcessor.getInterpolationMethods();
    private Vector fields, checkboxes;
	private double origWidth, origHeight;
	private boolean sizeToHeight;
 
	public void run(String arg) {
		boolean crop = arg.equals("crop");
		IjxImagePlus imp = IJ.getImage();
		ImageProcessor ip = imp.getProcessor();
		Roi roi = imp.getRoi();
		if (roi==null && crop) {
			IJ.error("Crop", "Area selection required");
			return;
		}
		if (roi!=null && roi.isLine()) {
			IJ.error("The Crop and Adjust>Size commands\ndo not work with line selections.");
			return;
		}
		Rectangle r = ip.getRoi();
		origWidth = r.width;;
		origHeight = r.height;
		sizeToHeight=false;
	    boolean restoreRoi = crop && roi!=null && roi.getType()!=Roi.RECTANGLE;
		if (roi!=null) {
			Rectangle b = roi.getBounds();
			int w = ip.getWidth();
			int h = ip.getHeight();
			if (b.x<0 || b.y<0 || b.x+b.width>w || b.y+b.height>h) {
				ShapeRoi shape1 = new ShapeRoi(roi);
				ShapeRoi shape2 = new ShapeRoi(new Roi(0, 0, w, h));
				roi = shape2.and(shape1);
				if (restoreRoi) imp.setRoi(roi);
			}
		}
		int stackSize= imp.getStackSize();
		int z1 = imp.getStackSize();
		int t1 = 0;
		int z2=0, t2=0;
		if (crop) {
			Rectangle bounds = roi.getBounds();
			newWidth = bounds.width;
			newHeight = bounds.height;
			interpolationMethod = ImageProcessor.NONE;
		} else {
			if (newWidth==0 || newHeight==0) {
				newWidth = (int)origWidth/2;
				newHeight = (int)origHeight/2;
			}
			if (constrain) newHeight = (int)(newWidth*(origHeight/origWidth));
			if (stackSize>1) {
				newWidth = (int)origWidth;
				newHeight = (int)origHeight;
			}
			GenericDialog gd = new GenericDialog("Resize", IJ.getTopComponentFrame());
			gd.addNumericField("Width (pixels):", newWidth, 0);
			gd.addNumericField("Height (pixels):", newHeight, 0);
			if (imp.isHyperStack()) {
				z1 = imp.getNSlices();
				t1 = imp.getNFrames();
			}
			if (z1>1 && z1==stackSize)
				gd.addNumericField("Depth (images):", z1, 0);
			else if (z1>1 && z1<stackSize)
				gd.addNumericField("Depth (slices):", z1, 0);
			if (t1>1)
				gd.addNumericField("Time (frames):", t1, 0);
			gd.addCheckbox("Constrain aspect ratio", constrain);
			gd.addCheckbox("Average when downsizing", averageWhenDownsizing);
			gd.addChoice("Interpolation:", methods, methods[interpolationMethod]);
			fields = gd.getNumericFields();
			for (int i=0; i<2; i++)
				((TextField)fields.elementAt(i)).addTextListener(this);
			checkboxes = gd.getCheckboxes();
			((Checkbox)checkboxes.elementAt(0)).addItemListener(this);
			gd.showDialog();
			if (gd.wasCanceled())
				return;
			newWidth = (int)gd.getNextNumber();
			newHeight = (int)gd.getNextNumber();
			if (z1>1)
				z2 = (int)gd.getNextNumber();
			if (t1>1)
				t2 = (int)gd.getNextNumber();
			if (gd.invalidNumber()) {
				IJ.error("Width or height are invalid.");
				return;
			}
			constrain = gd.getNextBoolean();
			averageWhenDownsizing = gd.getNextBoolean();
			interpolationMethod = gd.getNextChoiceIndex();
			if (constrain && newWidth==0)
				sizeToHeight = true;
			if (newWidth<=0.0 && !constrain)  newWidth = 50;
			if (newHeight<=0.0) newHeight = 50;
		}
		
		if (!crop && constrain) {
			if (sizeToHeight)
				newWidth = (int)Math.round(newHeight*(origWidth/origHeight));
			else
				newHeight = (int)Math.round(newWidth*(origHeight/origWidth));
		}
		if (ip.getWidth()==1 || ip.getHeight()==1)
			ip.setInterpolationMethod(ImageProcessor.NONE);
		else
			ip.setInterpolationMethod(interpolationMethod);
		if (!crop && stackSize==1)
			Undo.setup(Undo.TYPE_CONVERSION, imp);
			    	
		if (roi!=null || newWidth!=origWidth || newHeight!=origHeight) {
			try {
				StackProcessor sp = new StackProcessor(imp.getStack(), ip);
				IjxImageStack s2 = sp.resize(newWidth, newHeight, averageWhenDownsizing);
				int newSize = s2.getSize();
				if (s2.getWidth()>0 && newSize>0) {
					if (restoreRoi)
						imp.killRoi();
					//imp.hide();
					Calibration cal = imp.getCalibration();
					if (cal.scaled()) {
						cal.pixelWidth *= origWidth/newWidth;
						cal.pixelHeight *= origHeight/newHeight;
						imp.setCalibration(cal);
					}
					imp.setStack(null, s2);
					if (restoreRoi && roi!=null) {
						roi.setLocation(0, 0);
						imp.setRoi(roi);
						imp.draw();
					}
				}
				if (stackSize>1 && newSize<stackSize)
					IJ.error("ImageJ ran out of memory causing \nthe last "+(stackSize-newSize)+" slices to be lost.");
			} catch(OutOfMemoryError o) {
				IJ.outOfMemory("Resize");
			}
			imp.setChanged(true);
		}
		
		IjxImagePlus imp2 = null;
		if (z2>0 && z2!=z1)
			imp2 = zScale(imp, z2, interpolationMethod+IN_PLACE);
		if (t2>0 && t2!=t1)
			imp2 = zScale(imp2!=null?imp2:imp, t2, interpolationMethod+IN_PLACE+SCALE_T);
		if (imp2!=null && imp2!=imp) {
			imp2.show();
			imp.setChanged(false);
			imp.close();
		}
	}

	public IjxImagePlus zScale(IjxImagePlus imp, int newDepth, int interpolationMethod) {
		IjxImagePlus imp2 = null;
		if (imp.isHyperStack())
			imp2 = zScaleHyperstack(imp, newDepth, interpolationMethod);
		else {
			boolean inPlace = (interpolationMethod&IN_PLACE)!=0;
			interpolationMethod = interpolationMethod&15;
			int stackSize = imp.getStackSize();
			int bitDepth = imp.getBitDepth();
			if (newDepth<=stackSize/2 && interpolationMethod==ImageProcessor.NONE)
				imp2 = shrinkZ(imp, newDepth, inPlace);
			else
				imp2 = resizeZ(imp, newDepth, interpolationMethod);
			if (imp2==null) return null;
			ImageProcessor ip = imp.getProcessor();
			double min = ip.getMin();
			double max = ip.getMax();
			if (bitDepth==16||bitDepth==32)
				imp2.getProcessor().setMinAndMax(min, max);
		}
		if (imp2==null) return null;
		if (imp2!=imp && imp.isComposite()) {
			imp2 = new CompositeImage(imp2, ((CompositeImage)imp).getMode());
			((CompositeImage)imp2).copyLuts(imp);
		}
		imp2.setCalibration(imp.getCalibration());
		Calibration cal = imp2.getCalibration();
		if (cal.scaled()) cal.pixelDepth *= (double)imp.getNSlices()/imp2.getNSlices();
		Object info = imp.getProperty("Info");
		if (info!=null) imp2.setProperty("Info", info);
		if (imp.isHyperStack())
			imp2.setOpenAsHyperStack(imp.isHyperStack());
		return imp2;
	}

	private IjxImagePlus zScaleHyperstack(IjxImagePlus imp, int depth2, int interpolationMethod) {
		boolean inPlace = (interpolationMethod&IN_PLACE)!=0;
		boolean scaleT = (interpolationMethod&SCALE_T)!=0;
		interpolationMethod = interpolationMethod&15;
		int channels = imp.getNChannels();
		int slices = imp.getNSlices();
		int frames = imp.getNFrames();
		int slices2 = slices;
		int frames2 = frames;
		int bitDepth = imp.getBitDepth();
		if (slices==1 && frames>1)
			scaleT = true;
		if (scaleT)
			frames2 = depth2;
		else
			slices2 = depth2;
		double scale = (double)(depth2-1)/slices;
		if (scaleT) scale = (double)(depth2-1)/frames;
		if (scale<=0.5 && interpolationMethod==ImageProcessor.NONE)
			return shrinkHyperstack(imp, depth2, inPlace, scaleT);
		IjxImageStack stack1 = imp.getStack();
		int width = stack1.getWidth();
		int height = stack1.getHeight();
		IjxImagePlus imp2 = IJ.createImage(imp.getTitle(), bitDepth+"-bit", width, height, channels*slices2*frames2);
		if (imp2==null) return null;
		imp2.setDimensions(channels, slices2, frames2);
		IjxImageStack stack2 = imp2.getStack();
		ImageProcessor ip = imp.getProcessor();
		int count = 0;
		if (scaleT) {
			IJ.showStatus("T Scaling...");
			ImageProcessor xtPlane1 = ip.createProcessor(width, frames);
			xtPlane1.setInterpolationMethod(interpolationMethod);
			ImageProcessor xtPlane2;		
			Object xtpixels1 = xtPlane1.getPixels();
			int last = slices*channels*height-1;
			for (int z=1; z<=slices; z++) {
				for (int c=1; c<=channels; c++) {
					for (int y=0; y<height; y++) {
						IJ.showProgress(count++, last);
						for (int t=1; t<=frames; t++) {
							int index = imp.getStackIndex(c, z, t);
							//IJ.log("1: "+c+" "+z+" "+t+" "+index+" "+xzPlane1);
							Object pixels1 = stack1.getPixels(index);
							System.arraycopy(pixels1, y*width, xtpixels1, (t-1)*width, width);
						}
						xtPlane2 = xtPlane1.resize(width, depth2, averageWhenDownsizing);
						Object xtpixels2 = xtPlane2.getPixels();
						for (int t=1; t<=frames2; t++) {
							int index = imp2.getStackIndex(c, z, t);
							//IJ.log("2: "+c+" "+z+" "+t+" "+index+" "+xzPlane2);
							Object pixels2 = stack2.getPixels(index);
							System.arraycopy(xtpixels2, (t-1)*width, pixels2, y*width, width);
						}
					}
				}
			}
		} else {
			IJ.showStatus("Z Scaling...");
			ImageProcessor xzPlane1 = ip.createProcessor(width, slices);
			xzPlane1.setInterpolationMethod(interpolationMethod);
			ImageProcessor xzPlane2;		
			Object xypixels1 = xzPlane1.getPixels();
			int last = frames*channels*height-1;
			for (int t=1; t<=frames; t++) {
				for (int c=1; c<=channels; c++) {
					for (int y=0; y<height; y++) {
						IJ.showProgress(count++, last);
						for (int z=1; z<=slices; z++) {
							int index = imp.getStackIndex(c, z, t);
							Object pixels1 = stack1.getPixels(index);
							System.arraycopy(pixels1, y*width, xypixels1, (z-1)*width, width);
						}
						xzPlane2 = xzPlane1.resize(width, depth2, averageWhenDownsizing);
						Object xypixels2 = xzPlane2.getPixels();
						for (int z=1; z<=slices2; z++) {
							int index = imp2.getStackIndex(c, z, t);
							Object pixels2 = stack2.getPixels(index);
							System.arraycopy(xypixels2, (z-1)*width, pixels2, y*width, width);
						}
					}
				}
			}
		}
		imp2.setDimensions(channels, slices2, frames2);
		return imp2;
	}

	private IjxImagePlus shrinkHyperstack(IjxImagePlus imp, int newDepth, boolean inPlace, boolean scaleT) {
		int channels = imp.getNChannels();
		int slices = imp.getNSlices();
		int frames = imp.getNFrames();
		int factor = (int)Math.round((double)slices/newDepth);
		if (scaleT) factor = frames/newDepth;
		int zfactor = scaleT?1:factor;
		int tfactor = scaleT?factor:1;
		IjxImageStack stack = imp.getStack();
		IjxImageStack stack2 = IJ.getFactory().newImageStack(imp.getWidth(), imp.getHeight());
		boolean virtual = stack.isVirtual();
		int slices2 = slices/zfactor + ((slices%zfactor)!=0?1:0);
		int frames2 = frames/tfactor + ((frames%tfactor)!=0?1:0);
		int n = channels*slices2*frames2;
		int count = 1;
		for (int t=1; t<=frames; t+=tfactor) {
			for (int z=1; z<=slices; z+=zfactor) {
				for (int c=1; c<=channels; c++) {
					int i = imp.getStackIndex(c, z, t);
					IJ.showProgress(i, n);
					ImageProcessor ip = stack.getProcessor(imp.getStackIndex(c, z, t));
					if (!inPlace) ip=ip.duplicate();
					//IJ.log(count++ +"  "+i+" "+c+" "+z+" "+t);
					stack2.addSlice(stack.getSliceLabel(i), ip);
				}
			}
		}
		IjxImagePlus imp2 = IJ.getFactory().newImagePlus(imp.getTitle(), stack2);
		imp2.setDimensions(channels, slices2, frames2);
		IJ.showProgress(1.0);
		return imp2;
	}

	private IjxImagePlus shrinkZ(IjxImagePlus imp, int newDepth, boolean inPlace) {
		IjxImageStack stack = imp.getStack();
		int factor = imp.getStackSize()/newDepth;
		boolean virtual = stack.isVirtual();
		int n = stack.getSize();
		IjxImageStack stack2 = IJ.getFactory().newImageStack(stack.getWidth(), stack.getHeight());
		for (int i=1; i<=n; i+=factor) {
			if (virtual) IJ.showProgress(i, n);
			ImageProcessor ip2 = stack.getProcessor(i);
			if (!inPlace) ip2 = ip2.duplicate();
			stack2.addSlice(stack.getSliceLabel(i), ip2);
		}
		return IJ.getFactory().newImagePlus(imp.getTitle(), stack2);
	}

	private IjxImagePlus resizeZ(IjxImagePlus imp, int newDepth, int interpolationMethod) {
		IjxImageStack stack1 = imp.getStack();
		int width = stack1.getWidth();
		int height = stack1.getHeight();
		int depth = stack1.getSize();
		int bitDepth = imp.getBitDepth();
		IjxImagePlus imp2 = IJ.createImage(imp.getTitle(), bitDepth+"-bit", width, height, newDepth);
		if (imp2==null) return null;
		IjxImageStack stack2 = imp2.getStack();
		ImageProcessor ip = imp.getProcessor();
		ImageProcessor xzPlane1 = ip.createProcessor(width, depth);
		xzPlane1.setInterpolationMethod(interpolationMethod);
		ImageProcessor xzPlane2;		
		Object xypixels1 = xzPlane1.getPixels();
		IJ.showStatus("Z Scaling...");
		for (int y=0; y<height; y++) {
			IJ.showProgress(y, height-1);
			for (int z=0; z<depth; z++) {
				Object pixels1 = stack1.getPixels(z+1);
				System.arraycopy(pixels1, y*width, xypixels1, z*width, width);
			}
			xzPlane2 = xzPlane1.resize(width, newDepth, averageWhenDownsizing);
			Object xypixels2 = xzPlane2.getPixels();
			for (int z=0; z<newDepth; z++) {
				Object pixels2 = stack2.getPixels(z+1);
				System.arraycopy(xypixels2, z*width, pixels2, y*width, width);
			}
		}
		return imp2;
	}

    public void textValueChanged(TextEvent e) {
    	TextField widthField = (TextField)fields.elementAt(0);
    	TextField heightField = (TextField)fields.elementAt(1);
        int width = (int)Tools.parseDouble(widthField.getText(),-99);
        int height = (int)Tools.parseDouble(heightField.getText(),-99);
        if (width==-99 || height==-99)
        	return;
        if (constrain) {
        	if (width!=newWidth) {
         		sizeToHeight = false;
        		newWidth = width;
				updateFields();
         	} else if (height!=newHeight) {
         		sizeToHeight = true;
        		newHeight = height;
				updateFields();
			}
        }
    }
    
    void updateFields() {
		if (sizeToHeight) {
			newWidth = (int)(newHeight*(origWidth/origHeight));
			TextField widthField = (TextField)fields.elementAt(0);
			widthField.setText(""+newWidth);
		} else {
			newHeight = (int)(newWidth*(origHeight/origWidth));
			TextField heightField = (TextField)fields.elementAt(1);
			heightField.setText(""+newHeight);
		}
   }

	public void itemStateChanged(ItemEvent e) {
		Checkbox cb = (Checkbox)checkboxes.elementAt(0);
        boolean newConstrain = cb.getState();
        if (newConstrain && newConstrain!=constrain)
        	updateFields();
        constrain = newConstrain;
	}

}
