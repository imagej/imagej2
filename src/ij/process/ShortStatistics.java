package ij.process;
import ij.measure.Calibration;

/** 16-bit image statistics, including histogram. */
public class ShortStatistics extends ImageStatistics {

	/** Construct an ImageStatistics object from a ShortProcessor
		using the standard measurement options (area, mean,
		mode, min and max). */
	public ShortStatistics(ImageProcessor ip) {
		this(ip, AREA+MEAN+MODE+MIN_MAX, null);
	}

	/** Constructs a ShortStatistics object from a ShortProcessor using
		the specified measurement options. The 'cal' argument, which
		can be null, is currently ignored. */
	public ShortStatistics(ImageProcessor ip, int mOptions, Calibration cal) {
		this.width = ip.getWidth();
		this.height = ip.getHeight();
		setup(ip, cal);
		nBins = 256;
		double minT = ip.getMinThreshold();
		int minThreshold,maxThreshold;
		if ((mOptions&LIMIT)==0 || minT==ImageProcessor.NO_THRESHOLD)
			{minThreshold=0; maxThreshold=65535;}
		else
			{minThreshold=(int)minT; maxThreshold=(int)ip.getMaxThreshold();}
		int[] hist = ip.getHistogram(); // 65536 bin histogram
		histogram16 =hist;
		float[] cTable = cal!=null?cal.getCTable():null;
		getRawMinAndMax(hist, minThreshold, maxThreshold);
		histMin = min;
		histMax = max;
		getStatistics(ip, hist, (int)min, (int)max, cTable);
		if ((mOptions&MODE)!=0)
			getMode();
		if ((mOptions&ELLIPSE)!=0 || (mOptions&SHAPE_DESCRIPTORS)!=0)
			fitEllipse(ip);
		else if ((mOptions&CENTROID)!=0)
			getCentroid(ip, minThreshold, maxThreshold);
		if ((mOptions&(CENTER_OF_MASS|SKEWNESS|KURTOSIS))!=0)
			calculateMoments(ip, minThreshold, maxThreshold, cTable);
		if ((mOptions&MIN_MAX)!=0 && cTable!=null)
			getCalibratedMinAndMax(hist, (int)min, (int)max, cTable);
		if ((mOptions&MEDIAN)!=0)
			calculateMedian(hist, minThreshold, maxThreshold, cal);
		if ((mOptions&AREA_FRACTION)!=0)
			calculateAreaFraction(ip, hist);
	}

	void getRawMinAndMax(int[] hist, int minThreshold, int maxThreshold) {
		int min = minThreshold;
		while ((hist[min]==0) && (min<65535))
			min++;
		this.min = min;
		int max = maxThreshold;
		while ((hist[max]==0) && (max>0))
			max--;
		this.max = max;
	}

	void getStatistics(ImageProcessor ip, int[] hist, int min, int max, float[] cTable) {
		int count;
		double value;
		double sum = 0.0;
		double sum2 = 0.0;
		nBins = ip.getHistogramSize();
		histMin = ip.getHistogramMin();
		histMax = ip.getHistogramMax();
		if (histMin==0.0 && histMax==0.0) {
			histMin = min; 
			histMax = max;
		} else {
			if (min<histMin) min = (int)histMin;
			if (max>histMax) max = (int)histMax;
		}
		binSize = (histMax-histMin)/nBins;
		double scale = 1.0/binSize;
		int hMin = (int)histMin;
		histogram = new int[nBins]; // 256 bin histogram
		int index;
        int maxCount = 0;
				
		for (int i=min; i<=max; i++) {
			count = hist[i];
            if (count>maxCount) {
                maxCount = count;
                dmode = i;
            }
			pixelCount += count;
			value = cTable==null?i:cTable[i];
			sum += value*count;
			sum2 += (value*value)*count;
			index = (int)(scale*(i-hMin));
			if (index>=nBins)
				index = nBins-1;
			histogram[index] += count;
		}
		area = pixelCount*pw*ph;
		mean = sum/pixelCount;
		umean = mean;
		calculateStdDev(pixelCount, sum, sum2);
        if (cTable!=null)
        	dmode = cTable[(int)dmode];
	}
	
	void getMode() {
        int count;
        maxCount = 0;
        for (int i=0; i<nBins; i++) {
        	count = histogram[i];
            if (count > maxCount) {
                maxCount = count;
                mode = i;
            }
        }
		//ij.IJ.write("mode2: "+mode+" "+dmode+" "+maxCount);
	}


	void getCentroid(ImageProcessor ip, int minThreshold, int maxThreshold) {
		short[] pixels = (short[])ip.getPixels();
		byte[] mask = ip.getMaskArray();
		boolean limit = minThreshold>0 || maxThreshold<65535;
		int count=0, i, mi, v;
		double xsum=0.0, ysum=0.0;
		for (int y=ry,my=0; y<(ry+rh); y++,my++) {
			i = y*width + rx;
			mi = my*rw;
			for (int x=rx; x<(rx+rw); x++) {
				if (mask==null||mask[mi++]!=0) {
					if (limit) {
						v = pixels[i]&0xffff;
						if (v>=minThreshold&&v<=maxThreshold) {
							count++;
							xsum+=x;
							ysum+=y;
						}
					} else {
						count++;
						xsum+=x;
						ysum+=y;
					}
				}
				i++;
			}
		}
		xCentroid = xsum/count+0.5;
		yCentroid = ysum/count+0.5;
		if (cal!=null) {
			xCentroid = cal.getX(xCentroid);
			yCentroid = cal.getY(yCentroid, height);
		}
	}

	void calculateMoments(ImageProcessor ip,  int minThreshold, int maxThreshold, float[] cTable) {
		short[] pixels = (short[])ip.getPixels();
		byte[] mask = ip.getMaskArray();
		int i, mi, iv;
		double v, v2, sum1=0.0, sum2=0.0, sum3=0.0, sum4=0.0, xsum=0.0, ysum=0.0;
		for (int y=ry,my=0; y<(ry+rh); y++,my++) {
			i = y*width + rx;
			mi = my*rw;
			for (int x=rx; x<(rx+rw); x++) {
				if (mask==null || mask[mi++]!=0) {
					iv = pixels[i]&0xffff;
					if (iv>=minThreshold&&iv<=maxThreshold) {
						v = cTable!=null?cTable[iv]:iv;
						v2 = v*v;
						sum1 += v;
						sum2 += v2;
						sum3 += v*v2;
						sum4 += v2*v2;
						xsum += x*v;
						ysum += y*v;
					}
				}
				i++;
			}
		}
	    double mean2 = mean*mean;
	    double variance = sum2/pixelCount - mean2;
	    double sDeviation = Math.sqrt(variance);
	    skewness = ((sum3 - 3.0*mean*sum2)/pixelCount + 2.0*mean*mean2)/(variance*sDeviation);
	    kurtosis = (((sum4 - 4.0*mean*sum3 + 6.0*mean2*sum2)/pixelCount - 3.0*mean2*mean2)/(variance*variance)-3.0);
		xCenterOfMass = xsum/sum1+0.5;
		yCenterOfMass = ysum/sum1+0.5;
		if (cal!=null) {
			xCenterOfMass = cal.getX(xCenterOfMass);
			yCenterOfMass = cal.getY(yCenterOfMass, height);
		}
	}

	void getCalibratedMinAndMax(int[] hist, int minValue, int maxValue, float[] cTable) {
		min = Double.MAX_VALUE;
		max = -Double.MAX_VALUE;
		double v = 0.0;
		for (int i=minValue; i<=maxValue; i++) {
			if (hist[i]>0) {
				v = cTable[i];
				if (v<min) min = v;
				if (v>max) max = v;
			}
		}
	}

}
