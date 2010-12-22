package ij.process;

// BDZ - DELETED CODE
import java.awt.*;
import java.awt.image.*;
// BDZ - BEGIN ADDITIONS
import ij.measure.Calibration;
// BDZ - END ADDITIONS
import ij.process.ImageStatistics;
import ij.gui.*;
import ij.util.*;
import ij.plugin.filter.GaussianBlur;
import ij.process.AutoThresholder.Method;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.Prefs;

/**
This abstract class is the superclass for classes that process
the four data types (byte, short, float and RGB) supported by ImageJ.
// BDZ - BEGIN CHANGES
An ImageProcessor contains the pixel data of a 2D image and
// BDZ - END CHANGES
some basic methods to manipulate it.
@see ByteProcessor
@see ShortProcessor
@see FloatProcessor
@see ColorProcessor
@see ij.ImagePlus
@see ij.ImageStack
*/
public abstract class ImageProcessor extends Object {

	/** Value of pixels included in masks. */
	public static final int BLACK = 0xFF000000;
	
	/** Value returned by getMinThreshold() when thresholding is not enabled. */
	public static final double NO_THRESHOLD = -808080.0;
		
	/** Left justify text. */
	public static final int LEFT_JUSTIFY = 0;
	/** Center justify text. */
	public static final int CENTER_JUSTIFY = 1;
	/** Right justify text. */
	public static final int RIGHT_JUSTIFY = 2;
	
	/** Isodata thresholding method */
	public static final int ISODATA = 0;

	/** Modified isodata method used in Image/Adjust/Threshold tool */
	public static final int ISODATA2 = 1;
	
	/** Interpolation methods */
	public static final int NEAREST_NEIGHBOR=0, NONE=0, BILINEAR=1, BICUBIC=2;

	public static final int BLUR_MORE=0, FIND_EDGES=1, MEDIAN_FILTER=2, MIN=3, MAX=4, CONVOLVE=5;
	static public final int RED_LUT=0, BLACK_AND_WHITE_LUT=1, NO_LUT_UPDATE=2, OVER_UNDER_LUT=3;
	static final int INVERT=0, FILL=1, ADD=2, MULT=3, AND=4, OR=5,
		XOR=6, GAMMA=7, LOG=8, MINIMUM=9, MAXIMUM=10, SQR=11, SQRT=12, EXP=13, ABS=14;
	static final String WRONG_LENGTH = "width*height!=pixels.length";
	
	int fgColor = 0;
	protected int lineWidth = 1;
	protected int cx, cy; //current drawing coordinates
	protected Font font;
	protected FontMetrics fontMetrics;
	protected boolean antialiasedText;
	protected boolean boldFont;
	private static String[] interpolationMethods;
	// Over/Under tresholding colors
	private static int overRed, overGreen=255, overBlue;
	private static int underRed, underGreen, underBlue=255;
	private static boolean useBicubic;
	private int sliceNumber;
		
    ProgressBar progressBar;
	protected int width, snapshotWidth;
	protected int height, snapshotHeight;
	protected int roiX, roiY, roiWidth, roiHeight;
	protected int xMin, xMax, yMin, yMax;
	boolean snapshotCopyMode;
	ImageProcessor mask;
	protected ColorModel baseCM; // base color model
	protected ColorModel cm;
	protected byte[] rLUT1, gLUT1, bLUT1; // base LUT
	protected byte[] rLUT2, gLUT2, bLUT2; // LUT as modified by setMinAndMax and setThreshold
	protected boolean interpolate;  // replaced by interpolationMethod
	protected int interpolationMethod = NONE;
	protected double minThreshold=NO_THRESHOLD, maxThreshold=NO_THRESHOLD;
	protected int histogramSize = 256;
	protected double histogramMin, histogramMax;
	protected float[] cTable;
	protected boolean lutAnimation;
	protected MemoryImageSource source;
	protected Image img;
	protected boolean newPixels;
	protected Color drawingColor = Color.black;
	protected int clipXMin, clipXMax, clipYMin, clipYMax; // clip rect used by drawTo, drawLine, drawDot and drawPixel 
	protected int justification = LEFT_JUSTIFY;
	protected int lutUpdateMode;
	protected WritableRaster raster;
	protected BufferedImage image;
	protected BufferedImage fmImage;
	protected ColorModel cm2;
	protected SampleModel sampleModel;
	protected static IndexColorModel defaultColorModel;
	protected boolean minMaxSet;
		
// BDZ - BEGIN CHANGES
	public void showProgress(double percentDone) {
// BDZ - END CHANGES
		if (progressBar!=null)
        	progressBar.show(percentDone);
	}

	/** @deprecated */
	protected void hideProgress() {
		showProgress(1.0);
	}
		
	/** Returns the width of this image in pixels. */
	public int getWidth() {
		return width;
	}
	
	/** Returns the height of this image in pixels. */
	public int getHeight() {
		return height;
	}
	
	/** Returns this processor's color model. For non-RGB processors,
 		this is the base lookup table (LUT), not the one that may have
		been modified by setMinAndMax() or setThreshold(). */
	public ColorModel getColorModel() {
		if (cm==null)
			makeDefaultColorModel();
		if (baseCM!=null)
			return baseCM;
		else
			return cm;
	}

	/** Returns the current color model, which may have
		been modified by setMinAndMax() or setThreshold(). */
	public ColorModel getCurrentColorModel() {
		if (cm==null) makeDefaultColorModel();
		return cm;
	}

	/** Sets the color model. Must be an IndexColorModel (aka LUT)
		for all processors except the ColorProcessor. */
	public void setColorModel(ColorModel cm) {
		if (!(this instanceof ColorProcessor) && !(cm instanceof IndexColorModel))
			throw new IllegalArgumentException("Must be IndexColorModel");
		this.cm = cm;
		baseCM = null;
		rLUT1 = rLUT2 = null;
		newPixels = true;
		inversionTested = false;
		minThreshold = NO_THRESHOLD;
		source = null;
	}

	protected void makeDefaultColorModel() {
		cm = getDefaultColorModel();
	}

	/** Inverts the values in this image's LUT (indexed color model).
		Does nothing if this is a ColorProcessor. */
	public void invertLut() {
		if (cm==null)
			makeDefaultColorModel();
    	IndexColorModel icm = (IndexColorModel)cm;
		int mapSize = icm.getMapSize();
		byte[] reds = new byte[mapSize];
		byte[] greens = new byte[mapSize];
		byte[] blues = new byte[mapSize];	
		byte[] reds2 = new byte[mapSize];
		byte[] greens2 = new byte[mapSize];
		byte[] blues2 = new byte[mapSize];	
		icm.getReds(reds); 
		icm.getGreens(greens); 
		icm.getBlues(blues);
		for (int i=0; i<mapSize; i++) {
			reds2[i] = (byte)(reds[mapSize-i-1]&255);
			greens2[i] = (byte)(greens[mapSize-i-1]&255);
			blues2[i] = (byte)(blues[mapSize-i-1]&255);
		}
		ColorModel cm = new IndexColorModel(8, mapSize, reds2, greens2, blues2); 
		setColorModel(cm);
	}

	/** Returns the LUT index that's the best match for this color. */
	public int getBestIndex(Color c) {
    	IndexColorModel icm;
		if (cm==null)
			makeDefaultColorModel();
		if (minThreshold!=NO_THRESHOLD) {
			double saveMin = getMinThreshold(); 
			double saveMax = getMaxThreshold();
			resetThreshold();
			icm = (IndexColorModel)cm;
			setThreshold(saveMin, saveMax, lutUpdateMode);
		} else
    		icm = (IndexColorModel)cm;
		int mapSize = icm.getMapSize();
		byte[] rLUT = new byte[mapSize];
    	byte[] gLUT = new byte[mapSize];
		byte[] bLUT = new byte[mapSize];
    	icm.getReds(rLUT); 
    	icm.getGreens(gLUT); 
    	icm.getBlues(bLUT); 
		int minDistance = Integer.MAX_VALUE;
		int distance;
		int minIndex = 0;
		int r1=c.getRed();
		int g1=c.getGreen();
		int b1=c.getBlue();
		int r2,b2,g2;
    	for (int i=0; i<mapSize; i++) {
			r2 = rLUT[i]&0xff; g2 = gLUT[i]&0xff; b2 = bLUT[i]&0xff;
    		distance = (r2-r1)*(r2-r1)+(g2-g1)*(g2-g1)+(b2-b1)*(b2-b1);
			//ij.IJ.write(i+" "+minIndex+" "+distance+" "+(rLUT[i]&255));
    		if (distance<minDistance) {
    			minDistance = distance;
    			minIndex = i;
    		}
    		if (minDistance==0.0)
    			break;
    	}
    	return minIndex;
	}

	protected boolean inversionTested = false;
	protected boolean invertedLut;
	
	/** Returns true if this image uses an inverting LUT
		that displays zero as white and 255 as black. */
	public boolean isInvertedLut() {
		if (inversionTested)
			return invertedLut;
		inversionTested = true;
		if (cm==null || !(cm instanceof IndexColorModel))
			return (invertedLut=false);
		IndexColorModel icm = (IndexColorModel)cm;
		invertedLut = true;
		int v1, v2;
		for (int i=1; i<255; i++) {
			v1 = icm.getRed(i-1)+icm.getGreen(i-1)+icm.getBlue(i-1);
			v2 = icm.getRed(i)+icm.getGreen(i)+icm.getBlue(i);
			if (v1<v2) {
				invertedLut = false;
				break;
			}
		}
		return invertedLut;
	}
	
	/** Returns true if this image uses a color LUT. */
	public boolean isColorLut() {
		if (cm==null || !(cm instanceof IndexColorModel))
			return false;
    	IndexColorModel icm = (IndexColorModel)cm;
		int mapSize = icm.getMapSize();
		byte[] reds = new byte[mapSize];
		byte[] greens = new byte[mapSize];
		byte[] blues = new byte[mapSize];	
		icm.getReds(reds); 
		icm.getGreens(greens); 
		icm.getBlues(blues);
		boolean isColor = false;
		for (int i=0; i<mapSize; i++) {
			if ((reds[i] != greens[i]) || (greens[i] != blues[i])) {
				isColor = true;
				break;
			}
		}
		return isColor;
	}

	/** Returns true if this image uses a pseudocolor or grayscale LUT, 
		in other words, is this an image that can be filtered. */
    public boolean isPseudoColorLut() {
		if (cm==null || !(cm instanceof IndexColorModel))
			return false;
		if (getMinThreshold()!=NO_THRESHOLD)
			return true;
    	IndexColorModel icm = (IndexColorModel)cm;
		int mapSize = icm.getMapSize();
		if (mapSize!=256)
			return false;
		byte[] reds = new byte[mapSize];
		byte[] greens = new byte[mapSize];
		byte[] blues = new byte[mapSize];	
		icm.getReds(reds); 
		icm.getGreens(greens); 
		icm.getBlues(blues);
		int r, g, b, d;
		int r2=reds[0]&255, g2=greens[0]&255, b2=blues[0]&255;
		double sum=0.0, sum2=0.0;
		for (int i=0; i<mapSize; i++) {
			r=reds[i]&255; g=greens[i]&255; b=blues[i]&255;
			d=r-r2; sum+=d; sum2+=d*d;
			d=g-g2; sum+=d; sum2+=d*d;
			d=b-b2; sum+=d; sum2+=d*d;
			r2=r; g2=g; b2=b;
		}
		double stdDev = (768*sum2-sum*sum)/768.0;
		if (stdDev>0.0)
			stdDev = Math.sqrt(stdDev/(767.0));
		else
			stdDev = 0.0;
		boolean isPseudoColor = stdDev<20.0;
		if ((int)stdDev==67) isPseudoColor = true; // "3-3-2 RGB" LUT
		if (ij.IJ.debugMode)
			ij.IJ.log("isPseudoColorLut: "+(isPseudoColor) + " " + stdDev);
		return isPseudoColor;
	}
	
	/** Returns true if the image is using the default grayscale LUT. */
	public boolean isDefaultLut() {
		if (cm==null)
			makeDefaultColorModel();
		if (!(cm instanceof IndexColorModel))
			return false;
    	IndexColorModel icm = (IndexColorModel)cm;
		int mapSize = icm.getMapSize();
		if (mapSize!=256)
			return false;
		byte[] reds = new byte[mapSize];
		byte[] greens = new byte[mapSize];
		byte[] blues = new byte[mapSize];	
		icm.getReds(reds); 
		icm.getGreens(greens); 
		icm.getBlues(blues);
		boolean isDefault = true;
		for (int i=0; i<mapSize; i++) {
			if ((reds[i]&255)!=i || (greens[i]&255)!=i || (blues[i]&255)!=i) {
				isDefault = false;
				break;
			}
		}
		return isDefault;
	}

	/** Sets the default fill/draw value to the pixel
		value closest to the specified color. */
	public abstract void setColor(Color color);

	/** Sets the default fill/draw value. Use setValue() with float images. */
	public void setColor(int value) {
		setValue(value);
	}

	/** Sets the default fill/draw value. */
	public abstract void setValue(double value);

	/** Sets the background fill value used by the rotate(), scale() and translate() methods. */
	public abstract void setBackgroundValue(double value);

	/** Returns the background fill value. */
	public abstract double getBackgroundValue();

	/** Returns the smallest displayed pixel value. */
	public abstract double getMin();

	/** Returns the largest displayed pixel value. */
	public abstract double getMax();

	/** This image will be displayed by mapping pixel values in the
		range min-max to screen values in the range 0-255. For
		byte images, this mapping is done by updating the LUT. For
		short and float images, it's done by generating 8-bit AWT
		images. For RGB images, it's done by changing the pixel values. */
	public abstract void setMinAndMax(double min, double max);

	/** For short and float images, recalculates the min and max
		image values needed to correctly display the image. For
		ByteProcessors, resets the LUT. */
	public void resetMinAndMax() {}

	/** Sets the lower and upper threshold levels. The 'lutUpdate' argument
		can be RED_LUT, BLACK_AND_WHITE_LUT, OVER_UNDER_LUT or NO_LUT_UPDATE.
		Thresholding of RGB images is not supported. */
	public void setThreshold(double minThreshold, double maxThreshold, int lutUpdate) {
		//ij.IJ.log("setThreshold: "+" "+minThreshold+" "+maxThreshold+" "+lutUpdate);
		if (this instanceof ColorProcessor)
			return;
		this.minThreshold = minThreshold;
		this.maxThreshold = maxThreshold;
		lutUpdateMode = lutUpdate;

		if (minThreshold==NO_THRESHOLD) {
			resetThreshold();
			return;
		}

		if (lutUpdate==NO_LUT_UPDATE)
			return;
		if (rLUT1==null) {
			if (cm==null)
				makeDefaultColorModel();
			baseCM = cm;
			IndexColorModel m = (IndexColorModel)cm;
			rLUT1 = new byte[256]; gLUT1 = new byte[256]; bLUT1 = new byte[256];
			m.getReds(rLUT1); m.getGreens(gLUT1); m.getBlues(bLUT1);
			rLUT2 = new byte[256]; gLUT2 = new byte[256]; bLUT2 = new byte[256];
		}
		int t1 = (int)minThreshold;
		int t2 = (int)maxThreshold;
// BDZ - DELETED CODE
		if (lutUpdate==RED_LUT)
			for (int i=0; i<256; i++) {
				if (i>=t1 && i<=t2) {
					rLUT2[i] = (byte)255;
					gLUT2[i] = (byte)0;
					bLUT2[i] = (byte)0;
				} else {
					rLUT2[i] = rLUT1[i];
					gLUT2[i] = gLUT1[i];
					bLUT2[i] = bLUT1[i];
				}
			}

		else if (lutUpdate==BLACK_AND_WHITE_LUT) {
			// updated in v1.43i by Gabriel Lindini to use blackBackground setting

			byte  foreground = Prefs.blackBackground?(byte)255:(byte)0;

			byte background = (byte)(255 - foreground);

			for (int i=0; i<256; i++) {

				if (i>=t1 && i<=t2) {

					rLUT2[i] = foreground;

					gLUT2[i] = foreground;

					bLUT2[i] = foreground;

				} else {

					rLUT2[i] = background;

					gLUT2[i] =background;

					bLUT2[i] =background;

				}

			}
		} else {
			for (int i=0; i<256; i++) {
				if (i>=t1 && i<=t2) {
					rLUT2[i] = rLUT1[i];
					gLUT2[i] = gLUT1[i];
					bLUT2[i] = bLUT1[i];

				} else if (i>t2) {
					rLUT2[i] = (byte)overRed;
					gLUT2[i] = (byte)overGreen;
					bLUT2[i] = (byte)overBlue;
				} else { 
					rLUT2[i] = (byte)underRed;
					gLUT2[i] = (byte)underGreen; 
					bLUT2[i] = (byte)underBlue;
				}
			}
		}

		cm = new IndexColorModel(8, 256, rLUT2, gLUT2, bLUT2);
		newPixels = true;
		source = null;
	}
	
	public void setAutoThreshold(String mString) {
		if (mString==null)
			throw new IllegalArgumentException("Null method");
		boolean darkBackground = mString.indexOf("dark")!=-1;
		int index = mString.indexOf(" ");
		if (index!=-1)
			mString = mString.substring(0, index);
		setAutoThreshold(mString, darkBackground, RED_LUT);
	}
	
	public void setAutoThreshold(String mString, boolean darkBackground, int lutUpdate) {
		Method m = null;
		try {
			m = Method.valueOf(Method.class, mString);
		} catch(Exception e) {
			m = null;
		}
		if (m==null)
			throw new IllegalArgumentException("Invalid method (\""+mString+"\")");
		setAutoThreshold(m, darkBackground, lutUpdate);
	}

	public void setAutoThreshold(Method method, boolean darkBackground) {
		setAutoThreshold(method, darkBackground, RED_LUT);
	}

	public void setAutoThreshold(Method method, boolean darkBackground, int lutUpdate) {
		if (method==null || (this instanceof ColorProcessor))
			return;
		double min=0.0, max=0.0;
		boolean notByteData = !(this instanceof ByteProcessor);
		ImageProcessor ip2 = this;
		if (notByteData) {
			ImageProcessor mask = ip2.getMask();
			Rectangle rect = ip2.getRoi();
			resetMinAndMax();
			min = getMin(); max = getMax();
			ip2 = convertToByte(true);
			ip2.setMask(mask);
			ip2.setRoi(rect);	
		}
		int options = ij.measure.Measurements.AREA+ ij.measure.Measurements.MIN_MAX+ ij.measure.Measurements.MODE;
		ImageStatistics stats = ImageStatistics.getStatistics(ip2, options, null);
		AutoThresholder thresholder = new AutoThresholder();
		int threshold = thresholder.getThreshold(method, stats.histogram);
		double lower, upper;
		if (darkBackground) {
			if (isInvertedLut())
				{lower=0.0; upper=threshold;}
			else
				{lower=threshold+1; upper=255.0;}
		} else {
			if (isInvertedLut())
				{lower=threshold+1; upper=255.0;}
			else
				{lower=0.0; upper=threshold;}
		}
		if (lower>255) lower = 255;
		if (notByteData) {
			if (max>min) {
				lower = min + (lower/255.0)*(max-min);
				upper = min + (upper/255.0)*(max-min);
			} else
				lower = upper = min;
		}
		setThreshold(lower, upper, lutUpdate);
		//if (notByteData && lutUpdate!=NO_LUT_UPDATE)
		//	setLutAnimation(true);
	}

	/** Automatically sets the lower and upper threshold levels, where 'method'
		 must be ISODATA or ISODATA2 and 'lutUpdate' must be RED_LUT,
		 BLACK_AND_WHITE_LUT, OVER_UNDER_LUT or NO_LUT_UPDATE.
	*/
	public void setAutoThreshold(int method, int lutUpdate) {
		if (method<0 || method>ISODATA2)
			throw new IllegalArgumentException("Invalid thresholding method");
		if (this instanceof ColorProcessor)
			return;
		double min=0.0, max=0.0;
		boolean notByteData = !(this instanceof ByteProcessor);
		ImageProcessor ip2 = this;
		if (notByteData) {
			ImageProcessor mask = ip2.getMask();
			Rectangle rect = ip2.getRoi();
			resetMinAndMax();
			min = getMin(); max = getMax();
			ip2 = convertToByte(true);
			ip2.setMask(mask);
			ip2.setRoi(rect);	
		}
		int options = ij.measure.Measurements.AREA+ ij.measure.Measurements.MIN_MAX+ ij.measure.Measurements.MODE;
		ImageStatistics stats = ImageStatistics.getStatistics(ip2, options, null);
		int[] histogram = stats.histogram;
		int originalModeCount = histogram[stats.mode];
		if (method==ISODATA2) {
			int maxCount2 = 0;
			for (int i = 0; i<stats.nBins; i++) {
				if ((histogram[i] > maxCount2) && (i!=stats.mode))
					maxCount2 = histogram[i];
			}
			int hmax = stats.maxCount;
			if ((hmax>(maxCount2 * 2)) && (maxCount2 != 0)) {
				hmax = (int)(maxCount2 * 1.5);
				histogram[stats.mode] = hmax;
        		}
		}
		int threshold = ip2.getAutoThreshold(stats.histogram);
		histogram[stats.mode] = originalModeCount;
		float[] hist = new float[256];
		for (int i=0; i<256; i++)
			hist[i] = stats.histogram[i];
		FloatProcessor fp = new FloatProcessor(256, 1, hist, null);
		GaussianBlur gb = new GaussianBlur();
		gb.blur1Direction(fp, 2.0, 0.01, true, 0);
// BDZ - BEGIN CHANGES
		float maxCount=0f, sum=0f, count;
// BDZ - END CHANGES
		int mode = 0;
		for (int i=0; i<256; i++) {
			count = hist[i];
			sum += count;
			if (count>maxCount) {
				maxCount = count;
				mode = i;
			}
		}
		double avg = sum/256.0;
		double lower, upper;
		if (maxCount/avg>1.5) {
			if ((stats.max-mode)>(mode-stats.min))
				{lower=threshold; upper=255.0;}
			else
				{lower=0.0; upper=threshold;}
		} else {
			if (isInvertedLut())
				{lower=threshold; upper=255.0;}
			else
				{lower=0.0; upper=threshold;}
		}
		if (notByteData) {
			if (max>min) {
				lower = min + (lower/255.0)*(max-min);
				upper = min + (upper/255.0)*(max-min);
			} else
				lower = upper = min;
		}
		setThreshold(lower, upper, lutUpdate);
		//if (notByteData && lutUpdate!=NO_LUT_UPDATE)
		//	setLutAnimation(true);
	}

	/** Disables thresholding. */
	public void resetThreshold() {
		minThreshold = NO_THRESHOLD;
		if (baseCM!=null) {
			cm = baseCM;
			baseCM = null;
		}
		rLUT1 = rLUT2 = null;
		inversionTested = false;
		newPixels = true;
		source = null;
	}

	/** Returns the lower threshold level. Returns NO_THRESHOLD
		if thresholding is not enabled. */
	public double getMinThreshold() {
		return minThreshold;
	}

	/** Returns the upper threshold level. */
	public double getMaxThreshold() {
		return maxThreshold;
	}
	
	/** Returns the LUT update mode, which can be RED_LUT, BLACK_AND_WHITE_LUT, 
		OVER_UNDER_LUT or NO_LUT_UPDATE. */
	public int getLutUpdateMode() {
		return lutUpdateMode;
	}

	/* Sets the threshold levels (non-visible) of an 8-bit mask based on
		the state of Prefs.blackBackground and isInvertedLut().
		@see ImageProcessor#resetBinaryThreshold	
	*/
	public void setBinaryThreshold() {
		//ij.IJ.log("setMaskThreshold1");
		if (!(this instanceof ByteProcessor)) return;
		double t1=255.0, t2=255.0;
		boolean invertedLut = isInvertedLut();
		if ((invertedLut&&ij.Prefs.blackBackground) || (!invertedLut&&!ij.Prefs.blackBackground)) {
			t1 = 0.0;
			t2 = 0.0;
		}
		//ij.IJ.log("setMaskThreshold2 "+t1+" "+t2);
		setThreshold(t1, t2, ImageProcessor.NO_LUT_UPDATE);
	}

	/** Resets the threshold if minThreshold=maxThreshold and lutUpdateMode=NO_LUT_UPDATE. 
		This removes the invisible threshold set by the MakeBinary and Convert to Mask commands.
		@see ImageProcessor#setBinaryThreshold	
	*/
	public void resetBinaryThreshold() {
		if (minThreshold==maxThreshold && lutUpdateMode==NO_LUT_UPDATE)
			resetThreshold();
	}

	/** Defines a rectangular region of interest and sets the mask 
		to null if this ROI is not the same size as the previous one. 
		@see ImageProcessor#resetRoi		
	*/
	public void setRoi(Rectangle roi) {
		if (roi==null)
			resetRoi();
		else
			setRoi(roi.x, roi.y, roi.width, roi.height);
	}

	/** Defines a rectangular region of interest and sets the mask to 
		null if this ROI is not the same size as the previous one. 
		@see ImageProcessor#resetRoi		
	*/
	public void setRoi(int x, int y, int rwidth, int rheight) {
		if (x<0 || y<0 || x+rwidth>width || y+rheight>height) {
			//find intersection of roi and this image
			Rectangle r1 = new Rectangle(x, y, rwidth, rheight);
			Rectangle r2 = r1.intersection(new Rectangle(0, 0, width, height));
			if (r2.width<=0 || r2.height<=0) {
				roiX=0; roiY=0; roiWidth=0; roiHeight=0;
				xMin=0; xMax=0; yMin=0; yMax=0;
				mask=null;
				return;
			}
			if (mask!=null && mask.getWidth()==rwidth && mask.getHeight()==rheight) {
				Rectangle r3 = new Rectangle(0, 0, r2.width, r2.height);
				if (x<0) r3.x = -x;
				if (y<0) r3.y = -y;
				mask.setRoi(r3);
				mask = mask.crop();
			}
			roiX=r2.x; roiY=r2.y; roiWidth=r2.width; roiHeight=r2.height;
		} else {
			roiX=x; roiY=y; roiWidth=rwidth; roiHeight=rheight;
		}
		if (mask!=null && (mask.getWidth()!=roiWidth||mask.getHeight()!=roiHeight))
			mask = null;
		//setup limits for 3x3 filters
		xMin = Math.max(roiX, 1);
		xMax = Math.min(roiX + roiWidth - 1, width - 2);
		yMin = Math.max(roiY, 1);
		yMax = Math.min(roiY + roiHeight - 1, height - 2);
	}
	
	/** Defines a non-rectangular region of interest that will consist of a
		rectangular ROI and a mask. After processing, call <code>reset(mask)</code>
		to restore non-masked pixels. Here is an example:
		<pre>
		ip.setRoi(new OvalRoi(50, 50, 100, 50));
		ip.fill();
		ip.reset(ip.getMask());
		</pre>
		The example assumes <code>snapshot()</code> has been called, which is the case
		for code executed in the <code>run()</code> method of plugins that implement the 
		<code>PlugInFilter</code> interface.
		@see ij.ImagePlus#getRoi
	*/
	public void setRoi(Roi roi) {
		if (roi==null)
			resetRoi();
		else {
			setMask(roi.getMask());
			setRoi(roi.getBounds());
		}
	}

	/** Defines a polygonal region of interest that will consist of a
		rectangular ROI and a mask. After processing, call <code>reset(mask)</code>
		to restore non-masked pixels. Here is an example:
		<pre>
		Polygon p = new Polygon();
		p.addPoint(50, 0); p.addPoint(100, 100); p.addPoint(0, 100);
		ip.setRoi(triangle);
		ip.invert();
		ip.reset(ip.getMask());
		</pre>
		The example assumes <code>snapshot()</code> has been called, which is the case
		for code executed in the <code>run()</code> method of plugins that implement the 
		<code>PlugInFilter</code> interface.
		@see ij.gui.Roi#getPolygon
		@see ImageProcessor#drawPolygon
		@see ImageProcessor#fillPolygon
	*/
	public void setRoi(Polygon roi) {
		if (roi==null)
			{resetRoi(); return;}
		Rectangle bounds = roi.getBounds();
		for (int i=0; i<roi.npoints; i++) {
			roi.xpoints[i] -= bounds.x;
			roi.ypoints[i] -= bounds.y;
		}
		PolygonFiller pf = new PolygonFiller();
		pf.setPolygon(roi.xpoints, roi.ypoints, roi.npoints);
		ImageProcessor mask = pf.getMask(bounds.width, bounds.height);
		setMask(mask);
		setRoi(bounds);
		for (int i=0; i<roi.npoints; i++) {
			roi.xpoints[i] += bounds.x;
			roi.ypoints[i] += bounds.y;
		}
	}

	/** Sets the ROI (Region of Interest) and clipping rectangle to the entire image. */
	public void resetRoi() {
		roiX=0; roiY=0; roiWidth=width; roiHeight=height;
		xMin=1; xMax=width-2; yMin=1; yMax=height-2;
		mask=null;
		clipXMin=0; clipXMax=width-1; clipYMin=0; clipYMax=height-1; 
	}

	/** Returns a Rectangle that represents the current
		region of interest. */
	public Rectangle getRoi() {
		return new Rectangle(roiX, roiY, roiWidth, roiHeight);
	}

	/** Defines a byte mask that limits processing to an
		irregular ROI. Background pixels in the mask have
		a value of zero. */
	public void setMask(ImageProcessor mask) {
		this.mask = mask;
	}

	/** For images with irregular ROIs, returns a mask, otherwise, 
		returns null. Pixels outside the mask have a value of zero. */
	public ImageProcessor getMask() {
		return mask;
	}

	/** Returns the mask byte array, or null if there is no mask. */
	public byte[] getMaskArray() {
		return mask!=null?(byte[])mask.getPixels():null;
	}

	/** Assigns a progress bar to this processor. Set 'pb' to
		null to disable the progress bar. */
	public void setProgressBar(ProgressBar pb) {
		progressBar = pb;
	}

	/** This method has been replaced by setInterpolationMethod(). */
	public void setInterpolate(boolean interpolate) {
		this.interpolate = interpolate;
		if (interpolate)
			interpolationMethod = useBicubic?BICUBIC:BILINEAR;
		else
			interpolationMethod = NONE;
	}
	
	/** Use this method to set the interpolation method (NONE, 
		 BILINEAR or BICUBIC) used by scale(), resize() and rotate(). */
	public void setInterpolationMethod(int method) {
		if (method<NONE || method>BICUBIC)
			throw new IllegalArgumentException("Invalid interpolation method");
		interpolationMethod = method;
		interpolate = method!=NONE?true:false;
	}
	
	/** Returns the current interpolation method (NONE, BILINEAR or BICUBIC). */
	public int getInterpolationMethod() {
		return interpolationMethod;
	}
	
	public static String[] getInterpolationMethods() {
		if (interpolationMethods==null)
			interpolationMethods = new String[] {"None", "Bilinear", "Bicubic"};
		return interpolationMethods;
	}

	/** Returns the value of the interpolate field. */
	public boolean getInterpolate() {
		return interpolate;
	}

	/** @deprecated */
	public boolean isKillable() {
		return false;
	}

	private void process(int op, double value) {
		double SCALE = 255.0/Math.log(255.0);
		int v;
		
		int[] lut = new int[256];
		for (int i=0; i<256; i++) {
			switch(op) {
				case INVERT:
					v = 255 - i;
					break;
				case FILL:
					v = fgColor;
					break;
				case ADD:
					v = i + (int)value;
					break;
				case MULT:
					v = (int)Math.round(i * value);
					break;
				case AND:
					v = i & (int)value;
					break;
				case OR:
					v = i | (int)value;
					break;
				case XOR:
					v = i ^ (int)value;
					break;
				case GAMMA:
					v = (int)(Math.exp(Math.log(i/255.0)*value)*255.0);
					break;
				case LOG:
					if (i==0)
						v = 0;
					else
						v = (int)(Math.log(i) * SCALE);
					break;
				case EXP:
					v = (int)(Math.exp(i/SCALE));
					break;
				case SQR:
						v = i*i;
					break;
				case SQRT:
						v = (int)Math.sqrt(i);
					break;
				case MINIMUM:
					if (i<value)
						v = (int)value;
					else
						v = i;
					break;
				case MAXIMUM:
					if (i>value)
						v = (int)value;
					else
						v = i;
					break;
				 default:
				 	v = i;
			}
			if (v < 0)
				v = 0;
			if (v > 255)
				v = 255;
			lut[i] = v;
		}
		applyTable(lut);
    }

	/**
		Returns an array containing the pixel values along the
		line starting at (x1,y1) and ending at (x2,y2). For byte
		and short images, returns calibrated values if a calibration
		table has been set using setCalibrationTable().
		@see ImageProcessor#setInterpolate
	*/
	public double[] getLine(double x1, double y1, double x2, double y2) {
		double dx = x2-x1;
		double dy = y2-y1;
		int n = (int)Math.round(Math.sqrt(dx*dx + dy*dy));
		double xinc = dx/n;
		double yinc = dy/n;
		n++;
		double[] data = new double[n];
		double rx = x1;
		double ry = y1;
		if (interpolate) {
			for (int i=0; i<n; i++) {
				data[i] = getInterpolatedValue(rx, ry);
				rx += xinc;
				ry += yinc;
			}
		} else {
			for (int i=0; i<n; i++) {
				data[i] = getPixelValue((int)(rx+0.5), (int)(ry+0.5));
				rx += xinc;
				ry += yinc;
			}
		}
		return data;
	}
	
	/** Returns the pixel values along the horizontal line starting at (x,y). */
	public void getRow(int x, int y, int[] data, int length) {
		for (int i=0; i<length; i++)
			data[i] = getPixel(x++, y);
	}

	/** Returns the pixel values down the column starting at (x,y). */
	public void getColumn(int x, int y, int[] data, int length) {
		for (int i=0; i<length; i++)
			data[i] = getPixel(x, y++);
	}

	/** Inserts the pixels contained in 'data' into a 
		horizontal line starting at (x,y). */
	public void putRow(int x, int y, int[] data, int length) {
		for (int i=0; i<length; i++)
			putPixel(x++, y, data[i]);
	}
	
	/** Inserts the pixels contained in 'data' into a 
		column starting at (x,y). */
	public void putColumn(int x, int y, int[] data, int length) {
		//if (x>=0 && x<width && y>=0 && (y+length)<=height)
		//	((ShortProcessor)this).putColumn2(x, y, data, length);
		//else 
			for (int i=0; i<length; i++)
				putPixel(x, y++, data[i]);
	}

	/**
	Sets the current drawing location.
	@see ImageProcessor#lineTo
	@see ImageProcessor#drawString
	*/
	public void moveTo(int x, int y) {
		cx = x;
		cy = y;
	}
	
	/** Sets the line width used by lineTo() and drawDot(). */
	public void setLineWidth(int width) {
		lineWidth = width;
		if (lineWidth<1) lineWidth = 1;
	}
				
	/** Returns the current line width. */
	public int getLineWidth() {
		return lineWidth;
	}

	/** Draws a line from the current drawing location to (x2,y2). */
	public void lineTo(int x2, int y2) {
		int dx = x2-cx;
		int dy = y2-cy;
		int absdx = dx>=0?dx:-dx;
		int absdy = dy>=0?dy:-dy;
		int n = absdy>absdx?absdy:absdx;
		double xinc = (double)dx/n;
		double yinc = (double)dy/n;
		double x = cx;
		double y = cy;
		n++;
		cx = x2; cy = y2;
		if (n>1000000) return;
		do {
			if (lineWidth==1)
				drawPixel((int)Math.round(x), (int)Math.round(y));
			else if (lineWidth==2)
				drawDot2((int)Math.round(x), (int)Math.round(y));
			else
				drawDot((int)x, (int)y);
			x += xinc;
			y += yinc;
		} while (--n>0);
		//if (lineWidth>2) resetRoi();
	}
		
	/** Draws a line from (x1,y1) to (x2,y2). */
	public void drawLine(int x1, int y1, int x2, int y2) {
		moveTo(x1, y1);
		lineTo(x2, y2);
	}

	/** Draws a rectangle. */
	public void drawRect(int x, int y, int width, int height) {
		if (width<1 || height<1)
			return;
		if (lineWidth==1) {
			moveTo(x, y);
			lineTo(x+width-1, y);
			lineTo(x+width-1, y+height-1);
			lineTo(x, y+height-1);
			lineTo(x, y);
		} else {
			moveTo(x, y);
			lineTo(x+width, y);
			lineTo(x+width, y+height);
			lineTo(x, y+height);
			lineTo(x, y);
		}
	}

	/** Draws an elliptical shape. */
	public void drawOval(int x, int y, int width, int height) {
		if ((long)width*height>4*this.width*this.height) return;
		OvalRoi oval = new OvalRoi(x, y, width, height);
		drawPolygon(oval.getPolygon());
	}

	/** Fills an elliptical shape. */
	public void fillOval(int x, int y, int width, int height) {
		if ((long)width*height>4*this.width*this.height) return;
		OvalRoi oval = new OvalRoi(x, y, width, height);
		fillPolygon(oval.getPolygon());
	}

	/** Draws a polygon. */
	public void drawPolygon(Polygon p) {
		moveTo(p.xpoints[0], p.ypoints[0]);
		for (int i=0; i<p.npoints; i++)
			lineTo(p.xpoints[i], p.ypoints[i]);
		lineTo(p.xpoints[0], p.ypoints[0]);
	}

	/** Fills a polygon. */
	public void fillPolygon(Polygon p) {
		setRoi(p);
		fill(getMask());
		resetRoi();
	}

	/** @deprecated */
	public void drawDot2(int x, int y) {
		drawPixel(x, y);
		drawPixel(x-1, y);
		drawPixel(x, y-1);
		drawPixel(x-1, y-1);
	}
		
    /** Draws a dot using the current line width and fill/draw value. */
	public void drawDot(int xcenter, int ycenter) {
		double r = lineWidth/2.0;
		int xmin=(int)(xcenter-r+0.5), ymin=(int)(ycenter-r+0.5);
		int xmax=xmin+lineWidth, ymax=ymin+lineWidth;
		if (xmin<clipXMin || ymin<clipYMin || xmax>clipXMax || ymax>clipYMax ) {
			// draw edge dot
			double r2 = r*r;
			r -= 0.5;
			double xoffset=xmin+r, yoffset=ymin+r;
			double xx, yy;
			for (int y=ymin; y<ymax; y++) {
				for (int x=xmin; x<xmax; x++) {
					xx = x-xoffset; yy = y-yoffset;
					if (xx*xx+yy*yy<=r2)
						drawPixel(x, y);
				}
			}
		} else {
			if (dotMask==null || lineWidth!=dotMask.getWidth()) {
				OvalRoi oval = new OvalRoi(0, 0, lineWidth, lineWidth);
				dotMask = oval.getMask();
			}
			setRoi(xmin, ymin, lineWidth, lineWidth);
			fill(dotMask);
		}
	}
	
    private ImageProcessor dotMask;

	private void setupFontMetrics() {
		if (fmImage==null)
			fmImage=new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		if (font==null)
			font = new Font("SansSerif", Font.PLAIN, 12);
		if (fontMetrics==null) {
			Graphics g = fmImage.getGraphics();
			fontMetrics = g.getFontMetrics(font);
		}
	}

	/** Draws a string at the current location using the current fill/draw value.
        Draws multiple lines if the string contains newline characters. */
	public void drawString(String s) {
		if (s==null || s.equals("")) return;
		setupFontMetrics();
		if (ij.IJ.isMacOSX()) s += " ";
		if (s.indexOf("\n")==-1)
			drawString2(s);
		else {
			String[] s2 = Tools.split(s, "\n");
			for (int i=0; i<s2.length; i++)
				drawString2(s2[i]);
		}
	}

	private void drawString2(String s) {
		int w =  getStringWidth(s);
		int cxx = cx;
		if (justification==CENTER_JUSTIFY)
			cxx -= w/2;
		else if (justification==RIGHT_JUSTIFY)
			cxx -= w;
		int h =  fontMetrics.getHeight();
		if (w<=0 || h<=0) return;
		Image bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics g = bi.getGraphics();
		FontMetrics metrics = g.getFontMetrics(font);
// BDZ - DELETED CODE
		int descent = metrics.getDescent();
		g.setFont(font);

		if (antialiasedText && cxx>=00 && cy-h>=0) {
			Java2.setAntialiasedText(g, true);
			setRoi(cxx, cy-h, w, h);
			ImageProcessor ip = crop();
			resetRoi();
			if (ip.getWidth()==0||ip.getHeight()==0)
				return;
			g.drawImage(ip.createImage(), 0, 0, null);
			g.setColor(drawingColor);
			g.drawString(s, 0, h-descent);
			g.dispose();
			ip = new ColorProcessor(bi);
			if (this instanceof ByteProcessor) {
				ip = ip.convertToByte(false);
				if (isInvertedLut()) ip.invert();
			}
			//new ij.ImagePlus("ip",ip).show();
			insert(ip, cxx, cy-h);
			cy += h;
			return;
		}
		
		Java2.setAntialiasedText(g, false);
		g.setColor(Color.white);
		g.fillRect(0, 0, w, h);
		g.setColor(Color.black);
		g.drawString(s, 0, h-descent);
		g.dispose();
		ImageProcessor ip = new ColorProcessor(bi);
		ImageProcessor textMask = ip.convertToByte(false);
// BDZ - DELETED CODE
		//new ij.ImagePlus("textmask",textMask).show();
		textMask.invert();
		if (cxx<width && cy-h<height) {
			setMask(textMask);
			setRoi(cxx,cy-h,w,h);
			fill(getMask());
		}
		resetRoi();
		cy += h;
	}

	/** Draws a string at the specified location using the current fill/draw value. */
	public void drawString(String s, int x, int y) {
		moveTo(x, y);
		drawString(s);
	}

	/** Sets the justification used by drawString(), where <code>justification</code>
		is CENTER_JUSTIFY, RIGHT_JUSTIFY or LEFT_JUSTIFY. The default is LEFT_JUSTIFY. */
	public void setJustification(int justification) {
		this.justification = justification;
	}

	/** Sets the font used by drawString(). */
	public void setFont(Font font) {
		this.font = font;
		fontMetrics	= null;
		boldFont = font.isBold();
	}
	
	/** Specifies whether or not text is drawn using antialiasing. Antialiased
		test requires Java 2 and an 8 bit or RGB image. Antialiasing does not
		work with 8-bit images that are not using 0-255 display range. */
	public void setAntialiasedText(boolean antialiasedText) {
		if (antialiasedText && (((this instanceof ByteProcessor)&&getMin()==0.0&&getMax()==255.0) || (this instanceof ColorProcessor)))
			this.antialiasedText = true;
		else
			this.antialiasedText = false;
	}

	/** Returns the width in pixels of the specified string. */
	public int getStringWidth(String s) {
		setupFontMetrics();
		int w;
		if (antialiasedText) {
			Graphics g = fmImage.getGraphics();
			if (g==null) {
				fmImage = null;
				setupFontMetrics();
				g = fmImage.getGraphics();
			}
			Java2.setAntialiasedText(g, true);
			w = Java2.getStringWidth(s, fontMetrics, g);
			g.dispose();
		} else
			w =  fontMetrics.stringWidth(s);
		return w;
	}
	
	/** Returns the current font. */
	public Font getFont() {
		setupFontMetrics();
		return font;
	}

	/** Returns the current FontMetrics. */
	public FontMetrics getFontMetrics() {
		setupFontMetrics();
		return fontMetrics;
	}

	/** Replaces each pixel with the 3x3 neighborhood mean. */
	public void smooth() {
		if (width>1)
			filter(BLUR_MORE);
	}

	/** Sharpens the image or ROI using a 3x3 convolution kernel. */
	public void sharpen() {
		if (width>1) {
			int[] kernel = {-1, -1, -1,
		                -1, 12, -1,
		                -1, -1, -1};
			convolve3x3(kernel);
		}
	}
	
	/** Finds edges in the image or ROI using a Sobel operator. */
	public void findEdges() {
		if (width>1)
			filter(FIND_EDGES);
	}

	/** Flips the image or ROI vertically. */
	public abstract void flipVertical();

	/** Flips the image or ROI horizontally. */
	public void flipHorizontal() {
		int[] col1 = new int[roiHeight];
		int[] col2 = new int[roiHeight];
		for (int x=0; x<roiWidth/2; x++) {
			getColumn(roiX+x, roiY, col1, roiHeight);
			getColumn(roiX+roiWidth-x-1, roiY, col2, roiHeight);
			putColumn(roiX+x, roiY, col2, roiHeight);
			putColumn(roiX+roiWidth-x-1, roiY, col1, roiHeight);
		}
	}

	/** Rotates the entire image 90 degrees clockwise. Returns
		a new ImageProcessor that represents the rotated image. */
	public ImageProcessor rotateRight() {
		int width2 = height;
		int height2 = width;
        ImageProcessor ip2 = createProcessor(width2, height2);
		int[] arow = new int[width];
		for (int row=0; row<height; row++) {
			getRow(0, row, arow, width);
			ip2.putColumn(width2-row-1, 0, arow, height2);
		}
        return ip2;
	}
	
	/** Rotates the entire image 90 degrees counter-clockwise. Returns
		a new ImageProcessor that represents the rotated image. */
	public ImageProcessor rotateLeft() {
		int width2 = height;
		int height2 = width;
        ImageProcessor ip2 = createProcessor(width2, height2);
		int[] arow = new int[width];
		int[] arow2 = new int[width];
		for (int row=0; row<height; row++) {
			getRow(0, row, arow, width);
			for (int i=0; i<width; i++) {
				arow2[i] = arow[width-i-1];
			}
			ip2.putColumn(row, 0, arow2, height2);
		}
        return ip2;
	}

	/** Inserts the image contained in 'ip' at (xloc, yloc). */
	public void insert(ImageProcessor ip, int xloc, int yloc) {
		copyBits(ip, xloc, yloc, Blitter.COPY);
	}
		
	/** Returns a string containing information about this ImageProcessor. */
	public String toString() {
		return ("ip[width="+width+", height="+height+", min="+getMin()+", max="+getMax()+"]");
	}

	/** Fills the image or ROI bounding rectangle with the current fill/draw value. Use
	*	fill(Roi) or fill(ip.getMask()) to fill non-rectangular selections.
	*	@see #setColor(Color)
	*	@see #setValue(double)
	*	@see #fill(Roi)
	*/
	public void fill() {
		process(FILL, 0.0);
	}

	/** Fills pixels that are within the ROI bounding rectangle and part of 
	*	the mask (i.e. pixels that have a value=BLACK in the mask array).
	*	Use ip.getMask() to acquire the mask. 
	*	Throws and IllegalArgumentException if the mask is null or
	*	the size of the mask is not the same as the size of the ROI.
	*	@see #setColor(Color)
	*	@see #setValue(double)
	*	@see #getMask
	*	@see #fill(Roi)
	*/
	public abstract void fill(ImageProcessor mask);

	/**	 Fills the ROI with the current fill/draw value. 
	*	@see #setColor(Color)
	*	@see #setValue(double)
	*	@see #fill(Roi)
	*/
	public void fill(Roi roi) {
		ImageProcessor m = getMask();
		Rectangle r = getRoi();
		setRoi(roi);
		fill(getMask());
		setMask(m);
		setRoi(r);
	}

	/** Fills outside an Roi. */
	public void fillOutside(Roi roi) {
		if (roi==null || !roi.isArea()) return;
		ImageProcessor m = getMask();
		Rectangle r = getRoi();
		ShapeRoi s1, s2;
		if (roi instanceof ShapeRoi)
			s1 = (ShapeRoi)roi;
		else
			s1 = new ShapeRoi(roi);
		s2 = new ShapeRoi(new Roi(0,0, width, height));
		setRoi(s1.xor(s2));
		fill(getMask());
		setMask(m);
		setRoi(r);
	}

	/** Draws an Roi. */
	public void draw(Roi roi) {
		roi.drawPixels(this);
	}

	/** Set a lookup table used by getPixelValue(), getLine() and
		convertToFloat() to calibrate pixel values. The length of
		the table must be 256 for byte images and 65536 for short
		images. RGB and float processors do not do calibration.
		@see ij.measure.Calibration#setCTable
	*/
	public void setCalibrationTable(float[] cTable) {
		this.cTable = cTable;
	}

	/** Returns the calibration table or null. */
	public float[] getCalibrationTable() {
		return cTable;
	}

	/** Set the number of bins to be used for histograms of float images. */
	public void setHistogramSize(int size) {
		histogramSize = size;
		if (histogramSize<1) histogramSize = 1;
	}

	/**	Returns the number of float image histogram bins. The bin
		count is fixed at 256 for the other three data types. */
	public int getHistogramSize() {
		return histogramSize;
	}

	/** Set the range used for histograms of float images. The image range is
		used if both <code>histMin</code> and <code>histMax</code> are zero. */
	public void setHistogramRange(double histMin, double histMax) {
		if (histMin>histMax) {
			histMin = 0.0;
			histMax = 0.0;
		}
		histogramMin = histMin;
		histogramMax = histMax;
	}

	/**	Returns the minimum histogram value used for histograms of float images. */
	public double getHistogramMin() {
		return histogramMin;
	}

	/**	Returns the maximum histogram value used for histograms of float images. */
	public double getHistogramMax() {
		return histogramMax;
	}

	/** Returns a reference to this image's pixel array. The
		array type (byte[], short[], float[] or int[]) varies
		depending on the image type. */
	public abstract Object getPixels();
	
	/** Returns a copy of the pixel data. Or returns a reference to the
		snapshot buffer if it is not null and 'snapshotCopyMode' is true.
		@see ImageProcessor#snapshot
		@see ImageProcessor#setSnapshotCopyMode
	*/
	public abstract Object getPixelsCopy();

	/** Returns the value of the pixel at (x,y). For RGB images, the
		argb values are packed in an int. For float images, the
		the value must be converted using Float.intBitsToFloat().
		Returns zero if either the x or y coodinate is out of range. */
	public abstract int getPixel(int x, int y);
	
	public int getPixelCount() {
		return width*height;
	}

	/** This is a faster version of getPixel() that does not do bounds checking. */
	public abstract int get(int x, int y);
	
	public abstract int get(int index);

	/** This is a faster version of putPixel() that does not clip  
		out of range values and does not do bounds checking. */
	public abstract void set(int x, int y, int value);

	public abstract void set(int index, int value);

	public abstract float getf(int x, int y);
	
	public abstract float getf(int index);

	public abstract void setf(int x, int y, float value);

	public abstract void setf(int index, float value);

	/** Returns a copy of the pixel data as a 2D int array with
		dimensions [x=0..width-1][y=0..height-1]. With RGB
		images, the returned values are in packed ARGB format.
		With float images, the returned values must be converted
		to float using Float.intBitsToFloat(). */
	public int[][] getIntArray() {
		int[][] a = new int [width][height];
		for(int y=0; y<height; y++) {
			for(int x=0; x<width; x++)
				a[x][y]=get(x,y);
		}
		return a; 
	}

	/** Replaces the pixel data with contents of the specified 2D int array. */
	public void setIntArray(int[][] a) {
		for(int y=0; y<height; y++) {
			for(int x=0; x<width; x++)
				set(x, y, a[x][y]);
		}
	}

	/** Returns a copy of the pixel data as a 2D float 
		array with dimensions [x=0..width-1][y=0..height-1]. */
	public float[][] getFloatArray() {
		float[][] a = new float[width][height];
		for(int y=0; y<height; y++) {
			for(int x=0; x<width; x++)
				a[x][y]=getf(x,y);
		}
		return a; 
	}

	/** Replaces the pixel data with contents of the specified 2D float array. */
	public void setFloatArray(float[][] a) {
		for(int y=0; y<height; y++) {
			for(int x=0; x<width; x++)
				setf(x, y, a[x][y]);
		}
	}
	
	/** Experimental */
	public void getNeighborhood(int x, int y, double[][] arr) {
		int nx=arr.length;
		int ny=arr[0].length;
		int nx2 = (nx-1)/2;
		int ny2 = (ny-1)/2;
	 	if (x>=nx2 && y>=ny2 && x<width-nx2-1 && y<height-ny2-1) { 
			int index = (y-ny2)*width + (x-nx2);
			for (int y2=0; y2<ny; y2++) {
	 			for (int x2=0; x2<nx; x2++)
					arr[x2][y2] = getf(index++);			
				index += (width - nx);
			}	
		} else {
			for (int y2=0; y2<ny; y2++) {
	 			for (int x2=0; x2<nx; x2++)
					arr[x2][y2] = getPixelValue(x2, y2);			
			}	
		}
	}

    /** Returns the samples for the pixel at (x,y) in an int array.
    	RGB pixels have three samples, all others have one.
		Returns zeros if the the coordinates are not in bounds.
		iArray is an optional preallocated array. */
	public int[] getPixel(int x, int y, int[] iArray) {
		if (iArray==null) iArray = new int[1];
		iArray[0] = getPixel(x, y);
		return iArray;
	}

	/** Sets a pixel in the image using an int array of samples.
		RGB pixels have three samples, all others have one. */
	public void putPixel(int x, int y, int[] iArray) {
		putPixel(x, y, iArray[0]);
	}

	/** Uses the current interpolation method (bilinear or bicubic)
		to find the pixel value at real coordinates (x,y). */
	public abstract double getInterpolatedPixel(double x, double y);

	/** Uses the current interpolation method to find the pixel value at real coordinates (x,y).
		For RGB images, the argb values are packed in an int. For float images,
		the value must be converted using Float.intBitsToFloat().  Returns zero
		if the (x, y) is not inside the image. */
	public abstract int getPixelInterpolated(double x, double y);
	
	/** Uses bilinear interpolation to find the pixel value at real coordinates (x,y). 
		Returns zero if the (x, y) is not inside the image. */
	public final double getInterpolatedValue(double x, double y) {
		if (useBicubic)
			return getBicubicInterpolatedPixel(x, y, this);
		if (x<0.0 || x>=width-1.0 || y<0.0 || y>=height-1.0) {
			if (x<-1.0 || x>=width || y<-1.0 || y>=height)
				return 0.0;
			else
				return getInterpolatedEdgeValue(x, y);
		}
		int xbase = (int)x;
		int ybase = (int)y;
		double xFraction = x - xbase;
		double yFraction = y - ybase;
		if (xFraction<0.0) xFraction = 0.0;
		if (yFraction<0.0) yFraction = 0.0;
		double lowerLeft = getPixelValue(xbase, ybase);
		double lowerRight = getPixelValue(xbase+1, ybase);
		double upperRight = getPixelValue(xbase+1, ybase+1);
		double upperLeft = getPixelValue(xbase, ybase+1);
		double upperAverage = upperLeft + xFraction * (upperRight - upperLeft);
		double lowerAverage = lowerLeft + xFraction * (lowerRight - lowerLeft);
		return lowerAverage + yFraction * (upperAverage - lowerAverage);
	}

	/** This method is from Chapter 16 of "Digital Image Processing:
		An Algorithmic Introduction Using Java" by Burger and Burge
		(http://www.imagingbook.com/). */
	public double getBicubicInterpolatedPixel(double x0, double y0, ImageProcessor ip2) {
		int u0 = (int) Math.floor(x0);	//use floor to handle negative coordinates too
		int v0 = (int) Math.floor(y0);
		if (u0<=0 || u0>=width-2 || v0<=0 || v0>=height-2)
			return ip2.getBilinearInterpolatedPixel(x0, y0);
		double q = 0;
		for (int j = 0; j <= 3; j++) {
			int v = v0 - 1 + j;
			double p = 0;
			for (int i = 0; i <= 3; i++) {
				int u = u0 - 1 + i;
				p = p + ip2.get(u,v) * cubic(x0 - u);
			}
			q = q + p * cubic(y0 - v);
		}
		return q;
	}
	
// BDZ - BEGIN CHANGES
	public final double getBilinearInterpolatedPixel(double x, double y) {
// BDZ - END CHANGES
		if (x>=-1 && x<width && y>=-1 && y<height) {
			int method = interpolationMethod;
			interpolationMethod = BILINEAR;
			double value = getInterpolatedPixel(x, y);
			interpolationMethod = method;
			return value;
		} else
			return getBackgroundValue();
	}
	
	static final double a = 0.5; // Catmull-Rom interpolation
	public static final double cubic(double x) {
		if (x < 0.0) x = -x;
		double z = 0.0;
		if (x < 1.0) 
			z = x*x*(x*(-a+2.0) + (a-3.0)) + 1.0;
		else if (x < 2.0) 
			z = -a*x*x*x + 5.0*a*x*x - 8.0*a*x + 4.0*a;
		return z;
	}	

	/*
		// a = 0.5
	double cubic2(double x) {
		if (x < 0) x = -x;
		double z = 0;
		if (x < 1)
			z = 1.5*x*x*x + -2.5*x*x + 1.0;
		else if (x < 2)
			z = -0.5*x*x*x + 2.5*x*x - 4.0*x + 2.0;
		return z;
	}
	*/	

	private final double getInterpolatedEdgeValue(double x, double y) {
		int xbase = (int)x;
		int ybase = (int)y;
		double xFraction = x - xbase;
		double yFraction = y - ybase;
		if (xFraction<0.0) xFraction = 0.0;
		if (yFraction<0.0) yFraction = 0.0;
		double lowerLeft = getEdgeValue(xbase, ybase);
		double lowerRight = getEdgeValue(xbase+1, ybase);
		double upperRight = getEdgeValue(xbase+1, ybase+1);
		double upperLeft = getEdgeValue(xbase, ybase+1);
		double upperAverage = upperLeft + xFraction * (upperRight - upperLeft);
		double lowerAverage = lowerLeft + xFraction * (lowerRight - lowerLeft);
		return lowerAverage + yFraction * (upperAverage - lowerAverage);
	}

	private float getEdgeValue(int x, int y) {
		if (x<=0) x = 0;
		if (x>=width) x = width-1;
		if (y<=0) y = 0;
		if (y>=height) y = height-1;
		return getPixelValue(x, y);
	}

	/** Stores the specified value at (x,y). Does
		nothing if (x,y) is outside the image boundary.
		For 8-bit and 16-bit images, out of range values
		are clipped. For RGB images, the
		argb values are packed in 'value'. For float images,
		'value' is expected to be a float converted to an int
		using Float.floatToIntBits(). */
	public abstract void putPixel(int x, int y, int value);
	
	/** Returns the value of the pixel at (x,y). For byte and short
		images, returns a calibrated value if a calibration table
		has been  set using setCalibraionTable(). For RGB images,
		returns the luminance value. */
	public abstract float getPixelValue(int x, int y);
		
	/** Stores the specified value at (x,y). */
	public abstract void putPixelValue(int x, int y, double value);

	/** Sets the pixel at (x,y) to the current fill/draw value. */
	public abstract void drawPixel(int x, int y);
	
	/** Sets a new pixel array for the image. The length of the array must be equal to width*height.
		Use setSnapshotPixels(null) to clear the snapshot buffer. */
	public abstract void setPixels(Object pixels);
	
	/** Copies the image contained in 'ip' to (xloc, yloc) using one of
		the transfer modes defined in the Blitter interface. */
	public abstract void copyBits(ImageProcessor ip, int xloc, int yloc, int mode);

	/** Transforms the image or ROI using a lookup table. The
		length of the table must be 256 for byte images and 
		65536 for short images. RGB and float images are not
		supported. */
	public abstract void applyTable(int[] lut);

	/** Inverts the image or ROI. */
	public void invert() {process(INVERT, 0.0);}
	
	/** Adds 'value' to each pixel in the image or ROI. */
	public void add(int value) {process(ADD, value);}
	
	/** Adds 'value' to each pixel in the image or ROI. */
	public void add(double value) {process(ADD, value);}
	
	/** Multiplies each pixel in the image or ROI by 'value'. */
	public void multiply(double value) {process(MULT, value);}
	
	/** Binary AND of each pixel in the image or ROI with 'value'. */
	public void and(int value) {process(AND, value);}

	/** Binary OR of each pixel in the image or ROI with 'value'. */
	public void or(int value) {process(OR, value);}
	
	/** Binary exclusive OR of each pixel in the image or ROI with 'value'. */
	public void xor(int value) {process(XOR, value);}
	
	/** Performs gamma correction of the image or ROI. */
	public void gamma(double value) {process(GAMMA, value);}
	
	/** Performs a log transform on the image or ROI. */
	public void log() {process(LOG, 0.0);}

	/** Performs a exponential transform on the image or ROI. */
	public void exp() {process(EXP, 0.0);}

	/** Performs a square transform on the image or ROI. */
	public void sqr() {process(SQR, 0.0);}

	/** Performs a square root transform on the image or ROI. */
	public void sqrt() {process(SQRT, 0.0);}

	/** If this is a 32-bit or signed 16-bit image, performs an 
		absolute value transform, otherwise does nothing. */
	public void abs() {}

	/** Pixels less than 'value' are set to 'value'. */
	public void min(double value) {process(MINIMUM, value);}

	/** Pixels greater than 'value' are set to 'value'. */
	public void max(double value) {process(MAXIMUM, value);}

	/** Returns a copy of this image is the form of an AWT Image. */
	public abstract Image createImage();
	
	/** Returns this image as a BufferedImage. */
	public BufferedImage getBufferedImage() {
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D)bi.getGraphics();
		g.drawImage(createImage(), 0, 0, null);
		return bi;
	}

	/** Returns a new, blank processor with the specified width and height. */
	public abstract ImageProcessor createProcessor(int width, int height);
	
	/** Makes a copy of this image's pixel data that can be 
		later restored using reset() or reset(mask).
		@see ImageProcessor#reset		
		@see ImageProcessor#reset(ImageProcessor)		
	*/
	public abstract void snapshot();
	
	/** Restores the pixel data from the snapshot (undo) buffer. */
	public abstract void reset();
	
	/** Restores pixels from the snapshot buffer that are 
		within the rectangular roi but not part of the mask. */
	public abstract void reset(ImageProcessor mask);
	
	/** Sets a new pixel array for the snapshot (undo) buffer. */
	public abstract void setSnapshotPixels(Object pixels);

	/** Returns a reference to the snapshot (undo) buffer, or null. */
	public abstract Object getSnapshotPixels();

	/** Convolves the image or ROI with the specified
		3x3 integer convolution kernel. */
	public abstract void convolve3x3(int[] kernel);
	
	/** A 3x3 filter operation, where the argument (BLUR_MORE,  FIND_EDGES, 
	     MEDIAN_FILTER, MIN or MAX) determines the filter type. */
	public abstract void filter(int type);
	
	/** A 3x3 median filter. Requires 8-bit or RGB image. */
	public abstract void medianFilter();
	
    /** Adds random noise to the image or ROI.
    	@param range	the range of random numbers
    */
    public abstract void noise(double range);
    
	/** Creates a new processor containing an image
		that corresponds to the current ROI. */
	public abstract ImageProcessor crop();
	
	/** Sets pixels less than or equal to level to 0 and all other 
		pixels to 255. Only works with 8-bit and 16-bit images. */
	public abstract void threshold(int level);

	/** Returns a duplicate of this image. */
	public abstract ImageProcessor duplicate();

	/** Scales the image by the specified factors. Does not
		change the image size.
		@see ImageProcessor#setInterpolate
		@see ImageProcessor#resize
	*/
	public abstract void scale(double xScale, double yScale);
	
	/** Creates a new ImageProcessor containing a scaled copy of this image or ROI.
		@see ij.process.ImageProcessor#setInterpolate
	*/
	public abstract ImageProcessor resize(int dstWidth, int dstHeight);
	
	/** Creates a new ImageProcessor containing a scaled copy 
		of this image or ROI, with the aspect ratio maintained. */
	public ImageProcessor resize(int dstWidth) {
		return resize(dstWidth, (int)(dstWidth*((double)roiHeight/roiWidth)));
	}

	/** Creates a new ImageProcessor containing a scaled copy of this image or ROI.
		@param dstWidth   Image width of the resulting ImageProcessor
		@param dstHeight  Image height of the resulting ImageProcessor
		@param useAverging  True means that the averaging occurs to avoid
			aliasing artifacts; the kernel shape for averaging is determined by 
			the interpolationMethod. False if subsampling without any averaging  
			should be used on downsizing.  Has no effect on upsizing.
		@ImageProcessor#setInterpolationMethod for setting the interpolation method
		@author Michael Schmid
	*/
	public ImageProcessor resize(int dstWidth, int dstHeight, boolean useAverging) {
		Rectangle r = getRoi();
		int rWidth = r.width;
		int rHeight = r.height;
		if ((dstWidth>=rWidth && dstHeight>=rHeight) || !useAverging)
			return resize(dstWidth, dstHeight);  //upsizing or downsizing without averaging
		else {  //downsizing with averaging in at least one direction: convert to float
			ImageProcessor ip2 = createProcessor(dstWidth, dstHeight);
			FloatProcessor fp = null;
			for (int channelNumber=0; channelNumber<getNChannels(); channelNumber++) {
				fp = toFloat(channelNumber, fp);
				fp.setInterpolationMethod(interpolationMethod);
				fp.setRoi(getRoi());
				FloatProcessor fp2 = fp.downsize(dstWidth, dstHeight);
				ip2.setPixels(channelNumber, fp2);
			}
			return ip2;
		}
	}

	/** Rotates the image or selection 'angle' degrees clockwise.
		@see ImageProcessor#setInterpolate
	*/
  	public abstract void rotate(double angle);
  		
	/**  Moves the image or selection vertically or horizontally by a specified 
	      number of pixels. Positive x values move the image or selection to the 
	      right, negative values move it to the left. Positive y values move the 
	      image or selection down, negative values move it up.
	*/
  	public void translate(double xOffset, double yOffset) {
  		ImageProcessor ip2 = this.duplicate();
		boolean integerOffsets = xOffset==(int)xOffset && yOffset==(int)yOffset;
  		if (integerOffsets || interpolationMethod==NONE) {
			for (int y=roiY; y<(roiY + roiHeight); y++) {
				for (int x=roiX; x<(roiX + roiWidth); x++)
					putPixel(x, y, ip2.getPixel(x-(int)xOffset, y-(int)yOffset));
			}
		} else {
			if (interpolationMethod==BICUBIC && (this instanceof ColorProcessor))
				((ColorProcessor)this).filterRGB(ColorProcessor.RGB_TRANSLATE, xOffset, yOffset);
			else {
				for (int y=roiY; y<(roiY + roiHeight); y++) {
					if (y%30==0) showProgress((double)(y-roiY)/roiHeight);
					for (int x=roiX; x<(roiX + roiWidth); x++)
						putPixel(x, y, ip2.getPixelInterpolated(x-xOffset, y-yOffset));
				}
				showProgress(1.0);
			}
		} 
  	}
  	
	/**
	* @deprecated
	* replaced by translate(x,y)
	*/
  	public void translate(int xOffset, int yOffset, boolean eraseBackground) {
		translate(xOffset, yOffset);
  	}

	/** Returns the histogram of the image or ROI. Returns
		a luminosity histogram for RGB images and null
		for float images. */
	public abstract int[] getHistogram();
	
	/** Erodes the image or ROI using a 3x3 maximum filter. Requires 8-bit or RGB image. */
	public abstract void erode();
	
	/** Dilates the image or ROI using a 3x3 minimum filter. Requires 8-bit or RGB image. */
	public abstract void dilate();
	
	/** For 16 and 32 bit processors, set 'lutAnimation' true
		to have createImage() use the cached 8-bit version
		of the image. */
	public void setLutAnimation(boolean lutAnimation) {
		this.lutAnimation = lutAnimation;
		newPixels = true;
		source = null;
	}
	
// BDZ - BEGIN CHANGES
	protected void resetPixels(Object pixels) {
// BDZ - END CHANGES
		if (pixels==null) {
			if (img!=null) {
				img.flush();
				img = null;
			}
			source = null;
		}
		newPixels = true;
		source = null;
	}

	/** Returns an 8-bit version of this image as a ByteProcessor. */
	public ImageProcessor convertToByte(boolean doScaling) {
		TypeConverter tc = new TypeConverter(this, doScaling);
		return tc.convertToByte();
	}

	/** Returns a 16-bit version of this image as a ShortProcessor. */
	public ImageProcessor convertToShort(boolean doScaling) {
		TypeConverter tc = new TypeConverter(this, doScaling);
		return tc.convertToShort();
	}

	/** Returns a 32-bit float version of this image as a FloatProcessor. 
		For byte and short images, converts using a calibration function 
		if a calibration table has been set using setCalibrationTable(). */
	public ImageProcessor convertToFloat() {
		TypeConverter tc = new TypeConverter(this, false);
		return tc.convertToFloat(cTable);
	}
	
	/** Returns an RGB version of this image as a ColorProcessor. */
	public ImageProcessor convertToRGB() {
		TypeConverter tc = new TypeConverter(this, true);
		return tc.convertToRGB();
	}
	
	/** Performs a convolution operation using the specified kernel. 
	KernelWidth and kernelHeight must be odd. */
	public abstract void convolve(float[] kernel, int kernelWidth, int kernelHeight);
	
	/** Converts the image to binary using an automatically determined threshold.
		For byte and short images, converts to binary using an automatically determined
		threshold. For RGB images, converts each channel to binary. For
		float images, does nothing.
	*/
	public void autoThreshold() {
		threshold(getAutoThreshold());
	}

	/**	Returns a pixel value (threshold) that can be used to divide the image into objects 
		and background. It does this by taking a test threshold and computing the average 
		of the pixels at or below the threshold and pixels above. It then computes the average
		of those two, increments the threshold, and repeats the process. Incrementing stops 
		when the threshold is larger than the composite average. That is, threshold = (average 
		background + average objects)/2. This description was posted to the ImageJ mailing 
		list by Jordan Bevic. */
	public int getAutoThreshold() {
		return getAutoThreshold(getHistogram());
	}

	/**	This is a version of getAutoThreshold() that uses a histogram passed as an argument. */
	public int getAutoThreshold(int[] histogram) {
		int level;
		int maxValue = histogram.length - 1;
		double result, sum1, sum2, sum3, sum4;
		
		int count0 = histogram[0];
		histogram[0] = 0; //set to zero so erased areas aren't included
		int countMax = histogram[maxValue];
		histogram[maxValue] = 0;
		int min = 0;
		while ((histogram[min]==0) && (min<maxValue))
			min++;
		int max = maxValue;
		while ((histogram[max]==0) && (max>0))
			max--;
		if (min>=max) {
			histogram[0]= count0; histogram[maxValue]=countMax;
			level = histogram.length/2;
			return level;
		}
		
		int movingIndex = min;
		int inc = Math.max(max/40, 1);
		do {
			sum1=sum2=sum3=sum4=0.0;
			for (int i=min; i<=movingIndex; i++) {
				sum1 += (double)i*histogram[i];
				sum2 += histogram[i];
			}
			for (int i=(movingIndex+1); i<=max; i++) {
				sum3 += (double)i*histogram[i];
				sum4 += histogram[i];
			}			
			result = (sum1/sum2 + sum3/sum4)/2.0;
			movingIndex++;
			if (max>255 && (movingIndex%inc)==0)
				showProgress((double)(movingIndex)/max);
		} while ((movingIndex+1)<=result && movingIndex<max-1);
		
		showProgress(1.0);
		histogram[0]= count0; histogram[maxValue]=countMax;
		level = (int)Math.round(result);
		return level;
	}
	
	/** Updates the clipping rectangle used by lineTo(), drawLine(), drawDot() and drawPixel().
		The clipping rectangle is reset by passing a null argument or by calling resetRoi(). */
	public void setClipRect(Rectangle clipRect) {
		if (clipRect==null) {
			clipXMin=0; 
			clipXMax=width-1; 
			clipYMin=0; 
			clipYMax=height-1; 
		} else {
			clipXMin = clipRect.x; 
			clipXMax = clipRect.x + clipRect.width - 1; 
			clipYMin = clipRect.y; 
			clipYMax = clipRect.y + clipRect.height - 1; 
			if (clipXMin<0) clipXMin = 0;
			if (clipXMax>=width) clipXMax = width-1;
			if (clipYMin<0) clipYMin = 0;
			if (clipYMax>=height) clipYMax = height-1;
		}
	}
	
	protected String maskSizeError(ImageProcessor mask) {
		return "Mask size ("+mask.getWidth()+"x"+mask.getHeight()+") != ROI size ("+
			roiWidth+"x"+roiHeight+")";
	}
	
	protected SampleModel getIndexSampleModel() {
		if (sampleModel==null) {
			IndexColorModel icm = getDefaultColorModel();
			WritableRaster wr = icm.createCompatibleWritableRaster(1, 1);
			sampleModel = wr.getSampleModel();
			sampleModel = sampleModel.createCompatibleSampleModel(width, height);
		}
		return sampleModel;
	}

	/** Returns the default grayscale IndexColorModel. */
	public IndexColorModel getDefaultColorModel() {
		if (defaultColorModel==null) {
			byte[] r = new byte[256];
			byte[] g = new byte[256];
			byte[] b = new byte[256];
			for(int i=0; i<256; i++) {
				r[i]=(byte)i;
				g[i]=(byte)i;
				b[i]=(byte)i;
			}
			defaultColorModel = new IndexColorModel(8, 256, r, g, b);
		}
		return defaultColorModel;
	}
	
	/**	The getPixelsCopy() method returns a reference to the
		snapshot buffer if it is not null and 'snapshotCopyMode' is true.
		@see ImageProcessor#getPixelsCopy		
		@see ImageProcessor#snapshot		
	*/
	public void setSnapshotCopyMode(boolean b) {
		snapshotCopyMode = b;
	}
	
	/** Returns the number of color channels in the image. The color channels can be
	*  accessed by toFloat(channelNumber, fp) and written by setPixels(channelNumber, fp).
	* @return 1 for grayscale images, 3 for RGB images
	*/
	public int getNChannels() {
		return 1;   /* superseded by ColorProcessor */
	}
	
	/** Returns a FloatProcessor with the image or one color channel thereof.
	*  The roi and mask are also set for the FloatProcessor.
	*  @param channelNumber   Determines the color channel, 0=red, 1=green, 2=blue. Ignored for
	*                         grayscale images.
	*  @param fp     Here a FloatProcessor can be supplied, or null. The FloatProcessor
	*                         is overwritten when converting data (re-using its pixels array 
	*                         improves performance).
	*  @return A FloatProcessor with the converted image data of the color channel selected
	*/
	public abstract FloatProcessor toFloat(int channelNumber, FloatProcessor fp);
	
	/** Sets the pixels (of one color channel for RGB images) from a FloatProcessor.
	*  @param channelNumber   Determines the color channel, 0=red, 1=green, 2=blue.Ignored for
	*                         grayscale images.
	*  @param fp              The FloatProcessor where the image data are read from.
	*/
	public abstract void setPixels(int channelNumber, FloatProcessor fp);
	
	/** Returns the minimum possible pixel value. */
	public double minValue() {
		return 0.0;
	}

	/** Returns the maximum possible pixel value. */
	public double maxValue() {
		return 255.0;
	}
	
	/** CompositeImage calls this method to generate an updated color image. */
	public void updateComposite(int[] rgbPixels, int channel) {
		int redValue, greenValue, blueValue;
		int size = width*height;
		if (bytes==null || !lutAnimation)
			bytes = create8BitImage();
		if (cm==null)
			makeDefaultColorModel();
		if (reds==null || cm!=cm2)
			updateLutBytes();
		switch (channel) {
			case 1: // update red channel
				for (int i=0; i<size; i++)
					rgbPixels[i] = (rgbPixels[i]&0xff00ffff) | reds[bytes[i]&0xff];
				break;
			case 2: // update green channel
				for (int i=0; i<size; i++)
					rgbPixels[i] = (rgbPixels[i]&0xffff00ff) | greens[bytes[i]&0xff];
				break;
			case 3: // update blue channel
				for (int i=0; i<size; i++)
					rgbPixels[i] = (rgbPixels[i]&0xffffff00) | blues[bytes[i]&0xff];
				break;
			case 4: // get first channel
				for (int i=0; i<size; i++) {
					redValue = reds[bytes[i]&0xff];
					greenValue = greens[bytes[i]&0xff];
					blueValue = blues[bytes[i]&0xff];
					rgbPixels[i] = redValue | greenValue | blueValue;	
				}
				break;
			case 5: // merge next channel
				int pixel;
				for (int i=0; i<size; i++) {
					pixel = rgbPixels[i];
					redValue = (pixel&0x00ff0000) + reds[bytes[i]&0xff];
					greenValue = (pixel&0x0000ff00) + greens[bytes[i]&0xff];
					blueValue = (pixel&0x000000ff) + blues[bytes[i]&0xff];
					if (redValue>16711680) redValue = 16711680;
					if (greenValue>65280) greenValue = 65280;
					if (blueValue>255) blueValue = 255;
					rgbPixels[i] = redValue | greenValue | blueValue;	
				}
				break;
		}
		lutAnimation = false;
	}
	
	// method and variables used by updateComposite()
// BDZ - BEGIN CHANGES
	protected byte[]  create8BitImage() {return null;}
// BDZ - END CHANGES
	private byte[] bytes;
	private int[] reds, greens, blues;

	void updateLutBytes() {
		IndexColorModel icm = (IndexColorModel)cm;
		int mapSize = icm.getMapSize();
		if (reds==null || reds.length!=mapSize) {
			reds = new int[mapSize];
			greens = new int[mapSize];
			blues = new int[mapSize];
		}
		byte[] tmp = new byte[mapSize];
		icm.getReds(tmp);
		for (int i=0; i<mapSize; i++) reds[i] = (tmp[i]&0xff)<<16;
		icm.getGreens(tmp); 
		for (int i=0; i<mapSize; i++) greens[i] = (tmp[i]&0xff)<<8;
		icm.getBlues(tmp);
		for (int i=0; i<mapSize; i++) blues[i] = tmp[i]&0xff;
		cm2 = cm;
	}
	
	/** Sets the upper Over/Under threshold color. Can be called from a macro,
		e.g., call("ij.process.ImageProcessor.setOverColor", 0,255,255). */
	public static void setOverColor(int red, int green, int blue) {
		overRed=red; overGreen=green; overBlue=blue;
	}

	/** Set the lower Over/Under thresholding color. */
	public static void setUnderColor(int red, int green, int blue) {
		underRed=red; underGreen=green; underBlue=blue;
	}
	
	/** Returns 'true' if this is a binary image (8-bit-image with only 0 and 255). */
	public boolean isBinary() {
		return false;
	}

	/* This method is experimental and may be removed. */
	public static void setUseBicubic(boolean b) {
		useBicubic = b;
	}
	
	/* Calculates and returns statistics for this image. */
	public ImageStatistics getStatistics() {
		return ImageStatistics.getStatistics(this, 127, null);
	}
	
	/* Returns the PlugInFilter slice number. */
	public int getSliceNumber() {
		if (sliceNumber<1)
			return 1;
		else
			return sliceNumber;
	}

	/** PlugInFilterRunner uses this method to set the slice number. */
	public void setSliceNumber(int slice) {
		sliceNumber = slice;
	}
	
// BDZ - BEGIN ADDITIONS
	// NEW METHODS FOR IJ 2.0 SUPPORT
	
	public abstract int getBitDepth();
	public abstract double getBytesPerPixel();
	public abstract ImageStatistics getStatistics(int mOptions, Calibration cal);
	public abstract boolean isFloatingType();
	public abstract boolean isUnsignedType();
	public abstract double getMinimumAllowedValue();
	public abstract double getMaximumAllowedValue();
	public abstract String getTypeName();
	public abstract double getd(int x, int y);
	public abstract double getd(int index);

	protected boolean getSnapshotCopyMode()
	{
		return snapshotCopyMode;
	}

	protected void setFgColor(int color)
	{
		fgColor = color;
	}
	
	public int getFgColor()
	{
		return fgColor;
	}

	public Color getDrawingColor()
	{
		return drawingColor;
	}

	public void encodePixelInfo(int[] destination, int x, int y)
	{
		// DO NOTHING. Default behavior. This method should only ever get called on processors of type OTHER. They override this implementation.
// BDZ - END ADDITIONS
	}
}
