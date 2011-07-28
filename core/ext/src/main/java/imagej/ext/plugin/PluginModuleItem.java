//
// PluginModuleItem.java
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

package imagej.ext.plugin;

import imagej.ext.module.AbstractModuleItem;
import imagej.ext.module.ItemVisibility;
import imagej.ext.module.ModuleInfo;
import imagej.ext.module.ModuleItem;
import imagej.ext.module.ui.WidgetStyle;
import imagej.util.ClassUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link ModuleItem} implementation describing an input or output of a plugin.
 * 
 * @author Curtis Rueden
 */
public class PluginModuleItem<T> extends AbstractModuleItem<T> {

	private final Field field;

	public PluginModuleItem(final ModuleInfo info, final Field field) {
		super(info);
		this.field = field;
	}

	// -- PluginModuleItem methods --

	public Field getField() {
		return field;
	}

	public Parameter getParameter() {
		return field.getAnnotation(Parameter.class);
	}

	// -- ModuleItem methods --

	@Override
	public String getName() {
		return field.getName();
	}

	@Override
	public String getLabel() {
		return getParameter().label();
	}

	@Override
	public String getDescription() {
		return getParameter().description();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<T> getType() {
		return (Class<T>) field.getType();
	}

	@Override
	public ItemVisibility getVisibility() {
		return getParameter().visibility();
	}

	@Override
	public boolean isRequired() {
		return getParameter().required();
	}

	@Override
	public boolean isPersisted() {
		return getParameter().persist();
	}

	@Override
	public String getPersistKey() {
		return getParameter().persistKey();
	}

	@Override
	public String getCallback() {
		return getParameter().callback();
	}

	@Override
	public WidgetStyle getWidgetStyle() {
		return getParameter().style();
	}

	@Override
	public T getMinimumValue() {
		final Class<T> saneType = ClassUtils.getNonprimitiveType(getType());
		return ClassUtils.convert(getParameter().min(), saneType);
	}

	@Override
	public T getMaximumValue() {
		final Class<T> saneType = ClassUtils.getNonprimitiveType(getType());
		return ClassUtils.convert(getParameter().max(), saneType);
	}

	@Override
	public Number getStepSize() {
		final Class<T> saneType = ClassUtils.getNonprimitiveType(getType());
		return ClassUtils.toNumber(getParameter().stepSize(), saneType);
	}

	@Override
	public int getColumnCount() {
		return getParameter().columns();
	}

	@Override
	public List<T> getChoices() {
		final ArrayList<T> choices = new ArrayList<T>();
		for (final String choice : getParameter().choices()) {
			choices.add(ClassUtils.convert(choice, getType()));
		}
		return choices;
	}

}
