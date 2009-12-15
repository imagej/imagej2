package ij.process;
import ij.measure.Calibration;

/** 8-bit image statistics, including histogram. */
public class ByteStatistics extends ImageStatistics {

	/** Construct an ImageStatistics object from a ByteProcessor
		using the standard measurement options (area, mean,
		mode, min and max) and no calibration. */
	public ByteStatistics(ImageProcessor ip) {
		this(ip, AREA+MEAN+MODE+MIN_MAX, null);
	}

	/** Constructs a ByteStatistics object from a ByteProcessor using
		the specified measurement and calibration. */
	public ByteStatistics(ImageProcessor ip, int mOptions, Calibration cal) {
		ByteProcessor bp = (ByteProcessor)ip;
		histogram = bp.getHistogram();
		setup(ip, cal);
		double minT = ip.getMinThreshold();
		int minThreshold,maxThreshold;
		if ((mOptions&LIMIT)==0 || minT==ImageProcessor.NO_THRESHOLD)
			{minThreshold=0; maxThreshold=255;}
		else
			{minThreshold=(int)minT; maxThreshold=(int)ip.getMaxThreshold();}
		float[] cTable = cal!=null?cal.getCTable():null;
		if (cTable!=null)
			getCalibratedStatistics(minThreshold,maxThreshold,cTable);
		else
			getRawStatistics(minThreshold,maxThreshold);
		if ((mOptions&MIN_MAX)!=0) {
			if (cTable!=null)
				getCalibratedMinAndMax(minThreshold, maxThreshold, cTable);
			else
				getRawMinAndMax(minThreshold, maxThreshold);
		}
		if ((mOptions&ELLIPSE)!=0)
			fitEllipse(ip);
		else if ((mOptions&CENTROID)!=0)
			getCentroid(ip, minThreshold, maxThreshold);
		if ((mOptions&(CENTER_OF_MASS|SKEWNESS|KURTOSIS))!=0)
			calculateMoments(ip, minThreshold, maxThreshold, cTable);
		if ((mOptions&MEDIAN)!=0)
			calculateMedian(histogram, minThreshold, maxThreshold, cal);
		if ((mOptions&AREA_FRACTION)!=0)
			calculateAreaFraction(ip, histogram);
	}

	void getCalibratedStatistics(int minThreshold, int maxThreshold, float[] cTable) {
		int count;
		double value;
		double sum = 0;
		double sum2 = 0.0;
		int isum = 0;
		
		for (int i=minThreshold; i<=maxThreshold; i++) {
			count = histogram[i];
			value = cTable[i];
			if (count>0 && !Double.isNaN(value)) {
				pixelCount += count;
				sum += value*count;
				isum += i*count;
				sum2 += (value*value)*count;
				if (count>maxCount) {
					maxCount = count;
					mode = i;
				}
			}
		}
		area = pixelCount*pw*ph;
		mean = sum/pixelCount;
		umean = (double)isum/pixelCount;
		dmode = cTable[mode];
		calculateStdDev(pixelCount,sum,sum2);
		histMin = 0.0;
		histMax = 255.0;
	}
	
	void getCentroid(ImageProcessor ip, int minThreshold, int maxThreshold) {
		byte[] pixels = (byte[])ip.getPixels();
		byte[] mask = ip.getMaskArray();
		boolean limit = minThreshold>0 || maxThreshold<255;
		double xsum=0, ysum=0;
		int count=0,i,mi,v;
		for (int y=ry,my=0; y<(ry+rh); y++,my++) {
			i = y*width + rx;
			mi = my*rw;
			for (int x=rx; x<(rx+rw); x++) {
				if (mask==null||mask[mi++]!=0) {
					if (limit) {
						v = pixels[i]&255;
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
		byte[] pixels = (byte[])ip.getPixels();
		byte[] mask = ip.getMaskArray();
		int v, i, mi;
		double dv, dv2, sum1=0.0, sum2=0.0, sum3=0.0, sum4=0.0, xsum=0.0, ysum=0.0;
		for (int y=ry,my=0; y<(ry+rh); y++,my++) {
			i = y*width + rx;
			mi = my*rw;
			for (int x=rx; x<(rx+rw); x++) {
				if (mask==null || mask[mi++]!=0) {
					v = pixels[i]&255;
					if (v>=minThreshold&&v<=maxThreshold) {
						dv = ((cTable!=null)?cTable[v]:v)+Double.MIN_VALUE;
						dv2 = dv*dv;
						sum1 += dv;
						sum2 += dv2;
						sum3 += dv*dv2;
						sum4 += dv2*dv2;
						xsum += x*dv;
						ysum += y*dv;
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
	
	void getCalibratedMinAndMax(int minThreshold, int maxThreshold, float[] cTable) {
		if (pixelCount==0)
			{min=0.0; max=0.0; return;}
		min = Double.MAX_VALUE;
		max = -Double.MAX_VALUE;
		double v = 0.0;
		for (int i=minThreshold; i<=maxThreshold; i++) {
			if (histogram[i]>0) {
				v = cTable[i];
				if (v<min) min = v;
				if (v>max) max = v;
			}
		}
	}
	
}