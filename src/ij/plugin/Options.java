package ij.plugin;
import ijx.gui.IjxImageWindow;
import ijx.IjxImagePlus;
import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.io.*;
import ij.plugin.filter.*;
import ij.plugin.frame.LineWidthAdjuster;
import ijx.IjxImageStack;
import java.awt.*;

/** This plugin implements most of the commands
	in the Edit/Options sub-menu. */
public class Options implements PlugIn {

 	public void run(String arg) {
		if (arg.equals("misc"))
			{miscOptions(); return;}
		else if (arg.equals("line"))
			{lineWidth(); return;}
		else if (arg.equals("io"))
			{io(); return;}
		else if (arg.equals("conv"))
			{conversions(); return;}
		else if (arg.equals("display"))
			{appearance(); return;}
	}
				
	// Miscellaneous Options
	void miscOptions() {
		String key = IJ.isMacintosh()?"Command":"Control";
		GenericDialog gd = new GenericDialog("Miscellaneous Options", IJ.getTopComponentFrame());
		gd.addStringField("Divide by Zero Value:", ""+FloatBlitter.divideByZeroValue, 10);
		gd.addCheckbox("Use Pointer Cursor", Prefs.usePointerCursor);
		gd.addCheckbox("Hide \"Process Stack?\" Dialog", IJ.hideProcessStackDialog);
		//gd.addCheckbox("Antialiased_Text", Prefs.antialiasedText);
		gd.addCheckbox("Antialiased_Tool Icons", Prefs.antialiasedTools);
		gd.addCheckbox("Require "+key+" Key for Shortcuts", Prefs.requireControlKey);
		gd.addCheckbox("Move Isolated Plugins to Misc. Menu", Prefs.moveToMisc);
		gd.addCheckbox("Debug Mode", IJ.debugMode);
		gd.showDialog();
		if (gd.wasCanceled())
			return;
			
		String divValue = gd.getNextString();
		if (divValue.equalsIgnoreCase("infinity") || divValue.equalsIgnoreCase("infinite"))
			FloatBlitter.divideByZeroValue = Float.POSITIVE_INFINITY;
		else if (divValue.equalsIgnoreCase("NaN"))
			FloatBlitter.divideByZeroValue = Float.NaN;
		else if (divValue.equalsIgnoreCase("max"))
			FloatBlitter.divideByZeroValue = Float.MAX_VALUE;
		else {
			Float f;
			try {f = new Float(divValue);}
			catch (NumberFormatException e) {f = null;}
			if (f!=null)
				FloatBlitter.divideByZeroValue = f.floatValue();
		}
		IJ.register(FloatBlitter.class); 
			
		Prefs.usePointerCursor = gd.getNextBoolean();
		IJ.hideProcessStackDialog = gd.getNextBoolean();
		//Prefs.antialiasedText = gd.getNextBoolean();
		boolean antialiasedTools = gd.getNextBoolean();
		boolean change = antialiasedTools!=Prefs.antialiasedTools;
		Prefs.antialiasedTools = antialiasedTools;
		if (change) Toolbar.getInstance().repaint();
		Prefs.requireControlKey = gd.getNextBoolean();
		Prefs.moveToMisc = gd.getNextBoolean();
		IJ.debugMode = gd.getNextBoolean();
	}

	void lineWidth() {
		int width = (int)IJ.getNumber("Line Width:", Line.getWidth());
		if (width==IJ.CANCELED) return;
		Line.setWidth(width);
		LineWidthAdjuster.update();
		IjxImagePlus imp = WindowManager.getCurrentImage();
		if (imp!=null && imp.isProcessor()) {
			ImageProcessor ip = imp.getProcessor();
			ip.setLineWidth(Line.getWidth());
            Roi roi = imp.getRoi();
            if (roi!=null && roi.isLine()) imp.draw();
		}
	}

	// Input/Output options
	void io() {
		GenericDialog gd = new GenericDialog("I/O Options");
		gd.addNumericField("JPEG Quality (0-100):", FileSaver.getJpegQuality(), 0, 3, "");
		gd.addNumericField("GIF and PNG Transparent Index:", Prefs.getTransparentIndex(), 0, 3, "");
		gd.addStringField("File Extension for Tables:", Prefs.get("options.ext", ".xls"), 4);
		gd.addCheckbox("Use JFileChooser to Open/Save", Prefs.useJFileChooser);
		gd.addCheckbox("Save TIFF and Raw in Intel Byte Order", Prefs.intelByteOrder);
		gd.addCheckbox("Copy Column Headers", Prefs.copyColumnHeaders);
		gd.addCheckbox("Copy Row Numbers", !Prefs.noRowNumbers);
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		int quality = (int)gd.getNextNumber();
		if (quality<0) quality = 0;
		if (quality>100) quality = 100;
		FileSaver.setJpegQuality(quality);
		int transparentIndex = (int)gd.getNextNumber();
		Prefs.setTransparentIndex(transparentIndex);
		String extension = gd.getNextString();
		if (!extension.startsWith("."))
			extension = "." + extension;
		Prefs.set("options.ext", extension);
		Prefs.useJFileChooser = gd.getNextBoolean();
		Prefs.intelByteOrder = gd.getNextBoolean();
		Prefs.copyColumnHeaders = gd.getNextBoolean();
		Prefs.noRowNumbers = !gd.getNextBoolean();
		return;
	}

	// Conversion Options
	void conversions() {
		double[] weights = ColorProcessor.getWeightingFactors();
		boolean weighted = !(weights[0]==1d/3d && weights[1]==1d/3d && weights[2]==1d/3d);
		//boolean weighted = !(Math.abs(weights[0]-1d/3d)<0.0001 && Math.abs(weights[1]-1d/3d)<0.0001 && Math.abs(weights[2]-1d/3d)<0.0001);
		GenericDialog gd = new GenericDialog("Conversion Options");
		gd.addCheckbox("Scale When Converting", ImageConverter.getDoScaling());
		String prompt = "Weighted RGB Conversions";
		if (weighted)
			prompt += " (" + IJ.d2s(weights[0]) + "," + IJ.d2s(weights[1]) + ","+ IJ.d2s(weights[2]) + ")";
		gd.addCheckbox(prompt, weighted);
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		ImageConverter.setDoScaling(gd.getNextBoolean());
		Prefs.weightedColor = gd.getNextBoolean();
		if (!Prefs.weightedColor)
			ColorProcessor.setWeightingFactors(1d/3d, 1d/3d, 1d/3d);
		else if (Prefs.weightedColor && !weighted)
			ColorProcessor.setWeightingFactors(0.299, 0.587, 0.114);
		return;
	}
		
	void appearance() {
		GenericDialog gd = new GenericDialog("Appearance", IJ.getTopComponentFrame());
		gd.addCheckbox("Interpolate Zoomed Images", Prefs.interpolateScaledImages);
		gd.addCheckbox("Open Images at 100%", Prefs.open100Percent);
		gd.addCheckbox("Black Canvas", Prefs.blackCanvas);
		gd.addCheckbox("No Image Border", Prefs.noBorder);
		gd.addCheckbox("Use Inverting Lookup Table", Prefs.useInvertingLut);
		gd.addCheckbox("Double Buffer Selections", Prefs.doubleBuffer);
		gd.addNumericField("Menu Font Size:", Menus.getFontSize(), 0, 3, "points");
		gd.showDialog();
		if (gd.wasCanceled())
			return;			
		boolean interpolate = gd.getNextBoolean();
		Prefs.open100Percent = gd.getNextBoolean();
		boolean blackCanvas = gd.getNextBoolean();
		boolean noBorder = gd.getNextBoolean();
		boolean useInvertingLut = gd.getNextBoolean();
		Prefs.doubleBuffer = gd.getNextBoolean();
		int menuSize = (int)gd.getNextNumber();
		if (interpolate!=Prefs.interpolateScaledImages) {
			Prefs.interpolateScaledImages = interpolate;
			IjxImagePlus imp = WindowManager.getCurrentImage();
			if (imp!=null)
				imp.draw();
		}
		if (blackCanvas!=Prefs.blackCanvas) {
			Prefs.blackCanvas = blackCanvas;
			IjxImagePlus imp = WindowManager.getCurrentImage();
			if (imp!=null) {
				IjxImageWindow win = (IjxImageWindow)imp.getWindow();
				if (win!=null) {
					if (Prefs.blackCanvas) {
						win.setForeground(Color.white);
						win.setBackground(Color.black);
					} else {
						win.setForeground(Color.black);
						win.setBackground(Color.white);
					}
					imp.repaintWindow();
				}
			}
		}
		if (noBorder!=Prefs.noBorder) {
			Prefs.noBorder = noBorder;
			IjxImagePlus imp = WindowManager.getCurrentImage();
			if (imp!=null) imp.repaintWindow();
		}
		if (useInvertingLut!=Prefs.useInvertingLut) {
			invertLuts(useInvertingLut);
			Prefs.useInvertingLut = useInvertingLut;
		}
		if (Prefs.doubleBuffer && IJ.isMacOSX()) {
			IJ.error("Double-buffering is built into Mac OS X.");
			Prefs.doubleBuffer = false;
		}
		if (menuSize!=Menus.getFontSize() && !IJ.isMacintosh()) {
			Menus.setFontSize(menuSize);
			IJ.showMessage("Appearance", "Restart ImageJ to use the new font size");
		}
	}
	
	void invertLuts(boolean useInvertingLut) {
		int[] list = WindowManager.getIDList();
		if (list==null) return;
		for (int i=0; i<list.length; i++) {
			IjxImagePlus imp = WindowManager.getImage(list[i]);
			if (imp==null) return;
			ImageProcessor ip = imp.getProcessor();
			if (useInvertingLut != ip.isInvertedLut() && !ip.isColorLut()) {
				ip.invertLut();
				int nImages = imp.getStackSize();
				if (nImages==1)
					ip.invert();
				else {
					IjxImageStack stack2 = imp.getStack();
					for (int slice=1; slice<=nImages; slice++)
						stack2.getProcessor(slice).invert();
					stack2.setColorModel(ip.getColorModel());
				}
			}
		}
	}

} // class Options