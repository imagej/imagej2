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

import imagej.command.CommandService;
import imagej.platform.event.AppMenusCreatedEvent;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.plugin.AbstractSingletonService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;

/**
 * Default service for handling platform-specific deployment issues.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public final class DefaultPlatformService extends
	AbstractSingletonService<Platform> implements PlatformService
{

	@Parameter
	private LogService log;

	@Parameter
	private EventService eventService;

	@Parameter
	private CommandService commandService;

	@Parameter
	private AppEventService appEventService;

	/** Platform handlers applicable to this platform. */
	private List<Platform> targetPlatforms;

	// -- PlatformService methods --

	@Override
	public EventService getEventService() {
		return eventService;
	}

	@Override
	public CommandService getCommandService() {
		return commandService;
	}

	@Override
	public AppEventService getAppEventService() {
		return appEventService;
	}

	@Override
	public List<Platform> getTargetPlatforms() {
		return targetPlatforms;
	}

	@Override
	public void open(final URL url) throws IOException {
		IOException exception = null;
		for (final Platform platform : getTargetPlatforms()) {
			try {
				platform.open(url);
				return;
			}
			catch (final IOException e) {
				if (exception == null) exception = e;
			}
		}
		if (exception != null) throw exception;

		log.error("Could not find a browser; URL=" + url);
		throw new IOException("No browser found");
	}

	@Override
	public int exec(final String... args) throws IOException {
		final Process process = Runtime.getRuntime().exec(args);
		try {
			process.waitFor();
			return process.exitValue();
		}
		catch (final InterruptedException ie) {
			throw new IOException("InterruptedException during execution: " +
				ie.getMessage());
		}
	}

	@Override
	public boolean registerAppMenus(final Object menus) {
		for (final Platform platform : getTargetPlatforms()) {
			if (platform.registerAppMenus(menus)) return true;
		}
		return false;
	}

	@Override
	public List<? extends Platform> filterInstances(final List<Platform> list) {
		final Iterator<Platform> iter = list.iterator();
		while (iter.hasNext()) {
			if (!iter.next().isTarget()) {
				iter.remove();
			}
		}
		return list;
	}

	// -- PTService methods --

	@Override
	public Class<Platform> getPluginType() {
		return Platform.class;
	}

	// -- Service methods --

	@Override
	public void initialize() {
		super.initialize();

		// configure target platforms
		final List<Platform> platforms = getInstances();
		targetPlatforms = Collections.unmodifiableList(platforms);
		for (final Platform platform : platforms) {
			log.info("Configuring platform: " + platform.getClass().getName());
			platform.configure(this);
		}
		if (platforms.size() == 0) log.info("No platforms to configure.");
	}

	// -- Disposable methods --

	@Override
	public void dispose() {
		for (final Platform p : targetPlatforms) {
			p.dispose();
		}
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(final AppMenusCreatedEvent event) {
		if (registerAppMenus(event.getMenus())) event.consume();
	}

}
