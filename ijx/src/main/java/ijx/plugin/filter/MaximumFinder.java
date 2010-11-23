package ijx.plugin.filter;
import ijx.process.ImageProcessor;
import ijx.process.FloatProcessor;
import ijx.process.ByteProcessor;
import ijx.plugin.api.PlugInFilterRunner;
import ijx.plugin.api.ExtendedPlugInFilter;
import ijx.gui.Wand;
import ijx.gui.dialog.DialogListener;
import ijx.gui.dialog.GenericDialog;
import ijx.roi.Roi;
import ijx.roi.PolygonRoi;
import ijx.roi.PointRoi;
import ijx.measure.Calibration;
import ijx.measure.ResultsTable;
import ijx.Prefs;
import ijx.WindowManager;
import ijx.IJ;


import ijx.IjxImagePlus;
import ijx.IjxImageStack;
import java.awt.*;
import java.util.*;

/** This ImageJ plug-in filter finds the maxima (or minima) of an image.
 * It can create a mask where the local maxima of the current image are
 * marked (255; unmarked pixels 0).
 * The plug-in can also create watershed-segmented particles: Assume a
 * landscape of inverted heights, i.e., maxima of the image are now water sinks.
 * For each point in the image, the sink that the water goes to determines which
 * particle it belongs to.
 * When finding maxima (not minima), pixels with a level below the lower threshold
 * can be left unprocessed.
 *
 * Except for segmentation, this plugin works with ROIs, including non-rectangular ROIs.
 * Since this plug-in creates a separate output image it processes
 *    only single images or slices, no stacks.
 *
 * Notes:
 * - When using one instance of MaximumFinder for more than one image in parallel threads,
 *   all must images have the same width and height.
 *
 * version 09-Nov-2006 Michael Schmid
 * version 21-Nov-2006 Wayne Rasband. Adds "Display Point Selection" option and "Count" output type.
 * version 28-May-2007 Michael Schmid. Preview added, bugfix: minima of calibrated images, uses Arrays.sort
 * version 07-Aug-2007 Fixed a bug that could delete particles when doing watershed segmentation of an EDM.
 * version 21-Apr-2007 Adapted for float instead of 16-bit EDM; correct progress bar on multiple calls
 * version 05-May-2009 Works for images>32768 pixels in width or height
 * version 01-Nov-2009 Bugfix: extra lines in segmented output eliminated; watershed is also faster now
 *                     Maximum points encoded in long array for sorting instead of separete objects that need gc
 *                     New output type 'List'
 */

public class MaximumFinder implements ExtendedPlugInFilter, DialogListener {
    //filter params
    /** maximum height difference between points that are not counted as separate maxima */
    private static double tolerance = 10;
    /** Output type single points */
    public final static int SINGLE_POINTS=0;
    /** Output type all points around the maximum within the tolerance */
    public final static int IN_TOLERANCE=1;
    /** Output type watershed-segmented image */
    public final static int SEGMENTED=2;
    /** Do not create image, only mark points */
    public final static int POINT_SELECTION=3;
    /** Do not create an image, just list x, y of maxima in the Results table */
    public final static int LIST=4;
    /** Do not create an image, just count maxima and add count to Results table */
    public final static int COUNT=5;
    /** what type of output to create (see constants above)*/
    private static int outputType;
    /** what type of output to create was chosen in the dialog (see constants above)*/
    private static int dialogOutputType = POINT_SELECTION;
    /** output type names */
    final static String[] outputTypeNames = new String[]
        {"Single Points", "Maxima Within Tolerance", "Segmented Particles", "Point Selection", "List", "Count"};
    /** whether to exclude maxima at the edge of the image*/
    private static boolean excludeOnEdges;
    /** whether to accept maxima only in the thresholded height range*/
    private static boolean useMinThreshold;
    /** whether to find darkest points on light background */
    private static boolean lightBackground;
    private IjxImagePlus imp;                          // the IjxImagePlus of the setup call
    private int flags = DOES_ALL|NO_CHANGES|NO_UNDO;// the flags (see interfaces PlugInFilter & ExtendedPlugInFilter)
    private boolean   thresholded;                  // whether the input image has a threshold
    private boolean   roiSaved;                     // whether the filter has changed the roi and saved the original roi
    private boolean   previewing;                   // true while dialog is displayed (processing for preview)
    private Vector    checkboxes;                   // a reference to the Checkboxes of the dialog
    private boolean thresholdWarningShown = false;  // whether the warning "can't find minima with thresholding" has been shown
    private Label     messageArea;                  // reference to the textmessage area for displaying the number of maxima
    private boolean   noPointLabels;                // save this setting so it can be restored on exit
    private double    progressDone;                 // for progress bar, fraction of work done so far
    private int       nPasses = 0;                  // for progress bar, how many images to process (sequentially or parallel threads)
    //the following are class variables for having shorter argument lists
    private int       width, height;                // image dimensions
    private int       intEncodeXMask;               // needed for encoding x & y in a single int (watershed): mask for x
    private int       intEncodeYMask;               // needed for encoding x & y in a single int (watershed): mask for y
    private int       intEncodeShift;               // needed for encoding x & y in a single int (watershed): shift of y
    /** directions to 8 neighboring pixels, clockwise: 0=North (-y), 1=NE, 2=East (+x), ... 7=NW */
    private int[]     dirOffset;                    // pixel offsets of neighbor pixels for direct addressing
    final static int[] DIR_X_OFFSET = new int[] {  0,  1,  1,  1,  0, -1, -1, -1 };
    final static int[] DIR_Y_OFFSET = new int[] { -1, -1,  0,  1,  1,  1,  0, -1 };
    /** the following constants are used to set bits corresponding to pixel types */
    final static byte MAXIMUM = (byte)1;            // marks local maxima (irrespective of noise tolerance)
    final static byte LISTED = (byte)2;             // marks points currently in the list
    final static byte PROCESSED = (byte)4;          // marks points processed previously
    final static byte MAX_AREA = (byte)8;           // marks areas near a maximum, within the tolerance
    final static byte EQUAL = (byte)16;             // marks contigous maximum points of equal level
    final static byte MAX_POINT = (byte)32;         // marks a single point standing for a maximum
    final static byte ELIMINATED = (byte)64;        // marks maxima that have been eliminated before watershed
    /** type masks corresponding to the output types */
    final static byte[] outputTypeMasks = new byte[] {MAX_POINT, MAX_AREA, MAX_AREA};
    final static float SQRT2 = 1.4142135624f;


    /** Method to return types supported
     * @param arg   Not used by this plugin-filter
     * @param imp   The image to be filtered
     * @return      Code describing supported formats etc.
     * (see ij.plugin.filter.PlugInFilter & ExtendedPlugInFilter)
     */
    public int setup(String arg, IjxImagePlus imp) {
        this.imp = imp;
        noPointLabels = Prefs.noPointLabels;
        return flags;
    }

    public int showDialog(IjxImagePlus imp, String command, PlugInFilterRunner pfr) {
        ImageProcessor ip = imp.getProcessor();
        ip.resetBinaryThreshold(); // remove invisible threshold set by MakeBinary and Convert to Mask
        thresholded = ip.getMinThreshold()!=ImageProcessor.NO_THRESHOLD;
        GenericDialog gd = new GenericDialog(command);
        int digits = (ip instanceof FloatProcessor)?2:0;
        String unit = (imp.getCalibration()!=null)?imp.getCalibration().getValueUnit():null;
        unit = (unit==null||unit.equals("Gray Value"))?":":" ("+unit+"):";
        gd.addNumericField("Noise tolerance"+unit,tolerance, digits);
        gd.addChoice("Output type:", outputTypeNames, outputTypeNames[dialogOutputType]);
        gd.addCheckbox("Exclude edge maxima", excludeOnEdges);
        if (thresholded)
            gd.addCheckbox("Above lower threshold", useMinThreshold);
        gd.addCheckbox("Light background", lightBackground);
        gd.addPreviewCheckbox(pfr, "Preview point selection");
        gd.addMessage("                        "); //space for number of maxima
        messageArea = (Label)gd.getMessage();
        gd.addDialogListener(this);
        checkboxes = gd.getCheckboxes();
        previewing = true;
		gd.addHelp(IJ.URL+"/docs/menus/process.html#find-maxima");
        gd.showDialog();          //input by the user (or macro) happens here
        if (gd.wasCanceled())
            return DONE;
        previewing = false;
        if (!dialogItemChanged(gd, null))   //read parameters
            return DONE;
        IJ.register(this.getClass());       //protect static class variables (parameters) from garbage collection
        return flags;
    } // boolean showDialog

    /** Read the parameters (during preview or after showing the dialog) */
    public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
        tolerance = gd.getNextNumber();
        if (tolerance<0) tolerance = 0;
        dialogOutputType = gd.getNextChoiceIndex();
        outputType = previewing ? POINT_SELECTION : dialogOutputType;
        excludeOnEdges = gd.getNextBoolean();
        if (thresholded)
            useMinThreshold = gd.getNextBoolean();
        else
            useMinThreshold = false;
        lightBackground = gd.getNextBoolean();
        boolean invertedLut = imp.isInvertedLut();
        if (useMinThreshold && ((invertedLut&&!lightBackground) || (!invertedLut&&lightBackground))) {
            if (!thresholdWarningShown)
                if (!IJ.showMessageWithCancel(
                    "Find Maxima",
                    "\"Above Lower Threshold\" option cannot be used\n"+
                    "when finding minima (image with light background\n"+
                    "or image with dark background and inverting LUT).")
                    && !previewing)
                return false;               // if faulty input is not detected during preview, "cancel" quits
            thresholdWarningShown = true;
            useMinThreshold = false;
            ((Checkbox)(checkboxes.elementAt(1))).setState(false); //reset "Above Lower Threshold" checkbox
        }
        if (!gd.getPreviewCheckbox().getState())
            messageArea.setText("");        // no "nnn Maxima" message when not previewing
        return (!gd.invalidNumber());
    } // public boolean DialogItemChanged

    /** Set his to the number of images to process (for the watershed progress bar only).
     *  Don't call or set nPasses to zero if no progress bar is desired. */
    public void setNPasses(int nPasses) {
        this.nPasses = nPasses;
    }

    /** The plugin is inferred from ImageJ by this method
     * @param ip The image where maxima (or minima) should be found
     */
    public void run(ImageProcessor ip) {
        Prefs.noPointLabels = true;
        Roi roi = imp.getRoi();
        if (outputType == POINT_SELECTION && !roiSaved) {
            imp.saveRoi(); // save previous selection so user can restore it
            roiSaved = true;
        }
        if (roi!=null && (!roi.isArea() || outputType==SEGMENTED)) {
            imp.killRoi();
            roi = null;
        }
        boolean invertedLut = imp.isInvertedLut();
        double threshold = useMinThreshold?ip.getMinThreshold():ImageProcessor.NO_THRESHOLD;
        if ((invertedLut&&!lightBackground) || (!invertedLut&&lightBackground)) {
            threshold = ImageProcessor.NO_THRESHOLD;    //don't care about threshold when finding minima
            float[] cTable = ip.getCalibrationTable();
            ip = ip.duplicate();
            if (cTable==null) {                 //invert image for finding minima of uncalibrated images
                ip.invert();
            } else {                            //we are using getPixelValue, so the CalibrationTable must be inverted
                float[] invertedCTable = new float[cTable.length];
                for (int i=cTable.length-1; i>=0; i--)
                    invertedCTable[i] = -cTable[i];
                ip.setCalibrationTable(invertedCTable);
            }
            ip.setRoi(roi);
        }
        ByteProcessor outIp = null;
        outIp = findMaxima(ip, tolerance, threshold, outputType, excludeOnEdges, false); //process the image
        if (!previewing) Prefs.noPointLabels = noPointLabels;
        if (outIp == null) return;              //cancelled by user or previewing or no output image
        if (!Prefs.blackBackground)             //normally, output has an inverted LUT, "active" pixels black (255) - like a mask
            outIp.invertLut();
        String resultName;
        if (outputType == SEGMENTED)            //Segmentation required
            resultName = " Segmented";
        else
            resultName = " Maxima";
        String outname = imp.getTitle();
        if (imp.getNSlices()>1)
            outname += "("+imp.getCurrentSlice()+")";
        outname += resultName;
        if (WindowManager.getImage(outname)!=null)
            outname = WindowManager.getUniqueName(outname);
        IjxImagePlus maxImp = IJ.getFactory().newImagePlus(outname, outIp);
        Calibration cal = imp.getCalibration().copy();
        cal.disableDensityCalibration();
        maxImp.setCalibration(cal);             //keep the spatial calibration
        maxImp.show();
     } //public void run

    /** Here the processing is done: Find the maxima of an image (does not find minima).
     * @param ip             The input image
     * @param tolerance      Height tolerance: maxima are accepted only if protruding more than this value
     *                       from the ridge to a higher maximum
     * @param threshold      minimum height of a maximum (uncalibrated); for no minimum height set it to
     *                       ImageProcessor.NO_THRESHOLD
     * @param outputType     What to mark in output image: SINGLE_POINTS, IN_TOLERANCE or SEGMENTED.
     *                       No output image is created for output types POINT_SELECTION, LIST and COUNT.
     * @param excludeOnEdges Whether to exclude edge maxima
     * @param isEDM          Whether the image is a float Euclidian Distance Map
     * @return               A new byteProcessor with a normal (uninverted) LUT where the marked points
     *                       are set to 255 (Background 0). Pixels outside of the roi of the input ip are not set.
     *                       Returns null if outputType does not require an output or if cancelled by escape
     */
    public ByteProcessor findMaxima(ImageProcessor ip, double tolerance, double threshold,
            int outputType, boolean excludeOnEdges, boolean isEDM) {
        if (dirOffset == null) makeDirectionOffsets(ip);
        Rectangle roi = ip.getRoi();
        byte[] mask = ip.getMaskArray();
        if (threshold!=ImageProcessor.NO_THRESHOLD && ip.getCalibrationTable()!=null &&
                threshold>0 && threshold<ip.getCalibrationTable().length)
            threshold = ip.getCalibrationTable()[(int)threshold];   //convert threshold to calibrated
        ByteProcessor typeP = new ByteProcessor(width, height);     //will be a notepad for pixel types
        byte[] types = (byte[])typeP.getPixels();
        float globalMin = Float.MAX_VALUE;
        float globalMax = -Float.MAX_VALUE;
        for (int y=roi.y; y<roi.y+roi.height; y++) {         //find local minimum/maximum now
            for (int x=roi.x; x<roi.x+roi.width; x++) {      //ImageStatistics won't work if we have no IjxImagePlus
                float v = ip.getPixelValue(x, y);
                if (globalMin>v) globalMin = v;
                if (globalMax<v) globalMax = v;
            }
        }
        if (threshold !=ImageProcessor.NO_THRESHOLD)
            threshold -= (globalMax-globalMin)*1e-6;//avoid rounding errors
        //for segmentation, exclusion of edge maxima cannot be done now but has to be done after segmentation:
        boolean excludeEdgesNow = excludeOnEdges && outputType!=SEGMENTED;

        if (Thread.currentThread().isInterrupted()) return null;
        IJ.showStatus("Getting sorted maxima...");
        long[] maxPoints = getSortedMaxPoints(ip, typeP, excludeEdgesNow, isEDM, globalMin, globalMax, threshold); 
        if (Thread.currentThread().isInterrupted()) return null;
        IJ.showStatus("Analyzing  maxima...");
        analyzeAndMarkMaxima(ip, typeP, maxPoints, excludeEdgesNow, isEDM, globalMin, tolerance);
        //IJ.getFactory().newImagePlus("Pixel types",typeP.duplicate()).show();
        if (outputType==POINT_SELECTION || outputType==LIST || outputType==COUNT)
            return null;
        
        ByteProcessor outIp;
        byte[] pixels;
        if (outputType == SEGMENTED) {
            // Segmentation required, convert to 8bit (also for 8-bit images, since the calibration
            // may have a negative slope). outIp has background 0, maximum areas 255
            outIp = make8bit(ip, typeP, isEDM, globalMin, globalMax, threshold);
            //if (IJ.debugMode) IJ.getFactory().newImagePlus("pixel types precleanup", typeP.duplicate()).show();
            cleanupMaxima(outIp, typeP, maxPoints);     //eliminate all the small maxima (i.e. those outside MAX_AREA)
            //if (IJ.debugMode) IJ.getFactory().newImagePlus("pixel types postcleanup", typeP).show();
            //if (IJ.debugMode) IJ.getFactory().newImagePlus("pre-watershed", outIp.duplicate()).show();
            if (!watershedSegment(outIp))               //do watershed segmentation
                return null;                            //if user-cancelled, return
            if (!isEDM) cleanupExtraLines(outIp);       //eliminate lines due to local minima (none in EDM)
            watershedPostProcess(outIp);                //levels to binary image
            if (excludeOnEdges) deleteEdgeParticles(outIp, typeP);
        } else {                                        //outputType other than SEGMENTED
            for (int i=0; i<width*height; i++)
                types[i] = (byte)(((types[i]&outputTypeMasks[outputType])!=0)?255:0);
            outIp = typeP;
        }
        byte[] outPixels = (byte[])outIp.getPixels();
        //IJ.write("roi: "+roi.toString());
        if (roi!=null) {
            for (int y=0, i=0; y<outIp.getHeight(); y++) { //delete everything outside roi
                for (int x=0; x<outIp.getWidth(); x++, i++) {
                    if (x<roi.x || x>=roi.x+roi.width || y<roi.y || y>=roi.y+roi.height) outPixels[i] = (byte)0;
                    else if (mask !=null && (mask[x-roi.x + roi.width*(y-roi.y)]==0)) outPixels[i] = (byte)0;
                }
            }
        }

        return outIp;
    } // public ByteProcessor findMaxima

    /** Find all local maxima (irrespective whether they finally qualify as maxima or not)
     * @param ip    The image to be analyzed
     * @param typeP A byte image, same size as ip, where the maximum points are marked as MAXIMUM
     *              (do not use it as output: for rois, the points are shifted w.r.t. the input image)
     * @param excludeEdgesNow Whether to exclude edge pixels
     * @param isEDM     Whether ip is a float Euclidian distance map
     * @param globalMin The minimum value of the image or roi
     * @param threshold The threshold (calibrated) below which no pixels are processed. Ignored if ImageProcessor.NO_THRESHOLD
     * @return          Maxima sorted by value. In each array element (long, i.e., 64-bit integer), the value
     *                  is encoded in the upper 32 bits and the pixel offset in the lower 32 bit
     * Note: Do not use the positions of the points marked as MAXIMUM in typeP, they are invalid for images with a roi.
     */    
    long[] getSortedMaxPoints(ImageProcessor ip, ByteProcessor typeP, boolean excludeEdgesNow,
            boolean isEDM, float globalMin, float globalMax, double threshold) {
        Rectangle roi = ip.getRoi();
        byte[] types =  (byte[])typeP.getPixels();
        int nMax = 0;  //counts local maxima
        boolean checkThreshold = threshold!=ImageProcessor.NO_THRESHOLD;
        Thread thread = Thread.currentThread();
        //long t0 = System.currentTimeMillis();
        for (int y=roi.y; y<roi.y+roi.height; y++) {         // find local maxima now
            if (y%50==0 && thread.isInterrupted()) return null;
            for (int x=roi.x, i=x+y*width; x<roi.x+roi.width; x++, i++) {      // for better performance with rois, restrict search to roi
                float v = ip.getPixelValue(x,y);
                float vTrue = isEDM ? trueEdmHeight(x,y,ip) : v;  // for EDMs, use interpolated ridge height
                if (v==globalMin) continue;
                if (excludeEdgesNow && (x==0 || x==width-1 || y==0 || y==height-1)) continue;
                if (checkThreshold && v<threshold) continue;
                boolean isMax = true;
                /* check wheter we have a local maximum.
                 Note: For an EDM, we need all maxima: those of the EDM-corrected values
                 (needed by findMaxima) and those of the raw values (needed by cleanupMaxima) */
                boolean isInner = (y!=0 && y!=height-1) && (x!=0 && x!=width-1); //not necessary, but faster than isWithin
                for (int d=0; d<8; d++) {                         // compare with the 8 neighbor pixels
                    if (isInner || isWithin(x, y, d)) {
                        float vNeighbor = ip.getPixelValue(x+DIR_X_OFFSET[d], y+DIR_Y_OFFSET[d]);
                        float vNeighborTrue = isEDM ? trueEdmHeight(x+DIR_X_OFFSET[d], y+DIR_Y_OFFSET[d], ip) : vNeighbor;
                        if (vNeighbor > v && vNeighborTrue > vTrue) {
                            isMax = false;
                            break;
                        }
                    }
                }
                if (isMax) {
                    types[i] = MAXIMUM;
                    nMax++;
                }
            } // for x
        } // for y
        if (thread.isInterrupted()) return null;
        //long t1 = System.currentTimeMillis();IJ.log("markMax:"+(t1-t0));
        
        float vFactor = (float)(2e9/(globalMax-globalMin)); //for converting float values into a 32-bit int
        long[] maxPoints = new long[nMax];                  //value (int) is in the upper 32 bit, pixel offset in the lower
        int iMax = 0;
        for (int y=roi.y; y<roi.y+roi.height; y++)           //enter all maxima into an array
            for (int x=roi.x, p=x+y*width; x<roi.x+roi.width; x++, p++)
                if (types[p]==MAXIMUM) {
                    float fValue = isEDM?trueEdmHeight(x,y,ip):ip.getPixelValue(x,y);
                    int iValue = (int)((fValue-globalMin)*vFactor); //32-bit int, linear function of float value
                    maxPoints[iMax++] = (long)iValue<<32|p;
                }
        //long t2 = System.currentTimeMillis();IJ.log("makeArray:"+(t2-t1));
        if (thread.isInterrupted()) return null;
        Arrays.sort(maxPoints);                                 //sort the maxima by value
        //long t3 = System.currentTimeMillis();IJ.log("sort:"+(t3-t2));
        return maxPoints;
    } //getSortedMaxPoints

   /** Check all maxima in list maxPoints, mark type of the points in typeP
    * @param ip             the image to be analyzed
    * @param typeP          8-bit image, here the point types are marked by type: MAX_POINT, etc.
    * @param maxPoints      input: a list of all local maxima, sorted by height. Lower 32 bits are pixel offset
    * @param excludeEdgesNow whether to avoid edge maxima
    * @param isEDM          whether ip is a (float) Euclidian distance map
    * @param globalMin      minimum pixel value in ip
    * @param tolerance      minimum pixel value difference for two separate maxima
    */   
   void analyzeAndMarkMaxima(ImageProcessor ip, ByteProcessor typeP, long[] maxPoints, boolean excludeEdgesNow, boolean isEDM, float globalMin, double tolerance) {
        byte[] types =  (byte[])typeP.getPixels();
        int nMax = maxPoints.length;
        int [] pList = new int[width*height];       //here we enter points starting from a maximum
        Vector xyVector = null;
        Roi roi = null;
        boolean displayOrCount = imp!=null && (outputType==POINT_SELECTION||outputType==LIST||outputType==COUNT);
        if (displayOrCount) {
            xyVector=new Vector();
            roi = imp.getRoi();
        }
        for (int iMax=nMax-1; iMax>=0; iMax--) {    //process all maxima now, starting from the highest
            if (iMax%100 == 0 && Thread.currentThread().isInterrupted()) return;
            int offset0 = (int)maxPoints[iMax];     //type cast gets 32 lower bits, where pixel index is encoded
            //int offset0 = maxPoints[iMax].offset;
            if ((types[offset0]&PROCESSED)!=0)      //this maximum has been reached from another one, skip it
                continue;
            //we create a list of connected points and start the list at the current maximum
            pList[0] = offset0;
            types[offset0] |= (EQUAL|LISTED);       //mark first point as equal height (to itself) and listed
            int listLen = 1;                        //number of elements in the list
            int listI = 0;                          //index of current element in the list
            int x0 = offset0 % width;               
            int y0 = offset0 / width;
            float v = isEDM?trueEdmHeight(x0,y0,ip):ip.getPixelValue(x0,y0);
            boolean isEdgeMaximum = (x0==0 || x0==width-1 || y0==0 || y0==height-1);
            boolean maxPossible = true;             //it may be a true maximum
            double xEqual = x0;                     //for creating a single point: determine average over the
            double yEqual = y0;                     //  coordinates of contiguous equal-height points
            int nEqual = 1;                         //counts xEqual/yEqual points that we use for averaging
            do {
                int offset = pList[listI];
                int x = offset % width;
                int y = offset / width;
                boolean isInner = (y!=0 && y!=height-1) && (x!=0 && x!=width-1); //not necessary, but faster than isWithin
                for (int d=0; d<8; d++) {           //analyze all neighbors (in 8 directions) at the same level
                    int offset2 = offset+dirOffset[d];
                    if ((isInner || isWithin(x, y, d)) && (types[offset2]&LISTED)==0) {
                        if ((types[offset2]&PROCESSED)!=0) {
                            maxPossible = false;    //we have reached a point processed previously, thus it is no maximum now
                            //if(xList[0]>510&&xList[0]<77)IJ.write("stop at processed neighbor from x,y="+xList[listI]+","+yList[listI]+", dir="+d);
                            break;
                        }
                        int x2 = x+DIR_X_OFFSET[d];
                        int y2 = y+DIR_Y_OFFSET[d];
                        float v2 = ip.getPixelValue(x2, y2);
                        if (isEDM && (v2 <=v-(float)tolerance)) v2 = trueEdmHeight(x2, y2, ip); //correcting for EDM peaks may move the point up
                        if (v2 > v) {
                            maxPossible = false;    //we have reached a higher point, thus it is no maximum
                            //if(xList[0]>510&&xList[0]<77)IJ.write("stop at higher neighbor from x,y="+xList[listI]+","+yList[listI]+", dir,value,value2,v2-v="+d+","+v+","+v2+","+(v2-v));
                            break;
                        } else if (v2 >= v-(float)tolerance) {
                            pList[listLen] = offset2;
                            listLen++;              //we have found a new point within the tolerance
                            types[offset2] |= LISTED;
                            if (x2==0 || x2==width-1 || y2==0 || y2==height-1) {
                                isEdgeMaximum = true;
                                if (excludeEdgesNow) {
                                    maxPossible = false;
                                    break;          //we have an edge maximum;
                                }
                            }
                            if (v2==v) {            //prepare finding center of equal points (in case single point needed)
                                types[offset2] |= EQUAL;
                                xEqual += x2;
                                yEqual += y2;
                                nEqual ++;
                            }
                        }
                    } // if isWithin & not LISTED
                } // for directions d
                listI++;
            } while (listI < listLen);
            byte resetMask = (byte)~(maxPossible?LISTED:(LISTED|EQUAL));
            xEqual /= nEqual;
            yEqual /= nEqual;
            double minDist2 = 1e20;
            int nearestI = 0;
            for (listI=0; listI<listLen; listI++) {
                int offset = pList[listI];
                int x = offset % width;
                int y = offset / width;
                types[offset] &= resetMask;     //reset attributes no longer needed
                types[offset] |= PROCESSED;     //mark as processed
                if (maxPossible) {
                    types[offset] |= MAX_AREA;
                    if ((types[offset]&EQUAL)!=0) {
                        double dist2 = (xEqual-x)*(double)(xEqual-x) + (yEqual-y)*(double)(yEqual-y);
                        if (dist2 < minDist2) {
                            minDist2 = dist2;   //this could be the best "single maximum" point
                            nearestI = listI;
                        }
                    }
                }
            } // for listI
            if (maxPossible) {
                int offset = pList[nearestI];
                types[offset] |= MAX_POINT;
                if (displayOrCount && !(this.excludeOnEdges && isEdgeMaximum)) {
                    int x = offset % width;
                    int y = offset / width;
                    if (roi==null || roi.contains(x, y))
                        xyVector.addElement(new int[] {x, y});
                }
            }
        } // for all maxima iMax
        if (Thread.currentThread().isInterrupted()) return;
        if (displayOrCount && xyVector!=null) {
            int npoints = xyVector.size();
            if (outputType == POINT_SELECTION && npoints>0) {
                int[] xpoints = new int[npoints];
                int[] ypoints = new int[npoints];
                for (int i=0; i<npoints; i++) {
                    int[] xy = (int[])xyVector.elementAt(i);
                    xpoints[i] = xy[0];
                    ypoints[i] = xy[1];
                }
                imp.setRoi(new PointRoi(xpoints, ypoints, npoints));
            } else if (outputType==LIST) {
                Analyzer.resetCounter();
                ResultsTable rt = ResultsTable.getResultsTable();
                for (int i=0; i<npoints; i++) {
                    int[] xy = (int[])xyVector.elementAt(i);
                    rt.incrementCounter();
                    rt.addValue("X", xy[0]);
                    rt.addValue("Y", xy[1]);
                }
                rt.show("Results");
            } else if (outputType==COUNT) {
                ResultsTable rt = ResultsTable.getResultsTable();
                rt.incrementCounter();
                rt.setValue("Count", rt.getCounter()-1, npoints);
                rt.show("Results");
            } 
        }
        if (previewing)
            messageArea.setText((xyVector==null ? 0 : xyVector.size())+" Maxima");
    } //void analyzeAndMarkMaxima

   /** Create an 8-bit image by scaling the pixel values of ip to 1-254 (<lower threshold 0) and mark maximum areas as 255.
    * For use as input for watershed segmentation
    * @param ip         The original image that should be segmented
    * @param typeP      Pixel types in ip
    * @param isEDM      Whether ip is an Euclidian distance map
    * @param globalMin  The minimum pixel value of ip
    * @param globalMax  The maximum pixel value of ip
    * @param threshold  Pixels of ip below this value (calibrated) are considered background. Ignored if ImageProcessor.NO_THRESHOLD
    * @return           The 8-bit output image.
    */
    ByteProcessor make8bit(ImageProcessor ip, ByteProcessor typeP, boolean isEDM, float globalMin, float globalMax, double threshold) {
        byte[] types = (byte[])typeP.getPixels();
        double minValue;
        if (isEDM) {
            threshold = 0.5;
            minValue = 1.;
        } else
            minValue = (threshold == ImageProcessor.NO_THRESHOLD)?globalMin:threshold;
        double offset = minValue - (globalMax-minValue)*(1./253/2-1e-6); //everything above minValue should become >(byte)0
        double factor = 253/(globalMax-minValue);
        //IJ.write("min,max="+minValue+","+globalMax+"; offset, 1/factor="+offset+", "+(1./factor));
        if (isEDM && factor>1)
            factor = 1;   // with EDM, no better resolution
        ByteProcessor outIp = new ByteProcessor(width, height);
        //convert possibly calibrated image to byte without damaging threshold (setMinAndMax would kill threshold)
        byte[] pixels = (byte[])outIp.getPixels();
        long v;
        for (int y=0, i=0; y<height; y++) {
            for (int x=0; x<width; x++, i++) {
                float rawValue = ip.getPixelValue(x, y);
                if (threshold!=ImageProcessor.NO_THRESHOLD && rawValue<threshold)
                    pixels[i] = (byte)0;
                else if ((types[i]&MAX_AREA)!=0)
                    pixels[i] = (byte)255;  //prepare watershed by setting "true" maxima+surroundings to 255
                else {
                    v = 1 + Math.round((rawValue-offset)*factor);
                    if (v < 1) pixels[i] = (byte)1;
                    else if (v<=254) pixels[i] = (byte)(v&255);
                    else pixels[i] = (byte)254;
                }
            }
        }
        return outIp;
    } // byteProcessor make8bit

   /** Get estimated "true" height of a maximum or saddle point of a Euclidian Distance Map.
     * This is needed since the point sampled is not necessarily at the highest position.
     * For simplicity, we don't care about the Sqrt(5) distance here although this would be more accurate
     * @param x     x-position of the point
     * @param y     y-position of the point
     * @param ip    the EDM (FloatProcessor)
     * @return      estimated height
     */
    float trueEdmHeight(int x, int y, ImageProcessor ip) {
        int xmax = width - 1;
        int ymax = ip.getHeight() - 1;
        float[] pixels = (float[])ip.getPixels();
        int offset = x + y*width;
        float v =  pixels[offset];
        if (x==0 || y==0 || x==xmax || y==ymax || v==0) {
            return v;                               //don't recalculate for edge pixels or background
        } else {
            float trueH = v + 0.5f*SQRT2;           //true height can never by higher than this
            boolean ridgeOrMax = false;
            for (int d=0; d<4; d++) {               //for all directions halfway around:
                int d2 = (d+4)%8;                   //get the opposite direction and neighbors
                float v1 = pixels[offset+dirOffset[d]];
                float v2 = pixels[offset+dirOffset[d2]];
                float h;
                if (v>=v1 && v>=v2) {
                    ridgeOrMax = true;
                    h = (v1 + v2)/2;
                } else {
                    h = Math.min(v1, v2);
                }
                h += (d%2==0) ? 1 : SQRT2;          //in diagonal directions, distance is sqrt2
                if (trueH > h) trueH = h;
            }
            if (!ridgeOrMax) trueH = v;
            return trueH;
        }
    }
    
    /** eliminate unmarked maxima for use by watershed. Starting from each previous maximum,
     * explore the surrounding down to successively lower levels until a marked maximum is
     * touched (or the plateau of a previously eliminated maximum leads to a marked maximum).
     * Then set all the points above this value to this value
     * @param outIp     the image containing the pixel values
     * @param typeP     the types of the pixels are marked here
     * @param maxPoints array containing the coordinates of all maxima that might be relevant
     */    
    void cleanupMaxima(ByteProcessor outIp, ByteProcessor typeP, long[] maxPoints) {
        byte[] pixels = (byte[])outIp.getPixels();
        byte[] types = (byte[])typeP.getPixels();
        int nMax = maxPoints.length;
        int[] pList = new int[width*height];
        for (int iMax = nMax-1; iMax>=0; iMax--) {
            int offset0 = (int)maxPoints[iMax];     //type cast gets lower 32 bits where pixel offset is encoded
            if ((types[offset0]&(MAX_AREA|ELIMINATED))!=0) continue;
            int level = pixels[offset0]&255;
            int loLevel = level+1;
            pList[0] = offset0;                     //we start the list at the current maximum
            //if (xList[0]==122) IJ.write("max#"+iMax+" at x,y="+xList[0]+","+yList[0]+"; level="+level);
            types[offset0] |= LISTED;               //mark first point as listed
            int listLen = 1;                        //number of elements in the list
            int lastLen = 1;
            int listI = 0;                          //index of current element in the list
            boolean saddleFound = false;
            while (!saddleFound && loLevel >0) {
                loLevel--;
                lastLen = listLen;                  //remember end of list for previous level
                listI = 0;                          //in each level, start analyzing the neighbors of all pixels
                do {                                //for all pixels listed so far
                    int offset = pList[listI];
                    int x = offset % width;
                    int y = offset / width;
                    boolean isInner = (y!=0 && y!=height-1) && (x!=0 && x!=width-1); //not necessary, but faster than isWithin
                    for (int d=0; d<8; d++) {       //analyze all neighbors (in 8 directions) at the same level
                        int offset2 = offset+dirOffset[d];
                        if ((isInner || isWithin(x, y, d)) && (types[offset2]&LISTED)==0) {
                            if ((types[offset2]&MAX_AREA)!=0 || (((types[offset2]&ELIMINATED)!=0) && (pixels[offset2]&255)>=loLevel)) {
                                saddleFound = true; //we have reached a point touching a "true" maximum...
                                //if (xList[0]==122) IJ.write("saddle found at level="+loLevel+"; x,y="+xList[listI]+","+yList[listI]+", dir="+d);
                                break;              //...or a level not lower, but touching a "true" maximum
                            } else if ((pixels[offset2]&255)>=loLevel && (types[offset2]&ELIMINATED)==0) {
                                pList[listLen] = offset2;
                                //xList[listLen] = x+DIR_X_OFFSET[d];
                                //yList[listLen] = x+DIR_Y_OFFSET[d];
                                listLen++;          //we have found a new point to be processed
                                types[offset2] |= LISTED;
                            }
                        } // if isWithin & not LISTED
                    } // for directions d
                    if (saddleFound) break;         //no reason to search any further
                    listI++;
                } while (listI < listLen);
            } // while !levelFound && loLevel>=0
            for (listI=0; listI<listLen; listI++)   //reset attribute since we may come to this place again
                types[pList[listI]] &= ~LISTED;
            for (listI=0; listI<lastLen; listI++) { //for all points higher than the level of the saddle point
                int offset = pList[listI];
                pixels[offset] = (byte)loLevel;     //set pixel value to the level of the saddle point
                types[offset] |= ELIMINATED;        //mark as processed: there can't be a local maximum in this area
            }
        } // for all maxima iMax
    } // void cleanupMaxima

    /** Delete extra structures form watershed of non-EDM images, e.g., foreground patches,
     *  single dots and lines ending somewhere within a segmented particle
     *  Needed for post-processing watershed-segmented images that can have local minima
     *  @param ip 8-bit image with background = 0, lines between 1 and 254 and segmented particles = 255
     */    
    void cleanupExtraLines(ImageProcessor ip) {
        byte[] pixels =  (byte[])ip.getPixels();
        for (int y=0, i=0; y<height; y++) {
            for (int x=0; x<width; x++,i++) {
                int v = pixels[i];
                if (v!=(byte)255 && v!=0) {
                    int nRadii = nRadii(pixels, x, y);     //number of lines radiating
                    if (nRadii==0)                              //single point or foreground patch?
                        pixels[i] = (byte)255;
                    else if (nRadii==1)
                        removeLineFrom(pixels, x, y);
                } // if v<255 && v>0
            } // for x
        } // for y
    } // void cleanupExtraLines

    /** delete a line starting at x, y up to the next (4-connected) vertex */
    void removeLineFrom (byte[] pixels, int x, int y) {
        //IJ.log("del line from "+x+","+y);
        //if(x<50&&y<40)IJ.write("x,y start="+x+","+y);
        pixels[x + width*y] = (byte)255;                        //delete the first point
        boolean continues;
        do {
            continues = false;
            boolean isInner = (y!=0 && y!=height-1) && (x!=0 && x!=width-1); //not necessary, but faster than isWithin
            for (int d=0; d<8; d+=2) {                          //analyze 4-connected neighbors
                if (isInner || isWithin(x, y, d)) {
                    int v = pixels[x + width*y + dirOffset[d]];
                    if (v!=(byte)255 && v!=0) {
                        int nRadii = nRadii(pixels, x+DIR_X_OFFSET[d], y+DIR_Y_OFFSET[d]);
                        if (nRadii<=1) {                        //found a point or line end
                            x += DIR_X_OFFSET[d];
                            y += DIR_Y_OFFSET[d];
                            pixels[x + width*y] = (byte)255;    //delete the point
                            continues = nRadii==1;              //continue along that line
                            break;
                        }
                    }
                }
            } // for directions d
        //if(!continues && x<50&&y<40)IJ.write("x,y end="+x+","+y);
        } while (continues);
        //IJ.log("deleted to "+x+","+y);
    } // void removeLineFrom

    /** Analyze the neighbors of a pixel (x, y) in a byte image; pixels <255 ("non-white") are
     * considered foreground. Edge pixels are considered foreground.
     * @param   ip
     * @param   x coordinate of the point
     * @param   y coordinate of the point
     * @return  Number of 4-connected lines emanating from this point. Zero if the point is
     *          embedded in either foreground or background
     */
    int nRadii (byte[] pixels, int x, int y) {
        int offset = x + y*width;
        int countTransitions = 0;
        boolean prevPixelSet = true;
        boolean firstPixelSet = true;           //initialize to make the compiler happy
        boolean isInner = (y!=0 && y!=height-1) && (x!=0 && x!=width-1); //not necessary, but faster than isWithin
        for (int d=0; d<8; d++) {               //walk around the point and note every no-line->line transition
            boolean pixelSet = prevPixelSet;
            if (isInner || isWithin(x, y, d)) {
                boolean isSet = (pixels[offset+dirOffset[d]]!=(byte)255);
                if ((d&1)==0) pixelSet = isSet; //non-diagonal directions: always regarded
                else if (!isSet)                //diagonal directions may separate two lines,
                    pixelSet = false;           //    but are insufficient for a 4-connected line
            } else {
                pixelSet = true;
            }
            if (pixelSet && !prevPixelSet)
                countTransitions ++;
            prevPixelSet = pixelSet;
            if (d==0)
                firstPixelSet = pixelSet;
        }
        if (firstPixelSet && !prevPixelSet)
            countTransitions ++;
        return countTransitions;
    } // int nRadii

    /** after watershed, set all pixels in the background and segmentation lines to 0
     */
    private void watershedPostProcess(ImageProcessor ip) {
        //IJ.getFactory().newImagePlus("before postprocess",ip.duplicate()).show();
        byte[] pixels = (byte[])ip.getPixels();
        int size = ip.getWidth()*ip.getHeight();
        for (int i=0; i<size; i++) {
           if ((pixels[i]&255)<255)
                pixels[i] = (byte)0;
        }
        //IJ.getFactory().newImagePlus("after postprocess",ip.duplicate()).show();
    }

    /** delete particles corresponding to edge maxima
     * @param typeP Here the pixel types of the original image are noted,
     * pixels with bit MAX_AREA at the edge are considered indicators of an edge maximum.
     * @param ip the image resulting from watershed segmentaiton
     * (foreground pixels, i.e. particles, are 255, background 0)
     */
    void deleteEdgeParticles(ByteProcessor ip, ByteProcessor typeP) {
        byte[] pixels = (byte[])ip.getPixels();
        byte[] types = (byte[])typeP.getPixels();
        width = ip.getWidth();
        height = ip.getHeight();
        ip.setValue(0);
        Wand wand = new Wand(ip);
        for (int x=0; x<width; x++) {
            int y = 0;
            if ((types[x+y*width]&MAX_AREA) != 0 && pixels[x+y*width] != 0)
                deleteParticle(x,y,ip,wand);
            y = height - 1;
            if ((types[x+y*width]&MAX_AREA) != 0 && pixels[x+y*width] != 0)
                deleteParticle(x,y,ip,wand);            
        }
        for (int y=1; y<height-1; y++) {
            int x = 0;
            if ((types[x+y*width]&MAX_AREA) != 0 && pixels[x+y*width] != 0)
                deleteParticle(x,y,ip,wand);
            x = width - 1;
            if ((types[x+y*width]&MAX_AREA) != 0 && pixels[x+y*width] != 0)
                deleteParticle(x,y,ip,wand);            
        }
    } //void deleteEdgeParticles

    /** delete a particle (set from value 255 to current fill value).
     * Position x,y must be within the particle
     */
    void deleteParticle(int x, int y, ByteProcessor ip, Wand wand) {
        wand.autoOutline(x, y, 255, 255);
        if (wand.npoints==0) {
            IJ.log("wand error selecting edge particle at x, y = "+x+", "+y);
            return;
        }
        Roi roi = new PolygonRoi(wand.xpoints, wand.ypoints, wand.npoints, Roi.TRACED_ROI);
        ip.snapshot();                  //prepare for reset outside of mask
        ip.setRoi(roi);
        ip.fill();
        ip.reset(ip.getMask());
    }

    /** Do watershed segmentation on a byte image, with the start points (maxima)
     * set to 255 and the background set to 0. The image should not have any local maxima
     * other than the marked ones. Local minima will lead to artifacts that can be removed
     * later. On output, all particles will be set to 255, segmentation lines remain at their
     * old value.
     * @param ip  The byteProcessor containing the image, with size given by the class variables width and height
     * @return    false if canceled by the user (note: can be cancelled only if called by "run" with a known IjxImagePlus)
     */    
    private boolean watershedSegment(ByteProcessor ip) {
        boolean debug = IJ.debugMode;
        IjxImageStack movie=null;
        if (debug) {
            movie = IJ.getFactory().newImageStack(ip.getWidth(), ip.getHeight());
            movie.addSlice("pre-watershed EDM", ip.duplicate());
        }
        byte[] pixels = (byte[])ip.getPixels();
        // Create an array with the coordinates of all points between value 1 and 254
        // This method, suggested by Stein Roervik (stein_at_kjemi-dot-unit-dot-no),
        // greatly speeds up the watershed segmentation routine.
        int[] histogram = ip.getHistogram();
        int arraySize = width*height - histogram[0] -histogram[255];
        int[] coordinates = new int[arraySize];    //from pixel coordinates, low bits x, high bits y
        int highestValue = 0;
        int maxBinSize = 0;
        int offset = 0;
        int[] levelStart = new int[256];
        for (int v=1; v<255; v++) {
            levelStart[v] = offset;
            offset += histogram[v];
            if (histogram[v] > 0) highestValue = v;
            if (histogram[v] > maxBinSize) maxBinSize = histogram[v];
        }
        int[] levelOffset = new int[highestValue + 1];
        for (int y=0, i=0; y<height; y++) {
            for (int x=0; x<width; x++, i++) {
                int v = pixels[i]&255;
                if (v>0 && v<255) {
                    offset = levelStart[v] + levelOffset[v];
                    coordinates[offset] = x | y<<intEncodeShift;
                    levelOffset[v] ++;
                }
           } //for x
        } //for y
        // Create an array of the points (pixel offsets) that we set to 255 in one pass.
        // If we remember this list we need not create a snapshot of the ImageProcessor. 
        int[] setPointList = new int[Math.min(maxBinSize, (width*height+2)/3)];
        // now do the segmentation, starting at the highest level and working down.
        // At each level, dilate the particle (set pixels to 255), constrained to pixels
        // whose values are at that level and also constrained (by the fateTable)
        // to prevent features from merging.
        int[] table = makeFateTable();
        IJ.showStatus("Segmenting (Esc to cancel)");
        final int[] directionSequence = new int[] {7, 3, 1, 5, 0, 4, 2, 6}; // diagonal directions first
        for (int level=highestValue; level>=1; level--) {
            int remaining = histogram[level];  //number of points in the level that have not been processed
            int idle = 0;
            while (remaining>0 && idle<8) {
                int sumN = 0;
                int dIndex = 0;
                do {                        // expand each level in 8 directions
                    int n = processLevel(directionSequence[dIndex%8], ip, table,
                            levelStart[level], remaining, coordinates, setPointList);
                    //IJ.log("level="+level+" direction="+directionSequence[dIndex%8]+" remain="+remaining+"-"+n);
                    remaining -= n;         // number of points processed
                    sumN += n;
                    if (n > 0) idle = 0;    // nothing processed in this direction?
                    dIndex++;
                } while (remaining>0 && idle++<8);
                addProgress(sumN/(double)arraySize);
                if (IJ.escapePressed()) {   // cancelled by the user
                    IJ.beep();
                    IJ.showProgress(1.0);
                    return false;
                }
            }
            if (remaining>0 && level>1) {   // any pixels that we have not reached?
                int nextLevel = level;      // find the next level to process
                do
                    nextLevel--;
                while (nextLevel>1 && histogram[nextLevel]==0);
                // in principle we should add all unprocessed pixels of this level to the
                // tasklist of the next level. This would make it very slow for some images,
                // however. Thus we only add the pixels if they are at the border (of the
                // image or a thresholded area) and correct unprocessed pixels at the very
                // end by CleanupExtraLines
                if (nextLevel > 0) {
                    int newNextLevelEnd = levelStart[nextLevel] + histogram[nextLevel];
                    for (int i=0, p=levelStart[level]; i<remaining; i++, p++) {
                        int xy = coordinates[p];
                        int x = xy&intEncodeXMask;
                        int y = (xy&intEncodeYMask)>>intEncodeShift;
                        int pOffset = x + y*width;
                        if ((pixels[pOffset]&255)==255) IJ.log("ERROR");
                        boolean addToNext = false;
                        if (x==0 || y==0 || x==width-1 || y==height-1)
                            addToNext = true;           //image border
                        else for (int d=0; d<8; d++)
                            if (isWithin(x, y, d) && pixels[pOffset+dirOffset[d]]==0) {
                                addToNext = true;       //border of area below threshold
                                break;
                            }
                        if (addToNext)
                            coordinates[newNextLevelEnd++] = xy;
                    }
                    //IJ.log("level="+level+": add "+(newNextLevelEnd-levelStart[nextLevel+1])+" points to "+nextLevel);
                    //tasklist for the next level to process becomes longer by this:
                    histogram[nextLevel] = newNextLevelEnd - levelStart[nextLevel];
                }
            }
            if (debug && (level>170 || level>100 && level<110 || level<10))
                movie.addSlice("level "+level, ip.duplicate());
        }
        if (debug)
            IJ.getFactory().newImagePlus("Segmentation Movie", movie).show();
        return true;
    } // boolean watershedSegment


    /** dilate the UEP on one level by one pixel in the direction specified by step, i.e., set pixels to 255
     * @param pass gives direction of dilation, see makeFateTable
     * @param ip the EDM with the segmeted blobs successively getting set to 255
     * @param table             The fateTable
     * @param levelStart        offsets of the level in pixelPointers[]
     * @param levelNPoints      number of points in the current level
     * @param pixelPointers[]   list of pixel coordinates (x+y*width) sorted by level (in sequence of y, x within each level)
     * @param xCoordinates      list of x Coorinates for the current level only (no offset levelStart)
     * @return                  number of pixels that have been changed
     */
    private int processLevel(int pass, ImageProcessor ip, int[] fateTable,
            int levelStart, int levelNPoints, int[] coordinates, int[] setPointList) {
        int xmax = width - 1;
        int ymax = height - 1;
        byte[] pixels = (byte[])ip.getPixels();
        //byte[] pixels2 = (byte[])ip2.getPixels();
        int nChanged = 0;
        int nUnchanged = 0;
        for (int i=0, p=levelStart; i<levelNPoints; i++, p++) {
            int xy = coordinates[p];
            int x = xy&intEncodeXMask;
            int y = (xy&intEncodeYMask)>>intEncodeShift;
            int offset = x + y*width;
            int index = 0;      //neighborhood pixel ocupation: index in fateTable
            if (y>0 && (pixels[offset-width]&255)==255)
                index ^= 1;
            if (x<xmax && y>0 && (pixels[offset-width+1]&255)==255)
                index ^= 2;
            if (x<xmax && (pixels[offset+1]&255)==255)
                index ^= 4;
            if (x<xmax && y<ymax && (pixels[offset+width+1]&255)==255)
                index ^= 8;
            if (y<ymax && (pixels[offset+width]&255)==255)
                index ^= 16;
            if (x>0 && y<ymax && (pixels[offset+width-1]&255)==255)
                index ^= 32;
            if (x>0 && (pixels[offset-1]&255)==255)
                index ^= 64;
            if (x>0 && y>0 && (pixels[offset-width-1]&255)==255)
                index ^= 128;
            int mask = 1<<pass;
            if ((fateTable[index]&mask)==mask)
                setPointList[nChanged++] = offset;  //remember to set pixel to 255
            else
                coordinates[levelStart+(nUnchanged++)] = xy; //keep this pixel for future passes

        } // for pixel i
        //IJ.log("pass="+pass+", changed="+nChanged+" unchanged="+nUnchanged);
        for (int i=0; i<nChanged; i++)
            pixels[setPointList[i]] = (byte)255;
        return nChanged;
    } //processLevel

    /** Creates the lookup table used by the watershed function for dilating the particles.
     * The algorithm allows dilation in both straight and diagonal directions.
     * There is an entry in the table for each possible 3x3 neighborhood:
     *          x-1          x          x+1
     *  y-1    128            1          2
     *  y       64     pxl_unset_yet     4
     *  y+1     32           16          8
     * (to find throws entry, sum up the numbers of the neighboring pixels set; e.g.
     * entry 6=2+4 if only the pixels (x,y-1) and (x+1, y-1) are set.
     * A pixel is added on the 1st pass if bit 0 (2^0 = 1) is set,
     * on the 2nd pass if bit 1 (2^1 = 2) is set, etc.
     * pass gives the direction of rotation, with 0 = to top left (x--,y--), 1 to top,
     * and clockwise up to 7 = to the left (x--).
     * E.g. 4 = add on 3rd pass, 3 = add on either 1st or 2nd pass.
     */
    private int[] makeFateTable() {
        int[] table = new int[256];
        boolean[] isSet = new boolean[8];
        for (int item=0; item<256; item++) {        //dissect into pixels
            for (int i=0, mask=1; i<8; i++) {
                isSet[i] = (item&mask)==mask;
                mask*=2;
            }
            for (int i=0, mask=1; i<8; i++) {       //we dilate in the direction opposite to the direction of the existing neighbors
                if (isSet[(i+4)%8]) table[item] |= mask;
                mask*=2;
            }
            for (int i=0; i<8; i+=2)                //if side pixels are set, for counting transitions it is as good as if the adjacent edges were also set
                if (isSet[i]) {
                    isSet[(i+1)%8] = true;
                    isSet[(i+7)%8] = true;
                }
            int transitions=0;
            for (int i=0, mask=1; i<8; i++) {
                if (isSet[i] != isSet[(i+1)%8])
                    transitions++;
            }
            if (transitions>=4) {                   //if neighbors contain more than one region, dilation ito this pixel is forbidden
                table[item] = 0;
            } else {
            }
        }
        return table;
    } // int[] makeFateTable

    /** create an array of offsets within a pixel array for directions in clockwise order:
     * 0=(x,y-1), 1=(x+1,y-1), ... 7=(x-1,y)
     * Also creates further class variables:
     * width, height, and the following three values needed for storing coordinates in single ints for watershed:
     * intEncodeXMask, intEncodeYMask and intEncodeShift.
     * E.g., for width between 129 and 256, xMask=0xff and yMask = 0xffffff00 are bitwise masks
     * for x and y, respectively, and shift=8 is the bit shift to get y from the y-masked value
     * Returns as class variables: the arrays of the offsets to the 8 neighboring pixels
     * and the array maskAndShift for watershed
     */
    void makeDirectionOffsets(ImageProcessor ip) {
        width = ip.getWidth();
        height = ip.getHeight();
        int shift = 0, mult=1;
        do {
            shift++; mult*=2;
        }
        while (mult < width);
        intEncodeXMask = mult-1;
        intEncodeYMask = ~intEncodeXMask;
        intEncodeShift = shift;
        //IJ.log("masks (hex):"+Integer.toHexString(xMask)+","+Integer.toHexString(xMask)+"; shift="+shift);
        dirOffset  = new int[] {-width, -width+1, +1, +width+1, +width, +width-1,   -1, -width-1 };
        //dirOffset is created last, so check for it being null before makeDirectionOffsets
        //(in case we have multiple threads using the same MaximumFinder)
    }

    /** returns whether the neighbor in a given direction is within the image
     * NOTE: it is assumed that the pixel x,y itself is within the image!
     * Uses class variables width, height: dimensions of the image
     * @param x         x-coordinate of the pixel that has a neighbor in the given direction
     * @param y         y-coordinate of the pixel that has a neighbor in the given direction
     * @param direction the direction from the pixel towards the neighbor (see makeDirectionOffsets)
     * @return          true if the neighbor is within the image (provided that x, y is within)
     */
    boolean isWithin(int x, int y, int direction) {
        int xmax = width - 1;
        int ymax = height -1;
        switch(direction) {
            case 0:
                return (y>0);
            case 1:
                return (x<xmax && y>0);
            case 2:
                return (x<xmax);
            case 3:
                return (x<xmax && y<ymax);
            case 4:
                return (y<ymax);
            case 5:
                return (x>0 && y<ymax);
            case 6:
                return (x>0);
            case 7:
                return (x>0 && y>0);
        }
        return false;   //to make the compiler happy :-)
    } // isWithin

    /** add work done in the meanwhile and show progress */
    private void addProgress(double deltaProgress) {
        if (nPasses==0) return;
        progressDone += deltaProgress;
        IJ.showProgress(progressDone/nPasses);
    }

}


