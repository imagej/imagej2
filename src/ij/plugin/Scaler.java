package ij.plugin;
import ijx.IjxImagePlus;
import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.measure.*;
import ij.util.Tools;
import ijx.IjxImageStack;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/** This plugin implements the Edit/Scale command. */
public class Scaler implements PlugIn, TextListener, FocusListener {
    private IjxImagePlus imp;
    private static String xstr = "0.5";
    private static String ystr = "0.5";
    private static int newWidth, newHeight;
    private static boolean newWindow = true;
    private static boolean interpolate = true;
    private static boolean fillWithBackground;
    private static boolean processStack = true;
    private double xscale;
    private double yscale;
    private String title = "Untitled";
    private Vector fields;
    private double bgValue;
    private boolean constainAspectRatio = true;
    private TextField xField, yField, widthField, heightField;
    private Rectangle r;
    private Object fieldWithFocus;

	public void run(String arg) {
		imp = IJ.getImage();
		Roi roi = imp.getRoi();
		if (roi!=null && !roi.isArea())
			imp.killRoi(); // ignore any line selection
		ImageProcessor ip = imp.getProcessor();
		if (!showDialog(ip))
			return;
		if (ip.getWidth()>1 && ip.getHeight()>1)
			ip.setInterpolate(interpolate);
		else
			ip.setInterpolate(false);
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
	
	void createNewStack(IjxImagePlus imp, ImageProcessor ip) {
		Rectangle r = ip.getRoi();
		boolean crop = r.width!=imp.getWidth() || r.height!=imp.getHeight();
		int nSlices = imp.getStackSize();
	    IjxImageStack stack1 = imp.getStack();
	    IjxImageStack stack2 = IJ.getFactory().newImageStack(newWidth, newHeight);
 		ImageProcessor ip1, ip2;
 		boolean interp = interpolate;
 		if (imp.getWidth()==1 || imp.getHeight()==1)
 			interp = false;
		for (int i=1; i<=nSlices; i++) {
			IJ.showStatus("Scale: " + i + "/" + nSlices);
			ip1 = stack1.getProcessor(i);
			String label = stack1.getSliceLabel(i);
			if (crop) {
				ip1.setRoi(r);
				ip1 = ip1.crop();
			}
			ip1.setInterpolate(interp);
			ip2 = ip1.resize(newWidth, newHeight);
			if (ip2!=null)
				stack2.addSlice(label, ip2);
			IJ.showProgress(i, nSlices);
		}
		IjxImagePlus imp2 = imp.createImagePlus();
		imp2.setStack(title, stack2);
		Calibration cal = imp2.getCalibration();
		if (cal.scaled()) {
			cal.pixelWidth *= 1.0/xscale;
			cal.pixelHeight *= 1.0/yscale;
		}
		int[] dim = imp.getDimensions();
		imp2.setDimensions(dim[2], dim[3], dim[4]);
		IJ.showProgress(1.0);
		if (imp.isComposite()) {
			imp2 = new CompositeImage(imp2, 0);
			((CompositeImage)imp2).copyLuts(imp);
		}
		if (imp.isHyperStack())
			imp2.setOpenAsHyperStack(true);
		imp2.show();
		imp2.setChanged(true);
	}

	void scale(ImageProcessor ip) {
		if (newWindow) {
			Rectangle r = ip.getRoi();
			IjxImagePlus imp2 = imp.createImagePlus();
			imp2.setProcessor(title, ip.resize(newWidth, newHeight));
			Calibration cal = imp2.getCalibration();
			if (cal.scaled()) {
				cal.pixelWidth *= 1.0/xscale;
				cal.pixelHeight *= 1.0/yscale;
			}
			imp2.show();
			imp.trimProcessor();
			imp2.trimProcessor();
			imp2.setChanged(true);
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
			imp.setChanged(true);
		}
	}
	
	boolean showDialog(ImageProcessor ip) {
		int bitDepth = imp.getBitDepth();
		boolean isStack = imp.getStackSize()>1;
        r = ip.getRoi();
       	int width = newWidth;
		if (width==0) width = r.width;
        int height = (int)((double)width*r.height/r.width);
        xscale = Tools.parseDouble(xstr, 0.0);
        yscale = Tools.parseDouble(ystr, 0.0);
        if (xscale!=0.0 && yscale!=0.0) {
       		width = (int)(r.width*xscale);
        	height = (int)(r.height*yscale);
        } else {
        	xstr = "-";
        	ystr = "-";
        }
		GenericDialog gd = new GenericDialog("Scale");
		gd.addStringField("X Scale (0.05-25):", xstr);
		gd.addStringField("Y Scale (0.05-25):", ystr);
		gd.setInsets(5, 0, 5);
		gd.addStringField("Width (pixels):", ""+width);
		gd.addStringField("Height (pixels):", ""+height);
		fields = gd.getStringFields();
		for (int i=0; i<3; i++) {
			((TextField)fields.elementAt(i)).addTextListener(this);
            ((TextField)fields.elementAt(i)).addFocusListener(this);
        }
		xField = (TextField)fields.elementAt(0);
		yField = (TextField)fields.elementAt(1);
		widthField = (TextField)fields.elementAt(2);
		heightField = (TextField)fields.elementAt(3);
        fieldWithFocus = xField;
		gd.addCheckbox("Interpolate", interpolate);
		if (bitDepth==8 || bitDepth==24)
			gd.addCheckbox("Fill with Background Color", fillWithBackground);
		if (isStack)
			gd.addCheckbox("Process Entire Stack", processStack);
		gd.addCheckbox("Create New Window", newWindow);
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
        String wstr = gd.getNextString();
		newWidth = (int)Tools.parseDouble(wstr, 0);
		newHeight = (int)Tools.parseDouble(gd.getNextString(), 0);
		if (newHeight!=0 && (wstr.equals("-") || wstr.equals("0")))
                newWidth= (int)(newHeight*(double)r.width/r.height);
		if (newWidth==0 || newHeight==0) {
			IJ.error("Invalid width or height entered");
			return false;
		}
		if (xscale>25.0) xscale = 25.0;
		if (yscale>25.0) yscale = 25.0;
		if (xscale>0.0 && yscale>0.0) {
			newWidth = (int)(r.width*xscale);
			newHeight = (int)(r.height*yscale);
		}
		interpolate = gd.getNextBoolean();
		if (bitDepth==8 || bitDepth==24)
			fillWithBackground = gd.getNextBoolean();
		if (isStack)
			processStack = gd.getNextBoolean();
		newWindow = gd.getNextBoolean();
		if (!newWindow && xscale==0.0) {
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
		} else {
			if (bitDepth==8)
				bgValue = ip.isInvertedLut()?0.0:255.0; // white
			else if (bitDepth==24)
				bgValue = 0xffffffff; // white
		}
		return true;
	}

	public void textValueChanged(TextEvent e) {
        Object source = e.getSource();
        double newXScale = xscale;
        double newYScale = yscale;
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
        }
		xscale = newXScale;
		yscale = newYScale;
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
