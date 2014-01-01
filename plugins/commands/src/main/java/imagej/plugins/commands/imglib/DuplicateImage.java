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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.plugins.commands.imglib;

import imagej.command.Command;
import imagej.command.DynamicCommand;
import imagej.data.display.ImageDisplay;
import imagej.data.sampler.AxisSubrange;
import imagej.data.sampler.SamplerService;
import imagej.data.sampler.SamplingDefinition;
import imagej.display.DisplayService;
import imagej.menu.MenuConstants;
import imagej.module.DefaultMutableModuleItem;

import java.util.HashMap;
import java.util.Map;

import net.imglib2.meta.AxisType;
import net.imglib2.meta.SpaceUtils;

import org.scijava.ItemIO;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Duplicates data from one input display to an output display. The planes to be
 * duplicated can be specified via dialog parameters. The XY coordinates can be
 * further constrained to be a subset of the current selection bounds.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Command.class, menu = {
	@Menu(label = MenuConstants.IMAGE_LABEL, weight = MenuConstants.IMAGE_WEIGHT,
		mnemonic = MenuConstants.IMAGE_MNEMONIC),
	@Menu(label = "Duplicate", accelerator = "shift ^D") },
	headless = true, initializer = "initializer")
public class DuplicateImage extends DynamicCommand {

	// -- Parameters --

	@Parameter
	private SamplerService samplerService;

	@Parameter
	private DisplayService displayService;

	@Parameter
	private ImageDisplay inputDisplay;

	@Parameter(type = ItemIO.OUTPUT)
	private ImageDisplay outputDisplay;

	@Parameter(label = "Title:", initializer = "initName", persist = false)
	private String name = "";

	@Parameter(label = "Constrain axes as below:")
	private boolean specialBehavior = false;

	// -- instance variables that are not parameters --

	private Map<AxisType, AxisSubrange> definitions;
	private AxisType[] theAxes;

	// -- DuplicateImage methods --

	/**
	 * Specifies whether to use default behavior or special behavior. Default
	 * behavior copies the current composite XY plane. Special behavior is set
	 * when user defined axis definitions are to follow. If no axis definitions
	 * follow then all planes within the selected region will be copied.
	 */
	public void setDefaultBehavior(final boolean value) {
		specialBehavior = !value;
	}

	/**
	 * Returns true if current behavior is default. Otherwise current behavior is
	 * special. Default behavior copies the current composite XY plane. When
	 * special behavior is set then user defined axis definitions may have been
	 * specifed and if so those definitions are used during copying. Otherwise if
	 * no user defined axis definitions were specified then all planes within the
	 * selected region will be copied.
	 */
	public boolean isDefaultBehavior() {
		return !specialBehavior;
	}

	/**
	 * Sets the the input image to be sampled.
	 */
	public void setInputDisplay(final ImageDisplay disp) {
		inputDisplay = disp;
	}

	/**
	 * Returns the the input image to be sampled.
	 */
	public ImageDisplay getInputDisplay() {
		return inputDisplay;
	}

	/**
	 * The output image resulting after executing the run() method. This method
	 * returns null if the run() method has not yet been called.
	 */
	public ImageDisplay getOutputDisplay() {
		return outputDisplay;
	}

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
	 * <li>"1-10-2,20-30-3" : planes 1 through 10 by 2 and planes 20 through 30 by
	 * 3</li>
	 * <li>"1,3-5,12-60-6" : this shows each type of specification can be combined
	 * </li>
	 * </ul>
	 * 
	 * @param axis
	 * @param axisDefinition
	 * @param originIsOne
	 * @return null if successful else a message describing input error
	 */
	public String setAxisRange(final AxisType axis, final String axisDefinition,
		final boolean originIsOne)
	{
		specialBehavior = true;
		final AxisSubrange subrange =
			new AxisSubrange(inputDisplay, axis, axisDefinition, originIsOne);
		if (subrange.getError() == null) {
			definitions.put(axis, subrange);
		}
		return subrange.getError();
	}

	// -- Command methods --

	@Override
	public void run() {
		try {
			if (specialBehavior) {
				final SamplingDefinition samples = determineSamples();
				outputDisplay = samplerService.createSampledImage(samples);
			}
			else { // snapshot the existing composite selection
				outputDisplay =
					samplerService.duplicateSelectedCompositePlane(inputDisplay);
			}
		} catch (Exception e) {
			cancel(e.getMessage());
		}
		if (name.length() != 0) outputDisplay.setName(name);
		else outputDisplay.setName("Untitled");
	}

	// -- plugin parameter initializer --

	protected void initializer() {
		definitions = new HashMap<AxisType, AxisSubrange>();
		theAxes = SpaceUtils.getAxisTypes(inputDisplay);
		for (final AxisType axis : theAxes) {
			final DefaultMutableModuleItem<String> axisItem =
				new DefaultMutableModuleItem<String>(this, name(axis), String.class);
			axisItem.setPersisted(false);
			axisItem.setValue(this, fullRangeString(inputDisplay, axis));
			addInput(axisItem);
		}
	}

	protected void initName() {
		name = getUniqueName();
	}

	// -- private helpers --

	private String fullRangeString(final ImageDisplay disp, final AxisType axis) {
		final int axisIndex = disp.dimensionIndex(axis);
		return "1-" + disp.dimension(axisIndex);
	}

	/** takes definitions and applies them */
	private SamplingDefinition determineSamples() {
		if (definitions.size() > 0) {
			final SamplingDefinition def =
				SamplingDefinition.sampleAllPlanes(inputDisplay);
			for (final AxisType axis : definitions.keySet()) {
				def.constrain(axis, definitions.get(axis));
			}
			return def;
		}
		return parsedDefinition();
	}

	private SamplingDefinition parsedDefinition() {
		final SamplingDefinition sampleDef =
			SamplingDefinition.sampleAllPlanes(inputDisplay);
		for (final AxisType axis : theAxes) {
			final String definition = (String) getInput(name(axis));
			final AxisSubrange subrange =
				new AxisSubrange(inputDisplay, axis, definition, true);
			sampleDef.constrain(axis, subrange);
		}
		return sampleDef;
	}

	private String name(final AxisType axis) {
		return axis.getLabel() + " axis range";
	}

	private String getUniqueName() {
		String origName = inputDisplay.getName();
		String extension = "";
		int extPos = origName.lastIndexOf(".");
		if (extPos >= 0) {
			extension = origName.substring(extPos);
			origName = origName.substring(0, extPos);
		}
		String base = origName;
		if (isDefaultEnding(origName)) {
			int dashPos = base.lastIndexOf("-");
			base = base.substring(0, dashPos);
		}
		String s;
		int num = 1;
		do {
			s = base + "-" + num + extension;
			num++;
		}
		while (!displayService.isUniqueName(s));
		return s;
	}

	private boolean isDefaultEnding(String s) {
		int dashPos = s.lastIndexOf("-");
		if (dashPos < 0) return false;
		String rest = s.substring(dashPos + 1);
		if (rest == null || rest.length() == 0) return false;
		try {
			Integer.parseInt(rest);
			return true;
		}
		catch (NumberFormatException e) {
			return false;
		}
	}
}
