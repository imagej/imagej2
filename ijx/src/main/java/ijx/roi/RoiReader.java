package ijx.roi;
import ijx.plugin.api.PlugIn;
import ijx.process.ImageProcessor;
import ijx.process.ByteProcessor;
import ijx.roi.Roi;
import ijx.io.OpenDialog;
import ijx.io.RoiDecoder;
import ijx.WindowManager;
import ijx.IJ;


import ijx.IjxImagePlus;
import java.io.*;
import java.awt.*;

/** Opens ImageJ, NIH Image and Scion Image for windows ROI outlines. 
	RoiDecoder.java has a description of the file format.
	@see ij.io.RoiDecoder
	@see ij.plugin.filter.RoiWriter
*/
public class RoiReader implements PlugIn {
	final int polygon=0, rect=1, oval=2, line=3,freeLine=4, segLine=5, noRoi=6,freehand=7, traced=8;

	public void run(String arg) {
		OpenDialog od = new OpenDialog("Open ROI...", arg);
		String dir = od.getDirectory();
		String name = od.getFileName();
		if (name==null)
			return;
		try {
			openRoi(dir, name);
		} catch (IOException e) {
			String msg = e.getMessage();
			if (msg==null || msg.equals(""))
				msg = ""+e;
			IJ.error("ROI Reader", msg);
		}
	}

	public void openRoi(String dir, String name) throws IOException {
		String path = dir+name;
		RoiDecoder rd = new RoiDecoder(path);
		Roi roi = rd.getRoi();
		Rectangle r = roi.getBounds();
		IjxImagePlus img = WindowManager.getCurrentImage();
		if (img==null || img.getWidth()<(r.x+r.width) || img.getHeight()<(r.y+r.height)) {
			ImageProcessor ip =  new ByteProcessor(r.x+r.width+10, r.y+r.height+10);
			ip.setColor(Color.white);
			ip.fill();
			img = IJ.getFactory().newImagePlus(name, ip);
			img.show();
		}
		img.setRoi(roi);
	}

}
