/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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
 * #L%
 */

package imagej.data;

import imagej.data.display.DatasetView;
import net.imglib2.RandomAccess;
import net.imglib2.display.screenimage.awt.ARGBScreenImage;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.meta.ImgPlus;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

/**
 * An {@link ImageGrabber} creates a merged color {@link Dataset} from a
 * {@link DatasetView}. Note that it does not include overlay graphics.
 * 
 * @author Barry DeZonia
 */
public class ImageGrabber {
	private final DatasetService service;

	/**
	 * Constructs an {@link ImageGrabber} for a given (@link DatasetService}. The
	 * {@link DatasetService} is used to create {@link Dataset}s.
	 */
	public ImageGrabber(DatasetService service) {
		this.service = service;
	}
	
	/**
	 * Creates a merged color {@link Dataset} from a {@link DatasetView}. This
	 * method uses the provided String name for the output {@link Dataset}.
	 */
	public Dataset grab(DatasetView view, String outputName) {
		ARGBScreenImage screenImage = view.getScreenImage();
		long[] dims = new long[3];
		screenImage.dimensions(dims);  // fill X count & Y count
		dims[2] = 3;  // fill CHANNEL count
		if (dims[0] * dims[1] > Integer.MAX_VALUE)
			throw new IllegalArgumentException("image is too big to fit into memory");
		int xSize = (int) dims[0];
		int ySize = (int) dims[1];
		int[] argbPixels = view.getScreenImage().getData();
		Dataset dataset = 
				service.create(new UnsignedByteType(), dims, outputName,
												new AxisType[]{Axes.X, Axes.Y, Axes.CHANNEL});
		ImgPlus<? extends RealType<?>> imgPlus = dataset.getImgPlus();
		RandomAccess<? extends RealType<?>> accessor = imgPlus.randomAccess();
		for (int x = 0; x < xSize; x++) {
			accessor.setPosition(x, 0);
			for (int y = 0; y < ySize; y++) {
				accessor.setPosition(y, 1);
				int index = y*xSize + x; 
				int pixel = argbPixels[index];
				accessor.setPosition(0, 2);
				accessor.get().setReal((pixel >> 16) & 0xff);
				accessor.setPosition(1, 2);
				accessor.get().setReal((pixel >>  8) & 0xff);
				accessor.setPosition(2, 2);
				accessor.get().setReal((pixel >>  0) & 0xff);
			}
		}
		dataset.setRGBMerged(true);
		return dataset;
	}
}
