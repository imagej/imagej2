//
// LegacyService.java
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

package imagej.legacy;

import java.awt.Color;

import ij.IJ;
import ij.IJEventListener;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Toolbar;
import imagej.AbstractService;
import imagej.ImageJ;
import imagej.Service;
import imagej.data.Dataset;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.ext.KeyCode;
import imagej.ext.display.event.DisplayActivatedEvent;
import imagej.ext.display.event.input.KyPressedEvent;
import imagej.ext.display.event.input.KyReleasedEvent;
import imagej.options.OptionsService;
import imagej.options.event.OptionsEvent;
import imagej.options.plugins.OptionsColors;
import imagej.util.ColorRGB;
import imagej.util.Log;

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

	private final EventService eventService;
	private final OptionsService optionsService;
	private final ImageDisplayService imageDisplayService;

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

	public LegacyService(final ImageJ context, final EventService eventService,
		final OptionsService optionsService,
		final ImageDisplayService imageDisplayService)
	{
		super(context);
		this.eventService = eventService;
		this.optionsService = optionsService;
		this.imageDisplayService = imageDisplayService;
	}

	// -- LegacyService methods --

	public EventService getEventService() {
		return eventService;
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
		WindowManager.setTempCurrentImage(activeImagePlus);
	}

	// TODO - make private only???

	public void updateIJ1Settings() {
		optionsSynchronizer.updateIJ1SettingsFromIJ2();
	}

	public void updateIJ2Settings() {
		optionsSynchronizer.updateIJ2SettingsFromIJ1();
	}

	// -- IService methods --

	@Override
	@SuppressWarnings("synthetic-access")
	public void initialize() {
		imageMap = new LegacyImageMap(eventService);
		optionsSynchronizer = new OptionsSynchronizer(optionsService);

		// initialize legacy ImageJ application
		try {
			new ij.ImageJ(ij.ImageJ.NO_SHOW);
		}
		catch (final Throwable t) {
			Log.warn("Failed to instantiate IJ1.", t);
		}

		IJ.addEventListener(new IJ1EventListener());
		
		updateIJ1Settings();

		super.initialize();
	}

	// -- Event handlers --

	/**
	 * Keeps the active legacy {@link ImagePlus} in sync with the active modern
	 * {@link ImageDisplay}.
	 */
	@EventHandler
	protected void onEvent(@SuppressWarnings("unused")
		final DisplayActivatedEvent event)
	{
		syncActiveImage();
	}

	@EventHandler
	protected void onEvent(@SuppressWarnings("unused")
		final OptionsEvent event)
	{
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
	
	private class IJ1EventListener implements IJEventListener {
		@SuppressWarnings("synthetic-access")
		private OptionsColors colorOpts = optionsService.getOptions(OptionsColors.class);
		
		@Override
		public void eventOccurred(int eventID) {
			ColorRGB color;
			switch (eventID) {
				case ij.IJEventListener.COLOR_PICKER_CLOSED:
					color = getIJ2Color(Toolbar.getForegroundColor());
					colorOpts.setFgColor(color);
					color = getIJ2Color(Toolbar.getBackgroundColor());
					colorOpts.setBgColor(color);
					colorOpts.save();
					break;
				case ij.IJEventListener.FOREGROUND_COLOR_CHANGED:
					color = getIJ2Color(Toolbar.getForegroundColor());
					colorOpts.setFgColor(color);
					colorOpts.save();
					break;
				case ij.IJEventListener.BACKGROUND_COLOR_CHANGED:
					color = getIJ2Color(Toolbar.getBackgroundColor());
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
					/* do nothing */
					break;
			}
		}

		private ColorRGB getIJ2Color(Color ij1c) {
			return new ColorRGB(ij1c.getRed(), ij1c.getGreen(), ij1c.getBlue());
		}
	}

}
