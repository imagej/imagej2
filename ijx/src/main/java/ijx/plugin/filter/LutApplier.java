package ijx.plugin.filter;
import ijx.process.ImageProcessor;
import ijx.process.StackProcessor;
import ijx.plugin.api.PlugInFilter;
import ijx.Undo;
import ijx.WindowManager;
import ijx.IJ;
import ij.*;

import ijx.plugin.frame.ContrastAdjuster;
import ijx.IjxImagePlus;
import ijx.IjxImageStack;
import ijx.gui.IjxWindow;
import java.awt.*;

/** This plugin implements the Image/Lookup Tables/Apply LUT command. */
public class LutApplier implements PlugInFilter {
	IjxImagePlus imp;
	int min, max;
	boolean canceled;

	public int setup(String arg, IjxImagePlus imp) {
		this.imp = imp;
		int baseOptions = DOES_8G+DOES_8C+DOES_RGB+SUPPORTS_MASKING;
		if (imp!=null && imp.getType()==IjxImagePlus.COLOR_RGB)
			return baseOptions+NO_UNDO;
		else
			return baseOptions;
	}

	public void run(ImageProcessor ip) {
		apply(imp, ip);
	}
	
	void apply(IjxImagePlus imp, ImageProcessor ip) {
        if (ip.getMinThreshold()!=ImageProcessor.NO_THRESHOLD) {
            imp.unlock();
			IJ.runPlugIn("ij.plugin.Thresholder", "skip");
            return;
        }
		min = (int)ip.getMin();
		max = (int)ip.getMax();
		if (min==0 && max==255) {
				IJ.error("Apply LUT", "The display range must first be updated\n"
                +"using Image>Adjust>Brightness/Contrast\n"
                +"or threshold levels defined using\n"
                +"Image>Adjust>Threshold.");
				return;
		}
		if (imp.getType()==IjxImagePlus.COLOR_RGB) {
			if (imp.getStackSize()>1)
				applyRGBStack(imp);
			else {
				ip.reset();
				Undo.setup(Undo.TRANSFORM, imp);
				ip.setMinAndMax(min, max);
				//ip.snapshot();
			}
			if (canceled) ip.reset();
			resetContrastAdjuster();
			return;
		}
		ip.resetMinAndMax();
		int[] table = new int[256];
		for (int i=0; i<256; i++) {
			if (i<=min)
				table[i] = 0;
			else if (i>=max)
				table[i] = 255;
			else
				table[i] = (int)(((double)(i-min)/(max-min))*255);
		}
		if (imp.getStackSize()>1) {
			IjxImageStack stack = imp.getStack();
			
			int flags = IJ.setupDialog(imp, 0);
			if (flags==PlugInFilter.DONE)
				{ip.setMinAndMax(min, max); return;}
			if (flags==PlugInFilter.DOES_STACKS) {
				new StackProcessor(stack, ip).applyTable(table);
				Undo.reset();
			} else
				ip.applyTable(table);
		} else
			ip.applyTable(table);
		resetContrastAdjuster();
	}
	
	void resetContrastAdjuster() {
        IjxWindow frame = WindowManager.getFrame("B&C");
        if (frame==null)
            frame = WindowManager.getFrame("W&L");
        if (frame!=null && (frame instanceof ContrastAdjuster))
            ((ContrastAdjuster)frame).updateAndDraw();
	}

	void applyRGBStack(IjxImagePlus imp) {
		int current = imp.getCurrentSlice();
		int n = imp.getStackSize();
		if (!IJ.showMessageWithCancel("Update Entire Stack?",
		"Apply brightness and contrast settings\n"+
		"to all "+n+" slices in the stack?\n \n"+
		"NOTE: There is no Undo for this operation.")) {
			canceled = true;
			return;
		}
		for (int i=1; i<=n; i++) {
			if (i!=current) {
				imp.setSlice(i);
				ImageProcessor ip = imp.getProcessor();
				ip.setMinAndMax(min, max);
				IJ.showProgress((double)i/n);
			}
		}
		imp.setSlice(current);
	}
	
}
