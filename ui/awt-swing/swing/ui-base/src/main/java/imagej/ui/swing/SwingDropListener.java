//
// SwingDropListener.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.ui.swing;

import imagej.ImageJ;
import imagej.event.EventService;
import imagej.event.StatusEvent;
import imagej.ext.plugin.PluginService;
import imagej.io.plugins.OpenImage;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


// NOTE - this code adapted from IJ1's DragAndDrop class
//   Original contributor: Wayne Rasband
//   Extensions for linux support: Albert Cardona 2006

/**
 * SwingDropListener implements the drag and drop functionality of IJ2 that is
 * tied to dropping files on the swing application window items.
 * 
 * @author Barry DeZonia
 */
public class SwingDropListener implements DropTargetListener {

	// -- instance variables --
	
	private EventService eventService;

	// -- constructor --
	
	public SwingDropListener() {
		eventService = ImageJ.get(EventService.class);
	}
	
	// -- public interface --
	
	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
		eventService.publish(new StatusEvent("< <Drag and Drop> >"));
		dtde.acceptDrag(DnDConstants.ACTION_COPY);
	}

	@Override
	public void dragExit(DropTargetEvent dte) {
		eventService.publish(new StatusEvent(""));
	}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {
		// do nothing
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
		// do nothing
	}
	
	@Override
	public void drop(DropTargetDropEvent dtde) {
		dtde.acceptDrop(DnDConstants.ACTION_COPY);
		doDrop(dtde);
	}

	// -- private helpers --
	
	private void doDrop(DropTargetDropEvent dtde) {
		try {
			List<Object> inputs = buildInputs(dtde);
			for (int i = 0; i < inputs.size(); i++) {
				final Object input = inputs.get(i);
				Thread thread = new Thread(new Runnable() {

					@SuppressWarnings("synthetic-access")
					@Override
					public void run() {
							if (input instanceof String)
								openFile((String)input);
							if (input instanceof File)
								openFile((File)input);
					}
				});
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
	
	private List<Object> buildInputs(DropTargetDropEvent dtde) {
		List<Object> inputs = new ArrayList<Object>();
		Transferable t = dtde.getTransferable();
		try {
			for (DataFlavor flavor : t.getTransferDataFlavors()) {
				if (flavor.isFlavorJavaFileListType()) {
					Object data = t.getTransferData(DataFlavor.javaFileListFlavor);
					@SuppressWarnings("unchecked")
					List<Object> fileList = (List<Object>) data;
					inputs.addAll(fileList);
					break;
				}
				else if (flavor.isFlavorTextType()) {
					Object obj = t.getTransferData(flavor);
					if (!(obj instanceof String)) continue;
					String s = obj.toString().trim();
					if (isLinux() && (s.length()>1) && (s.charAt(1)==0))
						s = fixLinuxString(s);
					if (s.indexOf("href=\"")!=-1 || s.indexOf("src=\"")!=-1) {
						s = parseHTML(s);
						inputs.add(s);
						break;
					}
					BufferedReader br = new BufferedReader(new StringReader(s));
					String tmp;
					while (null != (tmp = br.readLine())) {
						tmp = java.net.URLDecoder.decode(tmp.replaceAll("\\+","%2b"), "UTF-8");
						if (tmp.startsWith("file://")) tmp = tmp.substring(7);
						if (tmp.startsWith("http://"))
							inputs.add(s);
						else
							inputs.add(tmp);
					}
					break;
				}
			}
		}
		catch (UnsupportedFlavorException e) { /*do nothing */ }
		catch (UnsupportedEncodingException e) { /*do nothing */ }
		catch (IOException e) { /*do nothing */ }
		
		return inputs;
	}
	
  private void openFile(File f) {
  	Map<String,Object> params = new HashMap<String,Object>();
  	params.put("inputFile",f);
 		ImageJ.get(PluginService.class).run(OpenImage.class, params);
  }
  
  private void openFile(String filename) {
  	File file = new File(filename);
  	openFile(file);
  }

  private boolean isLinux() {
		return System.getProperty("os.name").startsWith("Linux");
  }
  
  private String fixLinuxString(String s) {
  	StringBuffer sb = new StringBuffer(200);
  	for (int i=0; i<s.length(); i+=2)
  		sb.append(s.charAt(i));
  	return new String(sb);
  }

  private String parseHTML(String s) {
  	int index1 = s.indexOf("src=\"");
  	if (index1>=0) {
  		int index2 = s.indexOf("\"", index1+5);
  		if (index2>0)
  			return s.substring(index1+5, index2);
  	}
  	index1 = s.indexOf("href=\"");
  	if (index1>=0) {
  		int index2 = s.indexOf("\"", index1+6);
  		if (index2>0)
  			return s.substring(index1+6, index2);
  	}
  	return s;
  }
}
