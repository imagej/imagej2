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

package imagej.legacy.translate;

import ij.ImagePlus;
import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import net.imglib2.meta.AxisType;

/**
 * The default {@link ImageTranslator} between legacy and modern ImageJ image
 * structures. It delegates to the appropriate more specific translators based
 * on the type of data being translated.
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
public class DefaultImageTranslator implements ImageTranslator {

	private final ImageJ context;

	private final DisplayCreator colorDisplayCreator;
	private final DisplayCreator grayDisplayCreator;
	private final ImagePlusCreator colorImagePlusCreator;
	private final ImagePlusCreator grayImagePlusCreator;

	public DefaultImageTranslator(final ImageJ context) {
		this.context = context;
		colorDisplayCreator = new ColorDisplayCreator(context);
		grayDisplayCreator = new GrayDisplayCreator(context);
		colorImagePlusCreator = new ColorImagePlusCreator(context);
		grayImagePlusCreator = new GrayImagePlusCreator(context);
	}

	/**
	 * Creates a {@link ImageDisplay} from an {@link ImagePlus}. Shares planes of
	 * data when possible.
	 */
	@Override
	public ImageDisplay createDisplay(final ImagePlus imp) {

		if ((imp.getType() == ImagePlus.COLOR_RGB) && (imp.getNChannels() == 1)) {
			return colorDisplayCreator.createDisplay(imp);
		}

		return grayDisplayCreator.createDisplay(imp);
	}

	/**
	 * Creates a {@link ImageDisplay} from an {@link ImagePlus}. Shares planes of
	 * data when possible. Builds ImageDisplay with preferred Axis ordering.
	 */
	@Override
	public ImageDisplay createDisplay(final ImagePlus imp,
		final AxisType[] preferredOrder)
	{

		if ((imp.getType() == ImagePlus.COLOR_RGB) && (imp.getNChannels() == 1)) {
			return colorDisplayCreator.createDisplay(imp, preferredOrder);
		}

		return grayDisplayCreator.createDisplay(imp, preferredOrder);
	}

	/**
	 * Creates an {@link ImagePlus} from a {@link ImageDisplay}. Shares planes of
	 * data when possible.
	 */
	@Override
	public ImagePlus createLegacyImage(final ImageDisplay display) {

		final ImageDisplayService imageDisplayService =
			context.getService(ImageDisplayService.class);

		final Dataset ds = imageDisplayService.getActiveDataset(display);

		if (ds.isRGBMerged()) {
			return colorImagePlusCreator.createLegacyImage(display);
		}

		return grayImagePlusCreator.createLegacyImage(display);
	}

}
