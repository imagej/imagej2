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

package imagej.io.dnd;

import imagej.data.Dataset;
import imagej.display.Display;
import imagej.display.DisplayService;
import imagej.io.IOService;
import imagej.ui.dnd.AbstractDragAndDropHandler;
import imagej.ui.dnd.DragAndDropHandler;

import java.io.File;

import net.imglib2.exception.IncompatibleTypeException;

import ome.scifio.io.img.ImgIOException;

import org.scijava.Priority;
import org.scijava.log.LogService;
import org.scijava.plugin.Plugin;

/**
 * Drag-and-drop handler for image files.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
@Plugin(type = DragAndDropHandler.class, priority = Priority.LOW_PRIORITY)
public class ImageFileDragAndDropHandler extends
	AbstractDragAndDropHandler<File>
{

	// -- DragAndDropHandler methods --

	@Override
	public boolean supports(final File file) {
		if (!super.supports(file)) return false;

		// verify that the file is image data
		final IOService ioService = getContext().getService(IOService.class);
		if (ioService == null) return false;
		return ioService.isImageData(file.getAbsolutePath());
	}

	@Override
	public boolean drop(final File file, final Display<?> display) {
		check(file, display);
		if (file == null) return true; // trivial case

		final IOService ioService = getContext().getService(IOService.class);
		if (ioService == null) return false;

		final DisplayService displayService =
			getContext().getService(DisplayService.class);
		if (displayService == null) return false;

		final LogService log = getContext().getService(LogService.class);

		// load dataset
		final String filename = file.getAbsolutePath();
		final Dataset dataset;
		try {
			dataset = ioService.loadDataset(filename);
		}
		catch (final ImgIOException exc) {
			if (log != null) log.error("Error opening file: " + filename, exc);
			return false;
		}
		catch (final IncompatibleTypeException exc) {
			if (log != null) log.error("Error opening file: " + filename, exc);
			return false;
		}

		// display result
		displayService.createDisplay(dataset);
		return true;
	}

	// -- Typed methods --

	@Override
	public Class<File> getType() {
		return File.class;
	}

}
