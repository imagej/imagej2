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

import imagej.model.AxisLabel;
import imagej.model.Dataset;
import imagej.process.Index;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import loci.formats.gui.AWTImageTools;
import mpicbg.imglib.image.Image;

/**
 * TODO
 *
 * @author Curtis Rueden
 * @author Grant Harris
 */
public class NavigableImageFrame extends JFrame {

	// TODO - Rework this class to be a JPanel, not a JFrame.

	private Dataset dataset;
	private int[] dims;
	private AxisLabel[] dimLabels;
	private int xIndex, yIndex;
	private int[] pos;

	private JLabel imageLabel;
	private NavigableImagePanel imagePanel;
	private JPanel sliders;

	public NavigableImageFrame() {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		// maximize window size
		final GraphicsEnvironment ge =
			GraphicsEnvironment.getLocalGraphicsEnvironment();
		final Rectangle bounds = ge.getMaximumWindowBounds();
		setBounds(bounds.width / 6, bounds.height / 6,
			2 * bounds.width / 3, 2 * bounds.height / 3);

		imageLabel = new JLabel(" ");
		getContentPane().add(imageLabel, BorderLayout.NORTH);

		imagePanel = new NavigableImagePanel();
		final JPanel borderPanel = new JPanel();
		borderPanel.setLayout(new BorderLayout());
		borderPanel.setBorder(new CompoundBorder(
			new EmptyBorder(3, 3, 3, 3),
			new LineBorder(Color.black)));
		borderPanel.add(imagePanel, BorderLayout.CENTER);
		getContentPane().add(borderPanel, BorderLayout.CENTER);
	}

	public void setDataset(final Dataset dataset) {
		this.dataset = dataset;
		final Image<?> image = dataset.getImage();
		dims = image.getDimensions();
		dimLabels = dataset.getMetadata().getAxes();
		setTitle(dataset.getMetadata().getName());

		// extract width and height
		xIndex = yIndex = -1;
		for (int i = 0; i < dimLabels.length; i++) {
			if (dimLabels[i] == AxisLabel.X) xIndex = i;
			if (dimLabels[i] == AxisLabel.Y) yIndex = i;
		}
		if (xIndex < 0) throw new IllegalArgumentException("No X dimension");
		if (yIndex < 0) throw new IllegalArgumentException("No Y dimension");

		// create sliders
		if (sliders != null) remove(sliders);
		sliders = createSliders();
		getContentPane().add(sliders, BorderLayout.SOUTH);

		// display first image plane
		pos = new int[dims.length - 2];
		updatePosition();
	}

	public NavigableImagePanel getPanel() {
		return imagePanel;
	}

	private void updatePosition() {
		final BufferedImage image = getImagePlane();
		imagePanel.setImage(image);
		imagePanel.setNavigationImageEnabled(true);

		final StringBuilder sb = new StringBuilder();
		for (int i = 0, p = -1; i < dims.length; i++) {
			if (isXY(dimLabels[i])) continue;
			p++;
			if (dims[i] == 1) continue;
			sb.append(dimLabels[i] + ": " + (pos[p] + 1) + "/" + dims[i] + "; ");
		}
		sb.append(dims[xIndex] + "x" + dims[yIndex] + "; ");
		sb.append(image.getType());
		imageLabel.setText(sb.toString());
	}

	private BufferedImage getImagePlane() {
		// FIXME - how to get a subset with different axes?
		final int no = Index.positionToRaster(dims, pos);
		final Object plane = dataset.getPlane(no);
		if (plane instanceof byte[]) {
			return AWTImageTools.makeImage((byte[]) plane,
				dims[xIndex], dims[yIndex], dataset.isSigned());
		}
		else if (plane instanceof short[]) {
			return AWTImageTools.makeImage((short[]) plane,
					dims[xIndex], dims[yIndex], dataset.isSigned());			
		}
		else if (plane instanceof int[]) {
			return AWTImageTools.makeImage((int[]) plane,
					dims[xIndex], dims[yIndex], dataset.isSigned());			
		}
		else if (plane instanceof float[]) {
			return AWTImageTools.makeImage((float[]) plane,
				dims[xIndex], dims[yIndex]);			
		}
		else if (plane instanceof double[]) {
			return AWTImageTools.makeImage((double[]) plane,
				dims[xIndex], dims[yIndex]);
		}
		else {
			throw new IllegalStateException("Unknown data type: " +
				plane.getClass().getName());
		}
	}

	private JPanel createSliders() {
		if (dims.length == 2) return new JPanel();

		final StringBuilder rows = new StringBuilder("pref");
		for (int i = 3; i < dims.length; i++) {
			if (dims[i] > 1) rows.append(", 3dlu, pref");
		}
		final PanelBuilder panelBuilder = new PanelBuilder(
			new FormLayout("pref, 3dlu, pref:grow", rows.toString()));
		//panelBuilder.setDefaultDialogBorder();
		final CellConstraints cc = new CellConstraints();

		for (int i = 0, p = -1, row = 1; i < dims.length; i++) {
			if (isXY(dimLabels[i])) continue;
			p++;
			if (dims[i] == 1) continue;
			final JLabel label = new JLabel(dimLabels[i].toString());
			final JScrollBar slider = new JScrollBar(Adjustable.HORIZONTAL,
				1, 1, 1, dims[i] + 1);
			final int posIndex = p;
			slider.addAdjustmentListener(new AdjustmentListener() {
				@Override
				@SuppressWarnings("synthetic-access")
				public void adjustmentValueChanged(AdjustmentEvent e) {
					pos[posIndex] = slider.getValue() - 1;
					updatePosition();
				}
			});
			panelBuilder.add(label, cc.xy(1, row));
			panelBuilder.add(slider, cc.xy(3, row));
			row += 2;
		}

		return panelBuilder.getPanel();
	}

	private static boolean isXY(AxisLabel dimLabel) {
		return dimLabel == AxisLabel.X || dimLabel == AxisLabel.Y;
	}

}
