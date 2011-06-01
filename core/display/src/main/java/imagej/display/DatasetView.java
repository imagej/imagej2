//
// DatasetView.java
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

package imagej.display;

import imagej.data.Dataset;

import java.util.List;

import net.imglib2.display.ARGBScreenImage;
import net.imglib2.display.ColorTable8;
import net.imglib2.display.CompositeXYProjector;
import net.imglib2.display.RealLUTConverter;
import net.imglib2.img.ImgPlus;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;

/**
 * A linkage between a {@link Dataset} and a {@link Display}. The view takes
 * care of mapping the N-dimensional data into a representation suitable for
 * showing onscreen.
 * 
 * @author Curtis Rueden
 */
public interface DatasetView extends DisplayView {

	ARGBScreenImage getScreenImage();

	int getCompositeDimIndex();

	int getOffsetX();

	void setOffsetX(final int offsetX);

	int getOffsetY();

	void setOffsetY(final int offsetY);

	ImgPlus<? extends RealType<?>> getImgPlus();

	CompositeXYProjector<? extends RealType<?>, ARGBType> getProjector();

	List<RealLUTConverter<? extends RealType<?>>> getConverters();

	void setComposite(final boolean composite);

	List<ColorTable8> getColorTables();

	void setColorTable(final ColorTable8 colorTable, final int channel);

	void resetColorTables(final boolean grayscale);

	@Override
	Dataset getDataObject();

}
