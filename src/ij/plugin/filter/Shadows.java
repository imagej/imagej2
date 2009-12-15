package ij.plugin.filter;
import ijx.IjxImagePlus;
import ij.*;
import ij.gui.*;
import ij.process.*;
import java.awt.*;

/** Implements the commands in the Process/Shadows submenu. */
public class Shadows implements IjxPlugInFilter {
	
	String arg;
	IjxImagePlus imp;

	public int setup(String arg, IjxImagePlus imp) {
		this.arg = arg;
		this.imp = imp;
		if (imp!=null && imp.getStackSize()>1 && arg.equals("demo"))
			{IJ.error("This demo does not work with stacks."); return DONE;}
		return IJ.setupDialog(imp, DOES_ALL+SUPPORTS_MASKING);
	}

	public void run(ImageProcessor ip) {
		if (arg.equals("demo")) {
			IJ.resetEscape();
			while (!IJ.escapePressed()) {
				north(ip); imp.updateAndDraw(); ip.reset();
				northeast(ip); imp.updateAndDraw(); ip.reset();
				east(ip); imp.updateAndDraw(); ip.reset();
				southeast(ip); imp.updateAndDraw(); ip.reset();
				south(ip); imp.updateAndDraw(); ip.reset();
				southwest(ip); imp.updateAndDraw(); ip.reset();
				west(ip); imp.updateAndDraw(); ip.reset();
				northwest(ip); imp.updateAndDraw(); ip.reset();
			}
		}
		else if (arg.equals("north")) north(ip);
		else if (arg.equals("northeast")) northeast(ip);
		else if (arg.equals("east")) east(ip);
		else if (arg.equals("southeast")) southeast(ip);
		else if (arg.equals("south")) south(ip);
		else if (arg.equals("southwest")) southwest(ip);
		else if (arg.equals("west")) west(ip);
		else if (arg.equals("northwest")) northwest(ip);

	}
		
		
		public void north(ImageProcessor ip) {
			int[] kernel = {1,2,1, 0,1,0,  -1,-2,-1};
			ip.convolve3x3(kernel);
		}

		public void south(ImageProcessor ip) {
			int[] kernel = {-1,-2,-1,  0,1,0,  1,2,1};
			ip.convolve3x3(kernel);
		}

		public void east(ImageProcessor ip) {
			int[] kernel = {-1,0,1,  -2,1,2,  -1,0,1};
			ip.convolve3x3(kernel);
		}

		public void west(ImageProcessor ip) {
			int[] kernel = {1,0,-1,  2,1,-2,  1,0,-1};
			ip.convolve3x3(kernel);
		}

		public void northwest(ImageProcessor ip) {
			int[] kernel = {2,1,0,  1,1,-1,  0,-1,-2};
			ip.convolve3x3(kernel);
		}

		public void southeast(ImageProcessor ip) {
			int[] kernel = {-2,-1,0,  -1,1,1,  0,1,2};
			ip.convolve3x3(kernel);
		}
		
		public void northeast(ImageProcessor ip) {
			int[] kernel = {0,1,2,  -1,1,1,  -2,-1,0};
			ip.convolve3x3(kernel);
		}
		
		public void southwest(ImageProcessor ip) {
			int[] kernel = {0,-1,-2,  1,1,-1,  2,1,0};
			ip.convolve3x3(kernel);
		}
}
