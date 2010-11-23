package ijx.plugin.filter;
import ijx.process.ImageProcessor;
import ijx.plugin.api.PlugInFilterRunner;
import ijx.plugin.api.ExtendedPlugInFilter;
import ijx.gui.dialog.DialogListener;
import ijx.gui.dialog.GenericDialog;
import ijx.IJ;



import ijx.IjxImagePlus;
import java.awt.*;


/** This plugin implements the Image/Translate command. */
public class Translator implements ExtendedPlugInFilter, DialogListener {
	private int flags = DOES_ALL|PARALLELIZE_STACKS;
	private static double xOffset = 15;
	private static double yOffset = 15;
	private IjxImagePlus imp;
	private GenericDialog gd;
	private PlugInFilterRunner pfr;
	private static int interpolationMethod = ImageProcessor.NONE;
	private String[] methods = ImageProcessor.getInterpolationMethods();

	public int setup(String arg, IjxImagePlus imp) {
		this.imp = imp;
		return flags;
	}

	public void run(ImageProcessor ip) {
		ip.setInterpolationMethod(interpolationMethod);
		ip.translate(xOffset, yOffset);
	}

	public int showDialog(IjxImagePlus imp, String command, PlugInFilterRunner pfr) {
		this.pfr = pfr;
		int digits = xOffset==(int)xOffset&&yOffset==(int)yOffset?1:3;
		gd = new GenericDialog("Translate");
		gd.addNumericField("X Offset (pixels): ", xOffset, digits, 8, "");
		gd.addNumericField("Y Offset (pixels): ", yOffset, digits, 8, "");
		gd.addChoice("Interpolation:", methods, methods[interpolationMethod]);
		gd.addPreviewCheckbox(pfr);
		gd.addDialogListener(this);
		gd.showDialog();
		if (gd.wasCanceled())
			return DONE;
		return IJ.setupDialog(imp, flags);
	}
	
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		xOffset = gd.getNextNumber();
		yOffset = gd.getNextNumber();
		interpolationMethod = gd.getNextChoiceIndex();
		if (gd.invalidNumber()) {
			if (gd.wasOKed()) IJ.error("Offset is invalid.");
			return false;
		}
		return true;
	}

	public void setNPasses(int nPasses) {
	}

}

