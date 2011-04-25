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

package imagej.display.view;

import imagej.data.Dataset;
import imagej.display.ImageCanvas;
import imagej.display.lut.ColorTable8;

import java.util.ArrayList;

import net.imglib2.converter.Converter;
import net.imglib2.display.ARGBScreenImage;
import net.imglib2.display.RealARGBConverter;
import net.imglib2.display.XYProjector;
import net.imglib2.img.ImgPlus;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;

/**
 * Composite color image with arbitrary number of channels, each with a Lut A
 * View into a a Dataset
 * 
 * @author Grant Harris
 */
public class DatasetView {

	private final Dataset dataset;
	private final ARGBScreenImage screenImage;
	private final ArrayList<Converter<? extends RealType<?>, ARGBType>> converters =
		new ArrayList<Converter<? extends RealType<?>, ARGBType>>();
	private XYProjector<? extends RealType<?>, ARGBType> projector;
	private int positionX;
	private int positionY;
	private final ImgPlus<? extends RealType<?>> img;
	private ImageCanvas imgCanvas;

	public DatasetView(final String name, final Dataset dataset,
		final int channelDimIndex, final ArrayList<ColorTable8> luts)
	{

		this.dataset = dataset;

		this.img = dataset.getImage();

		screenImage =
			new ARGBScreenImage((int) img.dimension(0), (int) img.dimension(1));
		final int min = 0, max = 255;
		if (channelDimIndex < 0) {
			if (luts != null) {
				projector =
					new XYProjector(img, screenImage, new RealLUTConverter(min, max,
						luts.get(0)));
			}
			else {
				projector =
					new XYProjector(img, screenImage, new RealARGBConverter(min, max));
			}
		}
		else {
			for (int i = 0; i < luts.size(); i++) {
				final ColorTable8 lut = luts.get(i);
				converters.add(new CompositeLUTConverter(min, max, lut));
			}
			projector =
				new CompositeXYProjector(img, screenImage, converters, channelDimIndex);
		}
		projector.map();
	}

	public void setImgCanvas(final ImageCanvas imgCanvas) {
		this.imgCanvas = imgCanvas;
	}

	public int getPositionX() {
		return positionX;
	}

	public void setPositionX(final int positionX) {
		this.positionX = positionX;
	}

	public int getPositionY() {
		return positionY;
	}

	public void setPositionY(final int positionY) {
		this.positionY = positionY;
	}

	//
	public ImgPlus<? extends RealType<?>> getImg() {
		return img;
	}

	public ARGBScreenImage getScreenImage() {
		return screenImage;
	}

	public ArrayList<Converter<? extends RealType<?>, ARGBType>> getConverters()
	{
		return converters;
	}

	public XYProjector getProjector() {
		return projector;
	}

	void project() {}

	void setPosition(final int value, final int dim) {
		projector.setPosition(value, dim);
		projector.map();
		// tell display components to repaint
		if (imgCanvas != null) imgCanvas.updateImage();

		// Dataset emits a DatasetChangedEvent;
	}

}
