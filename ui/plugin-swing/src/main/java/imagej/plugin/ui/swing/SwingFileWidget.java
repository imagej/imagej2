//
// SwingFileWidget.java
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

import imagej.plugin.ui.FileWidget;
import imagej.plugin.ui.ParamDetails;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Swing implementation of file selector widget.
 *
 * @author Curtis Rueden
 */
public class SwingFileWidget extends JPanel
	implements ActionListener, DocumentListener, FileWidget
{

	private ParamDetails details;
	private JTextField path;
	private JButton browse;

	public SwingFileWidget(final ParamDetails details) {
		this.details = details;

		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		path = new JTextField("", 20);
		add(path);
		path.getDocument().addDocumentListener(this);

		add(Box.createHorizontalStrut(3));

		browse = new JButton("Browse");
		add(browse);
		browse.addActionListener(this);

		refresh();
	}

	// -- ActionListener methods --

	@Override
	public void actionPerformed(ActionEvent e) {
		File file = new File(path.getText());
		if (!file.isDirectory()) {
			file = file.getParentFile();
		}
		final JFileChooser chooser = new JFileChooser(file);
		final int rval = chooser.showOpenDialog(this);
		if (rval != JFileChooser.APPROVE_OPTION) return;
		file = chooser.getSelectedFile();
		if (file != null) path.setText(file.getAbsolutePath());
	}

	// -- DocumentListener methods --

	@Override
	public void changedUpdate(DocumentEvent e) {
		documentUpdate();
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		documentUpdate();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		documentUpdate();
	}

	// -- FileWidget methods --

	@Override
	public File getFile() {
		final String text = path.getText();
		return text.isEmpty() ? null : new File(text);
	}

	// -- InputWidget methods --

	@Override
	public void refresh() {
		final File value = (File) details.getValue();
		path.setText(value == null ? "" : value.toString());
	}

	// -- Helper methods --

	private void documentUpdate() {
		details.setValue(new File(path.getText()));
	}

}
