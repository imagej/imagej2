package ij.process;
import ij.measure.*;
import java.awt.*;

/** Statistics, including the histogram, of an image or selection. */
public class ImageStatistics implements Measurements {

	public int[] histogram;
	public int pixelCount;
	public int mode;
	public double dmode;
	public double area;
	public double min;
	public double max;
	public double mean;
	public double median;
	public double stdDev;
	public double skewness;
	public double kurtosis;
	public double xCentroid;
	public double yCentroid;
	public double xCenterOfMass;
	public double yCenterOfMass;
	public double roiX, roiY, roiWidth, roiHeight;
	/** Uncalibrated mean */
	public double umean;
	/** Length of major axis of fitted ellipse */
	public double major;
	/** Length of minor axis of fitted ellipse */
	public double minor;
	/** Angle in degrees of fitted ellipse */
	public double angle;
	/** 65536 element histogram (16-bit images only) */
	public int[] histogram16;
	public double areaFraction;
	/** Used internally by AnalyzeParticles */
	public int xstart, ystart;
	
	public double histMin;
	public double histMax;
	public int histYMax;
	public int maxCount;
	public int nBins = 256;
	public double binSize = 1.0;
	
	protected int width, height;
	protected int rx, ry, rw, rh;
	protected double pw, ph;
	protected Calibration cal;
	
	EllipseFitter ef;

	
	public static ImageStatistics getStatistics(ImageProcessor ip, int mOptions, Calibration cal) {
		if (ip instanceof ByteProcessor)
			return new ByteStatistics(ip, mOptions, cal);
		else if (ip instanceof ShortProcessor)
			return new ShortStatistics(ip, mOptions, cal);
		else if (ip instanceof ColorProcessor)
			return new ColorStatistics(ip, mOptions, cal);
		else
			return new FloatStatistics(ip, mOptions, cal);
	}

	void getRawMinAndMax(int minThreshold, int maxThreshold) {
		int min = minThreshold;
		while ((histogram[min] == 0) && (min < 255))
			min++;
		this.min = min;
		int max = maxThreshold;
		while ((histogram[max] == 0) && (max > 0))
			max--;
		this.max = max;
	}

	void getRawStatistics(int minThreshold, int maxThreshold) {
		int count;
		double value;
		double sum = 0.0;
		double sum2 = 0.0;
		
		for (int i=minThreshold; i<=maxThreshold; i++) {
			count = histogram[i];
			pixelCount += count;
			sum += (double)i*count;
			value = i;
			sum2 += (value*value)*count;
			if (count>maxCount) {
				maxCount = count;
				mode = i;
			}
		}
		area = pixelCount*pw*ph;
		mean = sum/pixelCount;
		umean = mean;
		dmode = mode;
		calculateStdDev(pixelCount, sum, sum2);
		histMin = 0.0;
		histMax = 255.0;
	}
	
	void calculateStdDev(int n, double sum, double sum2) {
		//ij.IJ.write("calculateStdDev: "+n+" "+sum+" "+sum2);
		if (n>0) {
			stdDev = (n*sum2-sum*sum)/n;
			if (stdDev>0.0)
				stdDev = Math.sqrt(stdDev/(n-1.0));
			else
				stdDev = 0.0;
		}
		else
			stdDev = 0.0;
	}
		
	void setup(ImageProcessor ip, Calibration cal) {
		width = ip.getWidth();
		height = ip.getHeight();
		this.cal = cal;
		Rectangle roi = ip.getRoi();
		if (roi != null) {
			rx = roi.x;
			ry = roi.y;
			rw = roi.width;
			rh = roi.height;
		}
		else {
			rx = 0;
			ry = 0;
			rw = width;
			rh = height;
		}
		
		if (cal!=null) {
			pw = cal.pixelWidth;
			ph = cal.pixelHeight;
		} else {
			pw = 1.0;
			ph = 1.0;
		}
		
		roiX = cal!=null?cal.getX(rx):rx;
		roiY = cal!=null?cal.getY(ry, height):ry;
		roiWidth = rw*pw;
		roiHeight = rh*ph;
	}
	
	void getCentroid(ImageProcessor ip) {
		byte[] mask = ip.getMaskArray();
		int count=0, mi;
		double xsum=0.0, ysum=0.0;
		for (int y=ry,my=0; y<(ry+rh); y++,my++) {
			mi = my*rw;
			for (int x=rx; x<(rx+rw); x++) {
				if (mask==null||mask[mi++]!=0) {
					count++;
					xsum += x;
					ysum += y;
				}
			}
		}
		xCentroid = xsum/count+0.5;
		yCentroid = ysum/count+0.5;
		if (cal!=null) {
			xCentroid = cal.getX(xCentroid);
			yCentroid = cal.getY(yCentroid, height);
		}
	}
	
	void fitEllipse(ImageProcessor ip) {
		if (ef==null)
			ef = new EllipseFitter();
		ef.fit(ip, this);
		double psize = (Math.abs(pw-ph)/pw)<.01?pw:0.0;
		major = ef.major*psize;
		minor = ef.minor*psize;
		angle = ef.angle;
		xCentroid = ef.xCenter;
		yCentroid = ef.yCenter;
		if (cal!=null) {
			xCentroid = cal.getX(xCentroid);
			yCentroid = cal.getY(yCentroid, height);
		}
		//if (ij.IJ.altKeyDown())
		//	ef.drawEllipse(ip);
	}
	
	public void drawEllipse(ImageProcessor ip) {
		if (ef!=null)
			ef.drawEllipse(ip);
	}
	
	void calculateMedian(int[] hist, int first, int last, Calibration cal) {
		//ij.IJ.log("calculateMedian: "+first+"  "+last+"  "+hist.length+"  "+pixelCount);
		double sum = 0;
		int i = first-1;
		double halfCount = pixelCount/2.0;
		do {
			sum += hist[++i];
		} while (sum<=halfCount && i<last);
		median = cal!=null?cal.getCValue(i):i;
	}
	
	void calculateAreaFraction(ImageProcessor ip, int[] hist) {
		int sum = 0;
		int total = 0;
		int t1 = (int)ip.getMinThreshold();
		int t2 = (int)ip.getMaxThreshold();
		if (t1==ImageProcessor.NO_THRESHOLD) {
			for (int i=0; i<hist.length; i++)
				total += hist[i];
			sum = total - hist[0];
		} else {
			for (int i=0; i<hist.length; i++) {
				if (i>=t1 && i<=t2)
					sum += hist[i];
				total += hist[i];
			}
		}
		areaFraction = sum*100.0/total;
	}
	
	public String toString() {
		return "stats[count="+pixelCount+", mean="+mean+", min="+min+", max="+max+"]";
	}

}
