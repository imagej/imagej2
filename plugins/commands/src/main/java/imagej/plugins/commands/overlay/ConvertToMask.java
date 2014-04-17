/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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
 * #L%
 */

package imagej.plugins.commands.overlay;

import imagej.command.Command;
import imagej.command.ContextCommand;
import imagej.data.Dataset;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.overlay.BinaryMaskOverlay;
import imagej.data.overlay.Overlay;
import imagej.data.overlay.ThresholdOverlay;
import imagej.data.threshold.ThresholdService;
import imagej.menu.MenuConstants;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.basictypeaccess.array.BitArray;
import net.imglib2.img.transform.ImgTranslationAdapter;
import net.imglib2.ops.pointset.PointSetIterator;
import net.imglib2.roi.BinaryMaskRegionOfInterest;
import net.imglib2.type.logic.BitType;

import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.ColorRGB;

/**
 * Creates an {@link Overlay} representing the thresholded pixels of a
 * {@link Dataset}.
 * 
 * @author Lee Kamentsky
 * @author Barry DeZonia
 */
@Plugin(type = Command.class, menu = {
	@Menu(label = MenuConstants.PROCESS_LABEL,
		weight = MenuConstants.PROCESS_WEIGHT,
		mnemonic = MenuConstants.PROCESS_MNEMONIC),
	@Menu(label = "Binary", mnemonic = 'b'),
	@Menu(label = "Make Mask Overlay...") }, headless = true)
public class ConvertToMask extends ContextCommand {

	// -- Parameters --

	// TODO - this points out an issue with threshold service. Does it map
	// thresh overlays to image displays or datasets. In the support code both
	// are used.

	@Parameter(
		label = "Input display",
		description = "The display containing the image to be converted to a binary mask.")
	private ImageDisplay input;

	@Parameter(label = "Output mask",
		description = "The overlay that is the result of the operation",
		type = ItemIO.OUTPUT)
	private Overlay output;

	@Parameter(label = "Overlay color",
		description = "The color used to display the overlay")
	private ColorRGB color = new ColorRGB(255, 0, 0);

	@Parameter(label = "Overlay alpha", description = "The transparency "
		+ "(transparent = 0) or opacity (opaque=255) of the overlay", min = "0",
		max = "255")
	private int alpha = 128;

	@Parameter
	private Context context;

	@Parameter
	private ImageDisplayService dispSrv;

	@Parameter
	private ThresholdService threshSrv;

	// -- accessors --

	public void setInput(ImageDisplay disp) {
		input = disp;
	}

	public ImageDisplay getInput() {
		return input;
	}

	public void setColor(ColorRGB val) {
		color = val;
	}

	public ColorRGB getColor() {
		return color;
	}

	public void setAlpha(int val) {
		alpha = val;
	}

	public int getAlpha() {
		return alpha;
	}

	public Overlay getOutput() {
		return output;
	}

	// -- Command methods --

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void run() {
		if (!threshSrv.hasThreshold(input)) {
			cancel("This command requires a thresholded image.");
			return;
		}
		ThresholdOverlay thresh = threshSrv.getThreshold(input);
		PointSetIterator iter = thresh.getPointsWithin().iterator();
		if (!iter.hasNext()) {
			cancel("No pixels are within the threshold");
			return;
		}
		Dataset ds = dispSrv.getActiveDataset(input);
		final int numDims = ds.numDimensions();
		final long[] dimensions = new long[numDims];
		final long[] min = new long[numDims];
		/*
		 * First pass - find minima and maxima so we can use a shrunken image in some cases.
		 */
		for (int i = 0; i < numDims; i++) {
			min[i] = (long) Math.floor(thresh.realMin(i));
			dimensions[i] =
				(long) Math.floor(thresh.realMax(i) - thresh.realMin(i) + 1);
		}
		final ArrayImg<BitType, BitArray> arrayMask =
			new ArrayImgFactory<BitType>().createBitInstance(dimensions, 1);
		final BitType t = new BitType(arrayMask);
		arrayMask.setLinkedType(t);
		final Img<BitType> mask =
			new ImgTranslationAdapter<BitType, ArrayImg<BitType, BitArray>>(
				arrayMask, min);
		final RandomAccess<BitType> raMask = mask.randomAccess();
		iter.reset();
		while (iter.hasNext()) {
			long[] pos = iter.next();
			raMask.setPosition(pos);
			raMask.get().set(true);
		}
		output =
			new BinaryMaskOverlay(context,
				new BinaryMaskRegionOfInterest<BitType, Img<BitType>>(mask));
		output.setAlpha(alpha);
		output.setFillColor(color);
		for (int i = 0; i < numDims; i++) {
			output.setAxis(ds.axis(i), i);
		}
	}
}
