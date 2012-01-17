//
// InplaceUnaryTransform.java
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
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.OverlayService;
import imagej.util.RealRect;
import net.imglib2.img.Img;
import net.imglib2.meta.Axes;
import net.imglib2.ops.UnaryOperation;
import net.imglib2.ops.function.general.GeneralUnaryFunction;
import net.imglib2.ops.function.real.RealImageFunction;
import net.imglib2.ops.image.ImageAssignment;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

/**
 * Helper class for use by many plugins that apply an {@link UnaryOperation} to
 * some input image. The run() method modifies the current selection of the
 * active {@link Dataset} of the given {@link ImageDisplay}. The given
 * {@link UnaryOperation} is applied on a pixel by pixel basis.
 * 
 * @author Barry DeZonia
 */
public class InplaceUnaryTransform {

	// -- instance variables --

	private final Dataset dataset;
	private long[] origin;
	private long[] span;
	private final ImageAssignment assigner;

	// -- constructor --

	public InplaceUnaryTransform(final ImageDisplay display,
		final UnaryOperation<ComplexType<?>, ComplexType<?>> operation)
	{
		dataset = ImageJ.get(ImageDisplayService.class).getActiveDataset(display);
		//RealDataset realSet = null;
		//final Img<? extends RealType<?>> img = realSet.getData();
		final Img<? extends RealType<?>> img = dataset.getImgPlus();
		final RealImageFunction<DoubleType> f1 = new RealImageFunction<DoubleType>(img, new DoubleType());
		final GeneralUnaryFunction<long[], DoubleType, DoubleType> function =
			new GeneralUnaryFunction<long[], DoubleType, DoubleType>(f1, operation, new DoubleType());
		setOriginAndSpan(display);
		assigner =
			new ImageAssignment(img, origin, span, function, null,
				new long[span.length], new long[span.length]);
	}
	
	// -- public interface --

	public void run() {
		assigner.assign();
		dataset.update();
	}

	// -- private helpers --

	private void setOriginAndSpan(final ImageDisplay disp) {
		final ImageDisplayService imageDisplayService =
			ImageJ.get(ImageDisplayService.class);
		final OverlayService overlayService = ImageJ.get(OverlayService.class);

		final Dataset ds = imageDisplayService.getActiveDataset(disp);
		final RealRect bounds = overlayService.getSelectionBounds(disp);

		// check dimensions of Dataset
		final int xIndex = ds.getAxisIndex(Axes.X);
		final int yIndex = ds.getAxisIndex(Axes.Y);
		if ((xIndex < 0) || (yIndex < 0)) throw new IllegalArgumentException(
			"display does not have XY planes");
		final long[] dims = ds.getDims();
		final long w = (long) bounds.width;
		final long h = (long) bounds.height;

		// calc origin of image for actual data changes
		origin = new long[dims.length];
		for (int i = 0; i < origin.length; i++)
			origin[i] = 0;
		origin[xIndex] = (long) bounds.x;
		origin[yIndex] = (long) bounds.y;

		// calc span of image for actual data changes
		span = new long[dims.length];
		for (int i = 0; i < span.length; i++)
			span[i] = dims[i] - 1;
		span[xIndex] = w;
		span[yIndex] = h;
	}

}
