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

package imagej.core.plugins.assign;

import imagej.data.Dataset;
import imagej.data.Position;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.OverlayService;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.PreviewPlugin;
import imagej.util.RealRect;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.meta.Axes;
import net.imglib2.ops.RegionIndexIterator;
import net.imglib2.ops.function.complex.ComplexImageFunction;
import net.imglib2.ops.function.general.GeneralUnaryFunction;
import net.imglib2.ops.image.ImageAssignment;
import net.imglib2.ops.operation.unary.complex.ComplexUnaryOperation;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;

/**
 * Base class for previewable math plugins.
 * 
 * @author Barry DeZonia
 */
public abstract class AbstractAssignPlugin<I extends ComplexType<I>, O extends ComplexType<O>>
	implements ImageJPlugin, PreviewPlugin
{

	// -- instance variables that are Parameters --

	@Parameter(persist = false)
	protected ImageDisplayService displayService;

	@Parameter(persist = false)
	protected OverlayService overlayService;

	@Parameter(persist = false)
	protected ImageDisplay display;

	@Parameter(label = "Preview")
	protected boolean preview;

	// -- instance variables --

	private Dataset dataset;

	private RealRect bounds;

	private double[] dataBackup;

	private long[] planeOrigin;

	private long[] planeSpan;

	private long[] imageOrigin;

	private long[] imageSpan;

	private RegionIndexIterator iter;

	private RandomAccess<? extends RealType<?>> accessor;
	
	private O outType;

	// -- public interface --

	public AbstractAssignPlugin(O outType) {
		this.outType = outType;
	}
	
	@Override
	public void run() {
		if (dataset == null) {
			initialize();
		}
		else if (preview) {
			restoreViewedPlane();
		}
		transformDataset();
	}

	@Override
	public void preview() {
		if (dataBackup == null) {
			initialize();
			saveViewedPlane();
		}
		else restoreViewedPlane();
		if (preview) transformViewedPlane();
	}

	@Override
	public void cancel() {
		if (preview) restoreViewedPlane();
	}

	public ImageDisplay getDisplay() {
		return display;
	}

	public void setDisplay(final ImageDisplay display) {
		this.display = display;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(final Dataset dataset) {
		this.dataset = dataset;
	}

	public abstract ComplexUnaryOperation<O,O> getOperation();

	// -- private helpers --

	// NB
	// - It is possible to design an elegant plane cloner but can only assume
	// certain types. And since dataset.setPlane() does not copy data if not
	// PlanarAccess backed it is not helpful to have exact data.
	// - we could make Dataset.copyOfPlane() visible but it assumes things
	// about all types being native types. And again can't set data.
	// - so we opt for copying to an array of doubles. However this could
	// cause precision loss for long data

	private void initialize() {
		dataset = displayService.getActiveDataset(display);
		bounds = overlayService.getSelectionBounds(display);

		// check dimensions of Dataset
		final int xIndex = dataset.getAxisIndex(Axes.X);
		final int yIndex = dataset.getAxisIndex(Axes.Y);
		if ((xIndex < 0) || (yIndex < 0)) throw new IllegalArgumentException(
			"display does not have XY planes");
		final long[] dims = dataset.getDims();
		final long w = (long) bounds.width;
		final long h = (long) bounds.height;
		if (w * h > Integer.MAX_VALUE) throw new IllegalArgumentException(
			"plane region too large to copy into memory");

		// calc origin of preview plane
		final Position planePos = display.getActiveView().getPlanePosition();
		planeOrigin = new long[dims.length];
		planeOrigin[xIndex] = (long) bounds.x;
		planeOrigin[yIndex] = (long) bounds.y;
		int p = 0;
		for (int i = 0; i < planeOrigin.length; i++) {
			if ((i == xIndex) || (i == yIndex)) continue;
			planeOrigin[i] = planePos.getLongPosition(p++);
		}

		// calc span of preview plane
		planeSpan = new long[dims.length];
		for (int i = 0; i < planeSpan.length; i++)
			planeSpan[i] = 1;
		planeSpan[xIndex] = w;
		planeSpan[yIndex] = h;

		// calc origin of image for actual data changes
		imageOrigin = new long[dims.length];
		for (int i = 0; i < imageOrigin.length; i++)
			imageOrigin[i] = 0;
		imageOrigin[xIndex] = (long) bounds.x;
		imageOrigin[yIndex] = (long) bounds.y;

		// calc span of image for actual data changes
		imageSpan = new long[dims.length];
		for (int i = 0; i < imageSpan.length; i++)
			imageSpan[i] = dims[i];
		imageSpan[xIndex] = w;
		imageSpan[yIndex] = h;

		// calc plane offsets for region iterator
		final long[] planeOffsets = new long[planeSpan.length];
		for (int i = 0; i < planeOffsets.length; i++)
			planeOffsets[i] = planeSpan[i] - 1;

		// setup region iterator
		accessor = dataset.getImgPlus().randomAccess();
		iter =
			new RegionIndexIterator(planeOrigin, new long[planeOrigin.length],
				planeOffsets);
		dataBackup = new double[(int) (w * h)];
	}

	private void saveViewedPlane() {

		// copy data to a double[]
		int index = 0;
		iter.reset();
		while (iter.hasNext()) {
			iter.fwd();
			accessor.setPosition(iter.getPosition());
			dataBackup[index++] = accessor.get().getRealDouble();
		}
	}

	private void restoreViewedPlane() {

		// restore data from our double[]
		int index = 0;
		iter.reset();
		while (iter.hasNext()) {
			iter.fwd();
			accessor.setPosition(iter.getPosition());
			accessor.get().setReal(dataBackup[index++]);
		}
		dataset.update();
	}

	private void transformDataset() {
		transformData(imageOrigin, imageSpan);
	}

	private void transformViewedPlane() {
		transformData(planeOrigin, planeSpan);
	}

	private void transformData(final long[] origin, final long[] span) {
		final Img<I> image = (Img<I>) dataset.getImgPlus();
		final ComplexImageFunction<I,O> imageFunc =
			new ComplexImageFunction<I,O>(image, outType.createVariable());
		final ComplexUnaryOperation<O,O> op = getOperation();
		final GeneralUnaryFunction<long[],O,O> function = new
				GeneralUnaryFunction<long[],O,O>(imageFunc, op, outType.createVariable());
		final ImageAssignment<I,O> assigner =
			new ImageAssignment<I,O>(image, origin, span,
				function, null, new long[span.length], new long[span.length]);
		assigner.assign();
		dataset.update();
	}

}
