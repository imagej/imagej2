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

package imagej.data;

import imagej.data.display.DataView;
import imagej.data.display.ImageDisplay;
import io.scif.FormatException;
import io.scif.img.ImgIOException;
import io.scif.img.ImgOpener;
import io.scif.img.ImgOptions;
import io.scif.img.ImgOptions.CheckMode;
import io.scif.img.ImgSaver;
import io.scif.services.FormatService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.ImgPlus;
import net.imglib2.img.planar.PlanarImgFactory;
import net.imglib2.meta.AxisType;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.integer.Unsigned12BitType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;

import org.scijava.log.LogService;
import org.scijava.object.ObjectService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * Default service for working with {@link Dataset}s.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
@Plugin(type = Service.class)
public final class DefaultDatasetService extends AbstractService implements
	DatasetService
{

	@Parameter
	private LogService log;

	@Parameter
	private ObjectService objectService;

	@Parameter
	private FormatService formatService;

	// -- DatasetService methods --

	@Override
	public ObjectService getObjectService() {
		return objectService;
	}

	@Override
	public List<Dataset> getDatasets() {
		return objectService.getObjects(Dataset.class);
	}

	@Override
	public List<Dataset> getDatasets(final ImageDisplay display) {
		final ArrayList<Dataset> datasets = new ArrayList<Dataset>();
		if (display != null) {
			for (final DataView view : display) {
				final Data data = view.getData();
				if (!(data instanceof Dataset)) continue;
				final Dataset dataset = (Dataset) data;
				datasets.add(dataset);
			}
		}
		return datasets;
	}

	@Override
	public Dataset create(final long[] dims, final String name,
		final AxisType[] axes, final int bitsPerPixel, final boolean signed,
		final boolean floating)
	{
		if (bitsPerPixel == 1) {
			if (signed || floating) invalidParams(bitsPerPixel, signed, floating);
			return create(new BitType(), dims, name, axes);
		}
		if (bitsPerPixel == 8) {
			if (floating) invalidParams(bitsPerPixel, signed, floating);
			if (signed) return create(new ByteType(), dims, name, axes);
			return create(new UnsignedByteType(), dims, name, axes);
		}
		if (bitsPerPixel == 12) {
			if (signed || floating) invalidParams(bitsPerPixel, signed, floating);
			return create(new Unsigned12BitType(), dims, name, axes);
		}
		if (bitsPerPixel == 16) {
			if (floating) invalidParams(bitsPerPixel, signed, floating);
			if (signed) return create(new ShortType(), dims, name, axes);
			return create(new UnsignedShortType(), dims, name, axes);
		}
		if (bitsPerPixel == 32) {
			if (floating) {
				if (!signed) invalidParams(bitsPerPixel, signed, floating);
				return create(new FloatType(), dims, name, axes);
			}
			if (signed) return create(new IntType(), dims, name, axes);
			return create(new UnsignedIntType(), dims, name, axes);
		}
		if (bitsPerPixel == 64) {
			if (!signed) invalidParams(bitsPerPixel, signed, floating);
			if (floating) return create(new DoubleType(), dims, name, axes);
			return create(new LongType(), dims, name, axes);
		}
		invalidParams(bitsPerPixel, signed, floating);
		return null;
	}

	@Override
	public <T extends RealType<T> & NativeType<T>> Dataset create(
		final T type, final long[] dims, final String name, final AxisType[] axes)
	{
		final PlanarImgFactory<T> imgFactory = new PlanarImgFactory<T>();
		return create(imgFactory, type, dims, name, axes);
	}

	@Override
	public <T extends RealType<T>> Dataset create(
		final ImgFactory<T> factory, final T type, final long[] dims,
		final String name, final AxisType[] axes)
	{
		final Img<T> img = factory.create(dims, type);
		final ImgPlus<T> imgPlus = new ImgPlus<T>(img, name, axes, null);
		return create(imgPlus);
	}

	@Override
	public <T extends RealType<T>> Dataset create(final ImgPlus<T> imgPlus) {
		return new DefaultDataset(getContext(), imgPlus);
	}

	@Override
	public boolean canOpen(final String source) {
		try {
			return formatService.getFormat(source, true) != null;
		}
		catch (final FormatException exc) {
			log.error(exc);
		}
		return false;
	}

	@Override
	public boolean canSave(final String destination) {
		try {
			return formatService.getWriterByExtension(destination) != null;
		}
		catch (final FormatException exc) {
			log.error(exc);
		}
		return false;
	}

	@Override
	public Dataset open(final String source) throws IOException {
		final ImgOpener imageOpener = new ImgOpener(getContext());
		/* Restore this when NativeType can be eliminated from this class decl.
		// TODO BDZ 7-17-12 Lowering reliance on NativeType. This cast is safe but
		// necessary in the short term to get code to compile. But
		// imageOpener.openImg() is being modified to have no reference to
		// NativeType. Later, when that has been accomplished remove this cast.
		final ImgPlus<T> imgPlus = (ImgPlus<T>) imageOpener.openImg(source);
		*/
		final ImgOptions options = new ImgOptions();
		options.setIndex(0);
		options.setCheckMode(CheckMode.DEEP);
		options.setComputeMinMax(false);
		try {
			@SuppressWarnings("rawtypes")
			final ImgPlus imgPlus = open(imageOpener, source, options);
			final DatasetService datasetService =
				getContext().getService(DatasetService.class);
			@SuppressWarnings("unchecked")
			final Dataset dataset = datasetService.create(imgPlus);
			return dataset;
		}
		catch (final ImgIOException exc) {
			throw new IOException(exc);
		}
	}

	@Override
	public void revert(final Dataset dataset) throws IOException {
		final String source = dataset.getSource();
		if (source == null || source.isEmpty()) {
			// no way to revert
			throw new IOException("Cannot revert image of unknown origin");
		}
		final Dataset revertedDataset = open(source);
		revertedDataset.copyInto(dataset);
	}

	@Override
	public void save(final Dataset dataset, final String destination)
		throws IOException
	{
		@SuppressWarnings("rawtypes")
		final ImgPlus img = dataset.getImgPlus();

		final ImgSaver imageSaver = new ImgSaver();
		imageSaver.setContext(getContext());
		try {
			save(imageSaver, destination, img);
		}
		catch (final ImgIOException exc) {
			throw new IOException(exc);
		}
		catch (final IncompatibleTypeException exc) {
			throw new IOException(exc);
		}

		final String name = new File(destination).getName();
		dataset.setName(name);
		dataset.setDirty(false);
	}

	// -- Helper methods --

	private void invalidParams(final int bitsPerPixel,
		final boolean signed, final boolean floating)
	{
		throw new IllegalArgumentException("Invalid parameters: bitsPerPixel=" +
			bitsPerPixel + ", signed=" + signed + ", floating=" + floating);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void save(final ImgSaver imageSaver, final String destination,
		final ImgPlus img) throws ImgIOException, IncompatibleTypeException
	{
		imageSaver.saveImg(destination, img);
	}

	// FIXME HACK: Workaround for javac bug with ImgOpener's <T> parameter.
	// There is probably a much less convoluted way to do this, but at least
	// with this approach, we avoid propagating the T parameter any further.
	// Ultimately, SCIFIO's ImgOpener needs to be changed to not return a
	// generically typed object when there is no way for it to infer the T
	// from the calling code.

	@SuppressWarnings("rawtypes")
	private static ImgPlus open(ImgOpener imageOpener, String source,
		ImgOptions options) throws ImgIOException
	{
		try {
			return ImgOpenerHack.class.newInstance().open(imageOpener, source,
				options);
		}
		catch (InstantiationException exc) {
			throw new IllegalStateException(exc);
		}
		catch (IllegalAccessException exc) {
			throw new IllegalStateException(exc);
		}
	}

	protected static class ImgOpenerHack<T extends RealType<T> & NativeType<T>> {

		public ImgPlus<T> open(ImgOpener imageOpener, String source,
			ImgOptions options) throws ImgIOException
		{
			return imageOpener.openImg(source, options);
		}

	}

}
