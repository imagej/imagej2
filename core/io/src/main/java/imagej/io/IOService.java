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

package imagej.io;

import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.module.ModuleService;
import imagej.text.TextService;

import java.io.File;
import java.io.IOException;

import net.imglib2.exception.IncompatibleTypeException;

import ome.scifio.io.img.ImgIOException;

import org.scijava.app.StatusService;
import org.scijava.event.EventService;
import org.scijava.service.Service;

/**
 * Interface for providing I/O convenience methods.
 * 
 * @author Curtis Rueden
 */
public interface IOService extends Service {

	// CTR TODO: Extend HandlerService<IOPlugin>.

	EventService getEventService();

	StatusService getStatusService();

	ModuleService getModuleService();

	DatasetService getDatasetService();

	TextService getTextService();

	/**
	 * Loads data from the given file.
	 * <p>
	 * The type of data is automatically determined. In the case of image data,
	 * the returned object will be a {@link Dataset}. If the file contains text
	 * data, the returned object will be a {@link String} formatted as HTML.
	 * </p>
	 * 
	 * @param file The file from which to load data.
	 * @return An object representing the loaded data, or null if the file is not
	 *         in a supported format.
	 * @throws IOException if something goes wrong loading the data.
	 */
	Object load(File file) throws IOException;

	/**
	 * Determines whether the given source is image data (and hence compatible
	 * with the {@link #loadDataset(String)} method).
	 */
	boolean isImageData(String source);

	/** Loads a dataset from a source (such as a file on disk). */
	Dataset loadDataset(String source) throws ImgIOException,
		IncompatibleTypeException;

	/** Reverts the given dataset to its original source. */
	void revertDataset(Dataset dataset) throws ImgIOException,
		IncompatibleTypeException;

	// TODO: Add a saveDataset method, and use it in SaveAsImage plugin.

}
