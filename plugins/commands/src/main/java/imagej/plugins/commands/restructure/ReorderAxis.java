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

package imagej.plugins.commands.restructure;

import imagej.command.Command;
import imagej.command.DynamicCommand;
import imagej.data.Dataset;
import imagej.data.display.DatasetView;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.menu.MenuConstants;
import imagej.module.DefaultMutableModuleItem;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.display.ColorTable;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.meta.ImgPlus;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import org.scijava.ItemIO;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Rearranges the planes along an axis of a {@link Dataset}. The Dataset is
 * taken from the active {@link DatasetView} of the active {@link ImageDisplay}.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Command.class, initializer = "initAxes", menu = {
	@Menu(label = MenuConstants.IMAGE_LABEL, weight = MenuConstants.IMAGE_WEIGHT,
		mnemonic = MenuConstants.IMAGE_MNEMONIC),
	@Menu(label = "Axes", mnemonic = 'a'), @Menu(label = "Reorder Axis") },
	headless = true)
public class ReorderAxis extends DynamicCommand {

	// -- constants --

	private static final String AXIS = "axis";

	// -- Parameters --

	@Parameter
	private ImageDisplayService imageDisplayService;

	@Parameter(type = ItemIO.BOTH)
	private ImageDisplay display;

	@Parameter(persist = false)
	private String order;

	// -- instance variables --

	private int axisNum = 0;

	private boolean channelsCase = false;

	private ArrayList<String> choices = new ArrayList<String>();

	private Dataset dataset;

	// -- accessors --

	/**
	 * Sets the {@link ImageDisplay} to change.
	 */
	public void setDisplay(ImageDisplay display) {
		this.display = display;
	}

	/**
	 * Gets the {@link ImageDisplay} to be changed.
	 */
	public ImageDisplay getDisplay() {
		return display;
	}

	/**
	 * Sets the axis to be change by referring to its position.
	 */
	public void setAxis(int d) {
		internalSetAxis(d);
	}

	/**
	 * Gets the type of the axis to be changed.
	 */
	public int getAxis() {
		return axisNum;
		/*
		String name = (String) getInput(AXIS);
		for (int i = 0; i < dataset.numDimensions(); i++) {
			AxisType type = dataset.axis(i).type();
			if (type.getLabel().equals(name)) return i;
		}
		return -1;
		*/
	}

	/**
	 * Sets the order of planes along an axis. The format is a comma separated set
	 * of numbers. Each number should appear once and only once. The numbers range
	 * from 0 to DIMENSION-1 for the current axis. For instance given a channel
	 * axis of size 3 a valid order string might be "0,1,2" or "2 , 1, 0" etc.
	 * 
	 * @param order The string representing the desired reordering of the axis.
	 */
	public void setOrder(String order) {
		if (!valid(dataset, order)) {
			throw new IllegalArgumentException(
				"Order string invalid: all axis indices should be separated by commas"
					+ "and specified once and only once");
		}
		this.order = order;
	}

	/**
	 * Sets the current specified ordering of planes along an axis. The format is
	 * a comma separated set of numbers. Each number appears once and only once.
	 * The numbers range from 0 to DIMENSION-1 for the current axis. For instance
	 * given a channel axis of size 3 a valid order string might be "0,1,2" or
	 * "2,1,0" etc.
	 */
	public String getOrder() {
		return order;
	}

	// -- Command methods --

	@Override
	public void run() {
		if (!valid(dataset, order)) {
			cancel("Invalid axis order specification: all axis indices should be "
				+ "separated by commas and specified once and only once");
		}
		ImgPlus<? extends RealType<?>> newData = newData();
		dataset.setImgPlus(newData);
		imageDisplayService.getActiveDatasetView(display).rebuild();
	}

	// -- initializers --

	protected void initAxes() {
		dataset = imageDisplayService.getActiveDataset(display);
		for (int i = 0; i < dataset.numDimensions(); i++) {
			AxisType type = dataset.axis(i).type();
			choices.add(type.getLabel());
		}
		final DefaultMutableModuleItem<String> axisItem =
			new DefaultMutableModuleItem<String>(this, AXIS, String.class);
		axisItem.setChoices(choices);
		axisItem.setPersisted(false);
		internalSetAxis(defaultAxis(dataset));
		axisItem.setValue(this, dataset.axis(axisNum).type().getLabel());
		axisItem.setCallback("axisChanged");
		addInput(axisItem);
		defaultOrderString(dataset, axisNum);
	}

	protected void axisChanged() {
		String axisVal = (String) getInput(AXIS);
		for (int i = 0; i < choices.size(); i++) {
			if (axisVal.equals(choices.get(i))) {
				internalSetAxis(i);
				break;
			}
		}
		defaultOrderString(dataset, axisNum);
	}

	// -- helpers --

	// NB - provide a method that tries to avoid breakage if setAxis(int d) is
	// overridden in a derived class.

	private void internalSetAxis(int d) {
		axisNum = d;
		channelsCase = dataset.axis(axisNum).type().equals(Axes.CHANNEL);
	}

	private int defaultAxis(Dataset ds) {
		// try for first nonspatial
		for (int i = 0; i < ds.numDimensions(); i++) {
			AxisType type = ds.axis(i).type();
			if ((type == Axes.X) || (type == Axes.Y) || (type == Axes.Z)) continue;
			return i;
		}
		// default to first axis
		return 0;
	}

	private boolean valid(Dataset ds, String orderString) {
		String[] terms = orderString.split(",");
		if (terms.length != ds.dimension(axisNum)) return false;
		int[] numbers = new int[terms.length];
		for (int i = 0; i < terms.length; i++) {
			int num;
			try {
				num = Integer.parseInt(terms[i].trim());
			}
			catch (NumberFormatException e) {
				return false;
			}
			if (num <= 0) return false;
			if (num > ds.dimension(axisNum)) return false;
			numbers[i] = num;
		}
		for (int num = 1; num <= numbers.length; num++) {
			if (occurrences(numbers, num) != 1) {
				return false;
			}
		}
		return true;
	}

	private int occurrences(int[] numbers, int num) {
		int count = 0;
		for (int i = 0; i < numbers.length; i++) {
			if (numbers[i] == num) count++;
		}
		return count;
	}

	private void defaultOrderString(Dataset ds, int axis) {
		StringBuilder builder = new StringBuilder();
		for (int i = 1; i <= ds.dimension(axis); i++) {
			if (i != 1) {
				builder.append(", ");
			}
			builder.append(i);
		}
		order = builder.toString();
	}

	private ImgPlus<? extends RealType<?>> newData() {
		DatasetView view = imageDisplayService.getActiveDatasetView(display);
		ColorTable[] origTable = origViewTables(view);
		double[] origMin = origDisplayMins(view);
		double[] origMax = origDisplayMaxes(view);
		Dataset newData = dataset.duplicate();
		int[] positions = newPositions();
		int tableCount = dataset.getColorTableCount();
		for (int i = 0; i < positions.length; i++) {
			copyHypersliceData(axisNum, dataset, positions[i], newData, i);
			if (channelsCase) {
				// set dataset color table
				ColorTable table = dataset.getColorTable(positions[i]);
				if (i < tableCount) newData.setColorTable(table, i);
				// set view color table
				view.setColorTable(origTable[positions[i]], i);
				// set display range
				view.setChannelRange(i, origMin[positions[i]], origMax[positions[i]]);
			}
		}
		return newData.getImgPlus();
	}

	private int[] newPositions() {
		String[] terms = order.split(",");
		int[] numbers = new int[terms.length];
		for (int i = 0; i < terms.length; i++) {
			int num = Integer.parseInt(terms[i].trim());
			numbers[i] = num - 1;
		}
		return numbers;
	}

	private double[] origDisplayMins(DatasetView view) {
		if (channelsCase) {
			int d = dataset.dimensionIndex(Axes.CHANNEL);
			int channels = (int) dataset.dimension(d);
			double[] ranges = new double[channels];
			for (int c = 0; c < channels; c++) {
				ranges[c] = view.getChannelMin(c);
			}
			return ranges;
		}
		return null;
	}

	private ColorTable[] origViewTables(DatasetView view) {
		if (channelsCase) {
			List<ColorTable> origTables = view.getColorTables();
			int d = dataset.dimensionIndex(Axes.CHANNEL);
			int channels = (int) dataset.dimension(d);
			ColorTable[] tables = new ColorTable[channels];
			for (int c = 0; c < channels; c++) {
				tables[c] = origTables.get(c);
			}
			return tables;
		}
		return null;
	}

	private double[] origDisplayMaxes(DatasetView view) {
		if (channelsCase) {
			int d = dataset.dimensionIndex(Axes.CHANNEL);
			int channels = (int) dataset.dimension(d);
			double[] ranges = new double[channels];
			for (int c = 0; c < channels; c++) {
				ranges[c] = view.getChannelMax(c);
			}
			return ranges;
		}
		return null;
	}
	
	private void copyHypersliceData(int axis, Dataset origData, int origPos,
		Dataset newData, int newPos)
	{
		IntervalView<? extends RealType<?>> origHyperSlice =
			Views.hyperSlice(origData.getImgPlus(), axis, origPos);
		IntervalView<? extends RealType<?>> newHyperSlice =
			Views.hyperSlice(newData.getImgPlus(), axis, newPos);
		Cursor<? extends RealType<?>> origCursor =
			Views.iterable(origHyperSlice).localizingCursor();
		RandomAccess<? extends RealType<?>> newAccessor =
			newHyperSlice.randomAccess();
		while (origCursor.hasNext()) {
			double origValue = origCursor.next().getRealDouble();
			newAccessor.setPosition(origCursor);
			newAccessor.get().setReal(origValue);
		}
	}

}
