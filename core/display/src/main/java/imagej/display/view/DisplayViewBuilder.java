//
// DisplayViewBuilder.java
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

package imagej.display.view;

import imagej.data.Dataset;
import imagej.display.lut.ColorTable8;
import imagej.display.lut.ColorTables;
import java.util.ArrayList;
import net.imglib2.img.Axes;
import net.imglib2.img.ImgPlus;

/**
 * TODO
 *
 * @author Grant Harris
 */
public class DisplayViewBuilder {

	public static DatasetView createCompositeRGBView(final String name,
		final ImgPlus img) {
		// create an RGB 3-channel Compositeview, with channel on axis 2
		ArrayList<ColorTable8> lutList = new ArrayList<ColorTable8>();
		lutList.add(ColorTables.RED);
		lutList.add(ColorTables.GREEN);
		lutList.add(ColorTables.BLUE);
		int channelDimIndex = img.getAxisIndex(Axes.CHANNEL);
		if (channelDimIndex < 0) {
			//"No Channel dimension."
		}
		Dataset dataset = new Dataset(img);
		final DatasetView view = new DatasetView(name, dataset, channelDimIndex, lutList);
		
		return view;
	}

	public static DatasetView createView(final String name,
		final ImgPlus img) {
		Dataset dataset = new Dataset(img);
		final DatasetView view = new DatasetView(name, dataset, -1, null);
		return view;
	}



}
