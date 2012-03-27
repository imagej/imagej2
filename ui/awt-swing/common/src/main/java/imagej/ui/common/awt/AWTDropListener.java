/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

// NOTE - this code adapted from IJ1's DragAndDrop class
// Original contributor: Wayne Rasband

package imagej.ui.common.awt;

import imagej.ImageJ;
import imagej.data.display.DatasetView;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.event.StatusEvent;
import imagej.io.plugins.NewImage;
import imagej.io.plugins.OpenImage;
import imagej.ui.OutputWindow;
import imagej.ui.UIService;

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
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.imglib2.display.ColorTable8;

/**
 * Implements functionality that enables AWT-based user interfaces to open files
 * in response to a drag and drop operation.
 * 
 * @author Barry DeZonia
 */
public class AWTDropListener implements DropTargetListener {

	// -- instance variables --

	protected UIService uiService;
	private final DropHandler dropHandler;

	// -- constructor --

	public AWTDropListener(final UIService uiService) {
		this.uiService = uiService;
		dropHandler = new DropHandler();
	}

	// -- public interface --

	@Override
	public void dragEnter(final DropTargetDragEvent dtde) {
		uiService.getEventService().publish(new StatusEvent("< <Drag and Drop> >"));
		dtde.acceptDrag(DnDConstants.ACTION_COPY);
	}

	@Override
	public void dragExit(final DropTargetEvent dte) {
		uiService.getEventService().publish(new StatusEvent(""));
	}

	@Override
	public void dragOver(final DropTargetDragEvent dtde) {
		// do nothing
	}

	@Override
	public void dropActionChanged(final DropTargetDragEvent dtde) {
		// do nothing
	}

	@Override
	public void drop(final DropTargetDropEvent dtde) {
		dtde.acceptDrop(DnDConstants.ACTION_COPY);
		doDrop(dtde);
	}

	// -- private helpers --

	private void doDrop(final DropTargetDropEvent dtde) {
		try {
			final List<Object> inputs = buildInputs(dtde);
			for (int i = 0; i < inputs.size(); i++) {
				final Object input = inputs.get(i);
				final Thread thread = new Thread(new Runnable() {

					@SuppressWarnings("synthetic-access")
					@Override
					public void run() {
						dropHandler.handleInput(input);
					}
				});
				thread.setPriority(Math.max(thread.getPriority() - 1,
					Thread.MIN_PRIORITY));
				thread.start();
			}
		}
		catch (final Exception e) {
			dtde.dropComplete(false);
			return;
		}
		dtde.dropComplete(true);
	}

	private List<Object> buildInputs(final DropTargetDropEvent dtde) {
		final List<Object> inputs = new ArrayList<Object>();
		final Transferable t = dtde.getTransferable();
		try {
			for (final DataFlavor flavor : t.getTransferDataFlavors()) {
				if (flavor.isFlavorJavaFileListType()) {
					final Object data = t.getTransferData(DataFlavor.javaFileListFlavor);
					@SuppressWarnings("unchecked")
					final List<Object> fileList = (List<Object>) data;
					inputs.addAll(fileList);
					break;
				}
				else if (flavor.isFlavorTextType()) {
					final Object obj = t.getTransferData(flavor);
					if (!(obj instanceof String)) continue;
					String s = obj.toString().trim();
					if (s.indexOf("href=\"") != -1 || s.indexOf("src=\"") != -1) {
						s = parseHTML(s);
						inputs.add(s);
						break;
					}
					final BufferedReader br = new BufferedReader(new StringReader(s));
					String tmp;
					while (null != (tmp = br.readLine())) {
						tmp =
							java.net.URLDecoder.decode(tmp.replaceAll("\\+", "%2b"), "UTF-8");
						if (tmp.startsWith("file://")) tmp = tmp.substring(7);
						if (tmp.startsWith("http://")) inputs.add(s);
						else inputs.add(tmp);
					}
					break;
				}
			}
		}
		catch (final UnsupportedFlavorException e) {
			// ignore
		}
		catch (final UnsupportedEncodingException e) {
			// ignore
		}
		catch (final IOException e) {
			// ignore
		}

		return inputs;
	}

	private String parseHTML(final String s) {
		int index1 = s.indexOf("src=\"");
		if (index1 >= 0) {
			final int index2 = s.indexOf("\"", index1 + 5);
			if (index2 > 0) return s.substring(index1 + 5, index2);
		}
		index1 = s.indexOf("href=\"");
		if (index1 >= 0) {
			final int index2 = s.indexOf("\"", index1 + 6);
			if (index2 > 0) return s.substring(index1 + 6, index2);
		}
		return s;
	}

	// TODO -- RELOCATE THE FOLLOWING CODE TO VARIOUS DropHandler implementations
	// later. They should be sezpoz discoverable. This code is a partial HACK
	// that allows beta1's drag and drop to actually do something. See ticket
	// #860.

	private class DropHandler {

		public DropHandler() {
			// nothing to do
		}

		public void handleInput(final Object input) {
			if (input instanceof String) handleInputAsFileName((String) input);
			if (input instanceof File) handleInputAsFile((File) input);
		}

		private void handleInputAsFileName(final String filename) {
			// is it a LUT?
			if (filename.toLowerCase().endsWith(".lut")) {
				importLut(filename);
				return;
			}

			// default case
			openAsTextFile(filename);
		}

		private void handleInputAsFile(final File file) {
			final String filename = file.getAbsolutePath();
			if (isKnownImageType(filename)) loadImage(file);
			else {
				handleInputAsFileName(filename);
			}
		}

		private boolean isKnownImageType(final String filename) {
			/*
			 * way that relies on BioFormats : avoid
			 * 
			IFormatReader reader = null;
			try {
				reader = ImgOpener.createReader(filename, false);
			} catch (FormatException e) {
				// fall through
			} catch (IOException e) {
				// fall through
			}
			return reader != null;
			 *
			 */
			// TODO - actually do something
			return true; // always open as image
			// return false; // always open as LUT or TEXT
		}

		private void loadImage(final File f) {
			final Map<String, Object> params = new HashMap<String, Object>();
			params.put("inputFile", f);
			uiService.getPluginService().run(OpenImage.class, params);
		}

		private void openAsTextFile(final String filename) {
			final String title = shortName(filename);
			final List<String> fileContents = loadFileContents(filename);
			final OutputWindow window = uiService.createOutputWindow(title);
			for (final String line : fileContents)
				window.append(line + '\n');
			window.setVisible(true);
		}

		private void importLut(final String filename) {
			final List<ImageDisplay> imageDisplays =
				ImageJ.get(ImageDisplayService.class).getImageDisplays();
			if (imageDisplays.size() == 0) {
				createSmallRampedImage(filename);
				// TODO TEMP HACK pause long enough so active image is set
				try {
					Thread.sleep(2500);
				}
				catch (final Exception e) {/**/}
			}
			applyLutToActiveImage(filename);
		}

		private void applyLutToActiveImage(final String filename) {
			final ImageDisplay display =
				ImageJ.get(ImageDisplayService.class).getActiveImageDisplay();
			final ColorTable8 colorTable = loadColorTable(filename);
			final DatasetView view = (DatasetView) display.getActiveView();
			// TODO - broken - THESE NEXT TWO LINES SEEM TO HAVE NO EFFECT
			view.setColorTable(colorTable, 0);
			view.update();
		}

		private List<String> loadFileContents(final String filename) {
			final List<String> contents = new LinkedList<String>();
			try {
				final FileReader fileReader = new FileReader(filename);
				final BufferedReader reader = new BufferedReader(fileReader);
				while (reader.ready()) {
					contents.add(reader.readLine());
				}
			}
			catch (final Exception e) {
				// do nothing
			}
			return contents;
		}

		private ColorTable8 loadColorTable(final String filename) {
			// TODO do something sensible by loading ColorTable from
			// .lut file
			final byte[] reds = new byte[256];
			final byte[] greens = new byte[256];
			final byte[] blues = new byte[256];
			for (int i = 0; i < 256; i++) {
				reds[i] = (byte) i;
				greens[i] = (byte) (255 - i / 2);
				blues[i] = (byte) (i * 0.8);
			}
			return new ColorTable8(reds, greens, blues);
		}

		private void createSmallRampedImage(final String filename) {
			final Map<String, Object> params = new HashMap<String, Object>();
			params.put("name", shortName(filename));
			params.put("bitDepth", NewImage.DEPTH8);
			params.put("signed", false);
			params.put("floating", false);
			params.put("fillType", NewImage.RAMP);
			params.put("width", 256L);
			params.put("height", 50L);
			uiService.getPluginService().run(NewImage.class, params);
		}

		private String shortName(final String filename) {
			String shortname = filename;
			final int lastSlash = filename.lastIndexOf(File.separatorChar);
			if (lastSlash >= 0) shortname = filename.substring(lastSlash + 1);
			return shortname;
		}
	}

}
