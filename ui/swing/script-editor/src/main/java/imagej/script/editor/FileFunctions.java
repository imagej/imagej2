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

package imagej.script.editor;

import imagej.command.CommandModule;
import imagej.script.editor.command.NewPlugin;
import imagej.util.AppUtils;
import imagej.util.LineOutputStream;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.scijava.util.ProcessUtils;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;

/**
 * TODO
 * 
 * @author Johannes Schindelin
 */
public class FileFunctions {

	protected static File imagejRoot = AppUtils.getBaseDirectory();

	protected TextEditor parent;

	public FileFunctions(TextEditor parent) {
		this.parent = parent;
	}

	public List<File> extractSourceJar(final File jarFile) throws IOException {
		String baseName = jarFile.getName();
		if (baseName.endsWith(".jar") || baseName.endsWith(".zip")) baseName =
			baseName.substring(0, baseName.length() - 4);
		File baseDirectory = new File(imagejRoot, "src/" + baseName);

		List<File> result = new ArrayList<File>();
		boolean foundPOM = false;
		JarFile jar = new JarFile(jarFile);
		for (JarEntry entry : Collections.list(jar.entries())) {
			String name = entry.getName();
			if (name.endsWith(".class") || name.endsWith("/") || name.startsWith("META-INF/") || name.endsWith(".DS_Store")) continue;
			File destination = new File(baseDirectory, "src/main/"
				+ (name.endsWith(".java") ? "java/" : "resources/") + name);
			if ("pom.xml".equals(name)) {
				foundPOM = true;
				destination = new File(baseDirectory, name);
			}
			copyTo(jar.getInputStream(entry), destination);
			result.add(destination);
		}
		if (!foundPOM) {
			final File pom = new File(baseDirectory, "pom.xml");
			try {
				copyTo(fakePOM(baseDirectory, null), pom);
			} catch (Exception e) {
				throw new IOException(e);
			}
			result.add(pom);
		}
		return result;
	}

	private final static String XALAN_INDENT_AMOUNT = "{http://xml.apache.org/xslt}indent-amount";

	private InputStream fakePOM(final File baseDirectory, final String mainClass) throws ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException {
		final String artifactId = baseDirectory.getName();

		final org.w3c.dom.Document pom = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		final Element project = pom.createElement("project");
		pom.appendChild(project);
		project.setAttribute("xmlns", "http://maven.apache.org/POM/4.0.0");
		project.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		project.setAttribute("xsi:schemaLocation", "http://maven.apache.org/POM/4.0.0 " +
				"http://maven.apache.org/xsd/maven-4.0.0.xsd");

		append(pom, project, "modelVersion", "4.0.0");

		final Element parent = append(pom, project, "parent", null);
		append(pom, parent, "groupId", "org.scijava");
		append(pom, parent, "artifactId", "pom-scijava");
		append(pom, parent, "version", "1.28");

		append(pom, project, "groupId", "net.imagej");
		append(pom, project, "artifactId", artifactId);
		append(pom, project, "version", "1.0.0-SNAPSHOT");

		final Element build = append(pom, project, "build", null);
		append(pom, build, "sourceDirectory", baseDirectory.getPath() + "/src");

		if (mainClass != null) {
			final Element plugins = append(pom, build, "plugins", null);
			final Element plugin = append(pom, plugins, "plugin", null);
			append(pom, plugin, "artifactId", "maven-jar-plugin");
			final Element configuration = append(pom, plugin, "configuration", null);
			final Element archive = append(pom, configuration, "archive", null);
			final Element manifest = append(pom, archive, "manifest", null);
			append(pom, manifest, "mainClass", mainClass);
		}

		Element dependencies = append(pom, project, "dependencies", null);
		/*
		for (Coordinate dependency : getAllDependencies(env)) {
			Element dep = append(pom, dependencies, "dependency", null);
			append(pom, dep, "groupId", dependency.getGroupId());
			append(pom, dep, "artifactId", dependency.getArtifactId());
			append(pom, dep, "version", dependency.getVersion());
		}
		*/

        project.appendChild(pom.createComment("NB: for project parent"));
		final Element repositories = append(pom, project, "repositories", null);
		final Element releases = append(pom, repositories, "repository", null);
		append(pom, releases, "id", "imagej.releases");
		append(pom, releases, "url", "http://maven.imagej.net/content/repositories/releases");
		final Element snapshots = append(pom, repositories, "repository", null);
		append(pom, snapshots, "id", "imagej.snapshots");
		append(pom, snapshots, "url", "http://maven.imagej.net/content/repositories/snapshots");

		final Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(XALAN_INDENT_AMOUNT, "4");
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		transformer.transform(new DOMSource(pom), new StreamResult(out));
		return new ByteArrayInputStream(out.toByteArray());
	}

	private static Element append(final org.w3c.dom.Document document, final Element parent, final String tag, final String content) {
		Element child = document.createElement(tag);
		if (content != null) child.setTextContent(content); //child.appendChild(document.createCDATASection(content));
		parent.appendChild(child);
		return child;
	}

	protected void copyTo(final InputStream in, final File file)
			throws IOException {
		makeParentDirectories(file);
		copyTo(in, new FileOutputStream(file));
	}

	protected void copyTo(InputStream in, OutputStream out)
			throws IOException {
		byte[] buffer = new byte[16384];
		for (;;) {
			int count = in.read(buffer);
			if (count < 0)
				break;
			out.write(buffer, 0, count);
		}
		in.close();
		out.close();
	}

	protected void makeParentDirectories(File file) {
		File parent = file.getParentFile();
		if (!parent.exists()) {
			makeParentDirectories(parent);
			parent.mkdir();
		}
	}

	/*
	 * This just checks for a NUL in the first 1024 bytes.
	 * Not the best test, but a pragmatic one.
	 */
	public boolean isBinaryFile(final File file) {
		try {
			InputStream in = new FileInputStream(file);
			byte[] buffer = new byte[1024];
			int offset = 0;
			while (offset < buffer.length) {
				int count = in.read(buffer, offset, buffer.length - offset);
				if (count < 0)
					break;
				else
					offset += count;
			}
			in.close();
			while (offset > 0)
				if (buffer[--offset] == 0)
					return true;
		} catch (IOException e) { }
		return false;
	}

	/**
	 * Make a sensible effort to get the path of the source for a class.
	 */
	public String getSourcePath(String className) throws ClassNotFoundException {
		// move updater's stuff into ij-core and re-use here
		throw new RuntimeException("TODO");
	}

	public String getSourceURL(String className) {
		return "http://fiji.sc/" + className.replace('.', '/') + ".java";
	}

	public String getJar(String className) {
		try {
			Class clazz = Class.forName(className);
			String baseName = className;
			int dot = baseName.lastIndexOf('.');
			if (dot > 0)
				baseName = baseName.substring(dot + 1);
			baseName += ".class";
			String url = clazz.getResource(baseName).toString();
			int dotJar = url.indexOf("!/");
			if (dotJar < 0)
				return null;
			int offset = url.startsWith("jar:file:") ? 9 : 0;
			return url.substring(offset, dotJar);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	protected static Map<String, List<String>> class2source;

	public String findSourcePath(String className) {
		if (class2source == null) {
			if (JOptionPane.showConfirmDialog(parent,
					"The class " + className + " was not found "
					+ "in the CLASSPATH. Do you want me to search "
					+ "for the source?",
					"Question", JOptionPane.YES_OPTION)
					!= JOptionPane.YES_OPTION)
				return null;
			class2source = new HashMap<String, List<String>>();
			findJavaPaths(imagejRoot, "");
		}
		int dot = className.lastIndexOf('.');
		String baseName = className.substring(dot + 1);
		List<String> paths = class2source.get(baseName);
		if (paths == null || paths.size() == 0) {
			JOptionPane.showMessageDialog(parent, "No source for class '"
					+ className + "' was not found!");
			return null;
		}
		if (dot >= 0) {
			String suffix = "/" + className.replace('.', '/') + ".java";
			paths = new ArrayList<String>(paths);
			Iterator<String> iter = paths.iterator();
			while (iter.hasNext())
				if (!iter.next().endsWith(suffix))
					iter.remove();
			if (paths.size() == 0) {
				JOptionPane.showMessageDialog(parent, "No source for class '"
						+ className + "' was not found!");
				return null;
			}
		}
		if (paths.size() == 1)
			return new File(imagejRoot, paths.get(0)).getAbsolutePath();
		String[] names = paths.toArray(new String[paths.size()]);
		JFileChooser chooser = new JFileChooser(imagejRoot);
		chooser.setDialogTitle("Choose path");
		if (chooser.showOpenDialog(parent) !=  JFileChooser.APPROVE_OPTION) return null;
		return chooser.getSelectedFile().getPath();
	}

	protected void findJavaPaths(File directory, String prefix) {
		String[] files = directory.list();
		if (files == null)
			return;
		Arrays.sort(files);
		for (int i = 0; i < files.length; i++)
			if (files[i].endsWith(".java")) {
				String baseName = files[i].substring(0, files[i].length() - 5);
				List<String> list = class2source.get(baseName);
				if (list == null) {
					list = new ArrayList<String>();
					class2source.put(baseName, list);
				}
				list.add(prefix + "/" + files[i]);
			}
			else if ("".equals(prefix) &&
					(files[i].equals("full-nightly-build") ||
					 files[i].equals("livecd") ||
					 files[i].equals("java") ||
					 files[i].equals("nightly-build") ||
					 files[i].equals("other") ||
					 files[i].equals("work") ||
					 files[i].startsWith("chroot-")))
				// skip known non-source directories
				continue;
			else {
				File file = new File(directory, files[i]);
				if (file.isDirectory())
					findJavaPaths(file, prefix + "/" + files[i]);
			}
	}

	public boolean newPlugin() {
		Future<CommandModule> result =
			parent.commandService.run(NewPlugin.class, "editor", parent);
		try {
			result.get();
			return true;
		} catch (Throwable t) {
			parent.handleException(t);
			return false;
		}
	}

	public boolean newPlugin(String name) {
		String originalName = name.replace('_', ' ');

		name = name.replace(' ', '_');
		if (name.indexOf('_') < 0)
			name += "_";

		final File file =
			new File(imagejRoot, "src-plugins/" + name + "/" +
				name + ".java");
		final File dir = file.getParentFile();
		if ((!dir.exists() && !dir.mkdirs()) || !dir.isDirectory()) return error("Could not make directory '" +
			dir.getAbsolutePath() + "'");

		String jar = "plugins/" + name + ".jar";
		addToGitignore(jar);
		addPluginJarToFakefile(jar);

		File pluginsConfig = new File(dir, "plugins.config");
		parent.open(pluginsConfig);
		if (parent.getEditorPane().getDocument().getLength() == 0)
			parent.getEditorPane().insert(
				"# " + originalName + "\n"
				+ "\n"
				+ "# Author: \n"
				+ "\n"
				+ "Plugins, \"" + originalName + "\", " + name + "\n", 0);
		parent.open(file);
		if (parent.getEditorPane().getDocument().getLength() == 0)
			parent.getEditorPane().insert(
				"import ij.ImagePlus;\n"
				+ "\n"
				+ "import ij.plugin.filter.PlugInFilter;\n"
				+ "\n"
				+ "import ij.process.ImageProcessor;\n"
				+ "\n"
				+ "public class " + name + " implements PlugInFilter {\n"
				+ "\tprotected ImagePlus image;\n"
				+ "\n"
				+ "\tpublic int setup(String arg, ImagePlus image) {\n"
				+ "\t\tthis.image = image;\n"
				+ "\t\treturn DOES_ALL;\n"
				+ "\t}\n"
				+ "\n"
				+ "\tpublic void run(ImageProcessor ip) {\n"
				+ "\t\t// Do something\n"
				+ "\t}\n"
				+ "}", 0);
		return true;
	}

	public boolean addToGitignore(String name) {
		if (!name.startsWith("/"))
			name = "/" + name;
		if (!name.endsWith("\n"))
			name += "\n";

		final File file = new File(imagejRoot, ".gitignore");
		if (!file.exists()) return false;

		try {
			String content = readStream(new FileInputStream(file));
			if (content.startsWith(name) || content.indexOf("\n" + name) >= 0)
				return false;

			FileOutputStream out = new FileOutputStream(file, true);
			if (!content.endsWith("\n"))
				out.write("\n".getBytes());
			out.write(name.getBytes());
			out.close();
			return true;
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			return error("Failure writing " + file);
		}
	}

	public boolean addPluginJarToFakefile(final String name) {
		final File file = new File(imagejRoot, "Fakefile");
		if (!file.exists()) return false;

		try {
			String content = readStream(new FileInputStream(file));

			// insert plugin target
			int start = content.indexOf("\nPLUGIN_TARGETS=");
			if (start < 0)
				return false;
			int end = content.indexOf("\n\n", start);
			if (end < 0)
				end = content.length();
			int offset = content.indexOf("\n\t" + name, start);
			if (offset < end && offset > start)
				return false;
			String insert = "\n\t" + name;
			if (content.charAt(end - 1) != '\\')
				insert = " \\" + insert;
			content = content.substring(0, end) + insert + content.substring(end);

			// insert classpath
			offset = content.lastIndexOf("\nCLASSPATH(");
			while (offset > 0 &&
					(content.substring(offset).startsWith("\nCLASSPATH(jars/test-fiji.jar)") ||
					content.substring(offset).startsWith("\nCLASSPATH(plugins/FFMPEG")))
				offset = content.lastIndexOf("\nCLASSPATH(", offset - 1);
			if (offset < 0)
				return false;
			offset = content.indexOf('\n', offset + 1);
			if (offset < 0)
				return false;
			content = content.substring(0, offset) + "\nCLASSPATH(" + name + ")=jars/ij.jar" + content.substring(offset);

			FileOutputStream out = new FileOutputStream(file);
			out.write(content.getBytes());
			out.close();

			return true;
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			return error("Failure writing " + file);
		}
	}

	protected String readStream(InputStream in) throws IOException {
		StringBuffer buf = new StringBuffer();
		byte[] buffer = new byte[65536];
		for (;;) {
			int count = in.read(buffer);
			if (count < 0)
				break;
			buf.append(new String(buffer, 0, count));
		}
		in.close();
		return buf.toString();
	}

	/**
	 * Get a list of files from a directory (recursively)
	 */
	public void listFilesRecursively(File directory, String prefix, List<String> result) {
		if (!directory.exists())
			return;
		for (File file : directory.listFiles())
			if (file.isDirectory())
				listFilesRecursively(file, prefix + file.getName() + "/", result);
			else if (file.isFile())
				result.add(prefix + file.getName());
	}

	/**
	 * Get a list of files from a directory or within a .jar file
	 *
	 * The returned items will only have the base path, to get at the
	 * full URL you have to prefix the url passed to the function.
	 */
	public List<String> getResourceList(String url) {
		List<String> result = new ArrayList<String>();

		if (url.startsWith("jar:")) {
			int bang = url.indexOf("!/");
			String jarURL = url.substring(4, bang);
			if (jarURL.startsWith("file:"))
				jarURL = jarURL.substring(5);
			String prefix = url.substring(bang + 2);
			int prefixLength = prefix.length();

			try {
				JarFile jar = new JarFile(jarURL);
				Enumeration<JarEntry> e = jar.entries();
				while (e.hasMoreElements()) {
					JarEntry entry = e.nextElement();
					if (entry.getName().startsWith(prefix))
						result.add(entry.getName().substring(prefixLength));
				}
			} catch (IOException e) {
				parent.handleException(e);
			}
		}
		else {
			String prefix = "file:";
			if (url.startsWith(prefix)) {
				int skip = prefix.length();
				if (url.startsWith(prefix + "//")) skip++;
				url = url.substring(skip);
			}
			listFilesRecursively(new File(url), "", result);
		}
		return result;
	}

	public File getGitDirectory(File file) {
		if (file == null)
			return null;
		for (;;) {
			file = file.getParentFile();
			if (file == null)
				return null;
			File git = new File(file, ".git");
			if (git.isDirectory())
				return git;
		}
	}

	public File getPluginRootDirectory(File file) {
		if (file == null)
			return null;
		if (!file.isDirectory())
			file = file.getParentFile();
		if (file == null)
			return null;

		File git = new File(file, ".git");
		if (git.isDirectory())
			return file;

		File backup = file;
		for (;;) {
			File parent = file.getParentFile();
			if (parent == null)
				return null;
			git = new File(parent, ".git");
			if (git.isDirectory())
				return file.getName().equals("src-plugins") ?
					backup : file;
			backup = file;
			file = parent;
		}
	}

	public String firstNLines(String text, int maxLineCount) {
		int offset = -1;
		while (maxLineCount-- > 0) {
			offset = text.indexOf('\n', offset + 1);
			if (offset < 0)
				return text;
		}
		int count = 0, next = offset;
		while ((next = text.indexOf('\n', next + 1)) > 0)
			count++;
		return count == 0 ? text : text.substring(0, offset + 1)
			+ "(" + count + " more line" + (count > 1 ? "s" : "") + ")...\n";
	}

	public class LengthWarner implements DocumentListener {
		protected int width;
		protected JTextComponent component;
		protected Color normal, warn;

		public LengthWarner(int width, JTextComponent component) {
			this.width = width;
			this.component = component;
			normal = component.getForeground();
			warn = Color.red;
		}

		public void changedUpdate(DocumentEvent e) { }

		public void insertUpdate(DocumentEvent e) {
			updateColor();
		}

		public void removeUpdate(DocumentEvent e) {
			updateColor();
		}

		public void updateColor() {
			component.setForeground(component.getDocument().getLength() <= width ? normal : warn);
		}
	}

	public class TextWrapper implements DocumentListener {
		protected int width;

		public TextWrapper(int width) {
			this.width = width;
		}

		public void changedUpdate(DocumentEvent e) { }
		public void insertUpdate(DocumentEvent e) {
			final Document document = e.getDocument();
			int offset = e.getOffset() + e.getLength();
			if (offset <= width)
				return;
			try {
				String text = document.getText(0, offset);
				int newLine = text.lastIndexOf('\n');
				if (offset - newLine <= width)
					return;
				int additional = 0;
				while (offset - newLine > width) {
					int remove = 0;
					int space = text.lastIndexOf(' ', newLine + width);
					if (space < newLine)
						break;
					if (space > 0) {
						int first = space;
						while (first > newLine + 1 && text.charAt(first - 1) == ' ')
							first--;
						remove = space + 1 - first;
						newLine = first;
					}
					else
						newLine += width;

					final int removeCount = remove, at = newLine;
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							try {
								if (removeCount > 0)
									document.remove(at, removeCount);
								document.insertString(at, "\n", null);
							} catch (BadLocationException e2) { /* ignore */ }
						}
					});
				}
			} catch (BadLocationException e2) { /* ignore */ }
		}
		public void removeUpdate(DocumentEvent e) { }
	}

	public class ScreenOutputStream extends LineOutputStream {
		@Override
		public void println(String line) {
			TextEditor.Tab tab = parent.getTab();
			tab.screen.insert(line + "\n", tab.screen.getDocument().getLength());
		}
	}

	public static class GrepLineHandler extends LineOutputStream {
		protected static Pattern pattern = Pattern.compile("([A-Za-z]:[^:]*|[^:]+):([1-9][0-9]*):.*", Pattern.DOTALL);

		public ErrorHandler errorHandler;
		protected String directory;

		public GrepLineHandler(JTextArea textArea, String directory) {
			errorHandler = new ErrorHandler(textArea);
			if (!directory.endsWith("/"))
				directory += "/";
			this.directory = directory;
		}

		@Override
		public void println(String line) {
			Matcher matcher = pattern.matcher(line);
			if (matcher.matches())
				errorHandler.addError(directory + matcher.group(1), Integer.parseInt(matcher.group(2)), line);
			else
				errorHandler.addError(null, -1, line);
		}
	}

	public void gitGrep(String searchTerm, File directory) {
		GrepLineHandler handler = new GrepLineHandler(parent.errorScreen, directory.getAbsolutePath());
		PrintStream out = new PrintStream(handler);
		parent.getTab().showErrors();
		try {
			ProcessUtils.exec(directory, out, out, "git", "grep", "-n", searchTerm);
			parent.errorHandler = handler.errorHandler;
		} catch (RuntimeException e) {
			parent.handleException(e);
		}
	}

	public void openInGitweb(File file, File gitDirectory, int line) {
		if (file == null || gitDirectory == null) {
			error("No file or git directory");
			return;
		}
		String url = getGitwebURL(file, gitDirectory, line);
		if (url == null)
			error("Could not get gitweb URL for " + file);
		else try {
			parent.platformService.open(new URL(url));
		} catch (MalformedURLException e) {
			parent.handleException(e);
		} catch (IOException e) {
			parent.handleException(e);
		}
	}

	public String git(File gitDirectory, File workingDirectory, String... args) {
		try {
			args = append(gitDirectory == null ? new String[] { "git" } :
				new String[] { "git", "--git-dir=" + gitDirectory.getAbsolutePath()}, args);
			PrintStream out = new PrintStream(new ScreenOutputStream());
			return ProcessUtils.exec(workingDirectory, out, out, args);
		} catch (RuntimeException e) {
			parent.write(e.getMessage());
		}
		return null;
	}

	public String git(File gitDirectory, String... args) {
		return git(gitDirectory, (File)null, args);
	}

	public String gitConfig(File gitDirectory, String key) {
		return git(gitDirectory, "config", key);
	}

	public String getGitwebURL(File file, File gitDirectory, int line) {
		String url = gitConfig(gitDirectory, "remote.origin.url");
		if (url == null) {
			String remote = gitConfig(gitDirectory, "branch.master.remote");
			if (remote != null)
				url = gitConfig(gitDirectory, "remote." + remote + ".url");
			if (url == null)
				return null;
		}
		if (url.startsWith("repo.or.cz:") || url.startsWith("ssh://repo.or.cz/")) {
			int index = url.indexOf("/srv/git/") + "/srv/git/".length();
			url = "http://repo.or.cz/w/" + url.substring(index);
		}
		else if (url.startsWith("git://repo.or.cz/"))
			url = "http://repo.or.cz/w/" + url.substring("git://repo.or.cz/".length());
		else {
			url = stripSuffix(url, "/");
			int slash = url.lastIndexOf('/');
			if (url.endsWith("/.git"))
				slash = url.lastIndexOf('/', slash - 1);
			String project = url.substring(slash + 1);
			if (!project.endsWith(".git"))
				project += "/.git";
			if (project.equals("imageja.git"))
				project = "ImageJA.git";
			url = "http://fiji.sc/cgi-bin/gitweb.cgi?p=" + project;
		}
		String head = git(gitDirectory, "rev-parse", "--symbolic-full-name", "HEAD");
		String path = git(null /* ls-files does not work with --git-dir */,
			file.getParentFile(), "ls-files", "--full-name", file.getName());
		if (url == null || head == null || path == null)
			return null;
		return url + ";a=blob;f=" + path + ";hb=" + head
			+ (line < 0 ? "" : "#l" + line);
	}

	protected String[] append(String[] array, String item) {
		String[] result = new String[array.length + 1];
		System.arraycopy(array, 0, result, 0, array.length);
		result[array.length] = item;
		return result;
	}

	protected String[] append(String[] array, String[] append ) {
		String[] result = new String[array.length + append.length];
		System.arraycopy(array, 0, result, 0, array.length);
		System.arraycopy(append, 0, result, array.length, append.length);
		return result;
	}

	protected String stripSuffix(String string, String suffix) {
		if (string.endsWith(suffix))
			return string.substring(0, string.length() - suffix.length());
		return string;
	}

	protected boolean error(String message) {
		JOptionPane.showMessageDialog(parent, message);
		return false;
	}

	public static void main(String[] args) {
		String root = System.getProperty("ij.dir");
		try {
			System.err.println(new FileFunctions(null).getSourcePath("script.imglib.analysis.DoGPeaks"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
