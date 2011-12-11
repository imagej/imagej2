//
// ServicePreprocessor.java
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

import imagej.IService;
import imagej.ImageJ;
import imagej.ext.Priority;
import imagej.ext.module.Module;
import imagej.ext.module.ModuleItem;
import imagej.ext.plugin.process.PreprocessorPlugin;

/**
 * The service preprocessor automatically populates module inputs that implement
 * {@link IService}.
 * <p>
 * Service objects are obtained from the ImageJ context associated with the
 * current thread.
 * </p>
 * 
 * @author Curtis Rueden
 */
@Plugin(type = PreprocessorPlugin.class,
	priority = Priority.VERY_HIGH_PRIORITY)
public class ServicePreprocessor implements PreprocessorPlugin {

	private ImageJ theContext;

	public ServicePreprocessor() {}

	public ServicePreprocessor(final ImageJ context) {
		theContext = context;
	}

	// -- PluginPreprocessor methods --

	@Override
	public boolean canceled() {
		return false;
	}

	@Override
	public String getMessage() {
		return null;
	}

	// -- ModuleProcessor methods --

	@Override
	public void process(final Module module) {
		final ImageJ context =
			theContext == null ? ImageJ.getContext() : theContext;

		for (final ModuleItem<?> input : module.getInfo().inputs()) {
			final Class<?> type = input.getType();
			if (IService.class.isAssignableFrom(type)) {
				// input is a service
				@SuppressWarnings("unchecked")
				final ModuleItem<? extends IService> serviceInput =
					(ModuleItem<? extends IService>) input;
				setServiceValue(context, module, serviceInput);
			}
			if (context.getClass().isAssignableFrom(type)) {
				// input is a compatible context
				final String name = input.getName();
				module.setInput(name, context);
				module.setResolved(name, true);
			}
		}
	}

	// -- Helper methods --

	private <S extends IService> void setServiceValue(final ImageJ context,
		final Module module, final ModuleItem<S> input)
	{
		final S service = context.getService(input.getType());
		input.setValue(module, service);
		module.setResolved(input.getName(), true);
	}

}
