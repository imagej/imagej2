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

import imagej.event.EventService;
import imagej.ext.plugin.PluginService;
import imagej.service.IService;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Interface for service that handles platform-specific deployment issues.
 * 
 * @author Curtis Rueden
 */
public interface PlatformService extends IService {

	EventService getEventService();

	PluginService getPluginService();

	AppEventService getAppEventService();

	/** Gets the platform handlers applicable to this platform. */
	List<Platform> getTargetPlatforms();

	/**
	 * Returns true if the menu bar should be duplicated for every window frame.
	 * If false, the menu bar should be present on the main ImageJ application
	 * frame only.
	 */
	boolean isMenuBarDuplicated();

	/** Sets whether the menu bar should be duplicated for every window frame. */
	void setMenuBarDuplicated(final boolean menuBarDuplicated);

	/**
	 * Opens a URL in a platform-dependent way. Typically the URL is opened
	 * external web browser instance, but the behavior is ultimately defined the
	 * available platform handler implementations.
	 */
	void open(final URL url) throws IOException;

	/** Executes a native program and waits for it to return. */
	boolean exec(final String... args) throws IOException;

}
