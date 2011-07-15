//
// ModuleRunner.java
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

package imagej.module;

import imagej.event.Events;
import imagej.event.StatusEvent;
import imagej.module.event.ModuleCanceledEvent;
import imagej.module.event.ModuleExecutedEvent;
import imagej.module.event.ModuleExecutingEvent;
import imagej.module.event.ModuleFinishedEvent;
import imagej.module.event.ModulePostprocessEvent;
import imagej.module.event.ModulePreprocessEvent;
import imagej.module.event.ModuleStartedEvent;
import imagej.module.process.ModulePostprocessor;
import imagej.module.process.ModulePreprocessor;

import java.util.List;

/**
 * Helper class for executing a {@link Module}, including pre- and
 * post-processing and event notification.
 * 
 * @author Curtis Rueden
 */
public class ModuleRunner {

	private final Module module;
	private final List<? extends ModulePreprocessor> pre;
	private final List<? extends ModulePostprocessor> post;

	public ModuleRunner(final Module module,
		final List<? extends ModulePreprocessor> preprocessors,
		final List<? extends ModulePostprocessor> postprocessors)
	{
		this.module = module;
		pre = preprocessors;
		post = postprocessors;
	}

	/**
	 * Executes the module, including pre- and post-processing and event
	 * notification.
	 */
	public void run() {
		if (module == null) return;

		// execute module
		Events.publish(new ModuleStartedEvent(module));
		final boolean ok = preProcess();
		if (!ok) {
			// execution canceled
			Events.publish(new ModuleCanceledEvent(module));
			return;
		}
		Events.publish(new ModuleExecutingEvent(module));
		module.run();
		Events.publish(new ModuleExecutedEvent(module));
		postProcess();
		Events.publish(new ModuleFinishedEvent(module));
	}

	/** Feeds the module through the available {@link ModulePreprocessor}s. */
	public boolean preProcess() {
		for (final ModulePreprocessor p : pre) {
			p.process(module);
			Events.publish(new ModulePreprocessEvent(module, p));
			if (p.canceled()) {
				// notify interested parties of any warning messages
				final String cancelMessage = p.getMessage();
				if (cancelMessage != null) {
					Events.publish(new StatusEvent(cancelMessage, true));
				}
				return false;
			}
		}
		return true;
	}

	/** Feeds the module through the available {@link ModulePostprocessor}s. */
	public void postProcess() {
		for (final ModulePostprocessor p : post) {
			p.process(module);
			Events.publish(new ModulePostprocessEvent(module, p));
		}
	}

}
