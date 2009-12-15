package ijx.gui;
import ij.gui.*;
import ijx.gui.IjxStackWindow;
import ijx.gui.IjxImageCanvas;
import ijx.IjxApplication;
import ijx.IjxImagePlus;
import ij.*;
import ij.measure.Calibration;
import ijx.IjxImageStack;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;

/** This class is an extended ImageWindow used to display image stacks. */
public class StackWindowSwing extends ImageWindowSwing implements IjxStackWindow {

	protected Scrollbar channelSelector, sliceSelector, frameSelector;
	protected Thread thread;
	protected volatile boolean done;
	protected volatile int slice;
	boolean hyperStack;
	int nChannels=1, nSlices=1, nFrames=1;
	int c=1, z=1, t=1;
	

	public StackWindowSwing(IjxImagePlus imp) {
		this(imp, null);
	}
    
    public StackWindowSwing(IjxImagePlus imp, IjxImageCanvas ic) {
		super(imp, ic);
		// add slice selection slider
		IjxImageStack s = imp.getStack();
		int stackSize = s.getSize();
		nSlices = stackSize;
		hyperStack = imp.getOpenAsHyperStack();
		imp.setOpenAsHyperStack(false);
		int[] dim = imp.getDimensions();
		int nDimensions = 2+(dim[2]>1?1:0)+(dim[3]>1?1:0)+(dim[4]>1?1:0);
		if (nDimensions<=3 && dim[2]!=nSlices) hyperStack = false;
		if (hyperStack) {
			nChannels = dim[2];
			nSlices = dim[3];
			nFrames = dim[4];
		}
		//IJ.log("StackWindow: "+hyperStack+" "+nChannels+" "+nSlices+" "+nFrames);
		if (nSlices==stackSize) hyperStack = false;
		if (nChannels*nSlices*nFrames!=stackSize) hyperStack = false;
		addMouseWheelListener(this);
		IjxApplication ij = IJ.getInstance();
		if (nChannels>1) {
			channelSelector = new Scrollbar(Scrollbar.HORIZONTAL, 1, 1, 1, nChannels+1);
			Panel panel = new Panel(new BorderLayout(2, 0));
			//panel.add(new Label("c"), BorderLayout.WEST);
			//panel.add(channelSelector, BorderLayout.CENTER);
			add(channelSelector);
			if (ij!=null) channelSelector.addKeyListener(ij);
			channelSelector.addAdjustmentListener(this);
			channelSelector.setFocusable(false); // prevents scroll bar from blinking on Windows
			channelSelector.setUnitIncrement(1);
			channelSelector.setBlockIncrement(1);
		}
		if (nSlices>1) {
			sliceSelector = new Scrollbar(Scrollbar.HORIZONTAL, 1, 1, 1, nSlices+1);
			add(sliceSelector);
			if (ij!=null) sliceSelector.addKeyListener(ij);
			sliceSelector.addAdjustmentListener(this);
			sliceSelector.setFocusable(false);
			int blockIncrement = nSlices/10;
			if (blockIncrement<1) blockIncrement = 1;
			sliceSelector.setUnitIncrement(1);
			sliceSelector.setBlockIncrement(blockIncrement);
		}
		if (nFrames>1) {
			frameSelector = new Scrollbar(Scrollbar.HORIZONTAL, 1, 1, 1, nFrames+1);
			add(frameSelector);
			if (ij!=null) frameSelector.addKeyListener(ij);
			frameSelector.addAdjustmentListener(this);
			frameSelector.setFocusable(false);
			int blockIncrement = nFrames/10;
			if (blockIncrement<1) blockIncrement = 1;
			frameSelector.setUnitIncrement(1);
			frameSelector.setBlockIncrement(blockIncrement);
		}
		if (sliceSelector==null && this.getClass().getName().indexOf("Image5D")!=-1)
			sliceSelector = new Scrollbar(); // prevents Image5D from crashing
		//IJ.log(nChannels+" "+nSlices+" "+nFrames);
		pack();
		ic = (ImageCanvas) imp.getCanvas();
		if (ic!=null) ic.setMaxBounds();
		show();
		int previousSlice = imp.getCurrentSlice();
		if (previousSlice>1 && previousSlice<=stackSize)
			imp.setSlice(previousSlice);
		else
			imp.setSlice(1);
		thread = new Thread(this, "SliceSelector");
		thread.start();
	}

	public synchronized void adjustmentValueChanged(AdjustmentEvent e) {
		if (!isRunning2()) {
			//slice = sliceSelector.getValue();
			if (e.getSource()==channelSelector)
				c = channelSelector.getValue();
			else if (e.getSource()==sliceSelector)
				z = sliceSelector.getValue();
			else if (e.getSource()==frameSelector)
				t = frameSelector.getValue();
			updatePosition();
			notify();
		}
	}
	
	void updatePosition() {
		slice = (t-1)*nChannels*nSlices + (z-1)*nChannels + c;
		imp.updatePosition(c, z, t);
	}

	public void actionPerformed(ActionEvent e) {
	}

	public void mouseWheelMoved(MouseWheelEvent event) {
		synchronized(this) {
			int rotation = event.getWheelRotation();
			if (hyperStack) {
				if (rotation>0)
					IJ.runPlugIn("ij.plugin.Animator", "next");
				else if (rotation<0)
					IJ.runPlugIn("ij.plugin.Animator", "previous");
			} else {
				int slice = imp.getCurrentSlice() + rotation;
				if (slice<1)
					slice = 1;
				else if (slice>imp.getStack().getSize())
					slice = imp.getStack().getSize();
				imp.setSlice(slice);
			}
		}
	}
    
    public void close() {
		setVisible(false);
		dispose();
		WindowManager.removeWindow(this);
    }
    
	public boolean canClose() {
		if (!super.canClose())
			return false;
		synchronized(this) {
			done = true;
			notify();
		}
        return true;
	}

	/** Displays the specified slice and updates the stack scrollbar. */
	public void showSlice(int index) {
		if (index>=1 && index<=imp.getStackSize())
			imp.setSlice(index);
	}
	
	/** Updates the stack scrollbar. */
	public void updateSliceSelector() {
		if (hyperStack) return;
		int stackSize = imp.getStackSize();
		int max = sliceSelector.getMaximum();
		if (max!=(stackSize+1))
			sliceSelector.setMaximum(stackSize+1);
		sliceSelector.setValue(imp.getCurrentSlice());
	}
	
	public void run() {
		while (!done) {
			synchronized(this) {
				try {wait();}
				catch(InterruptedException e) {}
			}
			if (done) return;
			if (slice>0) {
				int s = slice;
				slice = 0;
				if (s!=imp.getCurrentSlice())
					imp.setSlice(s);
			}
		}
	}
	
	public String createSubtitle() {
		String subtitle = super.createSubtitle();
		if (!hyperStack) return subtitle;
    	String s="";
    	int[] dim = imp.getDimensions();
    	int channels=dim[2], slices=dim[3], frames=dim[4];
		if (channels>1) {
			s += "c:"+imp.getChannel()+"/"+channels;
			if (slices>1||frames>1) s += " ";
		}
		if (slices>1) {
			s += "z:"+imp.getSlice()+"/"+slices;
			if (frames>1) s += " ";
		}
		if (frames>1)
			s += "t:"+imp.getFrame()+"/"+frames;
		if (isRunning2()) return s;
		int index = subtitle.indexOf(";");
		if (index!=-1) {
			int index2 = subtitle.indexOf("(");
			if (index2>=0 && index2<index && subtitle.length()>index2+4 && !subtitle.substring(index2+1, index2+4).equals("ch:")) {
				index = index2;
				s = s + " ";
			}
			subtitle = subtitle.substring(index, subtitle.length());
		} else
			subtitle = "";
    	return s + subtitle;
    }
    
    public boolean isHyperStack() {
    	return hyperStack;
    }
    
    public void setPosition(int channel, int slice, int frame) {
    	if (channelSelector!=null && channel!=c) {
    		c = channel;
			channelSelector.setValue(channel);
		}
    	if (sliceSelector!=null && slice!=z) {
    		z = slice;
			sliceSelector.setValue(slice);
		}
    	if (frameSelector!=null && frame!=t) {
    		t = frame;
			frameSelector.setValue(frame);
		}
    	updatePosition();
		if (this.slice>0) {
			int s = this.slice;
			this.slice = 0;
			if (s!=imp.getCurrentSlice())
				imp.setSlice(s);
		}
    }
    
    public boolean validDimensions() {
    	int c = imp.getNChannels();
    	int z = imp.getNSlices();
    	int t = imp.getNFrames();
    	if (c!=nChannels||z!=nSlices||t!=nFrames||c*z*t!=imp.getStackSize())
    		return false;
    	else
    		return true;
    }
    
}
