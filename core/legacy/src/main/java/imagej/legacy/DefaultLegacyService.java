/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageWindow;
import imagej.command.CommandService;
import imagej.core.options.OptionsMisc;
import imagej.data.DatasetService;
import imagej.data.display.DatasetView;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.OverlayService;
import imagej.data.options.OptionsChannels;
import imagej.data.threshold.ThresholdService;
import imagej.display.DisplayService;
import imagej.display.event.DisplayActivatedEvent;
import imagej.display.event.input.KyPressedEvent;
import imagej.display.event.input.KyReleasedEvent;
import imagej.legacy.plugin.LegacyCommand;
import imagej.legacy.plugin.LegacyPluginFinder;
import imagej.menu.MenuService;
import imagej.options.OptionsService;
import imagej.options.event.OptionsEvent;
import imagej.ui.ApplicationFrame;
import imagej.ui.UIService;
import imagej.ui.viewer.DisplayWindow;
import imagej.ui.viewer.image.ImageDisplayViewer;
import imagej.util.ColorRGB;

import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.scijava.app.StatusService;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.input.KeyCode;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * Default service for working with legacy ImageJ 1.x.
 * <p>
 * The legacy service overrides the behavior of various legacy ImageJ methods,
 * inserting seams so that (e.g.) the modern UI is aware of legacy ImageJ events
 * as they occur.
 * </p>
 * <p>
 * It also maintains an image map between legacy ImageJ {@link ImagePlus}
 * objects and modern ImageJ {@link ImageDisplay}s.
 * </p>
 * <p>
 * In this fashion, when a legacy command is executed on a {@link ImageDisplay},
 * the service transparently translates it into an {@link ImagePlus}, and vice
 * versa, enabling backward compatibility with legacy commands.
 * </p>
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public final class DefaultLegacyService extends AbstractService implements
	LegacyService
{

	static {
		final ClassLoader contextClassLoader =
			Thread.currentThread().getContextClassLoader();
		new LegacyInjector().injectHooks(contextClassLoader);
	}

	@Parameter
	private OverlayService overlayService;

	@Parameter
	private LogService log;

	@Parameter
	private EventService eventService;

	@Parameter
	private PluginService pluginService;

	@Parameter
	private CommandService commandService;

	@Parameter
	private OptionsService optionsService;

	@Parameter
	private ImageDisplayService imageDisplayService;

	@Parameter
	private DisplayService displayService;

	@Parameter
	private ThresholdService thresholdService;

	@Parameter
	private DatasetService datasetService;

	@Parameter
	private MenuService menuService;

	@Parameter
	private StatusService statusService;

	private boolean lastDebugMode;
	private static DefaultLegacyService instance;

	/** Mapping between modern and legacy image data structures. */
	private LegacyImageMap imageMap;

	/** Method of synchronizing modern & legacy options. */
	private OptionsSynchronizer optionsSynchronizer;

	public DefaultLegacyService() {
		if (GraphicsEnvironment.isHeadless()) {
			throw new IllegalStateException(
				"Legacy support not available in headless mode.");
		}
	}

	/** Legacy ImageJ 1.x mode: stop synchronizing */
	private boolean legacyIJ1Mode;

	// -- LegacyService methods --

	@Override
	public EventService getEventService() {
		return eventService;
	}

	@Override
	public PluginService getPluginService() {
		return pluginService;
	}

	@Override
	public OptionsService getOptionsService() {
		return optionsService;
	}

	@Override
	public ImageDisplayService getImageDisplayService() {
		return imageDisplayService;
	}

	@Override
	public LogService getLogService() {
		return log;
	}

	@Override
	public LegacyImageMap getImageMap() {
		return imageMap;
	}

	@Override
	public DisplayService getDisplayService() {
		return displayService;
	}

	@Override
	public DatasetService getDatasetService() {
		return datasetService;
	}

	@Override
	public OverlayService getOverlayService() {
		return overlayService;
	}

	@Override
	public ThresholdService getThresholdService() {
		return thresholdService;
	}

	@Override
	public StatusService getStatusService() {
		return statusService;
	}

	@Override
	public void
		runLegacyCommand(final String ij1ClassName, final String argument)
	{
		final String arg = argument == null ? "" : argument;
		final Map<String, Object> inputMap = new HashMap<String, Object>();
		inputMap.put("className", ij1ClassName);
		inputMap.put("arg", arg);
		commandService.run(LegacyCommand.class, inputMap);
	}

	@Override
	public void legacyImageChanged(final ImagePlus imp) {
		// CTR FIXME rework static InsideBatchDrawing logic?
		// BDZ - removal for now. replace if issues arise. Alternative fix outlined
		// in FunctionsMethods code. This code was for addressing bug #554
		// if (FunctionsMethods.InsideBatchDrawing > 0) return;

		// create a display if it doesn't exist yet.
		imageMap.registerLegacyImage(imp);

		// record resultant ImagePlus as a legacy command output
		LegacyOutputTracker.addOutput(imp);
	}

	@Override
	public void syncActiveImage() {
		final ImageDisplay activeDisplay =
			imageDisplayService.getActiveImageDisplay();
		final ImagePlus activeImagePlus = imageMap.lookupImagePlus(activeDisplay);
		// NB - old way - caused probs with 3d Project
		// WindowManager.setTempCurrentImage(activeImagePlus);
		// NB - new way - test thoroughly
		if (activeImagePlus == null) WindowManager.setCurrentWindow(null);
		else WindowManager.setCurrentWindow(activeImagePlus.getWindow());
	}

	@Override
	public boolean isInitialized() {
		return instance != null;
	}

	// TODO - make private only???

	@Override
	public void updateLegacyImageJSettings() {
		optionsSynchronizer.updateLegacyImageJSettingsFromModernImageJ();
	}

	// TODO - make private only???

	@Override
	public void updateModernImageJSettings() {
		optionsSynchronizer.updateModernImageJSettingsFromLegacyImageJ();
	}

	@Override
	public void syncColors() {
		final DatasetView view = imageDisplayService.getActiveDatasetView();
		if (view == null) return;
		final OptionsChannels channels = getChannels();
		final ColorRGB fgColor = view.getColor(channels.getFgValues());
		final ColorRGB bgColor = view.getColor(channels.getBgValues());
		optionsSynchronizer.colorOptions(fgColor, bgColor);
	}

	/**
	 * States whether we're running in legacy ImageJ 1.x mode.
	 * <p>
	 * To support work flows which are incompatible with ImageJ2, we want to allow
	 * users to run in legacy ImageJ 1.x mode, where the ImageJ2 GUI is hidden and
	 * the ImageJ 1.x GUI is shown. During this time, no synchronization should
	 * take place.
	 * </p>
	 */
	@Override
	public boolean isLegacyMode() {
		return legacyIJ1Mode;
	}

	/**
	 * Switch to/from running legacy ImageJ 1.x mode.
	 */
	@Override
	public synchronized void toggleLegacyMode(final boolean toggle) {
		if (toggle) legacyIJ1Mode = toggle;

		final ij.ImageJ ij = IJ.getInstance();

		SwitchToModernMode.registerMenuItem(this);

		// TODO: hide/show Brightness/Contrast, Color Picker, Command Launcher, etc
		// TODO: prevent IJ1 from quitting without IJ2 quitting, too

		final UIService uiService = getContext().getService(UIService.class);
		if (uiService != null) {
			// hide/show the IJ2 main window
			final ApplicationFrame appFrame =
				uiService.getDefaultUI().getApplicationFrame();
			appFrame.setVisible(!toggle);

			// TODO: move this into the LegacyImageMap's toggleLegacyMode, passing the uiService
			// hide/show the IJ2 datasets corresponding to legacy ImagePlus instances
			for (final ImageDisplay display : imageMap.getImageDisplays()) {
				final ImageDisplayViewer viewer =
					(ImageDisplayViewer) uiService.getDisplayViewer(display);
				if (viewer == null) continue;
				final DisplayWindow window = viewer.getWindow();
				if (window != null) window.showDisplay(!toggle);
			}
		}

		// hide/show IJ1 main window
		if (toggle) ij.pack();
		ij.setVisible(toggle);

		// hide/show the legacy ImagePlus instances
		for (final ImagePlus imp : imageMap.getImagePlusInstances()) {
			final ImageWindow window = imp.getWindow();
			if (window != null) window.setVisible(toggle);
		}

		if (!toggle) legacyIJ1Mode = toggle;
		imageMap.toggleLegacyMode(toggle);
	}

	@Override
	public String getLegacyVersion() {
		return IJ.getVersion();
	}

	// -- Service methods --

	@Override
	public void initialize() {
		checkInstance();

		imageMap = new LegacyImageMap(this);
		optionsSynchronizer = new OptionsSynchronizer(optionsService);

		synchronized (DefaultLegacyService.class) {
			checkInstance();
			instance = this;
		}

		// initialize legacy ImageJ application
		try {
			new ij.ImageJ(ij.ImageJ.NO_SHOW);
		}
		catch (final Throwable t) {
			log.warn("Failed to instantiate IJ1.", t);
		}

		// discover legacy plugins
		final OptionsMisc optsMisc = optionsService.getOptions(OptionsMisc.class);
		lastDebugMode = optsMisc.isDebugMode();
		final boolean enableBlacklist = !optsMisc.isDebugMode();
		addLegacyCommands(enableBlacklist);

		updateLegacyImageJSettings();
	}

	// -- Disposable methods --

	@Override
	public void dispose() {
		final ImageJ ij = IJ.getInstance();
		if (ij != null) {
			// close out all image windows, without dialog prompts
			while (true) {
				final ImagePlus imp = WindowManager.getCurrentImage();
				if (imp == null) break;
				imp.changes = false;
				imp.close();
			}

			// close any remaining (non-image) windows
			WindowManager.closeAllWindows();

			// quit legacy ImageJ on the same thread
			ij.run();
		}

		instance = null;
	}

	// -- Event handlers --

	/**
	 * Keeps the active legacy {@link ImagePlus} in sync with the active modern
	 * {@link ImageDisplay}.
	 */
	@EventHandler
	protected void onEvent(
		@SuppressWarnings("unused") final DisplayActivatedEvent event)
	{
		syncActiveImage();
	}

	@EventHandler
	protected void onEvent(final OptionsEvent event) {
		if (event.getOptions().getClass() == OptionsMisc.class) {
			final OptionsMisc opts = (OptionsMisc) event.getOptions();
			if (opts.isDebugMode() != lastDebugMode) updateMenus(opts);
		}
		updateLegacyImageJSettings();
	}

	@EventHandler
	protected void onEvent(final KyPressedEvent event) {
		final KeyCode code = event.getCode();
		if (code == KeyCode.SPACE) IJ.setKeyDown(KeyCode.SPACE.getCode());
		if (code == KeyCode.ALT) IJ.setKeyDown(KeyCode.ALT.getCode());
		if (code == KeyCode.SHIFT) IJ.setKeyDown(KeyCode.SHIFT.getCode());
		if (code == KeyCode.CONTROL) IJ.setKeyDown(KeyCode.CONTROL.getCode());
		if (IJ.isMacintosh() && code == KeyCode.META) {
			IJ.setKeyDown(KeyCode.CONTROL.getCode());
		}
	}

	@EventHandler
	protected void onEvent(final KyReleasedEvent event) {
		final KeyCode code = event.getCode();
		if (code == KeyCode.SPACE) IJ.setKeyUp(KeyCode.SPACE.getCode());
		if (code == KeyCode.ALT) IJ.setKeyUp(KeyCode.ALT.getCode());
		if (code == KeyCode.SHIFT) IJ.setKeyUp(KeyCode.SHIFT.getCode());
		if (code == KeyCode.CONTROL) IJ.setKeyUp(KeyCode.CONTROL.getCode());
		if (IJ.isMacintosh() && code == KeyCode.META) {
			IJ.setKeyUp(KeyCode.CONTROL.getCode());
		}
	}

	// -- helpers --

	/**
	 * Returns the legacy service associated with the ImageJ 1.x instance in the
	 * current class loader. This method is intended to be used by the
	 * {@link CodeHacker}; it is invoked by the javassisted methods.
	 * 
	 * @return the legacy service
	 */
	public static DefaultLegacyService getInstance() {
		return instance;
	}

	/**
	 * @throws UnsupportedOperationException if the singleton
	 *           {@code DefaultLegacyService} already exists.
	 */
	private void checkInstance() {
		if (instance != null) {
			throw new UnsupportedOperationException(
				"Cannot instantiate more than one DefaultLegacyService");
		}
	}

	private OptionsChannels getChannels() {
		return optionsService.getOptions(OptionsChannels.class);
	}

	private void updateMenus(final OptionsMisc optsMisc) {
		pluginService.reloadPlugins();
		final boolean enableBlacklist = !optsMisc.isDebugMode();
		addLegacyCommands(enableBlacklist);
		lastDebugMode = optsMisc.isDebugMode();
	}

	private void addLegacyCommands(final boolean enableBlacklist) {
		final LegacyPluginFinder finder =
			new LegacyPluginFinder(log, menuService.getMenu(), enableBlacklist);
		final ArrayList<PluginInfo<?>> plugins = new ArrayList<PluginInfo<?>>();
		finder.findPlugins(plugins);
		pluginService.addPlugins(plugins);
	}

	/* 3-1-12

	 We are no longer going to synchronize colors from IJ1 to modern ImageJ

	protected class IJ1EventListener implements IJEventListener {

		@Override
		public void eventOccurred(final int eventID) {
			final OptionsChannels colorOpts =
				optionsService.getOptions(OptionsChannels.class);
			ColorRGB color;
			switch (eventID) {
				case ij.IJEventListener.COLOR_PICKER_CLOSED:
					color = AWTColors.getColorRGB(Toolbar.getForegroundColor());
					colorOpts.setFgColor(color);
					color = AWTColors.getColorRGB(Toolbar.getBackgroundColor());
					colorOpts.setBgColor(color);
					colorOpts.save();
					break;
				case ij.IJEventListener.FOREGROUND_COLOR_CHANGED:
					color = AWTColors.getColorRGB(Toolbar.getForegroundColor());
					colorOpts.setFgColor(color);
					colorOpts.save();
					break;
				case ij.IJEventListener.BACKGROUND_COLOR_CHANGED:
					color = AWTColors.getColorRGB(Toolbar.getBackgroundColor());
					colorOpts.setBgColor(color);
					colorOpts.save();
					break;
				case ij.IJEventListener.LOG_WINDOW_CLOSED:
					// TODO - do something???
					break;
				case ij.IJEventListener.TOOL_CHANGED:
					// TODO - do something???
					break;
				default: // unknown event
					// do nothing
					break;
			}
		}
	}
	*/
}
