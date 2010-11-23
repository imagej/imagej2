package ijx.plugin.filter;
import ijx.process.ImageProcessor;
import ijx.plugin.api.PlugInFilter;
import ijx.IJ;
import java.awt.*;
import java.util.*;


import ijx.IjxImagePlus;

/** Implements ImageJ's Process/Noise/Salt and Pepper command. */
public class SaltAndPepper implements PlugInFilter {

	Random r = new Random();

	public int setup(String arg, IjxImagePlus imp) {
		return IJ.setupDialog(imp, DOES_8G+DOES_8C+SUPPORTS_MASKING);
	}

	public void run(ImageProcessor ip) {
		add(ip, 0.05);
	}

	public int rand(int min, int max) {
		return min + (int)(r.nextDouble()*(max-min));
	}

	public void add(ImageProcessor ip, double percent) {
		Rectangle roi = ip.getRoi();
		int n = (int)(percent*roi.width*roi.height);
		byte[] pixels = (byte[])ip.getPixels();
		int rx, ry;
		int width = ip.getWidth();
		int xmin = roi.x;
		int xmax = roi.x+roi.width-1;
		int ymin = roi.y;
		int ymax = roi.y+roi.height-1;
		
		for (int i=0; i<n/2; i++) {
			rx = rand(xmin, xmax);
			ry = rand(ymin, ymax);
			pixels[ry*width+rx] = (byte)255;
			rx = rand(xmin, xmax);
			ry = rand(ymin, ymax);
			pixels[ry*width+rx] = (byte)0;
		}
	}
}

