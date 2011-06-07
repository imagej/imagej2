//
// JHotDrawAdapterFinder.java
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

package imagej.ui.swing.tools.roi;

import imagej.data.roi.Overlay;
import imagej.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import org.jhotdraw.draw.Figure;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

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
		// NB: prevent instantiation of utility class.
	}

	private void initialize() {
		adapters = new ArrayList<IJHotDrawOverlayAdapter>();
		for (final IndexItem<JHotDrawOverlayAdapter, IJHotDrawOverlayAdapter> indexItem :
			Index.load(JHotDrawOverlayAdapter.class, IJHotDrawOverlayAdapter.class))
		{
			try {
				final IJHotDrawOverlayAdapter adapter = indexItem.instance();
				adapter.setPriority(indexItem.annotation().priority());
				adapters.add(adapter);
				Log.info("Found JHotDraw adapter: " + adapter);
			}
			catch (final InstantiationException e) {
				Log.warn("Failed to load " + indexItem.className(), e);
			}
		}
	}

	/**
	 * Get some adapter for the given overlay
	 * 
	 * @param overlay - the overlay that requires an adapter
	 * @return any adapter that supports the overlay
	 * 
	 * @throws UnsupportedOperationException
	 */
	public static IJHotDrawOverlayAdapter getAdapterForOverlay(final Overlay overlay) throws UnsupportedOperationException {
		ArrayList<IJHotDrawOverlayAdapter> c = new ArrayList<IJHotDrawOverlayAdapter>(getAdaptersForOverlay(overlay));
		if (c.isEmpty())
			throw new UnsupportedOperationException("Could not find adapter for " + overlay.getClass().getName());
		Collections.sort(c, new Comparator<IJHotDrawOverlayAdapter>() {

			@Override
			public int compare(IJHotDrawOverlayAdapter o1,
					IJHotDrawOverlayAdapter o2) {
				// Sort in reverse order
				return new Integer(o2.getPriority()).compareTo(o1.getPriority());
			}
		});
		return c.get(0);
	}
	/**
	 * Get some adapter for the given overlay
	 * 
	 * @param overlay - the overlay that requires an adapter
	 * @param figure - the figure to be adapted to the overlay
	 * @return any adapter that supports the overlay adapted to the figure
	 * 
	 * @throws UnsupportedOperationException
	 */
	public static IJHotDrawOverlayAdapter getAdapterForOverlay(final Overlay overlay, final Figure figure) throws UnsupportedOperationException {
		ArrayList<IJHotDrawOverlayAdapter> c = new ArrayList<IJHotDrawOverlayAdapter>(getAdaptersForOverlay(overlay, figure));
		if (c.isEmpty())
			throw new UnsupportedOperationException("Could not find adapter for " + overlay.getClass().getName());
		Collections.sort(c, new Comparator<IJHotDrawOverlayAdapter>() {

			@Override
			public int compare(IJHotDrawOverlayAdapter o1,
					IJHotDrawOverlayAdapter o2) {
				// Sort in reverse order
				return new Integer(o2.getPriority()).compareTo(o1.getPriority());
			}
		});
		return c.get(0);
	}
	/**
	 * Get the adapters capable of handling a given overlay
	 * 
	 * @param overlay - the overlay to adapt
	 * @return a collection of all adapters capable of handling the overlay
	 */
	public static Collection<IJHotDrawOverlayAdapter> getAdaptersForOverlay(
			final Overlay overlay) {
		return getAdaptersForOverlay(overlay, null);
	}
	/**
	 * Returns all adapters capable of handling a given overlay / figure combination.
	 * 
	 * @param overlay - the overlay to be adapted to the JHotDraw GUI
	 * @param figure - the figure to be associated with the overlay
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
