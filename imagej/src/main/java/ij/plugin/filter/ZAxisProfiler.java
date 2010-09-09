package ij.plugin.filter;
import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.measure.*;
import ij.plugin.filter.Analyzer;
import ij.util.Tools;
import java.awt.Rectangle;

/** Implements the Image/Stack/Plot Z-axis Profile command. */
public class ZAxisProfiler implements PlugInFilter, Measurements  {

	ImagePlus imp;

	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_ALL+NO_CHANGES;
	}

	public void run(ImageProcessor ip) {
		if (imp.getStackSize()<2) {
			IJ.error("ZAxisProfiler", "This command requires a stack.");
			return;
		}
		Roi roi = imp.getRoi();
		if (roi!=null && roi.isLine()) {
			IJ.error("ZAxisProfiler", "This command does not work with line selections.");
			return;
		}
		double minThreshold = ip.getMinThreshold();
		double maxThreshold = ip.getMaxThreshold();
		float[] y = getZAxisProfile(roi, minThreshold, maxThreshold);
		if (y!=null) {
			float[] x = new float[y.length];
			for (int i=0; i<x.length; i++)
				x[i] = i+1;
			String title;
			if (roi!=null) {
				Rectangle r = imp.getRoi().getBounds();
				title = imp.getTitle()+"-"+r.x+"-"+r.y;
			} else
				title = imp.getTitle()+"-0-0";
			Plot plot = new Plot(title, "Slice", "Mean", x, y);
			double ymin = ProfilePlot.getFixedMin();
			double ymax= ProfilePlot.getFixedMax();
			if (!(ymin==0.0 && ymax==0.0)) {
				double[] a = Tools.getMinMax(x);
				double xmin=a[0]; double xmax=a[1];
				plot.setLimits(xmin, xmax, ymin, ymax);
			}
			plot.show();
		}			
	}
		
	float[] getZAxisProfile(Roi roi, double minThreshold, double maxThreshold) {
		ImageStack stack = imp.getStack();
		int size = stack.getSize();
		float[] values = new float[size];
		Calibration cal = imp.getCalibration();
		Analyzer analyzer = new Analyzer(imp);
		int measurements = analyzer.getMeasurements();
		boolean showResults = measurements!=0 && measurements!=LIMIT;
		boolean showingLabels = (measurements&LABELS)!=0 || (measurements&SLICE)!=0;
		measurements |= MEAN;
		if (showResults) {
			if (!analyzer.resetCounter())
				return null;
		}
		int current = imp.getCurrentSlice();
		for (int i=1; i<=size; i++) {
			if (showingLabels) imp.setSlice(i);
			ImageProcessor ip = stack.getProcessor(i);
			if (minThreshold!=ImageProcessor.NO_THRESHOLD)
				ip.setThreshold(minThreshold,maxThreshold,ImageProcessor.NO_LUT_UPDATE);
			ip.setRoi(roi);
			ImageStatistics stats = ImageStatistics.getStatistics(ip, measurements, cal);
			analyzer.saveResults(stats, roi);
			if (showResults)			
				analyzer.displayResults();
			values[i-1] = (float)stats.mean;
		}
		if (showingLabels) imp.setSlice(current);
		return values;
	}
	
}

