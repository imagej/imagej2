//
// SwingColorWidget.java
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

package imagej.plugin.ui.swing;

import imagej.module.ui.ColorWidget;
import imagej.module.ui.WidgetModel;
import imagej.util.ColorRGB;
import imagej.util.awt.AWTColors;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;

/**
 * Swing implementation of color chooser widget.
 * 
 * @author Curtis Rueden
 */
public class SwingColorWidget extends SwingInputWidget implements
	ActionListener, ColorWidget
{

	private static final int SWATCH_WIDTH = 64, SWATCH_HEIGHT = 16;

	private final JButton choose;
	private Color color;

	public SwingColorWidget(final WidgetModel model) {
		super(model);
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		choose = new JButton() {

			@Override
			public Dimension getMaximumSize() {
				return getPreferredSize();
			}
		};
		setToolTip(choose);
		add(choose);
		choose.addActionListener(this);

		refreshWidget();
	}

	// -- ActionListener methods --

	@Override
	public void actionPerformed(final ActionEvent e) {
		final Color choice =
			JColorChooser.showDialog(choose, "Select a color", color);
		if (choice == null) return;
		color = choice;
		updateModel();
		refreshWidget();
	}

	// -- ColorWidget methods --

	@Override
	public ColorRGB getValue() {
		return AWTColors.getColorRGB(color);
	}

	// -- InputWidget methods --

	@Override
	public void refreshWidget() {
		final ColorRGB value = (ColorRGB) getModel().getValue();
		color = AWTColors.getColor(value);

		final BufferedImage image =
			new BufferedImage(SWATCH_WIDTH, SWATCH_HEIGHT,
				BufferedImage.TYPE_INT_RGB);
		final Graphics g = image.getGraphics();
		g.setColor(color);
		g.fillRect(0, 0, image.getWidth(), image.getHeight());
		g.dispose();
		final ImageIcon icon = new ImageIcon(image);
		choose.setIcon(icon);
	}

}
