//package ij.plugin;
import ij.plugin.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.awt.event.*;
import ij.*;
import ij.io.*;
import ij.gui.*;
import ij.process.*;

/** Opens a folder of images as a stack. */
public class Virtual_Stack_Opener implements PlugIn {

	private static boolean grayscale;
	private static double scale = 100.0;
	private int n, start, increment;
	private String filter;
	private FileInfo fi;
	private String info1;

	public void run(String arg) {
		String directory = IJ.getDirectory("Select a Directory");
		if (directory==null)
			return;
		Macro.setOptions(null); // Prevents later use of OpenDialog from reopening the same file

		String[] list = new File(directory).list();
		if (list==null || list.length==0)
			return;
		IJ.register(Virtual_Stack_Opener.class);
		list = sortFileList(list);
		if (list==null) return;
		if (IJ.debugMode) IJ.log("FolderOpener: "+directory+" ("+list.length+" files)");
		int width=0,height=0,type=0;
		VirtualStack stack = null;
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		
		try {
			for (int i=0; i<list.length; i++) {
				ImagePlus imp = new Opener().openImage(directory, list[i]);
				if (imp!=null) {
					width = imp.getWidth();
					height = imp.getHeight();
					type = imp.getType();
					fi = imp.getOriginalFileInfo();
					if (!showDialog(imp, list))
						return;
					break;
				}
			}
			if (width==0) {
				IJ.showMessage("Import Sequence", "This folder does not appear to contain any TIFF,\n"
				+ "JPEG, BMP, DICOM, GIF, FITS or PGM files.");
				return;
			}

			if (n<1)
				n = list.length;
			if (start<1 || start>list.length)
				start = 1;
			if (start+n-1>list.length)
				n = list.length-start+1;
			int filteredImages = n;
			if (filter!=null && (filter.equals("") || filter.equals("*")))
				filter = null;
			if (filter!=null) {
				filteredImages = 0;
				for (int i=start-1; i<start-1+n; i++) {
					if (list[i].indexOf(filter)>=0)
						filteredImages++;
				}
				if (filteredImages==0) {
					IJ.error("None of the "+n+" files contain\n the string '"+filter+"' in their name.");
					return;
				}
			}
			n = filteredImages;
			
			int count = 0;
			int counter = 0;
			ImagePlus imp = null;
			for (int i=start-1; i<list.length; i++) {
				if (filter!=null && (list[i].indexOf(filter)<0))
					continue;
				if ((counter++%increment)!=0)
					continue;
				if (stack==null)
					imp = new Opener().openImage(directory, list[i]);
				if (imp==null) continue;
				if (stack==null) {
					width = imp.getWidth();
					height = imp.getHeight();
					type = imp.getType();
					ColorModel cm = imp.getProcessor().getColorModel();
					stack = new VirtualStack(width, height, cm, directory);
				 }
				count = stack.getSize()+1;
				IJ.showStatus(count+"/"+n);
				IJ.showProgress((double)count/n);
				stack.addSlice(list[i]);
				if (count>=n)
					break;
			}
		} catch(OutOfMemoryError e) {
			IJ.outOfMemory("FolderOpener");
			if (stack!=null) stack.trim();
		}
		if (stack!=null && stack.getSize()>0) {
			ImagePlus imp2 = new ImagePlus("Stack", stack);
			if (imp2.getType()==ImagePlus.GRAY16 || imp2.getType()==ImagePlus.GRAY32)
				imp2.getProcessor().setMinAndMax(min, max);
			imp2.setFileInfo(fi); // saves FileInfo of the first image
			if (imp2.getStackSize()==1 && info1!=null)
				imp2.setProperty("Info", info1);
			imp2.show();
		}
		IJ.showProgress(1.0);
	}
	
	boolean showDialog(ImagePlus imp, String[] list) {
		int fileCount = list.length;
		VirtualOpenerDialog gd = new VirtualOpenerDialog("Sequence Options", imp, list);
		gd.addNumericField("Number of Images:", fileCount, 0);
		gd.addNumericField("Starting Image:", 1, 0);
		gd.addNumericField("Increment:", 1, 0);
		gd.addStringField("File Name Contains:", "");
		gd.addMessage("10000 x 10000 x 1000 (100.3MB)");
		gd.showDialog();
		if (gd.wasCanceled())
			return false;
		n = (int)gd.getNextNumber();
		start = (int)gd.getNextNumber();
		increment = (int)gd.getNextNumber();
		if (increment<1)
			increment = 1;
		filter = gd.getNextString();
		return true;
	}

	String[] sortFileList(String[] rawlist) {
		int count = 0;
		for (int i=0; i< rawlist.length; i++) {
			String name = rawlist[i];
			if (name.startsWith(".")||name.equals("Thumbs.db")||name.endsWith(".txt"))
				rawlist[i] = null;
			else
				count++;
		}
		if (count==0) return null;
		String[] list = rawlist;
		if (count<rawlist.length) {
			list = new String[count];
			int index = 0;
			for (int i=0; i< rawlist.length; i++) {
				if (rawlist[i]!=null)
					list[index++] = rawlist[i];
			}
		}
		int listLength = list.length;
		boolean allSameLength = true;
		int len0 = list[0].length();
		for (int i=0; i<listLength; i++) {
			if (list[i].length()!=len0) {
				allSameLength = false;
				break;
			}
		}
		if (allSameLength)
			{ij.util.StringSorter.sort(list); return list;}
		int maxDigits = 15;		
		String[] list2 = null;	
		char ch;	
		for (int i=0; i<listLength; i++) {
			int len = list[i].length();
			String num = "";
			for (int j=0; j<len; j++) {
				ch = list[i].charAt(j);
				if (ch>=48&&ch<=57) num += ch;
			}
			if (list2==null) list2 = new String[listLength];
			if (num.length()==0) num = "aaaaaa";
			num = "000000000000000" + num; // prepend maxDigits leading zeroes
			num = num.substring(num.length()-maxDigits);
			list2[i] = num + list[i];
		}
		if (list2!=null) {
			ij.util.StringSorter.sort(list2);
			for (int i=0; i<listLength; i++)
				list2[i] = list2[i].substring(maxDigits);
			return list2;	
		} else {
			ij.util.StringSorter.sort(list);
			return list;   
		}	
	}

}

class VirtualOpenerDialog extends GenericDialog {
	ImagePlus imp;
	int fileCount;
	boolean eightBits;
	String saveFilter = "";
	String[] list;

	public VirtualOpenerDialog(String title, ImagePlus imp, String[] list) {
		super(title);
		this.imp = imp;
		this.list = list;
		this.fileCount = list.length;
	}

	protected void setup() {
		setStackInfo();
	}
	
	public void itemStateChanged(ItemEvent e) {
		setStackInfo();
	}
	
	public void textValueChanged(TextEvent e) {
		setStackInfo();
	}

	void setStackInfo() {
		int width = imp.getWidth();
		int height = imp.getHeight();
		int bytesPerPixel = 1;
		int n = getNumber(numberField.elementAt(0));
		int start = getNumber(numberField.elementAt(1));
		int inc = getNumber(numberField.elementAt(2));
		 
		if (n<1)
			n = fileCount;
		if (start<1 || start>fileCount)
			start = 1;
		if (start+n-1>fileCount)
			n = fileCount-start+1;
		if (inc<1)
			inc = 1;
		TextField tf = (TextField)stringField.elementAt(0);
		String filter = tf.getText();
		// IJ.write(nImages+" "+startingImage);
		if (!filter.equals("") && !filter.equals("*")) {
			int n2 = n;
			n = 0;
			for (int i=start-1; i<start-1+n2; i++)
				if (list[i].indexOf(filter)>=0) {
					n++;
					//IJ.write(n+" "+list[i]);
				}
			saveFilter = filter;
		}
		switch (imp.getType()) {
			case ImagePlus.GRAY16:
				bytesPerPixel=2;break;
			case ImagePlus.COLOR_RGB:
			case ImagePlus.GRAY32:
				bytesPerPixel=4; break;
		}
		if (eightBits)
			bytesPerPixel = 1;
		int n2 = n/inc;
		if (n2<0)
			n2 = 0;
		double size = ((double)width*height*n2*bytesPerPixel)/(1024*1024);
		((Label)theLabel).setText(width+" x "+height+" x "+n2+" ("+IJ.d2s(size,1)+"MB)");
	}

	public int getNumber(Object field) {
		TextField tf = (TextField)field;
		String theText = tf.getText();
		double value;
		Double d;
		try {d = new Double(theText);}
		catch (NumberFormatException e){
			d = null;
		}
		if (d!=null)
			return (int)d.doubleValue();
		else
			return 0;
	  }

}


/**
This class represents an array of disk-resident images.
*/
class VirtualStack extends ImageStack{
	static final int INITIAL_SIZE = 100;
	String path;
	int nSlices;
	String[] names;
	
	/** Creates a new, empty virtual stack. */
	public VirtualStack(int width, int height, ColorModel cm, String path) {
		super(width, height, cm);
		this.path = path;
		names = new String[INITIAL_SIZE];
		//IJ.log("VirtualStack: "+path);
	}

	 /** Adds an image to the end of the stack. */
	public void addSlice(String name) {
		if (name==null) 
			throw new IllegalArgumentException("'name' is null!");
		nSlices++;
	   //IJ.log("addSlice: "+nSlices+"	"+name);
	   if (nSlices==names.length) {
			String[] tmp = new String[nSlices*2];
			System.arraycopy(names, 0, tmp, 0, nSlices);
			names = tmp;
		}
		names[nSlices-1] = name;
	}

   /** Does nothing. */
	public void addSlice(String sliceLabel, Object pixels) {
	}

	/** Does nothing.. */
	public void addSlice(String sliceLabel, ImageProcessor ip) {
	}
	
	/** Does noting. */
	public void addSlice(String sliceLabel, ImageProcessor ip, int n) {
	}

	/** Deletes the specified slice, were 1<=n<=nslices. */
	public void deleteSlice(int n) {
		if (n<1 || n>nSlices)
			throw new IllegalArgumentException("Argument out of range: "+n);
			if (nSlices<1)
				return;
			for (int i=n; i<nSlices; i++)
				names[i-1] = names[i];
			names[nSlices-1] = null;
			nSlices--;
		}
	
	/** Deletes the last slice in the stack. */
	public void deleteLastSlice() {
		if (nSlices>0)
			deleteSlice(nSlices);
	}
	   
   /** Returns the pixel array for the specified slice, were 1<=n<=nslices. */
	public Object getPixels(int n) {
		ImageProcessor ip = getProcessor(n);
		if (ip!=null)
			return ip.getPixels();
		else
			return null;
	}		
	
	 /** Assigns a pixel array to the specified slice,
		were 1<=n<=nslices. */
	public void setPixels(Object pixels, int n) {
	}

   /** Returns an ImageProcessor for the specified slice,
		were 1<=n<=nslices. Returns null if the stack is empty.
	*/
	public ImageProcessor getProcessor(int n) {
		//IJ.log("getProcessor: "+n+"  "+names[n-1]);
		ImagePlus imp = new Opener().openImage(path, names[n-1]);
		if (imp!=null) {
			int w = imp.getWidth();
			int h = imp.getHeight();
			int type = imp.getType();
			ColorModel cm = imp.getProcessor().getColorModel();
		} else
			return null;
		return imp.getProcessor();
	 }
 
	 /** Returns the number of slices in this stack. */
	public int getSize() {
		return nSlices;
	}

	/** Returns the file name of the Nth image. */
	public String getSliceLabel(int n) {
		 return names[n-1];
	}
	
	/** Returns null. */
	public Object[] getImageArray() {
		return null;
	}

   /** Does nothing. */
	public void setSliceLabel(String label, int n) {
	}

	/** Always return true. */
	public boolean isVirtual() {
		return true;
	}

   /** Does nothing. */
	public void trim() {
	}
		
}
