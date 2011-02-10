package ijx.plugin;
import ijx.plugin.api.PlugIn;
import ijx.process.ImageProcessor;
import ijx.process.FloatProcessor;
import ijx.process.StackStatistics;
import ijx.process.ImageStatistics;
import ijx.process.ShortProcessor;
import ijx.gui.dialog.GenericDialog;
import ijx.roi.Roi;
import ijx.measure.Measurements;
import ijx.Undo;
import ijx.IJ;
import ijx.CompositeImage;


import ijx.IjxImagePlus;
import ijx.IjxImageStack;

/** Implements ImageJ's Process/Enhance Contrast command. */
public class ContrastEnhancer implements PlugIn, Measurements {

	int max, range;
	boolean classicEqualization;
	int stackSize;
	boolean updateSelectionOnly;
	boolean equalize, normalize, processStack, useStackHistogram, entireImage;
	static double saturated = 0.35;
	static boolean gEqualize, gNormalize;

	public void run(String arg) {
		IjxImagePlus imp = IJ.getImage();
		stackSize = imp.getStackSize();
		imp.trimProcessor();
		if (!showDialog(imp))
			return;
		Roi roi = imp.getRoi();
		if (roi!=null) roi.endPaste();
		if (stackSize==1)
			Undo.setup(Undo.TRANSFORM, imp);
		else
			Undo.reset();
		if (equalize)
			equalize(imp);
		else
			stretchHistogram(imp, saturated);
		if (equalize || normalize)
			imp.getProcessor().resetMinAndMax();
		imp.updateAndDraw();
	}

	boolean showDialog(IjxImagePlus imp) {
		equalize=gEqualize; normalize=gNormalize;
		int bitDepth = imp.getBitDepth();
		boolean composite = imp.isComposite();
		if (composite) stackSize = 1;
		Roi roi = imp.getRoi();
		boolean areaRoi = roi!=null && roi.isArea() && !composite;
		GenericDialog gd = new GenericDialog("Enhance Contrast");
		gd.addNumericField("Saturated Pixels:", saturated, 1, 4, "%");
		if (bitDepth!=24 && !composite)
			gd.addCheckbox("Normalize", normalize);
		if (areaRoi) {
			String label = bitDepth==24?"Update Entire Image":"Update All When Normalizing";
			gd.addCheckbox(label, entireImage);
		}
		gd.addCheckbox("Equalize Histogram", equalize);
		if (stackSize>1) {
			if (!composite)
				gd.addCheckbox("Normalize_All "+stackSize+" Slices", processStack);
			gd.addCheckbox("Use Stack Histogram", useStackHistogram);
		}
		gd.showDialog();
		if (gd.wasCanceled())
			return false;
		saturated = gd.getNextNumber();
		if (bitDepth!=24 && !composite)
			normalize = gd.getNextBoolean();
		else
			normalize = false;
		if (areaRoi) {
			entireImage = gd.getNextBoolean();
			updateSelectionOnly = !entireImage;
			if (!normalize && bitDepth!=24) 
				updateSelectionOnly = false;
		}
		equalize = gd.getNextBoolean();
		processStack = stackSize>1?gd.getNextBoolean():false;
		useStackHistogram = stackSize>1?gd.getNextBoolean():false;
		if (saturated<0.0) saturated = 0.0;
		if (saturated>100.0) saturated = 100;
		if (processStack)
			normalize = true;
		gEqualize=equalize; gNormalize=normalize;
		return true;
	}
 
	public void stretchHistogram(IjxImagePlus imp, double saturated) {
		ImageStatistics stats = null;
		if (useStackHistogram)
			stats = new StackStatistics(imp);
		if (processStack) {
			IjxImageStack stack = imp.getStack();
			for (int i=1; i<=stackSize; i++) {
				IJ.showProgress(i, stackSize);
				ImageProcessor ip = stack.getProcessor(i);
				ip.setRoi(imp.getRoi());
				if (!useStackHistogram)
					stats = ImageStatistics.getStatistics(ip, MIN_MAX, null);
				stretchHistogram(ip, saturated, stats);
			}
		} else {
			ImageProcessor ip = imp.getProcessor();
			ip.setRoi(imp.getRoi());
			if (stats==null)
				stats = ImageStatistics.getStatistics(ip, MIN_MAX, null);
			if (imp.isComposite())
				stretchCompositeImageHistogram((CompositeImage)imp, saturated, stats);
			else
				stretchHistogram(ip, saturated, stats);
		}
	}
	
	public void stretchHistogram(ImageProcessor ip, double saturated) {
		useStackHistogram = false;
		stretchHistogram(IJ.getFactory().newImagePlus("", ip), saturated);
	}

	public void stretchHistogram(ImageProcessor ip, double saturated, ImageStatistics stats) {
		int[] a = getMinAndMax(ip, saturated, stats);
		int hmin=a[0], hmax=a[1];
		//IJ.log(hmin+" "+hmax+" "+threshold);
		if (hmax>hmin) {
			double min = stats.histMin+hmin*stats.binSize;
			double max = stats.histMin+hmax*stats.binSize;
			if (!updateSelectionOnly)
				ip.resetRoi();
			if (normalize)
				normalize(ip, min, max);
			else {
				if (updateSelectionOnly) {
					ImageProcessor mask = ip.getMask();
					if (mask!=null) ip.snapshot();
					ip.setMinAndMax(min, max);
					if (mask!=null) ip.reset(mask);
				} else
					ip.setMinAndMax(min, max);
			}
		}
	}
	
	void stretchCompositeImageHistogram(CompositeImage imp, double saturated, ImageStatistics stats) {
		ImageProcessor ip = imp.getProcessor();
		int[] a = getMinAndMax(ip, saturated, stats);
		int hmin=a[0], hmax=a[1];
		if (hmax>hmin) {
			double min = stats.histMin+hmin*stats.binSize;
			double max = stats.histMin+hmax*stats.binSize;
			imp.setDisplayRange(min, max);
		}
		/*
		int channels = imp.getNChannels();b
		int channel = imp.getChannel();
		int slice = imp.getSlice();
		int frame = imp.getFrame();
		for (int c=1; c<=channels; c++) {
			imp.setPosition(c, slice, frame);
			ImageProcessor ip = imp.getProcessor();
			int[] a = getMinAndMax(ip, saturated, stats);
			int hmin=a[0], hmax=a[1];
			if (hmax>hmin) {
				double min = stats.histMin+hmin*stats.binSize;
				double max = stats.histMin+hmax*stats.binSize;
				imp.setDisplayRange(min, max);
			}
		}
		imp.setPosition(channel, slice, frame);
		*/
	}

	int[] getMinAndMax(ImageProcessor ip, double saturated, ImageStatistics stats) {
		int hmin, hmax;
		int threshold;
		int[] histogram = stats.histogram;		
		if (saturated>0.0)
			threshold = (int)(stats.pixelCount*saturated/200.0);
		else
			threshold = 0;
		int i = -1;
		boolean found = false;
		int count = 0;
		do {
			i++;
			count += histogram[i];
			found = count>threshold;
		} while (!found && i<255);
		hmin = i;
				
		i = 256;
		count = 0;
		do {
			i--;
			count += histogram[i];
			found = count>threshold;
			//IJ.log(i+" "+count+" "+found);
		} while (!found && i>0);
		hmax = i;
		int[] a = new int[2];
		a[0]=hmin; a[1]=hmax;
		return a;
	}
	
	void normalize(ImageProcessor ip, double min, double max) {
		int min2 = 0;
		int max2 = 255;
		int range = 256;
		if (ip instanceof ShortProcessor)
			{max2 = 65535; range=65536;}
		else if (ip instanceof FloatProcessor)
			normalizeFloat(ip, min, max);
		
		//double scale = range/max-min);
		int[] lut = new int[range];
		for (int i=0; i<range; i++) {
			if (i<=min)
				lut[i] = 0;
			else if (i>=max)
				lut[i] = max2;
			else
				lut[i] = (int)(((double)(i-min)/(max-min))*max2);
		}
		applyTable(ip, lut);
	}
	
	void applyTable(ImageProcessor ip, int[] lut) {
		if (updateSelectionOnly) {
			ImageProcessor mask = ip.getMask();
			if (mask!=null) ip.snapshot();
				ip.applyTable(lut);
			if (mask!=null) ip.reset(mask);
		} else
			ip.applyTable(lut);
	}

	void normalizeFloat(ImageProcessor ip, double min, double max) {
		double scale = max>min?1.0/(max-min):1.0;
		int size = ip.getWidth()*ip.getHeight();
		float[] pixels = (float[])ip.getPixels();
		double v;
		for (int i=0; i<size; i++) {
			v = pixels[i] - min;
			if (v<0.0) v = 0.0;
			v *= scale;
			if (v>1.0) v = 1.0;
			pixels[i] = (float)v;
		}
	}

	public void equalize(IjxImagePlus imp) {
		if (imp.getBitDepth()==32) {
			IJ.showMessage("Contrast Enhancer", "Equalization of 32-bit images not supported.");
			return;
		}
		classicEqualization = IJ.altKeyDown();
		if (processStack) {
			//int[] mask = imp.getMask();
			//Rectangle rect = imp.get
			IjxImageStack stack = imp.getStack();
			for (int i=1; i<=stackSize; i++) {
				IJ.showProgress(i, stackSize);
				ImageProcessor ip = stack.getProcessor(i);
				equalize(ip);
			}
		} else
			equalize(imp.getProcessor());
	}

	/**	
		Changes the tone curves of images. 
		It should bring up the detail in the flat regions of your image.
		Histogram Equalization can enhance meaningless detail and hide 
		important but small high-contrast features. This method uses a
		similar algorithm, but uses the square root of the histogram 
		values, so its effects are less extreme. Hold the alt key down 
		to use the standard histogram equalization algorithm.
		This code was contributed by Richard Kirk (rak@cre.canon.co.uk).
	*/ 	
	public void equalize(ImageProcessor ip) {
	
		int[] histogram = ip.getHistogram();
		ip.resetRoi();
		if (ip instanceof ShortProcessor) {	// Short
			max = 65535;
			range = 65535;
		} else { //bytes
			max = 255;
			range = 255;
		}
		
		double sum;
		
		sum = getWeightedValue(histogram, 0);
		for (int i=1; i<max; i++)
			sum += 2 * getWeightedValue(histogram, i);
		sum += getWeightedValue(histogram, max);
		
		double scale = range/sum;
		int[] lut = new int[range+1];
		
		lut[0] = 0;
		sum = getWeightedValue(histogram, 0);
		for (int i=1; i<max; i++) {
			double delta = getWeightedValue(histogram, i);
			sum += delta;
			lut[i] = (int)Math.round(sum*scale);
			sum += delta;
		}
		lut[max] = max;
		
		applyTable(ip, lut);
	}

	private double getWeightedValue(int[] histogram, int i) {
		int h = histogram[i];
		if (h<2 || classicEqualization) return (double)h;
		return Math.sqrt((double)(h));
	}
	
}
