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

import imagej.display.Display;

import java.io.File;
import java.util.List;

import org.scijava.plugin.Plugin;

/**
 * Drag-and-drop handler for lists of files.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
@Plugin(type = DragAndDropHandler.class)
public class FileListDragAndDropHandler extends AbstractDragAndDropHandler {

	// -- constants --

	public static final String MIME_TYPE =
		"application/x-java-file-list; class=java.util.List";

	// -- DragAndDropHandler methods --

	@Override
	public boolean isCompatible(final Display<?> display, final Object data)
	{
		if (isListOfFiles(data)) return true;
		if (!(data instanceof DragAndDropData)) return false;
		DragAndDropData dndData = (DragAndDropData) data;
		for (final String mimeType : dndData.getMimeTypes()) {
			if (MIME_TYPE.equals(mimeType)) return true;
		}
		return false;
	}

	@Override
	public boolean drop(final Display<?> display, final Object data) {
		final DragAndDropService dndService =
			getContext().getService(DragAndDropService.class);
		if (dndService == null) return false;

		final List<File> files = getFileList(data);
		if (files == null) return false;

		// drop each file
		for (final File file : files) {
			if (dndService.isCompatible(display, file)) {
				dndService.drop(display, file);
			}
		}
		return true;
	}

	// -- helpers --

	private boolean isListOfFiles(Object data) {
		if (!(data instanceof List)) return false;
		List<?> list = (List<?>) data;
		if (list.size() == 0) return false;
		for (Object o : list) {
			if (!(o instanceof File)) return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private List<File> getFileList(Object data) {
		if (data instanceof DragAndDropData) {
			DragAndDropData dndData = (DragAndDropData) data;
			return (List<File>) dndData.getData(MIME_TYPE);
		}
		if (isListOfFiles(data)) {
			return (List<File>) data;
		}
		return null;
	}
}
