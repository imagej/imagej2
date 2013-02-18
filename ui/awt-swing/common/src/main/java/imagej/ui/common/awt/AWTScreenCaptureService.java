/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

package imagej.ui.common.awt;

import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.data.display.ScreenCaptureService;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;

import net.imglib2.RandomAccess;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.type.numeric.RealType;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * An AWT implementation of the ScreenCaptureService interface. Allows one to
 * capture regions of the screen to Datasets.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Service.class)
public class AWTScreenCaptureService
	extends AbstractService
	implements ScreenCaptureService
{
	
	@Parameter
	private DatasetService dataService;

	// -- public api --
	
	/**
	 * Captures a rectangular region of the screen and returns the result in
	 * a Dataset. Usually the data is in RGB format but could be others such
	 * as 8-bit gray or 16-bit gray.
	 */
	@Override
	public Dataset captureScreenRegion(int x, int y, int width, int height) {
		try {
			Rectangle r = new Rectangle(x, y, width, height);
			Robot robot = new Robot();
			BufferedImage img = robot.createScreenCapture(r);
			return datasetFromImage(img);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Captures the entire screen and returns the result in a Dataset. Usually
	 * the data is in RGB format but could be others such as 8-bit gray or
	 * 16-bit gray.
	 */
	@Override
	public Dataset captureScreen() {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		return captureScreenRegion(0, 0, dim.width, dim.height);
	}
	
	// -- private helpers --

	/**
	 * Make a Dataset from a BufferedImage.
	 * 
	 * @param img The input BufferedImage containing pixel data.
=		 * @return A Dataset populated with data from the BufferedImage.
	 */
	private Dataset datasetFromImage(BufferedImage img) {
		int type = img.getType();
		switch (type) {
			case BufferedImage.TYPE_BYTE_GRAY:
				return grayDataset(img, 8);
			case BufferedImage.TYPE_USHORT_GRAY:
				return grayDataset(img, 16);
			default:
				return rgbDataset(img);
		}
	}

	/**
	 * Make a gray Dataset from a BufferedImage.
	 * 
	 * @param img The input BufferedImage (known to be a gray type).
	 * @param bitsPerPixel 8 or 16 depending upon desired output bit depth.
	 * @return A Dataset populated with data from the BufferedImage.
	 */
	private Dataset grayDataset(BufferedImage img, int bitsPerPixel) {
		int width = img.getWidth();
		int height = img.getHeight();
		Dataset dataset = makeDataset(width, height, bitsPerPixel);
		RandomAccess<? extends RealType<?>> accessor =
				dataset.getImgPlus().randomAccess();
		Raster raster = img.getData();
		for (int x = 0; x < width; x++) {
			accessor.setPosition(x, 0);
			for (int y=0; y < height; y++) {
				accessor.setPosition(y, 1);
				int value = raster.getSample(x, y, 0);
				accessor.get().setReal(value);
			}
		}
		return dataset;
	}

	/** Make an RGB Dataset from a BufferedImage. */
	private Dataset rgbDataset(BufferedImage img) {
		int width = img.getWidth();
		int height = img.getHeight();
		Dataset dataset = makeDataset(width, height, 24);
		RandomAccess<? extends RealType<?>> accessor =
			dataset.getImgPlus().randomAccess();
		for (int x = 0; x < width; x++) {
			accessor.setPosition(x, 0);
			for (int y=0; y < height; y++) {
				accessor.setPosition(y, 1);
				int pixel = img.getRGB(x, y);
				for (int c = 0; c < 3; c++) {
					int byteNum = 2 - c;
					int shiftCount = 8 * byteNum;
					double value = (pixel >> shiftCount) & 0xff;
					accessor.setPosition(c, 2);
					accessor.get().setReal(value);
				}
			}
		}
		return dataset;
	}
	
	/** 
	 * Make a Dataset of given 2d dimensions and bits per pixel. Pixel values
	 * will be zero.
	 * 
	 * @param bitsPerPixel Must be 8, 16, or 24. 24 implies multichannel output.
	 */
	private Dataset makeDataset(int width, int height, int bitsPerPixel)
	{
		switch (bitsPerPixel) {
			case 8: case 16: case 24:
				break;
			default:
				throw new IllegalArgumentException("invalid bit depth specified");
		}
		String name = "Image Capture";
		final long[] dims;
		final AxisType[] axes;
		final int bpp;
		if (bitsPerPixel == 24) {
			dims = new long[]{width, height, 3};
			axes = new AxisType[]{Axes.X, Axes.Y, Axes.CHANNEL};
			bpp = 8;
		}
		else {
			dims = new long[]{width, height};
			axes = new AxisType[]{Axes.X, Axes.Y};
			bpp = bitsPerPixel;
		}
		boolean signed = false;
		boolean floating = false;
		Dataset ds = dataService.create(dims, name, axes, bpp, signed, floating);
		if (bitsPerPixel == 24) ds.setRGBMerged(true);
		return ds;
	}
}
