package ijx.stack;
import ijx.plugin.api.PlugIn;
import ijx.process.ImageProcessor;
import ijx.process.FloatProcessor;
import ijx.process.ColorProcessor;
import ijx.process.ByteProcessor;
import ijx.process.ShortProcessor;
import ijx.gui.dialog.GenericDialog;
import ijx.Macro;
import ijx.WindowManager;
import ijx.IJ;
import ij.*;


import ijx.measure.Calibration;
import ijx.io.FileInfo;
import ijx.IjxImagePlus;
import ijx.IjxImageStack;


/** Implements the Image/Stacks/Images to Stack" command. */
public class ImagesToStack implements PlugIn {
	private static final int rgb = 33;
	private static final int COPY_CENTER=0, COPY_TOP_LEFT=1, SCALE_SMALL=2, SCALE_LARGE=3;
	private static final String[] methods = {"Copy (center)", "Copy (top-left)", "Scale (smallest)", "Scale (largest)"};
	private static int method = COPY_CENTER;
	private static boolean bicubic;
	private static boolean keep;
	private static boolean titlesAsLabels = true;
	private String filter;
	private int width, height;
	private int maxWidth, maxHeight;
	private int minWidth, minHeight;
	private int minSize, maxSize;
	private Calibration cal2;
	private int stackType;
	private IjxImagePlus[] image;
	private String name = "Stack";

	public void run(String arg) {
    	convertImagesToStack();
	}

	public void convertImagesToStack() {
		boolean scale = false;
		int[] wList = WindowManager.getIDList();
		if (wList==null) {
			IJ.error("No images are open.");
			return;
		}

		int count = 0;
		image = new IjxImagePlus[wList.length];
		for (int i=0; i<wList.length; i++) {
			IjxImagePlus imp = WindowManager.getImage(wList[i]);
			if (imp.getStackSize()==1)
				image[count++] = imp;
		}		
		if (count<2) {
			IJ.error("Images to Stack", "There must be at least two open images.");
			return;
		}

		filter = null;
		count = findMinMaxSize(count);
		boolean sizesDiffer = width!=minWidth||height!=minHeight;
		boolean showDialog = true;
		String macroOptions = Macro.getOptions();
		if (IJ.macroRunning() && macroOptions==null) {
			if (sizesDiffer) {
				IJ.error("Images are not all the same size");
				return;
			} 
			showDialog = false;
		}
		if (showDialog) {
			GenericDialog gd = new GenericDialog("Images to Stack");
			if (sizesDiffer) {
				String msg = "The "+count+" images differ in size (smallest="+minWidth+"x"+minHeight
				+",\nlargest="+maxWidth+"x"+maxHeight+"). They will be converted\nto a stack using the specified method.";
				gd.setInsets(0,0,5);
				gd.addMessage(msg);
				gd.addChoice("Method:", methods, methods[method]);
			}
			gd.addStringField("Name:", name, 12);
			gd.addStringField("Title Contains:", "", 12);
			if (sizesDiffer)
				gd.addCheckbox("Bicubic Interpolation", bicubic);
			gd.addCheckbox("Use Titles as Labels", titlesAsLabels);
			gd.addCheckbox("Keep Source Images", keep);
			gd.showDialog();
			if (gd.wasCanceled()) return;
			if (sizesDiffer)
				method = gd.getNextChoiceIndex();
			name = gd.getNextString();
			filter = gd.getNextString();
			if (sizesDiffer)
				bicubic = gd.getNextBoolean();
			titlesAsLabels = gd.getNextBoolean();
			keep = gd.getNextBoolean();
			if (filter!=null && (filter.equals("") || filter.equals("*")))
				filter = null;
			if (filter!=null) {
				count = findMinMaxSize(count);
				if (count==0) {
					IJ.error("Images to Stack", "None of the images have a title containing \""+filter+"\"");
				}
			}
		} else
			keep = false;
		if (method==SCALE_SMALL) {
			width = minWidth;
			height = minHeight;
		} else if (method==SCALE_LARGE) {
			width = maxWidth;
			height = maxHeight;
		}
		
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		IjxImageStack stack = IJ.getFactory().newImageStack(width, height);
		FileInfo fi = image[0].getOriginalFileInfo();
		if (fi!=null && fi.directory==null) fi = null;
		for (int i=0; i<count; i++) {
			ImageProcessor ip = image[i].getProcessor();
			if (ip==null) break;
			if (ip.getMin()<min) min = ip.getMin();
			if (ip.getMax()>max) max = ip.getMax();
            String label = titlesAsLabels?image[i].getTitle():null;
            if (label!=null) {
            	String info = (String)image[i].getProperty("Info");
				if (info!=null) label += "\n" + info;
			}
            if (fi!=null) {
				FileInfo fi2 = image[i].getOriginalFileInfo();
				if (fi2!=null && !fi.directory.equals(fi2.directory))
					fi = null;
            }
            switch (stackType) {
            	case 16: ip = ip.convertToShort(false); break;
            	case 32: ip = ip.convertToFloat(); break;
            	case rgb: ip = ip.convertToRGB(); break;
            	default: break;
            }
            if (ip.getWidth()!=width||ip.getHeight()!=height) {
 				switch (method) {
					case COPY_TOP_LEFT: case COPY_CENTER:
						ImageProcessor ip2 = null;
						switch (stackType) {
							case 8: ip2 = new ByteProcessor(width, height); break;
							case 16: ip2 = new ShortProcessor(width, height); break;
							case 32: ip2 = new FloatProcessor(width, height); break;
							case rgb: ip2 = new ColorProcessor(width, height); break;
						}
						int xoff=0, yoff=0;
						if (method==COPY_CENTER) {
							xoff = (width-ip.getWidth())/2;
							yoff = (height-ip.getHeight())/2;
						}
 						ip2.insert(ip, xoff, yoff);
						ip = ip2;
						break;
					case SCALE_SMALL: case SCALE_LARGE:
						ip.setInterpolationMethod((bicubic?ImageProcessor.BICUBIC:ImageProcessor.BILINEAR));
						ip.resetRoi();
						ip = ip.resize(width, height);
						break;
				}
            } else if (keep)
            	ip = ip.duplicate();
            stack.addSlice(label, ip);
            if (!keep) {
				image[i].setChanged(false);
				image[i].close();
			}
		}
		if (stack.getSize()==0) return;
		IjxImagePlus imp = IJ.getFactory().newImagePlus(name, stack);
		if (stackType==16 || stackType==32)
			imp.getProcessor().setMinAndMax(min, max);
		if (cal2!=null)
			imp.setCalibration(cal2);
		if (fi!=null) {
			fi.fileName = "";
			fi.nImages = imp.getStackSize();
			imp.setFileInfo(fi);
		}
		imp.show();
	}
	
	final int findMinMaxSize(int count) {
		int index = 0;
		stackType = 8;
		width = 0;
		height = 0;
		cal2 = image[0].getCalibration();
		maxWidth = 0;
		maxHeight = 0;
		minWidth = Integer.MAX_VALUE;
		minHeight = Integer.MAX_VALUE;
		minSize = Integer.MAX_VALUE;
		maxSize = 0;
		for (int i=0; i<count; i++) {
			if (exclude(image[i].getTitle())) continue;
			if (image[i].getType()==IjxImagePlus.COLOR_256)
				stackType = rgb;
			int type = image[i].getBitDepth();
			if (type==24) type = rgb;
			if (type>stackType) stackType = type;
			int w=image[i].getWidth(), h=image[i].getHeight();
			if (w>width) width = w;
			if (h>height) height = h;
			int size = w*h;
			if (size<minSize) {
				minSize = size;
				minWidth = w;
				minHeight = h;
			}
			if (size>maxSize) {
				maxSize = size;
				maxWidth = w;
				maxHeight = h;
			}
			Calibration cal = image[i].getCalibration();
			if (!image[i].getCalibration().equals(cal2))
				cal2 = null;
			image[index++] = image[i];
		}
		return index;
	}

	final boolean exclude(String title) {
		return filter!=null && title!=null && title.indexOf(filter)==-1;
	}
	
}

