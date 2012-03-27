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

package imagej.ui.swing.overlay;

import imagej.data.display.ImageDisplay;
import imagej.data.display.OverlayView;
import imagej.data.overlay.Overlay;
import imagej.ui.swing.display.SwingOverlayView;

import javax.swing.event.EventListenerList;

import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.tool.CreationTool;

/**
 * TODO
 * 
 * @author Lee Kamentsky
 */
public class IJCreationTool extends CreationTool implements JHotDrawTool {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final ImageDisplay display;
	private final IJHotDrawOverlayAdapter adapter;
	private final EventListenerList listeners = new EventListenerList();

	public IJCreationTool(final ImageDisplay display,
		final IJHotDrawOverlayAdapter adapter,
		final OverlayCreatedListener... listeners)
	{
		super(adapter.createDefaultFigure());
		this.display = display;
		this.adapter = adapter;
		for (final OverlayCreatedListener listener : listeners) {
			addOverlayCreatedListener(listener);
		}
	}

	public void addOverlayCreatedListener(final OverlayCreatedListener listener) {
		listeners.add(OverlayCreatedListener.class, listener);
	}

	public void
		removeOverlayCreatedListener(final OverlayCreatedListener listener)
	{
		listeners.remove(OverlayCreatedListener.class, listener);
	}

	protected void fireOverlayCreatedEvent(final OverlayView overlay,
		final Figure figure)
	{
		final FigureCreatedEvent e = new FigureCreatedEvent(overlay, figure);
		for (final OverlayCreatedListener listener : listeners
			.getListeners(OverlayCreatedListener.class))
		{
			listener.overlayCreated(e);
		}
	}

	@Override
	protected Figure createFigure() {
		return adapter.createDefaultFigure();
	}

	@Override
	protected void creationFinished(final Figure figure) {
		super.creationFinished(figure);
		final Overlay overlay = adapter.createNewOverlay();
		final SwingOverlayView view =
			new SwingOverlayView(display, overlay, figure);
		adapter.updateOverlay(figure, view);
		fireOverlayCreatedEvent(view, figure);
	}

	@Override
	public boolean isConstructing() {
		return createdFigure != null;
	}

}
