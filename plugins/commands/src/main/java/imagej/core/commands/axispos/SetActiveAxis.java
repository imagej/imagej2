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

package imagej.core.commands.axispos;

import imagej.command.DynamicCommand;
import imagej.data.animation.Animation;
import imagej.data.animation.AnimationService;
import imagej.data.display.ImageDisplay;
import imagej.menu.MenuConstants;
import imagej.module.MutableModuleItem;

import java.util.ArrayList;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;

import org.scijava.ItemIO;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Changes the axis to move along to user specified value. This axis of movement
 * is used in the move axis position forward/backward plugins.
 * 
 * @author Barry DeZonia
 */
@Plugin(
	menu = {
		@Menu(label = MenuConstants.IMAGE_LABEL,
			weight = MenuConstants.IMAGE_WEIGHT,
			mnemonic = MenuConstants.IMAGE_MNEMONIC),
		@Menu(label = "Axes", mnemonic = 'a'),
		@Menu(label = "Set Active Axis...") }, headless = true)
public class SetActiveAxis extends DynamicCommand {

	// -- Constants --

	private static final String AXIS_NAME = "axisName";

	// -- Parameters --

	@Parameter(type = ItemIO.BOTH)
	private ImageDisplay display;

	@Parameter(persist = false, initializer = "initAxisName")
	private String axisName;

	@Parameter
	private AnimationService animationService;
	
	// -- SetActiveAxis methods --

	public ImageDisplay getDisplay() {
		return display;
	}

	public void setDisplay(final ImageDisplay display) {
		this.display = display;
	}

	public AxisType getAxis() {
		return Axes.get(axisName);
	}

	public void setAxis(final AxisType axis) {
		axisName = axis.toString();
	}

	// -- Runnable methods --

	@Override
	public void run() {
		final AxisType axis = getAxis();
		if (axis != null) {
			display.setActiveAxis(axis);
			int axisIndex = display.getAxisIndex(axis);
			long last = display.getExtents().dimension(axisIndex) - 1;
			Animation a = animationService.getAnimation(display);
			boolean active = a.isActive();
			if (active) a.stop();
			a.setAxis(axis);
			a.setFirst(0);
			a.setLast(last);
			if (active) a.start();
		}
	}

	// -- Initializers --

	protected void initAxisName() {
		@SuppressWarnings("unchecked")
		final MutableModuleItem<String> axisNameItem =
			(MutableModuleItem<String>) getInfo().getInput(AXIS_NAME);
		final AxisType[] axes = display.getAxes();
		final ArrayList<String> choices = new ArrayList<String>();
		for (final AxisType a : axes) {
			if (a.isXY()) continue;
			choices.add(a.getLabel());
		}
		axisNameItem.setChoices(choices);
	}

}
