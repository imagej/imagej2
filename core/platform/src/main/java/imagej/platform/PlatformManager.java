//
// PlatformManager.java
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

import imagej.Log;
import imagej.manager.Manager;
import imagej.manager.ManagerComponent;

import java.util.ArrayList;
import java.util.List;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

/**
 * Manager component for platform-specific deployment issues.
 *
 * @author Curtis Rueden
 */
@Manager(priority = PlatformManager.PRIORITY)
public final class PlatformManager implements ManagerComponent {

	public static final int PRIORITY = 0;

	// -- ManagerComponent methods --

	@Override
	public void initialize() {
		final List<PlatformHandler> platforms = getTargetPlatforms();
		for (final PlatformHandler platform : platforms) {
			Log.debug("Configuring platform: " + platform);
			platform.configure();
		}
    if (platforms.size() == 0) Log.debug("No platforms to configure.");
	}

	// -- Helper methods --

	private List<PlatformHandler> getTargetPlatforms() {
		// use SezPoz to discover all platform handlers
		final List<PlatformHandler> platforms = new ArrayList<PlatformHandler>();
		for (final IndexItem<Platform, PlatformHandler> item :
			Index.load(Platform.class, PlatformHandler.class))
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
