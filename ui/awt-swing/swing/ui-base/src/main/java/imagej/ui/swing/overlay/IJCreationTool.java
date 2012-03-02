//
// IJCreationTool.java
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
public class IJCreationTool extends CreationTool {

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

}
