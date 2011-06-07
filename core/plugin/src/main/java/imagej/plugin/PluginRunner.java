//
// PluginRunner.java
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

package imagej.plugin;

import imagej.ImageJ;
import imagej.event.Events;
import imagej.event.StatusEvent;
import imagej.plugin.event.PluginCanceledEvent;
import imagej.plugin.event.PluginFinishedEvent;
import imagej.plugin.event.PluginPostprocessEvent;
import imagej.plugin.event.PluginPreprocessEvent;
import imagej.plugin.event.PluginRunEvent;
import imagej.plugin.event.PluginStartedEvent;
import imagej.plugin.process.PluginPostprocessor;
import imagej.plugin.process.PluginPreprocessor;
import imagej.util.Log;

/**
 * Executes a runnable plugin.
 * 
 * @author Curtis Rueden
 */
public class PluginRunner<T extends RunnablePlugin> {

	private PluginModule<T> module;

	public PluginRunner(final PluginEntry<T> entry) {
		try {
			module = entry.createModule();
		}
		catch (final PluginException e) {
			Log.error(e);
		}
	}

	public PluginModule<T> getModule() {
		return module;
	}

	public T run() {
		if (module == null) return null;
		final T plugin = module.getPlugin();

		// execute plugin
		Events.publish(new PluginStartedEvent(module));
		final boolean ok = preProcess();
		if (!ok) {
			// execution canceled
			Events.publish(new PluginCanceledEvent(module));
			return null;
		}
		plugin.run();
		Events.publish(new PluginRunEvent(module));
		postProcess();
		Events.publish(new PluginFinishedEvent(module));

		return plugin;
	}

	public boolean preProcess() {
		final PluginManager pluginManager = ImageJ.get(PluginManager.class);
		for (final PluginEntry<PluginPreprocessor> p :
			pluginManager.getPlugins(PluginPreprocessor.class))
		{
			try {
				final PluginPreprocessor processor = p.createInstance();
				processor.process(module);
				Events.publish(new PluginPreprocessEvent(module, processor));
				if (processor.canceled()) {
					// notify interested parties of any warning messages
					final String cancelMessage = processor.getMessage();
					if (cancelMessage != null) {
						Events.publish(new StatusEvent(cancelMessage, true));
					}
					return false;
				}
			}
			catch (final PluginException e) {
				Log.error(e);
			}
		}
		return true;
	}

	public void postProcess() {
		final PluginManager pluginManager = ImageJ.get(PluginManager.class);
		for (final PluginEntry<PluginPostprocessor> p :
			pluginManager.getPlugins(PluginPostprocessor.class))
		{
			try {
				final PluginPostprocessor processor = p.createInstance();
				processor.process(module);
				Events.publish(new PluginPostprocessEvent(module, processor));
			}
			catch (final PluginException e) {
				Log.error(e);
			}
		}
	}

}
