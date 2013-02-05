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
import imagej.Contextual;
import imagej.ImageJ;
import imagej.event.EventSubscriber;
import imagej.event.EventUtils;
import imagej.module.DefaultModule;
import imagej.plugin.Parameter;
import imagej.util.ClassUtils;

import java.lang.reflect.Field;
import java.util.List;

/**
 * A class which can be extended to provide an ImageJ command with a variable
 * number of inputs and outputs. This class provides greater configurability,
 * but also greater complexity, than implementing the {@link Command} interface
 * and using only @{@link Parameter} annotations on instance fields.
 * 
 * @author Curtis Rueden
 */
public abstract class DynamicCommand extends DefaultModule implements
	Cancelable, Command, Contextual
{

	private ImageJ context;
	private DynamicCommandInfo info;

	/** Reason for cancelation, or null if not canceled. */
	private String cancelReason;

	/**
	 * The list of event subscribers, maintained to avoid garbage collection.
	 * 
	 * @see imagej.event.EventService#subscribe(Object)
	 */
	private List<EventSubscriber<?>> subscribers;

	// -- Object methods --

	@Override
	public void finalize() {
		// unregister any event handling methods
		EventUtils.unsubscribe(getContext(), subscribers);
	}

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
	public ImageJ getContext() {
		return context;
	}

	@Override
	public void setContext(final ImageJ context) {
		if (this.context != null) {
			throw new IllegalStateException("Context already set");
		}
		this.context = context;

		// populate service parameters
		final CommandInfo commandInfo =
			CommandUtils.populateServices(context, this);

		info = new DynamicCommandInfo(commandInfo, getClass());

		// NB: Subscribe to all events handled by this object.
		// This greatly simplifies event handling for subclasses.
		subscribers = EventUtils.subscribe(context, this);
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
