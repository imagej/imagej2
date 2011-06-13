/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2011, ImageJDev.org.
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
package imagej.ui.swing.plugins;

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
import imagej.ui.swing.SwingOutputWindow;
import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Listens for all events... 
 * ... for logging, history, macro recording, perhaps...
 * An eagerly initialized singleton.
 * 
 * 
//ImageJEvent
//ApplicationEvent
//StatusEvent
//ObjectEvent
//CanvasEvent
//DisplayEvent
//DisplayViewEvent
//OverlayCreatedEvent
//PluginEvent
//ToolEvent
 * 
 * All event types:

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
DisplayCreatedEvent
DisplayDeletedEvent
DisplayEvent
DisplayViewDeselectedEvent
DisplayViewEvent
DisplayViewSelectedEvent
DisplayViewSelectionEvent
 * 
KyEvent
KyPressedEvent
KyReleasedEvent
KyTypedEvent
 * 
MsButtonEvent
MsClickedEvent
MsDraggedEvent
MsEnteredEvent
MsEvent
MsExitedEvent
MsMovedEvent
MsPressedEvent
MsReleasedEvent
MsWheelEvent
 * 
WinActivatedEvent
WinClosedEvent
WinClosingEvent
WinDeactivatedEvent
WinDeiconifiedEvent
WinEvent
WinIconifiedEvent
WinOpenedEvent
ZoomEvent
 * 
ImageJEvent
 * 
OutputEvent
StatusEvent
 * 
ObjectChangedEvent
ObjectCreatedEvent
ObjectDeletedEvent
ObjectEvent
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
PluginCanceledEvent
PluginEvent
PluginExecutionEvent
PluginFinishedEvent
PluginPostprocessEvent
PluginPreprocessEvent
PluginProcessEvent
PluginRunEvent
PluginStartedEvent
 * 
ToolActivatedEvent
ToolDeactivatedEvent
ToolEvent

 * 
 * @author GBH
 */
@Plugin(menuPath = "Plugins>EvtListener")
public class IJEventListener implements ImageJPlugin, EventSubscriber<ImageJEvent> {

	private final static IJEventListener listener = new IJEventListener();
	private static SwingOutputWindow window;

	@Override
	public void run() {
		window = new SwingOutputWindow("EventMonitor");
		Events.subscribe(ImageJEvent.class, listener);
	}

	@Override
	public void onEvent(ImageJEvent evt) {
//		if (evt instanceof ImageJEvent) {
//			emitMessage("Event: " + evt.getClass());
//			showFields(evt);
//		} 
		if (PluginEvent.class.isAssignableFrom(evt.getClass())) {
			emitMessage(evt.getClass().getSimpleName());
			if (evt instanceof PluginPreprocessEvent) {
				emitMessage("  >> PreProcessor: "
						+ ((PluginPreprocessEvent) evt).getProcessor().getClass().getSimpleName());
			}
			if (evt instanceof PluginRunEvent) {
				PluginModule mod = ((PluginEvent) evt).getModule();
				emitMessage("  >> Plugin: " + mod.getInfo().getPluginEntry().getClassName());
				// parameter inputs
				Iterable<ModuleItem> inputs = mod.getInfo().inputs();
				for (ModuleItem moduleItem : inputs) {
					String param = moduleItem.getName();
					Class<?> type = moduleItem.getType();
					final Object value = mod.getInput(param);
					emitMessage("    " + param + " = " + value.toString() + "  {" + type.getSimpleName() + "}");
				}
			}
		}
		if (evt instanceof DisplayEvent) {
			if (!(evt instanceof MsEvent) && !(evt instanceof KyEvent)) {
				showClass(evt);
				showFields(evt);
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

	//		if (evt instanceof DisplayEvent) {
//		//if (DisplayEvent.class.isAssignableFrom(evt.getClass())) {
//			emitMessage(evt.getClass().getName());
//		}
//		if (WinEvent.class.isAssignableFrom(evt.getClass())) {
//			emitMessage(evt.getClass().getName());
//		}
//		if (MsEvent.class.isAssignableFrom(evt.getClass())) {
//		//	emitMessage(evt.getClass().getName());
//		}
//		if (KyEvent.class.isAssignableFrom(evt.getClass())) {
//			emitMessage(evt.getClass().getName());
//		}
	void emitMessage(final String msg) {
		if (window != null) {
			window.append(msg + "\n");
		}
	}

	void showClass(ImageJEvent evt) {
		emitMessage("" + evt.getClass().getSimpleName());
	}

	public void showFields(Object evt) {
		try {
			ArrayList<Field> allFields = getAllFields(evt.getClass());
			for (Field fld : allFields) {
				fld.setAccessible(true);
				emitMessage("    " + fld.getName() + "[" + fld.getType() + "] = " + fld.get(evt).toString());
			}
		} catch (Throwable e) {
			System.err.println(e);
		}
	}

	/**
	 * Return the set of fields declared at all level of class hierachy
	 */
	public ArrayList<Field> getAllFields(Class clazz) {
		return getAllFieldsRec(clazz, new ArrayList<Field>());
	}

	private ArrayList<Field> getAllFieldsRec(Class clazz, ArrayList<Field> ArrayList) {
		Class superClazz = clazz.getSuperclass();
		if (superClazz != null) {
			getAllFieldsRec(superClazz, ArrayList);
		}
		ArrayList.addAll(Arrays.asList(clazz.getDeclaredFields()));
		return ArrayList;
	}

}
