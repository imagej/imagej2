package ij.plugin;

import ijx.IjxImagePlus;
import java.awt.*;
import java.awt.image.*;
import java.util.Vector;
import java.io.*;
import ij.*;
import ij.process.*;
import ij.io.*;
import ij.gui.*;
import ijx.IjxImageStack;


/** Writes the XY coordinates and pixel values of all non-background pixels
	to a tab-delimited text file. Backround is assumed to be the value of
	the pixel in the upper left corner of the image. */
public class XYCoordinates implements PlugIn {

	static boolean processStack;
	static boolean invertY;
	static boolean suppress;

	public void run(String arg) {
		IjxImagePlus imp = IJ.getImage();
		ImageProcessor ip = imp.getProcessor();
		int width = imp.getWidth();
		int height = imp.getHeight();
		double background = ip.getPixelValue(0,0);
		String bg = " \n";
		boolean rgb = imp.getBitDepth()==24;
		if (rgb) {
			int c = ip.getPixel(0,0);
			int r = (c&0xff0000)>>16;
			int g = (c&0xff00)>>8;
			int b = c&0xff;
			bg = r+","+g+","+b;
		    bg = " \n    Background value: " + bg + "\n";
		}
		imp.killRoi();
		
		int slices = imp.getStackSize();
		String msg =
			"This plugin writes to a text file the XY coordinates and\n"
			+ "pixel value of all non-background pixels. Backround\n"
			+ "defaults to be the value of the pixel in the upper\n"
			+ "left corner of the image.\n"
			+ bg;
				
		GenericDialog gd = new GenericDialog("Save XY Coordinates");
		gd.addMessage(msg);
		int digits = (int)background==background?0:4;
		if (!rgb) {
			gd.setInsets(5, 35, 3);
			gd.addNumericField("Background Value:", background, digits);
		}
		gd.setInsets(10, 35, 0);
		gd.addCheckbox("Invert Y Coordinates", invertY);
		gd.setInsets(0, 35, 0);
		gd.addCheckbox("Suppress Log Output", suppress);
		if (slices>1) {
			gd.setInsets(0, 35, 0);
			gd.addCheckbox("Process all "+slices+" images", processStack);
		}
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		if (!rgb)
			background = gd.getNextNumber();
		invertY = gd.getNextBoolean();
		suppress = gd.getNextBoolean();
		if (slices>1)
			processStack = gd.getNextBoolean();
		else
			processStack = false;
		if (!processStack) slices = 1;

		SaveDialog sd = new SaveDialog("Save Coordinates as Text...", imp.getTitle(), ".txt");
		String name = sd.getFileName();
		if (name == null)
			return;
		String directory = sd.getDirectory();
		PrintWriter pw = null;
		try {
			FileOutputStream fos = new FileOutputStream(directory+name);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			pw = new PrintWriter(bos);
		}
		catch (IOException e) {
			IJ.write("" + e);
			return;
		}

		IJ.showStatus("Saving coordinates...");
		int count = 0;
		float v;
		int c,r,g,b;
		int type = imp.getType();
		IjxImageStack stack = imp.getStack();
		for (int z=0; z<slices; z++) {
			if (slices>1) ip = stack.getProcessor(z+1);
			String zstr = slices>1?z+"\t":"";
			for (int i=0; i<height; i++) {
				int y = invertY?i:height-1-i;
				for (int x=0; x<width; x++) {
					v = ip.getPixelValue(x,y);
					if (v!=background) {
						if (type==IjxImagePlus.GRAY32)
							pw.println(x+"\t"+(invertY?y:height-1-y)+"\t"+zstr+v);
						else if (rgb) {
							c = ip.getPixel(x,y);
							r = (c&0xff0000)>>16;
							g = (c&0xff00)>>8;
							b = c&0xff;
							pw.println(x+"\t"+(invertY?y:height-1-y)+"\t"+zstr+r+"\t"+g+"\t"+b);
						} else
							pw.println(x+"\t"+(invertY?y:height-1-y)+"\t"+zstr+(int)v);
						count++;
					}
				} // x
				if (slices==1&&y%10==0) IJ.showProgress((double)(height-y)/height);
			} // y
			if (slices>1) IJ.showProgress(z+1, slices);
			String img = slices>1?"-"+(z+1):"";
			if (!suppress)
				IJ.log(imp.getTitle() + img+": " + count + " pixels (" + IJ.d2s(count*100.0/(width*height)) + "%)\n");
			count = 0;
		} // z
		IJ.showProgress(1.0);
		IJ.showStatus("");
		pw.close();
	}

}
