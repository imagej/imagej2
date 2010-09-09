package ij.plugin;
import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.measure.*;
import ij.util.Tools;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/** This plugin implements the Image/Scale command. */
public class Scaler implements PlugIn, TextListener, FocusListener {
	private ImagePlus imp;
	private static String xstr = "0.5";
	private static String ystr = "0.5";
	private String zstr = "1.0";
	private static int newWidth, newHeight;
	private int newDepth;
	private static boolean newWindow = true;
	private static int interpolationMethod = ImageProcessor.BILINEAR;
	private String[] methods = ImageProcessor.getInterpolationMethods();
	private static boolean fillWithBackground;
	private static boolean processStack = true;
	private double xscale, yscale, zscale;
	private String title = "Untitled";
	private Vector fields;
	private double bgValue;
	private boolean constainAspectRatio = true;
	private TextField xField, yField, zField, widthField, heightField, depthField;
	private Rectangle r;
	private Object fieldWithFocus;
	private int oldDepth;

	public void run(String arg) {
		imp = IJ.getImage();
		Roi roi = imp.getRoi();
		if (roi!=null && !roi.isArea())
			imp.killRoi(); // ignore any line selection
		ImageProcessor ip = imp.getProcessor();
		if (!showDialog(ip))
			return;
		if (newDepth>0 && newDepth!=imp.getStackSize()) {
			newWindow = true;
			processStack = true;
		}
		if (ip.getWidth()>1 && ip.getHeight()>1)
			ip.setInterpolationMethod(interpolationMethod);
		else
			ip.setInterpolationMethod(ImageProcessor.NONE);
		ip.setBackgroundValue(bgValue);
		imp.startTiming();
		try {
			if (newWindow && imp.getStackSize()>1 && processStack)
				createNewStack(imp, ip);
			else
				scale(ip);
		}
		catch(OutOfMemoryError o) {
			IJ.outOfMemory("Scale");
		}
		IJ.showProgress(1.0);
	}
	
	void createNewStack(ImagePlus imp, ImageProcessor ip) {
		int nSlices = imp.getStackSize();
		int w=imp.getWidth(), h=imp.getHeight();
		ImagePlus imp2 = imp.createImagePlus();
		if (newWidth!=w || newHeight!=h) {
			Rectangle r = ip.getRoi();
			boolean crop = r.width!=imp.getWidth() || r.height!=imp.getHeight();
			ImageStack stack1 = imp.getStack();
			ImageStack stack2 = new ImageStack(newWidth, newHeight);
			ImageProcessor ip1, ip2;
			int method = interpolationMethod;
			if (w==1 || h==1)
				method = ImageProcessor.NONE;
			for (int i=1; i<=nSlices; i++) {
				IJ.showStatus("Scale: " + i + "/" + nSlices);
				ip1 = stack1.getProcessor(i);
				String label = stack1.getSliceLabel(i);
				if (crop) {
					ip1.setRoi(r);
					ip1 = ip1.crop();
				}
				ip1.setInterpolationMethod(method);
				ip2 = ip1.resize(newWidth, newHeight);
				if (ip2!=null)
					stack2.addSlice(label, ip2);
				IJ.showProgress(i, nSlices);
			}
			imp2.setStack(title, stack2);
			Calibration cal = imp2.getCalibration();
			if (cal.scaled()) {
				cal.pixelWidth *= 1.0/xscale;
				cal.pixelHeight *= 1.0/yscale;
			}
			IJ.showProgress(1.0);
		} else
			imp2.setStack(title, imp.getStack());
		int[] dim = imp.getDimensions();
		imp2.setDimensions(dim[2], dim[3], dim[4]);
		if (imp.isComposite()) {
			imp2 = new CompositeImage(imp2, ((CompositeImage)imp).getMode());
			((CompositeImage)imp2).copyLuts(imp);
		}
		if (imp.isHyperStack())
			imp2.setOpenAsHyperStack(true);
		if (newDepth>0 && newDepth!=oldDepth)
			imp2 = (new Resizer()).zScale(imp2, newDepth, interpolationMethod);
		if (imp2!=null) {
			imp2.show();
			imp2.changes = true;
		}
	}

	void scale(ImageProcessor ip) {
		if (newWindow) {
			Rectangle r = ip.getRoi();
			ImagePlus imp2 = imp.createImagePlus();
			imp2.setProcessor(title, ip.resize(newWidth, newHeight));
			Calibration cal = imp2.getCalibration();
			if (cal.scaled()) {
				cal.pixelWidth *= 1.0/xscale;
				cal.pixelHeight *= 1.0/yscale;
			}
			imp2.show();
			imp.trimProcessor();
			imp2.trimProcessor();
			imp2.changes = true;
		} else {
			if (processStack && imp.getStackSize()>1) {
				Undo.reset();
				StackProcessor sp = new StackProcessor(imp.getStack(), ip);
				sp.scale(xscale, yscale, bgValue);
			} else {
				ip.snapshot();
				Undo.setup(Undo.FILTER, imp);
				ip.setSnapshotCopyMode(true);
				ip.scale(xscale, yscale);
				ip.setSnapshotCopyMode(false);
			}
			imp.killRoi();
			imp.updateAndDraw();
			imp.changes = true;
		}
	}
	
	boolean showDialog(ImageProcessor ip) {
		String macroOptions = Macro.getOptions();
		if (macroOptions!=null) {
			if (macroOptions.indexOf(" interpolate")!=-1)
				macroOptions.replaceAll(" interpolate", " interpolation=Bilinear");
			else if (macroOptions.indexOf(" interpolation=")==-1)
				macroOptions = macroOptions+" interpolation=None";
			Macro.setOptions(macroOptions);
		}
		int bitDepth = imp.getBitDepth();
		int stackSize = imp.getStackSize();
		boolean isStack = stackSize>1;
		oldDepth = stackSize;
		if (isStack) {
			xstr = "1.0";
			ystr = "1.0";
			zstr = "1.0";
		}
		r = ip.getRoi();
		int width = newWidth;
		if (width==0) width = r.width;
		int height = (int)((double)width*r.height/r.width);
		xscale = Tools.parseDouble(xstr, 0.0);
		yscale = Tools.parseDouble(ystr, 0.0);
		zscale = 1.0;
		if (xscale!=0.0 && yscale!=0.0) {
			width = (int)(r.width*xscale);
			height = (int)(r.height*yscale);
		} else {
			xstr = "-";
			ystr = "-";
		}
		GenericDialog gd = new GenericDialog("Scale");
		gd.addStringField("X Scale:", xstr);
		gd.addStringField("Y Scale:", ystr);
		if (isStack)
			gd.addStringField("Z Scale:", zstr);
		gd.setInsets(5, 0, 5);
		gd.addStringField("Width (pixels):", ""+width);
		gd.addStringField("Height (pixels):", ""+height);
		if (isStack) {
			String label = "Depth (images):";
			if (imp.isHyperStack()) {
				int slices = imp.getNSlices();
				int frames = imp.getNFrames();
				if (slices==1&&frames>1) {
					label = "Depth (frames):";
					oldDepth = frames;
				} else {
					label = "Depth (slices):";
					oldDepth = slices;
				}
			}
			gd.addStringField(label, ""+oldDepth);
		}
		fields = gd.getStringFields();
		for (int i=0; i<fields.size(); i++) {
			((TextField)fields.elementAt(i)).addTextListener(this);
			((TextField)fields.elementAt(i)).addFocusListener(this);
		}
		xField = (TextField)fields.elementAt(0);
		yField = (TextField)fields.elementAt(1);
		if (isStack) {
			zField = (TextField)fields.elementAt(2);
			widthField = (TextField)fields.elementAt(3);
			heightField = (TextField)fields.elementAt(4);
			depthField = (TextField)fields.elementAt(5);
		} else {
			widthField = (TextField)fields.elementAt(2);
			heightField = (TextField)fields.elementAt(3);
		}
		fieldWithFocus = xField;
		gd.addChoice("Interpolation:", methods, methods[interpolationMethod]);
		if (bitDepth==8 || bitDepth==24)
			gd.addCheckbox("Fill with Background Color", fillWithBackground);
		if (isStack)
			gd.addCheckbox("Process entire stack", processStack);
		gd.addCheckbox("Create new window", newWindow);
		title = WindowManager.getUniqueName(imp.getTitle());
		gd.setInsets(10, 0, 0);
		gd.addStringField("Title:", title, 12);
		gd.showDialog();
		if (gd.wasCanceled())
			return false;
		xstr = gd.getNextString();
		ystr = gd.getNextString();
		xscale = Tools.parseDouble(xstr, 0.0);
		yscale = Tools.parseDouble(ystr, 0.0);
		if (isStack) {
			zstr = gd.getNextString();
			zscale = Tools.parseDouble(ystr, 0.0);
		}
		String wstr = gd.getNextString();
		newWidth = (int)Tools.parseDouble(wstr, 0);
		newHeight = (int)Tools.parseDouble(gd.getNextString(), 0);
		if (newHeight!=0 && (wstr.equals("-") || wstr.equals("0")))
				newWidth= (int)(newHeight*(double)r.width/r.height);
		if (newWidth==0 || newHeight==0) {
			IJ.error("Scaler", "Width or height is 0");
			return false;
		}
		if (xscale>25.0) xscale = 25.0;
		if (yscale>25.0) yscale = 25.0;
		if (xscale>0.0 && yscale>0.0) {
			newWidth = (int)(r.width*xscale);
			newHeight = (int)(r.height*yscale);
		}
		if (isStack)
			newDepth = (int)Tools.parseDouble(gd.getNextString(), 0);
		interpolationMethod = gd.getNextChoiceIndex();
		if (bitDepth==8 || bitDepth==24)
			fillWithBackground = gd.getNextBoolean();
		if (isStack)
			processStack = gd.getNextBoolean();
		newWindow = gd.getNextBoolean();
		if (xscale==0.0) {
			xscale = (double)newWidth/r.width;
			yscale = (double)newHeight/r.height;
		}
		title = gd.getNextString();

		if (fillWithBackground) {
			Color bgc = Toolbar.getBackgroundColor();
			if (bitDepth==8)
				bgValue = ip.getBestIndex(bgc);
			else if (bitDepth==24)
				bgValue = bgc.getRGB();
		} else
			bgValue = 0.0;
		return true;
	}

	public void textValueChanged(TextEvent e) {
		Object source = e.getSource();
		double newXScale = xscale;
		double newYScale = yscale;
		double newZScale = zscale;
		if (source==xField && fieldWithFocus==xField) {
			String newXText = xField.getText();
			newXScale = Tools.parseDouble(newXText,0);
			if (newXScale==0) return;
			if (newXScale!=xscale) {
				int newWidth = (int)(newXScale*r.width);
				widthField.setText(""+newWidth);
				if (constainAspectRatio) {
					yField.setText(newXText);
					int newHeight = (int)(newXScale*r.height);
					heightField.setText(""+newHeight);
				}
			}
		} else if (source==yField && fieldWithFocus==yField) {
			String newYText = yField.getText();
			newYScale = Tools.parseDouble(newYText,0);
			if (newYScale==0) return;
			if (newYScale!=yscale) {
				int newHeight = (int)(newYScale*r.height);
				heightField.setText(""+newHeight);
			}
		} else if (source==zField && fieldWithFocus==zField) {
			String newZText = zField.getText();
			newZScale = Tools.parseDouble(newZText,0);
			if (newZScale==0) return;
			if (newZScale!=zscale) {
				int newDepth= (int)(newZScale*imp.getStackSize());
				depthField.setText(""+newDepth);
			}
		} else if (source==widthField && fieldWithFocus==widthField) {
			int newWidth = (int)Tools.parseDouble(widthField.getText(), 0.0);
			if (newWidth!=0) {
				int newHeight = (int)(newWidth*(double)r.height/r.width);
				heightField.setText(""+newHeight);
				xField.setText("-");
				yField.setText("-");
				newXScale = 0.0;
				newYScale = 0.0;
			}
       } else if (source==depthField && fieldWithFocus==depthField) {
            int newDepth = (int)Tools.parseDouble(depthField.getText(), 0.0);
            if (newDepth!=0) {
                zField.setText("-");
                newZScale = 0.0;
            }
        }
		xscale = newXScale;
		yscale = newYScale;
		zscale = newZScale;
	}

	public void focusGained(FocusEvent e) {
		fieldWithFocus = e.getSource();
		if (fieldWithFocus==widthField)
			constainAspectRatio = true;
		else if (fieldWithFocus==yField)
			constainAspectRatio = false;
	}

	public void focusLost(FocusEvent e) {}

}
