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

package imagej.core.platforms.macosx;

import imagej.command.Command;
import imagej.command.CommandInfo;
import imagej.command.CommandService;
import imagej.display.event.window.WinActivatedEvent;
import imagej.module.ModuleInfo;
import imagej.module.event.ModulesUpdatedEvent;
import imagej.platform.AbstractPlatform;
import imagej.platform.AppEventService;
import imagej.platform.Platform;
import imagej.platform.PlatformService;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.event.EventSubscriber;
import org.scijava.plugin.Plugin;

/**
 * A platform implementation for handling Mac OS X platform issues:
 * <ul>
 * <li>Application events are rebroadcast as ImageJ events.</li>
 * <li>Mac OS X screen menu bar is enabled.</li>
 * <li>Special screen menu bar menu items are handled.</li>
 * </ul>
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Platform.class, name = "Mac OS X")
public class MacOSXPlatform extends AbstractPlatform {

	/** Debugging flag to allow easy toggling of Mac screen menu bar behavior. */
	private static final boolean SCREEN_MENU = true;

	@SuppressWarnings("unused")
	private MacOSXAppEventDispatcher appEventDispatcher;

	private JMenuBar menuBar;

	private List<EventSubscriber<?>> subscribers;

	// -- Platform methods --

	@Override
	public String osName() {
		return "Mac OS X";
	}

	@Override
	public void configure(final PlatformService service) {
		super.configure(service);

		// use Mac OS X screen menu bar
		if (SCREEN_MENU) System.setProperty("apple.laf.useScreenMenuBar", "true");

		// remove app commands from menu structure
		if (SCREEN_MENU) removeAppCommandsFromMenu();

		// translate Mac OS X application events into ImageJ events
		final EventService eventService = getPlatformService().getEventService();
		try {
			appEventDispatcher = new MacOSXAppEventDispatcher(eventService);
		}
		catch (final NoClassDefFoundError e) {
			// the interfaces implemented by MacOSXAppEventDispatcher might not be
			// available:
			// - on MacOSX Tiger without recent Java Updates
			// - on earlier MacOSX versions
		}

		// subscribe to relevant window-related events
		subscribers = eventService.subscribe(this);
	}

	@Override
	public void open(final URL url) throws IOException {
		if (getPlatformService().exec("open", url.toString()) != 0) {
			throw new IOException("Could not open " + url);
		}
	}

	@Override
	public boolean registerAppMenus(final Object menus) {
		if (SCREEN_MENU && menus instanceof JMenuBar) {
			menuBar = (JMenuBar) menus;
		}
		return false;
	}

	// -- Disposable methods --

	@Override
	public void dispose() {
		getPlatformService().getEventService().unsubscribe(subscribers);
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(final WinActivatedEvent evt) {
		if (!SCREEN_MENU) return;

		final Object window = evt.getWindow();
		if (!(window instanceof JFrame)) return;

		// assign the singleton menu bar to newly activated window
		((JFrame) window).setJMenuBar(menuBar);
	}

	// -- Helper methods --

	private void removeAppCommandsFromMenu() {
		final PlatformService platformService = getPlatformService();
		final EventService eventService = platformService.getEventService();
		final CommandService commandService = platformService.getCommandService();
		final AppEventService appEventService =
			platformService.getAppEventService();

		// get the list of commands being handled at the application level
		final List<Class<? extends Command>> commands =
			appEventService.getCommands();

		// remove said commands from the main menu bar
		// (the Mac application menu will trigger them instead)
		final ArrayList<ModuleInfo> infos = new ArrayList<ModuleInfo>();
		for (final Class<? extends Command> command : commands) {
			final CommandInfo info = commandService.getCommand(command);
			info.setMenuPath(null);
			infos.add(info);
		}
		eventService.publish(new ModulesUpdatedEvent(infos));
	}

}
