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

package imagej.core.plugins.debug;

import imagej.data.display.event.CanvasEvent;
import imagej.data.display.event.MouseCursorEvent;
import imagej.data.display.event.ZoomEvent;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.event.EventSubscriber;
import imagej.event.ImageJEvent;
import imagej.event.StatusEvent;
import imagej.ext.display.event.DisplayEvent;
import imagej.ext.display.event.input.InputEvent;
import imagej.ext.display.event.input.KyEvent;
import imagej.ext.display.event.input.MsButtonEvent;
import imagej.ext.display.event.input.MsMovedEvent;
import imagej.ext.module.ItemVisibility;
import imagej.ext.module.event.ModuleEvent;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.ext.tool.event.ToolEvent;
import imagej.io.event.FileEvent;
import imagej.object.event.ListEvent;
import imagej.object.event.ObjectEvent;
import imagej.options.event.OptionsEvent;
import imagej.platform.event.ApplicationEvent;
import imagej.ui.OutputWindow;
import imagej.ui.UIService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Listens for all events. Useful for logging, history, macro recording,
 * perhaps.
 * <p>
 * 1/22/12, GBH: Refined definition of a DisplayEvent (a not InputEvent or
 * CanvasEvent)
 * </p>
 * 
 * @author Grant Harris
 * @author Curtis Rueden
 */
@Plugin(menuPath = "Plugins>Debug>Watch Events", headless = true)
public class WatchEvents implements ImageJPlugin {

	// -- Parameters --

	@Parameter(persist = false)
	private EventService eventService;

	@Parameter(persist = false)
	private UIService uiService;

	@Parameter(visibility = ItemVisibility.MESSAGE)
	@SuppressWarnings("unused")
	private final String message = "Event types to monitor:";

	@Parameter(label = "ApplicationEvent")
	private boolean showApp = true;

	@Parameter(label = "DisplayEvent")
	private boolean showDisplay = true;
	
	@Parameter(label = "MouseCursorEvent")
	private boolean showMouseCursor = true;

	@Parameter(label = "MsButtonEvent")
	private boolean showMsButton = false;

	@Parameter(label = "MsMovedEvent")
	private boolean showMsMoved = false;

	@Parameter(label = "KeyEvent")
	private boolean showKy = false;

	@Parameter(label = "FileEvent")
	private boolean showFile = true;

	@Parameter(label = "ListEvent")
	private boolean showList = true;

	@Parameter(label = "ModuleEvent")
	private boolean showModule = true;

	@Parameter(label = "ObjectEvent")
	private boolean showObject = true;

	@Parameter(label = "OptionsEvent")
	private boolean showOptions = true;

	@Parameter(label = "StatusEvent")
	private boolean showStatus = false;

	@Parameter(label = "ToolEvent")
	private boolean showTool = true;
	
	@Parameter(label = "ZoomEvent")
	private boolean showZoom = true;

	// -- Fields --

	private OutputWindow window;

	@SuppressWarnings("unused")
	private List<EventSubscriber<?>> subscribers;

	// -- WatchEvents methods --

	public void setShowApplicationEvents(final boolean show) {
		showApp = show;
	}

	public void setShowDisplayEvents(final boolean show) {
		showDisplay = show;
	}
	
	public void setMouseCursorEvents(final boolean show) {
		showMouseCursor = show;
	}

	public void setShowMsMovedEvents(final boolean show) {
		showMsMoved = show;
	}

	public void setShowMsButtonEvents(final boolean show) {
		showMsButton = show;
	}

	public void setShowKyEvents(final boolean show) {
		showKy = show;
	}

	public void setShowFileEvents(final boolean show) {
		showFile = show;
	}

	public void setShowListEvents(final boolean show) {
		showList = show;
	}

	public void setShowModuleEvents(final boolean show) {
		showModule = show;
	}

	public void setShowObjectEvents(final boolean show) {
		showObject = show;
	}

	public void setShowOptionsEvents(final boolean show) {
		showOptions = show;
	}

	public void setShowStatusEvents(final boolean show) {
		showStatus = show;
	}

	public void setShowToolEvents(final boolean show) {
		showTool = show;
	}
	
	public void setZoomEvents(final boolean show) {
		showZoom = show;
	}

	// -- Runnable methods --

	@Override
	public void run() {
		window = uiService.createOutputWindow("Event Watcher");
		window.setVisible(true);
		subscribers = eventService.subscribe(this);
		// TODO - unsubscribe when the output window is closed
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(final ImageJEvent evt) {
		final boolean okApplication = showApp && evt instanceof ApplicationEvent;
		final boolean isDisplayEvent =
			evt instanceof DisplayEvent && !(evt instanceof InputEvent) &&
				!(evt instanceof CanvasEvent);
		final boolean okDisplay = showDisplay && isDisplayEvent;
		/* DisplayEvent
				InputEvent
					KyEvent
					MsEvent
				CanvasEvent
					ZoomEvent
		TODO add DataViewEvent
		*/
		
		final boolean okMouseCursor = showMouseCursor && evt instanceof MouseCursorEvent;
		final boolean okMsMoved = showMsMoved && evt instanceof MsMovedEvent;
		final boolean okMsButton = showMsButton && evt instanceof MsButtonEvent;
		final boolean okKy = showKy && evt instanceof KyEvent;

		final boolean okFile = showFile && evt instanceof FileEvent;
		final boolean okList = showList && evt instanceof ListEvent;
		final boolean okModule = showModule && evt instanceof ModuleEvent;
		final boolean okObject = showObject && evt instanceof ObjectEvent;
		final boolean okOptions = showOptions && evt instanceof OptionsEvent;
		final boolean okStatus = showStatus && evt instanceof StatusEvent;
		final boolean okTool = showTool && evt instanceof ToolEvent;
		final boolean okZoom = showZoom && evt instanceof ZoomEvent;

		if (okApplication || okDisplay || okMouseCursor || okMsButton || okMsMoved || okKy ||
			okFile || okList || okModule || okObject || okOptions || okStatus ||
			okTool || okZoom)
		{
			showEvent(evt);
		}
	}

	// -- Helper methods --

	private void showEvent(final ImageJEvent evt) {
		final String eventClass = evt.getClass().getSimpleName();
		emitMessage("[" + timeStamp() + "] " + eventClass + evt);
	}

	private String timeStamp() {
		final SimpleDateFormat formatter =
			new SimpleDateFormat("hh:mm:ss.SS", Locale.getDefault());
		final Date currentDate = new Date();
		final String dateStr = formatter.format(currentDate);
		return dateStr;
	}

	private void emitMessage(final String msg) {
		if (window == null) return;
		window.append(msg + "\n");
	}

}
