package ij.plugin;
import ijx.gui.IjxImageWindow;
import ijx.IjxImagePlus;
import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.measure.Calibration;
import ij.macro.Interpreter;
import ij.io.FileInfo;
import ijx.IjxImageStack;


/** Implements the AddSlice, DeleteSlice and "Convert Windows to Stack" commands. */
public class StackEditor implements IjxStackEditor {
	IjxImagePlus imp;
	int nSlices, width, height;

	public void run(String arg) {
		imp = IJ.getImage();
    	nSlices = imp.getStackSize();
    	width = imp.getWidth();
    	height = imp.getHeight();
    	
    	if (arg.equals("tostack"))
    		convertImagesToStack();
    	else if (arg.equals("add"))
    		addSlice();
    	else if (arg.equals("delete"))
    		deleteSlice();
    	else if (arg.equals("toimages"))
    		convertStackToImages(imp);
	}

	void addSlice() {
		if (imp.isHyperStack()) return;
 		if (!imp.lock()) return;
		int id = 0;
		IjxImageStack stack = imp.getStack();
		if (stack.getSize()==1) {
			String label = stack.getSliceLabel(1);
			if (label!=null && label.indexOf("\n")!=-1)
				stack.setSliceLabel(null, 1);
			Object obj = imp.getProperty("Label");
			if (obj!=null && (obj instanceof String))
				stack.setSliceLabel((String)obj, 1);
			id = imp.getID();
		}
		ImageProcessor ip = imp.getProcessor();
		int n = imp.getCurrentSlice();
		if (IJ.altKeyDown()) n--; // insert in front of current slice
		stack.addSlice(null, ip.createProcessor(width, height), n);
		imp.setStack(null, stack);
		imp.setSlice(n+1);
		imp.unlock();
		if (id!=0) IJ.selectWindow(id); // prevents macros from failing
	}
	
	void deleteSlice() {
		if (imp.isHyperStack()) return;
		if (nSlices<2)
			{IJ.error("\"Delete Slice\" requires a stack"); return;}
		if (!imp.lock()) return;
		IjxImageStack stack = imp.getStack();
		int n = imp.getCurrentSlice();
 		stack.deleteSlice(n);
 		if (stack.getSize()==1) {
			String label = stack.getSliceLabel(1);
 			if (label!=null) imp.setProperty("Label", label);
 		}
		imp.setStack(null, stack);
 		if (n--<1) n = 1;
		imp.setSlice(n);
		imp.unlock();
	}

	public void convertImagesToStack() {
		int[] wList = WindowManager.getIDList();
		if (wList==null) {
			IJ.error("No images are open.");
			return;
		}

		int count = 0;
		IjxImagePlus[] image = IJ.getFactory().newImagePlusArray(wList.length);
		for (int i=0; i<wList.length; i++) {
			IjxImagePlus imp = WindowManager.getImage(wList[i]);
			if (imp.getStackSize()==1)
				image[count++] = imp;
		}		
		if (count<2) {
			IJ.error("There must be at least two open images.");
			return;
		}

		Calibration cal2 = image[0].getCalibration();
		for (int i=0; i<(count-1); i++) {
			if (image[i].getType()!=image[i+1].getType()) {
				IJ.error("All open images must be the same type.");
				return;
			}
			if (image[i].getWidth()!=image[i+1].getWidth()
			|| image[i].getHeight()!=image[i+1].getHeight()) {
				IJ.error("All open images must be the same size.");
				return;
			}
			Calibration cal = image[i].getCalibration();
			if (!image[i].getCalibration().equals(cal2))
				cal2 = null;
		}
		
		int width = image[0].getWidth();
		int height = image[0].getHeight();
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		IjxImageStack stack = IJ.getFactory().newImageStack(width, height);
		FileInfo fi = image[0].getOriginalFileInfo();
		if (fi!=null && fi.directory==null) fi = null;
		for (int i=0; i<count; i++) {
			ImageProcessor ip = image[i].getProcessor();
			if (ip.getMin()<min) min = ip.getMin();
			if (ip.getMax()>max) max = ip.getMax();
            String label = image[i].getTitle();
            String info = (String)image[i].getProperty("Info");
            if (info!=null) label += "\n" + info;
            if (fi!=null) {
				FileInfo fi2 = image[i].getOriginalFileInfo();
				if (fi2!=null && !fi.directory.equals(fi2.directory))
					fi = null;
            }
            stack.addSlice(label, ip);
			image[i].setChanged(false);
			image[i].close();
		}
		IjxImagePlus imp = IJ.getFactory().newImagePlus("Stack", stack);
		if (imp.getType()==IjxImagePlus.GRAY16 || imp.getType()==IjxImagePlus.GRAY32)
			imp.getProcessor().setMinAndMax(min, max);
		if (cal2!=null)
			imp.setCalibration(cal2);
		if (fi!=null) {
			fi.fileName = "";
			fi.nImages = imp.getStackSize();
			imp.setFileInfo(fi);
		}
		imp.show();
	}

	public void convertStackToImages(IjxImagePlus imp) {
		if (nSlices<2)
			{IJ.error("\"Convert Stack to Images\" requires a stack"); return;}
		if (!imp.lock())
			return;
		IjxImageStack stack = imp.getStack();
		int size = stack.getSize();
		if (size>30 && !IJ.isMacro()) {
			boolean ok = IJ.showMessageWithCancel("Convert to Images?",
			"Are you sure you want to convert this\nstack to "
			+size+" separate windows?");
			if (!ok)
				{imp.unlock(); return;}
		}
		Calibration cal = imp.getCalibration();
		CompositeImage cimg = imp.isComposite()?(CompositeImage)imp:null;
		if (imp.getNChannels()!=imp.getStackSize()) cimg = null;
		for (int i=1; i<=size; i++) {
			String label = stack.getShortSliceLabel(i);
			String title = label!=null&&!label.equals("")?label:getTitle(imp, i);
			ImageProcessor ip = stack.getProcessor(i);
			if (cimg!=null) {
				LUT lut = cimg.getChannelLut(i);
				if (lut!=null) {
					ip.setColorModel(lut);
					ip.setMinAndMax(lut.min, lut.max);
				}
			}
			IjxImagePlus imp2 = IJ.getFactory().newImagePlus(title, ip);
			imp2.setCalibration(cal);
			String info = stack.getSliceLabel(i);
			if (info!=null && !info.equals(label))
				imp2.setProperty("Info", info);
			imp2.show();
		}
		imp.setChanged(false);
		IjxImageWindow win = imp.getWindow();
		if (win!=null)
			win.close();
		else if (Interpreter.isBatchMode())
			Interpreter.removeBatchModeImage(imp);
		imp.unlock();
	}

	String getTitle(IjxImagePlus imp, int n) {
		String digits = "00000000"+n;
		return imp.getShortTitle()+"-"+digits.substring(digits.length()-4,digits.length());
	}
	
}

