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

import imagej.ext.Priority;
import imagej.ext.plugin.Plugin;

import java.io.IOException;
import java.net.URL;

/**
 * A platform implementation for default handling of platform issues.
 * 
 * @author Curtis Rueden
 * @author Johannes Schindelin
 */
@Plugin(type = Platform.class, priority = Priority.VERY_LOW_PRIORITY)
public class DefaultPlatform extends AbstractPlatform {

	// -- PlatformHandler methods --

	/**
	 * Falls back to calling known browsers.
	 * <p>
	 * Based on <a
	 * href="http://www.centerkey.com/java/browser/">BareBonesBrowserLaunch</a>.
	 * </p>
	 * <p>
	 * The utility 'xdg-open' launches the URL in the user's preferred browser,
	 * therefore we try to use it first, before trying to discover other browsers.
	 * </p>
	 */
	@Override
	public void open(final URL url) throws IOException {
		if (!platformService.exec("open", url.toString())) {
			throw new IOException("Could not open " + url);
		}
		final String[] browsers =
			{ "xdg-open", "netscape", "firefox", "konqueror", "mozilla", "opera",
				"epiphany", "lynx" };
		for (final String browser : browsers) {
			if (platformService.exec(browser, url.toString())) return;
		}
	}

}
