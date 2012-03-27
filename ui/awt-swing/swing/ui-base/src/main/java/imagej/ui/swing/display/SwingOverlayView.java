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

package imagej.ui.swing.display;

import imagej.data.display.AbstractOverlayView;
import imagej.data.display.ImageDisplay;
import imagej.data.overlay.Overlay;
import imagej.ui.swing.overlay.IJHotDrawOverlayAdapter;
import imagej.ui.swing.overlay.JHotDrawAdapterFinder;

import java.awt.EventQueue;

import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.event.FigureAdapter;
import org.jhotdraw.draw.event.FigureEvent;

/**
 * TODO
 * 
 * @author Curtis Rueden
 * @author Lee Kamentsky
 */
@SuppressWarnings("synthetic-access")
public class SwingOverlayView extends AbstractOverlayView implements FigureView {

	private final ImageDisplay display;

	/** JHotDraw {@link Figure} linked to the associated {@link Overlay}. */
	private final Figure figure;

	private final IJHotDrawOverlayAdapter adapter;
	
	private boolean updatingFigure = false;
	
	private boolean updatingOverlay = false;
	
	private boolean updateScheduled = false;
	
	private boolean disposeScheduled = false;
	
	private boolean disposed = false;
	
	private boolean figureAdded = false;
	
	/**
	 * Constructor to use to discover the figure to use for an overlay
	 * @param display - hook to this display
	 * @param overlay - represent this overlay
	 */
	public SwingOverlayView(final ImageDisplay display, final Overlay overlay) {
		this(display, overlay, null);
	}
	
	/**
	 * Constructor to use if the figure already exists, for instance if it
	 * was created using the CreationTool
	 * 
	 * @param display - hook to this display
	 * @param overlay - represent this overlay
	 * @param figure - draw using this figure
	 */
	public SwingOverlayView(final ImageDisplay display,
		final Overlay overlay, Figure figure)
	{
		super(overlay);
		this.display = display;
		adapter = JHotDrawAdapterFinder.getAdapterForOverlay(overlay, figure);
		if (figure == null) {
			this.figure = adapter.createDefaultFigure();
			adapter.updateFigure(this, this.figure);
			EventQueue.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					synchronized(SwingOverlayView.this) {
						if (! disposeScheduled) {
							final JHotDrawImageCanvas canvas = (JHotDrawImageCanvas) display.getCanvas();
							final Drawing drawing = canvas.getDrawing();
							drawing.add(SwingOverlayView.this.figure);
							figureAdded = true;
						}
					}
				}
			});
		} else {
			this.figure = figure;
			figureAdded = true;
		}
		this.figure.addFigureListener(new FigureAdapter() {
			@Override
			public void attributeChanged(FigureEvent e) {
				synchronized(SwingOverlayView.this) {
					if (! updatingFigure) {
						updatingOverlay = true;
						try {
							adapter.updateOverlay(SwingOverlayView.this.figure, SwingOverlayView.this);
							overlay.update();
						} finally {
							updatingOverlay = false;
						}
					}
				}
			}

			@Override
			public void figureChanged(FigureEvent e) {
				synchronized(SwingOverlayView.this) {
					if (! updatingFigure) {
						updatingOverlay = true;
						try {
							adapter.updateOverlay(SwingOverlayView.this.figure, SwingOverlayView.this);
							overlay.update();
						} finally {
							updatingOverlay = false;
						}
					}
				}
			}

			@Override
			public void figureRemoved(FigureEvent e) {
				synchronized(SwingOverlayView.this) {
					if (disposed || disposeScheduled) return;
				}
				if (display.isVisible(SwingOverlayView.this)) {
					display.remove(SwingOverlayView.this);
					dispose();
					display.update();
				}
			}
		});
	}

	// -- DataView methods --

	private void show(final boolean doShow) {
		final JHotDrawImageCanvas canvas = (JHotDrawImageCanvas) display.getCanvas();
		final Drawing drawing = canvas.getDrawing();
		final Figure fig = getFigure();
		if (doShow) {
			if (! drawing.contains(fig)) {
				drawing.add(fig);
			}
		} else {
			if (drawing.contains(fig)) {
				drawing.remove(fig);
			}
		}
	}
	@Override
	public int getPreferredWidth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getPreferredHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void update() {
		updateFigure();
	}

	@Override
	public void rebuild() {
		updateFigure();
	}

	@Override
	public Figure getFigure() {
		return figure;
	}

	@Override
	public void dispose() {
		synchronized(this) {
			if (! disposeScheduled) {
				EventQueue.invokeLater(new Runnable() {

					@Override
					public void run() {
						synchronized(SwingOverlayView.this) {
							if (figureAdded) {
								figure.requestRemove();
							}
							disposed = true;
						}
					}});
				disposeScheduled = true;
			}
		}
		super.dispose();
	}
	
	private synchronized void updateFigure() {
		if (updatingOverlay || disposeScheduled) return;
		if (! updateScheduled) {
			EventQueue.invokeLater(new Runnable() {

				@Override
				public void run() {
					try {
						doUpdateFigure();
					} finally {
						updateScheduled = false;
					}
				}});
			updateScheduled = true;
		}
	}
	private synchronized void doUpdateFigure() {
		if (disposeScheduled) return;
		updatingFigure = true;
		try {
			adapter.updateFigure(this, figure);
		} finally {
			updatingFigure = false;
		}
		show(display.isVisible(this));
	}

}
