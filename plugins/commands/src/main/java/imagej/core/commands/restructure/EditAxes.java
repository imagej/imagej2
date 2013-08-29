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
import net.imglib2.meta.CalibratedAxis;

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
 * Changes the types and calibrations of the existing axes of a {@link Dataset}.
 * Only axis metadata is changed. Pixel data is NOT rearranged.
 * 
 * @author Barry DeZonia
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

	// -- AssignAxes methods --

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(final Dataset dataset) {
		this.dataset = dataset;
	}

	public AxisType getAxisMapping(int axisNum) {
		String axisName = (String) getInput(axisName(axisNum));
		return Axes.get(axisName);
	}
	
	public void setAxisMapping(int axisNum, AxisType axis) {
		String axisName = axisName(axisNum);
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
		for (int i = 0; i < dataset.numDimensions(); i++) {
			CalibratedAxis axis = dataset.axis(i);
			String axisName = (String) getInput(axisName(i));
			AxisType type = Axes.get(axisName);
			String unit = (String) getInput(unitName(i));
			double cal = (Double) getInput(scaleName(i));
			axis.setType(type);
			// TODO : at this point Dataset may have two Z axes for instance. If a
			// prob we need to set them all at once. See if an axis label change fires
			// some update events.
			if (unit.length() == 0) axis.setUnit(null);
			else axis.setUnit(unit);
			axis.setCalibration(cal);
		}
	}

	// -- Initializers --

	protected void initFields() {
		ArrayList<String> choices = new ArrayList<String>();
		AxisType[] axes = Axes.values();
		for (AxisType axis : axes) {
			choices.add(axis.getLabel());
		}
		for (int i = 0; i < dataset.numDimensions(); i++) {
			final DefaultMutableModuleItem<String> axisItem =
				new DefaultMutableModuleItem<String>(this, axisName(i), String.class);
			axisItem.setChoices(choices);
			axisItem.setPersisted(false);
			axisItem.setValue(this, dataset.axis(i).type().toString());
			addInput(axisItem);
			final DefaultMutableModuleItem<String> unitItem =
				new DefaultMutableModuleItem<String>(this, unitName(i), String.class);
			unitItem.setPersisted(false);
			unitItem.setValue(this, dataset.axis(i).unit());
			addInput(unitItem);
			final DefaultMutableModuleItem<Double> scaleItem =
				new DefaultMutableModuleItem<Double>(this, scaleName(i), Double.class);
			scaleItem.setMinimumValue(0.000000000001);
			scaleItem.setPersisted(false);
			scaleItem.setValue(this, dataset.axis(i).calibration());
			addInput(scaleItem);
		}
	}

	// -- Helper methods --

	private String axisName(final int i) {
		return "Axis #" + i;
	}

	private String unitName(final int i) {
		return "Unit #" + i;
	}

	private String scaleName(final int i) {
		return "Scale #" + i;
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

	/* 
	 * Save in case we need to set axes' types all at once
	 * 
	private CalibratedAxis[] axisOrder(AxisType[] order) {
		CalibratedAxis[] axisOrder = new CalibratedAxis[order.length];
		for (int i = 0; i < order.length; i++) {
			for (int j = 0; j < order.length; j++) {
				if (dataset.axis(j).type().equals(order[i])) {
					axisOrder[i] = dataset.axis(j);
				}
			}
		}
		return axisOrder;
	}
	*/
}
