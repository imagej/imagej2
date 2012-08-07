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

package imagej.core.plugins.imglib;

import imagej.data.display.ImageDisplay;
import imagej.data.sampler.AxisSubrange;
import imagej.data.sampler.SamplerService;
import imagej.data.sampler.SamplingDefinition;
import imagej.ext.menu.MenuConstants;
import imagej.ext.module.DefaultModuleItem;
import imagej.ext.module.ItemIO;
import imagej.ext.plugin.DynamicPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

import net.imglib2.meta.AxisType;

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

// TODO
//   1) Maintain metadata
//   2) maintain overlays: does an overlay in Z == 7 show up on correct slice
//      in output data?
//   3) report parse error string somehow
//   5) test the contains(num) code works
// TODO - multiple places I'm relying on a Display's axes rather than a
// Dataset's axes. See if there are problems with this
// TODO - the iterators work with Lists which can only hold 2 gig or fewer
// elements. Thus data cannot be copied > 2 gig per dimension.
// TODO:
// -- for speed eliminate reliance on Longs. Make primitive arrays.
// -- make a EnormousList that can store more than 2 gig of longs
// In other plugins replace RestructureUtils calls with methods from here
// TODO display ranges are all messed up

/**
 * Duplicates data from one input display to an output display. The planes to
 * be duplicated can be specified via dialog parameters. The XY coordinates can
 * be further constrained to be a subset of the current selection bounds.
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = MenuConstants.IMAGE_LABEL, weight = MenuConstants.IMAGE_WEIGHT,
		mnemonic = MenuConstants.IMAGE_MNEMONIC),
	@Menu(label = "Duplicate", accelerator = "shift control D") },
	headless = true, initializer = "initializer")
public class DuplicateImage extends DynamicPlugin {

	// -- Plugin parameters --

	@Parameter
	private SamplerService samplerService;

	@Parameter
	private ImageDisplay inputDisplay;

	@Parameter(type = ItemIO.OUTPUT)
	private ImageDisplay outputDisplay;

	@Parameter(label = "Constrain axes as below:")
	private boolean specialBehavior = false;
	
	// -- instance variables that are not parameters --

	private Map<AxisType,AxisSubrange> definitions;
	private AxisType[] theAxes;

	// -- DuplicateImage methods --

	/**
	 * Specifies whether to use default behavior or special behavior. Default
	 * behavior copies the current composite XY plane. Special behavior is set
	 * when user defined axis definitions are to follow. If no axis definitions
	 * follow then all planes within the selected region will be copied.
	 */
	public void setDefaultBehavior(boolean value) {
		specialBehavior = !value;
	}

	/**
	 * Returns ture if current behavior is default. Otherwise current behavior is
	 * special. Default behavior copies the current composite XY plane. When
	 * special behavior is set then user defined axis definitions may have been
	 * specifed and if so those definitions are used during copying. Otherwise if
	 * no user defined axis definitions were specified then all planes within the
	 * selected region will be copied.
	 */
	public boolean isDefaultBehavior() { return !specialBehavior; }

	/**
	 * Sets the the input image to be sampled.
	 */
	public void setInputDisplay(ImageDisplay disp) {
		inputDisplay = disp;
	}
	
	/**
	 * Returns the the input image to be sampled.
	 */
	public ImageDisplay getInputDisplay() { return inputDisplay; }

	/**
	 * The output image resulting after executing the run() method. This method
	 * returns null if the run() method has not yet been called. 
	 */
	public ImageDisplay getOutputDisplay() { return outputDisplay; }

	/**
	 * Sets the range of values to copy from the input display for the given axis.
	 * The definition is a textual language that allows one or more planes to be
	 * defined by one or more comma separated values. Some examples:
	 * <p>
	 * <ul>
	 * <li>"1" : plane 1</li>
	 * <li>"3,5" : planes 3 and 5</li>
	 * <li>"1-10" : planes 1 through 10</li>
	 * <li>"1-10,20-30" : planes 1 through 10 and 20 through 30</li>
	 * <li>"1-10-2,20-30-3" : planes 1 through 10 by 2 and planes 20 through 30 by 3</li>
	 * <li>"1,3-5,12-60-6" : this shows each type of specification can be combined </li>
	 * </ul>
	 * 
	 * @param axis
	 * @param axisDefinition
	 * @param originIsOne
	 * @return null if successful else a message describing input error
	 */
	public String setAxisRange(AxisType axis, String axisDefinition, boolean originIsOne) {
		specialBehavior = true;
		AxisSubrange subrange = new AxisSubrange(inputDisplay, axis, axisDefinition, originIsOne);
		if (subrange.getError() == null) {
			definitions.put(axis, subrange);
		}
		return subrange.getError();
	}
	
	// -- RunnablePlugin methods --

	@Override
	public void run() {
		if (specialBehavior) {
			SamplingDefinition samples = determineSamples();
			outputDisplay = samplerService.createSampledImage(samples);
		}
		else { // snapshot the existing composite selection
			outputDisplay = samplerService.duplicateSelectedCompositePlane(inputDisplay);
		}
		/*
		final List<List<Long>> planeIndices;
		
		// user has specified some ranges for the axes
		if (duplicatePlanes) {
			Tuple2<List<List<Long>>,String> result = parsePlaneIndices();
			if (result.get2() != null) {
				cancel();
				return;
			}
			planeIndices = result.get1();
		}
		else { // user only wants the current view duplicated
			planeIndices = calcPlaneIndices(inputDisplay,inputDataset);
		}
		RealRect xyPlaneInfo = getXySelection();
		long dx = (long) xyPlaneInfo.x;
		long dy = (long) xyPlaneInfo.y;
		long w = (long) xyPlaneInfo.width;
		long h = (long) xyPlaneInfo.height;
		Dataset newDataset = 
				makeOutputDataset(inputDataset, w, h, nonXyAxes, planeIndices);
		fillOutputDataset(inputDataset, planeIndices, dx, dy, w, h, newDataset);
		if (inputDataset.getCompositeChannelCount() > 1) {
			int chIndex = newDataset.getAxisIndex(Axes.CHANNEL);
			long count = (chIndex < 0) ? 1 : newDataset.dimension(chIndex);
			newDataset.setCompositeChannelCount((int)count);
		}
		outputDisplay = createDisplay(newDataset);
		attachOverlays(outputDisplay, overlayService.getOverlays(inputDisplay));
		*/
	}

	
	protected void initializer() {
		definitions = new HashMap<AxisType, AxisSubrange>();
		theAxes = inputDisplay.getAxes();
		for (AxisType axis : theAxes) {
			final DefaultModuleItem<String> axisItem =
				new DefaultModuleItem<String>(this, name(axis), String.class);
			axisItem.setPersisted(false);
			axisItem.setValue(this, fullRangeString(inputDisplay, axis));
			addInput(axisItem);
		}
	}

	private String fullRangeString(ImageDisplay disp, AxisType axis) {
		int axisIndex = disp.getAxisIndex(axis);
		return "1-" + disp.dimension(axisIndex);
	}

	/** takes definitions and applies them */
	private SamplingDefinition determineSamples() {
		if (definitions.size() > 0) {
			SamplingDefinition def = SamplingDefinition.sampleAllPlanes(inputDisplay);
			for (AxisType axis : definitions.keySet()) {
				def.constrain(axis, definitions.get(axis));
			}
			return def;
		}
		return parsedDefinition();
	}

	private SamplingDefinition parsedDefinition() {
		SamplingDefinition sampleDef = SamplingDefinition.sampleAllPlanes(inputDisplay);
		for (AxisType axis : theAxes) {
			String definition = (String) getInput(name(axis));
			AxisSubrange subrange = new AxisSubrange(inputDisplay, axis, definition, true);
			if (subrange.getError() != null)
				return SamplingDefinition.sampleAllPlanes(inputDisplay);
			sampleDef.constrain(axis, subrange);
		}
		return sampleDef;
	}
	


	private String name(AxisType axis) {
		return axis.getLabel() + " axis range";
	}
	
	/*
	private List<List<Long>> calcPlaneIndices(ImageDisplay display, Dataset dataset) {
		List<List<Long>> indices = new ArrayList<List<Long>>();
		for (int i = 0; i < nonXyAxes.length; i++) {
			AxisType axis = nonXyAxes[i];
			indices.add(new ArrayList<Long>());
			// channel axis? if so include all of them
			if (axis == Axes.CHANNEL) {
				int index = dataset.getAxisIndex(Axes.CHANNEL);
				long val = dataset.dimension(index);
				for (long c = 0; c < val; c++) {
					indices.get(i).add(c);
				}
			}
			else { // not channel axis : include current position
				long val = display.getIntPosition(axis);
				indices.get(i).add(val);
			}
		}
		return indices;
	}
	*/
	
	// make sure every field can be parsed (spaces between terms optional)
	// legal entries:
	//  "1"
	//  "1 , 12, 44"
	//  "1-4"
	//  "1-4-2"
	//  "1-4 , 7-10"
	//  "1-10-2,4-22-3"
	/*
	private Tuple2<List<List<Long>>,String> parsePlaneIndices() {
		List<List<Long>> indices = new ArrayList<List<Long>>();
		for (int i = 0; i < nonXyAxes.length; i++)
			indices.add(new ArrayList<Long>());
		for (int i = 0; i < nonXyAxes.length; i++) {
			AxisType axis = nonXyAxes[i];
			String fieldVal = (String) getInput(name(axis));
			String err = null; //parseAxisDefinition(i, axis, fieldVal, indices);
			if (err != null) return new Tuple2<List<List<Long>>,String>(null,err);
		}
		return new Tuple2<List<List<Long>>,String>(indices,null);
	}
	*/
	
	/*
	private AxisType[] getNonXyAxes(Dataset ds) {
		AxisType[] axes = ds.getAxes();
		List<AxisType> nonXy = new ArrayList<AxisType>();
		for (AxisType axis : axes) {
			if (Axes.isXY(axis)) continue;
			nonXy.add(axis);
		}
		AxisType[] outputAxes = new AxisType[nonXy.size()];
		for (int i = 0; i < nonXy.size(); i++)
			outputAxes[i] = nonXy.get(i);
		return outputAxes;
	}
	
	private Dataset makeOutputDataset(
		Dataset srcDataset, long x, long y, AxisType[] axes,
		List<List<Long>> planeIndices)
	{
		long[] newDims = findDims(x, y, planeIndices);
		AxisType[] newAxes = findAxes(axes);
		String name = srcDataset.getName();
		int bpp = srcDataset.getType().getBitsPerPixel();
		boolean signed = srcDataset.isSigned();
		boolean floating = !srcDataset.isInteger();
		return datasetService.create(newDims, name, newAxes, bpp, signed, floating);
	}

	private long[] findDims(long xDim, long yDim, List<List<Long>> planeIndices) {
		long[] newDims = new long[2 + planeIndices.size()];
		int xAxisIndex = inputDataset.getAxisIndex(Axes.X);
		int yAxisIndex = inputDataset.getAxisIndex(Axes.Y);
		int p = 0;
		for (int i = 0; i < newDims.length; i++) {
			if (i == xAxisIndex) newDims[i] = xDim;
			else if (i == yAxisIndex) newDims[i] = yDim;
			else newDims[i] = planeIndices.get(p++).size();
		}
		return newDims;
	}

	private AxisType[] findAxes(AxisType[] axes) {
		AxisType[] newAxes = new AxisType[2 + axes.length];
		int xAxisIndex = inputDataset.getAxisIndex(Axes.X);
		int yAxisIndex = inputDataset.getAxisIndex(Axes.Y);
		int p = 0;
		for (int i = 0; i < newAxes.length; i++) {
			if (i == xAxisIndex) newAxes[i] = Axes.X;
			else if (i == yAxisIndex) newAxes[i] = Axes.Y;
			else newAxes[i] = axes[p++];
		}
		return newAxes;
	}
	
	private void fillOutputDataset(
		Dataset srcDataset, List<List<Long>> planeIndices, long xOffs, long yOffs,
		long maxX, long maxY, Dataset dstDataset)
	{
		int xAxis = srcDataset.getAxisIndex(Axes.X);
		int yAxis = srcDataset.getAxisIndex(Axes.Y);
		RandomAccess<? extends RealType<?>> srcAccessor =
				srcDataset.getImgPlus().randomAccess();
		RandomAccess<? extends RealType<?>> dstAccessor =
				dstDataset.getImgPlus().randomAccess();
		DualPlaneIterator iter = new DualPlaneIterator(srcDataset, planeIndices);
		while (iter.hasNext()) {
			iter.next();
			long[] srcPos = iter.srcPos();
			long[] dstPos = iter.dstPos();
			for (int x = 0; x < maxX; x++) {
				srcPos[xAxis] = xOffs + x;
				dstPos[xAxis] = x;
				for (int y = 0; y < maxY; y++) {
					srcPos[yAxis] = yOffs + y;
					dstPos[yAxis] = y;
					srcAccessor.setPosition(srcPos);
					dstAccessor.setPosition(dstPos);
					double value = srcAccessor.get().getRealDouble();
					dstAccessor.get().setReal(value);
				}
			}
		}
	}

	*/

	/*
	private class DualPlaneIterator {

		private long[] srcPos;
		private long[] dstPos;
		private List<Integer> positions;
		private List<List<Long>> indices;
		private int xAxis;
		private int yAxis;
		private boolean singlePlaneNextStatus;
		
		public DualPlaneIterator(Dataset ds, List<List<Long>> indices) {
			this.srcPos = new long[ds.numDimensions()];
			this.dstPos = new long[ds.numDimensions()];
			this.positions = initForStart(indices);
			this.indices = indices;
			this.xAxis = ds.getAxisIndex(Axes.X);
			this.yAxis = ds.getAxisIndex(Axes.Y);
			this.singlePlaneNextStatus = true;
		}
		
		public boolean hasNext() {
			if (positions.size() == 0) return singlePlaneNextStatus;
			for (int i = 0; i < positions.size(); i++) {
				if (positions.get(i) < indices.get(i).size()-1) return true;
			}
			return false;
		}
		
		public void next() {
			// are there no dims beyond X & Y?
			if (srcPos.length == 2) {
				singlePlaneNextStatus = false;
				setSrcPosVals();
				setDstPosVals();
				return;
			}
			// else we have some other dims
			for (int i = 0; i < positions.size(); i++) {
				int currIndex = positions.get(i);
				currIndex++;
				if (currIndex < indices.get(i).size()) {
					positions.set(i, currIndex);
					setSrcPosVals();
					setDstPosVals();
					return;
				}
				positions.set(i, 0);
			}
			throw new IllegalArgumentException("Cannot run next() beyond end of data");
		}
		
		public long[] srcPos() { return srcPos; }
		
		public long[] dstPos() { return dstPos; }
		
		private List<Integer> initForStart(List<List<Long>> planeIndices) {
			List<Integer> index = new ArrayList<Integer>();
			for (int i = 0; i < planeIndices.size(); i++) {
				index.add(0);
			}
			if (index.size() > 0) index.set(0, -1);
			return index;
		}
		
		private void setSrcPosVals() {
			int p = 0;
			for (int i = 0; i < srcPos.length; i++) {
				if (i == xAxis) continue;
				if (i == yAxis) continue;
				int pos = positions.get(p);
				srcPos[i] = indices.get(p).get(pos);
				p++;
			}
		}

		private void setDstPosVals() {
			int p = 0;
			for (int i = 0; i < dstPos.length; i++) {
				if (i == xAxis) continue;
				if (i == yAxis) continue;
				dstPos[i] = positions.get(p++);
			}
		}
	}
	
	private void attachOverlays(ImageDisplay outputDisp, List<Overlay> overlays) {
		RealRect bounds = getXySelection();
		double[] toOrigin = new double[2];
		toOrigin[0] = -bounds.x;
		toOrigin[1] = -bounds.y;
		List<Overlay> newOverlays = new ArrayList<Overlay>();
		for (Overlay overlay : overlays) {
			if (overlayContained(overlay, bounds)) {
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
	
	private ImageDisplay createDisplay(Dataset ds) {
		return (ImageDisplay)	displayService.createDisplay(ds.getName(), ds);
	}

	private RealRect getXySelection() {
		RealRect bounds = overlayService.getSelectionBounds(inputDisplay);
		bounds.x = (long) bounds.x;
		bounds.y = (long) bounds.y;
		// NB : real rect works in doubles. Our space is integral. Thus there is an
		// off by one issue in the width and height calcs.
		bounds.width = (long) bounds.width + 1;
		bounds.height = (long) bounds.height + 1;
		return bounds;
	}
	
	private boolean overlayContained(Overlay overlay, RealRect bounds) {
		if (overlay.min(0) < bounds.x) return false;
		if (overlay.min(1) < bounds.y) return false;
		if (overlay.max(0) > bounds.x + bounds.width) return false;
		if (overlay.max(1) > bounds.y + bounds.height) return false;
		return true;
	}
	
	*/
}
