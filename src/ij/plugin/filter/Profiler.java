package ij.plugin.filter;
import ijx.IjxImagePlus;
import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import java.awt.event.*;

/** Implements the Process/Plot Profile and Edit/Options/Profile Plot Options commands. */
public class Profiler implements IjxPlugInFilter {

	IjxImagePlus imp;
	static boolean verticalProfile;

	public int setup(String arg, IjxImagePlus imp) {
		if (arg.equals("set"))
			{doOptions(); return DONE;}
		this.imp = imp;
		return DOES_ALL+NO_UNDO+NO_CHANGES+ROI_REQUIRED;
	}

	public void run(ImageProcessor ip) {
		boolean averageHorizontally = verticalProfile || IJ.altKeyDown();
		new ProfilePlot(imp, averageHorizontally).createWindow();
	}

	public void doOptions() {
		double ymin = ProfilePlot.getFixedMin();
		double ymax = ProfilePlot.getFixedMax();
		boolean fixedScale = ymin!=0.0 || ymax!=0.0;
		boolean wasFixedScale = fixedScale;
		
		GenericDialog gd = new GenericDialog("Profile Plot Options", IJ.getTopComponentFrame());
		gd.addNumericField("Width (pixels):", PlotWindow.plotWidth, 0);
		gd.addNumericField("Height (pixels):", PlotWindow.plotHeight, 0);
		gd.addNumericField("Minimum Y:", ymin, 2);
		gd.addNumericField("Maximum Y:", ymax, 2);
		gd.addCheckbox("Fixed Y-axis Scale", fixedScale);
		gd.addCheckbox("Do Not Save X-Values", !PlotWindow.saveXValues);
		gd.addCheckbox("Auto-close", PlotWindow.autoClose);
		gd.addCheckbox("Vertical Profile", verticalProfile);
		gd.addCheckbox("List Values", PlotWindow.listValues);
		gd.addCheckbox("Interpolate Line Profiles", PlotWindow.interpolate);
		gd.addCheckbox("Draw Grid Lines", !PlotWindow.noGridLines);
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		Dimension screen = IJ.getScreenSize();
		int w = (int)gd.getNextNumber();
		int h = (int)gd.getNextNumber();
		if (w<100) w = 100;
		if (w>screen.width-140) w = screen.width-140;
		if (h<50) h = 50;
		if (h>screen.height-300) h = screen.height-300;
		PlotWindow.plotWidth = w;
		PlotWindow.plotHeight = h;
		ymin = gd.getNextNumber();
		ymax = gd.getNextNumber();
		fixedScale = gd.getNextBoolean();
		PlotWindow.saveXValues = !gd.getNextBoolean();
		PlotWindow.autoClose = gd.getNextBoolean();
		verticalProfile = gd.getNextBoolean();
		PlotWindow.listValues = gd.getNextBoolean();
		PlotWindow.interpolate = gd.getNextBoolean();
		PlotWindow.noGridLines = !gd.getNextBoolean();
		if (!fixedScale && !wasFixedScale && (ymin!=0.0 || ymax!=0.0))
			fixedScale = true;
		if (!fixedScale) {
			ymin = 0.0;
			ymax = 0.0;
		} else if (ymin>ymax) {
			double tmp = ymin;
			ymin = ymax;
			ymax = tmp;
		}
		ProfilePlot.setMinAndMax(ymin, ymax);
		IJ.register(Profiler.class);
	}
		
}

