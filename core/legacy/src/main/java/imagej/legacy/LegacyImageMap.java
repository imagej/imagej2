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

package imagej.legacy;

import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.display.ImageDisplay;
import imagej.data.overlay.Overlay;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.event.EventSubscriber;
import imagej.ext.display.event.DisplayDeletedEvent;
import imagej.legacy.translate.DefaultImageTranslator;
import imagej.legacy.translate.ImageTranslator;
import imagej.legacy.translate.LegacyUtils;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An image map between IJ1 {@link ImagePlus} objects and IJ2
 * {@link ImageDisplay}s. Because every {@link ImagePlus} has a corresponding
 * {@link ImageWindow} and vice versa, it works out best to associate each
 * {@link ImagePlus} with a {@link ImageDisplay} rather than with a
 * {@link Dataset}.
 * <p>
 * Any {@link Overlay}s present in the {@link ImageDisplay} are translated to a
 * {@link Roi} attached to the {@link ImagePlus}, and vice versa.
 * </p>
 * <p>
 * In the case of one {@link Dataset} belonging to multiple {@link ImageDisplay}
 * s, there is a separate {@link ImagePlus} for each {@link ImageDisplay}, with
 * pixels by reference.
 * </p>
 * <p>
 * In the case of multiple {@link Dataset}s in a single {@link ImageDisplay},
 * only the first {@link Dataset} is translated to the {@link ImagePlus}.
 * </p>
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public class LegacyImageMap {

	// -- Fields --

	/** Table of {@link ImagePlus} objects corresponding to {@link ImageDisplay}s. */
	private final Map<ImageDisplay, ImagePlus> imagePlusTable;

	/**
	 * Table of {@link ImageDisplay} objects corresponding to {@link ImagePlus}es.
	 */
	private final Map<ImagePlus, ImageDisplay> displayTable;

	/**
	 * The {@link ImageTranslator} to use when creating {@link ImagePlus} and
	 * {@link ImageDisplay} objects corresponding to one another.
	 */
	private final DefaultImageTranslator imageTranslator;

	/** List of event subscribers, to avoid garbage collection. */
	@SuppressWarnings("unused")
	private final List<EventSubscriber<?>> subscribers;

	// -- Constructor --

	public LegacyImageMap(final ImageJ context) {
		imagePlusTable = new ConcurrentHashMap<ImageDisplay, ImagePlus>();
		displayTable = new ConcurrentHashMap<ImagePlus, ImageDisplay>();
		imageTranslator = new DefaultImageTranslator(context);
		final EventService eventService = context.getService(EventService.class);
		subscribers = eventService.subscribe(this);
	}

	// -- LegacyImageMap methods --

	/**
	 * Gets the {@link ImageDisplay} corresponding to the given {@link ImagePlus},
	 * or null if there is no existing table entry.
	 */
	public ImageDisplay lookupDisplay(final ImagePlus imp) {
		if (imp == null) return null;
		return displayTable.get(imp);
	}

	/**
	 * Gets the {@link ImagePlus} corresponding to the given {@link ImageDisplay},
	 * or null if there is no existing table entry.
	 */
	public ImagePlus lookupImagePlus(final ImageDisplay display) {
		if (display == null) return null;
		return imagePlusTable.get(display);
	}

	/**
	 * Ensures that the given {@link ImageDisplay} has a corresponding legacy
	 * image.
	 * 
	 * @return the {@link ImagePlus} object shadowing the given
	 *         {@link ImageDisplay}, creating it if necessary using the
	 *         {@link ImageTranslator}.
	 */
	public ImagePlus registerDisplay(final ImageDisplay display) {
		ImagePlus imp = lookupImagePlus(display);
		if (imp == null) {
			// mapping does not exist; mirror display to image window
			imp = imageTranslator.createLegacyImage(display);
			addMapping(display, imp);
			// Note - we need to register ImagePlus with IJ1 also
			new ImageWindow(imp);
		}
		return imp;
	}

	/**
	 * Ensures that the given legacy image has a corresponding
	 * {@link ImageDisplay}.
	 * 
	 * @return the {@link ImageDisplay} object shadowing the given
	 *         {@link ImagePlus}, creating it if necessary using the
	 *         {@link ImageTranslator}.
	 */
	public ImageDisplay registerLegacyImage(final ImagePlus imp) {
		ImageDisplay display = lookupDisplay(imp);
		if (display == null) {
			// mapping does not exist; mirror legacy image to display
			display = imageTranslator.createDisplay(imp);
			addMapping(display, imp);
		}
		return display;
	}

	/** Removes the mapping associated with the given {@link ImageDisplay}. */
	public void unregisterDisplay(final ImageDisplay display) {
		final ImagePlus imp = lookupImagePlus(display);
		removeMapping(display, imp);
	}

	/** Removes the mapping associated with the given {@link ImagePlus}. */
	public void unregisterLegacyImage(final ImagePlus imp) {
		final ImageDisplay display = lookupDisplay(imp);
		removeMapping(display, imp);
	}

	// -- Helper methods --

	private void addMapping(final ImageDisplay display, final ImagePlus imp) {
		// System.out.println("CREATE MAPPING "+display+" to "+imp+" isComposite()="+imp.isComposite());

		// Must remove old mappings to avoid memory leaks
		// Removal is tricky for the displayTable. Without removal different
		// ImagePluses and CompositeImages can point to the same ImageDisplay. To
		// avoid a memory leak and to stay consistent in our mappings we find
		// all current mappings and remove them before inserting new ones. This
		// ensures that a ImageDisplay is only linked with one ImagePlus or
		// CompositeImage.
		imagePlusTable.remove(display);
		for (final Entry<ImagePlus, ImageDisplay> entry : displayTable.entrySet())
		{
			if (entry.getValue() == display) {
				displayTable.remove(entry.getKey());
			}
		}
		imagePlusTable.put(display, imp);
		displayTable.put(imp, display);
	}

	private void removeMapping(final ImageDisplay display, final ImagePlus imp) {
		// System.out.println("REMOVE MAPPING "+display+" to "+imp+" isComposite()="+imp.isComposite());

		if (display != null) {
			imagePlusTable.remove(display);
		}
		if (imp != null) {
			displayTable.remove(imp);
			LegacyUtils.deleteImagePlus(imp);
		}
	}

	// -- Event handlers --

	/*
	Removing this code to fix bug #835. Rely on LegacyPlugin to create
	ImagePluses as they are needed.

	@EventHandler
	protected void onEvent(final DisplayCreatedEvent event) {
		if (event.getObject() instanceof ImageDisplay) {
			registerDisplay((ImageDisplay) event.getObject());
		}
	}
	*/

	@EventHandler
	protected void onEvent(final DisplayDeletedEvent event) {

		// Need to make sure:
		// - IJ2 Windows always close when IJ1 close expected
		// Stack to Images, Split Channels, etc.
		// - No ImagePlus/Display mapping becomes a zombie in the
		// LegacyImageMap failing to get garbage collected.
		// - That IJ2 does not think IJ1 initiated the ij1.close()
		if (event.getObject() instanceof ImageDisplay) {
			final ImagePlus imp = lookupImagePlus((ImageDisplay) event.getObject());

			if (imp != null) LegacyOutputTracker.closeInitiatedByIJ2(imp);

			unregisterDisplay((ImageDisplay) event.getObject());

			if (imp != null) LegacyOutputTracker.closeCompletedByIJ2(imp);
		}
	}

}
