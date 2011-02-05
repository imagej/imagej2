package imagej.ij1bridge;

import java.awt.Frame;
import java.awt.MenuItem;

import ij.DefaultWindowManager;
import ij.IWindowManager;
import ij.ImagePlus;
import ij.gui.ImageWindow;
import imagej.Log;

public class BridgeWindowManager implements IWindowManager {

	private DefaultWindowManager dwm = new DefaultWindowManager();

	@Override
	public void setCurrentWindow(ImageWindow win) {
		Log.debug("BridgeWindowManager.setCurrentWindow: win=" + win);//TEMP
		dwm.setCurrentWindow(win);
	}

	@Override
	public ImageWindow getCurrentWindow() {
		Log.debug("BridgeWindowManager.getCurrentWindow");//TEMP
		return dwm.getCurrentWindow();
	}

	@Override
	public int getCurrentIndex() {
		Log.debug("BridgeWindowManager.getCurrentIndex");//TEMP
		return dwm.getCurrentIndex();
	}

	@Override
	public ImagePlus getCurrentImage() {
		Log.debug("BridgeWindowManager.getCurrentImage");//TEMP
		return dwm.getCurrentImage();
	}

	@Override
	public void setTempCurrentImage(ImagePlus img) {
		Log.debug("setTempCurrentImage: img=" + img);//TEMP
		dwm.setTempCurrentImage(img);
	}

	@Override
	public void setTempCurrentImage(Thread thread, ImagePlus img) {
		Log.debug("setTempCurrentImage(Thread thread, ImagePlus img)");//TEMP
		dwm.setTempCurrentImage(thread, img);
	}

	@Override
	public int getWindowCount() {
		Log.debug("getWindowCount()");//TEMP
		return dwm.getWindowCount();
	}

	@Override
	public int getImageCount() {
		Log.debug("getImageCount()");//TEMP
		return dwm.getImageCount();
	}

	@Override
	public Frame getFrontWindow() {
		Log.debug("getFrontWindow()");//TEMP
		return dwm.getFrontWindow();
	}

	@Override
	public int[] getIDList() {
		Log.debug("getIDList()");//TEMP
		return dwm.getIDList();
	}

	@Override
	public Frame[] getNonImageWindows() {
		Log.debug("getNonImageWindows()");//TEMP
		return dwm.getNonImageWindows();
	}

	@Override
	public ImagePlus getImage(int imageID) {
		Log.debug("getImage(int imageID)");//TEMP
		return dwm.getImage(imageID);
	}

	@Override
	public int getNthImageID(int n) {
		Log.debug("getNthImageID(int n)");//TEMP
		return dwm.getNthImageID(n);
	}

	@Override
	public ImagePlus getImage(String title) {
		Log.debug("getImage(String title)");//TEMP
		return dwm.getImage(title);
	}

	@Override
	public void addWindow(Frame win) {
		Log.debug("addWindow(Frame win)");//TEMP
		dwm.addWindow(win);
	}

	@Override
	public void checkForDuplicateName(ImagePlus imp) {
		Log.debug("checkForDuplicateName(ImagePlus imp)");//TEMP
		dwm.checkForDuplicateName(imp);

	}

	@Override
	public boolean isDuplicateName(String name) {
		Log.debug("isDuplicateName(String name)");//TEMP
		return dwm.isDuplicateName(name);
	}

	@Override
	public String getUniqueName(String name) {
		Log.debug("getUniqueName(String name)");//TEMP
		return dwm.getUniqueName(name);
	}

	@Override
	public String makeUniqueName(String name) {
		Log.debug("makeUniqueName(String name)");//TEMP
		return dwm.makeUniqueName(name);
	}

	@Override
	public void removeWindow(Frame win) {
		Log.debug("removeWindow(Frame win)");//TEMP
		dwm.removeWindow(win);
	}

	@Override
	public void setWindow(Frame win) {
		Log.debug("setWindow(Frame win)");//TEMP
		dwm.setWindow(win);
	}

	@Override
	public boolean closeAllWindows() {
		Log.debug("closeAllWindows()");//TEMP
		return dwm.closeAllWindows();
	}

	@Override
	public void putBehind() {
		Log.debug("putBehind()");//TEMP
		dwm.putBehind();
	}

	@Override
	public ImagePlus getTempCurrentImage() {
		Log.debug("getTempCurrentImage()");//TEMP
		return dwm.getTempCurrentImage();
	}

	@Override
	public Frame getFrame(String title) {
		Log.debug("getFrame(String title)");//TEMP
		return dwm.getFrame(title);
	}

	@Override
	public void activateWindow(String menuItemLabel, MenuItem item) {
		Log.debug("activateWindow(String menuItemLabel, MenuItem item)");//TEMP
		dwm.activateWindow(menuItemLabel, item);
	}

	@Override
	public void repaintImageWindows() {
		Log.debug("repaintImageWindows()");//TEMP
		dwm.repaintImageWindows();
	}

	@Override
	public void showList() {
		Log.debug("showList()");//TEMP
		dwm.showList();
	}

}
