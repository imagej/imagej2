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
 * #L%
 */

package imagej.ui.common.awt;

import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import net.imagej.display.InputService;

import org.scijava.display.Display;
import org.scijava.event.EventService;
import org.scijava.input.InputModifiers;
import org.scijava.ui.dnd.DragAndDropData;
import org.scijava.ui.dnd.DragAndDropService;
import org.scijava.ui.dnd.event.DragAndDropEvent;
import org.scijava.ui.dnd.event.DragEnterEvent;
import org.scijava.ui.dnd.event.DragExitEvent;
import org.scijava.ui.dnd.event.DragOverEvent;
import org.scijava.ui.dnd.event.DropEvent;

/**
 * Rebroadcasts AWT {@link DropTargetEvent}s as ImageJ {@link DragAndDropEvent}
 * s.
 * 
 * @author Curtis Rueden
 */
public class AWTDropTargetEventDispatcher implements DropTargetListener {

	private final Display<?> display;

	private final EventService eventService;

	/** Creates an AWT drag-and-drop event dispatcher for the given display. */
	public AWTDropTargetEventDispatcher(final Display<?> display,
		final EventService eventService)
	{
		this.display = display;
		this.eventService = eventService;
	}

	// -- AWTDropTargetEventDispatcher methods --

	public void register(final Component c) {
		new DropTarget(c, this);
	}

	// -- DropTargetListener methods --

	@Override
	public void dragEnter(final DropTargetDragEvent e) {
		final InputModifiers mods = getModifiers();
		final Point p = e.getLocation();
		final DragAndDropData data = createData(e.getTransferable());
		final DragEnterEvent dragEnter =
			new DragEnterEvent(display, mods, p.x, p.y, data);

		eventService.publish(dragEnter);

		if (dragEnter.isAccepted()) e.acceptDrag(DnDConstants.ACTION_COPY);
		else e.rejectDrag();
	}

	@Override
	public void dragOver(final DropTargetDragEvent e) {
		final InputModifiers mods = getModifiers();
		final Point p = e.getLocation();
		final DragAndDropData data = createData(e.getTransferable());

		eventService.publish(new DragOverEvent(display, mods, p.x, p.y, data));
	}

	@Override
	public void dropActionChanged(final DropTargetDragEvent e) {
		// NB: No action needed.
	}

	@Override
	public void dragExit(final DropTargetEvent e) {
		eventService.publish(new DragExitEvent(display));
	}

	@Override
	public void drop(final DropTargetDropEvent e) {
		final InputModifiers mods = getModifiers();
		final Point p = e.getLocation();
		final DragAndDropData data = createData(e.getTransferable());
		final DropEvent drop = new DropEvent(display, mods, p.x, p.y, data);

		final DragAndDropService dragAndDropService =
			eventService.getContext().getService(DragAndDropService.class);
		if (dragAndDropService != null &&
			dragAndDropService.supports(data, display))
		{
			e.acceptDrop(DnDConstants.ACTION_COPY);
		}
		else e.rejectDrop();

		eventService.publish(drop);

		e.dropComplete(drop.isSuccessful());
	}

	// -- Helper methods --

	private InputModifiers getModifiers() {
		final InputService inputService =
			eventService.getContext().getService(InputService.class);
		return inputService == null ? null : inputService.getModifiers();
	}

	private DragAndDropData createData(final Transferable t) {
		return new AWTDragAndDropData(eventService.getContext(), t);
	}

}
