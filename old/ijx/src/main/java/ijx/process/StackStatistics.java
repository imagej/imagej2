package ijx.process;
import ijx.roi.Roi;
import ijx.measure.Calibration;
import ijx.IJ;
import ijx.plugin.filter.Analyzer;
import ijx.IjxImagePlus;
import ijx.IjxImageStack;
import java.awt.*;

/** Statistics, including the histogram, of a stack. */
public class StackStatistics extends ImageStatistics {
	
	public StackStatistics(IjxImagePlus imp) {
		this(imp, 256, 0.0, 0.0);
	}

	public StackStatistics(IjxImagePlus imp, int nBins, double histMin, double histMax) {
		int bits = imp.getBitDepth();
		if ((bits==8||bits==24) && nBins==256 && histMin==0.0 && histMax==256.0)
			sum8BitHistograms(imp);
		else if (bits==16 && nBins==256 && histMin==0.0 && histMax==0.0 && !imp.getCalibration().calibrated())
			sum16BitHistograms(imp);
		else
			doCalculations(imp, nBins, histMin, histMax);
	}

    void doCalculations(IjxImagePlus imp,  int bins, double histogramMin, double histogramMax) {
        ImageProcessor ip = imp.getProcessor();
		boolean limitToThreshold = (Analyzer.getMeasurements()&LIMIT)!=0;
		double minThreshold = -Float.MAX_VALUE;
		double maxThreshold = Float.MAX_VALUE;
        Calibration cal = imp.getCalibration();
		if (limitToThreshold && ip.getMinThreshold()!=ImageProcessor.NO_THRESHOLD) {
			minThreshold=cal.getCValue(ip.getMinThreshold());
			maxThreshold=cal.getCValue(ip.getMaxThreshold());
		}
    	nBins = bins;
    	histMin = histogramMin;
    	histMax = histogramMax;
        IjxImageStack stack = imp.getStack();
        int size = stack.getSize();
        ip.setRoi(imp.getRoi());
        byte[] mask = ip.getMaskArray();
        float[] cTable = imp.getCalibration().getCTable();
        histogram = new int[nBins];
        double v;
        double sum = 0;
        double sum2 = 0;
        int width, height;
        int rx, ry, rw, rh;
        double pw, ph;
        
        width = ip.getWidth();
        height = ip.getHeight();
        Rectangle roi = ip.getRoi();
        if (roi != null) {
            rx = roi.x;
            ry = roi.y;
            rw = roi.width;
            rh = roi.height;
        } else {
            rx = 0;
            ry = 0;
            rw = width;
            rh = height;
        }
        
        pw = 1.0;
        ph = 1.0;
        roiX = rx*pw;
        roiY = ry*ph;
        roiWidth = rw*pw;
        roiHeight = rh*ph;
        boolean fixedRange = histMin!=0 || histMax!=0.0;
        
        // calculate min and max
		double roiMin = Double.MAX_VALUE;
		double roiMax = -Double.MAX_VALUE;
		for (int slice=1; slice<=size; slice++) {
			IJ.showStatus("Calculating stack histogram...");
			IJ.showProgress(slice/2, size);
			ip = stack.getProcessor(slice);
			//ip.setCalibrationTable(cTable);
			for (int y=ry, my=0; y<(ry+rh); y++, my++) {
				int i = y * width + rx;
				int mi = my * rw;
				for (int x=rx; x<(rx+rw); x++) {
					if (mask==null || mask[mi++]!=0) {
						v = ip.getPixelValue(x,y);
						if (v>=minThreshold && v<=maxThreshold) {
							if (v<roiMin) roiMin = v;
							if (v>roiMax) roiMax = v;
						}
					}
					i++;
				}
			}
		 }
		min = roiMin;
		max = roiMax;
		if (fixedRange) {
			if (min<histMin) min = histMin;
			if (max>histMax) max = histMax;
		} else {
			histMin = min; 
			histMax =  max;
		}
       
        // Generate histogram
        double scale = nBins/( histMax-histMin);
        pixelCount = 0;
        int index;
        boolean first = true;
        for (int slice=1; slice<=size; slice++) {
            IJ.showProgress(size/2+slice/2, size);
            ip = stack.getProcessor(slice);
            ip.setCalibrationTable(cTable);
            for (int y=ry, my=0; y<(ry+rh); y++, my++) {
                int i = y * width + rx;
                int mi = my * rw;
                for (int x=rx; x<(rx+rw); x++) {
                    if (mask==null || mask[mi++]!=0) {
                        v = ip.getPixelValue(x,y);
						if (v>=minThreshold && v<=maxThreshold && v>=histMin && v<=histMax) {
							longPixelCount++;
							sum += v;
							sum2 += v*v;
							index = (int)(scale*(v-histMin));
							if (index>=nBins)
								index = nBins-1;
							histogram[index]++;
						}
                    }
                    i++;
                }
            }
        }
        pixelCount = (int)longPixelCount;
        area = longPixelCount*pw*ph;
        mean = sum/longPixelCount;
        calculateStdDev(longPixelCount, sum, sum2);
        histMin = cal.getRawValue(histMin); 
        histMax =  cal.getRawValue(histMax);
        binSize = (histMax-histMin)/nBins;
        int bits = imp.getBitDepth();
        if (histMin==0.0 && histMax==256.0 && (bits==8||bits==24))
        	histMax = 255.0;
        dmode = getMode(cal);
        IJ.showStatus("");
        IJ.showProgress(1.0);
    }
    
	void sum8BitHistograms(IjxImagePlus imp) {
		Calibration cal = imp.getCalibration();
		boolean limitToThreshold = (Analyzer.getMeasurements()&LIMIT)!=0;
		int minThreshold = 0;
		int maxThreshold = 255;
		ImageProcessor ip = imp.getProcessor();
		if (limitToThreshold && ip.getMinThreshold()!=ImageProcessor.NO_THRESHOLD) {
			minThreshold = (int)ip.getMinThreshold();
			maxThreshold = (int)ip.getMaxThreshold();
		}
		IjxImageStack stack = imp.getStack();
		Roi roi = imp.getRoi();
		histogram = new int[256];
		int n = stack.getSize();
		for (int slice=1; slice<=n; slice++) {
			IJ.showProgress(slice, n);
			ip = stack.getProcessor(slice);
			if (roi!=null) ip.setRoi(roi);
			int[] hist = ip.getHistogram();
			for (int i=0; i<256; i++)
				histogram[i] += hist[i];
		}
		pw=1.0; ph=1.0;
		getRawStatistics(minThreshold, maxThreshold);
		getRawMinAndMax(minThreshold, maxThreshold);
		IJ.showStatus("");
		IJ.showProgress(1.0);
	}

	void sum16BitHistograms(IjxImagePlus imp) {
		Calibration cal = imp.getCalibration();
		boolean limitToThreshold = (Analyzer.getMeasurements()&LIMIT)!=0;
		int minThreshold = 0;
		int maxThreshold = 65535;
		ImageProcessor ip = imp.getProcessor();
		if (limitToThreshold && ip.getMinThreshold()!=ImageProcessor.NO_THRESHOLD) {
			minThreshold = (int)ip.getMinThreshold();
			maxThreshold = (int)ip.getMaxThreshold();
		}
		IjxImageStack stack = imp.getStack();
		Roi roi = imp.getRoi();
		int[] hist16 = new int[65536];
		int n = stack.getSize();
		for (int slice=1; slice<=n; slice++) {
			IJ.showProgress(slice, n);
			IJ.showStatus(slice+"/"+n);
			ip = stack.getProcessor(slice);
			if (roi!=null) ip.setRoi(roi);
			int[] hist = ip.getHistogram();
			for (int i=0; i<65536; i++)
				hist16[i] += hist[i];
		}
		pw=1.0; ph=1.0;
		getRaw16BitMinAndMax(hist16, minThreshold, maxThreshold);
		get16BitStatistics(hist16, (int)min, (int)max);
		IJ.showStatus("");
		IJ.showProgress(1.0);
	}
	
	void getRaw16BitMinAndMax(int[] hist, int minThreshold, int maxThreshold) {
		int min = minThreshold;
		while ((hist[min]==0) && (min<65535))
			min++;
		this.min = min;
		int max = maxThreshold;
		while ((hist[max]==0) && (max>0))
			max--;
		this.max = max;
	}

	void get16BitStatistics(int[] hist, int min, int max) {
		int count;
		double value;
		double sum = 0.0;
		double sum2 = 0.0;
		nBins = 256;
		histMin = min; 
		histMax = max;
		binSize = (histMax-histMin)/nBins;
		double scale = 1.0/binSize;
		int hMin = (int)histMin;
		histogram = new int[nBins]; // 256 bin histogram
		int index;
        maxCount = 0;
		for (int i=min; i<=max; i++) {
			count = hist[i];
			longPixelCount += count;
			value = i;
			sum += value*count;
			sum2 += (value*value)*count;
			index = (int)(scale*(i-hMin));
			if (index>=nBins)
				index = nBins-1;
			histogram[index] += count;
		}
		pixelCount = (int)longPixelCount;
		area = longPixelCount*pw*ph;
		mean = sum/longPixelCount;
		umean = mean;
		dmode = getMode(null);
		calculateStdDev(longPixelCount, sum, sum2);
	}

   double getMode(Calibration cal) {
        int count;
        maxCount = 0;
        for (int i=0; i<nBins; i++) {
            count = histogram[i];
            if (count > maxCount) {
                maxCount = count;
                mode = i;
            }
        }
        double tmode = histMin+mode*binSize;
        if (cal!=null) tmode = cal.getCValue(tmode);
       return tmode;
    }
    
}
