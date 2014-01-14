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

import imagej.Cancelable;
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
import java.util.concurrent.Callable;

import org.scijava.AbstractContextual;
import org.scijava.Context;
import org.scijava.app.StatusService;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

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
public class ModuleRunner extends AbstractContextual implements
	Callable<Module>, Runnable
{

	private final Module module;
	private final List<? extends ModulePreprocessor> pre;
	private final List<? extends ModulePostprocessor> post;

	@Parameter(required = false)
	private EventService es;

	@Parameter(required = false)
	private StatusService ss;

	@Parameter(required = false)
	private LogService log;

	public ModuleRunner(final Context context, final Module module,
		final List<? extends ModulePreprocessor> pre,
		final List<? extends ModulePostprocessor> post)
	{
		setContext(context);
		this.module = module;
		this.pre = pre;
		this.post = post;
	}

	// -- ModuleRunner methods --

	/**
	 * Feeds the module through the {@link ModulePreprocessor}s.
	 * 
	 * @return The preprocessor that canceled the execution, or null if all
	 *         preprocessors completed successfully.
	 */
	public ModulePreprocessor preProcess() {
		if (pre == null) return null; // no preprocessors

		for (final ModulePreprocessor p : pre) {
			p.process(module);
			if (es != null) es.publish(new ModulePreprocessEvent(module, p));
			if (p.isCanceled()) return p;
		}
		return null;
	}

	/** Feeds the module through the {@link ModulePostprocessor}s. */
	public void postProcess() {
		if (post == null) return; // no postprocessors

		for (final ModulePostprocessor p : post) {
			p.process(module);
			if (es != null) es.publish(new ModulePostprocessEvent(module, p));
		}
	}

	// -- Callable methods --

	@Override
	public Module call() {
		try {
			run();
		}
		catch (final RuntimeException exc) {
			if (log != null) log.error("Module threw exception", exc);
			throw exc;
		}
		catch (final Error err) {
			if (log != null) log.error("Module threw error", err);
			throw err;
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

		final String title = module.getInfo().getTitle();

		// announce start of execution process
		if (ss != null) ss.showStatus("Running command: " + title);
		if (es != null) es.publish(new ModuleStartedEvent(module));

		// execute preprocessors
		final ModulePreprocessor canceler = preProcess();
		if (canceler != null) {
			// module execution was canceled by preprocessor
			cancel(title, canceler.getCancelReason());
			return;
		}

		// execute module
		if (es != null) es.publish(new ModuleExecutingEvent(module));
		module.run();
		if (module instanceof Cancelable) {
			final Cancelable cancelable = (Cancelable) module;
			if (cancelable.isCanceled()) {
				// module execution was canceled by the module itself
				cancel(title, cancelable.getCancelReason());
				return;
			}
		}
		if (es != null) es.publish(new ModuleExecutedEvent(module));

		// execute postprocessors
		postProcess();

		// announce completion of execution process
		if (es != null) es.publish(new ModuleFinishedEvent(module));
		if (ss != null) ss.showStatus("Command finished: " + title);
	}

	// -- Helper methods --

	private void cancel(final String title, final String reason) {
		if (ss != null) ss.showStatus("Canceling command: " + title);
		module.cancel();
		if (es != null) es.publish(new ModuleCanceledEvent(module, reason));
		if (ss != null) {
			ss.showStatus("Command canceled: " + title);
			if (reason != null) ss.warn(reason);
		}
	}

}
