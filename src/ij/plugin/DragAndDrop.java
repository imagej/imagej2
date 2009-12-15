package ij.plugin;
import ij.*;
import ij.gui.*;
import ij.io.*;
import java.awt.Frame;
import java.io.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

/** This class opens images, roi's, luts and text files dragged and dropped on  the "ImageJ" window.
     It requires Java 1.3.1 or later. Based on the Draw_And_Drop plugin by Eric Kischell (keesh@ieee.org).
     
     10 November 2006: Albert Cardona added Linux support and an  
     option to open all images in a dragged folder as a stack.
*/
     
public class DragAndDrop implements PlugIn, DropTargetListener, Runnable {
	private Iterator iterator;
	
	public void run(String arg) {
		Frame ij = IJ.getTopComponentFrame();
        
		ij.setDropTarget(null);
		new DropTarget(ij, this);
		new DropTarget(Toolbar.getInstance(), this);
		new DropTarget(IJ.getTopComponent().getStatusBar(), this);
	}  
	    
	public void drop(DropTargetDropEvent dtde)  {
		if (IJ.debugMode) IJ.log("DragAndDrop.drop: "+dtde);
		dtde.acceptDrop(DnDConstants.ACTION_COPY);
		try  {
			Transferable t = dtde.getTransferable();
			iterator = null;
			if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				Object data = t.getTransferData(DataFlavor.javaFileListFlavor);
				iterator = ((List)data).iterator();
			} else {
				// find a String path
				DataFlavor[] flavors = t.getTransferDataFlavors();
				for (int i=0; i<flavors.length; i++) {
					if (!flavors[i].getRepresentationClass().equals(String.class)) continue;
					Object ob = t.getTransferData(flavors[i]);
					if (!(ob instanceof String)) continue;
					String s = ob.toString().trim();
					BufferedReader br = new BufferedReader(new StringReader(s));
					String tmp;
					ArrayList list = new ArrayList();
					while (null != (tmp = br.readLine())) {
						tmp = java.net.URLDecoder.decode(tmp, "UTF-8");
						if (tmp.startsWith("file://")) {
							tmp = tmp.substring(7);
						}
						list.add(new File(tmp));
					}
					this.iterator = list.iterator();
					break;
				}
			}
			if (null != iterator) {
				Thread thread = new Thread(this, "DrawAndDrop");
				thread.setPriority(Math.max(thread.getPriority()-1, Thread.MIN_PRIORITY));
				thread.start();
			}
		}
		catch(Exception e)  {
			    dtde.dropComplete(false);
			    return;
		}
		dtde.dropComplete(true);
	    } 

	    public void dragEnter(DropTargetDragEvent dtde)  {
			if (IJ.debugMode) IJ.log("DragAndDrop.dragEnter: "+dtde);
			dtde.acceptDrag(DnDConstants.ACTION_COPY);
	    }

	    public void dragOver(DropTargetDragEvent e) {}
	    public void dragExit(DropTargetEvent e) {}
	    public void dropActionChanged(DropTargetDragEvent e) {}
	    
		public void run() {
			Iterator iterator = this.iterator;
			while(iterator.hasNext()) {
				File file = (File)iterator.next();
				openFile(file);
			}
		}

		/** Open a file. If it's a directory, ask to open all images as a sequence in a stack or individually. */
		public void openFile(File f) {
			if (IJ.debugMode) IJ.log("DragAndDrop.open: "+f);
			try {
				if (null == f) return;
				String path = f.getCanonicalPath();
				if (f.exists()) {
					if (f.isDirectory())
						openDirectory(f, path);
					else {
						(new Opener()).openAndAddToRecent(path);
						OpenDialog.setLastDirectory(f.getParent()+File.separator);
						OpenDialog.setLastName(f.getName());
					}
				} else {
					IJ.log("File not found: " + path);
				}
			} catch (Throwable e) {
				CharArrayWriter caw = new CharArrayWriter();
				PrintWriter pw = new PrintWriter(caw);
				e.printStackTrace(pw);
				String s = caw.toString();
				new ij.text.TextWindow("Exception", s, 400, 300);
			}
		}
		
		private void openDirectory(File f, String path) {
			String[] names = f.list();
			String msg = "Open all "+names.length+" images in \"" + f.getName() + "\" as a stack?";
			GenericDialog gd = new GenericDialog("Open Folder?");
			gd.setInsets(10,5,0);
			gd.addMessage(msg);
			gd.setInsets(15,35,0);
			gd.addCheckbox("Use Virtual Stack", false);
			gd.enableYesNoCancel();
			gd.showDialog();
			if (gd.wasCanceled()) return;
			if (gd.wasOKed()) {
				String options  = (gd.getNextBoolean()?" use":"")+" sort";
				IJ.run("Image Sequence...", "open=[" + path + "/]"+options);
			} else {
				for (int k=0; k<names.length; k++) {
					IJ.redirectErrorMessages();
					if (!names[k].startsWith("."))
						(new Opener()).open(path + "/" + names[k]);
				}
			}
		}
		
}
