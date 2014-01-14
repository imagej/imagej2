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

package imagej.platform;

import imagej.command.Command;
import imagej.platform.event.AppAboutEvent;
import imagej.platform.event.AppPreferencesEvent;
import imagej.platform.event.AppQuitEvent;

import java.util.Collections;
import java.util.List;

import org.scijava.Priority;
import org.scijava.event.EventHandler;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * Default service for handling application-level events.
 * <p>
 * An {@link AppAboutEvent} triggers a callback to {@link #about()}. An
 * {@link AppPreferencesEvent} triggers a callback to {@link #prefs()}. Finally,
 * an {@link AppQuitEvent} triggers a callback to {@link #quit()}. Note that
 * this class's implementations of the former two methods do nothing, and the
 * latter simply disposes the application context with no user checks.
 * </p>
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Service.class, priority = Priority.LOW_PRIORITY)
public class DefaultAppEventService extends AbstractService implements
	AppEventService
{

	// -- AppService methods --

	@Override
	public void about() {
		// NB: Do nothing.
	}

	@Override
	public void prefs() {
		// NB: Do nothing.
	}

	@Override
	public void quit() {
		getContext().dispose();
	}

	@Override
	public List<Class<? extends Command>> getCommands() {
		return Collections.emptyList();
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(@SuppressWarnings("unused") final AppAboutEvent event)
	{
		about();
	}

	@EventHandler
	protected void onEvent(
		@SuppressWarnings("unused") final AppPreferencesEvent event)
	{
		prefs();
	}

	@EventHandler
	protected void onEvent(@SuppressWarnings("unused") final AppQuitEvent event) {
		quit();
	}

}
