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

package imagej.command;

import imagej.Cancelable;
import imagej.module.DefaultMutableModule;

import java.lang.reflect.Field;

import org.scijava.Context;
import org.scijava.Contextual;
import org.scijava.plugin.Parameter;
import org.scijava.util.ClassUtils;

/**
 * A class which can be extended to provide an ImageJ command with a variable
 * number of inputs and outputs. This class provides greater configurability,
 * but also greater complexity, than implementing the {@link Command} interface
 * and using only @{@link Parameter} annotations on instance fields.
 * 
 * @author Curtis Rueden
 */
public abstract class DynamicCommand extends DefaultMutableModule implements
	Cancelable, Command, Contextual
{

	private Context context;
	private DynamicCommandInfo info;

	/** Reason for cancelation, or null if not canceled. */
	private String cancelReason;

	// -- Module methods --

	@Override
	public DynamicCommandInfo getInfo() {
		return info;
	}

	@Override
	public Object getInput(final String name) {
		final Field field = info.getInputField(name);
		if (field == null) return super.getInput(name);
		return ClassUtils.getValue(field, this);
	}

	@Override
	public Object getOutput(final String name) {
		final Field field = info.getOutputField(name);
		if (field == null) return super.getInput(name);
		return ClassUtils.getValue(field, this);
	}

	@Override
	public void setInput(final String name, final Object value) {
		final Field field = info.getInputField(name);
		if (field == null) super.setInput(name, value);
		else ClassUtils.setValue(field, this, value);
	}

	@Override
	public void setOutput(final String name, final Object value) {
		final Field field = info.getOutputField(name);
		if (field == null) super.setOutput(name, value);
		else ClassUtils.setValue(field, this, value);
	}

	// -- Contextual methods --

	@Override
	public Context getContext() {
		return context;
	}

	@Override
	public void setContext(final Context context) {
		if (this.context == null) {
			this.context = context;
		}
		else if (this.context != context) {
			throw new IllegalStateException("Context already set");
		}

		// inject context and service parameters, and subscribe to events
		context.inject(this);

		// create associated dynamic metadata
		final CommandService commandService =
			context.getService(CommandService.class);
		final CommandInfo commandInfo = commandService.getCommand(getClass());
		info = new DynamicCommandInfo(commandInfo, getClass());
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
