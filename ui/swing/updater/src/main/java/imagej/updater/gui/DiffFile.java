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

package imagej.updater.gui;

import imagej.log.LogService;
import imagej.updater.core.Diff;
import imagej.updater.core.Diff.Mode;
import imagej.updater.core.FileObject;
import imagej.updater.core.FilesCollection;
import imagej.updater.util.ByteCodeAnalyzer;
import imagej.updater.util.Util;
import imagej.util.ProcessUtils;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import javax.swing.JFrame;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * A {@link JFrame} to show the differences between the remote and local
 * versions of a file known to the ImageJ Updater.
 * 
 * @author Johannes Schindelin
 */
public class DiffFile extends JFrame {
	private static final long serialVersionUID = 1L;
	protected String title;
	protected LogService log;
	protected String filename;
	protected URL remote, local;
	protected DiffView diffView;
	protected Cursor normalCursor, waitCursor;
	protected Diff diff;
	protected int diffOffset;
	protected Thread worker;

	/**
	 * Initialize the frame.
	 * 
	 * @param files
	 *            the collection of files, including information about the
	 *            update site from which we got the file
	 * @param file
	 *            the file to diff
	 * @param mode
	 *            the diff mode
	 * @throws MalformedURLException
	 */
	public DiffFile(final FilesCollection files, final FileObject file, final Mode mode) throws MalformedURLException {
		title = file.getLocalFilename(true) + " differences";
		log = files.log;
		filename = file.getLocalFilename(false);
		remote = new URL(files.getURL(file));
		local = files.prefix(filename).toURI().toURL();

		diffView = new DiffView();
		normalCursor = diffView.getCursor();
		waitCursor = new Cursor(Cursor.WAIT_CURSOR);
		addModeLinks();
		addGitLogLink(files, file);
		diffOffset = diffView.getDocument().getLength();
		diff = new Diff(diffView.getPrintStream());
		show(mode);

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		getContentPane().add(diffView);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (worker != null)
					worker.interrupt();
			}
		});
		pack();
	}

	/**
	 * Switch to a different diff mode.
	 * 
	 * @param mode
	 *            the mode to diff to
	 */
	protected void show(final Mode mode) {
		show(new Runnable() {
			@Override
			public void run() {
				try {
					setTitle(title + " (" + mode + ")");
					diff.showDiff(filename, remote, local, mode);
				} catch (MalformedURLException e) {
					log.error(e);
				} catch (IOException e) {
					log.error(e);
				}
			}
		});
	}

	/**
	 * Show a different diff.
	 * 
	 * @param runnable
	 *            the object printing to the {@link DiffView}
	 */
	protected synchronized void show(final Runnable runnable) {
		if (worker != null)
			worker.interrupt();
		else
			diffView.setCursor(waitCursor);
		worker = new Thread() {
			@Override
			public void run() {
				try {
					clearDiff();
					runnable.run();
				} catch (RuntimeException e) {
					if (!(e.getCause() instanceof InterruptedException))
						log.error(e);
					worker.interrupt();
				} catch (Error e) {
					log.error(e);
					worker.interrupt();
				}
				diffView.setCursor(normalCursor);
				synchronized(DiffFile.this) {
					worker = null;
				}
			}
		};
		worker.start();
	}

	/**
	 * Remove the previous diff output from the {@link DiffView}.
	 */
	protected void clearDiff() {
		final Document doc = diffView.getDocument();
		try {
			doc.remove(diffOffset, doc.getLength() - diffOffset);
		} catch (BadLocationException e) {
			log.error(e);
		}
	}

	/**
	 * Add the action links for the available diff modes.
	 */
	private void addModeLinks() {
		for (final Mode mode : Mode.values()) {
			if (diffView.getDocument().getLength() > 0)
				diffView.normal(" ");
			diffView.link(mode.toString(), new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					show(mode);
				}
			});
		}
	}

	/**
	 * Add an action link to show the Git log of the file.
	 * 
	 * This method only adds the link if it can determine where the source for
	 * this component lives.
	 * 
	 * @param files
	 *            the file collection including information where the file lives
	 * @param fileObject
	 *            the component to inspect
	 */
	private void addGitLogLink(final FilesCollection files, final FileObject fileObject) {
		// first, we need to find Implementation-Build entries in the respective manifests
		String commitLocal = getCommit(local);
		if (commitLocal == null || "".equals(commitLocal)) commitLocal = "HEAD";
		String commitRemote = getCommit(remote);

		if (commitLocal.equals(commitRemote)) {
			diffView.warn("The remote and local versions were built from the same commit!");
			return;
		}

		// now, let's find the .git/ directory.
		File directory = files.prefix(".");
		while (!new File(directory, ".git").exists()) {
			directory = directory.getParentFile();
			if (directory == null) return;
		}
		final String baseName = fileObject.filename.substring(fileObject.filename.lastIndexOf('/') + 1);
		for (String pair : new String[] { "ij-[1-9].* ImageJA", "ij-[a-z].* imagej2", "imglib.* imglib", "TrakEM2.* TrakEM2", "mpicbg.* mpicbg" }) {
			final int space = pair.indexOf(' ');
			final String pattern = pair.substring(0, space);
			if (baseName.matches(pattern)) {
				final File submodule = new File(directory, "modules/" + pair.substring(space + 1));
				if (new File(submodule, ".git").isDirectory()) {
					directory = submodule;
					break;
				}
			}
		}
		final File gitWorkingDirectory = directory;

		// now, let's find the directory where the first source of the local .jar is stored
		final String relativePath = findSourceDirectory(gitWorkingDirectory, local);
		if (relativePath == null) return;

		final String commitRange, since, warning;
		if (commitRemote != null && !"".equals(commitRemote)) {
			commitRange = commitRemote + ".." + commitLocal;
			since = "-p";
			warning = null;
		}
		else {
			commitRange = commitLocal;
			long millis = Util.timestamp2millis(fileObject.current.timestamp);
			since = "--since=" + (millis / 1000l - 5 * 60);
			warning = "No precise commit information in the remote .jar;\n"
					+ "\tUsing timestamp from Updater instead: " + new Date(millis) + " - 5 minutes";
		}

		if (diffView.getDocument().getLength() > 0)
			diffView.normal(" ");
		diffView.link("Git Log", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				show(new Runnable() {
					@Override
					public void run() {
						setTitle(title + " (Git Log)");
						final PrintStream out = diffView.getPrintStream();
						out.println("\n");
						if (warning != null) diffView.warn(warning + "\n\n");
						ProcessUtils.exec(gitWorkingDirectory,  out, out, "git", "log", "-M", "-p", since, commitRange, "--", relativePath);
					}
				});
			}
		});
	}

	/**
	 * Given a {@link URL} to a <i>.jar</i> file, extract the Implementation-Build entry from the manifest.
	 * 
	 * @param jarURL the URL to the <i>.jar</i> file
	 */
	private static String getCommit(final URL jarURL) {
		try {
			final JarInputStream in = new JarInputStream(jarURL.openStream());
			Manifest manifest = in.getManifest();
			if (manifest == null)
				for (;;) {
					final JarEntry entry = in.getNextJarEntry();
					if (entry == null) return null;
					if (entry.getName().equals("META-INF/MANIFEST.MF")) {
						manifest = new Manifest(in);
						break;
					}
				}
			final Attributes attributes = manifest.getMainAttributes(); 
			return attributes.getValue(new Attributes.Name("Implementation-Build"));
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Given a {@link URL} to a <i>.jar</i> file, extract the path of the first <i>.class</i> file contained therein.
	 * 
	 * @param jarURL the URL to the <i>.jar</i> file
	 * @return the path stored in the <i>.jar</i> file
	 */
	private static String findSourceDirectory(final File gitWorkingDirectory, final URL jarURL) {
		try {
			int maxCount = 3;
			final JarInputStream in = new JarInputStream(jarURL.openStream());
			for (;;) {
				final JarEntry entry = in.getNextJarEntry();
				if (entry == null) break;
				String path = entry.getName();
				if (!path.endsWith(".class")) continue;
				if (--maxCount <= 0) break;
				final ByteCodeAnalyzer analyzer = Diff.analyzeByteCode(in, false);
				final String sourceFile = analyzer.getSourceFile();
				if (sourceFile == null) continue;
				final String suffix = path.substring(0, path.lastIndexOf('/') + 1) + sourceFile;
				try {
					path = ProcessUtils.exec(gitWorkingDirectory, null, null, "git", "ls-files", "*/" + suffix);
					if (path.length() <= suffix.length()) continue;
					if (path.endsWith("\n")) path = path.substring(0, path.length() - 1);
				} catch (RuntimeException e) {
					/* ignore */
					continue;
				}
				if (path.indexOf('\n') >= 0) continue; // ls-files found multiple files
				path = path.substring(0, path.length() - suffix.length());
				if ("".equals(path)) path = ".";
				else if (path.endsWith("/src/main/java/")) path = path.substring(0, path.length() - "/src/main/java/".length());
				return path;
			}
		} catch (IOException e) { /* ignore */ e.printStackTrace(); }
		return null;
	}
}
