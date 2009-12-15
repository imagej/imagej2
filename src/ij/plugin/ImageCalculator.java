package ij.plugin;
import ijx.gui.IjxImageWindow;
import ijx.IjxImagePlus;
import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.plugin.filter.*;
import ij.measure.Calibration;
import ij.plugin.frame.Recorder;
import ij.macro.Interpreter;
import ijx.IjxImageStack;

/** This plugin implements the Process/Image Calculator command. */
public class ImageCalculator implements PlugIn {

	private static String[] operators = {"Add","Subtract","Multiply","Divide", "AND", "OR", "XOR", "Min", "Max", "Average", "Difference", "Copy", "Transparent-zero"};
	private static String[] lcOperators = {"add","sub","mul","div", "and", "or", "xor", "min", "max", "ave", "diff", "copy", "zero"};
	private static int operator;
	private static String title1 = "";
	private static String title2 = "";
	private static boolean createWindow = true;
	private static boolean floatResult;
	private boolean processStack;
	
	public void run(String arg) {
		int[] wList = WindowManager.getIDList();
		if (wList==null) {
			IJ.noImage();
			return;
		}
		IJ.register(ImageCalculator.class);
		String[] titles = new String[wList.length];
		for (int i=0; i<wList.length; i++) {
			IjxImagePlus imp = WindowManager.getImage(wList[i]);
			if (imp!=null)
				titles[i] = imp.getTitle();
			else
				titles[i] = "";
		}
		GenericDialog gd = new GenericDialog("Image Calculator", IJ.getTopComponentFrame());
		String defaultItem;
		if (title1.equals(""))
			defaultItem = titles[0];
		else
			defaultItem = title1;
		gd.addChoice("Image1:", titles, defaultItem);
		gd.addChoice("Operation:", operators, operators[operator]);
		if (title2.equals(""))
			defaultItem = titles[0];
		else
			defaultItem = title2;
		gd.addChoice("Image2:", titles, defaultItem);
		//gd.addStringField("Result:", "Result", 10);
		gd.addCheckbox("Create New Window", createWindow);
		gd.addCheckbox("32-bit Result", floatResult);
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		int index1 = gd.getNextChoiceIndex();
		title1 = titles[index1];
		operator = gd.getNextChoiceIndex();
		int index2 = gd.getNextChoiceIndex();
		//String resultTitle = gd.getNextString();
		createWindow = gd.getNextBoolean();
		floatResult = gd.getNextBoolean();		
		title2 = titles[index2];
		IjxImagePlus img1 = WindowManager.getImage(wList[index1]);
		IjxImagePlus img2 = WindowManager.getImage(wList[index2]);
		calculate(img1, img2, false);
	}
	
	public void calculate(String params, IjxImagePlus img1, IjxImagePlus img2) {
		if (img1==null || img2==null || params==null) return;
		params = params.toLowerCase();
		int op= -1;
		if  (params.indexOf("xor")!=-1)
			op = 6;
		if (op==-1) {
			for (int i=0; i<lcOperators.length; i++) {
				if (params.indexOf(lcOperators[i])!=-1) {
					op = i;
					break;
				}
			}
		}
		if (op==-1)
			{IJ.error("Image Calclulator", "No valid operator"); return;}
		operator = op;
		createWindow = params.indexOf("create")!=-1;
		floatResult= params.indexOf("32")!=-1 || params.indexOf("float")!=-1;
		processStack = params.indexOf("stack")!=-1;
		calculate(img1, img2, true);
	}
		
	void calculate(IjxImagePlus img1, IjxImagePlus img2, boolean apiCall) {
		if (img1.getCalibration().isSigned16Bit() || img2.getCalibration().isSigned16Bit())
			floatResult = true;
		if (floatResult)
			createWindow = true;
		int size1 = img1.getStackSize();
		int size2 = img2.getStackSize();
		if (apiCall) {
			if (processStack && (size1>1||size2>1))
				doStackOperation(img1, img2);
			else
				doOperation(img1, img2);
			return;
		}
		boolean stackOp = false;
		if (size1>1) {
			int result = IJ.setupDialog(img1, 0);
			if (result==IjxPlugInFilter.DONE)
				return;
			if (result==IjxPlugInFilter.DOES_STACKS) {
				doStackOperation(img1, img2);
				stackOp = true;
			} else
				doOperation(img1, img2);
		} else
			doOperation(img1, img2);
		if (Recorder.record) {
			String params = operators[operator];
			if (createWindow) params += " create";
			if (floatResult) params += " 32-bit";
			if (stackOp) params += " stack";
			Recorder.record("imageCalculator", params, img1.getTitle(), img2.getTitle());
		}
	}

	/** img1 = img2 op img2 (e.g. img1 = img2/img1) */
	void doStackOperation(IjxImagePlus img1, IjxImagePlus img2) {
		int size1 = img1.getStackSize();
		int size2 = img2.getStackSize();
		if (size1>1 && size2>1 && size1!=size2) {
			IJ.error("Image Calculator", "'Image1' and 'image2' must be stacks with the same\nnumber of slices, or 'image2' must be a single image.");
			return;
		}
		if (createWindow) {
			img1 = duplicateStack(img1);
			if (img1==null) {
				IJ.error("Calculator", "Out of memory");
				return;
			}
			img1.show();
		}
		int mode = getBlitterMode();
		IjxImageWindow win = img1.getWindow();
		if (win!=null)
			WindowManager.setCurrentWindow(win);
		Undo.reset();
		IjxImageStack stack1 = img1.getStack();
		StackProcessor sp = new StackProcessor(stack1, img1.getProcessor());
		try {
			if (size2==1)
				sp.copyBits(img2.getProcessor(), 0, 0, mode);
			else
				sp.copyBits(img2.getStack(), 0, 0, mode);
		}
		catch (IllegalArgumentException e) {
			IJ.error("\""+img1.getTitle()+"\": "+e.getMessage());
			return;
		}
		img1.setStack(null, stack1);
		if (img1.getType()!=ImagePlus.GRAY8) {
			img1.getProcessor().resetMinAndMax();
		}
		img1.updateAndDraw();
	}

	void doOperation(IjxImagePlus img1, IjxImagePlus img2) {
		int mode = getBlitterMode();
		ImageProcessor ip1 = img1.getProcessor();
		ImageProcessor ip2 = img2.getProcessor();
		Calibration cal1 = img1.getCalibration();
		Calibration cal2 = img2.getCalibration();
		if (createWindow)
			ip1 = createNewImage(ip1, ip2);
		else {
			IjxImageWindow win = img1.getWindow();
			if (win!=null)
				WindowManager.setCurrentWindow(win);
			ip1.snapshot();
			Undo.setup(Undo.FILTER, img1);
		}
		if (floatResult) ip2 = ip2.convertToFloat();
		try {
			ip1.copyBits(ip2, 0, 0, mode);
		}
		catch (IllegalArgumentException e) {
			IJ.error("\""+img1.getTitle()+"\": "+e.getMessage());
			return;
		}
		if (!(ip1 instanceof ByteProcessor))
			ip1.resetMinAndMax();
		if (createWindow) {
			IjxImagePlus img3 = IJ.getFactory().newImagePlus("Result of "+img1.getShortTitle(), ip1);
			img3.setCalibration(cal1);
			img3.show();
		} else
			img1.updateAndDraw();
	}

	ImageProcessor createNewImage(ImageProcessor ip1, ImageProcessor ip2) {
		int width = Math.min(ip1.getWidth(), ip2.getWidth());
		int height = Math.min(ip1.getHeight(), ip2.getHeight());
		ImageProcessor ip3 = ip1.createProcessor(width, height);
		if (floatResult) {
			ip1 = ip1.convertToFloat();
			ip3 = ip3.convertToFloat();
		}
		ip3.insert(ip1, 0, 0);
		return ip3;
	}

	private int getBlitterMode() {
		int mode=0;
		switch (operator) {
			case 0: mode = Blitter.ADD; break;
			case 1: mode = Blitter.SUBTRACT; break;
			case 2: mode = Blitter.MULTIPLY; break;
			case 3: mode = Blitter.DIVIDE; break;
			case 4: mode = Blitter.AND; break;
			case 5: mode = Blitter.OR; break;
			case 6: mode = Blitter.XOR; break;
			case 7: mode = Blitter.MIN; break;
			case 8: mode = Blitter.MAX; break;
			case 9: mode = Blitter.AVERAGE; break;
			case 10: mode = Blitter.DIFFERENCE; break;
			case 11: mode = Blitter.COPY; break;
			case 12: mode = Blitter.COPY_ZERO_TRANSPARENT; break;
		}
		return mode;
	}
	
	IjxImagePlus duplicateStack(IjxImagePlus img1) {
		Calibration cal = img1.getCalibration();
		IjxImageStack stack1 = img1.getStack();
		int width = stack1.getWidth();
		int height = stack1.getHeight();
		int n = stack1.getSize();
		IjxImageStack stack2 = img1.createEmptyStack();
		try {
			for (int i=1; i<=n; i++) {
				ImageProcessor ip1 = stack1.getProcessor(i);
				ip1.resetRoi(); 
				ImageProcessor ip2 = ip1.crop();
				if (floatResult) {
					ip2.setCalibrationTable(cal.getCTable());
					ip2 = ip2.convertToFloat();
				} 
				stack2.addSlice(stack1.getSliceLabel(i), ip2);
			}
		}
		catch(OutOfMemoryError e) {
			stack2.trim();
			stack2 = null;
			return null;
		}
		IjxImagePlus img3 = IJ.getFactory().newImagePlus("Result of "+img1.getShortTitle(), stack2);
		img3.setCalibration(cal);
		if (img3.getStackSize()==n) {
			int[] dim = img1.getDimensions();
			img3.setDimensions(dim[2], dim[3], dim[4]);
			if (img1.isComposite()) {
				img3 = new CompositeImage(img3, 0);
				((CompositeImage)img3).copyLuts(img1);
			}
			if (img1.isHyperStack())
				img3.setOpenAsHyperStack(true);
		}
		return img3;
	}
	
}
