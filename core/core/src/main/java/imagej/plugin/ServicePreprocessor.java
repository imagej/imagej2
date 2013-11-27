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

package imagej.plugin;

import imagej.command.Command;
import imagej.module.Module;
import imagej.module.ModuleInfo;
import imagej.module.ModuleItem;
import imagej.module.process.AbstractPreprocessorPlugin;
import imagej.module.process.PreprocessorPlugin;

import org.scijava.Context;
import org.scijava.Priority;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;

/**
 * The service preprocessor automatically populates module inputs that implement
 * {@link Service}.
 * <p>
 * Services are obtained from this preprocessor instance's application context.
 * </p>
 * <p>
 * Many modules (e.g., most {@link Command}s) use service fields annotated with @
 * {@link Parameter}, resulting in those parameters being populated when the
 * SciJava application context is injected (via {@link Context#inject(Object)}.
 * However, some modules may have service parameters which are programmatically
 * generated (i.e., returned directly as inputs from {@link ModuleInfo#inputs()}
 * and as such not populated by context injection. In that case, we need this
 * service preprocessor to fill in the service values.
 * </p>
 * 
 * @author Curtis Rueden
 */
@Plugin(type = PreprocessorPlugin.class,
	priority = Priority.VERY_HIGH_PRIORITY)
public class ServicePreprocessor extends AbstractPreprocessorPlugin {

	// -- ModuleProcessor methods --

	@Override
	public void process(final Module module) {
		for (final ModuleItem<?> input : module.getInfo().inputs()) {
			if (!input.isAutoFill()) continue; // cannot auto-fill this input
			final Class<?> type = input.getType();
			if (Service.class.isAssignableFrom(type)) {
				// input is a service
				@SuppressWarnings("unchecked")
				final ModuleItem<? extends Service> serviceInput =
					(ModuleItem<? extends Service>) input;
				setServiceValue(getContext(), module, serviceInput);
			}
			if (type.isAssignableFrom(getContext().getClass())) {
				// input is a compatible context
				final String name = input.getName();
				module.setInput(name, getContext());
				module.setResolved(name, true);
			}
		}
	}

	// -- Helper methods --

	private <S extends Service> void setServiceValue(final Context context,
		final Module module, final ModuleItem<S> input)
	{
		final S service = context.getService(input.getType());
		input.setValue(module, service);
		module.setResolved(input.getName(), true);
	}

}
