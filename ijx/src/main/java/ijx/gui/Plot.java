package ijx.gui;
import ijx.process.ImageProcessor;
import ijx.process.ColorProcessor;
import ijx.process.ByteProcessor;
import imagej.util.Tools;
import ijx.Prefs;
import ijx.WindowManager;
import ijx.IJ;
import java.awt.*;
import java.util.*;

import ijx.macro.Interpreter;
import ijx.measure.Calibration;
import ijx.IjxImagePlus;


/** This class is an image that line graphs can be drawn on. */
public class Plot {

	/** Display points using a circle 5 pixels in diameter. */
	public static final int CIRCLE = 0;
	/** Display points using an X-shaped mark. */
	public static final int X = 1;
	/** Display points using an box-shaped mark. */
	public static final int BOX = 3;
	/** Display points using an tiangular mark. */
	public static final int TRIANGLE = 4;
	/** Display points using an cross-shaped mark. */
	public static final int CROSS = 5;
	/** Display points using a single pixel. */
	public static final int DOT = 6;
	/** Connect points with solid lines. */
	public static final int LINE = 2;
	///** flag multiplier for maximum number of ticks&grid lines along x */
	//public static final int X_INTERVALS_M = 0x1;
	///** flag multiplier for maximum number of ticks&grid lines along y */
	//public static final int Y_INTERVALS_M = 0x100;
	/** flag for numeric labels of x-axis ticks */
	public static final int X_NUMBERS = 0x1;
	/** flag for numeric labels of x-axis ticks */
	public static final int Y_NUMBERS = 0x2;
	/** flag for drawing x-axis ticks */
	public static final int X_TICKS = 0x4;
	/** flag for drawing x-axis ticks */
	public static final int Y_TICKS = 0x8;
	/** flag for drawing vertical grid lines for x-axis ticks */
	public static final int X_GRID = 0x10;
	/** flag for drawing horizontal grid lines for y-axis ticks */
	public static final int Y_GRID = 0x20;
	/** flag for forcing frame to coincide with the grid/ticks in x direction (results in unused space) */
	public static final int X_FORCE2GRID = 0x40;
	/** flag for forcing frame to coincide with the grid/ticks in y direction (results in unused space) */
	public static final int Y_FORCE2GRID = 0x80;
	/** the default flags */
	public static final int DEFAULT_FLAGS =	 X_NUMBERS + Y_NUMBERS + /*X_TICKS + Y_TICKS +*/ X_GRID + Y_GRID; 
	/** the margin width left of the plot frame (enough for 5-digit numbers such as unscaled 16-bit data*/
	public static final int LEFT_MARGIN = 60;
	/** the margin width right of the plot frame */
	public static final int RIGHT_MARGIN = 18;
	/** the margin width above the plot frame */
	public static final int TOP_MARGIN = 15;
	/** the margin width below the plot frame */
	public static final int BOTTOM_MARGIN = 40;

	private static final int WIDTH = 450;
	private static final int HEIGHT = 200;
	private static final int MAX_INTERVALS = 12;	//maximum number of intervals between ticks or grid lines
	private static final int MIN_X_GRIDWIDTH = 60;	//minimum distance between grid lines or ticks along x
	private static final int MIN_Y_GRIDWIDTH = 40;	//minimum distance between grid lines or ticks along y
	private static final int TICK_LENGTH = 3;		//length of ticks
	private final Color gridColor = new Color(0xc0c0c0); //light gray
	private int frameWidth;
	private int frameHeight;
	
	Rectangle frame = null;
	float[] xValues, yValues;
	float[] errorBars;
	int nPoints;
	double xMin, xMax, yMin, yMax;
	
	private double xScale, yScale;
	private static String defaultDirectory = null;
	private String xLabel;
	private String yLabel;
	private int flags;
	private Font font = new Font("Helvetica", Font.PLAIN, 12);
	private boolean fixedYScale;
	private int lineWidth = 1; // Line.getWidth();
	private int markSize = 5;
	private ImageProcessor ip;
	private String title;
	private boolean initialized;
	private boolean plotDrawn;
	private int plotWidth = PlotWindow.plotWidth;
	private int plotHeight = PlotWindow.plotHeight;
	private boolean multiplePlots;
	private boolean drawPending;
	
	/** keeps a reference to all of the data that is going to be plotted*/
	ArrayList storedData;
	
	/** Construct a new PlotWindow.
	 * @param title the window title
	 * @param xLabel	the x-axis label
	 * @param yLabel	the y-axis label
	 * @param xValues	the x-coodinates, or null
	 * @param yValues	the y-coodinates, or null
	 * @param flags		sum of flag values controlling appearance of ticks, grid, etc.
	 */
	public Plot(String title, String xLabel, String yLabel, float[] xValues, float[] yValues, int flags) {
		this.title = title;
		this.xLabel = xLabel;
		this.yLabel = yLabel;
		this.flags = flags;
		storedData = new ArrayList();
		if (xValues==null || yValues==null) {
			xValues = new float[1];
			yValues = new float[1];
			xValues[0] = -1f;
			yValues[0] = -1f;
		} else
			storeData(xValues, yValues);
		this.xValues = xValues;
		this.yValues = yValues;
		
		
		double[] a = Tools.getMinMax(xValues);
		xMin=a[0]; xMax=a[1];
		a = Tools.getMinMax(yValues);
		yMin=a[0]; yMax=a[1];
		fixedYScale = false;
		nPoints = xValues.length;
		drawPending = true;
	}
	
	/** This version of the constructor uses the default flags. */
	public Plot(String title, String xLabel, String yLabel, float[] xValues, float[] yValues) {
		this(title, xLabel, yLabel, xValues, yValues, DEFAULT_FLAGS);
	}

	/** This version of the constructor accepts double arrays. */
	public Plot(String title, String xLabel, String yLabel, double[] xValues, double[] yValues, int flags) {
		this(title, xLabel, yLabel, xValues!=null?Tools.toFloat(xValues):null, yValues!=null?Tools.toFloat(yValues):null, flags);
	}

	/** This version of the constructor accepts double arrays and uses the default flags */
	public Plot(String title, String xLabel, String yLabel, double[] xValues, double[] yValues) {
		this(title, xLabel, yLabel, xValues!=null?Tools.toFloat(xValues):null, yValues!=null?Tools.toFloat(yValues):null, DEFAULT_FLAGS);
	}

	/** Sets the x-axis and y-axis range. */
	public void setLimits(double xMin, double xMax, double yMin, double yMax) {
		this.xMin = xMin;
		this.xMax = xMax;
		this.yMin = yMin;
		this.yMax = yMax;
		fixedYScale = true;
		if (initialized) {
			ip.setColor(Color.white);
			ip.resetRoi();
			ip.fill();
			ip.setColor(Color.black);
			setScaleAndDrawAxisLabels();
		}
	}

	/** Sets the canvas size (i.e., size of the resulting ImageProcessor).
	 * By default, the size is adjusted for the plot frame size specified
	 * in Edit>Options>Profile Plot Options*/
	public void setSize(int width, int height) {
		if (!initialized && width>LEFT_MARGIN+RIGHT_MARGIN+20 && height>TOP_MARGIN+BOTTOM_MARGIN+20) {
			plotWidth = width-LEFT_MARGIN-RIGHT_MARGIN;
			plotHeight = height-TOP_MARGIN-BOTTOM_MARGIN;
		}
	}

	/** Adds a set of points to the plot or adds a curve if shape is set to LINE.
	 * @param x			the x-coodinates
	 * @param y			the y-coodinates
	 * @param shape		CIRCLE, X, BOX, TRIANGLE, CROSS, DOT or LINE
	 */
	public void addPoints(float[] x, float[] y, int shape) {
		setup();
		switch(shape) {
			case CIRCLE: case X:  case BOX: case TRIANGLE: case CROSS: case DOT:
				ip.setClipRect(frame);
				for (int i=0; i<x.length; i++) {
					int xt = LEFT_MARGIN + (int)((x[i]-xMin)*xScale);
					int yt = TOP_MARGIN + frameHeight - (int)((y[i]-yMin)*yScale);
					drawShape(shape, xt, yt, markSize);
				}
			   ip.setClipRect(null);
				break;
			case LINE:
				int xts[] = new int[x.length];
				int yts[] = new int[y.length];
				for (int i=0; i<x.length; i++) {
					xts[i] = LEFT_MARGIN + (int)((x[i]-xMin)*xScale);
					yts[i] = TOP_MARGIN + frameHeight - (int)((y[i]-yMin)*yScale);
				}
				drawPolyline(ip, xts, yts, x.length, true);
				break;
		}
		multiplePlots = true;
		if (xValues.length==1) {
			xValues = x;
			yValues = y;
			nPoints = x.length;
			drawPending = false;
		}
		storeData(x, y);
	}
	
	/** Adds a set of points to the plot using double arrays.
	 * Must be called before the plot is displayed. */
	public void addPoints(double[] x, double[] y, int shape) {
		addPoints(Tools.toFloat(x), Tools.toFloat(y), shape);
	}
	
	void drawShape(int shape, int x, int y, int size) {
		int xbase = x-size/2;
		int ybase = y-size/2;
		switch(shape) {
			case X:
				ip.drawLine(xbase,ybase,xbase+size,ybase+size);
				ip.drawLine(xbase+size,ybase,xbase,ybase+size);
				break;
			case BOX:
				ip.drawLine(xbase,ybase,xbase+size,ybase);
				ip.drawLine(xbase+size,ybase,xbase+size,ybase+size);
				ip.drawLine(xbase+size,ybase+size,xbase,ybase+size);
				ip.drawLine(xbase,ybase+size,xbase,ybase);
				break;
			case TRIANGLE:
				ip.drawLine(x,ybase,xbase+size,ybase+size);
				ip.drawLine(xbase+size,ybase+size,xbase,ybase+size);
				ip.drawLine(xbase,ybase+size,x,ybase);
				break;
			case CROSS:
				ip.drawLine(xbase,y,xbase+size,y);
				ip.drawLine(x,ybase,x,ybase+size);
				break;
			case DOT:
				ip.drawDot(x, y);
				break;
			default: // 5x5 oval
				ip.drawLine(x-1, y-2, x+1, y-2);
				ip.drawLine(x-1, y+2, x+1, y+2);
				ip.drawLine(x+2, y+1, x+2, y-1);
				ip.drawLine(x-2, y+1, x-2, y-1);
				break;
		}
	}
	
	/** Adds error bars to the plot. */
	public void addErrorBars(float[] errorBars) {
		if (errorBars.length!=nPoints)
			throw new IllegalArgumentException("errorBars.length != npoints");
		this.errorBars = errorBars	;
	}
	
	/** Adds error bars to the plot. */
	public void addErrorBars(double[] errorBars) {
		addErrorBars(Tools.toFloat(errorBars));
	}
	
	/** Draws text at the specified location, where (0,0)
	 * is the upper left corner of the the plot frame and (1,1) is
	 * the lower right corner. */
	public void addLabel(double x, double y, String label) {
		setup();
		int xt = LEFT_MARGIN + (int)(x*frameWidth);
		int yt = TOP_MARGIN + (int)(y*frameHeight);
		ip.drawString(label, xt, yt);
	}
	
		/* Draws text at the specified location, using the coordinate system defined
				by setLimits() and the justification specified by setJustification(). */
	//	public void addText(String text, double x, double y) {
	//		setup();
	//		int xt = LEFT_MARGIN + (int)((x-xMin)*xScale);
	//		int yt = TOP_MARGIN + frameHeight - (int)((y-yMin)*yScale);
	//		if (justification==CENTER)
	//			xt -= ip.getStringWidth(text)/2;
	//		else if (justification==RIGHT)
	//			xt -= ip.getStringWidth(text);
	//		ip.drawString(text, xt, yt);
	//	}
	
	/** Sets the justification used by addLabel(), where <code>justification</code>
	 * is ImageProcessor.LEFT, ImageProcessor.CENTER or ImageProcessor.RIGHT. */
	public void setJustification(int justification) {
		setup();
		ip.setJustification(justification);
	}
	
	/** Changes the drawing color. For selecting the color of the data passed with the constructor,
	 * use <code>setColor</code> before <code>draw</code>.
	 * The frame and labels are always drawn in black. */
	public void setColor(Color c) {
		setup();
		if (!(ip instanceof ColorProcessor)) {
			ip = ip.convertToRGB();
			ip.setLineWidth(lineWidth);
			ip.setFont(font);
			ip.setAntialiasedText(true);
		}
		ip.setColor(c);
	}
	
	/** Changes the line width. */
	public void setLineWidth(int lineWidth) {
		if (lineWidth<1) lineWidth = 1;
		setup();
		ip.setLineWidth(lineWidth);
		this.lineWidth = lineWidth;
		markSize = lineWidth==1?5:7;
	}
	
	/* Draws a line using the coordinate system defined by setLimits(). */
	public void drawLine(double x1, double y1, double x2, double y2) {
		setup();
		int ix1 = LEFT_MARGIN + (int)Math.round((x1-xMin)*xScale);
		int iy1 = TOP_MARGIN + frameHeight - (int)Math.round((y1-yMin)*yScale);
		int ix2 = LEFT_MARGIN + (int)Math.round((x2-xMin)*xScale);
		int iy2 = TOP_MARGIN + frameHeight - (int)Math.round((y2-yMin)*yScale);
		ip.drawLine(ix1, iy1, ix2, iy2);
	}

	/** Changes the font. */
	public void changeFont(Font font) {
		setup();
		ip.setFont(font);
		this.font = font;
	}
	
	void setup() {
		if (initialized)
			return;
		initialized = true;
		createImage();
		ip.setColor(Color.black);
		if (lineWidth>3)
			lineWidth = 3;
		ip.setLineWidth(lineWidth);
		ip.setFont(font);
		ip.setAntialiasedText(true);
		if (frameWidth==0) {
			frameWidth = plotWidth;
			frameHeight = plotHeight;
		}
		frame = new Rectangle(LEFT_MARGIN, TOP_MARGIN, frameWidth, frameHeight);
		setScaleAndDrawAxisLabels();
	}
	
	void setScaleAndDrawAxisLabels() {
		if ((xMax-xMin)==0.0)
			xScale = 1.0;
		else
			xScale = frame.width/(xMax-xMin);
		if ((yMax-yMin)==0.0)
			yScale = 1.0;
		else
			yScale = frame.height/(yMax-yMin);
		if (PlotWindow.noGridLines)
			drawAxisLabels();
		else
			drawTicksEtc();
	}
	
	void drawAxisLabels() {
		int digits = getDigits(yMin, yMax);
		String s = IJ.d2s(yMax, digits);
		int sw = ip.getStringWidth(s);
		if ((sw+4)>LEFT_MARGIN)
			ip.drawString(s, 4, TOP_MARGIN-4);
		else
			ip.drawString(s, LEFT_MARGIN-ip.getStringWidth(s)-4, TOP_MARGIN+10);
		s = IJ.d2s(yMin, digits);
		sw = ip.getStringWidth(s);
		if ((sw+4)>LEFT_MARGIN)
			ip.drawString(s, 4, TOP_MARGIN+frame.height);
		else
			ip.drawString(s, LEFT_MARGIN-ip.getStringWidth(s)-4, TOP_MARGIN+frame.height);
		FontMetrics fm = ip.getFontMetrics();
		int x = LEFT_MARGIN;
		int y = TOP_MARGIN + frame.height + fm.getAscent() + 6;
		digits = getDigits(xMin, xMax);
		ip.drawString(IJ.d2s(xMin,digits), x, y);
		s = IJ.d2s(xMax,digits);
		ip.drawString(s, x + frame.width-ip.getStringWidth(s)+6, y);
		ip.drawString(xLabel, LEFT_MARGIN+(frame.width-ip.getStringWidth(xLabel))/2, y+3);
		drawYLabel(yLabel,LEFT_MARGIN-4,TOP_MARGIN,frame.height, fm);
	}
	
	void drawTicksEtc() {
		int fontAscent = ip.getFontMetrics().getAscent();
		int fontMaxAscent = ip.getFontMetrics().getMaxAscent();
		if ((flags&(X_NUMBERS + X_TICKS + X_GRID)) != 0) {
			double step = Math.abs(Math.max(frame.width/MAX_INTERVALS, MIN_X_GRIDWIDTH)/xScale); //the smallest allowable increment
			step = niceNumber(step);
			int i1, i2;
			if ((flags&X_FORCE2GRID) != 0) {
				i1 = (int)Math.floor(Math.min(xMin,xMax)/step+1.e-10);	//this also allows for inverted xMin, xMax
				i2 = (int)Math.ceil(Math.max(xMin,xMax)/step-1.e-10);
				xMin = i1*step;
				xMax = i2*step;
				xScale = frame.width/(xMax-xMin);						//rescale to make it fit
			} else {
				i1 = (int)Math.ceil(Math.min(xMin,xMax)/step-1.e-10);
				i2 = (int)Math.floor(Math.max(xMin,xMax)/step+1.e-10);
			}
			int digits = -(int)Math.floor(Math.log(step)/Math.log(10)+1e-6);
			if (digits < 0) digits = 0;
			if (digits>5) digits = -3; // use scientific notation
			int y1 = TOP_MARGIN;
			int y2 = TOP_MARGIN+frame.height;
			int yNumbers = y2 + fontAscent + 7;
			for (int i=0; i<=(i2-i1); i++) {
				double v = (i+i1)*step;
				int x = (int)Math.round((v - xMin)*xScale) + LEFT_MARGIN;
				if ((flags&X_GRID) != 0) {
					ip.setColor(gridColor);
					ip.drawLine(x, y1, x, y2);
					ip.setColor(Color.black);
				}
				if ((flags&X_TICKS) !=0) {
					ip.drawLine(x, y1, x, y1+TICK_LENGTH);
					ip.drawLine(x, y2, x, y2-TICK_LENGTH);
				}
				if ((flags&X_NUMBERS) != 0) {
					String s = IJ.d2s(v,digits);
					ip.drawString(s, x-ip.getStringWidth(s)/2, yNumbers);
				}
			}
		}
		int maxNumWidth = 0;
		if ((flags&(Y_NUMBERS + Y_TICKS + Y_GRID)) != 0) {
			double step = Math.abs(Math.max(frame.width/MAX_INTERVALS, MIN_Y_GRIDWIDTH)/yScale); //the smallest allowable increment
			step = niceNumber(step);
			int i1, i2;
			if ((flags&X_FORCE2GRID) != 0) {
				i1 = (int)Math.floor(Math.min(yMin,yMax)/step+1.e-10);	//this also allows for inverted xMin, xMax
				i2 = (int)Math.ceil(Math.max(yMin,yMax)/step-1.e-10);
				yMin = i1*step;
				yMax = i2*step;
				yScale = frame.height/(yMax-yMin);						//rescale to make it fit
			} else {
				i1 = (int)Math.ceil(Math.min(yMin,yMax)/step-1.e-10);
				i2 = (int)Math.floor(Math.max(yMin,yMax)/step+1.e-10);
			}
			int digits = -(int)Math.floor(Math.log(step)/Math.log(10)+1e-6);
			if (digits < 0) digits = 0;
			if (digits>5) digits = -3; // use scientific notation
			int x1 = LEFT_MARGIN;
			int x2 = LEFT_MARGIN+frame.width;
			for (int i=0; i<=(i2-i1); i++) {
				double v = (i+i1)*step;
				int y = TOP_MARGIN + frame.height - (int)Math.round((v - yMin)*yScale);
				if ((flags&Y_GRID) != 0) {
					ip.setColor(gridColor);
					ip.drawLine(x1, y, x2, y);
					ip.setColor(Color.black);
				}
				if ((flags&Y_TICKS) !=0) {
					ip.drawLine(x1, y, x1+TICK_LENGTH, y);
					ip.drawLine(x2, y, x2-TICK_LENGTH, y);
				}
				if ((flags&Y_NUMBERS) != 0) {
					String s = IJ.d2s(v,digits);
					int w = ip.getStringWidth(s);
					if (w>maxNumWidth) maxNumWidth = w;
					ip.drawString(s, LEFT_MARGIN-w-4, y+fontMaxAscent/2+1);
				}
			}
		}
		if ((flags&Y_NUMBERS)==0) {					//simply note y-axis min&max
			int digits = getDigits(yMin, yMax);
			String s = IJ.d2s(yMax, digits);
			int sw = ip.getStringWidth(s);
			if ((sw+4)>LEFT_MARGIN)
				ip.drawString(s, 4, TOP_MARGIN-4);
			else
				ip.drawString(s, LEFT_MARGIN-ip.getStringWidth(s)-4, TOP_MARGIN+10);
			s = IJ.d2s(yMin, digits);
			sw = ip.getStringWidth(s);
			if ((sw+4)>LEFT_MARGIN)
				ip.drawString(s, 4, TOP_MARGIN+frame.height);
			else
				ip.drawString(s, LEFT_MARGIN-ip.getStringWidth(s)-4, TOP_MARGIN+frame.height);
		}
		FontMetrics fm = ip.getFontMetrics();
		int x = LEFT_MARGIN;
		int y = TOP_MARGIN + frame.height + fm.getAscent() + 6;
		if ((flags&X_NUMBERS)==0) {					//simply note x-axis min&max
			int digits = getDigits(xMin, xMax);
			ip.drawString(IJ.d2s(xMin,digits), x, y);
			String s = IJ.d2s(xMax,digits);
			ip.drawString(s, x + frame.width-ip.getStringWidth(s)+6, y);
		} else
			y += fm.getAscent();					//space needed for x numbers
		ip.drawString(xLabel, LEFT_MARGIN+(frame.width-ip.getStringWidth(xLabel))/2, y+6);
		drawYLabel(yLabel,LEFT_MARGIN-maxNumWidth-4,TOP_MARGIN,frame.height, fm);
	}
	
	double niceNumber(double v) {	//the smallest "nice" number >= v. "Nice" numbers are .. 0.5, 1, 2, 5, 10, 20 ...
		double base = Math.pow(10,Math.floor(Math.log(v)/Math.log(10)-1.e-6));
		if (v > 5.0000001*base) return 10*base;
		else if (v > 2.0000001*base) return 5*base;
		else return 2*base;
	}

	void createImage() {
		if (ip!=null) return;
	   int width = plotWidth+LEFT_MARGIN+RIGHT_MARGIN;
		int height = plotHeight+TOP_MARGIN+BOTTOM_MARGIN;
		byte[] pixels = new byte[width*height];
		for (int i=0; i<width*height; i++)
			pixels[i] = (byte)255;
		ip = new ByteProcessor(width, height, pixels, null);
	}
	
	int getDigits(double n1, double n2) {
		if (Math.round(n1)==n1 && Math.round(n2)==n2)
			return 0;
		else {
			n1 = Math.abs(n1);
			n2 = Math.abs(n2);
			double n = n1<n2&&n1>0.0?n1:n2;
			double diff = Math.abs(n2-n1);
			if (diff>0.0 && diff<n) n = diff;
			int digits = 1;
			if (n<10.0) digits = 2;
			if (n<0.01) digits = 3;
			if (n<0.001) digits = 4;
			if (n<0.0001) digits = -3; // use scientific notation
			return digits;
		}
	}
	
	/** Draws the plot specified in the constructor. */
	public void draw() {
		int x, y;
		double v;
		
		if (plotDrawn)
			return;
		plotDrawn = true;
		createImage();
		setup();
		
		if (drawPending) {
			int xpoints[] = new int[nPoints];
			int ypoints[] = new int[nPoints];
			for (int i=0; i<nPoints; i++) {
				xpoints[i] = LEFT_MARGIN + (int)((xValues[i]-xMin)*xScale);
				ypoints[i] = TOP_MARGIN + frame.height - (int)((yValues[i]-yMin)*yScale);
			}
			drawPolyline(ip, xpoints, ypoints, nPoints, true);
			if (this.errorBars != null) {
				xpoints = new int[2];
				ypoints = new int[2];
				for (int i=0; i<nPoints; i++) {
					xpoints[0] = xpoints[1] = LEFT_MARGIN + (int)((xValues[i]-xMin)*xScale);
					ypoints[0] = TOP_MARGIN + frame.height - (int)((yValues[i]-yMin-errorBars[i])*yScale);
					ypoints[1] = TOP_MARGIN + frame.height - (int)((yValues[i]-yMin+errorBars[i])*yScale);
					drawPolyline(ip, xpoints,ypoints, 2, false);
				}
			}
		}
		
		if (ip instanceof ColorProcessor)
			ip.setColor(Color.black);
		if (lineWidth>5) ip.setLineWidth(5);
		ip.drawRect(frame.x, frame.y, frame.width+1, frame.height+1);
		ip.setLineWidth(lineWidth);
	}
	
	void drawPolyline(ImageProcessor ip, int[] x, int[] y, int n, boolean clip) {
		if (clip) ip.setClipRect(frame);
		ip.moveTo(x[0], y[0]);
		for (int i=0; i<n; i++)
			ip.lineTo(x[i], y[i]);
		if (clip) ip.setClipRect(null);
	}
	
	void drawYLabel(String yLabel, int x, int y, int height, FontMetrics fm) {
		if (yLabel.equals(""))
			return;
		int w =	 fm.stringWidth(yLabel) + 5;
		int h =	 fm.getHeight() + 5;
		ImageProcessor label = new ByteProcessor(w, h);
		label.setColor(Color.white);
		label.fill();
		label.setColor(Color.black);
		label.setFont(font);
		label.setAntialiasedText(true);
		int descent = fm.getDescent();
		label.drawString(yLabel, 0, h-descent);
		label = label.rotateLeft();
		int y2 = y+(height-ip.getStringWidth(yLabel))/2;
		if (y2<y) y2 = y;
		int x2 = Math.max(x-h, 0);
		ip.insert(label, x2, y2);
	}
	
	ImageProcessor getBlankProcessor() {
		createImage();
		return ip;
	}
	
	String getCoordinates(int x, int y) {
		String text = "";
		if (!frame.contains(x, y))
			return text;
		if (fixedYScale || multiplePlots) { // display cursor location
			double xv = (x-LEFT_MARGIN)/xScale + xMin;
			double yv = (TOP_MARGIN+frameHeight-y)/yScale +yMin;
			text =	"X=" + IJ.d2s(xv,getDigits(xv,xv))+", Y=" + IJ.d2s(yv,getDigits(yv,yv));
		} else { // display x and f(x)
			int index = (int)((x-frame.x)/((double)frame.width/nPoints));
			if (index>0 && index<nPoints) {
				double xv = xValues[index];
				double yv = yValues[index];
				text = "X=" + IJ.d2s(xv,getDigits(xv,xv))+", Y=" + IJ.d2s(yv,getDigits(yv,yv));
			}
		}
		return text;
	}
	
	/** Returns the plot as an ImageProcessor. */
	public ImageProcessor getProcessor() {
		draw();
		return ip;
	}
	
	/** Returns the plot as an IjxImagePlus. */
	public IjxImagePlus getImagePlus() {
		draw();
		IjxImagePlus img = IJ.getFactory().newImagePlus(title, ip);
		Calibration cal = img.getCalibration();
		cal.xOrigin = LEFT_MARGIN;
		cal.yOrigin = TOP_MARGIN+frameHeight+yMin*yScale;
		cal.pixelWidth = 1.0/xScale;
		cal.pixelHeight = 1.0/yScale;
		cal.setInvertY(true);
		return img;
	}

	/** Displays the plot in a PlotWindow and returns a reference to the PlotWindow. */
	public PlotWindow show() {
		 draw();
		 if (Prefs.useInvertingLut && (ip instanceof ByteProcessor) && !Interpreter.isBatchMode() && IJ.getInstance()!=null) {
			ip.invertLut();
			ip.invert();
		}
		if ((IJ.macroRunning() && IJ.getInstance()==null) || Interpreter.isBatchMode()) {
			IjxImagePlus imp = IJ.getFactory().newImagePlus(title, ip);
			WindowManager.setTempCurrentImage(imp);
			imp.setProperty("XValues", xValues); //Allows values to be retrieved by 
			imp.setProperty("YValues", yValues); // by Plot.getValues() macro function
			Interpreter.addBatchModeImage(imp);
			return null;
		}
		WindowManager.setCenterNextImage(true);
		return new PlotWindow(this);
	}
	
	/** Stores plot data into an ArrayList  to be used 
	     when a plot window  wants to 'createlist'. */
	private void storeData(float[] xvalues, float[] yvalues){
		storedData.add(xvalues);
		storedData.add(yvalues);
	}

}


