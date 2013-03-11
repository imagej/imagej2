/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

package imagej.ui.dnd;

import imagej.data.lut.LutService;
import imagej.display.Display;
import imagej.display.DisplayService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

/**
 * Drag-and-drop handler for lists of files.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
@Plugin(type = DragAndDropHandler.class, priority = Priority.VERY_LOW_PRIORITY)
public class FileListDragAndDropHandler extends AbstractDragAndDropHandler {

	private static final String MIME_TYPE =
		"application/x-java-file-list; class=java.util.List";

	@Override
	public boolean isCompatible(final Display<?> display,
		final DragAndDropData data)
	{
		for (final String mimeType : data.getMimeTypes()) {
			if (MIME_TYPE.equals(mimeType)) return true;
		}
		return true;
	}

	@Override
	public boolean drop(final Display<?> display, final DragAndDropData data) {
		final DisplayService displayService =
			getContext().getService(DisplayService.class);
		final DragAndDropService dndService =
			getContext().getService(DragAndDropService.class);
		if (displayService == null) return false;
		if (dndService == null) return false;

		@SuppressWarnings("unchecked")
		final List<File> files = (List<File>) data.getData(MIME_TYPE);
		if (files == null) return false;

		// drop each file
		for (final File file : files) {
			DragAndDropData stuff = getNewDragAndDropData(file);
			if (dndService.isCompatible(display, stuff)) {
				dndService.drop(display, stuff);
			}
		}
		return true;
	}

	private DragAndDropData getNewDragAndDropData(File file) {
		// System.out.println("Creating appropriate drop data for file " +
		// file.getAbsolutePath());
		String path = file.getAbsolutePath();
		if (path.endsWith(".lut")) {
			return new LutFileDragAndDropData(path);
		}
		else if (path.endsWith(".txt")) {
			return new TextFileDragAndDropData(path);
		}
		return new ImageFileDragAndDropData(path);
	}

	private class LutFileDragAndDropData implements DragAndDropData {

		private String filename;

		public LutFileDragAndDropData(String filename) {
			this.filename = filename;
		}

		@Override
		public boolean isSupported(String mimeType) {
			return LutDragAndDropHandler.MIME_TYPE.equals(mimeType);
		}

		@Override
		public Object getData(String mimeType) {
			try {
				URL url = new URL("file://" + filename);
				LutService lutService = getContext().getService(LutService.class);
				return lutService.loadLut(url);
			}
			catch (Exception e) {
				return null;
			}
		}

		@Override
		public List<String> getMimeTypes() {
			return Arrays.asList(LutDragAndDropHandler.MIME_TYPE);
		}

	}

	private class TextFileDragAndDropData implements DragAndDropData {

		private String filename;

		public TextFileDragAndDropData(String filename) {
			this.filename = filename;
		}

		@Override
		public boolean isSupported(String mimeType) {
			return TextDragAndDropHandler.MIME_TYPE.equals(mimeType);
		}

		@Override
		public Object getData(String mimeType) {
			try {
				StringBuilder builder = new StringBuilder();
				BufferedReader in = new BufferedReader(new FileReader(filename));
				String line;
				while ((line = in.readLine()) != null) {
					builder.append(line);
					builder.append("\n");
				}
				in.close();
				return builder.toString();
			}
			catch (Exception e) {
				return null;
			}
		}

		@Override
		public List<String> getMimeTypes() {
			return Arrays.asList(new String[] { TextDragAndDropHandler.MIME_TYPE });
		}
	}

	private class ImageFileDragAndDropData implements DragAndDropData {

		private String filename;

		public ImageFileDragAndDropData(String filename) {
			this.filename = filename;
		}

		@Override
		public boolean isSupported(String mimeType) {
			return true;
		}

		@Override
		public Object getData(String mimeType) {
			System.out.println("NOW OPENING IMAGE " + filename);
			return null;
		}

		@Override
		public List<String> getMimeTypes() {
			return Arrays.asList(new String[0]); // ACK!!!! CHOKEMEISTER
		}

	}
}
