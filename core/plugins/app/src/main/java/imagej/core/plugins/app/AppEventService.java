//
// AppEventService.java
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

package imagej.core.plugins.app;

import imagej.AbstractService;
import imagej.ImageJ;
import imagej.Service;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.ext.plugin.PluginService;
import imagej.platform.event.AppAboutEvent;
import imagej.platform.event.AppPreferencesEvent;
import imagej.platform.event.AppQuitEvent;

/**
 * Service for executing plugins in response to application events.
 * 
 * @author Curtis Rueden
 */
@Service
public final class AppEventService extends AbstractService {

	private final EventService eventService;
	private final PluginService pluginService;

	// -- Constructors --

	public AppEventService() {
		// NB: Required by SezPoz.
		super(null);
		throw new UnsupportedOperationException();
	}

	public AppEventService(final ImageJ context,
		final EventService eventService, final PluginService pluginService)
	{
		super(context);
		this.eventService = eventService;
		this.pluginService = pluginService;
	}

	// -- AppEventService methods --

	public EventService getEventService() {
		return eventService;
	}

	public PluginService getPluginService() {
		return pluginService;
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(@SuppressWarnings("unused")
	final AppAboutEvent event)
	{
		getPluginService().run(AboutImageJ.class);
	}

	@EventHandler
	protected void onEvent(@SuppressWarnings("unused")
	final AppPreferencesEvent event)
	{
		getPluginService().run(ShowPrefs.class);
	}

	@EventHandler
	protected void onEvent(@SuppressWarnings("unused")
	final AppQuitEvent event)
	{
		getPluginService().run(QuitProgram.class);
	}

}
