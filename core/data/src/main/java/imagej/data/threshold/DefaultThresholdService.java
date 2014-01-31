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
 * #L%
 */

package imagej.data.threshold;

import imagej.data.Dataset;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.OverlayService;
import imagej.data.event.OverlayDeletedEvent;
import imagej.data.overlay.Overlay;
import imagej.data.overlay.ThresholdOverlay;
import imagej.display.Display;
import imagej.display.event.DisplayDeletedEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.scijava.event.EventHandler;
import org.scijava.log.LogService;
import org.scijava.plugin.AbstractSingletonService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;

/**
 * Default service for working with thresholds.
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public class DefaultThresholdService extends
	AbstractSingletonService<ThresholdMethod> implements ThresholdService
{

	// -- parameters --

	@Parameter
	private ImageDisplayService displayService;

	@Parameter
	private OverlayService overlayService;

	@Parameter
	private LogService log;

	// -- instance variables --

	private ConcurrentHashMap<ImageDisplay, ThresholdOverlay> thresholdMap;

	private HashMap<String, ThresholdMethod> methods;

	private ArrayList<String> methodNames;

	// -- ThresholdService methods --

	@Override
	public boolean hasThreshold(final ImageDisplay display) {
		return thresholdMap().get(display) != null;
	}

	@Override
	public ThresholdOverlay getThreshold(final ImageDisplay display) {
		ThresholdOverlay overlay = thresholdMap().get(display);
		if (overlay == null) {
			final Dataset dataset = displayService.getActiveDataset(display);
			if (dataset == null) {
				throw new IllegalArgumentException(
					"expected ImageDisplay to have active dataset");
			}
			overlay = new ThresholdOverlay(getContext(), dataset);
			thresholdMap().put(display, overlay);
			display.display(overlay);
			// NOTE - the call on prev line did a rebuild() but not necessarily an
			// update(). So graphics might not be up to date! This may be a bug in
			// display code. Anyhow this next line makes sure that the display is
			// updated. This fixes the problem where you adjust threshold and in
			// dialog you delete the thresh and then go into min or max field and make
			// a change. Correct behavior is for new thresh to immediately appear.
			// Without this update() call the thresh overlay exists but doesn't get
			// displayed.
			display.update(); // TEMP HACK
		}
		return overlay;
	}

	@Override
	public void removeThreshold(final ImageDisplay display) {
		final ThresholdOverlay overlay = thresholdMap().get(display);
		if (overlay != null) {
			overlayService.removeOverlay(display, overlay);
			thresholdMap().remove(display);
		}
	}

	@Override
	public Map<String, ThresholdMethod> getThresholdMethods() {
		return Collections.unmodifiableMap(methods());
	}

	@Override
	public List<String> getThresholdMethodNames() {
		return Collections.unmodifiableList(methodNames());
	}

	@Override
	public ThresholdMethod getThresholdMethod(final String name) {
		return methods().get(name);
	}

	// -- PTService methods --

	@Override
	public Class<ThresholdMethod> getPluginType() {
		return ThresholdMethod.class;
	}

	// -- event handlers --

	@EventHandler
	protected void onEvent(final DisplayDeletedEvent evt) {
		final Display<?> display = evt.getObject();
		if (display instanceof ImageDisplay) {
			removeThreshold((ImageDisplay) display);
		}
	}

	@EventHandler
	protected void onEvent(final OverlayDeletedEvent evt) {
		final Overlay overlay = evt.getObject();
		if (overlay instanceof ThresholdOverlay) {
			for (final Entry<ImageDisplay, ThresholdOverlay> entry : thresholdMap()
				.entrySet())
			{
				if (entry.getValue() == overlay) removeThreshold(entry.getKey());
			}
		}
	}

	// -- Helper methods - lazy initialization --

	/** Gets {@link #thresholdMap}, initializing if needed. */
	private ConcurrentHashMap<ImageDisplay, ThresholdOverlay> thresholdMap() {
		if (thresholdMap == null) initThresholdMap();
		return thresholdMap;
	}

	/** Gets {@link #methods}, initializing if needed. */
	private HashMap<String, ThresholdMethod> methods() {
		if (methods == null) initMethods();
		return methods;
	}

	/** Gets {@link #methodNames}, initializing if needed. */
	private List<String> methodNames() {
		if (methodNames == null) initMethodNames();
		return methodNames;
	}

	/** Initializes {@link #thresholdMap}. */
	private synchronized void initThresholdMap() {
		if (thresholdMap != null) return; // already initialized

		final ConcurrentHashMap<ImageDisplay, ThresholdOverlay> map =
			new ConcurrentHashMap<ImageDisplay, ThresholdOverlay>();

		thresholdMap = map;
	}

	/** Initializes {@link #methods}. */
	private synchronized void initMethods() {
		if (methods != null) return; // already initialized

		final HashMap<String, ThresholdMethod> map =
			new HashMap<String, ThresholdMethod>();
		for (final ThresholdMethod method : getInstances()) {
			final String name = method.getInfo().getName();
			map.put(name, method);
		}

		methods = map;
	}

	/** Initializes {@link #methodNames}. */
	private synchronized void initMethodNames() {
		if (methodNames != null) return; // already initialized

		final ArrayList<String> list = new ArrayList<String>();
		for (final ThresholdMethod method : getInstances()) {
			final String name = method.getInfo().getName();
			list.add(name);
		}

		methodNames = list;
	}

}
