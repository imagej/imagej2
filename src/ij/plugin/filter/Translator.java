package ij.plugin.filter;
import ijx.IjxImagePlus;
import ij.*;
import ij.gui.*;
import ij.process.*;
import java.awt.*;
import java.awt.geom.*;


/** This plugin implements the Image/Translate command. */
public class Translator implements ExtendedPlugInFilter, DialogListener {
	private int flags = DOES_ALL|PARALLELIZE_STACKS;
	private static int xOffset = 15;
	private static int yOffset = 15;
	private IjxImagePlus imp;
	GenericDialog gd;
	PlugInFilterRunner pfr;

	public int setup(String arg, IjxImagePlus imp) {
		this.imp = imp;
		return flags;
	}

	public void run(ImageProcessor ip) {
		if (imp.getRoi()!=null && imp.getRoi().isArea())
			ip.translate(xOffset, yOffset, false);
		else
			ip.translate(xOffset, yOffset, true);
	}

	public int showDialog(IjxImagePlus imp, String command, PlugInFilterRunner pfr) {
		this.pfr = pfr;
		gd = new GenericDialog("Translate");
		gd.addNumericField("X Offset (pixels): ", xOffset, 0);
		gd.addNumericField("Y Offset (pixels): ", yOffset, 0);
		gd.addPreviewCheckbox(pfr);
		gd.addDialogListener(this);
		gd.showDialog();
		if (gd.wasCanceled())
			return DONE;
		return IJ.setupDialog(imp, flags);
	}
	
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		xOffset = (int)gd.getNextNumber();
		yOffset = (int)gd.getNextNumber();
		if (gd.invalidNumber()) {
			if (gd.wasOKed()) IJ.error("Offset is invalid.");
			return false;
		}
		return true;
	}

	public void setNPasses(int nPasses) {
	}

}

