//
// SwingOverlayView.java
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

package imagej.ui.swing.display;

import imagej.data.roi.Overlay;
import imagej.display.OverlayView;
import imagej.ui.swing.tools.roi.IJHotDrawOverlayAdapter;
import imagej.ui.swing.tools.roi.JHotDrawAdapterFinder;
import imagej.util.Index;

import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.event.FigureAdapter;
import org.jhotdraw.draw.event.FigureEvent;

/**
 * TODO
 * 
 * @author Curtis Rueden
 */
public class SwingOverlayView extends OverlayView {

	private final SwingImageDisplay display;

	/** JHotDraw {@link Figure} linked to the associated {@link Overlay}. */
	private final Figure figure;

	
	/**
	 * Constructor to use to discover the figure to use for an overlay
	 * @param display - hook to this display
	 * @param overlay - represent this overlay
	 */
	public SwingOverlayView(final SwingImageDisplay display, final Overlay overlay) {
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
	public SwingOverlayView(final SwingImageDisplay display,
		final Overlay overlay, Figure figure)
	{
		super(display, overlay);
		this.display = display;
		final IJHotDrawOverlayAdapter adapter =
			JHotDrawAdapterFinder.getAdapterForOverlay(overlay, figure);
		this.figure = figure == null ? adapter.createDefaultFigure() : figure;
		adapter.updateFigure(overlay, figure);
		figure.addFigureListener(new FigureAdapter() {
			@Override
			public void attributeChanged(FigureEvent e) {
				adapter.updateOverlay(SwingOverlayView.this.figure, overlay);
				overlay.update();
			}

			@Override
			public void figureChanged(FigureEvent e) {
				adapter.updateOverlay(SwingOverlayView.this.figure, overlay);
				overlay.rebuild();
			}

			@Override
			public void figureRemoved(FigureEvent e) {
				overlay.delete();
			}
		});
	}

	// -- DisplayView methods --

	@Override
	public void setPosition(final int value, final int dim) {
		// CTR FIXME
		// 1. test if new position is MY position
		// 2. if so, add my figure to the drawing
		// 3. if not, but I was previously, remove my figure from the drawing
		final JHotDrawImageCanvas canvas = display.getImageCanvas();
		final Drawing drawing = canvas.getDrawing();
		final int oldIndex = (int) Index.indexNDto1D(planeDims, planePos);
		super.setPosition(value, dim);
		final int newIndex = (int) Index.indexNDto1D(planeDims, planePos);
		drawing.remove(figure);
		drawing.add(figure);
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
		// TODO Auto-generated method stub

	}

	@Override
	public void rebuild() {
		// TODO Auto-generated method stub

	}

}
