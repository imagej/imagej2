package ij.gui;
import ij.*;
import ij.process.*;

import java.awt.*;

/** This class implements ImageJ's wand (tracing) tool.
  * The wand selects pixels of equal or similar value or thresholded pixels
  * forming a contiguous area.
  * The wand creates selections that have only one boundary line (inner holes
  * are not excluded from the selection). There may be holes at the boundary,
  * however, if the boundary line touches the same vertex twice (both in
  * 4-connected and 8-connected mode).
  *
  * Version 2009-06-01  (code refurbished; tolerance, 4- & 8-connected options added)
  */
public class Wand {
    /** Wand operation type: trace outline of 4-connected pixels */
    public final static int FOUR_CONNECTED = 4;
    /** Wand operation type: trace outline of 8-connected pixels */
    public final static int EIGHT_CONNECTED = 8;
    /** Wand operation type similar to  that of ImageJ 1.42p and before; for backwards
      * compatibility.
      * In this mode, no checking is done whether the foreground or the background
      * gets selected; four- or 8-connected behaviour depends on foreground/background
      * and (if no selection) on whether the initial pixel is on a 1-pixel wide line. */
    public final static int LEGACY_MODE = 1;    
    /** The number of points in the generated outline. */
    public int npoints;
    private int maxPoints = 1000; // will be increased if necessary
    /** The x-coordinates of the points in the outline.
        A vertical boundary at x separates the pixels at x-1 and x. */
    public int[] xpoints = new int[maxPoints];
    /** The y-coordinates of the points in the outline.
        A horizontal boundary at y separates the pixels at y-1 and y. */
    public int[] ypoints = new int[maxPoints];

    private final static int THRESHOLDED_MODE = 256; //work on threshold
    private ImageProcessor ip;
    private boolean isColorProc;
    private boolean isFloatingType;
    private int width, height;
    private float lowerThreshold, upperThreshold;
    private int xmin;                   //of selection created
    private boolean exactPixelValue;    //For color, match RGB, not gray value
    private static boolean allPoints;


    /** Constructs a Wand object from an ImageProcessor. */
    public Wand(ImageProcessor ip) {
        this.ip = ip;
        this.isColorProc = ip instanceof ColorProcessor;
        this.isFloatingType = ip.isFloatingType();
        width = ip.getWidth();
        height = ip.getHeight();
    }



    /** Traces an object defined by lower and upper threshold values.
      * 'mode' can be FOUR_CONNECTED or EIGHT_CONNECTED.
      * ('LEGACY_MODE' is also supported and may result in selection of
      * interior holes instead of the thresholded area if one clicks left
      * of an interior hole).
      * The start coordinates must be inside the area or left of it.
      * When successful, npoints>0 and the boundary points can be accessed
      * in the public xpoints and ypoints fields. */
    public void autoOutline(int startX, int startY, double lower, double upper, int mode) {
        lowerThreshold = (float)lower;
        upperThreshold = (float)upper;
        autoOutline(startX, startY, 0.0, mode|THRESHOLDED_MODE);
    }

    /** Traces an object defined by lower and upper threshold values or an
      * interior hole; whatever is found first ('legacy mode').
      * For compatibility with previous versions of ImageJ.
      * The start coordinates must be inside the area or left of it.
      * When successful, npoints>0 and the boundary points can be accessed
      * in the public xpoints and ypoints fields. */
    public void autoOutline(int startX, int startY, double lower, double upper) {
        autoOutline(startX, startY, lower, upper, THRESHOLDED_MODE|LEGACY_MODE);
    }

    /** This is a variation of legacy autoOutline that uses int threshold arguments. */
    public void autoOutline(int startX, int startY, int lower, int upper) {
        autoOutline(startX, startY, (double)lower, (double)upper, THRESHOLDED_MODE|LEGACY_MODE);
    }

    /** Traces the boundary of an area of uniform color, where
      * 'startX' and 'startY' are somewhere inside the area.
      * When successful, npoints>0 and the boundary points can be accessed
      * in the public xpoints and ypoints fields.
      * For compatibility with previous versions of ImageJ only; otherwise
      * use the reliable method specifying 4-connected or 8-connected mode
      * and the tolerance. */
    public void autoOutline(int startX, int startY) {
        autoOutline(startX, startY, 0.0, LEGACY_MODE);
    }

    /** Traces the boundary of the area with pixel values within
      * 'tolerance' of the value of the pixel at the starting location.
      * 'tolerance' is in uncalibrated units.
      * 'mode' can be FOUR_CONNECTED or EIGHT_CONNECTED.
      * Mode LEGACY_MODE is for compatibility with previous versions of ImageJ;
      * ignored if tolerance > 0.
      * Mode bit THRESHOLDED_MODE for internal use only; it is set by autoOutline
      * with 'upper' and 'lower' arguments.
      * When successful, npoints>0 and the boundary points can be accessed
      * in the public xpoints and ypoints fields. */
    public void autoOutline(int startX, int startY, double tolerance, int mode) {
        if (startX<0 || startX>=width || startY<0 || startY>=height) return;
        if (this.isFloatingType && Float.isNaN(getPixel(startX, startY))) return;
        exactPixelValue = tolerance==0;
        boolean thresholdMode = (mode & THRESHOLDED_MODE) != 0;
        boolean legacyMode = (mode & LEGACY_MODE) != 0 && tolerance == 0;
        if (!thresholdMode) {
            double startValue = getPixel(startX, startY);
            lowerThreshold = (float)(startValue - tolerance);
            upperThreshold = (float)(startValue + tolerance);
        }
        int x = startX;
        int y = startY;
        int seedX;                      // the first inside pixel
        if (inside(x,y)) {              // find a border when coming from inside
            seedX = x;                // (seedX, startY) is an inside pixel
            do {x++;} while (inside(x,y));
        } else {                        // find a border when coming from outside (thresholded only)
            do {
                x++;
                if (x>=width) return;   // no border found
            } while (!inside(x,y));
            seedX = x;
        }
        boolean fourConnected;
        if (legacyMode)
            fourConnected = !thresholdMode && !(isLine(x, y));
        else
            fourConnected = (mode & FOUR_CONNECTED) != 0;
        //now, we have a border between (x-1, y) and (x,y)
        boolean first = true;
        while (true) {                  // loop until we have not traced an inner hole
            boolean insideSelected = traceEdge(x, y, fourConnected);
            if (legacyMode) return;     // in legacy mode, don't care what we have got
            if (insideSelected) {       // not an inner hole
                if (first) return;      // started at seed, so we got it (sucessful)
                if (xmin<=seedX) {      // possibly the correct particle
                    Polygon poly = new Polygon(xpoints, ypoints, npoints);
                    if (poly.contains(seedX, startY))
                        return;         // successful, particle contains seed
                }
            }
            first = false;
            // we have traced an inner hole or the wrong particle
            if (!inside(x,y)) do {
                x++;                    // traverse the hole
                if (x>width) throw new RuntimeException("Wand Malfunction"); //should never happen
            } while (!inside(x,y));
            do {x++;} while (inside(x,y)); //retry here; maybe no inner hole any more
        }
    }


    /* Trace the outline, starting at a point (startX, startY). 
     * Pixel (startX-1, startY) must be outside, (startX, startY) must be inside,
     * or reverse. Otherwise an endless loop will occur (and eat up all memory).
     * Traces 8-connected inside pixels unless fourConnected is true.
     * Returns whether the selection created encloses an 'inside' area
     * and not an inner hole.
     */
    private boolean traceEdge(int startX, int startY, boolean fourConnected) {
        // Let us name the crossings between 4 pixels vertices, then the
        // vertex (x,y) marked with '+', is between pixels (x-1, y-1) and (x,y):
        //
        //    pixel    x-1    x
        //      y-1        |
        //             ----+----
        //       y         |
        //
        // The four principal directions are numbered such that the direction
        // number * 90 degrees gives the angle in the mathematical sense; and
        // the directions to the adjacent pixels (for inside(x,y,direction) are
        // at (number * 90 - 45) degrees:
        //      walking                     pixel
        //   directions:   1           directions:     2 | 1
        //              2  +  0                      ----+----
        //                 3                           3 | 0
        //
        // Directions, like angles, are cyclic; direction -1 = direction 3, etc.
        //
        // The algorithm: We walk along the border, from one vertex to the next,
        // with the outside pixels always being at the left-hand side.
        // For 8-connected tracing, we always trying to turn left as much as
        // possible, to encompass an area as large as possible.
        // Thus, when walking in direction 1 (up, -y), we start looking
        // at the pixel in direction 2; if it is inside, we proceed in this
        // direction (left); otherwise we try with direction 1 (up); if pixel 1
        // is not inside, we must proceed in direction 0 (right).
        //
        //                     2 | 1                 (i=inside, o=outside)
        //      direction 2 < ---+---- > direction 0
        //                     o | i
        //                       ^ direction 1 = up = starting direction
        //
        // For 4-connected pixels, we try to go right as much as possible:
        // First try with pixel 1; if it is outside we go in direction 0 (right).
        // Otherwise, we examine pixel 2; if it is outside, we go in
        // direction 1 (up); otherwise in direction 2 (left).
        //
        // When moving a closed loop, 'direction' gets incremented or decremented
        // by a total of 360 degrees (i.e., 4) for counterclockwise and clockwise
        // loops respectively. As the inside pixels are at the right side, we have
        // got an outline of inner pixels after a cw loop (direction decremented
        // by 4).
        //
        npoints = 0;
        xmin = width;
        final int startDirection;
        if (inside(startX,startY))      // inside at left, outside right
            startDirection = 1;         // starting in direction 1 = up
        else {
            startDirection = 3;         // starting in direction 3 = down
            startY++;                   // continue after the boundary that has direction 3
        }
        int x = startX;
        int y = startY;
        int direction = startDirection;
        do {
            int newDirection;
            if (fourConnected) {
                newDirection = direction;
                do {
                    if (!inside(x, y, newDirection)) break;
                    newDirection++;
                } while (newDirection < direction+2);
                newDirection--;
            } else { // 8-connected
                newDirection = direction + 1;
                do {
                if (inside(x, y, newDirection)) break;
                    newDirection--;
                } while (newDirection >= direction);
            }
            if (allPoints || newDirection!=direction)
                addPoint(x,y);          // a corner point of the outline polygon: add to list
            switch (newDirection & 3) { // '& 3' is remainder modulo 4
                case 0: x++; break;
                case 1: y--; break;
                case 2: x--; break;
                case 3: y++; break;
            }
            direction = newDirection;
        } while (x!=startX || y!=startY || (direction&3)!=startDirection);
        if (allPoints || xpoints[0]!=x)            // if the start point = end point is a corner: add to list
            addPoint(x, y);
        return (direction <= 0);        // if we have done a clockwise loop, inside pixels are enclosed
    }

    // add a point x,y to the outline polygon
    private void addPoint (int x, int y) {
        if (npoints==maxPoints) {
            int[] xtemp = new int[maxPoints*2];
            int[] ytemp = new int[maxPoints*2];
            System.arraycopy(xpoints, 0, xtemp, 0, maxPoints);
            System.arraycopy(ypoints, 0, ytemp, 0, maxPoints);
            xpoints = xtemp;
            ypoints = ytemp;
            maxPoints *= 2;
        }
        xpoints[npoints] = x;
        ypoints[npoints] = y;
        npoints++;
        if (xmin > x) xmin = x;
    }

    // check pixel at (x,y), whether it is inside traced area
    private boolean inside(int x, int y) {
        if (x<0 || x>=width || y<0 || y>=height)
            return false;
        float value = getPixel(x, y);
        return value>=lowerThreshold && value<=upperThreshold;
    }

    // check pixel in a given direction from vertex (x,y)
    private boolean inside(int x, int y, int direction) {
        switch(direction & 3) {         // '& 3' is remainder modulo 4
            case 0: return inside(x, y);
            case 1: return inside(x, y-1);
            case 2: return inside(x-1, y-1);
            case 3: return inside(x-1, y);
        }
        return false; //will never occur, needed for the compiler
    }

    // get a pixel value; returns Float.NaN if outside the field.
    private float getPixel(int x, int y) {
        if (x<0 || x>=width || y<0 || y>=height)
            return Float.NaN;
        else if (this.isColorProc)
        {
            if (exactPixelValue)   //RGB for exact match
                return ip.get(x, y) & 0xffffff; //don't care for upper byte
            else                      //gray value of RGB
                return ip.getPixelValue(x,y);
        }
        else
        	return ip.getf(x, y);
    }

    /* Are we tracing a one pixel wide line? Makes Legacy mode 8-connected instead of 4-connected */
    private boolean isLine(int xs, int ys) {
        int r = 5;
        int xmin=xs;
        int xmax=xs+2*r;
        if (xmax>=width) xmax=width-1;
        int ymin=ys-r;
        if (ymin<0) ymin=0;
        int ymax=ys+r;
        if (ymax>=height) ymax=height-1;
        int area = 0;
        int insideCount = 0;
        for (int x=xmin; (x<=xmax); x++)
            for (int y=ymin; y<=ymax; y++) {
                area++;
                if (inside(x,y))
                    insideCount++;
            }
        if (IJ.debugMode)
            IJ.log((((double)insideCount)/area<0.25?"line ":"blob ")+insideCount+" "+area+" "+IJ.d2s(((double)insideCount)/area));
        return ((double)insideCount)/area<0.25;
    }

    public static void setAllPoints(boolean b) {
        allPoints = b;
    }

    public static boolean allPoints() {
        return allPoints;
    }

}
