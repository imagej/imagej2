package ijx.gui;

import ijx.IJ;
import ijx.gui.IjxStackWindow;
import ijx.gui.IjxImageCanvas;
import ijx.app.IjxApplication;
import ijx.IjxImagePlus;

import ijx.CentralLookup;
import ijx.IjxImageStack;
import ijx.app.KeyboardHandler;
import java.awt.*;
import java.awt.event.*;
import javax.swing.ImageIcon;

/** This class is an extended IjxImageWindow used to display image stacks. */
public class StackWindow extends ImageWindow implements IjxStackWindow {
    protected Scrollbar sliceSelector; // for backward compatibity with Image5D
    protected ScrollbarWithLabel cSelector, zSelector, tSelector;
    protected Thread thread;
    protected volatile boolean done;
    protected volatile int slice;
    private ScrollbarWithLabel animationSelector;
    boolean hyperStack;
    int nChannels = 1, nSlices = 1, nFrames = 1;
    int c = 1, z = 1, t = 1;

    public StackWindow(IjxImagePlus imp) {
        this(imp, null);
    }

    public StackWindow(IjxImagePlus imp, IjxImageCanvas ic) {
        super(imp, ic);
        addScrollbars(imp);
        addMouseWheelListener(this);
        if (sliceSelector == null && this.getClass().getName().indexOf("Image5D") != -1) {
            sliceSelector = new Scrollbar(); // prevents Image5D from crashing
        }		//IJ.log(nChannels+" "+nSlices+" "+nFrames);
        pack();
        ic = imp.getCanvas();
        if (ic != null) {
            ic.setMaxBounds();
        }
        show();
        int previousSlice = imp.getCurrentSlice();
        if (previousSlice > 1 && previousSlice <= imp.getStackSize()) {
            imp.setSlice(previousSlice);
        } else {
            imp.setSlice(1);
        }
        thread = new Thread(this, "zSelector");
        thread.start();
    }

    public void addScrollbars(IjxImagePlus imp) {
        IjxImageStack s = imp.getStack();
        int stackSize = s.getSize();
        nSlices = stackSize;
        hyperStack = imp.getOpenAsHyperStack();
        imp.setOpenAsHyperStack(false);
        int[] dim = imp.getDimensions();
        int nDimensions = 2 + (dim[2] > 1 ? 1 : 0) + (dim[3] > 1 ? 1 : 0) + (dim[4] > 1 ? 1 : 0);
        if (nDimensions <= 3 && dim[2] != nSlices) {
            hyperStack = false;
        }
        if (hyperStack) {
            nChannels = dim[2];
            nSlices = dim[3];
            nFrames = dim[4];
        }
        //IJ.log("StackWindow: "+hyperStack+" "+nChannels+" "+nSlices+" "+nFrames);
        if (nSlices == stackSize) {
            hyperStack = false;
        }
        if (nChannels * nSlices * nFrames != stackSize) {
            hyperStack = false;
        }
        if (cSelector != null || zSelector != null || tSelector != null) {
            removeScrollbars();
        }
        IjxApplication ij = IJ.getInstance();
        if (nChannels > 1) {
            cSelector = new ScrollbarWithLabel(this, 1, 1, 1, nChannels + 1, 'c');
            add(cSelector);
            if (ij != null) {
                cSelector.addKeyListener(CentralLookup.getDefault().lookup(KeyboardHandler.class));
            }
            cSelector.addAdjustmentListener(this);
            cSelector.setFocusable(false); // prevents scroll bar from blinking on Windows
            cSelector.setUnitIncrement(1);
            cSelector.setBlockIncrement(1);
        }
        if (nSlices > 1) {
            char label = nChannels > 1 || nFrames > 1 ? 'z' : 't';
            if (stackSize == dim[2] && imp.isComposite()) {
                label = 'c';
            }
            zSelector = new ScrollbarWithLabel(this, 1, 1, 1, nSlices + 1, label);
            if (label == 't') {
                animationSelector = zSelector;
            }
            add(zSelector);
            if (ij != null) {
                zSelector.addKeyListener(CentralLookup.getDefault().lookup(KeyboardHandler.class));
            }
            zSelector.addAdjustmentListener(this);
            zSelector.setFocusable(false);
            int blockIncrement = nSlices / 10;
            if (blockIncrement < 1) {
                blockIncrement = 1;
            }
            zSelector.setUnitIncrement(1);
            zSelector.setBlockIncrement(blockIncrement);
            sliceSelector = zSelector.bar;
        }
        if (nFrames > 1) {
            animationSelector = tSelector = new ScrollbarWithLabel(this, 1, 1, 1, nFrames + 1, 't');
            add(tSelector);
            if (ij != null) {
                tSelector.addKeyListener(CentralLookup.getDefault().lookup(KeyboardHandler.class));
            }
            tSelector.addAdjustmentListener(this);
            tSelector.setFocusable(false);
            int blockIncrement = nFrames / 10;
            if (blockIncrement < 1) {
                blockIncrement = 1;
            }
            tSelector.setUnitIncrement(1);
            tSelector.setBlockIncrement(blockIncrement);
        }
    }

    public synchronized void adjustmentValueChanged(AdjustmentEvent e) {
        if (!isRunning2()) {
            if (e.getSource() == cSelector) {
                c = cSelector.getValue();
                if (c == imp.getChannel() && e.getAdjustmentType() == AdjustmentEvent.TRACK) {
                    return;
                }
            } else if (e.getSource() == zSelector) {
                z = zSelector.getValue();
                int slice = hyperStack ? imp.getSlice() : imp.getCurrentSlice();
                if (z == slice && e.getAdjustmentType() == AdjustmentEvent.TRACK) {
                    return;
                }
            } else if (e.getSource() == tSelector) {
                t = tSelector.getValue();
                if (t == imp.getFrame() && e.getAdjustmentType() == AdjustmentEvent.TRACK) {
                    return;
                }
            }
            updatePosition();
            notify();
        }
    }

    void updatePosition() {
        slice = (t - 1) * nChannels * nSlices + (z - 1) * nChannels + c;
        imp.updatePosition(c, z, t);
    }

    public void actionPerformed(ActionEvent e) {
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent event) {
        synchronized (this) {
            int rotation = event.getWheelRotation();
            if (hyperStack) {
                if (rotation > 0) {
                    IJ.runPlugIn("ijx.plugin.Animator", "next");
                } else if (rotation < 0) {
                    IJ.runPlugIn("ijx.plugin.Animator", "previous");
                }
            } else {
                int slice = imp.getCurrentSlice() + rotation;
                if (slice < 1) {
                    slice = 1;
                } else if (slice > imp.getStack().getSize()) {
                    slice = imp.getStack().getSize();
                }
                imp.setSlice(slice);
            }
        }
    }

    @Override
    public boolean canClose() {
        throw new UnsupportedOperationException("Not supported yet."); // @todo
    }

    @Override
    public boolean close() {
        if (!super.close()) {
            return false;
        }
        synchronized (this) {
            done = true;
            notify();
        }
        return true;
    }

    /** Displays the specified slice and updates the stack scrollbar. */
    public void showSlice(int index) {
        if (imp != null && index >= 1 && index <= imp.getStackSize()) {
            imp.setSlice(index);
        }
    }

    /** Updates the stack scrollbar. */
    public void updateSliceSelector() {
        if (hyperStack || zSelector == null) {
            return;
        }
        int stackSize = imp.getStackSize();
        int max = zSelector.getMaximum();
        if (max != (stackSize + 1)) {
            zSelector.setMaximum(stackSize + 1);
        }
        zSelector.setValue(imp.getCurrentSlice());
    }

    public void run() {
        while (!done) {
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
            if (done) {
                return;
            }
            if (slice > 0) {
                int s = slice;
                slice = 0;
                if (s != imp.getCurrentSlice()) {
                    imp.setSlice(s);
                }
            }
        }
    }

    @Override
    public String createSubtitle() {
        String subtitle = super.createSubtitle();
        if (!hyperStack) {
            return subtitle;
        }
        String s = "";
        int[] dim = imp.getDimensions();
        int channels = dim[2], slices = dim[3], frames = dim[4];
        if (channels > 1) {
            s += "c:" + imp.getChannel() + "/" + channels;
            if (slices > 1 || frames > 1) {
                s += " ";
            }
        }
        if (slices > 1) {
            s += "z:" + imp.getSlice() + "/" + slices;
            if (frames > 1) {
                s += " ";
            }
        }
        if (frames > 1) {
            s += "t:" + imp.getFrame() + "/" + frames;
        }
        if (isRunning2()) {
            return s;
        }
        int index = subtitle.indexOf(";");
        if (index != -1) {
            int index2 = subtitle.indexOf("(");
            if (index2 >= 0 && index2 < index && subtitle.length() > index2 + 4 && !subtitle.substring(index2 + 1, index2 + 4).equals("ch:")) {
                index = index2;
                s = s + " ";
            }
            subtitle = subtitle.substring(index, subtitle.length());
        } else {
            subtitle = "";
        }
        return s + subtitle;
    }

    public boolean isHyperStack() {
        return hyperStack;
    }

    public void setPosition(int channel, int slice, int frame) {
        if (cSelector != null && channel != c) {
            c = channel;
            cSelector.setValue(channel);
        }
        if (zSelector != null && slice != z) {
            z = slice;
            zSelector.setValue(slice);
        }
        if (tSelector != null && frame != t) {
            t = frame;
            tSelector.setValue(frame);
        }
        updatePosition();
        if (this.slice > 0) {
            int s = this.slice;
            this.slice = 0;
            if (s != imp.getCurrentSlice()) {
                imp.setSlice(s);
            }
        }
    }

    public boolean validDimensions() {
        int c = imp.getNChannels();
        int z = imp.getNSlices();
        int t = imp.getNFrames();
        if (c != nChannels || z != nSlices || t != nFrames || c * z * t != imp.getStackSize()) {
            return false;
        } else {
            return true;
        }
    }

    public void setAnimate(boolean b) {
        if (super.isRunning2() != b && animationSelector != null) {
            animationSelector.updatePlayPauseIcon();
        }
        super.setRunning2(b);
    }

    public boolean getAnimate() {
        return super.isRunning2();
    }

    public int getNScrollbars() {
        int n = 0;
        if (cSelector != null) {
            n++;
        }
        if (zSelector != null) {
            n++;
        }
        if (tSelector != null) {
            n++;
        }
        return n;
    }

    public void removeScrollbars() {
        if (cSelector != null) {
            remove(cSelector);
            cSelector.removeAdjustmentListener(this);
            cSelector = null;
        }
        if (zSelector != null) {
            remove(zSelector);
            zSelector.removeAdjustmentListener(this);
            zSelector = null;
        }
        if (tSelector != null) {
            remove(tSelector);
            tSelector.removeAdjustmentListener(this);
            tSelector = null;
        }
    }

    public ScrollbarWithLabel getCSelector() {
        return cSelector;
    }

    public ScrollbarWithLabel getZSelector() {
        return zSelector;
    }

    public ScrollbarWithLabel getTSelector() {
        return tSelector;
    }

    public Container getContainer() {
        return this;
    }

    public void paintMe(Graphics g) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ImageIcon getImageIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
