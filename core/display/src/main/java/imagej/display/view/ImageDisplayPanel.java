//
// ImageDisplayPanel.java
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

package imagej.display.view;

import imagej.data.event.DatasetChangedEvent;
import imagej.display.ImageCanvas;
import imagej.event.EventSubscriber;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

// TODO - eliminate dependencies on AWT and Swing

/**
 * TODO
 * 
 * @author Grant Harris
 */
public class ImageDisplayPanel extends JPanel implements
	EventSubscriber<DatasetChangedEvent>, ImageCanvas
{

	protected int maxWidth = 0, maxHeight = 0;
	ImgDisplayController ctrl;
	ImagePanel imgPanel;

	// JPanel controlPanel = new JPanel();

	public ImageDisplayPanel(final ImgDisplayController ctrl) {
		this.ctrl = ctrl;
		// setBorder(new TitledBorder(view.getImg().getName()));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		// setLayout(new BorderLayout());
		imgPanel = new ImagePanel();
		add(imgPanel, BorderLayout.CENTER);
		// add(imgPanel, BorderLayout.CENTER);
		// add(controlPanel, BorderLayout.SOUTH);
		// controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));

	}

	public void setMaxDimension(final DatasetView view) {
		final int width = (int) view.getImg().dimension(0) + view.getPositionX();
		final int height = (int) view.getImg().dimension(1) + view.getPositionY();
		if (width > maxWidth) {
			maxWidth = width;
		}
		if (height > maxHeight) {
			maxHeight = height;
		}
	}

	public void resetSize() {
		this.setPreferredSize(new Dimension(maxWidth, maxHeight));
	}

	@Override
	public void onEvent(final DatasetChangedEvent event) {
		imgPanel.repaint();
	}

	@Override
	public int getImageWidth() {
		return maxWidth;
	}

	@Override
	public int getImageHeight() {
		return maxHeight;
	}

	@Override
	public void updateImage() {
		imgPanel.repaint();
	}

	public void addToControlPanel(final JComponent component) {
		// controlPanel.add(component);
	}

	class ImagePanel extends JPanel {

		@Override
		public void paint(final Graphics g) {
			for (final DatasetView view : ctrl.getViews()) {
				// System.out.println("Painting in ImagePanel...");
				final Image image = view.getScreenImage().image();
				g.drawImage(image, view.getPositionX(), view.getPositionY(), this);
			}
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(maxWidth, maxHeight);
		}

	};
}
