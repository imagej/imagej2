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

package imagej.ext.tool;

import imagej.ImageJ;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.ext.InstantiableException;
import imagej.ext.display.event.DisplayEvent;
import imagej.ext.display.event.input.KyPressedEvent;
import imagej.ext.display.event.input.KyReleasedEvent;
import imagej.ext.display.event.input.MsClickedEvent;
import imagej.ext.display.event.input.MsDraggedEvent;
import imagej.ext.display.event.input.MsMovedEvent;
import imagej.ext.display.event.input.MsPressedEvent;
import imagej.ext.display.event.input.MsReleasedEvent;
import imagej.ext.display.event.input.MsWheelEvent;
import imagej.ext.plugin.PluginInfo;
import imagej.ext.plugin.PluginService;
import imagej.ext.tool.event.ToolActivatedEvent;
import imagej.ext.tool.event.ToolDeactivatedEvent;
import imagej.service.AbstractService;
import imagej.service.Service;
import imagej.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default service for keeping track of available tools, including which tool is
 * active, and delegating UI events to the active tool.
 * 
 * @author Grant Harris
 * @author Curtis Rueden
 * @see Tool
 * @see Tool
 */
@Service
public class DefaultToolService extends AbstractService implements ToolService {

	private static final double SEPARATOR_DISTANCE = 10;

	private final EventService eventService;
	private final PluginService pluginService;

	private Map<String, Tool> alwaysActiveTools;
	private Map<String, Tool> tools;

	private List<Tool> alwaysActiveToolList;
	private List<Tool> toolList;

	private Tool activeTool;

	// -- Constructors --

	public DefaultToolService() {
		// NB: Required by SezPoz.
		super(null);
		throw new UnsupportedOperationException();
	}

	public DefaultToolService(final ImageJ context, final EventService eventService,
		final PluginService pluginService)
	{
		super(context);
		this.eventService = eventService;
		this.pluginService = pluginService;

		createTools();
		activeTool = new DummyTool();

		subscribeToEvents(eventService);
	}

	// -- ToolService methods --

	@Override
	public EventService getEventService() {
		return eventService;
	}

	@Override
	public PluginService getPluginService() {
		return pluginService;
	}

	@Override
	public Tool getTool(final String name) {
		final Tool alwaysActiveTool = alwaysActiveTools.get(name);
		if (alwaysActiveTool != null) return alwaysActiveTool;
		return tools.get(name);
	}

	@Override
	public <T extends Tool> T getTool(final Class<T> toolClass) {
		for (final Tool tool : alwaysActiveToolList) {
			if (toolClass.isInstance(tool)) return toolClass.cast(tool);
		}
		for (final Tool tool : toolList) {
			if (toolClass.isInstance(tool)) return toolClass.cast(tool);
		}
		return null;
	}

	@Override
	public List<Tool> getTools() {
		return toolList;
	}

	@Override
	public List<Tool> getAlwaysActiveTools() {
		return alwaysActiveToolList;
	}

	@Override
	public Tool getActiveTool() {
		return activeTool;
	}

	@Override
	public void setActiveTool(final Tool activeTool) {
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

	@Override
	public boolean isSeparatorNeeded(final Tool tool1, final Tool tool2) {
		if (tool1 == null || tool2 == null) return false;
		final double priority1 = tool1.getInfo().getPriority();
		final double priority2 = tool2.getInfo().getPriority();
		return Math.abs(priority1 - priority2) >= SEPARATOR_DISTANCE;
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(final KyPressedEvent event) {
		if (event.isConsumed()) return;
		final Tool aTool = getActiveTool();
		if (eventOk(event, aTool)) aTool.onKeyDown(event);
		for (final Tool tool : getAlwaysActiveTools()) {
			if (event.isConsumed()) break;
			if (eventOk(event, tool)) tool.onKeyDown(event);
		}
	}

	@EventHandler
	protected void onEvent(final KyReleasedEvent event) {
		if (event.isConsumed()) return;
		final Tool aTool = getActiveTool();
		if (eventOk(event, aTool)) aTool.onKeyUp(event);
		for (final Tool tool : getAlwaysActiveTools()) {
			if (event.isConsumed()) break;
			if (eventOk(event, tool)) tool.onKeyUp(event);
		}
	}

	@EventHandler
	protected void onEvent(final MsPressedEvent event) {
		if (event.isConsumed()) return;
		final Tool aTool = getActiveTool();
		if (eventOk(event, aTool)) aTool.onMouseDown(event);
		for (final Tool tool : getAlwaysActiveTools()) {
			if (event.isConsumed()) break;
			if (eventOk(event, tool)) tool.onMouseDown(event);
		}
	}

	@EventHandler
	protected void onEvent(final MsReleasedEvent event) {
		if (event.isConsumed()) return;
		final Tool aTool = getActiveTool();
		if (eventOk(event, aTool)) aTool.onMouseUp(event);
		for (final Tool tool : getAlwaysActiveTools()) {
			if (event.isConsumed()) break;
			if (eventOk(event, tool)) tool.onMouseUp(event);
		}
	}

	@EventHandler
	protected void onEvent(final MsClickedEvent event) {
		if (event.isConsumed()) return;
		final Tool aTool = getActiveTool();
		if (eventOk(event, aTool)) aTool.onMouseClick(event);
		for (final Tool tool : getAlwaysActiveTools()) {
			if (event.isConsumed()) break;
			if (eventOk(event, tool)) tool.onMouseClick(event);
		}
	}

	@EventHandler
	protected void onEvent(final MsMovedEvent event) {
		if (event.isConsumed()) return;
		final Tool aTool = getActiveTool();
		if (eventOk(event, aTool)) aTool.onMouseMove(event);
		for (final Tool tool : getAlwaysActiveTools()) {
			if (event.isConsumed()) break;
			if (eventOk(event, tool)) tool.onMouseMove(event);
		}
	}

	@EventHandler
	protected void onEvent(final MsDraggedEvent event) {
		if (event.isConsumed()) return;
		final Tool aTool = getActiveTool();
		if (eventOk(event, aTool)) aTool.onMouseDrag(event);
		for (final Tool tool : getAlwaysActiveTools()) {
			if (event.isConsumed()) break;
			if (eventOk(event, tool)) tool.onMouseDrag(event);
		}
	}

	@EventHandler
	protected void onEvent(final MsWheelEvent event) {
		if (event.isConsumed()) return;
		final Tool aTool = getActiveTool();
		if (eventOk(event, aTool)) aTool.onMouseWheel(event);
		for (final Tool tool : getAlwaysActiveTools()) {
			if (event.isConsumed()) break;
			if (eventOk(event, tool)) tool.onMouseWheel(event);
		}
	}

	// -- Helper methods --

	private void createTools() {
		// discover available tools
		final List<PluginInfo<Tool>> toolEntries =
			pluginService.getPluginsOfType(Tool.class);

		// create tool instances
		alwaysActiveTools = new HashMap<String, Tool>();
		alwaysActiveToolList = new ArrayList<Tool>();
		tools = new HashMap<String, Tool>();
		toolList = new ArrayList<Tool>();
		for (final PluginInfo<Tool> info : toolEntries) {
			final Tool tool;
			try {
				tool = info.createInstance();
				tool.setContext(getContext());
				tool.setInfo(info);
			}
			catch (final InstantiableException e) {
				Log.error("Invalid tool: " + info.getName(), e);
				continue;
			}
			if (info.isAlwaysActive()) {
				alwaysActiveTools.put(info.getName(), tool);
				alwaysActiveToolList.add(tool);
			}
			else {
				tools.put(info.getName(), tool);
				toolList.add(tool);
			}
		}
	}

	/** Checks that an event is OK to be dispatched to a particular tool. */
	private boolean eventOk(final DisplayEvent event, final Tool tool) {
		if (event.getDisplay() != null) return true;
		// NB: An event with a null display came from the main app frame.
		// We only pass these events on to tools flagged with activeInAppFrame.
		final PluginInfo<?> toolInfo = tool == null ? null : tool.getInfo();
		return toolInfo != null && toolInfo.isActiveInAppFrame();
	}

}
