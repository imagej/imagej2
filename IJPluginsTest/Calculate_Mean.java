	/**
	This plugin demonstrates how to use a mask to tally the values
	and number of pixels within a non-rectangular selection.
	*/
	import ij.*;
	import ij.process.*;
	import ij.gui.*;
	import java.awt.*;
	import ij.plugin.*;
	
	public class Calculate_Mean implements PlugIn {
		public void run(String arg) {
			ImagePlus imp = IJ.getImage();
			Roi roi = imp.getRoi();
			if (roi!=null && !roi.isArea()) roi = null;
			ImageProcessor ip = imp.getProcessor();
			ImageProcessor mask = roi!=null?roi.getMask():null;
			Rectangle r = roi!=null?roi.getBounds():new Rectangle(0,0,ip.getWidth(),ip.getHeight());
			double sum = 0;
			int count = 0;
			for (int y=0; y<r.height; y++) {
				for (int x=0; x<r.width; x++) {
					if (mask==null||mask.getPixel(x,y)!=0) {
						count++;
						sum += ip.getPixelValue(x+r.x, y+r.y);
					}
				}
			}
			IJ.log("count: "+count);
			IJ.log("mean: "+IJ.d2s(sum/count,4));
		}
	}
