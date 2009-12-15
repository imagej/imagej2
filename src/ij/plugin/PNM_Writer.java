/*
 This plugin saves grayscale images in PGM (portable graymap) format 
 and RGB images in PPM (portable pixmap) format. These formats, 
 along with PBM (portable bitmap), are collectively known as the 
 PNM format. More information can be found at 
 "http://en.wikipedia.org/wiki/Portable_Pixmap_file_format".
 
 @author Johannes Schindelin
 */
package ij.plugin;
import ijx.IjxImagePlus;
import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.io.SaveDialog;
import java.io.*;
import java.util.*;
import java.awt.image.*;

public class PNM_Writer implements PlugIn {

	public void run(String path) {
		IjxImagePlus img=IJ.getImage();
		boolean isGray = false;
		String extension = null;
		ImageProcessor ip = img.getProcessor();
		if (img.getBitDepth()==24)
			extension = ".pnm";
		else {
			if (img.getBitDepth()==8&& ip.isInvertedLut()) {
				ip = ip.duplicate();
				ip.invert();
			}
			ip = ip.convertToByte(true);
			isGray = true;
			extension = ".pgm";
		}
		String title=img.getTitle();
		int length=title.length();
		for(int i=2;i<5;i++)
			if(length>i+1 && title.charAt(length-i)=='.') {
				title=title.substring(0,length-i);
				break;
			}

		if (path==null || path.equals("")) {
			SaveDialog od = new SaveDialog("PNM Writer", title, extension);
			String dir=od.getDirectory();
			String name=od.getFileName();
			if(name==null)
				return;
			path = dir + name;
		}

		try {
			IJ.showStatus("Writing PNM "+path+"...");
			OutputStream fileOutput =
				new FileOutputStream(path);
			DataOutputStream output =
				new DataOutputStream(fileOutput);

			int w = img.getWidth(), h = img.getHeight();
			output.writeBytes((isGray ? "P5" : "P6")
					+ "\n# Written by ImageJ PNM Writer\n"
					+ w + " " + h + "\n255\n");
			if (isGray)
				output.write(
					(byte[])ip.getPixels(),
					0, w * h);
			else {
				byte[] pixels = new byte[w * h * 3];
				ColorProcessor proc =
					(ColorProcessor)ip;
				for (int j = 0; j < h; j++)
					for (int i = 0; i < w; i++) {
						int c = proc.getPixel(i, j);
						pixels[3 * (i + w * j) + 0] =
							(byte)((c & 0xff0000) >> 16);
						pixels[3 * (i + w * j) + 1] =
							(byte)((c & 0xff00) >> 8);
						pixels[3 * (i + w * j) + 2] =
							(byte)(c & 0xff);
					}
				output.write(pixels, 0, pixels.length);
			}
			output.flush();
		} catch(IOException e) {
			e.printStackTrace();
			IJ.error("Error writing file");
		}
		IJ.showStatus("");
	}

};

