//
// ToolService.java
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

package imagej.tool;

import imagej.AbstractService;
import imagej.ImageJ;
import imagej.Service;
import imagej.display.event.key.KyPressedEvent;
import imagej.display.event.key.KyReleasedEvent;
import imagej.display.event.mouse.MsClickedEvent;
import imagej.display.event.mouse.MsDraggedEvent;
import imagej.display.event.mouse.MsMovedEvent;
import imagej.display.event.mouse.MsPressedEvent;
import imagej.display.event.mouse.MsReleasedEvent;
import imagej.display.event.mouse.MsWheelEvent;
import imagej.event.EventService;
import imagej.event.EventSubscriber;
import imagej.ext.InstantiableException;
import imagej.tool.event.ToolActivatedEvent;
import imagej.tool.event.ToolDeactivatedEvent;
import imagej.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

/**
 * Service for keeping track of available tools, including which tool is active,
 * and delegating UI events to the active tool.
 * 
 * @author Grant Harris
 * @author Curtis Rueden
 * @see ITool
 * @see Tool
 */
@Service
public class ToolService extends AbstractService {

	private final EventService eventService;

	private Map<String, ITool> globalTools;
	private Map<String, ITool> tools;

	/** Maintain list of subscribers, to avoid garbage collection. */
	private List<EventSubscriber<?>> subscribers;

	private ITool activeTool;

	// -- Constructors --

	public ToolService() {
		// NB: Required by SezPoz.
		super(null);
		throw new UnsupportedOperationException();
	}

	public ToolService(final ImageJ context, final EventService eventService) {
		super(context);
		this.eventService = eventService;
	}

	// -- ToolService methods --

	public ITool getTool(final String name) {
		final ITool globalTool = globalTools.get(name);
		if (globalTool != null) return globalTool;
		return tools.get(name);
	}

	public Collection<ITool> getTools() {
		return tools.values();
	}

	public Collection<ITool> getGlobalTools() {
		return globalTools.values();
	}

	public ITool getActiveTool() {
		return activeTool;
	}

	public void setActiveTool(final ITool activeTool) {
		if (this.activeTool == activeTool) return; // nothing to do
		assert this.activeTool != null;
		if (activeTool == null) {
			throw new IllegalArgumentException("Active tool cannot be null");
		}

		// deactivate old tool
		this.activeTool.deactivate();
		eventService.publish(new ToolDeactivatedEvent(this.activeTool));

		// activate new tool
		this.activeTool = activeTool;
		activeTool.activate();
		eventService.publish(new ToolActivatedEvent(activeTool));
	}

	// -- IService methods --

	@Override
	public void initialize() {
		createTools();
		activeTool = new DummyTool();
		subscribeToEvents();
	}

	// -- Helper methods --

	private void createTools() {
		// discover available tools
		final List<ToolInfo> toolEntries = findTools();
		Collections.sort(toolEntries);

		// create tool instances
		globalTools = new HashMap<String, ITool>();
		tools = new HashMap<String, ITool>();
		for (final ToolInfo info : toolEntries) {
			final ITool tool;
			try {
				tool = info.createInstance();
				tool.setInfo(info);
			}
			catch (final InstantiableException e) {
				Log.error("Invalid tool: " + info.getName(), e);
				continue;
			}
			if (info.isGlobal()) {
				globalTools.put(info.getName(), tool);
			}
			else {
				tools.put(info.getName(), tool);
			}
		}
	}

	/** Discovers tools using SezPoz. */
	private List<ToolInfo> findTools() {
		final Index<Tool, ITool> toolIndex = Index.load(Tool.class, ITool.class);
		final List<ToolInfo> entries = new ArrayList<ToolInfo>();
		for (final IndexItem<Tool, ITool> item : toolIndex) {
			entries.add(createInfo(item));
		}
		return entries;
	}

	private ToolInfo createInfo(final IndexItem<Tool, ITool> item) {
		final String className = item.className();
		final Tool tool = item.annotation();
		return new ToolInfo(className, tool);
	}

	private void subscribeToEvents() {
		subscribers = new ArrayList<EventSubscriber<?>>();

		final EventSubscriber<KyPressedEvent> kyPressedSubscriber =
			new EventSubscriber<KyPressedEvent>() {

				@Override
				public void onEvent(final KyPressedEvent event) {
					getActiveTool().onKeyDown(event);
					for (ITool tool : getGlobalTools()) {
						tool.onKeyDown(event);
					}
				}
			};
		subscribers.add(kyPressedSubscriber);
		eventService.subscribe(KyPressedEvent.class, kyPressedSubscriber);

		final EventSubscriber<KyReleasedEvent> kyReleasedSubscriber =
			new EventSubscriber<KyReleasedEvent>() {

				@Override
				public void onEvent(final KyReleasedEvent event) {
					getActiveTool().onKeyUp(event);
					for (ITool tool : getGlobalTools()) {
						tool.onKeyUp(event);
					}
				}
			};
		subscribers.add(kyReleasedSubscriber);
		eventService.subscribe(KyReleasedEvent.class, kyReleasedSubscriber);

		final EventSubscriber<MsPressedEvent> msPressedSubscriber =
			new EventSubscriber<MsPressedEvent>() {

				@Override
				public void onEvent(final MsPressedEvent event) {
					getActiveTool().onMouseDown(event);
					for (ITool tool : getGlobalTools()) {
						tool.onMouseDown(event);
					}
				}
			};
		subscribers.add(msPressedSubscriber);
		eventService.subscribe(MsPressedEvent.class, msPressedSubscriber);

		final EventSubscriber<MsReleasedEvent> msReleasedSubscriber =
			new EventSubscriber<MsReleasedEvent>() {

				@Override
				public void onEvent(final MsReleasedEvent event) {
					getActiveTool().onMouseUp(event);
					for (ITool tool : getGlobalTools()) {
						tool.onMouseUp(event);
					}
				}
			};
		subscribers.add(msReleasedSubscriber);
		eventService.subscribe(MsReleasedEvent.class, msReleasedSubscriber);

		final EventSubscriber<MsClickedEvent> msClickedSubscriber =
			new EventSubscriber<MsClickedEvent>() {

				@Override
				public void onEvent(final MsClickedEvent event) {
					getActiveTool().onMouseClick(event);
					for (ITool tool : getGlobalTools()) {
						tool.onMouseClick(event);
					}
				}
			};
		subscribers.add(msClickedSubscriber);
		eventService.subscribe(MsClickedEvent.class, msClickedSubscriber);

		final EventSubscriber<MsMovedEvent> msMovedSubscriber =
			new EventSubscriber<MsMovedEvent>() {

				@Override
				public void onEvent(final MsMovedEvent event) {
					getActiveTool().onMouseMove(event);
					for (ITool tool : getGlobalTools()) {
						tool.onMouseMove(event);
					}
				}
			};
		subscribers.add(msMovedSubscriber);
		eventService.subscribe(MsMovedEvent.class, msMovedSubscriber);

		final EventSubscriber<MsDraggedEvent> msDraggedSubscriber =
			new EventSubscriber<MsDraggedEvent>() {

				@Override
				public void onEvent(final MsDraggedEvent event) {
					getActiveTool().onMouseDrag(event);
					for (ITool tool : getGlobalTools()) {
						tool.onMouseDrag(event);
					}
				}
			};
		subscribers.add(msDraggedSubscriber);
		eventService.subscribe(MsDraggedEvent.class, msDraggedSubscriber);

		final EventSubscriber<MsWheelEvent> msWheelSubscriber =
			new EventSubscriber<MsWheelEvent>() {

				@Override
				public void onEvent(final MsWheelEvent event) {
					getActiveTool().onMouseWheel(event);
					for (ITool tool : getGlobalTools()) {
						tool.onMouseWheel(event);
					}
				}
			};
		subscribers.add(msWheelSubscriber);
		eventService.subscribe(MsWheelEvent.class, msWheelSubscriber);
	}

}
