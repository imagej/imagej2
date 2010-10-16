package ij.io;
import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.plugin.frame.*;
import ij.plugin.DICOM;
import ij.plugin.AVI_Reader;
import ij.plugin.SimpleCommands;
import ij.text.TextWindow;
import ij.util.Java2;
import ij.measure.ResultsTable;
import imagej.io.ImageOpener;
import imagej.plugin.AutoPluginInvoker;
import imagej.process.ImageUtils;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.Hashtable;
import java.util.zip.*;
import java.util.Locale;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.awt.event.KeyEvent;
import javax.imageio.ImageIO;

import loci.common.StatusEvent;
import loci.common.StatusListener;
import loci.formats.FormatException;
import mpicbg.imglib.type.numeric.RealType;

/** Opens tiff (and tiff stacks), dicom, fits, pgm, jpeg, bmp or
	gif images, and look-up tables, using a file open dialog or a path.
	Calls HandleExtraFileTypes plugin if the file type is unrecognised. */
public class Opener {

	public static final int UNKNOWN=0,TIFF=1,DICOM=2,FITS=3,PGM=4,JPEG=5,
		GIF=6,LUT=7,BMP=8,ZIP=9,JAVA_OR_TEXT=10,ROI=11,TEXT=12,PNG=13,
		TIFF_AND_DICOM=14,CUSTOM=15, AVI=16, OJJ=17, TABLE=18; // don't forget to also update 'types'
	private static final String[] types = {"unknown","tif","dcm","fits","pgm",
		"jpg","gif","lut","bmp","zip","java/txt","roi","txt","png","t&d","custom","ojj","table"};
	private static String defaultDirectory = null;
	private static int fileType;
	private boolean error;
	private boolean isRGB48;
	private boolean silentMode;
	private String omDirectory;
	private File[] omFiles;
	private static boolean openUsingPlugins;
	private static boolean bioformats;

	static {
		Hashtable commands = Menus.getCommands();
		bioformats = commands!=null && commands.get("Bio-Formats Importer")!=null;
	}

	public Opener() {
	}

	/** Displays a file open dialog box and then opens the tiff, dicom, 
		fits, pgm, jpeg, bmp, gif, lut, roi, or text file selected by 
		the user. Displays an error message if the selected file is not
		in one of the supported formats. This is the method that
		ImageJ's File/Open command uses to open files. */
	public void open() {
		OpenDialog od = new OpenDialog("Open", "");
		String directory = od.getDirectory();
		String name = od.getFileName();
		if (name!=null) {
			String path = directory+name;
			error = false;
			open(path);
			if (!error) Menus.addOpenRecentItem(path);
		}
	}

	/** Displays a JFileChooser and then opens the tiff, dicom, 
		fits, pgm, jpeg, bmp, gif, lut, roi, or text files selected by 
		the user. Displays error messages if one or more of the selected 
		files is not in one of the supported formats. This is the method
		that ImageJ's File/Open command uses to open files if
		"Open/Save Using JFileChooser" is checked in EditOptions/Misc. */
	public void openMultiple() {
		Java2.setSystemLookAndFeel();
		// run JFileChooser in a separate thread to avoid possible thread deadlocks
		try {
			EventQueue.invokeAndWait(new Runnable() {
				public void run() {
					JFileChooser fc = new JFileChooser();
					fc.setMultiSelectionEnabled(true);
					File dir = null;
					String sdir = OpenDialog.getDefaultDirectory();
					if (sdir!=null)
						dir = new File(sdir);
					if (dir!=null)
						fc.setCurrentDirectory(dir);
					int returnVal = fc.showOpenDialog(IJ.getInstance());
					if (returnVal!=JFileChooser.APPROVE_OPTION)
						return;
					omFiles = fc.getSelectedFiles();
					if (omFiles.length==0) { // getSelectedFiles does not work on some JVMs
						omFiles = new File[1];
						omFiles[0] = fc.getSelectedFile();
					}
					omDirectory = fc.getCurrentDirectory().getPath()+File.separator;
				}
			});
		} catch (Exception e) {}
		if (omDirectory==null) return;
		OpenDialog.setDefaultDirectory(omDirectory);
		for (int i=0; i<omFiles.length; i++) {
			String path = omDirectory + omFiles[i].getName();
			open(path);
			if (i==0 && Recorder.record)
				Recorder.recordPath("open", path);
			if (i==0 && !error)
				Menus.addOpenRecentItem(path);
		}
	}

	/** Opens and displays a tiff, dicom, fits, pgm, jpeg, bmp, gif, lut, 
		roi, or text file. Displays an error message if the specified file
		is not in one of the supported formats. */
	public void open(String path) {
		boolean isURL = path.startsWith("http://");
		if (isURL && isText(path)) {
			openTextURL(path);
			return;
		}
		if (path.endsWith(".jar") || path.endsWith(".class")) {
				(new PluginInstaller()).install(path);
				return;
		}
		boolean fullPath = path.startsWith("/") || path.startsWith("\\") || path.indexOf(":\\")==1 || isURL;
		if (!fullPath) {
			String defaultDir = OpenDialog.getDefaultDirectory();
			if (defaultDir!=null)
				path = defaultDir + path;
			else
				path = (new File(path)).getAbsolutePath();
		}
		if (!silentMode) IJ.showStatus("Opening: " + path);
		long start = System.currentTimeMillis();
		ImagePlus imp = openImage(path);
		if (imp!=null) {
			WindowManager.checkForDuplicateName = true;
			if (isRGB48)
				openRGB48(imp);
			else if (!(new AutoPluginInvoker()).matchPlugin(imp)) {
				imp.show(IJ.d2s((System.currentTimeMillis()-start)/1000.0,3)+" seconds");
                        }
		} else {
			switch (fileType) {
				case LUT:
					imp = (ImagePlus)IJ.runPlugIn("ij.plugin.LutLoader", path);
					if (imp.getWidth()!=0)
						imp.show();
					break;
				case ROI:
					IJ.runPlugIn("ij.plugin.RoiReader", path);
					break;
				case JAVA_OR_TEXT: case TEXT:
					if (IJ.altKeyDown()) { // open in TextWindow if alt key down
						new TextWindow(path,400,450);
						IJ.setKeyUp(KeyEvent.VK_ALT);
						break;
					}
					File file = new File(path);
					int maxSize = 250000;
					long size = file.length();
					if (size>=28000) {
						String osName = System.getProperty("os.name");
						if (osName.equals("Windows 95") || osName.equals("Windows 98") || osName.equals("Windows Me"))
							maxSize = 60000;
					}
					if (size<maxSize) {
						Editor ed = (Editor)IJ.runPlugIn("ij.plugin.frame.Editor", "");
						if (ed!=null) ed.open(getDir(path), getName(path));
					} else
						new TextWindow(path,400,450);
					break;
				case OJJ:  // ObjectJ project
					IJ.runPlugIn("ObjectJ_", path);
					break;
				case TABLE:  // ImageJ Results table
					openResultsTable(path);
					break;
				case UNKNOWN:
					String msg =
						"File is not in a supported format, a reader\n"+
						"plugin is not available, or it was not found.";
					if (path!=null) {
						if (path.length()>64)
							path = (new File(path)).getName();
						if (path.length()<=64) {
							if (IJ.redirectingErrorMessages())
								msg += " \n   "+path;
							else
								msg += " \n	 \n"+path;
						}
					}
					if (openUsingPlugins)
						msg += "\n \nNOTE: The \"OpenUsingPlugins\" option is set.";
					IJ.wait(IJ.isMacro()?500:100); // work around for OS X thread deadlock problem
					IJ.error("Opener", msg);
					error = true;
					break;
			}
		}
	}
	
	private boolean isText(String path) {
		if (path.endsWith(".txt") || path.endsWith(".ijm") || path.endsWith(".java")
		|| path.endsWith(".js") || path.endsWith(".html") || path.endsWith(".htm")
		|| path.endsWith("/"))
			return true;
		int lastSlash = path.lastIndexOf("/");
		if (lastSlash==-1) lastSlash = 0;
		int lastDot = path.lastIndexOf(".");
		if (lastDot==-1 || lastDot<lastSlash || (path.length()-lastDot)>6)
			return true;  // no extension
		else
			return false;
	}
	
	/** Opens the specified file and adds it to the File/Open Recent menu.
		Returns true if the file was opened successfully.  */
	public	boolean openAndAddToRecent(String path) {
		open(path);
		if (!error)
			Menus.addOpenRecentItem(path);
		return error;
	}

	/** Attempts to open the specified file as a tiff, bmp, dicom, fits,
		pgm, gif or jpeg image. Returns an ImagePlus object if successful.
		Modified by Gregory Jefferis to call HandleExtraFileTypes plugin if 
		the file type is unrecognised. */
	public ImagePlus openImage(String directory, String name) {
		FileOpener.setSilentMode(silentMode);
		if (directory.length()>0)
			if (!directory.endsWith("/") && !directory.endsWith("\\"))
				directory += Prefs.separator;
		String path = directory+name;
		fileType = getFileType(path);
		if (IJ.debugMode)
			IJ.log("openImage: \""+types[fileType]+"\", "+path);
		// CTR 2010-09-24: First cut at using bf-imglib image opener.
		return openImglibImage(path);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private ImagePlus openImglibImage(String path) {
		final ImageOpener imageOpener = new ImageOpener();
		try {
			imageOpener.addStatusListener(new StatusListener() {
				public void statusUpdated(StatusEvent e) {
					final String message = e.getStatusMessage();
					final int value = e.getProgressValue();
					final int maximum = e.getProgressMaximum();
					if (value >= 0 && maximum >= 0) IJ.showProgress(value, maximum);
					IJ.showStatus(message);
				}
			});
			final mpicbg.imglib.image.Image<? extends RealType> img =
				imageOpener.openImage(path);
			return ImageUtils.createImagePlus(img, path);
		}
		catch (FormatException e) {
			IJ.handleException(e);
		}
		catch (IOException e) {
			IJ.handleException(e);
		}
		return null;
	}

	/** Attempts to open the specified file as a tiff, bmp, dicom, fits,
		pgm, gif or jpeg. Displays a file open dialog if 'path' is null or
		an empty string. Returns an ImagePlus object if successful. */
	public ImagePlus openImage(String path) {
		if (path==null || path.equals(""))
			path = getPath();
		if (path==null) return null;
		ImagePlus img = null;
		if (path.indexOf("://")>0)
			img = openURL(path);
		else
			img = openImage(getDir(path), getName(path));
		return img;
	}
	
	/** Open the nth image of the specified tiff stack. */
	public ImagePlus openImage(String path, int n) {
		if (path==null || path.equals(""))
			path = getPath();
		if (path==null) return null;
		int type = getFileType(path);
		if (type!=TIFF)
			throw new IllegalArgumentException("TIFF file require");
		return openTiff(path, n);
	}

	String getPath() {
		OpenDialog od = new OpenDialog("Open", "");
		String dir = od.getDirectory();
		String name = od.getFileName();
		if (name==null)
			return null;
		else
			return dir+name;
	}

	/** Attempts to open the specified url as a tiff, zip compressed tiff, 
		dicom, gif or jpeg. Tiff file names must end in ".tif", ZIP file names 
		must end in ".zip" and dicom file names must end in ".dcm". Returns an 
		ImagePlus object if successful. */
	public ImagePlus openURL(String url) {
		try {
			String name = "";
			int index = url.lastIndexOf('/');
			if (index==-1)
				index = url.lastIndexOf('\\');
			if (index>0)
				name = url.substring(index+1);
			else
				throw new MalformedURLException("Invalid URL: "+url);
			if (url.indexOf(" ")!=-1)
				url = url.replaceAll(" ", "%20");
			URL u = new URL(url);
			IJ.showStatus(""+url);
			String lurl = url.toLowerCase(Locale.US);
			ImagePlus imp = null;
			if (lurl.endsWith(".tif"))
				imp = openTiff(u.openStream(), name);
			else if (lurl.endsWith(".zip"))
				imp = openZipUsingUrl(u);
			else if (lurl.endsWith(".jpg") || lurl.endsWith(".gif"))
				imp = openJpegOrGifUsingURL(name, u);
			else if (lurl.endsWith(".dcm") || lurl.endsWith(".ima")) {
				imp = (ImagePlus)IJ.runPlugIn("ij.plugin.DICOM", url);
				if (imp!=null && imp.getWidth()==0) imp = null;
			} else if (lurl.endsWith(".png"))
				imp = openPngUsingURL(name, u);
			else {
				URLConnection uc = u.openConnection();
				String type = uc.getContentType();
				if (type!=null && (type.equals("image/jpeg")||type.equals("image/gif")))
					imp = openJpegOrGifUsingURL(name, u);
				else if (type!=null && type.equals("image/png"))
					imp = openPngUsingURL(name, u);
				else
					imp = openWithHandleExtraFileTypes(url, new int[]{0});
			}
			IJ.showStatus("");
			return imp;
		} catch (Exception e) {
			String msg = e.getMessage();
			if (msg==null || msg.equals(""))
				msg = "" + e;	
			IJ.error("Open URL",msg + "\n \n" + url);
			return null;
		} 
	}
	
	/** Used by open() and IJ.open() to open text URLs. */
	void openTextURL(String url) {
		if (url.endsWith(".pdf")||url.endsWith(".zip"))
			return;
		String text = IJ.openUrlAsString(url);
		String name = url.substring(7);
		int index = name.lastIndexOf("/");
		int len = name.length();
		if (index==len-1)
			name = name.substring(0, len-1);
		else if (index!=-1 && index<len-1)
			name = name.substring(index+1);
		name = name.replaceAll("%20", " ");
		Editor ed = new Editor();
		ed.setSize(600, 300);
		ed.create(name, text);
		IJ.showStatus("");
	}

	
	public ImagePlus openWithHandleExtraFileTypes(String path, int[] fileType) {
		ImagePlus imp = null;
		if (path.endsWith(".db")) {
			// skip hidden Thumbs.db files on Windows
			fileType[0] = CUSTOM;
			return null;
		}
		imp = (ImagePlus)IJ.runPlugIn("HandleExtraFileTypes", path);
		if (imp==null) return null;
		FileInfo fi = imp.getOriginalFileInfo();
		if (fi==null) {
			fi = new FileInfo();
			fi.width = imp.getWidth();
			fi.height = imp.getHeight();
			fi.directory = getDir(path);
			fi.fileName = getName(path);
			imp.setFileInfo(fi);
		}
		if (imp.getWidth()>0 && imp.getHeight()>0) {
			fileType[0] = CUSTOM;
			return imp;
		} else {
			if (imp.getWidth()==-1)
				fileType[0] = CUSTOM; // plugin opened image so don't display error
			return null;
		}
	}

	/** Opens the ZIP compressed TIFF or DICOM at the specified URL. */
	ImagePlus openZipUsingUrl(URL url) throws IOException {
		URLConnection uc = url.openConnection();
		int fileSize = uc.getContentLength(); // compressed size
		fileSize *=2; // estimate uncompressed size
		InputStream in = uc.getInputStream();
		ZipInputStream zin = new ZipInputStream(in);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buf = new byte[4096];
		ZipEntry entry = zin.getNextEntry();
		if (entry==null)
			return null;
		String name = entry.getName();
		//double fileSize = entry.getSize(); //returns -1
		if (!(name.endsWith(".tif")||name.endsWith(".dcm")))
			throw new IOException("This ZIP archive does not appear to contain a .tif or .dcm file");
		if (name.endsWith(".dcm")) {
			DICOM dcm = new DICOM(zin);
			dcm.run(name);
			return dcm;
		} else
			return openTiff(zin, name);
	}

	ImagePlus openJpegOrGifUsingURL(String title, URL url) {
		if (url==null) return null;
		Image img = Toolkit.getDefaultToolkit().createImage(url);
		if (img!=null) {
			ImagePlus imp = new ImagePlus(title, img);
			return imp;
		} else
			return null;
	}

	ImagePlus openPngUsingURL(String title, URL url) {
		if (url==null) return null;
		Image img = null;
		try {
			img = ImageIO.read(url);
		} catch (IOException e) {
			IJ.log(""+e);
		}
		if (img!=null) {
			ImagePlus imp = new ImagePlus(title, img);
			return imp;
		} else
			return null;
	}

	ImagePlus openJpegOrGif(String dir, String name) {
		ImagePlus imp = null;
		Image img = Toolkit.getDefaultToolkit().createImage(dir+name);
		if (img!=null) {
			try {
				imp = new ImagePlus(name, img);
			} catch (IllegalStateException e) {
				return null; // error loading image
			}				
			if (imp.getType()==ImagePlus.COLOR_RGB)
				convertGrayJpegTo8Bits(imp);
			FileInfo fi = new FileInfo();
			fi.fileFormat = fi.GIF_OR_JPG;
			fi.fileName = name;
			fi.directory = dir;
			imp.setFileInfo(fi);
		}
		return imp;
	}
	
	ImagePlus openUsingImageIO(String path) {
		ImagePlus imp = null;
		BufferedImage img = null;
		File f = new File(path);
		try {
			img = ImageIO.read(f);
		} catch (Exception e) {
			IJ.error("Open Using ImageIO", ""+e);
		} 
		if (img==null) return null;
		imp = new ImagePlus(f.getName(), img);
		FileInfo fi = new FileInfo();
		fi.fileFormat = fi.IMAGEIO;
		fi.fileName = f.getName();
		fi.directory = f.getParent()+File.separator;
		imp.setFileInfo(fi);
		return imp;
	}

	/** If this image is grayscale, convert it to 8-bits. */
	public static void convertGrayJpegTo8Bits(ImagePlus imp) {
		ImageProcessor ip = imp.getProcessor();
		int width = ip.getWidth();
		int height = ip.getHeight();
		int[] pixels = (int[])ip.getPixels();
		int c,r,g,b,offset;
		for (int y=0; y<(height-8); y++) {
			offset = y*width;
			for (int x=0; x<(width-8); x++) {
				c = pixels[offset+x];
				r = (c&0xff0000)>>16;
				g = (c&0xff00)>>8;
				b = c&0xff;
				if (!((r==g)&&(g==b))) {
					//IJ.write("count: "+count+" "+r+" "+g+" "+b);
					return;
				}
			}
			//count++;
		}
		IJ.showStatus("Converting to 8-bits");
		new ImageConverter(imp).convertToGray8();
	}

	/** Are all the images in this file the same size and type? */
	boolean allSameSizeAndType(FileInfo[] info) {
		boolean sameSizeAndType = true;
		boolean contiguous = true;
		int startingOffset = info[0].offset;
		int size = info[0].width*info[0].height*info[0].getBytesPerPixel();
		for (int i=1; i<info.length; i++) {
			sameSizeAndType &= info[i].fileType==info[0].fileType
				&& info[i].width==info[0].width
				&& info[i].height==info[0].height;
			contiguous &= info[i].offset==startingOffset+i*size;
		}
		if (contiguous &&  info[0].fileType!=FileInfo.RGB48)
			info[0].nImages = info.length;
		if (IJ.debugMode) {
			IJ.log("  sameSizeAndType: " + sameSizeAndType);
			IJ.log("  contiguous: " + contiguous);
		}
		return sameSizeAndType;
	}
	
	/** Attemps to open a tiff file as a stack. Returns 
		an ImagePlus object if successful. */
	public ImagePlus openTiffStack(FileInfo[] info) {
		if (info.length>1 && !allSameSizeAndType(info))
			return null;
		FileInfo fi = info[0];
		if (fi.nImages>1)
			return new FileOpener(fi).open(false); // open contiguous images as stack
		else {
			ColorModel cm = createColorModel(fi);
			ImageStack stack = new ImageStack(fi.width, fi.height, cm);
			Object pixels = null;
			int skip = fi.offset;
			int imageSize = fi.width*fi.height*fi.getBytesPerPixel();
			if (info[0].fileType==FileInfo.GRAY12_UNSIGNED) {
				imageSize = (int)(fi.width*fi.height*1.5);
				if ((imageSize&1)==1) imageSize++; // add 1 if odd
			} if (info[0].fileType==FileInfo.BITMAP) {
				int scan=(int)Math.ceil(fi.width/8.0);
				imageSize = scan*fi.height;
			}
			int loc = 0;
			int nChannels = 1;
			try {
				InputStream is = createInputStream(fi);
				ImageReader reader = new ImageReader(fi);
				IJ.resetEscape();
				for (int i=0; i<info.length; i++) {
					nChannels = 1;
					Object[] channels = null;
					if (!silentMode)
						IJ.showStatus("Reading: " + (i+1) + "/" + info.length);
					if (IJ.escapePressed()) {
						IJ.beep();
						IJ.showProgress(1.0);
						return null;
					}
					if (info[i].compression>=FileInfo.LZW) {
						fi.stripOffsets = info[i].stripOffsets;
						fi.stripLengths = info[i].stripLengths;
					}
					if (info[i].samplesPerPixel>1 && !(info[i].getBytesPerPixel()==3||info[i].getBytesPerPixel()==6)) {
						nChannels = fi.samplesPerPixel;
						channels = new Object[nChannels];
						for (int c=0; c<nChannels; c++) {
							pixels = reader.readPixels(is, c==0?skip:0);
							channels[c] = pixels;
						}
					} else 
						pixels = reader.readPixels(is, skip);
					if (pixels==null && channels==null) break;
					loc += imageSize*nChannels+skip;
					if (i<(info.length-1)) {
						skip = info[i+1].offset-loc;
						if (info[i+1].compression>=FileInfo.LZW) skip = 0;
						if (skip<0)
							throw new IOException("Images are not in order");
					}
					if (fi.fileType==FileInfo.RGB48) {
						Object[] pixels2 = (Object[])pixels;
						stack.addSlice(null, pixels2[0]);					
						stack.addSlice(null, pixels2[1]);					
						stack.addSlice(null, pixels2[2]);
						isRGB48 = true;					
					} else if (nChannels>1) {
						for (int c=0; c<nChannels; c++) {
							if (channels[c]!=null)
								stack.addSlice(null, channels[c]);
						}
					} else
						stack.addSlice(null, pixels);					
					IJ.showProgress(i, info.length);
				}
				is.close();
			}
			catch (Exception e) {
				IJ.handleException(e);
			}
			catch(OutOfMemoryError e) {
				IJ.outOfMemory(fi.fileName);
				stack.deleteLastSlice();
				stack.deleteLastSlice();
			}
			IJ.showProgress(1.0);
			if (stack.getSize()==0)
				return null;
			if (fi.fileType==FileInfo.GRAY16_UNSIGNED||fi.fileType==FileInfo.GRAY12_UNSIGNED
			||fi.fileType==FileInfo.GRAY32_FLOAT||fi.fileType==FileInfo.RGB48) {
				ImageProcessor ip = stack.getProcessor(1);
				ip.resetMinAndMax();
				stack.update(ip);
			}
			//if (fi.whiteIsZero)
			//	new StackProcessor(stack, stack.getProcessor(1)).invert();
			ImagePlus imp = new ImagePlus(fi.fileName, stack);
			new FileOpener(fi).setCalibration(imp);
			imp.setFileInfo(fi);
			int stackSize = stack.getSize();
			if (nChannels>1 && (stackSize%nChannels)==0) {
				imp.setDimensions(nChannels, stackSize/nChannels, 1);
				imp = new CompositeImage(imp, CompositeImage.COMPOSITE);
				imp.setOpenAsHyperStack(true);
			}
			IJ.showProgress(1.0);
			return imp;
		}
	}
	
	/** Attempts to open the specified file as a tiff.
		Returns an ImagePlus object if successful. */
	public ImagePlus openTiff(String directory, String name) {
		TiffDecoder td = new TiffDecoder(directory, name);
		if (IJ.debugMode) td.enableDebugging();
		FileInfo[] info=null;
		try {info = td.getTiffInfo();}
		catch (IOException e) {
			String msg = e.getMessage();
			if (msg==null||msg.equals("")) msg = ""+e;
			IJ.error("TiffDecoder", msg);
			return null;
		}
		if (info==null)
			return null;
		return openTiff2(info);
	}
	
	/** Opens the nth image of the specified TIFF stack. */
	public ImagePlus openTiff(String path, int n) {
		TiffDecoder td = new TiffDecoder(getDir(path), getName(path));
		if (IJ.debugMode) td.enableDebugging();
		FileInfo[] info=null;
		try {info = td.getTiffInfo();}
		catch (IOException e) {
			String msg = e.getMessage();
			if (msg==null||msg.equals("")) msg = ""+e;
			IJ.error("TiffDecoder", msg);
			return null;
		}
		if (info==null) return null;
		FileInfo fi = info[0];
		if (info.length==1 && fi.nImages>1) {
			if (n<1 || n>fi.nImages)
				throw new IllegalArgumentException("N out of 1-"+fi.nImages+" range");
			long size = fi.width*fi.height*fi.getBytesPerPixel();
			fi.longOffset = fi.getOffset() + (n-1)*(size+fi.gapBetweenImages);
			fi.offset = 0;
			fi.nImages = 1;
		} else {
			if (n<1 || n>info.length)
				throw new IllegalArgumentException("N out of 1-"+info.length+" range");
			fi.longOffset = info[n-1].getOffset();
			fi.offset = 0;
			fi.stripOffsets = info[n-1].stripOffsets; 
			fi.stripLengths = info[n-1].stripLengths; 
		}
		FileOpener fo = new FileOpener(fi);
		return fo.open(false);
	}

	/** Attempts to open the specified inputStream as a
		TIFF, returning an ImagePlus object if successful. */
	public ImagePlus openTiff(InputStream in, String name) {
		FileInfo[] info = null;
		try {
			TiffDecoder td = new TiffDecoder(in, name);
			if (IJ.debugMode) td.enableDebugging();
			info = td.getTiffInfo();
		} catch (FileNotFoundException e) {
			IJ.error("TiffDecoder", "File not found: "+e.getMessage());
			return null;
		} catch (Exception e) {
			IJ.error("TiffDecoder", ""+e);
			return null;
		}
		return openTiff2(info);
	}

		/** Opens a single TIFF or DICOM contained in a ZIP archive,
			or a ZIPed collection of ".roi" files created by the ROI manager. */	
	public ImagePlus openZip(String path) {
		ImagePlus imp = null;
		try {
			ZipInputStream in = new ZipInputStream(new FileInputStream(path));
			if (in==null) return null;
			ZipEntry entry = in.getNextEntry();
			if (entry==null) return null;
			String name = entry.getName();
			if (name.endsWith(".roi")) {
				in.close();
				IJ.runMacro("roiManager(\"Open\", getArgument());", path);
				return null;
			}
			if (name.endsWith(".tif")) {
				imp = openTiff(in, name);
			} else if (name.endsWith(".dcm")) {
				DICOM dcm = new DICOM(in);
				dcm.run(name);
				imp = dcm;
			} else {
				in.close();
				IJ.error("This ZIP archive does not appear to contain a \nTIFF (\".tif\") or DICOM (\".dcm\") file, or ROIs (\".roi\").");
				return null;
			}
		} catch (Exception e) {
			IJ.error("ZipDecoder", ""+e);
			return null;
		}
		File f = new File(path);
		FileInfo fi = imp.getOriginalFileInfo();
		fi.fileFormat = FileInfo.ZIP_ARCHIVE;
		fi.fileName = f.getName();
		fi.directory = f.getParent()+File.separator;
		return imp;
	}
		
	public String getName(String path) {
		int i = path.lastIndexOf('/');
		if (i==-1)
			i = path.lastIndexOf('\\');
		if (i>0)
			return path.substring(i+1);
		else
			return path;
	}
	
	public String getDir(String path) {
		int i = path.lastIndexOf('/');
		if (i==-1)
			i = path.lastIndexOf('\\');
		if (i>0)
			return path.substring(0, i+1);
		else
			return "";
	}

	ImagePlus openTiff2(FileInfo[] info) {
		if (info==null)
			return null;
		ImagePlus imp = null;
		if (IJ.debugMode) // dump tiff tags
			IJ.log(info[0].debugInfo);
		if (info.length>1) { // try to open as stack
			imp = openTiffStack(info);
			if (imp!=null)
				return imp;
		}
		FileOpener fo = new FileOpener(info[0]);
		imp = fo.open(false);
		if (imp==null) return null;
		int[] offsets = info[0].stripOffsets;
		if (offsets!=null&&offsets.length>1 && offsets[offsets.length-1]<offsets[0])
			ij.IJ.run(imp, "Flip Vertically", "stack");
		int c = imp.getNChannels();
		boolean composite = c>1 && info[0].description!=null && info[0].description.indexOf("mode=")!=-1;
		if (c>1 && (imp.getOpenAsHyperStack()||composite) && !imp.isComposite() && imp.getType()!=ImagePlus.COLOR_RGB) {
			int mode = CompositeImage.COLOR;
			if (info[0].description!=null) {
				if (info[0].description.indexOf("mode=composite")!=-1)
					mode = CompositeImage.COMPOSITE;
				else if (info[0].description.indexOf("mode=gray")!=-1)
					mode = CompositeImage.GRAYSCALE;
			}
			imp = new CompositeImage(imp, mode);
		}
		return imp;
	}
	
	/** Attempts to open the specified ROI, returning null if unsuccessful. */
	public Roi openRoi(String path) {
		Roi roi = null;
		RoiDecoder rd = new RoiDecoder(path);
		try {roi = rd.getRoi();}
		catch (IOException e) {
			IJ.error("RoiDecoder", e.getMessage());
			return null;
		}
		return roi;
	}
	
	/** Opens a tab or comma delimited text file in the Results window. */
	public static void openResultsTable(String path) {
		try {
			ResultsTable rt = ResultsTable.open(path);
			if (rt!=null) rt.show("Results");
		} catch(IOException e) {
			IJ.error("Open Results", e.getMessage());
		}
	}
	
	/**
	Attempts to determine the image file type by looking for
	'magic numbers' and the file name extension.
	 */
	public int getFileType(String path) {
		// TODO: Use Bio-Formats for file type identification.
		if (openUsingPlugins && !path.endsWith(".txt") &&  !path.endsWith(".java"))
			return UNKNOWN;
		File file = new File(path);
		String name = file.getName();
		InputStream is;
		byte[] buf = new byte[132];
		try {
			is = new FileInputStream(file);
			is.read(buf, 0, 132);
			is.close();
		} catch (IOException e) {
			return UNKNOWN;
		}
		
		int b0=buf[0]&255, b1=buf[1]&255, b2=buf[2]&255, b3=buf[3]&255;
		//IJ.log("getFileType: "+ name+" "+b0+" "+b1+" "+b2+" "+b3);
		
		 // Combined TIFF and DICOM created by GE Senographe scanners
		if (buf[128]==68 && buf[129]==73 && buf[130]==67 && buf[131]==77
		&& ((b0==73 && b1==73)||(b0==77 && b1==77)))
			return TIFF_AND_DICOM;

		 // Big-endian TIFF ("MM")
		if (name.endsWith(".lsm"))
				return UNKNOWN; // The LSM	Reader plugin opens these files
		if (b0==73 && b1==73 && b2==42 && b3==0 && !(bioformats&&name.endsWith(".flex")))
			return TIFF;

		 // Little-endian TIFF ("II")
		if (b0==77 && b1==77 && b2==0 && b3==42)
			return TIFF;

		 // JPEG
		if (b0==255 && b1==216 && b2==255)
			return JPEG;

		 // GIF ("GIF8")
		if (b0==71 && b1==73 && b2==70 && b3==56)
			return GIF;

		name = name.toLowerCase(Locale.US);

		 // DICOM ("DICM" at offset 128)
		if (buf[128]==68 && buf[129]==73 && buf[130]==67 && buf[131]==77 || name.endsWith(".dcm")) {
			return DICOM;
		}

		// ACR/NEMA with first tag = 00002,00xx or 00008,00xx
		if ((b0==8||b0==2) && b1==0 && b3==0 && !name.endsWith(".spe") && !name.equals("fid"))	
				return DICOM;

		// PGM ("P1", "P4", "P2", "P5", "P3" or "P6")
		if (b0==80&&(b1==49||b1==52||b1==50||b1==53||b1==51||b1==54)&&(b2==10||b2==13||b2==32||b2==9))
			return PGM;

		// Lookup table
		if (name.endsWith(".lut"))
			return LUT;
		
		// PNG
		if (b0==137 && b1==80 && b2==78 && b3==71)
			return PNG;
				
		// ZIP containing a TIFF
		if (name.endsWith(".zip"))
			return ZIP;

		// FITS ("SIMP")
		if ((b0==83 && b1==73 && b2==77 && b3==80) || name.endsWith(".fts.gz") || name.endsWith(".fits.gz"))
			return FITS;
			
		// Java source file, text file or macro
		if (name.endsWith(".java") || name.endsWith(".txt") || name.endsWith(".ijm") || name.endsWith(".js"))
			return JAVA_OR_TEXT;

		// ImageJ, NIH Image, Scion Image for Windows ROI
		if (b0==73 && b1==111) // "Iout"
			return ROI;
			
		// ObjectJ project
		if (name.endsWith(".ojj")) 
			return OJJ;

		// Results table (tab-delimited or comma-separated tabular text)
		if (name.endsWith(".xls") || name.endsWith(".csv")) 
			return TABLE;

		// Text file
		boolean isText = true;
		for (int i=0; i<10; i++) {
		  int c = buf[i]&255;
		  if ((c<32&&c!=9&&c!=10&&c!=13) || c>126) {
			  isText = false;
			  break;
		  }
		}
		if (isText)
		   return TEXT;

		// BMP ("BM")
		if ((b0==66 && b1==77)||name.endsWith(".dib"))
			return BMP;
				
		// AVI
		if (name.endsWith(".avi"))
			return AVI;

		return UNKNOWN;
	}

	/** Returns an IndexColorModel for the image specified by this FileInfo. */
	ColorModel createColorModel(FileInfo fi) {
		if (fi.fileType==FileInfo.COLOR8 && fi.lutSize>0)
			return new IndexColorModel(8, fi.lutSize, fi.reds, fi.greens, fi.blues);
		else
			return LookUpTable.createGrayscaleColorModel(fi.whiteIsZero);
	}

	/** Returns an InputStream for the image described by this FileInfo. */
	InputStream createInputStream(FileInfo fi) throws IOException, MalformedURLException {
		if (fi.inputStream!=null)
			return fi.inputStream;
		else if (fi.url!=null && !fi.url.equals(""))
			return new URL(fi.url+fi.fileName).openStream();
		else {
			File f = new File(fi.directory + fi.fileName);
			if (f==null || f.isDirectory())
				return null;
			else {
				InputStream is = new FileInputStream(f);
				if (fi.compression>=FileInfo.LZW)
					is = new RandomAccessStream(is);
				return is;
			}
		}
	}
	
	void openRGB48(ImagePlus imp) {
			isRGB48 = false;
			int stackSize = imp.getStackSize();
			imp.setDimensions(3, stackSize/3, 1);
			imp = new CompositeImage(imp, CompositeImage.COMPOSITE);
			imp.show();
	}
	
	/** The "Opening: path" status message is not displayed in silent mode. */
	public void setSilentMode(boolean mode) {
		silentMode = mode;
	}

	/** Open all images using HandleExtraFileTypes. Set from
		a macro using setOption("openUsingPlugins", true). */
	public static void setOpenUsingPlugins(boolean b) {
		openUsingPlugins = b;
	}

	/** Returns the state of the openUsingPlugins flag. */
	public static boolean getOpenUsingPlugins() {
		return openUsingPlugins;
	}
	
}
