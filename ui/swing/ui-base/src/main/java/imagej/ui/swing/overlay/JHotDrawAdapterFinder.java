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

package imagej.ui.swing.overlay;

import imagej.data.overlay.Overlay;
import imagej.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

import org.jhotdraw.draw.Figure;

/**
 * The JHotDrawAdapterFinder finds all JHotDrawAdapters that can adapt to a
 * particular ROI.
 * 
 * @author Lee Kamentsky
 */
public class JHotDrawAdapterFinder {

	private static JHotDrawAdapterFinder theFinder;

	private ArrayList<IJHotDrawOverlayAdapter> adapters;

	private JHotDrawAdapterFinder() {
		// prevent instantiation of utility class
	}

	private void initialize() {
		adapters = new ArrayList<IJHotDrawOverlayAdapter>();
		for (final IndexItem<JHotDrawOverlayAdapter, IJHotDrawOverlayAdapter> indexItem : Index
			.load(JHotDrawOverlayAdapter.class, IJHotDrawOverlayAdapter.class))
		{
			try {
				final IJHotDrawOverlayAdapter adapter = indexItem.instance();
				adapter.setPriority(indexItem.annotation().priority());
				adapters.add(adapter);
			}
			catch (final InstantiationException e) {
				Log.warn("Failed to load " + indexItem.className(), e);
			}
		}
		Log.info("Found " + adapters.size() + " JHotDraw adapters.");
	}

	/**
	 * Get some adapter for the given overlay.
	 * 
	 * @param overlay the overlay that requires an adapter
	 * @return any adapter that supports the overlay
	 * @throws UnsupportedOperationException
	 */
	public static IJHotDrawOverlayAdapter getAdapterForOverlay(
		final Overlay overlay) throws UnsupportedOperationException
	{
		final ArrayList<IJHotDrawOverlayAdapter> c =
			new ArrayList<IJHotDrawOverlayAdapter>(getAdaptersForOverlay(overlay));
		if (c.isEmpty()) throw new UnsupportedOperationException(
			"Could not find adapter for " + overlay.getClass().getName());
		return Collections.min(c, new Comparator<IJHotDrawOverlayAdapter>() {

			@Override
			public int compare(final IJHotDrawOverlayAdapter o1,
				final IJHotDrawOverlayAdapter o2)
			{
				// Sort in reverse order
				return new Integer(o2.getPriority()).compareTo(o1.getPriority());
			}
		});
	}

	/**
	 * Get some adapter for the given overlay.
	 * 
	 * @param overlay the overlay that requires an adapter
	 * @param figure the figure to be adapted to the overlay
	 * @return any adapter that supports the overlay adapted to the figure
	 * @throws UnsupportedOperationException
	 */
	public static IJHotDrawOverlayAdapter getAdapterForOverlay(
		final Overlay overlay, final Figure figure)
		throws UnsupportedOperationException
	{
		final ArrayList<IJHotDrawOverlayAdapter> c =
			new ArrayList<IJHotDrawOverlayAdapter>(getAdaptersForOverlay(overlay,
				figure));
		if (c.isEmpty()) throw new UnsupportedOperationException(
			"Could not find adapter for " + overlay.getClass().getName());
		return Collections.min(c, new Comparator<IJHotDrawOverlayAdapter>() {

			@Override
			public int compare(final IJHotDrawOverlayAdapter o1,
				final IJHotDrawOverlayAdapter o2)
			{
				return new Integer(o2.getPriority()).compareTo(o1.getPriority());
			}
		});
	}

	/**
	 * Get the adapters capable of handling a given overlay.
	 * 
	 * @param overlay the overlay to adapt
	 * @return a collection of all adapters capable of handling the overlay
	 */
	public static Collection<IJHotDrawOverlayAdapter> getAdaptersForOverlay(
		final Overlay overlay)
	{
		return getAdaptersForOverlay(overlay, null);
	}

	/**
	 * Returns all adapters capable of handling a given overlay / figure
	 * combination.
	 * 
	 * @param overlay the overlay to be adapted to the JHotDraw GUI
	 * @param figure the figure to be associated with the overlay
	 * @return collection of valid adapters
	 */
	public static Collection<IJHotDrawOverlayAdapter> getAdaptersForOverlay(
		final Overlay overlay, final Figure figure)
	{
		if (theFinder == null) {
			theFinder = new JHotDrawAdapterFinder();
			theFinder.initialize();
		}
		final ArrayList<IJHotDrawOverlayAdapter> result =
			new ArrayList<IJHotDrawOverlayAdapter>();
		for (final IJHotDrawOverlayAdapter adapter : theFinder.adapters) {
			if (adapter.supports(overlay, figure)) result.add(adapter);
		}
		return result;
	}

	/** Gets all of the discovered adapters. */
	public static Collection<IJHotDrawOverlayAdapter> getAllAdapters() {
		if (theFinder == null) {
			theFinder = new JHotDrawAdapterFinder();
			theFinder.initialize();
		}
		return Collections.unmodifiableCollection(theFinder.adapters);
	}

}
