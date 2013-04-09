/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

import imagej.updater.util.ByteCodeAnalyzer;
import imagej.updater.util.Util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.scijava.util.FileUtils;
import org.scijava.util.IteratorPlus;
import org.scijava.util.ProcessUtils;

/**
 * Show differences between remote and local versions of files.
 * 
 * Sometimes it is not obvious why a file is marked locally-modified. This class
 * is supposed to help.
 * 
 * It relies heavily on the 'git' executable to generate the diffs, the 'javap'
 * executable to disassemble byte code and the 'hexdump' executable to make hex
 * dumps.
 * 
 * @author Johannes Schindelin
 */
public class Diff {
	public static enum Mode {
		LIST_FILES, JAVAP, CLASS_FILE_DIFF, HEX_DIFF;

		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder();
			boolean upperCase = true;
			for (char c : name().toCharArray())
				switch (c) {
				case '_':
					builder.append(' ');
					upperCase = true;
					break;
				default:
					builder.append(upperCase ? Character.toUpperCase(c) : Character.toLowerCase(c));
					upperCase = false;
					break;
				}
			return builder.toString();
		}
	}

	private final PrintStream out;

	/**
	 * Construct a Diff object.
	 * 
	 * @param out this is where the output goes
	 */
	public Diff(final PrintStream out) {
		this.out = out;
	}

	/**
	 * Show the differences between {@code remote} and {@code local} versions of
	 * a file.
	 * 
	 * @param name
	 *            the file name
	 * @param remote
	 *            the {@link URL} to the remote version
	 * @param local
	 *            the {@link URL} to the local version
	 * @param mode
	 *            how to print the differences
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public void showDiff(final String name, final URL remote, final URL local, final Mode mode) throws IOException, MalformedURLException {
		File remoteFile = cacheFile(remote, false);
		File localFile = cacheFile(local, false);

		if (local.getFile().endsWith(".jar")) {
			out.println("\n" + remote.getFile());
			JarFile remoteJar = new JarFile(remoteFile);
			JarFile localJar = new JarFile(localFile);
			List<JarEntry> remoteEntries = getSortedEntries(remoteJar);
			List<JarEntry> localEntries = getSortedEntries(localJar);
			int remoteIndex = 0, localIndex = 0;
			for (;;) {
				if (remoteIndex >= remoteEntries.size()) {
					if (localIndex >= localEntries.size()) {
						break;
					}
					out.println("Local entry: " + localEntries.get(localIndex++).getName());
					continue;
				}
				if (localIndex >= localEntries.size()) {
					out.println("Remote entry: " + remoteEntries.get(remoteIndex++).getName());
					continue;
				}
				final JarEntry remoteEntry = remoteEntries.get(remoteIndex);
				final JarEntry localEntry = localEntries.get(localIndex);
				int nameDiff = remoteEntry.getName().compareTo(localEntry.getName());
				if (nameDiff < 0) {
					out.println("Remote entry: " + remoteEntry.getName());
					remoteIndex++;
				}
				else if (nameDiff > 0) {
					out.println("Local entry: " + localEntry.getName());
					localIndex++;
				}
				else {
					if (remoteEntry.getCrc() != localEntry.getCrc() || remoteEntry.getSize() != localEntry.getSize()) {
						if (mode == Mode.LIST_FILES) {
							out.println("Entry " + remoteEntry.getName() + " is different from local one");
						}
						else {
							if (localEntry.getName().endsWith(".class") && (mode == Mode.JAVAP || mode == Mode.CLASS_FILE_DIFF)) {
								String className = remoteEntry.getName();
								className = className.substring(0, className.length() - 6).replace('/', '.').replace('$', '.');
								File remote2, local2;
								if (mode == Mode.CLASS_FILE_DIFF) {
									remote2 = File.createTempFile("class-", "");
									analyzeByteCode(remoteJar.getInputStream(remoteEntry), remote2);
									local2 = File.createTempFile("class-", "");
									analyzeByteCode(localJar.getInputStream(localEntry), local2);
								} else {
									remote2 = javap(remoteFile, className);
									local2 = javap(localFile, className);
								}
								if (offsetOfFirstDiff(remote2, local2) >= 0) {
									out.println("Entry " + remoteEntry.getName() + " is different from local one");
									showDiff(className, remote2.toURI().toURL(), local2.toURI().toURL(), mode);
								}
							}
							else {
								showDiff(remoteEntry.getName(), 
										new URL("jar:file:" + remoteFile.getAbsolutePath() + "!/" + remoteEntry.getName()),
										new URL("jar:file:" + localFile.getAbsolutePath() + "!/" + localEntry.getName()),
										mode);
							}
						}
					}
					remoteIndex++;
					localIndex++;
				}
			}
			return;
		}

		boolean isClass = local.getFile().endsWith(".class");
		if (isClass) {
			float remoteVersion = getClassVersion(remoteFile);
			float localVersion = getClassVersion(localFile);
			if (remoteVersion != localVersion)
				out.println("class versions differ! remote: " + remoteVersion + ", local: " + localVersion);
		}
		if (mode == Mode.LIST_FILES) {
			if (remoteFile.length() != localFile.length()) {
				out.println("Size differs: " + remote.getPath() + " (remote: " + remoteFile.length() + ", local: " + localFile.length());
				return;
			}
			try {
				long compare = offsetOfFirstDiff(remoteFile, localFile);
				if (compare >= 0) {
					out.println("Offset of first difference in " + remote.getPath() + ": " + compare);
				}
			} catch (final IOException e) {
				out.println("IOException while comparing " + remote.getPath() + " to local version");
			}
			return;
		}
		if (isLocal(remote)) {
			remoteFile = cacheFile(remote, true);
		}
		if (isLocal(local)) {
			localFile = cacheFile(local, true);
		}
		if (mode == Mode.HEX_DIFF) {
			try {
				hexdump(remoteFile, remoteFile);
			} catch (Exception e) {
				out.println("Could not make a hexdump of " + remote);
			}
			try {
				hexdump(localFile, localFile);
			} catch (Exception e) {
				out.println("Could not make a hexdump of " + local);
			}
		}
		else if (isClass) {
			if (mode == Mode.CLASS_FILE_DIFF) {
				try {
					analyzeByteCode(new FileInputStream(remoteFile), remoteFile);
				} catch (Exception e) {
					out.println("Could not analyze bytecode of " + remote);
				}
				try {
					analyzeByteCode(new FileInputStream(localFile), localFile);
				} catch (Exception e) {
					out.println("Could not analyze bytecode of " + local);
				}
			}
			else {
				javap(remoteFile);
				javap(localFile);
			}
		}

		try {
			boolean color = false;
			ProcessUtils.exec(null, out, out, "git", "diff",
					"--color=" + (color ? "always" : "auto"),
					"--no-index",
					"--src-prefix=remote/" + name + "/", "--dst-prefix=local/" + name + "/",
					remoteFile.getAbsolutePath(), localFile.getAbsolutePath());
		} catch (RuntimeException e) {
			if (e.getCause() != null && e.getCause() instanceof InterruptedException)
				throw e;
			// we expect the diff to return 1 if there were differences
			if (!e.getMessage().startsWith("exit status 1")) {
				e.printStackTrace(out);
			}
		}
	}

	/**
	 * Make an alphabetically-sorted list of files inside a <i>.jar</i> file.
	 * 
	 * @param jar
	 *            the <i>.jar</i> file
	 * @return the sorted list
	 */
	protected static List<JarEntry> getSortedEntries(JarFile jar) {
		List<JarEntry> result = new ArrayList<JarEntry>();
		for (final JarEntry entry : new IteratorPlus<JarEntry>(jar.entries())) {
			result.add(entry);
		}
		Collections.sort(result, new Comparator<JarEntry>() {
			@Override
			public int compare(JarEntry a, JarEntry b) {
				return a.getName().compareTo(b.getName());
			}
		});
		return result;
	}

	/**
	 * Make a hex-dump.
	 * 
	 * This calls the {@code hex-dump} program.
	 * 
	 * @param inputFile
	 *            the file to hex-dump
	 * @param outputFile
	 *            the output file (can be the same as the {@code inputFile}
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	protected void hexdump(File inputFile, File outputFile) throws FileNotFoundException, IOException {
		final String result = ProcessUtils.exec(null, out, null, "hexdump", "-C", inputFile.getAbsolutePath());
		copy(new ByteArrayInputStream(result.getBytes()), new FileOutputStream(outputFile), true, true);
	}

	/**
	 * Inspect the byte code of a class file.
	 * 
	 * @param in
	 *            the input stream to analyze
	 * @param outputFile
	 *            the output file (can be the same as the {@code inputFile}
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	protected static void analyzeByteCode(final InputStream in, File outputFile) throws FileNotFoundException, IOException {
		final ByteCodeAnalyzer analyzer = analyzeByteCode(in, true);
		InputStream inStream = new ByteArrayInputStream(analyzer.toString()
				.getBytes());
		copy(inStream, new FileOutputStream(outputFile), true, true);
	}

	/**
	 * Analyze the byte code of a class file.
	 * 
	 * @throws IOException
	 */
	public static ByteCodeAnalyzer analyzeByteCode(final InputStream in, boolean closeStream) throws IOException {
		final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		copy(in, outStream, closeStream, true);
		return new ByteCodeAnalyzer(outStream.toByteArray(), ByteCodeAnalyzer.Mode.ALL);
	}

	/**
	 * Run {@code javap} on a class file.
	 * 
	 * @param jarFile
	 *            the <i>.jar</i> file containing the class
	 * @param className
	 *            the name of the class
	 * @return the output file
	 * @throws IOException
	 */
	protected File javap(final File jarFile, final String className) throws IOException {
		File file = File.createTempFile("javap-", "");
		try {
			final String result = ProcessUtils.exec(null, out, null, "javap", "-classpath", jarFile.getAbsolutePath(), "-c", className);
			copy(new ByteArrayInputStream(result.getBytes()), new FileOutputStream(file), true, true);
		} catch (RuntimeException e) {
			if (e.getCause() != null && e.getCause() instanceof InterruptedException)
				throw e;
			e.printStackTrace();
			out.println("class " + className + " misses dependencies");
		}
		return file;
	}

	/**
	 * Run {@code javap} on a class file.
	 * 
	 * @param file
	 *            the <i>.class</i> file (and the output file)
	 * @throws IOException
	 */
	protected void javap(final File file) throws IOException {
		final byte[] buffer = Util.readStreamAsBytes(new FileInputStream(file));
		final ByteCodeAnalyzer analyzer = new ByteCodeAnalyzer(buffer, ByteCodeAnalyzer.Mode.CONSTANTS);
		final String path = analyzer.getPathForClass();
		file.delete();
		File classFile = new File(file, path);
		classFile.getParentFile().mkdirs();
		copy(new ByteArrayInputStream(buffer), new FileOutputStream(classFile), true, true);
		final String result = ProcessUtils.exec(null, out, null, "javap", "-classpath", file.getAbsolutePath(), "-c", path.replace('/', '.'));
		while (!classFile.equals(file)) {
			classFile.delete();
			classFile = classFile.getParentFile();
		}
		copy(new ByteArrayInputStream(result.getBytes()), new FileOutputStream(file), true, true);
	}

	/**
	 * Get the version of a class file.
	 * 
	 * @param file
	 *            the <i>.class</i> file
	 * @return the version in dotted format
	 * @throws IOException
	 */
	protected static float getClassVersion(File file) throws IOException {
		return getClassVersion(new FileInputStream(file));
	}

	/**
	 * Get the version of a class file.
	 * 
	 * @param stream
	 *            the {@link InputStream} representing the <i>.class</i> file
	 * @return the version in dotted format
	 * @throws IOException
	 */
	protected static float getClassVersion(InputStream stream) throws IOException {
		DataInputStream data = new DataInputStream(stream);

		if (data.readInt() != 0xcafebabe)
			throw new IOException("Not a class");
		int minor = data.readShort();
		int major = data.readShort();
		data.close();
		return major + minor / 100000.0f;
	}

	/**
	 * Cache a file locally.
	 * 
	 * @param url
	 *            the URL of the file to cache
	 * @param evenLocal
	 *            force "caching" even local files, e.g. to transform the files
	 *            without destroying the original contents
	 * @return the cached file, or the original file if nothing was cached
	 * @throws IOException
	 */
	protected static File cacheFile(final URL url, boolean evenLocal) throws IOException {
		if (!evenLocal && isLocal(url))
			return new File(url.getPath());
		String extension = FileUtils.getExtension(url.getFile());
		if (extension.startsWith("jar-")) extension = "jar";
		final File result = File.createTempFile("diff-", "".equals(extension) ? "" : "." + extension);
		result.deleteOnExit();
		copy(url.openStream(), new FileOutputStream(result), true, true);
		return result;
	}

	/**
	 * Determine whether a URL refers to a local file.
	 * 
	 * @param url
	 *            the URL
	 * @return whether the URL is really a local one
	 */
	protected static boolean isLocal(URL url) {
		return url.getProtocol().equals("file");
	}

	/**
	 * Determine whether two files' contents differ.
	 * 
	 * @param file1
	 *            the one file
	 * @param file2
	 *            the other file
	 * @return the offset of the first difference, or -1 if the files' contents
	 *         are identical
	 * @throws IOException
	 */
	protected static long offsetOfFirstDiff(final File file1, final File file2) throws IOException {
		final BufferedInputStream in1 = new BufferedInputStream(new FileInputStream(file1));
		final BufferedInputStream in2 = new BufferedInputStream(new FileInputStream(file2));
		long counter = 0;
		for (;;) {
			int a = in1.read();
			int b = in2.read();
			if (a != b) break;
			if (a < 0) {
				counter = -1;
				break;
			}
		}
		in1.close();
		in2.close();
		return counter;
	}

	/**
	 * Copy bytes from an {@link InputStream} to an {@link OutputStream}.
	 * 
	 * @param in
	 *            the input
	 * @param out
	 *            the output
	 * @param closeIn
	 *            whether to close {@code in} after reading
	 * @param closeOut
	 *            whether to close {@code out} after reading
	 * @throws IOException
	 */
	protected static void copy(final InputStream in, final OutputStream out, boolean closeIn, boolean closeOut) throws IOException {
		byte[] buffer = new byte[16384];
		for (;;) {
			int count = in.read(buffer);
			if (count < 0)
				break;
			out.write(buffer, 0, count);
		}
		if (closeIn)
			in.close();
		if (closeOut)
			out.flush();
		out.close();
	}

	/**
	 * A main method for testing.
	 * 
	 * @param args
	 *            the command-line arguments
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		final PrintStream out = new PrintStream(System.err) {
			@Override
			public void println(String s) {
				super.println(s);
				flush();
			}
		};
		new Diff(out).showDiff(args[0], args[0].startsWith("http") ? new URL(args[0]) : new File(args[0]).toURI().toURL(), new File(args[1]).toURI().toURL(), Mode.JAVAP);
	}
}
