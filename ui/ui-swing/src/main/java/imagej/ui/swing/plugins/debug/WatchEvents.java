//
// EvtListener.java
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

package imagej.ui.swing.plugins.debug;

import imagej.display.event.DisplayEvent;
import imagej.display.event.DisplayViewEvent;
import imagej.display.event.key.KyEvent;
import imagej.display.event.mouse.MsEvent;
import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.event.ImageJEvent;
import imagej.module.ModuleItem;
import imagej.object.event.ObjectEvent;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Plugin;
import imagej.plugin.PluginModule;
import imagej.plugin.event.PluginEvent;
import imagej.plugin.event.PluginPreprocessEvent;
import imagej.plugin.event.PluginRunEvent;
import imagej.tool.event.ToolEvent;
import imagej.ui.swing.StaticSwingUtils;
import imagej.ui.swing.SwingOutputWindow;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

/**
 * Listens for all events. Useful for logging, history, macro recording,
 * perhaps. An eagerly initialized singleton.
 * 
 * * 
 All Event Types
 -----------------------------------------------
 Grouped by package
 * 
ImageJEvent
 * 
StatusEvent
OutputEvent
 * 
AppAboutEvent
AppFocusEvent
ApplicationEvent
AppMenusCreatedEvent
AppPreferencesEvent
AppPrintEvent
AppQuitEvent
AppReOpenEvent
AppScreenSleepEvent
AppSystemSleepEvent
AppUserSessionEvent
AppVisibleEvent
 * 
ObjectEvent
ObjectChangedEvent
ObjectCreatedEvent
ObjectDeletedEvent
 * 
ObjectsUpdatedEvent (pub'd after add or remove)
 * 
DataObjectChangedEvent
DataObjectCreatedEvent
DataObjectDeletedEvent
DataObjectRestructuredEvent
DataObjectUpdatedEvent
 * 
DatasetCreatedEvent
DatasetDeletedEvent
DatasetRestructuredEvent
DatasetRGBChangedEvent
DatasetTypeChangedEvent
DatasetUpdatedEvent
 * 
OverlayCreatedEvent
OverlayDeletedEvent
OverlayRestructuredEvent
OverlayUpdatedEvent
 * 
CanvasEvent
*
DisplayEvent
DisplayCreatedEvent
DisplayDeletedEvent

DisplayViewEvent
DisplayViewDeselectedEvent
DisplayViewSelectedEvent
DisplayViewSelectionEvent
 * 
KyEvent
KyPressedEvent
KyReleasedEvent
KyTypedEvent
 * 
MsEvent
MsButtonEvent
MsClickedEvent
MsDraggedEvent
MsEnteredEvent
MsExitedEvent
MsMovedEvent
MsPressedEvent
MsReleasedEvent
MsWheelEvent
 * 
WinEvent
WinActivatedEvent
WinClosedEvent
WinClosingEvent
WinDeactivatedEvent
WinDeiconifiedEvent
WinIconifiedEvent
WinOpenedEvent
 * 
ZoomEvent
 * 
PluginEvent
PluginCanceledEvent
PluginExecutionEvent
PluginFinishedEvent
PluginPostprocessEvent
PluginPreprocessEvent
PluginProcessEvent
PluginRunEvent
PluginStartedEvent
 * 
ToolEvent
ToolActivatedEvent
ToolDeactivatedEvent
-------------------------------------------------------
Event Hierarchy
 
All decend from ImageJEvent:
 
StatusEvent
OutputEvent
FileOpenedEvent
FileSavedEvent
ApplicationEvent
	AppAboutEvent
	AppFocusEvent
	AppMenusCreatedEvent
	AppPreferencesEvent
	AppPrintEvent
	AppQuitEvent
	AppReOpenEvent
	AppScreenSleepEvent
	AppSystemSleepEvent
	AppUserSessionEvent
	AppVisibleEvent
ObjectEvent
	ObjectChangedEvent
		DataObjectChangedEvent
			DataObjectRestructuredEvent
				DatasetRestructuredEvent
				OverlayRestructuredEvent
			DataObjectUpdatedEvent
				DatasetUpdatedEvent
					DatasetRGBChangedEvent
					DatasetTypeChangedEvent
			OverlayUpdatedEvent
	ObjectCreatedEvent
		DataObjectCreatedEvent
			DatasetCreatedEvent
			DisplayCreatedEvent
			OverlayCreatedEvent
	ObjectDeletedEvent
		DataObjectDeletedEvent
			DatasetDeletedEvent
			DisplayDeletedEvent
			OverlayDeletedEvent
	ObjectsUpdatedEvent (pub'd after add or remove)
PluginEvent
	PluginExecutionEvent
		PluginRunEvent
		PluginStartedEvent
		PluginFinishedEvent
		PluginProcessEvent
			PluginPostprocessEvent
			PluginPreprocessEvent
		PluginCanceledEvent
ToolEvent
	ToolActivatedEvent
	ToolDeactivatedEvent
CanvasEvent
	ZoomEvent
DisplayEvent
	AxisPositionEvent
	DisplayViewEvent
		DisplayViewDeselectedEvent
		DisplayViewSelectedEvent
		DisplayViewSelectionEvent
	KyEvent
		KyPressedEvent
		KyReleasedEvent
		KyTypedEvent
	MsEvent
		MsButtonEvent
		MsClickedEvent
		MsDraggedEvent
		MsEnteredEvent
		MsExitedEvent
		MsMovedEvent
		MsPressedEvent
		MsReleasedEvent
		MsWheelEvent
	WinEvent
		WinActivatedEvent
		WinClosedEvent
		WinClosingEvent
		WinDeactivatedEvent
		WinDeiconifiedEvent
		WinIconifiedEvent
		WinOpenedEvent


 * 
 * @author GBH
 */
@Plugin(menuPath = "Plugins>Debug>Watch Events")
public class WatchEvents implements ImageJPlugin, EventSubscriber<ImageJEvent>
{

	private final static WatchEvents listener = new WatchEvents();
	private static SwingOutputWindow window;

	@Override
	public void run() {
		window = new SwingOutputWindow("EventMonitor");
		StaticSwingUtils.locateUpperRight(window);
		Events.subscribe(ImageJEvent.class, listener);
	}

	@Override
	public void onEvent(final ImageJEvent evt) {
//		if (evt instanceof ImageJEvent) {
//			emitMessage("Event: " + evt.getClass());
//			showFields(evt);
//		} 
		if (PluginEvent.class.isAssignableFrom(evt.getClass())) {
			emitMessage(evt.getClass().getSimpleName());
			if (evt instanceof PluginPreprocessEvent) {
				emitMessage("  >> PreProcessor: " +
					((PluginPreprocessEvent) evt).getProcessor().getClass()
						.getSimpleName());
			}
			if (evt instanceof PluginRunEvent) {
				final PluginModule<?> mod = ((PluginEvent) evt).getModule();
				emitMessage("  >> Plugin: " +
					mod.getInfo().getPluginEntry().getClassName());
				// parameter inputs
				final Iterable<ModuleItem> inputs = mod.getInfo().inputs();
				for (final ModuleItem moduleItem : inputs) {
					final String param = moduleItem.getName();
					final Class<?> type = moduleItem.getType();
					final Object value = mod.getInput(param);
					emitMessage("    " + param + " = " + value.toString() + "  {" +
						type.getSimpleName() + "}");
				}
			}
		}
		if (evt instanceof DisplayEvent) {
			if (!(evt instanceof MsEvent) && !(evt instanceof KyEvent)) {
				showClass(evt);
				showFields(evt);
				emitMessage("    DisplayName: " +  ((DisplayEvent)evt).getDisplay().getName());
			}
		}
		if (evt instanceof DisplayViewEvent) {
			showClass(evt);
			showFields(evt);
		}
		if (evt instanceof ObjectEvent) {
			showClass(evt);
			showFields(evt);
		}
		if (evt instanceof ToolEvent) {
			showClass(evt);
			showFields(evt);
		}
	}

	void emitMessage(final String msg) {
		if (window != null) {
			window.append(msg + "\n");
		}
	}
   public static String timeStamp() {
      SimpleDateFormat formatter =
            new SimpleDateFormat("mm:ss.SS", Locale.getDefault());
      Date currentDate = new Date();
      String dateStr = formatter.format(currentDate);
      return dateStr;
   }
   
	void showClass(final ImageJEvent evt) {
		emitMessage("[" + timeStamp() + "] " + evt.getClass().getSimpleName());
	}

	public void showFields(final Object evt) {
		try {
			final ArrayList<Field> allFields = getAllFields(evt.getClass());
			for (final Field fld : allFields) {
				fld.setAccessible(true);
				emitMessage("    " + fld.getName() + "= " + fld.get(evt).toString());
				// emitMessage("    " + fld.getName() + "[" + fld.getType() + "] = " +
				// fld.get(evt).toString());
			}
		}
		catch (final Throwable e) {
			System.err.println(e);
		}
	}

	/**
	 * Return the set of fields declared at all level of class hierachy
	 */
	public ArrayList<Field> getAllFields(final Class<?> clazz) {
		return getAllFieldsRec(clazz, new ArrayList<Field>());
	}

	private ArrayList<Field> getAllFieldsRec(final Class<?> clazz,
		final ArrayList<Field> ArrayList)
	{
		final Class<?> superClazz = clazz.getSuperclass();
		if (superClazz != null) {
			getAllFieldsRec(superClazz, ArrayList);
		}
		ArrayList.addAll(Arrays.asList(clazz.getDeclaredFields()));
		return ArrayList;
	}

}
