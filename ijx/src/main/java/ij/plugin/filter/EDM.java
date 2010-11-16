package ij.plugin.filter;
import ij.*;
import ij.process.*;
import ijx.IjxImagePlus;
import ijx.IjxImageStack;

/**
 * This plugin implements the Euclidean Distance Map (EDM), Watershed,
 * Ultimate Eroded Points and Voronoi commands in the Process/Binary submenu.
 *
 * - Euclidean Distance Map: The value of each pixel is the distance to the nearest
 *   background pixel (for background pixels, the EDM is 0)
 * - Ultimate Eroded Points  (UEPs) are maxima of the EDM. In the output, the points
 *   are assigned the EDM value, which is equal to the radius of the largest circle
 *   that fits into the particle, with the UEP as the center.
 * - Watershed segmentation of the EDM splits particles at "necks"; starting at
 *   maxima of the EDM.
 * - 'Voronoi' splits the image by lines of points having equal distance to the
 *   borders of the two nearest particles. Thus, the Voronoi cell of each particle
 *   includes all points that are nearer to this particle than any other particle.
 *   For the case of the priticles being single points, this is a Voronoi tessellation
 *   (also known as Dirichlet tessellation).
 *   In the output, the value inside the Voronoi cells is zero; the pixel values
 *   of the dividing lines between the cells are equal to the distance to the two
 *   nearest particles. This is similar to a medial axis transform of the background,
 *   but there are no lines in inner holes of particles.
 *
 * Watershed, Ultimate Eroded Points and Voronoi are handled by the MaximumFinder 
 * plugin applied to the EDM
 * Note: These functions do not take ROIs into account.
 * Setup is called with argument "" (empty string) for EDM,
 * "watershed" for watershed segmentation, "points" for ultimate eroded points and
 * "voronoi" for Voronoi segmentation of the background
 *
 * The EDM algorithm is similar to the 8SSEDT in
 *   F. Leymarie, M. D. Levine, in: CVGIP Image Understanding, vol. 55 (1992), pp 84-94
 *   http://dx.doi.org/10.1016/1049-9660(92)90008-Q
 *
 * The algorithm provides a fast approximation of the EDM, with the deviation from a
 * full calculation being between -0.09 and 0. The algorithm is exact for distances<13.
 * For d>=13, deviations from the true result can occur, but are very rare: typically
 * the fraction of pixels deviating from the exact result is in the 10^-5 range, with
 * most deviations between -0.03 and -0.04.
 *
 * Limitations:
 * Maximum image diagonal for EDM: 46340 pixels (sqrt(2^31)); if the particles are
 * dense enough it also works for width, height <=65534.
 *
 * Version 30-Apr-2008 Michael Schmid:  more accurate EDM algorithm,
 *                                      16-bit and float output possible,
 *                                      parallel processing for stacks
 *                                      Voronoi output added
 */
public class EDM implements ExtendedPlugInFilter {
    /** Output type: overwrite current 8-bit image */
    public static final int BYTE_OVERWRITE = 0;
    /** Output type: new 8-bit image */
    public static final int BYTE = 1;
    /** Output type: new 16-bit image */
    public static final int SHORT = 2;
    /** Output type: new 32-bit image */
    public static final int FLOAT = 3;
    /** Unit in old make16bitEDM: this pixel value corresponds to a distance of one pixel.
     *  For compatibility only. */
    public static final int ONE = 41;
    /** In old make16bitEDM this pixel value corresponds to a pixel distance of sqrt(2) */
    public static final int SQRT2 = 58; // ~ 41 * sqrt(2)
    /** In old make16bitEDM this pixel value corresponds to a pixel distance of sqrt(5) */
    public static final int SQRT5 = 92; // ~ 41 * sqrt(5)

    private IjxImagePlus imp;              //input
    private IjxImagePlus outImp;           //output if a new window is desired
    private PlugInFilterRunner pfr;     //needed to extract the stack slice if needed
    private String command;             //for showing status
    private int outImageType;           //output type; BYTE_OVERWRITE, BYTE, SHORT or FLOAT
    private IjxImageStack outStack;        //in case output should be a new stack
    private int processType;            //can be EDM, WATERSHED, UEP, VORONOI
    private MaximumFinder maxFinder = new MaximumFinder();    //we use only one MaximumFinder (nice progress bar)
    private double progressDone;        //for progress bar, fraction of work done so far
    private int nPasses;                //for progress bar, how many images to process (sequentially or parallel threads)
    private boolean interrupted;        //whether watershed segmentation has been interrrupted by the user

    private boolean background255;      //whether background for EDM is 255, not zero
    private int flags = DOES_8G | PARALLELIZE_STACKS | FINAL_PROCESSING;
    //processType can be:
    private static final int EDM = 0, WATERSHED = 1, UEP = 2, VORONOI = 3;
    //whether MaximumFinder is needed for processType:
    private static final boolean[] USES_MAX_FINDER = new boolean[] {
            false, true, true, true };
    //whether watershed segmentation is needed for processType:
    private static final boolean[] USES_WATERSHED = new boolean[] {
            false, true, false, true };
    //prefixes for titles of separate output images; for each processType:
    private static final String[] TITLE_PREFIX = new String[] {
            "EDM of ", null, "UEPs of ", "Voronoi of "};
    private static final int NO_POINT = -1; //no nearest point in array of nearest points
    private static final double MAXFINDER_TOLERANCE = 0.5; //reasonable values are 0.3 ... 0.8;
                                    //segmentation is more aggressive with smaller values
    /** Output type (BYTE_OVERWRITE, BYTE, SHORT or FLOAT) */
    private static int outputType = BYTE_OVERWRITE;

    /** Prepare for processing; also called at the very end with argument 'final'
     *  to show any newly created output image.
     */
    public int setup (String arg, IjxImagePlus imp) {
        if (arg.equals("final")) {
            showOutput();
            return DONE;
        }
        this.imp = imp;
        //'arg' is processing type; default is 'EDM' (0)
        if (arg.equals("watershed")) {
            processType = WATERSHED;
            flags += KEEP_THRESHOLD;
        } else if (arg.equals("points"))
            processType = UEP;
        else if (arg.equals("voronoi"))
            processType = VORONOI;

        //output type
        if (processType != WATERSHED)           //Watershed always has output BYTE_OVERWRITE=0
            outImageType = outputType;          //otherwise use the static variable from setOutputType
        if (outImageType != BYTE_OVERWRITE)
            flags |= NO_CHANGES;

        //check image and prepare
        if (imp != null) {
            ImageProcessor ip = imp.getProcessor();
            if (!ip.isBinary()) {
                IJ.error("8-bit binary image (0 and 255) required.");
                return DONE;
            }
            ip.resetRoi();
            //processing routines assume background=0; image may be otherwise
            boolean invertedLut = imp.isInvertedLut();
            background255 = (invertedLut && Prefs.blackBackground) || (!invertedLut && !Prefs.blackBackground);
        }
        return flags;
    } //public int setup

    /** Called by the PlugInFilterRunner after setup.
     *  Asks the user in case of a stack and prepares a separate ouptut stack if required
     */

    public int showDialog (IjxImagePlus imp, String command, PlugInFilterRunner pfr) {
        this.pfr = pfr;
        int width = imp.getWidth();
        int height= imp.getHeight();
        //ask whether to process all slices of stack & prepare stack
        //(if required) for writing into it in parallel threads
        flags = IJ.setupDialog(imp, flags);
        if ((flags&DOES_STACKS)!=0 && outImageType!=BYTE_OVERWRITE) {
            outStack = IJ.getFactory().newImageStack(width, height, imp.getStackSize());
            maxFinder.setNPasses(imp.getStackSize());
        }
        return flags;
    } //public int showDialog

    /** Called by the PlugInFilterRunner to process the image or one frame of a stack */
    public void run (ImageProcessor ip) {
        if (interrupted) return;
        int width = ip.getWidth();
        int height = ip.getHeight();

        int backgroundValue = (processType==VORONOI) ?
                (background255 ? 0 : (byte)255) : //Voronoi needs EDM of the background
                (background255 ? (byte)255 : 0);  //all others do EDM of the foreground
        if (USES_WATERSHED[processType]) nPasses = 0; //watershed has its own progress bar
        FloatProcessor floatEdm = makeFloatEDM(ip, backgroundValue, false);

        ByteProcessor maxIp = null;
        if (USES_MAX_FINDER[processType]) {
            if (processType == VORONOI) floatEdm.multiply(-1); //Voronoi starts from minima of EDM
            int maxOutputType = USES_WATERSHED[processType] ? MaximumFinder.SEGMENTED : MaximumFinder.SINGLE_POINTS;
            boolean isEDM = processType!=VORONOI;
            maxIp = maxFinder.findMaxima(floatEdm, MAXFINDER_TOLERANCE,
                ImageProcessor.NO_THRESHOLD, maxOutputType, false, isEDM);
            if (maxIp == null) {  //segmentation cancelled by user?
                interrupted = true;
                return;
            } else if (processType != WATERSHED) {
                if (processType == VORONOI) floatEdm.multiply(-1);
                resetMasked(floatEdm, maxIp, processType == VORONOI ? -1 : 0);
            }
        }

        ImageProcessor outIp = null;
        if (processType==WATERSHED) {
            if (background255) maxIp.invert();
            ip.copyBits(maxIp, 0, 0, Blitter.COPY);
            ip.setBinaryThreshold();
        } else switch (outImageType) {          //for all these, output contains the values of the EDM
            case FLOAT:
                outIp = floatEdm;
                break;
            case SHORT:
                floatEdm.setMinAndMax(0., 65535.);
                outIp = floatEdm.convertToShort(true);
                break;
            case BYTE:
                floatEdm.setMinAndMax(0., 255.);
                outIp = floatEdm.convertToByte(true);
                break;
            case BYTE_OVERWRITE:
                ip.setPixels(0, floatEdm);
                if (floatEdm.getMax() > 255.)
                    ip.resetMinAndMax();        //otherwise we have max of floatEdm
        }

        if (outImageType != BYTE_OVERWRITE) {   //new output image
            if (outStack==null) {
                outImp = IJ.getFactory().newImagePlus(TITLE_PREFIX[processType]+imp.getShortTitle(), outIp);
            } else
                outStack.setPixels(outIp.getPixels(), pfr.getSliceNumber());
        }
    } //public void run

    /** Prepare the progress bar.
     *  Without calling it or if nPasses=0, no progress bar will be shown.
     *  @param nPasses Number of images that this EDM will process.
     */
    public void setNPasses (int nPasses) {
        this.nPasses = nPasses;
        progressDone = 0;
        if (USES_MAX_FINDER[processType]) maxFinder.setNPasses(nPasses);
    }

    /** Converts a binary image into a 8-bit grayscale Euclidean Distance Map
     *  (EDM). Each foreground (nonzero) pixel in the binary image is
     *  assigned a value equal to its distance from the nearest
     *  background (zero) pixel.
     */
    public void toEDM (ImageProcessor ip) {
        ip.setPixels(0, makeFloatEDM(ip, 0, false));
        ip.resetMinAndMax();
    }

    /** Do watershed segmentation based on the EDM of the
     *  foreground objects (nonzero pixels) in an 8-bit image.
     *  Particles are segmented by their shape; segmentation
     *  lines added are background pixels (value = 0);
     */
    public void toWatershed (ImageProcessor ip) {
        FloatProcessor floatEdm = makeFloatEDM(ip, 0, false);
        ByteProcessor maxIp = maxFinder.findMaxima(floatEdm, MAXFINDER_TOLERANCE,
                ImageProcessor.NO_THRESHOLD, MaximumFinder.SEGMENTED, false, true);
        if (maxIp != null) ip.copyBits(maxIp, 0, 0, Blitter.AND);
    }

    /** Calculates a 16-bit grayscale Euclidean Distance Map for a binary 8-bit image.
     * Each foreground (nonzero) pixel in the binary image is assigned a value equal to
     * its distance from the nearest background (zero) pixel, multiplied by EDM.ONE.
     * For compatibility with previous versions of ImageJ only.
     */
    public ShortProcessor make16bitEDM (ImageProcessor ip) {
        FloatProcessor floatEdm = makeFloatEDM(ip, 0, false);
        floatEdm.setMinAndMax(0, 65535./ONE);
        return (ShortProcessor)floatEdm.convertToShort(true);
    }

    /**
     * Creates the Euclidian Distance Map of a (binary) byte image.
     * @param ip                The input image, not modified; must be a ByteProcessor.
     * @param backgroundValue   Pixels in the input with this value are interpreted as background.
     *                          Note: for pixel value 255, write either -1 or (byte)255.
     * @param edgesAreBackground Whether out-of-image pixels are considered background
     * @return                  The EDM, containing the distances to the nearest background pixel.
     *                          Returns null if the thread is interrupted.
     */
    public FloatProcessor makeFloatEDM (ImageProcessor ip, int backgroundValue, boolean edgesAreBackground) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        FloatProcessor fp = new FloatProcessor(width, height);
        byte[] bPixels = (byte[])ip.getPixels();
        float[] fPixels = (float[])fp.getPixels();
        final int progressInterval = 100;
        int nProgressUpdates = height/progressInterval;  //how often the progress bar is updated when passing once through y range
        double progressAddendum = (nProgressUpdates>0) ? 0.5/nProgressUpdates : 0;

        for (int i=0; i<width*height; i++)
            if (bPixels[i]!=backgroundValue) fPixels[i] = Float.MAX_VALUE;

        int[][] pointBufs = new int[2][width];  //two buffers for two passes; low short contains x, high short y
        int yDist = Integer.MAX_VALUE;          //this value is used only if edges are not background
        // pass 1 & 2: increasing y
        for (int x=0; x<width; x++) {
            pointBufs[0][x] = NO_POINT;
            pointBufs[1][x] = NO_POINT;
        }
        for (int y=0; y<height; y++) {
            if (edgesAreBackground) yDist = y+1; //distance to nearest background point (along y)
            edmLine(bPixels, fPixels, pointBufs, width, y*width, y, backgroundValue, yDist);
            if (y%progressInterval == 0) {
                if (Thread.currentThread().isInterrupted()) return null;
                addProgress(progressAddendum);
            }
        }
        //pass 3 & 4: decreasing y
        for (int x=0; x<width; x++) {
            pointBufs[0][x] = NO_POINT;
            pointBufs[1][x] = NO_POINT;
        }
        for (int y=height-1; y>=0; y--) {
            if (edgesAreBackground) yDist = height-y;
            edmLine(bPixels, fPixels, pointBufs, width, y*width, y, backgroundValue, yDist);
            if (y%progressInterval == 0) {
                if (Thread.currentThread().isInterrupted()) return null;
                addProgress(progressAddendum);
            }
        }

        fp.sqrt();
        return fp;
    } //public FloatProcessor makeFloatEDM

    // Handle a line; two passes: left-to-right and right-to-left
    private void edmLine(byte[] bPixels, float[] fPixels, int[][] pointBufs, int width,
            int offset, int y, int backgroundValue, int yDist) {
        int[] points = pointBufs[0];        // the buffer for the left-to-right pass
        int pPrev = NO_POINT;
        int pDiag = NO_POINT;               // point at (-/+1, -/+1) to current one (-1,-1 in the first pass)
        int pNextDiag;
        boolean edgesAreBackground = yDist != Integer.MAX_VALUE;
        int distSqr = Integer.MAX_VALUE;    // this value is used only if edges are not background
        for (int x=0; x<width; x++, offset++) {
            pNextDiag = points[x];
            if (bPixels[offset] == backgroundValue) {
                points[x] = x | y<<16;      // remember coordinates as a candidate for nearest background point
            } else {                        // foreground pixel:
                if (edgesAreBackground)
                    distSqr = (x+1 < yDist) ? (x+1)*(x+1) : yDist*yDist; //distance from edge
                float dist2 = minDist2(points, pPrev, pDiag, x, y, distSqr);
                if (fPixels[offset] > dist2) fPixels[offset] = dist2;
            }
            pPrev = points[x];
            pDiag = pNextDiag;
        }
        offset--; //now points to the last pixel in the line
        points = pointBufs[1];              // the buffer for the right-to-left pass. Low short contains x, high short y
        pPrev = NO_POINT;
        pDiag = NO_POINT;
        for (int x=width-1; x>=0; x--, offset--) {
            pNextDiag = points[x];
            if (bPixels[offset] == backgroundValue) {
                points[x] = x | y<<16;      // remember coordinates as a candidate for nearest background point
            } else {                        // foreground pixel:
                if (edgesAreBackground)
                    distSqr = (width-x < yDist) ? (width-x)*(width-x) : yDist*yDist;
                float dist2 = minDist2(points, pPrev, pDiag, x, y, distSqr);
                if (fPixels[offset] > dist2) fPixels[offset] = dist2;
            }
            pPrev = points[x];
            pDiag = pNextDiag;
        }
    } //private void edmLine

    // Calculates minimum distance^2 of x,y from the following three points:
    //  - points[x] (nearest point found for previous line, same x)
    //  - pPrev (nearest point found for same line, previous x), and
    //  - pDiag (nearest point found for diagonal, i.e., previous line, previous x)
    // Sets array element points[x] to the coordinates of the point having the minimum distance to x,y
    // If the distSqr parameter is lower than the distance^2, then distSqr is used
    // Returns to the minimum distance^2 obtained
    private float minDist2 (int[] points, int pPrev, int pDiag, int x, int y, int distSqr) {
        int p0 = points[x];              // the nearest background point for the same x in the previous line
        int nearestPoint = p0;
        if (p0 != NO_POINT) {
            int x0 = p0& 0xffff; int y0 = (p0>>16)&0xffff;
            int dist1Sqr = (x-x0)*(x-x0)+(y-y0)*(y-y0);
            if (dist1Sqr < distSqr)
                distSqr = dist1Sqr;
        }
        if (pDiag!=p0 && pDiag!=NO_POINT) {
            int x1 = pDiag&0xffff; int y1 = (pDiag>>16)&0xffff;
            int dist1Sqr = (x-x1)*(x-x1)+(y-y1)*(y-y1);
            if (dist1Sqr < distSqr) {
                nearestPoint = pDiag;
                distSqr = dist1Sqr;
            }
        }
        if (pPrev!=pDiag && pPrev!=NO_POINT) {
            int x1 = pPrev& 0xffff; int y1 = (pPrev>>16)&0xffff;
            int dist1Sqr = (x-x1)*(x-x1)+(y-y1)*(y-y1);
            if (dist1Sqr < distSqr) {
                nearestPoint = pPrev;
                distSqr = dist1Sqr;
            }
        }
        points[x] = nearestPoint;
        return (float)distSqr;
    } //private float minDist2

    // overwrite ip with floatEdm converted to bytes
    private void byteFromFloat(ImageProcessor ip, FloatProcessor floatEdm) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        byte[] bPixels = (byte[])ip.getPixels();
        float[] fPixels = (float[])floatEdm.getPixels();
        for (int i=0; i<width*height; i++) {
            float v = fPixels[i];
            bPixels[i] = v<255f ? (byte)(v+0.5) : (byte)255;
        }
    }

    // set values in floatEdm to zero if pixel in mask equals 'resetOnThis'
    private void resetMasked(FloatProcessor floatEdm, ImageProcessor mask, int resetOnThis) {
        int width = mask.getWidth();
        int height = mask.getHeight();
        byte[] mPixels = (byte[])mask.getPixels();
        float[] fPixels = (float[])floatEdm.getPixels();
        for (int i=0; i<width*height; i++)
            if (mPixels[i] == resetOnThis) fPixels[i] = 0;
    }

    // at the very end - show output image (if the is a separate one)
    private void showOutput() {
        if (interrupted) return;
        if (outStack!=null) {
            outImp = IJ.getFactory().newImagePlus(TITLE_PREFIX[processType]+imp.getShortTitle(), outStack);
            int[] d = imp.getDimensions();
            outImp.setDimensions(d[2], d[3], d[4]);
            for (int i=1; i<=imp.getStackSize(); i++)
                outStack.setSliceLabel(imp.getStack().getSliceLabel(i), i);
        }
        if (outImageType != BYTE_OVERWRITE) {
            ImageProcessor ip = outImp.getProcessor();
            if (!Prefs.blackBackground) ip.invertLut();
            ip.resetMinAndMax();
            outImp.show();
        }
    }

    /** add work done in the meanwhile and show progress */
    private void addProgress(double deltaProgress) {
        if (nPasses == 0) return;
        progressDone += deltaProgress;
        IJ.showProgress(progressDone/nPasses);
    }
    
    /** Sets the output type (BYTE_OVERWRITE, BYTE, SHORT or FLOAT) */
    public static void setOutputType(int type) {
        if (type<BYTE_OVERWRITE || type>FLOAT)
            throw new IllegalArgumentException("Invalid type: "+type);
        outputType = type;
    }

    /** Returns the current output type (BYTE_OVERWRITE, BYTE, SHORT or FLOAT) */
    public static int getOutputType() {
        return outputType;
    }

}
