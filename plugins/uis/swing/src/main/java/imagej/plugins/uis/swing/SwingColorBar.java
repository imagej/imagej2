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

package imagej.plugins.uis.swing;

import imagej.util.awt.AWTImageTools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import net.imagej.display.ColorTables;
import net.imglib2.display.ColorTable;

/**
 * A widget for displaying a {@link ColorTable} as a bar.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 */
public final class SwingColorBar extends JComponent {

	private static final int DEFAULT_HEIGHT = 24;

	private final int height;

	private BufferedImage bar;

	private Dimension preferredSize;

	public SwingColorBar() {
		this(DEFAULT_HEIGHT);
	}

	public SwingColorBar(final int height) {
		this(null, height);
	}

	public SwingColorBar(final ColorTable lut) {
		this(lut, DEFAULT_HEIGHT);
	}

	public SwingColorBar(final ColorTable lut, final int height) {
		this.height = height;
		if (lut != null) setColorTable(lut);
	}

	// -- SwingColorBar methods --

	/** Sets the {@link ColorTable} displayed by this color bar. */
	public void setColorTable(final ColorTable colorTable) {
		
		if (bar == null || bar.getWidth() != colorTable.getLength()) {
			// create compatible image
			bar = AWTImageTools.createImage(colorTable.getLength(), 1);
		}

		// paint color table onto image
		final Graphics gfx = bar.getGraphics();
		for (int i = 0; i < colorTable.getLength(); i++) {
			final int argb = colorTable.lookupARGB(0, colorTable.getLength(), i);
			gfx.setColor(new Color(argb, false));
			gfx.drawLine(i, 0, i, 1);
		}
		gfx.dispose();		
	}

	// -- JComponent methods --

	@Override
	public Dimension getMinimumSize() {
		return new Dimension(0, height);
	}

	@Override
	public Dimension getPreferredSize() {
		if (preferredSize != null) return preferredSize;
		return new Dimension(bar == null ? 0 : bar.getWidth(), height);
	}

	@Override
	public void setPreferredSize(final Dimension preferredSize) {
		this.preferredSize = preferredSize;
	}

	@Override
	public void paintComponent(final Graphics g) {
		if (bar == null) return;
		g.drawImage(bar, 0, 0, getWidth(), getHeight(), this);
	}

	// -- Main method --

	public static void main(final String[] args) {
		final ColorTable[] luts =
			{ ColorTables.FIRE, ColorTables.ICE, ColorTables.SPECTRUM,
				ColorTables.RED, ColorTables.GREEN, ColorTables.BLUE, ColorTables.CYAN,
				ColorTables.MAGENTA, ColorTables.YELLOW, ColorTables.GRAYS,
				ColorTables.REDGREEN, ColorTables.RGB332 };

		final JFrame frame = new JFrame();
		frame.setTitle("LUTs");
		final JPanel pane = new JPanel();
		frame.setContentPane(pane);
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		for (final ColorTable lut : luts) {
			final SwingColorBar colorBar = new SwingColorBar(lut);
			colorBar.setBorder(new LineBorder(Color.black));
			pane.add(colorBar);
		}
		frame.pack();
		frame.setVisible(true);
	}

}
