//
// Platform.java
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

import imagej.ext.plugin.IPlugin;
import imagej.ext.plugin.Plugin;

import java.io.IOException;
import java.net.URL;

/**
 * An interface for configuring a specific deployment platform, defined by
 * criteria such as operating system, machine architecture or Java version.
 * Platforms discoverable at runtime must implement this interface and be
 * annotated with <code>@{@link Plugin}(type = Platform.class)</code>.
 * 
 * @author Curtis Rueden
 * @see Plugin
 * @see PlatformService
 */
public interface Platform extends IPlugin {

	/** Java Runtime Environment vendor to match. */
	String javaVendor();

	/** Minimum required Java Runtime Environment version. */
	String javaVersion();

	/** Operating system architecture to match. */
	String osArch();

	/** Operating system name to match. */
	String osName();

	/** Minimum required operating system version. */
	String osVersion();

	void configure(PlatformService service);

	void open(URL url) throws IOException;

}
