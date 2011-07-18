//
// LegacyImageMap.java
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

import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import imagej.data.Dataset;
import imagej.data.roi.Overlay;
import imagej.display.Display;
import imagej.display.event.DisplayCreatedEvent;
import imagej.display.event.DisplayDeletedEvent;
import imagej.event.EventSubscriber;
import imagej.event.Events;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An image map between IJ1 {@link ImagePlus} objects and IJ2 {@link Display}s.
 * Because every {@link ImagePlus} has a corresponding {@link ImageWindow} and
 * vice versa, it works out best to associate each {@link ImagePlus} with a
 * {@link Display} rather than with a {@link Dataset}.
 * <p>
 * Any {@link Overlay}s present in the {@link Display} are translated to a
 * {@link Roi} attached to the {@link ImagePlus}, and vice versa.
 * </p>
 * <p>
 * In the case of one {@link Dataset} belonging to multiple {@link Display}s,
 * there is a separate {@link ImagePlus} for each {@link Display}, with pixels
 * by reference.
 * </p>
 * <p>
 * In the case of multiple {@link Dataset}s in a single {@link Display}, only
 * the first {@link Dataset} is translated to the {@link ImagePlus}.
 * </p>
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public class LegacyImageMap {

	// -- Fields --

	/** Table of {@link ImagePlus} objects corresponding to {@link Display}s. */
	private final Map<Display, ImagePlus> imagePlusTable;

	/** Table of {@link Display} objects corresponding to {@link ImagePlus}es. */
	private final Map<ImagePlus, Display> displayTable;

	/**
	 * The {@link ImageTranslator} to use when creating {@link ImagePlus} and
	 * {@link Display} objects corresponding to one another.
	 */
	private final ImageTranslator imageTranslator;

	/** List of event subscribers, to avoid garbage collection. */
	private final ArrayList<EventSubscriber<?>> subscribers;

	// -- Constructor --

	public LegacyImageMap() {
		imagePlusTable = new ConcurrentHashMap<Display, ImagePlus>();
		displayTable = new ConcurrentHashMap<ImagePlus, Display>();
		imageTranslator = new DefaultImageTranslator();
		subscribers = new ArrayList<EventSubscriber<?>>();
		subscribeToEvents();
	}

	// -- LegacyImageMap methods --

	/**
	 * Gets the {@link ImageTranslator} used to create {@link ImagePlus} and
	 * {@link Display} objects linked to one another.
	 */
	public ImageTranslator getTranslator() {
		return imageTranslator;
	}

	/**
	 * Gets the {@link Display} corresponding to the given {@link ImagePlus}, or
	 * null if there is no existing table entry.
	 */
	public Display lookupDisplay(final ImagePlus imp) {
		if (imp == null) return null;
		return displayTable.get(imp);
	}

	/**
	 * Gets the {@link ImagePlus} corresponding to the given {@link Display}, or
	 * null if there is no existing table entry.
	 */
	public ImagePlus lookupImagePlus(final Display display) {
		if (display == null) return null;
		return imagePlusTable.get(display);
	}

	/**
	 * Ensures that the given {@link Display} has a corresponding legacy image.
	 * 
	 * @return the {@link ImagePlus} object shadowing the given {@link Display},
	 *         creating it if necessary using the {@link ImageTranslator}.
	 */
	public ImagePlus registerDisplay(final Display display) {
		ImagePlus imp = lookupImagePlus(display);
		if (imp == null) {
			// mapping does not exist; mirror display to image window
			imp = imageTranslator.createLegacyImage(display);
			addMapping(display, imp);
		}
		return imp;
	}

	/**
	 * Ensures that the given legacy image has a corresponding {@link Display}.
	 * 
	 * @return the {@link Display} object shadowing the given {@link ImagePlus},
	 *         creating it if necessary using the {@link ImageTranslator}.
	 */
	public Display registerLegacyImage(final ImagePlus imp) {
		Display display = lookupDisplay(imp);
		if (display == null) {
			// mapping does not exist; mirror legacy image to display
			display = imageTranslator.createDisplay(imp);
			addMapping(display, imp);
		}
		return display;
	}

	/** Removes the mapping associated with the given {@link Display}. */
	public void unregisterDisplay(final Display display) {
		final ImagePlus imp = lookupImagePlus(display);
		removeMapping(display, imp);
	}

	/** Removes the mapping associated with the given {@link ImagePlus}. */
	public void unregisterLegacyImage(final ImagePlus imp) {
		final Display display = lookupDisplay(imp);
		removeMapping(display, imp);
	}

	// -- Helper methods --

	private void addMapping(Display display, ImagePlus imp) {
		//System.out.println("CREATE MAPPING "+display+" to "+imp+" isComposite()="+imp.isComposite());
		imagePlusTable.put(display, imp);
		displayTable.put(imp, display);
	}
	
	private void removeMapping(Display display, ImagePlus imp) {
		//System.out.println("REMOVE MAPPING "+display+" to "+imp+" isComposite()="+imp.isComposite());
		if (display != null) {
			imagePlusTable.remove(display);
		}
		if (imp != null) {
			displayTable.remove(imp);
			LegacyUtils.deleteImagePlus(imp);
		}
	}
	
	private void subscribeToEvents() {
		final EventSubscriber<DisplayCreatedEvent> creationSubscriber =
			new EventSubscriber<DisplayCreatedEvent>() {

				@Override
				public void onEvent(final DisplayCreatedEvent event) {
					registerDisplay(event.getObject());
				}
			};
		subscribers.add(creationSubscriber);
		Events.subscribe(DisplayCreatedEvent.class, creationSubscriber);

		final EventSubscriber<DisplayDeletedEvent> deletionSubscriber =
			new EventSubscriber<DisplayDeletedEvent>() {

				@Override
				public void onEvent(final DisplayDeletedEvent event) {
					
					// FIXME - HACK
					
					// A hack is needed here to avoid memory leaks. This event is
					// initiated by IJ2. It eventually calls deleteImagePlus(). That
					// method closes the IJ1 window. The legacy layer thinks this is
					// a close initiated by an IJ1 plugin. It records the fact. That
					// keeps a HashSet sitting around with all ImagePluses that have
					// ever been closed. If we could track whether we were in a plugin
					// in the ImageWindowMethods::close() we could avoid recording it
					// as a closed ImagePlus there or in LegacyPutputTracker. For now
					// just remember the state of the set of closed imps and restore
					// that state after the unregisterDisplay() call.
					
					// HACK part 1
					// 
					HashSet<ImagePlus> savedStatus = new HashSet<ImagePlus>();
					savedStatus.addAll(LegacyOutputTracker.getClosedImps());
					
					// correct code
					unregisterDisplay(event.getObject());
					
					// HACK part 2
					// 
					Set<ImagePlus> closedImps = LegacyOutputTracker.getClosedImps();
					closedImps.clear();
					closedImps.addAll(savedStatus);
				}
			};
		subscribers.add(deletionSubscriber);
		Events.subscribe(DisplayDeletedEvent.class, deletionSubscriber);
	}

}
