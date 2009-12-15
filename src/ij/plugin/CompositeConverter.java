package ij.plugin;
import ijx.gui.IjxImageWindow;
import ijx.IjxImagePlus;
import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import java.awt.image.*;
import ij.plugin.frame.ContrastAdjuster;
import ijx.IjxImageStack;

/** This plugin imlements the Image/Color/Make Composite command. */
public class CompositeConverter implements PlugIn {

	public void run(String arg) {
		String[] modes = {"Composite", "Color", "Grayscale"};
		IjxImagePlus imp = IJ.getImage();
		if (imp.isComposite()) {
			CompositeImage ci = (CompositeImage)imp;
			if (ci.getMode()!=CompositeImage.COMPOSITE) {
				ci.setMode(CompositeImage.COMPOSITE);
				ci.updateAndDraw();
			}
			if (!IJ.isMacro()) IJ.run("Channels Tool...");
			return;
		}
		String mode = modes[0];
		int z = imp.getStackSize();
		int c = imp.getNChannels();
		if (c==1) {
			c = z;
			imp.setDimensions(c, 1, 1);
			if (c>7) mode = modes[2];
		}
		if (imp.getBitDepth()==24) {
			if (z>1)
				convertRGBToCompositeStack(imp, arg);
			else
				convertRGBToCompositeImage(imp);
			if (!IJ.isMacro()) IJ.run("Channels Tool...");
		} else if (c>=2) {
			GenericDialog gd = new GenericDialog("Make Composite");
			gd.addChoice("Display Mode:", modes, mode);
			gd.showDialog();
			if (gd.wasCanceled()) return;
			int index = gd.getNextChoiceIndex();
			CompositeImage ci = new CompositeImage(imp, index+1);
			ci.show();
			imp.hide();
			if (!IJ.isMacro()) IJ.run("Channels Tool...");
		} else
			IJ.error("To create a composite, the current image must be\n a stack with at least 2 channels or be in RGB format.");
	}
	
	void convertRGBToCompositeImage(IjxImagePlus imp) {
			IjxImageWindow win = imp.getWindow();
			Point loc = win!=null?win.getLocation():null;
			IjxImagePlus imp2 = new CompositeImage(imp, CompositeImage.COMPOSITE);
			imp.hide();
			imp2.show();
			IjxImageWindow win2 = imp2.getWindow();
			if (loc!=null&&win2!=null) win2.setLocation(loc);
	}

	void convertRGBToCompositeStack(IjxImagePlus imp, String arg) {
		int width = imp.getWidth();
		int height = imp.getHeight();
		IjxImageStack stack1 = imp.getStack();
		int n = stack1.getSize();
		IjxImageStack stack2 = new ImageStack(width, height);
		for (int i=0; i<n; i++) {
			ColorProcessor ip = (ColorProcessor)stack1.getProcessor(1);
			stack1.deleteSlice(1);
			byte[] R = new byte[width*height];
			byte[] G = new byte[width*height];
			byte[] B = new byte[width*height];
			ip.getRGB(R, G, B);
			stack2.addSlice(null, R);
			stack2.addSlice(null, G);
			stack2.addSlice(null, B);
		}
		n *= 3;
		imp.setChanged(false);
		IjxImageWindow win = imp.getWindow();
		Point loc = win!=null?win.getLocation():null;
		IjxImagePlus imp2 = IJ.getFactory().newImagePlus(imp.getTitle(), stack2);
		imp2.setDimensions(3, n/3, 1);
		int mode = arg!=null && arg.equals("color")?CompositeImage.COLOR:CompositeImage.COMPOSITE;
 		imp2 = new CompositeImage(imp2, mode);
		imp2.show();
		imp.setChanged(false);
		imp.close();
	}

}
