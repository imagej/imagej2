package ijx.plugin;
import ijx.plugin.api.PlugIn;
import ijx.process.ImageProcessor;
import ijx.process.LUT;
import ijx.gui.dialog.GenericDialog;
import ijx.roi.Roi;
import ijx.WindowManager;
import ijx.IJ;
import ijx.CompositeImage;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;



import imagej.util.Tools;
import ijx.plugin.frame.Recorder;
import ijx.IjxImagePlus;
import ijx.IjxImageStack;

/** This plugin implements the Image/Duplicate command.
<pre>
   // test script
   img1 = IJ.getImage();
   img2 = new Duplicator().run(img1);
   //img2 = new Duplicator().run(img1,1,10);
   img2.show();
</pre>
*/
public class Duplicator implements PlugIn, TextListener {
	private static boolean duplicateStack;
	private boolean duplicateSubstack;
	private int first, last;
	private Checkbox checkbox;
	private TextField rangeField;

	public void run(String arg) {
		IjxImagePlus imp = IJ.getImage();
		int stackSize = imp.getStackSize();
		String title = imp.getTitle();
		String newTitle = WindowManager.getUniqueName(title);
		if (!IJ.altKeyDown()||stackSize>1)
			newTitle = showDialog(imp, "Duplicate...", "Title: ", newTitle);
		if (newTitle==null)
			return;
		IjxImagePlus imp2;
		Roi roi = imp.getRoi();
		if (duplicateSubstack && (first>1||last<stackSize))
			imp2 = run(imp, first, last);
		else if (duplicateStack || imp.getStackSize()==1)
			imp2 = run(imp);
		else
			imp2 = duplicateImage(imp);
		imp2.setTitle(newTitle);
		imp2.show();
		if (roi!=null && roi.isArea() && roi.getType()!=Roi.RECTANGLE)
			imp2.restoreRoi();
	}
                
	/** Returns a copy of the image, stack or hyperstack contained in the specified IjxImagePlus. */
	public IjxImagePlus run(IjxImagePlus imp) {
   		if (Recorder.record) Recorder.recordCall("imp = new Duplicator().run(imp);");
		if (imp.getStackSize()==1)
			return duplicateImage(imp);
		Rectangle rect = null;
		Roi roi = imp.getRoi();
		if (roi!=null && roi.isArea())
			rect = roi.getBounds();
		int width = rect!=null?rect.width:imp.getWidth();
		int height = rect!=null?rect.height:imp.getHeight();
		IjxImageStack stack = imp.getStack();
		IjxImageStack stack2 = IJ.getFactory().newImageStack(width, height, imp.getProcessor().getColorModel());
		for (int i=1; i<=stack.getSize(); i++) {
			ImageProcessor ip2 = stack.getProcessor(i);
			ip2.setRoi(rect);
			ip2 = ip2.crop();
			stack2.addSlice(stack.getSliceLabel(i), ip2);
		}
		IjxImagePlus imp2 = imp.createImagePlus();
		imp2.setStack("DUP_"+imp.getTitle(), stack2);
		int[] dim = imp.getDimensions();
		imp2.setDimensions(dim[2], dim[3], dim[4]);
		if (imp.isComposite()) {
			imp2 = new CompositeImage(imp2, 0);
			((CompositeImage)imp2).copyLuts(imp);
		}
		if (imp.isHyperStack())
			imp2.setOpenAsHyperStack(true);
		if (!imp.getHideOverlay())
			imp2.setOverlay(imp.getOverlay());
		return imp2;
	}
	
	IjxImagePlus duplicateImage(IjxImagePlus imp) {
		ImageProcessor ip2 = imp.getProcessor().crop();
		IjxImagePlus imp2 = imp.createImagePlus();
		imp2.setProcessor("DUP_"+imp.getTitle(), ip2);
		String info = (String)imp.getProperty("Info");
		if (info!=null)
			imp2.setProperty("Info", info);
		if (imp.getStackSize()>1) {
			IjxImageStack stack = imp.getStack();
			String label = stack.getSliceLabel(imp.getCurrentSlice());
			if (label!=null && label.indexOf('\n')>0)
				imp2.setProperty("Info", label);
			if (imp.isComposite()) {
				LUT lut = ((CompositeImage)imp).getChannelLut();
				imp2.getProcessor().setColorModel(lut);
			}
		}
		if (!imp.getHideOverlay())
			imp2.setOverlay(imp.getOverlay());
		return imp2;
	}
	
	/** Returns a new stack containing a subrange of the specified stack. */
	public IjxImagePlus run(IjxImagePlus imp, int firstSlice, int lastSlice) {
		Rectangle rect = null;
		Roi roi = imp.getRoi();
		if (roi!=null && roi.isArea())
			rect = roi.getBounds();
		int width = rect!=null?rect.width:imp.getWidth();
		int height = rect!=null?rect.height:imp.getHeight();
		IjxImageStack stack = imp.getStack();
		IjxImageStack stack2 = IJ.getFactory().newImageStack(width, height, imp.getProcessor().getColorModel());
		for (int i=firstSlice; i<=lastSlice; i++) {
			ImageProcessor ip2 = stack.getProcessor(i);
			ip2.setRoi(rect);
			ip2 = ip2.crop();
			stack2.addSlice(stack.getSliceLabel(i), ip2);
		}
		IjxImagePlus imp2 = imp.createImagePlus();
		imp2.setStack("DUP_"+imp.getTitle(), stack2);
		int size = stack2.getSize();
		boolean tseries = imp.getNFrames()==imp.getStackSize();
		if (tseries)
			imp2.setDimensions(1, 1, size);
		else
			imp2.setDimensions(1, size, 1);
   		if (Recorder.record) Recorder.recordCall("imp = new Duplicator().run(imp, "+firstSlice+", "+lastSlice+");");
		return imp2;
	}

	String showDialog(IjxImagePlus imp, String title, String prompt, String defaultString) {
		int stackSize = imp.getStackSize();
		duplicateSubstack = stackSize>1 && (stackSize==imp.getNSlices()||stackSize==imp.getNFrames());
		GenericDialog gd = new GenericDialog(title);
		gd.addStringField(prompt, defaultString, duplicateSubstack?15:20);
		if (stackSize>1) {
			String msg = duplicateSubstack?"Duplicate Stack":"Duplicate Entire Stack";
			gd.addCheckbox(msg, duplicateStack||imp.isComposite());
			if (duplicateSubstack) {
				gd.setInsets(2, 30, 3);
				gd.addStringField("Range:", "1-"+stackSize);
				Vector v = gd.getStringFields();
				rangeField = (TextField)v.elementAt(1);
				rangeField.addTextListener(this);
				checkbox = (Checkbox)(gd.getCheckboxes().elementAt(0));
			}
		} else
			duplicateStack = false;
		gd.showDialog();
		if (gd.wasCanceled())
			return null;
		title = gd.getNextString();
		if (stackSize>1) {
			duplicateStack = gd.getNextBoolean();
			if (duplicateStack && duplicateSubstack) {
				String[] range = Tools.split(gd.getNextString(), " -");
				double d1 = Tools.parseDouble(range[0]);
				double d2 = range.length==2?Tools.parseDouble(range[1]):Double.NaN;
				first = Double.isNaN(d1)?1:(int)d1;
				last = Double.isNaN(d2)?stackSize:(int)d2;
				if (first<1) first = 1;
				if (last>stackSize) last = stackSize;
				if (first>last) {first=1; last=stackSize;}
			} else {
				first = 1;
				last = stackSize;
			}
		}
		return title;
	}
	
	public void textValueChanged(TextEvent e) {
		checkbox.setState(true);
	}

	
}
