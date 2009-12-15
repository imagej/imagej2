import ij.*;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import ij.gui.GenericDialog;
import ij.util.StringSorter;
import java.awt.*;
import java.util.Vector;

/** This sample ImageJ plugin filter blends an image with another one,
 *  i.e., it adds another image with user-specified weight.  */
/*
A few things to note:
    1) Filter plugins must implement the PlugInFilter interface.
    2) User plugins do not use the package statement;
    3) Plugins residing in the "plugins" folder, and with at
       least one underscore in their name, are automatically
       installed in the PlugIns menu.
    4) Plugins can be installed in other menus by 
       packaging them as JAR files.
    5) The class name ("Blend_Images") and file name 
       ("Blend_Images.java") must be the same.
    6) An ImagePlus is, roughly speaking, an image or stack that has a name
       and usually its own window. An ImageProcessor (ip) carries image data
       for a single image (grayscale or color), e.g. the image displayed in
       the window of an ImagePlus, or a single slice of a stack. Depending
       on the image type, an ImageProcesor can be a ByteProcessor (8 bit),
       ShortProcessor (16 bit), FloatProcessor (32 bit) or ColorProcessor (RGB).
    7) This filter works with selections, including non-rectangular
       selections. It is the programmer's responsibility to modify only
       the pixels within the ip.getRoi() rectangle; with the flag
       SUPPORTS_MASKING ImageJ takes care of out-of-roi pixels in that
       rectangle (for non-rectangular rois).
    8) The run method of a PlugInFilter will be called repeatedly to
       process all the slices in a stack if requested by the user.
    9) It supports Undo for single images. ImageJ takes care of this.
   10) This filter uses the following methods of the ImageProcessor class:
       ip.toFloat(i, fp), ip.setPixels(i, fp) and ip.getNChannels(),
       simplifying code that does float operations on grayscale images of
       any type or on the channels of color images. See the run(ip) method.
       Requires ImageJ 1.38m or later.
*/

public class Blend_Images implements PlugInFilter {
    /* Parameters from the dialog. These are declared static, so they are
     * remembered until ImageJ quits. */
    /** The name of the other image that will be added ("Image 2") */
    static String image2name = "";
    /** The weight of the image that the filter is applied to ("Image 1") */
    static float weight1;
    /** The weight of the other image, that will be added ("Image 2") */
    static float weight2 = 0.5f;
    /** Whether to set weight1 = 1. Otherwise weight1 = 1 - weight2 */
    static boolean fixWeight1 = false;
    /* Further class variables */
    /** The ImagePlus of the image (or ImageStack) to be filtered */
    private ImagePlus imp;
    /** The name of this PlugInFIlter (declaring it once reduces copy&paste errors) */
    private final static String plugInFilterName = "Blend Images";

    /**
     * This method is called by ImageJ for initialization.
     * @param arg Unused here; for plugins in a .jar file arguments can be
     * specified in the plugins.config file of the .jar archive.
     * @param imp The ImagePlus containing the image (or stack) to be processed
     * @return    The method returns flags (i.e., a bit mask) specifying the
     *            capabilities and needs of the filter. See PlugInFilter.java
     *            in the ImageJ sources for details.
     */
    public int setup(String arg, ImagePlus imp) {
        if (IJ.versionLessThan("1.38m")) {  // also generates an error message for older versions
            return DONE;
        } else {
            int flags = DOES_ALL|SUPPORTS_MASKING; // capabilities and needs of the filter.
            if (imp==null) {
                IJ.noImage();               // error message if no image is open
                return DONE;
            }
            this.imp = imp;                 // we need the ImagePlus as class variable to determine the image size&type
            if (!showDialog())
                return DONE;                // if dialog cancelled
            IJ.register(this.getClass());   // protect static class variables (parameters) from garbage collection
            return IJ.setupDialog(imp, flags); // for stacks ask "process all slices?"
        }
    }
    
    /**
     * This method is called by ImageJ once for a single image or repeatedly
     * for all images in a stack.
     * It creates a weighted sum (blend) of image ip and an image ip2 determined
     * previously in the dialog.
     * @param ip The image that should be processed
     */
    public void run(ImageProcessor ip) {
        ImagePlus imp2 = WindowManager.getImage(image2name);
        ImageProcessor ip2 = null;              // the image that we will blend ip with
        if (imp2 != null)
            ip2 = imp2.getProcessor();
        if (ip2 == null) 
            return;             // should never happen, we have imp2 from a list of suitable images
        FloatProcessor fp1 = null, fp2 = null;  // non-float images will be converted to these
        for (int i=0; i<ip.getNChannels(); i++) { //grayscale: once. RBG: once per color, i.e., 3 times
            fp1 = ip.toFloat(i, fp1);           // convert image or color channel to float (unless float already)
            fp2 = ip2.toFloat(i, fp2);
            blendFloat(fp1, weight1, fp2, weight2);
            ip.setPixels(i, fp1);               // convert back from float (unless ip is a FloatProcessor)
        }
    }

    /**
     * Blend a FloatProcessor (i.e., a 32-bit image) with another one, i.e.
     * set the pixel values of fp1 according to a weighted sum of the corresponding
     * pixels of fp1 and fp2. This is done for pixels in the rectangle fp1.getRoi()
     * only.
     * Note that both FloatProcessors, fp1 and fp2 must have be the same width and height.
     * @param fp1 The FloatProcessor that will be modified.
     * @param weight1 The weight of the pixels of fp1 in the sum.
     * @param fp2  The FloatProcessor that will be read only.
     * @param weight2 The weight of the pixels of fp2 in the sum.
     */
    public static void blendFloat(FloatProcessor fp1, float weight1, FloatProcessor fp2, float weight2) {
        Rectangle r = fp1.getRoi();
        int width = fp1.getWidth();
        float[] pixels1 = (float[])fp1.getPixels();     // array of the pixels of fp1
        float[] pixels2 = (float[])fp2.getPixels();
        for (int y=r.y; y<(r.y+r.height); y++)          // loop over all pixels inside the roi rectangle
            for (int x=r.x; x<(r.x+r.width); x++) {
                int i = x + y*width;                    // this is how the pixels are addressed
                pixels1[i] = weight1*pixels1[i] + weight2*pixels2[i]; //the weighted sum
            }
    }

    /**
     * The dialog asking for what exactly to do (the parameters)
     * @return Whether further processing should be done
     */
    boolean showDialog() {
        String[] suitableImages = getSuitableImages();  // images that we can blend with the current one
        if (suitableImages == null) {
            String type = imp.getBitDepth()==24?"RGB":"grayscale";
            IJ.error(plugInFilterName+" Error", "No suitable image ("+type+", "+imp.getWidth()+"x"+imp.getHeight()+") to blend with");
            return false;
        }
        GenericDialog gd = new GenericDialog(plugInFilterName+"...");
        gd.addMessage("Image 1: "+imp.getTitle());
        gd.addChoice("Image 2:", suitableImages, image2name);
        gd.addCheckbox("Fix Weight 1 = 1", fixWeight1);
        gd.addNumericField("Weight 2", weight2, 3);
        gd.showDialog();                            // user input (or reading from macro) happens here
        if (gd.wasCanceled())                       // dialog cancelled?
            return false;
        image2name = gd.getNextChoice();
        fixWeight1 = gd.getNextBoolean();
        weight2 = (float)gd.getNextNumber();
        if (fixWeight1)
            weight1 = 1f;
        else
            weight1 = 1f - weight2;
        return true;
    }

    /**
     * Get a list of open images with the same size and channel number as the current
     * ImagePlus (channel number is 1 for grayscale, 3 for RGB). The current image is
     * not entered in to the list.
     * @return A sorted list of the names of the images. Duplicate names are listed only once.
     */
    String[] getSuitableImages() {
        int width = imp.getWidth();          // determine properties of the current image
        int height = imp.getHeight();
        int channels = imp.getProcessor().getNChannels();
        int thisID = imp.getID();
        int[] fullList = WindowManager.getIDList();//IDs of all open image windows
        Vector suitables = new Vector(fullList.length); //will hold names of suitable images
        for (int i=0; i<fullList.length; i++) { // check images for suitability, make condensed list
            ImagePlus imp2 = WindowManager.getImage(fullList[i]);
            if (imp2.getWidth()==width && imp2.getHeight()==height &&
                    imp2.getProcessor().getNChannels()==channels && fullList[i]!=thisID) {
                String name = imp2.getTitle();  // found suitable image
                if (!suitables.contains(name))  // enter only if a new name
                    suitables.addElement(name);
            }
        }
        if (suitables.size() == 0)
            return null;                        // nothing found
        String[] suitableImages = new String[suitables.size()];
        for (int i=0; i<suitables.size(); i++)  // vector to array conversion
            suitableImages[i] = (String)suitables.elementAt(i);
        StringSorter.sort(suitableImages);
        return suitableImages;
    }
}
