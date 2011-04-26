//
// DatasetViewBuilder2.java
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
import imagej.display.ImageCanvas;

import java.util.ArrayList;

import net.imglib2.converter.Converter;
import net.imglib2.display.ARGBScreenImage;
import net.imglib2.display.ColorTable8;
import net.imglib2.display.XYProjector;
import net.imglib2.img.Axes;
import net.imglib2.img.ImgPlus;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;

/**
 * Builder of DatasetViews...
 * 
 * @author Grant Harris
 */
public class DatasetViewBuilder2 {

//		public DatasetView2(final String name, final Dataset dataset,
//		final int channelDimIndex, final ArrayList<ColorTable8> luts, boolean composite) {
//
//		this.dataset = dataset;
//		this.img = dataset.getImage();
//		this.channelDimIndex = channelDimIndex;
//		this.luts = luts;

	public static DatasetView2 createView(final String name, final ImgPlus img,
		final boolean composite)
	{
		return createView(name, img, (int) img.dimension(0), (int) img
			.dimension(1), composite);

	}

	public static DatasetView2 createView(final String name,
		final ImgPlus imgIn, final int scrnImgW, final int scrnImgH,
		final boolean composite)
	{

		final Dataset dataset = null;
		ARGBScreenImage screenImage;
		final ArrayList<Converter<? extends RealType<?>, ARGBType>> converters =
			new ArrayList<Converter<? extends RealType<?>, ARGBType>>();
		final XYProjector<? extends RealType<?>, ARGBType> projector = null;
		final int positionX;
		final int positionY;
		final ImgPlus<? extends RealType<?>> imgP;
		final ImageCanvas imgCanvas;
		final ArrayList<ColorTable8> luts;
		final int channelDimIndex = -1;

		screenImage = new ARGBScreenImage(scrnImgW, scrnImgH);

		final int min = 0, max = 255;

//
//		if (channelDimIndex < 0) { // No channels
//			if (luts != null) { // single channel, use lut for display.
//				projector =
//					new XYProjector(img, screenImage, new RealLUTConverter(min, max,
//					luts.get(0)));
//			} else {
//				projector =
//					new XYProjector(img, screenImage, new RealARGBConverter(min, max));
//			}
//		} else {
//
//			if (composite) { // multichannel composite
//				for (int i = 0; i < luts.size(); i++) {
//					ColorTable8 lut = luts.get(i);
//					converters.add(new CompositeLUTConverter(min, max, lut));
//				}
//				projector = new CompositeXYProjector(img, screenImage, converters, channelDimIndex);
//			} else { // multichannel with sep. ColorTable8 for each
//				projector = new LutXYProjector(img, screenImage, new RealLUTConverter(min, max, luts.get(0)));
//			}
//		}
//		projector.map();
//	}

		return new DatasetView2(name, dataset, converters, projector, screenImage,
			channelDimIndex, composite);
	}

	/*
	 * // create an RGB 3-channel Compositeview
	 */

	public static DatasetView createCompositeRGBView(final String name,
		final ImgPlus img)
	{
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
		final Dataset dataset = new Dataset(img);
		final DatasetView view =
			new DatasetView(name, dataset, channelDimIndex, lutList, true);

		return view;
	}

	/*
	 * Grayscale view
	 */
	public static DatasetView createView(final String name, final ImgPlus img) {
		final Dataset dataset = new Dataset(img);
		final DatasetView view = new DatasetView(name, dataset, -1, null, false);
		return view;
	}

	public static DatasetView createCompositeView(final String name,
		final ImgPlus img)
	{
		// create a multichannel Compositeview, up to 6 channels
		final int channels = (int) img.dimension(img.getAxisIndex(Axes.CHANNEL));
		final ArrayList<ColorTable8> lutList = defaultLutList(channels);
		final int channelDimIndex = img.getAxisIndex(Axes.CHANNEL);
		if (channelDimIndex < 0) {
			// "No Channel dimension."
		}
		final Dataset dataset = new Dataset(img);
		final DatasetView view =
			new DatasetView(name, dataset, channelDimIndex, lutList, true);
		return view;
	}

	public static DatasetView createMultichannelView(final String name,
		final ImgPlus img)
	{
		// create a multichannel Compositeview, up to 6 channels
		final int channels = (int) img.dimension(img.getAxisIndex(Axes.CHANNEL));
		final ArrayList<ColorTable8> lutList = defaultLutList(channels);
		final int channelDimIndex = img.getAxisIndex(Axes.CHANNEL);
		if (channelDimIndex < 0) {
			// "No Channel dimension."
		}
		final Dataset dataset = new Dataset(img);
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
