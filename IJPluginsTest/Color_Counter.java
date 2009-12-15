import ij.*;
import ij.process.*;
import ij.plugin.filter.*;

/** This plugin counts the number of unique colors in an RGB image. Displays
    the pixels counts for each of the colors if there are no more than 64 colors.  */
public class Color_Counter implements PlugInFilter {
	ImagePlus imp;
	int colors;
	static final int MAX_COLORS = 16777216;
	int[] counts = new int[MAX_COLORS];
	int slice;

	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_RGB+NO_UNDO+NO_CHANGES+DOES_STACKS;
	}

	public void run(ImageProcessor ip) {
		int[] pixels = (int[])ip.getPixels();
		for (int i=0; i<pixels.length; i++)
			counts[pixels[i]&0xffffff]++;
		if (++slice==imp.getStackSize()) {
			for (int i=0; i<MAX_COLORS; i++) {
				if (counts[i]>0) colors++;
			}
			IJ.log("Unique colors: "+colors);
			if (colors<=64) {
				IJ.log("Counts");
				for (int i=0; i<MAX_COLORS; i++) {
					if (counts[i]>0)
						IJ.log("   "+Integer.toHexString(i)+": "+counts[i]);
				}
			}
		}
	}

}
