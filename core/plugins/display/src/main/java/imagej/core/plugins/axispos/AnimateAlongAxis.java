package imagej.core.plugins.axispos;

//
//AnimateAlongAxis.java
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

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.display.Display;
import imagej.display.DisplayService;
import imagej.display.event.AxisPositionEvent;
import imagej.display.event.DisplayDeletedEvent;
import imagej.display.event.key.KyPressedEvent;
import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.event.StatusEvent;
import imagej.ext.module.DefaultModuleItem;
import imagej.ext.plugin.DynamicPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Plugin;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.imglib2.img.Axes;
import net.imglib2.img.Axis;


/**
 * Partial replacement for animation commands in IJ1.
 *  
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = "Image", mnemonic = 'i'),
	@Menu(label = "Stacks", mnemonic = 's'),
	@Menu(label = "Tools", mnemonic = 't'),
	@Menu(label = "Animate", accelerator = "BACK_SLASH") })
public class AnimateAlongAxis extends DynamicPlugin {

	private static final String NAME_KEY = "Axis";
	private static final String FIRST_POS_KEY = "First position";
	private static final String LAST_POS_KEY = "Last position";
	private static final String FPS_KEY = "Speed (0.1 - 1000 fps)";
	private static final String BACK_FORTH_KEY = "Loop back and forth";
	
	// -- Parameters --

	private Dataset dataset;
	private double fps;
	String axisName;
	private long oneBasedFirst;
	private long oneBasedLast;
	private boolean backAndForth;
	
	// -- private instance variables --

	private static final String REGULAR_STATUS =
		"Press ESC to terminate. Pressing P toggles pause on and off.";
	private static final String PAUSED_STATUS =
		"Animation paused. Press P to continue or ESC to terminate.";
	private static final String DONE_STATUS =
		"Animation terminated";
	private Display currDisplay;
	private long first;
	private long last;
	private boolean userHasQuit;
	private boolean pause;
	private EventSubscriber<KyPressedEvent> kyPressSubscriber;
	private EventSubscriber<DisplayDeletedEvent> displaySubscriber;

	// -- public interface --

	public AnimateAlongAxis() {
		final DisplayService displayService = ImageJ.get(DisplayService.class);
		currDisplay = displayService.getActiveDisplay();
		if (currDisplay == null) return;
		dataset = ImageJ.get(DisplayService.class).getActiveDataset(currDisplay);
		if (dataset == null) return;
		
		final DefaultModuleItem<String> name =
			new DefaultModuleItem<String>(this, NAME_KEY, String.class);
		List<Axis> datasetAxes = Arrays.asList(dataset.getAxes());
		ArrayList<String> choices = new ArrayList<String>();
		for (Axis candidateAxis : Axes.values()) {
			if ((candidateAxis == Axes.X) || (candidateAxis == Axes.Y)) continue;
			if (datasetAxes.contains(candidateAxis))
				choices.add(candidateAxis.getLabel());
		}
		name.setChoices(choices);
		addInput(name);
		
		final DefaultModuleItem<Long> firstPos =
			new DefaultModuleItem<Long>(this, FIRST_POS_KEY, Long.class);
		firstPos.setMinimumValue(1L);
		// TODO - set max somehow based upon dataset's dimension along axis
		addInput(firstPos);
		
		final DefaultModuleItem<Long> lastPos =
			new DefaultModuleItem<Long>(this, LAST_POS_KEY, Long.class);
		lastPos.setMinimumValue(1L);
		// TODO - set max somehow based upon dataset's dimension along axis
		addInput(lastPos);
		
		final DefaultModuleItem<Double> framesPerSec =
			new DefaultModuleItem<Double>(this, FPS_KEY, Double.class);
		framesPerSec.setMinimumValue(0.1);
		framesPerSec.setMaximumValue(1000.0);
		addInput(framesPerSec);
		
		final DefaultModuleItem<Boolean> bf =
			new DefaultModuleItem<Boolean>(this, BACK_FORTH_KEY, Boolean.class);
		addInput(bf);
	}
	
	/**
	 * Runs an animation along the currently chosen axis repeatedly until ESC
	 * has been pressed by user
	 */
	@Override
	public void run() {
		if (currDisplay == null || dataset == null) return;
		harvestInputs();
		Axis axis = Axes.get(axisName);
		int axisIndex = dataset.getImgPlus().getAxisIndex(axis);
		if (axisIndex < 0) return;
		long totalHyperplanes = dataset.getImgPlus().dimension(axisIndex);
		subscribeToEvents();
		setFirstAndLast(totalHyperplanes);
		animateAlongAxis(currDisplay, axis, totalHyperplanes);
		unsubscribeFromEvents();
	}
	
	// -- private interface --

	private void harvestInputs() {
		final Map<String, Object> inputs = getInputs();
		axisName = (String) inputs.get(NAME_KEY);
		oneBasedFirst = (Long) inputs.get(FIRST_POS_KEY);
		oneBasedLast = (Long) inputs.get(LAST_POS_KEY);
		fps = (Double) inputs.get(FPS_KEY);
		backAndForth = (Boolean) inputs.get(BACK_FORTH_KEY);
	}
	
	/**
	 * Sets the zero-based indices of the first and last frames */
	private void setFirstAndLast(long totalHyperplanes) {
		first = Math.min(oneBasedFirst, oneBasedLast) - 1;
		last = Math.max(oneBasedFirst, oneBasedLast) - 1;
		if (first < 0) first = 0;
		if (last > totalHyperplanes-1) last = totalHyperplanes-1;
	}

	/**
	 * Do the actual animation. generates multiple AxisPositionEvents */
	private void animateAlongAxis(Display display, Axis axis, long total) {
		Events.publish(new StatusEvent(REGULAR_STATUS));
		Events.publish(
			new AxisPositionEvent(display, axis, first, total, false));
		int increment = 1;
		long currPos = first;
		long delta = 1;
		boolean isRelative = true;
		userHasQuit = false;
		pause = false;
		while (!userHasQuit) {
			if (pause) {
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					// do nothing
				}
				continue;
			}
			// reached right end
			if ((increment > 0) && (currPos == last)) {
				if (!backAndForth) {
					isRelative = false;
					delta = first;
					currPos = first;
				}
				else {
					increment = -increment;
					isRelative = true;
					delta = -1;
					currPos--;
				}
			}
			// reached left end
			else if ((increment < 0) && (currPos == first)) {
				if (!backAndForth) {
					isRelative = false;
					delta = last;
					currPos = last;
				}
				else {
					increment = -increment;
					isRelative = true;
					delta = +1;
					currPos++;
				}
			}
			else {  // somewhere in the middle
				isRelative = true;
				if (increment > 0) {
					delta = +1;
					currPos++;
				}
				else {  // increment < 0
					delta = -1;
					currPos--;
				}
			}

			Events.publish(
				new AxisPositionEvent(display, axis, delta, total, isRelative));
			
			try {
				Thread.sleep((long)(1000/fps));
			} catch(Exception e) {
				// do nothing
			}
		}
		Events.publish(new StatusEvent(DONE_STATUS));
	}
	
	/**
	 * Subscribes to events that will track when the user has decided to quit */
	@SuppressWarnings("synthetic-access")
	private void subscribeToEvents() {
		kyPressSubscriber = new EventSubscriber<KyPressedEvent>() {
			@Override
			public void onEvent(KyPressedEvent event) {
				if (event.getDisplay() != currDisplay) return;
				if (event.getCode() == KeyEvent.VK_ESCAPE)
					userHasQuit = true;
				else if (event.getCode() == KeyEvent.VK_P) {
					pause = !pause;
					if (pause)
						Events.publish(new StatusEvent(PAUSED_STATUS));
					else
						Events.publish(new StatusEvent(REGULAR_STATUS));
				}
			}
		};
		Events.subscribe(KyPressedEvent.class, kyPressSubscriber);
		
		displaySubscriber = new EventSubscriber<DisplayDeletedEvent>() {
			@Override
			public void onEvent(DisplayDeletedEvent event) {
				if (event.getObject() == currDisplay)
					userHasQuit = true;
			}
		};
		Events.subscribe(DisplayDeletedEvent.class, displaySubscriber);
	}

	/**
	 * Unsubscribes from events. this keeps IJ2 from maintaining dangling
	 *  references to obsolete event listeners
	 */
	private void unsubscribeFromEvents() {
		Events.unsubscribe(KyPressedEvent.class, kyPressSubscriber);
		Events.unsubscribe(DisplayDeletedEvent.class, displaySubscriber);
	}
}
