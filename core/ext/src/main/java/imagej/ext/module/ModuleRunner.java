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

package imagej.ext.module;

import imagej.ImageJ;
import imagej.event.EventService;
import imagej.event.StatusEvent;
import imagej.ext.module.event.ModuleCanceledEvent;
import imagej.ext.module.event.ModuleExecutedEvent;
import imagej.ext.module.event.ModuleExecutingEvent;
import imagej.ext.module.event.ModuleFinishedEvent;
import imagej.ext.module.event.ModulePostprocessEvent;
import imagej.ext.module.event.ModulePreprocessEvent;
import imagej.ext.module.event.ModuleStartedEvent;
import imagej.ext.module.process.ModulePostprocessor;
import imagej.ext.module.process.ModulePreprocessor;

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
		final List<? extends ModulePreprocessor> pre,
		final List<? extends ModulePostprocessor> post)
	{
		this.module = module;
		this.pre = pre;
		this.post = post;
	}

	/**
	 * Executes the module, including pre- and post-processing and event
	 * notification.
	 */
	public void run() {
		if (module == null) return;
		final EventService eventService = ImageJ.get(EventService.class);

		// execute module
		eventService.publish(new ModuleStartedEvent(module));
		final boolean ok = preProcess();
		if (!ok) {
			// execution canceled
			eventService.publish(new ModuleCanceledEvent(module));
			return;
		}
		eventService.publish(new ModuleExecutingEvent(module));
		module.run();
		eventService.publish(new ModuleExecutedEvent(module));
		postProcess();
		eventService.publish(new ModuleFinishedEvent(module));
	}

	/** Feeds the module through the {@link ModulePreprocessor}s. */
	public boolean preProcess() {
		if (pre == null) return true; // no preprocessors
		final EventService eventService = ImageJ.get(EventService.class);

		for (final ModulePreprocessor p : pre) {
			p.process(module);
			eventService.publish(new ModulePreprocessEvent(module, p));
			if (p.canceled()) {
				// notify interested parties of any warning messages
				final String cancelMessage = p.getMessage();
				if (cancelMessage != null) {
					eventService.publish(new StatusEvent(cancelMessage, true));
				}
				return false;
			}
		}
		return true;
	}

	/** Feeds the module through the {@link ModulePostprocessor}s. */
	public void postProcess() {
		if (post == null) return; // no postprocessors
		final EventService eventService = ImageJ.get(EventService.class);

		for (final ModulePostprocessor p : post) {
			p.process(module);
			eventService.publish(new ModulePostprocessEvent(module, p));
		}
	}

}
