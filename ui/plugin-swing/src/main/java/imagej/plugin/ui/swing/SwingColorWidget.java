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

import imagej.plugin.ui.ColorWidget;
import imagej.plugin.ui.ParamModel;
import imagej.util.ColorRGB;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JColorChooser;

/**
 * Swing implementation of file selector widget.
 *
 * @author Curtis Rueden
 */
public class SwingColorWidget extends SwingInputWidget
	implements ActionListener, ColorWidget
{

	private JButton choose;
	private Color color;

	public SwingColorWidget(final ParamModel model) {
		super(model);

		choose = new JButton(" ");
		add(choose, BorderLayout.CENTER);
		choose.addActionListener(this);

		refresh();
	}

	// -- ActionListener methods --

	@Override
	public void actionPerformed(ActionEvent e) {
		Color choice = JColorChooser.showDialog(choose, "Select a color", color);
		if (choice == null) return;
		color = choice;
		choose.setBackground(color);
	}

	// -- ColorWidget methods --

	@Override
	public ColorRGB getColor() {
		return new ColorRGB(color.getRed(), color.getGreen(), color.getBlue());
	}

	// -- InputWidget methods --

	@Override
	public void refresh() {
		final Color value = (Color) model.getValue();
		choose.setBackground(value);
	}

}
