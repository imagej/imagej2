/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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
 * #L%
 */

package imagej.updater.gui;

import imagej.updater.core.Conflicts.Conflict;
import imagej.updater.core.FileObject;
import imagej.updater.core.FilesCollection;
import imagej.updater.core.Installer;
import imagej.updater.core.UpdaterUI;
import imagej.updater.core.UploaderService;
import imagej.updater.gui.ViewOptions.Option;
import imagej.updater.util.AvailableSites;
import imagej.updater.util.Progress;
import imagej.updater.util.UpdateCanceledException;
import imagej.updater.util.UpdaterUserInterface;
import imagej.updater.util.Util;

import java.io.File;
import java.io.IOException;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import net.imagej.util.AppUtils;

import org.scijava.app.StatusService;
import org.scijava.command.CommandService;
import org.scijava.log.LogService;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * The Updater. As a command.
 * <p>
 * Incidentally, this class can be used as an out-of-ImageJ entry point to the
 * updater, as it does not *require* a StatusService to run.
 * 
 * @author Johannes Schindelin
 */
@Plugin(type = UpdaterUI.class, menu = { @Menu(label = "Help"),
	@Menu(label = "Update...") })
public class ImageJUpdater implements UpdaterUI {
	private UpdaterFrame main;

	@Parameter
	private StatusService statusService;

	@Parameter
	private LogService log;

	@Parameter
	private UploaderService uploaderService;

	@Parameter
	private CommandService commandService;

	@Override
	public void run() {

		if (errorIfDebian()) return;

		if (log == null) {
			log = Util.getLogService();
		}

		final File imagejRoot = AppUtils.getBaseDirectory();
		final FilesCollection files = new FilesCollection(imagejRoot);
		AvailableSites.initializeAndAddSites(files);

		UpdaterUserInterface.set(new SwingUserInterface(log, statusService));

		if (new File(imagejRoot, "update").exists()) {
			if (!UpdaterUserInterface.get().promptYesNo("It is suggested that you restart ImageJ, then continue the update.\n"
					+ "Alternately, you can attempt to continue the upgrade without\n"
					+ "restarting, but ImageJ might crash.\n\n"
					+ "Do you want to try it?",
					"Restart required to finalize update"))
				return;
			try {
				new Installer(files, null).moveUpdatedIntoPlace();
			} catch (IOException e) {
				log.debug(e);
				UpdaterUserInterface.get().error("Could not move files into place: " + e);
				return;
			}
		}
		Util.useSystemProxies();
		Authenticator.setDefault(new SwingAuthenticator());

		SwingTools.invokeOnEDT(new Runnable() {
			@Override
			public void run() {
				main = new UpdaterFrame(log, uploaderService, files);
			}
		});

		main.setEasyMode(true);
		Progress progress = main.getProgress("Starting up...");

		try {
			String warnings = files.downloadIndexAndChecksum(progress);
			main.checkWritable();
			main.addCustomViewOptions();
			if (!warnings.equals("")) main.warn(warnings);
			final List<Conflict> conflicts = files.getConflicts();
			if (conflicts != null && conflicts.size() > 0 &&
					!new ConflictDialog(main, "Conflicting versions") {
						private static final long serialVersionUID = 1L;

						@Override
						protected void updateConflictList() {
							conflictList = conflicts;
						}
					}.resolve())
				return;
		}
		catch (final UpdateCanceledException e) {
			main.error("Canceled");
			return;
		}
		catch (final Exception e) {
			log.error(e);
			String message;
			if (e instanceof UnknownHostException) message =
				"Failed to lookup host " + e.getMessage();
			else message = "There was an error reading the cached metadata: " + e;
			main.error(message);
			return;
		}

		if (Installer.isTheUpdaterUpdateable(files, commandService)) {
			try {
				// download just the updater
				Installer.updateTheUpdater(files, main.getProgress("Installing the updater..."), commandService);
			}
			catch (final UpdateCanceledException e) {
				main.error("Canceled");
				return;
			}
			catch (final IOException e) {
				main.error("Installer failed: " + e);
				return;
			}

			// make a class path using the updated files
			final List<URL> classPath = new ArrayList<URL>();
			for (FileObject component : Installer.getUpdaterFiles(files, commandService, false)) {
				final String name = component.getLocalFilename(false);
				File file = files.prefix(name);
				try {
					classPath.add(file.toURI().toURL());
				} catch (MalformedURLException e) {
					log.error(e);
				}
			}
			try {
				log.info("Trying to install and execute the new updater");
				new Installer(files, null).moveUpdatedIntoPlace();
				final URL[] urls = classPath.toArray(new URL[classPath.size()]);
				URLClassLoader remoteClassLoader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
				System.setProperty("imagej.update.updater", "true");
				Class<?> runnable = remoteClassLoader.loadClass(ImageJUpdater.class.getName());
				new Thread((Runnable)runnable.newInstance()).start();
				return;
			} catch (Throwable t) {
				log.error(t);
			}

			main.info("Please restart ImageJ and call Help>Update to continue with the update");
			return;
		}

		final String missingUploaders = main.files.protocolsMissingUploaders(main.getUploaderService(), main.getProgress(null));
		if (missingUploaders != null) {
			main.warn(missingUploaders);
		}

		main.setLocationRelativeTo(null);
		main.setVisible(true);
		main.requestFocus();

		files.markForUpdate(false);
		main.setViewOption(Option.UPDATEABLE);
		if (files.hasForcableUpdates()) {
			main.warn("There are locally modified files!");
			if (files.hasUploadableSites() && !files.hasChanges()) {
				main.setViewOption(Option.LOCALLY_MODIFIED);
				main.setEasyMode(false);
			}
		}
		else if (!files.hasChanges()) main.info("Your ImageJ is up to date!");

		main.updateFilesTable();
	}

	protected boolean overwriteWithUpdated(final FilesCollection files,
		final FileObject file)
	{
		File downloaded = files.prefix("update/" + file.filename);
		if (!downloaded.exists()) return true; // assume all is well if there is no updated file
		final File jar = files.prefix(file.filename);
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
	 * This returns true if this seems to be the Debian packaged version of
	 * ImageJ, or false otherwise.
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
			UpdaterUserInterface.get().error(message);
			return true;
		}
		return false;
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

	public static void main(String[] args) {
		new ImageJUpdater().run();
	}
}
