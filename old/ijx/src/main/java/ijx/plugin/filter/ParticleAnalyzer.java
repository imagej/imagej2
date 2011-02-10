package ijx.plugin.filter;
import ijx.process.ImageProcessor;
import ijx.process.FloatStatistics;
import ijx.process.FloatProcessor;
import ijx.process.ByteStatistics;
import ijx.process.ColorStatistics;
import ijx.process.FloodFiller;
import ijx.process.ColorProcessor;
import ijx.process.ByteProcessor;
import ijx.process.ImageStatistics;
import ijx.process.ShortProcessor;
import ijx.process.PolygonFiller;
import ijx.process.ShortStatistics;
import ijx.plugin.api.PlugInFilter;
import ijx.gui.Wand;
import ijx.gui.Overlay;
import ijx.gui.dialog.GenericDialog;
import ijx.roi.Roi;
import ijx.roi.PolygonRoi;
import ijx.measure.ResultsTable;
import ijx.measure.Calibration;
import ijx.measure.Measurements;
import ijx.Undo;
import ijx.Macro;
import ijx.Prefs;
import ijx.WindowManager;
import ijx.IJ;
import ijx.LookUpTable;
import java.awt.*;
import java.awt.image.IndexColorModel;
import java.util.Properties;


import ijx.text.*;
import ijx.roi.RoiManager;
import ijx.macro.Interpreter;
import imagej.util.Tools;
import ijx.IjxImagePlus;
import ijx.IjxImageStack;
import ijx.SavesPrefs;
import ijx.gui.IjxImageWindow;
import ijx.gui.IjxWindow;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=SavesPrefs.class)


/** Implements ImageJ's Analyze Particles command.
	<p>
	<pre>
	for each line do
		for each pixel in this line do
			if the pixel value is "inside" the threshold range then
				trace the edge to mark the object
				do the measurement
				fill the object with a color outside the threshold range
			else
				continue the scan
	</pre>
*/
public class ParticleAnalyzer implements PlugInFilter, Measurements, SavesPrefs {

	/** Display results in the ImageJ console. */
	public static final int SHOW_RESULTS = 1;
	
	/** Obsolete */
	public static final int SHOW_SUMMARY = 2;
	
	/** Display image containing outlines of measured particles. */
	public static final int SHOW_OUTLINES = 4;
	
	/** Do not measure particles touching edge of image. */
	public static final int EXCLUDE_EDGE_PARTICLES = 8;
	
	/** Display image containing grayscales masks that identify measured particles. */
	public static final int SHOW_ROI_MASKS = 16;
	
	/** Display a progress bar. */
	public static final int SHOW_PROGRESS = 32;
	
	/** Clear ImageJ console before starting. */
	public static final int CLEAR_WORKSHEET = 64;
	
	/** Record starting coordinates so outline can be recreated later using doWand(x,y). */
	public static final int RECORD_STARTS = 128;

	/** Display a summary. */
	public static final int DISPLAY_SUMMARY = 256;

	/** Do not display particle outline image. */
	public static final int SHOW_NONE = 512;

	/** Flood fill to ignore interior holes. */
	public static final int INCLUDE_HOLES = 1024;
	
	/** Add particles to ROI Manager. */
	public static final int ADD_TO_MANAGER = 2048;

	/** Display image containing binary masks of measured particles. */
	public static final int SHOW_MASKS = 4096;

	/** Use 4-connected particle tracing. */
	public static final int FOUR_CONNECTED = 8192;

	/** Replace original image with masks. */
	public static final int IN_SITU_SHOW = 16384;

	/** Display particle outlines as an overlay. */
	public static final int SHOW_OVERLAY_OUTLINES = 32768;
	
	/** Display filled particle as an overlay. */
	public static final int SHOW_OVERLAY_MASKS = 65536;

	static final String OPTIONS = "ap.options";
	
	static final int BYTE=0, SHORT=1, FLOAT=2, RGB=3;
	static final double DEFAULT_MIN_SIZE = 0.0;
	static final double DEFAULT_MAX_SIZE = Double.POSITIVE_INFINITY;
	
	private static double staticMinSize = 0.0;
	private static double staticMaxSize = DEFAULT_MAX_SIZE;
	private static boolean pixelUnits;
	private static int staticOptions = Prefs.getInt(OPTIONS,CLEAR_WORKSHEET);
	private static String[] showStrings = {"Nothing", "Outlines", "Masks", "Ellipses", "Count Masks", "Overlay Outlines", "Overlay Masks"};
	private static double staticMinCircularity=0.0, staticMaxCircularity=1.0;
	private static String prevHdr;
		
	protected static final int NOTHING=0,OUTLINES=1,MASKS=2,ELLIPSES=3,ROI_MASKS=4,
		OVERLAY_OUTLINES=5, OVERLAY_MASKS=6;
	protected static int staticShowChoice;
	protected IjxImagePlus imp;
	protected ResultsTable rt;
	protected Analyzer analyzer;
	protected int slice;
	protected boolean processStack;
	protected boolean showResults,excludeEdgeParticles,showSizeDistribution,
		resetCounter,showProgress, recordStarts, displaySummary, floodFill,
		addToManager, inSituShow;
		
	private String summaryHdr = "Slice\tCount\tTotal Area\tAverage Size\tArea Fraction";
	private double level1, level2;
	private double minSize, maxSize;
	private double minCircularity, maxCircularity;
	private int showChoice;
	private int options;
	private int measurements;
	private Calibration calibration;
	private String arg;
	private double fillColor;
	private boolean thresholdingLUT;
	private ImageProcessor drawIP;
	private int width,height;
	private boolean canceled;
	private IjxImageStack outlines;
	private IndexColorModel customLut;
	private int particleCount;
	private int maxParticleCount = 0;
	private int totalCount;
	private TextWindow tw;
	private Wand wand;
	private int imageType, imageType2;
	private boolean roiNeedsImage;
	private int minX, maxX, minY, maxY;
	private IjxImagePlus redirectImp;
	private ImageProcessor redirectIP;
	private PolygonFiller pf;
	private Roi saveRoi;
	private int beginningCount;
	private Rectangle r;
	private ImageProcessor mask;
	private double totalArea;
	private FloodFiller ff;
	private Polygon polygon;
	private RoiManager roiManager;
	private IjxImagePlus outputImage;
	private boolean hideOutputImage;
	private int roiType;
	private int wandMode = Wand.LEGACY_MODE;
	private Overlay overlay;
	boolean blackBackground;

			
	/** Constructs a ParticleAnalyzer.
		@param options	a flag word created by Oring SHOW_RESULTS, EXCLUDE_EDGE_PARTICLES, etc.
		@param measurements a flag word created by ORing constants defined in the Measurements interface
		@param rt		a ResultsTable where the measurements will be stored
		@param minSize	the smallest particle size in pixels
		@param maxSize	the largest particle size in pixels
		@param minCirc	minimum circularity
		@param maxCirc	maximum circularity
	*/
	public ParticleAnalyzer(int options, int measurements, ResultsTable rt, double minSize, double maxSize, double minCirc, double maxCirc) {
		this.options = options;
		this.measurements = measurements;
		this.rt = rt;
		if (this.rt==null)
			this.rt = new ResultsTable();
		this.minSize = minSize;
		this.maxSize = maxSize;
		this.minCircularity = minCirc;
		this.maxCircularity = maxCirc;
		slice = 1;
		if ((options&SHOW_ROI_MASKS)!=0)
			showChoice = ROI_MASKS;
		if ((options&SHOW_OVERLAY_OUTLINES)!=0)
			showChoice = OVERLAY_OUTLINES;
		if ((options&SHOW_OVERLAY_MASKS)!=0)
			showChoice = OVERLAY_MASKS;
		if ((options&SHOW_OUTLINES)!=0)
			showChoice = OUTLINES;
		if ((options&SHOW_MASKS)!=0)
			showChoice = MASKS;
		if ((options&SHOW_NONE)!=0)
			showChoice = NOTHING;
		if ((options&FOUR_CONNECTED)!=0) {
			wandMode = Wand.FOUR_CONNECTED;
			options |= INCLUDE_HOLES;
		}
	}
	
	/** Constructs a ParticleAnalyzer using the default min and max circularity values (0 and 1). */
	public ParticleAnalyzer(int options, int measurements, ResultsTable rt, double minSize, double maxSize) {
		this(options, measurements, rt, minSize, maxSize, 0.0, 1.0);
	}

	/** Default constructor */
	public ParticleAnalyzer() {
		slice = 1;
	}
	
	public int setup(String arg, IjxImagePlus imp) {
		this.arg = arg;
		this.imp = imp;
		IJ.register(ParticleAnalyzer.class);
		if (imp==null)
			{IJ.noImage();return DONE;}
		if (imp.getBitDepth()==24 && !isBinaryRGB(imp)) {
			IJ.error("Particle Analyzer",
			"RGB images must be converted to binary using\n"
			+"Process>Binary>Make Binary or thresholded\n"
			+"using Image>Adjust>Color Threshold.");
			return DONE;
		}
		if (!showDialog())
			return DONE;
		int baseFlags = DOES_ALL+NO_CHANGES+NO_UNDO;
		int flags = IJ.setupDialog(imp, baseFlags);
		processStack = (flags&DOES_STACKS)!=0;
		slice = 0;
		saveRoi = imp.getRoi();
		if (saveRoi!=null && saveRoi.getType()!=Roi.RECTANGLE && saveRoi.isArea())
			polygon = saveRoi.getPolygon();
		imp.startTiming();
		return flags;
	}

	public void run(ImageProcessor ip) {
		if (canceled)
			return;
		slice++;
		if (imp.getStackSize()>1 && processStack)
			imp.setSlice(slice);
		if (imp.getType()==IjxImagePlus.COLOR_RGB) {
			ip = ip.convertToByte(false);
			int t = Prefs.blackBackground?255:0;
			ip.setThreshold(t, t, ImageProcessor.NO_LUT_UPDATE);
		}		
		if (!analyze(imp, ip))
			canceled = true;
		if (slice==imp.getStackSize()) {
			imp.updateAndDraw();
			if (saveRoi!=null) imp.setRoi(saveRoi);
		}
	}
	
	/** Displays a modal options dialog. */
	public boolean showDialog() {
		Calibration cal = imp!=null?imp.getCalibration():(new Calibration());
		double unitSquared = cal.pixelWidth*cal.pixelHeight;
		if (pixelUnits)
			unitSquared = 1.0;
		if (Macro.getOptions()!=null) {
			boolean oldMacro = updateMacroOptions();
			if (oldMacro) unitSquared = 1.0;
			staticMinSize = 0.0; staticMaxSize = DEFAULT_MAX_SIZE;
			staticMinCircularity=0.0; staticMaxCircularity=1.0;
			staticShowChoice = NOTHING;
		}
		GenericDialog gd = new GenericDialog("Analyze Particles");
		minSize = staticMinSize;
		maxSize = staticMaxSize;
		minCircularity = staticMinCircularity;
		maxCircularity = staticMaxCircularity;
		showChoice = staticShowChoice;
		if (maxSize==999999) maxSize = DEFAULT_MAX_SIZE;
		options = staticOptions;
		String unit = cal.getUnit();
		boolean scaled = cal.scaled();
		if (unit.equals("inch")) {
			unit = "pixel";
			unitSquared = 1.0;
			scaled = false;
			pixelUnits = true;
		}
		String units = unit+"^2";
		int places = 0;
		double cmin = minSize*unitSquared;
		if ((int)cmin!=cmin) places = 2;
		double cmax = maxSize*unitSquared;
		if ((int)cmax!=cmax && cmax!=DEFAULT_MAX_SIZE) places = 2;
		String minStr = ResultsTable.d2s(cmin,places);
		if (minStr.indexOf("-")!=-1) {
			for (int i=places; i<=6; i++) {
				minStr = ResultsTable.d2s(cmin, i);
				if (minStr.indexOf("-")==-1) break;
			}
		}
		String maxStr = ResultsTable.d2s(cmax, places);
		if (maxStr.indexOf("-")!=-1) {
			for (int i=places; i<=6; i++) {
				maxStr = ResultsTable.d2s(cmax, i);
				if (maxStr.indexOf("-")==-1) break;
			}
		}
		if (scaled)
			gd.setInsets(5, 0, 0);
		gd.addStringField("Size ("+units+"):", minStr+"-"+maxStr, 12);
		if (scaled) {
			gd.setInsets(0, 40, 5);
			gd.addCheckbox("Pixel units", pixelUnits);
		}
		gd.addStringField("Circularity:", IJ.d2s(minCircularity)+"-"+IJ.d2s(maxCircularity), 12);
		gd.addChoice("Show:", showStrings, showStrings[showChoice]);
		String[] labels = new String[8];
		boolean[] states = new boolean[8];
		labels[0]="Display results"; states[0] = (options&SHOW_RESULTS)!=0;
		labels[1]="Exclude on edges"; states[1]=(options&EXCLUDE_EDGE_PARTICLES)!=0;
		labels[2]="Clear results"; states[2]=(options&CLEAR_WORKSHEET)!=0;
		labels[3]="Include holes"; states[3]=(options&INCLUDE_HOLES)!=0;
		labels[4]="Summarize"; states[4]=(options&DISPLAY_SUMMARY)!=0;
		labels[5]="Record starts"; states[5]=(options&RECORD_STARTS)!=0;
		labels[6]="Add to Manager"; states[6]=(options&ADD_TO_MANAGER)!=0;
		labels[7]="In_situ Show"; states[7]=(options&IN_SITU_SHOW)!=0;
		gd.addCheckboxGroup(4, 2, labels, states);
		gd.addHelp(IJ.URL+"/docs/menus/analyze.html#ap");
		gd.showDialog();
		if (gd.wasCanceled())
			return false;
			
		String size = gd.getNextString(); // min-max size
		if (scaled)
			pixelUnits = gd.getNextBoolean();
		if (pixelUnits)
			unitSquared = 1.0;
		else
			unitSquared = cal.pixelWidth*cal.pixelHeight;
		String[] minAndMax = Tools.split(size, " -");
		double mins = Tools.parseDouble(minAndMax[0]);
		double maxs = minAndMax.length==2?Tools.parseDouble(minAndMax[1]):Double.NaN;
		minSize = Double.isNaN(mins)?DEFAULT_MIN_SIZE:mins/unitSquared;
		maxSize = Double.isNaN(maxs)?DEFAULT_MAX_SIZE:maxs/unitSquared;
		if (minSize<DEFAULT_MIN_SIZE) minSize = DEFAULT_MIN_SIZE;
		if (maxSize<minSize) maxSize = DEFAULT_MAX_SIZE;
		staticMinSize = minSize;
		staticMaxSize = maxSize;
		
		minAndMax = Tools.split(gd.getNextString(), " -"); // min-max circularity
		double minc = Tools.parseDouble(minAndMax[0]);
		double maxc = minAndMax.length==2?Tools.parseDouble(minAndMax[1]):Double.NaN;
		minCircularity = Double.isNaN(minc)?0.0:minc;
		maxCircularity = Double.isNaN(maxc)?1.0:maxc;
		if (minCircularity<0.0 || minCircularity>1.0) minCircularity = 0.0;
		if (maxCircularity<minCircularity || maxCircularity>1.0) maxCircularity = 1.0;
		if (minCircularity==1.0 && maxCircularity==1.0) minCircularity = 0.0;
		staticMinCircularity = minCircularity;
		staticMaxCircularity = maxCircularity;
		
		if (gd.invalidNumber()) {
			IJ.error("Bins invalid.");
			canceled = true;
			return false;
		}
		showChoice = gd.getNextChoiceIndex();
		staticShowChoice = showChoice;
		if (gd.getNextBoolean())
			options |= SHOW_RESULTS; else options &= ~SHOW_RESULTS;
		if (gd.getNextBoolean())
			options |= EXCLUDE_EDGE_PARTICLES; else options &= ~EXCLUDE_EDGE_PARTICLES;
		if (gd.getNextBoolean())
			options |= CLEAR_WORKSHEET; else options &= ~CLEAR_WORKSHEET;
		if (gd.getNextBoolean())
			options |= INCLUDE_HOLES; else options &= ~INCLUDE_HOLES;
		if (gd.getNextBoolean())
			options |= DISPLAY_SUMMARY; else options &= ~DISPLAY_SUMMARY;
		if (gd.getNextBoolean())
			options |= RECORD_STARTS; else options &= ~RECORD_STARTS;
		if (gd.getNextBoolean())
			options |= ADD_TO_MANAGER; else options &= ~ADD_TO_MANAGER;
		if (gd.getNextBoolean())
			options |= IN_SITU_SHOW; else options &= ~IN_SITU_SHOW;
		staticOptions = options;
		options |= SHOW_PROGRESS;
		if ((options&DISPLAY_SUMMARY)!=0)
			Analyzer.setMeasurements(Analyzer.getMeasurements()|AREA);
		return true;
	}
	
	private boolean isBinaryRGB(IjxImagePlus imp) {
		ImageProcessor ip = imp.getProcessor();
		int[] pixels = (int[])ip.getPixels();
		int size = imp.getWidth()*imp.getHeight();
		for (int i=0; i<size; i++) {
			if ((pixels[i]&0xffffff)!=0 && (pixels[i]&0xffffff)!=0xffffff)
				return false;
		}
		return true;
	}

	boolean updateMacroOptions() {
		String options = Macro.getOptions();
		int index = options.indexOf("maximum=");
		if (index==-1) return false;
		index +=8;
		int len = options.length();
		while (index<len-1 && options.charAt(index)!=' ')
			index++;
		if (index==len-1) return false;
		int min = (int)Tools.parseDouble(Macro.getValue(options, "minimum", "1"));
		int max = (int)Tools.parseDouble(Macro.getValue(options, "maximum", "999999"));
		options = "size="+min+"-"+max+options.substring(index, len);
		Macro.setOptions(options);
		return true;
	}

	/** Performs particle analysis on the specified image. Returns
		false if there is an error. */
	public boolean analyze(IjxImagePlus imp) {
		return analyze(imp, imp.getProcessor());
	}

	/** Performs particle analysis on the specified IjxImagePlus and
		ImageProcessor. Returns false if there is an error. */
	public boolean analyze(IjxImagePlus imp, ImageProcessor ip) {
		if (this.imp==null) this.imp = imp;
		showResults = (options&SHOW_RESULTS)!=0;
		excludeEdgeParticles = (options&EXCLUDE_EDGE_PARTICLES)!=0;
		resetCounter = (options&CLEAR_WORKSHEET)!=0;
		showProgress = (options&SHOW_PROGRESS)!=0;
		floodFill = (options&INCLUDE_HOLES)==0;
		recordStarts = (options&RECORD_STARTS)!=0;
		addToManager = (options&ADD_TO_MANAGER)!=0;
		displaySummary = (options&DISPLAY_SUMMARY)!=0;
		inSituShow = (options&IN_SITU_SHOW)!=0;
		outputImage = null;
		ip.snapshot();
		ip.setProgressBar(null);
		if (Analyzer.isRedirectImage()) {
			redirectImp = Analyzer.getRedirectImage(imp);
			if (redirectImp==null) return false;
			int depth = redirectImp.getStackSize();
			if (depth>1 && depth==imp.getStackSize()) {
				IjxImageStack redirectStack = redirectImp.getStack();
				redirectIP = redirectStack.getProcessor(imp.getCurrentSlice());
			} else
				redirectIP = redirectImp.getProcessor();
		}
		if (!setThresholdLevels(imp, ip))
			return false;
		width = ip.getWidth();
		height = ip.getHeight();
		if (!(showChoice==NOTHING||showChoice==OVERLAY_OUTLINES||showChoice==OVERLAY_MASKS)) {
			blackBackground = Prefs.blackBackground && inSituShow;
			if (slice==1)
				outlines = IJ.getFactory().newImageStack(width, height);
			if (showChoice==ROI_MASKS)
				drawIP = new ShortProcessor(width, height);
			else
				drawIP = new ByteProcessor(width, height);
			if (showChoice==ROI_MASKS)
				{} // Place holder for now...
			else if (showChoice==MASKS&&!blackBackground)
				drawIP.invertLut();
			else if (showChoice==OUTLINES) {
				if (!inSituShow) {
					if (customLut==null)
						makeCustomLut();
					drawIP.setColorModel(customLut);
				}
				drawIP.setFont(new Font("SansSerif", Font.PLAIN, 9));
			}
			outlines.addSlice(null, drawIP);

			if (showChoice==ROI_MASKS || blackBackground) {
				drawIP.setColor(Color.black);
				drawIP.fill();
				drawIP.setColor(Color.white);
			} else {
				drawIP.setColor(Color.white);
				drawIP.fill();
				drawIP.setColor(Color.black);
			}
		}
		calibration = redirectImp!=null?redirectImp.getCalibration():imp.getCalibration();
		
		if (rt==null) {
			rt = Analyzer.getResultsTable();
			analyzer = new Analyzer(imp);
		} else
			analyzer = new Analyzer(imp, measurements, rt);
		if (resetCounter && slice==1) {
			if (!Analyzer.resetCounter())
				return false;
		}
		beginningCount = Analyzer.getCounter();

		byte[] pixels = null;
		if (ip instanceof ByteProcessor)
			pixels = (byte[])ip.getPixels();
		if (r==null) {
			r = ip.getRoi();
			mask = ip.getMask();
			if (displaySummary) {
				if (mask!=null)
					totalArea = ImageStatistics.getStatistics(ip, AREA, calibration).area;
				else
					totalArea = r.width*calibration.pixelWidth*r.height*calibration.pixelHeight;
			}
		}
		minX=r.x; maxX=r.x+r.width; minY=r.y; maxY=r.y+r.height;
		if (r.width<width || r.height<height || mask!=null) {
			if (!eraseOutsideRoi(ip, r, mask)) return false;
		}
		int offset;
		double value;
		int inc = Math.max(r.height/25, 1);
		int mi = 0;
		IjxImageWindow win = imp.getWindow();
		if (win!=null)
			win.setRunning(true);
		if (measurements==0)
			measurements = Analyzer.getMeasurements();
		if (showChoice==ELLIPSES)
			measurements |= ELLIPSE;
		measurements &= ~LIMIT;	 // ignore "Limit to Threshold"
		roiNeedsImage = (measurements&PERIMETER)!=0 || (measurements&SHAPE_DESCRIPTORS)!=0 || (measurements&FERET)!=0;
		particleCount = 0;
		wand = new Wand(ip);
		pf = new PolygonFiller();
		if (floodFill) {
			ImageProcessor ipf = ip.duplicate();
			ipf.setValue(fillColor);
			ff = new FloodFiller(ipf);
		}
		roiType = Wand.allPoints()?Roi.FREEROI:Roi.TRACED_ROI;

		for (int y=r.y; y<(r.y+r.height); y++) {
			offset = y*width;
			for (int x=r.x; x<(r.x+r.width); x++) {
				if (pixels!=null)
					value = pixels[offset+x]&255;
				else if (imageType==SHORT)
					value = ip.getPixel(x, y);
				else
					value = ip.getPixelValue(x, y);
				if (value>=level1 && value<=level2)
					analyzeParticle(x, y, imp, ip);
			}
			if (showProgress && ((y%inc)==0))
				IJ.showProgress((double)(y-r.y)/r.height);
			if (win!=null)
				canceled = !win.isRunning();
			if (canceled) {
				Macro.abort();
				break;
			}
		}
		if (showProgress)
			IJ.showProgress(1.0);
		if (showResults)
			rt.updateResults();
		imp.killRoi();
		ip.resetRoi();
		ip.reset();
		if (displaySummary && IJ.getInstance()!=null)
			updateSliceSummary();
		if (addToManager && roiManager!=null)
			roiManager.setEditMode(imp, true);
		maxParticleCount = (particleCount > maxParticleCount) ? particleCount : maxParticleCount;
		totalCount += particleCount;
		if (!canceled)
			showResults();
		return true;
	}
	
	void updateSliceSummary() {
		int slices = imp.getStackSize();
		float[] areas = rt.getColumn(ResultsTable.AREA);
		String label = imp.getTitle();
		if (slices>1) {
			label = imp.getStack().getShortSliceLabel(slice);
			label = label!=null&&!label.equals("")?label:""+slice;
		}
		String aLine = null;
		if (areas==null) return;
		double sum = 0.0;
		int start = areas.length-particleCount;
		if (start<0)
			return;
		for (int i=start; i<areas.length; i++)
			sum += areas[i];
		int places = Analyzer.getPrecision();
		Calibration cal = imp.getCalibration();
		String total = "\t"+ResultsTable.d2s(sum,places);
		String average = "\t"+ResultsTable.d2s(sum/particleCount,places);
		String fraction = "\t"+ResultsTable.d2s(sum*100.0/totalArea,1);
		aLine = label+"\t"+particleCount+total+average+fraction;
		aLine = addMeans(aLine, start);
		if (slices==1) {
			IjxWindow frame = WindowManager.getFrame("Summary");
			if (frame!=null && (frame instanceof TextWindow) && summaryHdr.equals(prevHdr))
				tw = (TextWindow)frame;
		}
		if (tw==null) {
			String title = slices==1?"Summary":"Summary of "+imp.getTitle();
			tw = new TextWindow(title, summaryHdr, aLine, 450, 300);
			prevHdr = summaryHdr;
		} else
			tw.append(aLine);
	}

	String addMeans(String line, int start) {
		if ((measurements&MEAN)!=0) line=addMean(ResultsTable.MEAN, line, start);
		if ((measurements&MODE)!=0) line=addMean(ResultsTable.MODE, line, start);
		if ((measurements&PERIMETER)!=0)
			line=addMean(ResultsTable.PERIMETER, line, start);
		if ((measurements&ELLIPSE)!=0) {
			line=addMean(ResultsTable.MAJOR, line, start);
			line=addMean(ResultsTable.MINOR, line, start);
			line=addMean(ResultsTable.ANGLE, line, start);
		}
		if ((measurements&SHAPE_DESCRIPTORS)!=0) {
			line=addMean(ResultsTable.CIRCULARITY, line, start);
			line=addMean(ResultsTable.SOLIDITY, line, start);
		}
		if ((measurements&FERET)!=0) {
			line=addMean(ResultsTable.FERET, line, start);
			line=addMean(ResultsTable.FERET_X, line, start);
			line=addMean(ResultsTable.FERET_Y, line, start);
			line=addMean(ResultsTable.FERET_ANGLE, line, start);
			line=addMean(ResultsTable.MIN_FERET, line, start);
		}
		if ((measurements&INTEGRATED_DENSITY)!=0)
			line=addMean(ResultsTable.INTEGRATED_DENSITY, line, start);
		if ((measurements&MEDIAN)!=0)
			line=addMean(ResultsTable.MEDIAN, line, start);
		if ((measurements&SKEWNESS)!=0)
			line=addMean(ResultsTable.SKEWNESS, line, start);
		if ((measurements&KURTOSIS)!=0)
			line=addMean(ResultsTable.KURTOSIS, line, start);
		return line;
	}

	private String addMean(int column, String line, int start) {
		float[] c = column>=0?rt.getColumn(column):null;
		if (c!=null) {
			ImageProcessor ip = new FloatProcessor(c.length, 1, c, null);
			if (ip==null) return line;
			ip.setRoi(start, 0, ip.getWidth()-start, 1);
			ip = ip.crop();
			ImageStatistics stats = new FloatStatistics(ip);
			if (stats==null)
				return line;
			line += n(stats.mean);
		} else
			line += "-\t";
		summaryHdr += "\t"+rt.getColumnHeading(column);
		return line;
	}

	String n(double n) {
		String s;
		if (Math.round(n)==n)
			s = ResultsTable.d2s(n,0);
		else
			s = ResultsTable.d2s(n, Analyzer.getPrecision());
		return "\t"+s;
	}

	boolean eraseOutsideRoi(ImageProcessor ip, Rectangle r, ImageProcessor mask) {
		int width = ip.getWidth();
		int height = ip.getHeight();
		ip.setRoi(r);
		if (excludeEdgeParticles && polygon!=null) {
			ImageStatistics stats = ImageStatistics.getStatistics(ip, MIN_MAX, null);
			if (fillColor>=stats.min && fillColor<=stats.max) {
				double replaceColor = level1-1.0;
				if (replaceColor<0.0 || replaceColor==fillColor) {
					replaceColor = level2+1.0;
					int maxColor = imageType==BYTE?255:65535;
					if (replaceColor>maxColor || replaceColor==fillColor) {
						IJ.error("Particle Analyzer", "Unable to remove edge particles");
						return false;
					}
				}
				for (int y=minY; y<maxY; y++) {
					for (int x=minX; x<maxX; x++) {
						int v  = ip.getPixel(x, y);
						if (v==fillColor) ip.putPixel(x, y, (int)replaceColor);
					}
				}
			}
		}
		ip.setValue(fillColor);		
		if (mask!=null) {
			mask = mask.duplicate();
			mask.invert();
			ip.fill(mask);
		}		
		ip.setRoi(0, 0, r.x, height);
		ip.fill();
		ip.setRoi(r.x, 0, r.width, r.y);
		ip.fill();
		ip.setRoi(r.x, r.y+r.height, r.width, height-(r.y+r.height));
		ip.fill();
		ip.setRoi(r.x+r.width, 0, width-(r.x+r.width), height);
		ip.fill();
		ip.resetRoi();
		//IJ.log("erase: "+fillColor+"	"+level1+"	"+level2+"	"+excludeEdgeParticles);
		//(IJ.getFactory().newImagePlus("ip2", ip.duplicate())).show();
		return true;
	}

	boolean setThresholdLevels(IjxImagePlus imp, ImageProcessor ip) {
		double t1 = ip.getMinThreshold();
		double t2 = ip.getMaxThreshold();
		boolean invertedLut = imp.isInvertedLut();
		boolean byteImage = ip instanceof ByteProcessor;
		if (ip instanceof ShortProcessor)
			imageType = SHORT;
		else if (ip instanceof FloatProcessor)
			imageType = FLOAT;
		else
			imageType = BYTE;
		if (t1==ImageProcessor.NO_THRESHOLD) {
			ImageStatistics stats = imp.getStatistics();
			if (imageType!=BYTE || (stats.histogram[0]+stats.histogram[255]!=stats.pixelCount)) {
				IJ.error("Particle Analyzer",
					"A thresholded image or 8-bit binary image is\n"
					+"required. Threshold levels can be set using\n"
					+"the Image->Adjust->Threshold tool.");
				canceled = true;
				return false;
			}
			if (invertedLut) {
				level1 = 255;
				level2 = 255;
				fillColor = 64;
			} else {
				level1 = 0;
				level2 = 0;
				fillColor = 192;
			}
		} else {
			level1 = t1;
			level2 = t2;
			if (imageType==BYTE) {
				if (level1>0)
					fillColor = 0;
				else if (level2<255)
					fillColor = 255;
			} else if (imageType==SHORT) {
				if (level1>0)
					fillColor = 0;
				else if (level2<65535)
					fillColor = 65535;
			} else if (imageType==FLOAT)
					fillColor = -Float.MAX_VALUE;
			else
				return false;
		}
		imageType2 = imageType;
		if (redirectIP!=null) {
			if (redirectIP instanceof ShortProcessor)
				imageType2 = SHORT;
			else if (redirectIP instanceof FloatProcessor)
				imageType2 = FLOAT;
			else if (redirectIP instanceof ColorProcessor)
				imageType2 = RGB;
			else
				imageType2 = BYTE;
		}
		return true;
	}
	
	int counter = 0;
	
	void analyzeParticle(int x, int y, IjxImagePlus imp, ImageProcessor ip) {
		//Wand wand = new Wand(ip);
		ImageProcessor ip2 = redirectIP!=null?redirectIP:ip;
		wand.autoOutline(x, y, level1, level2, wandMode);
		if (wand.npoints==0)
			{IJ.log("wand error: "+x+" "+y); return;}
		Roi roi = new PolygonRoi(wand.xpoints, wand.ypoints, wand.npoints, roiType);
		Rectangle r = roi.getBounds();
		if (r.width>1 && r.height>1) {
			PolygonRoi proi = (PolygonRoi)roi;
			pf.setPolygon(proi.getXCoordinates(), proi.getYCoordinates(), proi.getNCoordinates());
			ip2.setMask(pf.getMask(r.width, r.height));
			if (floodFill) ff.particleAnalyzerFill(x, y, level1, level2, ip2.getMask(), r);
		}
		ip2.setRoi(r);
		ip.setValue(fillColor);
		ImageStatistics stats = getStatistics(ip2, measurements, calibration);
		boolean include = true;
		if (excludeEdgeParticles) {
			if (r.x==minX||r.y==minY||r.x+r.width==maxX||r.y+r.height==maxY)
				include = false;
			if (polygon!=null) {
				Rectangle bounds = roi.getBounds();
				int x1=bounds.x+wand.xpoints[wand.npoints-1];
				int y1=bounds.y+wand.ypoints[wand.npoints-1];
				int x2, y2;
				for (int i=0; i<wand.npoints; i++) {
					x2=bounds.x+wand.xpoints[i];
					y2=bounds.y+wand.ypoints[i];
					if (!polygon.contains(x2, y2))
						{include = false; break;}
					if ((x1==x2 && ip.getPixel(x1,y1-1)==fillColor) || (y1==y2 && ip.getPixel(x1-1,y1)==fillColor))
						{include = false; break;}
					x1=x2; y1=y2;
				}
			}
		}
		ImageProcessor mask = ip2.getMask();
		if (minCircularity>0.0 || maxCircularity<1.0) {
			double perimeter = roi.getLength();
			double circularity = perimeter==0.0?0.0:4.0*Math.PI*(stats.pixelCount/(perimeter*perimeter));
			if (circularity>1.0) circularity = 1.0;
			//IJ.log(circularity+"	"+perimeter+"  "+stats.area);
			if (circularity<minCircularity || circularity>maxCircularity) include = false;
		}
		if (stats.pixelCount>=minSize && stats.pixelCount<=maxSize && include) {
			particleCount++;
			if (roiNeedsImage)
				roi.setImage(imp);
			stats.xstart=x; stats.ystart=y;
			saveResults(stats, roi);
			if (showChoice!=NOTHING)
				drawParticle(drawIP, roi, stats, mask);
		}
		if (redirectIP!=null)
			ip.setRoi(r);
		ip.fill(mask);
	}

	ImageStatistics getStatistics(ImageProcessor ip, int mOptions, Calibration cal) {
		switch (imageType2) {
			case BYTE:
				return new ByteStatistics(ip, mOptions, cal);
			case SHORT:
				return new ShortStatistics(ip, mOptions, cal);
			case FLOAT:
				return new FloatStatistics(ip, mOptions, cal);
			case RGB:
				return new ColorStatistics(ip, mOptions, cal);
			default:
				return null;
		}
	}

	/** Saves statistics for one particle in a results table. This is
		a method subclasses may want to override. */
	protected void saveResults(ImageStatistics stats, Roi roi) {
		analyzer.saveResults(stats, roi);
		if (recordStarts) {
			rt.addValue("XStart", stats.xstart);
			rt.addValue("YStart", stats.ystart);
		}
		if (addToManager) {
			if (roiManager==null) {
				if (Macro.getOptions()!=null && Interpreter.isBatchMode())
					roiManager = Interpreter.getBatchModeRoiManager();
				if (roiManager==null) {
					IjxWindow frame = WindowManager.getFrame("ROI Manager");
					if (frame==null)
						IJ.run("ROI Manager...");
					frame = WindowManager.getFrame("ROI Manager");
					if (frame==null || !(frame instanceof RoiManager))
						{addToManager=false; return;}
					roiManager = (RoiManager)frame;
				}
				if (resetCounter)
					roiManager.runCommand("reset");
			}
			roiManager.add(imp, roi, rt.getCounter());
		}
		if (showResults)
			rt.addResults();
	}
	
	/** Draws a selected particle in a separate image.	This is
		another method subclasses may want to override. */
	protected void drawParticle(ImageProcessor drawIP, Roi roi,
	ImageStatistics stats, ImageProcessor mask) {
		switch (showChoice) {
			case MASKS: drawFilledParticle(drawIP, roi, mask); break;
			case OUTLINES: case OVERLAY_OUTLINES: case OVERLAY_MASKS:
				drawOutline(drawIP, roi, rt.getCounter()); break;
			case ELLIPSES: drawEllipse(drawIP, stats, rt.getCounter()); break;
			case ROI_MASKS: drawRoiFilledParticle(drawIP, roi, mask, rt.getCounter()); break;
			default:
		}
	}

	void drawFilledParticle(ImageProcessor ip, Roi roi, ImageProcessor mask) {
		//IJ.write(roi.getBounds()+" "+mask.length);
		ip.setRoi(roi.getBounds());
		ip.fill(mask);
	}

	void drawOutline(ImageProcessor ip, Roi roi, int count) {
		if (showChoice==OVERLAY_OUTLINES || showChoice==OVERLAY_MASKS) {
			if (overlay==null) {
				overlay = new Overlay();
				overlay.drawLabels(true);
			}
			roi.setStrokeColor(Color.cyan);
			if (showChoice==OVERLAY_MASKS)
				roi.setFillColor(Color.cyan);
			overlay.add((Roi)roi.clone());
		} else {
			Rectangle r = roi.getBounds();
			int nPoints = ((PolygonRoi)roi).getNCoordinates();
			int[] xp = ((PolygonRoi)roi).getXCoordinates();
			int[] yp = ((PolygonRoi)roi).getYCoordinates();
			int x=r.x, y=r.y;
			if (!inSituShow)
				ip.setValue(0.0);
			ip.moveTo(x+xp[0], y+yp[0]);
			for (int i=1; i<nPoints; i++)
				ip.lineTo(x+xp[i], y+yp[i]);
			ip.lineTo(x+xp[0], y+yp[0]);
			String s = ResultsTable.d2s(count,0);
			ip.moveTo(r.x+r.width/2-ip.getStringWidth(s)/2, r.y+r.height/2+4);
			if (!inSituShow)
				ip.setValue(1.0);
			ip.drawString(s);
		}
	}

	void drawEllipse(ImageProcessor ip, ImageStatistics stats, int count) {
		stats.drawEllipse(ip);
	}

	void drawRoiFilledParticle(ImageProcessor ip, Roi roi, ImageProcessor mask, int count) {
		int grayLevel = (count < 65535) ? count : 65535;
		ip.setValue((double) grayLevel); 
		ip.setRoi(roi.getBounds());
		ip.fill(mask);
	}

	void showResults() {
		int count = rt.getCounter();
		// if (count==0) return;
		boolean lastSlice = !processStack||slice==imp.getStackSize();
		if ((showChoice==OVERLAY_OUTLINES||showChoice==OVERLAY_MASKS) && slice==1 && count>0)
			imp.setOverlay(overlay);
		else if (outlines!=null && lastSlice) {
			String title = imp!=null?imp.getTitle():"Outlines";
			String prefix;
			if (showChoice == MASKS)
				prefix = "Mask of ";
			else if (showChoice == ROI_MASKS)
				prefix = "Count Masks of ";
			else
				prefix = "Drawing of ";
			outlines.update(drawIP);
			outputImage = IJ.getFactory().newImagePlus(prefix+title, outlines);
			if (inSituShow) {
				if (imp.getStackSize()==1)
					Undo.setup(Undo.TRANSFORM, imp);
				imp.setStack(null, outputImage.getStack());
			} else if (!hideOutputImage)
				outputImage.show();
		}
		if (showResults && !processStack) {
			TextPanel tp = IJ.getTextPanel();
			if (beginningCount>0 && tp!=null && tp.getLineCount()!=count)
				rt.show("Results");
			Analyzer.firstParticle = beginningCount;
			Analyzer.lastParticle = Analyzer.getCounter()-1;
		} else
			Analyzer.firstParticle = Analyzer.lastParticle = 0;
	}
	
	/** Returns the "Outlines", "Masks", "Elipses" or "Count Masks" image,
		or null if "Nothing" is selected in the "Show:" menu. */
	public IjxImagePlus getOutputImage() {
		return outputImage;
	}

	/** Set 'hideOutputImage' true to not display the "Show:" image. */
	public void setHideOutputImage(boolean hideOutputImage) {
		this.hideOutputImage = hideOutputImage;
	}

	int getColumnID(String name) {
		int id = rt.getFreeColumn(name);
		if (id==ResultsTable.COLUMN_IN_USE)
			id = rt.getColumnIndex(name);
		return id;
	}

	void makeCustomLut() {
		IndexColorModel cm = (IndexColorModel)LookUpTable.createGrayscaleColorModel(false);
		byte[] reds = new byte[256];
		byte[] greens = new byte[256];
		byte[] blues = new byte[256];
		cm.getReds(reds);
		cm.getGreens(greens);
		cm.getBlues(blues);
		reds[1] =(byte) 255;
		greens[1] = (byte)0;
		blues[1] = (byte)0;
		customLut = new IndexColorModel(8, 256, reds, greens, blues);
	}

	/** Called once when ImageJ quits. */
	public void savePreferences(Properties prefs) {
		prefs.put(OPTIONS, Integer.toString(staticOptions));
	}

}
