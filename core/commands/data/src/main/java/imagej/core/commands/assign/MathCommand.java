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

package imagej.core.commands.assign;

import imagej.Previewable;
import imagej.command.ContextCommand;
import imagej.data.Dataset;
import imagej.data.Position;
import imagej.data.display.DatasetView;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.OverlayService;
import imagej.data.overlay.Overlay;
import imagej.module.ItemIO;
import imagej.plugin.Parameter;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.meta.Axes;
import net.imglib2.ops.operation.complex.unary.ComplexUnaryOperation;
import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.ops.pointset.PointSet;
import net.imglib2.ops.pointset.PointSetIterator;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

/**
 * Base class for previewable math commands.
 * 
 * @author Barry DeZonia
 */
public abstract class MathCommand<I extends ComplexType<I>, O extends ComplexType<O>>
	extends ContextCommand implements Previewable
{
	// -- instance variables that are Parameters --

	@Parameter
	protected ImageDisplayService displayService;

	@Parameter
	protected OverlayService overlayService;

	@Parameter(type = ItemIO.BOTH)
	protected ImageDisplay display;

	@Parameter(label = "Preview")
	protected boolean preview;

	@Parameter(label = "Apply to all planes")
	protected boolean allPlanes;

	// -- instance variables --

	private O outType;
	private Img<DoubleType> dataBackup;
	private PointSetIterator iter;
	private RandomAccess<? extends RealType<?>> dataAccess;
	private RandomAccess<? extends RealType<?>> backupAccess;
	private Dataset dataset;
	private Overlay overlay;
	private Position planePos;
	
	// -- public interface --

	public MathCommand(O outType) {
		this.outType = outType;
	}
	
	@Override
	public void run() {
		if (dataset == null) {
			initialize();
		}
		else if (preview) {
			restorePreviewRegion();
		}
		transformFullRegion();
	}

	@Override
	public void preview() {
		if (dataset == null) {
			initialize();
			savePreviewRegion();
		}
		else restorePreviewRegion();
		if (preview) transformPreviewRegion();
	}

	@Override
	public void cancel() {
		if (preview) restorePreviewRegion();
	}

	public ImageDisplay getDisplay() {
		return display;
	}

	public void setDisplay(final ImageDisplay display) {
		this.display = display;
	}

	public boolean getPreview() {
		return preview;
	}

	public void setPreview(final boolean preview) {
		this.preview = preview;
	}

	public abstract ComplexUnaryOperation<O,O> getOperation();

	// -- private helpers --

	private void initialize() {
		dataset = displayService.getActiveDataset(display);
		overlay = overlayService.getActiveOverlay(display);
		DatasetView view = displayService.getActiveDatasetView(display);
		planePos = view.getPlanePosition();

		InplaceUnaryTransform<?,?> xform =
				getPreviewTransform(dataset, overlay);
		PointSet region =
			determineRegion(dataset, xform.getRegionOrigin(), xform.getRegionSpan());
		iter = region.iterator();
		ArrayImgFactory<DoubleType> factory = new ArrayImgFactory<DoubleType>();
		dataBackup = factory.create(new long[] { region.size() }, new DoubleType());
		backupAccess = dataBackup.randomAccess();
		dataAccess = dataset.getImgPlus().randomAccess();

		// check dimensions of Dataset
		final long w = xform.getRegionSpan()[0];
		final long h = xform.getRegionSpan()[1];
		if (w * h > Integer.MAX_VALUE)
			throw new IllegalArgumentException(
				"preview region too large to copy into memory");
	}

	private InplaceUnaryTransform<I,O> getPreviewTransform(
				Dataset ds, Overlay ov)
	{
		return new InplaceUnaryTransform<I,O>(
					getOperation(), outType, ds, ov, planePos);
	}
	
	private InplaceUnaryTransform<I,O> getFinalTransform(
			Dataset ds, Overlay ov)
	{
		if (allPlanes)
			return new InplaceUnaryTransform<I,O>(
					getOperation(), outType, ds, ov);
		return getPreviewTransform(ds, ov);
	}

	private PointSet determineRegion(Dataset ds, long[] planeOrigin,
		long[] planeSpan)
	{
		// copy data to a double[]
		final long[] origin = planeOrigin.clone();
		final long[] offsets = planeSpan.clone();
		if (ds.isRGBMerged()) {
			int chIndex = ds.getAxisIndex(Axes.CHANNEL);
			origin[chIndex] = 0;
			offsets[chIndex] = 3;
		}
		for (int i = 0; i < offsets.length; i++)
			offsets[i]--;
		return new HyperVolumePointSet(origin, new long[origin.length], offsets);
	}
	
	// NB
	// We are backing up preview region to doubles. This can cause precision
	// loss for long backed datasets with large values. But using dataset's
	// getPlane()/setPlane() code takes more ram/time than needed. And it has
	// various container limitations.

	private void savePreviewRegion() {
		iter.reset();
		long pos = 0;
		while (iter.hasNext()) {
			dataAccess.setPosition(iter.next());
			double value = dataAccess.get().getRealDouble();
			backupAccess.setPosition(pos++, 0);
			backupAccess.get().setReal(value);
		}
	}

	private void restorePreviewRegion() {
		iter.reset();
		long pos = 0;
		while (iter.hasNext()) {
			backupAccess.setPosition(pos++, 0);
			double value = backupAccess.get().getRealDouble();
			dataAccess.setPosition(iter.next());
			dataAccess.get().setReal(value);
		}
		dataset.update();
	}

	private void transformFullRegion() {
		getFinalTransform(dataset,overlay).run();
	}

	private void transformPreviewRegion() {
		getPreviewTransform(dataset,overlay).run();
	}

}
