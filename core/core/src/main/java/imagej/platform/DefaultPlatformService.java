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

package imagej.platform;

import imagej.InstantiableException;
import imagej.command.CommandService;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.log.LogService;
import imagej.platform.event.AppMenusCreatedEvent;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.plugin.PluginInfo;
import imagej.plugin.PluginService;
import imagej.service.AbstractService;
import imagej.service.Service;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default service for handling platform-specific deployment issues.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public final class DefaultPlatformService extends AbstractService implements
	PlatformService
{

	@Parameter
	private LogService log;

	@Parameter
	private EventService eventService;

	@Parameter
	private PluginService pluginService;

	@Parameter
	private CommandService commandService;

	@Parameter
	private AppService appService;

	/** Platform handlers applicable to this platform. */
	private List<Platform> targetPlatforms;

	// -- PlatformService methods --

	@Override
	public EventService getEventService() {
		return eventService;
	}

	@Override
	public PluginService getPluginService() {
		return pluginService;
	}

	@Override
	public CommandService getCommandService() {
		return commandService;
	}

	@Override
	public AppService getAppService() {
		return appService;
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

	// -- Service methods --

	@Override
	public void initialize() {
		final List<Platform> platforms = discoverTargetPlatforms();
		targetPlatforms = Collections.unmodifiableList(platforms);
		for (final Platform platform : platforms) {
			log.info("Configuring platform: " + platform.getClass().getName());
			platform.configure(this);
		}
		if (platforms.size() == 0) log.info("No platforms to configure.");
		// ENABLE NEXT LINE and all plugins run twice
		// DISABLE NEXT LINE and default menu disappears
		subscribeToEvents(eventService);
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(final AppMenusCreatedEvent event) {
		if (registerAppMenus(event.getMenus())) event.consume();
	}

	// -- Helper methods --

	/** Discovers target platform handlers. */
	private List<Platform> discoverTargetPlatforms() {
		final List<Platform> platforms = new ArrayList<Platform>();
		final List<PluginInfo<Platform>> infos =
			pluginService.getPluginsOfType(Platform.class);
		for (final PluginInfo<? extends Platform> info : infos) {
			try {
				final Platform platform = info.createInstance();
				if (!isTargetPlatform(platform)) continue;
				platforms.add(platform);
			}
			catch (final InstantiableException e) {
				log.warn("Invalid platform: " + info, e);
			}
		}
		return platforms;
	}

	/**
	 * Determines whether the given platform description is applicable to this
	 * platform.
	 */
	private boolean isTargetPlatform(final Platform p) {
		if (p.javaVendor() != null) {
			final String javaVendor = System.getProperty("java.vendor");
			if (!javaVendor.matches(".*" + p.javaVendor() + ".*")) return false;
		}

		if (p.javaVersion() != null) {
			final String javaVersion = System.getProperty("java.version");
			if (javaVersion.compareTo(p.javaVersion()) < 0) return false;
		}

		if (p.osName() != null) {
			final String osName = System.getProperty("os.name");
			if (!osName.matches(".*" + p.osName() + ".*")) return false;
		}

		if (p.osArch() != null) {
			final String osArch = System.getProperty("os.arch");
			if (!osArch.matches(".*" + p.osArch() + ".*")) return false;
		}

		if (p.osVersion() != null) {
			final String osVersion = System.getProperty("os.version");
			if (osVersion.compareTo(p.osVersion()) < 0) return false;
		}

		return true;
	}

}
