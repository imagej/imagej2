/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
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

package imagej.core.plugins.axispos;

import imagej.ImageJ;
import imagej.data.Data;
import imagej.data.display.ImageDisplay;
import imagej.data.event.DataRestructuredEvent;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.event.StatusEvent;
import imagej.ext.KeyCode;
import imagej.ext.display.Display;
import imagej.ext.display.event.DisplayDeletedEvent;
import imagej.ext.display.event.input.KyPressedEvent;
import imagej.service.AbstractService;
import imagej.service.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for working with {@link Animation}s.
 * 
 * @author Curtis Rueden
 */
@Service
public class AnimationService extends AbstractService {

	private static final String STARTED_STATUS =
		"Animation started. Press '\\' or ESC to stop.";
	private static final String STOPPED_STATUS =
		"Animation stopped. Press '\\' to resume.";
	private static final String ALL_STOPPED_STATUS = "All animations stopped.";

	private final EventService eventService;

	private final Map<ImageDisplay, Animation> animations =
		new ConcurrentHashMap<ImageDisplay, Animation>();

	// -- Constructors --

	public AnimationService() {
		// NB: Required by SezPoz.
		super(null);
		throw new UnsupportedOperationException();
	}

	public AnimationService(final ImageJ context, final EventService eventService)
	{
		super(context);
		this.eventService = eventService;

		subscribeToEvents(eventService);
	}

	// -- AnimationService methods --

	public EventService getEventService() {
		return eventService;
	}

	/** Toggles animation for the given {@link ImageDisplay}. */
	public void toggle(final ImageDisplay display) {
		if (getAnimation(display).isActive()) stop(display);
		else start(display);
	}

	/** Starts animation for the given {@link ImageDisplay}. */
	public void start(final ImageDisplay display) {
		getAnimation(display).start();
		eventService.publish(new StatusEvent(STARTED_STATUS));
	}

	/** Stops animation for the given {@link ImageDisplay}. */
	public void stop(final ImageDisplay display) {
		final Animation animation = animations.get(display);
		if (animation != null) {
			animation.stop();
			eventService.publish(new StatusEvent(STOPPED_STATUS));
		}
	}

	/** Stops all animations. */
	public void stopAll() {
		for (final Animation animation : animations.values()) {
			animation.stop();
		}
		eventService.publish(new StatusEvent(ALL_STOPPED_STATUS));
	}

	/** Gets the given {@link ImageDisplay}'s corresponding {@link Animation}. */
	public Animation getAnimation(final ImageDisplay display) {
		Animation animation = animations.get(display);
		if (animation == null) {
			// animation did not already exist; create it
			animation = new Animation(display);
			animations.put(display, animation);
		}
		return animation;
	}

	// -- Event handlers --

	/** Stops animation if ESC key is pressed. */
	@EventHandler
	protected void onEvent(final KyPressedEvent event) {
		final ImageDisplay imageDisplay = toImageDisplay(event.getDisplay());
		if (imageDisplay == null) return;
		if (event.getCode() == KeyCode.ESCAPE) stop(imageDisplay);
	}

	/** Stops animation if display has been deleted. */
	@EventHandler
	protected void onEvent(final DisplayDeletedEvent event) {
		final ImageDisplay imageDisplay = toImageDisplay(event.getObject());
		if (imageDisplay == null) return;
		stop(imageDisplay);
		animations.remove(imageDisplay);
	}

	/** Stops animation of displays whose {@link Data} have been restructured. */
	@EventHandler
	protected void onEvent(final DataRestructuredEvent event) {
		final Data data = event.getObject();
		for (final Animation animation : animations.values()) {
			final ImageDisplay display = animation.getDisplay();
			if (display.containsData(data)) stop(display);
		}
	}

	// -- Helper methods --

	private ImageDisplay toImageDisplay(final Display<?> display) {
		if (!(display instanceof ImageDisplay)) return null;
		return (ImageDisplay) display;
	}

}
