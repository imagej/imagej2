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
import imagej.io.event.FileOpenedEvent;
import imagej.module.ModuleService;
import imagej.text.TextService;

import java.io.File;
import java.io.IOException;

import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.ImgPlus;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import ome.scifio.Format;
import ome.scifio.FormatException;
import ome.scifio.SCIFIO;
import ome.scifio.io.img.ImgIOException;
import ome.scifio.io.img.ImgOpener;

import org.scijava.app.StatusService;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * Default service that provides I/O convenience methods.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public final class DefaultIOService<T extends RealType<T> & NativeType<T>> 
	extends AbstractService implements IOService
{
	
	// TODO: eliminate bogus T parameter above. Rather, find a different way of
	// handling ImgOpener's need to pass forward a T parameter.

	@Parameter
	private EventService eventService;

	@Parameter
	private StatusService statusService;

	@Parameter
	private ModuleService moduleService;

	@Parameter
	private DatasetService datasetService;

	@Parameter
	private TextService textService;

	// -- IOService methods --

	@Override
	public EventService getEventService() {
		return eventService;
	}

	@Override
	public StatusService getStatusService() {
		return statusService;
	}

	@Override
	public ModuleService getModuleService() {
		return moduleService;
	}

	@Override
	public DatasetService getDatasetService() {
		return datasetService;
	}

	@Override
	public TextService getTextService() {
		return textService;
	}

	@Override
	public Object load(final File file) throws IOException {
		final String source = file.getAbsolutePath();
		if (isImageData(source)) {
			try {
				return loadDataset(source);
			}
			catch (final ImgIOException e) {
				throw new IOException(e);
			}
			catch (final IncompatibleTypeException e) {
				throw new IOException(e);
			}
		}
		else if (textService.supports(file)) {
			return textService.asHTML(file);
		}
		return null;
	}

	@Override
	public boolean isImageData(final String source) {

	  Format format = null;
	  try {
	    format = new SCIFIO(getContext()).format().getFormat(source, true);
	  } catch (FormatException e) {
	    throw new IllegalStateException(e);
	  }

	  return format != null;
	}

	@Override
	public Dataset loadDataset(final String source) throws ImgIOException,
		IncompatibleTypeException
	{
		if (source == null) return null;
		final ImgOpener imageOpener = new ImgOpener();
		imageOpener.addStatusListener(new StatusDispatcher(statusService));
		/* Restore this when NativeType can be eliminated from this class decl.
		// TODO BDZ 7-17-12 Lowering reliance on NativeType. This cast is safe but
		// necessary in the short term to get code to compile. But
		// imageOpener.openImg() is being modified to have no reference to
		// NativeType. Later, when that has been accomplished remove this cast.
		final ImgPlus<T> imgPlus = (ImgPlus<T>) imageOpener.openImg(source);
		*/
		final ImgPlus<T> imgPlus = imageOpener.openImg(source, 0, true, false);
		final Dataset dataset = datasetService.create(imgPlus);
		eventService.publish(new FileOpenedEvent(source));
		return dataset;
	}

	@Override
	public void revertDataset(final Dataset dataset) throws IncompatibleTypeException, ImgIOException
	{
		final String source = dataset.getSource();
		if (source == null) return; // no way to revert
		final Dataset revertedDataset = loadDataset(source);
		revertedDataset.copyInto(dataset);
	}

}
