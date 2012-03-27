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

package imagej.ui;

import imagej.ImageJ;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.event.EventSubscriber;
import imagej.platform.event.AppQuitEvent;
import imagej.util.Prefs;

import java.util.List;

/**
 * Abstract superclass for {@link UserInterface} implementations.
 * 
 * @author Curtis Rueden
 */
public abstract class AbstractUserInterface implements UserInterface {

	private static final String PREF_FIRST_RUN = "firstRun-" + ImageJ.VERSION;
	private static final String LAST_X = "lastXLocation";
	private static final String LAST_Y = "lastYLocation";

	private UIService uiService;

	@SuppressWarnings("unused")
	private List<EventSubscriber<?>> subscribers;

	// -- UserInterface methods --

	@Override
	public void initialize(final UIService service) {
		uiService = service;
		createUI();
		displayReadme();
	}

	@Override
	public UIService getUIService() {
		return uiService;
	}

	@Override
	public void processArgs(final String[] args) {
		// TODO
	}

	@Override
	public Desktop getDesktop() {
		return null;
	}

	@Override
	public ApplicationFrame getApplicationFrame() {
		return null;
	}

	@Override
	public ToolBar getToolBar() {
		return null;
	}

	@Override
	public StatusBar getStatusBar() {
		return null;
	}

	// -- Internal methods --

	/**
	 * Subclasses override to control UI creation. They must also call
	 * super.createUI() after creating the {@link ApplicationFrame} but before
	 * showing it (assuming the UI has an {@link ApplicationFrame}).
	 */
	protected void createUI() {
		subscribers = getEventService().subscribe(this);
		restoreLocation();
	}

	/** Persists the application frame's current location. */
	protected void saveLocation() {
		final ApplicationFrame appFrame = getApplicationFrame();
		if (appFrame != null) {
			Prefs.put(getClass(), LAST_X, appFrame.getLocationX());
			Prefs.put(getClass(), LAST_Y, appFrame.getLocationY());
		}
	}

	/** Restores the application frame's current location. */
	protected void restoreLocation() {
		final ApplicationFrame appFrame = getApplicationFrame();
		if (appFrame != null) {
			final int lastX = Prefs.getInt(getClass(), LAST_X, 0);
			final int lastY = Prefs.getInt(getClass(), LAST_Y, 0);
			appFrame.setLocation(lastX, lastY);
		}
	}

	// -- Event handlers --

	// TODO - migrate event handling logic to UIService

	@EventHandler
	public void onEvent(@SuppressWarnings("unused") final AppQuitEvent event) {
		saveLocation();
	}

	// -- Helper methods --

	protected EventService getEventService() {
		return uiService.getEventService();
	}

	/** Shows the readme, if this is the first time ImageJ has run. */
	private void displayReadme() {
		final String firstRun = Prefs.get(getClass(), PREF_FIRST_RUN);
		if (firstRun != null) return;
		Prefs.put(getClass(), PREF_FIRST_RUN, false);
		uiService.getPluginService().run(ShowReadme.class);
	}

}
