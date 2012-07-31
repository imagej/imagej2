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

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.display.DatasetView;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.options.OptionsChannels;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.ext.KeyCode;
import imagej.ext.display.event.DisplayActivatedEvent;
import imagej.ext.display.event.input.KyPressedEvent;
import imagej.ext.display.event.input.KyReleasedEvent;
import imagej.ext.menu.MenuService;
import imagej.ext.plugin.PluginInfo;
import imagej.ext.plugin.PluginService;
import imagej.legacy.plugin.LegacyPlugin;
import imagej.legacy.plugin.LegacyPluginFinder;
import imagej.log.LogService;
import imagej.options.OptionsService;
import imagej.options.event.OptionsEvent;
import imagej.options.plugins.OptionsMisc;
import imagej.service.AbstractService;
import imagej.service.Service;
import imagej.util.ColorRGB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.imglib2.display.CompositeXYProjector;
import net.imglib2.display.RealLUTConverter;
import net.imglib2.type.numeric.RealType;

/**
 * Service for working with legacy ImageJ 1.x.
 * <p>
 * The legacy service overrides the behavior of various IJ1 methods, inserting
 * seams so that (e.g.) the modern UI is aware of IJ1 events as they occur.
 * </p>
 * <p>
 * It also maintains an image map between IJ1 {@link ImagePlus} objects and IJ2
 * {@link Dataset}s.
 * </p>
 * <p>
 * In this fashion, when a legacy plugin is executed on a {@link Dataset}, the
 * service transparently translates it into an {@link ImagePlus}, and vice
 * versa, enabling backward compatibility with legacy plugins.
 * </p>
 * 
 * @author Curtis Rueden
 */
@Service
public final class LegacyService extends AbstractService {

	static {
		new LegacyInjector().injectHooks();
	}

	private final LogService log;
	private final EventService eventService;
	private final PluginService pluginService;
	private final OptionsService optionsService;
	private final ImageDisplayService imageDisplayService;
	private final MenuService menuService;
	private boolean lastDebugMode;
	private boolean initialized;

	/** Mapping between modern and legacy image data structures. */
	private LegacyImageMap imageMap;

	/** Method of synchronizing IJ2 & IJ1 options. */
	private OptionsSynchronizer optionsSynchronizer;

	// -- Constructors --

	public LegacyService() {
		// NB: Required by SezPoz.
		super(null);
		throw new UnsupportedOperationException();
	}

	public LegacyService(final ImageJ context, final LogService log,
		final EventService eventService, final PluginService pluginService,
		final OptionsService optionsService,
		final ImageDisplayService imageDisplayService, final MenuService menuService)
	{
		super(context);
		this.log = log;
		this.eventService = eventService;
		this.pluginService = pluginService;
		this.optionsService = optionsService;
		this.imageDisplayService = imageDisplayService;
		this.menuService = menuService;

		imageMap = new LegacyImageMap(context);
		optionsSynchronizer = new OptionsSynchronizer(optionsService);

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
		addLegacyPlugins(enableBlacklist);

		updateIJ1Settings();

		subscribeToEvents(eventService);

		initialized = true;
	}

	// -- LegacyService methods --

	public EventService getEventService() {
		return eventService;
	}

	public PluginService getPluginService() {
		return pluginService;
	}

	public OptionsService getOptionsService() {
		return optionsService;
	}

	public ImageDisplayService getImageDisplayService() {
		return imageDisplayService;
	}

	public LegacyImageMap getImageMap() {
		return imageMap;
	}

	public void runLegacyPlugin(final String ij1ClassName, final String argument)
	{
		final String arg = (argument == null) ? "" : argument;
		final Map<String, Object> inputMap = new HashMap<String, Object>();
		inputMap.put("className", ij1ClassName);
		inputMap.put("arg", arg);
		pluginService.run(LegacyPlugin.class, inputMap);
	}

	/**
	 * Indicates to the service that the given {@link ImagePlus} has changed as
	 * part of a legacy plugin execution.
	 */
	public void legacyImageChanged(final ImagePlus imp) {
		// CTR FIXME rework static InsideBatchDrawing logic?
		// BDZ - removal for now. replace if issues arise. Alternative fix outlined
		// in FunctionsMethods code. This code was for addressing bug #554
		// if (FunctionsMethods.InsideBatchDrawing > 0) return;

		// create a display if it doesn't exist yet.
		imageMap.registerLegacyImage(imp);

		// record resultant ImagePlus as a legacy plugin output
		LegacyOutputTracker.getOutputImps().add(imp);
	}

	/**
	 * Ensures that the currently active {@link ImagePlus} matches the currently
	 * active {@link ImageDisplay}. Does not perform any harmonization.
	 */
	public void syncActiveImage() {
		final ImageDisplay activeDisplay =
			imageDisplayService.getActiveImageDisplay();
		final ImagePlus activeImagePlus = imageMap.lookupImagePlus(activeDisplay);
		// NB - old way - caused probs with 3d Project
		//WindowManager.setTempCurrentImage(activeImagePlus);
		// NB - new way - test thoroughly
		if (activeImagePlus == null)
			WindowManager.setCurrentWindow(null);
		else
			WindowManager.setCurrentWindow(activeImagePlus.getWindow());
	}

	public boolean isInitialized() {
		return initialized;
	}

	// TODO - make private only???

	public void updateIJ1Settings() {
		optionsSynchronizer.updateIJ1SettingsFromIJ2();
	}

	public void updateIJ2Settings() {
		optionsSynchronizer.updateIJ2SettingsFromIJ1();
	}

	public void syncColors() {
		DatasetView view = imageDisplayService.getActiveDatasetView();
		if (view == null) return;
		OptionsChannels channels = getChannels();
		ColorRGB fgColor = view.getColor(channels.getFgValues());
		ColorRGB bgColor = view.getColor(channels.getBgValues());
		optionsSynchronizer.colorOptions(fgColor, bgColor);
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
		updateIJ1Settings();
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

	private OptionsChannels getChannels() {
		final OptionsService service =
			getContext().getService(OptionsService.class);

		return service.getOptions(OptionsChannels.class);
	}

	private void updateMenus(final OptionsMisc optsMisc) {
		pluginService.reloadPlugins();
		final boolean enableBlacklist = !optsMisc.isDebugMode();
		addLegacyPlugins(enableBlacklist);
		lastDebugMode = optsMisc.isDebugMode();
	}

	private void addLegacyPlugins(final boolean enableBlacklist) {
		final LegacyPluginFinder finder =
			new LegacyPluginFinder(log, menuService.getMenu(), enableBlacklist);
		final ArrayList<PluginInfo<?>> plugins = new ArrayList<PluginInfo<?>>();
		finder.findPlugins(plugins);
		pluginService.addPlugins(plugins);
	}

	/* 3-1-12

	 We are no longer going to synchronize colors from IJ1 to IJ2

	protected class IJ1EventListener implements IJEventListener {

		@Override
		public void eventOccurred(final int eventID) {
			@SuppressWarnings("synthetic-access")
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
