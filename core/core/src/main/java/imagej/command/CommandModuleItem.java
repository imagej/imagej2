/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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
 * #L%
 */

package imagej.command;

import imagej.module.AbstractModuleItem;
import imagej.module.ModuleInfo;
import imagej.module.ModuleItem;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.scijava.ItemIO;
import org.scijava.ItemVisibility;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Parameter;
import org.scijava.util.ClassUtils;
import org.scijava.util.ConversionUtils;
import org.scijava.util.NumberUtils;

/**
 * {@link ModuleItem} implementation describing an input or output of a command.
 * 
 * @author Curtis Rueden
 */
public class CommandModuleItem<T> extends AbstractModuleItem<T> {

	private final Field field;

	public CommandModuleItem(final ModuleInfo info, final Field field) {
		super(info);
		this.field = field;
	}

	// -- CommandModuleItem methods --

	public Field getField() {
		return field;
	}

	public Parameter getParameter() {
		return field.getAnnotation(Parameter.class);
	}

	// -- ModuleItem methods --

	@Override
	public Class<T> getType() {
		final Class<?> type = ClassUtils.getTypes(field, getDelegateClass()).get(0);
		@SuppressWarnings("unchecked")
		final Class<T> typedType = (Class<T>) type;
		return typedType;
	}

	@Override
	public Type getGenericType() {
		return ClassUtils.getGenericType(field, getDelegateClass());
	}

	@Override
	public ItemIO getIOType() {
		return getParameter().type();
	}

	@Override
	public ItemVisibility getVisibility() {
		return getParameter().visibility();
	}

	@Override
	public boolean isAutoFill() {
		return getParameter().autoFill();
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
	public String getInitializer() {
		return getParameter().initializer();
	}

	@Override
	public String getCallback() {
		return getParameter().callback();
	}

	@Override
	public String getWidgetStyle() {
		return getParameter().style();
	}

	@Override
	public T getMinimumValue() {
		final Class<T> saneType = ConversionUtils.getNonprimitiveType(getType());
		return ConversionUtils.convert(getParameter().min(), saneType);
	}

	@Override
	public T getMaximumValue() {
		final Class<T> saneType = ConversionUtils.getNonprimitiveType(getType());
		return ConversionUtils.convert(getParameter().max(), saneType);
	}

	@Override
	public Number getStepSize() {
		final Class<T> saneType = ConversionUtils.getNonprimitiveType(getType());
		return NumberUtils.toNumber(getParameter().stepSize(), saneType);
	}

	@Override
	public int getColumnCount() {
		return getParameter().columns();
	}

	@Override
	public List<T> getChoices() {
		final ArrayList<T> choices = new ArrayList<T>();
		for (final String choice : getParameter().choices()) {
			choices.add(ConversionUtils.convert(choice, getType()));
		}
		return choices;
	}

	// -- BasicDetails methods --

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
	public boolean is(final String key) {
		for (final Attr attr : getParameter().attrs()) {
			if (attr.name().equals(key)) return true;
		}
		return false;
	}

	@Override
	public String get(final String key) {
		for (final Attr attr : getParameter().attrs()) {
			if (attr.name().equals(key)) return attr.value();
		}
		return null;
	}

}
