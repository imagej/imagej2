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
import imagej.event.EventService;
import imagej.event.StatusService;
import imagej.ext.plugin.SortablePlugin;
import imagej.updater.core.UpToDate;
import imagej.updater.ui.UpdatesAvailable;
import imagej.util.Prefs;

/**
 * Abstract superclass for {@link UserInterface} implementations.
 * 
 * @author Curtis Rueden
 */
public abstract class AbstractUserInterface extends SortablePlugin
	implements UserInterface
{

	private static final String PREF_FIRST_RUN = "firstRun-" + ImageJ.VERSION;
	private static final String LAST_X = "lastXLocation";
	private static final String LAST_Y = "lastYLocation";

	private UIService uiService;

	// -- UserInterface methods --

	@Override
	public void initialize(final UIService service) {
		if (getContext() != service.getContext()) {
			throw new IllegalArgumentException("Mismatched context: " + service);
		}
		uiService = service;
	}

	@Override
	public UIService getUIService() {
		return uiService;
	}

	@Override
	public void create() {
		createUI();
		displayReadme();
		updaterCheck();
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

	@Override
	public void saveLocation() {
		final ApplicationFrame appFrame = getApplicationFrame();
		if (appFrame != null) {
			Prefs.put(getClass(), LAST_X, appFrame.getLocationX());
			Prefs.put(getClass(), LAST_Y, appFrame.getLocationY());
		}
	}

	@Override
	public void restoreLocation() {
		final ApplicationFrame appFrame = getApplicationFrame();
		if (appFrame != null) {
			final int lastX = Prefs.getInt(getClass(), LAST_X, 0);
			final int lastY = Prefs.getInt(getClass(), LAST_Y, 0);
			appFrame.setLocation(lastX, lastY);
		}
	}

	// -- Internal methods --

	/**
	 * Subclasses override to control UI creation. They must also call
	 * super.createUI() after creating the {@link ApplicationFrame} but before
	 * showing it (assuming the UI has an {@link ApplicationFrame}).
	 */
	protected void createUI() {
		restoreLocation();
	}

	// -- Helper methods --

	protected EventService getEventService() {
		return uiService.getEventService();
	}

	protected StatusService getStatusService() {
		return uiService.getStatusService();
	}

	/** Shows the readme, if this is the first time ImageJ has run. */
	private void displayReadme() {
		final String firstRun = Prefs.get(getClass(), PREF_FIRST_RUN);
		if (firstRun != null) return;
		Prefs.put(getClass(), PREF_FIRST_RUN, false);
		uiService.getPluginService().run(ShowReadme.class);
	}

	/** Tests whether updates are available */
	private void updaterCheck() {
		try {
			final UpToDate.Result result = UpToDate.check();
			switch (result) {
				case UP_TO_DATE:
				case OFFLINE:
				case REMIND_LATER:
				case CHECK_TURNED_OFF:
				case UPDATES_MANAGED_DIFFERENTLY:
				case DEVELOPER:
					return;
				case UPDATEABLE:
					uiService.getPluginService().run(UpdatesAvailable.class);
					break;
				case PROXY_NEEDS_AUTHENTICATION:
					throw new RuntimeException(
						"TODO: authenticate proxy with the configured user/pass pair");
				case READ_ONLY:
					final String message =
						"Your ImageJ installation cannot be updated because it is read-only";
					uiService.getLog().warn(message);
					getStatusService().showStatus(message);
					break;
				default:
					uiService.getLog().error("Unhandled UpToDate case: " + result);
			}
		}
		catch (final Exception e) {
			uiService.getLog().error(e);
			return;
		}
	}

}
