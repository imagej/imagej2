/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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

import org.scijava.app.StatusService;
import org.scijava.event.EventHandler;
import org.scijava.plugin.AbstractHandlerService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;

/**
 * Default service for handling drag and drop events.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public class DefaultDragAndDropService extends
	AbstractHandlerService<Object, DragAndDropHandler<Object>> implements
	DragAndDropService
{

	private static final String SUPPORTED = "Drag and Drop";
	private static final String UNSUPPORTED = "Unsupported Object";

	@Parameter
	private StatusService statusService;

	// -- DragAndDropService methods --

	@Override
	public boolean supports(final DragAndDropData data,
		final Display<?> display)
	{
		return getHandler(data, display) != null;
	}

	@Override
	public boolean supports(final Object object, final Display<?> display) {
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
		for (final DragAndDropHandler<?> handler : getInstances()) {
			if (handler.supportsData(data, display)) return handler;
		}
		return null;
	}

	@Override
	public DragAndDropHandler<?> getHandler(final Object object,
		final Display<?> display)
	{
		for (final DragAndDropHandler<?> handler : getInstances()) {
			if (handler.supportsObject(object, display)) return handler;
		}
		return null;
	}

	// -- PTService methods --

	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	public Class<DragAndDropHandler<Object>> getPluginType() {
		return (Class) DragAndDropHandler.class;
	}

	// -- Typed methods --

	@Override
	public Class<Object> getType() {
		return Object.class;
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(final DragEnterEvent e) {
		// determine whether the given drop operation is supported
		final boolean compatible = supports(e.getData(), e.getDisplay());

		// update the ImageJ status accordingly
		final String message = compatible ? SUPPORTED : UNSUPPORTED;
		statusService.showStatus("< <" + message + "> >");

		// accept the drag if the operation is supported
		if (compatible) e.setAccepted(true);
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
		if (!supports(e.getData(), e.getDisplay())) return;

		// perform the drop
		final boolean success = drop(e.getData(), e.getDisplay());
		e.setSuccessful(success);
	}

}
