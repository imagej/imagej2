package ij.plugin.filter;
import ijx.IjxImagePlus;
import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.measure.Calibration;
import ij.plugin.HyperStackReducer;
import ijx.IjxImageStack;
import java.awt.*;

/** Splits an RGB image or stack into three 8-bit grayscale images or stacks. */
public class RGBStackSplitter implements IjxPlugInFilter {
    IjxImagePlus imp;
    /** These are the three stacks created by the split(ImageStack) method. */
    public IjxImageStack red, green, blue;


    public int setup(String arg, IjxImagePlus imp) {
        this.imp = imp;
        if (imp!=null && imp.isComposite()) {
        	splitChannels(imp);
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
        split(imp.getStack(), keepSource);
        if (!keepSource)
            {imp.unlock(); imp.close();}
        IjxImagePlus rImp = IJ.getFactory().newImagePlus(title+" (red)",red);
        rImp.setCalibration(cal);
        rImp.show();
        if (IJ.isMacOSX()) IJ.wait(500);
        IjxImagePlus gImp = IJ.getFactory().newImagePlus(title+" (green)",green);
        gImp.setCalibration(cal);
        gImp.show();
        if (IJ.isMacOSX()) IJ.wait(500);
        IjxImagePlus bImp = IJ.getFactory().newImagePlus(title+" (blue)",blue);
        bImp.setCalibration(cal);
        bImp.show();
    }

    /** Splits the specified RGB stack into three 8-bit grayscale stacks. 
    	Deletes the source stack if keepSource is false. */
    public void split(IjxImageStack rgb, boolean keepSource) {
         int w = rgb.getWidth();
         int h = rgb.getHeight();
         red = new ImageStack(w,h);
         green = new ImageStack(w,h);
         blue = new ImageStack(w,h);
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
    
    void splitChannels(IjxImagePlus imp) {
		int width = imp.getWidth();
		int height = imp.getHeight();
		int channels = imp.getNChannels();
		int slices = imp.getNSlices();
		int frames = imp.getNFrames();
		int bitDepth = imp.getBitDepth();
		int size = slices*frames;
		HyperStackReducer reducer = new HyperStackReducer(imp);
		for (int c=1; c<=channels; c++) {
			IjxImageStack stack2 = IJ.getFactory().newImageStack(width, height, size); // create empty stack
			stack2.setPixels(imp.getProcessor().getPixels(), 1); // can't create ImagePlus will null 1st image
			IjxImagePlus imp2 = IJ.getFactory().newImagePlus("C"+c+"-"+imp.getTitle(), stack2);
			stack2.setPixels(null, 1);
			imp.setPosition(c, 1, 1);
			imp2.setDimensions(1, slices, frames);
			reducer.reduce(imp2);
			imp2.setOpenAsHyperStack(true);
			imp2.show();
		}
		imp.setChanged(false);
		imp.close();
    }

}



