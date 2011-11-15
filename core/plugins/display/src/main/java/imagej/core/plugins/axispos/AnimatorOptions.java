//
// AnimatorOptions.java
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

	// -- Parameters --

	@Parameter(required = true, persist = false)
	@SuppressWarnings("unused")
	private ImageDisplay display;

	@Parameter(required = true, persist = false)
	@SuppressWarnings("unused")
	private AnimationService animationService;

	@Parameter(label = "Axis", persist = false, initializer = "initAxis")
	@SuppressWarnings("unused")
	private String axisName;

	@Parameter(label = "First position", persist = false,
		initializer = "initFirst", min = "1")
	@SuppressWarnings("unused")
	private long first;

	@Parameter(label = "Last position", persist = false,
		initializer = "initLast", min = "1")
	@SuppressWarnings("unused")
	private long last;

	@Parameter(label = "Speed (0.1 - 1000 fps)", persist = false,
		initializer = "initFPS", min = "0.1", max = "1000")
	@SuppressWarnings("unused")
	private double fps;

	@Parameter(label = "Loop back and forth", persist = false,
		initializer = "initBackAndForth")
	@SuppressWarnings("unused")
	private boolean backForth;

	// -- AnimatorOptions methods --

	// CTR TODO - Solve issues with DynamicPlugin + @Parameter:
	//
	// 1) Values assigned to the @Parameter fields at any point will not be
	// reflected in the module itself (e.g., when the input dialog pops up).
	// This is because AbstractModule (and by extension DefaultModule) keeps
	// its own copy of all the values in an internal table. The @Parameter
	// logic works with non-dynamic plugins because PluginModule overrides
	// Module.setInput(String, Object) and Module.setOutput(String, Object)
	// to write to the @Parameter field directly.
	//
	// 2) Similarly, accessing @Parameter fields in the run method will not work,
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

	public ImageDisplay getDisplay() {
		return (ImageDisplay) getInput("display");
	}

	public AnimationService getAnimationService() {
		return (AnimationService) getInput("animationService");
	}

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

	// -- Runnable methods --

	/**
	 * Updates a display's set of animation options. Each display has its own set
	 * of options. Any animation launched on a display will use its set of
	 * options. Options can be changed during the run of an animation.
	 */
	@Override
	public void run() {
		final Axis axis = getAxis();
		final int axisIndex = getDisplay().getAxisIndex(axis);
		if (axisIndex < 0) return;
		final long max = getDisplay().getExtents().dimension(axisIndex);
		clampFirstAndLast(max);

		// update animation settings
		final Animation animation = getAnimation();
		final boolean active = animation.isActive();
		animation.stop();
		animation.setAxis(axis);
		animation.setBackAndForth(isBackAndForth());
		animation.setFirst(getFirst() - 1);
		animation.setLast(getLast() - 1);
		animation.setFPS(getFPS());
		if (active) animation.start();
	}

	// -- Initializer methods --

	/** Initializes axisName value. */
	protected void initAxis() {
		@SuppressWarnings("unchecked")
		final DefaultModuleItem<String> axisNameItem =
			(DefaultModuleItem<String>) getInfo().getInput("axisName");
		final Axis[] axes = getDisplay().getAxes();
		final ArrayList<String> choices = new ArrayList<String>();
		for (final Axis axis : axes) {
			if (Axes.isXY(axis)) continue;
			choices.add(axis.getLabel());
		}
		axisNameItem.setChoices(choices);
		final Axis curAxis = getAnimation().getAxis();
		if (curAxis != null) setAxis(curAxis);
	}

	/** Initializes first value. */
	protected void initFirst() {
		// TODO - can't set max value as it varies based upon the axis the user
		// selects at dialog run time. Need a callback that can manipulate the
		// field's max value from chosen axis max.
		setFirst(getAnimation().getFirst() + 1);
	}

	/** Initializes last value. */
	protected void initLast() {
		// TODO - can't set max value as it varies based upon the axis the user
		// selects at dialog run time. Need a callback that can manipulate the
		// field's max value from chosen axis max.
		setLast(getAnimation().getLast() + 1);
	}

	/** Initializes fps value. */
	protected void initFPS() {
		setFPS(getAnimation().getFPS());
	}

	/** Initializes backForth value. */
	protected void initBackAndForth() {
		setBackAndForth(getAnimation().isBackAndForth());
	}

	// -- Helper methods --

	/** Ensures the first and last values fall within the allowed range. */
	private void clampFirstAndLast(final long max) {
		long f = getFirst(), l = getLast();
		if (f < 1) f = 1;
		if (l < 1) l = 1;
		if (f > max) f = max;
		if (l > max) l = max;
		setFirst(f);
		setLast(l);
	}

	private Animation getAnimation() {
		return getAnimationService().getAnimation(getDisplay());
	}

}
