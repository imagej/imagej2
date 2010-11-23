package ijx.plugin.filter;
import ijx.process.ImageProcessor;
import ijx.plugin.api.PlugInFilter;
import ijx.IJ;
import ij.*;

import ijx.IjxImagePlus;

/**
* @deprecated
* replaced by ij.plugin.Resizer
*/
public class Resizer implements PlugInFilter {
 
	public int setup(String arg, IjxImagePlus imp) {
		if (imp!=null)
			IJ.run(imp, "Resize...", "");
		return DONE;
	}

	public void run(ImageProcessor ip) {
	}

}
