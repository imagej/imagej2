package ij.plugin.filter;
import java.awt.Color;
import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.plugin.filter.ParticleAnalyzer;
import ij.measure.*;
import ij.util.*;

/** Implements ImageJ's Analyze/Tools/Analyze Line Graph command. */
public class LineGraphAnalyzer implements PlugInFilter, Measurements  {
	ImagePlus imp;

	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_8G+NO_CHANGES;
	}

	public void run(ImageProcessor ip) {
		analyze(imp);
	}
	
	/** Uses ImageJ's particle analyzer to extract a set
		of coordinate pairs from a digitized line graph. */
	public void analyze(ImagePlus imp) {
		ByteProcessor ip = (ByteProcessor)imp.getProcessor();
		ImageProcessor ip2 = ip.crop();
		int width = ip2.getWidth();
		int height = ip2.getHeight();
		ip2.setColor(Color.white);
		for (int i=1; i<width; i+=2) {
			ip2.moveTo(i,0);
			ip2.lineTo(i,height-1);
		}
		ip2 = ip2.rotateRight();
		ImagePlus imp2 = imp.createImagePlus();
		ip2.setThreshold(ip.getMinThreshold(), ip.getMaxThreshold(), ImageProcessor.NO_LUT_UPDATE);
		imp2.setProcessor("Temp", ip2);
		Calibration cal = imp2.getCalibration();
		double pw = cal.pixelWidth;
		double ph = cal.pixelHeight;
		cal.pixelWidth = ph;
		cal.pixelHeight = pw;
		imp2.setCalibration(cal);
		if (IJ.altKeyDown()) imp2.show();
		
		int options = ParticleAnalyzer.SHOW_PROGRESS;
		int measurements = CENTROID;
		int minSize = 1;
		int maxSize = 999999;
		ResultsTable rt = new ResultsTable();
		ParticleAnalyzer pa = new ParticleAnalyzer(options, measurements, rt, minSize, maxSize);
		if (!pa.analyze(imp2))
			return;
		float[] y = rt.getColumn(ResultsTable.X_CENTROID);
		if (y==null)
			return;				
		float[] x = rt.getColumn(ResultsTable.Y_CENTROID);
		double[] a = Tools.getMinMax(x);
		double xmin=a[0], xmax=a[1];
		a = Tools.getMinMax(y);
		double ymin=a[0], ymax=a[1];
		
		String units = " ("+cal.getUnits()+")";
		String xLabel = "X"+units;
		String yLabel = "Y"+units;
		Plot plot = new Plot("Line Graph", xLabel, yLabel, x, y);
		plot.setLimits(0.0, width*ph, 0.0, height*pw);				
		plot.show();				
	}
	
}
