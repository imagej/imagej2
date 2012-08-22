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

package imagej.widget;

import imagej.ImageJ;
import imagej.ext.InstantiableException;
import imagej.ext.plugin.Plugin;
import imagej.ext.plugin.PluginInfo;
import imagej.ext.plugin.PluginService;
import imagej.log.LogService;
import imagej.service.AbstractService;
import imagej.service.Service;

import java.util.List;

/**
 * Service for managing available input widgets.
 * 
 * @author Curtis Rueden
 * @see InputWidget
 */
@Plugin(type = Service.class)
public class WidgetService extends AbstractService {

	private final PluginService pluginService;
	private final LogService log;

	public WidgetService() {
		// NB: Required by SezPoz.
		super(null);
		throw new UnsupportedOperationException();
	}

	public WidgetService(final ImageJ context, final PluginService pluginService,
		final LogService log)
	{
		super(context);
		this.pluginService = pluginService;
		this.log = log;
	}

	/** Creates a widget that represents the given widget model. */
	public InputWidget<?, ?> createWidget(final WidgetModel model) {
		@SuppressWarnings("rawtypes")
		final List<PluginInfo<? extends InputWidget>> infos =
			pluginService.getPluginsOfType(InputWidget.class);
		for (@SuppressWarnings("rawtypes")
		final PluginInfo<? extends InputWidget> info : infos)
		{
			final InputWidget<?, ?> widget;
			try {
				widget = info.createInstance();
			}
			catch (final InstantiableException e) {
				log.error("Invalid widget: " + info.getClassName(), e);
				continue;
			}
			if (widget.isCompatible(model)) {
				widget.initialize(model);
				return widget;
			}
		}
		log.warn("No widget found for input: " + model.getItem().getName());
		return null;
	}

}
