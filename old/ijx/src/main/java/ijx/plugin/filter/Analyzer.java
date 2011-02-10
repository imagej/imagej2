package ijx.plugin.filter;
import ijx.process.ImageProcessor;
import ijx.process.FloatStatistics;
import ijx.process.FloatProcessor;
import ijx.process.ImageStatistics;
import ijx.plugin.api.PlugInFilter;
import ijx.gui.IjxToolbar;
import ijx.gui.ProfilePlot;
import ijx.gui.dialog.YesNoCancelDialog;
import ijx.gui.dialog.GenericDialog;
import ijx.roi.Line;
import ijx.roi.PointRoi;
import ijx.roi.Roi;
import ijx.roi.PolygonRoi;
import ijx.measure.ResultsTable;
import ijx.measure.Calibration;
import ijx.measure.Measurements;
import ijx.Macro;
import ijx.Prefs;
import ijx.WindowManager;
import ijx.IJ;
import java.awt.*;
import java.util.Properties;




import ijx.text.*;
import ijx.plugin.MeasurementsWriter;
import ijx.plugin.Straightener;
import ijx.macro.Interpreter;
import ijx.CentralLookup;
import ijx.IjxImagePlus;
import ijx.IjxImageStack;
import ijx.SavesPrefs;
import ijx.app.IjxApplication;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=SavesPrefs.class)

/** This plugin implements ImageJ's Analyze/Measure and Analyze/Set Measurements commands. */
public class Analyzer implements PlugInFilter, Measurements, SavesPrefs {
	
	private String arg;
	private IjxImagePlus imp;
	private ResultsTable rt;
	private int measurements;
	private StringBuffer min,max,mean,sd;
	
	// Order must agree with order of checkboxes in Set Measurements dialog box
	private static final int[] list = {AREA,MEAN,STD_DEV,MODE,MIN_MAX,
		CENTROID,CENTER_OF_MASS,PERIMETER,RECT,ELLIPSE,SHAPE_DESCRIPTORS, FERET,
		INTEGRATED_DENSITY,MEDIAN,SKEWNESS,KURTOSIS,AREA_FRACTION,STACK_POSITION,
		LIMIT,LABELS,INVERT_Y,SCIENTIFIC_NOTATION};

	private static final String MEASUREMENTS = "measurements";
	private static final String MARK_WIDTH = "mark.width";
	private static final String PRECISION = "precision";
	//private static int counter;
	private static boolean unsavedMeasurements;
	public static Color darkBlue = new Color(0,0,160);
	private static int systemMeasurements = Prefs.getInt(MEASUREMENTS,AREA+MEAN+MIN_MAX);
	public static int markWidth = Prefs.getInt(MARK_WIDTH,0);
	public static int precision = Prefs.getInt(PRECISION,3);
	private static float[] umeans = new float[MAX_STANDARDS];
	private static ResultsTable systemRT = new ResultsTable();
	private static int redirectTarget;
	private static String redirectTitle = "";
	private static IjxImagePlus redirectImage; // non-displayed images
	static int firstParticle, lastParticle;
	private static boolean summarized;
	private static boolean switchingModes;
	private static boolean showMin = true;
	
	public Analyzer() {
		rt = systemRT;
		rt.setPrecision((systemMeasurements&SCIENTIFIC_NOTATION)!=0?-precision:precision);
		measurements = systemMeasurements;
	}
	
	/** Constructs a new Analyzer using the specified IjxImagePlus object
		and the current measurement options and default results table. */
	public Analyzer(IjxImagePlus imp) {
		this();
		this.imp = imp;
	}
	
	/** Construct a new Analyzer using an IjxImagePlus object and private
		measurement options and results table. */
	public Analyzer(IjxImagePlus imp, int measurements, ResultsTable rt) {
		this.imp = imp;
		this.measurements = measurements;
		this.rt = rt;
	}
	
	public int setup(String arg, IjxImagePlus imp) {
		this.arg = arg;
		this.imp = imp;
		IJ.register(Analyzer.class);
		if (arg.equals("set"))
			{doSetDialog(); return DONE;}
		else if (arg.equals("sum"))
			{summarize(); return DONE;}
		else if (arg.equals("clear")) {
			if (IJ.macroRunning()) unsavedMeasurements = false;
			resetCounter();
			return DONE;
		} else
			return DOES_ALL+NO_CHANGES;
	}

	public void run(ImageProcessor ip) {
		measure();
		displayResults();
	}

	void doSetDialog() {
		String NONE = "None";
		String[] titles;
        int[] wList = WindowManager.getIDList();
        if (wList==null) {
        	titles = new String[1];
            titles[0] = NONE;
        } else {
			titles = new String[wList.length+1];
			titles[0] = NONE;
			for (int i=0; i<wList.length; i++) {
				IjxImagePlus imp = WindowManager.getImage(wList[i]);
				titles[i+1] = imp!=null?imp.getTitle():"";
			}
		}
		IjxImagePlus tImp = WindowManager.getImage(redirectTarget);
		String target = tImp!=null?tImp.getTitle():NONE;
		String macroOptions = Macro.getOptions();
		if (macroOptions!=null && macroOptions.indexOf("circularity ")!=-1)
			Macro.setOptions(macroOptions.replaceAll("circularity ", "shape "));
		if (macroOptions!=null && macroOptions.indexOf("slice ")!=-1)
			Macro.setOptions(macroOptions.replaceAll("slice ", "stack "));

 		GenericDialog gd = new GenericDialog("Set Measurements", IJ.getTopComponentFrame());
		String[] labels = new String[18];
		boolean[] states = new boolean[18];
		labels[0]="Area"; states[0]=(systemMeasurements&AREA)!=0;
		labels[1]="Mean gray value"; states[1]=(systemMeasurements&MEAN)!=0;
		labels[2]="Standard deviation"; states[2]=(systemMeasurements&STD_DEV)!=0;
		labels[3]="Modal gray value"; states[3]=(systemMeasurements&MODE)!=0;
		labels[4]="Min & max gray value"; states[4]=(systemMeasurements&MIN_MAX)!=0;
		labels[5]="Centroid"; states[5]=(systemMeasurements&CENTROID)!=0;
		labels[6]="Center of mass"; states[6]=(systemMeasurements&CENTER_OF_MASS)!=0;
		labels[7]="Perimeter"; states[7]=(systemMeasurements&PERIMETER)!=0;
		labels[8]="Bounding rectangle"; states[8]=(systemMeasurements&RECT)!=0;
		labels[9]="Fit ellipse"; states[9]=(systemMeasurements&ELLIPSE)!=0;
		labels[10]="Shape descriptors"; states[10]=(systemMeasurements&SHAPE_DESCRIPTORS)!=0;
		labels[11]="Feret's diameter"; states[11]=(systemMeasurements&FERET)!=0;
		labels[12]="Integrated density"; states[12]=(systemMeasurements&INTEGRATED_DENSITY)!=0;
		labels[13]="Median"; states[13]=(systemMeasurements&MEDIAN)!=0;
		labels[14]="Skewness"; states[14]=(systemMeasurements&SKEWNESS)!=0;
		labels[15]="Kurtosis"; states[15]=(systemMeasurements&KURTOSIS)!=0;
		labels[16]="Area_fraction"; states[16]=(systemMeasurements&AREA_FRACTION)!=0;
		labels[17]="Stack position"; states[17]=(systemMeasurements&STACK_POSITION)!=0;
		gd.setInsets(0, 0, 0);
		gd.addCheckboxGroup(10, 2, labels, states);
		labels = new String[4];
		states = new boolean[4];
		labels[0]="Limit to threshold"; states[0]=(systemMeasurements&LIMIT)!=0;
		labels[1]="Display label"; states[1]=(systemMeasurements&LABELS)!=0;
		labels[2]="Invert Y coordinates"; states[2]=(systemMeasurements&INVERT_Y)!=0;
		labels[3]="Scientific notation"; states[3]=(systemMeasurements&SCIENTIFIC_NOTATION)!=0;;
		gd.setInsets(0, 0, 0);
		gd.addCheckboxGroup(2, 2, labels, states);
		gd.setInsets(15, 0, 0);
        gd.addChoice("Redirect to:", titles, target);
		gd.setInsets(5, 0, 0);
		gd.addNumericField("Decimal places (0-9):", precision, 0, 2, "");
		gd.addHelp(IJ.URL+"/docs/menus/analyze.html#set");
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		int oldMeasurements = systemMeasurements;
		setOptions(gd);
		int index = gd.getNextChoiceIndex();
		redirectTarget = index==0?0:wList[index-1];
		redirectTitle = titles[index];
		IjxImagePlus imp = WindowManager.getImage(redirectTarget);
		redirectImage = imp!=null && imp.getWindow()==null?imp:null;

		int prec = (int)gd.getNextNumber();
		if (prec<0) prec = 0;
		if (prec>9) prec = 9;
		boolean notationChanged = (oldMeasurements&SCIENTIFIC_NOTATION)!=(systemMeasurements&SCIENTIFIC_NOTATION);
		if (prec!=precision || notationChanged) {
			precision = prec;
			rt.setPrecision((systemMeasurements&SCIENTIFIC_NOTATION)!=0?-precision:precision);
			rt.show("Results");
		}
	}
	
	void setOptions(GenericDialog gd) {
		int oldMeasurements = systemMeasurements;
		int previous = 0;
		boolean b = false;
		for (int i=0; i<list.length; i++) {
			//if (list[i]!=previous)
			b = gd.getNextBoolean();
			previous = list[i];
			if (b)
				systemMeasurements |= list[i];
			else
				systemMeasurements &= ~list[i];
		}
		if ((oldMeasurements&(~LIMIT)&(~SCIENTIFIC_NOTATION))!=(systemMeasurements&(~LIMIT)&(~SCIENTIFIC_NOTATION))&&IJ.isResultsWindow()) {
				rt.setPrecision((systemMeasurements&SCIENTIFIC_NOTATION)!=0?-precision:precision);
				rt.update(systemMeasurements, imp, null);
		}
		if ((systemMeasurements&LABELS)==0)
			systemRT.disableRowLabels();
	}
	
	/** Measures the image or selection and adds the results to the default results table. */
	public void measure() {
		String lastHdr = rt.getColumnHeading(ResultsTable.LAST_HEADING);
		if (lastHdr==null || lastHdr.charAt(0)!='S') {
			if (!reset()) return;
		}
		firstParticle = lastParticle = 0;
		Roi roi = imp.getRoi();
		if (roi!=null && roi.getType()==Roi.POINT) {
			measurePoint(roi);
			return;
		}
		if (roi!=null && roi.isLine()) {
			measureLength(roi);
			return;
		}
		if (roi!=null && roi.getType()==Roi.ANGLE) {
			measureAngle(roi);
			return;
		}
		ImageStatistics stats;
		if (isRedirectImage()) {
			stats = getRedirectStats(measurements, roi);
			if (stats==null) return;
		} else
			stats = imp.getStatistics(measurements);
		if (!IJ.isResultsWindow() && IJ.getInstance()!=null)
			reset();
		saveResults(stats, roi);
	}
	
	boolean reset() {
		boolean ok = true;
		if (rt.getCounter()>0)
			ok = resetCounter();
		if (ok && rt.getColumnHeading(ResultsTable.LAST_HEADING)==null)
			rt.setDefaultHeadings();
		return ok;
	}

	/** Returns <code>true</code> if an image is selected in the "Redirect To:"
		popup menu of the Analyze/Set Measurements dialog box. */
	public static boolean isRedirectImage() {
		return redirectTarget!=0;
	}
	
	/** Set the "Redirect To" image. Pass 'null' as the 
	    argument to disable redirected sampling. */
	public static void setRedirectImage(IjxImagePlus imp) {
		if (imp==null) {
			redirectTarget = 0;
			redirectTitle = null;
			redirectImage = null;
		} else {
			redirectTarget = imp.getID();
			redirectTitle = imp.getTitle();
			if (imp.getWindow()==null)
				redirectImage = imp;
		}
	}
	
	/** Returns the image selected in the "Redirect To:" popup
		menu of the Analyze/Set Measurements dialog or null
		if "None" is selected, the image was not found or the 
		image is not the same size as <code>currentImage</code>. */
	public static IjxImagePlus getRedirectImage(IjxImagePlus currentImage) {
		IjxImagePlus rImp = WindowManager.getImage(redirectTarget);
		if (rImp==null)
			rImp = redirectImage;
		if (rImp==null) {
			IJ.error("Analyzer", "Redirect image (\""+redirectTitle+"\")\n"
				+ "not found.");
			redirectTarget = 0;
			Macro.abort();
			return null;
		}
		if (rImp.getWidth()!=currentImage.getWidth() || rImp.getHeight()!=currentImage.getHeight()) {
			IJ.error("Analyzer", "Redirect image (\""+redirectTitle+"\") \n"
				+ "is not the same size as the current image.");
			Macro.abort();
			return null;
		}
		return rImp;
	}

	ImageStatistics getRedirectStats(int measurements, Roi roi) {
		IjxImagePlus redirectImp = getRedirectImage(imp);
		if (redirectImp==null)
			return null;
		int depth = redirectImp.getStackSize();
		if (depth>1 && depth==imp.getStackSize())
			redirectImp.setSlice(imp.getCurrentSlice());
		ImageProcessor ip = redirectImp.getProcessor();
		if (imp.getTitle().equals("mask") && imp.getBitDepth()==8) {
			ip.setMask(imp.getProcessor());
			ip.setRoi(0, 0, imp.getWidth(), imp.getHeight());
		} else
			ip.setRoi(roi);
		return ImageStatistics.getStatistics(ip, measurements, redirectImp.getCalibration());
	}
	
	void measurePoint(Roi roi) {
		if (rt.getCounter()>0) {
			if (!IJ.isResultsWindow()) reset();
			int index = rt.getColumnIndex("X");
			if (index<0 || !rt.columnExists(index))
				rt.update(measurements, imp, roi);
		}
		Polygon p = roi.getPolygon();
		for (int i=0; i<p.npoints; i++) {
			ImageProcessor ip = imp.getProcessor();
			ip.setRoi(p.xpoints[i], p.ypoints[i], 1, 1);
			ImageStatistics stats = ImageStatistics.getStatistics(ip, measurements, imp.getCalibration());
			saveResults(stats, new PointRoi(p.xpoints[i], p.ypoints[i]));
			if (i!=p.npoints-1) displayResults();
		}
	}
	
	void measureAngle(Roi roi) {
		if (rt.getCounter()>0) {
			if (!IJ.isResultsWindow()) reset();
			int index = rt.getColumnIndex("Angle");
			if (index<0 || !rt.columnExists(index))
				rt.update(measurements, imp, roi);
		}
		ImageProcessor ip = imp.getProcessor();
		ip.setRoi(roi.getPolygon());
		ImageStatistics stats = new ImageStatistics();
		saveResults(stats, roi);
	}
	
	void measureLength(Roi roi) {
		if (rt.getCounter()>0) {
			if (!IJ.isResultsWindow()) reset();
			boolean update = false;
			int index = rt.getColumnIndex("Length");
			if (index<0 || !rt.columnExists(index)) update=true;
			if (roi.getType()==Roi.LINE) {
				index = rt.getColumnIndex("Angle");
				if (index<0 || !rt.columnExists(index)) update=true;
			}
			if (update) rt.update(measurements, imp, roi);
		}
		boolean straightLine = roi.getType()==Roi.LINE;
		int lineWidth = (int)Math.round(roi.getStrokeWidth());
		ImageProcessor ip2;
		Rectangle saveR = null;
		if (straightLine && lineWidth>1) {
			ip2 = imp.getProcessor();
			saveR = ip2.getRoi();
			ip2.setRoi(roi.getPolygon());
		} else if (lineWidth>1) {
			if ((measurements&AREA)!=0 || (measurements&MEAN)!=0)
				ip2 = (new Straightener()).straightenLine(imp, lineWidth);
			else {
				saveResults(new ImageStatistics(), roi);
				return;
			}
		} else {
			ProfilePlot profile = new ProfilePlot(imp);
			double[] values = profile.getProfile();
			if (values==null) return;
			ip2 = new FloatProcessor(values.length, 1, values);
			if (straightLine) {
				Line l = (Line)roi;
				if ((l.y1==l.y2||l.x1==l.x2)&&l.x1==l.x1d&& l.y1==l.y1d&& l.x2==l.x2d&& l.y2==l.y2d)
					ip2.setRoi(0, 0, ip2.getWidth()-1, 1);
			}
		}
		ImageStatistics stats = ImageStatistics.getStatistics(ip2, AREA+MEAN+STD_DEV+MODE+MIN_MAX, imp.getCalibration());
		if (saveR!=null) ip2.setRoi(saveR);
		saveResults(stats, roi);
	}
	
	/** Saves the measurements specified in the "Set Measurements" dialog,
		or by calling setMeasurements(), in the default results table.
	*/
	public void saveResults(ImageStatistics stats, Roi roi) {
		if (rt.getColumnHeading(ResultsTable.LAST_HEADING)==null)
			reset();
		incrementCounter();
		int counter = rt.getCounter();
		if (counter<=MAX_STANDARDS) {
			if (umeans==null) umeans = new float[MAX_STANDARDS];
			umeans[counter-1] = (float)stats.umean;
		}
		if ((measurements&LABELS)!=0)
			rt.addLabel("Label", getFileName());
		if ((measurements&AREA)!=0) rt.addValue(ResultsTable.AREA,stats.area);
		if ((measurements&MEAN)!=0) rt.addValue(ResultsTable.MEAN,stats.mean);
		if ((measurements&STD_DEV)!=0) rt.addValue(ResultsTable.STD_DEV,stats.stdDev);
		if ((measurements&MODE)!=0) rt.addValue(ResultsTable.MODE, stats.dmode);
		if ((measurements&MIN_MAX)!=0) {
			if (showMin) rt.addValue(ResultsTable.MIN,stats.min);
			rt.addValue(ResultsTable.MAX,stats.max);
		}
		if ((measurements&CENTROID)!=0) {
			rt.addValue(ResultsTable.X_CENTROID,stats.xCentroid);
			rt.addValue(ResultsTable.Y_CENTROID,stats.yCentroid);
		}
		if ((measurements&CENTER_OF_MASS)!=0) {
			rt.addValue(ResultsTable.X_CENTER_OF_MASS,stats.xCenterOfMass);
			rt.addValue(ResultsTable.Y_CENTER_OF_MASS,stats.yCenterOfMass);
		}
		if ((measurements&PERIMETER)!=0 || (measurements&SHAPE_DESCRIPTORS)!=0) {
			double perimeter;
			if (roi!=null)
				perimeter = roi.getLength();
			else
				perimeter = 0.0;
			if ((measurements&PERIMETER)!=0) 
				rt.addValue(ResultsTable.PERIMETER,perimeter);
			if ((measurements&SHAPE_DESCRIPTORS)!=0) {
				double circularity = perimeter==0.0?0.0:4.0*Math.PI*(stats.area/(perimeter*perimeter));
				if (circularity>1.0) circularity = 1.0;
				rt.addValue(ResultsTable.CIRCULARITY, circularity);
				Polygon ch = null;
				boolean isArea = roi!=null && roi.isArea();
				if (isArea)
					ch = roi.getConvexHull();
				rt.addValue(ResultsTable.ASPECT_RATIO, isArea?stats.major/stats.minor:0.0);
				rt.addValue(ResultsTable.ROUNDNESS, isArea?4.0*stats.area/(Math.PI*stats.major*stats.major):0.0);
				rt.addValue(ResultsTable.SOLIDITY, ch!=null?stats.pixelCount/getArea(ch):Double.NaN);
				//rt.addValue(ResultsTable.CONVEXITY, getConvexPerimeter(roi, ch)/perimeter);
			}
		}
		if ((measurements&RECT)!=0) {
			if (roi!=null && roi.isLine()) {
				Rectangle bounds = roi.getBounds();
				rt.addValue(ResultsTable.ROI_X, bounds.x);
				rt.addValue(ResultsTable.ROI_Y, bounds.y);
				rt.addValue(ResultsTable.ROI_WIDTH, bounds.width);
				rt.addValue(ResultsTable.ROI_HEIGHT, bounds.height);
			} else {
				rt.addValue(ResultsTable.ROI_X,stats.roiX);
				rt.addValue(ResultsTable.ROI_Y,stats.roiY);
				rt.addValue(ResultsTable.ROI_WIDTH,stats.roiWidth);
				rt.addValue(ResultsTable.ROI_HEIGHT,stats.roiHeight);
			}
		}
		if ((measurements&ELLIPSE)!=0) {
			rt.addValue(ResultsTable.MAJOR,stats.major);
			rt.addValue(ResultsTable.MINOR,stats.minor);
			rt.addValue(ResultsTable.ANGLE,stats.angle);
		}
		if ((measurements&FERET)!=0) {
			boolean extras = true;
			double FeretDiameter=Double.NaN, feretAngle=Double.NaN, minFeret=Double.NaN,
				feretX=Double.NaN, feretY=Double.NaN;
			if (roi!=null) {
				double[] a = roi.getFeretValues();
				if (a!=null) {
					FeretDiameter = a[0];
					feretAngle = a[1];
					minFeret = a[2];
					feretX = a[3];
					feretY = a[4];
				}
			}
			rt.addValue(ResultsTable.FERET, FeretDiameter);
			rt.addValue(ResultsTable.FERET_X, feretX);
			rt.addValue(ResultsTable.FERET_Y, feretY);
			rt.addValue(ResultsTable.FERET_ANGLE, feretAngle);
			rt.addValue(ResultsTable.MIN_FERET, minFeret);
		}
		if ((measurements&INTEGRATED_DENSITY)!=0) {
			rt.addValue(ResultsTable.INTEGRATED_DENSITY,stats.area*stats.mean);
			rt.addValue(ResultsTable.RAW_INTEGRATED_DENSITY,stats.pixelCount*stats.umean);
		}
		if ((measurements&MEDIAN)!=0) rt.addValue(ResultsTable.MEDIAN, stats.median);
		if ((measurements&SKEWNESS)!=0) rt.addValue(ResultsTable.SKEWNESS, stats.skewness);
		if ((measurements&KURTOSIS)!=0) rt.addValue(ResultsTable.KURTOSIS, stats.kurtosis);
		if ((measurements&AREA_FRACTION)!=0) rt.addValue(ResultsTable.AREA_FRACTION, stats.areaFraction);
		if ((measurements&STACK_POSITION)!=0) {
			boolean update = false;
			if (imp!=null && (imp.isHyperStack()||imp.isComposite())) {
				int[] position = imp.convertIndexToPosition(imp.getCurrentSlice());
				if (imp.getNChannels()>1) {
					int index = rt.getColumnIndex("Ch");
					if (index<0 || !rt.columnExists(index)) update=true;
					rt.addValue("Ch", position[0]);
				}
				if (imp.getNSlices()>1) {
					int index = rt.getColumnIndex("Slice");
					if (index<0 || !rt.columnExists(index)) update=true;
					rt.addValue("Slice", position[1]);
				}
				if (imp.getNFrames()>1) {
					int index = rt.getColumnIndex("Frame");
					if (index<0 || !rt.columnExists(index)) update=true;
					rt.addValue("Frame", position[2]);
				}
			} else {
				int index = rt.getColumnIndex("Slice");
				if (index<0 || !rt.columnExists(index)) update=true;
				rt.addValue("Slice", imp!=null?imp.getCurrentSlice():1.0);
			}
			if (update && rt==systemRT) rt.update(measurements, imp, roi);
		}
		if (roi!=null) {
			if (roi.isLine()) {
				rt.addValue("Length", roi.getLength());
				if (roi.getType()==Roi.LINE) {
					double angle = 0.0;
					Line l = (Line)roi;
					angle = roi.getAngle(l.x1, l.y1, l.x2, l.y2);
					rt.addValue("Angle", angle);
				}
			} else if (roi.getType()==Roi.ANGLE)
				rt.addValue("Angle", ((PolygonRoi)roi).getAngle());
			else if (roi.getType()==Roi.POINT)
				savePoints(roi);
		}
	}
		
	final double getArea(Polygon p) {
		int carea = 0;
		int iminus1;
		for (int i=0; i<p.npoints; i++) {
			iminus1 = i-1;
			if (iminus1<0) iminus1=p.npoints-1;
			carea += (p.xpoints[i]+p.xpoints[iminus1])*(p.ypoints[i]-p.ypoints[iminus1]);
		}
		return (Math.abs(carea/2.0));
	}
	
	/*
	final double getConvexPerimeter(Roi roi, Polygon ch) {
		if (roi==null || ch==null || !(roi instanceof PolygonRoi))
			return 0.0;
		int[] xp = ((PolygonRoi)roi).getXCoordinates();
		int[] yp = ((PolygonRoi)roi).getYCoordinates();
		int n = ((PolygonRoi)roi).getNCoordinates();
		double perim = getPerimeter(xp, yp, n);
		double convexPerim = getPerimeter(ch.xpoints, ch.ypoints, ch.npoints);
		return convexPerim;
	}
	
	final double getPerimeter(int[] xp, int yp[], int n) {
		double dx, dy, perim=0.0;
		for (int i=0; i<n-1; i++) {
			dx = xp[i+1]-xp[i];
			dy = yp[i+1]-yp[i];
			perim += Math.sqrt(dx*dx+dy*dy);
		}
		dx = xp[n-1] - xp[0];
		dy = yp[n-1] - yp[0];
		perim += Math.sqrt(dx*dx+dy*dy);
		return perim;
	}
	*/
	
	void savePoints(Roi roi) {
		if (imp==null) {
			rt.addValue("X", 0.0);
			rt.addValue("Y", 0.0);
			rt.addValue("Slice", 0.0);
			return;
		}
		if ((measurements&AREA)!=0)
			rt.addValue(ResultsTable.AREA,0);
		Polygon p = roi.getPolygon();
		ImageProcessor ip = imp.getProcessor();
		Calibration cal = imp.getCalibration();
		int x = p.xpoints[0];
		int y = p.ypoints[0];
		double value = ip.getPixelValue(x,y);
		if (markWidth>0 && !((IjxToolbar) CentralLookup.getDefault().lookup(IjxToolbar.class)).getMultiPointMode()) {
			ip.setColor(((IjxToolbar) CentralLookup.getDefault().lookup(IjxToolbar.class)).getForegroundColor());
			ip.setLineWidth(markWidth);
			ip.moveTo(x,y);
			ip.lineTo(x,y);
			imp.updateAndDraw();
			ip.setLineWidth(Line.getWidth());
		}
		rt.addValue("X", cal.getX(x));
		rt.addValue("Y", cal.getY(y, imp.getHeight()));
		if (imp.isHyperStack() || imp.isComposite()) {
			if (imp.getNChannels()>1)
				rt.addValue("Ch", imp.getChannel());
			if (imp.getNSlices()>1)
				rt.addValue("Slice", imp.getSlice());
			if (imp.getNFrames()>1)
				rt.addValue("Frame", imp.getFrame());
		} else
			rt.addValue("Slice", cal.getZ(imp.getCurrentSlice()));
		if (imp.getProperty("FHT")!=null) {
			double center = imp.getWidth()/2.0;
			y = imp.getHeight()-y-1;
			double r = Math.sqrt((x-center)*(x-center) + (y-center)*(y-center));
			if (r<1.0) r = 1.0;
			double theta = Math.atan2(y-center, x-center);
			theta = theta*180.0/Math.PI;
			if (theta<0) theta = 360.0+theta;
			rt.addValue("R", (imp.getWidth()/r)*cal.pixelWidth);
			rt.addValue("Theta", theta);
		}
		//if ((measurements&MEAN)==0)
		//	rt.addValue("Mean", value);
	}

	String getFileName() {
		String s = "";
		if (imp!=null) {
			if (redirectTarget!=0) {
				IjxImagePlus rImp = WindowManager.getImage(redirectTarget);
				if (rImp==null) rImp = redirectImage;
				if (rImp!=null) s = rImp.getTitle();				
			} else
				s = imp.getTitle();
			//int len = s.length();
			//if (len>4 && s.charAt(len-4)=='.' && !Character.isDigit(s.charAt(len-1)))
			//	s = s.substring(0,len-4); 
			Roi roi = imp.getRoi();
			String roiName = roi!=null?roi.getName():null;
			if (roiName!=null)
				s += ":"+roiName;
			if (imp.getStackSize()>1) {
				IjxImageStack stack = imp.getStack();
				int currentSlice = imp.getCurrentSlice();
				String label = stack.getShortSliceLabel(currentSlice);
				String colon = s.equals("")?"":":";
				if (label!=null && !label.equals(""))
					s += colon+label;
				else
					s += colon+currentSlice;
			}
		}
		return s;
	}

	/** Writes the last row in the system results table to the Results window. */
	public void displayResults() {
		int counter = rt.getCounter();
		if (counter==1)
			IJ.setColumnHeadings(rt.getColumnHeadings());		
		IJ.write(rt.getRowAsString(counter-1));
	}

	/** Redisplays the results table. */
	public void updateHeadings() {
		rt.show("Results");
	}

	/** Converts a number to a formatted string with a tab at the end. */
	public String n(double n) {
		String s;
		if (Math.round(n)==n)
			s = ResultsTable.d2s(n,0);
		else
			s = ResultsTable.d2s(n,precision);
		return s+"\t";
	}
		
	void incrementCounter() {
		//counter++;
		if (rt==null) rt = systemRT;
		rt.incrementCounter();
		unsavedMeasurements = true;
	}
	
	public void summarize() {
		rt = systemRT;
		if (rt.getCounter()==0)
			return;
		if (summarized)
			rt.show("Results");
		measurements = systemMeasurements;
		min = new StringBuffer(100);
		max = new StringBuffer(100);
		mean = new StringBuffer(100);
		sd = new StringBuffer(100);
		min.append("Min\t");
		max.append("Max\t");
		mean.append("Mean\t");
		sd.append("SD\t");
		if ((measurements&LABELS)!=0) {
			min.append("\t");
			max.append("\t");
			mean.append("\t");
			sd.append("\t");
		}
		summarizeAreas();
		int index = rt.getColumnIndex("Angle");
		if (rt.columnExists(index)) add2(index);
		index = rt.getColumnIndex("Length");
		if (rt.columnExists(index)) add2(index);
		TextPanel tp = IJ.getTextPanel();
		if (tp!=null) {
			String worksheetHeadings = tp.getColumnHeadings();		
			if (worksheetHeadings.equals(""))
				IJ.setColumnHeadings(rt.getColumnHeadings());
		}		
		IJ.write("");		
		IJ.write(new String(mean));		
		IJ.write(new String(sd));		
		IJ.write(new String(min));		
		IJ.write(new String(max));
		IJ.write("");		
		mean = null;		
		sd = null;		
		min = null;		
		max = null;
		summarized = true;		
	}
	
	void summarizeAreas() {
		if ((measurements&AREA)!=0) add2(ResultsTable.AREA);
		if ((measurements&MEAN)!=0) add2(ResultsTable.MEAN);
		if ((measurements&STD_DEV)!=0) add2(ResultsTable.STD_DEV);
		if ((measurements&MODE)!=0) add2(ResultsTable.MODE);
		if ((measurements&MIN_MAX)!=0) {
			if (showMin) add2(ResultsTable.MIN);
			add2(ResultsTable.MAX);
		}
		if ((measurements&CENTROID)!=0) {
			add2(ResultsTable.X_CENTROID);
			add2(ResultsTable.Y_CENTROID);
		}
		if ((measurements&CENTER_OF_MASS)!=0) {
			add2(ResultsTable.X_CENTER_OF_MASS);
			add2(ResultsTable.Y_CENTER_OF_MASS);
		}
		if ((measurements&PERIMETER)!=0)
			add2(ResultsTable.PERIMETER);
		if ((measurements&RECT)!=0) {
			add2(ResultsTable.ROI_X);
			add2(ResultsTable.ROI_Y);
			add2(ResultsTable.ROI_WIDTH);
			add2(ResultsTable.ROI_HEIGHT);
		}
		if ((measurements&ELLIPSE)!=0) {
			add2(ResultsTable.MAJOR);
			add2(ResultsTable.MINOR);
			add2(ResultsTable.ANGLE);
		}
		if ((measurements&SHAPE_DESCRIPTORS)!=0)
			add2(ResultsTable.CIRCULARITY);
		if ((measurements&FERET)!=0)
			add2(ResultsTable.FERET);
		if ((measurements&INTEGRATED_DENSITY)!=0)
			add2(ResultsTable.INTEGRATED_DENSITY);
		if ((measurements&MEDIAN)!=0)
			add2(ResultsTable.MEDIAN);
		if ((measurements&SKEWNESS)!=0)
			add2(ResultsTable.SKEWNESS);
		if ((measurements&KURTOSIS)!=0)
			add2(ResultsTable.KURTOSIS);
		if ((measurements&AREA_FRACTION)!=0)
			add2(ResultsTable.AREA_FRACTION);
		if ((measurements&STACK_POSITION)!=0) {
			int index = rt.getColumnIndex("Ch");
			if (rt.columnExists(index)) add2(index);
			index = rt.getColumnIndex("Slice");
			if (rt.columnExists(index)) add2(index);
			index = rt.getColumnIndex("Frame");
			if (rt.columnExists(index)) add2(index);
		}
		if ((measurements&FERET)!=0) {
			add2(ResultsTable.FERET_X);
			add2(ResultsTable.FERET_Y);
			add2(ResultsTable.FERET_ANGLE);
			add2(ResultsTable.MIN_FERET);
		}
		if ((measurements&SHAPE_DESCRIPTORS)!=0) {
			add2(ResultsTable.ASPECT_RATIO);
			add2(ResultsTable.ROUNDNESS);
			add2(ResultsTable.SOLIDITY);
		}
	}

	private void add2(int column) {
		float[] c = column>=0?rt.getColumn(column):null;
		if (c!=null) {
			ImageProcessor ip = new FloatProcessor(c.length, 1, c, null);
			if (ip==null)
				return;
			ImageStatistics stats = new FloatStatistics(ip);
			if (stats==null)
				return;
			mean.append(n(stats.mean));
			min.append(n(stats.min));
			max.append(n(stats.max));
			sd.append(n(stats.stdDev));
		} else {
			mean.append("-\t");
			min.append("-\t");
			max.append("-\t");
			sd.append("-\t");
		}
	}

	/** Returns the current measurement count. */
	public static int getCounter() {
		return systemRT.getCounter();
	}

	/** Sets the measurement counter to zero. Displays a dialog that
	    allows the user to save any existing measurements. Returns
	    false if the user cancels the dialog.
	*/
	public synchronized static boolean resetCounter() {
		TextPanel tp = IJ.isResultsWindow()?IJ.getTextPanel():null;
		int counter = systemRT.getCounter();
		int lineCount = tp!=null?IJ.getTextPanel().getLineCount():0;
		IjxApplication ij = IJ.getInstance();
		boolean macro = (IJ.macroRunning()&&!switchingModes) || Interpreter.isBatchMode();
		switchingModes = false;
		if (counter>0 && lineCount>0 && unsavedMeasurements && !macro && ij!=null && !ij.quitting()) {
			YesNoCancelDialog d = new YesNoCancelDialog(IJ.getTopComponentFrame(), "ImageJ", "Save "+counter+" measurements?");
			if (d.cancelPressed())
				return false;
			else if (d.yesPressed()) {
				if (!(new MeasurementsWriter()).save(""))
					return false;
			}
		}
		umeans = null;
		systemRT.reset();
		unsavedMeasurements = false;
		if (tp!=null) tp.clear();
		summarized = false;
		return true;
	}
	
	public static void setUnsavedMeasurements(boolean b) {
		unsavedMeasurements = b;
	}
	
	// Returns the measurement options defined in the Set Measurements dialog. */
	public static int getMeasurements() {
		return systemMeasurements;
	}

	/** Sets the system-wide measurement options. */
	public static void setMeasurements(int measurements) {
		systemMeasurements = measurements;
	}

	/** Sets the specified system-wide measurement option. */
	public static void setMeasurement(int option, boolean state) {
			if (state)
				systemMeasurements |= option;
			else
				systemMeasurements &= ~option;
	}

	/** Called once when ImageJ quits. */
	public void savePreferences(Properties prefs) {
		prefs.put(MEASUREMENTS, Integer.toString(systemMeasurements));
		prefs.put(MARK_WIDTH, Integer.toString(markWidth));
		prefs.put(PRECISION, Integer.toString(precision));	}

	/** Returns an array containing the first 20 uncalibrated means. */
	public static float[] getUMeans() {
		return umeans;
	}

	/** Returns the default results table. This table should only
		be displayed in a the "Results" window. */
	public static ResultsTable getResultsTable() {
		return systemRT;
	}

	/** Returns the number of digits displayed to the right of decimal point. */
	public static int getPrecision() {
		return precision;
	}

	/** Sets the number of digits displayed to the right of decimal point. */
	public static void setPrecision(int decimalPlaces) {
		if (decimalPlaces<0) decimalPlaces = 0;
		if (decimalPlaces>9) decimalPlaces = 9;
		precision = decimalPlaces;
	}

	/** Returns an updated Y coordinate based on
		the current "Invert Y Coordinates" flag. */
	public static int updateY(int y, int imageHeight) {
		if ((systemMeasurements&INVERT_Y)!=0)
			y = imageHeight-y-1;
		return y;
	}
	
	/** Returns an updated Y coordinate based on
		the current "Invert Y Coordinates" flag. */
	public static double updateY(double y, int imageHeight) {
		if ((systemMeasurements&INVERT_Y)!=0)
			y = imageHeight-y-1;
		return y;
	}
	
	/** Sets the default headings ("Area", "Mean", etc.). */
	public static void setDefaultHeadings() {
		systemRT.setDefaultHeadings();
	}

	public static void setOption(String option, boolean b) {
		if (option.indexOf("min")!=-1)
			showMin = b;
	}
	
	public static void setResultsTable(ResultsTable rt) {
		if (rt==null) rt = new ResultsTable();
		systemRT = rt;
	}

}
	
