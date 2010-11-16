package ij.plugin.filter;
import ij.*;
import ij.process.*;
import ij.measure.Calibration;
import ij.plugin.HyperStackReducer;
import ij.io.FileInfo;
import ijx.IjxImagePlus;
import ijx.IjxImageStack;

/** Splits an RGB image or stack into three 8-bit grayscale images or stacks. */
public class RGBStackSplitter implements PlugInFilter {
    IjxImagePlus imp;
    /** These are the three stacks created by the split(IjxImageStack) method. */
    public IjxImageStack red, green, blue;


    public int setup(String arg, IjxImagePlus imp) {
        this.imp = imp;
        if (imp!=null && imp.isComposite()) {
        	splitChannels(imp);
        	return DONE;
        }
        if (imp!=null && imp.getBitDepth()!=24) {
        	IJ.error("Split Channels", "Multichannel image required");
        	return DONE;
        }
        return DOES_RGB+NO_UNDO;
    }

    public void run(ImageProcessor ip) {
        split(imp);
    }

    /** Splits the specified RGB image or stack into three 8-bit grayscale images or stacks. */
    public void split(IjxImagePlus imp) {
    	boolean keepSource = IJ.altKeyDown();
        String title = imp.getTitle();
        Calibration cal = imp.getCalibration();
        FileInfo fi = imp.getOriginalFileInfo();
        split(imp.getStack(), keepSource);
        if (!keepSource)
            {imp.unlock(); imp.setChanged(false); imp.close();}
        IjxImagePlus rImp = IJ.getFactory().newImagePlus(title+" (red)",red);
        rImp.setCalibration(cal);
        rImp.setFileInfo(fi);
        rImp.show();
        if (IJ.isMacOSX()) IJ.wait(500);
        IjxImagePlus gImp = IJ.getFactory().newImagePlus(title+" (green)",green);
        gImp.setCalibration(cal);
        rImp.setFileInfo(fi);
        gImp.show();
        if (IJ.isMacOSX()) IJ.wait(500);
        IjxImagePlus bImp = IJ.getFactory().newImagePlus(title+" (blue)",blue);
        bImp.setCalibration(cal);
        bImp.setFileInfo(fi);
        bImp.show();
    }

    /** Splits the specified RGB stack into three 8-bit grayscale stacks. 
    	Deletes the source stack if keepSource is false. */
    public void split(IjxImageStack rgb, boolean keepSource) {
         int w = rgb.getWidth();
         int h = rgb.getHeight();
         red = IJ.getFactory().newImageStack(w,h);
         green = IJ.getFactory().newImageStack(w,h);
         blue = IJ.getFactory().newImageStack(w,h);
         byte[] r,g,b;
         ColorProcessor cp;
         int slice = 1;
         int inc = keepSource?1:0;
         int n = rgb.getSize();
         for (int i=1; i<=n; i++) {
             IJ.showStatus(i+"/"+n);
             r = new byte[w*h];
             g = new byte[w*h];
             b = new byte[w*h];
             cp = (ColorProcessor)rgb.getProcessor(slice);
             slice += inc;
             cp.getRGB(r,g,b);
             if (!keepSource)
             	rgb.deleteSlice(1);
             red.addSlice(null,r);
             green.addSlice(null,g);
             blue.addSlice(null,b);
             IJ.showProgress((double)i/n);
        }
    }

	public static IjxImagePlus[] splitChannelsToArray(
		IjxImagePlus imp,
		boolean closeAfter) {

		if(!imp.isComposite()) {
			String error="splitChannelsToArray was called "+
				"on a non-composite image";
			IJ.error(error);
			return null;
		}

		int width = imp.getWidth();
		int height = imp.getHeight();
		int channels = imp.getNChannels();
		int slices = imp.getNSlices();
		int frames = imp.getNFrames();
		int bitDepth = imp.getBitDepth();
		int size = slices*frames;
		FileInfo fi = imp.getOriginalFileInfo();
		IjxImagePlus[] result=new IjxImagePlus[channels];
		HyperStackReducer reducer = new HyperStackReducer(imp);
		for (int c=1; c<=channels; c++) {
			IjxImageStack stack2 = IJ.getFactory().newImageStack(width, height, size); // create empty stack
			stack2.setPixels(imp.getProcessor().getPixels(), 1); // can't create IjxImagePlus will null 1st image
			IjxImagePlus imp2 = IJ.getFactory().newImagePlus("C"+c+"-"+imp.getTitle(), stack2);
			stack2.setPixels(null, 1);
			imp.setPosition(c, 1, 1);
			imp2.setDimensions(1, slices, frames);
			imp2.setCalibration(imp.getCalibration());
			reducer.reduce(imp2);
			if (imp2.getNDimensions()>3)
				imp2.setOpenAsHyperStack(true);
			imp2.setFileInfo(fi);
			result[c - 1] = imp2;
		}
		imp.setChanged(false);
		if (closeAfter)
			imp.close();
		return result;
	}

	void splitChannels(IjxImagePlus imp) {
		IjxImagePlus[] a=splitChannelsToArray(imp,true);
		for(int i=0;i<a.length;++i)
			a[i].show();
	}

}
