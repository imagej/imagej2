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

import imagej.event.EventService;
import imagej.ext.AbstractUIDetails;
import imagej.ext.module.event.ModulesUpdatedEvent;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default {@link ModuleInfo} implementation.
 * <p>
 * By default, {@link ModuleItem}s are stored in {@link HashMap}s and
 * {@link ArrayList}s, internally. The {@link Module} {@link Class} given in the
 * {@link #setModuleClass(Class)} method is given as the delegate class name for
 * {@link #getDelegateClassName()}, and instantiated using a constructor that
 * takes a single {@link ModuleInfo} parameter.
 * </p>
 * 
 * @author Curtis Rueden
 */
public class DefaultModuleInfo extends AbstractUIDetails implements ModuleInfo
{

	/** Table of inputs, keyed on name. */
	private final Map<String, ModuleItem<?>> inputMap =
		new HashMap<String, ModuleItem<?>>();

	/** Table of outputs, keyed on name. */
	private final Map<String, ModuleItem<?>> outputMap =
		new HashMap<String, ModuleItem<?>>();

	/** Ordered list of input items. */
	private final List<ModuleItem<?>> inputList = new ArrayList<ModuleItem<?>>();

	/** Ordered list of output items. */
	private final List<ModuleItem<?>> outputList =
		new ArrayList<ModuleItem<?>>();

	private Class<? extends Module> moduleClass;

	// -- DefaultModuleInfo methods --

	/** Sets the module class described by this {@link ModuleInfo}. */
	public void setModuleClass(final Class<? extends Module> moduleClass) {
		this.moduleClass = moduleClass;
	}

	/** Gets the module class described by this {@link ModuleInfo}. */
	public Class<? extends Module> getModuleClass() {
		return moduleClass;
	}

	/** Adds an input to the list. */
	public void addInput(final ModuleItem<?> input) {
		inputMap.put(input.getName(), input);
		inputList.add(input);
	}

	/** Adds an output to the list. */
	public void addOutput(final ModuleItem<?> output) {
		outputMap.put(output.getName(), output);
		outputList.add(output);
	}

	/** Removes an input from the list. */
	public void removeInput(final ModuleItem<?> input) {
		inputMap.remove(input.getName());
		inputList.remove(input);
	}

	/** Removes an output from the list. */
	public void removeOutput(final ModuleItem<?> output) {
		outputMap.remove(output.getName());
		outputList.remove(output);
	}

	// -- ModuleInfo methods --

	@Override
	public ModuleItem<?> getInput(final String name) {
		return inputMap.get(name);
	}

	@Override
	public ModuleItem<?> getOutput(final String name) {
		return outputMap.get(name);
	}

	@Override
	public Iterable<ModuleItem<?>> inputs() {
		return Collections.unmodifiableList(inputList);
	}

	@Override
	public Iterable<ModuleItem<?>> outputs() {
		return Collections.unmodifiableList(outputList);
	}

	@Override
	public String getDelegateClassName() {
		return moduleClass.getName();
	}

	@Override
	public Module createModule() throws ModuleException {
		try {
			final Constructor<? extends Module> ctor =
				moduleClass.getConstructor(ModuleInfo.class);
			return ctor.newInstance(this);
		}
		catch (final Exception e) {
			// NB: Several types of exceptions; simpler to handle them all the same.
			throw new ModuleException(e);
		}
	}

	@Override
	public boolean canPreview() {
		return false;
	}

	@Override
	public boolean canCancel() {
		return true;
	}

	@Override
	public boolean canRunHeadless() {
		return false;
	}

	@Override
	public String getInitializer() {
		return null;
	}

	@Override
	public void update(final EventService eventService) {
		eventService.publish(new ModulesUpdatedEvent(this));
	}

	// -- UIDetails methods --

	@Override
	public String getTitle() {
		final String title = super.getTitle();
		if (!title.equals(getClass().getSimpleName())) return title;

		// use delegate class name rather than actual class name
		final String className = getDelegateClassName();
		final int dot = className.lastIndexOf(".");
		return dot < 0 ? className : className.substring(dot + 1);
	}

}
