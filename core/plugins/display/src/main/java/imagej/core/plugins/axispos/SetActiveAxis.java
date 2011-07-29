package imagej.core.plugins.axispos;

//
//SetActiveAxis.java
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.display.Display;
import imagej.display.DisplayService;
import imagej.ext.module.DefaultModuleItem;
import imagej.ext.plugin.DynamicPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Plugin;
import net.imglib2.img.Axes;
import net.imglib2.img.Axis;


/**
* Changes the axis to move along to user specified value. This axis of movement
* is used in the move axis position forward/backward plugins.
* 
* @author Barry DeZonia
*/
@Plugin(menu = {
@Menu(label = "Image", mnemonic = 'i'),
@Menu(label = "Stacks", mnemonic = 's'),
@Menu(label = "Set Active Axis...") })
public class SetActiveAxis extends DynamicPlugin {

	private static final String NAME_KEY = "Axis";

	Display display;
	
	String axisName;
	
	public SetActiveAxis() {
		final DisplayService displayService = ImageJ.get(DisplayService.class);
		display = displayService.getActiveDisplay();
		if (display == null) return;
		Dataset dataset = ImageJ.get(DisplayService.class).getActiveDataset(display);
		final DefaultModuleItem<String> name =
			new DefaultModuleItem<String>(this, NAME_KEY, String.class);
		List<Axis> datasetAxes = Arrays.asList(dataset.getAxes());
		ArrayList<String> choices = new ArrayList<String>();
		for (Axis candidateAxis : Axes.values()) {
			// TODO - remove someday when we allow X or Y sliders
			if ((candidateAxis == Axes.X) || (candidateAxis == Axes.Y)) continue;
			if (datasetAxes.contains(candidateAxis))
				choices.add(candidateAxis.getLabel());
		}
		name.setChoices(choices);
		addInput(name);
	}
	
	@Override
	public void run() {
		final Map<String, Object> inputs = getInputs();
		axisName = (String) inputs.get(NAME_KEY);
		Axis newActiveAxis = Axes.get(axisName);
		if (newActiveAxis != null)
			display.setActiveAxis(newActiveAxis);
	}
}
