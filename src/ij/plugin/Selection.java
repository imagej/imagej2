package ij.plugin;
import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.measure.*;
import ij.plugin.frame.*;
import ij.macro.Interpreter;
import ij.plugin.filter.GaussianBlur;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Vector;



/** This plugin implements the commands in the Edit/Section submenu. */
public class Selection implements PlugIn, Measurements {
	private ImagePlus imp;
	private float[] kernel = {1f, 1f, 1f, 1f, 1f};
	private float[] kernel3 = {1f, 1f, 1f};
	private static String angle = "15"; // degrees
	private static String enlarge = "15"; // pixels
	private static String bandSize = "15"; // pixels
	private static boolean nonScalable;
	private static Color linec, fillc;
	private static int lineWidth = 1;
	

	public void run(String arg) {
		imp = WindowManager.getCurrentImage();
    	if (arg.equals("add"))
    		{addToRoiManager(imp); return;}
		if (imp==null)
			{IJ.noImage(); return;}
    	if (arg.equals("all"))
    		imp.setRoi(0,0,imp.getWidth(),imp.getHeight());
    	else if (arg.equals("none"))
    		imp.killRoi();
    	else if (arg.equals("restore"))
    		imp.restoreRoi();
    	else if (arg.equals("spline"))
    		fitSpline();
    	else if (arg.equals("ellipse"))
    		drawEllipse(imp);
    	else if (arg.equals("hull"))
    		convexHull(imp);
    	else if (arg.equals("mask"))
    		createMask(imp);    	
     	else if (arg.equals("from"))
    		createSelectionFromMask(imp);    	
    	else if (arg.equals("inverse"))
    		invert(imp); 
    	else if (arg.equals("properties"))
    		{setProperties("Properties", imp.getRoi()); imp.draw();}
    	else if (arg.equals("overlay"))
    		createOverlay(imp); 
    	else
    		runMacro(arg);
	}
	
	void runMacro(String arg) {
		Roi roi = imp.getRoi();
		if (IJ.macroRunning()) {
			String options = Macro.getOptions();
			if (options!=null && (options.indexOf("grid=")!=-1||options.indexOf("interpolat")!=-1)) {
				IJ.run("Rotate... ", options); // run Image>Transform>Rotate
				return;
			}
		}
		if (roi==null) {
			IJ.error("Rotate>Selection", "This command requires a selection");
			return;
		}
		roi = (Roi)roi.clone();
		if (arg.equals("rotate")) {
			String value = IJ.runMacroFile("ij.jar:RotateSelection", angle);
			if (value!=null) angle = value;    	
		} else if (arg.equals("enlarge")) {
			String value = IJ.runMacroFile("ij.jar:EnlargeSelection", enlarge); 
			if (value!=null) enlarge = value; 
			Roi.previousRoi = roi;
		} else if (arg.equals("band")) {
			String value = IJ.runMacroFile("ij.jar:MakeSelectionBand", bandSize); 
			if (value!=null) bandSize = value;    	
			Roi.previousRoi = roi;
		}
	}
	
	void fitSpline() {
		Roi roi = imp.getRoi();
		if (roi==null)
			{IJ.error("Spline", "Selection required"); return;}
		int type = roi.getType();
		boolean segmentedSelection = type==Roi.POLYGON||type==Roi.POLYLINE;
		if (!(segmentedSelection||type==Roi.FREEROI||type==Roi.TRACED_ROI||type==Roi.FREELINE))
			{IJ.error("Spline", "Polygon or polyline selection required"); return;}
		PolygonRoi p = (PolygonRoi)roi;
		if (!segmentedSelection)
			p = trimPolygon(p, getUncalibratedLength(p));
		String options = Macro.getOptions();
		if (options!=null && options.indexOf("straighten")!=-1)
			p.fitSplineForStraightening();
		else if (options!=null && options.indexOf("remove")!=-1)
			p.removeSplineFit();
		else
			p.fitSpline();
		imp.draw();
		LineWidthAdjuster.update();	
	}
	
	double getUncalibratedLength(PolygonRoi roi) {
		Calibration cal = imp.getCalibration();
		double spw=cal.pixelWidth, sph=cal.pixelHeight;
		cal.pixelWidth=1.0; cal.pixelHeight=1.0;
		double length = roi.getLength();
		cal.pixelWidth=spw; cal.pixelHeight=sph;
		return length;
	}

	PolygonRoi trimPolygon(PolygonRoi roi, double length) {
		int[] x = roi.getXCoordinates();
		int[] y = roi.getYCoordinates();
		int n = roi.getNCoordinates();
		x = smooth(x, n);
		y = smooth(y, n);
		float[] curvature = getCurvature(x, y, n);
		Rectangle r = roi.getBounds();
		double threshold = rodbard(length);
		//IJ.log("trim: "+length+" "+threshold);
		double distance = Math.sqrt((x[1]-x[0])*(x[1]-x[0])+(y[1]-y[0])*(y[1]-y[0]));
		x[0] += r.x; y[0]+=r.y;
		int i2 = 1;
		int x1,y1,x2=0,y2=0;
		for (int i=1; i<n-1; i++) {
			x1=x[i]; y1=y[i]; x2=x[i+1]; y2=y[i+1];
			distance += Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1)) + 1;
			distance += curvature[i]*2;
			if (distance>=threshold) {
				x[i2] = x2 + r.x;
				y[i2] = y2 + r.y;
				i2++;
				distance = 0.0;
			}
		}
		int type = roi.getType()==Roi.FREELINE?Roi.POLYLINE:Roi.POLYGON;
		if (type==Roi.POLYLINE && distance>0.0) {
			x[i2] = x2 + r.x;
			y[i2] = y2 + r.y;
			i2++;
		}		
		PolygonRoi p = new PolygonRoi(x, y, i2, type);
		imp.setRoi(p);
		return p;
	}
	
    double rodbard(double x) {
    	// y = c*((a-x/(x-d))^(1/b)
    	// a=3.9, b=.88, c=712, d=44
		double ex;
		if (x == 0.0)
			ex = 5.0;
		else
			ex = Math.exp(Math.log(x/700.0)*0.88);
		double y = 3.9-44.0;
		y = y/(1.0+ex);
		return y+44.0;
    }

	int[] smooth(int[] a, int n) {
		FloatProcessor fp = new FloatProcessor(n, 1);
		for (int i=0; i<n; i++)
			fp.putPixelValue(i, 0, a[i]);
		GaussianBlur gb = new GaussianBlur();
		gb.blur1Direction(fp, 2.0, 0.01, true, 0);
		for (int i=0; i<n; i++)
			a[i] = (int)Math.round(fp.getPixelValue(i, 0));
		return a;
	}
	
	float[] getCurvature(int[] x, int[] y, int n) {
		float[] x2 = new float[n];
		float[] y2 = new float[n];
		for (int i=0; i<n; i++) {
			x2[i] = x[i];
			y2[i] = y[i];
		}
		ImageProcessor ipx = new FloatProcessor(n, 1, x2, null);
		ImageProcessor ipy = new FloatProcessor(n, 1, y2, null);
		ipx.convolve(kernel, kernel.length, 1);
		ipy.convolve(kernel, kernel.length, 1);
		float[] indexes = new float[n];
		float[] curvature = new float[n];
		for (int i=0; i<n; i++) {
			indexes[i] = i;
			curvature[i] = (float)Math.sqrt((x2[i]-x[i])*(x2[i]-x[i])+(y2[i]-y[i])*(y2[i]-y[i]));
		}
		return curvature;
	}
	
	void drawEllipse(ImagePlus imp) {
		IJ.showStatus("Fitting ellipse");
		Roi roi = imp.getRoi();
		if (roi==null)
			{IJ.error("Fit Ellipse", "Selection required"); return;}
		if (roi.isLine())
			{IJ.error("Fit Ellipse", "\"Fit Ellipse\" does not work with line selections"); return;}
		ImageProcessor ip = imp.getProcessor();
		ImageStatistics stats;
		if (roi.getType()==Roi.COMPOSITE)
			stats = imp.getStatistics();
		else {
			ip.setRoi(roi.getPolygon());
			stats = ImageStatistics.getStatistics(ip, AREA+MEAN+MODE+MIN_MAX, null);
		}
		EllipseFitter ef = new EllipseFitter();
		ef.fit(ip, stats);
		ef.makeRoi(ip);
		imp.setRoi(new PolygonRoi(ef.xCoordinates, ef.yCoordinates, ef.nCoordinates, roi.FREEROI));
		IJ.showStatus("");
	}

	void convexHull(ImagePlus imp) {
		Roi roi = imp.getRoi();
		int type = roi!=null?roi.getType():-1;
		if (!(type==Roi.FREEROI||type==Roi.TRACED_ROI||type==Roi.POLYGON||type==Roi.POINT))
			{IJ.error("Convex Hull", "Polygonal or point selection required"); return;}
		Polygon p = roi.getConvexHull();
		if (p!=null)
			imp.setRoi(new PolygonRoi(p.xpoints, p.ypoints, p.npoints, roi.POLYGON));
	}
	
	// Finds the index of the upper right point that is guaranteed to be on convex hull
	int findFirstPoint(int[] xCoordinates, int[] yCoordinates, int n, ImagePlus imp) {
		int smallestY = imp.getHeight();
		int x, y;
		for (int i=0; i<n; i++) {
			y = yCoordinates[i];
			if (y<smallestY)
			smallestY = y;
		}
		int smallestX = imp.getWidth();
		int p1 = 0;
		for (int i=0; i<n; i++) {
			x = xCoordinates[i];
			y = yCoordinates[i];
			if (y==smallestY && x<smallestX) {
				smallestX = x;
				p1 = i;
			}
		}
		return p1;
	}
	
	void createMask(ImagePlus imp) {
		Roi roi = imp.getRoi();
		boolean useInvertingLut = Prefs.useInvertingLut;
		Prefs.useInvertingLut = false;
		if (roi==null || !(roi.isArea()||roi.getType()==Roi.POINT)) {
			createMaskFromThreshold(imp);
			Prefs.useInvertingLut = useInvertingLut;
			return;
		}
		ImagePlus maskImp = null;
		Frame frame = WindowManager.getFrame("Mask");
		if (frame!=null && (frame instanceof ImageWindow))
			maskImp = ((ImageWindow)frame).getImagePlus();
		if (maskImp==null) {
			ImageProcessor ip = new ByteProcessor(imp.getWidth(), imp.getHeight());
			if (!Prefs.blackBackground)
				ip.invertLut();
			maskImp = new ImagePlus("Mask", ip);
			maskImp.show();
		}
		ImageProcessor ip = maskImp.getProcessor();
		ip.setRoi(roi);
		ip.setValue(255);
		ip.fill(ip.getMask());
		maskImp.updateAndDraw();
		Prefs.useInvertingLut = useInvertingLut;
	}
	
	void createMaskFromThreshold(ImagePlus imp) {
		ImageProcessor ip = imp.getProcessor();
		if (ip.getMinThreshold()==ImageProcessor.NO_THRESHOLD)
			{IJ.error("Create Mask", "Area selection or thresholded image required"); return;}
		double t1 = ip.getMinThreshold();
		double t2 = ip.getMaxThreshold();
		IJ.run("Duplicate...", "title=mask");
		ImagePlus imp2 = WindowManager.getCurrentImage();
		ImageProcessor ip2 = imp2.getProcessor();
		ip2.setThreshold(t1, t2, ImageProcessor.NO_LUT_UPDATE);
		IJ.run("Convert to Mask");
	}

	void createSelectionFromMask(ImagePlus imp) {
		ImageProcessor ip = imp.getProcessor();
		if (ip.getMinThreshold()!=ImageProcessor.NO_THRESHOLD) {
			IJ.runPlugIn("ij.plugin.filter.ThresholdToSelection", "");
			return;
		}
		if (!ip.isBinary()) {
			IJ.error("Create Selection",
				"This command creates a composite selection from\n"+
				"a mask (8-bit binary image with white background)\n"+
				"or from an image that has been thresholded using\n"+
				"the Image>Adjust>Threshold tool. The current\n"+
				"image is not a mask and has not been thresholded.");
			return;
		}
		int threshold = ip.isInvertedLut()?255:0;
		ip.setThreshold(threshold, threshold, ImageProcessor.NO_LUT_UPDATE);
		IJ.runPlugIn("ij.plugin.filter.ThresholdToSelection", "");
	}

	void invert(ImagePlus imp) {
		Roi roi = imp.getRoi();
		if (roi==null || !roi.isArea())
			{IJ.error("Inverse", "Area selection required"); return;}
		ShapeRoi s1, s2;
		if (roi instanceof ShapeRoi)
			s1 = (ShapeRoi)roi;
		else
			s1 = new ShapeRoi(roi);
		s2 = new ShapeRoi(new Roi(0,0, imp.getWidth(), imp.getHeight()));
		imp.setRoi(s1.xor(s2));
	}
	
	void addToRoiManager(ImagePlus imp) {
		if (IJ.macroRunning() &&  Interpreter.isBatchModeRoiManager())
			IJ.error("run(\"Add to Manager\") may not work in batch mode macros");
		Frame frame = WindowManager.getFrame("ROI Manager");
		if (frame==null)
			IJ.run("ROI Manager...");
		if (imp==null) return;
		Roi roi = imp.getRoi();
		if (roi==null) return;
		frame = WindowManager.getFrame("ROI Manager");
		if (frame==null || !(frame instanceof RoiManager))
			IJ.error("ROI Manager not found");
		RoiManager rm = (RoiManager)frame;
		boolean altDown= IJ.altKeyDown();
		IJ.setKeyUp(IJ.ALL_KEYS);
		if (altDown) IJ.setKeyDown(KeyEvent.VK_SHIFT);
		rm.runCommand("add");
		IJ.setKeyUp(IJ.ALL_KEYS);
	}
	
	void createOverlay(ImagePlus imp) {
		String macroOptions = Macro.getOptions();
		if (macroOptions!=null && IJ.macroRunning() && macroOptions.indexOf("remove")!=-1) {
			imp.setDisplayList(null);
			return;
		}
		Roi roi = imp.getRoi();
		if (roi==null && imp.getDisplayList()!=null) {
			GenericDialog gd = new GenericDialog("No Selection");
			gd.addMessage("\"Create Overlay\" requires a selection.");
			gd.setInsets(15, 40, 0);
			gd.addCheckbox("Remove existing overlay", false);
			gd.showDialog();
			if (gd.wasCanceled()) return;
			if (gd.getNextBoolean()) imp.setDisplayList(null);
			return;
 		}
		if (!setProperties("Create Overlay", roi))
			return;
		ImageCanvas ic = imp.getCanvas();
		Vector list = new Vector();
		list.addElement(roi);
		ic.setDisplayList(list);
		imp.killRoi();
	}
	
	boolean setProperties(String title, Roi roi) {
		if (roi==null) {
			IJ.error("This command requires a selection.");
			return false;
		}
		RoiProperties rp = new RoiProperties(title, roi);
		return rp.showDialog();
	}

}

