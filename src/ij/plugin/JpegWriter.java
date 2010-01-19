package ij.plugin;
import ij.*;
import ij.process.*;
import ij.io.FileSaver;
import java.awt.image.*;
import java.awt.*;
import java.io.*;
import java.util.Iterator;
import javax.imageio.*;
import javax.imageio.stream.*;

/** The File/Save As/Jpeg command (FileSaver.saveAsJpeg() method) 
      uses this plugin to save images in JPEG format. */
public class JpegWriter implements PlugIn {
	public static final int DEFAULT_QUALITY = 75;

	public void run(String arg) {
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp==null) return;
		imp.startTiming();
		saveAsJpeg(imp,arg,FileSaver.getJpegQuality());
		IJ.showTime(imp, imp.getStartTime(), "JpegWriter: ");
	}

	/** Thread-safe method. */
	public static String save(ImagePlus imp, String path, int quality) {
		imp.startTiming();
		String error = (new JpegWriter()).saveAsJpeg(imp, path, quality);
		IJ.showTime(imp, imp.getStartTime(), "JpegWriter: ");
		return error;
	}

	String saveAsJpeg(ImagePlus imp, String path, int quality) {
		int width = imp.getWidth();
		int height = imp.getHeight();
		int biType = BufferedImage.TYPE_INT_RGB;
		if (imp.getProcessor().isDefaultLut())
			biType = BufferedImage.TYPE_BYTE_GRAY;
		BufferedImage bi = new BufferedImage(width, height, biType);
		String error = null;
		try {
			Graphics g = bi.createGraphics();
			g.drawImage(imp.getImage(), 0, 0, null);
			g.dispose();            
			Iterator iter = ImageIO.getImageWritersByFormatName("jpeg");
			ImageWriter writer = (ImageWriter)iter.next();
			ImageOutputStream ios = ImageIO.createImageOutputStream(new File(path));
			writer.setOutput(ios);
			ImageWriteParam param = writer.getDefaultWriteParam();
			param.setCompressionMode(param.MODE_EXPLICIT);
			param.setCompressionQuality(quality/100f);
			if (quality == 100)
				param.setSourceSubsampling(1, 1, 0, 0);
			IIOImage iioImage = new IIOImage(bi, null, null);
			writer.write(null, iioImage, param);
			ios.close();
			writer.dispose();
		} catch (Exception e) {
			error = ""+e;
			IJ.error("Jpeg Writer", ""+error);
		}
		return error;
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
