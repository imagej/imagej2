//
// DatasetViewBuilder.java
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

import java.util.ArrayList;

import net.imglib2.display.ColorTable8;
import net.imglib2.img.Axes;
import net.imglib2.img.ImgPlus;
import net.imglib2.type.numeric.RealType;

/**
 * Builder of DatasetViews...
 * 
 * @author Grant Harris
 */
public class DatasetViewBuilder {

	/** Creates a grayscale view with no color table. */
	public static DatasetView createView(final String name,
		final ImgPlus<? extends RealType<?>> img)
	{
		return createView(name, new Dataset(img));
	}

	public static DatasetView
		createView(final String name, final Dataset dataset)
	{
		final DatasetView view = new DatasetView(name, dataset, -1, null, false);
		return view;
	}

	/** Creates an RGB 3-channel composite view. */
	public static DatasetView createCompositeRGBView(final String name,
		final ImgPlus<? extends RealType<?>> img)
	{
		return createCompositeRGBView(name, new Dataset(img));
	}

	public static DatasetView createCompositeRGBView(final String name,
		final Dataset dataset)
	{
		final ImgPlus<? extends RealType<?>> img = dataset.getImgPlus();
		final ArrayList<ColorTable8> lutList = new ArrayList<ColorTable8>();
		lutList.add(ColorTables.RED);
		lutList.add(ColorTables.GREEN);
		lutList.add(ColorTables.BLUE);
		final int channelDimIndex = img.getAxisIndex(Axes.CHANNEL);
		if (channelDimIndex < 0) {
			System.err.println("No Channel dimension.");
		}
		final int channels = (int) img.dimension(channelDimIndex);
		if (channels != 3) {
			System.err.println("Creating RBG composite, but not 3 channels");
		}
		final DatasetView view =
			new DatasetView(name, dataset, channelDimIndex, lutList, true);
		return view;

	}

	// Composite
	//
	public static DatasetView createCompositeView(final String name,
		final ImgPlus<? extends RealType<?>> img)
	{
		return createCompositeView(name, new Dataset(img));
	}

	public static DatasetView createCompositeView(final String name,
		final Dataset dataset)
	{
		final ImgPlus<? extends RealType<?>> img = dataset.getImgPlus();
		// create a multichannel Compositeview, up to 6 channels
		final int channels = (int) img.dimension(img.getAxisIndex(Axes.CHANNEL));
		final ArrayList<ColorTable8> lutList = defaultLutList(channels);
		final int channelDimIndex = img.getAxisIndex(Axes.CHANNEL);
		if (channelDimIndex < 0) {
			// "No Channel dimension."
		}
		final DatasetView view =
			new DatasetView(name, dataset, channelDimIndex, lutList, true);
		return view;
	}

	// Multichannel, not composite
	//
	public static DatasetView createMultichannelView(final String name,
		final ImgPlus<? extends RealType<?>> img)
	{
		return createMultichannelView(name, new Dataset(img));
	}

	public static DatasetView createMultichannelView(final String name,
		final Dataset dataset)
	{
		// create a multichannel Compositeview, up to 6 channels
		final ImgPlus<? extends RealType<?>> img = dataset.getImgPlus();
		final int channels = (int) img.dimension(img.getAxisIndex(Axes.CHANNEL));
		final ArrayList<ColorTable8> lutList = defaultLutList(channels);
		final int channelDimIndex = img.getAxisIndex(Axes.CHANNEL);
		if (channelDimIndex < 0) {
			// "No Channel dimension."
		}
		final DatasetView view =
			new DatasetView(name, dataset, channelDimIndex, lutList, false);
		return view;
	}

	private static ArrayList<ColorTable8> defaultLutList(final int channels) {
		final ArrayList<ColorTable8> lutList = new ArrayList<ColorTable8>();
		lutList.add(ColorTables.RED);
		if (channels > 1) {
			lutList.add(ColorTables.GREEN);
		}
		if (channels > 2) {
			lutList.add(ColorTables.BLUE);
		}
		if (channels > 3) {
			lutList.add(ColorTables.CYAN);
		}
		if (channels > 4) {
			lutList.add(ColorTables.MAGENTA);
		}
		if (channels > 5) {
			lutList.add(ColorTables.YELLOW);
		}
		return lutList;
	}

}
