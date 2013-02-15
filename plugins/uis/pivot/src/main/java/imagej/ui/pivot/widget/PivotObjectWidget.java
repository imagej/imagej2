/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.ui.pivot.widget;

import imagej.plugin.Plugin;
import imagej.widget.InputWidget;
import imagej.widget.ObjectWidget;
import imagej.widget.WidgetModel;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.ListButton;

/**
 * Pivot implementation of object selector widget.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = InputWidget.class)
public class PivotObjectWidget extends PivotInputWidget<Object> implements
	ObjectWidget<BoxPane>
{

	private ListButton listButton;

	// -- InputWidget methods --

	@Override
	public boolean isCompatible(final WidgetModel model) {
		return super.isCompatible(model) && model.getObjectPool().size() > 0;
	}

	@Override
	public void initialize(final WidgetModel model) {
		super.initialize(model);

		listButton = new ListButton();
		final Object[] items = model.getObjectPool().toArray();
		final List<Object> listData = new ArrayList<Object>(items);
		listButton.setListData(listData);
		getComponent().add(listButton);

		refreshWidget();
	}

	@Override
	public Object getValue() {
		return listButton.getSelectedItem();
	}

	@Override
	public void refreshWidget() {
		listButton.setSelectedItem(getValidValue());
	}

	// -- Helper methods --

	private Object getValidValue() {
		final int itemCount = listButton.getListSize();
		if (itemCount == 0) return null; // no valid values exist

		final Object value = getModel().getValue();
		for (int i = 0; i < itemCount; i++) {
			final Object item = listButton.getListData().get(i);
			if (value == item) return value;
		}

		// value was invalid; reset to first choice on the list
		final Object validValue = listButton.getListData().get(0);
		// CTR FIXME should not update model in getter!
		getModel().setValue(validValue);
		return validValue;
	}

}
