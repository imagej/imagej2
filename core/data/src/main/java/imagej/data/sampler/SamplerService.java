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
import imagej.ext.display.DisplayService;
import imagej.ext.plugin.Plugin;
import imagej.service.AbstractService;
import imagej.service.Service;
import imagej.util.RealRect;

import java.util.List;

import net.imglib2.RandomAccess;
import net.imglib2.display.ColorTable16;
import net.imglib2.display.ColorTable8;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.type.numeric.RealType;


/*
 * Nicer design
 *   External API
 *     constructor
 *       default initialization: set to single plane
 *     setInput()
 *     wantCurrentPlaneOnly()
 *     wantValues(AxisType axis, String definition)
 *     run()
 *     getOutput()
 *   UI
 *     all axes appear
 *     radio buttons
 *       single plane vs. multiple planes
 *       selecting button resets the field value defaults to their original
 *         state. so one plane fills in curr indices. multiple planes defaults
 *         to full ranges
 *   internally
 *     somehow want this to be dynamic and headless. possible?
 *     copy from some multidim point in N-space to different multidim point
 *       in M-space. M is less or equal (in size and/or num dims) to N space.
 */

/**
 * @author Barry DeZonia
 */
@Plugin(type = Service.class)
public class SamplerService extends AbstractService {

	private final DisplayService displayService;
	private final DatasetService datasetService;
	private final OverlayService overlayService;
	private final ImageDisplayService imgDispService;
	
	public SamplerService() {
		// NB: Required by SezPoz.
		super(null);
		throw new UnsupportedOperationException();
	}

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

	// this will create an output display
	// then it will define two iterators and walk them in sync setting values
	// finally it will handle compos cnt, metadata, overlays, colortables, 
	public ImageDisplay createSampledImage(SamplingDefinition def) {
		ImageDisplay outputImage = createOutputImage(def);
		copyData(def, outputImage);
		return outputImage;
	}
	
	public ImageDisplay duplicate(ImageDisplay display) {
		SamplingDefinition copyDef = SamplingDefinition.sampleAllPlanes(display);
		return createSampledImage(copyDef);
	}
	
	public ImageDisplay duplicateSelectedPlane(ImageDisplay display) {
		SamplingDefinition copyDef = SamplingDefinition.sampleXYPlane(display);
		RealRect selection = overlayService.getSelectionBounds(display);
		long minX = (long) selection.x;
		long minY = (long) selection.y;
		long maxX = (long) (selection.x + selection.width);
		long maxY = (long) (selection.y + selection.height);
		AxisSubrange xSubrange = new AxisSubrange(display, minX, maxX);
		AxisSubrange ySubrange = new AxisSubrange(display, minY, maxY);
		copyDef.constrain(Axes.X, xSubrange);
		copyDef.constrain(Axes.Y, ySubrange);
		return createSampledImage(copyDef);
	}


	public ImageDisplay duplicateSelectedCompositePlane(ImageDisplay display) {
		SamplingDefinition copyDef =
				SamplingDefinition.sampleCompositeXYPlane(display);
		RealRect selection = overlayService.getSelectionBounds(display);
		long minX = (long) selection.x;
		long minY = (long) selection.y;
		long maxX = (long) (selection.x + selection.width);
		long maxY = (long) (selection.y + selection.height);
		AxisSubrange xSubrange = new AxisSubrange(display, minX, maxX);
		AxisSubrange ySubrange = new AxisSubrange(display, minY, maxY);
		copyDef.constrain(Axes.X, xSubrange);
		copyDef.constrain(Axes.Y, ySubrange);
		return createSampledImage(copyDef);
	}
	
	public ImageDisplay duplicateSelectedPlanes(ImageDisplay display) {
		SamplingDefinition copyDef = SamplingDefinition.sampleAllPlanes(display);
		RealRect selection = overlayService.getSelectionBounds(display);
		long minX = (long) selection.x;
		long minY = (long) selection.y;
		long maxX = (long) (selection.x + selection.width);
		long maxY = (long) (selection.y + selection.height);
		AxisSubrange xSubrange = new AxisSubrange(display, minX, maxX);
		AxisSubrange ySubrange = new AxisSubrange(display, minY, maxY);
		copyDef.constrain(Axes.X, xSubrange);
		copyDef.constrain(Axes.Y, ySubrange);
		return createSampledImage(copyDef);
	}
	
	private ImageDisplay createOutputImage(SamplingDefinition def) {
		ImageDisplay origDisp = def.getDisplay();
		// TODO - remove evil cast
		Dataset origDs = (Dataset) origDisp.getActiveView().getData();
		long[] dims = def.getOutputDims();
		String name = origDisp.getName();
		AxisType[] axes = def.getOutputAxes();
		int bitsPerPixel = origDs.getType().getBitsPerPixel();
		boolean signed = origDs.isSigned();
		boolean floating = !origDs.isInteger();
		Dataset output =
			datasetService.create(dims, name, axes, bitsPerPixel, signed, floating);
		long numPlanes = 1;
		for (long dim : dims) numPlanes *= dim;
		output.getImgPlus().initializeColorTables((int)numPlanes);
		// TODO - remove evil cast
		return (ImageDisplay) displayService.createDisplay(name, output);
	}

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
		
		/* TODO
		set display ranges from input?
		attachOverlays();
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

	private int planeNum(long[] dims, long[] pos) {
		int plane = 0;
		int inc = 1;
		for (int i = 2; i < dims.length; i++) {
			plane += pos[i] * inc;
			inc *= dims[i];
		}
		return plane;
	}
	
	private void setCompositeChannelCount(Dataset input, Dataset output) {
		if (input.getCompositeChannelCount() == 1) return;
		int index = output.getAxisIndex(Axes.CHANNEL);
		long numChannels = (index < 0) ? 1 : output.dimension(index);
		output.setCompositeChannelCount((int)numChannels);
	}
	
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
		// idea how to copy color tables
		// For now will assume 1 per channel
		DatasetView inView = imgDispService.getActiveDatasetView(input);
		DatasetView outView = imgDispService.getActiveDatasetView(output);
		List<ColorTable8> colorTables = inView.getColorTables();
		for (int c = 0; c < colorTables.size(); c++) {
			ColorTable8 table = colorTables.get(c);
			outView.setColorTable(table, c);
		}
	}
}
