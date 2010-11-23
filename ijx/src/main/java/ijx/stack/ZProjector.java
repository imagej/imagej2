package ijx.stack; 
import ijx.plugin.filter.RGBStackSplitter;
import ijx.plugin.api.PlugIn;
import ijx.process.ImageProcessor;
import ijx.process.FloatProcessor;
import ijx.process.ByteProcessor;
import ijx.process.ShortProcessor;
import ijx.WindowManager;
import ijx.IJ;
import ijx.CompositeImage;
import ij.*; 
import ijx.gui.dialog.GenericDialog;

import ij.plugin.filter.*; 
import ijx.IjxImagePlus;
import ijx.IjxImageStack;
import java.lang.*; 

/** This plugin performs a z-projection of the input stack. Type of
    output image is same as type of input image.

    @author Patrick Kelly <phkelly@ucsd.edu> */

public class ZProjector implements PlugIn {
    public static final int AVG_METHOD = 0; 
    public static final int MAX_METHOD = 1;
    public static final int MIN_METHOD = 2;
    public static final int SUM_METHOD = 3;
	public static final int SD_METHOD = 4;
	public static final int MEDIAN_METHOD = 5;
	public static final String[] METHODS = 
		{"Average Intensity", "Max Intensity", "Min Intensity", "Sum Slices", "Standard Deviation", "Median"}; 
    private static int method = AVG_METHOD;

    private static final int BYTE_TYPE  = 0; 
    private static final int SHORT_TYPE = 1; 
    private static final int FLOAT_TYPE = 2;
    
    public static final String lutMessage =
    	"Stacks with inverter LUTs may not project correctly.\n"
    	+"To create a standard LUT, invert the stack (Edit/Invert)\n"
    	+"and invert the LUT (Image/Lookup Tables/Invert LUT)."; 

    /** Image to hold z-projection. */
    private IjxImagePlus projImage = null; 

    /** Image stack to project. */
    private IjxImagePlus imp = null; 

    /** Projection starts from this slice. */
    private int startSlice = 1;
    /** Projection ends at this slice. */
    private int stopSlice = 1;
    /** Project all time points? */
    private static boolean allTimeFrames = true;
    
    private String color = "";
    private boolean isHyperstack;
    private int increment = 1;
    private int sliceCount;

    public ZProjector() {
    }

    /** Construction of ZProjector with image to be projected. */
    public ZProjector(IjxImagePlus imp) {
		setImage(imp); 
    }

    /** Explicitly set image to be projected. This is useful if
	ZProjection_ object is to be used not as a plugin but as a
	stand alone processing object.  */
    public void setImage(IjxImagePlus imp) {
    	this.imp = imp; 
		startSlice = 1; 
		stopSlice = imp.getStackSize(); 
    }

    public void setStartSlice(int slice) {
		if(imp==null || slice < 1 || slice > imp.getStackSize())
	    	return; 
		startSlice = slice; 
    }

    public void setStopSlice(int slice) {
		if(imp==null || slice < 1 || slice > imp.getStackSize())
	    	return; 
		stopSlice = slice; 
    }

	public void setMethod(int projMethod){
		method = projMethod;
	}
    
    /** Retrieve results of most recent projection operation.*/
    public IjxImagePlus getProjection() {
		return projImage; 
    }

    public void run(String arg) {
		imp = WindowManager.getCurrentImage();
		int stackSize = imp.getStackSize();
		if(imp==null) {
	    	IJ.noImage(); 
	    	return; 
		}

		//  Make sure input image is a stack.
		if(stackSize==1) {
	    	IJ.error("ZProjection", "Stack required"); 
	    	return; 
		}
	
		//  Check for inverting LUT.
		if (imp.getProcessor().isInvertedLut()) {
	    	if (!IJ.showMessageWithCancel("ZProjection", lutMessage))
	    		return; 
		}

		// Set default bounds.
		int frames = imp.getNFrames();
		int slices = imp.getNSlices();
		isHyperstack = imp.isHyperStack()||( ijx.macro.Interpreter.isBatchMode()&&((frames>1&&frames<stackSize)||(slices>1&&slices<stackSize)));
		startSlice = 1; 
		if (isHyperstack) {
			int nSlices = imp.getNSlices();
			if (nSlices>1)
				stopSlice = nSlices;
			else
				stopSlice = imp.getNFrames();
		} else
			stopSlice  = stackSize;
			
		// Build control dialog
		GenericDialog gd = buildControlDialog(startSlice,stopSlice); 
		gd.showDialog(); 
		if(gd.wasCanceled()) return; 

		if (!imp.lock()) return;   // exit if in use
		long tstart = System.currentTimeMillis();
		setStartSlice((int)gd.getNextNumber()); 
		setStopSlice((int)gd.getNextNumber()); 
		method = gd.getNextChoiceIndex();
		if (isHyperstack) {
			allTimeFrames = imp.getNFrames()>1&&imp.getNSlices()>1?gd.getNextBoolean():false;
			doHyperStackProjection(allTimeFrames);
		} else if (imp.getType()==IjxImagePlus.COLOR_RGB)
			doRGBProjection();
		else 
			doProjection(); 

		if (arg.equals("") && projImage!=null) {
			long tstop = System.currentTimeMillis();
			projImage.setCalibration(imp.getCalibration()); 
	    	projImage.show("ZProjector: " +IJ.d2s((tstop-tstart)/1000.0,2)+" seconds");
		}

		imp.unlock(); 
		IJ.register(ZProjector.class);
		return; 
    }
    
    public void doRGBProjection() {
		doRGBProjection(imp.getStack());
    }

    private void doRGBProjection(IjxImageStack stack) {
        RGBStackSplitter splitter = new RGBStackSplitter();
        splitter.split(stack, true);
        IjxImagePlus red = IJ.getFactory().newImagePlus("Red", splitter.red);
        IjxImagePlus green = IJ.getFactory().newImagePlus("Green", splitter.green);
        IjxImagePlus blue = IJ.getFactory().newImagePlus("Blue", splitter.blue);
        imp.unlock();
        IjxImagePlus saveImp = imp;
        imp = red;
		color = "(red)"; doProjection();
		IjxImagePlus red2 = projImage;
        imp = green;
		color = "(green)"; doProjection();
		IjxImagePlus green2 = projImage;
        imp = blue;
		color = "(blue)"; doProjection();
		IjxImagePlus blue2 = projImage;
        int w = red2.getWidth(), h = red2.getHeight(), d = red2.getStackSize();
        RGBStackMerge merge = new RGBStackMerge();
        IjxImageStack stack2 = merge.mergeStacks(w, h, d, red2.getStack(), green2.getStack(), blue2.getStack(), true);
        imp = saveImp;
        projImage = IJ.getFactory().newImagePlus(makeTitle(), stack2);
    }

    /** Builds dialog to query users for projection parameters.
	@param start starting slice to display
	@param stop last slice */
    protected GenericDialog buildControlDialog(int start, int stop) {
		GenericDialog gd = new GenericDialog("ZProjection",IJ.getTopComponentFrame());
		gd.addNumericField("Start slice:",startSlice,0/*digits*/); 
		gd.addNumericField("Stop slice:",stopSlice,0/*digits*/);
		gd.addChoice("Projection Type", METHODS, METHODS[method]); 
		if (isHyperstack && imp.getNFrames()>1&& imp.getNSlices()>1)
			gd.addCheckbox("All Time Frames", allTimeFrames); 
		return gd; 
    }

    /** Performs actual projection using specified method. */
    public void doProjection() {
		if(imp==null)
			return;
		sliceCount = 0;
    	for (int slice=startSlice; slice<=stopSlice; slice+=increment)
    		sliceCount++;
		if (method==MEDIAN_METHOD) {
			projImage = doMedianProjection();
			return;
		} 
		
		// Create new float processor for projected pixels.
		FloatProcessor fp = new FloatProcessor(imp.getWidth(),imp.getHeight()); 
		IjxImageStack stack = imp.getStack();
		RayFunction rayFunc = getRayFunction(method, fp);
		if(IJ.debugMode==true) {
	    	IJ.log("\nProjecting stack from: "+startSlice
		     	+" to: "+stopSlice); 
		}

		// Determine type of input image. Explicit determination of
		// processor type is required for subsequent pixel
		// manipulation.  This approach is more efficient than the
		// more general use of ImageProcessor's getPixelValue and
		// putPixel methods.
		int ptype; 
		if(stack.getProcessor(1) instanceof ByteProcessor) ptype = BYTE_TYPE; 
		else if(stack.getProcessor(1) instanceof ShortProcessor) ptype = SHORT_TYPE; 
		else if(stack.getProcessor(1) instanceof FloatProcessor) ptype = FLOAT_TYPE; 
		else {
	    	IJ.error("ZProjector: Non-RGB stack required"); 
	    	return; 
		}

		// Do the projection.
		for(int n=startSlice; n<=stopSlice; n+=increment) {
	    	IJ.showStatus("ZProjection " + color +": " + n + "/" + stopSlice);
	    	IJ.showProgress(n-startSlice, stopSlice-startSlice);
	    	projectSlice(stack.getPixels(n), rayFunc, ptype);
		}

		// Finish up projection.
		if (method==SUM_METHOD) {
			fp.resetMinAndMax();
			projImage = IJ.getFactory().newImagePlus(makeTitle(),fp); 
		} else if (method==SD_METHOD) {
			rayFunc.postProcess();
			fp.resetMinAndMax();
			projImage = IJ.getFactory().newImagePlus(makeTitle(), fp); 
		} else {
			rayFunc.postProcess(); 
			projImage = makeOutputImage(imp, fp, ptype);
		}

		if(projImage==null)
	    	IJ.error("ZProjection - error computing projection.");
    }

	public void doHyperStackProjection(boolean allTimeFrames) {
		int start = startSlice;
		int stop = stopSlice;
		int firstFrame = 1;
		int lastFrame = imp.getNFrames();
		if (!allTimeFrames)
			firstFrame = lastFrame = imp.getFrame();
		IjxImageStack stack = IJ.getFactory().newImageStack(imp.getWidth(), imp.getHeight());
		int channels = imp.getNChannels();
		int slices = imp.getNSlices();
		if (slices==1) {
			slices = imp.getNFrames();
			firstFrame = lastFrame = 1;
		}
		int frames = lastFrame-firstFrame+1;
		increment = channels;
		boolean rgb = imp.getBitDepth()==24;
		for (int frame=firstFrame; frame<=lastFrame; frame++) {
			for (int channel=1; channel<=channels; channel++) {
				startSlice = (frame-1)*channels*slices + (start-1)*channels + channel;
				stopSlice = (frame-1)*channels*slices + (stop-1)*channels + channel;
				if (rgb)
					doHSRGBProjection(imp);
				else
					doProjection();
				stack.addSlice(null, projImage.getProcessor());
			}
		}
        projImage = IJ.getFactory().newImagePlus(makeTitle(), stack);
        projImage.setDimensions(channels, 1, frames);
        if (channels>1) {
           	projImage = new CompositeImage(projImage, 0);
        	((CompositeImage)projImage).copyLuts(imp);
      		if (method==SUM_METHOD || method==SD_METHOD)
        			((CompositeImage)projImage).resetDisplayRanges();
        }
        if (frames>1)
        	projImage.setOpenAsHyperStack(true);
	}
	
	private void doHSRGBProjection(IjxImagePlus rgbImp) {
		IjxImageStack stack = rgbImp.getStack();
		IjxImageStack stack2 = IJ.getFactory().newImageStack(stack.getWidth(), stack.getHeight());
		for (int i=startSlice; i<=stopSlice; i++)
			stack2.addSlice(null, stack.getProcessor(i));
		startSlice = 1;
		stopSlice = stack2.getSize();
		doRGBProjection(stack2);
	}

 	private RayFunction getRayFunction(int method, FloatProcessor fp) {
 		switch (method) {
 			case AVG_METHOD: case SUM_METHOD:
	    		return new AverageIntensity(fp, sliceCount); 
			case MAX_METHOD:
	    		return new MaxIntensity(fp);
	    	case MIN_METHOD:
	    		return new MinIntensity(fp); 
			case SD_METHOD:
	    		return new StandardDeviation(fp, sliceCount); 
			default:
	    		IJ.error("ZProjection - unknown method.");
	    		return null;
	    }
	}

    /** Generate output image whose type is same as input image. */
    private IjxImagePlus makeOutputImage(IjxImagePlus imp, FloatProcessor fp, int ptype) {
		int width = imp.getWidth(); 
		int height = imp.getHeight(); 
		float[] pixels = (float[])fp.getPixels(); 
		ImageProcessor oip=null; 

		// Create output image consistent w/ type of input image.
		int size = pixels.length;
		switch (ptype) {
			case BYTE_TYPE:
				oip = imp.getProcessor().createProcessor(width,height);
				byte[] pixels8 = (byte[])oip.getPixels(); 
				for(int i=0; i<size; i++)
					pixels8[i] = (byte)pixels[i];
				break;
			case SHORT_TYPE:
				oip = imp.getProcessor().createProcessor(width,height);
				short[] pixels16 = (short[])oip.getPixels(); 
				for(int i=0; i<size; i++)
					pixels16[i] = (short)pixels[i];
				break;
			case FLOAT_TYPE:
				oip = new FloatProcessor(width, height, pixels, null);
				break;
		}
	
		// Adjust for display.
	    // Calling this on non-ByteProcessors ensures image
	    // processor is set up to correctly display image.
	    oip.resetMinAndMax(); 

		// Create new image plus object. Don't use
		// IjxImagePlus.createImagePlus here because there may be
		// attributes of input image that are not appropriate for
		// projection.
		return IJ.getFactory().newImagePlus(makeTitle(), oip); 
    }

    /** Handles mechanics of projection by selecting appropriate pixel
	array type. We do this rather than using more general
	ImageProcessor getPixelValue() and putPixel() methods because
	direct manipulation of pixel arrays is much more efficient.  */
	private void projectSlice(Object pixelArray, RayFunction rayFunc, int ptype) {
		switch(ptype) {
			case BYTE_TYPE:
	    		rayFunc.projectSlice((byte[])pixelArray); 
	    		break; 
			case SHORT_TYPE:
	    		rayFunc.projectSlice((short[])pixelArray); 
	    		break; 
			case FLOAT_TYPE:
	    		rayFunc.projectSlice((float[])pixelArray); 
	    		break; 
		}
    }
    
    String makeTitle() {
    	String prefix = "AVG_";
 		switch (method) {
 			case SUM_METHOD: prefix = "SUM_"; break;
			case MAX_METHOD: prefix = "MAX_"; break;
	    	case MIN_METHOD: prefix = "MIN_"; break;
			case SD_METHOD:  prefix = "STD_"; break;
			case MEDIAN_METHOD:  prefix = "MED_"; break;
	    }
    	return WindowManager.makeUniqueName(prefix+imp.getTitle());
    }

	IjxImagePlus doMedianProjection() {
		IJ.showStatus("Calculating median...");
		IjxImageStack stack = imp.getStack();
		ImageProcessor[] slices = new ImageProcessor[sliceCount];
		int index = 0;
		for (int slice=startSlice; slice<=stopSlice; slice+=increment)
			slices[index++] = stack.getProcessor(slice);
		ImageProcessor ip2 = slices[0].duplicate();
		ip2 = ip2.convertToFloat();
		float[] values = new float[sliceCount];
		int width = ip2.getWidth();
		int height = ip2.getHeight();
		int inc = Math.max(height/30, 1);
		for (int y=0; y<height; y++) {
			if (y%inc==0) IJ.showProgress(y, height-1);
			for (int x=0; x<width; x++) {
				for (int i=0; i<sliceCount; i++)
				values[i] = slices[i].getPixelValue(x, y);
				ip2.putPixelValue(x, y, median(values));
			}
		}
		return IJ.getFactory().newImagePlus(makeTitle(), ip2);
	}

	float median(float[] a) {
		sort(a);
		int length = a.length;
		if ((length&1)==0)
			return (a[length/2-1]+a[length/2])/2f; // even
		else
			return a[length/2]; // odd
	}
	
	void sort(float[] a) {
		if (!alreadySorted(a))
			sort(a, 0, a.length - 1);
	}
	
	void sort(float[] a, int from, int to) {
		int i = from, j = to;
		float center = a[ (from + to) / 2 ];
		do {
			while ( i < to && center>a[i] ) i++;
			while ( j > from && center<a[j] ) j--;
			if (i < j) {float temp = a[i]; a[i] = a[j]; a[j] = temp; }
			if (i <= j) { i++; j--; }
		} while(i <= j);
		if (from < j) sort(a, from, j);
		if (i < to) sort(a,  i, to);
	}
		
	boolean alreadySorted(float[] a) {
		for ( int i=1; i<a.length; i++ ) {
			if (a[i]<a[i-1] )
			return false;
		}
		return true;
	}

/*
    IjxImagePlus doModeProjection() {
    	IJ.showStatus("Calculating mode...");
    	IjxImageStack stack = imp.getStack();
    	ImageProcessor[] slices = new ImageProcessor[sliceCount];
    	int index = 0;
    	for (int slice=startSlice; slice<=stopSlice; slice+=increment)
    		slices[index++] = stack.getProcessor(slice);
    	ImageProcessor ip2 = slices[0].duplicate();
    	ip2 = ip2.convertToShort(false);
    	short[] values = new short[sliceCount];
    	int width = ip2.getWidth();
    	int height = ip2.getHeight();
    	int inc = Math.max(height/30, 1);
    	for (int y=0; y<height; y++) {
    		if (y%inc==0) IJ.showProgress(y, height-1);
    		for (int x=0; x<width; x++) {
    			for (int i=0; i<sliceCount; i++)
    				values[i] = (short)slices[i].getPixel(x, y);
    			ip2.putPixel(x, y, mode(values));
    		}
    	}
  		return IJ.getFactory().newImagePlus(makeTitle(), ip2);
    }
    
    ImageProcessor modeProcessor=null;

	int mode(short[] a) {
		if (modeProcessor==null)
			modeProcessor = new ShortProcessor(a.length, 1, a, null);
		else
			modeProcessor.setPixels(a);
		int[] histogram = modeProcessor.getHistogram();
		int count, mode=0, maxCount=0;
		for (int i=0; i<histogram.length; i++) {
			count = histogram[i];
			if (count>maxCount) {
				maxCount = count;
				mode = i;
			}
		}
		return mode;
	}
*/

     /** Abstract class that specifies structure of ray
	function. Preprocessing should be done in derived class
	constructors.
	*/
    abstract class RayFunction {
		/** Do actual slice projection for specific data types. */
		public abstract void projectSlice(byte[] pixels);
		public abstract void projectSlice(short[] pixels);
		public abstract void projectSlice(float[] pixels);
		
		/** Perform any necessary post processing operations, e.g.
	    	averging values. */
		public void postProcess() {}

    } // end RayFunction


    /** Compute average intensity projection. */
    class AverageIntensity extends RayFunction {
     	private float[] fpixels;
 		private int num, len; 

		/** Constructor requires number of slices to be
	    	projected. This is used to determine average at each
	    	pixel. */
		public AverageIntensity(FloatProcessor fp, int num) {
			fpixels = (float[])fp.getPixels();
			len = fpixels.length;
	    	this.num = num;
		}

		public void projectSlice(byte[] pixels) {
	    	for(int i=0; i<len; i++)
				fpixels[i] += (pixels[i]&0xff); 
		}

		public void projectSlice(short[] pixels) {
	    	for(int i=0; i<len; i++)
				fpixels[i] += pixels[i]&0xffff;
		}

		public void projectSlice(float[] pixels) {
	    	for(int i=0; i<len; i++)
				fpixels[i] += pixels[i]; 
		}

		public void postProcess() {
			float fnum = num;
	    	for(int i=0; i<len; i++)
				fpixels[i] /= fnum;
		}

    } // end AverageIntensity


     /** Compute max intensity projection. */
    class MaxIntensity extends RayFunction {
    	private float[] fpixels;
 		private int len; 

		/** Simple constructor since no preprocessing is necessary. */
		public MaxIntensity(FloatProcessor fp) {
			fpixels = (float[])fp.getPixels();
			len = fpixels.length;
			for (int i=0; i<len; i++)
				fpixels[i] = -Float.MAX_VALUE;
		}

		public void projectSlice(byte[] pixels) {
	    	for(int i=0; i<len; i++) {
				if((pixels[i]&0xff)>fpixels[i])
		    		fpixels[i] = (pixels[i]&0xff); 
	    	}
		}

		public void projectSlice(short[] pixels) {
	    	for(int i=0; i<len; i++) {
				if((pixels[i]&0xffff)>fpixels[i])
		    		fpixels[i] = pixels[i]&0xffff;
	    	}
		}

		public void projectSlice(float[] pixels) {
	    	for(int i=0; i<len; i++) {
				if(pixels[i]>fpixels[i])
		    		fpixels[i] = pixels[i]; 
	    	}
		}
		
    } // end MaxIntensity

     /** Compute min intensity projection. */
    class MinIntensity extends RayFunction {
    	private float[] fpixels;
 		private int len; 

		/** Simple constructor since no preprocessing is necessary. */
		public MinIntensity(FloatProcessor fp) {
			fpixels = (float[])fp.getPixels();
			len = fpixels.length;
			for (int i=0; i<len; i++)
				fpixels[i] = Float.MAX_VALUE;
		}

		public void projectSlice(byte[] pixels) {
	    	for(int i=0; i<len; i++) {
				if((pixels[i]&0xff)<fpixels[i])
		    		fpixels[i] = (pixels[i]&0xff); 
	    	}
		}

		public void projectSlice(short[] pixels) {
	    	for(int i=0; i<len; i++) {
				if((pixels[i]&0xffff)<fpixels[i])
		    		fpixels[i] = pixels[i]&0xffff;
	    	}
		}

		public void projectSlice(float[] pixels) {
	    	for(int i=0; i<len; i++) {
				if(pixels[i]<fpixels[i])
		    		fpixels[i] = pixels[i]; 
	    	}
		}
		
    } // end MaxIntensity


    /** Compute standard deviation projection. */
    class StandardDeviation extends RayFunction {
    	private float[] result;
    	private double[] sum, sum2;
		private int num,len; 

		public StandardDeviation(FloatProcessor fp, int num) {
			result = (float[])fp.getPixels();
			len = result.length;
		    this.num = num;
			sum = new double[len];
			sum2 = new double[len];
		}
	
		public void projectSlice(byte[] pixels) {
			int v;
		    for(int i=0; i<len; i++) {
		    	v = pixels[i]&0xff;
				sum[i] += v;
				sum2[i] += v*v;
			} 
		}
	
		public void projectSlice(short[] pixels) {
			double v;
		    for(int i=0; i<len; i++) {
		    	v = pixels[i]&0xffff;
				sum[i] += v;
				sum2[i] += v*v;
			} 
		}
	
		public void projectSlice(float[] pixels) {
			double v;
		    for(int i=0; i<len; i++) {
		    	v = pixels[i];
				sum[i] += v;
				sum2[i] += v*v;
			} 
		}
	
		public void postProcess() {
			double stdDev;
			double n = num;
		    for(int i=0; i<len; i++) {
				if (num>1) {
					stdDev = (n*sum2[i]-sum[i]*sum[i])/n;
					if (stdDev>0.0)
						result[i] = (float)Math.sqrt(stdDev/(n-1.0));
					else
						result[i] = 0f;
				} else
					result[i] = 0f;
			}
		}

    } // end StandardDeviation

}  // end ZProjection


