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

package imagej.core.plugins.assign;

import imagej.data.Dataset;
import imagej.data.Position;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.OverlayService;
import imagej.data.overlay.Overlay;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.PreviewPlugin;
import net.imglib2.RandomAccess;
import net.imglib2.meta.Axes;
import net.imglib2.ops.Condition;
import net.imglib2.ops.PointSet;
import net.imglib2.ops.PointSetIterator;
import net.imglib2.ops.UnaryOperation;
import net.imglib2.ops.condition.UVInsideRoiCondition;
import net.imglib2.ops.function.real.PrimitiveDoubleArray;
import net.imglib2.ops.pointset.ConditionalPointSet;
import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

/**
 * Base class for previewable math plugins.
 * 
 * @author Barry DeZonia
 */
public abstract class AbstractPreviewPlugin<T extends RealType<T>>
	implements ImageJPlugin, PreviewPlugin
{
	// -- instance variables that are Parameters --

	@Parameter(persist = false)
	protected ImageDisplay display;

	@Parameter(label = "Preview")
	protected boolean preview;

	@Parameter(label = "Apply to all planes")
	protected boolean allPlanes;

	// -- instance variables --

	private DoubleType tmpVar;
	
	private Dataset dataset;

	private PrimitiveDoubleArray planeBackup;

	private PointSetIterator planeIter;

	private PointSetIterator imageIter;

	private RandomAccess<T> accessor;
	
	private int xAxis, yAxis;
	
	// -- public interface --

	public AbstractPreviewPlugin()
	{
	}

	public abstract UnaryOperation<T,DoubleType> getOperation();

	// TODO - optimize when doing single plane case
	// to not restore/transform unnecessarily

	@Override
	public void run() {
		initialize();
		restoreRegionInPlane();
		if (allPlanes)
			transformRegionAcrossPlanes();
		else
			transformRegionInPlane();
	}

	// TODO - optimize when doing single plane case
	// to not restore/transform unnecessarily

	@Override
	public void preview() {
		initialize();
		restoreRegionInPlane();
		if (preview == true) transformRegionInPlane();
	}

	// TODO - optimize when doing single plane case
	// to not restore/transform unnecessarily

	@Override
	public void cancel() {
		restoreRegionInPlane();
	}

	public ImageDisplay getDisplay() {
		return display;
	}
	
	public void setDisplay(ImageDisplay display) {
		this.display = display;
	}
	
	public boolean isAllPlanes() {
		return allPlanes;
	}

	public void setAllPlanes(boolean value) {
		this.allPlanes = value;
	}
	
	public boolean isPreview() {
		return preview;
	}

	public void setPreview(boolean value) {
		this.preview = value;
	}

	// -- private helpers --

	// NB
	// - It is possible to design an elegant plane cloner but can only assume
	// certain types. And since dataset.setPlane() does not copy data if not
	// PlanarAccess backed it is not helpful to have exact data.
	// - we could make Dataset.copyOfPlane() visible but it assumes things
	// about all types being native types. And again can't set data.
	// - so we opt for copying to an array of doubles. However this could
	// cause precision loss for long data

	private void initialize() {
		if (dataset != null) return;
		ImageDisplayService displayService =
				display.getContext().getService(ImageDisplayService.class);
		dataset = displayService.getActiveDataset(display);

		tmpVar = new DoubleType();
		
		// check dimensions of Dataset
		xAxis = dataset.getAxisIndex(Axes.X);
		yAxis = dataset.getAxisIndex(Axes.Y);
		if ((xAxis < 0) || (yAxis < 0)) throw new IllegalArgumentException(
			"display does not have XY planes");
		
		OverlayService overlayService =
				display.getContext().getService(OverlayService.class);
		Overlay overlay = overlayService.getActiveOverlay(display);
		accessor = (RandomAccess<T>) dataset.getImgPlus().randomAccess();
		PointSet planeRegion = getRegionInPlane(dataset, overlay);
		planeIter = planeRegion.createIterator();
		PointSet imageRegion = getRegionAcrossPlanes(dataset, overlay);
		imageIter = imageRegion.createIterator();
		
		// check size of preview region
		if (planeRegion.calcSize() > Integer.MAX_VALUE)
			throw new IllegalArgumentException(
				"work region too large to hold in memory");
		
		saveRegionInPlane();
	}

	private void saveRegionInPlane() {
		// copy data to a double[]
		planeBackup = new PrimitiveDoubleArray();
		planeBackup.clear();
		planeIter.reset();
		while (planeIter.hasNext()) {
			accessor.setPosition(planeIter.next());
			planeBackup.add(accessor.get().getRealDouble());
		}
	}

	private void restoreRegionInPlane() {
		// restore data from our double[]
		int index = 0;
		planeIter.reset();
		while (planeIter.hasNext()) {
			accessor.setPosition(planeIter.next());
			accessor.get().setReal(planeBackup.get(index++));
		}
		dataset.update();
	}

	private void transformRegionAcrossPlanes() {
		transformData(imageIter);
	}

	private void transformRegionInPlane() {
		transformData(planeIter);
	}

	private void transformData(PointSetIterator iter) {
		// NB - the call to getOperation() must be deferred as late as
		// possible as operation is affected by changing dialog settings.
		// Thats why we do it here at the last possible moment.
		UnaryOperation<T,DoubleType> operation = getOperation();
		iter.reset();
		while (iter.hasNext()) {
			accessor.setPosition(iter.next());
			operation.compute(accessor.get(), tmpVar);
			accessor.get().setReal(tmpVar.getRealDouble());
		}
		dataset.update();
	}

	private PointSet getRegionInPlane(Dataset ds, Overlay overlay) {
		
		final Position planePos = display.getActiveView().getPlanePosition();
		long[] planeMin = new long[ds.numDimensions()];
		long[] planeMax = new long[ds.numDimensions()];
		
		// calc min and max points
		for (int i = 0; i < planeMax.length; i++)
			planeMax[i] = ds.dimension(i) - 1;
		int p = 0;
		for (int i = 0; i < planeMin.length; i++) {
			if ((i == xAxis) || (i == yAxis)) continue;
			long pos = planePos.getLongPosition(p++);
			planeMin[i] = pos;
			planeMax[i] = pos;
		}
		
		// Minimize size of PointSet if an overlay is present
		// This improves performance
		if (overlay != null) {
			planeMin[xAxis] = (long) overlay.realMin(0);
			planeMin[yAxis] = (long) overlay.realMin(1);
			planeMax[xAxis] = (long) overlay.realMax(0);
			planeMax[yAxis] = (long) overlay.realMax(1);
		}
		
		// create encompassing PointSet
		PointSet ps = new HyperVolumePointSet(planeMin, planeMax);
		
		// constrain points within outline of overlay if desired
		if (overlay != null) {
			Condition<long[]> condition =
					new UVInsideRoiCondition(overlay.getRegionOfInterest());
			ps = new ConditionalPointSet(ps, condition);
		}
		
		return ps;
	}
	
	private PointSet getRegionAcrossPlanes(Dataset ds, Overlay overlay) {

		// calc min and max points
		long[] imageMin = new long[ds.numDimensions()];
		long[] imageMax = new long[ds.numDimensions()];
		for (int i = 0; i < imageMax.length; i++)
			imageMax[i] = ds.dimension(i) - 1;

		// Minimize size of PointSet if an overlay is present
		// This improves performance
		if (overlay != null) {
			imageMin[xAxis] = (long) overlay.realMin(0);
			imageMin[yAxis] = (long) overlay.realMin(1);
			imageMax[xAxis] = (long) overlay.realMax(0);
			imageMax[yAxis] = (long) overlay.realMax(1);
		}
		
		// create encompassing PointSet
		PointSet ps = new HyperVolumePointSet(imageMin, imageMax);
		
		// constrain within outline of overlay if desired
		if (overlay != null) {
			Condition<long[]> condition =
					new UVInsideRoiCondition(overlay.getRegionOfInterest());
			ps = new ConditionalPointSet(ps, condition);
		}
		
		return ps;
	}

	
}
