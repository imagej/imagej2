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

package imagej.ext.tool;

import imagej.AbstractService;
import imagej.ImageJ;
import imagej.Service;
import imagej.event.EventService;
import imagej.event.EventSubscriber;
import imagej.ext.InstantiableException;
import imagej.ext.display.event.input.KyPressedEvent;
import imagej.ext.display.event.input.KyReleasedEvent;
import imagej.ext.display.event.input.MsClickedEvent;
import imagej.ext.display.event.input.MsDraggedEvent;
import imagej.ext.display.event.input.MsMovedEvent;
import imagej.ext.display.event.input.MsPressedEvent;
import imagej.ext.display.event.input.MsReleasedEvent;
import imagej.ext.display.event.input.MsWheelEvent;
import imagej.ext.tool.event.ToolActivatedEvent;
import imagej.ext.tool.event.ToolDeactivatedEvent;
import imagej.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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

	private Map<String, ITool> alwaysActiveTools;
	private Map<String, ITool> tools;

	private List<ITool> alwaysActiveToolList;
	private List<ITool> toolList;

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
		final ITool alwaysActiveTool = alwaysActiveTools.get(name);
		if (alwaysActiveTool != null) return alwaysActiveTool;
		return tools.get(name);
	}
	
	/**
	 * Get a tool given its class.
	 * 
	 * @param <T> the tool's type
	 * @param toolClass the class of the tool to fetch
	 * @return the tool, or null if no such tool
	 */
	public <T extends ITool> T getTool(final Class<T> toolClass) {
		for (ITool tool: alwaysActiveToolList) {
			if (toolClass.isInstance(tool)) return toolClass.cast(tool);
		}
		for (ITool tool: toolList) {
			if (toolClass.isInstance(tool)) return toolClass.cast(tool);
		}
		return null;
	}

	public List<ITool> getTools() {
		return toolList;
	}

	public List<ITool> getAlwaysActiveTools() {
		return alwaysActiveToolList;
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
		alwaysActiveTools = new HashMap<String, ITool>();
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
			if (info.isAlwaysActive()) {
				alwaysActiveTools.put(info.getName(), tool);
			}
			else {
				tools.put(info.getName(), tool);
			}
		}

		// sort tools by priority
		final Comparator<ITool> toolComparator = new Comparator<ITool>() {

			@Override
			public int compare(final ITool tool1, final ITool tool2) {
				return tool1.getInfo().compareTo(tool2.getInfo());
			}
		};
		alwaysActiveToolList = createSortedList(alwaysActiveTools.values(), toolComparator);
		toolList = createSortedList(tools.values(), toolComparator);
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

	private <T> List<T> createSortedList(final Collection<T> c,
		final Comparator<? super T> comparator)
	{
		final ArrayList<T> list = new ArrayList<T>(c);
		Collections.sort(list, comparator);
		return Collections.unmodifiableList(list);
	}

	private void subscribeToEvents() {
		subscribers = new ArrayList<EventSubscriber<?>>();

		final EventSubscriber<KyPressedEvent> kyPressedSubscriber =
			new EventSubscriber<KyPressedEvent>() {

				@Override
				public void onEvent(final KyPressedEvent event) {
					if (event.isConsumed()) return;
					getActiveTool().onKeyDown(event);
					for (final ITool tool : getAlwaysActiveTools()) {
						if (event.isConsumed()) break;
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
					if (event.isConsumed()) return;
					getActiveTool().onKeyUp(event);
					for (final ITool tool : getAlwaysActiveTools()) {
						if (event.isConsumed()) break;
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
					if (event.isConsumed()) return;
					getActiveTool().onMouseDown(event);
					for (final ITool tool : getAlwaysActiveTools()) {
						if (event.isConsumed()) break;
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
					if (event.isConsumed()) return;
					getActiveTool().onMouseUp(event);
					for (final ITool tool : getAlwaysActiveTools()) {
						if (event.isConsumed()) break;
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
					if (event.isConsumed()) return;
					getActiveTool().onMouseClick(event);
					for (final ITool tool : getAlwaysActiveTools()) {
						if (event.isConsumed()) break;
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
					if (event.isConsumed()) return;
					getActiveTool().onMouseMove(event);
					for (final ITool tool : getAlwaysActiveTools()) {
						if (event.isConsumed()) break;
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
					if (event.isConsumed()) return;
					getActiveTool().onMouseDrag(event);
					for (final ITool tool : getAlwaysActiveTools()) {
						if (event.isConsumed()) break;
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
					if (event.isConsumed()) return;
					getActiveTool().onMouseWheel(event);
					for (final ITool tool : getAlwaysActiveTools()) {
						if (event.isConsumed()) break;
						tool.onMouseWheel(event);
					}
				}
			};
		subscribers.add(msWheelSubscriber);
		eventService.subscribe(MsWheelEvent.class, msWheelSubscriber);
	}

}
