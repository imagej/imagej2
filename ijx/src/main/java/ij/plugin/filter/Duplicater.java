package ij.plugin.filter;
import ij.process.*;
import ij.plugin.Duplicator;
import ijx.IjxImagePlus;

/**
* @deprecated
* replaced by Duplicator class
*/
public class Duplicater implements PlugInFilter {
	IjxImagePlus imp;

	public int setup(String arg, IjxImagePlus imp) {
		this.imp = imp;
		return DOES_ALL+NO_CHANGES;
	}

	public void run(ImageProcessor ip) {
	}

	public IjxImagePlus duplicateStack(IjxImagePlus imp, String newTitle) {
		IjxImagePlus imp2 = (new Duplicator()).run(imp);
		imp2.setTitle(newTitle);
		return imp2;
	}
	
	public IjxImagePlus duplicateSubstack(IjxImagePlus imp, String newTitle, int first, int last) {
		IjxImagePlus imp2 = (new Duplicator()).run(imp, first, last);
		imp2.setTitle(newTitle);
		return imp2;
	}

}
