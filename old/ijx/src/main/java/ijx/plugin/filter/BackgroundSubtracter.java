package ijx.plugin.filter;
import ijx.process.ImageProcessor;
import ijx.process.FloatProcessor;
import ijx.process.ColorProcessor;
import ijx.process.ByteProcessor;
import ijx.process.ShortProcessor;
import ijx.plugin.api.PlugInFilterRunner;
import ijx.plugin.api.ExtendedPlugInFilter;
import ijx.gui.dialog.DialogListener;
import ijx.gui.dialog.GenericDialog;
import ijx.Macro;
import ijx.Prefs;
import ijx.IJ;



import imagej.util.Tools;
import ijx.IjxImagePlus;
import java.awt.*;


/** Implements ImageJ's Subtract Background command. Based on the concept of the
rolling ball algorithm described in Stanley Sternberg's article, "Biomedical Image
Processing", IEEE Computer, January 1983.

Imagine that the 2D grayscale image has a third (height) dimension by the image
value at every point in the image, creating a surface. A ball of given radius is
rolled over the bottom side of this surface; the hull of the volume reachable by
the ball is the background.

With "Sliding Parabvoloid", the rolling ball is replaced by a sliding paraboloid
of rotation with the same curvature at its apex as a ball of a given radius.
A paraboloid has the advantage that suitable paraboloids can be found for any image
values, even if the pixel values are much larger than a typical object size (in pixels).
The paraboloid of rotation is approximated as parabolae in 4 directions: x, y and
the two 45-degree directions. Lines of the image in these directions are processed
by sliding a parabola against them. Obtaining the hull needs the parabola for a
given direction to be applied multiple times (after doing the other directions);
in this respect the current code is a compromise between accuracy and speed.

For noise rejection, with the sliding paraboloid algorithm, a 3x3 maximum of the
background is applied. With both, rolling ball and sliding paraboloid,
the image used for calculating the background is slightly smoothened (3x3 average).
This can result in negative values after background subtraction. This preprocessing
can be disabled.

In the sliding paraboloid algorithm, additional code has been added to avoid
subtracting corner objects as a background (note that a paraboloid or ball would
always touch the 4 corner pixels and thus make them background pixels).
This code assumes that corner particles reach less than 1/4 of the image size
into the image.

Rolling ball code based on the NIH Image Pascal version by Michael Castle and Janice 
Keller of the University of Michigan Mental Health Research Institute.
Sliding Paraboloid by Michael Schmid, 2007.

Version 10-Jan-2008
*/
public class BackgroundSubtracter implements ExtendedPlugInFilter, DialogListener {
    /* parameters from the dialog: */
    private static double radius = 50;  // default rolling ball radius
    private static boolean lightBackground = Prefs.get("bs.background", true);
    private static boolean separateColors; // whether to create a separate background for each color channel
    private static boolean createBackground;   // don't subtract background (e.g., for processing the background before subtracting)
    private static boolean useParaboloid; // use "Sliding Paraboloid" instead of rolling ball algorithm
    private static boolean doPresmooth = true; // smoothen the image before creating the background
    /* more class variables */
    private boolean isRGB;              // whether we have an RGB image
    private boolean previewing;
    private final static int MAXIMUM = 0, MEAN = 1;         //filter types of filter3x3
    private final static int X_DIRECTION = 0, Y_DIRECTION = 1,
            DIAGONAL_1A = 2, DIAGONAL_1B = 3, DIAGONAL_2A = 4, DIAGONAL_2B = 5; //filter directions
    private final static int DIRECTION_PASSES = 9; //number of passes for different directions
    private int nPasses = DIRECTION_PASSES;
    private int pass;
    private int flags = DOES_ALL|FINAL_PROCESSING|KEEP_PREVIEW|PARALLELIZE_STACKS;

    public int setup(String arg, IjxImagePlus imp) {
        if (arg.equals("final")) {
            imp.getProcessor().resetMinAndMax();
            return DONE;
        } else
            return flags;
    }

    public int showDialog(IjxImagePlus imp, String command, PlugInFilterRunner pfr) {
        isRGB = imp.getProcessor() instanceof ColorProcessor;
        String options = Macro.getOptions();
        if  (options!=null)
            Macro.setOptions(options.replaceAll("white", "light"));
        GenericDialog gd = new GenericDialog(command);
        gd.addNumericField("Rolling ball radius:", radius, 1, 6, "pixels");
        gd.addCheckbox("Light background", lightBackground);
        if (isRGB) gd.addCheckbox("Separate colors", separateColors);
        gd.addCheckbox("Create background (don't subtract)", createBackground);
        gd.addCheckbox("Sliding paraboloid", useParaboloid);
        gd.addCheckbox("Disable smoothing", !doPresmooth);
        gd.addPreviewCheckbox(pfr);
        gd.addDialogListener(this);
        previewing = true;
		gd.addHelp(IJ.URL+"/docs/menus/process.html#background");
        gd.showDialog();
        previewing = false;
        if (gd.wasCanceled()) return DONE;
        IJ.register(this.getClass());       //protect static class variables (filter parameters) from garbage collection
        Prefs.set("bs.background", lightBackground);
        if ((imp.getProcessor() instanceof FloatProcessor) && !createBackground)
            flags |= SNAPSHOT;              //FloatProcessors need the original to subtract it from the background
        return IJ.setupDialog(imp, flags);  //ask whether to process all slices of stack (if a stack)
    }

    public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
        radius = gd.getNextNumber();
        if (radius <= 0.0001 || gd.invalidNumber())
            return false;
        lightBackground = gd.getNextBoolean();
        if (isRGB) separateColors = gd.getNextBoolean();
        createBackground = gd.getNextBoolean();
        useParaboloid = gd.getNextBoolean();
        doPresmooth = !gd.getNextBoolean();
        return true;
    }

    /** Background for any image type */
    public void run(ImageProcessor ip) {
        if (isRGB && !separateColors)
            rollingBallBrightnessBackground((ColorProcessor)ip, radius, createBackground, lightBackground, useParaboloid, doPresmooth, true);
        else
            rollingBallBackground(ip, radius, createBackground, lightBackground, useParaboloid, doPresmooth, true);
        if (previewing && (ip instanceof FloatProcessor || ip instanceof ShortProcessor)) {
            ip.resetMinAndMax();
        }
    }

    /** Depracated. For compatibility with previous ImageJ versions */
    public void subtractRGBBackround(ColorProcessor ip, int ballRadius) {
        rollingBallBrightnessBackground(ip, (double)ballRadius, false, lightBackground, false, true, true);
    }
    /** Depracated. For compatibility with previous ImageJ versions */
    public void subtractBackround(ImageProcessor ip, int ballRadius) {
        rollingBallBackground(ip, (double)ballRadius, false, lightBackground, false, true, true);
    }

    /** Create or subtract a background, based on the brightness of an RGB image (keeping
     * the hue of each pixel unchanged)
     * @param ip            The RGB image. On output, it will become the background-subtracted image or
     *                      the background (depending on <code>createBackground</code>).
     * @param radius        Radius of the rolling ball creating the background (actually a
     *                      paraboloid of rotation with the same curvature)
     * @param createBackground  Whether to create a background, not to subtract it.
     * @param lightBackground   Whether the image has a light background.
     * @param doPresmooth   Whether the image should be smoothened (3x3 mean) before creating
     *                      the background. With smoothing, the background will not necessarily
     *                      be below the image data.
     * @param correctCorners    Whether the algorithm should try to detect corner particles to avoid
     *                      subtracting them as a background.
     */
    public void rollingBallBrightnessBackground(ColorProcessor ip, double radius, boolean createBackground,
            boolean lightBackground, boolean useParaboloid, boolean doPresmooth, boolean correctCorners) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        byte[] H = new byte[width*height];
        byte[] S = new byte[width*height];
        byte[] B = new byte[width*height];
        ip.getHSB(H, S, B);
        ByteProcessor bp = new ByteProcessor(width, height, B, null);
        rollingBallBackground(bp, radius, createBackground, lightBackground, useParaboloid, doPresmooth, correctCorners);
        ip.setHSB(H, S, (byte[])bp.getPixels());
    }

    /** Create or subtract a background, works for all image types. For RGB images, the
     * background is subtracted from each channel separately
     * @param ip            The image. On output, it will become the background-subtracted image or
     *                      the background (depending on <code>createBackground</code>).
     * @param radius        Radius of the rolling ball creating the background (actually a
     *                      paraboloid of rotation with the same curvature)
     * @param createBackground  Whether to create a background, not to subtract it.
     * @param lightBackground   Whether the image has a light background.
     * @param useParaboloid   Whether to use the "sliding paraboloid" algorithm.
     * @param doPresmooth   Whether the image should be smoothened (3x3 mean) before creating
     *                      the background. With smoothing, the background will not necessarily
     *                      be below the image data.
     * @param correctCorners    Whether the algorithm should try to detect corner particles to avoid
     *                      subtracting them as a background.
     */
    public void rollingBallBackground(ImageProcessor ip, double radius, boolean createBackground,
            boolean lightBackground, boolean useParaboloid, boolean doPresmooth, boolean correctCorners) {
        boolean invertedLut = ip.isInvertedLut();
        boolean invert = (invertedLut && !lightBackground) || (!invertedLut && lightBackground);
        RollingBall ball = null;
        if (!useParaboloid) ball = new RollingBall(radius);
        FloatProcessor fp = null;
        for (int channelNumber=0; channelNumber<ip.getNChannels(); channelNumber++) {
            fp = ip.toFloat(channelNumber, fp);
            if (useParaboloid)
                slidingParaboloidFloatBackground(fp, (float)radius, invert, doPresmooth, correctCorners);
            else
                rollingBallFloatBackground(fp, (float)radius, invert, doPresmooth, ball);

            if (createBackground)
                ip.setPixels(channelNumber, fp);
            else {                                          //subtract the background now
                float[] bgPixels = (float[])fp.getPixels(); //currently holds the background
                if (ip instanceof FloatProcessor) {         //here ip and fp are the same (bgPixels will be output)
                    float[] snapshotPixels = (float[])fp.getSnapshotPixels(); //original data in the snapshot
                    for (int p=0; p<bgPixels.length; p++)
                        bgPixels[p] = snapshotPixels[p]-bgPixels[p];
                //for all others, the image data are in ip, the background is in fp
                } else if (ip instanceof ShortProcessor) {
                    float offset = invert ? 65535.5f : 0.5f;//includes 0.5 for rounding when converting float to short
                    short[] pixels = (short[])ip.getPixels();
                    for (int p=0; p<bgPixels.length; p++) {
                        float value = (pixels[p]&0xffff) - bgPixels[p] + offset;
                        if (value<0f) value = 0f;

                        if (value>65535f) value = 65535f;

                        pixels[p] = (short)(value);
                    }
                } else if (ip instanceof ByteProcessor) {
                    float offset = invert ? 255.5f : 0.5f;  //includes 0.5 for rounding when converting float to byte
                    byte[] pixels = (byte[])ip.getPixels();
                    for (int p=0; p<bgPixels.length; p++) {
                        float value = (pixels[p]&0xff) - bgPixels[p] + offset;
                        if (value<0f) value = 0f;

                        if (value>255f) value = 255f;

                        pixels[p] = (byte)(value);
                    }
                } else if (ip instanceof ColorProcessor) {
                    float offset = invert ? 255.5f : 0.5f;
                    int[] pixels = (int[])ip.getPixels();
            		int shift = 16 - 8*channelNumber;

            		int byteMask = 255<<shift;
            		int resetMask = 0xffffffff^(255<<shift);

                    for (int p=0; p<bgPixels.length; p++) {
                        int pxl = pixels[p];
                        float value = ((pxl&byteMask)>>shift) - bgPixels[p] + offset;
                        if (value<0f) value = 0f;

                        if (value>255f) value = 255f;
                        pixels[p] = (pxl&resetMask) | ((int)value<<shift);

                    }
                }
            }
        }
    }

    //  S L I D E   P A R A B O L O I D   S E C T I O N

    /** Create background for a float image by sliding a paraboloid over
     * the image. */
    void slidingParaboloidFloatBackground(FloatProcessor fp, float radius, boolean invert,
            boolean doPresmooth, boolean correctCorners) {
        float[] pixels = (float[])fp.getPixels();   //this will become the background
        int width = fp.getWidth();
        int height = fp.getHeight();
        float[] cache = new float[Math.max(width, height)]; //work array for lineSlideParabola
        int[] nextPoint = new int[Math.max(width, height)]; //work array for lineSlideParabola
        float coeff2 = 0.5f/radius;                 //2nd-order coefficient of the polynomial approximating the ball
        float coeff2diag = 1.f/radius;              //same for diagonal directions where step is sqrt2

        showProgress(0.000001);                     //start the progress bar (only filter1D will increment it)
        if (invert)
            for (int i=0; i<pixels.length; i++)
                pixels[i] = -pixels[i];

        float shiftBy = 0;
        if (doPresmooth) {
            shiftBy = (float)filter3x3(fp, MAXIMUM);//3x3 maximum to remove dust etc.
            showProgress(0.5);
            filter3x3(fp, MEAN);                    //smoothing to remove noise
            pass++;
            //IJ.log("shiftBy="+shiftBy);
            //IJ.getFactory().newImagePlus("preprocessed",fp.duplicate()).show();        
        }
        if (correctCorners)
            correctCorners(fp, coeff2, cache, nextPoint);   //modify corner data, avoids subtracting corner particles

        /* Slide the parabola over the image in different directions */
        /* Doing the diagonal directions at the end is faster (diagonal lines are denser,
         * so there are more such lines, and the algorithm gets faster with each iteration) */
        filter1D(fp, X_DIRECTION, coeff2, cache, nextPoint);
        filter1D(fp, Y_DIRECTION, coeff2, cache, nextPoint);
        filter1D(fp, X_DIRECTION, coeff2, cache, nextPoint);    //redo for better accuracy
        filter1D(fp, DIAGONAL_1A, coeff2diag, cache, nextPoint);
        filter1D(fp, DIAGONAL_1B, coeff2diag, cache, nextPoint);
        filter1D(fp, DIAGONAL_2A, coeff2diag, cache, nextPoint);
        filter1D(fp, DIAGONAL_2B, coeff2diag, cache, nextPoint);
        filter1D(fp, DIAGONAL_1A, coeff2diag, cache, nextPoint);//redo for better accuracy
        filter1D(fp, DIAGONAL_1B, coeff2diag, cache, nextPoint);

        if (invert)
            for (int i=0; i<pixels.length; i++)
                pixels[i] = -(pixels[i] - shiftBy);
        else if (doPresmooth)
            for (int i=0; i<pixels.length; i++)
                pixels[i] -= shiftBy;   //correct for shift by 3x3 maximum

    }

    /** Filter by subtracting a sliding parabola for all lines in one direction, x, y or one of
     *  the two diagonal directions (diagonals are processed only for half the image per call). */
    void filter1D(FloatProcessor fp, int direction, float coeff2, float[] cache, int[] nextPoint) {
        float[] pixels = (float[])fp.getPixels();   //this will become the background
        int width = fp.getWidth();
        int height = fp.getHeight();
        int startLine = 0;          //index of the first line to handle
        int nLines = 0;             //index+1 of the last line to handle (initialized to avoid compile-time error)
        int lineInc = 0;            //increment from one line to the next in pixels array
        int pointInc = 0;           //increment from one point to the next along the line
        int length = 0;             //length of the line
        switch (direction) {
            case X_DIRECTION:       //lines parallel to x direction
                nLines = height;
                lineInc = width;
                pointInc = 1;
                length = width;
            break;
            case Y_DIRECTION:       //lines parallel to y direction
                nLines = width;
                lineInc = 1;
                pointInc = width;
                length = height;
            break;
            case DIAGONAL_1A:       //lines parallel to x=y, starting at x axis
                nLines = width-2;   //the algorithm makes no sense for lines shorter than 3 pixels
                lineInc = 1;
                pointInc = width + 1;
            break;
            case DIAGONAL_1B:       //lines parallel to x=y, starting at y axis
                startLine = 1;
                nLines = height-2;
                lineInc = width;
                pointInc = width + 1;
            break;
            case DIAGONAL_2A:       //lines parallel to x=-y, starting at x axis
                startLine = 2;
                nLines = width;
                lineInc = 1;
                pointInc = width - 1;
            break;
            case DIAGONAL_2B:       //lines parallel to x=-y, starting at x=width-1, y=variable
                startLine = 0;
                nLines = height-2;
                lineInc = width;
                pointInc = width - 1;
            break;
        }
        for (int i=startLine; i<nLines; i++) {
            if (i%50==0) {
                if (Thread.currentThread().isInterrupted()) return;
                showProgress(i/(double)nLines);
            }
            int startPixel = i*lineInc;
            if (direction == DIAGONAL_2B) startPixel += width-1;
            switch (direction) {
                case DIAGONAL_1A: length = Math.min(height, width-i); break;
                case DIAGONAL_1B: length = Math.min(width, height-i); break;
                case DIAGONAL_2A: length = Math.min(height, i+1);     break;
                case DIAGONAL_2B: length = Math.min(width, height-i); break;
            }
            lineSlideParabola(pixels, startPixel, pointInc, length, coeff2, cache, nextPoint, null);
        }
        pass++;
    } //void filter1D

    /** Process one straight line in the image by sliding a parabola along the line
     *  (from the bottom) and setting the values to make all points reachable by
     *  the parabola
     * @param pixels    Image data, will be modified by parabolic interpolation
     *                  where the parabola does not touch.
     * @param start     Index of first pixel of the line in pixels array
     * @param inc       Increment of index in pixels array
     * @param length    Number of points the line consists of
     * @param coeff2    2nd order coefficient of the polynomial describing the parabola,
     *                  must be positive (although a parabola with negative curvature is
     *                  actually used)
     * @param cache     Work array, length at least <code>length</code>. Will usually remain
     *                  in the CPU cache and may therefore speed up the code.
     * @param nextPoint Work array. Will hold the index of the next point with sufficient local
     *                  curvature to get touched by the parabola.
     * @param correctedEdges Should be a 2-element array used for output or null.
     * @return          The correctedEdges array (if non-null on input) with the two estimated
     *                  edge pixel values corrected for edge particles.
     */
    static float[] lineSlideParabola(float[] pixels, int start, int inc, int length, float coeff2, float[] cache, int[] nextPoint, float[] correctedEdges) {
        float minValue = Float.MAX_VALUE;
        int lastpoint = 0;
        int firstCorner = length-1;             // the first point except the edge that is touched
        int lastCorner = 0;                     // the last point except the edge that is touched
        float vPrevious1 = 0f;
        float vPrevious2 = 0f;
        float curvatureTest = 1.999f*coeff2;     //not 2: numeric scatter of 2nd derivative
        /* copy data to cache, determine the minimum, and find points with local curvature such
         * that the parabola can touch them - only these need to be examined futher on */
        for (int i=0, p=start; i<length; i++, p+=inc) {
            float v = pixels[p];
            cache[i] = v;
            if (v < minValue) minValue = v;
            if (i >= 2 && vPrevious1+vPrevious1-vPrevious2-v < curvatureTest) {
                nextPoint[lastpoint] = i-1;     // point i-1 may be touched
                lastpoint = i-1;
            }
            vPrevious2 = vPrevious1;
            vPrevious1 = v;
        }
        nextPoint[lastpoint] = length-1;
        nextPoint[length-1] = Integer.MAX_VALUE;// breaks the search loop

        int i1 = 0;                             // i1 and i2 will be the two points where the parabola touches
        while (i1<length-1) {
            float v1 = cache[i1];
            float minSlope = Float.MAX_VALUE;
            int i2 = 0;                         //(initialized to avoid compile-time error)
            int searchTo = length;
            int recalculateLimitNow = 0;        // when 0, limits for searching will be recalculated
            /* find the second point where the parabola through point i1,v1 touches: */
            for (int j=nextPoint[i1]; j<searchTo; j=nextPoint[j], recalculateLimitNow++) {
                float v2 = cache[j];
                float slope = (v2-v1)/(j-i1)+coeff2*(j-i1);
                if (slope < minSlope) {
                    minSlope = slope;
                    i2 = j;
                    recalculateLimitNow = -3;
                }
                if (recalculateLimitNow==0) {   //time-consuming recalculation of search limit: wait a bit after slope is updated
                    double b = 0.5f*minSlope/coeff2;
                    int maxSearch = i1+(int)(b+Math.sqrt(b*b+(v1-minValue)/coeff2)+1); //(numeric overflow may make this negative)
                    if (maxSearch < searchTo && maxSearch > 0) searchTo = maxSearch;
                }
            }
            if (i1 == 0) firstCorner = i2;
            if (i2 == length-1) lastCorner = i1;
            /* interpolate between the two points where the parabola touches: */
            for (int j=i1+1, p=start+j*inc; j<i2; j++, p+=inc)
                pixels[p] = v1 + (j-i1)*(minSlope - (j-i1)*coeff2);
            i1 = i2;                            // continue from this new point
        } //while (i1<length-1)
        /* Now calculate estimated edge values without an edge particle, allowing for vignetting
         * described as a 6th-order polynomial: */
        if (correctedEdges != null) {
            if (4*firstCorner >= length) firstCorner = 0; // edge particles must be < 1/4 image size
            if (4*(length - 1 - lastCorner) >= length) lastCorner = length - 1;
            float v1 = cache[firstCorner];
            float v2 = cache[lastCorner];
            float slope = (v2-v1)/(lastCorner-firstCorner); // of the line through the two outermost non-edge touching points
            float value0 = v1 - slope * firstCorner;        // offset of this line
            float coeff6 = 0;                               // coefficient of 6th order polynomial
            float mid = 0.5f * (lastCorner + firstCorner);
            for (int i=(length+2)/3; i<=(2*length)/3; i++) {// compare with mid-image pixels to detect vignetting
                float dx = (i-mid)*2f/(lastCorner-firstCorner);
                float poly6 = dx*dx*dx*dx*dx*dx - 1f;       // the 6th order polynomial, zero at firstCorner and lastCorner
                if (cache[i] < value0 + slope*i + coeff6*poly6) {
                    coeff6 = -(value0 + slope*i - cache[i])/poly6;
                }
            }
            float dx = (firstCorner-mid)*2f/(lastCorner-firstCorner);
            correctedEdges[0] = value0 + coeff6*(dx*dx*dx*dx*dx*dx - 1f) + coeff2*firstCorner*firstCorner;
            dx = (lastCorner-mid)*2f/(lastCorner-firstCorner);
            correctedEdges[1] = value0 + (length-1)*slope + coeff6*(dx*dx*dx*dx*dx*dx - 1f) + coeff2*(length-1-lastCorner)*(length-1-lastCorner);
            //IJ.log("edge corr: corners@"+firstCorner+","+lastCorner+":"+v1+","+v2);
            //IJ.log("from "+cache[0]+","+cache[length-1]+" to linear:"+(v1-firstCorner*slope)+","+(v2+(length-1-lastCorner)*slope)+";full:"+correctedEdges[0]+","+correctedEdges[1]);
        }
        return correctedEdges;
    } //void lineSlideParabola

    /** Detect corner particles and adjust corner pixels if a particle is there.
     *  Analyzing the directions parallel to the edges and the diagonals, we
     *  average over the 3 correction values (found for the 3 directions)
     */
    void correctCorners(FloatProcessor fp, float coeff2, float[] cache, int[] nextPoint) {
        int width = fp.getWidth();
        int height = fp.getHeight();
        float[] pixels = (float[])fp.getPixels();
        float[] corners = new float[4];         //(0,0); (xmax,0); (ymax,0); (xmax,ymax)
        float[] correctedEdges = new float[2];
        correctedEdges = lineSlideParabola(pixels, 0, 1, width, coeff2, cache, nextPoint, correctedEdges);
        corners[0] = correctedEdges[0];
        corners[1] = correctedEdges[1];
        correctedEdges = lineSlideParabola(pixels, (height-1)*width, 1, width, coeff2, cache, nextPoint, correctedEdges);
        corners[2] = correctedEdges[0];
        corners[3] = correctedEdges[1];
        correctedEdges = lineSlideParabola(pixels, 0, width, height, coeff2, cache, nextPoint, correctedEdges);
        corners[0] += correctedEdges[0];
        corners[2] += correctedEdges[1];
        correctedEdges = lineSlideParabola(pixels, width-1, width, height, coeff2, cache, nextPoint, correctedEdges);
        corners[1] += correctedEdges[0];
        corners[3] += correctedEdges[1];
        int diagLength = Math.min(width,height);        //length of a 45-degree line from a corner
        float coeff2diag = 2 * coeff2;
        correctedEdges = lineSlideParabola(pixels, 0, 1+width, diagLength, coeff2diag, cache, nextPoint, correctedEdges);
        corners[0] += correctedEdges[0];
        correctedEdges = lineSlideParabola(pixels, width-1, -1+width, diagLength, coeff2diag, cache, nextPoint, correctedEdges);
        corners[1] += correctedEdges[0];
        correctedEdges = lineSlideParabola(pixels, (height-1)*width, 1-width, diagLength, coeff2diag, cache, nextPoint, correctedEdges);
        corners[2] += correctedEdges[0];
        correctedEdges = lineSlideParabola(pixels, width*height-1, -1-width, diagLength, coeff2diag, cache, nextPoint, correctedEdges);
        corners[3] += correctedEdges[0];
        //IJ.log("corner 00:"+pixels[0]+"->"+(corners[0]/3));
        //IJ.log("corner 01:"+pixels[width-1]+"->"+(corners[1]/3));
        //IJ.log("corner 10:"+pixels[(height-1)*width]+"->"+(corners[2]/3));
        //IJ.log("corner 11:"+pixels[width*height-1]+"->"+(corners[3]/3));
        if (pixels[0] > corners[0]/3) pixels[0] = corners[0]/3;
        if (pixels[width-1] > corners[1]/3) pixels[width-1] = corners[1]/3;
        if (pixels[(height-1)*width] > corners[2]/3) pixels[(height-1)*width] = corners[2]/3;
        if (pixels[width*height-1] > corners[3]/3) pixels[width*height-1] = corners[3]/3;
        //IJ.getFactory().newImagePlus("corner corrected",fp.duplicate()).show();
    } //void correctCorners

    //  R O L L   B A L L   S E C T I O N

    /** Create background for a float image by rolling a ball over
     * the image. */
    void rollingBallFloatBackground(FloatProcessor fp, float radius, boolean invert,
            boolean doPresmooth, RollingBall ball) {
        float[] pixels = (float[])fp.getPixels();   //this will become the background
        boolean shrink = ball.shrinkFactor >1;

        showProgress(0.0);
        if (invert)
            for (int i=0; i<pixels.length; i++)
                pixels[i] = -pixels[i];
        if (doPresmooth)
            filter3x3(fp, MEAN);
        double[] minmax = Tools.getMinMax(pixels);
        if (Thread.currentThread().isInterrupted()) return;
        FloatProcessor smallImage = shrink ? shrinkImage(fp, ball.shrinkFactor) : fp;
        if (Thread.currentThread().isInterrupted()) return;
        rollBall(ball, smallImage);
        if (Thread.currentThread().isInterrupted()) return;
        showProgress(0.9);
        if (shrink)
            enlargeImage(smallImage, fp, ball.shrinkFactor);
        if (Thread.currentThread().isInterrupted()) return;

        if (invert)
            for (int i=0; i<pixels.length; i++)
                pixels[i] = -pixels[i];
        pass++;
    }

    /** Creates a lower resolution image for ball-rolling. */
    FloatProcessor shrinkImage(FloatProcessor ip, int shrinkFactor) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        float[] pixels = (float[])ip.getPixels();
        int sWidth = (width+shrinkFactor-1)/shrinkFactor;
        int sHeight = (height+shrinkFactor-1)/shrinkFactor;
        showProgress(0.1);
        FloatProcessor smallImage = new FloatProcessor(sWidth, sHeight);
        float[] sPixels = (float[])smallImage.getPixels();
        float min, thispixel;
        for (int ySmall=0; ySmall<sHeight; ySmall++) {
            for (int xSmall=0; xSmall<sWidth; xSmall++) {
                min = Float.MAX_VALUE;
                for (int j=0, y=shrinkFactor*ySmall; j<shrinkFactor&&y<height; j++, y++) {
                    for (int k=0, x=shrinkFactor*xSmall; k<shrinkFactor&&x<width; k++, x++) {
                        thispixel = pixels[x+y*width];
                        if (thispixel<min)
                            min = thispixel;
                    }
                }
                sPixels[xSmall+ySmall*sWidth] = min; // each point in small image is minimum of its neighborhood
            }
        }
        //IJ.getFactory().newImagePlus("smallImage", smallImage).show();
        return smallImage;
    }

    /** 'Rolls' a filtering object over a (shrunken) image in order to find the
        image's smooth continuous background.  For the purpose of explaining this
        algorithm, imagine that the 2D grayscale image has a third (height) dimension
        defined by the intensity value at every point in the image.  The center of
        the filtering object, a patch from the top of a sphere having radius BallRadius,
        is moved along each scan line of the image so that the patch is tangent to the
        image at one or more points with every other point on the patch below the
        corresponding (x,y) point of the image.  Any point either on or below the patch
        during this process is considered part of the background.  Shrinking the image
        before running this procedure is advised for large ball radii because the
        processing time increases with ball radius^2.
    */
    void rollBall(RollingBall ball, FloatProcessor fp) {
        float[] pixels = (float[])fp.getPixels();   //the input pixels
        int width = fp.getWidth();
        int height = fp.getHeight();
        float[] zBall = ball.data;
        int ballWidth = ball.width;
        int radius = ballWidth/2;
        float[] cache = new float[width*ballWidth]; //temporarily stores the pixels we work on

		Thread thread = Thread.currentThread();
		long lastTime = System.currentTimeMillis();
        for (int y=-radius; y<height+radius; y++) { //for all positions of the ball center:
			long time = System.currentTimeMillis();
			if (time-lastTime > 100) {
				lastTime = time;
				if (thread.isInterrupted()) return;
				showProgress(0.1+0.8*y/(height+ballWidth));
            }
            int nextLineToWriteInCache = (y+radius)%ballWidth;
            int nextLineToRead = y + radius;        //line of the input not touched yet
            if (nextLineToRead<height) {
                System.arraycopy(pixels, nextLineToRead*width, cache, nextLineToWriteInCache*width, width);
                for (int x=0, p=nextLineToRead*width; x<width; x++,p++)
                    pixels[p] = -Float.MAX_VALUE;   //unprocessed pixels start at minus infinity
            }
            int y0 = y-radius;                      //the first line to see whether the ball touches
            if (y0 < 0) y0 = 0;
            int yBall0 = y0-y+radius;               //y coordinate in the ball corresponding to y0
            int yend = y+radius;                    //the last line to see whether the ball touches
            if (yend>=height) yend = height-1;
            for (int x=-radius; x<width+radius; x++) {
                float z = Float.MAX_VALUE;          //the height of the ball (ball is in position x,y)
                int x0 = x-radius;
                if (x0 < 0) x0 = 0;
                int xBall0 = x0-x+radius;
                int xend = x+radius;
                if (xend>=width) xend = width-1;
                for (int yp=y0, yBall=yBall0; yp<=yend; yp++,yBall++) { //for all points inside the ball
                    int cachePointer = (yp%ballWidth)*width+x0;
                    for (int xp=x0, bp=xBall0+yBall*ballWidth; xp<=xend; xp++, cachePointer++, bp++) {
                        float zReduced = cache[cachePointer] - zBall[bp];
                        if (z > zReduced)           //does this point imply a greater height?
                            z = zReduced;
                    }
                }
                for (int yp=y0, yBall=yBall0; yp<=yend; yp++,yBall++) //raise pixels to ball surface
                    for (int xp=x0, p=xp+yp*width, bp=xBall0+yBall*ballWidth; xp<=xend; xp++, p++, bp++) {
                        float zMin = z + zBall[bp];
                        if (pixels[p] < zMin)
                            pixels[p] = zMin;
                    }
            // if (x>=0&&y>=0&&x<width&&y<height) bgPixels[x+y*width] = z; //debug, ball height output
            }
        }
        
        //IJ.getFactory().newImagePlus("bg rolled", fp.duplicate()).show();
    }
    
    /** Uses bilinear interpolation to find the points in the full-scale background
        given the points from the shrunken image background. (At the edges, it is
        actually extrapolation.)
    */                                 
    void enlargeImage(FloatProcessor smallImage, FloatProcessor fp, int shrinkFactor) {
        int width = fp.getWidth();
        int height = fp.getHeight();
        int smallWidth = smallImage.getWidth();
        int smallHeight = smallImage.getHeight();
        float[] pixels = (float[])fp.getPixels();
        float[] sPixels = (float[])smallImage.getPixels();
        int[] xSmallIndices = new int[width];         //index of first point in smallImage
        float[] xWeights = new float[width];        //weight of this point
        makeInterpolationArrays(xSmallIndices, xWeights, width, smallWidth, shrinkFactor);
        int[] ySmallIndices = new int[height];
        float[] yWeights = new float[height];
        makeInterpolationArrays(ySmallIndices, yWeights, height, smallHeight, shrinkFactor);
        float[] line0 = new float[width];
        float[] line1 = new float[width];
        for (int x=0; x<width; x++)                 //x-interpolation of the first smallImage line
            line1[x] = sPixels[xSmallIndices[x]] * xWeights[x] +
                    sPixels[xSmallIndices[x]+1] * (1f - xWeights[x]);
        int ySmallLine0 = -1;                       //line0 corresponds to this y of smallImage
        for (int y=0; y<height; y++) {
            if (ySmallLine0 < ySmallIndices[y]) {
                float[] swap = line0;               //previous line1 -> line0
                line0 = line1;
                line1 = swap;                       //keep the other array for filling with new data
                ySmallLine0++;
                int sYPointer = (ySmallIndices[y]+1)*smallWidth; //points to line0 + 1 in smallImage
                for (int x=0; x<width; x++)         //x-interpolation of the new smallImage line -> line1
                    line1[x] = sPixels[sYPointer+xSmallIndices[x]] * xWeights[x] +
                            sPixels[sYPointer+xSmallIndices[x]+1] * (1f - xWeights[x]);
            }
            float weight = yWeights[y];
            for (int x=0, p=y*width; x<width; x++,p++)
                pixels[p] = line0[x]*weight + line1[x]*(1f - weight);
        }
    }

    /** Create arrays of indices and weigths for interpolation.
     <pre>
     Example for shrinkFactor = 4:
        small image pixel number         |       0       |       1       |       2       | ...
        full image pixel number          | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 |10 |11 | ...
        smallIndex for interpolation(0)  | 0 | 0 | 0 | 0 | 0 | 0 | 1 | 1 | 1 | 1 | 2 | 2 | ...
     (0) Note: This is smallIndex for the left pixel; for the right pixel used for interpolation
               it is higher by one
     </pre>
     */
    void makeInterpolationArrays(int[] smallIndices, float[] weights, int length, int smallLength, int shrinkFactor) {
        for (int i=0; i<length; i++) {
            int smallIndex = (i - shrinkFactor/2)/shrinkFactor;
            if (smallIndex >= smallLength-1) smallIndex = smallLength - 2;
            smallIndices[i] = smallIndex;
            float distance = (i + 0.5f)/shrinkFactor - (smallIndex + 0.5f); //distance of pixel centers (in smallImage pixels)
            weights[i] = 1f - distance;
            //if(i<12)IJ.log("i,sI="+i+","+smallIndex+", weight="+weights[i]);
        }
    }

    //   C O M M O N   S E C T I O N   F O R   B O T H   A L G O R I T H M S

    /** Replace the pixels by the mean or maximum in a 3x3 neighborhood.
     *  No snapshot is required (less memory needed than e.g., fp.smooth()).
     *  When used as maximum filter, it returns the average change of the
     *  pixel value by this operation
     */
    double filter3x3(FloatProcessor fp, int type) {
        int width = fp.getWidth();
        int height = fp.getHeight();
        double shiftBy = 0;
        float[] pixels = (float[])fp.getPixels();
        for (int y=0; y<height; y++)
            shiftBy += filter3(pixels, width, y*width, 1, type);
        for (int x=0; x<width; x++)
            shiftBy += filter3(pixels, height, x, width, type);
        return shiftBy/width/height;
    }

    /** Filter a line: maximum or average of 3-pixel neighborhood */
    double filter3(float[] pixels, int length, int pixel0, int inc, int type) {
        double shiftBy = 0;
        float v3 = pixels[pixel0];  //will be pixel[i+1]
        float v2 = v3;              //will be pixel[i]
        float v1;                   //will be pixel[i-1]
        for (int i=0, p=pixel0; i<length; i++,p+=inc) {
            v1 = v2;
            v2 = v3;
            if (i<length-1) v3 = pixels[p+inc];
            if (type == MAXIMUM) {
                float max = v1 > v3 ? v1 : v3;
                if (v2 > max) max = v2;
                shiftBy += max - v2;
                pixels[p] = max;
            } else
                pixels[p] = (v1+v2+v3)*0.33333333f;
        }
        return shiftBy;
    }


    public void setNPasses(int nPasses) {
        if (isRGB && separateColors) nPasses *= 3;
        this.nPasses = nPasses;
        if (useParaboloid) nPasses*= (doPresmooth) ? DIRECTION_PASSES+2 : DIRECTION_PASSES;
        pass = 0;
    }

    private void showProgress(double percent) {
        if (nPasses <= 0) return;
        percent = (double)pass/nPasses + percent/nPasses;
        IJ.showProgress(percent);
    }
}

//  C L A S S   R O L L I N G B A L L

/** A rolling ball (or actually a square part thereof)
 *  Here it is also determined whether to shrink the image
 */
class RollingBall {

    float[] data;
    int width;
    int shrinkFactor;
    
    RollingBall(double radius) {
        int arcTrimPer;
        if (radius<=10) {
            shrinkFactor = 1;
            arcTrimPer = 24; // trim 24% in x and y
        } else if (radius<=30) {
            shrinkFactor = 2;
            arcTrimPer = 24; // trim 24% in x and y
        } else if (radius<=100) {
            shrinkFactor = 4;
            arcTrimPer = 32; // trim 32% in x and y
        } else {
            shrinkFactor = 8;
            arcTrimPer = 40; // trim 40% in x and y
        }
        buildRollingBall(radius, arcTrimPer);
    }
    
    /** Computes the location of each point on the rolling ball patch relative to the 
    center of the sphere containing it.  The patch is located in the top half 
    of this sphere.  The vertical axis of the sphere passes through the center of 
    the patch.  The projection of the patch in the xy-plane below is a square.
    */
    void buildRollingBall(double ballradius, int arcTrimPer) {
        double rsquare;     // rolling ball radius squared
        int xtrim;          // # of pixels trimmed off each end of ball to make patch
        int xval, yval;     // x,y-values on patch relative to center of rolling ball
        double smallballradius; // radius of rolling ball (downscaled in x,y and z when image is shrunk)
        int halfWidth;      // distance in x or y from center of patch to any edge (patch "radius")
        
        this.shrinkFactor = shrinkFactor;
        smallballradius = ballradius/shrinkFactor;
        if (smallballradius<1)
            smallballradius = 1;
        rsquare = smallballradius*smallballradius;
        xtrim = (int)(arcTrimPer*smallballradius)/100; // only use a patch of the rolling ball
        halfWidth = (int)Math.round(smallballradius - xtrim);
        width = 2*halfWidth+1;
        data = new float[width*width];

        for (int y=0, p=0; y<width; y++)
            for (int x=0; x<width; x++, p++) {
                xval = x - halfWidth;
                yval = y - halfWidth;
                double temp = rsquare - xval*xval - yval*yval;
                data[p] = temp>0. ? (float)(Math.sqrt(temp)) : 0f;
                //-Float.MAX_VALUE might be better than 0f, but gives different results than earlier versions
            }
        //IJ.log(ballradius+"\t"+smallballradius+"\t"+width); //###
        //IJ.log("half patch width="+halfWidth+", size="+data.length);
    }

}
