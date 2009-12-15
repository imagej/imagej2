package ijx.gui;

import ij.gui.*;
import ijx.gui.ImageCanvasSwing;
import ijx.gui.IjxStackWindow;
import ijx.gui.IjxImageWindow;
import ijx.gui.IjxImageCanvas;
import ijx.IjxApplication;
import ijx.IjxImagePlus;
import java.awt.*;
import java.awt.event.*;
import ij.*;
import ij.io.*;
import ij.measure.*;
import ij.plugin.frame.*;
import ij.macro.Interpreter;
import ijx.IjxImageStack;
import javax.swing.JFrame;

/** A frame for displaying images. */
public class ImageWindowSwing extends JFrame implements IjxImageWindow {

  protected IjxImagePlus imp;
  protected IjxApplication ij;
  protected IjxImageCanvas ic;
  private double initialMagnification = 1;
  private int newWidth,  newHeight;
  protected boolean closed;
  private boolean newCanvas;
  private boolean unzoomWhenMinimizing = true;
  Rectangle maxWindowBounds; // largest possible window on this screen
  Rectangle maxBounds; // Size of this window after it is maximized
  long setMaxBoundsTime;
  private static final int XINC = 8;
  private static final int YINC = 12;
  private static final int TEXT_GAP = 10;
  private static int xbase = -1;
  private static int ybase;
  private static int xloc;
  private static int yloc;
  private static int count;
  private int textGap = IJ.isCenterNextImage() ? 0 : TEXT_GAP;
  /** This variable is set false if the user presses the escape key or closes the window. */
  private boolean running;
  /** This variable is set false if the user clicks in this
  window, presses the escape key, or closes the window. */
  private boolean running2;

  public ImageWindowSwing(String title) {
    super(title);
  }

  public ImageWindowSwing(IjxImagePlus imp) {
    this(imp, null);
  }

  public ImageWindowSwing(IjxImagePlus imp, IjxImageCanvas ic) {
    super(imp.getTitle());
    if (Prefs.blackCanvas && getClass().getName().equals("ij.gui.ImageWindow")) {
      setForeground(Color.white);
      setBackground(Color.black);
    } else {
      setForeground(Color.black);
      if (IJ.isLinux()) {
        setBackground(IJ.backgroundColor);
      } else {
        setBackground(Color.white);
      }
    }
    ij = IJ.getInstance();
    this.imp = imp;
    if (ic == null) {
      ic = new ImageCanvasSwing(imp);
      newCanvas = true;
    }
    this.ic = ic;
    ImageWindowSwing previousWindow = (ImageWindowSwing) imp.getWindow();
    setLayout(new ImageLayout(ic));
    add((ImageCanvasSwing)ic);
    addFocusListener(this);
    addWindowListener(this);
    addWindowStateListener(this);
    addKeyListener(ij);
    setFocusTraversalKeysEnabled(false);
    if (!(this instanceof IjxStackWindow)) {
      addMouseWheelListener(this);
    }
    setResizable(true);
    WindowManager.addWindow(this);
    imp.setWindow(this);
    if (previousWindow != null) {
      if (newCanvas) {
        setLocationAndSize(false);
      } else {
        ic.update(previousWindow.getCanvas());
      }
      Point loc = previousWindow.getLocation();
      setLocation(loc.x, loc.y);
      if (!(this instanceof IjxStackWindow)) {
        pack();
        show();
      }
      boolean unlocked = imp.lockSilently();
      boolean changes = imp.wasChanged();
      imp.setChanged(false);
      previousWindow.close();
      imp.setChanged(changes);
      if (unlocked) {
        imp.unlock();
      }
      WindowManager.setCurrentWindow(this);
    } else {
      setLocationAndSize(false);
      if (ij != null && !IJ.isMacintosh()) {
        Image img = IJ.getTopComponentFrame().getIconImage();
        if (img != null) {
          try {
            setIconImage(img);
          } catch (Exception e) {
          }
        }
      }
      if (IJ.isCenterNextImage()) {
        GUI.center(this);
        IJ.setCenterOnScreen(false);
      }
      if (Interpreter.isBatchMode()) {
        WindowManager.setTempCurrentImage(imp);
        Interpreter.addBatchModeImage(imp);
      } else {
        show();
      }
    }
  }

  private void setLocationAndSize(boolean updating) {
    int width = imp.getWidth();
    int height = imp.getHeight();
    Rectangle maxWindow = getMaxWindow();
    if (maxWindow.x == maxWindow.width) // work around for Linux bug
    {
      maxWindow = new Rectangle(0, maxWindow.y, maxWindow.width, maxWindow.height);
    }
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

    int sliderHeight = (this instanceof IjxStackWindow) ? 20 : 0;
    int screenHeight = maxWindow.height - sliderHeight;
    double mag = 1;
    while (xbase + XINC * 4 + width * mag > maxWindow.width || ybase + height * mag > screenHeight) {
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
      setLocation(x, y);
    }
    if (Prefs.open100Percent && ic.getMagnification() < 1.0) {
      while (ic.getMagnification() < 1.0) {
        ic.zoomIn(0, 0);
      }
      setSize(Math.min(width, maxWindow.width - x), Math.min(height, screenHeight - y));
      validate();
    } else {
      pack();
    }
  }

  Rectangle getMaxWindow() {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    Rectangle maxWindow = ge.getMaximumWindowBounds();
    Dimension ijSize = ij != null ? IJ.getTopComponentFrame().getSize() : new Dimension(0, 0);
    if (maxWindow.height > 600) {
      maxWindow.y += ijSize.height;
      maxWindow.height -= ijSize.height;
    }
    return maxWindow;
  }

  public double getInitialMagnification() {
    return initialMagnification;
  }

  /** Override Container getInsets() to make room for some text above the image. */
  public Insets getInsets() {
    Insets insets = super.getInsets();
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
      Insets insets = super.getInsets();
      if (imp.isComposite()) {
        CompositeImage ci = (CompositeImage) imp;
        if (ci.getMode() == CompositeImage.COMPOSITE) {
          g.setColor(ci.getChannelColor());
        }
      }
      g.drawString(createSubtitle(), 5, insets.top + TEXT_GAP);
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
      if ((this instanceof IjxStackWindow) && isRunning2()) {
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
      s += IJ.d2s(imp.getWidth() * cal.pixelWidth, 2) + "x" + IJ.d2s(imp.getHeight() * cal.pixelHeight, 2) + " " + cal.getUnits() + " (" + imp.getWidth() + "x" + imp.getHeight() + "); ";
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

  public void paint(Graphics g) {
    //if (IJ.debugMode) IJ.log("wPaint: " + imp.getTitle());
    drawInfo(g);
    Rectangle r = ic.getBounds();
    int extraWidth = MIN_WIDTH - r.width;
    int extraHeight = MIN_HEIGHT - r.height;
    if (extraWidth <= 0 && extraHeight <= 0 && !Prefs.noBorder && !IJ.isLinux()) {
      g.drawRect(r.x - 1, r.y - 1, r.width + 1, r.height + 1);
    }
  }

  public void close() {
    setVisible(false);
    dispose();
    WindowManager.removeWindow(this);
  }

  /** Removes this window from the window list and disposes of it.
  Returns false if the user cancels the "save changes" dialog. */
  public boolean canClose() {
    boolean isRunning = isRunning() || isRunning2();
    setRunning(false);
    setRunning2(false);

    if (isRunning()) {
      IJ.wait(500);
    }
    if (ij == null || IJ.getApplet() != null || Interpreter.isBatchMode() || IJ.macroRunning()) {
      imp.setChanged(false);
    }
    if (imp.wasChanged()) {
      String msg;
      String name = imp.getTitle();
      if (name.length() > 22) {
        msg = "Save changes to\n" + "\"" + name + "\"?";
      } else {
        msg = "Save changes to \"" + name + "\"?";
      }
      YesNoCancelDialog d = new YesNoCancelDialog(this, "ImageJ", msg);
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
    setVisible(false);
    if (ij != null && ij.quitting()) // this may help avoid thread deadlocks
    {
      return true;
    }
    dispose();
    imp.flush();
    return true;
  }

  public IjxImagePlus getImagePlus() {
    return imp;
  }

  void setImagePlus(IjxImagePlus imp) {
    this.imp = imp;
    repaint();
  }

  public void updateImage(IjxImagePlus imp) {
    if (imp != this.imp) {
      throw new IllegalArgumentException("imp!=this.imp");
    }
    this.imp = imp;
    ic.updateImage(imp);
    setLocationAndSize(true);
    pack();
    repaint();
    maxBounds = getMaximumBounds();
    //if (!IJ.isLinux()) {
    setMaximizedBounds(maxBounds);
    setMaxBoundsTime = System.currentTimeMillis();
  //}
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
    int members = getComponentCount();
    if (IJ.debugMode) {
      IJ.log("getExtraSize: " + members + " " + insets);
    }
    for (int i = 1; i < members; i++) {
      Component m = getComponent(i);
      Dimension d = m.getPreferredSize();
      extraHeight += d.height + 5;
      if (IJ.debugMode) {
        IJ.log(i + "  " + d.height + " " + extraHeight);
      }
    }
    return new Dimension(extraWidth, extraHeight);
  }

  public Component add(Component comp) {
    comp = super.add(comp);
    if (IJ.debugMode) {
      IJ.log("add: " + comp);
    }
    maxBounds = getMaximumBounds();
    //if (!IJ.isLinux()) {
    setMaximizedBounds(maxBounds);
    setMaxBoundsTime = System.currentTimeMillis();
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
    setSize(getMaximizedBounds().width, getMaximizedBounds().height);
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

  /** Has this window been closed? */
  public boolean isClosed() {
    return closed;
  }

  public void focusGained(FocusEvent e) {
    //IJ.log("focusGained: "+imp.getTitle());
    if (!Interpreter.isBatchMode() && ij != null && !ij.quitting()) {
      WindowManager.setCurrentWindow(this);
    }
  }

  public void windowActivated(WindowEvent e) {
    if (IJ.debugMode) {
      IJ.log("windowActivated: " + imp.getTitle());
    }
    IjxApplication ij = IJ.getInstance();
    boolean quitting = ij != null && ij.quitting();
    if (IJ.isMacintosh() && ij != null && !quitting) {
      IJ.wait(10); // may be needed for Java 1.4 on OS X
      setMenuBar(Menus.getMenuBar());
    }
    imp.setActivated(); // notify ImagePlus that image has been activated
    if (!closed && !quitting && !Interpreter.isBatchMode()) {
      WindowManager.setCurrentWindow(this);
    }
    Frame channels = Channels.getInstance();
    if (channels != null && imp.isComposite()) {
      ((Channels) channels).update();
    }
  }

  public void windowClosing(WindowEvent e) {
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

  public void focusLost(FocusEvent e) {
  }

  public void windowDeiconified(WindowEvent e) {
  }

  public void windowIconified(WindowEvent e) {
  }

  public void windowOpened(WindowEvent e) {
  }

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
    setBounds(x, y, width, height);
    getCanvas().fitToWindow();
    pack();
  }

  public boolean isRunning() {
    return running;
  }

  public void setRunning(boolean b) {
    this.running = b;
  }

  public boolean isRunning2() {
    return running2;
  }

  public void setRunning2(boolean b) {
    this.running2 = b;
  }

  @Override
  public void setMaxBoundsTime(long time) {
    setMaxBoundsTime = time;
  }

  @Override
  public long getMaxBoundsTime() {
    return setMaxBoundsTime;
  }
  /** Overrides the setBounds() method in Component so
  we can find out when the window is resized. */
  //public void setBounds(int x, int y, int width, int height)	{
  //	super.setBounds(x, y, width, height);
  //	ic.resizeSourceRect(width, height);
  //}
} //class ImageWindow

