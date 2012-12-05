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

package imagej.build.minimaven;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * TODO
 * 
 * @author Johannes Schindelin
 */
@SuppressWarnings("hiding")
public class BuildEnvironment {
	protected String endLine = isInteractiveConsole() ? "\033[K\r" : "\n";
	protected boolean verbose, debug = false, downloadAutomatically, offlineMode, ignoreMavenRepositories;
	protected int updateInterval = 24 * 60; // by default, check once per 24h for new snapshot versions
	protected PrintStream err;
	protected JavaCompiler javac;
	protected Map<String, MavenProject> localPOMCache = new HashMap<String, MavenProject>();
	protected Map<File, MavenProject> file2pom = new HashMap<File, MavenProject>();
	protected Stack<File> multiProjectRoots = new Stack<File>();
	protected Set<File> excludedFromMultiProjects = new HashSet<File>();
	protected final static File mavenRepository;

	static {
		File repository = new File(System.getProperty("user.home"), ".m2/repository");
		try {
			repository = repository.getCanonicalFile();
		} catch (IOException e) {
			// ignore
		}
		mavenRepository = repository;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public boolean getDownloadAutomatically() {
		return downloadAutomatically && !offlineMode;
	}

	protected static boolean isInteractiveConsole() {
		// We want to compile/run with Java5, so we cannot test System.console() directly
		try {
			return null != System.class.getMethod("console").invoke(null);
		} catch (Throwable t) {
			return false;
		}
	}

	public BuildEnvironment(PrintStream err, boolean downloadAutomatically, boolean verbose, boolean debug) {
		this.err = err == null ? System.err : err;
		javac = new JavaCompiler(this.err, this.err);
		this.downloadAutomatically = downloadAutomatically;
		this.verbose = verbose;
		this.debug = debug;
		if ("true".equalsIgnoreCase(System.getProperty("minimaven.offline")))
			offlineMode = true;
		if ("ignore".equalsIgnoreCase(System.getProperty("minimaven.repositories")))
			ignoreMavenRepositories = true;
		String updateInterval = System.getProperty("minimaven.updateinterval");
		if (updateInterval != null && !updateInterval.equals("")) try {
			this.updateInterval = Integer.parseInt(updateInterval);
			if (verbose)
				this.err.println("Setting update interval to " + this.updateInterval + " minutes");
		} catch (NumberFormatException e) {
			this.err.println("Warning: ignoring invalid update interval " + updateInterval);
		}
	}

	protected void print80(String string) {
		int length = string.length();
		err.print((verbose || length < 80 ? string : string.substring(0, 80)) + endLine);
	}

	public MavenProject parse(File file) throws IOException, ParserConfigurationException, SAXException {
		return parse(file, null);
	}

	public MavenProject parse(File file, MavenProject parent) throws IOException, ParserConfigurationException, SAXException {
		return parse(file, parent, null);
	}

	public MavenProject parse(File file, MavenProject parent, String classifier) throws IOException, ParserConfigurationException, SAXException {
		if (file2pom.containsKey(file))
			return file2pom.get(file);

		if (!file.exists())
			return null;
		if (verbose)
			print80("Parsing " + file);
		File directory = file.getCanonicalFile().getParentFile();
		final MavenProject pom = parse(new FileInputStream(file), directory, parent, classifier);
		file2pom.put(file, pom);
		return pom;
	}

	public MavenProject parse(final InputStream in, final File directory, final MavenProject parent, final String classifier) throws SAXException, ParserConfigurationException, IOException {
		MavenProject pom = new MavenProject(this, directory, parent);
		pom.coordinate.classifier = classifier;
		if (parent != null) {
			pom.sourceDirectory = parent.sourceDirectory;
			pom.includeImplementationBuild = parent.includeImplementationBuild;
		}
		XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
		reader.setContentHandler(pom);
		//reader.setXMLErrorHandler(...);
		reader.parse(new InputSource(in));
		in.close();
		if (pom.coordinate.artifactId == null || pom.coordinate.artifactId.equals(""))
			throw new SAXException("Missing artifactId: " + new File(directory, "pom.xml"));
		if (pom.coordinate.groupId == null || pom.coordinate.groupId.equals(""))
			throw new SAXException("Missing groupId: " + new File(directory, "pom.xml"));
		String version = pom.coordinate.getVersion();
		if (version == null || version.equals(""))
			throw new SAXException("Missing version: " + new File(directory, "pom.xml"));

		pom.children = new MavenProject[pom.modules.size()];
		for (int i = 0; i < pom.children.length; i++) {
			File child = new File(directory, pom.modules.get(i) + "/pom.xml");
			pom.children[i] = parse(child, pom);
		}

		if (pom.target == null) {
			String fileName = pom.coordinate.getJarName();
			pom.target = new File(directory, fileName);
		}

		String key = pom.expand(pom.coordinate).getKey();
		if (!localPOMCache.containsKey(key))
			localPOMCache.put(key, pom);

		if (pom.packaging.equals("jar") && !directory.getPath().startsWith(mavenRepository.getPath())) {
			pom.buildFromSource = true;
			pom.target = new File(directory, "target/classes");
		}

		if (pom.parentCoordinate != null && pom.parent == null) {
			Coordinate dependency = pom.expand(pom.parentCoordinate);
			pom.parent = pom.findPOM(dependency, true, false);

			if (pom.parent == null) {
				File parentDirectory = pom.directory.getParentFile();
				if (parentDirectory == null) try {
					parentDirectory = pom.directory.getCanonicalFile().getParentFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (parentDirectory != null) {
					File parentFile = new File(parentDirectory, "pom.xml");
					if (parentFile.exists())
						pom.parent = parse(parentFile, null, null);
				}
			}

			if (pom.parent == null && downloadAutomatically) {
				if (pom.maybeDownloadAutomatically(pom.parentCoordinate, !verbose, downloadAutomatically))
					pom.parent = pom.findPOM(dependency, !verbose, downloadAutomatically);
			}
			if (pom.parent == null) {
				throw new RuntimeException("Parent not found: " + pom.parentCoordinate
						+ (downloadAutomatically ? "" : " (please call MiniMaven's 'download'"));
			}
			// prevent infinite loops (POMs without parents get the current root as parent)
			if (pom.parent.parent == pom)
				pom.parent.parent = null;
			if (pom.parent.includeImplementationBuild)
				pom.includeImplementationBuild = true;
			pom.parent.addChild(pom);
		}

		return pom;
	}

	public MavenProject fakePOM(File target, Coordinate dependency) {
		MavenProject pom = new MavenProject(this, target, null);
		pom.directory = target.getParentFile();
		pom.target = target;
		pom.children = new MavenProject[0];
		pom.coordinate = dependency;
		if (dependency.artifactId.equals("ij")) {
			String javac = pom.expand("${java.home}/../lib/tools.jar");
			if (new File(javac).exists())
				pom.dependencies.add(new Coordinate("com.sun", "tools", "1.4.2", null, false, javac, null));
		}
		else if (dependency.artifactId.equals("imglib2-io"))
			pom.dependencies.add(new Coordinate("loci", "bio-formats", "${bio-formats.version}"));
		else if (dependency.artifactId.equals("jfreechart"))
			pom.dependencies.add(new Coordinate("jfree", "jcommon", "1.0.17"));

		String key = dependency.getKey();
		if (localPOMCache.containsKey(key))
			err.println("Warning: " + target + " overrides " + localPOMCache.get(key));
		localPOMCache.put(key, pom);

		return pom;
	}

	public boolean containsProject(final String groupId, final String artifactId) {
		return containsProject(new Coordinate(groupId, artifactId, null));
	}

	public boolean containsProject(final Coordinate coordinate) {
		return localPOMCache.containsKey(coordinate.getKey());
	}

	public void addMultiProjectRoot(File root) {
		try {
			multiProjectRoots.push(root.getCanonicalFile());
		} catch (IOException e) {
			multiProjectRoots.push(root);
		}
	}

	public void excludeFromMultiProjects(File directory) {
		try {
			excludedFromMultiProjects.add(directory.getCanonicalFile());
		} catch (IOException e) {
			excludedFromMultiProjects.add(directory);
		}
	}

	public void parseMultiProjects() throws IOException, ParserConfigurationException, SAXException {
		while (!multiProjectRoots.empty()) {
			File root = multiProjectRoots.pop();
			if (root == null || !root.exists())
				continue;
			File[] list = root.listFiles();
			if (list == null)
				continue;
			Arrays.sort(list);
			for (File directory : list) {
				if (excludedFromMultiProjects.contains(directory))
					continue;
				File file = new File(directory, "pom.xml");
				if (!file.exists())
					continue;
				parse(file, null);
			}
		}
	}

	protected void downloadAndVerify(String repositoryURL, Coordinate dependency, boolean quiet) throws MalformedURLException, IOException, NoSuchAlgorithmException, ParserConfigurationException, SAXException {
		String path = "/" + dependency.groupId.replace('.', '/') + "/" + dependency.artifactId + "/" + dependency.version + "/";
		File directory = new File(mavenRepository, path);
		if (dependency.version.endsWith("-SNAPSHOT")) {
			// Only check snapshots once per day
			File snapshotMetaData = new File(directory, "maven-metadata-snapshot.xml");
			if (System.currentTimeMillis() - snapshotMetaData.lastModified() < updateInterval * 60 * 1000l)
				return;

			String message = quiet ? null : "Checking for new snapshot of " + dependency.artifactId;
			String metadataURL = repositoryURL + path + "maven-metadata.xml";
			downloadAndVerify(metadataURL, directory, snapshotMetaData.getName(), message);
			String snapshotVersion = SnapshotPOMHandler.parse(snapshotMetaData);
			if (snapshotVersion == null)
				throw new IOException("No version found in " + metadataURL);
			dependency.setSnapshotVersion(snapshotVersion);
			if (new File(directory, dependency.getJarName()).exists() &&
					new File(directory, dependency.getPOMName()).exists())
				return;
		}
		else if (dependency.version.startsWith("[")) {
			path = "/" + dependency.groupId.replace('.', '/') + "/" + dependency.artifactId + "/";
			directory = new File(mavenRepository, path);

			// Only check versions once per day
			File versionMetaData = new File(directory, "maven-metadata-version.xml");
			if (System.currentTimeMillis() - versionMetaData.lastModified() < updateInterval * 60 * 1000l)
				return;

			String message = quiet ? null : "Checking for new version of " + dependency.artifactId;
			String metadataURL = repositoryURL + path + "maven-metadata.xml";
			downloadAndVerify(metadataURL, directory, versionMetaData.getName(), message);
			dependency.snapshotVersion = VersionPOMHandler.parse(versionMetaData);
			if (dependency.snapshotVersion == null)
				throw new IOException("No version found in " + metadataURL);
			path = "/" + dependency.groupId.replace('.', '/') + "/" + dependency.artifactId + "/" + dependency.snapshotVersion + "/";
			directory = new File(mavenRepository, path);
			if (new File(directory, dependency.getJarName()).exists() &&
					new File(directory, dependency.getPOMName()).exists())
				return;
		}
		String message = quiet ? null : "Downloading " + dependency.artifactId;
		String baseURL = repositoryURL + path;
		downloadAndVerify(baseURL + dependency.getPOMName(), directory, null);
		if (!isAggregatorPOM(new File(directory, dependency.getPOMName())))
			downloadAndVerify(baseURL + dependency.getJarName(), directory, message);
	}

	protected void downloadAndVerify(String url, File directory, String message) throws IOException, NoSuchAlgorithmException {
		downloadAndVerify(url, directory, null, message);
	}

	protected void downloadAndVerify(String url, File directory, String fileName, String message) throws IOException, NoSuchAlgorithmException {
		File sha1 = download(new URL(url + ".sha1"), directory, fileName == null ? null : fileName + ".sha1", null);
		File file = download(new URL(url), directory, fileName, message);
		MessageDigest digest = MessageDigest.getInstance("SHA-1");
		FileInputStream fileStream = new FileInputStream(file);
		DigestInputStream digestStream = new DigestInputStream(fileStream, digest);
		byte[] buffer = new byte[131072];
		while (digestStream.read(buffer) >= 0) {
			/* do nothing */
		}
		digestStream.close();

		byte[] digestBytes = digest.digest();
		fileStream = new FileInputStream(sha1);
		for (int i = 0; i < digestBytes.length; i++) {
			int value = (hexNybble(fileStream.read()) << 4) |
				hexNybble(fileStream.read());
			int d = digestBytes[i] & 0xff;
			if (value != d)
				throw new IOException("SHA1 mismatch: " + sha1 + ": " + Integer.toHexString(value) + " != " + Integer.toHexString(d));
		}
		fileStream.close();
	}

	protected boolean isAggregatorPOM(File xml) {
		if (!xml.exists())
			return false;
		try {
			return isAggregatorPOM(new FileInputStream(xml));
		} catch (IOException e) {
			e.printStackTrace(err);
			return false;
		}
	}

	protected boolean isAggregatorPOM(final InputStream in) {
		final RuntimeException yes = new RuntimeException(), no = new RuntimeException();
		try {
			DefaultHandler handler = new DefaultHandler() {
				protected int level = 0;

				@Override
				public void startElement(String uri, String localName, String qName, Attributes attributes) {
					if ((level == 0 && "project".equals(qName)) || (level == 1 && "packaging".equals(qName)))
						level++;
				}

				@Override
				public void endElement(String uri, String localName, String qName) {
					if ((level == 1 && "project".equals(qName)) || (level == 2 && "packaging".equals(qName)))
						level--;
				}

				@Override
				public void characters(char[] ch, int start, int length) {
					if (level == 2)
						throw "pom".equals(new String(ch, start, length)) ? yes : no;
				}
			};
			XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
			reader.setContentHandler(handler);
			reader.parse(new InputSource(in));
			in.close();
			return false;
		} catch (Exception e) {
			try {
				in.close();
			} catch (IOException e2) {
				e2.printStackTrace(err);
			}
			if (e == yes)
				return true;
			if (e == no)
				return false;
			e.printStackTrace(err);
			return false;
		}
	}

	protected static int hexNybble(int b) {
		return (b < 'A' ? (b < 'a' ? b - '0' : b - 'a' + 10) : b - 'A' + 10) & 0xf;
	}

	protected static void rmRF(File directory) {
		for (File file : directory.listFiles())
			if (file.isDirectory())
				rmRF(file);
			else
				file.delete();
		directory.delete();
	}

	protected File download(URL url, File directory, String message) throws IOException {
		return download(url, directory, null, message);
	}

	protected File download(URL url, File directory, String fileName, String message) throws IOException {
		if (offlineMode)
			throw new RuntimeException("Offline!");
		if (verbose)
			err.println("Trying to download " + url);
		String name = fileName;
		if (name == null) {
			name = url.getPath();
			name = name.substring(name.lastIndexOf('/') + 1);
		}
		InputStream in = url.openStream();
		if (message != null)
			err.println(message);
		directory.mkdirs();
		File result = new File(directory, name);
		copy(in, result);
		return result;
	}

	public static void copyFile(File source, File target) throws IOException {
		copy(new FileInputStream(source), target);
	}

	public static void copy(InputStream in, File target) throws IOException {
		copy(in, new FileOutputStream(target), true);
	}

	public static void copy(InputStream in, OutputStream out, boolean closeOutput) throws IOException {
		byte[] buffer = new byte[131072];
		for (;;) {
			int count = in.read(buffer);
			if (count < 0)
				break;
			out.write(buffer, 0, count);
		}
		in.close();
		if (closeOutput)
			out.close();
	}

	protected static int compareVersion(String version1, String version2) {
		if (version1 == null)
			return version2 == null ? 0 : -1;
		if (version1.equals(version2))
			return 0;
		String[] split1 = version1.split("\\.");
		String[] split2 = version2.split("\\.");

		for (int i = 0; ; i++) {
			if (i == split1.length)
				return i == split2.length ? 0 : -1;
			if (i == split2.length)
				return +1;
			int end1 = firstNonDigit(split1[i]);
			int end2 = firstNonDigit(split2[i]);
			if (end1 != end2)
				return end1 - end2;
			int result = end1 == 0 ? 0 :
				Integer.parseInt(split1[i].substring(0, end1))
				- Integer.parseInt(split2[i].substring(0, end2));
			if (result != 0)
				return result;
			result = split1[i].substring(end1).compareTo(split2[i].substring(end2));
			if (result != 0)
				return result;
		}
	}

	protected static int firstNonDigit(String string) {
		int length = string.length();
		for (int i = 0; i < length; i++)
			if (!Character.isDigit(string.charAt(i)))
				return i;
		return length;
	}

	protected String getImplementationBuild(File fileOrDirectory) {
		File file = fileOrDirectory;
		if (!file.isAbsolute()) try {
			file = file.getCanonicalFile();
		}
		catch (IOException e) {
			file = file.getAbsoluteFile();
		}
		for (;;) {
			File gitDir = new File(file, ".git");
			if (gitDir.exists())
				return exec(gitDir.getParentFile(), "git", "rev-parse", "HEAD");
			file = file.getParentFile();
			if (file == null)
				return null;
		}
	}

	protected String exec(File gitDir, String... args) {
		try {
			Process process = Runtime.getRuntime().exec(args, null, gitDir);
			process.getOutputStream().close();
			ReadInto err = new ReadInto(process.getErrorStream(), this.err);
			ReadInto out = new ReadInto(process.getInputStream(), null);
			try {
				process.waitFor();
				err.join();
				out.join();
			}
			catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			if (process.exitValue() != 0)
				throw new RuntimeException("Error executing " + Arrays.toString(args) + "\n" + err);
			return out.toString();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
