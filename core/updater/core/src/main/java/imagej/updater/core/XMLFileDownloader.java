//
// XMLFileDownloader.java
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

package imagej.updater.core;

import imagej.updater.core.PluginCollection.UpdateSite;
import imagej.updater.util.Progressable;
import imagej.updater.util.Util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.zip.GZIPInputStream;

/*
 * Directly in charge of downloading and saving start-up files (i.e.: XML file
 * and related).
 */
public class XMLFileDownloader extends Progressable {

	protected PluginCollection plugins;
	protected Collection<String> updateSites;
	protected String warnings;

	public XMLFileDownloader(final PluginCollection plugins) {
		this(plugins, plugins.getUpdateSiteNames());
	}

	public XMLFileDownloader(final PluginCollection plugins,
		final Collection<String> updateSites)
	{
		this.plugins = plugins;
		this.updateSites = updateSites;
	}

	public void start() throws IOException {
		setTitle("Updating the index of available files");
		final XMLFileReader reader = new XMLFileReader(plugins);
		final int current = 0, total = updateSites.size();
		warnings = "";
		for (final String name : updateSites) {
			final UpdateSite updateSite = plugins.getUpdateSite(name);
			final String title =
				"Updating from " + (name.equals("") ? "main" : name) + " site";
			addItem(title);
			setCount(current, total);
			try {
				final URLConnection connection =
					new URL(updateSite.url + Util.XML_COMPRESSED).openConnection();
				final long lastModified = connection.getLastModified();
				final int fileSize = connection.getContentLength();
				final InputStream in =
					getInputStream(new GZIPInputStream(connection.getInputStream()),
						fileSize);
				reader.read(name, in, updateSite.timestamp);
				updateSite.setLastModified(lastModified);
			}
			catch (final Exception e) {
				if (e instanceof FileNotFoundException) updateSite.setLastModified(0); // it
																																								// was
																																								// deleted
				e.printStackTrace();
				warnings += "Could not update from site '" + name + "': " + e;
			}
			itemDone(title);
		}
		done();
		warnings += reader.getWarnings();
	}

	public String getWarnings() {
		return warnings;
	}

	public InputStream getInputStream(final InputStream in, final int fileSize) {
		return new InputStream() {

			int current = 0;

			@Override
			public int read() throws IOException {
				final int result = in.read();
				setItemCount(++current, fileSize);
				return result;
			}

			@Override
			public int read(final byte[] b) throws IOException {
				final int result = in.read(b);
				if (result > 0) {
					current += result;
					setItemCount(current, fileSize);
				}
				return result;
			}

			@Override
			public int read(final byte[] b, final int off, final int len)
				throws IOException
			{
				final int result = in.read(b, off, len);
				if (result > 0) {
					current += result;
					setItemCount(current, fileSize);
				}
				return result;
			}
		};
	}
}
