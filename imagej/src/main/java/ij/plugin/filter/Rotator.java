package ij.plugin.filter;
import ij.*;
import ij.gui.*;
import ij.process.*;
import java.awt.*;
import java.awt.geom.*;


/** This plugin implements the Image/Rotate/Arbitrarily command. */
public class Rotator implements ExtendedPlugInFilter, DialogListener {
	private int flags = DOES_ALL|SUPPORTS_MASKING|PARALLELIZE_STACKS;
	private static double angle = 15.0;
	private static boolean fillWithBackground;
	private static boolean enlarge;
	private static int gridLines = 1;
	private ImagePlus imp;
	private int bitDepth;
	private boolean canEnlarge;
	private boolean isEnlarged;
	private GenericDialog gd;
	private PlugInFilterRunner pfr;
	private String[] methods = ImageProcessor.getInterpolationMethods();
	private static int interpolationMethod = ImageProcessor.BILINEAR;

	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		if (imp!=null) {
			bitDepth = imp.getBitDepth();
			Roi roi = imp.getRoi();
			Rectangle r = roi!=null?roi.getBounds():null;
			canEnlarge = r==null || (r.x==0&&r.y==0&&r.width==imp.getWidth()&&r.height==imp.getHeight());
		}
		return flags;
	}

	public void run(ImageProcessor ip) {
		if(enlarge && gd.wasOKed()) synchronized(this) {
			if (!isEnlarged) {
				enlargeCanvas();
				isEnlarged=true;
			}
		}
		if (isEnlarged) {	//enlarging may have made the ImageProcessor invalid, also for the parallel threads
			int slice = pfr.getSliceNumber();
			if (imp.getStackSize()==1)
				ip = imp.getProcessor();
			else
				ip = imp.getStack().getProcessor(slice);
		}
		ip.setInterpolationMethod(interpolationMethod);
		if (fillWithBackground) {
			Color bgc = Toolbar.getBackgroundColor();
			if (bitDepth==8)
				ip.setBackgroundValue(ip.getBestIndex(bgc));
			else if (bitDepth==24)
				ip.setBackgroundValue(bgc.getRGB());
		} else
			ip.setBackgroundValue(0);
		ip.rotate(angle);
		if (!gd.wasOKed())
			drawGridLines(gridLines);
		if (isEnlarged && imp.getStackSize()==1) {
			imp.changes = true;
			imp.updateAndDraw();
			Undo.setup(Undo.COMPOUND_FILTER_DONE, imp);
		}
	}

	void enlargeCanvas() {
		imp.unlock();
		if (imp.getStackSize()==1)
			Undo.setup(Undo.COMPOUND_FILTER, imp);
		IJ.run("Select All");
		IJ.run("Rotate...", "angle="+angle);
		Roi roi = imp.getRoi();
		Rectangle r = roi.getBounds();
		if (r.width<imp.getWidth()) r.width = imp.getWidth();
		if (r.height<imp.getHeight()) r.height = imp.getHeight();
		IJ.showStatus("Rotate: Enlarging...");
		IJ.run("Canvas Size...", "width="+r.width+" height="+r.height+" position=Center "+(fillWithBackground?"":"zero"));
		IJ.showStatus("Rotating...");
	}


	void drawGridLines(int lines) {
		ImageCanvas ic = imp.getCanvas();
		if (ic==null) return;
		if (lines==0) {ic.setDisplayList(null); return;}
		GeneralPath path = new GeneralPath();
		float width = imp.getWidth();
		float height = imp.getHeight();
		float xinc = width/lines;
		float yinc = height/lines;
		float xstart = xinc/2f;
		float ystart = yinc/2f;
		for (int i=0; i<lines; i++) {
			path.moveTo(xstart+xinc*i, 0f);
			path.lineTo(xstart+xinc*i, height);
			path.moveTo(0f, ystart+yinc*i);
			path.lineTo(width, ystart+yinc*i);
		}
		ic.setDisplayList(path, null, null);
	}
	
	public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr) {
		this.pfr = pfr;
		String macroOptions = Macro.getOptions();
		if (macroOptions!=null) {
			if (macroOptions.indexOf(" interpolate")!=-1)
				macroOptions.replaceAll(" interpolate", " interpolation=Bilinear");
			else if (macroOptions.indexOf(" interpolation=")==-1)
				macroOptions = macroOptions+" interpolation=None";
			Macro.setOptions(macroOptions);
		}
		gd = new GenericDialog("Rotate", IJ.getInstance());
		gd.addNumericField("Angle (degrees):", angle, (int)angle==angle?1:2);
		gd.addNumericField("Grid Lines:", gridLines, 0);
		gd.addChoice("Interpolation:", methods, methods[interpolationMethod]);
		if (bitDepth==8 || bitDepth==24)
			gd.addCheckbox("Fill with Background Color", fillWithBackground);
		if (canEnlarge)
			gd.addCheckbox("Enlarge Image to Fit Result", enlarge);
		else
			enlarge = false;
		gd.addPreviewCheckbox(pfr);
		gd.addDialogListener(this);
		gd.showDialog();
		drawGridLines(0);
		if (gd.wasCanceled()) {
			return DONE;
		}
		if (!enlarge)
			flags |= KEEP_PREVIEW;		// standard filter without enlarge
		else if (imp.getStackSize()==1)
			flags |= NO_CHANGES;			// undoable as a "compound filter"
		return IJ.setupDialog(imp, flags);
	}
	
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		angle = gd.getNextNumber();
		//only check for invalid input to "angle", don't care about gridLines
		if (gd.invalidNumber()) {
			if (gd.wasOKed()) IJ.error("Angle is invalid.");
			return false;
		}
		gridLines = (int)gd.getNextNumber();
		interpolationMethod = gd.getNextChoiceIndex();
		if (bitDepth==8 || bitDepth==24)
			fillWithBackground = gd.getNextBoolean();
		if (canEnlarge)
			enlarge = gd.getNextBoolean();
		return true;
	}

	public void setNPasses(int nPasses) {
	}

}

