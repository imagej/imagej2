package ijx.io;
import ijx.process.ImageProcessor;
import ijx.process.LUT;
import ijx.VirtualStack;
import ijx.IJ;
import ijx.LookUpTable;
import ijx.CompositeImage;
import ijx.IjxImagePlus;
import java.io.*;
import java.util.zip.*;


import ijx.measure.Calibration;
import ijx.plugin.filter.Analyzer;
import ijx.plugin.readwrite.JpegWriter;
import ijx.roi.Roi;
import ijx.gui.Overlay;
import ijx.IjxImageStack;
import ijx.Prefs;
import ijx.gui.IjxImageCanvas;

/** Saves images in tiff, gif, jpeg, raw, zip and text format. */
public class FileSaver {

	public static final int DEFAULT_JPEG_QUALITY = 75;
	private static int jpegQuality;
	
    static {setJpegQuality(ijx.Prefs.getInt(ijx.Prefs.JPEG, DEFAULT_JPEG_QUALITY));}

	private static String defaultDirectory = null;
	private IjxImagePlus imp;
	private FileInfo fi;
	private String name;
	private String directory;

	/** Constructs a FileSaver from an IjxImagePlus. */
	public FileSaver(IjxImagePlus imp) {
		this.imp = imp;
		fi = imp.getFileInfo();
	}

	/** Resaves the image. Calls saveAsTiff() if this is a new image, not a TIFF,
		or if the image was loaded using a URL. Returns false if saveAsTiff() is
		called and the user selects cancel in the file save dialog box. */
	public boolean save() {
		FileInfo ofi = null;
		if (imp!=null) ofi = imp.getOriginalFileInfo();
		boolean validName = ofi!=null && imp.getTitle().equals(ofi.fileName);
		if (validName && ofi.fileFormat==FileInfo.TIFF && ofi.directory!=null && !ofi.directory.equals("") && (ofi.url==null||ofi.url.equals(""))) {
            name = imp.getTitle();
            directory = ofi.directory;
			String path = directory+name;
			File f = new File(path);
			if (f==null || !f.exists())
				return saveAsTiff();
			if (!IJ.isMacro()) {
				if (!IJ.showMessageWithCancel("Save as TIFF", "The file "+ofi.fileName+" already exists.\nDo you want to replace it?"))
					return false;
			}
			IJ.showStatus("Saving "+path);
			if (imp.getStackSize()>1) {
				IJ.saveAs(imp, "tif", path);
				return true;
			} else
		    	return saveAsTiff(path);
		} else
			return saveAsTiff();
	}
	
	String getPath(String type, String extension) {
		name = imp.getTitle();
		SaveDialog sd = new SaveDialog("Save as "+type, name, extension);
		name = sd.getFileName();
		if (name==null)
			return null;
		directory = sd.getDirectory();
		imp.startTiming();
		String path = directory+name;
		return path;
	}
	
	/** Save the image or stack in TIFF format using a save file
		dialog. Returns false if the user selects cancel. */
	public boolean saveAsTiff() {
		String path = getPath("TIFF", ".tif");
		if (path==null)
			return false;
		if (fi.nImages>1)
			return saveAsTiffStack(path);
		else
			return saveAsTiff(path);
	}
	
	/** Save the image in TIFF format using the specified path. */
	public boolean saveAsTiff(String path) {
		fi.nImages = 1;
		Object info = imp.getProperty("Info");
		if (info!=null && (info instanceof String))
			fi.info = (String)info;
		Object label = imp.getProperty("Label");
		if (label!=null && (label instanceof String)) {
			fi.sliceLabels = new String[1];
			fi.sliceLabels[0] = (String)label;
		}
		fi.description = getDescriptionString();
		fi.roi = RoiEncoder.saveAsByteArray(imp.getRoi());
		fi.overlay = getOverlay(imp);
		try {
			TiffEncoder file = new TiffEncoder(fi);
			DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(path)));
			file.write(out);
			out.close();
		}
		catch (IOException e) {
			showErrorMessage(e);
			return false;
		}
		updateImp(fi, FileInfo.TIFF);
		return true;
	}
	
	byte[][] getOverlay(IjxImagePlus imp) {
		if (imp.getHideOverlay())
			return null;
		Overlay overlay = imp.getOverlay();
		if (overlay==null) {
			IjxImageCanvas ic = imp.getCanvas();
			if (ic==null) return null;
			overlay = ic.getShowAllList(); // ROI Manager "Show All" list
			if (overlay==null) return null;
		}
		int n = overlay.size();
		if (n==0) return null;
        // @todo re-enable this...
//		if (Orthogonal_Views.isOrthoViewsImage(imp))
//			return null;
		byte[][] array = new byte[n][];
		for (int i=0; i<overlay.size(); i++) {
			Roi roi = overlay.get(i);
			array[i] = RoiEncoder.saveAsByteArray(roi);
		}
		return array;
	}

	/** Save the stack as a multi-image TIFF using the specified path. */
	public boolean saveAsTiffStack(String path) {
		if (fi.nImages==1)
			{IJ.error("This is not a stack"); return false;}
		boolean virtualStack = imp.getStack().isVirtual();
		if (virtualStack)
			fi.virtualStack = (VirtualStack)imp.getStack();
		Object info = imp.getProperty("Info");
		if (info!=null && (info instanceof String))
			fi.info = (String)info;
		fi.description = getDescriptionString();
		if (virtualStack) {
			String[] labels = null;
			IjxImageStack vs = imp.getStack();
			for (int i=1; i<=vs.getSize(); i++) {
				ImageProcessor ip = vs.getProcessor(i);
				String label = vs.getSliceLabel(i);
				if (i==1 && (label==null||label.length()<200)) break;
				if (labels==null) labels = new String[vs.getSize()];
				labels[i-1] = label;
			}
			fi.sliceLabels = labels;
		} else
			fi.sliceLabels = imp.getStack().getSliceLabels();
		fi.roi = RoiEncoder.saveAsByteArray(imp.getRoi());
		fi.overlay = getOverlay(imp);
		if (imp.isComposite()) saveDisplayRangesAndLuts(imp, fi);
		try {
			TiffEncoder file = new TiffEncoder(fi);
			DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(path)));
			file.write(out);
			out.close();
		}
		catch (IOException e) {
			showErrorMessage(e);
			return false;
		}
		updateImp(fi, FileInfo.TIFF);
		return true;
	}
	
	void  saveDisplayRangesAndLuts(IjxImagePlus imp, FileInfo fi) {
		CompositeImage ci = (CompositeImage)imp;
		int channels = imp.getNChannels();
		fi.displayRanges = new double[channels*2];
		for (int i=1; i<=channels; i++) {
			LUT lut = ci.getChannelLut(i);
			fi.displayRanges[(i-1)*2] = lut.min;
			fi.displayRanges[(i-1)*2+1] = lut.max;
		}
		if (ci.hasCustomLuts()) {
			fi.channelLuts = new byte[channels][];
			for (int i=0; i<channels; i++) {
				LUT lut = ci.getChannelLut(i+1);
				byte[] bytes = lut.getBytes();
				if (bytes==null)
					{fi.channelLuts=null; break;}
				fi.channelLuts[i] = bytes;
			}
		}	
	}

	/** Uses a save file dialog to save the image or stack as a TIFF
		in a ZIP archive. Returns false if the user selects cancel. */
	public boolean saveAsZip() {
		String path = getPath("TIFF/ZIP", ".zip");
		if (path==null)
			return false;
		else
			return saveAsZip(path);
	}
	
	/** Save the image or stack in TIFF/ZIP format using the specified path. */
	public boolean saveAsZip(String path) {
		//fi.nImages = 1;
		if (!path.endsWith(".zip"))
			path = path+".zip";
		if (name==null)
			name = imp.getTitle();
		if (name.endsWith(".zip"))
			name = name.substring(0,name.length()-4);
		if (!name.endsWith(".tif"))
			name = name+".tif";
		fi.description = getDescriptionString();
		Object info = imp.getProperty("Info");
		if (info!=null && (info instanceof String))
			fi.info = (String)info;
		fi.roi = RoiEncoder.saveAsByteArray(imp.getRoi());
		fi.overlay = getOverlay(imp);
		fi.sliceLabels = imp.getStack().getSliceLabels();
		if (imp.isComposite()) saveDisplayRangesAndLuts(imp, fi);
		if (fi.nImages>1 && imp.getStack().isVirtual())
			fi.virtualStack = (VirtualStack)imp.getStack();
		try {
			ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(path));
			DataOutputStream out = new DataOutputStream(new BufferedOutputStream(zos));
        	zos.putNextEntry(new ZipEntry(name));
			TiffEncoder te = new TiffEncoder(fi);
			te.write(out);
			out.close();
		}
		catch (IOException e) {
			showErrorMessage(e);
			return false;
		}
		updateImp(fi, FileInfo.TIFF);
		return true;
	}

	public static boolean okForGif(IjxImagePlus imp) {
		int type = imp.getType();
		if (type==IjxImagePlus.COLOR_RGB) {
			IJ.error("To save as Gif, the image must be converted to \"8-bit Color\".");
			return false;
		} else
			return true;
	}

	/** Save the image in GIF format using a save file
		dialog. Returns false if the user selects cancel
		or the image is not 8-bits. */
	public boolean saveAsGif() {
		if (!okForGif(imp))
			return false;
		String path = getPath("GIF", ".gif");
		if (path==null)
			return false;
		else
			return saveAsGif(path);
	}
	
	/** Save the image in Gif format using the specified path. Returns
		false if the image is not 8-bits or there is an I/O error. */
	public boolean saveAsGif(String path) {
		if (!okForGif(imp)) return false;
		IJ.runPlugIn(imp, "ijx.plugin.GifWriter", path);
		updateImp(fi, FileInfo.GIF_OR_JPG);
		return true;
	}

	/** Always returns true. */
	public static boolean okForJpeg(IjxImagePlus imp) {
		return true;
	}

	/** Save the image in JPEG format using a save file
		dialog. Returns false if the user selects cancel.
		@see setJpegQuality
		@see getJpegQuality
	*/
	public boolean saveAsJpeg() {
		String type = "JPEG ("+getJpegQuality()+")";
		String path = getPath(type, ".jpg");
		if (path==null)
			return false;
		else
			return saveAsJpeg(path);
	}

	/** Save the image in JPEG format using the specified path.
		@see setJpegQuality
		@see getJpegQuality
	*/
	public boolean saveAsJpeg(String path) {
		String err = JpegWriter.save(imp, path, jpegQuality);
		if (err==null && !(imp.getType()==IjxImagePlus.GRAY16 || imp.getType()==IjxImagePlus.GRAY32))
			updateImp(fi, FileInfo.GIF_OR_JPG);
		return true;
	}

	/** Save the image in BMP format using a save file dialog. 
		Returns false if the user selects cancel. */
	public boolean saveAsBmp() {
		String path = getPath("BMP", ".bmp");
		if (path==null)
			return false;
		else
			return saveAsBmp(path);
	}

	/** Save the image in BMP format using the specified path. */
	public boolean saveAsBmp(String path) {
		IJ.runPlugIn(imp, "ijx.plugin.BMP_Writer", path);
		updateImp(fi, FileInfo.BMP);
		return true;
	}

	/** Saves grayscale images in PGM (portable graymap) format 
		and RGB images in PPM (portable pixmap) format,
		using a save file dialog.
		Returns false if the user selects cancel.
	*/
	public boolean saveAsPgm() {
		String extension = imp.getBitDepth()==24?".pnm":".pgm";
		String path = getPath("PGM", extension);
		if (path==null)
			return false;
		else
			return saveAsPgm(path);
	}

	/** Saves grayscale images in PGM (portable graymap) format 
		and RGB images in PPM (portable pixmap) format,
		using the specified path. */
	public boolean saveAsPgm(String path) {
		IJ.runPlugIn(imp, "ijx.plugin.PNM_Writer", path);
		updateImp(fi, FileInfo.PGM);
		return true;
	}

	/** Save the image in PNG format using a save file dialog. 
		Returns false if the user selects cancel. */
	public boolean saveAsPng() {
		String path = getPath("PNG", ".png");
		if (path==null)
			return false;
		else
			return saveAsPng(path);
	}

	/** Save the image in PNG format using the specified path. */
	public boolean saveAsPng(String path) {
		IJ.runPlugIn(imp, "ijx.plugin.PNG_Writer", path);
		updateImp(fi, FileInfo.IMAGEIO);
		return true;
	}

	/** Save the image in FITS format using a save file dialog. 
		Returns false if the user selects cancel. */
	public boolean saveAsFits() {
		if (!okForFits(imp)) return false;
		String path = getPath("FITS", ".fits");
		if (path==null)
			return false;
		else
			return saveAsFits(path);
	}

	/** Save the image in FITS format using the specified path. */
	public boolean saveAsFits(String path) {
		if (!okForFits(imp)) return false;
		IJ.runPlugIn(imp, "ijx.plugin.FITS_Writer", path);
		updateImp(fi, FileInfo.FITS);
		return true;
	}

	public static boolean okForFits(IjxImagePlus imp) {
		if (imp.getBitDepth()==24) {
			IJ.error("FITS Writer", "Grayscale image required");
			return false;
		} else
			return true;
	}

	/** Save the image or stack as raw data using a save file
		dialog. Returns false if the user selects cancel. */
	public boolean saveAsRaw() {
		String path = getPath("Raw", ".raw");
		if (path==null)
			return false;
		if (imp.getStackSize()==1)
			return saveAsRaw(path);
		else
			return saveAsRawStack(path);
	}
	
	/** Save the image as raw data using the specified path. */
	/** Save the image as raw data using the specified path. */
	public boolean saveAsRaw(String path) {
		fi.nImages = 1;
		fi.intelByteOrder = Prefs.intelByteOrder;
		boolean signed16Bit = false;
		short[] pixels = null;
		int n = 0;
		try {
			signed16Bit = imp.getCalibration().isSigned16Bit();
			if (signed16Bit) {
				pixels = (short[])imp.getProcessor().getPixels();
				n = imp.getWidth()*imp.getHeight();
				for (int i=0; i<n; i++)
					pixels[i] = (short)(pixels[i]-32768);
			}
			ImageWriter file = new ImageWriter(fi);
			OutputStream out = new BufferedOutputStream(new FileOutputStream(path));
			file.write(out);
			out.close();
		}
		catch (IOException e) {
			showErrorMessage(e);
			return false;
		}
		if (signed16Bit) {
			for (int i=0; i<n; i++)
			pixels[i] = (short)(pixels[i]+32768);
		}
		updateImp(fi, fi.RAW);
		return true;
	}

	/** Save the stack as raw data using the specified path. */
	public boolean saveAsRawStack(String path) {
		if (fi.nImages==1)
			{IJ.write("This is not a stack"); return false;}
		fi.intelByteOrder = Prefs.intelByteOrder;
		boolean signed16Bit = false;
		Object[] stack = null;
		int n = 0;
		boolean virtualStack = imp.getStackSize()>1 && imp.getStack().isVirtual();
		if (virtualStack) {
			fi.virtualStack = (VirtualStack)imp.getStack();
			if (imp.getProperty("AnalyzeFormat")!=null) fi.fileName="FlipTheseImages";
		}
		try {
			signed16Bit = imp.getCalibration().isSigned16Bit();
			if (signed16Bit && !virtualStack) {
				stack = (Object[])fi.pixels;
				n = imp.getWidth()*imp.getHeight();
				for (int slice=0; slice<fi.nImages; slice++) {
					short[] pixels = (short[])stack[slice];
					for (int i=0; i<n; i++)
						pixels[i] = (short)(pixels[i]-32768);
				}
			}
			ImageWriter file = new ImageWriter(fi);
			OutputStream out = new BufferedOutputStream(new FileOutputStream(path));
			file.write(out);
			out.close();
		}
		catch (IOException e) {
			showErrorMessage(e);
			return false;
		}
		if (signed16Bit) {
			for (int slice=0; slice<fi.nImages; slice++) {
				short[] pixels = (short[])stack[slice];
				for (int i=0; i<n; i++)
					pixels[i] = (short)(pixels[i]+32768);
			}
		}
		updateImp(fi, fi.RAW);
		return true;
	}

	/** Save the image as tab-delimited text using a save file
		dialog. Returns false if the user selects cancel. */
	public boolean saveAsText() {
		String path = getPath("Text", ".txt");
		if (path==null)
			return false;
		return saveAsText(path);
	}
	
	/** Save the image as tab-delimited text using the specified path. */
	public boolean saveAsText(String path) {
		try {
			Calibration cal = imp.getCalibration();
			int precision = Analyzer.getPrecision();
			TextEncoder file = new TextEncoder(imp.getProcessor(), cal, precision);
			DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(path)));
			file.write(out);
			out.close();
		}
		catch (IOException e) {
			showErrorMessage(e);
			return false;
		}
		return true;
	}

	/** Save the current LUT using a save file
		dialog. Returns false if the user selects cancel. */
	public boolean saveAsLut() {
		if (imp.getType()==IjxImagePlus.COLOR_RGB) {
			IJ.error("RGB Images do not have a LUT.");
			return false;
		}
		String path = getPath("LUT", ".lut");
		if (path==null)
			return false;
		return saveAsLut(path);
	}
	
	/** Save the current LUT using the specified path. */
	public boolean saveAsLut(String path) {
		LookUpTable lut = imp.createLut();
		int mapSize = lut.getMapSize();
		if (mapSize==0) {
			IJ.error("RGB Images do not have a LUT.");
			return false;
		}
		if (mapSize<256) {
			IJ.error("Cannot save LUTs with less than 256 entries.");
			return false;
		}
		byte[] reds = lut.getReds(); 
		byte[] greens = lut.getGreens();
		byte[] blues = lut.getBlues();
		byte[] pixels = new byte[768];
		for (int i=0; i<256; i++) {
			pixels[i] = reds[i];
			pixels[i+256] = greens[i];
			pixels[i+512] = blues[i];
		}
		FileInfo fi = new FileInfo();
		fi.width = 768;
		fi.height = 1;
		fi.pixels = pixels;

		try {
			ImageWriter file = new ImageWriter(fi);
			OutputStream out = new FileOutputStream(path);
			file.write(out);
			out.close();
		}
		catch (IOException e) {
			showErrorMessage(e);
			return false;
		}
		return true;
	}

	private void updateImp(FileInfo fi, int fileFormat) {
		imp.setChanged(false);
		if (name!=null) {
			fi.fileFormat = fileFormat;
			fi.fileName = name;
			fi.directory = directory;
			//if (fileFormat==fi.TIFF)
			//	fi.offset = TiffEncoder.IMAGE_START;
			fi.description = null;
			imp.setTitle(name);
			imp.setFileInfo(fi);
		}
	}

	void showErrorMessage(IOException e) {
		String msg = e.getMessage();
		if (msg.length()>100)
			msg = msg.substring(0, 100);
		IJ.error("FileSaver", "An error occured writing the file.\n \n" + msg);
	}

	/** Returns a string containing information about the specified  image. */
	public String getDescriptionString() {
		Calibration cal = imp.getCalibration();
		StringBuffer sb = new StringBuffer(100);
		sb.append("ImageJ="+IJ.getVersion()+"\n");
		if (fi.nImages>1 && fi.fileType!=FileInfo.RGB48)
			sb.append("images="+fi.nImages+"\n");
		int channels = imp.getNChannels();
		if (channels>1)
			sb.append("channels="+channels+"\n");
		int slices = imp.getNSlices();
		if (slices>1)
			sb.append("slices="+slices+"\n");
		int frames = imp.getNFrames();
		if (frames>1)
			sb.append("frames="+frames+"\n");
		if (imp.isHyperStack()) sb.append("hyperstack=true\n");
		if (imp.isComposite()) {
			String mode = ((CompositeImage)imp).getModeAsString();
			sb.append("mode="+mode+"\n");
		}
		if (fi.unit!=null)
			sb.append("unit="+(fi.unit.equals("\u00B5m")?"um":fi.unit)+"\n");
		if (fi.valueUnit!=null && fi.calibrationFunction!=Calibration.CUSTOM) {
			sb.append("cf="+fi.calibrationFunction+"\n");
			if (fi.coefficients!=null) {
				for (int i=0; i<fi.coefficients.length; i++)
					sb.append("c"+i+"="+fi.coefficients[i]+"\n");
			}
			sb.append("vunit="+fi.valueUnit+"\n");
			if (cal.zeroClip()) sb.append("zeroclip=true\n");
		}
		
		// get stack z-spacing and fps
		if (cal.frameInterval!=0.0) {
			if ((int)cal.frameInterval==cal.frameInterval)
				sb.append("finterval="+(int)cal.frameInterval+"\n");
			else
				sb.append("finterval="+cal.frameInterval+"\n");
		}
		if (!cal.getTimeUnit().equals("sec"))
			sb.append("tunit="+cal.getTimeUnit()+"\n");
		if (fi.nImages>1) {
			if (fi.pixelDepth!=0.0 && fi.pixelDepth!=1.0)
				sb.append("spacing="+fi.pixelDepth+"\n");
			if (cal.fps!=0.0) {
				if ((int)cal.fps==cal.fps)
					sb.append("fps="+(int)cal.fps+"\n");
				else
					sb.append("fps="+cal.fps+"\n");
			}
			sb.append("loop="+(cal.loop?"true":"false")+"\n");
		}
		
		// get min and max display values
		ImageProcessor ip = imp.getProcessor();
		double min = ip.getMin();
		double max = ip.getMax();
		int type = imp.getType();
		boolean enhancedLut = (type==IjxImagePlus.GRAY8 || type==IjxImagePlus.COLOR_256) && (min!=0.0 || max !=255.0);
		if (enhancedLut || type==IjxImagePlus.GRAY16 || type==IjxImagePlus.GRAY32) {
			sb.append("min="+min+"\n");
			sb.append("max="+max+"\n");
		}
		
		// get non-zero origins
		if (cal.xOrigin!=0.0)
			sb.append("xorigin="+cal.xOrigin+"\n");
		if (cal.yOrigin!=0.0)
			sb.append("yorigin="+cal.yOrigin+"\n");
		if (cal.zOrigin!=0.0)
			sb.append("zorigin="+cal.zOrigin+"\n");
		if (cal.info!=null && cal.info.length()<=64 && cal.info.indexOf('=')==-1 && cal.info.indexOf('\n')==-1)
			sb.append("info="+cal.info+"\n");			
		sb.append((char)0);
		return new String(sb);
	}
	
	/** Specifies the image quality (0-100). 0 is poorest image quality,
		highest compression, and 100 is best image quality, lowest compression. */
    public static void setJpegQuality(int quality) {
        jpegQuality = quality;
    	if (jpegQuality<0) jpegQuality = 0;
    	if (jpegQuality>100) jpegQuality = 100;
    }

    /** Returns the current JPEG quality setting (0-100). */
    public static int getJpegQuality() {
        return jpegQuality;
    }


}
