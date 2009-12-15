package ij.plugin;
import ijx.IjxImagePlus;
import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.measure.Calibration;
import ijx.IjxImageStack;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;

/** Implements the Image/Stacks/Make Montage command. */
public class MontageMaker implements PlugIn {
			
	private static int columns, rows, first, last, inc, borderWidth;
	private static double scale;
	private static boolean label;
	private static boolean useForegroundColor;
	private static int saveID;

	public void run(String arg) {
		IjxImagePlus imp = WindowManager.getCurrentImage();
		if (imp==null || imp.getStackSize()==1)
			{IJ.error("Stack required"); return;}
		int channels = imp.getNChannels();
		if (imp.isComposite() && channels>1) {
			int channel = imp.getChannel();
			CompositeImage ci = (CompositeImage)imp;
			int mode = ci.getMode();
			if (mode==CompositeImage.COMPOSITE)
				ci.setMode(CompositeImage.COLOR);
			ImageStack stack = new ImageStack(imp.getWidth(), imp.getHeight());
			for (int c=1; c<=channels; c++) {
				imp.setPosition(c, imp.getSlice(), imp.getFrame());
				Image img = imp.getImage();
				stack.addSlice(null, new ColorProcessor(img));
			}
			if (ci.getMode()!=mode)
				ci.setMode(mode);
			imp.setPosition(channel, imp.getSlice(), imp.getFrame());
			imp = IJ.getFactory().newImagePlus(imp.getTitle(), stack);
		}
		makeMontage(imp);
		imp.updateImage();
		saveID = imp.getID();
	}
	
	public void makeMontage(IjxImagePlus imp) {
			int nSlices = imp.getStackSize();
			if (columns==0 || imp.getID()!=saveID) {
				columns = (int)Math.sqrt(nSlices);
				rows = columns;
				int n = nSlices - columns*rows;
				if (n>0) columns += (int)Math.ceil((double)n/rows);
				scale = 1.0;
				if (imp.getWidth()*columns>800)
					scale = 0.5;
				if (imp.getWidth()*columns>1600)
					scale = 0.25;
				inc = 1;
				first = 1;
				last = nSlices;
			}
			
			GenericDialog gd = new GenericDialog("Make Montage", IJ.getTopComponentFrame());
			gd.addNumericField("Columns:", columns, 0);
			gd.addNumericField("Rows:", rows, 0);
			gd.addNumericField("Scale Factor:", scale, 2);
			gd.addNumericField("First Slice:", first, 0);
			gd.addNumericField("Last Slice:", last, 0);
			gd.addNumericField("Increment:", inc, 0);
			gd.addNumericField("Border Width:", borderWidth, 0);
			gd.addCheckbox("Label Slices", label);
			gd.addCheckbox("Use Foreground Color", useForegroundColor);
			gd.showDialog();
			if (gd.wasCanceled())
				return;
			columns = (int)gd.getNextNumber();
			rows = (int)gd.getNextNumber();
			scale = gd.getNextNumber();
			first = (int)gd.getNextNumber();
			last = (int)gd.getNextNumber();
			inc = (int)gd.getNextNumber();
			borderWidth = (int)gd.getNextNumber();
			if (borderWidth<0) borderWidth = 0;
			if (first<1) first = 1;
			if (last>nSlices) last = nSlices;
			if (first>last)
				{first=1; last=nSlices;}
			if (inc<1) inc = 1;
			if (gd.invalidNumber()) {
				IJ.error("Invalid number");
				return;
			}
			label = gd.getNextBoolean();
			useForegroundColor = gd.getNextBoolean();
			makeMontage(imp, columns, rows, scale, first, last, inc, borderWidth, label);
	}
	
	public void makeMontage(IjxImagePlus imp, int columns, int rows, double scale, int first, int last, int inc, int borderWidth, boolean labels) {
		int stackWidth = imp.getWidth();
		int stackHeight = imp.getHeight();
		int nSlices = imp.getStackSize();
		int width = (int)(stackWidth*scale);
		int height = (int)(stackHeight*scale);
		int montageWidth = width*columns;
		int montageHeight = height*rows;
		ImageProcessor ip = imp.getProcessor();
		ImageProcessor montage = ip.createProcessor(montageWidth+borderWidth/2, montageHeight+borderWidth/2);
		Color fgColor=Color.white;
		Color bgColor = Color.black;
		if (useForegroundColor) {
			fgColor = Toolbar.getForegroundColor();
			bgColor = Toolbar.getBackgroundColor();
		} else {
			boolean whiteBackground = false;
			if ((ip instanceof ByteProcessor) || (ip instanceof ColorProcessor)) {
				ImageStatistics is = imp.getStatistics();
				whiteBackground = is.mode>=200;
				if (imp.isInvertedLut())
					whiteBackground = !whiteBackground;
			}
			if (whiteBackground) {
				fgColor=Color.black;
				bgColor = Color.white;
			}
		}
		montage.setColor(bgColor);
		montage.fill();
		montage.setColor(fgColor);
		IjxImageStack stack = imp.getStack();
		int x = 0;
		int y = 0;
		ImageProcessor aSlice;
	    int slice = first;
		while (slice<=last) {
			aSlice = stack.getProcessor(slice);
			if (scale!=1.0)
				aSlice = aSlice.resize(width, height);
			montage.insert(aSlice, x, y);
			String label = stack.getShortSliceLabel(slice);
			if (borderWidth>0) drawBorder(montage, x, y, width, height, borderWidth);
			if (labels) drawLabel(montage, slice, label, x, y, width, height);
			x += width;
			if (x>=montageWidth) {
				x = 0;
				y += height;
				if (y>=montageHeight)
					break;
			}
			IJ.showProgress((double)(slice-first)/(last-first));
			slice += inc;
		}
		if (borderWidth>0) {
			int w2 = borderWidth/2;
			drawBorder(montage, w2, w2, montageWidth-w2, montageHeight-w2, borderWidth);
		}
		IJ.showProgress(1.0);
		IjxImagePlus imp2 = IJ.getFactory().newImagePlus("Montage", montage);
		imp2.setCalibration(imp.getCalibration());
		Calibration cal = imp2.getCalibration();
		if (cal.scaled()) {
			cal.pixelWidth /= scale;
			cal.pixelHeight /= scale;
		}
        imp2.setProperty("Info", "xMontage="+columns+"\nyMontage="+rows+"\n");
		imp2.show();
	}
		
	void drawBorder(ImageProcessor montage, int x, int y, int width, int height, int borderWidth) {
		montage.setLineWidth(borderWidth);
		montage.moveTo(x, y);
		montage.lineTo(x+width, y);
		montage.lineTo(x+width, y+height);
		montage.lineTo(x, y+height);
		montage.lineTo(x, y);
	}
	
	void drawLabel(ImageProcessor montage, int slice, String label, int x, int y, int width, int height) {
		if (label!=null && !label.equals("") && montage.getStringWidth(label)>=width) {
			do {
				label = label.substring(0, label.length()-1);
			} while (label.length()>1 && montage.getStringWidth(label)>=width);
		}
		if (label==null || label.equals(""))
			label = ""+slice;
		int swidth = montage.getStringWidth(label);
		x += width/2 - swidth/2;
		y += height;
		montage.moveTo(x, y); 
		montage.drawString(label);
	}
}


