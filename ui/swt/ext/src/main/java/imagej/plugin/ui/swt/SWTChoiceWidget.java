//
// SWTChoiceWidget.java
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

package imagej.plugin.ui.swt;

import imagej.ext.module.ui.ChoiceWidget;
import imagej.ext.module.ui.WidgetModel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

/**
 * SWT implementation of multiple choice selector widget.
 * 
 * @author Curtis Rueden
 */
public class SWTChoiceWidget extends SWTInputWidget implements ChoiceWidget {

	private final Combo combo;

	public SWTChoiceWidget(final Composite parent, final WidgetModel model,
		final String[] items)
	{
		super(parent, model);

		combo = new Combo(this, SWT.DROP_DOWN);
		combo.setItems(items);

		refreshWidget();
	}

	// -- ChoiceWidget methods --

	@Override
	public String getValue() {
		return combo.getItem(combo.getSelectionIndex());
	}

	@Override
	public int getIndex() {
		return combo.getSelectionIndex();
	}

	// -- InputWidget methods --

	@Override
	public void refreshWidget() {
		final String value = getModel().getValue().toString();
		if (value.equals(getValue())) return; // no change
		for (int i = 0; i < combo.getItemCount(); i++) {
			final String item = combo.getItem(i);
			if (item.equals(value)) {
				combo.select(i);
				break;
			}
		}
	}

}
