//
// SwingInputWidget.java
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

import imagej.ext.module.ui.InputWidget;
import imagej.ext.module.ui.WidgetModel;

import javax.swing.JComponent;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * Common superclass for Swing-based input widgets.
 * 
 * @author Curtis Rueden
 */
public abstract class SwingInputWidget extends JPanel implements InputWidget {

	private WidgetModel model;

	public SwingInputWidget(final WidgetModel model) {
		this.model = model;
		setLayout(new MigLayout("fillx,ins 3 0 3 0", "[fill,grow|pref]"));
	}

	// -- InputWidget methods --

	@Override
	public WidgetModel getModel() {
		return model;
	}

	@Override
	public void updateModel() {
		model.setValue(getValue());
	}

	// -- Helper methods --

	/** Assigns the model's description as the given component's tool tip. */
	protected void setToolTip(final JComponent c) {
		final String desc = model.getItem().getDescription();
		if (desc == null || desc.isEmpty()) return;
		c.setToolTipText(desc);
	}

}
