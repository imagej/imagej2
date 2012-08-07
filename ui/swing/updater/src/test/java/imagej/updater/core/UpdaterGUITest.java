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

package imagej.updater.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.GZIPOutputStream;

import imagej.updater.gui.ImageJUpdater;
import imagej.updater.gui.ProgressDialog;
import imagej.updater.gui.SwingUserInterface;
import imagej.updater.util.Progress;
import imagej.updater.util.StderrLogService;
import imagej.updater.util.StderrProgress;
import imagej.util.MersenneTwisterFast;

/**
 * This class is meant for interactive debugging of GUI issues in the Updater.
 * 
 * Issues such as problems with the password dialog are often tested much more
 * efficiently when run outside of the Updater, without having to start ImageJ
 * over and over again. This class helps with such issues.
 * 
 * It is not meant to be run unattendedly, ie. with JUnit.
 * 
 * @author Johannes Schindelin
 */
public class UpdaterGUITest {
	public static void main(String[] args) throws Exception {
		//testProgressDialog();
		testStringDialog();
		//testPassword();
		testUpdateTheUpdater();
	}

	protected static void testProgressDialog() {
		int count = 35;
		int minSize = 8192;
		int maxSize = 65536;
		int minChunk = 256;
		int maxChunk = 16384;

		MersenneTwisterFast random = new MersenneTwisterFast();

		ProgressDialog progress = new ProgressDialog(null);

		progress.setTitle("Hello");

		int totalSize = 0;
		int totalCurrent = 0;
		int[] sizes = new int[count];
		for (int i = 0; i < count; i++) {
			sizes[i] = minSize + random.nextInt(maxSize - minSize);
			totalSize += sizes[i];
		}

		for (int i = 0; i < count; i++) {
			int current = 0;
			String item = "Item " + i + "/" + sizes[i];
			progress.addItem(item);
			while (current < sizes[i]) {
				int byteCount = minChunk + random.nextInt(maxChunk - minChunk);
				current += byteCount;
				progress.setItemCount(current, sizes[i]);
				totalCurrent += byteCount;
				progress.setCount(totalCurrent, totalSize);
				int millis = random.nextInt(500);
				if (millis > 0) try {
					Thread.sleep(millis);
				} catch (InterruptedException e) {
					// we've been asked to stop
					progress.done();
					return;
				}
			}
			progress.itemDone(item);
		}
		progress.done();
	}

	protected static void testStringDialog() {
		SwingUserInterface ui = new SwingUserInterface(new StderrLogService(), null);
		System.err.println(ui.getString("Login for blub"));
	}

	protected static void testPassword() {
		SwingUserInterface ui = new SwingUserInterface(new StderrLogService(), null);
		System.err.println(ui.getPassword("Enter password"));
	}

	/**
	 * Test the "update-the-updater" functionality manually
	 * 
	 * This method sets up a minimal ImageJ directory, an update site.
	 * Then it sets up a second minimal ImageJ directory, modifies the
	 * updater so that the first update site will want to update.
	 * 
	 * To be able to verify that it actually works, we remove the pom.xml
	 * -- from which the Updater can infer the metadata like description,
	 * author, etc -- from the originally "uploaded" updater. If things
	 * work alright, the updater will have correct metadata from the update.
	 * If not, it will be blank.
	 */
	protected static void testUpdateTheUpdater() throws Exception {
		File ijRoot = createTempDirectory("testUpdaterIJRoot");
		File ij2Root = createTempDirectory("testUpdaterIJ2Root");
		File webRoot = createTempDirectory("testUpdaterWebRoot");

		copyClassPathComponentsTo(new File(ijRoot, "jars/"));
		copyClassPathComponentsTo(new File(ij2Root, "jars/"));

		writeDbXml(ijRoot, webRoot);
		writeDbXml(ij2Root, webRoot);
		writeDbXml(webRoot, null);

		File[] updaterJar = new File(ijRoot, "jars/").listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("ij-updater-core");
			}
		});
		assertTrue(updaterJar.length == 1);
		deletePomFromJarFiles(updaterJar[0]);

		uploadAll(ijRoot);
		uploadAll(ij2Root);

		System.setProperty("ij.dir", ijRoot.getAbsolutePath());
		ImageJUpdater.main(new String[0]);
	}

	/**
	 * Call the updater to upload all local files
	 * 
	 * @param ijRoot the ImageJ root
	 * @throws Exception 
	 */
	private static void uploadAll(File ijRoot) throws Exception {
		String updateSite = FilesCollection.DEFAULT_UPDATE_SITE;
		FilesCollection files = new FilesCollection(ijRoot);
		Progress progress = new StderrProgress();
		files.downloadIndexAndChecksum(progress);
		for (FileObject file : files) {
			if (file.isUploadable(files)) {
				file.stageForUpload(files, updateSite);
			}
		}
		final FilesUploader uploader = new FilesUploader(files, updateSite);
		assertTrue(uploader.login());
		uploader.upload(progress);
		System.err.println("description in " + ijRoot + ": " + files.get("jars/ij-updater-core.jar").getDescription());
		files.write();
	}

	/**
	 * Delete any pom.xml file from a given .jar file
	 * 
	 * @param jar the jar to be modified
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	private static void deletePomFromJarFiles(File jar) throws FileNotFoundException, IOException {
		JarInputStream in = new JarInputStream(new FileInputStream(jar));
		Manifest manifest = in.getManifest();

		File newJar = new File(jar.getAbsolutePath() + ".new");
		JarOutputStream out = new JarOutputStream(new FileOutputStream(newJar), manifest);
		for (;;) {
			JarEntry entry = in.getNextJarEntry();
			if (entry == null) break;
			if (("/" + entry.getName()).endsWith("/pom.xml")) continue;
			out.putNextEntry(entry);
			copy(in, out, false);
		}
		in.close();
		out.close();
		assertTrue(jar.delete());
		assertTrue(newJar.renameTo(jar));
	}

	private static void writeDbXml(File ijRoot, File webRoot) throws FileNotFoundException, IOException {
		PrintStream writer = new PrintStream(new GZIPOutputStream(new FileOutputStream(new File(ijRoot, "db.xml.gz"))));
		writer.println("<pluginRecords>");
		if (webRoot != null) {
			writer.println("\t<update-site"
					+ " name=\"" + FilesCollection.DEFAULT_UPDATE_SITE + "\""
					+ " url=\"" + webRoot.toURI().toURL() + "\""
					+ " ssh-host=\"file:localhost\""
					+ " upload-directory=\"" + webRoot.getAbsolutePath() + "\""
					+ " timestamp=\"0\""
					+ "/>");
		}
		writer.println("</pluginRecords>");
		writer.close();
	}

	private static void copyClassPathComponentsTo(File destination) throws FileNotFoundException, IOException {
		if (!destination.isDirectory()) assertTrue(destination.mkdirs());
		String classPath = System.getProperty("java.class.path");
		for (String component : classPath.split(File.pathSeparator)) {
			File file = new File(component);
			assertTrue(file.exists());
			if (file.isDirectory()) {
				if (file.getName().equals("test-classes"))
					continue;
				assertTrue(file.getName().equals("classes"));
				file = findSingleJarFile(file.getParentFile());
			}
			else if (file.getName().equals("tools.jar")) continue;
			copy(file, new File(destination, file.getName()));
		}
	}

	private static void copy(File from, File to) throws FileNotFoundException, IOException {
		copy(new FileInputStream(from), new FileOutputStream(to), true);
	}

	private static void copy(InputStream in, OutputStream out, boolean closeAtEnd) throws IOException {
		byte[] buffer = new byte[65536];
		for (;;) {
			int count = in.read(buffer);
			if (count < 0) break;
			out.write(buffer, 0, count);
		}
		if (closeAtEnd) in.close();
		if (closeAtEnd) out.close();
	}

	private static File findSingleJarFile(File directory) {
		File[] list = directory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar");
			}
		});
		assertTrue(list.length == 1);
		return list[0];
	}

	private static void assertTrue(boolean condition) {
		if (!condition) {
			throw new RuntimeException("Assertion failed");
		}
	}

	/**
	 * Create a temporary directory
	 * 
	 * @param prefix the prefix as for {@link File.createTempFile}
	 * @return the File object describing the directory
	 * @throws IOException
	 */
	protected static File createTempDirectory(final String prefix) throws IOException {
		final File file = File.createTempFile(prefix, "");
		file.delete();
		file.mkdir();
		return file;
	}
}
