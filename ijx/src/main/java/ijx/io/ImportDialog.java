package ijx.io;

import ijx.process.ImageProcessor;
import ijx.gui.dialog.GenericDialog;
import ijx.Prefs;
import ijx.IJ;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;



import imagej.util.StringSorter;
import ijx.plugin.frame.Recorder;
import ijx.plugin.FolderOpener;
import ijx.stack.FileInfoVirtualStack;
import ijx.measure.Calibration;
import ijx.IjxImagePlus;
import ijx.IjxImageStack;
import ijx.SavesPrefs;
import org.openide.util.lookup.ServiceProvider;


/** This is a dialog box used to imports raw 8, 16, 24 and 32-bit images. */

@ServiceProvider(service=SavesPrefs.class)

public class ImportDialog implements SavesPrefs {
	private String fileName;
    private String directory;
	static final String TYPE = "raw.type";
	static final String WIDTH = "raw.width";
	static final String HEIGHT = "raw.height";
	static final String OFFSET = "raw.offset";
	static final String N = "raw.n";
	static final String GAP = "raw.gap";
	static final String OPTIONS = "raw.options";
	static final int WHITE_IS_ZERO = 1;
	static final int INTEL_BYTE_ORDER = 2;
	static final int OPEN_ALL = 4;
	
    // default settings
    private static int choiceSelection = Prefs.getInt(TYPE,0);
    private static int width = Prefs.getInt(WIDTH,512);
    private static int height = Prefs.getInt(HEIGHT,512);
    private static long offset = Prefs.getInt(OFFSET,0);
    private static int nImages = Prefs.getInt(N,1);
    private static int gapBetweenImages = Prefs.getInt(GAP,0);
	private static int options;
    private static boolean whiteIsZero,intelByteOrder;
    private static boolean virtual;
    private boolean openAll;
    private static FileInfo lastFileInfo;
    private static String[] types = {"8-bit", "16-bit Signed", "16-bit Unsigned",
		"32-bit Signed", "32-bit Unsigned", "32-bit Real", "64-bit Real", "24-bit RGB", 
		"24-bit RGB Planar", "24-bit BGR", "24-bit Integer", "32-bit ARGB", "32-bit ABGR", "1-bit Bitmap"};
    	
    static {
    	options = Prefs.getInt(OPTIONS,0);
    	whiteIsZero = (options&WHITE_IS_ZERO)!=0;
    	intelByteOrder = (options&INTEL_BYTE_ORDER)!=0;
    	//openAll = (options&OPEN_ALL)!=0;
    }
	
    public ImportDialog(String fileName, String directory) {
        this.fileName = fileName;
        this.directory = directory;
		IJ.showStatus("Importing: " + fileName);
	}

    public ImportDialog() {
	}

	boolean showDialog() {
		if (choiceSelection>=types.length)
			choiceSelection = 0;
		GenericDialog gd = new GenericDialog("Import...", IJ.getTopComponentFrame());
		gd.addChoice("Image type:", types, types[choiceSelection]);
		gd.addNumericField("Width:", width, 0, 6, "pixels");
		gd.addNumericField("Height:", height, 0, 6, "pixels");
		gd.addNumericField("Offset to first image:", offset, 0, 6, "bytes");
		gd.addNumericField("Number of images:", nImages, 0, 6, null);
		gd.addNumericField("Gap between images:", gapBetweenImages, 0, 6, "bytes");
		gd.addCheckbox("White is zero", whiteIsZero);
		gd.addCheckbox("Little-endian byte order", intelByteOrder);
		gd.addCheckbox("Open all files in folder", openAll);
		gd.addCheckbox("Use virtual stack", virtual);
		gd.addHelp(IJ.URL+"/docs/menus/file.html#raw");
		gd.showDialog();
		if (gd.wasCanceled())
			return false;
		choiceSelection = gd.getNextChoiceIndex();
		width = (int)gd.getNextNumber();
		height = (int)gd.getNextNumber();
		offset = (long)gd.getNextNumber();
		nImages = (int)gd.getNextNumber();
		gapBetweenImages = (int)gd.getNextNumber();
		whiteIsZero = gd.getNextBoolean();
		intelByteOrder = gd.getNextBoolean();
		openAll = gd.getNextBoolean();
		virtual = gd.getNextBoolean();
		IJ.register(ImportDialog.class);
		return true;
	}
	
	/** Opens all the images in the directory. */
	void openAll(String[] list, FileInfo fi) {
		//StringSorter.sort(list);
		FolderOpener fo = new FolderOpener();
		list = fo.trimFileList(list);
		list = fo.sortFileList(list);
		if (list==null) return;
		IjxImageStack stack=null;
		IjxImagePlus imp=null;
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		for (int i=0; i<list.length; i++) {
			if (list[i].startsWith("."))
				continue;
			fi.fileName = list[i];
			imp = new FileOpener(fi).open(false);
			if (imp==null)
				IJ.log(list[i] + ": unable to open");
			else {
				if (stack==null)
					stack = imp.createEmptyStack();	
				try {
					ImageProcessor ip = imp.getProcessor();
					if (ip.getMin()<min) min = ip.getMin();
					if (ip.getMax()>max) max = ip.getMax();
					stack.addSlice(list[i], ip);
				}
				catch(OutOfMemoryError e) {
					IJ.outOfMemory("OpenAll");
					stack.trim();
					break;
				}
				IJ.showStatus((stack.getSize()+1) + ": " + list[i]);
			}
		}
		if (stack!=null) {
			imp = IJ.getFactory().newImagePlus("Imported Stack", stack);
			if (imp.getBitDepth()==16 || imp.getBitDepth()==32)
				imp.getProcessor().setMinAndMax(min, max);
                Calibration cal = imp.getCalibration();
                if (fi.fileType==FileInfo.GRAY16_SIGNED)
                	cal.setSigned16BitCalibration();
			imp.show();
		}
	}
	
	/** Displays the dialog and opens the specified image or images.
		Does nothing if the dialog is canceled. */
	public void openImage() {
		FileInfo fi = getFileInfo();
		if (fi==null) return;
		if (openAll) {
			if (virtual) {
				virtual = false;
				IJ.error("Import Raw", "\"Open All\" does not currently support virtual stacks");
				return;
			}
			String[] list = new File(directory).list();
			if (list==null) return;
			openAll(list, fi);
		} else if (virtual)
			new FileInfoVirtualStack(fi);
		else {
			FileOpener fo = new FileOpener(fi);
			IjxImagePlus imp = fo.open(false);
			if (imp!=null) {
				imp.show();
				int n = imp.getStackSize();
				if (n>1) {
					imp.setSlice(n/2);
					ImageProcessor ip = imp.getProcessor();
					ip.resetMinAndMax();
					imp.setDisplayRange(ip.getMin(),ip.getMax());
				}
			}
		}
	}

	/** Displays the dialog and returns a FileInfo object that can be used to
		open the image. Returns null if the dialog is canceled. The fileName 
		and directory fields are null if the no argument constructor was used. */
	public FileInfo getFileInfo() {
		if (!showDialog())
			return null;
		String imageType = types[choiceSelection];
		FileInfo fi = new FileInfo();
		fi.fileFormat = fi.RAW;
		fi.fileName = fileName;
		fi.directory = directory;
		fi.width = width;
		fi.height = height;
		if (offset>2147483647)
			fi.longOffset = offset;
		else
			fi.offset = (int)offset;
		fi.nImages = nImages;
		fi.gapBetweenImages = gapBetweenImages;
		fi.intelByteOrder = intelByteOrder;
		fi.whiteIsZero = whiteIsZero;
		if (imageType.equals("8-bit"))
			fi.fileType = FileInfo.GRAY8;
		else if (imageType.equals("16-bit Signed"))
			fi.fileType = FileInfo.GRAY16_SIGNED;
		else if (imageType.equals("16-bit Unsigned"))
			fi.fileType = FileInfo.GRAY16_UNSIGNED;
		else if (imageType.equals("32-bit Signed"))
			fi.fileType = FileInfo.GRAY32_INT;
		else if (imageType.equals("32-bit Unsigned"))
			fi.fileType = FileInfo.GRAY32_UNSIGNED;
		else if (imageType.equals("32-bit Real"))
			fi.fileType = FileInfo.GRAY32_FLOAT;
		else if (imageType.equals("64-bit Real"))
			fi.fileType = FileInfo.GRAY64_FLOAT;
		else if (imageType.equals("24-bit RGB"))
			fi.fileType = FileInfo.RGB;
		else if (imageType.equals("24-bit RGB Planar"))
			fi.fileType = FileInfo.RGB_PLANAR;
		else if (imageType.equals("24-bit BGR"))
			fi.fileType = FileInfo.BGR;
		else if (imageType.equals("24-bit Integer"))
			fi.fileType = FileInfo.GRAY24_UNSIGNED;
		else if (imageType.equals("32-bit ARGB"))
			fi.fileType = FileInfo.ARGB;
		else if (imageType.equals("32-bit ABGR"))
			fi.fileType = FileInfo.ABGR;
		else if (imageType.equals("1-bit Bitmap"))
			fi.fileType = FileInfo.BITMAP;
		else
			fi.fileType = FileInfo.GRAY8;
		if (IJ.debugMode) IJ.log("ImportDialog: "+fi);
		lastFileInfo = (FileInfo)fi.clone();
		return fi;
	}

	/** Called once when ImageJ quits. */
	public void savePreferences(Properties prefs) {
		prefs.put(TYPE, Integer.toString(choiceSelection));
		prefs.put(WIDTH, Integer.toString(width));
		prefs.put(HEIGHT, Integer.toString(height));
		prefs.put(OFFSET, Integer.toString(offset>2147483647?0:(int)offset));
		prefs.put(N, Integer.toString(nImages));
		prefs.put(GAP, Integer.toString(gapBetweenImages));
		int options = 0;
		if (whiteIsZero)
			options |= WHITE_IS_ZERO;
		if (intelByteOrder)
			options |= INTEL_BYTE_ORDER;
		//if (openAll)
		//	options |= OPEN_ALL;
		prefs.put(OPTIONS, Integer.toString(options));
	}
	
	/** Returns the FileInfo object used to import the last raw image,
		or null if a raw image has not been imported. */
	public static FileInfo getLastFileInfo() {
		return lastFileInfo;
	}

}
