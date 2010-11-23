package ijx.stack;
import ijx.plugin.api.PlugIn;
import ijx.process.ImageProcessor;
import ijx.gui.dialog.GenericDialog;
import ijx.WindowManager;
import ijx.IJ;



import ijx.measure.Calibration;
import ijx.IjxImagePlus;
import ijx.IjxImageStack;

/** This plugin implements the Image/Stacks/Misc/Reduce command. */
public class StackReducer implements PlugIn {
	IjxImagePlus imp;
	private static int factor = 2;
	private boolean hyperstack, reduceSlices;

	public void run(String arg) {
		imp = WindowManager.getCurrentImage();
		if (imp==null)
			{IJ.noImage(); return;}
		IjxImageStack stack = imp.getStack();
		int size = stack.getSize();
		if (size==1 || (imp.getNChannels()==size&&imp.isComposite()))
			{IJ.error("Stack or hyperstack required"); return;}
		if (!showDialog(stack))
			return;
		if (hyperstack)
			reduceHyperstack(imp, factor, reduceSlices);
		else
			reduceStack(imp, factor);
	}

	public boolean showDialog(IjxImageStack stack) {
		hyperstack = imp.isHyperStack();
		boolean showCheckbox = false;
		if (hyperstack && imp.getNSlices()>1 && imp.getNFrames()>1)
			showCheckbox = true;
		else if (hyperstack && imp.getNSlices()>1)
			reduceSlices = true;
		int n = stack.getSize();
		GenericDialog gd = new GenericDialog("Reduce Size");
		gd.addNumericField("Reduction Factor:", factor, 0);
		if (showCheckbox)
			gd.addCheckbox("Reduce in Z-Dimension", false);
		gd.showDialog();
		if (gd.wasCanceled()) return false;
		factor = (int) gd.getNextNumber();
		if (showCheckbox)
			reduceSlices = gd.getNextBoolean();
		return true;
	}
	
	public void reduceStack(IjxImagePlus imp, int factor) {
		IjxImageStack stack = imp.getStack();
		boolean virtual = stack.isVirtual();
		int n = stack.getSize();
		IjxImageStack stack2 = IJ.getFactory().newImageStack(stack.getWidth(), stack.getHeight());
		for (int i=1; i<=n; i+=factor) {
			if (virtual) IJ.showProgress(i, n);
			stack2.addSlice(stack.getSliceLabel(i), stack.getProcessor(i));
		}
		imp.setStack(null, stack2);
		if (virtual) {
			IJ.showProgress(1.0);
			imp.setTitle(imp.getTitle());
		}
		Calibration cal = imp.getCalibration();
		if (cal.scaled()) cal.pixelDepth *= factor;
	}
	
	public void reduceHyperstack(IjxImagePlus imp, int factor, boolean reduceSlices) {
		int channels = imp.getNChannels();
		int slices = imp.getNSlices();
		int frames = imp.getNFrames();
		int zfactor = reduceSlices?factor:1;
		int tfactor = reduceSlices?1:factor;
		IjxImageStack stack = imp.getStack();
		IjxImageStack stack2 = IJ.getFactory().newImageStack(imp.getWidth(), imp.getHeight());
		boolean virtual = stack.isVirtual();
		int slices2 = slices/zfactor + ((slices%zfactor)!=0?1:0);
		int frames2 = frames/tfactor + ((frames%tfactor)!=0?1:0);
		int n = channels*slices2*frames2;
		int count = 1;
		for (int t=1; t<=frames; t+=tfactor) {
			for (int z=1; z<=slices; z+=zfactor) {
				for (int c=1; c<=channels; c++) {
					int i = imp.getStackIndex(c, z, t);
					IJ.showProgress(i, n);
					ImageProcessor ip = stack.getProcessor(imp.getStackIndex(c, z, t));
					//IJ.log(count++ +"  "+i+" "+c+" "+z+" "+t);
					stack2.addSlice(stack.getSliceLabel(i), ip);
				}
			}
		}
		imp.setStack(stack2, channels, slices2, frames2);
		Calibration cal = imp.getCalibration();
		if (cal.scaled()) cal.pixelDepth *= zfactor;
		if (virtual) imp.setTitle(imp.getTitle());
		IJ.showProgress(1.0);
	}

}
