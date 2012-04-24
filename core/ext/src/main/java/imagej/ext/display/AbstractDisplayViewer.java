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
package imagej.ext.display;

import java.util.List;

import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.event.EventSubscriber;
import imagej.ext.display.DisplayViewer;
import imagej.ext.display.event.DisplayActivatedEvent;
import imagej.ext.display.event.DisplayDeletedEvent;
import imagej.ext.display.event.DisplayUpdatedEvent;
import imagej.ext.display.event.DisplayUpdatedEvent.DisplayUpdateLevel;


/**
 * @author Lee Kamentsky
 *
 * The AbstractDisplayViewer provides some basic generic implementations
 * for a DisplayViewer such as storing and providing the display, window
 * and panel for a DisplayViewer.
 */
public abstract class AbstractDisplayViewer<T> implements DisplayViewer<T> {
	protected Display<T> display;
	protected DisplayWindow window;
	protected DisplayPanel panel;
	List<EventSubscriber<?>> subscribers;
	
	public AbstractDisplayViewer() {
	}

	@SuppressWarnings("unchecked")
	@Override
	public void view(DisplayWindow window, Display<?> display) {
		this.window = window;
		assert this.canView(display);
		this.display = (Display<T>)display;
		EventService eventService = display.getContext().getService(EventService.class);
		subscribers = eventService.subscribe(this);
	}

	@Override
	public Display<T> getDisplay() {
		return display;
	}

	@Override
	public DisplayWindow getDisplayWindow() {
		return window;
	}

	@Override
	public void setPanel(DisplayPanel panel) {
		this.panel = panel;
	}

	@Override
	public DisplayPanel getPanel() {
		return panel;
	}
	
	//-- Display lifecycle events --//
	@EventHandler
	protected void onEvent(DisplayActivatedEvent e) {
		if (e.getDisplay() == getDisplay())
			onDisplayActivated(e);
	}
	
	/**
	 * Activate a display
	 * @param e
	 */
	protected void onDisplayActivated(DisplayActivatedEvent e) {
		getPanel().getWindow().requestFocus();
	}

	@EventHandler
	protected void onEvent(DisplayDeletedEvent e) {
		if (e.getObject() == getDisplay())
			onDisplayDeleted(e);
	}

	/**
	 * Implement the user interface for deleting a display
	 * @param e
	 */
	protected void onDisplayDeleted(DisplayDeletedEvent e) {
		getPanel().getWindow().close();
	}

	
	@EventHandler
	protected void onEvent(DisplayUpdatedEvent e) {
		if (e.getDisplay() == getDisplay()) {
			onDisplayUpdated(e);
		}
	}
	/**
	 * Synchronize the user interface appearance with that
	 * of the display model.
	 *  
	 * @param e
	 */
	protected void onDisplayUpdated(DisplayUpdatedEvent e) {
		if (e.getLevel() == DisplayUpdateLevel.REBUILD)
			getPanel().redoLayout();
		getPanel().redraw();
	}
	
}
