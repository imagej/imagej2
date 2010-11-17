package ij.plugin;
import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.io.*;
import java.awt.*;
import java.io.*;
import java.util.*;

/** This plugin opens images specified by list of file paths as a virtual stack.
	It implements the File/Import/Stack From List command. */
public class ListVirtualStack extends VirtualStack implements PlugIn {
	private static boolean virtual;
	private String[] list;
	private String[] labels;
	private int nImages;
	private int imageWidth, imageHeight;

	public void run(String arg) {
		OpenDialog  od = new OpenDialog("Open Image List", arg);
		String name = od.getFileName();
		if (name==null) return;
		String  dir = od.getDirectory();
		list = open(dir+name);
		nImages = list.length;
		labels = new String[nImages];
		//for (int i=0; i<list.length; i++)
		//	IJ.log(i+"  "+list[i]);
		if (list.length==0) {
			IJ.error("Stack From List", "The file path list is empty");
			return;
		}
		File f = new File(list[0]);
		if (!f.exists()) {
			IJ.error("Stack From List", "The first file on the list does not exist:\n \n"+list[0]);
			return;
		}
		ImagePlus imp = IJ.openImage(list[0]);
		if (imp==null) return;
		imageWidth = imp.getWidth();
		imageHeight = imp.getHeight();
		setBitDepth(imp.getBitDepth());
		ImageStack stack = this;
		if (!showDialog(imp)) return;
		if (!virtual)
			stack = convertToRealStack(imp);
		ImagePlus imp2 = new ImagePlus(name, stack);
		imp2.setCalibration(imp.getCalibration());
		imp2.show();
	}
	
	boolean showDialog(ImagePlus imp) {
		double bytesPerPixel = imp.getBytesPerPixel();
		double size = (imageWidth*imageHeight*bytesPerPixel)/(1024.0*1024.0);
		int digits = size*getSize()<10.0?1:0;
		String size1 = IJ.d2s(size*getSize(), digits)+" MB";
		String size2 = IJ.d2s(size,1)+" MB";
		GenericDialog gd = new GenericDialog("Open Stack From List");
		gd.addCheckbox("Use Virtual Stack", virtual);
		gd.addMessage("This "+imageWidth+"x"+imageHeight+"x"+getSize()+" stack will require "+size1+",\n or "+size2+" if opened as a virtual stack.");
		gd.showDialog();
		if (gd.wasCanceled()) return false;
		virtual = gd.getNextBoolean();
		return true;
	}
	
	ImageStack convertToRealStack(ImagePlus imp) {
		ImageStack stack2 = new ImageStack(imageWidth, imageHeight, imp.getProcessor().getColorModel());
		int n = this.getSize();
		for (int i=1; i<=this.getSize(); i++) {
			IJ.showProgress(i, n);
			IJ.showStatus("Opening: "+i+"/"+n);
			ImageProcessor ip2 = this.getProcessor(i);
			if (ip2!=null)
				stack2.addSlice(this.getSliceLabel(i), ip2);
		}
		return stack2;
	}
	
	String[] open(String path) {
		Vector v = new Vector();
		File file = new File(path);
		try {
			BufferedReader r = new BufferedReader(new FileReader(file));
			while (true) {
				String s=r.readLine();
				if (s==null || s.equals("") || s.startsWith(" "))
					break;
				else
					v.addElement(s);
			}
			r.close();
    		String[] list = new String[v.size()];
			v.copyInto((String[])list);
    		return list;
		}
		catch (Exception e) {
			IJ.error("Open List Error \n\""+e.getMessage()+"\"\n");
		}
		return null;
	}

	/** Deletes the specified image, were 1<=n<=nslices. */
	public void deleteSlice(int n) {
		if (n<1 || n>nImages)
			throw new IllegalArgumentException("Argument out of range: "+n);
		if (nImages<1) return;
		for (int i=n; i<nImages; i++)
			list[i-1] = list[i];
		list[nImages-1] = null;
		nImages--;
	}
	
	/** Returns an ImageProcessor for the specified slice,
		were 1<=n<=nslices. Returns null if the stack is empty.
	*/
	public ImageProcessor getProcessor(int n) {
		if (n<1 || n>nImages)
			throw new IllegalArgumentException("Argument out of range: "+n);
		ImagePlus imp = IJ.openImage(list[n-1]);
		if (imp!=null) {
			labels[n-1] = (new File(list[n-1])).getName()+"\n"+(String)imp.getProperty("Info");
			ImageProcessor ip =  imp.getProcessor();
			int bitDepth = getBitDepth();
			if (imp.getBitDepth()!=bitDepth) {
				switch (bitDepth) {
					case 8: ip=ip.convertToByte(true); break;
					case 16: ip=ip.convertToShort(true); break;
					case 24:  ip=ip.convertToRGB(); break;
					case 32: ip=ip.convertToFloat(); break;
				}
			}
			if (ip.getWidth()!=imageWidth || ip.getHeight()!=imageHeight)
			ip = ip.resize(imageWidth, imageHeight);
			return ip;
		} else {
				ImageProcessor ip = null;
				switch (getBitDepth()) {
					case 8: ip=new ByteProcessor(imageWidth,imageHeight); break;
					case 16: ip=new ShortProcessor(imageWidth,imageHeight); break;
					case 24:  ip=new ColorProcessor(imageWidth,imageHeight); break;
					case 32: ip=new FloatProcessor(imageWidth,imageHeight); break;
				}
			return ip;
		}
	 }
 
	 /** Returns the number of images in this stack. */
	public int getSize() {
		return nImages;
	}

	/** Returns the name of the specified image. */
	public String getSliceLabel(int n) {
		if (n<1 || n>nImages)
			throw new IllegalArgumentException("Argument out of range: "+n);
		if (labels[n-1]!=null)
			return labels[n-1];
		else
			return (new File(list[n-1])).getName();
	}
	
	public int getWidth() {
		return imageWidth;
	}

	public int getHeight() {
		return imageHeight;
	}


}
