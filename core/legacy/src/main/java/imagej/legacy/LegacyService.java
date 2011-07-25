//
// LegacyService.java
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

package imagej.legacy;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import imagej.AbstractService;
import imagej.ImageJ;
import imagej.Service;
import imagej.data.Dataset;
import imagej.display.Display;
import imagej.display.DisplayService;
import imagej.display.event.DisplayActivatedEvent;
import imagej.display.event.key.KyPressedEvent;
import imagej.display.event.key.KyReleasedEvent;
import imagej.event.EventService;
import imagej.event.EventSubscriber;
import imagej.event.OptionsChangedEvent;
import imagej.legacy.patches.FunctionsMethods;
import imagej.util.Log;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for working with legacy ImageJ 1.x.
 * <p>
 * The legacy service overrides the behavior of various IJ1 methods, inserting
 * seams so that (e.g.) the modern UI is aware of IJ1 events as they occur.
 * </p>
 * <p>
 * It also maintains an image map between IJ1 {@link ImagePlus} objects and IJ2
 * {@link Dataset}s.
 * </p>
 * <p>
 * In this fashion, when a legacy plugin is executed on a {@link Dataset}, the
 * service transparently translates it into an {@link ImagePlus}, and vice
 * versa, enabling backward compatibility with legacy plugins.
 * </p>
 * 
 * @author Curtis Rueden
 */
@Service
public final class LegacyService extends AbstractService {

	static {
		new LegacyInjector().injectHooks();
	}

	private final EventService eventService;
	private final DisplayService displayService;

	/** Mapping between modern and legacy image data structures. */
	private LegacyImageMap imageMap;

	/** Method of synchronizing IJ2 & IJ1 options. */
	private OptionsSynchronizer optionsSynchronizer;

	/** Maintain list of subscribers, to avoid garbage collection. */
	private List<EventSubscriber<?>> subscribers;

	// -- Constructors --

	public LegacyService() {
		// NB: Required by SezPoz.
		super(null);
		throw new UnsupportedOperationException();
	}

	public LegacyService(final ImageJ context, final EventService eventService,
		final DisplayService displayService)
	{
		super(context);
		this.eventService = eventService;
		this.displayService = displayService;
	}

	// -- LegacyService methods --

	public LegacyImageMap getImageMap() {
		return imageMap;
	}

	/**
	 * Indicates to the service that the given {@link ImagePlus} has changed as
	 * part of a legacy plugin execution.
	 */
	public void legacyImageChanged(final ImagePlus imp) {
		// CTR FIXME rework static InsideBatchDrawing logic?
		if (FunctionsMethods.InsideBatchDrawing > 0) return;
		// record resultant ImagePlus as a legacy plugin output
		LegacyOutputTracker.getOutputImps().add(imp);
	}

	/**
	 * Ensures that the currently active {@link ImagePlus} matches the currently
	 * active {@link Display}. Does not perform any harmonization.
	 */
	public void syncActiveImage() {
		final Display activeDisplay = displayService.getActiveDisplay();
		final ImagePlus activeImagePlus = imageMap.lookupImagePlus(activeDisplay);
		WindowManager.setTempCurrentImage(activeImagePlus);
	}

	// -- IService methods --

	@Override
	public void initialize() {
		imageMap = new LegacyImageMap();
		optionsSynchronizer = new OptionsSynchronizer();

		// initialize legacy ImageJ application
		try {
			new ij.ImageJ(ij.ImageJ.NO_SHOW);
		}
		catch (final Throwable t) {
			Log.warn("Failed to instantiate IJ1.", t);
		}

		// TODO - FIXME
		// call optionsSynchronizer.update() here? Need to determine when the
		// IJ2 settings file has been read/initialized and then call update() once.

		subscribeToEvents();
	}

	// -- Helper methods --

	@SuppressWarnings("synthetic-access")
	private void subscribeToEvents() {
		subscribers = new ArrayList<EventSubscriber<?>>();

		// keep the active legacy ImagePlus in sync with the active modern Display
		final EventSubscriber<DisplayActivatedEvent> displayActivatedSubscriber =
			new EventSubscriber<DisplayActivatedEvent>() {

				@Override
				public void onEvent(final DisplayActivatedEvent event) {
					syncActiveImage();
				}
			};
		subscribers.add(displayActivatedSubscriber);
		eventService.subscribe(DisplayActivatedEvent.class,
			displayActivatedSubscriber);

		final EventSubscriber<OptionsChangedEvent> optionSubscriber =
			new EventSubscriber<OptionsChangedEvent>() {

				@Override
				public void onEvent(final OptionsChangedEvent event) {
					optionsSynchronizer.update();
				}

			};
		subscribers.add(optionSubscriber);
		eventService.subscribe(OptionsChangedEvent.class, optionSubscriber);

		// TODO - FIXME remove AWT dependency when we have implemented our own
		// KyEvent constants

		final EventSubscriber<KyPressedEvent> pressSubscriber =
			new EventSubscriber<KyPressedEvent>() {

				@Override
				public void onEvent(final KyPressedEvent event) {
					final int code = event.getCode();
					if (code == KeyEvent.VK_SPACE) IJ.setKeyDown(KeyEvent.VK_SPACE);
					if (code == KeyEvent.VK_ALT) IJ.setKeyDown(KeyEvent.VK_ALT);
					if (code == KeyEvent.VK_SHIFT) IJ.setKeyDown(KeyEvent.VK_SHIFT);
					if (code == KeyEvent.VK_CONTROL) IJ.setKeyDown(KeyEvent.VK_CONTROL);
					if ((IJ.isMacintosh()) && (code == KeyEvent.VK_META)) IJ
						.setKeyDown(KeyEvent.VK_CONTROL);
				}
			};
		subscribers.add(pressSubscriber);
		eventService.subscribe(KyPressedEvent.class, pressSubscriber);

		// TODO - FIXME remove AWT dependency when we have implemented our own
		// KyEvent constants

		final EventSubscriber<KyReleasedEvent> releaseSubscriber =
			new EventSubscriber<KyReleasedEvent>() {

				@Override
				public void onEvent(final KyReleasedEvent event) {
					final int code = event.getCode();
					if (code == KeyEvent.VK_SPACE) IJ.setKeyUp(KeyEvent.VK_SPACE);
					if (code == KeyEvent.VK_ALT) IJ.setKeyUp(KeyEvent.VK_ALT);
					if (code == KeyEvent.VK_SHIFT) IJ.setKeyUp(KeyEvent.VK_SHIFT);
					if (code == KeyEvent.VK_CONTROL) IJ.setKeyUp(KeyEvent.VK_CONTROL);
					if ((IJ.isMacintosh()) && (code == KeyEvent.VK_CONTROL)) IJ
						.setKeyUp(KeyEvent.VK_CONTROL);
				}
			};
		subscribers.add(releaseSubscriber);
		eventService.subscribe(KyReleasedEvent.class, releaseSubscriber);
	}
}
