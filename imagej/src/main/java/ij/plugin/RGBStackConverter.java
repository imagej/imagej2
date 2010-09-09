package ij.plugin;
import java.awt.*;
import ij.*;
import ij.process.*;
import ij.gui.*;

/** Converts a 2 or 3 slice stack, or a hyperstack, to RGB. */
public class RGBStackConverter implements PlugIn, DialogListener {
	int channels1, slices1, frames1;
	int slices2, frames2;
	int width, height;
	double imageSize;
	static boolean keep = true;
	
	public void run(String arg) {
		ImagePlus imp = IJ.getImage();
		CompositeImage cimg = imp.isComposite()?(CompositeImage)imp:null;
		int size = imp.getStackSize();
		if ((size<2||size>3) && cimg==null) {
			IJ.error("A 2 or 3 image stack, or a HyperStack, required");
			return;
		}
		int type = imp.getType();
		if (cimg==null && !(type==ImagePlus.GRAY8 || type==ImagePlus.GRAY16)) {
			IJ.error("8-bit or 16-bit grayscale stack required");
			return;
		}
		if (!imp.lock())
			return;
		Undo.reset();
		String title = imp.getTitle()+" (RGB)";
		if (cimg!=null)
			compositeToRGB(cimg, title);
		else if (type==ImagePlus.GRAY16) {
			sixteenBitsToRGB(imp);
		} else {
			ImagePlus imp2 = imp.createImagePlus();
			imp2.setStack(title, imp.getStack());
	 		ImageConverter ic = new ImageConverter(imp2);
			ic.convertRGBStackToRGB();
			imp2.show();
		}
		imp.unlock();
	}
	
	void compositeToRGB(CompositeImage imp, String title) {
		int channels = imp.getNChannels();
		int slices = imp.getNSlices();
		int frames = imp.getNFrames();
		int images = channels*slices*frames;
		if (channels==images) {
			compositeImageToRGB(imp, title);
			return;
		}
		width = imp.getWidth();
		height = imp.getHeight();
		imageSize = width*height*4.0/(1024.0*1024.0);
		channels1 = imp.getNChannels();
		slices1 = slices2 = imp.getNSlices();
		frames1 = frames2 = imp.getNFrames();
		int c1 = imp.getChannel();
		int z1 = imp.getSlice();
		int t2 = imp.getFrame();
		if (!showDialog())
			return;
		//IJ.log("HyperStackReducer-2: "+keep+" "+channels2+" "+slices2+" "+frames2);
		String title2 = keep?WindowManager.getUniqueName(imp.getTitle()):imp.getTitle();
		ImagePlus imp2 = imp.createHyperStack(title2, 1, slices2, frames2, 24);
		convertHyperstack(imp, imp2);
		imp2.setOpenAsHyperStack(slices2>1||frames2>1);
		imp2.show();
		if (!keep) {
			imp.changes = false;
			imp.close();
		}
	}

	public void convertHyperstack(ImagePlus imp, ImagePlus imp2) {
		int slices = imp2.getNSlices();
		int frames = imp2.getNFrames();
		int c1 = imp.getChannel();
		int z1 = imp.getSlice();
		int t1 = imp.getFrame();
		int i = 1;
		int c = 1;
		ImageStack stack = imp.getStack();
		ImageStack stack2 = imp2.getStack();
		imp.setPositionWithoutUpdate(c, 1, 1);
		ImageProcessor ip = imp.getProcessor();
		double min = ip.getMin();
		double max = ip.getMax();
		for (int z=1; z<=slices; z++) {
			if (slices==1) z = z1;
			for (int t=1; t<=frames; t++) {
				//IJ.showProgress(i++, n);
				if (frames==1) t = t1;
				//ip = stack.getProcessor(n1);
				imp.setPositionWithoutUpdate(c, z, t);
				Image img = imp.getImage();
				int n2 = imp2.getStackIndex(c, z, t);
				stack2.setPixels((new ColorProcessor(img)).getPixels(), n2);
			}
		}
		imp.setPosition(c1, z1, t1);
		imp2.resetStack();
		imp2.setPosition(1, 1, 1);
	}

	void compositeToRGB2(CompositeImage imp, String title) {
		int channels = imp.getNChannels();
		int slices = imp.getNSlices();
		int frames = imp.getNFrames();
		int images = channels*slices*frames;
		if (channels==images) {
			compositeImageToRGB(imp, title);
			return;
		}
		String msg = null;
		if (frames>1)
			msg = "Convert all "+frames+" frames?";
		else
			msg = "Convert all "+slices+" slices?";
		if (!IJ.isMacro()) {
			YesNoCancelDialog d = new YesNoCancelDialog(IJ.getInstance(), "Convert to RGB", msg);
			if (d.cancelPressed())
				return;
			else if (!d.yesPressed()) {
				compositeImageToRGB(imp, title);
				return;
			}
		}
		//if (!imp.isHyperStack()) return;
		int n = frames;
		if (n==1) n = slices;
		ImageStack stack = new ImageStack(imp.getWidth(), imp.getHeight());
		int c=imp.getChannel(), z=imp.getSlice(), t=imp.getFrame();
		for (int i=1; i<=n; i++) {
			if (frames==1)
				imp.setPositionWithoutUpdate(imp.getChannel(), i, imp.getFrame());
			else
				imp.setPositionWithoutUpdate(imp.getChannel(), imp.getSlice(), i);
			stack.addSlice(null, new ColorProcessor(imp.getImage()));
		}
		imp.setPosition(c, z, t);
		ImagePlus imp2 = imp.createImagePlus();
		imp2.setStack(title, stack);
		Object info = imp.getProperty("Info");
		if (info!=null) imp2.setProperty("Info", info);
		imp2.show();
	}

	void compositeImageToRGB(CompositeImage imp, String title) {
		if (imp.getMode()==CompositeImage.COMPOSITE) {
			ImagePlus imp2 = imp.createImagePlus();
			imp.updateImage();
			imp2.setProcessor(title, new ColorProcessor(imp.getImage()));
			imp2.show();
			return;
		}
		ImageStack stack = new ImageStack(imp.getWidth(), imp.getHeight());
		int c = imp.getChannel();
		int n = imp.getNChannels();
		for (int i=1; i<=n; i++) {
			imp.setPositionWithoutUpdate(i, 1, 1);
			stack.addSlice(null, new ColorProcessor(imp.getImage()));
		}
		imp.setPosition(c, 1, 1);
		ImagePlus imp2 = imp.createImagePlus();
		imp2.setStack(title, stack);
		Object info = imp.getProperty("Info");
		if (info!=null) imp2.setProperty("Info", info);
		imp2.show();
	}

	void sixteenBitsToRGB(ImagePlus imp) {
		Roi roi = imp.getRoi();
		int width, height;
		Rectangle r;
		if (roi!=null) {
			r = roi.getBounds();
			width = r.width;
			height = r.height;
		} else
			r = new Rectangle(0,0,imp.getWidth(),imp.getHeight());
		ImageProcessor ip;
		ImageStack stack1 = imp.getStack();
		ImageStack stack2 = new ImageStack(r.width, r.height);
		for (int i=1; i<=stack1.getSize(); i++) {
			ip = stack1.getProcessor(i);
			ip.setRoi(r);
			ImageProcessor ip2 = ip.crop();
			ip2 = ip2.convertToByte(true);
			stack2.addSlice(null, ip2);
		}
		ImagePlus imp2 = imp.createImagePlus();
		imp2.setStack(imp.getTitle()+" (RGB)", stack2);
	 	ImageConverter ic = new ImageConverter(imp2);
		ic.convertRGBStackToRGB();
		imp2.show();
	}
	
	boolean showDialog() {
		GenericDialog gd = new GenericDialog("Convert to RGB");
		gd.setInsets(10, 20, 5);
		gd.addMessage("Create RGB Image With:");
		gd.setInsets(0, 35, 0);
		if (slices1!=1) gd.addCheckbox("Slices ("+slices1+")", true);
		gd.setInsets(0, 35, 0);
		if (frames1!=1) gd.addCheckbox("Frames ("+frames1+")", true);
		gd.setInsets(5, 20, 0);
		gd.addMessage(getNewDimensions()+"      ");
		gd.setInsets(15, 20, 0);
		gd.addCheckbox("Keep Source", keep);
		gd.addDialogListener(this);
		gd.showDialog();
		if (gd.wasCanceled())
			return false;
		else
			return true;
	}

	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		if (IJ.isMacOSX()) IJ.wait(100);
		if (slices1!=1) slices2 = gd.getNextBoolean()?slices1:1;
		if (frames1!=1) frames2 = gd.getNextBoolean()?frames1:1;
		keep = gd.getNextBoolean();
		((Label)gd.getMessage()).setText(getNewDimensions());
		return true;
	}
	
	String getNewDimensions() {
		String s1 = slices2>1?"x"+slices2:"";
		String s2 = frames2>1?"x"+frames2:"";
		String s = width+"x"+height+s1+s2;
		s += " ("+(int)Math.round(imageSize*slices2*frames2)+"MB)";
		return(s);
	}

	
}
