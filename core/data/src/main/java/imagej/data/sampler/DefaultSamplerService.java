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

package imagej.data.sampler;

import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.data.display.DatasetView;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.OverlayService;
import imagej.data.overlay.Overlay;
import imagej.display.DisplayService;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.RandomAccess;
import net.imglib2.display.ColorTable;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.meta.CalibratedAxis;
import net.imglib2.meta.ImgPlus;
import net.imglib2.meta.IntervalUtils;
import net.imglib2.type.numeric.RealType;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.util.RealRect;

//TODO
//1) Maintain metadata
//2) maintain overlays: does an overlay in Z == 7 show up on correct slice
// in output data?
//3) report parse error string somehow
//5) test the contains(num) code works
//TODO - multiple places I'm relying on a Display's axes rather than a
//Dataset's axes. See if there are problems with this
//TODO - the iterators work with Lists which can only hold 2 gig or fewer
//elements. Thus data cannot be copied > 2 gig per dimension.
//TODO:
//-- for speed eliminate reliance on Longs. Make primitive arrays.
//-- make a EnormousList that can store more than 2 gig of longs
//In other plugins replace RestructureUtils calls with methods from here
//Note when user specifies 1-1000 I am storing them all consecutively (i.e.
// 1,2,3,4,5...1000. For small amounts this is fine but might want something
// more sophisticated for large dimensional ranges.

/**
 * Default implementation of {@link SamplerService}.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Service.class)
public class DefaultSamplerService extends AbstractService implements
	SamplerService
{

	// -- instance variables --

	@Parameter
	private DisplayService displayService;

	@Parameter
	private DatasetService datasetService;

	@Parameter
	private OverlayService overlayService;

	@Parameter
	private ImageDisplayService imgDispService;

	// -- public interface --

	@Override
	public ImageDisplay createSampledImage(final SamplingDefinition def) {
		if (def.getError() != null) {
			throw new IllegalArgumentException(
				"SamplingDefinition error: "+def.getError());
		}
		final ImageDisplay outputImage = createOutputImage(def);
		copyData(def, outputImage);
		return outputImage;
	}

	@Override
	public ImageDisplay duplicate(final ImageDisplay display) {
		final SamplingDefinition copyDef =
			SamplingDefinition.sampleAllPlanes(display);
		return createSampledImage(copyDef);
	}

	@Override
	public ImageDisplay duplicateSelectedPlane(final ImageDisplay display) {
		final SamplingDefinition copyDef =
			SamplingDefinition.sampleXYPlane(display);
		final RealRect selection = overlayService.getSelectionBounds(display);
		final long minX = (long) selection.x;
		final long minY = (long) selection.y;
		final long maxX = (long) (selection.x + selection.width);
		final long maxY = (long) (selection.y + selection.height);
		final AxisSubrange xSubrange = new AxisSubrange(minX, maxX);
		final AxisSubrange ySubrange = new AxisSubrange(minY, maxY);
		copyDef.constrain(Axes.X, xSubrange);
		copyDef.constrain(Axes.Y, ySubrange);
		return createSampledImage(copyDef);
	}

	@Override
	public ImageDisplay
		duplicateSelectedCompositePlane(final ImageDisplay display)
	{
		final SamplingDefinition copyDef =
			SamplingDefinition.sampleCompositeXYPlane(display);
		final RealRect selection = overlayService.getSelectionBounds(display);
		final long minX = (long) selection.x;
		final long minY = (long) selection.y;
		final long maxX = (long) (selection.x + selection.width);
		final long maxY = (long) (selection.y + selection.height);
		final AxisSubrange xSubrange = new AxisSubrange(minX, maxX);
		final AxisSubrange ySubrange = new AxisSubrange(minY, maxY);
		copyDef.constrain(Axes.X, xSubrange);
		copyDef.constrain(Axes.Y, ySubrange);
		return createSampledImage(copyDef);
	}

	@Override
	public ImageDisplay duplicateSelectedPlanes(final ImageDisplay display) {
		final SamplingDefinition copyDef =
			SamplingDefinition.sampleAllPlanes(display);
		final RealRect selection = overlayService.getSelectionBounds(display);
		final long minX = (long) selection.x;
		final long minY = (long) selection.y;
		final long maxX = (long) (selection.x + selection.width);
		final long maxY = (long) (selection.y + selection.height);
		final AxisSubrange xSubrange = new AxisSubrange(minX, maxX);
		final AxisSubrange ySubrange = new AxisSubrange(minY, maxY);
		copyDef.constrain(Axes.X, xSubrange);
		copyDef.constrain(Axes.Y, ySubrange);
		return createSampledImage(copyDef);
	}

	// -- private helpers --

	/**
	 * Creates an output image from a sampling definition. All data initialized to
	 * zero.
	 */
	private ImageDisplay createOutputImage(final SamplingDefinition def) {
		final ImageDisplay origDisp = def.getDisplay();
		// TODO - remove evil cast
		final Dataset origDs = (Dataset) origDisp.getActiveView().getData();
		final long[] dims = def.getOutputDims();
		final String name = origDisp.getName();
		final AxisType[] axes = def.getOutputAxes();
		final CalibratedAxis[] calibAxes = def.getOutputCalibratedAxes();
		final int bitsPerPixel = origDs.getType().getBitsPerPixel();
		final boolean signed = origDs.isSigned();
		final boolean floating = !origDs.isInteger();
		final Dataset output =
			datasetService.create(dims, name, axes, bitsPerPixel, signed, floating);
		output.setAxes(calibAxes);
		long numPlanes = calcNumPlanes(dims, axes);
		if (numPlanes > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(
				"output image has more too many planes " + numPlanes + " (max = " +
					Integer.MAX_VALUE + ")");
		}
		output.getImgPlus().initializeColorTables((int)numPlanes);
		if (origDs.isRGBMerged()) {
			final int chanAxis = output.dimensionIndex(Axes.CHANNEL);
			if (chanAxis >= 0) {
				if (output.dimension(chanAxis) == 3) {
					output.setRGBMerged(true);
				}
			}
		}
		// TODO - remove evil cast
		return (ImageDisplay) displayService.createDisplay(name, output);
	}

	/**
	 * Copies all associated data from a SamplingDefinition to an output image.
	 */
	private void copyData(final SamplingDefinition def,
		final ImageDisplay outputImage)
	{
		final PositionIterator iter1 = new SparsePositionIterator(def);
		final PositionIterator iter2 = new DensePositionIterator(def);
		// TODO - remove evil casts
		final Dataset input = (Dataset) def.getDisplay().getActiveView().getData();
		final Dataset output = (Dataset) outputImage.getActiveView().getData();
		final long[] inputDims = IntervalUtils.getDims(input);
		final long[] outputDims = IntervalUtils.getDims(output);
		final RandomAccess<? extends RealType<?>> inputAccessor =
			input.getImgPlus().randomAccess();
		final RandomAccess<? extends RealType<?>> outputAccessor =
			output.getImgPlus().randomAccess();
		while (iter1.hasNext() && iter2.hasNext()) {

			// determine data positions within datasets
			final long[] inputPos = iter1.next();
			final long[] outputPos = iter2.next();
			inputAccessor.setPosition(inputPos);
			outputAccessor.setPosition(outputPos);

			// copy value
			final double value = inputAccessor.get().getRealDouble();
			outputAccessor.get().setReal(value);

			// TODO - notice there is a lot of inefficiency following here.
			// We are setting color tables once per pixel in image and do a lot of
			// calculation to figure out what table. This should be done once per
			// plane.
			
			// keep dataset color tables in sync
			final int inputPlaneNumber = planeNum(inputDims, inputPos);
			final ColorTable lut = input.getColorTable(inputPlaneNumber);
			final int outputPlaneNumber = planeNum(outputDims, outputPos);
			output.setColorTable(lut, outputPlaneNumber);
		}
		// TODO - enable this code
		// List<Overlay> overlays = overlayService.getOverlays(def.getDisplay());
		// attachOverlays(def.getDisplay(), outputImage, overlays);

		/* TODO
		setOtherMetadata();  // user defined info that has been added to orig data
		*/

		// keep composite status in sync
		setCompositeChannelCount(input, output);

		// keep display color tables in sync
		updateDisplayColorTables(def, outputImage);

		// Invalidate the cached channel min and max in the output ImgPlus
		// This may be set to something other than the obvious values
		// by the autoscale service, I (leek) think.
		final ImgPlus<? extends RealType<?>> outputImgPlus = output.getImgPlus();
		for (int channel=0; channel < outputImgPlus.getCompositeChannelCount(); channel++) {
			outputImgPlus.setChannelMinimum(channel, Double.NaN);
			outputImgPlus.setChannelMaximum(channel, Double.NaN);
		}

		// set the display range from input data's view min/max settings
		setDisplayRanges(def, outputImage);
	}

	/** Calculates a plane number from a position within a dimensional space. */
	private int planeNum(final long[] dims, final long[] pos) {
		int plane = 0;
		int inc = 1;
		// TODO - assumes X & Y are 1st two dims
		for (int i = 2; i < dims.length; i++) {
			plane += pos[i] * inc;
			inc *= dims[i];
		}
		return plane;
	}

	/**
	 * Sets an output Dataset's composite channel count based upon an input
	 * Dataset's composite channel characteristics.
	 */
	private void setCompositeChannelCount(final Dataset input,
		final Dataset output)
	{
		if (input.getCompositeChannelCount() == 1) return;
		final int index = output.dimensionIndex(Axes.CHANNEL);
		final long numChannels = (index < 0) ? 1 : output.dimension(index);
		output.setCompositeChannelCount((int) numChannels);
		// outside viewers need to know composite channel count changed
		output.rebuild();
	}

	/**
	 * Copies the appropriate color tables from the input ImageDisplay to the
	 * output ImageDisplay.
	 */
	private void updateDisplayColorTables(
		final SamplingDefinition def,
		final ImageDisplay output)
	{
		final ImageDisplay input = def.getDisplay();
		final DatasetView inView = imgDispService.getActiveDatasetView(input);
		final DatasetView outView = imgDispService.getActiveDatasetView(output);
		final List<ColorTable> inputColorTables = inView.getColorTables();
		final int inputChanAxis = input.dimensionIndex(Axes.CHANNEL); 
		final List<List<Long>> inputRanges = def.getInputRanges();
		for (int i = 0; i < inputColorTables.size(); i++) {
			int outIndex = outputColorTableNumber(inputRanges, i, inputChanAxis);
			if (outIndex >= 0) {
				outView.setColorTable(inputColorTables.get(i), outIndex);
			}
		}
		/*
		final int chAxisIn = input.dimensionIndex(Axes.CHANNEL);
		final int chAxisOut = output.dimensionIndex(Axes.CHANNEL);
		if (chAxisIn < 0 || chAxisOut < 0) {
			return;
		}
		if (input.dimension(chAxisIn) != output.dimension(chAxisOut)) {
			return;
		}

		// otherwise if here the two displays have channel axes of the same size

		// TODO - cannot assume 1 color table per channel, right? if so then no
		// idea how to copy color tables. For now will assume 1 per channel
		for (int c = 0; c < colorTables.size(); c++) {
			final ColorTable8 table = colorTables.get(c);
			outView.setColorTable(table, c);
		}
		*/
	}
	
	private int outputColorTableNumber(
		List<List<Long>> inputRanges, int inputChannel, int inputChanAxis)
	{
		if (inputChanAxis < 0) {
			if (inputChannel == 0) return 0;
			return -1;
		}
		List<Long> channelRanges = inputRanges.get(inputChanAxis);
		for (int pos = 0; pos < channelRanges.size(); pos++) {
			if (channelRanges.get(pos) == inputChannel)
				return pos;
		}
		return -1;
	}

	// TODO - utilize me? Maybe it doesn't make sense to duplicate overlays.
	// Also its not really correct as written. Overlays all go in the 1st
	// view plane rather than on each separate view plane from which they came.
	
	private void attachOverlays(final ImageDisplay inputDisp,
		final ImageDisplay outputDisp, final List<Overlay> overlays)
	{
		final RealRect bounds = overlayService.getSelectionBounds(inputDisp);
		final double[] toOrigin = new double[2];
		toOrigin[0] = -bounds.x;
		toOrigin[1] = -bounds.y;
		final List<Overlay> newOverlays = new ArrayList<Overlay>();
		for (final Overlay overlay : overlays) {
			if (overlayWithinBounds(overlay, bounds)) {
				// add a reference to existing overlay?
				if (toOrigin[0] == 0 && toOrigin[1] == 0) {
					newOverlays.add(overlay);
				}
				else { // different origins means must create new overlays
					final Overlay newOverlay = overlay.duplicate();
					newOverlay.move(toOrigin);
					newOverlays.add(newOverlay);
				}
			}
		}
		overlayService.addOverlays(outputDisp, newOverlays);
	}

	private boolean overlayWithinBounds(final Overlay overlay,
		final RealRect bounds)
	{
		if (overlay.realMin(0) < bounds.x) return false;
		if (overlay.realMin(1) < bounds.y) return false;
		if (overlay.realMax(0) > bounds.x + bounds.width) return false;
		if (overlay.realMax(1) > bounds.y + bounds.height) return false;
		return true;
	}

	private long calcNumPlanes(long[] dims, AxisType[] axes) {
		long num = 1;
		for (int i = 0; i < dims.length; i++) {
			AxisType type = axes[i];
			if (type == Axes.X || type == Axes.Y) continue;
			num *= dims[i];
		}
		return num;
	}
	
	private void setDisplayRanges(SamplingDefinition def, ImageDisplay output) {
		final ImageDisplay input = def.getDisplay();
		final DatasetView inView = imgDispService.getActiveDatasetView(input);
		final DatasetView outView = imgDispService.getActiveDatasetView(output);
		final int inputChanAxis = input.dimensionIndex(Axes.CHANNEL);
		final List<List<Long>> inputRanges = def.getInputRanges();
		for (int i = 0; i < inView.getChannelCount(); i++) {
			int outIndex = outputColorTableNumber(inputRanges, i, inputChanAxis);
			if (outIndex >= 0) {
				double min = inView.getChannelMin(i);
				double max = inView.getChannelMax(i);
				outView.setChannelRange(outIndex, min, max);
			}
		}
	}
}
