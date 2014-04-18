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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.imagej.Dataset;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.meta.CalibratedAxis;
import net.imglib2.meta.axis.VariableAxis;

import org.scijava.ItemIO;
import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.command.DynamicCommand;
import org.scijava.log.LogService;
import org.scijava.menu.MenuConstants;
import org.scijava.module.MutableModuleItem;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

// TODO
// - code elsewhere assumes X and Y always present. this plugin can break that
//   assumption. This could be useful in future but might need to block now.

// TODO: add callbacks as appropriate to keep input valid

/**
 * Changes the types and calibrations of the existing axes of a {@link Dataset}.
 * Only axis metadata is changed. Pixel data is NOT rearranged.
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
@Plugin(type = Command.class, menu = {
	@Menu(label = MenuConstants.IMAGE_LABEL, weight = MenuConstants.IMAGE_WEIGHT,
		mnemonic = MenuConstants.IMAGE_MNEMONIC),
	@Menu(label = "Axes", mnemonic = 'a'), @Menu(label = "Edit Axes...") },
	headless = true, initializer = "initFields")
public class EditAxes extends DynamicCommand {

	// -- Parameters --

	@Parameter
	private LogService log;

	@Parameter(type = ItemIO.BOTH)
	private Dataset dataset;

	// -- EditAxes methods --

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(final Dataset dataset) {
		this.dataset = dataset;
	}

	public AxisType getAxisType(final int d) {
		final String value = typeInput(d).getValue(this);
		return value == null || value.isEmpty() ? null : Axes.get(value);
	}

	public void setAxisType(final int d, final AxisType axisType) {
		typeInput(d).setValue(this, axisType.toString());
	}

	public String getUnit(final int d) {
		final String value = unitInput(d).getValue(this);
		return value == null || value.isEmpty() ? null : value;
	}

	public void setUnit(final int d, final String unit) {
		unitInput(d).setValue(this, unit);
	}

	public double getVar(final int d, final String var) {
		return varInput(d, var).getValue(this);
	}

	public void setVar(final int d, final String var, final double value) {
		varInput(d, var).setValue(this, value);
	}

	// -- Runnable methods --

	/** Assigns axis attributes to the axes of the input dataset. */
	@Override
	public void run() {
		checkAxisTypes();
		if (isCanceled()) return;

		for (int d = 0; d < dataset.numDimensions(); d++) {
			final CalibratedAxis axis = dataset.axis(d);
			axis.setType(getAxisType(d));
			axis.setUnit(getUnit(d));
			if (!(axis instanceof VariableAxis)) continue; // nothing else to do
			final VariableAxis varAxis = (VariableAxis) axis;
			for (final String var : vars(varAxis)) {
				varAxis.set(var, getVar(d, var));
			}
		}
	}

	// -- Initializers --

	protected void initFields() {
		final ArrayList<String> choices = new ArrayList<String>();
		final AxisType[] axes = Axes.knownTypes();
		for (final AxisType axis : axes) {
			choices.add(axis.getLabel());
		}

		for (int d = 0; d < dataset.numDimensions(); d++) {
			final CalibratedAxis axis = dataset.axis(d);

			// add message for visually grouping related items per axis
			final MutableModuleItem<String> axisItem =
				addInput("axis" + d, String.class);
			axisItem.setPersisted(false);
			axisItem.setVisibility(ItemVisibility.MESSAGE);
			axisItem.setValue(this, "-- Axis #" + (d + 1) + " --");

			// add axis type selector
			final MutableModuleItem<String> typeItem =
				addInput(typeName(d), String.class);
			typeItem.setPersisted(false);
			typeItem.setLabel("Type");
			typeItem.setChoices(choices);
			typeItem.setValue(this, axis.type().toString());

			// add unit text field
			final MutableModuleItem<String> unitItem =
				addInput(unitName(d), String.class);
			unitItem.setPersisted(false);
			unitItem.setLabel("Unit");
			unitItem.setValue(this, axis.unit());

			final boolean isVariable = axis instanceof VariableAxis;
			final String equation =
				isVariable ? axis.generalEquation() : axis.particularEquation();

			// add equation message
			final MutableModuleItem<String> equationItem =
				addInput("equation" + d, String.class);
			equationItem.setPersisted(false);
			equationItem.setVisibility(ItemVisibility.MESSAGE);
			equationItem.setValue(this, equation);

			if (!isVariable) {
				// NB: No known way to provide control over the axis calibration.
				continue;
			}

			// add variables
			final VariableAxis varAxis = (VariableAxis) axis;
			for (final String var : vars(varAxis)) {
				final MutableModuleItem<Double> varItem =
					addInput(varName(d, var), Double.class);
				varItem.setPersisted(false);
				varItem.setLabel(var);
				varItem.setValue(this, varAxis.get(var));
			}
		}
	}

	// -- Helper methods --

	private String typeName(final int d) {
		return "type" + d;
	}

	private String unitName(final int d) {
		return "unit" + d;
	}

	private String varName(final int d, final String var) {
		return "var" + d + ":" + var;
	}

	@SuppressWarnings("unchecked")
	private MutableModuleItem<String> typeInput(final int d) {
		return (MutableModuleItem<String>) getInfo().getInput(typeName(d));
	}

	@SuppressWarnings("unchecked")
	private MutableModuleItem<String> unitInput(final int d) {
		return (MutableModuleItem<String>) getInfo().getInput(unitName(d));
	}

	@SuppressWarnings("unchecked")
	private MutableModuleItem<Double> varInput(final int d, final String var) {
		return (MutableModuleItem<Double>) getInfo().getInput(varName(d, var));
	}

	/** Checks that the specified axis ordering does not repeat any axis types. */
	private void checkAxisTypes() {
		for (int i = 0; i < dataset.numDimensions(); i++) {
			final AxisType axisType = getAxisType(i);
			for (int j = i + 1; j < dataset.numDimensions(); j++) {
				if (axisType == getAxisType(j)) {
					cancel("Axes #" + (i + 1) + " and #" + (j + 1) + " are both " +
						axisType + ". Axis designations must be mutually exclusive");
					return;
				}
			}
		}
	}

	/** Gets a sorted list of variables associated with the given axis. */
	private List<String> vars(final VariableAxis varAxis) {
		final ArrayList<String> vars = new ArrayList<String>(varAxis.vars());
		Collections.sort(vars);
		return vars;
	}

}
