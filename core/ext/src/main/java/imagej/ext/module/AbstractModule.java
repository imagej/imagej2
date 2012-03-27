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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Abstract superclass of {@link Module} implementations.
 * <p>
 * By default, input and output values are stored in {@link HashMap}s.
 * </p>
 * 
 * @author Curtis Rueden
 */
public abstract class AbstractModule implements Module {

	/** The {@link ModuleInfo} describing the module. */
	private final ModuleInfo info;

	private final HashMap<String, Object> inputs;
	private final HashMap<String, Object> outputs;

	/** Table indicating resolved inputs. */
	private final HashSet<String> resolvedInputs;

	private MethodRef initializerRef;

	public AbstractModule(final ModuleInfo info) {
		this.info = info;
		inputs = new HashMap<String, Object>();
		outputs = new HashMap<String, Object>();
		resolvedInputs = new HashSet<String>();
	}

	// -- Module methods --

	@Override
	public void preview() {
		// do nothing by default
	}

	@Override
	public void cancel() {
		// do nothing by default
	}

	@Override
	public void initialize() {
		if (initializerRef == null) {
			initializerRef =
				new MethodRef(info.getDelegateClassName(), info.getInitializer());
		}
		initializerRef.execute(getDelegateObject());
	}

	@Override
	public ModuleInfo getInfo() {
		return info;
	}

	@Override
	public Object getDelegateObject() {
		return this;
	}

	@Override
	public Object getInput(final String name) {
		return inputs.get(name);
	}

	@Override
	public Object getOutput(final String name) {
		return outputs.get(name);
	}

	@Override
	public Map<String, Object> getInputs() {
		return createMap(info.inputs(), false);
	}

	@Override
	public Map<String, Object> getOutputs() {
		return createMap(info.outputs(), true);
	}

	@Override
	public void setInput(final String name, final Object value) {
		inputs.put(name, value);
	}

	@Override
	public void setOutput(final String name, final Object value) {
		outputs.put(name, value);
	}

	@Override
	public void setInputs(final Map<String, Object> inputs) {
		for (final String name : inputs.keySet()) {
			setInput(name, inputs.get(name));
		}
	}

	@Override
	public void setOutputs(final Map<String, Object> outputs) {
		for (final String name : outputs.keySet()) {
			setOutput(name, outputs.get(name));
		}
	}

	@Override
	public boolean isResolved(final String name) {
		return resolvedInputs.contains(name);
	}

	@Override
	public void setResolved(final String name, final boolean resolved) {
		if (resolved) resolvedInputs.add(name);
		else resolvedInputs.remove(name);
	}

	// -- Helper methods --

	private Map<String, Object> createMap(final Iterable<ModuleItem<?>> items,
		final boolean outputMap)
	{
		final Map<String, Object> map = new HashMap<String, Object>();
		for (final ModuleItem<?> item : items) {
			final String name = item.getName();
			final Object value = outputMap ? getOutput(name) : getInput(name);
			map.put(name, value);
		}
		return map;
	}

}
