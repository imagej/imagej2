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

import imagej.data.Data;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.data.display.DataView;
import imagej.data.display.ImageDisplay;
import imagej.data.display.OverlayService;
import imagej.data.overlay.Overlay;
import imagej.ext.display.DisplayService;
import imagej.ext.menu.MenuConstants;
import imagej.ext.module.DefaultModuleItem;
import imagej.ext.module.ItemIO;
import imagej.ext.plugin.DynamicPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.util.RealRect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.imglib2.RandomAccess;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.ops.Tuple2;
import net.imglib2.ops.Tuple3;
import net.imglib2.type.numeric.RealType;

// TODO
//   1) Maintain color tables and metadata
//   2) maintain overlays: does an overlay in Z == 7 show up on correct slice
//      in output data?
//   3) report parse error string somehow
//   4) attach duplicate planes bool at runtime. if its an XY only image it
//      should not get attached. and the harvester shouldn't then show an empty
//      dialog but just run.
//   5) test the contains(num) code works
//   6) eliminate restriction of X and Y axes as being constrained by user
//   7) duplicate a single plane creates Z etc. axis of size 1. Eliminate.
//   8) finish headless user API

/**
 * Duplicates data from one input display to an output display. The planes to
 * be duplicated can be specified via dialog parameters. The XY coordinates of
 * the planes are determined by the current selection bounds.
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
	private DatasetService datasetService;
	
	@Parameter
	private OverlayService overlayService;
	
	@Parameter
	private DisplayService displayService;
	
	@Parameter
	private ImageDisplay inputDisplay;

	@Parameter
	private Dataset inputDataset;

	@Parameter(type = ItemIO.OUTPUT)
	private ImageDisplay outputDisplay;

	@Parameter(label="Duplicate multiple planes")
	private boolean duplicatePlanes;
	
	// -- instance variables that are not parameters --
	
	private AxisType[] nonXyAxes;

	// -- DuplicateImage methods --


	/**
	 * Sets the range of values to copy from the input display for the given axis.
	 * The definition is a textual language that allows one or more planes to be
	 * defined by 1 or more comma separated values. Some examples:
	 * <p>
	 * <ul>
	 * <li>"1" : plane 1</li>
	 * <li>"3,5" : planes 3 and 5</li>
	 * <li>"1-10" : planes 1 through 10</li>
	 * <li>"1-10,20-30" : planes 1 through 10 and 20 through 30</li>
	 * <li>"1-10-2,20-30-3" : planes 1 through 10 by 2 and planes 20 through 30 by 3</li>
	 * <li>"1,3-5,12-60-6" : each type of specification can be combined </li>
	 * </ul>
	 * 
	 * @param axis
	 * @param definition
	 * @return null if successful else a message describing input error
	 */
	public String setAxisRange(AxisType axis, String definition) {
		if (Axes.isXY(axis)) {
			return "X and Y axes cannot be constrained";
		}
		if (inputDisplay.getAxisIndex(axis) < 0) {
			return "Axis "+axis+" not present in input data";
		}
		return null;
	}
	
	// -- RunnablePlugin methods --

	@Override
	public void run() {
		
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
	}

	
	protected void initializer() {
		nonXyAxes = getNonXyAxes(inputDataset);
		for (AxisType axis : nonXyAxes) {
			final DefaultModuleItem<String> axisItem =
				new DefaultModuleItem<String>(this, name(axis), String.class);
			axisItem.setPersisted(false);
			axisItem.setValue(this, fullRangeString(inputDataset, axis));
			addInput(axisItem);
		}
	}

	private String fullRangeString(Dataset ds, AxisType axis) {
		int axisIndex = ds.getAxisIndex(axis);
		return "1-" + ds.dimension(axisIndex);
	}

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
	
	// make sure every field can be parsed (spaces between terms optional)
	// legal entries:
	//  "1"
	//  "1 , 12, 44"
	//  "1-4"
	//  "1-4-2"
	//  "1-4 , 7-10"
	//  "1-10-2,4-22-3"
	private Tuple2<List<List<Long>>,String> parsePlaneIndices() {
		List<List<Long>> indices = new ArrayList<List<Long>>();
		for (int i = 0; i < nonXyAxes.length; i++)
			indices.add(new ArrayList<Long>());
		for (int i = 0; i < nonXyAxes.length; i++) {
			AxisType axis = nonXyAxes[i];
			String fieldVal = (String) getInput(name(axis));
			String err = parseAxisDefinition(i, axis, fieldVal, indices);
			if (err != null) return new Tuple2<List<List<Long>>,String>(null,err);
		}
		return new Tuple2<List<List<Long>>,String>(indices,null);
	}

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
	
	private String name(AxisType axis) {
		return axis.getLabel() + " axis range";
	}
	
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
	
	// TODO - multiple places I'm relying on a Display's axes rather than a
	// Dataset's axes. See if there are problems with this
	
	private static class AxisSubrange {

		private ImageDisplay display;
		private String err;
		private List<Long> indices;
		
		private AxisSubrange(ImageDisplay display) {
			this.err = null;
			this.indices = new ArrayList<Long>();
			this.display = display;
		}
		
		public String getError() { return err; }
		
		public List<Long> getIndices() {
			return Collections.unmodifiableList(indices);
		}

		public static AxisSubrange parse(ImageDisplay display, AxisType axis, String definition) {
			AxisSubrange subrange = new AxisSubrange(display);
			if (!subrange.parseAxisDefinition(axis, definition))
				throw new IllegalArgumentException(subrange.getError());
			return subrange; 
		}
		
		private boolean parseAxisDefinition(AxisType axis, String fieldValue)
		{
			int axisIndex = display.getAxisIndex(axis);
			long maxDim = display.dimension(axisIndex);
			String[] terms = fieldValue.split(",");
			for (int i = 0; i < terms.length; i++) {
				terms[i] = terms[i].trim();
			}
			for (String term : terms) {
				Long num = number(term);
				Tuple2<Long, Long> numDashNum = numberDashNumber(term);
				Tuple3<Long, Long, Long> numDashNumDashNum =
						numberDashNumberDashNumber(term);
				if (num != null) {
					if (num >= 1 && num <= maxDim) {
						if (!indices.contains(num-1)) indices.add(num-1);
					}
				}
				else if (numDashNum != null) {
					long start = numDashNum.get1();
					long end = numDashNum.get2();
					if (start < 1) start = 1;
					if (start > maxDim) start = maxDim;
					if (end < 1) end = 1;
					if (end > maxDim) end = maxDim;
					
					for (long i = start; i <= end; i++) {
						if (!indices.contains(i-1)) indices.add(i-1);
					}
				}
				else if (numDashNumDashNum != null) {
					long start = numDashNumDashNum.get1();
					long end = numDashNumDashNum.get2();
					long by = numDashNumDashNum.get3();
					if (start < 1) start = 1;
					if (start > maxDim) start = maxDim;
					if (end < 1) end = 1;
					if (end > maxDim) end = maxDim;
					for (long i = start; i <= end; i += by) {
						if (!indices.contains(i-1)) indices.add(i-1);
					}
				}
				else {
					err = "Illegal axis subrange definition : "+fieldValue;
					return false;
				}
			}
			Collections.sort(indices);
			return true;
		}

		private Long number(String term) {
			Matcher matcher = Pattern.compile("\\d+").matcher(term);
			if (!matcher.matches()) return null;
			return Long.parseLong(term);
		}
		
		private Tuple2<Long,Long> numberDashNumber(String term) {
			Matcher matcher = Pattern.compile("\\d+-\\d+").matcher(term);
			if (!matcher.matches()) return null;
			String[] values = term.split("-");
			Long start = Long.parseLong(values[0]);
			Long end = Long.parseLong(values[1]);
			if (end < start) return null;
			return new Tuple2<Long,Long>(start, end);
		}
		
		private Tuple3<Long,Long,Long> numberDashNumberDashNumber(String term) {
			Matcher matcher = Pattern.compile("\\d+-\\d+-\\d+").matcher(term);
			if (!matcher.matches()) return null;
			String[] values = term.split("-");
			Long start = Long.parseLong(values[0]);
			Long end = Long.parseLong(values[1]);
			Long by = Long.parseLong(values[2]);
			if (end < start) return null;
			if (by <= 0) return null;
			return new Tuple3<Long,Long,Long>(start, end, by);
		}
		
	}
	
	private static class SamplingDefinition {
		private ImageDisplay display;
		private Map<AxisType,AxisSubrange> axisSubranges;
		private String err;
		
		private SamplingDefinition(ImageDisplay display) {
			this.display = display;
			this.axisSubranges = new HashMap<AxisType,AxisSubrange>();
			this.err = null;
		}
		
		public ImageDisplay getDisplay() { return display; }
		
		public String getError() { return err; }

		// TODO :
		// - make a simple duplicate call in SamplingService
		// - make 0 vs. 1 based axis definition parsing
		// - add simple calls in SamplingService to define snapshots that include
		//     the current selection. Maybe a way to further constrain an axis
		//     definition to fit within the selection
		
		public AxisType[] getInputAxes() {
			return display.getAxes();
		}
		
		public List<List<Long>> getInputRanges() {
			AxisType[] axes = display.getAxes();
			List<List<Long>> axesDefs = new ArrayList<List<Long>>();
			for (AxisType axis : axes) {
				AxisSubrange subrange = axisSubranges.get(axis);
				List<Long> axisValues = subrange.getIndices();
				axesDefs.add(axisValues);
			}
			return Collections.unmodifiableList(axesDefs);
		}
		
		public AxisType[] getOutputAxes() {
			
		}
		
		public long[] getOutputDims() {
			
		}
		
		public static SamplingDefinition sampleUVPlane(
			ImageDisplay display,	AxisType uAxis, AxisType vAxis)
		{
			SamplingDefinition definition = new SamplingDefinition(display);
			Data data = display.getActiveView().getData();
			AxisType[] axes = data.getAxes();
			for (AxisType axis : axes) {
				if ((axis == uAxis) || (axis == vAxis)) {
					int axisIndex = display.getAxisIndex(axis);
					long size = display.getExtents().dimension(axisIndex);
					String axisString = "1-"+size;
					AxisSubrange subrange = AxisSubrange.parse(display, axis, axisString);
					definition.constrain(axis, subrange);
				}
				else { // other axis
					long pos = display.getLongPosition(axis);
					String axisString = ""+pos;
					AxisSubrange subrange = AxisSubrange.parse(display, axis, axisString);
					definition.constrain(axis, subrange);
				}
			}
			return definition;
		}
		
		public static SamplingDefinition sampleXYPlane(ImageDisplay display) {
			return sampleUVPlane(display, Axes.X, Axes.Y);
		}
		
		public static SamplingDefinition sampleCompositeUVPlane(ImageDisplay display, AxisType uAxis, AxisType vAxis) {
			if ((uAxis == Axes.CHANNEL) || (vAxis == Axes.CHANNEL))
				throw new IllegalArgumentException(
					"UV composite plane - cannot specify channels as one of the axes");
			SamplingDefinition definition = new SamplingDefinition(display);
			Data data = display.getActiveView().getData();
			AxisType[] axes = data.getAxes();
			for (AxisType axis : axes) {
				if ((axis == uAxis) || (axis == vAxis) || (axis == Axes.CHANNEL)) {
					int axisIndex = display.getAxisIndex(axis);
					long size = display.getExtents().dimension(axisIndex);
					String axisString = "1-"+size;
					AxisSubrange subrange = AxisSubrange.parse(display, axis, axisString);
					definition.constrain(axis, subrange);
				}
				else { // other axis
					long pos = display.getLongPosition(axis);
					String axisString = ""+pos;
					AxisSubrange subrange = AxisSubrange.parse(display, axis, axisString);
					definition.constrain(axis, subrange);
				}
			}
			return definition;
		}

		public static SamplingDefinition sampleCompositeXYPlane(ImageDisplay display) {
			return sampleCompositeUVPlane(display, Axes.X, Axes.Y);
		}
		
		public static SamplingDefinition sampleAllPlanes(ImageDisplay display) {
			SamplingDefinition definition = new SamplingDefinition(display);
			AxisType[] axes = display.getAxes();
			for (int i = 0; i < axes.length; i++) {
				AxisType axis = axes[i];
				long size = display.dimension(i);
				String axisRangeDef = "1-"+size;
				AxisSubrange subrange = AxisSubrange.parse(display, axis, axisRangeDef);
				definition.constrain(axis, subrange);
			}
			return definition;
		}
		
		public boolean constrain(AxisType axis, AxisSubrange subrange) {
			Data data = display.getActiveView().getData();
			int axisIndex = data.getAxisIndex(axis);
			if (axisIndex < 0) {
				err = "Undefined axis " + axis + " for display " + display.getName();
				return false;
			}
			List<Long> indices = subrange.getIndices();
			if (data.dimension(axisIndex) < indices.get(0)) {
				err = "Axis range fully beyond dimensions of display " + display.getName() + " for axis " + axis;
				return false;
			}
			axisSubranges.put(axis,  subrange);
			return true;
		}
	}

	// TODO - the iterators work with Lists which can only hold 2 gig or fewer
	// elements. Thus data cannot be copied > 2 gig per dimension.
	
	private interface PositionIterator {
		boolean hasNext();
		long[] next();
	}
	
	private class SparsePositionIterator implements PositionIterator {
		private int[] maxIndexes;
		private List<Integer> indexes;
		private List<List<Long>> actualValues;
		private long[] currPos;
		
		public SparsePositionIterator(SamplingDefinition def) {
			actualValues = def.getInputRanges();
			maxIndexes = calcMaxes(def);
			currPos = new long[maxIndexes.length];
			for (int i = 0; i < currPos.length; i++)
				currPos[i] = actualValues.get(i).get(0);
			currPos[0]--;
			indexes = new ArrayList<Integer>();
			int firstValidIndex = 0;
			indexes.add(firstValidIndex-1);
			for (int i = 1; i < maxIndexes.length; i++)
				indexes.add(0);
		}

		@Override
		public boolean hasNext() {
			for (int i = 0; i < currPos.length; i++) {
				if (indexes.get(i) < maxIndexes[i]) return true;
			}
			return false;
		}

		@Override
		public long[] next() {
			for (int i = 0; i < indexes.size(); i++) {
				int nextPos = indexes.get(i) + 1;
				if (nextPos <= maxIndexes[i]) {
					indexes.set(i, nextPos);
					currPos[i] = actualValues.get(i).get(nextPos);
					return currPos;
				}
				indexes.set(i, 0);
				currPos[i] = actualValues.get(i).get(0);
			}
			throw new IllegalArgumentException("Can't position iterator beyond end");
		}
		
		private int[] calcMaxes(SamplingDefinition def) {
			int[] mn = new int[actualValues.size()];
			for (int i = 0; i < mn.length; i++) {
				mn[i] = actualValues.get(i).size() - 1;
			}
			return mn;
		}
	}
	
	private class DensePositionIterator implements PositionIterator {
		private int[] maxIndexes;
		private List<Integer> indexes;
		private long[] currPos;
		
		public DensePositionIterator(SamplingDefinition def) {
			maxIndexes = calcMaxes(def);
			currPos = new long[maxIndexes.length];
			for (int i = 0; i < currPos.length; i++)
				currPos[i] = 0;
			currPos[0]--;
			indexes = new ArrayList<Integer>();
			int firstValidIndex = 0;
			indexes.add(firstValidIndex-1);
			for (int i = 1; i < maxIndexes.length; i++)
				indexes.add(0);
		}

		@Override
		public boolean hasNext() {
			for (int i = 0; i < currPos.length; i++) {
				if (indexes.get(i) < maxIndexes[i]) return true;
			}
			return false;
		}

		@Override
		public long[] next() {
			for (int i = 0; i < indexes.size(); i++) {
				int nextPos = indexes.get(i) + 1;
				if (nextPos <= maxIndexes[i]) {
					indexes.set(i, nextPos);
					currPos[i] = nextPos;
					return currPos;
				}
				indexes.set(i, 0);
				currPos[i] = 0;
			}
			throw new IllegalArgumentException("Can't position iterator beyond end");
		}
		
		private int[] calcMaxes(SamplingDefinition def) {
			long[] dims = def.getOutputDims();
			int[] mn = new int[dims.length];
			for (int i = 0; i < dims.length; i++) {
				if (dims[i] > Integer.MAX_VALUE)
					throw new IllegalArgumentException("Can only iterate <= 2 gig per dimension");
				mn[i] = (int) (dims[i] - 1);
			}
			return mn;
		}
	}
	
	private class SamplingService {

		private final DisplayService displayService;
		private final DatasetService datasetService;
		private final OverlayService overlayService;
		
		public SamplingService(DisplayService dspSrv, DatasetService datSrv, OverlayService ovrSrv) {
			this.displayService = dspSrv;
			this.datasetService = datSrv;
			this.overlayService = ovrSrv;
		}

		// this will create an output display
		// then it will define two iterators and walk them in sync setting values
		// finally it will handle compos cnt, metadata, overlays, colortables, 
		public ImageDisplay createSampledImage(SamplingDefinition def) {
			ImageDisplay outputImage = createOutputImage(def);
			PositionIterator iter1 = new SparsePositionIterator(def);
			PositionIterator iter2 = new DensePositionIterator(def);
			// TODO - remove evil casts
			Dataset input = (Dataset) def.getDisplay().getActiveView().getData();
			Dataset output = (Dataset) outputImage.getActiveView().getData();
			RandomAccess<? extends RealType<?>> inputAccessor =
					input.getImgPlus().randomAccess();
			RandomAccess<? extends RealType<?>> outputAccessor =
					output.getImgPlus().randomAccess();
			while (iter1.hasNext() && iter2.hasNext()) {
				long[] inputPos = iter1.next();
				long[] outputPos = iter2.next();
				inputAccessor.setPosition(inputPos);
				outputAccessor.setPosition(outputPos);
				double value = inputAccessor.get().getRealDouble();
				outputAccessor.get().setReal(value);
			}
			/* TODO
			setCompositeChannelCount();
			setColorTables();
			setOtherMetadata();
			attachOverlays();
			*/
			outputImage.update();
			return outputImage;
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
			// TODO - remove evil cast
			return (ImageDisplay) displayService.createDisplay(name, output);
		}
		
	}
}
