import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.*;

/** This plugin removes slices from a stack. */
public class Slice_Remover implements PlugIn {
	private static int inc = 2;
	int first, last;

	public void run(String arg) {
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp==null)
			{IJ.noImage(); return;}
		ImageStack stack = imp.getStack();
		if (stack.getSize()==1)
			{IJ.error("Stack Required"); return;}
		if (!showDialog(stack))
			return;
		removeSlices(imp, first, last, inc);
	}

	public boolean showDialog(ImageStack stack) {
		int n = stack.getSize();
		GenericDialog gd = new GenericDialog("Slice Remover");
		gd.addNumericField("First Slice:", 1, 0);
		gd.addNumericField("Last Slice:", n, 0);
		gd.addNumericField("Increment:", inc, 0);
		gd.showDialog();
		if (gd.wasCanceled())
			return false;
		first = (int) gd.getNextNumber();
		last = (int) gd.getNextNumber();
		inc = (int) gd.getNextNumber();
		return true;
	}
	
	public void removeSlices(ImagePlus imp, int first, int last, int inc) {
		ImageStack stack = imp.getStack();
		boolean virtual = stack.isVirtual();
		if (last>stack.getSize())
			last = stack.getSize();
		ImageStack stack2 = new ImageStack(stack.getWidth(), stack.getHeight());
		for (int i=first; i<=last; i+=inc) {
			if (virtual) IJ.showProgress(i, last);
			stack2.addSlice(stack.getSliceLabel(i), stack.getProcessor(i));
		}
		imp.setStack(null, stack2);
		if (virtual) imp.setTitle(imp.getTitle());
	}

}
