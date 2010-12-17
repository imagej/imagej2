package ij;

import ij.gui.HistogramWindow;
import ij.gui.ImageWindow;
import ij.gui.PlotWindow;
import ij.macro.Interpreter;
import ij.plugin.frame.PlugInFrame;
import ij.plugin.frame.Recorder;
import ij.text.TextWindow;

import java.awt.CheckboxMenuItem;
import java.awt.Frame;
import java.awt.MenuItem;
import java.util.Hashtable;
import java.util.Vector;

/** Default implementation of ImageJ window management logic. */
public class DefaultWindowManager implements IWindowManager {

	public static boolean checkForDuplicateName;
	private Vector<ImageWindow> imageList = new Vector<ImageWindow>();		 // list of image windows
	private Vector<Frame> nonImageList = new Vector<Frame>();	 // list of non-image windows
	private ImageWindow currentWindow;			 // active image window
	private Frame frontWindow;
	private Hashtable<Thread, ImagePlus> tempImageTable = new Hashtable<Thread, ImagePlus>();

	/* (non-Javadoc)
	 * @see ij.IWindowManager#setCurrentWindow(ij.gui.ImageWindow)
	 */
	@Override
	public void setCurrentWindow(ImageWindow win) {
		if (win==null || win.isClosed() || win.getImagePlus()==null) // deadlock-"wait to lock"
			return;
		//IJ.log("setCurrentWindow: "+win.getImagePlus().getTitle()+" ("+(currentWindow!=null?currentWindow.getImagePlus().getTitle():"null") + ")");
		setWindow(win);
		tempImageTable.remove(Thread.currentThread());
		if (win==currentWindow || imageList.size()==0)
			return;
		if (currentWindow!=null) {
			// free up pixel buffers used by current window
			ImagePlus imp = currentWindow.getImagePlus();
			if (imp!=null ) {
				imp.trimProcessor();
				imp.saveRoi();
			}
		}
		Undo.reset();
		currentWindow = win;
		Menus.updateMenus();
		if (Recorder.record && !IJ.isMacro())
			Recorder.record("selectWindow", win.getImagePlus().getTitle());
	}

	/* (non-Javadoc)
	 * @see ij.IWindowManager#getCurrentWindow()
	 */
	@Override
	public ImageWindow getCurrentWindow() {
		//if (IJ.debugMode) IJ.write("ImageWindow.getCurrentWindow");
		return currentWindow;
	}

	/* (non-Javadoc)
	 * @see ij.IWindowManager#getCurrentIndex()
	 */
	@Override
	public int getCurrentIndex() {
		return imageList.indexOf(currentWindow);
	}

	/* (non-Javadoc)
	 * @see ij.IWindowManager#getCurrentImage()
	 */
	@Override
	public ImagePlus getCurrentImage() {
		ImagePlus img = tempImageTable.get(Thread.currentThread());
		//String str = (img==null)?" null":"";
		if (img==null)
			img = getActiveImage();
		//if (img!=null) IJ.log("getCurrentImage: "+img.getTitle()+" "+Thread.currentThread().hashCode()+str);
		return img;
	}

	/* (non-Javadoc)
	 * @see ij.IWindowManager#setTempCurrentImage(ij.ImagePlus)
	 */
	@Override
	public void setTempCurrentImage(ImagePlus img) {
		//IJ.log("setTempImage: "+(img!=null?img.getTitle():"null ")+Thread.currentThread().hashCode());
		if (img==null)
			tempImageTable.remove(Thread.currentThread());
		else
			tempImageTable.put(Thread.currentThread(), img);
	}

	/* (non-Javadoc)
	 * @see ij.IWindowManager#setTempCurrentImage(java.lang.Thread, ij.ImagePlus)
	 */
	@Override
	public void setTempCurrentImage(Thread thread, ImagePlus img) {
		if (thread==null)
			throw new RuntimeException("thread==null");
		if (img==null)
			tempImageTable.remove(thread);
		else
			tempImageTable.put(thread, img);
	}

	/** Returns the active ImagePlus. */
	private ImagePlus getActiveImage() {
		if (currentWindow!=null)
			return currentWindow.getImagePlus();
		else if (frontWindow!=null && (frontWindow instanceof ImageWindow))
			return ((ImageWindow)frontWindow).getImagePlus();
		else 	if (imageList.size()>0) {	
			ImageWindow win = imageList.elementAt(imageList.size()-1);
			return win.getImagePlus();
		} else
			return Interpreter.getLastBatchModeImage(); 
	}

	/* (non-Javadoc)
	 * @see ij.IWindowManager#getWindowCount()
	 */
	@Override
	public int getWindowCount() {
		int count = imageList.size();
		return count;
	}

	/* (non-Javadoc)
	 * @see ij.IWindowManager#getImageCount()
	 */
	@Override
	public int getImageCount() {
		int count = imageList.size();
		count += Interpreter.getBatchModeImageCount();
		if (count==0 && getCurrentImage()!=null)
			count = 1;
		return count;
	}

	/* (non-Javadoc)
	 * @see ij.IWindowManager#getFrontWindow()
	 */
	@Override
	public Frame getFrontWindow() {
		return frontWindow;
	}

	/* (non-Javadoc)
	 * @see ij.IWindowManager#getIDList()
	 */
	@Override
	public synchronized int[] getIDList() {
		int nWindows = imageList.size();
		int[] batchModeImages = Interpreter.getBatchModeImageIDs();
		int nBatchImages = batchModeImages.length;
		if ((nWindows+nBatchImages)==0)
			return null;
		int[] list = new int[nWindows+nBatchImages];
		for (int i=0; i<nBatchImages; i++)
			list[i] = batchModeImages[i];
		int index = 0;
		for (int i=nBatchImages; i<nBatchImages+nWindows; i++) {
			ImageWindow win = imageList.elementAt(index++);
			list[i] = win.getImagePlus().getID();
		}
		return list;
	}

	/* (non-Javadoc)
	 * @see ij.IWindowManager#getNonImageWindows()
	 */
	@Override
	public synchronized Frame[] getNonImageWindows() {
		Frame[] list = new Frame[nonImageList.size()];
		nonImageList.copyInto(list);
		return list;
	}

	/* (non-Javadoc)
	 * @see ij.IWindowManager#getImage(int)
	 */
	@Override
	public synchronized ImagePlus getImage(int imageID) {
		//if (IJ.debugMode) IJ.write("ImageWindow.getImage");
		if (imageID>0)
			imageID = getNthImageID(imageID);
		if (imageID==0 || getImageCount()==0)
			return null;
		ImagePlus imp2 = Interpreter.getBatchModeImage(imageID);
		if (imp2!=null)
			return imp2;
		ImagePlus imp = null;
		for (int i=0; i<imageList.size(); i++) {
			ImageWindow win = imageList.elementAt(i);
			imp2 = win.getImagePlus();
			if (imageID==imp2.getID()) {
				imp = imp2;
				break;
			}
		}
		imp2 = getCurrentImage();
		if (imp==null &&imp2!=null && imp2.getID()==imageID)
			return imp2;
		return imp;
	}

	/* (non-Javadoc)
	 * @see ij.IWindowManager#getNthImageID(int)
	 */
	@Override
	public synchronized int getNthImageID(int n) {
		if (n<=0) return 0;
		if (Interpreter.isBatchMode()) {
			int[] list = getIDList();
			if (n>list.length)
				return 0;
			return list[n-1];
		}
		if (n>imageList.size()) return 0;
		ImageWindow win = imageList.elementAt(n-1);
		if (win!=null)
			return win.getImagePlus().getID();
		return 0;
	}


	/* (non-Javadoc)
	 * @see ij.IWindowManager#getImage(java.lang.String)
	 */
	@Override
	public synchronized ImagePlus getImage(String title) {
		int[] wList = getIDList();
		if (wList==null) return null;
		for (int i=0; i<wList.length; i++) {
			ImagePlus imp = getImage(wList[i]);
			if (imp!=null && imp.getTitle().equals(title))
				return imp;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see ij.IWindowManager#addWindow(java.awt.Frame)
	 */
	@Override
	public synchronized void addWindow(Frame win) {
		//IJ.write("addWindow: "+win.getTitle());
		if (win==null)
			return;
		else if (win instanceof ImageWindow)
			addImageWindow((ImageWindow)win);
		else {
			Menus.insertWindowMenuItem(win);
			nonImageList.addElement(win);
		}
	}

	private void addImageWindow(ImageWindow win) {
		ImagePlus imp = win.getImagePlus();
		if (imp==null) return;
		checkForDuplicateName(imp);
		imageList.addElement(win);
		Menus.addWindowMenuItem(imp);
		setCurrentWindow(win);
	}

	/* (non-Javadoc)
	 * @see ij.IWindowManager#checkForDuplicateName(ij.ImagePlus)
	 */
	@Override
	public void checkForDuplicateName(ImagePlus imp) {
		if (checkForDuplicateName) {
			String name = imp.getTitle();
			if (isDuplicateName(name))
				imp.setTitle(getUniqueName(name));
		} 
		checkForDuplicateName = false;
	}

	/* (non-Javadoc)
	 * @see ij.IWindowManager#isDuplicateName(java.lang.String)
	 */
	@Override
	public boolean isDuplicateName(String name) {
		int n = imageList.size();
		for (int i=0; i<n; i++) {
			ImageWindow win = imageList.elementAt(i);
			String name2 = win.getImagePlus().getTitle();
			if (name.equals(name2))
				return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see ij.IWindowManager#getUniqueName(java.lang.String)
	 */
	@Override
	public String getUniqueName(String name) {
		String name2 = name;
		String extension = "";
		int len = name2.length();
		int lastDot = name2.lastIndexOf(".");
		if (lastDot!=-1 && len-lastDot<6 && lastDot!=len-1) {
			extension = name2.substring(lastDot, len);
			name2 = name2.substring(0, lastDot);
		}
		int lastDash = name2.lastIndexOf("-");
		len = name2.length();
		if (lastDash!=-1&&len-lastDash<4&&lastDash<len-1&&Character.isDigit(name2.charAt(lastDash+1))&&name2.charAt(lastDash+1)!='0')
			name2 = name2.substring(0, lastDash);
		for (int i=1; i<=99; i++) {
			String name3 = name2+"-"+ i + extension;
			//IJ.log(i+" "+name3);
			if (!isDuplicateName(name3))
				return name3;
		}
		return name;
	}

	/* (non-Javadoc)
	 * @see ij.IWindowManager#makeUniqueName(java.lang.String)
	 */
	@Override
	public String makeUniqueName(String name) {
		return isDuplicateName(name)?getUniqueName(name):name;
	}

	/* (non-Javadoc)
	 * @see ij.IWindowManager#removeWindow(java.awt.Frame)
	 */
	@Override
	public synchronized void removeWindow(Frame win) {
		//IJ.write("removeWindow: "+win.getTitle());
		if (win instanceof ImageWindow)
			removeImageWindow((ImageWindow)win);
		else {
			int index = nonImageList.indexOf(win);
			if (index>=0) {
				//if (ij!=null && !ij.quitting())
				Menus.removeWindowMenuItem(index);
				nonImageList.removeElement(win);
			}
		}
		setWindow(null);
	}

	private void removeImageWindow(ImageWindow win) {
		int index = imageList.indexOf(win);
		if (index==-1)
			return;  // not on the window list
		if (imageList.size()>1) {
			int newIndex = index-1;
			if (newIndex<0)
				newIndex = imageList.size()-1;
			setCurrentWindow(imageList.elementAt(newIndex));
		} else
			currentWindow = null;
		imageList.removeElementAt(index);
		setTempCurrentImage(null);  //???
		int nonImageCount = nonImageList.size();
		if (nonImageCount>0)
			nonImageCount++;
		Menus.removeWindowMenuItem(nonImageCount+index);
		Menus.updateMenus();
		Undo.reset();
	}

	/* (non-Javadoc)
	 * @see ij.IWindowManager#setWindow(java.awt.Frame)
	 */
	@Override
	public void setWindow(Frame win) {
		frontWindow = win;
		//IJ.log("Set window: "+(win!=null?win.getTitle():"null"));
	}

	/* (non-Javadoc)
	 * @see ij.IWindowManager#closeAllWindows()
	 */
	@Override
	public synchronized boolean closeAllWindows() {
		while (imageList.size()>0) {
			if (!imageList.elementAt(0).close())
				return false;
			IJ.wait(100);
		}
		ImageJ ij = IJ.getInstance();
		if (ij!=null && ij.quitting() && IJ.getApplet()==null)
			return true;
		Frame[] list = getNonImageWindows();
		for (int i=0; i<list.length; i++) {
			Frame frame = list[i];
			if (frame instanceof PlugInFrame)
				((PlugInFrame)frame).close();
			else if (frame instanceof TextWindow)
				((TextWindow)frame).close();
			else {
				frame.setVisible(false);
				frame.dispose();
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see ij.IWindowManager#putBehind()
	 */
	@Override
	public void putBehind() {
		if (IJ.debugMode) IJ.log("putBehind");
		if(imageList.size()<1 || currentWindow==null)
			return;
		int index = imageList.indexOf(currentWindow);
		ImageWindow win;
		int count = 0;
		do {
			index--;
			if (index<0) index = imageList.size()-1;
			win = imageList.elementAt(index);
			if (++count==imageList.size()) return;
		} while (win instanceof HistogramWindow || win instanceof PlotWindow);
		setCurrentWindow(win);
		win.toFront();
		Menus.updateMenus();
	}

	/* (non-Javadoc)
	 * @see ij.IWindowManager#getTempCurrentImage()
	 */
	@Override
	public ImagePlus getTempCurrentImage() {
		return tempImageTable.get(Thread.currentThread()); 
	}

	/* (non-Javadoc)
	 * @see ij.IWindowManager#getFrame(java.lang.String)
	 */
	@Override
	public Frame getFrame(String title) {
		for (int i=0; i<nonImageList.size(); i++) {
			Frame frame = nonImageList.elementAt(i);
			if (title.equals(frame.getTitle()))
				return frame;
		}
		int[] wList = getIDList();
		int len = wList!=null?wList.length:0;
		for (int i=0; i<len; i++) {
			ImagePlus imp = getImage(wList[i]);
			if (imp!=null) {
				if (imp.getTitle().equals(title))
					return imp.getWindow();
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see ij.IWindowManager#activateWindow(java.lang.String, java.awt.MenuItem)
	 */
	@Override
	public synchronized void activateWindow(String menuItemLabel, MenuItem item) {
		//IJ.write("activateWindow: "+menuItemLabel+" "+item);
		for (int i=0; i<nonImageList.size(); i++) {
			Frame win = nonImageList.elementAt(i);
			String title = win.getTitle();
			if (menuItemLabel.equals(title)) {
				win.toFront();
				((CheckboxMenuItem)item).setState(false);
				if (Recorder.record && !IJ.isMacro())
					Recorder.record("selectWindow", title);
				return;
			}
		}
		int lastSpace = menuItemLabel.lastIndexOf(' ');
		if (lastSpace>0) // remove image size (e.g., " 90K")
			menuItemLabel = menuItemLabel.substring(0, lastSpace);
		for (int i=0; i<imageList.size(); i++) {
			ImageWindow win = imageList.elementAt(i);
			String title = win.getImagePlus().getTitle();
			if (menuItemLabel.equals(title)) {
				setCurrentWindow(win);
				win.toFront();
				int index = imageList.indexOf(win);
				int n = Menus.window.getItemCount();
				int start = Menus.WINDOW_MENU_ITEMS+Menus.windowMenuItems2;
				for (int j=start; j<n; j++) {
					MenuItem mi = Menus.window.getItem(j);
					((CheckboxMenuItem)mi).setState((j-start)==index);						
				}
				break;
			}
		}
	}

	/* (non-Javadoc)
	 * @see ij.IWindowManager#repaintImageWindows()
	 */
	@Override
	public synchronized void repaintImageWindows() {
		int[] list = getIDList();
		if (list==null) return;
		for (int i=0; i<list.length; i++) {
			ImagePlus imp2 = getImage(list[i]);
			if (imp2!=null) {
				imp2.setTitle(imp2.getTitle()); // update "(G)" flag (global calibration)
				ImageWindow win = imp2.getWindow();
				if (win!=null) win.repaint();
			}
		}
	}

	/* (non-Javadoc)
	 * @see ij.IWindowManager#showList()
	 */
	@Override
	public void showList() {
		if (IJ.debugMode) {
			for (int i=0; i<imageList.size(); i++) {
				ImageWindow win = imageList.elementAt(i);
				ImagePlus imp = win.getImagePlus();
				IJ.log(i + " " + imp.getTitle() + (win==currentWindow?"*":""));
			}
			IJ.log(" ");
		}
	}

}
