package imagej.ij1bridge;

import ij.IWindowManager;
import ij.ImagePlus;
import ij.gui.ImageWindow;

import java.awt.Frame;
import java.awt.MenuItem;

/** ImageJ window manager that bridges ImageWindow with Dataset. */
public class BridgeWindowManager implements IWindowManager {

	@Override
	public void activateWindow(String arg0, MenuItem arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addWindow(Frame arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void checkForDuplicateName(ImagePlus arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean closeAllWindows() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ImagePlus getCurrentImage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getCurrentIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ImageWindow getCurrentWindow() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Frame getFrame(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Frame getFrontWindow() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] getIDList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ImagePlus getImage(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ImagePlus getImage(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getImageCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Frame[] getNonImageWindows() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNthImageID(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ImagePlus getTempCurrentImage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUniqueName(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getWindowCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isDuplicateName(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String makeUniqueName(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void putBehind() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeWindow(Frame arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void repaintImageWindows() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCurrentWindow(ImageWindow arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTempCurrentImage(ImagePlus arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTempCurrentImage(Thread arg0, ImagePlus arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setWindow(Frame arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showList() {
		// TODO Auto-generated method stub
		
	}

}
