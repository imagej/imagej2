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

package imagej.plugins.uis.swing.viewer.image;

import imagej.plugins.uis.swing.overlay.JHotDrawAdapter;
import imagej.plugins.uis.swing.overlay.JHotDrawService;
import net.imagej.display.DataView;
import net.imagej.display.ImageDisplay;
import net.imagej.display.OverlayView;
import net.imagej.overlay.Overlay;

import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.event.FigureAdapter;
import org.jhotdraw.draw.event.FigureEvent;
import org.scijava.AbstractContextual;
import org.scijava.display.Display;
import org.scijava.plugin.Parameter;

/**
 * A figure view that links an ImageJ {@link OverlayView} to a JHotDraw
 * {@link Figure}.
 * 
 * @author Curtis Rueden
 * @author Lee Kamentsky
 */
public class OverlayFigureView extends AbstractContextual implements FigureView
{

	private final SwingImageDisplayViewer displayViewer;
	private final OverlayView overlayView;

	/** JHotDraw {@link Figure} linked to the associated {@link Overlay}. */
	private final Figure figure;

	private final JHotDrawAdapter adapter;

	@Parameter
	private JHotDrawService jHotDrawService;

	private boolean updatingFigure = false;

	private boolean updatingOverlay = false;

	/**
	 * Constructor to use to discover the figure to use for an overlay
	 * 
	 * @param displayViewer - hook to this display viewer
	 * @param overlayView - represent this overlay
	 */
	public OverlayFigureView(final SwingImageDisplayViewer displayViewer,
		final OverlayView overlayView)
	{
		this(displayViewer, overlayView, null);
	}

	/**
	 * Constructor to use if the figure already exists, for instance if it was
	 * created using the CreationTool
	 * 
	 * @param displayViewer - hook to this display viewer
	 * @param overlayView - represent this overlay
	 * @param figure - draw using this figure
	 */
	public OverlayFigureView(final SwingImageDisplayViewer displayViewer,
		final OverlayView overlayView, final Figure figure)
	{
		setContext(displayViewer.getDisplay().getContext());
		this.displayViewer = displayViewer;
		this.overlayView = overlayView;

		adapter = jHotDrawService.getAdapter(overlayView.getData(), figure);
		if (figure == null) {
			this.figure = adapter.createDefaultFigure();
			adapter.updateFigure(overlayView, this.figure);

			final JHotDrawImageCanvas canvas = displayViewer.getCanvas();
			final Drawing drawing = canvas.getDrawing();
			drawing.add(this.figure);
		}
		else {
			this.figure = figure;
		}
		this.figure.addFigureListener(new FigureAdapter() {

			@Override
			public void attributeChanged(final FigureEvent e) {
				if (updatingFigure) return;
				updatingOverlay = true;
				try {
					adapter.updateOverlay(OverlayFigureView.this.figure,
						OverlayFigureView.this.overlayView);
					overlayView.update();
				}
				finally {
					updatingOverlay = false;
				}
			}

			@Override
			public void figureChanged(final FigureEvent e) {
				if (updatingFigure) return;
				updatingOverlay = true;
				try {
					adapter.updateOverlay(OverlayFigureView.this.figure,
						OverlayFigureView.this.overlayView);
					overlayView.update();
				}
				finally {
					updatingOverlay = false;
				}
			}

			@Override
			public void figureRemoved(final FigureEvent e) {
				final ImageDisplay d = getDisplay();
				if (d.isVisible(overlayView)) {
					DataView view = getDataView();
					// TODO : replace next two lines with call to OverlayService to
					// removeOverlay(d, getDataView().getData());
					d.remove(view);
					view.dispose();
					// end TODO replace
					dispose();
					d.update();
				}
			}
		});
	}

	// -- DataView methods --

	private ImageDisplay getDisplay() {
		final Display<DataView> display = displayViewer.getDisplay();
		assert display instanceof ImageDisplay;
		return (ImageDisplay) display;
	}

	private void show(final boolean doShow) {
		final JHotDrawImageCanvas canvas = displayViewer.getCanvas();
		final Drawing drawing = canvas.getDrawing();
		final Figure fig = getFigure();
		if (doShow) {
			if (!drawing.contains(fig)) {
				drawing.add(fig);
			}
		}
		else {
			if (drawing.contains(fig)) {
				drawing.remove(fig);
			}
		}
	}

	@Override
	public void update() {
		updateFigure();
	}

	@Override
	public Figure getFigure() {
		return figure;
	}

	@Override
	public void dispose() {
		figure.requestRemove();
	}

	private void updateFigure() {
		if (updatingOverlay) return;
		updatingFigure = true;
		try {
			adapter.updateFigure(overlayView, figure);
			show(getDisplay().isVisible(overlayView));
		}
		finally {
			updatingFigure = false;
		}
	}

	@Override
	public DataView getDataView() {
		return overlayView;
	}

}
