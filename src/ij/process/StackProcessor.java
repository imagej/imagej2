package ij.process;
import java.awt.*;
import ij.*;
import ij.process.*;
import ij.macro.Interpreter;
import ijx.IjxImageStack;

/** This class processes stacks. */
public class StackProcessor {
    private IjxImageStack stack;
    private ImageProcessor ip;
	int nSlices;
	double xScale, yScale;
	int[] table;
	double fillValue;
	    
    /* Constructs a StackProcessor from a stack. */
    //public StackProcessor(IjxImageStack stack) {
    //	this.StackProcessor(stack, stack.getProcessor());
   //}
	
    /** Constructs a StackProcessor from a stack. 'ip' is the
    	processor that will be used to process the slices. 
    	'ip' can be null when using crop(). */
    public StackProcessor(IjxImageStack stack, ImageProcessor ip) {
    	this.stack = stack;
    	this.ip = ip;
    	nSlices = stack.getSize();
 	    if (nSlices>1 && ip!=null)
 	    	ip.setProgressBar(null);
   }
	
	static final int FLIPH=0, FLIPV=1, SCALE=2, INVERT=3, APPLY_TABLE=4, SCALE_WITH_FILL=5;
	
	void process(int command) {
	    String s = "";
 	   	ImageProcessor ip2 = stack.getProcessor(1);
    	switch (command) {
    		case FLIPH: case FLIPV: s="Flip: "; break;
    		case SCALE: s="Scale: "; break;
    		case SCALE_WITH_FILL: s="Scale: "; ip2.setBackgroundValue(fillValue); break;
    		case INVERT: s="Invert: "; break;
    		case APPLY_TABLE: s="Apply: "; break;
    	}
 	   	ip2.setRoi(this.ip.getRoi());
	    ip2.setInterpolate(this.ip.getInterpolate());
	    for (int i=1; i<=nSlices; i++) {
    		showStatus(s,i,nSlices);
	    	ip2.setPixels(stack.getPixels(i));
	    	if (nSlices==1 && i==1 && command==SCALE)
	    		ip2.snapshot();
	    	switch (command) {
	    		case FLIPH: ip2.flipHorizontal(); break;
	    		case FLIPV: ip2.flipVertical(); break;
	    		case SCALE: case SCALE_WITH_FILL: ip2.scale(xScale, yScale); break;
	    		case INVERT: ip2.invert(); break;
	    		case APPLY_TABLE: ip2.applyTable(table); break;
	    	}
			IJ.showProgress((double)i/nSlices);
	    }
		IJ.showProgress(1.0);
	}

	public void invert() {
		process(INVERT);
	}
	
	public void flipHorizontal() {
		process(FLIPH);
	}
	
	public void flipVertical() {
		process(FLIPV);
	}
	
	public void applyTable(int[] table) {
		this.table = table;
		process(APPLY_TABLE);
	}

	public void scale(double xScale, double yScale) {
		this.xScale = xScale;
		this.yScale = yScale;
		process(SCALE);
 	}

	public void scale(double xScale, double yScale, double fillValue) {
		this.xScale = xScale;
		this.yScale = yScale;
		this.fillValue = fillValue;
		process(SCALE_WITH_FILL);
 	}

	/** Creates a new stack with dimensions 'newWidth' x 'newHeight'.
		To reduce memory requirements, the orginal stack is deleted
		as the new stack is created. */
	public IjxImageStack resize(int newWidth, int newHeight) {
	    IjxImageStack stack2 = IJ.getFactory().newImageStack(newWidth, newHeight);
 		ImageProcessor ip2;
		try {
	    	for (int i=1; i<=nSlices; i++) {
    			showStatus("Resize: ",i,nSlices);
	    		ip.setPixels(stack.getPixels(1));
	    		String label = stack.getSliceLabel(1);
	    		stack.deleteSlice(1);
				ip2 = ip.resize(newWidth, newHeight);
				if (ip2!=null)
					stack2.addSlice(label, ip2);
				IJ.showProgress((double)i/nSlices);
	    	}
			IJ.showProgress(1.0);
		} catch(OutOfMemoryError o) {
			while(stack.getSize()>1)
				stack.deleteLastSlice();
			IJ.outOfMemory("StackProcessor.resize");
			IJ.showProgress(1.0);
		}
		return stack2;
	}

	/** Crops the stack to the specified rectangle. */
	public IjxImageStack crop(int x, int y, int width, int height) {
	    IjxImageStack stack2 = IJ.getFactory().newImageStack(width, height);
 		ImageProcessor ip2;
		for (int i=1; i<=nSlices; i++) {
			ImageProcessor ip1 = stack.getProcessor(1);
			ip1.setRoi(x, y, width, height);
			String label = stack.getSliceLabel(1);
			stack.deleteSlice(1);
			ip2 = ip1.crop();
			stack2.addSlice(label, ip2);
			IJ.showProgress((double)i/nSlices);
		}
		IJ.showProgress(1.0);
		return stack2;
	}

	IjxImageStack rotate90Degrees(boolean clockwise) {
 	    IjxImageStack stack2 = IJ.getFactory().newImageStack(stack.getHeight(), stack.getWidth());
 		ImageProcessor ip2;
    	for (int i=1; i<=nSlices; i++) {
    		showStatus("Rotate: ",i,nSlices);
    		ip.setPixels(stack.getPixels(1));
    		String label = stack.getSliceLabel(1);
    		stack.deleteSlice(1);
			if (clockwise)
				ip2 = ip.rotateRight();
			else
				ip2 = ip.rotateLeft();
			if (ip2!=null)
				stack2.addSlice(label, ip2);
			if (!Interpreter.isBatchMode())
				IJ.showProgress((double)i/nSlices);
    	}
		if (!Interpreter.isBatchMode())
			IJ.showProgress(1.0);
		return stack2;
	}
	
	public IjxImageStack rotateRight() {
		return rotate90Degrees(true);
 	}
 	
	public IjxImageStack rotateLeft() {
		return rotate90Degrees(false);
 	}
 	
 	public void copyBits(ImageProcessor src, int xloc, int yloc, int mode) {
 		copyBits(src, null, xloc, yloc, mode);
 	}

 	public void copyBits(IjxImageStack src, int xloc, int yloc, int mode) {
 		copyBits(null, src, xloc, yloc, mode);
 	}

 	private void copyBits(ImageProcessor srcIp, IjxImageStack srcStack, int xloc, int yloc, int mode) {
	    int inc = nSlices/20;
	    if (inc<1) inc = 1;
	    boolean stackSource = srcIp==null;
	    for (int i=1; i<=nSlices; i++) {
	    	if (stackSource)
	    		srcIp = srcStack.getProcessor(i);
 	    	ImageProcessor dstIp = stack.getProcessor(i);
	    	dstIp.copyBits(srcIp, xloc, yloc, mode);
			if ((i%inc) == 0) IJ.showProgress((double)i/nSlices);
	    }
		IJ.showProgress(1.0);
 	}
 	
 	void showStatus(String s, int n, int total) {
 		IJ.showStatus(s+n+"/"+total);
 	}	
 	
}