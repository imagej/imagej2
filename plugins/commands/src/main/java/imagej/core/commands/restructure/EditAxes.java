/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

package imagej.core.commands.restructure;

import imagej.command.Command;
import imagej.command.DynamicCommand;
import imagej.data.Dataset;
import imagej.menu.MenuConstants;
import imagej.module.DefaultMutableModuleItem;

import java.util.ArrayList;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;

import org.scijava.ItemIO;
import org.scijava.log.LogService;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

// TODO
// - code elsewhere assumes X and Y always present. this plugin can break that
//   assumption. This could be useful in future but might need to block now.

// TODO: add callbacks as appropriate to keep input valid

/**
 * Changes the values of the axes. For example they can go from [x,y,z] to
 * [c,t,frequency]. This is a convenience plugin that allows axis types to be
 * reassigned. Useful if imported data has the wrong axis designations. Pixel
 * data is NOT rearranged. Calibration values are maintained where possible.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Command.class, menu = {
	@Menu(label = MenuConstants.IMAGE_LABEL, weight = MenuConstants.IMAGE_WEIGHT,
		mnemonic = MenuConstants.IMAGE_MNEMONIC),
	@Menu(label = "Axes", mnemonic = 'a'), @Menu(label = "Edit Axes...") },
	headless = true, initializer = "initAxes")
public class EditAxes extends DynamicCommand {

	// -- Parameters --

	@Parameter
	private LogService log;

	@Parameter(type = ItemIO.BOTH)
	private Dataset dataset;

	// -- AssignAxes methods --

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(final Dataset dataset) {
		this.dataset = dataset;
	}

	public AxisType getAxisMapping(int axisNum) {
		String axisName = (String) getInput(name(axisNum));
		return Axes.get(axisName);
	}
	
	public void setAxisMapping(int axisNum, AxisType axis) {
		String axisName = name(axisNum);
		setInput(axisName, axis.getLabel());
	}
	
	// -- Runnable methods --

	/** Runs the plugin and assigns axes as specified by user. */
	@Override
	public void run() {
		if (dataset == null) {
			log.error("EditAxes plugin error: given a null dataset as input");
		}
		AxisType[] desiredAxes = getAxes();
		if (inputBad(desiredAxes)) {
			// error already logged
			return;
		}
		double[] newCal = getNewCalibration(dataset,desiredAxes);
		dataset.setCalibration(newCal);
		dataset.setAxes(desiredAxes);
	}

	// -- Initializers --

	protected void initAxes() {
		ArrayList<String> choices = new ArrayList<String>();
		AxisType[] axes = Axes.values();
		for (AxisType axis : axes) {
			choices.add(axis.getLabel());
		}
		for (int i = 0; i < dataset.numDimensions(); i++) {
			final DefaultMutableModuleItem<String> axisItem =
				new DefaultMutableModuleItem<String>(this, name(i), String.class);
			axisItem.setChoices(choices);
			axisItem.setPersisted(false);
			axisItem.setValue(this, dataset.axis(i).getLabel());
			addInput(axisItem);
		}
	}

	// -- Helper methods --

	private String name(final int i) {
		return "Axis #" + i;
	}

	/**
	 * Gets the names of the axes in the order the user specified.
	 */
	private AxisType[] getAxes() {
		AxisType[] axes = new AxisType[dataset.getImgPlus().numDimensions()];
		for (int i = 0; i < axes.length; i++) {
			axes[i] = getAxisMapping(i);
		}
		return axes;
	}

	/**
	 * Returns true if user input is invalid. Basically this is a test that the
	 * user did not repeat any axis when specifying the axis ordering.
	 */
	private boolean inputBad(AxisType[] axes) {
		for (int i = 0; i < axes.length; i++) {
			for (int j = i+1; j < axes.length; j++) {
				if (axes[i].equals(axes[j])) {
					log.error("At least one axis designation is repeated:"
							+ " axis designations must be mututally exclusive");
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Copies already existing calibration values into new calibration when
	 * possible. If new axis is not present in original Dataset sets that
	 * calibration value to NaN.
	 */
	double[] getNewCalibration(Dataset origDs, AxisType[] newAxes) {
		double[] newCal = new double[newAxes.length];
		int a = 0;
		for (AxisType axis : newAxes) {
			int index = origDs.getAxisIndex(axis);
			if (index < 0)
				newCal[a++] = Double.NaN;
			else
				newCal[a++] = origDs.calibration(index);
		}
		return newCal;
	}
}
