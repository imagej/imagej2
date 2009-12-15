package ij.plugin;
import ijx.IjxImagePlus;
import ij.*;
import ij.process.*;
import ij.gui.*;
import ijx.IjxImageStack;
import java.awt.Color;

public class RGBStackMerge implements PlugIn {

    private static boolean createComposite = true;
    private IjxImagePlus imp;
    private byte[] blank;
 
    /** Merges one, two or three 8-bit or RGB stacks into a single RGB stack. */
    public void run(String arg) {
        imp = WindowManager.getCurrentImage();
        mergeStacks();
    }

    /** Combines three grayscale stacks into one RGB stack. */
    public void mergeStacks() {
        int[] wList = WindowManager.getIDList();
        if (wList==null) {
            IJ.error("No images are open.");
            return;
        }

        String[] titles = new String[wList.length+1];
        for (int i=0; i<wList.length; i++) {
            IjxImagePlus imp = WindowManager.getImage(wList[i]);
            titles[i] = imp!=null?imp.getTitle():"";
        }
        String none = "*None*";
        titles[wList.length] = none;

        GenericDialog gd = new GenericDialog("Color Merge");
        gd.addChoice("Red:", titles, titles[0]);
        gd.addChoice("Green:", titles, titles[1]);
        String title3 = titles.length>2?titles[2]:none;
        gd.addChoice("Blue:", titles, title3);
        String title4 = titles.length>3?titles[3]:none;
        gd.addChoice("Gray:", titles, title4);
		gd.addCheckbox("Create Composite", createComposite);
        gd.addCheckbox("Keep Source Images", false);
		gd.showDialog();
        if (gd.wasCanceled())
            return;
        int[] index = new int[4];
        index[0] = gd.getNextChoiceIndex();
        index[1] = gd.getNextChoiceIndex();
        index[2] = gd.getNextChoiceIndex();
        index[3] = gd.getNextChoiceIndex();
        createComposite = gd.getNextBoolean();
        boolean keep = gd.getNextBoolean();

        IjxImagePlus[] image = IJ.getFactory().newImagePlusArray(4);
        int stackSize = 0;
        int width = 0;
        int height = 0;
        for (int i=0; i<4; i++) {
            if (index[i]<wList.length) {
                image[i] = WindowManager.getImage(wList[index[i]]);
                width = image[i].getWidth();
                height = image[i].getHeight();
                stackSize = image[i].getStackSize();
            }
        }
        if (width==0) {
            IJ.error("There must be at least one source image or stack.");
            return;
        }
        for (int i=0; i<4; i++) {
            IjxImagePlus img = image[i];
            if (img!=null) {
                if (img.getStackSize()!=stackSize) {
                    IJ.error("The source stacks must all have the same number of slices.");
                    return;
                }
                if (img.isHyperStack()) {
              		if (img.isComposite()) {
              			CompositeImage ci = (CompositeImage)img;
              			if (ci.getMode()!=CompositeImage.COMPOSITE) {
              				ci.setMode(CompositeImage.COMPOSITE);
              				img.updateAndDraw();
              				if (!IJ.isMacro()) IJ.run("Channels Tool...");
              				return;
              			}
              		}
                    IJ.error("Source stacks cannot be hyperstacks.");
                    return;
                }
                if (img.getWidth()!=width || image[i].getHeight()!=height) {
                    IJ.error("The source images or stacks must have the same width and height.");
                    return;
                }
                if (createComposite) {
					for (int j=0; j<4; j++) {
						if (j!=i && image[j]!=null && img==image[j])
							createComposite = false;
					}
                }
            }
        }

		IjxImageStack red = image[0]!=null?image[0].getStack():null;
		IjxImageStack green = image[1]!=null?image[1].getStack():null;
		IjxImageStack blue = image[2]!=null?image[2].getStack():null;
		IjxImageStack gray = image[3]!=null?image[3].getStack():null;
		String options = Macro.getOptions();
		if  (options!=null && options.indexOf("gray=")==-1)
			gray = null; // ensure compatibility with old macros
        IjxImagePlus imp2;
        if (gray!=null)
        	createComposite = true;
        for (int i=0; i<4; i++) {
        	if (image[i]!=null && image[i].getBitDepth()==24)
        		createComposite = false;
        }
		if (createComposite) {
			IjxImageStack[] stacks = IJ.getFactory().newImageStackArray(4);
            
			stacks[0]=red; stacks[1]=green; stacks[2]=blue; stacks[3]=gray;
			imp2 = createComposite(width, height, stackSize, stacks, keep);
			if (imp2==null) return;
		} else {
			IjxImageStack rgb = mergeStacks(width, height, stackSize, red, green, blue, keep);
			imp2 = IJ.getFactory().newImagePlus("RGB", rgb);
		}
		if (image[0]!=null)
			imp2.setCalibration(image[0].getCalibration());
		if (!keep) {
			for (int i=0; i<4; i++) {
				if (image[i]!=null) {
					image[i].setChanged(false);
					image[i].close();
				}
			}
		}
		imp2.show();
     }
    
	public IjxImagePlus createComposite(int w, int h, int d, IjxImageStack[] stacks, boolean keep) {
		IjxImageStack composite = IJ.getFactory().newImageStack(w, h);
		int n = stacks.length;
		int[] index = new int[n];
		int channels = 0;
		boolean customColors = false;
		for (int i=0; i<n; i++) {
			index[i] = 1;
			if (stacks[i]!=null) {
				channels++;
				if (i>0 && stacks[i-1]==null)
					customColors = true;
			}
		}
		if (channels<2) {
            IJ.error("At least 2 channels required");
            return null;
		}
		for (int i=0; i<d; i++) {
			for (int j=0; j<n; j++) {
				if (stacks[j]!=null) {
					ImageProcessor ip = stacks[j].getProcessor(index[j]);
					if (keep) ip = ip.duplicate();
					composite.addSlice(null, ip);
					if (keep)
						index[j]++;
					else
						if (stacks[j]!=null) stacks[j].deleteSlice(1);
				}
			}
		}
		IjxImagePlus imp2 = IJ.getFactory().newImagePlus("Composite", composite);
		imp2.setDimensions(channels, d, 1);
		imp2 = new CompositeImage(imp2, CompositeImage.COMPOSITE);
		if (customColors) {
			Color[] colors = {Color.red, Color.green, Color.blue, Color.white};
			CompositeImage ci = (CompositeImage)imp2;
			int color = 0;
			int c = 1;
			for (int i=0; i<n; i++) {
				if (stacks[i]!=null && c<=n) {
					ci.setPosition(c, 1, 1);
					LUT lut = ci.createLutFromColor(colors[color]);
					ci.setChannelLut(lut);
					c++;
				}
				color++;
			}
			ci.setPosition(1, 1, 1);
		}
		if (d>1) imp2.setOpenAsHyperStack(true);
		return imp2;
	}

    public IjxImageStack mergeStacks(int w, int h, int d, IjxImageStack red, IjxImageStack green, IjxImageStack blue, boolean keep) {
        IjxImageStack rgb =IJ.getFactory().newImageStack(w, h);
        int inc = d/10;
        if (inc<1) inc = 1;
        ColorProcessor cp;
        int slice = 1;
        blank = new byte[w*h];
        byte[] redPixels, greenPixels, bluePixels;
        boolean invertedRed = red!=null?red.getProcessor(1).isInvertedLut():false;
        boolean invertedGreen = green!=null?green.getProcessor(1).isInvertedLut():false;
        boolean invertedBlue = blue!=null?blue.getProcessor(1).isInvertedLut():false;
        try {
            for (int i=1; i<=d; i++) {
            	cp = new ColorProcessor(w, h);
                redPixels = getPixels(red, slice, 0);
                greenPixels = getPixels(green, slice, 1);
                bluePixels = getPixels(blue, slice, 2);
                if (invertedRed) redPixels = invert(redPixels);
                if (invertedGreen) greenPixels = invert(greenPixels);
                if (invertedBlue) bluePixels = invert(bluePixels);
                cp.setRGB(redPixels, greenPixels, bluePixels);
            if (keep) {
                slice++;
            } else {
                    if (red!=null) red.deleteSlice(1);
                if (green!=null &&green!=red) green.deleteSlice(1);
                if (blue!=null&&blue!=red && blue!=green) blue.deleteSlice(1);
            }
            rgb.addSlice(null, cp);
            if ((i%inc) == 0) IJ.showProgress((double)i/d);
            }
        IJ.showProgress(1.0);
        } catch(OutOfMemoryError o) {
            IJ.outOfMemory("Merge Stacks");
            IJ.showProgress(1.0);
        }
        return rgb;
    }
    
     byte[] getPixels(IjxImageStack stack, int slice, int color) {
         if (stack==null)
            return blank;
        Object pixels = stack.getPixels(slice);
        if (!(pixels instanceof int[])) {
        	if (pixels instanceof byte[])
            	return (byte[])pixels;
            else {
            	ImageProcessor ip = stack.getProcessor(slice);
            	ip = ip.convertToByte(true);
            	return (byte[])ip.getPixels();
            }
        } else { //RGB
            byte[] r,g,b;
            int size = stack.getWidth()*stack.getHeight();
            r = new byte[size];
            g = new byte[size];
            b = new byte[size];
            ColorProcessor cp = (ColorProcessor)stack.getProcessor(slice);
            cp.getRGB(r, g, b);
            switch (color) {
                case 0: return r;
                case 1: return g;
                case 2: return b;
            }
        }
        return null;
    }

    byte[] invert(byte[] pixels) {
        byte[] pixels2 = new byte[pixels.length];
        System.arraycopy(pixels, 0, pixels2, 0, pixels.length);
        for (int i=0; i<pixels2.length; i++)
            pixels2[i] = (byte)(255-pixels2[i]&255);
        return pixels2;
    }

}

