package ij.plugin;
import ijx.IjxImagePlus;
import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.io.*;
import ij.measure.Calibration;
import java.util.Locale;	
import com.sun.image.codec.jpeg.*;
import java.awt.image.*;
import java.awt.*;
import java.io.*;

/** The File->Save As->Jpeg command (FileSaver.saveAsJpeg()) uses
      this plugin to save images in JPEG format. The path where 
      the image is to be saved is passed to the run method. ImageJ
      save JPEGs using ImageIO if this class is missing. */
public class JpegWriter implements PlugIn {
	public static final int DEFAULT_QUALITY = 75;

    public void run(String arg) {
        IjxImagePlus imp = WindowManager.getCurrentImage();
        if (imp==null) return;
        imp.startTiming();
        saveAsJpeg(imp,arg);
        IJ.showTime(imp, imp.getStartTime(), "JpegWriter: ");
    } 

    void saveAsJpeg(IjxImagePlus imp, String path) {
        //IJ.log("saveAsJpeg: "+path);
        int width = imp.getWidth();
        int height = imp.getHeight();
        int quality = FileSaver.getJpegQuality();
        BufferedImage   bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        try {
            FileOutputStream  f  = new FileOutputStream(path);                
            Graphics g = bi.createGraphics();
            g.drawImage(imp.getImage(), 0, 0, null);
            g.dispose();            
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(f);
            JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(bi);
			if (quality==100) { // no color subsampling, better quality
            	param.setQuality(1f, true);
				param.setHorizontalSubsampling(1, 1);
				param.setHorizontalSubsampling(2, 1);
				param.setVerticalSubsampling(1, 1);
				param.setVerticalSubsampling(2, 1);
			} else {
				float q = quality==99?1f:(float)(quality/100.0);
            	param.setQuality(q, true);
            }
			Calibration cal = imp.getCalibration();
			String unit = cal.getUnit().toLowerCase(Locale.US);
			if (cal.getUnit().equals("inch")||cal.getUnit().equals("in")) {
					param.setDensityUnit(JPEGEncodeParam.DENSITY_UNIT_DOTS_INCH);
					param.setXDensity((int)Math.round(1.0/cal.pixelWidth));
					param.setYDensity((int)Math.round(1.0/cal.pixelHeight));
			}
            encoder.encode(bi, param);
            f.close();
        }
        catch (Exception e) {
           IJ.error("Jpeg Writer", ""+e);
        }
    }

	/** Obsolete, replaced by FileSaver.setJpegQuality(). */
    public static void setQuality(int jpegQuality) {
    	FileSaver.setJpegQuality(jpegQuality);
    }

	/** Obsolete, replaced by FileSaver.getJpegQuality(). */
    public static int getQuality() {
        return FileSaver.getJpegQuality();
    }

}
