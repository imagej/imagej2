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
import imagej.util.Log;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Helper class for executing a {@link Module}, including pre- and
 * post-processing and event notification.
 * <p>
 * This class implements both {@link Runnable} and {@link Callable}, to make it
 * easier to invoke in a variety of ways, such as with the
 * {@link java.util.concurrent} package.
 * </p>
 * 
 * @author Curtis Rueden
 */
public class ModuleRunner implements Callable<Module>, Runnable {

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

	// -- ModuleRunner methods --

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

	// -- Callable methods --

	@Override
	public Module call() {
		try {
			run();
		}
		catch (final RuntimeException e) {
			Log.error("Module threw exception", e);
			throw e;
		}
		return module;
	}

	// -- Runnable methods --

	/**
	 * Executes the module, including pre- and post-processing and event
	 * notification.
	 */
	@Override
	public void run() {
		if (module == null) return;
		final EventService eventService = ImageJ.get(EventService.class);

		// execute module
		eventService.publish(new ModuleStartedEvent(module));
		final boolean ok = preProcess();
		if (!ok) {
			// execution canceled
			module.cancel();
			eventService.publish(new ModuleCanceledEvent(module));
			return;
		}
		eventService.publish(new ModuleExecutingEvent(module));
		module.run();
		eventService.publish(new ModuleExecutedEvent(module));
		postProcess();
		eventService.publish(new ModuleFinishedEvent(module));
	}

}
