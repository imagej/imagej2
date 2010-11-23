package ijx.plugin;
import ijx.plugin.api.PlugIn;
import ijx.process.ImageProcessor;
import ijx.process.StackStatistics;
import ijx.process.ImageStatistics;
import ijx.gui.HistogramWindow;
import ijx.gui.dialog.YesNoCancelDialog;
import ijx.gui.dialog.GenericDialog;
import ijx.Macro;
import ijx.WindowManager;
import ijx.IJ;
import ij.*;


import ijx.util.Tools;
import ijx.plugin.api.PlugInFilter;
import ijx.plugin.frame.Recorder;
import ijx.measure.Calibration;
import ijx.IjxImagePlus;
import ijx.gui.IjxImageWindow;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;


/** This plugin implements the Analyze/Histogram command. */
public class Histogram implements PlugIn, TextListener {

	private static int nBins = 256;
	private static boolean useImageMinAndMax = true;
	private static double xMin, xMax;
	private static String yMax = "Auto";
	private static boolean stackHistogram;
	private static int imageID;	
	private Checkbox checkbox;
	private TextField minField, maxField;
	private String defaultMin, defaultMax;

 	public void run(String arg) {
 		IjxImagePlus imp = IJ.getImage();
 		int bitDepth = imp.getBitDepth();
 		if (bitDepth==32 || IJ.altKeyDown()) {
			IJ.setKeyUp(KeyEvent.VK_ALT);
 			if (!showDialog(imp))
 				return;
 		} else {
 			int flags = setupDialog(imp, 0);
 			if (flags==PlugInFilter.DONE) return;
			stackHistogram = flags==PlugInFilter.DOES_STACKS;
			Calibration cal = imp.getCalibration();
 			nBins = 256;
			if (stackHistogram && ((bitDepth==8&&!cal.calibrated())||bitDepth==24)) {
				xMin = 0.0;
				xMax = 256.0;
				useImageMinAndMax = false;
			} else
				useImageMinAndMax = true;
 			yMax = "Auto";
 		}
 		ImageStatistics stats = null;
 		if (useImageMinAndMax)
 			{xMin=0.0; xMax=0.0;}
 		int iyMax = (int)Tools.parseDouble(yMax, 0.0);
 		boolean customHistogram = (bitDepth==8||bitDepth==24) && (!(xMin==0.0&&xMax==0.0)||nBins!=256||iyMax>0);
 		WindowManager.setCenterNextImage(true);
 		if (stackHistogram || customHistogram) {
 			IjxImagePlus imp2 = imp;
 			if (customHistogram && !stackHistogram && imp.getStackSize()>1)
 				imp2 = IJ.getFactory().newImagePlus("Temp", imp.getProcessor());
			stats = new StackStatistics(imp2, nBins, xMin, xMax);
			stats.histYMax = iyMax;
			new HistogramWindow("Histogram of "+imp.getShortTitle(), imp, stats);
		} else
			new HistogramWindow("Histogram of "+imp.getShortTitle(), imp, nBins, xMin, xMax, iyMax);
	}
	
	boolean showDialog(IjxImagePlus imp) {
		ImageProcessor ip = imp.getProcessor();
		double min = ip.getMin();
		double max = ip.getMax();
		if (imp.getID()!=imageID || (min==xMin&&min==xMax))
			useImageMinAndMax = true;
		if (imp.getID()!=imageID || useImageMinAndMax) {
			xMin = min;
			xMax = max;
			Calibration cal = imp.getCalibration();
			xMin = cal.getCValue(xMin);
			xMax = cal.getCValue(xMax);
		}
		defaultMin = IJ.d2s(xMin,2);
		defaultMax = IJ.d2s(xMax,2);
		imageID = imp.getID();
		int stackSize = imp.getStackSize();
		GenericDialog gd = new GenericDialog("Histogram");
		gd.addNumericField("Bins:", HistogramWindow.nBins, 0);
		gd.addCheckbox("Use min/max or:", useImageMinAndMax);
		//gd.addMessage("          or");
		gd.addMessage("");
		int fwidth = 6;
		int nwidth = Math.max(IJ.d2s(xMin,2).length(), IJ.d2s(xMax,2).length());
		if (nwidth>fwidth) fwidth = nwidth;
		gd.addNumericField("X_Min:", xMin, 2, fwidth, null);
		gd.addNumericField("X_Max:", xMax, 2, fwidth, null);
		gd.addMessage(" ");
		gd.addStringField("Y_Max:", yMax, 6);
		if (stackSize>1)
			gd.addCheckbox("Stack Histogram", stackHistogram);
		Vector numbers = gd.getNumericFields();
		minField = (TextField)numbers.elementAt(1);
		minField.addTextListener(this);
		maxField = (TextField)numbers.elementAt(2);
		maxField.addTextListener(this);
		checkbox = (Checkbox)(gd.getCheckboxes().elementAt(0));
		gd.showDialog();
		if (gd.wasCanceled())
			return false;			
		nBins = (int)gd.getNextNumber();
		if (nBins>=2 && nBins<=1000)
			HistogramWindow.nBins = nBins;
		useImageMinAndMax = gd.getNextBoolean();
		xMin = gd.getNextNumber();
		xMax = gd.getNextNumber();
		yMax = gd.getNextString();
		stackHistogram = (stackSize>1)?gd.getNextBoolean():false;
		IJ.register(Histogram.class);
		return true;
	}

	public void textValueChanged(TextEvent e) {
		boolean rangeChanged = !defaultMin.equals(minField.getText())
			|| !defaultMax.equals(maxField.getText());
		if (rangeChanged)
			checkbox.setState(false);
	}
	
	int setupDialog(IjxImagePlus imp, int flags) {
		int stackSize = imp.getStackSize();
		if (stackSize>1) {
			String macroOptions = Macro.getOptions();
			if (macroOptions!=null) {
				if (macroOptions.indexOf("stack ")>=0)
					return flags+PlugInFilter.DOES_STACKS;
				else
					return flags;
			}
			YesNoCancelDialog d = new YesNoCancelDialog(IJ.getTopComponentFrame(),
				"Histogram", "Include all "+stackSize+" images?");
			if (d.cancelPressed())
				return PlugInFilter.DONE;
			else if (d.yesPressed()) {
				if (Recorder.record)
					Recorder.recordOption("stack");
				return flags+PlugInFilter.DOES_STACKS;
			}
			if (Recorder.record)
				Recorder.recordOption("slice");
		}
		return flags;
	}

}
