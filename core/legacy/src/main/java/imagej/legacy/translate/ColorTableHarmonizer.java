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

package imagej.legacy.translate;

import ij.CompositeImage;
import ij.ImagePlus;
import ij.process.LUT;
import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.display.ColorTables;
import imagej.data.display.DataView;
import imagej.data.display.DatasetView;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;

import java.awt.image.IndexColorModel;
import java.util.ArrayList;
import java.util.List;

import net.imglib2.display.ColorTable;
import net.imglib2.display.ColorTable8;
import net.imglib2.display.RealLUTConverter;
import net.imglib2.type.numeric.RealType;

/**
 * This class synchronizes {@link ImageDisplay} {@link ColorTable}s with
 * {@link ImagePlus} {@link LUT}s.
 * 
 * @author Barry DeZonia
 */
public class ColorTableHarmonizer implements DisplayHarmonizer {

	private final ImageJ context;

	public ColorTableHarmonizer(ImageJ context) {
		this.context = context;
	}
	
	/**
	 * Sets the ColorTables of the active view of an IJ2 ImageDisplay from the
	 * LUTs of a given ImagePlus or CompositeImage.
	 */
	@Override
	public void updateDisplay(final ImageDisplay disp, final ImagePlus imp) {
		final boolean sixteenBitLuts = imp.getType() == ImagePlus.GRAY16;
		final List<ColorTable<?>> colorTables = colorTablesFromImagePlus(imp);
		assignColorTables(disp, colorTables, sixteenBitLuts);
		assignChannelMinMax(disp, imp);

		/* BDZ REMOVING on Dec-8-2011. Does not seem to be needed anymore. Resurrect
		 * later if needed again.
		// Force current plane to redraw : HACK to fix bug #668
		final DataView dispView = disp.getActiveView();
		if (dispView == null) return;
		final DatasetView dsView = (DatasetView) dispView;
		dsView.getProjector().map();
		disp.update();
		*/
	}

	/**
	 * Sets LUTs of an ImagePlus or CompositeImage. If given an ImagePlus this
	 * method sets it's single LUT from the first ColorTable of the active view
	 * that displays the active Dataset of the given ImageDisplay. If given a
	 * CompositeImage this method sets all it's LUTs from the ColorTables of the
	 * active view of the given ImageDisplay. In both cases if there is no active
	 * view for the ImageDisplay the LUTs are assigned with sensible default
	 * values.
	 */
	@Override
	public void updateLegacyImage(final ImageDisplay disp, final ImagePlus imp) {
		final DatasetView activeView = (DatasetView) disp.getActiveView();
		if (imp instanceof CompositeImage) {
			final CompositeImage ci = (CompositeImage) imp;
			final List<ColorTable8> colorTables =
				(activeView == null) ? null : activeView.getColorTables();
			setCompositeImageLuts(ci, colorTables);
			final int composChannCount =
				(activeView == null) ? 1 : activeView.getData()
					.getCompositeChannelCount();
			setCompositeImageMode(ci, composChannCount, colorTables);
		}
		else { // regular ImagePlus
			// NOTE to fix bug #849 the nonnull case was added below. This reflects
			// a significant behavior change.
			if (activeView == null) {
				final ImageDisplayService imageDisplayService =
					context.getService(ImageDisplayService.class);
				final Dataset ds = imageDisplayService.getActiveDataset(disp);
				setImagePlusLutToFirstInDataset(ds, imp);
			}
			else setImagePlusLutToFirstInView(activeView, imp);
		}
		assignImagePlusMinMax(disp, imp);
	}

	// -- private interface --

	/**
	 * For each channel in CompositeImage, sets LUT to one from default
	 * progression
	 */
	private void setCompositeImageLutsToDefault(final CompositeImage ci) {
		for (int i = 0; i < ci.getNChannels(); i++) {
			final ColorTable8 cTable = ColorTables.getDefaultColorTable(i);
			final LUT lut = make8BitLut(cTable);
			ci.setChannelLut(lut, i + 1);
		}
	}

	/**
	 * For each channel in CompositeImage, sets LUT to one from given ColorTables
	 */
	private void setCompositeImageLuts(final CompositeImage ci,
		final List<ColorTable8> cTables)
	{
		if (cTables == null || cTables.size() == 0) {
			setCompositeImageLutsToDefault(ci);
		}
		else {
			for (int i = 0; i < ci.getNChannels(); i++) {
				final ColorTable8 cTable = cTables.get(i);
				final LUT lut = make8BitLut(cTable);
				ci.setChannelLut(lut, i + 1);
			}
		}
	}

	/**
	 * Sets the correct IJ1 CompositeImage display mode based upon input data
	 * values.
	 */
	private void setCompositeImageMode(final CompositeImage ci,
		final int composCount, final List<ColorTable8> cTables)
	{
		if ((composCount > 1) || (cTables == null) || (cTables.size() == 0)) ci
			.setMode(CompositeImage.COMPOSITE);
		else {
			boolean allGrayTables = true;
			for (int i = 0; i < ci.getNChannels(); i++) {
				final ColorTable8 cTable = cTables.get(i);
				if ((allGrayTables) && (!ColorTables.isGrayColorTable(cTable))) allGrayTables =
					false;
			}
			if (allGrayTables) {
				ci.setMode(CompositeImage.GRAYSCALE);
			}
			else {
				ci.setMode(CompositeImage.COLOR);
			}
		}
	}

	/** Sets the single LUT of an ImagePlus to the first ColorTable of a Dataset */
	private void setImagePlusLutToFirstInDataset(final Dataset ds,
		final ImagePlus imp)
	{
		ColorTable8 cTable = ds.getColorTable8(0);
		if (cTable == null) cTable = ColorTables.GRAYS;
		final LUT lut = make8BitLut(cTable);
		imp.getProcessor().setColorModel(lut);
		// or imp.getStack().setColorModel(lut);
	}

	/** Sets the single LUT of an ImagePlus to the first ColorTable of a Dataset */
	private void setImagePlusLutToFirstInView(final DatasetView view,
		final ImagePlus imp)
	{
		ColorTable8 cTable = view.getColorTables().get(0);
		if (cTable == null) cTable = ColorTables.GRAYS;
		final LUT lut = make8BitLut(cTable);
		imp.getProcessor().setColorModel(lut);
		// or imp.getStack().setColorModel(lut);
	}

	/**
	 * Assigns the given ImagePlus's per-channel min/max values to the active view
	 * of the specified ImageDisplay.
	 */
	private void assignImagePlusMinMax(final ImageDisplay disp,
		final ImagePlus imp)
	{
		final DataView dataView = disp.getActiveView();
		if (!(dataView instanceof DatasetView)) return;
		final DatasetView view = (DatasetView) dataView;
		final List<RealLUTConverter<? extends RealType<?>>> converters =
			view.getConverters();
		final int channelCount = converters.size();
		final double[] min = new double[channelCount];
		final double[] max = new double[channelCount];
		double overallMin = Double.POSITIVE_INFINITY;
		double overallMax = Double.NEGATIVE_INFINITY;
		for (int c = 0; c < channelCount; c++) {
			final RealLUTConverter<? extends RealType<?>> conv = converters.get(c);
			min[c] = conv.getMin();
			max[c] = conv.getMax();
			if (min[c] < overallMin) overallMin = min[c];
			if (max[c] > overallMax) overallMax = max[c];
		}

		if (imp instanceof CompositeImage) {
			// set each channel's display range
			final CompositeImage ci = (CompositeImage) imp;
			if (channelCount != ci.getNChannels()) {
				throw new IllegalArgumentException("Channel mismatch: " +
					converters.size() + " vs. " + ci.getNChannels());
			}
			// NB
			//   Originally I used ci.getLUTs() and modified each LUT's min and max.
			//   This cannot work as getLUTs() returns copies rather than originals.
			//   Unfortunately setLUTs() does not use min/max of passed in LUTs. So
			//   can't tweak and set back. So we'll cycle through the channels setting
			//   min/max and make sure channel set where it started when we're done.
			int origC = ci.getC();
			for (int i = 0; i < channelCount; i++) {
				ci.setC(i+1);
				ci.setDisplayRange(min[i], max[i]);
			}
			ci.setC(origC);
		}
		else { // regular ImagePlus
			imp.setDisplayRange(overallMin, overallMax);
		}
	}

	/**
	 * Makes a ColorTable8 from an IndexColorModel. Note that IJ1 LUT's are a kind
	 * of IndexColorModel.
	 */
	private ColorTable8 make8BitColorTable(final IndexColorModel icm) {
		final byte[] reds = new byte[256];
		final byte[] greens = new byte[256];
		final byte[] blues = new byte[256];
		icm.getReds(reds);
		icm.getGreens(greens);
		icm.getBlues(blues);
		return new ColorTable8(reds, greens, blues);
	}

	/** Makes an 8-bit LUT from a ColorTable8. */
	private LUT make8BitLut(final ColorTable8 cTable) {
		final byte[] reds = new byte[256];
		final byte[] greens = new byte[256];
		final byte[] blues = new byte[256];

		for (int i = 0; i < 256; i++) {
			reds[i] = (byte) cTable.get(0, i);
			greens[i] = (byte) cTable.get(1, i);
			blues[i] = (byte) cTable.get(2, i);
		}
		return new LUT(reds, greens, blues);
	}

	/** Assigns the color tables of the active view of a ImageDisplay. */
	private void assignColorTables(final ImageDisplay disp,
		final List<ColorTable<?>> colorTables,
		@SuppressWarnings("unused") final boolean sixteenBitLuts)
	{
		// FIXME HACK
		// Grab the active view of the given ImageDisplay and set it's default
		// channel
		// luts. When we allow multiple views of a Dataset this will break. We
		// avoid setting a Dataset's per plane LUTs because it would be expensive
		// and also IJ1 LUTs are not model space constructs but rather view space
		// constructs.
		final DataView dispView = disp.getActiveView();
		if (dispView == null) return;
		final DatasetView dsView = (DatasetView) dispView;

		// TODO - removing this old code allows color tables to be applied to
		// gray images. Does this break anything? Note that avoiding this code
		// fixes #550, #765, #768, and #774.
		// final ColorMode currMode = dsView.getColorMode();
		// if (currMode == ColorMode.GRAYSCALE) return;

		// either we're given one color table for whole dataset
		if (colorTables.size() == 1) {
			final ColorTable8 newTable = (ColorTable8) colorTables.get(0);
			final List<ColorTable8> existingColorTables = dsView.getColorTables();
			for (int i = 0; i < existingColorTables.size(); i++)
				dsView.setColorTable(newTable, i);
		}
		else { // or we're given one per channel
			/* debugging hacks
			// FIXME - temp debugging hack - this should be removed if it somehow gets
			// into master code. It avoids a race condition between two threads
			// manipulating the color tables
			//try { Thread.sleep(3000); } catch (Exception e) {}
			// FIXME - more debugging hacks - resize color tables to fit
			//int numColorTablesInView = dsView.getColorTables().size();
			//if (colorTables.size() > numColorTablesInView)
			//	dsView.resetColorTables(false); // TODO - when to use "true"?
			*/
			for (int i = 0; i < colorTables.size(); i++)
				dsView.setColorTable((ColorTable8) colorTables.get(i), i);
		}
		// TODO : note Dec 20, 2011 BDZ  See bug #915
		// we should tell the dsView that it needs a redraw (projector.map()) to be
		// done soon. We don't mess with a Dataset here. So a DatasetUpdatedEvent is
		// not generated. And thus no redraw happens. Because of this the last LUT
		// is not applied. For Blobs this means it does not display with a white
		// background. We need to notify the view. Soon the display event updates
		// will be modified and when that happens an update call of some kind needs
		// to go here. Note that a clearly marked blobs workaround is located in
		// Harmonizer that should go away when this issue here is resolved.
	}

	/**
	 * Assigns the per-channel min/max values of active view of given ImageDisplay
	 * to the specified ImagePlus/CompositeImage range(s).
	 */
	private void
		assignChannelMinMax(final ImageDisplay disp, final ImagePlus imp)
	{
		final DataView dataView = disp.getActiveView();
		if (!(dataView instanceof DatasetView)) return;
		final DatasetView view = (DatasetView) dataView;
		final List<RealLUTConverter<? extends RealType<?>>> converters =
			view.getConverters();
		final int channelCount = converters.size();
		final double[] min = new double[channelCount];
		final double[] max = new double[channelCount];

		if (imp instanceof CompositeImage) {
			final CompositeImage ci = (CompositeImage) imp;
			final LUT[] luts = ci.getLuts();
			if (channelCount != luts.length) {
				throw new IllegalArgumentException("Channel mismatch: " +
					converters.size() + " vs. " + luts.length);
			}
			for (int c = 0; c < channelCount; c++) {
				min[c] = luts[c].min;
				max[c] = luts[c].max;
			}
		}
		else {
			final double mn = imp.getDisplayRangeMin();
			final double mx = imp.getDisplayRangeMax();
			for (int c = 0; c < channelCount; c++) {
				min[c] = mn;
				max[c] = mx;
			}
		}

		for (int c = 0; c < channelCount; c++) {
			final RealLUTConverter<? extends RealType<?>> conv = converters.get(c);
			conv.setMin(min[c]);
			conv.setMax(max[c]);
		}
	}

	/** Creates a list of ColorTables from an ImagePlus. */
	private List<ColorTable<?>> colorTablesFromImagePlus(final ImagePlus imp) {
		final List<ColorTable<?>> colorTables = new ArrayList<ColorTable<?>>();
		final LUT[] luts = imp.getLuts();
		if (luts == null) { // not a CompositeImage
			if (imp.getType() == ImagePlus.COLOR_RGB) {
				for (int i = 0; i < imp.getNChannels() * 3; i++) {
					final ColorTable<?> cTable = ColorTables.getDefaultColorTable(i);
					colorTables.add(cTable);
				}
			}
			else { // not a direct color model image
				final IndexColorModel icm =
					(IndexColorModel) imp.getProcessor().getColorModel();
				ColorTable<?> cTable;
//				if (icm.getPixelSize() == 16) // is 16 bit table
//					cTable = make16BitColorTable(icm);
//				else // 8 bit color table
				cTable = make8BitColorTable(icm);
				colorTables.add(cTable);
			}
		}
		else { // we have multiple LUTs from a CompositeImage, 1 per channel
			ColorTable<?> cTable;
			for (int i = 0; i < luts.length; i++) {
//				if (luts[i].getPixelSize() == 16) // is 16 bit table
//					cTable = make16BitColorTable(luts[i]);
//				else // 8 bit color table
				cTable = make8BitColorTable(luts[i]);
				colorTables.add(cTable);
			}
		}

		return colorTables;
	}

}
