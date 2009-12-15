package ij.plugin.filter;
import ijx.IjxImagePlus;
import ij.*;
import ij.gui.*;
import ij.process.*;
import java.awt.*;

/** Implements the Erode, Dilate, Open, Close, Outline, Skeletonize
	and Fill Holes commands in the Process/Binary submenu. 
	Gabriel Landini contributed the clever binary fill algorithm
	that fills holes in objects by filling the background.
*/
public class Binary implements IjxPlugInFilter {
	
	String arg;
	IjxImagePlus imp;
	static int iterations = 1;
	static int count = 1;
	int foreground, background;

	public int setup(String arg, IjxImagePlus imp) {
		this.arg = arg;
		this.imp = imp;
		IJ.register(Binary.class);
		
		if (arg.equals("options")) {
			showDialog();
			return DONE;
		}
		
		if (imp!=null && (imp.getType()==ImagePlus.GRAY8 || imp.getType()==ImagePlus.COLOR_256)) {
			ImageStatistics stats = imp.getStatistics();
			if (stats.histogram[0]+stats.histogram[255]!=stats.pixelCount) {
				IJ.error("8-bit binary (black and white only) image required.");
				return DONE;
			}
		}
		return IJ.setupDialog(imp, DOES_8G+DOES_8C+SUPPORTS_MASKING);
	}

	public void run(ImageProcessor ip) {
		foreground = Prefs.blackBackground?255:0;
		if (ip.isInvertedLut())
			foreground = 255 - foreground;
        background = 255 - foreground;
		ip.setSnapshotCopyMode(true);
		if (arg.equals("erode")) erode(ip);
		else if (arg.equals("dilate")) dilate(ip);
		else if (arg.equals("open")) open(ip);
		else if (arg.equals("close")) close(ip);
		else if (arg.equals("outline")) outline(ip);
		else if (arg.equals("skel")) skeletonize(ip);
		else if (arg.equals("fill")) fill(ip, foreground, background);
		ip.setSnapshotCopyMode(false);
	}

		
	void erode(ImageProcessor ip) {
		for (int i=0; i<iterations; i++)
			((ByteProcessor)ip).erode(count, background);
	}
	
	void dilate(ImageProcessor ip) {
		for (int i=0; i<iterations; i++)
			((ByteProcessor)ip).dilate(count, background);
	}
	

	void open(ImageProcessor ip) {
		for (int i=0; i<iterations; i++)
			((ByteProcessor)ip).erode(count, background);
		for (int i=0; i<iterations; i++)
			((ByteProcessor)ip).dilate(count, background);
	}
	
	void close(ImageProcessor ip) {
		for (int i=0; i<iterations; i++)
			((ByteProcessor)ip).dilate(count, background);
		for (int i=0; i<iterations; i++)
			((ByteProcessor)ip).erode(count, background);
	}
	
	void outline(ImageProcessor ip) {
		if (Prefs.blackBackground) ip.invert();
		((ByteProcessor)ip).outline();
		if (Prefs.blackBackground) ip.invert();
	}

	void skeletonize(ImageProcessor ip) {
		if (Prefs.blackBackground) ip.invert();
		boolean edgePixels = hasEdgePixels(ip);
		ImageProcessor ip2 = expand(ip, edgePixels);
		((ByteProcessor)ip2).skeletonize();
		ip = shrink(ip, ip2, edgePixels);
		if (Prefs.blackBackground) ip.invert();
	}
		
	void showDialog() {
		GenericDialog gd = new GenericDialog("Binary Options");
		gd.addNumericField("Iterations (1-25):", iterations, 0, 3, "");
		gd.addNumericField("Count (1-8):", count, 0, 3, "");
		gd.addCheckbox("Black Background", Prefs.blackBackground);
		gd.showDialog();
		if (gd.wasCanceled()) return;
		int n = (int)gd.getNextNumber();
		Prefs.blackBackground = gd.getNextBoolean();
		if (n>25) n = 25;
		if (n<1) n = 1;
		iterations = n;
		count = (int)gd.getNextNumber();
        if (count<1) count = 1;
        if (count>8) count = 8;
	}
	
	boolean hasEdgePixels(ImageProcessor ip) {
		int width = ip.getWidth();
		int height = ip.getHeight();
		boolean edgePixels = false;
		for (int x=0; x<width; x++) { // top edge
			if (ip.getPixel(x, 0)==foreground)
				edgePixels = true;
		}
		for (int x=0; x<width; x++) { // bottom edge
			if (ip.getPixel(x, height-1)==foreground)
				edgePixels = true;
		}
		for (int y=0; y<height; y++) { // left edge
			if (ip.getPixel(0, y)==foreground)
				edgePixels = true;
		}
		for (int y=0; y<height; y++) { // right edge
			if (ip.getPixel(height-1, y)==foreground)
				edgePixels = true;
		}
		return edgePixels;
	}
	
	ImageProcessor expand(ImageProcessor ip, boolean hasEdgePixels) {
		if (hasEdgePixels) {
			ImageProcessor ip2 = ip.createProcessor(ip.getWidth()+2, ip.getHeight()+2);
			if (foreground==0) {
				ip2.setColor(255);
				ip2.fill();
			}
			ip2.insert(ip, 1, 1);
            //new ImagePlus("ip2", ip2).show();
			return ip2;
		} else
			return ip;
	}

	ImageProcessor shrink(ImageProcessor ip, ImageProcessor ip2, boolean hasEdgePixels) {
		if (hasEdgePixels) {
			int width = ip.getWidth();
			int height = ip.getHeight();
			for (int y=0; y<height; y++)
				for (int x=0; x<width; x++)
					ip.putPixel(x, y, ip2.getPixel(x+1, y+1));
		}
		return ip;
	}

	// Binary fill by Gabriel Landini, G.Landini at bham.ac.uk
	// 21/May/2008
	void fill(ImageProcessor ip, int foreground, int background) {
		int width = ip.getWidth();
		int height = ip.getHeight();
		FloodFiller ff = new FloodFiller(ip);
		ip.setColor(127);
		for (int y=0; y<height; y++) {
			if (ip.getPixel(0,y)==background) ff.fill(0, y);
			if (ip.getPixel(width-1,y)==background) ff.fill(width-1, y);
		}
		for (int x=0; x<width; x++){
			if (ip.getPixel(x,0)==background) ff.fill(x, 0);
			if (ip.getPixel(x,height-1)==background) ff.fill(x, height-1);
		}
		byte[] pixels = (byte[])ip.getPixels();
		int n = width*height;
		for (int i=0; i<n; i++) {
		if (pixels[i]==127)
			pixels[i] = (byte)background;
		else
			pixels[i] = (byte)foreground;
		}
	}

}
