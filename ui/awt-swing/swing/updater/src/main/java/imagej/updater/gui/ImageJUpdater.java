//
// ImageJUpdater.java
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


package imagej.updater.gui;

import imagej.event.EventService;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.updater.core.Checksummer;
import imagej.updater.core.PluginCollection;
import imagej.updater.core.PluginObject;
import imagej.updater.core.XMLFileDownloader;
import imagej.updater.gui.ViewOptions.Option;
import imagej.updater.util.Canceled;
import imagej.updater.util.Progress;
import imagej.updater.util.UserInterface;
import imagej.updater.util.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.Authenticator;
import java.net.UnknownHostException;

@Plugin(menu = {
	@Menu(label = "Help"),
	@Menu(label = "Update...") })
public class ImageJUpdater implements ImageJPlugin {

	@Parameter(required = true)
	private EventService eventService;

	@Override
	public void run() {

		UserInterface.set(new SwingUserInterface(eventService));

		if (errorIfDebian()) return;

		if (new File(Util.imagejRoot, "update").exists()) {
			UserInterface.get().error(
				"ImageJ restart required to finalize previous update");
			return;
		}
		Util.useSystemProxies();

		final PluginCollection plugins = new PluginCollection();
		try {
			plugins.read();
		}
		catch (final FileNotFoundException e) { /* ignore */}
		catch (final Exception e) {
			e.printStackTrace();
			UserInterface.get().error(
				"There was an error reading the cached metadata: " + e);
			return;
		}

		Authenticator.setDefault(new SwingAuthenticator());

		final UpdaterFrame main = new UpdaterFrame(plugins, false);
		main.setEasyMode(true);

		Progress progress = main.getProgress("Starting up...");
		final XMLFileDownloader downloader = new XMLFileDownloader(plugins);
		downloader.addProgress(progress);
		try {
			downloader.start();
		}
		catch (final Canceled e) {
			downloader.done();
			main.error("Canceled");
			return;
		}
		catch (final Exception e) {
			e.printStackTrace();
			downloader.done();
			String message;
			if (e instanceof UnknownHostException) message =
				"Failed to lookup host " + e.getMessage();
			else message = "Download/checksum failed: " + e;
			main.error(message);
			return;
		}

		final String warnings = downloader.getWarnings();
		if (!warnings.equals("")) main.warn(warnings);

		progress = main.getProgress("Matching with local files...");
		final Checksummer checksummer = new Checksummer(plugins, progress);
		try {
			checksummer.updateFromLocal();
		}
		catch (final Canceled e) {
			checksummer.done();
			main.error("Canceled");
			return;
		}

		// TODO: find .jar name from this class' resource
		// TODO: mark all dependencies for update
		// TODO: we may get away with a custom class loader... but probably not!
		final PluginObject updater = plugins.getPlugin("plugins/Fiji_Updater.jar");
		if ((updater != null && updater.getStatus() == PluginObject.Status.UPDATEABLE))
		{
			if (SwingTools.showQuestion(false, main, "Update the updater",
				"There is an update available for the Updater. Install now?"))
			{
				// download just the updater
				main.updateTheUpdater();

				main
					.info("Please restart ImageJ and call Help>Update to continue with the update");
			}
			// we do not save the plugins to prevent the mtime from changing
			return;
		}

		main.setLocationRelativeTo(null);
		main.setVisible(true);
		main.requestFocus();

		plugins.markForUpdate(false);
		main.setViewOption(Option.UPDATEABLE);
		if (plugins.hasForcableUpdates()) {
			main.warn("There are locally modified files!");
			if (plugins.hasUploadableSites() && !plugins.hasChanges()) {
				main.setViewOption(Option.LOCALLY_MODIFIED);
				main.setEasyMode(false);
			}
		}
		else if (!plugins.hasChanges()) main.info("Your ImageJ is up to date!");

		main.updatePluginsTable();
	}

	protected boolean overwriteWithUpdated(final PluginObject plugin) {
		File downloaded = new File(Util.prefix("update/" + plugin.filename));
		if (!downloaded.exists()) return true; // assume all is well if there is no
																						// updated file
		final File jar = new File(Util.prefix(plugin.filename));
		if (!jar.delete() && !moveOutOfTheWay(jar)) return false;
		if (!downloaded.renameTo(jar)) return false;
		for (;;) {
			downloaded = downloaded.getParentFile();
			if (downloaded == null) return true;
			final String[] list = downloaded.list();
			if (list != null && list.length > 0) return true;
			// dir is empty, remove
			if (!downloaded.delete()) return false;
		}
	}

	/**
	 * This returns true if this seems to be the Debian packaged version of ImageJ,
	 * or false otherwise.
	 */

	public static boolean isDebian() {
		final String debianProperty = System.getProperty("fiji.debian");
		return debianProperty != null && debianProperty.equals("true");
	}

	/**
	 * If this seems to be the Debian packaged version of ImageJ, then produce an
	 * error and return true. Otherwise return false.
	 */

	public static boolean errorIfDebian() {
		// If this is the Debian / Ubuntu packaged version, then
		// insist that the user uses apt-get / synaptic instead:
		if (isDebian()) {
			String message = "You are using the Debian packaged version of ImageJ.\n";
			message +=
				"You should update ImageJ with your system's usual package manager instead.";
			UserInterface.get().error(message);
			return true;
		}
		else return false;
	}

	protected static boolean moveOutOfTheWay(final File file) {
		if (!file.exists()) return true;
		File backup = new File(file.getParentFile(), file.getName() + ".old");
		if (backup.exists() && !backup.delete()) {
			final int i = 2;
			for (;;) {
				backup = new File(file.getParentFile(), file.getName() + ".old" + i);
				if (!backup.exists()) break;
			}
		}
		return file.renameTo(backup);
	}

}
