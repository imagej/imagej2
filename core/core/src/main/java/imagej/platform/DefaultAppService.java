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

package imagej.platform;

import imagej.command.Command;
import imagej.command.CommandService;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.platform.event.AppAboutEvent;
import imagej.platform.event.AppPreferencesEvent;
import imagej.platform.event.AppQuitEvent;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.service.AbstractService;
import imagej.service.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Default service for providing application-level functionality.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public final class DefaultAppService extends AbstractService implements
	AppService
{

	@Parameter
	private EventService eventService;

	@Parameter
	private CommandService commandService;

	private Class<? extends Command> aboutCommand;
	private Class<? extends Command> prefsCommand;
	private Class<? extends Command> quitCommand;

	// -- AppService methods --

	@Override
	public void about() {
		if (aboutCommand == null) return;
		commandService.run(aboutCommand);
	}

	@Override
	public void showPrefs() {
		if (prefsCommand == null) return;
		commandService.run(prefsCommand);
	}

	@Override
	public void quit() {
		if (quitCommand == null) return;
		commandService.run(quitCommand);
	}

	@Override
	public void setAboutHandler(final Class<? extends Command> aboutCommand) {
		this.aboutCommand = aboutCommand;
	}

	@Override
	public void setPrefsHandler(final Class<? extends Command> prefsCommand) {
		this.prefsCommand = prefsCommand;
	}

	@Override
	public void setQuitHandler(final Class<? extends Command> quitCommand) {
		this.quitCommand = quitCommand;
	}

	@Override
	public List<Class<? extends Command>> getHandlers() {
		final ArrayList<Class<? extends Command>> handledCommands =
			new ArrayList<Class<? extends Command>>();
		if (aboutCommand != null) handledCommands.add(aboutCommand);
		if (prefsCommand != null) handledCommands.add(prefsCommand);
		if (quitCommand != null) handledCommands.add(quitCommand);
		return handledCommands;
	}

	// -- Event handlers --

	@EventHandler
	protected void
		onEvent(@SuppressWarnings("unused") final AppAboutEvent event)
	{
		about();
	}

	@EventHandler
	protected void onEvent(
		@SuppressWarnings("unused") final AppPreferencesEvent event)
	{
		showPrefs();
	}

	@EventHandler
	protected void onEvent(@SuppressWarnings("unused") final AppQuitEvent event)
	{
		quit();
	}

}
