package ij.plugin.filter;
import ijx.IjxImagePlus;
import ij.*;
import ij.plugin.*;
import ij.process.*;
import ij.gui.*;

/**
 * This plugin implements the Euclidean Distance Map (EDM), Ultimate Eroded Points and
 * Watershed commands in the Process/Binary submenu.
 * Note: These functions do not take ROIs into account (any ROI gets deselected).
 * setup is called with no argument for EDM, "points" for ultimate eroded points and "watershed"
 * for watershed segmentation.
 * Ultimate Eroded Points and Watershed are handled by the MaximumFinder 
 * plugin applied to the EDM
 * 
 * version 09-Nov-2006 Michael Schmid
 */
	public class EDM implements IjxPlugInFilter {
    /** unit in 16-bit EDM image: this value corresponds to a distance of one pixel */
    public static final int ONE = 41;
    /** in 16-bit EDM image this value corresponds to a pixel distance of sqrt(2) */
    public static final int SQRT2 = 58; // ~ 41 * sqrt(2)
    /** in 16-bit EDM image this value corresponds to a pixel distance of sqrt(2) */
    public static final int SQRT5 = 92; // ~ 41 * sqrt(5)

    IjxImagePlus imp;
    String arg;
    int slice;                                  // when processing a stack
    boolean invertImage;
    
    public int setup(String arg, IjxImagePlus imp) {
        this.imp = imp;
        this.arg = arg;
        if (imp != null) {
            boolean invertedLut = imp.isInvertedLut();
            invertImage = (invertedLut && Prefs.blackBackground) || (!invertedLut && !Prefs.blackBackground);
        }
        return IJ.setupDialog(imp, DOES_8G);
    }
    
    public void run(ImageProcessor ip) {
        imp.killRoi();                          //otherwise invert won't work as expected
        int[] histogram;
        slice++;
        ImageStatistics stats = imp.getStatistics();
        if (slice==1 && stats.histogram[0]+stats.histogram[255]!=stats.pixelCount) {
            IJ.error("8-bit binary image (0 and 255) required.");
            return;
        }

        if (invertImage)
            ip.invert();                            // independent of settings, now 255=foreground, 0=background
        if (arg.equals("points")||arg.equals("watershed")) {
            int outputType;
            if (arg.equals("watershed")) {
                outputType = MaximumFinder.SEGMENTED;
            } else {                                // output ultimate points: one per maximum
                outputType = MaximumFinder.SINGLE_POINTS;
            }
            ImageProcessor ip16 = make16bitEDM(ip);
            //new ImagePlus("16-bit EDM",ip16).show();
            MaximumFinder fm = new MaximumFinder();

            //setting the tolerance to lower values such as 0.3*ONE creates more segments,
            //larger values like 0.7*ONE give fewer segments
            //reasonable values are 0.3 ... 0.8 * ONE.
            ByteProcessor maxIp = fm.findMaxima(ip16, 0.5*ONE, ImageProcessor.NO_THRESHOLD, outputType, false, true);
            if (maxIp != null) {                    //no further action if cancelled by user
                if (arg.equals("watershed")) {
                    ip.copyBits(maxIp, 0, 0, Blitter.COPY);
                    
                } else {                            // output ultimate points
                    byte[] image8 = (byte[])ip.getPixels();
                    short[] image16 = (short[])ip16.getPixels();
                    convertToBytes(image16, image8);
                    ip.copyBits(maxIp, 0, 0, Blitter.AND); // maxima are 255 and keep the EDM value
                }
            }
        }
        else {                                      // if output should be EDM
            toEDM(ip);
        }
        if (invertImage)
            ip.invert();                            // back to black foreground (unless settings say reverse)
    } //public void run
    
    
    
    /**	Converts a binary image into a 8-bit grayscale Euclidean Distance Map
     * (EDM). Each foreground (zero) pixel in the binary image is
     * assigned a value equal to its distance from the nearest
     * background (255) pixel.
     */
    public void toEDM(ImageProcessor ip) {
        ImageProcessor ip16 = make16bitEDM(ip);
        byte[] image8 = (byte[])ip.getPixels();
        short[] image16 = (short[])ip16.getPixels();
        convertToBytes(image16, image8);
    }
        
    /**	Calculates a 16-bit grayscale Euclidean Distance Map for a binary 8-bit image.
     * Each foreground (black) pixel in the binary image is
     * assigned a value equal to its distance from the nearest
     * background (white) pixel.  Uses the two-pass EDM algorithm
     * from the "Image Processing Handbook" by John Russ.
     */
    public ShortProcessor make16bitEDM(ImageProcessor ip) {
        int xmax, ymax;
        int offset, rowsize;
        
        IJ.showStatus("Generating EDM");
        int width = imp.getWidth();
        int height = imp.getHeight();
        rowsize = width;
        xmax    = width - 2;
        ymax    = height - 2;
        ShortProcessor ip16 = (ShortProcessor)ip.convertToShort(false);
        ip16.multiply(128); //foreground pixels set (almost) as high as possible for signed short
        short[] image16 = (short[])ip16.getPixels();
        int inc = Math.max(height/50,1);
        
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                offset = x + y * rowsize;
                if (image16[offset] > 0) {
                    if ((x<=1) || (x>=xmax) || (y<=1) || (y>=ymax))
                        setEdgeValue(offset, rowsize, image16, x, y, xmax, ymax);
                    else
                        setValue(offset, rowsize, image16);
                }
            } // for x
            if ((inc&0)==0) IJ.showProgress(y/2, height);
        } // for y
        
        for (int y=height-1; y>=0; y--) {
            for (int x=width-1; x>=0; x--) {
                offset = x + y * rowsize;
                if (image16[offset] > 0) {
                    if ((x<=1) || (x>=xmax) || (y<=1) || (y>=ymax))
                        setEdgeValue(offset, rowsize, image16, x, y, xmax, ymax);
                    else
                        setValue(offset, rowsize, image16);
                }
            } // for x
            if ((inc&0)==0) IJ.showProgress(height/2+(height-y)/2, height);
        } // for y
        IJ.showProgress(1,1);
        //(new ImagePlus("EDM16", ip16.duplicate())).show();
        
        return ip16;
    } // make16bitEDM(ip)
    
    void setValue(int offset, int rowsize, short[] image16) {
        int  v;
        int r1  = offset - rowsize - rowsize - 2;
        int r2  = r1 + rowsize;
        int r3  = r2 + rowsize;
        int r4  = r3 + rowsize;
        int r5  = r4 + rowsize;
        int min = 32767;
        
        v = image16[r2 + 2] + ONE;
        if (v < min)
            min = v;
        v = image16[r3 + 1] + ONE;
        if (v < min)
            min = v;
        v = image16[r3 + 3] + ONE;
        if (v < min)
            min = v;
        v = image16[r4 + 2] + ONE;
        if (v < min)
            min = v;
        
        v = image16[r2 + 1] + SQRT2;
        if (v < min)
            min = v;
        v = image16[r2 + 3] + SQRT2;
        if (v < min)
            min = v;
        v = image16[r4 + 1] + SQRT2;
        if (v < min)
            min = v;
        v = image16[r4 + 3] + SQRT2;
        if (v < min)
            min = v;
        
        v = image16[r1 + 1] + SQRT5;
        if (v < min)
            min = v;
        v = image16[r1 + 3] + SQRT5;
        if (v < min)
            min = v;
        v = image16[r2 + 4] + SQRT5;
        if (v < min)
            min = v;
        v = image16[r4 + 4] + SQRT5;
        if (v < min)
            min = v;
        v = image16[r5 + 3] + SQRT5;
        if (v < min)
            min = v;
        v = image16[r5 + 1] + SQRT5;
        if (v < min)
            min = v;
        v = image16[r4] + SQRT5;
        if (v < min)
            min = v;
        v = image16[r2] + SQRT5;
        if (v < min)
            min = v;
        
        image16[offset] = (short)min;
        
    } // setValue()
    
    void setEdgeValue(int offset, int rowsize, short[] image16, int x, int y, int xmax, int ymax) {
        int  v;
        int r1 = offset - rowsize - rowsize - 2;
        int r2 = r1 + rowsize;
        int r3 = r2 + rowsize;
        int r4 = r3 + rowsize;
        int r5 = r4 + rowsize;
        int min = 32767;
        int offimage = image16[r3 + 2];
        
        if (y<1)
            v = offimage + ONE;
        else
            v = image16[r2 + 2] + ONE;
        if (v < min)
            min = v;
        
        if (x<1)
            v = offimage + ONE;
        else
            v = image16[r3 + 1] + ONE;
        if (v < min)
            min = v;
        
        if (x>xmax)
            v = offimage + ONE;
        else
            v = image16[r3 + 3] + ONE;
        if (v < min)
            min = v;
        
        if (y>ymax)
            v = offimage + ONE;
        else
            v = image16[r4 + 2] + ONE;
        if (v < min)
            min = v;
        
        if ((x<1) || (y<1))
            v = offimage + SQRT2;
        else
            v = image16[r2 + 1] + SQRT2;
        if (v < min)
            min = v;
        
        if ((x>xmax) || (y<1))
            v = offimage + SQRT2;
        else
            v = image16[r2 + 3] + SQRT2;
        if (v < min)
            min = v;
        
        if ((x<1) || (y>ymax))
            v = offimage + SQRT2;
        else
            v = image16[r4 + 1] + SQRT2;
        if (v < min)
            min = v;
        
        if ((x>xmax) || (y>ymax))
            v = offimage + SQRT2;
        else
            v = image16[r4 + 3] + SQRT2;
        if (v < min)
            min = v;
        
        if ((x<1) || (y<=1))
            v = offimage + SQRT5;
        else
            v = image16[r1 + 1] + SQRT5;
        if (v < min)
            min = v;
        
        if ((x>xmax) || (y<=1))
            v = offimage + SQRT5;
        else
            v = image16[r1 + 3] + SQRT5;
        if (v < min)
            min = v;
        
        if ((x>=xmax) || (y<1))
            v = offimage + SQRT5;
        else
            v = image16[r2 + 4] + SQRT5;
        if (v < min)
            min = v;
        
        if ((x>=xmax) || (y>ymax))
            v = offimage + SQRT5;
        else
            v = image16[r4 + 4] + SQRT5;
        if (v < min)
            min = v;
        
        if ((x>xmax) || (y>=ymax))
            v = offimage + SQRT5;
        else
            v = image16[r5 + 3] + SQRT5;
        if (v < min)
            min = v;
        
        if ((x<1) || (y>=ymax))
            v = offimage + SQRT5;
        else
            v = image16[r5 + 1] + SQRT5;
        if (v < min)
            min = v;
        
        if ((x<=1) || (y>ymax))
            v = offimage + SQRT5;
        else
            v = image16[r4] + SQRT5;
        if (v < min)
            min = v;
        
        if ((x<=1) || (y<1))
            v = offimage + SQRT5;
        else
            v = image16[r2] + SQRT5;
        if (v < min)
            min = v;
        
        image16[offset] = (short)min;
        
    } // setEdgeValue()
    
    /**convert 16-bit EDM to 8 bits*/
    void convertToBytes(short[] image16, byte[] image8) {
        int v;
        int round = ONE / 2;

        for (int i=0; i<image16.length; i++) {
                v = (image16[i] + round) / ONE;
                if (v > 255)
                    v = 255;
                image8[i] = (byte) v;
        } // for i
    } // end ConvertToBytes()
}
