//
// PlatformService.java
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

package imagej.platform;

import imagej.ImageJ;
import imagej.event.EventService;
import imagej.service.AbstractService;
import imagej.service.Service;
import imagej.util.Log;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

/**
 * Service for platform-specific deployment issues.
 * 
 * @author Curtis Rueden
 */
@Service
public final class PlatformService extends AbstractService {

	private final EventService eventService;

	/** Platform handlers applicable to this platform. */
	private List<IPlatform> targetPlatforms;

	/** Whether the menu bar should be duplicated for every window frame. */
	private boolean menuBarDuplicated;

	// -- Constructors --

	public PlatformService() {
		// NB: Required by SezPoz.
		super(null);
		throw new UnsupportedOperationException();
	}

	public PlatformService(final ImageJ context, final EventService eventService)
	{
		super(context);
		this.eventService = eventService;

		final List<IPlatform> platforms = discoverTargetPlatforms();
		targetPlatforms = Collections.unmodifiableList(platforms);
		for (final IPlatform platform : platforms) {
			Log.info("Configuring platform: " + platform.getClass().getName());
			platform.configure(this);
		}
		if (platforms.size() == 0) Log.info("No platforms to configure.");
	}

	// -- PlatformService methods --

	public EventService getEventService() {
		return eventService;
	}

	/** Gets the platform handlers applicable to this platform. */
	public List<IPlatform> getTargetPlatforms() {
		return targetPlatforms;
	}

	/**
	 * Returns true if the menu bar should be duplicated for every window frame.
	 * If false, the menu bar should be present on the main ImageJ application
	 * frame only.
	 */
	public boolean isMenuBarDuplicated() {
		return menuBarDuplicated;
	}

	/** Sets whether the menu bar should be duplicated for every window frame. */
	public void setMenuBarDuplicated(final boolean menuBarDuplicated) {
		this.menuBarDuplicated = menuBarDuplicated;
	}

	/**
	 * Open a URL in platform-dependent way
	 */
	public void open(final URL url) throws IOException {
		IOException exception = null;
		for (final IPlatform platform : getTargetPlatforms())
			try {
				platform.open(url);
				return;
			}
			catch (final IOException e) {
				if (exception == null) exception = e;
			}
		if (exception != null) throw exception;

		/*
		 * Fall back to calling known browsers.
		 *
		 * Based on BareBonesBrowserLaunch (http://www.centerkey.com/java/browser/).
		 *
		 * The utility 'xdg-open' launches the URL in the user's preferred
		 * browser, therefore we try to use it first, before trying to discover
		 * other browsers.
		 */
		final String[] browsers =
			{ "xdg-open", "netscape", "firefox", "konqueror", "mozilla", "opera",
				"epiphany", "lynx" };
		for (final String browser : browsers) {
			if (exec(browser, url.toString())) return;
		}
		Log.error("Could not find a browser; URL=" + url);
		throw new IOException("No browser found");
	}

	/**
	 * Execute a native program and wait for it to return
	 */
	public static boolean exec(final String... args) throws IOException {
		final Process process = Runtime.getRuntime().exec(args);
		try {
			process.waitFor();
			return process.exitValue() == 0;
		}
		catch (final InterruptedException ie) {
			throw new IOException("InterruptedException while launching browser: " +
				ie.getMessage());
		}
	}

	// -- Helper methods --

	/** Discovers target platform handlers using SezPoz. */
	private List<IPlatform> discoverTargetPlatforms() {
		final List<IPlatform> platforms = new ArrayList<IPlatform>();
		for (final IndexItem<Platform, IPlatform> item : Index.load(
			Platform.class, IPlatform.class))
		{
			if (!isTargetPlatform(item.annotation())) continue;
			try {
				platforms.add(item.instance());
			}
			catch (final InstantiationException e) {
				Log.warn("Invalid platform: " + item, e);
			}
		}
		return platforms;
	}

	/**
	 * Determines whether the given platform description is applicable to this
	 * platform.
	 */
	private boolean isTargetPlatform(final Platform p) {
		final String javaVendor = System.getProperty("java.vendor");
		if (!javaVendor.matches(".*" + p.javaVendor() + ".*")) return false;

		final String javaVersion = System.getProperty("java.version");
		if (javaVersion.compareTo(p.javaVersion()) < 0) return false;

		final String osName = System.getProperty("os.name");
		if (!osName.matches(".*" + p.osName() + ".*")) return false;

		final String osArch = System.getProperty("os.arch");
		if (!osArch.matches(".*" + p.osArch() + ".*")) return false;

		final String osVersion = System.getProperty("os.version");
		if (osVersion.compareTo(p.osVersion()) < 0) return false;

		return true;
	}

}
