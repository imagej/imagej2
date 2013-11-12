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

package imagej.script;

import imagej.module.AbstractModuleInfo;
import imagej.module.DefaultMutableModuleItem;
import imagej.module.Module;
import imagej.module.ModuleException;

import org.scijava.Context;
import org.scijava.Contextual;
import org.scijava.plugin.Parameter;

/**
 * Metadata about a script module.
 * 
 * @author Curtis Rueden
 * @author Johannes Schindelin
 */
public class ScriptInfo extends AbstractModuleInfo implements Contextual {

	private final String path;

	@Parameter
	private Context context;

	public ScriptInfo(final String path, final Context context) {
		this.path = path;
		setContext(context);
	}

	// -- ScriptInfo methods --

	public String getPath() {
		return path;
	}

	public boolean hasInputs() {
		return !inputMap.isEmpty();
	}

	public <T> void addInput(final String name, final Class<T> type) {
		final DefaultMutableModuleItem<T> item =
			new DefaultMutableModuleItem<T>(this, name, type);
		inputMap.put(name, item);
		inputList.add(item);
	}

	// -- ModuleInfo methods --

	@Override
	public String getDelegateClassName() {
		return ScriptModule.class.getName();
	}

	@Override
	public Module createModule() throws ModuleException {
		return new ScriptModule(this);
	}

	// -- Contextual methods --

	@Override
	public Context getContext() {
		return context;
	}

	@Override
	public void setContext(final Context context) {
		context.inject(this);
	}

}
