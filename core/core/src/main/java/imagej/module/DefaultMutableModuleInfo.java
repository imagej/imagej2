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

package imagej.module;

/**
 * Default {@link MutableModuleInfo} implementation.
 * <p>
 * The {@link Module} {@link Class} given in the {@link #setModuleClass(Class)}
 * method is used by {@link #getDelegateClassName()} as the delegate class name,
 * and instantiated using a no-argument constructor. As such, it is important
 * for downstream code to call the {@link #setModuleClass(Class)} method to
 * associate the module info with its module class prior to using the module
 * info for anything; the {@link #getDelegateClassName()} and
 * {@link #createModule()} methods will fail if the module class has not been
 * set.
 * </p>
 * 
 * @author Curtis Rueden
 */
public class DefaultMutableModuleInfo extends AbstractModuleInfo implements
	MutableModuleInfo
{

	private Class<? extends Module> moduleClass;

	// -- MutableModuleInfo methods --

	@Override
	public void setModuleClass(final Class<? extends Module> moduleClass) {
		this.moduleClass = moduleClass;
	}

	@Override
	public Class<? extends Module> getModuleClass() {
		return moduleClass;
	}

	@Override
	public void addInput(final ModuleItem<?> input) {
		inputMap.put(input.getName(), input);
		inputList.add(input);
	}

	@Override
	public void addOutput(final ModuleItem<?> output) {
		outputMap.put(output.getName(), output);
		outputList.add(output);
	}

	@Override
	public void removeInput(final ModuleItem<?> input) {
		inputMap.remove(input.getName());
		inputList.remove(input);
	}

	@Override
	public void removeOutput(final ModuleItem<?> output) {
		outputMap.remove(output.getName());
		outputList.remove(output);
	}

	// -- ModuleInfo methods --

	@Override
	public String getDelegateClassName() {
		return getModuleClass().getName();
	}

	@Override
	public Module createModule() throws ModuleException {
		try {
			return getModuleClass().newInstance();
		}
		catch (final Exception e) {
			// NB: Several types of exceptions; simpler to handle them all the same.
			throw new ModuleException(e);
		}
	}

}
