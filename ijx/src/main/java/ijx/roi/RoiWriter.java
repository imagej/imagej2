package ijx.roi;

import ijx.process.ImageProcessor;
import ijx.io.RoiEncoder;
import ijx.io.SaveDialog;
import ijx.IJ;
import java.io.*;

import ijx.plugin.api.PlugInFilter;
import ijx.IjxImagePlus;

/** Saves the current ROI outline to a file. RoiDecoder.java 
	has a description of the file format.
	@see ij.io.RoiDecoder
	@see ij.plugin.RoiReader
*/
public class RoiWriter implements PlugInFilter {
	IjxImagePlus imp;

	public int setup(String arg, IjxImagePlus imp) {
		this.imp = imp;
		return DOES_ALL+ROI_REQUIRED+NO_CHANGES;
	}

	public void run(ImageProcessor ip) {
		try {
			saveRoi(imp);
		} catch (IOException e) {
			String msg = e.getMessage();
			if (msg==null || msg.equals(""))
				msg = ""+e;
			IJ.error("ROI Writer", msg);
		}
	}

	public void saveRoi(IjxImagePlus imp) throws IOException{
		Roi roi = imp.getRoi();
		if (roi==null)
			throw new IllegalArgumentException("ROI required");
		String name = roi.getName();
		if (name==null)
			name = imp.getTitle();
		SaveDialog sd = new SaveDialog("Save Selection...", name, ".roi");
		name = sd.getFileName();
		if (name == null)
			return;
		String dir = sd.getDirectory();
		RoiEncoder re = new RoiEncoder(dir+name);
		re.write(roi);
		if (name.endsWith(".roi"))
			name = name.substring(0, name.length()-4);
		roi.setName(name);
	}
	
}
