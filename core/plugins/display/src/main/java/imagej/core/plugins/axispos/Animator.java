//
// Animator.java
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

import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.imglib2.img.Axis;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.event.DatasetRestructuredEvent;
import imagej.display.Display;
import imagej.display.DisplayService;
import imagej.display.ImageDisplay;
import imagej.display.event.AxisPositionEvent;
import imagej.display.event.DisplayDeletedEvent;
import imagej.display.event.key.KyPressedEvent;
import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.event.StatusEvent;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;

/**
 * This class is responsible for running animations of stack data. One can
 * start, pause, resume, and stop animations via the '\' and ESC keys.
 * Animations can be modified during execution by running the
 * {@link AnimatorOptionsPlugin}.
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = { @Menu(label = "Image", mnemonic = 'i'),
	@Menu(label = "Stacks", mnemonic = 's'),
	@Menu(label = "Tools", mnemonic = 't'),
	@Menu(label = "Start Animation", accelerator = "BACK_SLASH") })
public class Animator implements ImageJPlugin {

	// - parameters - one per Animator instance --
	
	@Parameter
	private ImageDisplay activeImageDisplay;

	// -- private constants --
	
	private static final String REGULAR_STATUS =
		"Press ESC to terminate. Pressing '\' pauses.";
	private static final String PAUSED_STATUS =
		"Animation paused. Press '\' to continue or ESC to terminate.";
	private static final String DONE_STATUS = "Animation terminated.";

	// -- static variables - only one for all Animator instances --
	
	private static EventSubscriber<KyPressedEvent> KEYPRESS_SUBSCRIBER;
	private static EventSubscriber<DisplayDeletedEvent> DISPLAY_SUBSCRIBER;
	private static EventSubscriber<DatasetRestructuredEvent> RESTRUCTURE_SUBSCRIBER;
	private static final Map<ImageDisplay,Animation> ANIMATIONS =
		new ConcurrentHashMap<ImageDisplay,Animation>();
	private static final Map<ImageDisplay,AnimatorOptions> OPTIONS =
		new ConcurrentHashMap<ImageDisplay,AnimatorOptions>();

	// -- package access static methods --
	
	/** Called from AnimatorOptionsPlugin */
	static AnimatorOptions getOptions(ImageDisplay display) {
		AnimatorOptions options = OPTIONS.get(display);
		if (options == null) {
			options = defaultOptions(display);
			OPTIONS.put(display, options);
		}
		return options;
	}
	
	/** Called from AnimatorOptionsPlugin */
	static void optionsUpdated(ImageDisplay display) {
		Animation a = ANIMATIONS.get(display);
		if (a != null) {
			a.pause();
			a.initFromOptions();
			a.resume();
		}
	}
	
	/** Terminate a single animation. Called from StopAnimation plugin */
	static void terminateAnimation(ImageDisplay display) {
		Animation a = ANIMATIONS.get(display);
		if (a != null)
				a.stop();
	}
	
	/** Terminate all animations. Called from StopAllAnimations plugin */
	static void terminateAll() {
		DisplayService service = ImageJ.get(DisplayService.class);
		List<Display> displays = service.getDisplays();
		for (Display d : displays) {
			Animation a = ANIMATIONS.get(d);
			if (a != null)
				a.stop();
		}
	}
	
	// -- public instance methods --
	
	public Animator() {
		synchronized (ANIMATIONS) {
			if (KEYPRESS_SUBSCRIBER == null)
				subscribeToEvents();
		}
	}

	/** 
	 * Starts a new animation or modifies an existing animation. Only does any
	 * work when the active display's {@link Dataset} has 3 or more axes.
	 */
	@Override
	public void run() {
		Dataset ds =
			ImageJ.get(DisplayService.class).getActiveDataset(activeImageDisplay);
		if (ds == null) return;
		if (ds.getDims().length <= 2) return;
		Animation a = ANIMATIONS.get(activeImageDisplay);
		if (a == null) {
			a = new Animation(activeImageDisplay);
			a.start();
		}
		else if (a.isPaused())
			a.resume();
		else
			a.pause();
	}

	// -- private helpers --

	/**
	 * Creates an AnimatorOptions structure populated with default values
	 * appropriate to the input {@link ImageDisplay}.
	 */
	private static AnimatorOptions defaultOptions(ImageDisplay display) {
	  // NOTE - elsewhere we've been careful to make sure this method never
		//   gets called for displays whose dataset has 2 or fewer dimensions.
		//   So can safely grab Dataset values for axis index 2.
		Dataset ds = ImageJ.get(DisplayService.class).getActiveDataset(display);
		long total = ds.getImgPlus().dimension(2);
		Axis axis = ds.getAxes()[2];
		double fps = 8;
		long first =  0;
		long last =  total-1;
		boolean backAndForth =  false;
		
		// return options
		return new AnimatorOptions(axis, fps, first, last, total, backAndForth);
	}

	/**
	 * Subscribes to events that are of interest to the Animator. ESC key press
	 * will terminate a running animation. So will a {@link DisplayDeletedEvent}
	 * and a {@link DatasetRestructuredEvent}.
	 */
	@SuppressWarnings("synthetic-access")
	private static void subscribeToEvents() {
		KEYPRESS_SUBSCRIBER = new EventSubscriber<KyPressedEvent>() {

			@Override
			public void onEvent(final KyPressedEvent event) {
				Animation a = ANIMATIONS.get(event.getDisplay());
				if ((a != null) && (event.getCode() == KeyEvent.VK_ESCAPE))
					a.stop();
			}
		};
		Events.subscribe(KyPressedEvent.class, KEYPRESS_SUBSCRIBER);

		DISPLAY_SUBSCRIBER = new EventSubscriber<DisplayDeletedEvent>() {

			@Override
			public void onEvent(final DisplayDeletedEvent event) {
				Animation a = ANIMATIONS.get(event.getObject());
				if (a != null)
					a.stop();
			}
		};
		Events.subscribe(DisplayDeletedEvent.class, DISPLAY_SUBSCRIBER);

		RESTRUCTURE_SUBSCRIBER = new EventSubscriber<DatasetRestructuredEvent>() {

			@Override
			public void onEvent(DatasetRestructuredEvent event) {
				// NOTE - this event might get captured wel after an animation update
				// took place with a modified Dataset.
				List<ImageDisplay> displays =
					ImageJ.get(DisplayService.class).getDisplays(event.getObject());
				for (ImageDisplay display : displays) {
					Animation a = ANIMATIONS.get(display);
					if (a != null)
						a.stop();
				}
			}
		};
		Events.subscribe(DatasetRestructuredEvent.class, RESTRUCTURE_SUBSCRIBER);
	}

	/**
	 * The Animation class takes care of running an animation along an axis.
	 * Multiple animations can be running concurrently. Each animation runs in
	 * its own thread. Running animations can be modified through the
	 * {@link AnimatorOptionsPlugin}.
	 */
	@SuppressWarnings("synthetic-access")
	private class Animation implements Runnable {
		
		private ImageDisplay display;
		private boolean quitting;
		private boolean paused;

		private Axis axis;
		private long first;
		private long last;
		private long total;
		private double fps;
		private boolean backAndForth;
		
		private long increment;
		private long currPos;
		private long delta;
		private boolean isRelative;

		/** Create an Animation on an ImageDisplay. There should only be one
		 * Animation per ImageDisplay.
		 */
		Animation(ImageDisplay display) {
			this.display = display;
			this.paused = false;
			this.quitting = false;
		}

		/**
		 * Sets the Animation's iteration variables from the AnimatorOptions
		 * associated with this Animation. Can be called multiple times for
		 * an Animation via the AnimatorOptionsPlugin indirectly. 
		 */
		void initFromOptions() {
			AnimatorOptions options = OPTIONS.get(display);

			axis = options.axis;
			first = options.first;
			last = options.last;
			total = options.total;
			fps = options.fps;
			backAndForth = options.backAndForth;
			
			increment = 1;
			currPos = options.first;
			delta = 1;
			isRelative = true;

			Events.publish(new AxisPositionEvent(display, axis, first, total, false));
		}
		
		/**
		 * Starts an Animation
		 */
		void start() {
			paused = false;
			ANIMATIONS.put(display, this);
			if (OPTIONS.get(display) == null)
				OPTIONS.put(display, defaultOptions(display));
			new Thread(this).start();
			Events.publish(new StatusEvent(REGULAR_STATUS));
		}
		
		/**
		 * Pauses an Animation
		 */
		void pause() {
			paused = true;
			Events.publish(new StatusEvent(PAUSED_STATUS));
		}
		
		/**
		 * Resumes a paused Animation
		 */
		void resume() {
			paused = false;
			Events.publish(new StatusEvent(REGULAR_STATUS));
		}
		
		/**
		 * Terminates an Animation
		 */
		void stop() {
			quitting = true;
			ANIMATIONS.remove(display);
			Events.publish(new StatusEvent(DONE_STATUS));
		}
		
		/**
		 * Returns true if an Animation is currently paused.
		 */
		boolean isPaused() {
			return paused;
		}

		/**
		 * This is the only public method of an Animation. Called from the
		 * Animation's own Thread.
		 */
		@Override
		public void run() {
			initFromOptions();
			while (!quitting) {
				if (paused) {
					try {
						Thread.sleep(1000);
					}
					catch (final Exception e) {
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
				else { // somewhere in the middle
					isRelative = true;
					if (increment > 0) {
						delta = +1;
						currPos++;
					}
					else { // increment < 0
						delta = -1;
						currPos--;
					}
				}

				Events.publish(
					new AxisPositionEvent(display, axis, delta, total, isRelative));

				try {
					Thread.sleep((long) (1000 / fps));
				}
				catch (final Exception e) {
					// do nothing
				}
			}
		}
	}

}
