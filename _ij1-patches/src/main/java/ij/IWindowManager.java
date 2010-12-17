package ij;

import ij.gui.ImageWindow;

import java.awt.Frame;
import java.awt.MenuItem;

public interface IWindowManager {

	/** Makes the image contained in the specified window the active image. */
	void setCurrentWindow(ImageWindow win);

	/** Returns the active ImageWindow. */
	ImageWindow getCurrentWindow();

	int getCurrentIndex();

	/** Returns a reference to the active image or null if there isn't one. */
	ImagePlus getCurrentImage();

	/** Makes the specified image temporarily the active 
		image for this thread. Call again with a null
		argument to revert to the previous active image. */
	void setTempCurrentImage(ImagePlus img);

	/** Sets a temporary image for the specified thread. */
	void setTempCurrentImage(Thread thread, ImagePlus img);

	/** Returns the number of open image windows. */
	int getWindowCount();

	/** Returns the number of open images. */
	int getImageCount();

	/** Returns the front most window or null. */
	Frame getFrontWindow();

	/** Returns a list of the IDs of open images. Returns
		null if no windows are open. */
	int[] getIDList();

	/** Returns an array containing a list of the non-image windows. */
	Frame[] getNonImageWindows();

	/** For IDs less than zero, returns the ImagePlus with the specified ID. 
		Returns null if no open window has a matching ID or no images are open. 
		For IDs greater than zero, returns the Nth ImagePlus. Returns null if 
		the ID is zero. */
	ImagePlus getImage(int imageID);

	/** Returns the ID of the Nth open image. Returns zero if n<=0 
		or n greater than the number of open image windows. */
	int getNthImageID(int n);

	/** Returns the first image that has the specified title or null if it is not found. */
	ImagePlus getImage(String title);

	/** Adds the specified window to the Window menu. */
	void addWindow(Frame win);

	void checkForDuplicateName(ImagePlus imp);

	boolean isDuplicateName(String name);

	/** Returns a unique name by adding, before the extension,  -1, -2, etc. as needed. */
	String getUniqueName(String name);

	/** If 'name' is not unique, adds -1, -2, etc. as needed to make it unique. */
	String makeUniqueName(String name);

	/** Removes the specified window from the Window menu. */
	void removeWindow(Frame win);

	/** The specified frame becomes the front window, the one returnd by getFrontWindow(). */
	void setWindow(Frame win);

	/** Closes all windows. Stops and returns false if any image "save changes" dialog is canceled. */
	boolean closeAllWindows();

	/** Activates the next image window on the window list. */
	void putBehind();

	/** Returns the temporary current image for this thread, or null. */
	ImagePlus getTempCurrentImage();

	/** Returns the frame with the specified title or null if a frame with that 
		title is not found. */
	Frame getFrame(String title);

	/** Activates a window selected from the Window menu. */
	void activateWindow(String menuItemLabel, MenuItem item);

	/** Repaints all open image windows. */
	void repaintImageWindows();

	void showList();

}
