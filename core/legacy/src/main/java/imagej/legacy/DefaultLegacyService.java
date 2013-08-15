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
 * It also maintains an image map between legacy ImageJ {@link ij.ImagePlus}
 * objects and modern ImageJ {@link ImageDisplay}s.
 * </p>
 * <p>
 * In this fashion, when a legacy command is executed on a {@link ImageDisplay},
 * the service transparently translates it into an {@link ij.ImagePlus}, and vice
 * versa, enabling backward compatibility with legacy commands.
 * </p>
 * 
 * @author Barry DeZonia
 * @author Curtis Rueden
 * @author Johannes Schindelin
 */
@Plugin(type = Service.class)
public final class DefaultLegacyService extends AbstractService implements
	LegacyService
{
	private final static LegacyInjector legacyInjector;

	static {
		final ClassLoader contextClassLoader =
			Thread.currentThread().getContextClassLoader();
		legacyInjector = new LegacyInjector();
		legacyInjector.injectHooks(contextClassLoader);
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

	@Parameter(required = false)
	private UIService uiService;

	private boolean lastDebugMode;
	private static DefaultLegacyService instance;

	/** Mapping between modern and legacy image data structures. */
	private LegacyImageMap imageMap;

	/** Method of synchronizing modern & legacy options. */
	private OptionsSynchronizer optionsSynchronizer;

	/** Keep references to ImageJ 1.x separate */
	private IJ1Helper ij1Helper;

	/** Legacy ImageJ 1.x mode: stop synchronizing */
	private boolean legacyIJ1Mode;

	// -- LegacyService methods --

	@Override
	public LogService log() {
		return log;
	}

	@Override
	public StatusService status() {
		return statusService;
	}

	@Override
	public LegacyImageMap getImageMap() {
		return imageMap;
	}

	@Override
	public OptionsSynchronizer getOptionsSynchronizer() {
		return optionsSynchronizer;
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
	public void syncActiveImage() {
		final ImageDisplay activeDisplay =
			imageDisplayService.getActiveImageDisplay();
		ij1Helper.syncActiveImage(activeDisplay);
	}

	@Override
	public boolean isInitialized() {
		return instance != null;
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
	public void toggleLegacyMode(final boolean wantIJ1) {
		toggleLegacyMode(wantIJ1, false);
	}

	public synchronized void toggleLegacyMode(final boolean wantIJ1, final boolean initializing) {
		if (wantIJ1) legacyIJ1Mode = true;

		// TODO: hide/show Brightness/Contrast, Color Picker, Command Launcher, etc
		// TODO: prevent IJ1 from quitting without IJ2 quitting, too

		if (!initializing) {
			if (uiService != null) {
				// hide/show the IJ2 main window
				final ApplicationFrame appFrame =
					uiService.getDefaultUI().getApplicationFrame();
				if (appFrame == null) {
					if (!wantIJ1) uiService.showUI();
				} else {
					appFrame.setVisible(!wantIJ1);
				}
			}

			// TODO: move this into the LegacyImageMap's toggleLegacyMode, passing
			// the uiService
			// hide/show the IJ2 datasets corresponding to legacy ImagePlus instances
			for (final ImageDisplay display : imageMap.getImageDisplays()) {
				final ImageDisplayViewer viewer =
					(ImageDisplayViewer) uiService.getDisplayViewer(display);
				if (viewer == null) continue;
				final DisplayWindow window = viewer.getWindow();
				if (window != null) window.showDisplay(!wantIJ1);
			}
		}

		// hide/show IJ1 main window
		ij1Helper.setVisible(wantIJ1);

		if (wantIJ1 && !initializing) {
			optionsSynchronizer.updateLegacyImageJSettingsFromModernImageJ();
		}

		if (!wantIJ1) legacyIJ1Mode = false;
		imageMap.toggleLegacyMode(wantIJ1);
	}

	@Override
	public String getLegacyVersion() {
		return ij1Helper.getVersion();
	}

	// -- Service methods --

	@Override
	public void initialize() {
		checkInstance();

		ij1Helper = new IJ1Helper(this);
		boolean hasIJ1Instance = ij1Helper.hasInstance();

		// as long as we're starting up, we're in legacy mode
		legacyIJ1Mode = true;

		imageMap = new LegacyImageMap(this);
		optionsSynchronizer = new OptionsSynchronizer(optionsService);

		synchronized (DefaultLegacyService.class) {
			checkInstance();
			instance = this;
			legacyInjector.setLegacyService(this);
		}

		ij1Helper.initialize();

		SwitchToModernMode.registerMenuItem();

		// discover legacy plugins
		final boolean enableBlacklist = true;
		addLegacyCommands(enableBlacklist);

		if (!hasIJ1Instance && !GraphicsEnvironment.isHeadless()) toggleLegacyMode(false, true);
	}

	// -- Disposable methods --

	@Override
	public void dispose() {
		ij1Helper.dispose();

		legacyInjector.setLegacyService(new DummyLegacyService());
		instance = null;
	}

	// -- Event handlers --

	/**
	 * Keeps the active legacy {@link ij.ImagePlus} in sync with the active modern
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
		optionsSynchronizer.updateModernImageJSettingsFromLegacyImageJ();
	}

	@EventHandler
	protected void onEvent(final KyPressedEvent event) {
		final KeyCode code = event.getCode();
		if (code == KeyCode.SPACE) ij1Helper.setKeyDown(KeyCode.SPACE.getCode());
		if (code == KeyCode.ALT) ij1Helper.setKeyDown(KeyCode.ALT.getCode());
		if (code == KeyCode.SHIFT) ij1Helper.setKeyDown(KeyCode.SHIFT.getCode());
		if (code == KeyCode.CONTROL) ij1Helper.setKeyDown(KeyCode.CONTROL.getCode());
		if (ij1Helper.isMacintosh() && code == KeyCode.META) {
			ij1Helper.setKeyDown(KeyCode.CONTROL.getCode());
		}
	}

	@EventHandler
	protected void onEvent(final KyReleasedEvent event) {
		final KeyCode code = event.getCode();
		if (code == KeyCode.SPACE) ij1Helper.setKeyUp(KeyCode.SPACE.getCode());
		if (code == KeyCode.ALT) ij1Helper.setKeyUp(KeyCode.ALT.getCode());
		if (code == KeyCode.SHIFT) ij1Helper.setKeyUp(KeyCode.SHIFT.getCode());
		if (code == KeyCode.CONTROL) ij1Helper.setKeyUp(KeyCode.CONTROL.getCode());
		if (ij1Helper.isMacintosh() && code == KeyCode.META) {
			ij1Helper.setKeyUp(KeyCode.CONTROL.getCode());
		}
	}

	// -- pre-initialization

	/**
	 * Makes sure that the ImageJ 1.x classes are patched.
	 * <p>
	 * We absolutely require that the LegacyInjector did its job before we use the
	 * ImageJ 1.x classes.
	 * </p>
	 * <p>
	 * Just loading the {@link DefaultLegacyService} class is not enough; it will
	 * not necessarily get initialized. So we provide this method just to force
	 * class initialization (and thereby the LegacyInjector to patch ImageJ 1.x).
	 * </p>
	 */
	public static void preinit() {
		if (legacyInjector == null) {
			throw new RuntimeException("LegacyInjector was not instantiated!");
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
