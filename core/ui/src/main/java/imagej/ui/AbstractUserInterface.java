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

package imagej.ui;

import imagej.command.CommandService;
import imagej.display.Display;
import imagej.display.DisplayService;
import imagej.ui.viewer.DisplayViewer;
import imagej.ui.viewer.DisplayWindow;
import imagej.updater.core.UpToDate;
import imagej.updater.ui.UpdatesAvailable;

import java.util.List;

import org.scijava.app.App;
import org.scijava.app.StatusService;
import org.scijava.log.LogService;
import org.scijava.plugin.AbstractRichPlugin;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.thread.ThreadService;
import org.scijava.util.Prefs;

/**
 * Abstract superclass for {@link UserInterface} implementations.
 * 
 * @author Curtis Rueden
 */
public abstract class AbstractUserInterface extends AbstractRichPlugin
	implements UserInterface
{

	private static final String LAST_X = "lastXLocation";
	private static final String LAST_Y = "lastYLocation";

	@Parameter
	private CommandService commandService;

	@Parameter
	private DisplayService displayService;

	@Parameter
	private LogService log;

	@Parameter
	private PluginService pluginService;

	@Parameter
	private StatusService statusService;

	@Parameter
	private ThreadService threadService;

	@Parameter
	private UIService uiService;

	/** Whether the UI is currently being displayed. */
	private boolean visible = false;

	// -- UserInterface methods --

	@Override
	public void show() {
		createUI();
		displayReadme();
		updaterCheck();
		visible = true;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	@Override
	public void show(final Object o) {
		show(null, o);
	}

	@Override
	public void show(final String name, final Object o) {
		final Display<?> display;
		if (o instanceof Display) {
			display = (Display<?>) o;
		}
		else {
			display = displayService.createDisplay(name, o);
		}
		if (!isVisible()) {
			// NB: If this UI is invisible, the display will not be automatically
			// shown. So in that case, we show it explicitly here.
			show(display);
		}
	}

	@Override
	public void show(final Display<?> display) {
		if (uiService.getDisplayViewer(display) != null) {
			// display is already being shown
			return;
		}

		final List<PluginInfo<DisplayViewer<?>>> viewers =
			uiService.getViewerPlugins();

		DisplayViewer<?> displayViewer = null;
		for (final PluginInfo<DisplayViewer<?>> info : viewers) {
			// check that viewer can actually handle the given display
			final DisplayViewer<?> viewer = pluginService.createInstance(info);
			if (viewer == null) continue;
			if (!viewer.canView(display)) continue;
			if (!viewer.isCompatible(this)) continue;
			displayViewer = viewer;
			break; // found a suitable viewer; we are done
		}
		if (displayViewer == null) {
			log.warn("For UI '" + getClass().getName() +
				"': no suitable viewer for display: " + display);
			return;
		}

		final DisplayViewer<?> finalViewer = displayViewer;
		threadService.queue(new Runnable() {
			@Override
			public void run() {
				final DisplayWindow displayWindow = createDisplayWindow(display);
				finalViewer.view(displayWindow, display);
				displayWindow.setTitle(display.getName());
				uiService.addDisplayViewer(finalViewer);
				displayWindow.showDisplay(true);
				display.update();
			}
		});
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

	protected App getApp() {
		return uiService.getApp();
	}

	/** Shows the readme, if this is the first time ImageJ has run. */
	private void displayReadme() {
		final String version = getApp().getVersion();
		final String prefFirstRun = "firstRun-" + version;
		final String firstRun = Prefs.get(getClass(), prefFirstRun);
		if (firstRun != null) return;
		Prefs.put(getClass(), prefFirstRun, false);
		commandService.run(ShowReadme.class, true);
	}

	/** Tests whether updates are available. */
	private void updaterCheck() {
		// TODO: Migrate this code to the ij-updater component.
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
					commandService.run(UpdatesAvailable.class, true);
					break;
				case PROXY_NEEDS_AUTHENTICATION:
					throw new RuntimeException(
						"TODO: authenticate proxy with the configured user/pass pair");
				case READ_ONLY:
					final String message =
						"Your ImageJ installation cannot be updated because it is read-only";
					log.warn(message);
					statusService.showStatus(message);
					break;
				default:
					log.error("Unhandled UpToDate case: " + result);
			}
		}
		catch (final Exception e) {
			log.error(e);
			return;
		}
	}

}
