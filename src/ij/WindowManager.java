package ij;

import ijx.gui.IjxImageWindow;
import ijx.IjxApplication;
import ijx.IjxImagePlus;
import ij.plugin.Converter;
import ij.plugin.frame.Recorder;
import ij.macro.Interpreter;
import ij.text.TextWindow;
import ij.plugin.frame.PlugInFrame;
import java.awt.*;
import java.util.*;
import ij.gui.*;
import ijx.gui.IjxWindow;

/**
 * WindowManager: New window manager that handles objects using IJX interfaces
 * Modified to managed IJX interfaces by Grant B. Harris, November 2008, at ImageJ 2008, Luxembourg
 **/
/** This class consists of static methods used to manage ImageJ's windows. */
public class WindowManager {

  public static boolean checkForDuplicateName;
  private static Vector imageList = new Vector();		 // list of image windows
  private static Vector nonImageList = new Vector();	 // list of non-image windows
  private static IjxWindow currentWindow;			 // active image window
  private static IjxWindow frontWindow;
  private static Hashtable tempImageTable = new Hashtable();

  private WindowManager() {
  }

  /** Makes the image contained in the specified window the active image. */
  public static void setCurrentWindow(IjxWindow win) {
    if (win == null || win.isClosed()) // deadlock-"wait to lock"
    {
      return;
    }
    if (win instanceof IjxImageWindow) {
      if (((IjxImageWindow) win).getImagePlus() == null) {
        return;
      //IJ.log("setCurrentWindow: "+win.getImagePlus().getTitle()+" ("+(currentWindow!=null?currentWindow.getImagePlus().getTitle():"null") + ")");
      }
    }
    setWindow(win);
    tempImageTable.remove(Thread.currentThread());
    if (win == currentWindow || imageList.size() == 0) {
      return;
    }
    if (currentWindow != null) {
      // free up pixel buffers used by current window
      IjxImagePlus imp = ((IjxImageWindow) currentWindow).getImagePlus();
      if (imp != null) {
        imp.trimProcessor();
        imp.saveRoi();
      }
    }
    Undo.reset();
    currentWindow = win;
    Menus.updateMenus();
  }

  /** Returns the active ImageWindow. */
  public static IjxWindow getCurrentWindow() {
    //if (IJ.debugMode) IJ.write("ImageWindow.getCurrentWindow");
    return currentWindow;
  }

  public static int getCurrentIndex() {
    return imageList.indexOf(currentWindow);
  }

  /** Returns a reference to the active image or null if there isn't one. */
  public static ImagePlus getCurrentImage() {
    ImagePlus img = (ImagePlus) tempImageTable.get(Thread.currentThread());
    //String str = (img==null)?" null":"";
    if (img == null) {
      img = (ImagePlus) getActiveImage();
    //if (img!=null) IJ.log("getCurrentImage: "+img.getTitle()+" "+Thread.currentThread().hashCode()+str);
    }
    return img;
  }

  public static IjxImagePlus getCurrentImageX() {
    IjxImagePlus img = (IjxImagePlus) tempImageTable.get(Thread.currentThread());
    //String str = (img==null)?" null":"";
    if (img == null) {
      img = getActiveImage();
    //if (img!=null) IJ.log("getCurrentImage: "+img.getTitle()+" "+Thread.currentThread().hashCode()+str);
    }
    return img;
  }

  /** Makes the specified image temporarily the active
  image for this thread. Call again with a null
  argument to revert to the previous active image. */
  public static void setTempCurrentImage(IjxImagePlus img) {
    //IJ.log("setTempImage: "+(img!=null?img.getTitle():"null ")+Thread.currentThread().hashCode());
    if (img == null) {
      tempImageTable.remove(Thread.currentThread());
    } else {
      tempImageTable.put(Thread.currentThread(), img);
    }
  }

  /** Sets a temporary image for the specified thread. */
  public static void setTempCurrentImage(Thread thread, IjxImagePlus img) {
    if (thread == null) {
      throw new RuntimeException("thread==null");
    }
    if (img == null) {
      tempImageTable.remove(thread);
    } else {
      tempImageTable.put(thread, img);
    }
  }

  /** Returns the active ImagePlus. */
  private static IjxImagePlus getActiveImage() {
    if (currentWindow != null) {
      if (currentWindow instanceof IjxImageWindow) {
        return ((IjxImageWindow) currentWindow).getImagePlus();
      } else if (frontWindow != null && (frontWindow instanceof IjxImageWindow)) {
        return ((IjxImageWindow) frontWindow).getImagePlus();
      } else if (imageList.size() > 0) {
        IjxImageWindow win = (IjxImageWindow) imageList.elementAt(imageList.size() - 1);
        return win.getImagePlus();
      } else {
        return Interpreter.getLastBatchModeImage();
      }
    }
    return null;
  }

  /** Returns the number of open image windows. */
  public static int getWindowCount() {
    int count = imageList.size();
    return count;
  }

  /** Returns the number of open images. */
  public static int getImageCount() {
    int count = imageList.size();
    count += Interpreter.getBatchModeImageCount();
    if (count == 0 && getCurrentImage() != null) {
      count = 1;
    }
    return count;
  }

  /** Returns the front most window or null. */
  public static IjxWindow getFrontWindow() {
    return frontWindow;
  }

  /** Returns a list of the IDs of open images. Returns
  null if no windows are open. */
  public synchronized static int[] getIDList() {
    int nWindows = imageList.size();
    int[] batchModeImages = Interpreter.getBatchModeImageIDs();
    int nBatchImages = batchModeImages.length;
    if ((nWindows + nBatchImages) == 0) {
      return null;
    }
    int[] list = new int[nWindows + nBatchImages];
    for (int i = 0; i < nBatchImages; i++) {
      list[i] = batchModeImages[i];
    }
    int index = 0;
    for (int i = nBatchImages; i < nBatchImages + nWindows; i++) {
      IjxImageWindow win = (IjxImageWindow) imageList.elementAt(index++);
      list[i] = win.getImagePlus().getID();
    }
    return list;
  }

  /** Returns an array containing a list of the non-image windows. */
  public synchronized static IjxWindow[] getNonImageWindows() {
    IjxWindow[] list = new IjxWindow[nonImageList.size()];
    nonImageList.copyInto((IjxWindow[]) list);
    return list;
  }

  /** For IDs less than zero, returns the ImagePlus with the specified ID.
  Returns null if no open window has a matching ID or no images are open.
  For IDs greater than zero, returns the Nth ImagePlus. Returns null if
  the ID is zero. */
  public synchronized static IjxImagePlus getImage(int imageID) {
    //if (IJ.debugMode) IJ.write("ImageWindow.getImage");
    if (imageID > 0) {
      imageID = getNthImageID(imageID);
    }
    if (imageID == 0 || getImageCount() == 0) {
      return null;
    }
    IjxImagePlus imp2 = Interpreter.getBatchModeImage(imageID);
    if (imp2 != null) {
      return imp2;
    }
    IjxImagePlus imp = null;
    for (int i = 0; i < imageList.size(); i++) {
      IjxImageWindow win = (IjxImageWindow) imageList.elementAt(i);
      imp2 = win.getImagePlus();
      if (imageID == imp2.getID()) {
        imp = imp2;
        break;
      }
    }
    imp2 = getCurrentImage();
    if (imp == null && imp2 != null && imp2.getID() == imageID) {
      return imp2;
    }
    return imp;
  }

  /** Returns the ID of the Nth open image. Returns zero if n<=0
  or n greater than the number of open image windows. */
  public synchronized static int getNthImageID(int n) {
    if (n <= 0) {
      return 0;
    }
    if (Interpreter.isBatchMode()) {
      int[] list = getIDList();
      if (n > list.length) {
        return 0;
      } else {
        return list[n - 1];
      }
    } else {
      if (n > imageList.size()) {
        return 0;
      }
      IjxImageWindow win = (IjxImageWindow) imageList.elementAt(n - 1);
      if (win != null) {
        return win.getImagePlus().getID();
      } else {
        return 0;
      }
    }
  }

  /** Returns the first image that has the specified title or null if it is not found. */
  public synchronized static IjxImagePlus getImage(String title) {
    int[] wList = getIDList();
    if (wList == null) {
      return null;
    }
    for (int i = 0; i < wList.length; i++) {
      IjxImagePlus imp = getImage(wList[i]);
      if (imp != null && imp.getTitle().equals(title)) {
        return imp;
      }
    }
    return null;
  }

  /** Adds the specified window to the Window menu. */
  public synchronized static void addWindow(IjxWindow win) {
    //IJ.write("addWindow: "+win.getTitle());
    if (win == null) {
      return;
    } else if (win instanceof IjxImageWindow) {
      addImageWindow((IjxImageWindow) win);
    } else {
      Menus.insertWindowMenuItem(win);
      nonImageList.addElement(win);
    }
  }

  private static void addImageWindow(IjxImageWindow win) {
    IjxImagePlus imp = win.getImagePlus();
    if (imp == null) {
      return;
    }
    checkForDuplicateName(imp);
    imageList.addElement(win);
    Menus.addWindowMenuItem(imp);
    setCurrentWindow(win);
  }

  static void checkForDuplicateName(IjxImagePlus imp) {
    if (checkForDuplicateName) {
      String name = imp.getTitle();
      if (isDuplicateName(name)) {
        imp.setTitle(getUniqueName(name));
      }
    }
    checkForDuplicateName = false;
  }

  static boolean isDuplicateName(String name) {
    int n = imageList.size();
    for (int i = 0; i < n; i++) {
      IjxImageWindow win = (IjxImageWindow) imageList.elementAt(i);
      String name2 = win.getImagePlus().getTitle();
      if (name.equals(name2)) {
        return true;
      }
    }
    return false;
  }

  /** Returns a unique name by adding, before the extension,  -1, -2, etc. as needed. */
  public static String getUniqueName(String name) {
    String name2 = name;
    String extension = "";
    int len = name2.length();
    int lastDot = name2.lastIndexOf(".");
    if (lastDot != -1 && len - lastDot < 6 && lastDot != len - 1) {
      extension = name2.substring(lastDot, len);
      name2 = name2.substring(0, lastDot);
    }
    int lastDash = name2.lastIndexOf("-");
    if (lastDash != -1 && name2.length() - lastDash < 4) {
      name2 = name2.substring(0, lastDash);
    }
    for (int i = 1; i <= 99; i++) {
      String name3 = name2 + "-" + i + extension;
      //IJ.log(i+" "+name3);
      if (!isDuplicateName(name3)) {
        return name3;
      }
    }
    return name;
  }

  /** If 'name' is not unique, adds -1, -2, etc. as needed to make it unique. */
  public static String makeUniqueName(String name) {
    return isDuplicateName(name) ? getUniqueName(name) : name;
  }

  /** Removes the specified window from the Window menu. */
  public synchronized static void removeWindow(IjxWindow win) {
    //IJ.write("removeWindow: "+win.getTitle());
    if (win instanceof IjxImageWindow) {
      removeImageWindow((IjxImageWindow) win);
    } else {
      int index = nonImageList.indexOf(win);
      IjxApplication ij = IJ.getInstance();
      if (index >= 0) {
        //if (ij!=null && !ij.quitting())
        Menus.removeWindowMenuItem(index);
        nonImageList.removeElement(win);
      }
    }
    setWindow(null);
  }

  private static void removeImageWindow(IjxImageWindow win) {
    int index = imageList.indexOf(win);
    if (index == -1) {
      return;  // not on the window list
    }
    if (imageList.size() > 1) {
      int newIndex = index - 1;
      if (newIndex < 0) {
        newIndex = imageList.size() - 1;
      }
      setCurrentWindow((IjxImageWindow) imageList.elementAt(newIndex));
    } else {
      currentWindow = null;
    }
    imageList.removeElementAt(index);
    setTempCurrentImage(null);  //???
    int nonImageCount = nonImageList.size();
    if (nonImageCount > 0) {
      nonImageCount++;
    }
    Menus.removeWindowMenuItem(nonImageCount + index);
    Menus.updateMenus();
    Undo.reset();
  }

  /** The specified IjxWindow becomes the front window, the one returnd by getFrontWindow(). */
  public static void setWindow(IjxWindow win) {
    /*
    if (Recorder.record && win!=null && win!=frontWindow) {
    String title = win.getTitle();
    IJ.log("Set window: "+title+"  "+(getFrame(title)!=null?"not null":"null"));
    if (getFrame(title)!=null && !title.equals("Recorder"))
    Recorder.record("selectWindow", title);
    }
     */
    frontWindow = win;
  //IJ.log("Set window: "+(win!=null?win.getTitle():"null"));
  }

  /** Closes all windows. Stops and returns false if any image "save changes" dialog is canceled. */
  public synchronized static boolean closeAllWindows() {
    while (imageList.size() > 0) {
      if (!((IjxImageWindow) imageList.elementAt(0)).canClose()) {
        return false;
      }
      IJ.wait(100);
    }
    IjxApplication ij = IJ.getInstance();
    if (ij != null && ij.quitting() && IJ.getApplet() == null) {
      return true;
    }
    IjxWindow[] list = getNonImageWindows();
    for (int i = 0; i < list.length; i++) {
      IjxWindow frame = list[i];
      frame.close();
      frame.setVisible(false);
      frame.dispose();
//      if (frame instanceof PlugInFrame) {
//        ((PlugInFrame) frame).close();
//      } else if (frame instanceof TextWindow) {
//        ((TextWindow) frame).close();
//      } else {
//        frame.setVisible(false);
//        frame.dispose();
//      }
    }
    return true;
  }

  /** Activates the next image window on the window list. */
  public static void putBehind() {
    if (IJ.debugMode) {
      IJ.log("putBehind");
    }
    if (imageList.size() < 1 || currentWindow == null) {
      return;
    }
    int index = imageList.indexOf(currentWindow);
    IjxImageWindow win;
    int count = 0;
    do {
      index--;
      if (index < 0) {
        index = imageList.size() - 1;
      }
      win = (IjxImageWindow) imageList.elementAt(index);
      if (++count == imageList.size()) {
        return;
      }
    } while (win instanceof HistogramWindow || win instanceof PlotWindow);
    setCurrentWindow(win);
    win.toFront();
    Menus.updateMenus();
  }

  /** Returns the temporary current image for this thread, or null. */
  public static IjxImagePlus getTempCurrentImage() {
    return (IjxImagePlus) tempImageTable.get(Thread.currentThread());
  }

  /** Returns the frame with the specified title or null if a frame with that
  title is not found. */
  public static IjxWindow getFrame(String title) {
    for (int i = 0; i < nonImageList.size(); i++) {
      IjxWindow frame = (IjxWindow) nonImageList.elementAt(i);
      if (title.equals(frame.getTitle())) {
        return frame;
      }
    }
    int[] wList = getIDList();
    int len = wList != null ? wList.length : 0;
    for (int i = 0; i < len; i++) {
      IjxImagePlus imp = getImage(wList[i]);
      if (imp != null) {
        if (imp.getTitle().equals(title)) {
          return imp.getWindow();
        }
      }
    }
    return null;
  }

  /** Activates a window selected from the Window menu. */
  public synchronized static void activateWindow(String menuItemLabel, MenuItem item) {
    //IJ.write("activateWindow: "+menuItemLabel+" "+item);
    for (int i = 0; i < nonImageList.size(); i++) {
      IjxWindow win = (IjxWindow) nonImageList.elementAt(i);
      String title = win.getTitle();
      if (menuItemLabel.equals(title)) {
        win.toFront();
        ((CheckboxMenuItem) item).setState(false);
        if (Recorder.record) {
          Recorder.record("selectWindow", title);
        }
        return;
      }
    }
    int lastSpace = menuItemLabel.lastIndexOf(' ');
    if (lastSpace > 0) // remove image size (e.g., " 90K")
    {
      menuItemLabel = menuItemLabel.substring(0, lastSpace);
    }
    for (int i = 0; i < imageList.size(); i++) {
      IjxImageWindow win = (IjxImageWindow) imageList.elementAt(i);
      String title = win.getImagePlus().getTitle();
      if (menuItemLabel.equals(title)) {
        setCurrentWindow(win);
        win.toFront();
        int index = imageList.indexOf(win);
        int n = Menus.window.getItemCount();
        int start = Menus.WINDOW_MENU_ITEMS + Menus.windowMenuItems2;
        for (int j = start; j < n; j++) {
          MenuItem mi = Menus.window.getItem(j);
          ((CheckboxMenuItem) mi).setState((j - start) == index);
        }
        if (Recorder.record) {
          Recorder.record("selectWindow", title);
        }
        break;
      }
    }
  }

  /** Repaints all open image windows. */
  public synchronized static void repaintImageWindows() {
    int[] list = getIDList();
    if (list == null) {
      return;
    }
    for (int i = 0; i < list.length; i++) {
      IjxImagePlus imp2 = getImage(list[i]);
      if (imp2 != null) {
        imp2.setTitle(imp2.getTitle()); // update "(G)" flag (global calibration)
        IjxImageWindow win = (IjxImageWindow) imp2.getWindow();
        if (win != null) {
          win.repaint();
        }
      }
    }
  }

  static void showList() {
    if (IJ.debugMode) {
      for (int i = 0; i < imageList.size(); i++) {
        IjxImageWindow win = (IjxImageWindow) imageList.elementAt(i);
        IjxImagePlus imp = win.getImagePlus();
        IJ.log(i + " " + imp.getTitle() + (win == currentWindow ? "*" : ""));
      }
      IJ.log(" ");
    }
  }
}