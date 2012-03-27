/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
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

package imagej.ext.module;

import imagej.ext.module.ui.WidgetStyle;
import imagej.util.ClassUtils;
import imagej.util.NumberUtils;
import imagej.util.Prefs;
import imagej.util.StringMaker;

import java.util.List;

/**
 * Abstract superclass of {@link ModuleItem} implementations.
 * 
 * @author Curtis Rueden
 */
public abstract class AbstractModuleItem<T> implements ModuleItem<T> {

	private final ModuleInfo info;

	private MethodRef initializerRef;
	private MethodRef callbackRef;

	public AbstractModuleItem(final ModuleInfo info) {
		this.info = info;
	}

	// -- Object methods --

	@Override
	public String toString() {
		final StringMaker sm = new StringMaker();
		sm.append("label", getLabel());
		sm.append("description", getDescription());
		sm.append("visibility", getVisibility(), ItemVisibility.NORMAL);
		sm.append("required", isRequired());
		sm.append("persisted", isPersisted());
		sm.append("persistKey", getPersistKey());
		sm.append("callback", getCallback());
		sm.append("widgetStyle", getWidgetStyle(), WidgetStyle.DEFAULT);
		sm.append("min", getMinimumValue());
		sm.append("max", getMaximumValue());
		sm.append("stepSize", getStepSize(), NumberUtils.toNumber("1", getType()));
		sm.append("columnCount", getColumnCount(), 6);
		sm.append("choices", getChoices());
		return getName() + ": " + sm.toString();
	}

	// -- ModuleItem methods --

	@Override
	public ItemIO getIOType() {
		return ItemIO.INPUT;
	}

	@Override
	public boolean isInput() {
		final ItemIO ioType = getIOType();
		return ioType == ItemIO.INPUT || ioType == ItemIO.BOTH;
	}

	@Override
	public boolean isOutput() {
		final ItemIO ioType = getIOType();
		return ioType == ItemIO.OUTPUT || ioType == ItemIO.BOTH;
	}

	@Override
	public ItemVisibility getVisibility() {
		return ItemVisibility.NORMAL;
	}

	@Override
	public boolean isAutoFill() {
		return true;
	}

	@Override
	public boolean isRequired() {
		return false;
	}

	@Override
	public boolean isPersisted() {
		return true;
	}

	@Override
	public String getPersistKey() {
		return null;
	}

	/**
	 * Returns the persisted value of a ModuleItem. Returns null if nothing has
	 * been persisted. It is the API user's responsibility to check the return
	 * value for null.
	 */
	@Override
	public T loadValue() {
		// if there is nothing to load from persistence return nothing
		if (!isPersisted()) return null;

		final String sValue;
		final String persistKey = getPersistKey();
		if (persistKey == null || persistKey.isEmpty()) {
			final Class<?> prefClass = getDelegateClass();
			final String prefKey = getName();
			sValue = Prefs.get(prefClass, prefKey);
		}
		else sValue = Prefs.get(persistKey);

		// if persisted value has never been set before return null
		if (sValue == null) return null;

		return ClassUtils.convert(sValue, getType());
	}

	@Override
	public void saveValue(final T value) {
		if (!isPersisted()) return;

		final String sValue = value == null ? "" : value.toString();

		final String persistKey = getPersistKey();
		if (persistKey == null || persistKey.isEmpty()) {
			final Class<?> prefClass = getDelegateClass();
			final String prefKey = getName();
			Prefs.put(prefClass, prefKey, sValue);
		}
		else Prefs.put(persistKey, sValue);
	}

	@Override
	public String getInitializer() {
		return null;
	}

	@Override
	public void initialize(final Module module) {
		if (initializerRef == null) {
			initializerRef =
				new MethodRef(info.getDelegateClassName(), getInitializer());
		}
		initializerRef.execute(module.getDelegateObject());
	}

	@Override
	public String getCallback() {
		return null;
	}

	@Override
	public void callback(final Module module) {
		if (callbackRef == null) {
			callbackRef = new MethodRef(info.getDelegateClassName(), getCallback());
		}
		callbackRef.execute(module.getDelegateObject());
	}

	@Override
	public WidgetStyle getWidgetStyle() {
		return WidgetStyle.DEFAULT;
	}

	@Override
	public T getMinimumValue() {
		return null;
	}

	@Override
	public T getMaximumValue() {
		return null;
	}

	@Override
	public Number getStepSize() {
		if (!ClassUtils.isNumber(getType())) return null;
		return NumberUtils.toNumber("1", getType());
	}

	@Override
	public int getColumnCount() {
		return 6;
	}

	@Override
	public List<T> getChoices() {
		return null;
	}

	@Override
	public T getValue(final Module module) {
		final Object result;
		if (isInput()) result = module.getInput(getName());
		else if (isOutput()) result = module.getOutput(getName());
		else result = null;
		@SuppressWarnings("unchecked")
		final T value = (T) result;
		return value;
	}

	@Override
	public void setValue(final Module module, final T value) {
		if (isInput()) module.setInput(getName(), value);
		if (isOutput()) module.setOutput(getName(), value);
	}

	// -- BasicDetails methods --

	@Override
	public String getName() {
		return null;
	}

	@Override
	public String getLabel() {
		return null;
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public void setName(final String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLabel(final String description) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDescription(final String description) {
		throw new UnsupportedOperationException();
	}

	// -- Helper methods --

	private Class<?> getDelegateClass() {
		return ClassUtils.loadClass(info.getDelegateClassName());
	}

}
