//
// DefaultModuleItem.java
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

package imagej.ext.module;

import imagej.ext.module.ui.WidgetStyle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default {@link ModuleItem} implementation, for use with custom {@link Module}
 * implementations.
 * 
 * @author Curtis Rueden
 */
public class DefaultModuleItem<T> extends AbstractModuleItem<T> {

	private final Class<T> type;
	private ItemVisibility visibility;
	private boolean required;
	private boolean persisted;
	private String persistKey;
	private String callback;
	private WidgetStyle widgetStyle;
	private T minimumValue;
	private T maximumValue;
	private Number stepSize;
	private int columnCount;
	private final List<T> choices = new ArrayList<T>();
	private String name;
	private String label;
	private String description;

	public DefaultModuleItem(final Module module, final String name,
		final Class<T> type)
	{
		this(module.getInfo(), name, type);
	}

	public DefaultModuleItem(final ModuleInfo info, final String name,
		final Class<T> type)
	{
		super(info);
		this.name = name;
		this.type = type;

		visibility = super.getVisibility();
		required = super.isRequired();
		persisted = super.isPersisted();
		persistKey = super.getPersistKey();
		callback = super.getCallback();
		widgetStyle = super.getWidgetStyle();
		minimumValue = super.getMinimumValue();
		maximumValue = super.getMaximumValue();
		stepSize = super.getStepSize();
		columnCount = super.getColumnCount();
		final List<T> superChoices = super.getChoices();
		if (superChoices != null) choices.addAll(superChoices);
		label = super.getLabel();
		description = super.getDescription();
	}

	// -- DefaultModuleItem methods --

	public void setVisibility(final ItemVisibility visibility) {
		this.visibility = visibility;
	}

	public void setRequired(final boolean required) {
		this.required = required;
	}

	public void setPersisted(final boolean persisted) {
		this.persisted = persisted;
	}

	public void setPersistKey(final String persistKey) {
		this.persistKey = persistKey;
	}

	public void setCallback(final String callback) {
		this.callback = callback;
	}

	public void setWidgetStyle(final WidgetStyle widgetStyle) {
		this.widgetStyle = widgetStyle;
	}

	public void setMinimumValue(final T minimumValue) {
		this.minimumValue = minimumValue;
	}

	public void setMaximumValue(final T maximumValue) {
		this.maximumValue = maximumValue;
	}

	public void setStepSize(final Number stepSize) {
		this.stepSize = stepSize;
	}

	public void setColumnCount(final int columnCount) {
		this.columnCount = columnCount;
	}

	public void setChoices(final List<? extends T> choices) {
		this.choices.clear();
		this.choices.addAll(choices);
	}

	// -- ModuleItem methods --

	@Override
	public Class<T> getType() {
		return type;
	}

	@Override
	public ItemVisibility getVisibility() {
		return visibility;
	}

	@Override
	public boolean isRequired() {
		return required;
	}

	@Override
	public boolean isPersisted() {
		return persisted;
	}

	@Override
	public String getPersistKey() {
		return persistKey;
	}

	@Override
	public String getCallback() {
		return callback;
	}

	@Override
	public WidgetStyle getWidgetStyle() {
		return widgetStyle;
	}

	@Override
	public T getMinimumValue() {
		return minimumValue;
	}

	@Override
	public T getMaximumValue() {
		return maximumValue;
	}

	@Override
	public Number getStepSize() {
		return stepSize;
	}

	@Override
	public int getColumnCount() {
		return columnCount;
	}

	@Override
	public List<T> getChoices() {
		return Collections.unmodifiableList(choices);
	}

	// -- BasicDetails methods --

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public void setLabel(final String label) {
		this.label = label;
	}

	@Override
	public void setDescription(final String description) {
		this.description = description;
	}

}
