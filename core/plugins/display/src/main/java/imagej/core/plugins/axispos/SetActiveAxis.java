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

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.ext.module.DefaultModuleItem;
import imagej.ext.plugin.DynamicPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;

/**
 * Changes the axis to move along to user specified value. This axis of movement
 * is used in the move axis position forward/backward plugins.
 * 
 * @author Barry DeZonia
 */
@Plugin(
	menu = { @Menu(label = "Image", mnemonic = 'i'),
		@Menu(label = "Stacks", mnemonic = 's'),
		@Menu(label = "Set Active Axis...") })
public class SetActiveAxis extends DynamicPlugin {

	// -- instance variables --
	
	private static final String DISPLAY = "display";
	private static final String AXIS_NAME = "axisName";

	@SuppressWarnings("unused")
	@Parameter(required = true, persist = false)
	private ImageDisplay display;

	@SuppressWarnings("unused")
	@Parameter(persist = false, initializer = "initAxisName")
	private String axisName;

	// public interface --
	
	@Override
	public void run() {
		ImageDisplay disp = getDisplay();
		String axis = getAxisName();
		final AxisType newActiveAxis = Axes.get(axis);
		if (newActiveAxis != null) disp.setActiveAxis(newActiveAxis);
	}
	
	// -- private helpers --
	
	private ImageDisplay getDisplay() {
		return (ImageDisplay) getInput(DISPLAY); 
	}
	
	private String getAxisName() {
		return (String) getInput(AXIS_NAME); 
	}

	private Dataset getDataset() {
		final ImageDisplayService imageDisplayService =
				ImageJ.get(ImageDisplayService.class);
		return imageDisplayService.getActiveDataset(getDisplay());
	}
	
	@SuppressWarnings("unused")
	private void initAxisName() {
		@SuppressWarnings("unchecked")
		final DefaultModuleItem<String> axisNameItem =
			(DefaultModuleItem<String>) getInfo().getInput(AXIS_NAME);
		final AxisType[] axes = getDataset().getAxes();
		final ArrayList<String> choices = new ArrayList<String>();
		for (final AxisType a : axes) {
			if (a.isXY()) continue;
			choices.add(a.getLabel());
		}
		axisNameItem.setChoices(choices);
	}
}
