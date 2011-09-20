//
// AbstractPreviewPlugin.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.core.plugins.assign;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.Position;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.OverlayService;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.PreviewPlugin;
import imagej.util.RealRect;
import net.imglib2.RandomAccess;
import net.imglib2.img.Axes;
import net.imglib2.ops.DiscreteNeigh;
import net.imglib2.ops.Real;
import net.imglib2.ops.RegionIndexIterator;
import net.imglib2.ops.UnaryOperation;
import net.imglib2.ops.function.general.GeneralUnaryFunction;
import net.imglib2.ops.function.real.RealImageFunction;
import net.imglib2.ops.image.RealImageAssignment;
import net.imglib2.type.numeric.RealType;

/**
 * Base class for the various math plugins. They are previewable.
 * 
 * @author Barry DeZonia
 */
public abstract class AbstractPreviewPlugin
	implements ImageJPlugin, PreviewPlugin
{
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
	
	// -- public interface --
	
	@Override
	public void run() {
		if (dataset == null) {
			initialize();
		} else if (previewOn()) {
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
		else
			restoreViewedPlane();
		if (previewOn())
			transformViewedPlane();
	}

	@Override
	public void cancel() {
		if (previewOn())
			restoreViewedPlane();
	}

	public abstract ImageDisplay getDisplay();
	public abstract boolean previewOn();
	public abstract UnaryOperation<Real,Real> getOperation();
	
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
		final ImageDisplayService displayService = ImageJ.get(ImageDisplayService.class);
		final OverlayService overlayService = ImageJ.get(OverlayService.class);

		dataset = displayService.getActiveDataset(getDisplay());
		bounds = overlayService.getSelectionBounds(getDisplay());

		// check dimensions of Dataset
		int xIndex = dataset.getAxisIndex(Axes.X);
		int yIndex = dataset.getAxisIndex(Axes.Y);
		if ((xIndex < 0) || (yIndex < 0))
			throw new IllegalArgumentException("display does not have XY planes");
		long[] dims = dataset.getDims();
		long w = (long) bounds.width;
		long h = (long) bounds.height;
		if (w*h > Integer.MAX_VALUE)
			throw new IllegalArgumentException(
				"plane region too large to copy into memory");
		
		// calc origin of preview plane
		Position planePos = getDisplay().getActiveView().getPlanePosition();
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
		long[] planeOffsets = new long[planeSpan.length];
		for (int i = 0; i < planeOffsets.length; i++)
			planeOffsets[i] = planeSpan[i] - 1;
		
		// setup region iterator
		accessor = dataset.getImgPlus().randomAccess();
		iter = new RegionIndexIterator(planeOrigin, new long[planeOrigin.length], planeOffsets);
		dataBackup = new double[(int)(w*h)];
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

	private void transformData(long[] origin, long[] span) {
		RealImageFunction imageFunc =
			new RealImageFunction(dataset.getImgPlus().getImg());
		UnaryOperation<Real,Real> op = getOperation();
		GeneralUnaryFunction<long[], Real, Real> function =
			new GeneralUnaryFunction<long[], Real, Real>(imageFunc, op);
		RealImageAssignment assigner =
			new RealImageAssignment(dataset.getImgPlus().getImg(), origin, span,
				function, new long[span.length], new long[span.length]);
		assigner.assign();
		dataset.update();
	}
	
}
