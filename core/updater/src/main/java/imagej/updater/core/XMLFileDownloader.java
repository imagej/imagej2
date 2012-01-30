
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
