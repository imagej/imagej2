//
// SWTFileWidget.java
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

package imagej.ext.ui.swt;

import imagej.ext.module.ui.FileWidget;
import imagej.ext.module.ui.WidgetModel;

import java.io.File;

import net.miginfocom.swt.MigLayout;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * SWT implementation of file selector widget.
 *
 * @author Curtis Rueden
 */
public class SWTFileWidget extends SWTInputWidget implements FileWidget {

	private Text path;
	private Button browse;

	public SWTFileWidget(final Composite parent, final WidgetModel model) {
		super(parent, model);

		setLayout(new MigLayout());

		path = new Text(this, 0);
		path.setTextLimit(20);

		browse = new Button(this, 0);
		browse.setText("Browse");

		refreshWidget();
	}

	// -- FileWidget methods --

	@Override
	public File getValue() {
		return new File(path.getText());
	}

	// -- InputWidget methods --

	@Override
	public void refreshWidget() {
		final File value = (File) getModel().getValue();
		final String text = value == null ? "" : value.toString();
		if (text.equals(path.getText())) return; // no change
		path.setText(text);
	}

}
