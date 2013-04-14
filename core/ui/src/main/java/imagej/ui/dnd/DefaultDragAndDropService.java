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

package imagej.ui.dnd;

import imagej.display.Display;
import imagej.ui.dnd.event.DragEnterEvent;
import imagej.ui.dnd.event.DragExitEvent;
import imagej.ui.dnd.event.DragOverEvent;
import imagej.ui.dnd.event.DropEvent;

import java.util.Collections;
import java.util.List;

import org.scijava.app.StatusService;
import org.scijava.event.EventHandler;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * Default service for handling drag and drop events.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public class DefaultDragAndDropService extends AbstractService implements
	DragAndDropService
{

	private static final String UNSUPPORTED = "Unsupported Object";

	@Parameter
	private PluginService pluginService;

	@Parameter
	private LogService log;

	@Parameter
	private StatusService statusService;

	private List<DragAndDropHandler<?>> handlers;

	@Override
	public boolean isCompatible(final DragAndDropData data,
		final Display<?> display)
	{
		return getHandler(data, display) != null;
	}

	@Override
	public boolean isCompatible(final Object object, final Display<?> display) {
		return getHandler(object, display) != null;
	}

	@Override
	public boolean drop(final DragAndDropData data, final Display<?> display) {
		final DragAndDropHandler<?> handler = getHandler(data, display);
		if (handler == null) return false;
		return handler.dropData(data, display);
	}

	@Override
	public boolean drop(final Object data, final Display<?> display) {
		final DragAndDropHandler<?> handler = getHandler(data, display);
		if (handler == null) return false;
		return handler.dropObject(data, display);
	}

	@Override
	public DragAndDropHandler<?> getHandler(final DragAndDropData data,
		final Display<?> display)
	{
		for (final DragAndDropHandler<?> handler : getHandlers()) {
			if (handler.isCompatibleData(data, display)) return handler;
		}
		return null;
	}

	@Override
	public DragAndDropHandler<?> getHandler(final Object object,
		final Display<?> display)
	{
		for (final DragAndDropHandler<?> handler : getHandlers()) {
			if (handler.isCompatibleObject(object, display)) return handler;
		}
		return null;
	}

	@Override
	public List<DragAndDropHandler<?>> getHandlers() {
		return handlers;
	}

	// -- Service methods --

	@Override
	public void initialize() {
		// ask the plugin service for the list of available drag-and-drop handlers
		@SuppressWarnings({ "rawtypes", "unchecked" })
		final List<DragAndDropHandler<?>> instances =
			(List) pluginService.createInstancesOfType(DragAndDropHandler.class);
		handlers = Collections.unmodifiableList(instances);
		log.info("Found " + handlers.size() + " drag-and-drop handlers.");
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(final DragEnterEvent e) {
		final DragAndDropHandler<?> handler =
			getHandler(e.getData(), e.getDisplay());
		final String message =
			handler == null ? UNSUPPORTED : handler.getClass().getName();
		statusService.showStatus("< <" + message + "> >");
		if (handler == null) return;

		// accept the drag
		e.setAccepted(true);
	}

	@EventHandler
	protected void onEvent(@SuppressWarnings("unused") final DragExitEvent e) {
		statusService.clearStatus();
	}

	@EventHandler
	protected void onEvent(@SuppressWarnings("unused") final DragOverEvent e) {
		// NB: No action needed.
	}

	@EventHandler
	protected void onEvent(final DropEvent e) {
		if (!isCompatible(e.getData(), e.getDisplay())) return;

		// perform the drop
		final boolean success = drop(e.getData(), e.getDisplay());
		e.setSuccessful(success);
	}

}
