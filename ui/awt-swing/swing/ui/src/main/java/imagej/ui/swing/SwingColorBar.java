//
// SwingColorBar.java
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

package imagej.ui.swing;

import imagej.display.ColorTables;
import imagej.util.awt.AWTImageTools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.imglib2.display.ColorTable8;

/**
 * A widget for displaying a {@link ColorTable8} bar.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 */
public final class SwingColorBar extends JPanel {

	private final int length;
	private final int height = 24;

	public SwingColorBar(final ColorTable8 lut) {
		length = lut.getLength();

		// create compatible image
		final BufferedImage bi = AWTImageTools.createImage(length + 2, height);

		// paint color table onto image
		final Graphics gfx = bi.getGraphics();
		gfx.setColor(Color.black);
		gfx.drawRect(0, 0, length + 1, height - 1);
		for (int i = 0; i < length; i++) {
			final int r = lut.get(0, i);
			final int g = lut.get(1, i);
			final int b = lut.get(2, i);
			gfx.setColor(new Color(r, g, b));
			gfx.drawLine(i + 1, 1, i + 1, height - 2);
		}
		gfx.dispose();

		// add image to component
		add(new JLabel(new ImageIcon(bi)));
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(length + 2, height);
	}

	public static void main(final String[] args) {
		final ColorTable8[] luts = {
			ColorTables.FIRE, ColorTables.ICE, ColorTables.SPECTRUM,
			ColorTables.RED, ColorTables.GREEN, ColorTables.BLUE,
			ColorTables.CYAN, ColorTables.MAGENTA, ColorTables.YELLOW,
			ColorTables.GRAYS, ColorTables.REDGREEN, ColorTables.RGB332
		};

		final JFrame frame = new JFrame();
		frame.setTitle("LUTs");
		final JPanel pane = new JPanel();
		frame.setContentPane(pane);
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				System.exit(0);
			}
		});
		for (final ColorTable8 lut : luts) {
			pane.add(new SwingColorBar(lut));
		}
		frame.pack();
		frame.setVisible(true);
	}

}
