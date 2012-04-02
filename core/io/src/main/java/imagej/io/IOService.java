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

package imagej.io;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.event.EventService;
import imagej.ext.module.ModuleService;
import imagej.io.event.FileOpenedEvent;
import imagej.service.AbstractService;
import imagej.service.Service;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.ImgPlus;
import net.imglib2.io.ImgIOException;
import net.imglib2.io.ImgOpener;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

/**
 * Service that provides convenience methods for I/O.
 * 
 * @author Curtis Rueden
 */
@Service
public final class IOService<T extends RealType<T> & NativeType<T>>
	extends AbstractService
{

	private EventService eventService;
	private ModuleService moduleService;
	private DatasetService datasetService;

	public IOService() {
		// NB: Required by SezPoz.
		super(null);
		throw new UnsupportedOperationException();
	}

	public IOService(final ImageJ context, final EventService eventService,
		final ModuleService moduleService, final DatasetService datasetService)
	{
		super(context);
		this.eventService = eventService;
		this.moduleService = moduleService;
		this.datasetService = datasetService;
	}

	// -- IOService methods --

	public EventService getEventService() {
		return eventService;
	}

	public ModuleService getModuleService() {
		return moduleService;
	}

	public DatasetService getDatasetService() {
		return datasetService;
	}

	/** Loads a dataset from a source (such as a file on disk). */
	public Dataset loadDataset(final String source) throws ImgIOException,
		IncompatibleTypeException
	{
		if (source == null) return null;
		final ImgOpener imageOpener = new ImgOpener();
		imageOpener.addStatusListener(new StatusDispatcher(eventService));
		final ImgPlus<T> imgPlus = imageOpener.openImg(source);
		final Dataset dataset = datasetService.create(imgPlus);
		eventService.publish(new FileOpenedEvent(source));
		return dataset;
	}

	/** Reverts the given dataset to its original source. */
	public void revertDataset(final Dataset dataset) throws ImgIOException,
		IncompatibleTypeException
	{
		final String source = dataset.getSource();
		if (source == null) return; // no way to revert
		final Dataset revertedDataset = loadDataset(source);
		revertedDataset.copyInto(dataset);
	}

	// TODO: Add a saveDataset method, and use it in SaveAsImage plugin.

}
