//
// NavigableImageFrame.java
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

package imagej.gui.swing.display;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import imagej.display.DisplayController;
import imagej.display.EventDispatcher;
import imagej.display.ImageCanvas;
import imagej.display.ImageDisplayWindow;
import imagej.model.AxisLabel;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * TODO
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 */
public class NavigableImageFrame extends JFrame implements ImageDisplayWindow {

	// TODO - Rework this class to be a JPanel, not a JFrame.
	private final JLabel imageLabel;
	private final NavigableImagePanel imgCanvas;
	private JPanel sliders;
	//
	private DisplayController controller;

	public NavigableImageFrame(final NavigableImagePanel imgCanvas) {
		this.imgCanvas = imgCanvas;
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		// maximize window size
		final GraphicsEnvironment ge =
			GraphicsEnvironment.getLocalGraphicsEnvironment();
		final Rectangle bounds = ge.getMaximumWindowBounds();
		setBounds(bounds.width / 6, bounds.height / 6, 2 * bounds.width / 3,
			2 * bounds.height / 3);
		imageLabel = new JLabel(" ");
		getContentPane().add(imageLabel, BorderLayout.NORTH);
		final JPanel borderPanel = new JPanel();
		borderPanel.setLayout(new BorderLayout());
		borderPanel.setBorder(new CompoundBorder(new EmptyBorder(3, 3, 3, 3),
			new LineBorder(Color.black)));
		borderPanel.add(imgCanvas, BorderLayout.CENTER);
		getContentPane().add(borderPanel, BorderLayout.CENTER);

	}

	@Override
	public void setDisplayController(final DisplayController controller) {
		this.controller = controller;
		final int[] dims = controller.getDims();
		final AxisLabel[] dimLabels = controller.getDimLabels();
		sliders = createSliders(dims, dimLabels);
		getContentPane().add(sliders, BorderLayout.SOUTH);
	}

	@Override
	public void setLabel(final String s) {
		imageLabel.setText(s);
	}

	public ImageCanvas getPanel() {
		return imgCanvas;
	}

	private JPanel createSliders(final int[] dims, final AxisLabel[] dimLabels) {
		if (sliders != null) {
			remove(sliders);
		}
		if (dims.length == 2) {
			return new JPanel();
		}

		final StringBuilder rows = new StringBuilder("pref");
		for (int i = 3; i < dims.length; i++) {
			if (dims[i] > 1) {
				rows.append(", 3dlu, pref");
			}
		}
		final PanelBuilder panelBuilder =
			new PanelBuilder(
				new FormLayout("pref, 3dlu, pref:grow", rows.toString()));
		// panelBuilder.setDefaultDialogBorder();
		final CellConstraints cc = new CellConstraints();

		for (int i = 0, p = -1, row = 1; i < dims.length; i++) {
			if (AxisLabel.isXY(dimLabels[i])) {
				continue;
			}
			p++;
			if (dims[i] == 1) {
				continue;
			}
			final JLabel label = new JLabel(dimLabels[i].toString());
			final JScrollBar slider =
				new JScrollBar(Adjustable.HORIZONTAL, 1, 1, 1, dims[i] + 1);
			final int posIndex = p;
			slider.addAdjustmentListener(new AdjustmentListener() {

				@Override
				@SuppressWarnings("synthetic-access")
				public void adjustmentValueChanged(final AdjustmentEvent e) {

					// pos[posIndex] = slider.getValue() - 1;
					// controller.updatePosition();
					controller.updatePosition(posIndex, slider.getValue() - 1);
				}

			});
			panelBuilder.add(label, cc.xy(1, row));
			panelBuilder.add(slider, cc.xy(3, row));
			row += 2;
		}
		return panelBuilder.getPanel();
	}

	@Override
	public void setImageCanvas(final ImageCanvas canvas) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public ImageCanvas getImageCanvas() {
		return imgCanvas;
	}

	@Override
	public void updateImage() {
		imgCanvas.updateImage();
	}

	@Override
	public void addEventDispatcher(final EventDispatcher dispatcher) {
		addWindowListener((AWTEventDispatcher) dispatcher);

	}

}
