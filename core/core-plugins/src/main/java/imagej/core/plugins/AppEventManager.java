//
// AppEventManager.java
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

package imagej.core.plugins;

import imagej.ImageJ;
import imagej.Manager;
import imagej.ManagerComponent;
import imagej.core.plugins.app.AboutImageJ;
import imagej.core.plugins.app.QuitProgram;
import imagej.core.plugins.app.ShowPrefs;
import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.platform.event.AppAboutEvent;
import imagej.platform.event.AppPreferencesEvent;
import imagej.platform.event.AppQuitEvent;
import imagej.plugin.PluginManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager component for executing plugins in response to application events.
 *
 * @author Curtis Rueden
 */
@Manager(priority = Manager.LAST_PRIORITY)
public final class AppEventManager implements ManagerComponent {

	/** Maintain list of subscribers, to avoid garbage collection. */
	private List<EventSubscriber<?>> subscribers;

	// -- ManagerComponent methods --

	@Override
	public void initialize() {
		subscribeToEvents();
	}

	// -- Helper methods --

	private void subscribeToEvents() {
		final PluginManager pluginManager = ImageJ.get(PluginManager.class);
		subscribers = new ArrayList<EventSubscriber<?>>();

		final EventSubscriber<AppAboutEvent> appAboutSubscriber =
			new EventSubscriber<AppAboutEvent>()
		{
			@Override
			public void onEvent(final AppAboutEvent event) {
				pluginManager.run(AboutImageJ.class);
			}
		};
		subscribers.add(appAboutSubscriber);
		Events.subscribe(AppAboutEvent.class, appAboutSubscriber);

		final EventSubscriber<AppPreferencesEvent> appPreferencesSubscriber =
			new EventSubscriber<AppPreferencesEvent>()
		{
			@Override
			public void onEvent(final AppPreferencesEvent event) {
				pluginManager.run(ShowPrefs.class);
			}
		};
		subscribers.add(appPreferencesSubscriber);
		Events.subscribe(AppPreferencesEvent.class, appPreferencesSubscriber);

		final EventSubscriber<AppQuitEvent> appQuitSubscriber =
			new EventSubscriber<AppQuitEvent>()
		{
			@Override
			public void onEvent(final AppQuitEvent event) {
				pluginManager.run(QuitProgram.class);
			}
		};
		subscribers.add(appQuitSubscriber);
		Events.subscribe(AppQuitEvent.class, appQuitSubscriber);
	}

}
