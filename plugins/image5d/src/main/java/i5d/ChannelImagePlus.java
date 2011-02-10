package i5d;

import java.awt.Rectangle;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.PlotWindow;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.process.ImageProcessor;

public class ChannelImagePlus extends ImagePlus {

    public ChannelImagePlus(String title, ImageProcessor ip) {
        super(title, ip);
    }
    


    /* Empty method. Prevents messing up the Image5DWindow's title. 
     * A ChannelImagePlus needs no title. */
    public void setTitle(String title) {
        return;
    }


    public String getValueAsStringI5d(int x, int y) {
        if (win!=null && win instanceof PlotWindow)
            return "";
        Calibration cal = getCalibration();
        int v = getProcessor().getPixel(x, y);
        int type = getType();
        switch (type) {
            case GRAY8: case GRAY16:
                double cValue = cal.getCValue(v);
                if (cValue==v)
                    return(""+v);
                else
                    return(""+IJ.d2s(cValue) + " ("+v+")");
            case GRAY32:
                return("" + Float.intBitsToFloat(v));
            default: return("");
        }
    }
    
    /** Assigns the specified ROI to this image and displays it without saving it as previousRoi
     * as it is done by <code>setRoi()</code>.  */
    public void putRoi(Roi newRoi) {

        Rectangle bounds = new Rectangle();
        
        if (newRoi!=null) {        
            // Roi with width and height = 0 is same as null Roi.
            bounds = newRoi.getBounds();
            if (bounds.width==0 && bounds.height==0 && newRoi.getType()!=Roi.POINT) {
                newRoi = null;
            }
        }
        
        roi = newRoi;
        
        if (roi != null) {
            if (ip!=null) {
                ip.setMask(null);
                if (roi.isArea())
                    ip.setRoi(bounds);
                else
                    ip.resetRoi();
            }
            roi.setImage(this);
        }
        
        draw();
    }
    
    
}
