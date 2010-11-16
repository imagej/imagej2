package ij.io;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import ij.gui.*;
import ij.process.*;
import ij.measure.*;
import ij.*;
import ij.plugin.frame.ThresholdAdjuster;
import ijx.IjxImagePlus;
import ijx.IjxImageStack;

/**
 * Opens or reverts an image specified by a FileInfo object. Images can
 * be loaded from either a file (directory+fileName) or a URL (url+fileName).
 * Here is an example:	
 * <pre>
 *   public class FileInfo_Test implements PlugIn {
 *     public void run(String arg) {
 *       FileInfo fi = new FileInfo();
 *       fi.width = 256;
 *       fi.height = 254;
 *       fi.offset = 768;
 *       fi.fileName = "blobs.tif";
 *       fi.directory = "/Users/wayne/Desktop/";
 *       new FileOpener(fi).open();
 *     }  
 *   }	
 * </pre> 
 */
public class FileOpener {

	private FileInfo fi;
	private int width, height;
	private static boolean showConflictMessage = true;
	private double minValue, maxValue;
	private static boolean silentMode;

	public FileOpener(FileInfo fi) {
		this.fi = fi;
		if (fi!=null) {
			width = fi.width;
			height = fi.height;
		}
		if (IJ.debugMode) IJ.log("FileInfo: "+fi);
	}
	
	/** Opens the image and displays it. */
	public void open() {
		open(true);
	}
	
	/** Opens the image. Displays it if 'show' is
	true. Returns an IjxImagePlus object if successful. */
	public IjxImagePlus open(boolean show) {
		IjxImagePlus imp=null;
		Object pixels;
		ProgressBar pb=null;
	    ImageProcessor ip;
		
		ColorModel cm = createColorModel(fi);
		if (fi.nImages>1)
			{return openStack(cm, show);}
		switch (fi.fileType) {
			case FileInfo.GRAY8:
			case FileInfo.COLOR8:
			case FileInfo.BITMAP:
				pixels = readPixels(fi);
				if (pixels==null) return null;
				ip = new ByteProcessor(width, height, (byte[])pixels, cm);
    			imp = IJ.getFactory().newImagePlus(fi.fileName, ip);
				break;
			case FileInfo.GRAY16_SIGNED:
			case FileInfo.GRAY16_UNSIGNED:
			case FileInfo.GRAY12_UNSIGNED:
				pixels = readPixels(fi);
				if (pixels==null) return null;
	    		ip = new ShortProcessor(width, height, (short[])pixels, cm);
       			imp = IJ.getFactory().newImagePlus(fi.fileName, ip);
				break;
			case FileInfo.GRAY32_INT:
			case FileInfo.GRAY32_UNSIGNED:
			case FileInfo.GRAY32_FLOAT:
			case FileInfo.GRAY24_UNSIGNED:
			case FileInfo.GRAY64_FLOAT:
				pixels = readPixels(fi);
				if (pixels==null) return null;
	    		ip = new FloatProcessor(width, height, (float[])pixels, cm);
       			imp = IJ.getFactory().newImagePlus(fi.fileName, ip);
				break;
			case FileInfo.RGB:
			case FileInfo.BGR:
			case FileInfo.ARGB:
			case FileInfo.ABGR:
			case FileInfo.BARG:
			case FileInfo.RGB_PLANAR:
				pixels = readPixels(fi);
				if (pixels==null) return null;
	    		ip = new ColorProcessor(width, height, (int[])pixels);
        		imp = IJ.getFactory().newImagePlus(fi.fileName, ip);
				break;
			case FileInfo.RGB48:
			case FileInfo.RGB48_PLANAR:
				boolean planar = fi.fileType==FileInfo.RGB48_PLANAR;
				Object[] pixelArray = (Object[])readPixels(fi);
				if (pixelArray==null) return null;
				IjxImageStack stack = IJ.getFactory().newImageStack(width, height);
				stack.addSlice("Red", pixelArray[0]);
				stack.addSlice("Green", pixelArray[1]);
				stack.addSlice("Blue", pixelArray[2]);
        		imp = IJ.getFactory().newImagePlus(fi.fileName, stack);
        		imp.setDimensions(3, 1, 1);
        		if (planar)
        			imp.getProcessor().resetMinAndMax();
				imp.setFileInfo(fi);
				int mode = CompositeImage.COMPOSITE;
				if (fi.description!=null) {
					if (fi.description.indexOf("mode=color")!=-1)
					mode = CompositeImage.COLOR;
					else if (fi.description.indexOf("mode=gray")!=-1)
					mode = CompositeImage.GRAYSCALE;
				}
        		imp = new CompositeImage(imp, mode);
        		if (!planar && fi.displayRanges==null) {
        			for (int c=1; c<=3; c++) {
        				imp.setPosition(c, 1, 1);
        				imp.setDisplayRange(minValue, maxValue);
        			}
       				imp.setPosition(1, 1, 1);
        		}
				break;
		}
		imp.setFileInfo(fi);
		setCalibration(imp);
		if (fi.info!=null)
			imp.setProperty("Info", fi.info);
		if (fi.sliceLabels!=null&&fi.sliceLabels.length==1&&fi.sliceLabels[0]!=null)
			imp.setProperty("Label", fi.sliceLabels[0]);
		if (fi.roi!=null)
			imp.setRoi(RoiDecoder.openFromByteArray(fi.roi));
		if (fi.overlay!=null)
			setOverlay(imp, fi.overlay);
		if (show) imp.show();
		return imp;
	}
	
	void setOverlay(IjxImagePlus imp, byte[][] rois) {
		Overlay overlay = new Overlay();
		for (int i=0; i<rois.length; i++) {
			Roi roi = RoiDecoder.openFromByteArray(rois[i]);
			overlay.add(roi);
		}
		imp.setOverlay(overlay);
	}

	/** Opens a stack of images. */
	IjxImagePlus openStack(ColorModel cm, boolean show) {
		IjxImageStack stack = IJ.getFactory().newImageStack(fi.width, fi.height, cm);
		long skip = fi.getOffset();
		Object pixels;
		try {
			ImageReader reader = new ImageReader(fi);
			InputStream is = createInputStream(fi);
			if (is==null) return null;
			IJ.resetEscape();
			for (int i=1; i<=fi.nImages; i++) {
				if (!silentMode)
					IJ.showStatus("Reading: " + i + "/" + fi.nImages);
				if (IJ.escapePressed()) {
					IJ.beep();
					IJ.showProgress(1.0);
					silentMode = false;
					return null;
				}
				pixels = reader.readPixels(is, skip);
				if (pixels==null) break;
				stack.addSlice(null, pixels);
				skip = fi.gapBetweenImages;
				if (!silentMode)
					IJ.showProgress(i, fi.nImages);
			}
			is.close();
		}
		catch (Exception e) {
			IJ.log("" + e);
		}
		catch(OutOfMemoryError e) {
			IJ.outOfMemory(fi.fileName);
			stack.trim();
		}
		if (!silentMode) IJ.showProgress(1.0);
		if (stack.getSize()==0)
			return null;
		if (fi.sliceLabels!=null && fi.sliceLabels.length<=stack.getSize()) {
			for (int i=0; i<fi.sliceLabels.length; i++)
				stack.setSliceLabel(fi.sliceLabels[i], i+1);
		}
		IjxImagePlus imp = IJ.getFactory().newImagePlus(fi.fileName, stack);
		if (fi.info!=null)
			imp.setProperty("Info", fi.info);
		if (fi.roi!=null)
			imp.setRoi(RoiDecoder.openFromByteArray(fi.roi));
		if (fi.overlay!=null)
			setOverlay(imp, fi.overlay);
		if (show) imp.show();
		imp.setFileInfo(fi);
		setCalibration(imp);
		ImageProcessor ip = imp.getProcessor();
		if (ip.getMin()==ip.getMax())  // find stack min and max if first slice is blank
			setStackDisplayRange(imp);
		if (!silentMode) IJ.showProgress(1.0);
		//silentMode = false;
		return imp;
	}

	void setStackDisplayRange(IjxImagePlus imp) {
		IjxImageStack stack = imp.getStack();
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		int n = stack.getSize();
		for (int i=1; i<=n; i++) {
			if (!silentMode)
				IJ.showStatus("Calculating stack min and max: "+i+"/"+n);
			ImageProcessor ip = stack.getProcessor(i);
			ip.resetMinAndMax();
			if (ip.getMin()<min)
				min = ip.getMin();
			if (ip.getMax()>max)
				max = ip.getMax();
		}
		imp.getProcessor().setMinAndMax(min, max);
		imp.updateAndDraw();
	}
	
	/** Restores original disk or network version of image. */
	public void revertToSaved(IjxImagePlus imp) {
		Image img;
		ImageProcessor ip;
		String path = fi.directory + fi.fileName;
		
		if (fi.fileFormat==fi.GIF_OR_JPG) {
			// restore gif or jpg
			img = Toolkit.getDefaultToolkit().createImage(path);
			imp.setImage(img);
			if (imp.getType()==IjxImagePlus.COLOR_RGB)
				Opener.convertGrayJpegTo8Bits(imp);
	    	return;
		}
		
				
		if (fi.fileFormat==fi.DICOM) {
			// restore DICOM
			IjxImagePlus imp2 = (IjxImagePlus)IJ.runPlugIn("ij.plugin.DICOM", path);
			if (imp2!=null)
				imp.setProcessor(null, imp2.getProcessor());
			if (fi.fileType==FileInfo.GRAY16_UNSIGNED || fi.fileType==FileInfo.GRAY32_FLOAT)
				ThresholdAdjuster.update();
	    	return;
		}

		if (fi.fileFormat==fi.BMP) {
			// restore BMP
			IjxImagePlus imp2 = (IjxImagePlus)IJ.runPlugIn("ij.plugin.BMP_Reader", path);
			if (imp2!=null)
				imp.setProcessor(null, imp2.getProcessor());
	    	return;
		}

		if (fi.fileFormat==fi.PGM) {
			// restore PGM
			IjxImagePlus imp2 = (IjxImagePlus)IJ.runPlugIn("ij.plugin.PGM_Reader", path);
			if (imp2!=null)
				imp.setProcessor(null, imp2.getProcessor());
	    	return;
		}

		if (fi.fileFormat==fi.FITS) {
			// restore FITS
			IjxImagePlus imp2 = (IjxImagePlus)IJ.runPlugIn("ij.plugin.FITS_Reader", path);
			if (imp2!=null)
				imp.setProcessor(null, imp2.getProcessor());
			return;
		}

		if (fi.fileFormat==fi.ZIP_ARCHIVE) {
			// restore ".zip" file
			IjxImagePlus imp2 = (new Opener()).openZip(path);
			if (imp2!=null)
				imp.setProcessor(null, imp2.getProcessor());
	    	return;
		}

		// restore PNG or another image opened using ImageIO
		if (fi.fileFormat==fi.IMAGEIO) {
			IjxImagePlus imp2 = (new Opener()).openUsingImageIO(path);
			if (imp2!=null) imp.setProcessor(null, imp2.getProcessor());
	    	return;
		}

		if (fi.nImages>1)
			return;
		
		ColorModel cm;
		if (fi.url==null || fi.url.equals(""))
			IJ.showStatus("Loading: " + path);
		else
			IJ.showStatus("Loading: " + fi.url + fi.fileName);
		Object pixels = readPixels(fi);
		if (pixels==null) return;
		cm = createColorModel(fi);
		switch (fi.fileType) {
			case FileInfo.GRAY8:
			case FileInfo.COLOR8:
			case FileInfo.BITMAP:
				ip = new ByteProcessor(width, height, (byte[])pixels, cm);
		        imp.setProcessor(null, ip);
				break;
			case FileInfo.GRAY16_SIGNED:
			case FileInfo.GRAY16_UNSIGNED:
			case FileInfo.GRAY12_UNSIGNED:
	    		ip = new ShortProcessor(width, height, (short[])pixels, cm);
        		imp.setProcessor(null, ip);
				break;
			case FileInfo.GRAY32_INT:
			case FileInfo.GRAY32_FLOAT:
	    		ip = new FloatProcessor(width, height, (float[])pixels, cm);
        		imp.setProcessor(null, ip);
				break;
			case FileInfo.RGB:
			case FileInfo.BGR:
			case FileInfo.ARGB:
			case FileInfo.ABGR:
			case FileInfo.RGB_PLANAR:
	    		img = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(width, height, (int[])pixels, 0, width));
		        imp.setImage(img);
				break;
		}
	}
	
	void setCalibration(IjxImagePlus imp) {
		if (fi.fileType==FileInfo.GRAY16_SIGNED) {
			if (IJ.debugMode) IJ.log("16-bit signed");
 			imp.getLocalCalibration().setSigned16BitCalibration();
		}
		
		Properties props = decodeDescriptionString(fi);
		Calibration cal = imp.getCalibration();
		boolean calibrated = false;
		if (fi.pixelWidth>0.0 && fi.unit!=null) {
			cal.pixelWidth = fi.pixelWidth;
			cal.pixelHeight = fi.pixelHeight;
			cal.pixelDepth = fi.pixelDepth;
			cal.setUnit(fi.unit);
			calibrated = true;
		}
		
		if (fi.valueUnit!=null) {
			int f = fi.calibrationFunction;
			if ((f>=Calibration.STRAIGHT_LINE && f<=Calibration.RODBARD2 && fi.coefficients!=null)
			|| f==Calibration.UNCALIBRATED_OD) {
				boolean zeroClip = props!=null && props.getProperty("zeroclip", "false").equals("true");	
				cal.setFunction(f, fi.coefficients, fi.valueUnit, zeroClip);
				calibrated = true;
			}
		}
		
		if (calibrated)
			checkForCalibrationConflict(imp, cal);
		
		if (fi.frameInterval!=0.0)
			cal.frameInterval = fi.frameInterval;
		
		if (props==null)
			return;
					
		cal.xOrigin = getDouble(props,"xorigin");
		cal.yOrigin = getDouble(props,"yorigin");
		cal.zOrigin = getDouble(props,"zorigin");
		cal.info = props.getProperty("info");		
				
		cal.fps = getDouble(props,"fps");
		cal.loop = getBoolean(props, "loop");
		cal.frameInterval = getDouble(props,"finterval");
		cal.setTimeUnit(props.getProperty("tunit", "sec"));

		double displayMin = getDouble(props,"min");
		double displayMax = getDouble(props,"max");
		if (!(displayMin==0.0&&displayMax==0.0)) {
			int type = imp.getType();
			ImageProcessor ip = imp.getProcessor();
			if (type==IjxImagePlus.GRAY8 || type==IjxImagePlus.COLOR_256)
				ip.setMinAndMax(displayMin, displayMax);
			else if (type==IjxImagePlus.GRAY16 || type==IjxImagePlus.GRAY32) {
				if (ip.getMin()!=displayMin || ip.getMax()!=displayMax)
					ip.setMinAndMax(displayMin, displayMax);
			}
		}
		
		int stackSize = imp.getStackSize();
		if (stackSize>1) {
			int channels = (int)getDouble(props,"channels");
			int slices = (int)getDouble(props,"slices");
			int frames = (int)getDouble(props,"frames");
			if (channels==0) channels = 1;
			if (slices==0) slices = 1;
			if (frames==0) frames = 1;
			//IJ.log("setCalibration: "+channels+"  "+slices+"  "+frames);
			if (channels*slices*frames==stackSize) {
				imp.setDimensions(channels, slices, frames);
				if (getBoolean(props, "hyperstack"))
					imp.setOpenAsHyperStack(true);
			}
		}
	}

		
	void checkForCalibrationConflict(IjxImagePlus imp, Calibration cal) {
		Calibration gcal = imp.getGlobalCalibration();
		if  (gcal==null || !showConflictMessage || IJ.isMacro())
			return;
		if (cal.pixelWidth==gcal.pixelWidth && cal.getUnit().equals(gcal.getUnit()))
			return;
		GenericDialog gd = new GenericDialog(imp.getTitle());
		gd.addMessage("The calibration of this image conflicts\nwith the current global calibration.");
		gd.addCheckbox("Disable_Global Calibration", true);
		gd.addCheckbox("Disable_these Messages", false);
		gd.showDialog();
		if (gd.wasCanceled()) return;
		boolean disable = gd.getNextBoolean();
		if (disable) {
			imp.setGlobalCalibration(null);
			imp.setCalibration(cal);
			WindowManager.repaintImageWindows();
		}
		boolean dontShow = gd.getNextBoolean();
		if (dontShow) showConflictMessage = false;
	}

	/** Returns an IndexColorModel for the image specified by this FileInfo. */
	public ColorModel createColorModel(FileInfo fi) {
		if (fi.fileType==FileInfo.COLOR8 && fi.lutSize>0)
			return new IndexColorModel(8, fi.lutSize, fi.reds, fi.greens, fi.blues);
		else
			return LookUpTable.createGrayscaleColorModel(fi.whiteIsZero);
	}

	/** Returns an InputStream for the image described by this FileInfo. */
	public InputStream createInputStream(FileInfo fi) throws IOException, MalformedURLException {
		InputStream is = null;
		boolean gzip = fi.fileName!=null && (fi.fileName.endsWith(".gz")||fi.fileName.endsWith(".GZ"));
		if (fi.inputStream!=null)
			is = fi.inputStream;
		else if (fi.url!=null && !fi.url.equals(""))
			is = new URL(fi.url+fi.fileName).openStream();
		else {
			if (fi.directory.length()>0 && !fi.directory.endsWith(Prefs.separator))
				fi.directory += Prefs.separator;
		    File f = new File(fi.directory + fi.fileName);
		    if (gzip) fi.compression = FileInfo.COMPRESSION_UNKNOWN;
		    if (f==null || f.isDirectory() || !validateFileInfo(f, fi))
		    	is = null;
		    else
				is = new FileInputStream(f);
		}
		if (is!=null) {
		    if (fi.compression>=FileInfo.LZW)
				is = new RandomAccessStream(is);
			else if (gzip)
				is = new GZIPInputStream(is, 50000);
		}
		return is;
	}
	
	static boolean validateFileInfo(File f, FileInfo fi) {
		long offset = fi.getOffset();
		long length = 0;
		if (fi.width<=0 || fi.height<=0) {
		   error("Width or height <= 0.", fi, offset, length);
		   return false;
		}
		if (offset>=0 && offset<1000L)
			 return true;
		if (offset<0L) {
		   error("Offset is negative.", fi, offset, length);
		   return false;
		}
		if (fi.fileType==FileInfo.BITMAP || fi.compression!=FileInfo.COMPRESSION_NONE)
			return true;
		length = f.length();
		long size = fi.width*fi.height*fi.getBytesPerPixel();
		size = fi.nImages>1?size:size/4;
		if (fi.height==1) size = 0; // allows plugins to read info of unknown length at end of file
		if (offset+size>length) {
		   error("Offset + image size > file length.", fi, offset, length);
		   return false;
		}
		return true;
	}

	static void error(String msg, FileInfo fi, long offset, long length) {
		String msg2 = "FileInfo parameter error. \n"
			+msg + "\n \n"
			+"  Width: " + fi.width + "\n"
			+"  Height: " + fi.height + "\n"
			+"  Offset: " + offset + "\n"
			+"  Bytes/pixel: " + fi.getBytesPerPixel() + "\n"
			+(length>0?"  File length: " + length + "\n":"");
		if (silentMode) {
			IJ.log("Error opening "+fi.directory+fi.fileName);
			IJ.log(msg2);
		} else
			IJ.error("FileOpener", msg2);
	}


	/** Reads the pixel data from an image described by a FileInfo object. */
	Object readPixels(FileInfo fi) {
		Object pixels = null;
		try {
			InputStream is = createInputStream(fi);
			if (is==null)
				return null;
			ImageReader reader = new ImageReader(fi);
			pixels = reader.readPixels(is);
			minValue = reader.min;
			maxValue = reader.max;
			is.close();
		}
		catch (Exception e) {
			if (!Macro.MACRO_CANCELED.equals(e.getMessage()))
				IJ.handleException(e);
		}
		return pixels;
	}

	public Properties decodeDescriptionString(FileInfo fi) {
		if (fi.description==null || fi.description.length()<7)
			return null;
		if (IJ.debugMode)
			IJ.log("Image Description: " + new String(fi.description).replace('\n',' '));
		if (!fi.description.startsWith("ImageJ"))
			return null;
		Properties props = new Properties();
		InputStream is = new ByteArrayInputStream(fi.description.getBytes());
		try {props.load(is); is.close();}
		catch (IOException e) {return null;}
		fi.unit = props.getProperty("unit","");
		Double n = getNumber(props,"cf");
		if (n!=null) fi.calibrationFunction = n.intValue();
		double c[] = new double[5];
		int count = 0;
		for (int i=0; i<5; i++) {
			n = getNumber(props,"c"+i);
			if (n==null) break;
			c[i] = n.doubleValue();
			count++;
		}
		if (count>=2) {
			fi.coefficients = new double[count];
			for (int i=0; i<count; i++)
				fi.coefficients[i] = c[i];			
		}
		fi.valueUnit = props.getProperty("vunit");
		n = getNumber(props,"images");
		if (n!=null && n.doubleValue()>1.0)
			fi.nImages = (int)n.doubleValue();
		if (fi.nImages>1) {
			double spacing = getDouble(props,"spacing");
			if (spacing!=0.0) {
				if (spacing<0) spacing = -spacing;
				fi.pixelDepth = spacing;
			}
		}
		return props;
	}

	private Double getNumber(Properties props, String key) {
		String s = props.getProperty(key);
		if (s!=null) {
			try {
				return Double.valueOf(s);
			} catch (NumberFormatException e) {}
		}	
		return null;
	}
	
	private double getDouble(Properties props, String key) {
		Double n = getNumber(props, key);
		return n!=null?n.doubleValue():0.0;
	}
	
	private boolean getBoolean(Properties props, String key) {
		String s = props.getProperty(key);
		return s!=null&&s.equals("true")?true:false;
	}
	
	public static void setShowConflictMessage(boolean b) {
		showConflictMessage = b;
	}
	
	static void setSilentMode(boolean mode) {
		silentMode = mode;
	}


}
