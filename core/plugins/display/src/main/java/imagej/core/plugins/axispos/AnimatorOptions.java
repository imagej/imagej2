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

import imagej.ImageJ;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.ext.module.DefaultModuleItem;
import imagej.ext.plugin.DynamicPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;

import java.util.ArrayList;

import net.imglib2.img.Axes;
import net.imglib2.img.Axis;

/**
 * Plugin for adjusting options that affect the behavior of animations.
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
@Plugin(menu = { @Menu(label = "Image", mnemonic = 'i'),
	@Menu(label = "Stacks", mnemonic = 's'),
	@Menu(label = "Tools", mnemonic = 't'),
	@Menu(label = "Animation Options...", weight = 4) })
public class AnimatorOptions extends DynamicPlugin {

	// -- parameters --

	@Parameter(label = "Axis", persist = false)
	@SuppressWarnings("unused")
	private String axisName;

	@Parameter(label = "First position", persist = false, min = "1")
	@SuppressWarnings("unused")
	private long first;

	@Parameter(label = "Last position", persist = false, min = "1")
	@SuppressWarnings("unused")
	private long last;

	@Parameter(label = "Speed (0.1 - 1000 fps)", persist = false, min = "0.1",
		max = "1000")
	@SuppressWarnings("unused")
	private double fps;

	@Parameter(label = "Loop back and forth", persist = false)
	@SuppressWarnings("unused")
	private boolean backForth;

	// -- instance variables --

	private ImageDisplay display;
	private Animation animation;

	public AnimatorOptions() {
		// make sure input is okay
		final ImageDisplayService imageDisplayService =
			ImageJ.get(ImageDisplayService.class);
		display = imageDisplayService.getActiveImageDisplay();
		if (display == null) return;

		final AnimationService animationService =
			ImageJ.get(AnimationService.class);
		animation = animationService.getAnimation(display);

		// axis name field initialization
		@SuppressWarnings("unchecked")
		final DefaultModuleItem<String> axisNameItem =
			(DefaultModuleItem<String>) getInfo().getInput("axisName");
		final Axis[] axes = display.getAxes();
		final ArrayList<String> choices = new ArrayList<String>();
		for (final Axis axis : axes) {
			if (Axes.isXY(axis)) continue;
			choices.add(axis.getLabel());
		}
		axisNameItem.setChoices(choices);
		final Axis curAxis = animation.getAxis();
		if (curAxis != null) setAxis(curAxis);

		// first position field initialization
		// TODO - can't set max value as it varies based upon the axis the user
		// selects at dialog run time. Need a callback that can manipulate the
		// field's max value from chosen axis max.
		// firstItem.setMaximumValue(new Long(options.total));
		setFirst(animation.getFirst() + 1);

		// last position field initialization
		// TODO - can't set max value as it varies based upon the axis the user
		// selects at dialog run time. Need a callback that can manipulate the
		// field's max value from chosen axis max.
		// lastItem.setMaximumValue(new Long(options.total));
		setLast(animation.getLast() + 1);

		// frames per second field initialization
		setFPS(animation.getFps());

		// back and forth field initialization
		setBackAndForth(animation.isBackAndForth());
	}

	// CTR TODO - Solve issues with DynamicPlugin + @Parameter:
	//
	// 1) Values assigned to the @Parameter fields at any point will not be
	// reflected in the module itself (e.g., when the input dialog pops up).
	// This is because AbstractModule (and by extension DefaultModule) keeps
	// its own copy of all the values in an internal table. The @Parameter
	// logic works with non-dynamic plugins because PluginModule overrides
	// Module.setInput(String, Object) and Module.setOutput(String, Object)
	// to write to the @Parameter field directly.
	// 2) Similarly, reading @Parameter fields in the run method will not work,
	// because they will not have been updated to match the DynamicPlugin's
	// internal table of values.
	//
	// How can we keep the values of the @Parameter variables in sync?
	// At minimum, DynamicPlugin could have two public methods for syncing,
	// one for each direction. Would be nice if it wasn't necessary to call
	// them explicitly, though.
	//
	// In the meantime, we must be careful to always get and set the parameter
	// values using the accessors and mutators below, rather than accessing the
	// @Parameter fields directly.

	public Axis getAxis() {
		return Axes.get((String) getInput("axisName"));
	}

	public void setAxis(final Axis axis) {
		setInput("axisName", axis.toString());
	}

	public double getFPS() {
		return (Double) getInput("fps");
	}

	public void setFPS(final double fps) {
		setInput("fps", fps);
	}

	public long getFirst() {
		return (Long) getInput("first");
	}

	public void setFirst(final long first) {
		setInput("first", first);
	}

	public long getLast() {
		return (Long) getInput("last");
	}

	public void setLast(final long last) {
		setInput("last", last);
	}

	public boolean isBackAndForth() {
		return (Boolean) getInput("backForth");
	}

	public void setBackAndForth(final boolean backAndForth) {
		setInput("backForth", backAndForth);
	}

	/**
	 * Updates a display's set of animation options. Each display has its own set
	 * of options. Any animation launched on a display will use its set of
	 * options. Options can be changed during the run of an animation.
	 */
	@Override
	public void run() {
		if (display == null) return;
		final Axis axis = getAxis();
		final int axisIndex = display.getAxisIndex(axis);
		if (axisIndex < 0) return;
		final long totalHyperplanes = display.getExtents().dimension(axisIndex);
		setFirstAndLast(totalHyperplanes);

		// update animation settings
		final boolean active = animation.isActive();
		animation.stop();
		animation.setAxis(axis);
		animation.setBackAndForth(isBackAndForth());
		animation.setFirst(getFirst() - 1);
		animation.setLast(getLast() - 1);
		animation.setFps(getFPS());
		if (active) animation.start();
	}

	// -- private helpers --

	/** Sets the zero-based indices of the first and last frames. */
	private void setFirstAndLast(final long totalHyperplanes) {
		long f = getFirst(), l = getLast();
		if (f < 1) f = 1;
		if (l < 1) l = 1;
		if (f > totalHyperplanes) f = totalHyperplanes;
		if (l > totalHyperplanes) l = totalHyperplanes;
		setFirst(f);
		setLast(l);
	}

}
