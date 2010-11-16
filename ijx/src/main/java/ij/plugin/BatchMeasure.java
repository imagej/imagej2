package ij.plugin;
import ij.IJ;
import ij.measure.Measurements;
import ij.plugin.filter.Analyzer;
import ijx.IjxImagePlus;
import java.io.*;

/** This plugin implements the File/Batch/Measure command, 
	which measures all the images in a user-specified folder. */
	public class BatchMeasure implements PlugIn {

	public void run(String arg) {
		String dir = IJ.getDirectory("Choose a Folder");
		if (dir==null) return;
		String[] list = (new File(dir)).list();
		if (list==null) return;
		Analyzer.setMeasurement(Measurements.LABELS, true);
		for (int i=0; i<list.length; i++) {
			if (list[i].startsWith(".")) continue;
			String path = dir + list[i];
			IJ.showProgress(i+1, list.length);
			IjxImagePlus imp = !path.endsWith("/")?IJ.openImage(path):null;
			if (imp!=null) {
				IJ.run(imp, "Measure", "");
				imp.close();
			}
		}
	}

}
