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

package imagej.data.sampler;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.data.display.DatasetView;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.OverlayService;
import imagej.data.overlay.Overlay;
import imagej.ext.display.DisplayService;
import imagej.ext.plugin.Plugin;
import imagej.service.AbstractService;
import imagej.service.Service;
import imagej.util.RealRect;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.RandomAccess;
import net.imglib2.display.ColorTable16;
import net.imglib2.display.ColorTable8;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.type.numeric.RealType;


/**
 * The SamplerService provides methods for duplicating ImageDisplay data.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Service.class)
public class SamplerService extends AbstractService {

	// -- instance variables --
	
	private final DisplayService displayService;
	private final DatasetService datasetService;
	private final OverlayService overlayService;
	private final ImageDisplayService imgDispService;
	
	// -- default constructor that doesn't really do anything --
	
	public SamplerService() {
		// NB: Required by SezPoz.
		super(null);
		throw new UnsupportedOperationException();
	}

	// -- constructor --
	
	/** Creates a SamplerService using references to other services */
	public SamplerService(ImageJ context, DisplayService dspSrv,
		DatasetService datSrv, OverlayService ovrSrv,
		ImageDisplayService imgDispSrv)
	{
		super(context);
		this.displayService = dspSrv;
		this.datasetService = datSrv;
		this.overlayService = ovrSrv;
		this.imgDispService = imgDispSrv;
	}

	// -- public interface --

	/** Creates an output ImageDisplay containing data from an input
	 * SamplingDefinition. This is the most general and custom way to sample
	 * existing image data. The SamplingDefinition class has some static
	 * construction utilities for creating common definitions.
	 *  
	 * @param def The prespecified SamplingDefinition to use
	 * @return The display containing the sampled data
	 */
	public ImageDisplay createSampledImage(SamplingDefinition def) {
		ImageDisplay outputImage = createOutputImage(def);
		copyData(def, outputImage);
		return outputImage;
	}

	/** Creates a copy of an existing ImageDisplay */
	public ImageDisplay duplicate(ImageDisplay display) {
		SamplingDefinition copyDef = SamplingDefinition.sampleAllPlanes(display);
		return createSampledImage(copyDef);
	}

	/** Creates a copy of the currently selected 2d region of an ImageDisplay */
	public ImageDisplay duplicateSelectedPlane(ImageDisplay display) {
		SamplingDefinition copyDef = SamplingDefinition.sampleXYPlane(display);
		RealRect selection = overlayService.getSelectionBounds(display);
		long minX = (long) selection.x;
		long minY = (long) selection.y;
		long maxX = (long) (selection.x + selection.width);
		long maxY = (long) (selection.y + selection.height);
		AxisSubrange xSubrange = new AxisSubrange(minX, maxX);
		AxisSubrange ySubrange = new AxisSubrange(minY, maxY);
		copyDef.constrain(Axes.X, xSubrange);
		copyDef.constrain(Axes.Y, ySubrange);
		return createSampledImage(copyDef);
	}


	/** Creates a multichannel copy of the currently selected 2d region of an
	 *  ImageDisplay */
	public ImageDisplay duplicateSelectedCompositePlane(ImageDisplay display) {
		SamplingDefinition copyDef =
				SamplingDefinition.sampleCompositeXYPlane(display);
		RealRect selection = overlayService.getSelectionBounds(display);
		long minX = (long) selection.x;
		long minY = (long) selection.y;
		long maxX = (long) (selection.x + selection.width);
		long maxY = (long) (selection.y + selection.height);
		AxisSubrange xSubrange = new AxisSubrange(minX, maxX);
		AxisSubrange ySubrange = new AxisSubrange(minY, maxY);
		copyDef.constrain(Axes.X, xSubrange);
		copyDef.constrain(Axes.Y, ySubrange);
		return createSampledImage(copyDef);
	}
	
	/** Creates a copy of all the planes bounded by the currently selected 2d
	 *  region of an ImageDisplay */
	public ImageDisplay duplicateSelectedPlanes(ImageDisplay display) {
		SamplingDefinition copyDef = SamplingDefinition.sampleAllPlanes(display);
		RealRect selection = overlayService.getSelectionBounds(display);
		long minX = (long) selection.x;
		long minY = (long) selection.y;
		long maxX = (long) (selection.x + selection.width);
		long maxY = (long) (selection.y + selection.height);
		AxisSubrange xSubrange = new AxisSubrange(minX, maxX);
		AxisSubrange ySubrange = new AxisSubrange(minY, maxY);
		copyDef.constrain(Axes.X, xSubrange);
		copyDef.constrain(Axes.Y, ySubrange);
		return createSampledImage(copyDef);
	}
	
	// -- private helpers --
	
	/** Creates an output image from a sampling definition. All data initialized
	 * to zero.
	 */
	private ImageDisplay createOutputImage(SamplingDefinition def) {
		ImageDisplay origDisp = def.getDisplay();
		// TODO - remove evil cast
		Dataset origDs = (Dataset) origDisp.getActiveView().getData();
		long[] dims = def.getOutputDims();
		String name = origDisp.getName();
		AxisType[] axes = def.getOutputAxes();
		double[] cal = def.getOutputCalibration(axes);
		int bitsPerPixel = origDs.getType().getBitsPerPixel();
		boolean signed = origDs.isSigned();
		boolean floating = !origDs.isInteger();
		Dataset output =
			datasetService.create(dims, name, axes, bitsPerPixel, signed, floating);
		output.setCalibration(cal);
		long numPlanes = 1;
		for (long dim : dims) numPlanes *= dim;
		output.getImgPlus().initializeColorTables((int)numPlanes);
		if (origDs.isRGBMerged()) {
			int chanAxis = output.getAxisIndex(Axes.CHANNEL);
			if (chanAxis >= 0)
				if (output.dimension(chanAxis) == 3)
					output.setRGBMerged(true);
		}
		// TODO - remove evil cast
		return (ImageDisplay) displayService.createDisplay(name, output);
	}


	/** Copies all associated data from a SamplingDefinition to an output image */
	private void copyData(SamplingDefinition def, ImageDisplay outputImage) {
		PositionIterator iter1 = new SparsePositionIterator(def);
		PositionIterator iter2 = new DensePositionIterator(def);
		// TODO - remove evil casts
		Dataset input = (Dataset) def.getDisplay().getActiveView().getData();
		Dataset output = (Dataset) outputImage.getActiveView().getData();
		long[] inputDims = input.getDims();
		long[] outputDims = output.getDims();
		RandomAccess<? extends RealType<?>> inputAccessor =
				input.getImgPlus().randomAccess();
		RandomAccess<? extends RealType<?>> outputAccessor =
				output.getImgPlus().randomAccess();
		while (iter1.hasNext() && iter2.hasNext()) {

			// determine data positions within datasets
			long[] inputPos = iter1.next();
			long[] outputPos = iter2.next();
			inputAccessor.setPosition(inputPos);
			outputAccessor.setPosition(outputPos);
			
			// copy value
			double value = inputAccessor.get().getRealDouble();
			outputAccessor.get().setReal(value);
			
			// keep dataset color tables in sync
			int inputPlaneNumber = planeNum(inputDims, inputPos);
			ColorTable8 lut8 = input.getColorTable8(inputPlaneNumber);
			ColorTable16 lut16 = input.getColorTable16(inputPlaneNumber);
			int outputPlaneNumber = planeNum(outputDims, outputPos);
			output.setColorTable(lut8, outputPlaneNumber);
			output.setColorTable(lut16, outputPlaneNumber);
		}
		
		// keep composite status in sync
		setCompositeChannelCount(input, output);
		
		// keep display color tables in sync
		updateDisplayColorTables(def.getDisplay(), outputImage);

		// TODO - enable this code
		//List<Overlay> overlays = overlayService.getOverlays(def.getDisplay());
		//attachOverlays(def.getDisplay(), outputImage, overlays);
		
		/* TODO
		set display ranges from input?
		setOtherMetadata();
		*/
		
	  // code elsewhere needs to know composite channel count may have changed
		output.rebuild(); // removing has no effect on organ of corti not appearing
		// Alternative that doesn't really work either
		//evtService.publish/publishLater(new DatasetRestructuredEvent(output));
		
		// TODO
		// I can get color tables and compos chan count working. But with Organ of
		// Corti nothing appears after running. However when running from within
		// the debugger and no breakpoints set it works. A timing bug is apparent.
		// Great.
	}

	/** Calculates a plane number from a position within a dimensionsal space. */
	private int planeNum(long[] dims, long[] pos) {
		int plane = 0;
		int inc = 1;
		for (int i = 2; i < dims.length; i++) {
			plane += pos[i] * inc;
			inc *= dims[i];
		}
		return plane;
	}
	
	/** Sets an output Dataset's composite channel count based upon an input
	 * Dataset's composite channel characteristics.  
	 */
	private void setCompositeChannelCount(Dataset input, Dataset output) {
		if (input.getCompositeChannelCount() == 1) return;
		int index = output.getAxisIndex(Axes.CHANNEL);
		long numChannels = (index < 0) ? 1 : output.dimension(index);
		output.setCompositeChannelCount((int)numChannels);
	}

	/** Copies the appropriate color tables from the input ImageDisplay to the
	 * output ImageDiosplay. 
	 */
	private void updateDisplayColorTables(ImageDisplay input, ImageDisplay output){
		int chAxisIn = input.getAxisIndex(Axes.CHANNEL);
		int chAxisOut = output.getAxisIndex(Axes.CHANNEL);
		if (chAxisIn < 0 || chAxisOut < 0) {
			return;
		}
		if (input.dimension(chAxisIn) != output.dimension(chAxisOut))
		{
			return;
		}

		// otherwise if here the two displays have channel axes of the same size
		
		// TODO - cannot assume 1 color table per channel, right? if so then no
		// idea how to copy color tables. For now will assume 1 per channel
		DatasetView inView = imgDispService.getActiveDatasetView(input);
		DatasetView outView = imgDispService.getActiveDatasetView(output);
		List<ColorTable8> colorTables = inView.getColorTables();
		for (int c = 0; c < colorTables.size(); c++) {
			ColorTable8 table = colorTables.get(c);
			outView.setColorTable(table, c);
		}
	}

	private void attachOverlays(ImageDisplay inputDisp, ImageDisplay outputDisp, List<Overlay> overlays) {
		RealRect bounds = overlayService.getSelectionBounds(inputDisp);
		double[] toOrigin = new double[2];
		toOrigin[0] = -bounds.x;
		toOrigin[1] = -bounds.y;
		List<Overlay> newOverlays = new ArrayList<Overlay>();
		for (Overlay overlay : overlays) {
			if (overlayWithinBounds(overlay, bounds)) {
				// add a reference to existing overlay?
				if (toOrigin[0] == 0 && toOrigin[1] == 0) {
					newOverlays.add(overlay);
				}
				else { // different origins means must create new overlays
					Overlay newOverlay = overlay.duplicate();
					newOverlay.move(toOrigin);
					newOverlays.add(newOverlay);
				}
			}
		}
		overlayService.addOverlays(outputDisp, newOverlays);
	}
	
	private boolean overlayWithinBounds(Overlay overlay, RealRect bounds) {
		if (overlay.min(0) < bounds.x) return false;
		if (overlay.min(1) < bounds.y) return false;
		if (overlay.max(0) > bounds.x + bounds.width) return false;
		if (overlay.max(1) > bounds.y + bounds.height) return false;
		return true;
	}
	
}
