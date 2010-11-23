package ijx.stack; 
import ijx.plugin.api.PlugIn;
import ijx.IJ;
import ij.*; 
import ijx.gui.dialog.GenericDialog;
import ijx.IjxImagePlus;

/** This plugin implements the Image>Stacks>Tools>Grouped Z Project command. */

public class GroupedZProjector implements PlugIn {
	private static int method = ZProjector.AVG_METHOD;
	private int groupSize;

	public void run(String arg) {
		IjxImagePlus imp = IJ.getImage();
		int size = imp.getStackSize();
		if (size==1) {
			IJ.error("Z Project", "This command requires a stack");
			return;
		}
		if (imp.isHyperStack()) {
			IJ.run(imp, "Z Project...", "");
			return;
		}
		if (!showDialog(imp)) return;
		IjxImagePlus imp2 = groupZProject(imp, method, groupSize);
		if (imp!=null) imp2.show();
	}
	
	public IjxImagePlus groupZProject(IjxImagePlus imp, int method, int groupSize) {
		if (method<0 || method>=ZProjector.METHODS.length)
			return null;
		imp.setDimensions(1, groupSize, imp.getStackSize()/groupSize);
		ZProjector zp = new ZProjector(imp);
		zp.setMethod(method);
		zp.setStartSlice(1);
		zp.setStopSlice(groupSize);
		zp.doHyperStackProjection(true);
		return zp.getProjection();
	}
	
	boolean showDialog(IjxImagePlus imp) {
		int size = imp.getStackSize();
		GenericDialog gd = new GenericDialog("Z Project");
		gd.addChoice("Projection method", ZProjector.METHODS, ZProjector.METHODS[method]);
		gd.addNumericField("Group size:", size, 0);
		gd.showDialog();
		if (gd.wasCanceled()) return false; 
		method = gd.getNextChoiceIndex();
		groupSize = (int)gd.getNextNumber(); 
		if (groupSize<1  ||  groupSize>size || (size%groupSize)!=0) {
			IJ.error("ZProject", "Group size must divide evenly into the stack size.");
			return false;
		}
		return true;
	}
    
} 


