//
// WindowsPlatform.java
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

package imagej.platform.windows;

import imagej.platform.IPlatform;
import imagej.platform.Platform;
import imagej.platform.PlatformService;

import java.io.IOException;
import java.net.URL;

/**
 * A platform implementation for handling Windows platform issues.
 * 
 * @author Johannes Schindelin
 */
@Platform(osName = "Windows")
public class WindowsPlatform implements IPlatform {

	// -- PlatformHandler methods --

	@Override
	public void configure(final PlatformService platformService) {}

	@Override
	public void open(final URL url) throws IOException {
		final String cmd;
		if (System.getProperty("os.name").startsWith("Windows 2000")) cmd =
			"rundll32 shell32.dll,ShellExec_RunDLL";
		else cmd = "rundll32 url.dll,FileProtocolHandler";
		if (!PlatformService.exec(cmd, url.toString())) throw new IOException(
			"Could not open " + url);
	}

}
