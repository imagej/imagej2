//
// SwingObjectWidget.java
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

import imagej.plugin.ui.ObjectWidget;
import imagej.plugin.ui.ParamDetails;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JPanel;

/**
 * Swing implementation of object selector widget.
 * 
 * @author Curtis Rueden
 */
public class SwingObjectWidget extends JPanel
	implements ActionListener, ObjectWidget
{

	private final ParamDetails details;
	private final JComboBox comboBox;

	public SwingObjectWidget(final ParamDetails details,
		final Object initialValue, final Object[] items)
	{
		this.details = details;
		comboBox = new JComboBox(items);
		comboBox.setSelectedItem(initialValue);
		if (comboBox.getSelectedIndex() < 0 && items.length > 0) {
			comboBox.setSelectedIndex(0);
		}
		add(comboBox, BorderLayout.CENTER);
		comboBox.addActionListener(this);
	}

	// -- ActionListener methods --

	@Override
	public void actionPerformed(final ActionEvent e) {
		details.setValue(comboBox.getSelectedItem());
	}

	// -- ObjectWidget methods --

	@Override
	public Object getObject() {
		return comboBox.getSelectedItem();
	}

	// -- InputWidget methods --

	@Override
	public void refresh() {
		comboBox.removeActionListener(this);
		comboBox.setSelectedItem(details.getValue());
		comboBox.addActionListener(this);
	}

}
