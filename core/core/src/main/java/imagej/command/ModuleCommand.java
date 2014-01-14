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

package imagej.command;

import imagej.Cancelable;
import imagej.module.AbstractModule;
import imagej.module.Module;

import org.scijava.Context;
import org.scijava.Contextual;
import org.scijava.plugin.Parameter;

/**
 * An ImageJ command which implements {@link Module} directly (rather than using
 * the {@link CommandModule} adapter class). This is useful for commands which
 * want to inspect and manipulate their own inputs and outputs programmatically.
 * 
 * @author Curtis Rueden
 */
public abstract class ModuleCommand extends AbstractModule implements
	Cancelable, Command, Contextual
{

	@Parameter
	private Context context;

	@Parameter
	private CommandService commandService;

	private CommandInfo info;

	/** Reason for cancelation, or null if not canceled. */
	private String cancelReason;

	// -- Module methods --

	@Override
	public CommandInfo getInfo() {
		if (info == null) {
			// NB: Obtain metadata lazily.
			info = commandService.getCommand(getClass());
		}
		return info;
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

	// -- Cancelable methods --

	@Override
	public boolean isCanceled() {
		return cancelReason != null;
	}

	@Override
	public String getCancelReason() {
		return cancelReason;
	}

	// -- Internal methods --

	/** Cancels the command execution, with the given reason for doing so. */
	protected void cancel(final String reason) {
		cancelReason = reason;
	}

}
