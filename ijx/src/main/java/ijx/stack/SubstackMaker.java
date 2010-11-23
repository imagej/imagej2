package ijx.stack;
import ijx.plugin.api.PlugIn;
import ijx.process.ImageProcessor;
import ijx.gui.dialog.GenericDialog;
import ijx.roi.Roi;
import ijx.IJ;
import ij.*;


import ijx.IjxImagePlus;
import ijx.IjxImageStack;

/**
 * This plugin extracts selected images from a stack to make a new substack.
 * It takes three types of inputs: a range of images (e.g. 2-14), a range of images
 * with an increment (e.g. 2-14-3), or a list of images (e.g. 7,9,25,27,34,132).
 * It then copies those images from the active stack to a new stack in the order
 * of listing or range.
 *
 * @author Anthony Padua
 * @author Neuroradiology
 * @author Duke University Medical Center
 * @author padua001@mc.duke.edu
 *
 * @author Daniel Barboriak, MD
 * @author Neuroradiology
 * @author Duke University Medical Center
 * @author barbo013@mc.duke.edu
 *
 * @author Ved P. Sharma, Ph.D.
 * @author Anatomy and Structural Biology
 * @author Albert Einstein College of Medicine
 * @author ved.sharma@einstein.yu.edu
 *
 */

public class SubstackMaker implements PlugIn {
		
	public void run(String arg) {
		IjxImagePlus imp = IJ.getImage();
		if (imp.isHyperStack() || imp.isComposite()) {
			IJ.error("Make Substack", "This command does not currently work with hyperstacks.");
			return;
		}
		String userInput = showDialog();
		if (userInput==null)
			return;
		IjxImagePlus imp2 = makeSubstack(imp, userInput);
		if (imp2!=null)
			imp2.show();
	}

	public IjxImagePlus makeSubstack(IjxImagePlus imp, String userInput) {
		String stackTitle = "Substack ("+userInput+")";
		if (stackTitle.length()>25) {
			int idxA = stackTitle.indexOf(",",18);
			int idxB = stackTitle.lastIndexOf(",");
			if(idxA>=1 && idxB>=1){
				String strA = stackTitle.substring(0,idxA);
				String strB = stackTitle.substring(idxB+1);
				stackTitle = strA + ", ... " + strB;
			}
		}
		IjxImagePlus imp2 = null;
		try {
			int idx1 = userInput.indexOf("-");
			if (idx1>=1) {									// input displayed in range
				String rngStart = userInput.substring(0, idx1);
				String rngEnd = userInput.substring(idx1+1);
				Integer obj = new Integer(rngStart);
				int first = obj.intValue();
				int inc = 1;
				int idx2 = rngEnd.indexOf("-");
				if (idx2>=1) {
					String rngEndAndInc = rngEnd;
					rngEnd = rngEndAndInc.substring(0, idx2);
					String rngInc = rngEndAndInc.substring(idx2+1);
					obj = new Integer(rngInc);
					inc = obj.intValue();
				}
				obj = new Integer(rngEnd);
				int last = obj.intValue();
				imp2 = stackRange(imp, first, last, inc, stackTitle);
			} else {
				int count = 1; // count # of slices to extract
				for (int j=0; j<userInput.length(); j++) {
					char ch = Character.toLowerCase(userInput.charAt(j));
					if (ch==',') {count += 1;}
				}
				int[] numList = new int[count];
				for(int i=0; i<count; i++) {
					int idx2 = userInput.indexOf(",");
					if(idx2>0) {
						String num = userInput.substring(0,idx2);
						Integer obj = new Integer(num);
						numList[i] = obj.intValue();
						userInput = userInput.substring(idx2+1);
					}
					else{
						String num = userInput;
						Integer obj = new Integer(num);
						numList[i] = obj.intValue();
					}
				}
				imp2 = stackList(imp, count, numList, stackTitle);
			}
		} catch (Exception e) {
			IJ.error("Substack Maker", "Invalid input string:        \n \n  \""+userInput+"\"");
		}
		return imp2;
	}

	String showDialog() {
		GenericDialog gd = new GenericDialog("Substack Maker");
		gd.setInsets(10,45,0);
		gd.addMessage("Enter a range (e.g. 2-14), a range with increment\n(e.g. 1-100-2) or a list (e.g. 7,9,25,27)");
		gd.addStringField("Slices:", "", 40);
		gd.showDialog();
		if (gd.wasCanceled())
			return null;
		else
			return gd.getNextString();
	}

	// extract specific slices
	IjxImagePlus stackList(IjxImagePlus imp, int count, int[] numList, String stackTitle) throws Exception {
		IjxImageStack stack = imp.getStack();
		IjxImageStack stack2 = null;
		Roi roi = imp.getRoi();
		for (int i=0; i<count; i++) {
			int currSlice = numList[i];
			ImageProcessor ip2 = stack.getProcessor(currSlice);
			ip2.setRoi(roi);
			ip2 = ip2.crop();
			if (stack2==null)
				stack2 = IJ.getFactory().newImageStack(ip2.getWidth(), ip2.getHeight());
			stack2.addSlice(stack.getSliceLabel(currSlice), ip2);
		}
		IjxImagePlus impSubstack = imp.createImagePlus();
		impSubstack.setStack(stackTitle, stack2);
		impSubstack.setCalibration(imp.getCalibration());
		return impSubstack;
	}
	
	// extract range of slices
	IjxImagePlus stackRange(IjxImagePlus imp, int first, int last, int inc, String title) throws Exception {		int width = imp.getWidth();
		IjxImageStack stack = imp.getStack();
		IjxImageStack stack2 = null;
		Roi roi = imp.getRoi();
		for (int i= first; i<= last; i+=inc) {
			//IJ.log(first+" "+last+" "+inc+" "+i);
			ImageProcessor ip2 = stack.getProcessor(i);
			ip2.setRoi(roi);
			ip2 = ip2.crop();
			if (stack2==null)
				stack2 = IJ.getFactory().newImageStack(ip2.getWidth(), ip2.getHeight());
			stack2.addSlice(stack.getSliceLabel(i), ip2);
		}
		IjxImagePlus substack = imp.createImagePlus();
		substack.setStack(title, stack2);
		substack.setCalibration(imp.getCalibration());
		return substack;
	}
		
}
