package ijx.plugin;
import ijx.plugin.api.PlugIn;
import ijx.process.ImageProcessor;
import ijx.util.StringSorter;
import ijx.util.Tools;
import ijx.IJ;

import ijx.IjxImagePlus;
import ijx.IjxImageStack;

/* Sorts a DICOM stack by image number. */
public class DICOM_Sorter implements PlugIn {
	static final int MAX_DIGITS = 5;

	public void run(String arg) {
		IjxImagePlus imp = IJ.getImage();
		if (!isDicomStack(imp)) {
			IJ.showMessage("DICOM Sorter", "This command requires a DICOM stack");
			return;
		}
		int stackSize = imp.getStackSize();
		IjxImageStack stack = imp.getStack();
		String[] strings = getSortStrings(stack, "0020,0013");
		if (strings==null) return;
		StringSorter.sort(strings);
		IjxImageStack stack2 = sortStack(stack, strings);
		if (stack2!=null)
			imp.setStack(null, stack2);
	}
	
	public IjxImageStack sort(IjxImageStack stack) {
		if (IJ.debugMode) IJ.log("DICOM_Sorter: sorting by image number");
		if (stack.getSize()==1) return stack;
		String[] strings = getSortStrings(stack, "0020,0013");
		if (strings==null) return stack;
		StringSorter.sort(strings);
		IjxImageStack stack2 = sortStack(stack, strings);
		return stack2!=null?stack2:stack;
	}
	
	IjxImageStack sortStack(IjxImageStack stack, String[] strings) {
		ImageProcessor ip = stack.getProcessor(1);
		IjxImageStack stack2 = IJ.getFactory().newImageStack(ip.getWidth(), ip.getHeight(), ip.getColorModel());
		for (int i=0; i<stack.getSize(); i++) {
			int slice = (int)Tools.parseDouble(strings[i].substring(strings[i].length()-MAX_DIGITS), 0.0);
			if (slice==0) return null;
			stack2.addSlice(stack.getSliceLabel(slice), stack.getPixels(slice));
		}
		stack2.update(stack.getProcessor(1));
		return stack2;
	}

	String[] getSortStrings(IjxImageStack stack, String tag) {
		double series = getSeriesNumber(stack.getSliceLabel(1));
		int n = stack.getSize();
		String[] values = new String[n];
		for (int i=1; i<=n; i++) {
			String tags = stack.getSliceLabel(i);
			if (tags==null) return null;
			double value = getNumericTag(tags, tag);
			if (Double.isNaN(value)) {
				if (IJ.debugMode) IJ.log("  "+tag+"  tag missing in slice "+i);
				return null;
			}
			if (getSeriesNumber(tags)!=series) {
				if (IJ.debugMode) IJ.log("  all slices must be part of the same series");
				return null;
			}
			values[i-1] = toString(value, MAX_DIGITS) + toString(i, MAX_DIGITS);
		}
		return values;
	}

	String toString(double value, int width) {
		String s = "       " + IJ.d2s(value,0);
		return s.substring(s.length()-MAX_DIGITS);
	}

	boolean isDicomStack(IjxImagePlus imp) {
		if (imp.getStackSize()==1)
			return false;
		IjxImageStack stack = imp.getStack();
		String label = stack.getSliceLabel(1);
		return label!=null && label.lastIndexOf("7FE0,0010")>0;
	}
	
	double getSeriesNumber(String tags) {
		double series = getNumericTag(tags, "0020,0011");
		if (Double.isNaN(series)) series = 0;
		return series;
	}

	double getNumericTag(String hdr, String tag) {
		String value = getTag(hdr, tag);
		if (value.equals("")) return Double.NaN;
		int index3 = value.indexOf("\\");
		if (index3>0)
			value = value.substring(0, index3);
		return Tools.parseDouble(value);
	}

	String getTag(String hdr, String tag) {
		if (hdr==null) return "";
		int index1 = hdr.indexOf(tag);
		if (index1==-1) return "";
		//IJ.log(hdr.charAt(index1+11)+"   "+hdr.substring(index1,index1+20));
		if (hdr.charAt(index1+11)=='>') {
			// ignore tags in sequences
			index1 = hdr.indexOf(tag, index1+10);
			if (index1==-1) return "";
		}
		index1 = hdr.indexOf(":", index1);
		if (index1==-1) return "";
		int index2 = hdr.indexOf("\n", index1);
		String value = hdr.substring(index1+1, index2);
		return value;
	}

}

