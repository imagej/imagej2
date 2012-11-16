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

package imagej.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * TODO
 * 
 * @author Johannes Schindelin
 */
public class CheckSezpoz {

	public static boolean verbose;

	public static final String FILE_NAME = "latest-sezpoz-check.txt";

	/**
	 * Checks the annotations of all CLASSPATH components. Optionally, it only
	 * checks the non-.jar components of the CLASSPATH. This is for Eclipse.
	 * Eclipse fails to run the annotation processor at each incremental build. In
	 * contrast to Maven, Eclipse usually does not build .jar files, though, so we
	 * can have a very quick check at startup if the annotation processor was not
	 * run correctly and undo the damage.
	 * 
	 * @param checkJars whether to inspect .jar components of the CLASSPATH
	 * @return false, when the annotation processor had to be run
	 * @throws IOException
	 */
	public static boolean check(final boolean checkJars) throws IOException {
		boolean upToDate = true;
		for (final String path : System.getProperty("java.class.path").split(
			File.pathSeparator))
		{
			if (!checkJars && path.endsWith(".jar")) continue;
			if (!check(new File(path))) upToDate = false;
		}
		return upToDate;
	}

	/**
	 * Checks the annotations of a CLASSPATH component.
	 * 
	 * @param file the CLASSPATH component (.jar file or directory)
	 * @return false, when the annotation processor had to be run
	 * @throws IOException
	 */
	public static boolean check(final File file) throws IOException {
		if (!file.exists()) return true;
		if (file.isDirectory()) return checkDirectory(file);
		else if (file.isFile() && file.getName().endsWith(".jar")) checkJar(file);
		else System.err.println("WARN: Skipping SezPoz check of " + file);
		return true;
	}

	/**
	 * Checks the annotations of a directory in the CLASSPATH.
	 * 
	 * @param classes the CLASSPATH component directory
	 * @return false, when the annotation processor had to be run
	 * @throws IOException
	 */
	public static boolean checkDirectory(final File classes) throws IOException {
		if (!FileUtils.getPath(classes).endsWith("target/classes")) {
			System.err.println("WARN: Ignoring non-Maven build directory: " +
				classes.getPath());
			return true;
		}
		for (File file : classes.listFiles()) {
			if (file.isFile() && file.getName().startsWith(".netbeans_")) {
				System.err.println("WARN: Ignoring NetBeans build directory: " +
						classes.getPath());
				return true;
			}
		}
		final File projectRoot = classes.getParentFile().getParentFile();
		final File source = new File(projectRoot, "src/main/java");
		if (!source.isDirectory()) {
			System.err.println("WARN: No src/main/java found for " + classes);
			return true;
		}
		final long latestCheck = getLatestCheck(classes.getParentFile());
		final boolean upToDate = checkDirectory(classes, source, latestCheck);
		if (!upToDate) {
			fixEclipseConfiguration(projectRoot);
			return !fix(classes, source);
		}
		return true;
	}

	/**
	 * Determines when we checked whether SezPoz ran alright last time.
	 * 
	 * @param targetDirectory the <i>target/</i> directory Maven writes into
	 * @return the timestamp of our last check
	 */
	protected static long getLatestCheck(final File targetDirectory) {
		try {
			final File file = new File(targetDirectory, FILE_NAME);
			if (!file.exists()) return -1;
			final BufferedReader reader = new BufferedReader(new FileReader(file));
			String firstLine = reader.readLine();
			reader.close();
			if (firstLine == null) return -1;
			if (firstLine.endsWith("\n")) firstLine =
				firstLine.substring(0, firstLine.length() - 1);
			return Long.parseLong(firstLine);
		}
		catch (final IOException e) {
			return -1;
		}
		catch (final NumberFormatException e) {
			return -1;
		}
	}

	/**
	 * Fakes the check for <i>.jar</i> files a la {@link #getLatestCheck(File)}.
	 * 
	 * @param jar the <i>.jar</i> file
	 * @return -1 since we cannot really tell
	 */
	protected static long getLatestCheck(final JarFile jar) {
		return -1;
	}

	private static void setLatestCheck(final File targetDirectory) {
		final File file = new File(targetDirectory, FILE_NAME);
		// let's make sure this file has LF-terminated lines
		try {
			final Date date = new Date();
			final String content =
				"" + date.getTime() + "\n" +
					DateFormat.getDateTimeInstance().format(date) + "\n";
			final OutputStream out = new FileOutputStream(file);
			out.write(content.getBytes());
			out.close();
		}
		catch (final IOException e) {
			e.printStackTrace();
			System.err.println("ERROR: Failure updating the Sezpoz check timestamp");
		}
	}

	/**
	 * Checks whether the annotations are possibly out-of-date.
	 * <p>
	 * This method looks whether there are any <i>.class</i> files older than
	 * their corresponding <i>.java</i> files, or whether there are <i>.class</i>
	 * files that were generated since last time we checked.
	 * </p>
	 * 
	 * @param classes the <i>classes/</i> directory where Maven puts the
	 *          <i>.class</i> files
	 * @param source the <i>src/main/java/<i> directory where Maven expects the
	 *          <i>.java</i> files
	 * @param youngerThan the date/time when we last checked
	 */
	public static boolean checkDirectory(final File classes, final File source,
		final long youngerThan) throws IOException
	{
		if (classes.getName().equals("META-INF") || !source.isDirectory()) return true;

		final File[] list = classes.listFiles();
		if (list == null) return true;
		for (final File file : list) {
			final String name = file.getName();
			if (file.isDirectory()) {
				if (!checkDirectory(file, new File(source, name), youngerThan)) return false;
			}
			else if (file.isFile() &&
				file.lastModified() > youngerThan &&
				name.endsWith(".class") &&
				hasAnnotation(new File(source, name.substring(0, name.length() - 5) +
					"java")))
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks a <i>.jar</i> file for stale annotations.
	 * <p>
	 * This method is broken at the moment since there is no good way to verify
	 * that SezPoz ran before the <i>.jar</i> file was packaged.
	 * </p>
	 * 
	 * @param file the <i>.jar</i> file
	 */
	public static void checkJar(final File file) throws IOException {
		final JarFile jar = new JarFile(file);
		final long mtime = getLatestCheck(jar);
		if (mtime < 0) {
			// Eclipse cannot generate .jar files (except in manual mode).
			// Assume everything is alright
			return;
		}
		for (final JarEntry entry : new IteratorPlus<JarEntry>(jar.entries())) {
			if (entry.getTime() > mtime) {
				throw new IOException("Annotations for " + entry + " in " + file +
					" are out-of-date!");
			}
		}
	}

	/**
	 * Determines whether the class defined in a file has at least one annotation.
	 * <p>
	 * This method simply parses everything before the first occurrence of the
	 * word {@code class}, skipping comments, for things looking like annotations.
	 * </p>
	 * 
	 * @param file the <i>.java</i> file to check
	 */
	protected static boolean hasAnnotation(final File file) {
		if (!file.getName().endsWith(".java")) return false;
		try {
			final BufferedReader reader = new BufferedReader(new FileReader(file));
			boolean inComment = false;
			for (;;) {
				final String line = reader.readLine();
				if (line == null) break;
				int offset = 0;
				if (inComment) {
					offset = line.indexOf("*/");
					if (offset < 0) continue;
					offset += 2;
					inComment = false;
				}
				final int eol = line.length();
				while (offset < eol) {
					final int commentStart = line.indexOf("/*", offset);
					final int lineCommentStart = line.indexOf("//", offset);
					final int end =
						Math.min(eol, Math.min(commentStart < 0 ? Integer.MAX_VALUE
							: commentStart, lineCommentStart < 0 ? Integer.MAX_VALUE
							: lineCommentStart));
					if (offset < end) {
						final int at = line.indexOf("@", offset);
						int clazz = offset;
						for (;;) {
							clazz = line.indexOf("class", clazz);
							if (clazz < 0) break;
							// is "class" the keyword, i.e. not a substring of
							// something else?
							if ((clazz == 0 || !Character.isJavaIdentifierPart(line
								.charAt(clazz - 1))) &&
								(clazz + 4 >= end || !Character.isJavaIdentifierPart(line
									.charAt(clazz + 5)))) break;
							clazz += 4;
						}
						if (at >= 0 && at < end && (clazz < 0 || at < clazz)) {
							reader.close();
							return true;
						}
						if (clazz >= 0 && clazz < end) {
							reader.close();
							return false;
						}
					}
					if (end == commentStart) {
						offset = line.indexOf("*/", commentStart + 2);
						if (offset > 0) {
							offset += 2;
							continue;
						}
						inComment = true;
					}
					break;
				}
			}
			reader.close();
			return false;
		}
		catch (final Exception e) {
			// If we cannot read it, it does not have an annotation for all we
			// know.
			return false;
		}
	}

	/**
	 * Runs SezPoz on the sources, writing the annotations into the classes'
	 * {@code META-INF/annotations/} directory.
	 * 
	 * @param classes the output directory
	 * @param sources the directory containing the source files
	 * @return whether anything in {@code META-INF/annotations/*} changed
	 */
	public static boolean fix(final File classes, final File sources) {
		final Method aptProcess;
		try {
			final Class<?> aptClass =
				CheckSezpoz.class.getClassLoader().loadClass("com.sun.tools.apt.Main");
			aptProcess =
				aptClass.getMethod("process", new Class[] { String[].class });
		}
		catch (final Exception e) {
			e.printStackTrace();
			System.err
				.println("ERROR: Could not fix " + sources + ": apt not found");
			return false;
		}
		if (!sources.exists()) {
			System.err.println("ERROR: Sources are not in the expected place: " +
				sources);
			return false;
		}

		final List<String> aptArgs = new ArrayList<String>();
		aptArgs.add("-nocompile");
		if (verbose) aptArgs.add("-verbose");
		aptArgs.add("-factory");
		aptArgs.add("net.java.sezpoz.impl.IndexerFactory");
		aptArgs.add("-d");
		aptArgs.add(classes.getPath());
		final int count = aptArgs.size();
		addJavaPathsRecursively(aptArgs, sources);
		// do nothing if there is nothing to
		if (count == aptArgs.size()) return false;

		// remove possibly outdated annotations
		final File[] annotationsBefore =
			new File(classes, "META-INF/annotations").listFiles();

		// checksum the annotations so that we can determine whether something
		// changed
		// if nothing changed, we can safely proceed
		final Map<String, byte[]> checksumsBefore = checksum(annotationsBefore);

		// before running, remove possibly outdated annotations
		if (annotationsBefore != null) {
			for (final File annotation : annotationsBefore)
				annotation.delete();
		}

		final String[] args = aptArgs.toArray(new String[aptArgs.size()]);
		try {
			System.err.println("WARN: Updating the annotation index in " + classes);
			aptProcess.invoke(null, new Object[] { args });
		}
		catch (final Exception e) {
			e.printStackTrace();
			System.err.println("WARN: Could not fix " + sources + ": apt failed");
			return false;
		}

		boolean result = true;

		final File[] annotationsAfter =
			new File(classes, "META-INF/annotations").listFiles();
		final Map<String, byte[]> checksumsAfter = checksum(annotationsAfter);
		if (checksumsAfter.size() == checksumsBefore.size()) {
			result = false;
			for (final String key : checksumsAfter.keySet()) {
				final byte[] before = checksumsBefore.get(key);
				if (before == null || !Arrays.equals(before, checksumsAfter.get(key)))
				{
					result = true;
				}
			}
		}

		setLatestCheck(classes.getParentFile());
		return result;
	}

	private static MessageDigest digest;

	/**
	 * Calculates checksums of a list of files.
	 * <p>
	 * This method is used to determine whether annotation files have been changed
	 * by SezPoz rather than re-generated identically.
	 * </p>
	 * 
	 * @param files the files to process
	 * @return a map containing (filename, checksum) mappings
	 */
	protected static Map<String, byte[]> checksum(final File[] files) {
		final Map<String, byte[]> result = new HashMap<String, byte[]>();
		if (files != null && files.length != 0) {
			for (final File file : files)
				result.put(file.getName(), checksum(file));
		}
		return result;
	}

	/**
	 * Calculate the checksum of one file.
	 * 
	 * @param file the file to process
	 * @return the checksum
	 */
	protected synchronized static byte[] checksum(final File file) {
		try {
			if (digest == null) digest = MessageDigest.getInstance("SHA-1");
			else digest.reset();
			final byte[] buffer = new byte[65536];
			final DigestInputStream digestStream =
				new DigestInputStream(new FileInputStream(file), digest);
			while (digestStream.read(buffer) >= 0) {
				// do nothing
			}
			digestStream.close();
			return digest.digest();
		}
		catch (final Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * Builds a list of <i>.java</i> files in a directory including all its
	 * sub-directories.
	 * 
	 * @param list the list of filenames to append to
	 * @param directory the directory
	 */
	protected static void addJavaPathsRecursively(final List<String> list,
		final File directory)
	{
		final File[] files = directory.listFiles();
		if (files == null) return;
		for (final File file : files) {
			if (file.isDirectory()) addJavaPathsRecursively(list, file);
			else if (file.isFile() && file.getName().endsWith(".java")) {
				list.add(file.getPath());
			}
		}
	}

	/**
	 * Adjusts the mtime of a file to "now".
	 * 
	 * @param file the file to touch
	 * @throws IOException
	 */
	protected static void touch(final File file) throws IOException {
		new FileOutputStream(file, true).close();
	}

	/**
	 * Makes sure that the given Eclipse project is set up correctly to run
	 * SezPoz.
	 * <p>
	 * If the {@code directory} does not point to an Eclipse project, the method
	 * will simply return.
	 * </p>
	 * 
	 * @param directory the directory in which the project lives
	 */
	protected static void fixEclipseConfiguration(final File directory) {
		// is this an Eclipse project at all?
		if (!new File(directory, ".settings").isDirectory()) return;
		fixFactoryPath(directory);
		fixAnnotationProcessingSettings(directory);
	}

	/**
	 * Makes sure that the given Eclipse project has a <i>.factorypath</i>
	 * pointing to SezPoz in the current user's Maven repository.
	 * 
	 * @param directory the Eclipse project to fix
	 */
	protected static void fixFactoryPath(final File directory) {
		final File factoryPath = new File(directory, ".factorypath");
		try {
			final Document xml;
			if (factoryPath.exists()) {
				xml = readXMLFile(factoryPath);
			}
			else {
				xml =
					DocumentBuilderFactory.newInstance().newDocumentBuilder()
						.newDocument();
				xml.appendChild(xml.createElement("factorypath"));
			}
			if (!containsSezpozId(xml.getElementsByTagName("factorypathentry"))) {
				final Element element = xml.createElement("factorypathentry");
				element.setAttribute("enabled", "true");
				element.setAttribute("id",
					"M2_REPO/net/java/sezpoz/sezpoz/1.9/sezpoz-1.9.jar");
				element.setAttribute("kind", "VARJAR");
				element.setAttribute("runInBatchMode", "true");
				xml.getDocumentElement().appendChild(element);
				writeXMLFile(xml, factoryPath);
			}
		}
		catch (final Exception e) {
			e.printStackTrace();
			System.err.println("ERROR: Could not modify " + factoryPath);
		}
	}

	/**
	 * Determines whether a parsed Eclipse configuration file contains a SezPoz
	 * entry already.
	 * 
	 * @param elements the parsed Eclipse configuration
	 * @return whether SezPoz is configured already
	 */
	private static boolean containsSezpozId(final NodeList elements) {
		if (elements == null) return false;
		for (int i = 0; i < elements.getLength(); i++) {
			final NamedNodeMap attributes = elements.item(i).getAttributes();
			for (int j = 0; j < attributes.getLength(); j++) {
				final Attr attribute = (Attr) attributes.item(j);
				if (attribute.getName().equals("id") &&
					attribute.getValue().indexOf("sezpoz") >= 0) return true;
			}
		}
		return false;
	}

	/**
	 * Parses an <i>.xml</i> file into a DOM.
	 * 
	 * @param file the <i>.xml</i> file to parse
	 * @return the DOM
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	protected static Document readXMLFile(final File file)
		throws ParserConfigurationException, SAXException, IOException
	{
		final DocumentBuilderFactory builderFactory =
			DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		builder = builderFactory.newDocumentBuilder();
		return builder.parse(file);
	}

	/**
	 * Writes out a DOM as <i>.xml</i> file.
	 * 
	 * @param xml the DOM
	 * @param file the file to write
	 * @throws TransformerException
	 */
	public static void writeXMLFile(final Document xml, final File file)
		throws TransformerException
	{
		final Source source = new DOMSource(xml);
		final Result result = new StreamResult(file);
		final TransformerFactory factory = TransformerFactory.newInstance();
		final Transformer transformer = factory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
			"4");
		transformer.transform(source, result);
	}

	/**
	 * Makes sure that the given Eclipse project has annotation processing
	 * switched on.
	 * 
	 * @param directory the Eclipse project to fix
	 */
	protected static void fixAnnotationProcessingSettings(final File directory) {
		final File jdtSettings =
			new File(directory, ".settings/org.eclipse.jdt.apt.core.prefs");
		try {
			final Properties properties = new Properties();
			if (jdtSettings.exists()) {
				properties.load(new FileInputStream(jdtSettings));
			}
			boolean changed = false;
			for (final String pair : new String[] { "aptEnabled=true",
				"genSrcDir=target/classes", "reconcileEnabled=false" })
			{
				final int equals = pair.indexOf('=');
				final String key = "org.eclipse.jdt.apt." + pair.substring(0, equals);
				final String value = pair.substring(equals + 1);
				if (value.equals(properties.get(key))) continue;
				properties.put(key, value);
				changed = true;
			}
			if (changed) properties.store(new FileOutputStream(jdtSettings), null);
		}
		catch (final Exception e) {
			e.printStackTrace();
			System.err.println("ERROR: Could not edit " + jdtSettings);
		}
	}

	/**
	 * Writes plain text into a plain file.
	 * 
	 * @param file the plain file
	 * @param contents the plain text
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	protected static void write(final File file, final String contents)
		throws IOException, UnsupportedEncodingException
	{
		final OutputStream out = new FileOutputStream(file);
		out.write(contents.getBytes("UTF-8"));
		out.close();
	}
}
