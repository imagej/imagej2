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

package imagej.ui.viewer;

import imagej.data.Dataset;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.display.Display;
import imagej.display.event.DisplayActivatedEvent;
import imagej.display.event.DisplayCreatedEvent;
import imagej.display.event.DisplayDeletedEvent;
import imagej.display.event.DisplayUpdatedEvent;
import imagej.display.event.DisplayUpdatedEvent.DisplayUpdateLevel;
import net.imglib2.img.Img;
import net.imglib2.img.cell.AbstractCellImg;

import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.plugin.SortablePlugin;

/**
 * The AbstractDisplayViewer provides some basic generic implementations for a
 * DisplayViewer such as storing and providing the display, window and panel for
 * a DisplayViewer.
 * 
 * @author Lee Kamentsky
 * @author Curtis Rueden
 */
public abstract class AbstractDisplayViewer<T> extends SortablePlugin implements
	DisplayViewer<T>
{

	private Display<T> display;
	private DisplayWindow window;
	private DisplayPanel panel;

	@Override
	public void view(final DisplayWindow w, final Display<?> d) {
		if (!canView(d)) {
			throw new IllegalArgumentException("Incompatible display: " + d);
		}
		@SuppressWarnings("unchecked")
		final Display<T> typedDisplay = (Display<T>) d;
		display = typedDisplay;
		window = w;
	}

	@Override
	public Display<T> getDisplay() {
		return display;
	}

	@Override
	public DisplayWindow getWindow() {
		return window;
	}

	@Override
	public void setPanel(final DisplayPanel panel) {
		this.panel = panel;
	}

	@Override
	public DisplayPanel getPanel() {
		return panel;
	}

	@Override
	public void onDisplayDeletedEvent(final DisplayDeletedEvent e) {
		getPanel().getWindow().close();
	}

	@Override
	public void onDisplayUpdatedEvent(final DisplayUpdatedEvent e) {
		if (e.getLevel() == DisplayUpdateLevel.REBUILD) {
			getPanel().redoLayout();
		}
		getPanel().redraw();
	}

	@Override
	public void onDisplayActivatedEvent(final DisplayActivatedEvent e) {
		getPanel().getWindow().requestFocus();
	}

	// -- Disposable methods --

	@Override
	public void dispose() {
		final DisplayWindow w = getWindow();
		if (w != null) w.close();
	}

	// -- Internal AbstractDisplayViewer methods --

	/** Convenience method to obtain the appropriate {@link EventService}. */
	protected EventService getEventService() {
		return getContext().getService(EventService.class);
	}

	protected void updateTitle() {
		String trailer = "";
		if (display instanceof ImageDisplay) {
			ImageDisplayService srv =
				getContext().getService(ImageDisplayService.class);
			if (srv != null) {
				Dataset ds = srv.getActiveDataset((ImageDisplay) display);
				if (ds != null) {
					Img<?> img = ds.getImgPlus().getImg();
					if (AbstractCellImg.class.isAssignableFrom(img.getClass())) {
						trailer = " (V)";
					}
				}
			}
		}
		getWindow().setTitle(getDisplay().getName() + trailer);
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(final DisplayCreatedEvent event) {
		if (event.getObject() != getDisplay()) return;
		updateTitle();
	}

	@EventHandler
	protected void onEvent(final DisplayUpdatedEvent event) {
		if (event.getDisplay() != getDisplay()) return;
		updateTitle();
	}

}
