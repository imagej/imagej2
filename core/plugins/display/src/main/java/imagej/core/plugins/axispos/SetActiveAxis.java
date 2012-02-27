//
// SetActiveAxis.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.core.plugins.axispos;

import imagej.data.display.ImageDisplay;
import imagej.ext.menu.MenuConstants;
import imagej.ext.module.DefaultModuleItem;
import imagej.ext.plugin.DynamicPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;

import java.util.ArrayList;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;

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
		@Menu(label = "Stacks", mnemonic = 's'),
		@Menu(label = "Set Active Axis...") }, headless = true)
public class SetActiveAxis extends DynamicPlugin {

	// -- Constants --

	private static final String AXIS_NAME = "axisName";

	// -- Parameters --

	@Parameter(persist = false)
	private ImageDisplay display;

	@Parameter(persist = false, initializer = "initAxisName")
	private String axisName;

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
		if (axis != null) display.setActiveAxis(axis);
	}

	// -- Initializers --

	protected void initAxisName() {
		@SuppressWarnings("unchecked")
		final DefaultModuleItem<String> axisNameItem =
			(DefaultModuleItem<String>) getInfo().getInput(AXIS_NAME);
		final AxisType[] axes = display.getAxes();
		final ArrayList<String> choices = new ArrayList<String>();
		for (final AxisType a : axes) {
			if (a.isXY()) continue;
			choices.add(a.getLabel());
		}
		axisNameItem.setChoices(choices);
	}

}
