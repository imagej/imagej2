//
// BrowserLauncher.java
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

package imagej.script.editor;

import imagej.util.Log;

import java.io.IOException;
import java.net.URL;

/**
 * A helper class to open a URL in a browser TODO: this class wants to find a
 * better home than ij-ui-script-editor
 * 
 * @author Johannes Schindelin
 */
public class BrowserLauncher {

	public static boolean open(final URL url) throws IOException {
		return open(url.toString());
	}

	public static boolean open(final String url) throws IOException {
		final String osName = System.getProperty("os.name");
		if (osName.startsWith("Mac")) {
			return exec("open", url);
		}
		else if (osName.startsWith("Win")) {
			final String cmd;
			if (System.getProperty("os.name").startsWith("Windows 2000")) cmd =
				"rundll32 shell32.dll,ShellExec_RunDLL";
			else cmd = "rundll32 url.dll,FileProtocolHandler";
			return exec(cmd, url);
		}
		else {
			// Assume Linux or Unix
			// Based on BareBonesBrowserLaunch
			// (http://www.centerkey.com/java/browser/)
			// The utility 'xdg-open' launches the URL in the user's preferred
			// browser,
			// therefore we try to use it first, before trying to discover other
			// browsers.
			final String[] browsers =
				{ "xdg-open", "netscape", "firefox", "konqueror", "mozilla", "opera",
					"epiphany", "lynx" };
			for (final String browser : browsers) {
				if (exec(browser, url)) return true;
			}
			Log.error("Could not find a browser; URL=" + url);
			throw new IOException("No browser found");
		}
	}

	protected static boolean exec(final String... args) throws IOException {
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
}
