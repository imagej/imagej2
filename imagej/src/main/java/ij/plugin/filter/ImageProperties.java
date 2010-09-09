package ij.plugin.filter;
import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.util.Tools;
import ij.io.FileOpener;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import ij.measure.Calibration;

public class ImageProperties implements PlugInFilter, TextListener {
	ImagePlus imp;
	static final int NANOMETER=0, MICROMETER=1, MILLIMETER=2, CENTIMETER=3,
		 METER=4, KILOMETER=5, INCH=6, FOOT=7, MILE=8, PIXEL=9, OTHER_UNIT=10;
	int oldUnitIndex;
	double oldUnitsPerCm;
	Vector nfields, sfields;
	boolean duplicatePixelWidth = true;
	String calUnit;
	double calPixelWidth, calPixelHeight, calPixelDepth;
	TextField pixelWidthField, pixelHeightField, pixelDepthField;
	int textChangedCount;

	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_ALL+NO_CHANGES;
	}

	public void run(ImageProcessor ip) {
		showDialog(imp);
	}
	
	void showDialog(ImagePlus imp) {
		String options = Macro.getOptions();
		if (options!=null ) {
			String options2 = options.replaceAll(" depth=", " slices=");
			options2 = options2.replaceAll(" interval=", " frame=");
			Macro.setOptions(options2);
		}
		Calibration cal = imp.getCalibration();
		Calibration calOrig = cal.copy();
		oldUnitIndex = getUnitIndex(cal.getUnit());
		oldUnitsPerCm = getUnitsPerCm(oldUnitIndex);
		int stackSize = imp.getImageStackSize();
		int channels = imp.getNChannels();
		int slices = imp.getNSlices();
		int frames = imp.getNFrames();
		boolean global1 = imp.getGlobalCalibration()!=null;
		boolean global2;
		int digits = cal.pixelWidth<1.0||cal.pixelHeight<1.0||cal.pixelDepth<1.0?7:4;
		GenericDialog gd = new GenericDialog(imp.getTitle());
		gd.addNumericField("Channels (c):", channels, 0);
		gd.addNumericField("Slices (z):", slices, 0);
		gd.addNumericField("Frames (t):", frames, 0);
		gd.setInsets(0, 5, 0);
		gd.addMessage("Note: c*z*t must equal "+stackSize);
		gd.setInsets(15, 0, 0);
		gd.addStringField("Unit of Length:", cal.getUnit());
		gd.addNumericField("Pixel_Width:", cal.pixelWidth, digits, 8, null);
		gd.addNumericField("Pixel_Height:", cal.pixelHeight, digits, 8, null);
		gd.addNumericField("Voxel_Depth:", cal.pixelDepth, digits, 8, null);
		gd.setInsets(10, 0, 5);
		double interval = cal.frameInterval;
		String intervalStr = IJ.d2s(interval, (int)interval==interval?0:2) + " " + cal.getTimeUnit();
		gd.addStringField("Frame Interval:", intervalStr);
		String xo = cal.xOrigin==(int)cal.xOrigin?IJ.d2s(cal.xOrigin,0):IJ.d2s(cal.xOrigin,2);
		String yo = cal.yOrigin==(int)cal.yOrigin?IJ.d2s(cal.yOrigin,0):IJ.d2s(cal.yOrigin,2);
		String zo = "";
		if (cal.zOrigin!=0.0) {
			zo = cal.zOrigin==(int)cal.zOrigin?IJ.d2s(cal.zOrigin,0):IJ.d2s(cal.zOrigin,2);
			zo = "," + zo;
		}
		gd.addStringField("Origin (pixels):", xo+","+yo+zo);
		gd.setInsets(5, 20, 0);
		gd.addCheckbox("Global", global1);
		nfields = gd.getNumericFields();
		pixelWidthField  = (TextField)nfields.elementAt(3);
		pixelHeightField  = (TextField)nfields.elementAt(4);
		pixelDepthField  = (TextField)nfields.elementAt(5);
        for (int i=0; i<nfields.size(); i++)
            ((TextField)nfields.elementAt(i)).addTextListener(this);
        sfields = gd.getStringFields();
        for (int i=0; i<sfields.size(); i++)
            ((TextField)sfields.elementAt(i)).addTextListener(this);
		calUnit = cal.getUnit();
		calPixelWidth = cal.pixelWidth;
		calPixelHeight = cal.pixelHeight;
		calPixelDepth = cal.pixelDepth;
		gd.showDialog();
		if (gd.wasCanceled())
			return;
 		channels = (int)gd.getNextNumber();
 		if (channels<1) channels = 1;
 		slices = (int)gd.getNextNumber();
 		if (slices<1) slices = 1;
 		frames = (int)gd.getNextNumber();
 		if (frames<1) frames = 1;
 		if (channels*slices*frames==stackSize)
 			imp.setDimensions(channels, slices, frames);
 		else
 			IJ.error("Properties", "The product of channels ("+channels+"), slices ("+slices
 				+")\n and frames ("+frames+") must equal the stack size ("+stackSize+").");

		String unit = gd.getNextString();
        if (unit.equals("u"))
            unit = "" + IJ.micronSymbol;
        else if (unit.equals("A"))
        	unit = ""+IJ.angstromSymbol;
 		double pixelWidth = gd.getNextNumber();
 		double pixelHeight = gd.getNextNumber();
 		double pixelDepth = gd.getNextNumber();
		//IJ.log(calPixelWidth+" "+calPixelHeight+" "+calPixelDepth);
 		//if (calPixelWidth!=cal.pixelWidth) pixelWidth = calPixelWidth;
 		//if (calPixelHeight!=cal.pixelHeight) pixelHeight = calPixelHeight;
 		//if (calPixelDepth!=cal.pixelDepth) pixelDepth = calPixelDepth;
		if (unit.equals("") || unit.equalsIgnoreCase("none") || pixelWidth==0.0) {
			cal.setUnit(null);
			cal.pixelWidth = 1.0;
			cal.pixelHeight = 1.0;
			cal.pixelDepth = 1.0;
		} else {
			cal.setUnit(unit);
			cal.pixelWidth = pixelWidth;
			cal.pixelHeight = pixelHeight;
			cal.pixelDepth = pixelDepth;
		}

		String frameInterval = validateInterval(gd.getNextString());
		String[] intAndUnit = Tools.split(frameInterval, " -");
		interval = Tools.parseDouble(intAndUnit[0]);
		cal.frameInterval = Double.isNaN(interval)?0.0:interval;
		String timeUnit = intAndUnit.length>=2?intAndUnit[1]:"sec";
        if (timeUnit.equals("usec"))
            timeUnit = IJ.micronSymbol + "sec";
		cal.setTimeUnit(timeUnit);

        String[] origin = Tools.split(gd.getNextString(), " ,");
		double x = Tools.parseDouble(origin[0]);
		double y = origin.length>=2?Tools.parseDouble(origin[1]):Double.NaN;
		double z = origin.length>=3?Tools.parseDouble(origin[2]):Double.NaN;
		cal.xOrigin= Double.isNaN(x)?0.0:x;
		cal.yOrigin= Double.isNaN(y)?cal.xOrigin:y;
		cal.zOrigin= Double.isNaN(z)?0.0:z;
 		global2 = gd.getNextBoolean();
		if (!cal.equals(calOrig))
			imp.setCalibration(cal);
 		imp.setGlobalCalibration(global2?cal:null);
		if (global2 || global2!=global1)
			WindowManager.repaintImageWindows();
		else
			imp.repaintWindow();
		if (global2 && global2!=global1)
			FileOpener.setShowConflictMessage(true);
	}
	
	String validateInterval(String interval) {
		if (interval.indexOf(" ")!=-1)
			return interval;
		int firstLetter = -1;
		for (int i=0; i<interval.length(); i++) {
			char c = interval.charAt(i);
			if (Character.isLetter(c)) {
				firstLetter = i;
				break;
			}
		}
		if (firstLetter>0 && firstLetter<interval.length()-1)
			interval = interval.substring(0,firstLetter)+" "+interval.substring(firstLetter, interval.length());
		return interval;
	}
	
	double getNewScale(String newUnit, double oldScale) {
		//IJ.log("getNewScale: "+newUnit);
		if (oldUnitsPerCm==0.0) return 0.0;
		double newScale = 0.0;
		int newUnitIndex = getUnitIndex(newUnit);
		if (newUnitIndex!=oldUnitIndex) {
			double newUnitsPerCm = getUnitsPerCm(newUnitIndex);
			if (oldUnitsPerCm!=0.0 && newUnitsPerCm!=0.0) {
				newScale = oldScale * (oldUnitsPerCm/newUnitsPerCm);
			}
		}
		return newScale;
	}
	
	int getUnitIndex(String unit) {
		unit = unit.toLowerCase(Locale.US);
		if (unit.equals("cm")||unit.startsWith("cent"))
			return CENTIMETER;
		else if (unit.equals("mm")||unit.startsWith("milli"))
			return MILLIMETER;
		else if (unit.startsWith("inch"))
			return INCH;
		else if (unit.startsWith(""+IJ.micronSymbol)||unit.startsWith("u")||unit.startsWith("micro"))
			return MICROMETER;
		else if (unit.equals("nm")||unit.startsWith("nano"))
			return NANOMETER;
		else if (unit.startsWith("meter"))
			return METER;
		else if (unit.equals("km")||unit.startsWith("kilo"))
			return KILOMETER;
		else if (unit.equals("ft")||unit.equals("foot")||unit.equals("feet"))
			return FOOT;
		else if (unit.equals("mi")||unit.startsWith("mile"))
			return MILLIMETER;
		else
			return OTHER_UNIT;
	}
	
	double getUnitsPerCm(int unitIndex) {
		switch (unitIndex) {
			case NANOMETER: return  10000000.0;
			case MICROMETER: return    10000.0;
			case MILLIMETER: return       10.0;
			case CENTIMETER: return        1.0;
			case METER: return             0.01;
			case KILOMETER: return         0.00001;
			case INCH: return              0.3937;
			case FOOT: return              0.0328083;
			case MILE: return              0.000006213;
			default: return                0.0;
		}
	}

   	public void textValueChanged(TextEvent e) {
   		textChangedCount++;
		Object source = e.getSource();
        
        int channels = (int)Tools.parseDouble(((TextField)nfields.elementAt(2)).getText(),-99);
        int depth = (int)Tools.parseDouble(((TextField)nfields.elementAt(3)).getText(),-99);
        int frames = (int)Tools.parseDouble(((TextField)nfields.elementAt(4)).getText(),-99);
        
		double newPixelWidth = calPixelWidth;
		String newWidthText = pixelWidthField.getText();
		if (source==pixelWidthField)
			newPixelWidth = Tools.parseDouble(newWidthText,-99);
		double newPixelHeight = calPixelHeight;
		if (source==pixelHeightField) {
			String newHeightText = pixelHeightField.getText();
			newPixelHeight = Tools.parseDouble(newHeightText,-99);
			if (!newHeightText.equals(newWidthText))
				duplicatePixelWidth = false;
		}
		double newPixelDepth = calPixelDepth;
		if (source==pixelDepthField) {
			String newDepthText = pixelDepthField.getText();
			newPixelDepth = Tools.parseDouble(newDepthText,-99);
			if (!newDepthText.equals(newWidthText))
				duplicatePixelWidth = false;
		}
		if (textChangedCount==1 && (calPixelHeight!=1.0||calPixelDepth!=1.0))
			duplicatePixelWidth = false;
		if (source==pixelWidthField && newPixelWidth!=-99 && duplicatePixelWidth) {
			pixelHeightField.setText(newWidthText);
			pixelDepthField.setText(newWidthText);
			calPixelHeight = calPixelWidth;
			calPixelDepth = calPixelWidth;
		}
		calPixelWidth = newPixelWidth;
		calPixelHeight = newPixelHeight;
 		calPixelDepth = newPixelDepth;
 		TextField unitField = (TextField)sfields.elementAt(0);
 		String newUnit = unitField.getText();
 		if (!newUnit.equals(calUnit)) {
			double oldXScale = newPixelWidth!=0?1.0/newPixelWidth:0;
			double oldYScale = newPixelHeight!=0?1.0/newPixelHeight:0;
			double oldZScale = newPixelDepth!=0?1.0/newPixelDepth:0;
			double newXScale = getNewScale(newUnit, oldXScale);
			double newYScale = getNewScale(newUnit, oldYScale);
			double newZScale = getNewScale(newUnit, oldZScale);
			if (newXScale!=0.0) {
				double w = 1.0/newXScale;
				double h = 1.0/newYScale;
				double d = 1.0/newZScale;
				int digits = w<1.0||h<1.0||d<1.0?7:4;
				pixelWidthField.setText(IJ.d2s(1/newXScale,digits));
				pixelHeightField.setText(IJ.d2s(1/newYScale,digits));
				pixelDepthField.setText(IJ.d2s(1/newZScale,digits));
				calPixelWidth = 1.0/newXScale;
				calPixelHeight = 1.0/newYScale;
				calPixelDepth = 1.0/newZScale;
				oldUnitIndex = getUnitIndex(newUnit);
				oldUnitsPerCm = getUnitsPerCm(oldUnitIndex);
			}
			calUnit = newUnit;
		}
 	}

}
