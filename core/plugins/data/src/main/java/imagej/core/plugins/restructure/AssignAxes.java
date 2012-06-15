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

package imagej.core.plugins.restructure;

import imagej.data.Dataset;
import imagej.ext.menu.MenuConstants;
import imagej.ext.module.DefaultModuleItem;
import imagej.ext.module.ItemIO;
import imagej.ext.plugin.DynamicPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.util.Log;

import java.util.ArrayList;
import java.util.Map;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;

// TODO
// - code elsewhere assumes X and Y always present. this plugin can break that
//   assumption. This could be useful in future but might need to block now.

// TODO: add callbacks as appropriate to keep input valid

/**
 * Changes the values of the axes. For example they can go from [x,y,z] to
 * [c,t,frequency]. This is a convenience plugin that allows axis types to be
 * reassigned. Useful if imported data has the wrong axis designations. Pixel
 * data is NOT rearranged.
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = MenuConstants.IMAGE_LABEL, weight = MenuConstants.IMAGE_WEIGHT,
		mnemonic = MenuConstants.IMAGE_MNEMONIC),
	@Menu(label = "Axes", mnemonic = 'a'), @Menu(label = "Assign Axes...") },
	headless = true, initializer = "initAxes")
public class AssignAxes extends DynamicPlugin {

	// -- Parameters --

	@Parameter(type = ItemIO.BOTH)
	private Dataset dataset;

	// -- AssignAxes methods --

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(final Dataset dataset) {
		this.dataset = dataset;
	}

	// -- Runnable methods --

	/** Runs the plugin and assigns axes as specified by user. */
	@Override
	public void run() {
		if (dataset == null) return;
		String[] axisNames = getAxisNames();
		if (inputBad(axisNames)) return;
		AxisType[] desiredAxes = getDesiredAxes(axisNames);
		dataset.setAxes(desiredAxes);
	}

	// -- Initializers --

	protected void initAxes() {
		final ArrayList<String> choices = new ArrayList<String>();
		AxisType[] axes = Axes.values();
		for (AxisType axis : axes) {
			choices.add(axis.getLabel());
		}
		for (int i = 0; i < dataset.numDimensions(); i++) {
			final DefaultModuleItem<String> axisItem =
				new DefaultModuleItem<String>(this, name(i), String.class);
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
	private String[] getAxisNames() {
		final Map<String, Object> inputs = getInputs();
		String[] axisNames = new String[dataset.getImgPlus().numDimensions()];
		for (int i = 0; i < axisNames.length; i++)
			axisNames[i] = (String) inputs.get(name(i));
		return axisNames;
	}

	/**
	 * Returns true if user input is invalid. Basically this is a test that the
	 * user did not repeat any axis when specifying the axis ordering.
	 */
	private boolean inputBad(String[] axes) {
		for (int i = 0; i < axes.length; i++) {
			for (int j = i+1; j < axes.length; j++) {
				if (axes[i].equals(axes[j])) {
					Log.error("At least one axis designation is repeated:"
							+ " axis designations must be mututally exclusive");
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns the new axes as specified by the user.
	 */
	private AxisType[] getDesiredAxes(String[] axisNames) {
		final AxisType[] newAxes = new AxisType[axisNames.length];
		for (int i = 0; i < newAxes.length; i++) {
			newAxes[i] = Axes.get(axisNames[i]);
		}
		return newAxes;
	}

}
