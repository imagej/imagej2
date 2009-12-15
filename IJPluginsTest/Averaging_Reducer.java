import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.filter.*;

public class Averaging_Reducer  implements PlugInFilter {
    ImagePlus imp;
    static int xshrink=5, yshrink=5;
    double product;
    int[] pixel = new int[3];
    int[] sum = new int[3];
    int samples;

    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;
        if (IJ.versionLessThan("1.28e"))
            return DONE;
        else
            return DOES_ALL+NO_UNDO;
    }

    public void run(ImageProcessor ip) {
        if (showDialog())
            shrink(ip);
    }

    public void shrink(ImageProcessor ip) {
        if (imp.getBitDepth()==32)
                    {shrinkFloat(ip); return;}
        samples = ip instanceof ColorProcessor?3:1;
        int w = ip.getWidth()/xshrink;
        int h = ip.getHeight()/yshrink;
         ImageProcessor ip2 = ip.createProcessor(w, h);
        for (int y=0; y<h; y++)
            for (int x=0; x<w; x++) 
                ip2.putPixel(x, y, getAverage(ip, x, y));
         ip2.resetMinAndMax();
        new ImagePlus("Reduced "+imp.getShortTitle(), ip2).show();
    }

    int[] getAverage(ImageProcessor ip, int x, int y) {
         for (int i=0; i<samples; i++)
            sum[i] = 0;       
         for (int y2=0; y2<yshrink; y2++) {
            for (int x2=0;  x2<xshrink; x2++) {
                pixel = ip.getPixel(x*xshrink+x2, y*yshrink+y2, pixel); 
                for (int i=0; i<samples; i++)
                     sum[i] += pixel[i];
             }
        }
        for (int i=0; i<samples; i++)
            sum[i] = (int)(sum[i]/product+0.5);
       return sum;
    }

    boolean showDialog() {
        GenericDialog gd = new GenericDialog("Image Shrink");
        gd.addNumericField("X Shrink Factor:", xshrink, 0);
        gd.addNumericField("Y Shrink Factor:", yshrink, 0);
        gd.showDialog();
        if (gd.wasCanceled()) 
            return false;
        xshrink = (int) gd.getNextNumber();
        yshrink = (int) gd.getNextNumber();
        product = xshrink*yshrink;
        return true;
    }

   void shrinkFloat(ImageProcessor ip) {
        int w = ip.getWidth()/xshrink;
        int h = ip.getHeight()/yshrink;
        ImageProcessor ip2 = ip.createProcessor(w, h);
        for (int y=0; y<h; y++)
            for (int x=0; x<w; x++) 
                ip2.putPixelValue(x, y, getFloatAverage(ip, x, y));
        ip2.resetMinAndMax();
        new ImagePlus("Reduced "+imp.getShortTitle(), ip2).show();
    }

    float getFloatAverage(ImageProcessor ip, int x, int y) {
        double sum = 0.0;
        for (int y2=0; y2<yshrink; y2++)
            for (int x2=0;  x2<xshrink; x2++)
                sum += ip.getPixelValue(x*xshrink+x2, y*yshrink+y2); 
        return (float)(sum/product);
    }

}
