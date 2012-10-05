package fiji.build.minimaven;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.Attributes.Name;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import fiji.build.minimaven.JavaCompiler.CompileError;

public class POM extends DefaultHandler implements Comparable<POM> {
	protected final BuildEnvironment env;
	protected boolean buildFromSource, built;
	protected File directory, target;
	protected String sourceDirectory = "src/main/java";
	protected POM parent;
	protected POM[] children;

	protected Coordinate coordinate = new Coordinate(), parentCoordinate;
	protected Map<String, String> properties = new HashMap<String, String>();
	protected List<String> modules = new ArrayList<String>();
	protected List<Coordinate> dependencies = new ArrayList<Coordinate>();
	protected Set<String> repositories = new TreeSet<String>();
	protected String sourceVersion, targetVersion, mainClass;
	protected boolean includeImplementationBuild;
	protected String packaging = "jar";

	// only used during parsing
	protected String prefix = "";
	protected Coordinate latestDependency = new Coordinate();
	protected boolean isCurrentProfile;
	protected String currentPluginName;

	protected POM addModule(String name) throws IOException, ParserConfigurationException, SAXException {
		return addChild(env.parse(new File(new File(directory, name), "pom.xml"), this));
	}

	protected POM addChild(POM child) {
		POM[] newChildren = new POM[children.length + 1];
		System.arraycopy(children, 0, newChildren, 0, children.length);
		newChildren[children.length] = child;
		children = newChildren;
		return child;
	}

	protected POM(final BuildEnvironment miniMaven, File directory, POM parent) {
		env = miniMaven;
		this.directory = directory;
		this.parent = parent;
		if (parent != null) {
			coordinate.groupId = parent.coordinate.groupId;
			coordinate.version = parent.coordinate.version;
			parentCoordinate = parent.coordinate;
			includeImplementationBuild = parent.includeImplementationBuild;
		}
	}

	public void clean() throws IOException, ParserConfigurationException, SAXException {
		if (!buildFromSource)
			return;
		for (POM child : getDependencies(true, env.downloadAutomatically))
			if (child != null)
				child.clean();
		if (target.isDirectory())
			BuildEnvironment.rmRF(target);
		else if (target.exists())
			target.delete();
		File jar = getTarget();
		if (jar.exists())
			jar.delete();
	}

	public void downloadDependencies() throws IOException, ParserConfigurationException, SAXException {
		getDependencies(true, true, "test");
		download();
	}

	protected void download() throws FileNotFoundException {
		if (buildFromSource || target.exists())
			return;
		download(coordinate, true);
	}

	protected void download(Coordinate dependency, boolean quiet) throws FileNotFoundException {
		for (String url : getRoot().getRepositories()) try {
			env.downloadAndVerify(url, dependency, quiet);
			return;
		} catch (Exception e) {
			if (env.verbose)
				e.printStackTrace();
		}
		throw new FileNotFoundException("Could not download " + dependency.getJarName());
	}

	public boolean upToDate(boolean includingJar) throws IOException, ParserConfigurationException, SAXException {
		if (!buildFromSource)
			return true;
		for (POM child : getDependencies(true, env.downloadAutomatically, "test"))
			if (child != null && !child.upToDate(includingJar)) {
				if (env.verbose) {
					env.err.println(getArtifactId() + " not up-to-date because of " + child.getArtifactId());
				}
				return false;
			}

		File source = getSourceDirectory();

		List<String> notUpToDates = new ArrayList<String>();
		long lastModified = addRecursively(notUpToDates, source, ".java", target, ".class", false);
		int count = notUpToDates.size();

		// ugly work-around for Bio-Formats: EFHSSF.java only contains commented-out code
		if (count == 1 && notUpToDates.get(0).endsWith("poi/hssf/dev/EFHSSF.java")) {
			count = 0;
		}

		if (count > 0) {
			if (env.verbose) {
				final StringBuilder files = new StringBuilder();
				int counter = 0;
				for (String item : notUpToDates) {
					if (counter > 3) {
						files.append(", ...");
						break;
					}
					else if (counter > 0) {
						files.append(", ");
					}
					files.append(item);
				}
				env.err.println(getArtifactId() + " not up-to-date because " + count + " source files are not up-to-date (" + files + ")");
			}
			return false;
		}
		long lastModified2 = updateRecursively(new File(source.getParentFile(), "resources"), target, true);
		if (lastModified < lastModified2)
			lastModified = lastModified2;
		if (includingJar) {
			File jar = getTarget();
			if (!jar.exists() || jar.lastModified() < lastModified) {
				if (env.verbose) {
					env.err.println(getArtifactId() + " not up-to-date because " + jar + " is not up-to-date");
				}
				return false;
			}
		}
		return true;
	}

	public File getSourceDirectory() {
		String sourcePath = getSourcePath();
		File file = new File(sourcePath);
		if (file.isAbsolute())
			return file;
		return new File(directory, sourcePath);
	}

	public String getSourcePath() {
		return expand(sourceDirectory);
	}

	protected void addToJarRecursively(JarOutputStream out, File directory, String prefix) throws IOException {
		for (File file : directory.listFiles())
			if (file.isFile()) {
				// For backwards-compatibility with the Fiji Updater, let's not include pom.properties files in the Updater itself
				if (file.getAbsolutePath().endsWith("/Fiji_Updater/target/classes/META-INF/maven/sc.fiji/Fiji_Updater/pom.properties"))
					continue;
				out.putNextEntry(new ZipEntry(prefix + file.getName()));
				BuildEnvironment.copy(new FileInputStream(file), out, false);
			}
			else if (file.isDirectory())
				addToJarRecursively(out, file, prefix + file.getName() + "/");
	}

	public void buildJar() throws CompileError, IOException, ParserConfigurationException, SAXException {
		build(true);
	}

	public void build() throws CompileError, IOException, ParserConfigurationException, SAXException {
		build(false);
	}

	public void build(boolean makeJar) throws CompileError, IOException, ParserConfigurationException, SAXException {
		if (!buildFromSource || built)
			return;
		boolean forceFullBuild = false;
		for (POM child : getDependencies(true, env.downloadAutomatically, "test"))
			if (child != null && !child.upToDate(makeJar)) {
				child.build(makeJar);
				forceFullBuild = true;
			}

		// do not build aggregator projects
		if (!new File(directory, "src").exists())
			return;

		target.mkdirs();
		File source = getSourceDirectory();

		List<String> arguments = new ArrayList<String>();
		// classpath
		String classPath = getClassPath(true);
		POM pom2 = this;
		while (pom2 != null && pom2.sourceVersion == null)
			pom2 = pom2.parent;
		if (pom2 != null) {
			arguments.add("-source");
			arguments.add(pom2.sourceVersion);
		}
		pom2 = this;
		while (pom2 != null && pom2.targetVersion == null)
			pom2 = pom2.parent;
		if (pom2 != null) {
			arguments.add("-target");
			arguments.add(pom2.targetVersion);
		}
		arguments.add("-classpath");
		arguments.add(classPath);
		// output directory
		arguments.add("-d");
		arguments.add(target.getPath());
		// the files
		int count = arguments.size();
		addRecursively(arguments, source, ".java", target, ".class", !forceFullBuild);
		count = arguments.size() - count;

		if (count > 0) {
			env.err.println("Compiling " + count + " files in " + directory);
			if (env.verbose) {
				env.err.println(arguments.toString());
				env.err.println("using the class path: " + classPath);
			}
			String[] array = arguments.toArray(new String[arguments.size()]);
			if (env.javac != null)
				env.javac.call(array, env.verbose);
		}

		updateRecursively(new File(source.getParentFile(), "resources"), target, false);

		File pom = new File(directory, "pom.xml");
		if (pom.exists()) {
			File targetFile = new File(target, "META-INF/maven/" + coordinate.groupId + "/" + coordinate.artifactId + "/pom.xml");
			targetFile.getParentFile().mkdirs();
			BuildEnvironment.copyFile(pom, targetFile);
		}

		if (mainClass != null || includeImplementationBuild) {
			File file = new File(target, "META-INF/MANIFEST.MF");
			Manifest manifest = null;
			if (file.exists())
				manifest = new Manifest(new FileInputStream(file));
			else {
				manifest = new Manifest();
				manifest.getMainAttributes().put(Name.MANIFEST_VERSION, "1.0");
				file.getParentFile().mkdirs();
			}
			if (mainClass != null)
				manifest.getMainAttributes().put(Name.MAIN_CLASS, mainClass);
			if (includeImplementationBuild && !getArtifactId().equals("Fiji_Updater"))
				manifest.getMainAttributes().put(new Name("Implementation-Build"), env.getImplementationBuild(directory));
			manifest.write(new FileOutputStream(file));
		}

		if (makeJar) {
			JarOutputStream out = new JarOutputStream(new FileOutputStream(getTarget()));
			addToJarRecursively(out, target, "");
			out.close();
		}

		built = true;
	}

	protected long addRecursively(List<String> list, File directory, String extension, File targetDirectory, String targetExtension, boolean includeUpToDates) {
		long lastModified = 0;
		if (list == null)
			return lastModified;
		File[] files = directory.listFiles();
		if (files == null)
			return lastModified;
		for (File file : files)
			if (file.isDirectory()) {
				long lastModified2 = addRecursively(list, file, extension, new File(targetDirectory, file.getName()), targetExtension, includeUpToDates);
				if (lastModified < lastModified2)
					lastModified = lastModified2;
			}
			else {
				String name = file.getName();
				if (!name.endsWith(extension) || name.equals("package-info.java"))
					continue;
				File targetFile = new File(targetDirectory, name.substring(0, name.length() - extension.length()) + targetExtension);
				long lastModified2 = file.lastModified();
				if (lastModified < lastModified2)
					lastModified = lastModified2;
				if (includeUpToDates || !targetFile.exists() || targetFile.lastModified() < lastModified2)
					list.add(file.getPath());
			}
		return lastModified;
	}

	protected long updateRecursively(File source, File target, boolean dryRun) throws IOException {
		long lastModified = 0;
		File[] list = source.listFiles();
		if (list == null)
			return lastModified;
		for (File file : list) {
			File targetFile = new File(target, file.getName());
			if (file.isDirectory()) {
				long lastModified2 = updateRecursively(file, targetFile, dryRun);
				if (lastModified < lastModified2)
					lastModified = lastModified2;
			}
			else if (file.isFile()) {
				long lastModified2 = file.lastModified();
				if (lastModified < lastModified2)
					lastModified = lastModified2;
				if (dryRun || (targetFile.exists() && targetFile.lastModified() >= lastModified2))
					continue;
				targetFile.getParentFile().mkdirs();
				BuildEnvironment.copyFile(file, targetFile);
			}
		}
		return lastModified;
	}

	public Coordinate getCoordinate() {
		return coordinate;
	}

	public String getGroupId() {
		return coordinate.groupId;
	}

	public String getArtifactId() {
		return coordinate.artifactId;
	}

	public String getVersion() {
		return coordinate.version;
	}

	public String getJarName() {
		return coordinate.getJarName();
	}

	public String getMainClass() {
		return mainClass;
	}

	public String getPackaging() {
		return packaging;
	}

	public File getTarget() {
		if (!buildFromSource)
			return target;
		return new File(new File(directory, "target"), getJarName());
	}

	public File getDirectory() {
		return directory;
	}

	public boolean getBuildFromSource() {
		return buildFromSource;
	}

	public String getClassPath(boolean forCompile) throws IOException, ParserConfigurationException, SAXException {
		StringBuilder builder = new StringBuilder();
		builder.append(target);
		if (env.debug)
			env.err.println("Get classpath for " + coordinate + " for " + (forCompile ? "compile" : "runtime"));
		for (POM pom : getDependencies(true, env.downloadAutomatically, "test", forCompile ? "runtime" : "provided")) {
			if (env.debug)
				env.err.println("Adding dependency " + pom.coordinate + " to classpath");
			builder.append(File.pathSeparator).append(pom.getTarget());
		}
		return builder.toString();
	}

	/**
	 * Copy the runtime dependencies
	 *
	 * @param directory where to copy the files to
	 * @param onlyNewer whether to copy the files only if the sources are newer
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public void copyDependencies(File directory, boolean onlyNewer) throws IOException, ParserConfigurationException, SAXException {
		for (POM pom : getDependencies(true, env.downloadAutomatically, "test", "provided")) {
			File file = pom.getTarget();
			File destination = new File(directory, pom.coordinate.artifactId + ".jar");
			if (file.exists() && (!onlyNewer || (!destination.exists() || destination.lastModified() < file.lastModified())))
				BuildEnvironment.copyFile(file, destination);
		}
	}

	public Set<POM> getDependencies() throws IOException, ParserConfigurationException, SAXException {
		return getDependencies(false, env.downloadAutomatically);
	}

	public Set<POM> getDependencies(boolean excludeOptionals, boolean downloadAutomatically, String... excludeScopes) throws IOException, ParserConfigurationException, SAXException {
		Set<POM> set = new TreeSet<POM>();
		getDependencies(set, excludeOptionals, downloadAutomatically, excludeScopes);
		return set;
	}

	public void getDependencies(Set<POM> result, boolean excludeOptionals, boolean downloadAutomatically, String... excludeScopes) throws IOException, ParserConfigurationException, SAXException {
		for (Coordinate dependency : dependencies) {
			if (excludeOptionals && dependency.optional)
				continue;
			String scope = expand(dependency.scope);
			if (scope != null && excludeScopes != null && arrayContainsString(excludeScopes, scope))
				continue;
			Coordinate expanded = expand(dependency);
			POM pom = findPOM(expanded, !env.verbose, false);
			String systemPath = expand(dependency.systemPath);
			if (pom == null && systemPath != null) {
				File file = new File(systemPath);
				if (file.exists()) {
					result.add(env.fakePOM(file, expanded));
					continue;
				}
			}
			// make sure that snapshot .pom files are updated once a day
			if (!env.offlineMode && downloadAutomatically && pom != null && dependency.version != null &&
					(dependency.version.startsWith("[") || dependency.version.endsWith("-SNAPSHOT")) &&
					pom.directory.getPath().startsWith(BuildEnvironment.mavenRepository.getPath())) {
				if (maybeDownloadAutomatically(dependency, !env.verbose, downloadAutomatically)) {
					if (dependency.version.startsWith("["))
						dependency.setSnapshotVersion(VersionPOMHandler.parse(new File(pom.directory.getParentFile(), "maven-metadata-version.xml")));
					else
						dependency.setSnapshotVersion(SnapshotPOMHandler.parse(new File(pom.directory, "maven-metadata-snapshot.xml")));
				}
			}
			if (pom == null && downloadAutomatically)
				pom = findPOM(expanded, !env.verbose, downloadAutomatically);
			if (pom == null || result.contains(pom))
				continue;
			result.add(pom);
			pom.getDependencies(result, env.downloadAutomatically, excludeOptionals, excludeScopes);
		}
	}

	protected boolean arrayContainsString(String[] array, String key) {
		for (String string : array)
			if (string.equals(key))
				return true;
		return false;
	}

	// expands ${<property-name>}
	public Coordinate expand(Coordinate dependency) {
		boolean optional = dependency.optional;
		String scope = expand(dependency.scope);
		String groupId = expand(dependency.groupId);
		String artifactId = expand(dependency.artifactId);
		String version = expand(dependency.version);
		String classifier = expand(dependency.classifier);
		String systemPath = expand(dependency.systemPath);
		return new Coordinate(groupId, artifactId, version, scope, optional, systemPath, classifier);
	}

	public String expand(String string) {
		if (string == null)
			return null;
		for (;;) {
			int dollarCurly = string.indexOf("${");
			if (dollarCurly < 0)
				return string;
			int endCurly = string.indexOf("}", dollarCurly + 2);
			if (endCurly < 0)
				throw new RuntimeException("Invalid string: " + string);
			String property = getProperty(string.substring(dollarCurly + 2, endCurly));
			if (property == null) {
				if (dollarCurly == 0 && endCurly == string.length() - 1)
					return null;
				property = "";
			}
			string = string.substring(0, dollarCurly)
				+ property
				+ string.substring(endCurly + 1);
		}
	}

	public String getProperty(String key) {
		if (properties.containsKey(key))
			return properties.get(key);
		if (key.equals("project.basedir"))
			return directory.getPath();
		if (parent == null) {
			// hard-code a few variables
			if (key.equals("bio-formats.groupId"))
				return "loci";
			if (key.equals("bio-formats.version"))
				return "4.4-SNAPSHOT";
			if (key.equals("imagej.groupId"))
				return "imagej";
			if (key.equals("java.home"))
				return System.getProperty("java.home");
			return null;
		}
		return parent.getProperty(key);
	}

	public POM[] getChildren() {
		if (children == null)
			return new POM[0];
		return children;
	}

	public POM getRoot() {
		POM result = this;
		while (result.parent != null)
			result = result.parent;
		return result;
	}

	protected Set<String> getRepositories() {
		Set<String> result = new TreeSet<String>();
		getRepositories(result);
		return result;
	}

	protected void getRepositories(Set<String> result) {
		// add a default to the root
		if (parent == null)
			result.add("http://repo1.maven.org/maven2/");
		result.addAll(repositories);
		for (POM child : getChildren())
			if (child != null)
				child.getRepositories(result);
	}

	public POM findPOM(Coordinate dependency, boolean quiet, boolean downloadAutomatically) throws IOException, ParserConfigurationException, SAXException {
		if (dependency.version == null && "aopalliance".equals(dependency.artifactId))
			dependency.version = "1.0";
		if (dependency.version == null && "provided".equals(dependency.scope))
			return null;
		if (dependency.artifactId.equals(expand(coordinate.artifactId)) &&
				dependency.groupId.equals(expand(coordinate.groupId)) &&
				dependency.version.equals(expand(coordinate.version)))
			return this;
		// fall back to Fiji's modules/, $HOME/.m2/repository/ and Fiji's jars/ and plugins/ directories
		String key = dependency.getKey();
		if (env.localPOMCache.containsKey(key)) {
			POM result = env.localPOMCache.get(key); // may be null
			if (result == null || BuildEnvironment.compareVersion(dependency.getVersion(), result.coordinate.getVersion()) <= 0)
				return result;
		}

		POM pom = findInMultiProjects(dependency);
		if (pom != null)
			return pom;

		if (env.ignoreMavenRepositories) {
			File file = findInFijiDirectories(dependency);
			if (file != null)
				return env.fakePOM(file, dependency);
			if (!quiet && !dependency.optional)
				env.err.println("Skipping artifact " + dependency.artifactId + " (for " + coordinate.artifactId + "): not in jars/ nor plugins/");
			return cacheAndReturn(key, null);
		}

		String path = BuildEnvironment.mavenRepository.getPath() + "/" + dependency.groupId.replace('.', '/') + "/" + dependency.artifactId + "/";
		if (dependency.version == null) {
			env.err.println("Skipping invalid dependency (version unset): " + dependency);
			return null;
		}
		if (dependency.version.startsWith("[") && dependency.snapshotVersion == null) try {
			if (!maybeDownloadAutomatically(dependency, quiet, downloadAutomatically))
				return null;
			if (dependency.version.startsWith("["))
				dependency.snapshotVersion = VersionPOMHandler.parse(new File(path, "maven-metadata-version.xml"));
		} catch (FileNotFoundException e) { /* ignore */ }
		path += dependency.getVersion() + "/";
		if (dependency.version.endsWith("-SNAPSHOT")) try {
			if (!maybeDownloadAutomatically(dependency, quiet, downloadAutomatically)) {
				File file = findInFijiDirectories(dependency);
				if (file != null)
					return env.fakePOM(file, dependency);
				return null;
			}
			if (dependency.version.endsWith("-SNAPSHOT"))
				dependency.setSnapshotVersion(SnapshotPOMHandler.parse(new File(path, "maven-metadata-snapshot.xml")));
		} catch (FileNotFoundException e) { /* ignore */ }
		else if (env.ignoreMavenRepositories) {
			File file = findInFijiDirectories(dependency);
			if (file != null)
				return env.fakePOM(file, dependency);
		}

		File file = new File(path, dependency.getPOMName());
		if (!file.exists()) {
			if (downloadAutomatically) {
				if (!maybeDownloadAutomatically(dependency, quiet, downloadAutomatically))
					return null;
			}
			else {
				if (!quiet && !dependency.optional)
					env.err.println("Skipping artifact " + dependency.artifactId + " (for " + coordinate.artifactId + "): not found");
				if (!downloadAutomatically && env.downloadAutomatically)
					return null;
				return cacheAndReturn(key, null);
			}
		}

		POM result = env.parse(new File(path, dependency.getPOMName()), null, dependency.classifier);
		if (result != null) {
			if (result.target.getName().endsWith("-SNAPSHOT.jar")) {
				result.coordinate.version = dependency.version;
				result.target = new File(result.directory, dependency.getJarName());
			}
			if (result.parent == null)
				result.parent = getRoot();
			if (result.packaging.equals("jar") && !new File(path, dependency.getJarName()).exists()) {
				if (downloadAutomatically)
					download(dependency, quiet);
				else {
					env.localPOMCache.remove(key);
					return null;
				}
			}
		}
		else if (!quiet && !dependency.optional)
			env.err.println("Artifact " + dependency.artifactId + " not found" + (downloadAutomatically ? "" : "; consider 'get-dependencies'"));
		return result;
	}

	protected POM findInMultiProjects(Coordinate dependency) throws IOException, ParserConfigurationException, SAXException {
		env.parseMultiProjects();
		String key = dependency.getKey();
		POM result = env.localPOMCache.get(key);
		if (result != null && BuildEnvironment.compareVersion(dependency.getVersion(), result.coordinate.getVersion()) <= 0)
			return result;
		return null;
	}

	protected File findInFijiDirectories(Coordinate dependency) {
		for (String jarName : new String[] {
			"jars/" + dependency.artifactId + "-" + dependency.getVersion() + ".jar",
			"plugins/" + dependency.artifactId + "-" + dependency.getVersion() + ".jar",
			"jars/" + dependency.artifactId + ".jar",
			"plugins/" + dependency.artifactId + ".jar"
		}) {
			File file = new File(System.getProperty("ij.dir"), jarName);
			if (file.exists())
				return file;
		}
		return null;
	}

	protected POM cacheAndReturn(String key, POM pom) {
		env.localPOMCache.put(key, pom);
		return pom;
	}

	protected boolean maybeDownloadAutomatically(Coordinate dependency, boolean quiet, boolean downloadAutomatically) {
		if (!downloadAutomatically || env.offlineMode)
			return true;
		try {
			download(dependency, quiet);
		} catch (Exception e) {
			if (!quiet && !dependency.optional) {
				e.printStackTrace(env.err);
				env.err.println("Could not download " + dependency.artifactId + ": " + e.getMessage());
			}
			String key = dependency.getKey();
			env.localPOMCache.put(key, null);
			return false;
		}
		return true;
	}

	protected String findLocallyCachedVersion(String path) throws IOException {
		File file = new File(path, "maven-metadata-local.xml");
		if (!file.exists()) {
			String[] list = new File(path).list();
			return list != null && list.length > 0 ? list[0] : null;
		}
		BufferedReader reader = new BufferedReader(new FileReader(file));
		for (;;) {
			String line = reader.readLine();
			if (line == null)
				throw new RuntimeException("Could not determine version for " + path);
			int tag = line.indexOf("<version>");
			if (tag < 0)
				continue;
			reader.close();
			int endTag = line.indexOf("</version>");
			return line.substring(tag + "<version>".length(), endTag);
		}
	}

	// XML parsing

	public void startDocument() {}

	public void endDocument() {
		if (!properties.containsKey("project.groupId"))
			properties.put("project.groupId", coordinate.groupId);
		if (!properties.containsKey("project.version"))
			properties.put("project.version", coordinate.getVersion());
	}

	public void startElement(String uri, String name, String qualifiedName, Attributes attributes) {
		prefix += ">" + qualifiedName;
		if (env.debug)
			env.err.println("start(" + uri + ", " + name + ", " + qualifiedName + ", " + toString(attributes) + ")");
	}

	public void endElement(String uri, String name, String qualifiedName) {
		if (prefix.equals(">project>dependencies>dependency") || (isCurrentProfile && prefix.equals(">project>profiles>profile>dependencies>dependency"))) {
			if (env.debug)
				env.err.println("Adding dependendency " + latestDependency + " to " + this);
			if (coordinate.artifactId.equals("javassist") && latestDependency.artifactId.equals("tools"))
				latestDependency.optional = false;
			dependencies.add(latestDependency);
			latestDependency = new Coordinate();
		}
		if (prefix.equals(">project>profiles>profile"))
			isCurrentProfile = false;
		prefix = prefix.substring(0, prefix.length() - 1 - qualifiedName.length());
		if (env.debug)
			env.err.println("end(" + uri + ", " + name + ", " + qualifiedName + ")");
	}

	public void characters(char[] buffer, int offset, int length) {
		String string = new String(buffer, offset, length);
		if (env.debug)
			env.err.println("characters: " + string + " (prefix: " + prefix + ")");

		String prefix = this.prefix;
		if (isCurrentProfile)
			prefix = ">project" + prefix.substring(">project>profiles>profile".length());

		if (prefix.equals(">project>groupId"))
			coordinate.groupId = string;
		else if (prefix.equals(">project>artifactId"))
			coordinate.artifactId = string;
		else if (prefix.equals(">project>version"))
			coordinate.version = string;
		else if (prefix.equals(">project>packaging"))
			packaging = string;
		else if (prefix.equals(">project>modules"))
			buildFromSource = true; // might not be building a target
		else if (prefix.equals(">project>modules>module"))
			modules.add(string);
		else if (prefix.startsWith(">project>properties>"))
			properties.put(prefix.substring(">project>properties>".length()), string);
		else if (prefix.equals(">project>dependencies>dependency>groupId"))
			latestDependency.groupId = string;
		else if (prefix.equals(">project>dependencies>dependency>artifactId"))
			latestDependency.artifactId = string;
		else if (prefix.equals(">project>dependencies>dependency>version"))
			latestDependency.version = string;
		else if (prefix.equals(">project>dependencies>dependency>scope"))
			latestDependency.scope = string;
		else if (prefix.equals(">project>dependencies>dependency>optional"))
			latestDependency.optional = string.equalsIgnoreCase("true");
		else if (prefix.equals(">project>dependencies>dependency>systemPath"))
			latestDependency.systemPath = string;
		else if (prefix.equals(">project>dependencies>dependency>classifier"))
			latestDependency.classifier = string;
		else if (prefix.equals(">project>profiles>profile>id")) {
			isCurrentProfile = (!System.getProperty("os.name").equals("Mac OS X") && "javac".equals(string)) || (coordinate.artifactId.equals("javassist") && (string.equals("jdk16") || string.equals("default-tools")));
			if (env.debug)
				env.err.println((isCurrentProfile ? "Activating" : "Ignoring") + " profile " + string);
		}
		else if (!isCurrentProfile && prefix.equals(">project>profiles>profile>activation>file>exists"))
			isCurrentProfile = new File(directory, string).exists();
		else if (!isCurrentProfile && prefix.equals(">project>profiles>profile>activation>activeByDefault"))
			isCurrentProfile = "true".equalsIgnoreCase(string);
		else if (!isCurrentProfile && prefix.equals(">project>profiles>profile>activation>property>name")) {
			boolean negate = false;
			if (string.startsWith("!")) {
				negate = true;
				string = string.substring(1);
			}
			isCurrentProfile = negate ^ (expand("${" + string + "}") != null);
		}
		else if (prefix.equals(">project>repositories>repository>url"))
			repositories.add(string);
		else if (prefix.equals(">project>build>sourceDirectory"))
			sourceDirectory = string;
		else if (prefix.startsWith(">project>parent>")) {
			if (parentCoordinate == null)
				parentCoordinate = new Coordinate();
			if (prefix.equals(">project>parent>groupId")) {
				if (coordinate.groupId == null)
					coordinate.groupId = string;
				if (parentCoordinate.groupId == null)
					parentCoordinate.groupId = string;
				else
					checkParentTag("groupId", parentCoordinate.groupId, string);
			}
			else if (prefix.equals(">project>parent>artifactId")) {
				if (parentCoordinate.artifactId == null)
					parentCoordinate.artifactId = string;
				else
					checkParentTag("artifactId", parentCoordinate.artifactId, string);
			}
			else if (prefix.equals(">project>parent>version")) {
				if (coordinate.version == null)
					coordinate.version = string;
				if (parentCoordinate.version == null)
					parentCoordinate.version = string;
				else
					checkParentTag("version", parentCoordinate.version, string);
			}
		}
		else if (prefix.equals(">project>build>plugins>plugin>artifactId")) {
			currentPluginName = string;
			if (string.equals("buildnumber-maven-plugin"))
				includeImplementationBuild = true;
		}
		else if (prefix.equals(">project>build>plugins>plugin>configuration>source") && "maven-compiler-plugin".equals(currentPluginName))
			sourceVersion = string;
		else if (prefix.equals(">project>build>plugins>plugin>configuration>target") && "maven-compiler-plugin".equals(currentPluginName))
			targetVersion = string;
		else if (prefix.equals(">project>build>plugins>plugin>configuration>archive>manifest>mainClass") && "maven-jar-plugin".equals(currentPluginName))
			mainClass = string;
		/* This would be needed to compile clojure.jar. However, it does not work because we do not support the antrun plugin
		else if (prefix.equals(">project>build>plugins>plugin>executions>execution>configuration>sources>source") && "build-helper-maven-plugin".equals(currentPluginName))
			sourceDirectory = string;
		*/
		else if (env.debug)
			env.err.println("Ignoring " + prefix);
	}

	protected void checkParentTag(String tag, String string1, String string2) {
		String expanded1 = expand(string1);
		String expanded2 = expand(string2);
		if (((expanded1 == null || expanded2 == null) && expanded1 != expanded2) || !expanded1.equals(expanded2))
			env.err.println("Warning: " + tag + " mismatch in " + directory + "'s parent: " + string1 + " != " + string2);
	}

	public String toString(Attributes attributes) {
		StringBuilder builder = new StringBuilder();
		builder.append("[ ");
		for (int i = 0; i < attributes.getLength(); i++)
			builder.append(attributes.getQName(i))
				. append("='").append(attributes.getValue(i))
				. append("' ");
		builder.append("]");
		return builder.toString();
	}

	public int compareTo(POM other) {
		int result = coordinate.artifactId.compareTo(other.coordinate.artifactId);
		if (result != 0)
			return result;
		if (coordinate.groupId != null && other.coordinate.groupId != null)
			result = coordinate.groupId.compareTo(other.coordinate.groupId);
		if (result != 0)
			return result;
		return BuildEnvironment.compareVersion(coordinate.getVersion(), other.coordinate.getVersion());
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		append(builder, "");
		return builder.toString();
	}

	public void append(StringBuilder builder, String indent) {
		builder.append(indent + coordinate.getKey() + "\n");
		if (children != null)
			for (POM child : getChildren())
				if (child == null)
					builder.append(indent).append("  (null)\n");
				else
					child.append(builder, indent + "  ");
	}
}
