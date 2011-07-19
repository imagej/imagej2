//
// WidgetModel.java
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

package imagej.ext.module.ui;

import imagej.ext.module.Module;
import imagej.ext.module.ModuleItem;

/**
 * The backing data model for a particular {@link InputWidget}.
 *
 * @author Curtis Rueden
 */
public class WidgetModel {

	private final InputPanel inputPanel;
	private final Module module;
	private final ModuleItem<?> item;

	private final String widgetLabel;

	private boolean initialized;

	public WidgetModel(final InputPanel inputPanel,
		final Module module, final ModuleItem<?> item)
	{
		this.inputPanel = inputPanel;
		this.module = module;
		this.item = item;

		widgetLabel = makeWidgetLabel(item.getLabel());
	}

	public Module getModule() {
		return module;
	}

	public ModuleItem<?> getItem() {
		return item;
	}

	public String getWidgetLabel() {
		return widgetLabel;
	}

	public Object getValue() {
		return item.getValue(module);
	}

	public void setValue(final Object value) {
		final String name = item.getName();
		if (module.getInput(name) == value) return; // no change
		module.setInput(name, value);
		if (initialized) {
			item.callback(module);
			inputPanel.refresh();
			module.preview();
		}
	}

	public void setInitialized(final boolean initialized) {
		this.initialized = initialized;
	}

	public boolean isInitialized() {
		return initialized;
	}

	// -- Helper methods --

	private String makeWidgetLabel(final String s) {
		if (s == null || s.isEmpty()) {
			final String name = item.getName();
			return name.substring(0, 1).toUpperCase() + name.substring(1);
		}
		return s;
	}

}
