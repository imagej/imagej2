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

package imagej.core.plugins.display;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;

import net.imglib2.RandomAccess;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.type.numeric.RealType;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.data.display.ImageCanvas;
import imagej.data.display.ImageDisplay;
import imagej.ext.menu.MenuConstants;
import imagej.ext.module.ItemIO;
import imagej.ext.plugin.RunnablePlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.service.AbstractService;
import imagej.util.IntCoords;

// NOTE: the following TODO may be invalid
// TODO - write code that captures part of the screen as a merged color Dataset.
// Then use it for Capture Screen, Capture Image, and Flatten. Can get rid of
// the capture logic in the ImageDisplayViewer hierarchy. And can get rid of
// ImageGrabber maybe. Would also fix issue where current Flatten code draws
// JHotDraw ellipses less well than how they appear in the canvas.

// NOTE:
// In IJ1 Flatten and Image Capture are different beasts.
//   Flatten makes an RGB image from the current view. 
//     Zoom level is ignored and the data dimensions match the input image.
//     IJ2's flatten seems to be working correctly.
//   Image Capture does a screen grab of the current image window. So its pixel
//     format can be a number of things (though in practice I always get RGB).
//     Regardless since its is represented by an ImagePlus it must end up one
//     of ImageJ1's 4 pixel types. Note that a zoomed image captures as the
//     magnified data and dimensions may not match original input image.
//
// So we probably need a way to capture data in screen pixel format of the
// current image window view (i.e. current zoom matters). And in Imglib world
// we only have ARGB data. So we should do a screen grab like IJ2 of the coords
// of the current image window. All without using AWT.


/**
 * Captures the current view of an {@link ImageDisplay} to a {@link Dataset}.
 * Unlike the Flatten command this plugin captures a view of the current image
 * canvas (without mouse cursor present). Therefore the current zoom level is
 * represented in the output data.
 * 
 * @author Barry DeZonia
 */
@Plugin(iconPath = "/icons/bricks.png", menu = {
	@Menu(label = MenuConstants.PLUGINS_LABEL,
			weight = MenuConstants.PLUGINS_WEIGHT,
			mnemonic = MenuConstants.PLUGINS_MNEMONIC),
	@Menu(label = "Utilities"),
	@Menu(label = "Capture Image", weight = 20)})
public class CaptureImage implements RunnablePlugin {

	// -- Parameters --
	
	@Parameter(required=true)
	private DatasetService datasetService;
	
	@Parameter(required=true)
	private ImageJ context;
	
	@Parameter(required=true)
	private ImageDisplay display;
	
	@Parameter(type=ItemIO.OUTPUT)
	private Dataset output;

	//TODO - make it a @Parameter later
	private ScreenGrabberService grabberService;
	
	// -- instance variables --
	
	private int foundOriginX;
	private int foundOriginY;
	
	// -- accessors --
	
	public void setImageDisplay(ImageDisplay disp) {
		display = disp;
	}

	public ImageDisplay getImageDisplay() {
		return display;
	}
	
	public Dataset getOutput() {
		return output;
	}
	
	// -- run() method --
	
	@Override
	public void run() {
		grabberService = new ScreenGrabberService(context);
		grabberService.setScreenGrabber(new AwtScreenGrabber(datasetService));
		ImageCanvas canvas = display.getCanvas();
		// TEMP - hack until we make display code return screen coords of canvas
		findPanelOriginInScreenSpace(canvas);
		int x = foundOriginX;
		int y = foundOriginY;
		int width = canvas.getViewportWidth();
		int height = canvas.getViewportHeight();
		output = grabberService.captureScreenRegion(x, y, width, height);
		String name = display.getName();
		output.setName(name);
	}

	// HACK
	// Repeatedly take screen coords and ask panel if its in image. If search from
	// top left we can find origin of panel. Its a slow hack. We should use screen
	// dimensions in the for loops soon. And later make UI return screen coords
	// of ImageCanvas origin.
	
	private void findPanelOriginInScreenSpace(ImageCanvas canvas) {
		foundOriginX = 0;
		foundOriginY = 0;
		IntCoords point = new IntCoords(0, 0);
		for (int x = 0; x < 3000; x++) {
			point.x = x;
			for (int y = 0; y < 3000; y++) {
				point.y = y;
				// THIS CAN"T WORK. THE CALL EXPECTS PANEL COORDS AND NOT SCREEN COORDS.
				// SO RESULT IS ALWAYS (0,0).
				if (canvas.isInImage(point)) {
					foundOriginX = x;
					foundOriginY = y;
					return;
				}
			}
		}
	}

	/**
	 * An interface for supporting the capturing of screen data to a Dataset.
	 * 
	 * @author Barry DeZonia
	 */
	private interface ScreenGrabber {
		Dataset captureScreenRegion(int x, int y, int width, int height);
	}

	/**
	 * A service that allows portions of the screen to be grabbed from the the OS
	 * in a gui agnostic fashion.
	 *  
	 * @author Barry DeZonia
	 */
	private class ScreenGrabberService extends AbstractService {

		// -- instance variables --
		
		private ScreenGrabber grabber;
		
		// -- constructors --
		
		/**
		 * Default constructor. Needed for sezpoz compatiblity and is not to be
		 * used.
		 */
		public ScreenGrabberService() {
			// NB - needed by Sezpoz
			super(null);
			throw new UnsupportedOperationException(
				"this constructor not meant to be used");
		}
		
		// NOTE Is this enough and use setScreenGrabber() below at approp time? Or
		// do we pass in grabber as a constructor parameter?
		
		/** Creates a ScreenGrabberService for a given ImageJ context. */
		public ScreenGrabberService(ImageJ context) {
			super(context);
		}
		
		// NOTE - the UI init code will call this at the appropriate time with the
		//   UI specific screen grabber.
		
		/** Set the (UI-specific) ScreenGrabber of this ScreenGrabberService. */
		public void setScreenGrabber(ScreenGrabber grabber) {
			this.grabber = grabber;
		}
		
		/** Capture screen data to a Dataset */
		public Dataset captureScreenRegion(int x, int y, int w, int h) {
			return grabber.captureScreenRegion(x, y, w, h);
		}
	}

	/**
	 * An AWT implementation of the ScreenGrabber interface.
	 * 
	 * @author Barry DeZonia
	 */
	private class AwtScreenGrabber implements ScreenGrabber {

		// -- instance variables --
		
		private final DatasetService dataService;

		// -- constructor --

		/** Constructor: creates an AwtScreenGrabber using a DatasetService. */
		public AwtScreenGrabber(DatasetService datasetService) {
			this.dataService = datasetService;
		}
		
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

		// -- private helpers --

		/**
		 * Make a Dataset from a BufferedImage.
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
}
