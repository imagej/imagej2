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

package imagej.options;

import imagej.command.CommandService;

import java.util.List;

import org.scijava.log.LogService;
import org.scijava.object.ObjectService;
import org.scijava.plugin.AbstractSingletonService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;

/**
 * Default service for keeping track of the available options and their
 * settings.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 * @see OptionsPlugin
 */
@Plugin(type = Service.class)
public class DefaultOptionsService extends
	AbstractSingletonService<OptionsPlugin> implements OptionsService
{

	@Parameter
	private LogService log;

	@Parameter
	private ObjectService objectService;

	// NB: Required by DynamicCommand, the OptionsPlugin superclass.
	@Parameter
	private CommandService commandService;

	// -- OptionsService methods --

	@Override
	public <O extends OptionsPlugin> O getOptions(final Class<O> optionsClass) {
		final List<O> objects = objectService.getObjects(optionsClass);
		return objects == null || objects.isEmpty() ? null : objects.get(0);
	}

	@Override
	public void reset() {
		final List<OptionsPlugin> optionsPlugins = getInstances();
		for (final OptionsPlugin plugin : optionsPlugins) {
			plugin.reset();
		}
	}

	// -- SingletonService methods --

	@Override
	public List<OptionsPlugin> getInstances() {
		final List<OptionsPlugin> instances = super.getInstances();

		// load previous values from persistent storage
		for (final OptionsPlugin options : instances) {
			options.load();
		}

		return instances;
	}

	// -- PTService methods --

	@Override
	public Class<OptionsPlugin> getPluginType() {
		return OptionsPlugin.class;
	}

}
