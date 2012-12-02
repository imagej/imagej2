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

package imagej.data;

import imagej.data.display.DataView;
import imagej.data.display.ImageDisplay;
import imagej.object.ObjectService;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.service.AbstractService;
import imagej.service.Service;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.ImgPlus;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.planar.PlanarImgFactory;
import net.imglib2.meta.AxisType;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.ops.pointset.PointSetIterator;
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

/**
 * Default service for working with {@link Dataset}s.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public final class DefaultDatasetService extends AbstractService implements
	DatasetService
{

	@Parameter
	private ObjectService objectService;

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
	public <T extends RealType<T>> Dataset create(final ImgPlus<T> imgPlus)
	{
		return new DefaultDataset(getContext(), imgPlus);
	}

	@Override
	public Img<DoubleType> captureData(final Dataset source,
		final PointSet points)
	{
		return captureData(source, points, new ArrayImgFactory<DoubleType>());
	}

	@Override
	public Img<DoubleType> captureData(final Dataset source,
		final PointSet points, final ImgFactory<DoubleType> factory)
	{
		final long numPoints = points.size();
		final Img<DoubleType> backup =
			factory.create(new long[] { numPoints }, new DoubleType());
		long i = 0;
		final RandomAccess<? extends RealType<?>> dataAccessor =
			source.getImgPlus().randomAccess();
		final RandomAccess<DoubleType> backupAccessor = backup.randomAccess();
		final PointSetIterator iter = points.iterator();
		while (iter.hasNext()) {
			final long[] pos = iter.next();
			dataAccessor.setPosition(pos);
			final double val = dataAccessor.get().getRealDouble();
			backupAccessor.setPosition(i++, 0);
			backupAccessor.get().setReal(val);
		}
		return backup;
	}

	@Override
	public void restoreData(final Dataset target, final PointSet points,
		final Img<DoubleType> backup)
	{
		long i = 0;
		final RandomAccess<? extends RealType<?>> dataAccessor =
			target.getImgPlus().randomAccess();
		final RandomAccess<DoubleType> backupAccessor = backup.randomAccess();
		final PointSetIterator iter = points.iterator();
		while (iter.hasNext()) {
			final long[] pos = iter.next();
			backupAccessor.setPosition(i++, 0);
			final double val = backupAccessor.get().getRealDouble();
			dataAccessor.setPosition(pos);
			dataAccessor.get().setReal(val);
		}
		target.update();
	}

	// -- Helper methods --

	private void invalidParams(final int bitsPerPixel,
		final boolean signed, final boolean floating)
	{
		throw new IllegalArgumentException("Invalid parameters: bitsPerPixel=" +
			bitsPerPixel + ", signed=" + signed + ", floating=" + floating);
	}

}
