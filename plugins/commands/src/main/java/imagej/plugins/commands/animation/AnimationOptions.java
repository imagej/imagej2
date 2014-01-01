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

package imagej.plugins.commands.animation;

import imagej.command.Command;
import imagej.command.DynamicCommand;
import imagej.data.animation.Animation;
import imagej.data.animation.AnimationService;
import imagej.data.display.ImageDisplay;
import imagej.menu.MenuConstants;
import imagej.module.MutableModuleItem;

import java.util.ArrayList;

import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;

import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Plugin for adjusting options that affect the behavior of animations.
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
@Plugin(type = Command.class, menu = {
	@Menu(label = MenuConstants.IMAGE_LABEL, weight = MenuConstants.IMAGE_WEIGHT,
		mnemonic = MenuConstants.IMAGE_MNEMONIC),
	@Menu(label = "Animation", mnemonic = 'a'),
	@Menu(label = "Animation Options...", weight = 4) }, headless = true)
public class AnimationOptions extends DynamicCommand {

	// -- Parameters --

	@Parameter
	private AnimationService animationService;

	@Parameter
	private ImageDisplay display;

	@Parameter(label = "Axis", persist = false, initializer = "initAxisName",
		callback = "axisChanged")
	private String axisName;

	@Parameter(label = "First position", persist = false,
		initializer = "initFirst", min = "1")
	private long first;

	@Parameter(label = "Last position", persist = false,
		initializer = "initLast", min = "1")
	private long last;

	@Parameter(label = "Speed (0.1 - 1000 fps)", persist = false,
		initializer = "initFPS", min = "0.1", max = "1000")
	private double fps;

	@Parameter(label = "Loop back and forth", persist = false,
		initializer = "initBackAndForth")
	private boolean backAndForth;

	// -- AnimatorOptions methods --

	public AnimationService getAnimationService() {
		return animationService;
	}

	public void setAnimationService(final AnimationService animationService) {
		this.animationService = animationService;
	}

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

	public double getFPS() {
		return fps;
	}

	public void setFPS(final double fps) {
		this.fps = fps;
	}

	public long getFirst() {
		return first;
	}

	public void setFirst(final long first) {
		this.first = first;
	}

	public long getLast() {
		return last;
	}

	public void setLast(final long last) {
		this.last = last;
	}

	public boolean isBackAndForth() {
		return backAndForth;
	}

	public void setBackAndForth(final boolean backAndForth) {
		this.backAndForth = backAndForth;
	}

	// -- Runnable methods --

	/**
	 * Updates a display's set of animation options. Each display has its own set
	 * of options. Any animation launched on a display will use its set of
	 * options. Options can be changed during the run of an animation.
	 */
	@Override
	public void run() {
		clampFirstAndLast();

		// update animation settings
		final Animation animation = getAnimation();
		final boolean active = animation.isActive();
		animation.stop();
		animation.setAxis(getAxis());
		animation.setBackAndForth(isBackAndForth());
		animation.setFirst(getFirst() - 1);
		animation.setLast(getLast() - 1);
		animation.setFPS(getFPS());
		if (active) animation.start();
	}

	// -- Initializer methods --

	/** Initializes axisName value. */
	protected void initAxisName() {
		final MutableModuleItem<String> axisNameItem =
			getInfo().getMutableInput("axisName", String.class);
		final ArrayList<String> choices = new ArrayList<String>();
		for (int d = 0; d < getDisplay().numDimensions(); d++) {
			AxisType axisType = getDisplay().axis(d).type();
			if (axisType.isXY()) continue;
			choices.add(axisType.getLabel());
		}
		axisNameItem.setChoices(choices);
		final AxisType curAxis = getAnimation().getAxis();
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

	/** Initializes backAndForth value. */
	protected void initBackAndForth() {
		setBackAndForth(getAnimation().isBackAndForth());
	}

	// -- Callback methods --

	/** Updates the first and last values when the axis changes. */
	protected void axisChanged() {
		setLast(getAxisLength());
		clampFirstAndLast();
	}

	// -- Helper methods --

	/** Ensures the first and last values fall within the allowed range. */
	private void clampFirstAndLast() {
		final long max = getAxisLength();
		long f = getFirst(), l = getLast();
		if (f < 1) f = 1;
		if (l < 1) l = 1;
		if (f > max) f = max;
		if (l > max) l = max;
		setFirst(f);
		setLast(l);
	}

	/** Gets the length of the selected axis. */
	private long getAxisLength() {
		final int axisIndex = getDisplay().dimensionIndex(getAxis());
		if (axisIndex < 0) return -1;
		return getDisplay().dimension(axisIndex);
	}

	private Animation getAnimation() {
		return getAnimationService().getAnimation(getDisplay());
	}

}
