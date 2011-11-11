//
// TypeChangeService.java
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

package imagej.core.plugins.typechange;

import imagej.AbstractService;
import imagej.ImageJ;
import imagej.Service;
import imagej.data.Dataset;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.ext.display.Display;
import imagej.ext.display.event.DisplayActivatedEvent;
import imagej.ext.module.event.ModulesUpdatedEvent;
import imagej.ext.plugin.PluginModuleInfo;
import imagej.ext.plugin.PluginService;

import java.util.List;

/**
 * Service for keeping the Type submenu synced with the active display.
 * 
 * @author Curtis Rueden
 */
@Service
public final class TypeChangeService extends AbstractService {

	private final EventService eventService;
	private final PluginService pluginService;
	private final ImageDisplayService imageDisplayService;

	// -- Constructors --

	public TypeChangeService() {
		// NB: Required by SezPoz.
		super(null);
		throw new UnsupportedOperationException();
	}

	public TypeChangeService(final ImageJ context,
		final EventService eventService, final PluginService pluginService,
		final ImageDisplayService imageDisplayService)
	{
		super(context);
		this.eventService = eventService;
		this.pluginService = pluginService;
		this.imageDisplayService = imageDisplayService;
	}

	// -- TypeChangeService methods --

	public EventService getEventService() {
		return eventService;
	}

	public PluginService getPluginService() {
		return pluginService;
	}

	public ImageDisplayService getImageDisplayService() {
		return imageDisplayService;
	}

	/** Selects the module matching the active dataset's type. */
	public void refreshSelectedType(final ImageDisplay display) {
		final Dataset dataset = imageDisplayService.getActiveDataset(display);
		final String typeLabel = dataset == null ? "" : dataset.getTypeLabelShort();
		final String suffix = ".ChangeTo" + typeLabel.toUpperCase();

		final List<PluginModuleInfo<TypeChanger>> plugins =
			pluginService.getRunnablePluginsOfType(TypeChanger.class);
		for (final PluginModuleInfo<TypeChanger> info : plugins) {
			final boolean selected = info.getDelegateClassName().endsWith(suffix);
			info.setSelected(selected);
		}

		// notify interested parties that the TypeChanger plugins have changed
		eventService.publish(new ModulesUpdatedEvent(plugins));
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(final DisplayActivatedEvent event) {
		final Display<?> display = event.getDisplay();
		if (display instanceof ImageDisplay) {
			refreshSelectedType((ImageDisplay) display);
		}
	}

}
