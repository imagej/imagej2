/*
 * 
 * $Id$
 * 
 * Software License Agreement (BSD License)
 * 
 * Copyright (c) 2010, Expression company is undefined on line 9, column 62 in Templates/Licenses/license-bsd.txt.
 * All rights reserved.
 * 
 * Redistribution and use of this software in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * 
 *   Redistributions of source code must retain the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer.
 * 
 *   Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer in the documentation and/or other
 *   materials provided with the distribution.
 * 
 *   Neither the name of Expression company is undefined on line 24, column 41 in Templates/Licenses/license-bsd.txt. nor the names of its
 *   contributors may be used to endorse or promote products
 *   derived from this software without specific prior
 *   written permission of Expression company is undefined on line 27, column 43 in Templates/Licenses/license-bsd.txt.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ijx.gui;

import ijx.CompositeImage;
import ijx.IJ;
import ijx.ImageJApplet;
import ijx.Menus;
import ijx.Prefs;
import ijx.WindowManager;
import ijx.gui.dialog.YesNoCancelDialog;
import ijx.implementation.mdi.TopComponentMDI;
import ijx.io.FileSaver;
import ijx.macro.Interpreter;
import ijx.measure.Calibration;
import ijx.plugin.frame.Channels;
import ijx.CentralLookup;
import ijx.IjxImagePlus;
import ijx.IjxImageStack;
import ijx.IjxMenus;
import ijx.IjxTopComponent;
import ijx.app.IjxApplication;
import ijx.app.KeyboardHandler;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

/** Window for displaying images.
 * Using composition rather than inheritance...
 * The component, displayWindow, acts as the window and is set when this class is constructed
 * GBH, Sept 2010
 */
public class AbstractImageWindow //extends Frame
        implements IjxImageWindow, InternalFrameListener {
    public static final int MIN_WIDTH = 128;
    public static final int MIN_HEIGHT = 32;
    private static final int XINC = 8;
    private static final int YINC = 12;
    private static final int TEXT_GAP = 10;
    private static int xbase = -1;
    private static int ybase;
    private static int xloc;
    private static int yloc;
    private static int count;
    //
    protected IjxImagePlus imp;
    protected IjxApplication ij;
    protected IjxImageCanvas ic;
    protected Container displayWindow; // the Window - a Frame, JFrame or JInternalFrame
    //
    protected KeyboardHandler keyHandler = CentralLookup.getDefault().lookup(KeyboardHandler.class);
    private double initialMagnification = 1;
    private int newWidth, newHeight;
    protected boolean closed;
    protected boolean newCanvas;
    private boolean unzoomWhenMinimizing = true;
    Rectangle maxWindowBounds; // largest possible window on this screen
    Rectangle maxBounds; // Size of this window after it is maximized
    long maxBoundsTime;
    private int textGap = WindowManager.isCenterNextImage() ? 0 : TEXT_GAP;
    /** This variable is set false if the user presses the escape key or closes the window. */
    public boolean running;
    /** This variable is set false if the user clicks in this
    window, presses the escape key, or closes the window. */
    private boolean running2;
    private String title = "";
    protected boolean isStackWindow = false;

    public AbstractImageWindow(String _title, Container window) {
        this.title = _title;
        displayWindow = window;
        if (displayWindow == null) {
            throw new UnsupportedOperationException("Uh Oh... window is null"); // @todo
        }
        setTitle(this.title);

        // @todo no ImagePlus assigned... ??
    }

    public AbstractImageWindow(IjxImagePlus _imp, Container window) {
        this(_imp, null, window);
    }

    public AbstractImageWindow(IjxImagePlus _imp, IjxImageCanvas _ic, Container window) {
        this.displayWindow = window;
        this.imp = _imp;
        this.ic = _ic;
        ij = IJ.getInstance();
        if (displayWindow == null || imp == null) {
            throw new UnsupportedOperationException("Uh Oh...window or ImagePlus is null"); // @todo
        }
        this.setTitle(imp.getTitle());
        IjxImageWindow previousWindow = imp.getWindow();
        {
            if (Prefs.blackCanvas && getClass().getName().equals("ijx.gui.IjxImageWindow")) {
                displayWindow.setForeground(Color.white);
                displayWindow.setBackground(Color.black);
            } else {
                displayWindow.setForeground(Color.black);
                if (IJ.isLinux()) {
                    displayWindow.setBackground(IJ.backgroundColor);
                } else {
                    displayWindow.setBackground(Color.white);
                }
            }
        }
        if (ic == null) {
            ic = IJ.getFactory().newImageCanvas(imp);
            newCanvas = true;
        }
        // Add the canvas to the window
        displayWindow.add(ic.getCanvas());
        displayWindow.setLayout(new ImageLayout(ic)); //  <<============
        //displayWindow.setLayout(new BorderLayout()); //  <<============
        //
        ((IjxImageDisplayWindow) displayWindow).setDrawingDelegate(this);
        //
        isStackWindow = (IjxStackWindow.class.isAssignableFrom(this.getClass()));

        if (Frame.class.isAssignableFrom(displayWindow.getClass())) {
            setListenersOnWindow(((Frame) displayWindow));
        }
        if (displayWindow instanceof JInternalFrame) {
            setListenersOnInternalWindow(((JInternalFrame) displayWindow));
        }

        WindowManager.addWindow(this);
        imp.setWindow(this);
        ImageJApplet applet = getApplet();

        if (applet != null) {  // is runnning as an Applet
            if (Interpreter.isBatchMode()) {
                WindowManager.setTempCurrentImage(imp);
                Interpreter.addBatchModeImage(imp);
            } else {
                applet.setImageCanvas((ImageCanvas) ic);
            }
        } else if (previousWindow != null) { // get pos & location from existing window
            if (newCanvas) {
                setLocationAndSize(false);
            } else {
                ic.update(previousWindow.getCanvas());
            }
            Point loc = previousWindow.getLocation();
            displayWindow.setLocation(loc.x, loc.y);
            // @todo ... ??
//            if (!isStackWindow) {
            if (!(this instanceof IjxStackWindow)) {
                packAndShow(displayWindow);
            }
            boolean unlocked = imp.lockSilently();
            boolean changes = imp.isChanged();
            imp.setChanged(false);
            previousWindow.close();
            imp.setChanged(changes);
            if (unlocked) {
                imp.unlock();
            }
            WindowManager.setCurrentWindow(this);
        } else {
            setLocationAndSize(false);
            // @todo Macintosh-related
            if (ij != null && !IJ.isMacintosh()) {
                ImageIcon img = ij.getImageIcon();
                if (img != null) {
                    if (Frame.class.isAssignableFrom(displayWindow.getClass())) {
                        ((Frame) displayWindow).setIconImage(img.getImage());
                        ((Frame) displayWindow).setTitle(title);
                    }
                }
                if (displayWindow instanceof JInternalFrame) {
                    ((JInternalFrame) displayWindow).setFrameIcon(img);
                    ((JInternalFrame) displayWindow).setTitle(title);
                }
            }
            // @todo  add JInternalFrame handling
            if (WindowManager.isCenterNextImage()) {
                GUI.center((Frame) displayWindow);
                WindowManager.setCenterNextImage(false);
            } else if (WindowManager.getNextLocation() != null) {
                displayWindow.setLocation(WindowManager.getNextLocation());
                WindowManager.setNextLocation(null);
            }
            // @todo need HistogramWindow that is an AbstractImageWindow...
//            if (Interpreter.isBatchMode()
//                    || (IJ.getInstance() == null && this instanceof HistogramWindow)) {
//                WindowManager.setTempCurrentImage(imp);
//                Interpreter.addBatchModeImage(imp);
//            } else {
            if (!(this instanceof IjxStackWindow)) {
                packAndShow(displayWindow);
            }
//            }
        }
    }

    protected void packAndShow(Container displayWindow) {
        if (Frame.class.isAssignableFrom(displayWindow.getClass())) {
            ((Frame) displayWindow).pack();
            ((Frame) displayWindow).setVisible(true);
        }
        if (displayWindow instanceof JInternalFrame) {
            JInternalFrame frame = (JInternalFrame) displayWindow;
            frame.setBounds(10, 10, 300, 200);
            frame.pack();
            TopComponentMDI top = (TopComponentMDI) CentralLookup.getDefault().lookup(IjxTopComponent.class);
            try {
                top.getDesktop().add(frame);
                frame.setVisible(true);
            } catch (Exception e) {
            }
            try {
                frame.setSelected(true);
            } catch (java.beans.PropertyVetoException pve) {
            }
        }
    }

    public Container getContainer() {
        return displayWindow;
    }

    private ImageJApplet getApplet() {
        if (null == IJ.getInstance()) {
            return null;
        }
        return IJ.getInstance().getApplet();
    }

    public void pack() {
        ImageJApplet applet = getApplet();
        if (applet != null) {
            applet.pack();
        } else if (Frame.class.isAssignableFrom(displayWindow.getClass())) {
            // awt.Frame or swing.JFrame
            ((Frame) displayWindow).pack();
        } else if (displayWindow instanceof JInternalFrame) {
            // swing.JInternalFrame
            ((JInternalFrame) displayWindow).pack();
        }
    }

    public void show() {
        ImageJApplet applet = getApplet();
        if (applet != null) {
            applet.setImageCanvas((ImageCanvas) ic);
        } else if (Frame.class.isAssignableFrom(displayWindow.getClass())) {
            // awt.Frame or swing.JFrame
            ((Frame) displayWindow).show();
        } else if (displayWindow instanceof JInternalFrame) {
            // swing.JInternalFrame
            ((JInternalFrame) displayWindow).setVisible(true);
        }
    }

    public void toFront() {
        if (Frame.class.isAssignableFrom(displayWindow.getClass())) {
            // awt.Frame or swing.JFrame
            ((Frame) displayWindow).toFront();
        } else if (displayWindow instanceof JInternalFrame) {
            // swing.JInternalFrame
            ((JInternalFrame) displayWindow).toFront();
        }
        ImageJApplet applet = getApplet();
        if (applet != null) {
            applet.setImageCanvas((ImageCanvas) ic);
        }
    }

    private void setLocationAndSize(boolean updating) {
        int width = imp.getWidth();
        int height = imp.getHeight();
        Rectangle maxWindow = getMaxWindow(0, 0);
        //if (maxWindow.x==maxWindow.width)  // work around for Linux bug
        //	maxWindow = new Rectangle(0, maxWindow.y, maxWindow.width, maxWindow.height);
        if (WindowManager.getWindowCount() <= 1) {
            xbase = -1;
        }
        if (width > maxWindow.width / 2 && xbase > maxWindow.x + 5 + XINC * 6) {
            xbase = -1;
        }
        if (xbase == -1) {
            count = 0;
            xbase = maxWindow.x + 5;
            if (width * 2 <= maxWindow.width) {
                xbase = maxWindow.x + maxWindow.width / 2 - width / 2;
            }
            ybase = maxWindow.y;
            xloc = xbase;
            yloc = ybase;
        }
        int x = xloc;
        int y = yloc;
        xloc += XINC;
        yloc += YINC;
        count++;
        if (count % 6 == 0) {
            xloc = xbase;
            yloc = ybase;
        }
        int sliderHeight = isStackWindow ? 20 : 0;
        //int sliderHeight = (this instanceof IjxStackWindow) ? 20 : 0;
        int screenHeight = maxWindow.y + maxWindow.height - sliderHeight;
        double mag = 1;
        while (xbase + XINC * 4 + width * mag > maxWindow.x + maxWindow.width || ybase + height * mag >= screenHeight) {
            //IJ.log(mag+"  "+xbase+"  "+width*mag+"  "+maxWindow.width);
            double mag2 = ImageCanvasHelper.getLowerZoomLevel(mag);
            if (mag2 == mag) {
                break;
            }
            mag = mag2;
        }

        if (mag < 1.0) {
            initialMagnification = mag;
            ic.setDrawingSize((int) (width * mag), (int) (height * mag));
        }
        ic.setMagnification(mag);
        if (y + height * mag > screenHeight) {
            y = ybase;
        }
        if (!updating) {
            displayWindow.setLocation(x, y);
        }
        if (Prefs.open100Percent && ic.getMagnification() < 1.0) {
            while (ic.getMagnification() < 1.0) {
                ic.zoomIn(0, 0);
            }
            displayWindow.setSize(Math.min(width, maxWindow.width - x), Math.min(height, screenHeight - y));
            displayWindow.validate();
        } else {
            if (Frame.class.isAssignableFrom(displayWindow.getClass())) {
                ((Frame) displayWindow).pack();

            }
            if (displayWindow instanceof JInternalFrame) {
                ((JInternalFrame) displayWindow).pack();
            }
        }
    }

    public Rectangle getMaxWindow(int xloc, int yloc) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle bounds = ge.getMaximumWindowBounds();
        //bounds.x=960; bounds.y=0; bounds.width=960; bounds.height=1200;
        if (IJ.debugMode) {
            IJ.log("getMaxWindow: " + bounds + "  " + xloc + "," + yloc);
        }
        if (xloc > bounds.x + bounds.width || yloc > bounds.y + bounds.height) {
            Rectangle bounds2 = getSecondaryMonitorBounds(ge, xloc, yloc);
            if (bounds2 != null) {
                return bounds2;
            }
        }
// @todo fix this...
//		Dimension ijSize = ij!=null?ij.getSize():new Dimension(0,0);
//		if (bounds.height>600) {
//			bounds.y += ijSize.height;
//			bounds.height -= ijSize.height;
//		}
        return bounds;
    }

    private Rectangle getSecondaryMonitorBounds(GraphicsEnvironment ge, int xloc, int yloc) {
        //IJ.log("getSecondaryMonitorBounds "+wb);
        GraphicsDevice[] gs = ge.getScreenDevices();
        for (int j = 0; j < gs.length; j++) {
            GraphicsDevice gd = gs[j];
            GraphicsConfiguration[] gc = gd.getConfigurations();
            for (int i = 0; i < gc.length; i++) {
                Rectangle bounds = gc[i].getBounds();
                //IJ.log(j+" "+i+" "+bounds+"  "+bounds.contains(wb.x, wb.y));
                if (bounds != null && bounds.contains(xloc, yloc)) {
                    return bounds;
                }
            }
        }
        return null;
    }

    public double getInitialMagnification() {
        return initialMagnification;
    }

    /** Override Container getInsets() to make room for some text above the image. */
    public Insets getInsets() {
        Insets insets = displayWindow.getInsets();
        double mag = ic.getMagnification();
        int extraWidth = (int) ((MIN_WIDTH - imp.getWidth() * mag) / 2.0);
        if (extraWidth < 0) {
            extraWidth = 0;
        }
        int extraHeight = (int) ((MIN_HEIGHT - imp.getHeight() * mag) / 2.0);
        if (extraHeight < 0) {
            extraHeight = 0;
        }
        insets = new Insets(insets.top + textGap + extraHeight, insets.left + extraWidth, insets.bottom + extraHeight, insets.right + extraWidth);
        return insets;
    }

    /** Draws the subtitle. */
    public void drawInfo(Graphics g) {
        if (textGap != 0) {
            Insets insets = displayWindow.getInsets();
            if (imp.isComposite()) {
                CompositeImage ci = (CompositeImage) imp;
                if (ci.getMode() == CompositeImage.COMPOSITE) {
                    g.setColor(ci.getChannelColor());
                }
            }
            g.drawString(createSubtitle(), insets.left + 5, insets.top + TEXT_GAP);
        }
    }

    /** Creates the subtitle. */
    public String createSubtitle() {
        String s = "";
        int nSlices = imp.getStackSize();
        if (nSlices > 1) {
            IjxImageStack stack = imp.getStack();
            int currentSlice = imp.getCurrentSlice();
            s += currentSlice + "/" + nSlices;
            String label = stack.getShortSliceLabel(currentSlice);
            if (label != null && label.length() > 0) {
                if (imp.isHyperStack()) {
                    label = label.replace(';', ' ');
                }
                s += " (" + label + ")";
            }
            if ((this instanceof IjxStackWindow) && running2) {
                return s;
            }
            s += "; ";
        } else {
            String label = (String) imp.getProperty("Label");
            if (label != null) {
                int newline = label.indexOf('\n');
                if (newline > 0) {
                    label = label.substring(0, newline);
                }
                int len = label.length();
                if (len > 4 && label.charAt(len - 4) == '.' && !Character.isDigit(label.charAt(len - 1))) {
                    label = label.substring(0, len - 4);
                }
                if (label.length() > 60) {
                    label = label.substring(0, 60);
                }
                s = label + "; ";
            }
        }
        int type = imp.getType();
        Calibration cal = imp.getCalibration();
        if (cal.scaled()) {
            s += IJ.d2s(imp.getWidth() * cal.pixelWidth, 2) + "x" + IJ.d2s(imp.getHeight() * cal.pixelHeight, 2)
                    + " " + cal.getUnits() + " (" + imp.getWidth() + "x" + imp.getHeight() + "); ";
        } else {
            s += imp.getWidth() + "x" + imp.getHeight() + " pixels; ";
        }
        double size = ((double) imp.getWidth() * imp.getHeight() * imp.getStackSize()) / 1024.0;
        switch (type) {
            case IjxImagePlus.GRAY8:
            case IjxImagePlus.COLOR_256:
                s += "8-bit";
                break;
            case IjxImagePlus.GRAY16:
                s += "16-bit";
                size *= 2.0;
                break;
            case IjxImagePlus.GRAY32:
                s += "32-bit";
                size *= 4.0;
                break;
            case IjxImagePlus.COLOR_RGB:
                s += "RGB";
                size *= 4.0;
                break;
        }
        if (imp.isInvertedLut()) {
            s += " (inverting LUT)";
        }
        String s2 = null, s3 = null;
        if (size < 1024.0) {
            s2 = IJ.d2s(size, 0);
            s3 = "K";
        } else if (size < 10000.0) {
            s2 = IJ.d2s(size / 1024.0, 1);
            s3 = "MB";
        } else if (size < 1048576.0) {
            s2 = IJ.d2s(Math.round(size / 1024.0), 0);
            s3 = "MB";
        } else {
            s2 = IJ.d2s(size / 1048576.0, 1);
            s3 = "GB";
        }
        if (s2.endsWith(".0")) {
            s2 = s2.substring(0, s2.length() - 2);
        }
        return s + "; " + s2 + s3;
    }

    public void paintMe(Graphics g) {
        //if (IJ.debugMode) IJ.log("wPaint: " + imp.getTitle());
        Graphics wg = displayWindow.getGraphics();
        if (wg == null) {
            return;
        }
        drawInfo(wg);
        Rectangle r = ic.getBounds();
        int extraWidth = MIN_WIDTH - r.width;
        int extraHeight = MIN_HEIGHT - r.height;
        wg.setColor(Color.BLACK);
        if (extraWidth <= 0 && extraHeight <= 0 && !Prefs.noBorder && !IJ.isLinux()) {
            wg.drawRect(r.x - 1, r.y - 1, r.width + 1, r.height + 1);
        }
    }

    /** Removes this window from the window list and disposes of it.
    Returns false if the user cancels the "save changes" dialog. */
    public boolean close() {
        boolean isRunning = running || running2;
        running = running2 = false;
        if (isRunning) {
            IJ.wait(500);
        }
        if (ij == null || IJ.getApplet() != null || Interpreter.isBatchMode() || IJ.macroRunning()) {
            imp.setChanged(false);
        }
        if (imp.isChanged()) {
            String msg;
            String name = imp.getTitle();
            if (name.length() > 22) {
                msg = "Save changes to\n" + "\"" + name + "\"?";
            } else {
                msg = "Save changes to \"" + name + "\"?";
            }
            YesNoCancelDialog d = new YesNoCancelDialog(null, "ImageJ", msg);
            if (d.cancelPressed()) {
                return false;
            } else if (d.yesPressed()) {
                FileSaver fs = new FileSaver(imp);
                if (!fs.save()) {
                    return false;
                }
            }
        }
        closed = true;
        if (WindowManager.getWindowCount() == 0) {
            xloc = 0;
            yloc = 0;
        }
        WindowManager.removeWindow(this);
        displayWindow.setVisible(false);
        if (ij != null && ij.quitting()) // this may help avoid thread deadlocks
        {
            return true;
        }
        dispose();
        imp.flush();
        imp = null;
        return true;
    }

    public IjxImagePlus getImagePlus() {
        return imp;
    }

    public void setImage(IjxImagePlus imp2) {
        IjxImageCanvas ic = getCanvas();
        if (ic == null || imp2 == null) {
            return;
        }
        imp = imp2;
        imp.setWindow(this);
        ic.updateImage(imp);
        ic.setImageUpdated();
        ic.repaint();
        repaint();
    }

    public void updateImage(IjxImagePlus imp) {
        if (imp != this.imp) {
            throw new IllegalArgumentException("imp!=this.imp");
        }
        this.imp = imp;
        ic.updateImage(imp);
        setLocationAndSize(true);
        if (isStackWindow) {
            IjxStackWindow sw = (IjxStackWindow) this;
            int stackSize = imp.getStackSize();
            int nScrollbars = sw.getNScrollbars();
            if (stackSize == 1 && nScrollbars > 0) {
                sw.removeScrollbars();
            } else if (stackSize > 1 && nScrollbars == 0) {
                sw.addScrollbars(imp);
            }
        }
        pack();
        repaint();
        maxBounds = getMaximumBounds();
        setMaximizedBounds(maxBounds);
        setMaxBoundsTime(System.currentTimeMillis());
    }

    public IjxImageCanvas getCanvas() {
        return ic;
    }

    public Rectangle getMaximumBounds() {
        double width = imp.getWidth();
        double height = imp.getHeight();
        double iAspectRatio = width / height;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle maxWindow = ge.getMaximumWindowBounds();
        maxWindowBounds = maxWindow;
        if (iAspectRatio / ((double) maxWindow.width / maxWindow.height) > 0.75) {
            maxWindow.y += 22;  // uncover ImageJ menu bar
            maxWindow.height -= 22;
        }
        Dimension extraSize = getExtraSize();
        double maxWidth = maxWindow.width - extraSize.width;
        double maxHeight = maxWindow.height - extraSize.height;
        double mAspectRatio = maxWidth / maxHeight;
        int wWidth, wHeight;
        double mag;
        if (iAspectRatio >= mAspectRatio) {
            mag = maxWidth / width;
            wWidth = maxWindow.width;
            wHeight = (int) (height * mag + extraSize.height);
        } else {
            mag = maxHeight / height;
            wHeight = maxWindow.height;
            wWidth = (int) (width * mag + extraSize.width);
        }
        int xloc = (int) (maxWidth - wWidth) / 2;
        if (xloc < 0) {
            xloc = 0;
        }
        return new Rectangle(xloc, maxWindow.y, wWidth, wHeight);
    }

    Dimension getExtraSize() {
        Insets insets = getInsets();
        int extraWidth = insets.left + insets.right + 10;
        int extraHeight = insets.top + insets.bottom + 10;
        if (extraHeight == 20) {
            extraHeight = 42;
        }
        int members = displayWindow.getComponentCount();
        //if (IJ.debugMode) IJ.log("getExtraSize: "+members+" "+insets);
        for (int i = 1; i < members; i++) {
            Component m = displayWindow.getComponent(i);
            Dimension d = m.getPreferredSize();
            extraHeight += d.height + 5;
            if (IJ.debugMode) {
                IJ.log(i + "  " + d.height + " " + extraHeight);
            }
        }
        return new Dimension(extraWidth, extraHeight);
    }

    public Component add(Component comp) {
        comp = displayWindow.add(comp);
        maxBounds = getMaximumBounds();
        //if (!IJ.isLinux()) {
        setMaximizedBounds(maxBounds);
        setMaxBoundsTime(System.currentTimeMillis());
        //}
        return comp;
    }

    //public void setMaximizedBounds(Rectangle r) {
    //	super.setMaximizedBounds(r);
    //	IJ.log("setMaximizedBounds: "+r+" "+getMaximizedBounds());
    //	if (getMaximizedBounds().x==0)
    //		throw new IllegalArgumentException("");
    //}
    public void maximize() {
        if (maxBounds == null) {
            return;
        }
        int width = imp.getWidth();
        int height = imp.getHeight();
        double aspectRatio = (double) width / height;
        Dimension extraSize = getExtraSize();
        int extraHeight = extraSize.height;
        double mag = (double) (maxBounds.height - extraHeight) / height;
        if (IJ.debugMode) {
            IJ.log("maximize: " + mag + " " + ic.getMagnification() + " " + maxBounds);
        }
        if (Frame.class.isAssignableFrom(displayWindow.getClass())) {
            setSize(((Frame) displayWindow).getMaximizedBounds().width, ((Frame) displayWindow).getMaximizedBounds().height);
        }
        if (displayWindow instanceof JInternalFrame) {
            try {
                ((JInternalFrame) displayWindow).setMaximum(true);
            } catch (PropertyVetoException ex) {
            }
        }
        if (mag > ic.getMagnification() || aspectRatio < 0.5 || aspectRatio > 2.0) {
            ic.setMagnification2(mag);
            ic.setSrcRect(new Rectangle(0, 0, width, height));
            ic.setDrawingSize((int) (width * mag), (int) (height * mag));
            validate();
            unzoomWhenMinimizing = true;
        } else {
            unzoomWhenMinimizing = false;
        }
    }

    public void minimize() {
        if (unzoomWhenMinimizing) {
            ic.unzoom();
        }
        unzoomWhenMinimizing = true;
    }

    // These are the actual methods invoked by events emitted by JInternalFrames, Windows, FocusManager...

    /*  Regarding FocusGained and WindowsActivated...
    WindowsActivated window (frame or dialog) — This window is either the focused window,
    or owns the focused window.  Since 1.4, WindowFocusListener and WindowStateListener are preferred.
    Note: Not all window managers/native platforms support all window states. The 1.4 java.awt.Toolkit
    method isFrameStateSupported(int) can be used to determine whether a particular window state is
    supported by a particular window manager.
     */
    public void windowActivatedorGainedFocus(WindowEvent e) {
        if (IJ.debugMode) {
            IJ.log("windowActivated: " + imp.getTitle());
        }
        //ImageJ ij = IJ.getInstance();
        IjxApplication ij = IJ.getInstance();
        boolean quitting = ij != null && ij.quitting();
        // @todo Deal with Mac Menu issues here
        if (IJ.isMacintosh() && ij != null && ij.getApplet() == null && !quitting) {
            IjxMenus menus = CentralLookup.getDefault().lookup(IjxMenus.class);
            IJ.wait(10); // may be needed for Java 1.4 on OS X
            //setMenuBar(menus.getMenuBar());
        }
        if (imp == null) {
            System.err.println("imp==null in AbstractImageWindow");
            return;
        }
        imp.setActivated(); // notify IjxImagePlus that image has been activated
        if (!closed && !quitting && !Interpreter.isBatchMode()) {
            WindowManager.setCurrentWindow(this);
        }
        // @todo   Frame?? Ok for now...
        Frame channels = Channels.getInstance();
        if (channels != null && imp.isComposite()) {
            ((Channels) channels).update();
        }
    }

    /** Has this window been closed? */
    public boolean isClosed() {
        return closed;
    }

    public void windowOrIFrameClosing(WindowEvent e) {
        //IJ.log("windowClosing: "+imp.getTitle()+" "+closed);
        if (closed) {
            return;
        }
        if (ij != null) {
            WindowManager.setCurrentWindow(this);
            IJ.doCommand("Close");
        } else {
            setVisible(false);
            dispose();
            WindowManager.removeWindow(this);
        }
    }

    // Listeners.................................................................
    public void focusGained(FocusEvent e) {
        //IJ.log("focusGained: "+imp.getTitle());
        if (!Interpreter.isBatchMode() && ij != null && !ij.quitting()) {
            WindowManager.setCurrentWindow(this);
        }
    }

    public void focusLost(FocusEvent e) {
    }

// <editor-fold defaultstate="collapsed" desc=" WindowListener">
    private void setListenersOnWindow(Frame w) {
        w.addFocusListener(this);
        w.addWindowFocusListener(this);
        ((Window) w).addWindowListener(this);
        w.addWindowStateListener(this);
        w.addKeyListener(keyHandler);
        w.setFocusTraversalKeysEnabled(false);
        //if (!(this instanceof IjxStackWindow)) {
        if (!(IjxStackWindow.class.isAssignableFrom(this.getClass()))) {
            w.addMouseWheelListener(this);
        }
        w.setResizable(true);
    }

    public void windowGainedFocus(WindowEvent e) {
        windowActivatedorGainedFocus(e);
    }

    public void windowLostFocus(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
        windowActivatedorGainedFocus(e);
    }

    public void windowClosing(WindowEvent e) {
        windowOrIFrameClosing(e);
    }

    public void windowStateChanged(WindowEvent e) {
        int oldState = e.getOldState();
        int newState = e.getNewState();
        //IJ.log("WSC: "+getBounds()+" "+oldState+" "+newState);
        if ((oldState & Frame.MAXIMIZED_BOTH) == 0 && (newState & Frame.MAXIMIZED_BOTH) != 0) {
            maximize();
        } else if ((oldState & Frame.MAXIMIZED_BOTH) != 0 && (newState & Frame.MAXIMIZED_BOTH) == 0) {
            minimize();
        }
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc=" InternalFrameListener ">
    // @todo: !!! Implement for internal frame
    private void setListenersOnInternalWindow(JInternalFrame w) {
        w.addFocusListener(this);
        w.addInternalFrameListener(this);
        //displayWindow.addWindowStateListener(this);
        w.addKeyListener(keyHandler);
        w.setFocusTraversalKeysEnabled(false);
        if (!(this instanceof IjxStackWindow)) {
            w.addMouseWheelListener(this);
        }
        w.setResizable(true);
    }

    public void internalFrameClosing(InternalFrameEvent e) {
        //IJ.log("windowClosing: "+imp.getTitle()+" "+closed);
        if (closed) {
            return;
        }
        if (ij != null) {
            WindowManager.setCurrentWindow(this);
            IJ.doCommand("Close");
        } else {
            setVisible(false);
            dispose();
            WindowManager.removeWindow(this);
        }
    }

    public void internalFrameClosed(InternalFrameEvent e) {
    }

    public void internalFrameOpened(InternalFrameEvent e) {
    }

    public void internalFrameIconified(InternalFrameEvent e) {
    }

    public void internalFrameDeiconified(InternalFrameEvent e) {
    }

    public void internalFrameActivated(InternalFrameEvent e) {
        windowActivatedorGainedFocus(null);
    }

    public void internalFrameDeactivated(InternalFrameEvent e) {
    }

// </editor-fold>
    // Pans up/down, left/right if spacebar pressed
    public void mouseWheelMoved(MouseWheelEvent event) {
        int rotation = event.getWheelRotation();
        int width = imp.getWidth();
        int height = imp.getHeight();
        Rectangle srcRect = ic.getSrcRect();
        int xstart = srcRect.x;
        int ystart = srcRect.y;
        if (IJ.spaceBarDown() || srcRect.height == height) {
            srcRect.x += rotation * Math.max(width / 200, 1);
            if (srcRect.x < 0) {
                srcRect.x = 0;
            }
            if (srcRect.x + srcRect.width > width) {
                srcRect.x = width - srcRect.width;
            }
        } else {
            srcRect.y += rotation * Math.max(height / 200, 1);
            if (srcRect.y < 0) {
                srcRect.y = 0;
            }
            if (srcRect.y + srcRect.height > height) {
                srcRect.y = height - srcRect.height;
            }
        }
        if (srcRect.x != xstart || srcRect.y != ystart) {
            ic.repaint();
        }
    }

    /** Copies the current ROI to the clipboard. The entire
    image is copied if there is no ROI. */
    public void copy(boolean cut) {
        imp.copy(cut);
    }

    public void paste() {
        imp.paste();
    }

    /** This method is called by ImageCanvas.mouseMoved(MouseEvent).
    @see ij.gui.ImageCanvas#mouseMoved
     */
    public void mouseMoved(int x, int y) {
        imp.mouseMoved(x, y);
    }

    public String toString() {
        return imp.getTitle();
    }

    /** Moves and resizes this window. Changes the
    magnification so the image fills the window. */
    public void setLocationAndSize(int x, int y, int width, int height) {
        displayWindow.setBounds(x, y, width, height);
        getCanvas().fitToWindow();
        pack();
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean b) {
        running = b;
    }

    public boolean isRunning2() {
        return running2;
    }

    public void setRunning2(boolean b) {
        running2 = b;
    }

    public boolean canClose() {
        return true;
    }

    public void setMaxBoundsTime(long time) {
        maxBoundsTime = time;
    }

    public long getMaxBoundsTime() {
        return maxBoundsTime;
    }

    /** Overrides the setBounds() method in Component so
    we can find out when the window is resized. */
    //public void setBounds(int x, int y, int width, int height)	{
    //	super.setBounds(x, y, width, height);
    //	ic.resizeSourceRect(width, height);
    //}
    public void setBounds(Rectangle r) {
        if (Frame.class.isAssignableFrom(displayWindow.getClass())) {
            ((Frame) displayWindow).setBounds(r);
        }
        if (displayWindow instanceof JInternalFrame) {
            ((JInternalFrame) displayWindow).setBounds(r);
        }
    }

    public void setMaximizedBounds(Rectangle r) {
        if (Frame.class.isAssignableFrom(displayWindow.getClass())) {
            ((Frame) displayWindow).setMaximizedBounds(maxBounds);
        }
        // @todo -- does this work correctly...?
        if (displayWindow instanceof JInternalFrame) {
            ((JInternalFrame) displayWindow).setMaximumSize(new Dimension(r.width, r.height));
        }
    }

    public void repaint() {
        displayWindow.repaint();
    }

    public void setForeground(Color c) {
        displayWindow.setForeground(c);
    }

    public void setBackground(Color c) {
        displayWindow.setBackground(c);
    }

    public void setSize(int w, int h) {
        this.displayWindow.setSize(w, h);
    }

    public void validate() {
        displayWindow.validate();
    }

    public String getTitle() {
        if (Frame.class.isAssignableFrom(displayWindow.getClass())) {
            return ((Frame) displayWindow).getTitle();
        }
        if (displayWindow instanceof JInternalFrame) {
            return ((JInternalFrame) displayWindow).getTitle();
        }
        return "";
    }

    public void setTitle(String s) {
        if (Frame.class.isAssignableFrom(displayWindow.getClass())) {
            ((Frame) displayWindow).setTitle(s);
        }
        if (displayWindow instanceof JInternalFrame) {
            ((JInternalFrame) displayWindow).setTitle(title);
        }
    }

    public ImageIcon getImageIcon() {
        if (Frame.class.isAssignableFrom(displayWindow.getClass())) {
            return new ImageIcon(((Frame) displayWindow).getIconImage());
        }
        if (displayWindow instanceof JInternalFrame) {
            return ((ImageIcon) ((JInternalFrame) displayWindow).getFrameIcon());
        }
        return null;
    }

    public boolean isVisible() {
        return displayWindow.isVisible();
    }

    public void setVisible(boolean b) {
        displayWindow.setVisible(b);
    }

    public Dimension getSize() {
        return displayWindow.getSize();
    }

    public Point getLocation() {
        return displayWindow.getLocation();
    }

    public Point getLocationOnScreen() {
        return displayWindow.getLocationOnScreen();
    }

    public Rectangle getBounds() {
        return displayWindow.getBounds();
    }

    public void setLocation(int x, int y) {
        displayWindow.setLocation(x, y);
    }

    public void setLocation(Point p) {
        displayWindow.setLocation(p);
    }

    public void dispose() {
        if (Frame.class.isAssignableFrom(displayWindow.getClass())) {
            // awt.Frame or swing.JFrame
            ((Frame) displayWindow).dispose();
        }
        if (displayWindow instanceof JInternalFrame) {
            // swing.JInternalFrame
            ((JInternalFrame) displayWindow).hide();
        }
    }
}
//class IjxImageWindow

