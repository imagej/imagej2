package ijx.plugin.filter;
import ijx.process.ImageProcessor;
import ijx.process.FloatProcessor;
import ijx.plugin.api.PlugInFilterRunner;
import ijx.plugin.api.ExtendedPlugInFilter;
import ijx.IJ;

import ijx.gui.dialog.GenericDialog;
import ijx.gui.dialog.DialogListener;

import ijx.plugin.ContrastEnhancer;
import ijx.IjxImagePlus;
import ijx.IjxImageStack;
import java.awt.*;

/** This plugin implements the Mean, Minimum, Maximum, Variance, Median,
 *  Remove Outliers and Despeckle commands. */
public class RankFilters implements ExtendedPlugInFilter, DialogListener {
    public static final int  MEAN=0, MIN=1, MAX=2, VARIANCE=3, MEDIAN=4, OUTLIERS=5, DESPECKLE=6;
    private static final int BRIGHT_OUTLIERS = 0, DARK_OUTLIERS = 1;
    private static final String[] outlierStrings = {"Bright","Dark"};
    // Filter parameters
    private static double radius = 2;
    private static double threshold = 50.;
    private static int whichOutliers = BRIGHT_OUTLIERS;
    private int filterType = MEDIAN;
    // F u r t h e r   c l a s s   v a r i a b l e s
    int flags = DOES_ALL|SUPPORTS_MASKING|CONVERT_TO_FLOAT|SNAPSHOT|KEEP_PREVIEW|
            PARALLELIZE_STACKS;
    private IjxImagePlus imp;
    private int nPasses = 1;            // The number of passes (color channels * stack slices)
    private int pass;                   // Current pass
    protected int kRadius;              // kernel radius. Size is (2*kRadius+1)^2
    protected int kNPoints;             // number of points in the kernel
    protected int[] lineRadius;         // the length of each kernel line is 2*lineRadius+1

    /** Setup of the PlugInFilter. Returns the flags specifying the capabilities and needs
     * of the filter.
     *
     * @param arg   Defines type of filter operation
     * @param imp   The IjxImagePlus to be processed
     * @return      Flags specifying further action of the PlugInFilterRunner
     */    
    public int setup(String arg, IjxImagePlus imp) {
        this.imp = imp;
            if (arg.equals("mean"))
                filterType = MEAN;
            else if (arg.equals("min"))
                filterType = MIN;
            else if (arg.equals("max"))
                filterType = MAX;
            else if (arg.equals("variance"))
                filterType = VARIANCE;
            else if (arg.equals("median"))
                filterType = MEDIAN;
            else if (arg.equals("outliers"))
                filterType = OUTLIERS;
            else if (arg.equals("despeckle"))
                filterType = DESPECKLE;
            else if (arg.equals("masks")) {
                showMasks();
                return DONE;
            } else {
                IJ.error("RankFilters","Argument missing or undefined: "+arg);
                return DONE;
            }
        return flags;
    }

    public int showDialog(IjxImagePlus imp, String command, PlugInFilterRunner pfr) {
        if (filterType == DESPECKLE) {
            filterType = MEDIAN;
            makeKernel(1.0);
        } else {
            GenericDialog gd = new GenericDialog(command+"...");
            gd.addNumericField("Radius", radius, 1, 6, "pixels");
            int digits = imp.getType() == IjxImagePlus.GRAY32 ? 2 : 0;
            if(filterType == OUTLIERS) {
                gd.addNumericField("Threshold", threshold, digits);
                gd.addChoice("Which Outliers", outlierStrings, outlierStrings[whichOutliers]);
            }
            gd.addPreviewCheckbox(pfr);     //passing pfr makes the filter ready for preview
            gd.addDialogListener(this);     //the DialogItemChanged method will be called on user input
            gd.showDialog();                //display the dialog; preview runs in the  now
            if (gd.wasCanceled()) return DONE;
            IJ.register(this.getClass());   //protect static class variables (filter parameters) from garbage collection
        }
        return IJ.setupDialog(imp, flags);  //ask whether to process all slices of stack (if a stack)
    }

    public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
        radius = gd.getNextNumber();
        if (filterType == OUTLIERS) {
            threshold = gd.getNextNumber();
            whichOutliers = gd.getNextChoiceIndex();
        }
        if (gd.invalidNumber() || radius<0 || radius>1000 || (filterType==OUTLIERS && threshold <0))
            return false;
        makeKernel(radius);             //determine the kernel size once for all channels&slices
        return true;
    }

    public void run(ImageProcessor ip) {
        //copy class variables to local ones - this is necessary for preview
        int[] lineRadius;
        int kRadius, kNPoints;
        synchronized(this) {                        //the two following items must be consistent
            lineRadius = (int[])(this.lineRadius.clone()); //cloning also required by doFiltering method
            kRadius = this.kRadius;
            kNPoints = this.kNPoints;
        }
        if (Thread.currentThread().isInterrupted()) return;
        pass++;
        doFiltering((FloatProcessor)ip, kRadius, lineRadius, filterType, whichOutliers, (float)threshold);
        if (imp!=null  && imp.getBitDepth()!=24 && imp.getRoi()==null && filterType==VARIANCE) {
            new ContrastEnhancer().stretchHistogram(this.imp.getProcessor(), 0.5);
        }
    }

    /** Filter a FloatProcessor according to filterType
     * @param ip The image subject to filtering
     * @param kRadius The kernel radius. The kernel has a side length of 2*kRadius+1
     * @param lineRadius The radius of the lines in the kernel. Line length of line i is 2*lineRadius[i]+1.
     * Note that the array <code>lineRadius</code> will be modified, thus call this method
     * with a clone of the original lineRadius array if the array should be used again.
     * @param filterType as defined above; DESPECKLE is not a valid type here.
     * @param threshold Threshold for 'outliers' filter*/
    //
    // Data handling: The area needed for processing a line, i.e. a stripe of width (2*kRadius+1)
    // is written into the array 'cache'. This array is padded at the edges of the image so that
    // a surrounding with radius kRadius for each pixel processed is within 'cache'. Out-of-image
    // pixels are set to the value of the neares edge pixel. When adding a new line, the lines in
    // 'cache' are not shifted but rather the smaller array with the line lengths of the kernel is
    // shifted.
    //
    // Algorithm: For mean and variance, except for small radius, usually do not calculate the
    // sum over all pixels. This sum is calculated for the first pixel of every line. For the
    // following pixels, add the new values and subtract those that are not in the sum any more.
    // For min/max, also first look at the new values, use their maximum if larger than the old
    // one. The look at the values not in the area any more; if it does not contain the old
    // maximum, leave the maximum unchanged. Otherwise, determine the maximum inside the area.
    // For outliers, calculate the median only if the pixel deviates by more than the threshold
    // from any pixel in the area. Therfore min or max is calculated; this is a much faster
    // operation than the median.
    public void doFiltering(FloatProcessor ip, int kRadius, int[] lineRadius, int filterType, int whichOutliers, float threshold) {
        if (imp!= null && IJ.escapePressed()) return;
        boolean minOrMax = filterType == MIN || filterType == MAX;
        boolean minOrMaxOrOutliers = minOrMax || filterType == OUTLIERS;
        boolean sumFilter = filterType == MEAN || filterType == VARIANCE;
        boolean medianFilter = filterType == MEDIAN || filterType == OUTLIERS;
        double[] sums = sumFilter ? new double[2] : null;
        float[] medianBuf1 = medianFilter ? new float[kNPoints] : null;
        float[] medianBuf2 = medianFilter ? new float[kNPoints] : null;
        float sign = filterType==MIN ? -1f : 1f;
        if (filterType == OUTLIERS)     //sign is -1 for high outliers: compare number with minimum
            sign = (ip.isInvertedLut()==(whichOutliers==DARK_OUTLIERS)) ? -1f : 1f;
        float[] pixels = (float[])ip.getPixels();   // array of the pixel values of the input image
        int width = ip.getWidth();
        int height = ip.getHeight();
        Rectangle roi = ip.getRoi();
        int xmin = roi.x - kRadius;
        int xEnd = roi.x + roi.width;
        int xmax = xEnd + kRadius;
        int kSize = 2*kRadius + 1;
        int cacheWidth = xmax - xmin;
        int xminInside = xmin>0 ? xmin : 0;
        int xmaxInside = xmax<width ? xmax : width;
        int widthInside = xmaxInside - xminInside;
        boolean smallKernel = kRadius < 2;
        float[] cache = new float[cacheWidth*kSize]; //a stripe of the image with height=2*kRadius+1
        for (int y=roi.y-kRadius, iCache=0; y<roi.y+kRadius; y++)
            for (int x=xmin; x<xmax; x++, iCache++)  // fill the cache for filtering the first line
                cache[iCache] = pixels[(x<0 ? 0 : x>=width ? width-1 : x) + width*(y<0 ? 0 : y>=height ? height-1 : y)];
        int nextLineInCache = 2*kRadius;            // where the next line should be written to
        float median = cache[0];                    // just any value as a first guess
        Thread thread = Thread.currentThread();     // needed to check for interrupted state
        long lastTime = System.currentTimeMillis();
        for (int y=roi.y; y<roi.y+roi.height; y++) {
            long time = System.currentTimeMillis();
            if (time-lastTime > 100) {
                lastTime = time;
                if (thread.isInterrupted()) return;
                showProgress(y/(double)(roi.height));
                if (imp!= null && IJ.escapePressed()) {
                    ip.reset();
                    ImageProcessor originalIp = imp.getProcessor();
                    if (originalIp.getNChannels() > 1)
                        originalIp.reset();
                    return;
                }
            }
            int ynext = y+kRadius;                  // C O P Y   N E W   L I N E  into cache
            if (ynext >= height) ynext = height-1;
            float leftpxl = pixels[width*ynext];    //edge pixels of the line replace out-of-image pixels
            float rightpxl = pixels[width-1+width*ynext];
            int iCache = cacheWidth*nextLineInCache;//where in the cach we have to copy to
            for (int x=xmin; x<0; x++, iCache++)
                cache[iCache] = leftpxl;
            System.arraycopy(pixels, xminInside+width*ynext, cache, iCache, widthInside);
            iCache += widthInside;
            for (int x=width; x<xmax; x++, iCache++)
                cache[iCache] = rightpxl;
            nextLineInCache = (nextLineInCache + 1) % kSize;
            float max = 0f;                         // F I L T E R   the line
            boolean fullCalculation = true;
            for (int x=roi.x, p=x+y*width, xCache0=kRadius;  x<xEnd; x++, p++, xCache0++) {
                if (fullCalculation) {
                    fullCalculation = smallKernel;  //for small kernel, always use the full area, not incremental algorithm
                    if (minOrMaxOrOutliers)
                        max = getAreaMax(cache, cacheWidth, xCache0, lineRadius, kSize, 0, -Float.MAX_VALUE, sign);
                    if (minOrMax) pixels[p] = max*sign;
                    else if (sumFilter)
                        getAreaSums(cache, cacheWidth, xCache0, lineRadius, kSize, sums);
                } else {
                    if (minOrMaxOrOutliers) {
                        float newPointsMax = getSideMax(cache, cacheWidth, xCache0, lineRadius, kSize, true, sign);
                        if (newPointsMax >= max) { //compare with previous maximum 'max'
                            max = newPointsMax;
                        } else {
                            float removedPointsMax = getSideMax(cache, cacheWidth, xCache0, lineRadius, kSize, false, sign);
                            if (removedPointsMax >= max)
                                max = getAreaMax(cache, cacheWidth, xCache0, lineRadius, kSize, 1, newPointsMax, sign);
                        }
                        if (minOrMax) pixels[p] = max*sign;
                    } else if (sumFilter)
                        addSideSums(cache, cacheWidth, xCache0, lineRadius, kSize, sums);
                }
                if (medianFilter) {
                    if (filterType==MEDIAN || pixels[p]*sign+threshold <max) {
                        median = getMedian(cache, cacheWidth, xCache0, lineRadius, kSize, medianBuf1, medianBuf2, median);
                        if (filterType==MEDIAN || pixels[p]*sign+threshold < median*sign)
                        pixels[p] = median;
                    }
                } else if (sumFilter) {
                    if (filterType == MEAN)
                        pixels[p] = (float)(sums[0]/kNPoints);
                    else    // Variance: sum of squares - square of sums
                        pixels[p] = (float)((sums[1] - sums[0]*sums[0]/kNPoints)/kNPoints);
                }
            } // for x
            int newLineRadius0 = lineRadius[kSize-1];   //shift kernel lineRadii one line
            System.arraycopy(lineRadius, 0, lineRadius, 1, kSize-1);
            lineRadius[0] = newLineRadius0;
        } // for y
    }

    /** Filters an image
     *  @param ip      The ImageProcessor that should be filtered (all 4 types supported)
     *  @param radius  Determines the kernel size, see Process>Filters>Show Circular Masks.
     *                 Must not be negative. No checking is done for large values that would
     *                 lead to excessive computing times.
     *  @param rankType May be MEAN, MIN, MAX, VARIANCE, or MEDIAN.
     */
    public void rank(ImageProcessor ip, double radius, int rankType) {
        FloatProcessor fp = null;
        for (int i=0; i<ip.getNChannels(); i++) {
            makeKernel(radius);
            fp = ip.toFloat(i, fp);
            doFiltering(fp, kRadius, lineRadius, rankType, BRIGHT_OUTLIERS, 50f);
            ip.setPixels(i, fp);
        }
    }

    /** Get max (or -min if sign=-1) within the kernel area.
     *  @param xCache0 points to cache element equivalent to x
     *  @param ignoreRight should be 0 for analyzing all data or 1 for leaving out the row at the right
     *  @param max should be -Float.MAX_VALUE or the smallest value the maximum can be */
    private float getAreaMax(float[] cache, int cacheWidth, int xCache0, int[] lineRadius, int kSize, int ignoreRight, float max, float sign) {
        for (int y=0; y<kSize; y++) {   // y within the cache stripe
            for (int x=xCache0-lineRadius[y], iCache=y*cacheWidth+x; x<=xCache0+lineRadius[y]-ignoreRight; x++, iCache++) {
                float v = cache[iCache]*sign;
                if (max < v) max = v;
            }
        }
        return max;
    }

    /** Get max (or -min if sign=-1) at the right border inside or left border outside the kernel area.
     *  cache0 points to cache element equivalent to x */
    private float getSideMax(float[] cache, int cacheWidth, int xCache0, int[] lineRadius, int kSize, boolean isRight, float sign) {
        float max=-Float.MAX_VALUE;
        for (int y=0; y<kSize; y++) {   // y within the cache stripe
            int x = isRight ? xCache0+lineRadius[y] : xCache0-lineRadius[y]-1;
            int iCache = y*cacheWidth + x;
            float v = cache[iCache]*sign;
            if (max < v) max = v;
        }
        return max;
    }

    /** Get sum of values and values squared within the kernel area.
     *  xCache0 points to cache element equivalent to current x coordinate.
     *  Output is written to array sums[0] = sum; sums[1] = sum of squares */
    private void getAreaSums(float[] cache, int cacheWidth, int xCache0, int[] lineRadius, int kSize, double[] sums) {
        double sum=0, sum2=0;
        for (int y=0; y<kSize; y++) {   // y within the cache stripe
            for (int x=xCache0-lineRadius[y], iCache=y*cacheWidth+x; x<=xCache0+lineRadius[y]; x++, iCache++) {
                float v = cache[iCache];
                sum += v;
                sum2 += v*v;
            }
        }
        sums[0] = sum;
        sums[1] = sum2;
        return;
    }

    /** Add all values and values squared at the right border inside minus at the left border outside the kernal area.
     *  Output is added or subtracted to/from array sums[0] += sum; sums[1] += sum of squares  when at 
     *  the right border, minus when at the left border */
    private void addSideSums(float[] cache, int cacheWidth, int xCache0, int[] lineRadius, int kSize, double[] sums) {
        double sum=0, sum2=0;
        for (int y=0; y<kSize; y++) {   // y within the cache stripe
            int iCache0 = y*cacheWidth + xCache0;
            float v = cache[iCache0 + lineRadius[y]];
            sum += v;
            sum2 += v*v;
            v = cache[iCache0 - lineRadius[y] - 1];
            sum -= v;
            sum2 -= v*v;
        }
        sums[0] += sum;
        sums[1] += sum2;
        return;
    }

    /** Get median of values and values squared within area. Kernel size kNPoints should be odd. */
    private float getMedian(float[] cache, int cacheWidth, int xCache0, int[] lineRadius, int kSize, float[] aboveBuf, float[]belowBuf, float guess) {
        int half = kNPoints/2;
        int nAbove = 0, nBelow = 0;
        for (int y=0; y<kSize; y++) {   // y within the cache stripe
            for (int x=xCache0-lineRadius[y], iCache=y*cacheWidth+x; x<=xCache0+lineRadius[y]; x++, iCache++) {
                float v = cache[iCache];
                if (v > guess) {
                    aboveBuf[nAbove] = v;
                    nAbove++;
                }
                else if (v < guess) {
                    belowBuf[nBelow] = v;
                    nBelow++;
                }
            }
        }
        if (nAbove>half)
            return findNthLowestNumber(aboveBuf, nAbove, nAbove-half-1);
        else if (nBelow>half)
            return findNthLowestNumber(belowBuf, nBelow, half);
        else
            return guess;
    }

    /** Find the n-th lowest number in part of an array
     *  @param buf The input array. Only values 0 ... bufLength are read. <code>buf</code> will be modified.
     *  @param bufLength Number of values in <code>buf</code> that should be read
     *  @param n which value should be found; n=0 for the lowest, n=bufLength-1 for the highest
     *  @return the value */
    public static float findNthLowestNumber(float[] buf, int bufLength, int n) {
        // Modified algorithm according to http://www.geocities.com/zabrodskyvlada/3alg.html
        // Contributed by Heinz Klar
        int i,j;
        int l=0;
        int m=bufLength-1;
        float med=buf[n];
        float dum ;

        while (l<m) {
            i=l ;
            j=m ;
            do {
                while (buf[i]<med) i++ ;
                while (med<buf[j]) j-- ;
                dum=buf[j];
                buf[j]=buf[i];
                buf[i]=dum;
                i++ ; j-- ;
            } while ((j>=n) && (i<=n)) ;
            if (j<n) l=i ;
            if (n<i) m=j ;
            med=buf[n] ;
        }
    return med ;
    }

    /** Create a circular kernel of a given radius. Radius = 0.5 includes the 4 neighbors of the
     *  pixel in the center, radius = 1 corresponds to a 3x3 kernel size.
     *  The output is written to class variables kNPoints (number of points inside the kernel) and
     *  lineRadius, which is an array giving the radius of each line. Line length is 2*lineRadius+1.
     */
    public synchronized void makeKernel(double radius) {
        if (radius>=1.5 && radius<1.75) //this code creates the same sizes as the previous RankFilters
            radius = 1.75;
        else if (radius>=2.5 && radius<2.85)
            radius = 2.85;
        int r2 = (int) (radius*radius) + 1;
        kRadius = (int)(Math.sqrt(r2+1e-10));
        lineRadius = new int[2*kRadius+1];
        lineRadius[kRadius] = kRadius;
        kNPoints = 2*kRadius + 1;
        for (int y=1; y<=kRadius; y++) {
            int dx = (int)(Math.sqrt(r2-y*y+1e-10));
            lineRadius[kRadius+y] = dx;
            lineRadius[kRadius-y] = dx;
            kNPoints += 4*dx + 2;
        }
    }

    void showMasks() {
        int w=150, h=150;
        IjxImageStack stack = IJ.getFactory().newImageStack(w, h);
        //for (double r=0.1; r<3; r+=0.01) {
        for (double r=0.5; r<50; r+=0.5) {
            ImageProcessor ip = new FloatProcessor(w,h,new int[w*h]);
            float[] pixels = (float[])ip.getPixels();
            makeKernel(r);
            for (int i = 0, y = h/2-kRadius; i < (2*kRadius+1); i++, y++)
                for (int x = w/2-lineRadius[i], p = x+y*w; x <= w/2+lineRadius[i]; x++, p++)
                    pixels[p] = 1f;
            stack.addSlice("radius="+r+", size="+(2*kRadius+1), ip);
        }
        IJ.getFactory().newImagePlus("Masks", stack).show();
    }

    /** This method is called by ImageJ to set the number of calls to run(ip)
     *  corresponding to 100% of the progress bar */
    public void setNPasses (int nPasses) {
        this.nPasses = nPasses;
        pass = 0;
    }

    private void showProgress(double percent) {
        percent = (double)(pass-1)/nPasses + percent/nPasses;
        IJ.showProgress(percent);
    }

}
