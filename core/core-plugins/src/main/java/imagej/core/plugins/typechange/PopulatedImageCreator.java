//
// ImageCreator.java
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

package imagej.core.plugins.typechange;

import net.imglib2.container.ContainerFactory;
import net.imglib2.cursor.LocalizableByDimCursor;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.*;
import net.imglib2.type.numeric.real.*;
import net.imglib2.type.logic.*;
import imagej.data.Dataset;

// NOTE that Imglib does not do any range clamping while moving between image
// types. So translations may not always be nice (i.e. narrowing cases) but
// best behavior may be undefined.

/** creates an Imglib Image of user specified type. It uses an input Image
 * to determine container type, and image dimensions of output image. It also
 * populates the output Image's data from the input Image (which is likely of
 * a different data type). No range clamping of data is done.
 *  
 * @author bdezonia
 *
 */
public class PopulatedImageCreator {
	
	private PopulatedImageCreator() {
		// noninstantiable
	}
	
	public static <T extends RealType<T>> Image<T>
		createPopulatedImage(Image<? extends RealType<?>> currImageData,
			T newType) {
		
		Image<T> newImageData;

		ContainerFactory containerFactory =
			currImageData.getContainerFactory();

		ImageFactory<T> imageFactory =
			new ImageFactory<T>(newType, containerFactory);
		
		newImageData = 
			imageFactory.createImage(
				currImageData.getDimensions(),
				currImageData.getName());
		
		LocalizableByDimCursor<? extends RealType<?>> currDataCursor =
			currImageData.createLocalizableByDimCursor();
		
		LocalizableByDimCursor<T> newDataCursor =
			newImageData.createLocalizableByDimCursor();
		
		int[] currPos = currDataCursor.createPositionArray();
		while (currDataCursor.hasNext()) {
			currDataCursor.fwd();
			currDataCursor.getPosition(currPos);
			newDataCursor.setPosition(currPos);
			double currValue = currDataCursor.getType().getRealDouble();
			newDataCursor.getType().setReal(currValue);
		}
		
		currDataCursor.close();
		newDataCursor.close();
		
		return newImageData;
	}
}
