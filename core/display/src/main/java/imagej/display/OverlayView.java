//
// OverlayView.java
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

package imagej.display;

import imagej.data.Overlay;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jhotdraw.draw.DefaultDrawing;
import org.jhotdraw.draw.DefaultDrawingEditor;
import org.jhotdraw.draw.DefaultDrawingView;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.EllipseFigure;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.RectangleFigure;
import org.jhotdraw.draw.tool.DelegationSelectionTool;

/**
 * A view into an {@link Overlay}, for use with a {@link Display}.
 * 
 * @author Curtis Rueden
 */
public class OverlayView implements DisplayView {

	private final Display display;
	private final Overlay overlay;

//	private final DrawingEditor drawingEditor;
	private final DefaultDrawingView drawingView;
	private final Drawing drawing;

	private BufferedImage image;

	public OverlayView(final Display display, final Overlay overlay) {
		this.display = display;
		this.overlay = overlay;
		// CTR TEMP
//		drawingEditor = new DefaultDrawingEditor();
		drawingView = new DefaultDrawingView();
//		drawingEditor.add(drawingView);
		drawing = new DefaultDrawing(); // or QuadTreeDrawing
		drawingView.setDrawing(drawing);
		final Figure fig1 = new RectangleFigure(10, 15, 120.4, 35.61);
		final Figure fig2 = new EllipseFigure(12.6, 68.2, 24, 24);
		drawing.add(fig1);
		drawing.add(fig2);
		drawingView.addToSelection(fig1);

		rebuild();
//
//		subscribeToEvents();
	}

	// -- DisplayView methods --

	@Override
	public Display getDisplay() {
		return display;
	}

	@Override
	public Overlay getDataObject() {
		return overlay;
	}

	@Override
	public long[] getPlanePosition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getPlaneIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setPosition(final int value, final int dim) {
		// TODO Auto-generated method stub
	}

	@Override
	public Object getImage() {
		return image;
	}

	@Override
	public int getImageWidth() {
		return 500;
	}

	@Override
	public int getImageHeight() {
		return 500;
	}

	@Override
	public void rebuild() {
		image = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = (Graphics2D) image.getGraphics();
		final JFrame f = new JFrame();
		f.getContentPane().add(drawingView, BorderLayout.CENTER);
		f.pack();
		drawingView.paintComponent(g);
//		drawing.draw(g);
		g.dispose();
	}

	private boolean disposed; // TEMP - change to AbstractDisplayView common code

	@Override
	public void dispose() {
		if (disposed) return;
		disposed = true;
		overlay.decrementReferences();
	}

	public static void main(final String[] args) {
		final DrawingEditor drawingEditor = new DefaultDrawingEditor();
		final DefaultDrawingView drawingView = new DefaultDrawingView();
		drawingEditor.add(drawingView);
//		drawingEditor.setTool(new CreationTool(new RectangleFigure()));
		drawingEditor.setTool(new DelegationSelectionTool());
		final Drawing drawing = new DefaultDrawing(); // or QuadTreeDrawing
		drawingView.setDrawing(drawing);
		final Figure fig1 = new RectangleFigure(10, 15, 120.4, 35.61);
		final Figure fig2 = new EllipseFigure(12.6, 68.2, 24, 24);
		drawing.add(fig1);
		drawing.add(fig2);
		drawingView.addToSelection(fig1);

		final JFrame frame = new JFrame("JHotDraw test");
		final JPanel pane = new JPanel();
		pane.setLayout(new BorderLayout());
		frame.setContentPane(pane);
		pane.add(drawingView, BorderLayout.CENTER);
		final JButton zoom = new JButton("Zoom");
		zoom.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				drawingView.setScaleFactor(drawingView.getScaleFactor() * 1.1);
			}
		});
		pane.add(zoom, BorderLayout.SOUTH);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

}
