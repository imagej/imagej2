package ijx.stack;
import ijx.plugin.api.PlugIn;
import ijx.IJ;

import ijx.IjxImagePlus;
import ijx.IjxImageStack;

/** Implements the Image/Transform/Flip Z command. */
public class StackReverser implements PlugIn {
	
	public void run(String arg) {
		IjxImagePlus imp = IJ.getImage();
		if (imp.getStackSize()==1) {
			IJ.error("Flip Z", "This command requires a stack");
			return;
		}
		if (imp.isHyperStack()) {
			IJ.error("Flip Z", "This command does not currently work with hyperstacks.");
			return;
		}
		flipStack(imp);
	}
	
	public void flipStack(IjxImagePlus imp) {
		IjxImageStack stack = imp.getStack();
 		IjxImageStack stack2 = imp.createEmptyStack();
 		int n;
		while ((n=stack.getSize())>0) { 
			stack2.addSlice(stack.getSliceLabel(n), stack.getProcessor(n));
			stack.deleteLastSlice();
		}
		imp.setStack(null, stack2);
	}

}
