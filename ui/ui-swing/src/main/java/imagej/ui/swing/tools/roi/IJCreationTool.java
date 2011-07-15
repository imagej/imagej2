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

package imagej.ui.swing.tools.roi;

import imagej.data.roi.Overlay;

import java.util.EventListener;

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
	 * @author leek
	 *
	 *An event that tells the listener that an overlay has
	 *been created, associated with a figure.
	 */
	public class FigureCreatedEvent {
		final protected Overlay overlay;
		final protected Figure figure;
		FigureCreatedEvent(Overlay overlay, Figure figure) {
			this.overlay = overlay;
			this.figure = figure;
		}
		/**
		 * @return the overlay
		 */
		public Overlay getOverlay() {
			return overlay;
		}
		/**
		 * @return the figure
		 */
		public Figure getFigure() {
			return figure;
		}
	}
	
	public interface OverlayCreatedListener extends EventListener {
		public void overlayCreated(FigureCreatedEvent e);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final IJHotDrawOverlayAdapter adapter;
	private final EventListenerList listeners = new EventListenerList();

	public IJCreationTool(final IJHotDrawOverlayAdapter adapter)
	{
		super(adapter.createDefaultFigure());
		this.adapter = adapter;
	}
	
	public void addOverlayCreatedListener(OverlayCreatedListener listener) {
		listeners.add(OverlayCreatedListener.class, listener);
	}
	
	public void removeOverlayCreatedListener(OverlayCreatedListener listener) {
		listeners.remove(OverlayCreatedListener.class, listener);
	}

	protected void fireOverlayCreatedEvent(Overlay overlay, Figure figure) {
		FigureCreatedEvent e = new FigureCreatedEvent(overlay, figure);
		for (OverlayCreatedListener listener: listeners.getListeners(OverlayCreatedListener.class)) {
			listener.overlayCreated(e);
		}
	}
	
	@Override
	protected Figure createFigure() {
		return adapter.createDefaultFigure();
	}

	/* (non-Javadoc)
	 * @see org.jhotdraw.draw.tool.CreationTool#creationFinished(org.jhotdraw.draw.Figure)
	 */
	@Override
	protected void creationFinished(Figure createdFigure) {
		super.creationFinished(createdFigure);
		Overlay overlay = adapter.createNewOverlay();
		adapter.updateOverlay(createdFigure, overlay);
		fireOverlayCreatedEvent(overlay, createdFigure);
	}

}
