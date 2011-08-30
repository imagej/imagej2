//
// AnimatorOptionsPlugin.java
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.imglib2.img.Axes;
import net.imglib2.img.Axis;
import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.display.DisplayService;
import imagej.display.ImageDisplay;
import imagej.ext.module.DefaultModuleItem;
import imagej.ext.plugin.DynamicPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Plugin;

/**
 * This class manipulates options that affect the {@link Animator} class'
 * run() method.
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = { @Menu(label = "Image", mnemonic = 'i'),
	@Menu(label = "Stacks", mnemonic = 's'),
	@Menu(label = "Tools", mnemonic = 't'),
	@Menu(label = "Animate Options") })
public class AnimatorOptionsPlugin extends DynamicPlugin {
	
	// -- private constants --
	
	private static final String NAME_KEY = "Axis";
	private static final String FIRST_POS_KEY = "First position";
	private static final String LAST_POS_KEY = "Last position";
	private static final String FPS_KEY = "Speed (0.1 - 1000 fps)";
	private static final String BACK_FORTH_KEY = "Loop back and forth";

	// -- instance variables --
	
	private ImageDisplay currDisplay;
	private Dataset dataset;
	String axisName;
	private long oneBasedFirst;
	private long oneBasedLast;

	private long first;
	private long last;
	private double fps;
	private boolean backForth;

	private AnimatorOptions options;
	
	/**
	 * construct the DynamicPlugin from a Display's Dataset
	 */
	public AnimatorOptionsPlugin() {
		final DisplayService displayService = ImageJ.get(DisplayService.class);
		currDisplay = displayService.getActiveImageDisplay();
		if (currDisplay == null) return;
		dataset = ImageJ.get(DisplayService.class).getActiveDataset(currDisplay);
		if (dataset == null) return;
		if (dataset.getDims().length <= 2) return;
		options = Animator.getOptions(currDisplay);  
		final DefaultModuleItem<String> name =
			new DefaultModuleItem<String>(this, NAME_KEY, String.class);
		final List<Axis> datasetAxes = Arrays.asList(dataset.getAxes());
		final ArrayList<String> choices = new ArrayList<String>();
		for (final Axis candidateAxis : Axes.values()) {
			if ((candidateAxis == Axes.X) || (candidateAxis == Axes.Y)) continue;
			if (datasetAxes.contains(candidateAxis))
				choices.add(candidateAxis.getLabel());
		}
		name.setChoices(choices);
		addInput(name);
		setInput(NAME_KEY, new String(options.axis.getLabel()));

		final DefaultModuleItem<Long> firstPos =
			new DefaultModuleItem<Long>(this, FIRST_POS_KEY, Long.class);
		firstPos.setMinimumValue(1L);
		// TODO - set max somehow based upon dataset's dimension along axis
		addInput(firstPos);
		setInput(FIRST_POS_KEY, new Long(options.first+1));

		final DefaultModuleItem<Long> lastPos =
			new DefaultModuleItem<Long>(this, LAST_POS_KEY, Long.class);
		lastPos.setMinimumValue(1L);
		// TODO - set max somehow based upon dataset's dimension along axis
		addInput(lastPos);
		setInput(LAST_POS_KEY, new Long(options.last+1));

		final DefaultModuleItem<Double> framesPerSec =
			new DefaultModuleItem<Double>(this, FPS_KEY, Double.class);
		framesPerSec.setMinimumValue(0.1);
		framesPerSec.setMaximumValue(1000.0);
		addInput(framesPerSec);
		setInput(FPS_KEY, new Double(options.fps));

		final DefaultModuleItem<Boolean> bf =
			new DefaultModuleItem<Boolean>(this, BACK_FORTH_KEY, Boolean.class);
		addInput(bf);
		setInput(BACK_FORTH_KEY, new Boolean(options.backAndForth));
	}

	/**
	 * Harvests the input values from the user and updates the current
	 * display's set of animation options. Each display has it's own set
	 * of options. Any animation launched on a display will use it's set
	 * of options. Options can be changed during the run of an animation.
	 */
	@Override
	public void run() {
		if (currDisplay == null) return;
		harvestInputs();
		Axis axis = Axes.get(axisName);
		final int axisIndex = dataset.getImgPlus().getAxisIndex(axis);
		if (axisIndex < 0) return;
		final long totalHyperplanes = dataset.getImgPlus().dimension(axisIndex);
		setFirstAndLast(totalHyperplanes);
		options.axis = axis;
		options.backAndForth = backForth;
		options.first = first;
		options.last = last;
		options.fps = fps;
		options.total = totalHyperplanes;
		Animator.optionsUpdated(currDisplay);
	}

	// -- private helpers --

	/**
	 * Harvest the user's input values from the dialog
	 */
	private void harvestInputs() {
		final Map<String, Object> inputs = getInputs();
		axisName = (String) inputs.get(NAME_KEY);
		oneBasedFirst = (Long) inputs.get(FIRST_POS_KEY);
		oneBasedLast = (Long) inputs.get(LAST_POS_KEY);
		fps = (Double) inputs.get(FPS_KEY);
		backForth = (Boolean) inputs.get(BACK_FORTH_KEY);
	}

	/**
	 * Sets the zero-based indices of the first and last frames
	 */
	private void setFirstAndLast(final long totalHyperplanes) {
		first = Math.min(oneBasedFirst, oneBasedLast) - 1;
		last = Math.max(oneBasedFirst, oneBasedLast) - 1;
		if (first < 0) first = 0;
		if (last < 0) last = 0;
		if (first >= totalHyperplanes) first = totalHyperplanes-1;
		if (last >= totalHyperplanes) last = totalHyperplanes-1;
	}
}
