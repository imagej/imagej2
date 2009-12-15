import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.filter.*;

/** Replaces the odd lines in an image with the average of the two adjacent lines. */
public class Deinterlace_ implements PlugInFilter {

     public int setup(String arg, ImagePlus imp) {
         if (IJ.versionLessThan("1.28e"))
            return DONE;
         else
            return DOES_ALL-DOES_32+DOES_STACKS;
     }

     public void run(ImageProcessor ip) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        int bottom = (height&1)==1?height:height-1;
        int[]  p1=new int[3],p2=new int[3],avg=new int[3];
        int samples = ip instanceof ColorProcessor?3:1;
        for (int y=1; y<bottom; y+=2) {
             for (int x=0; x<width; x++) {
                 p1 = ip.getPixel(x, y-1, p1);
                 p2 = ip.getPixel(x, y+1, p2);
                 for (int i=0; i<samples; i++) {
                      avg[i] = ((p1[i]+p2[i])/2);
                      ip.putPixel(x, y, avg);
                }
            }
        }
        if (bottom!=height) {
            int[] aRow = new int[width];
            ip.getRow(0, height-2, aRow, width);
            ip.putRow(0, height-1, aRow, width);
        }
     }

   /* This version that doesn't use preallocted arrays runs about 25% slower
    public void run(ImageProcessor ip) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        int bottom = (height&1)==1?height:height-1;
        int[]  p1, p2, avg=new int[3];
        for (int y=1; y<bottom; y+=2) {
             for (int x=0; x<width; x++) {
                 p1 = ip.getPixel(x, y-1, null);
                 p2 = ip.z(x, y+1, null);
                 for (int i=0; i<p1.length; i++) {
                      avg[i] = ((p1[i]+p2[i])/2);
                      ip.putPixel(x, y, avg);
                }
            }
        }
        if (bottom!=height) {
            int[] aRow = new int[width];
            ip.getRow(0, height-2, aRow, width);
            ip.putRow(0, height-1, aRow, width);
        }
     }
    */

}
