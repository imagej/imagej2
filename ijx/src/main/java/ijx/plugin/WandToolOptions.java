package ijx.plugin;
import ijx.plugin.api.PlugIn;
import ijx.gui.dialog.GenericDialog;
import ijx.WindowManager;
import ijx.IJ;
import ij.*;

import ijx.IjxImagePlus;

/** This plugin implements the Edit/Options/Wand Tool command. */
public class WandToolOptions implements PlugIn {
	private static final String[] modes = {"Legacy", "4-connected", "8-connected"};
	private static String mode = modes[0];
	private static double tolerance;

 	public void run(String arg) {
 		IjxImagePlus imp = WindowManager.getCurrentImage();
 		boolean showCheckbox = imp!=null && imp.getBitDepth()!=24 && WindowManager.getFrame("Threshold")==null;
		GenericDialog gd = new GenericDialog("Wand Tool");
		gd.addChoice("Mode: ", modes, mode);
		gd.addNumericField("Tolerance: ", tolerance, 1);
		if (showCheckbox)
			gd.addCheckbox("Enable Thresholding", false);
		gd.showDialog();
		if (gd.wasCanceled()) return;
		mode = gd.getNextChoice();
		tolerance = gd.getNextNumber();
		if (showCheckbox) {
			if (gd.getNextBoolean()) {
				imp.killRoi();
				IJ.run("Threshold...");
			}
		}
	}
	
	public static String getMode() {
		return mode;
	}

	public static double getTolerance() {
		return tolerance;
	}

}
