//
// ImageTranslator.java
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

package imagej.legacy;

import ij.ImagePlus;
import imagej.model.AxisLabel;
import imagej.model.Dataset;
import imagej.model.Metadata;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.image.ImagePlusAdapter;
import mpicbg.imglib.image.display.imagej.ImageJFunctions;

/**
 * Translates between legacy and modern ImageJ image structures.
 * 
 * @author Curtis Rueden
 */
public class ImageTranslator {

	public Dataset createDataset(final ImagePlus imp) {
		// HACK - avoid ImagePlusAdapter.wrap method's use of generics
		final Image<?> img;
		try {
			final Method m =
				ImagePlusAdapter.class.getMethod("wrap", ImagePlus.class);
			img = (Image<?>) m.invoke(null, imp);
		}
		catch (final NoSuchMethodException exc) {
			return null;
		}
		catch (final IllegalArgumentException e) {
			return null;
		}
		catch (final IllegalAccessException e) {
			return null;
		}
		catch (final InvocationTargetException e) {
			return null;
		}
		final Metadata metadata = populateMetadata(imp);
		final Dataset dataset = new Dataset(img, metadata);
		return dataset;
	}

	public ImagePlus createLegacyImage(final Dataset dataset) {
		return ImageJFunctions.displayAsVirtualStack(dataset.getImage());
	}

	private Metadata populateMetadata(final ImagePlus imp) {
		final AxisLabel[] axes =
			{ AxisLabel.X, AxisLabel.Y, AxisLabel.CHANNEL, AxisLabel.Z,
				AxisLabel.TIME };
		final Metadata metadata = new Metadata();
		metadata.setName(imp.getTitle());
		metadata.setAxes(axes);
		return metadata;
	}

}
