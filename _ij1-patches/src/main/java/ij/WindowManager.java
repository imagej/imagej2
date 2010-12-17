package ij;

import ij.gui.ImageWindow;

import java.awt.Frame;
import java.awt.MenuItem;

/** This class consists of static methods used to manage ImageJ's windows. */
public class WindowManager {

	public static boolean checkForDuplicateName;

	/** Injectable singleton to which WindowManager delegates. */
	private static IWindowManager windowManager = new DefaultWindowManager();

	private WindowManager() {
	}

	public static void setWindowManager(IWindowManager wm) {
		windowManager = wm;
	}

	public static IWindowManager getWindowManager() {
		return windowManager;
	}

	/** Makes the image contained in the specified window the active image. */
	public static void setCurrentWindow(ImageWindow win) {
		windowManager.setCurrentWindow(win);
	}

	/** Returns the active ImageWindow. */
	public static ImageWindow getCurrentWindow() {
		return windowManager.getCurrentWindow();
	}

	static int getCurrentIndex() {
		return windowManager.getCurrentIndex();
	}

	/** Returns a reference to the active image or null if there isn't one. */
	public static ImagePlus getCurrentImage() {
		return windowManager.getCurrentImage();
	}

	/** Makes the specified image temporarily the active 
		image for this thread. Call again with a null
		argument to revert to the previous active image. */
	public static void setTempCurrentImage(ImagePlus img) {
		windowManager.setTempCurrentImage(img);
	}

	/** Sets a temporary image for the specified thread. */
	public static void setTempCurrentImage(Thread thread, ImagePlus img) {
		windowManager.setTempCurrentImage(thread, img);
	}

	/** Returns the number of open image windows. */
	public static int getWindowCount() {
		return windowManager.getWindowCount();
	}

	/** Returns the number of open images. */
	public static int getImageCount() {
		return windowManager.getImageCount();
	}

	/** Returns the front most window or null. */
	public static Frame getFrontWindow() {
		return windowManager.getFrontWindow();
	}

	/** Returns a list of the IDs of open images. Returns
		null if no windows are open. */
	public synchronized static int[] getIDList() {
		return windowManager.getIDList();
	}

	/** Returns an array containing a list of the non-image windows. */
	public synchronized static Frame[] getNonImageWindows() {
		return windowManager.getNonImageWindows();
	}

	/** For IDs less than zero, returns the ImagePlus with the specified ID. 
		Returns null if no open window has a matching ID or no images are open. 
		For IDs greater than zero, returns the Nth ImagePlus. Returns null if 
		the ID is zero. */
	public synchronized static ImagePlus getImage(int imageID) {
		return windowManager.getImage(imageID);
	}

	/** Returns the ID of the Nth open image. Returns zero if n<=0 
		or n greater than the number of open image windows. */
	public synchronized static int getNthImageID(int n) {
		return windowManager.getNthImageID(n);
	}

	/** Returns the first image that has the specified title or null if it is not found. */
	public synchronized static ImagePlus getImage(String title) {
		return windowManager.getImage(title);
	}

	/** Adds the specified window to the Window menu. */
	public synchronized static void addWindow(Frame win) {
		windowManager.addWindow(win);
	}

	public static void checkForDuplicateName(ImagePlus imp) {
		windowManager.checkForDuplicateName(imp);
	}

	public static boolean isDuplicateName(String name) {
		return windowManager.isDuplicateName(name);
	}

	/** Returns a unique name by adding, before the extension,  -1, -2, etc. as needed. */
	public static String getUniqueName(String name) {
		return windowManager.getUniqueName(name);
	}

	/** If 'name' is not unique, adds -1, -2, etc. as needed to make it unique. */
	public static String makeUniqueName(String name) {
		return windowManager.makeUniqueName(name);
	}

	/** Removes the specified window from the Window menu. */
	public synchronized static void removeWindow(Frame win) {
		windowManager.removeWindow(win);
	}

	/** The specified frame becomes the front window, the one returnd by getFrontWindow(). */
	public static void setWindow(Frame win) {
		windowManager.setWindow(win);
	}

	/** Closes all windows. Stops and returns false if any image "save changes" dialog is canceled. */
	public synchronized static boolean closeAllWindows() {
		return windowManager.closeAllWindows();
	}

	/** Activates the next image window on the window list. */
	public static void putBehind() {
		windowManager.putBehind();
	}

	/** Returns the temporary current image for this thread, or null. */
	public static ImagePlus getTempCurrentImage() {
		return windowManager.getTempCurrentImage();
	}

	/** Returns the frame with the specified title or null if a frame with that 
    	title is not found. */
	public static Frame getFrame(String title) {
		return windowManager.getFrame(title);
	}

	/** Activates a window selected from the Window menu. */
	synchronized static void activateWindow(String menuItemLabel, MenuItem item) {
		windowManager.activateWindow(menuItemLabel, item);
	}

	/** Repaints all open image windows. */
	public synchronized static void repaintImageWindows() {
		windowManager.repaintImageWindows();
	}

	static void showList() {
		windowManager.showList();
	}

}
