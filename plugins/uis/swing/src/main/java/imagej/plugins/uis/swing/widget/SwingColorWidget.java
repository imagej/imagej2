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

package imagej.plugins.uis.swing.widget;

import imagej.util.awt.AWTColors;
import imagej.widget.ColorWidget;
import imagej.widget.InputWidget;
import imagej.widget.WidgetModel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.colorchooser.AbstractColorChooserPanel;

import org.scijava.plugin.Plugin;
import org.scijava.util.ColorRGB;

/**
 * Swing implementation of color chooser widget.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = InputWidget.class)
public class SwingColorWidget extends SwingInputWidget<ColorRGB> implements
	ActionListener, ColorWidget<JPanel>
{

	private static final int SWATCH_WIDTH = 64, SWATCH_HEIGHT = 16;

	private static final String HSB_CLASS_NAME =
		"javax.swing.colorchooser.DefaultHSBChooserPanel";

	protected static final String RGB_CLASS_NAME =
		"javax.swing.colorchooser.DefaultRGBChooserPanel";

	protected static final String SWATCHES_CLASS_NAME =
		"javax.swing.colorchooser.DefaultSwatchChooserPanel";

	private JButton choose;
	private Color color;

	// -- ActionListener methods --

	@Override
	public void actionPerformed(final ActionEvent e) {
		final Color choice = showColorDialog(choose, "Select a color", color);
		if (choice == null) return;
		color = choice;
		updateModel();
		refreshWidget();
	}

	// -- InputWidget methods --

	@Override
	public ColorRGB getValue() {
		return AWTColors.getColorRGB(color);
	}

	// -- WrapperPlugin methods --

	@Override
	public void set(final WidgetModel model) {
		super.set(model);

		getComponent().setLayout(new BoxLayout(getComponent(), BoxLayout.X_AXIS));

		choose = new JButton() {

			@Override
			public Dimension getMaximumSize() {
				return getPreferredSize();
			}
		};
		setToolTip(choose);
		getComponent().add(choose);
		choose.addActionListener(this);

		refreshWidget();
	}

	// -- Typed methods --

	@Override
	public boolean supports(final WidgetModel model) {
		return super.supports(model) && model.isType(ColorRGB.class);
	}

	// -- Utility methods --

	/**
	 * This method is identical to
	 * {@link JColorChooser#showDialog(Component, String, Color)} except that it
	 * reorders the panels of the color chooser to be more desirable. It uses
	 * (HSB, RGB, Swatches) rather than the default of (Swatches, HSB, RGB).
	 */
	public static Color showColorDialog(final Component component,
		final String title, final Color initialColor) throws HeadlessException
	{
		final JColorChooser pane = createColorChooser(initialColor);

		class ColorTracker implements ActionListener {

			private final JColorChooser chooser;
			private Color color;

			public ColorTracker(final JColorChooser c) {
				chooser = c;
			}

			@Override
			public void actionPerformed(final ActionEvent e) {
				color = chooser.getColor();
			}

			public Color getColor() {
				return color;
			}
		}
		final ColorTracker ok = new ColorTracker(pane);

		final JDialog dialog =
			JColorChooser.createDialog(component, title, true, pane, ok, null);

		dialog.setVisible(true);

		return ok.getColor();
	}

	// -- Helper methods --

	/**
	 * Creates a new {@link JColorChooser} with panels in a desirable order.
	 * <p>
	 * All of this code exists solely to reorder the panels from (Swatches, HSB,
	 * RGB) to (HSB, RGB, Swatches) since we believe the HSB tab is the most
	 * commonly useful.
	 * </p>
	 */
	private static JColorChooser createColorChooser(final Color initialColor) {
		final JColorChooser chooser =
			new JColorChooser(initialColor != null ? initialColor : Color.white);

		// get the list of panels
		final AbstractColorChooserPanel[] panels =
			chooser.getChooserPanels().clone();

		// sort panels into the desired order
		Arrays.sort(panels, new Comparator<Object>() {

			@Override
			public int compare(final Object o1, final Object o2) {
				return value(o1) - value(o2);
			}

			private int value(final Object o) {
				final String className = o.getClass().getName();
				if (className.equals(HSB_CLASS_NAME)) return 1;
				if (className.equals(RGB_CLASS_NAME)) return 2;
				if (className.equals(SWATCHES_CLASS_NAME)) return 3;
				return 4;
			}
		});

		// reset the panels to match the sorted list
		chooser.setChooserPanels(panels);

		return chooser;
	}

	// -- AbstractUIInputWidget methods ---

	@Override
	public void doRefresh() {
		final ColorRGB value = (ColorRGB) get().getValue();
		color = AWTColors.getColor(value);

		final BufferedImage image =
			new BufferedImage(SWATCH_WIDTH, SWATCH_HEIGHT, BufferedImage.TYPE_INT_RGB);
		final Graphics g = image.getGraphics();
		g.setColor(color);
		g.fillRect(0, 0, image.getWidth(), image.getHeight());
		g.dispose();
		final ImageIcon icon = new ImageIcon(image);
		choose.setIcon(icon);
	}
}
