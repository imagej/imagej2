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

import java.util.Collections;
import java.util.List;

import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

/**
 * Abstract base class for {@link HandlerService}s.
 * 
 * @author Curtis Rueden
 * @param <DT> Base data type handled by the handlers.
 * @param <PT> Plugin type of the handlers.
 */
public abstract class AbstractHandlerService<DT, PT extends TypedPlugin<DT>>
	extends AbstractTypedService<DT, PT> implements HandlerService<DT, PT>
{

	@Parameter
	private LogService log;

	// TODO: Listen for PluginsAddedEvent and PluginsRemovedEvent
	// and update the list of singletons accordingly.

	/** List of handler plugin instances. */
	private List<PT> handlers;

	// -- HandlerService methods --

	@Override
	public <D extends DT> TypedPlugin<D> getHandler(final D data) {
		final PT handler = handler(data);
		@SuppressWarnings("unchecked")
		final TypedPlugin<D> typedHandler = (TypedPlugin<D>) handler;
		return typedHandler;
	}

	@Override
	public List<PT> getHandlers() {
		return handlers;
	}

	// -- Service methods --

	@Override
	public void initialize() {
		createHandlers();
	}

	// -- Typed methods --

	@Override
	public boolean supports(final Object data) {
		return handler(data) != null;
	}

	// -- Helper methods --

	private void createHandlers() {
		final List<PT> instances =
			getPluginService().createInstancesOfType(getPluginType());
		handlers = Collections.unmodifiableList(instances);

		log.info("Found " + handlers.size() + " " +
			getPluginType().getSimpleName() + " plugins.");
	}

	private PT handler(final Object data) {
		for (final PT handler : getHandlers()) {
			if (handler.supports(data)) return handler;
		}
		return null;
	}

}
