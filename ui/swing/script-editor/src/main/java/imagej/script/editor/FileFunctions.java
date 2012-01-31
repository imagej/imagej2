//
// FileFunctions.java
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

package imagej.script.editor;

import imagej.util.Log;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

public class FileFunctions {

	protected static String ijDir;

	static {
		String dir = System.getProperty("ij.dir");
		if (!dir.endsWith("/")) dir += "/";
		ijDir = dir;
	}

	protected TextEditor parent;

	public FileFunctions(final TextEditor parent) {
		this.parent = parent;
	}

	public List<String> extractSourceJar(final String path) throws IOException {
		String baseName = new File(path).getName();
		if (baseName.endsWith(".jar") || baseName.endsWith(".zip")) baseName =
			baseName.substring(0, baseName.length() - 4);
		final String baseDirectory =
			System.getProperty("ij.dir") + "/src-plugins/" + baseName + "/";

		final List<String> result = new ArrayList<String>();
		final JarFile jar = new JarFile(path);
		for (final JarEntry entry : Collections.list(jar.entries())) {
			final String name = entry.getName();
			if (name.endsWith(".class") || name.endsWith("/")) continue;
			final String destination = baseDirectory + name;
			copyTo(jar.getInputStream(entry), destination);
			result.add(destination);
		}
		return result;
	}

	protected void copyTo(final InputStream in, final String destination)
		throws IOException
	{
		final File file = new File(destination);
		makeParentDirectories(file);
		copyTo(in, new FileOutputStream(file));
	}

	protected void copyTo(final InputStream in, final OutputStream out)
		throws IOException
	{
		final byte[] buffer = new byte[16384];
		for (;;) {
			final int count = in.read(buffer);
			if (count < 0) break;
			out.write(buffer, 0, count);
		}
		in.close();
		out.close();
	}

	protected void makeParentDirectories(final File file) {
		final File parent = file.getParentFile();
		if (!parent.exists()) {
			makeParentDirectories(parent);
			parent.mkdir();
		}
	}

	/*
	 * This just checks for a NUL in the first 1024 bytes.
	 * Not the best test, but a pragmatic one.
	 */
	public boolean isBinaryFile(final String path) {
		try {
			final InputStream in = new FileInputStream(path);
			final byte[] buffer = new byte[1024];
			int offset = 0;
			while (offset < buffer.length) {
				final int count = in.read(buffer, offset, buffer.length - offset);
				if (count < 0) break;
				else offset += count;
			}
			in.close();
			while (offset > 0)
				if (buffer[--offset] == 0) return true;
		}
		catch (final IOException e) {}
		return false;
	}

	/**
	 * Make a sensible effort to get the path of the source for a class.
	 */
	public String getSourcePath(final String className)
		throws ClassNotFoundException
	{
		// TODO!
		throw new ClassNotFoundException("TODO");
	}

	public String getSourceURL(final String className) {
		return "http://fiji.sc/" + className.replace('.', '/') + ".java";
	}

	public String getJar(final String className) {
		try {
			final Class<?> clazz = Class.forName(className);
			String baseName = className;
			final int dot = baseName.lastIndexOf('.');
			if (dot > 0) baseName = baseName.substring(dot + 1);
			baseName += ".class";
			final String url = clazz.getResource(baseName).toString();
			final int dotJar = url.indexOf("!/");
			if (dotJar < 0) return null;
			final int offset = url.startsWith("jar:file:") ? 9 : 0;
			return url.substring(offset, dotJar);
		}
		catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String findSourcePath(final String className) {
		throw new RuntimeException("TODO");
	}

	/* TODO!
	public boolean newPlugin() {
		final GenericDialog gd = new GenericDialog("New Plugin");
		gd.addStringField("Plugin_name", "", 30);
		gd.showDialog();
		if (gd.wasCanceled()) return false;
		final String name = gd.getNextString();
		if (!newPlugin(name)) return false;
		return true;
	}

	public boolean newPlugin(String name) {
		final String originalName = name.replace('_', ' ');

		name = name.replace(' ', '_');
		if (name.indexOf('_') < 0) name += "_";

		final File file =
			new File(System.getProperty("ij.dir") + "/src-plugins/" + name + "/" +
				name + ".java");
		final File dir = file.getParentFile();
		if ((!dir.exists() && !dir.mkdirs()) || !dir.isDirectory()) return error("Could not make directory '" +
			dir.getAbsolutePath() + "'");

		final String jar = "plugins/" + name + ".jar";
		addToGitignore(jar);
		addPluginJarToFakefile(jar);

		final File pluginsConfig = new File(dir, "plugins.config");
		parent.open(pluginsConfig.getAbsolutePath());
		if (parent.getEditorPane().getDocument().getLength() == 0) parent
			.getEditorPane().insert(
				"# " + originalName + "\n" + "\n" + "# Author: \n" + "\n" +
					"Plugins, \"" + originalName + "\", " + name + "\n", 0);
		parent.open(file.getAbsolutePath());
		if (parent.getEditorPane().getDocument().getLength() == 0) parent
			.getEditorPane().insert(
				"import ij.ImagePlus;\n" + "\n" +
					"import ij.plugin.filter.PlugInFilter;\n" + "\n" +
					"import ij.process.ImageProcessor;\n" + "\n" + "public class " +
					name + " implements PlugInFilter {\n" +
					"\tprotected ImagePlus image;\n" + "\n" +
					"\tpublic int setup(String arg, ImagePlus image) {\n" +
					"\t\tthis.image = image;\n" + "\t\treturn DOES_ALL;\n" + "\t}\n" +
					"\n" + "\tpublic void run(ImageProcessor ip) {\n" +
					"\t\t// Do something\n" + "\t}\n" + "}", 0);
		return true;
	}

	public boolean addToGitignore(String name) {
		if (!name.startsWith("/")) name = "/" + name;
		if (!name.endsWith("\n")) name += "\n";

		final File file = new File(System.getProperty("ij.dir"), ".gitignore");
		if (!file.exists()) return false;

		try {
			final String content = readStream(new FileInputStream(file));
			if (content.startsWith(name) || content.indexOf("\n" + name) >= 0) return false;

			final FileOutputStream out = new FileOutputStream(file, true);
			if (!content.endsWith("\n")) out.write("\n".getBytes());
			out.write(name.getBytes());
			out.close();
			return true;
		}
		catch (final FileNotFoundException e) {
			return false;
		}
		catch (final IOException e) {
			return error("Failure writing " + file);
		}
	}

	public boolean addPluginJarToFakefile(final String name) {
		final File file = new File(System.getProperty("ij.dir"), "Fakefile");
		if (!file.exists()) return false;

		try {
			String content = readStream(new FileInputStream(file));

			// insert plugin target
			final int start = content.indexOf("\nPLUGIN_TARGETS=");
			if (start < 0) return false;
			int end = content.indexOf("\n\n", start);
			if (end < 0) end = content.length();
			int offset = content.indexOf("\n\t" + name, start);
			if (offset < end && offset > start) return false;
			String insert = "\n\t" + name;
			if (content.charAt(end - 1) != '\\') insert = " \\" + insert;
			content = content.substring(0, end) + insert + content.substring(end);

			// insert classpath
			offset = content.lastIndexOf("\nCLASSPATH(");
			while (offset > 0 &&
				(content.substring(offset)
					.startsWith("\nCLASSPATH(jars/test-fiji.jar)") || content.substring(
					offset).startsWith("\nCLASSPATH(plugins/FFMPEG")))
				offset = content.lastIndexOf("\nCLASSPATH(", offset - 1);
			if (offset < 0) return false;
			offset = content.indexOf('\n', offset + 1);
			if (offset < 0) return false;
			content =
				content.substring(0, offset) + "\nCLASSPATH(" + name + ")=jars/ij.jar" +
					content.substring(offset);

			final FileOutputStream out = new FileOutputStream(file);
			out.write(content.getBytes());
			out.close();

			return true;
		}
		catch (final FileNotFoundException e) {
			return false;
		}
		catch (final IOException e) {
			return error("Failure writing " + file);
		}
	}
	*/

	protected String readStream(final InputStream in) throws IOException {
		final StringBuffer buf = new StringBuffer();
		final byte[] buffer = new byte[65536];
		for (;;) {
			final int count = in.read(buffer);
			if (count < 0) break;
			buf.append(new String(buffer, 0, count));
		}
		in.close();
		return buf.toString();
	}

	/**
	 * Get a list of files from a directory (recursively)
	 */
	public void listFilesRecursively(final File directory, final String prefix,
		final List<String> result)
	{
		if (!directory.exists()) return;
		for (final File file : directory.listFiles())
			if (file.isDirectory()) listFilesRecursively(file, prefix +
				file.getName() + "/", result);
			else if (file.isFile()) result.add(prefix + file.getName());
	}

	/**
	 * Get a list of files from a directory or within a .jar file The returned
	 * items will only have the base path, to get at the full URL you have to
	 * prefix the url passed to the function.
	 */
	public List<String> getResourceList(String url) {
		final List<String> result = new ArrayList<String>();

		if (url.startsWith("jar:")) {
			final int bang = url.indexOf("!/");
			String jarURL = url.substring(4, bang);
			if (jarURL.startsWith("file:")) jarURL = jarURL.substring(5);
			final String prefix = url.substring(bang + 2);
			final int prefixLength = prefix.length();

			try {
				final JarFile jar = new JarFile(jarURL);
				final Enumeration<JarEntry> e = jar.entries();
				while (e.hasMoreElements()) {
					final JarEntry entry = e.nextElement();
					if (entry.getName().startsWith(prefix)) result.add(entry.getName()
						.substring(prefixLength));
				}
			}
			catch (final IOException e) {
				Log.error(e);
			}
		}
		else {
			final String prefix =
				/* TODO! */System.getProperty("os.name").startsWith("Win") ? "file:/"
					: "file:";
			if (url.startsWith(prefix)) url = url.substring(prefix.length());
			listFilesRecursively(new File(url), "", result);
		}
		return result;
	}

	public File getGitDirectory(File file) {
		if (file == null) return null;
		for (;;) {
			file = file.getParentFile();
			if (file == null) return null;
			final File git = new File(file, ".git");
			if (git.isDirectory()) return git;
		}
	}

	public File getPluginRootDirectory(File file) {
		if (file == null) return null;
		if (!file.isDirectory()) file = file.getParentFile();
		if (file == null) return null;

		File git = new File(file, ".git");
		if (git.isDirectory()) return file;

		File backup = file;
		for (;;) {
			final File parent = file.getParentFile();
			if (parent == null) return null;
			git = new File(parent, ".git");
			if (git.isDirectory()) return file.getName().equals("src-plugins")
				? backup : file;
			backup = file;
			file = parent;
		}
	}

	/* TODO!
	public void showDiff(final File file, final File gitDirectory) {
		showDiffOrCommit(file, gitDirectory, true);
	}

	public void commit(final File file, final File gitDirectory) {
		showDiffOrCommit(file, gitDirectory, false);
	}
	*/

	public String firstNLines(final String text, int maxLineCount) {
		int offset = -1;
		while (maxLineCount-- > 0) {
			offset = text.indexOf('\n', offset + 1);
			if (offset < 0) return text;
		}
		int count = 0, next = offset;
		while ((next = text.indexOf('\n', next + 1)) > 0)
			count++;
		return count == 0 ? text : text.substring(0, offset + 1) + "(" + count +
			" more line" + (count > 1 ? "s" : "") + ")...\n";
	}

	/* TODO!
	public void showDiffOrCommit(final File file, final File gitDirectory,
		final boolean diffOnly)
	{
		if (file == null || gitDirectory == null) return;
		final boolean isInFijiGit =
			gitDirectory.equals(new File(System.getProperty("ij.dir"), ".git"));
		final File root =
			isInFijiGit ? getPluginRootDirectory(file) : gitDirectory.getParentFile();

		try {
			String[] cmdarray =
				{ "git", "ls-files", "--exclude-standard", "--other", "." };
			SimpleExecuter e = new SimpleExecuter(cmdarray, root);
			if (e.getExitCode() != 0) {
				error("Could not determine whether there are untracked files");
				return;
			}
			final String out = e.getOutput();
			if (!out.equals("")) if (JOptionPane.showConfirmDialog(parent,
				"Do you want to commit the following untracked files?\n\n" +
					firstNLines(out, 10)) == JOptionPane.YES_OPTION)
			{
				cmdarray = new String[] { "git", "add", "-N", "." };
				e = new SimpleExecuter(cmdarray, root);
				if (e.getExitCode() != 0) {
					error("Could not add untracked files:\n" + e.getError());
					return;
				}
			}
		}
		catch (final IOException e) {
			IJ.handleException(e);
			return;
		}

		final DiffView diff = new DiffView();
		final String configPath =
			System.getProperty("ij.dir") + "/staged-plugins/" + root.getName() +
				".config";
		// only include .config file if gitDirectory is ij.dir/.git
		final String config =
			isInFijiGit && new File(configPath).exists() ? configPath : null;
		try {
			String[] cmdarray = { "git", "diff", "--", "." };
			if (config != null) cmdarray = append(cmdarray, config);
			final SimpleExecuter e =
				new SimpleExecuter(cmdarray, diff, new DiffView.IJLog(), root);
		}
		catch (final IOException e) {
			IJ.handleException(e);
			return;
		}

		if (diff.getChanges() == 0) {
			error("No changes detected for " + root);
			return;
		}

		final JFrame frame =
			new JFrame((diffOnly ? "Unstaged differences for " : "Commit ") + root);
		frame.setPreferredSize(new Dimension(640, diffOnly ? 480 : 640));
		if (diffOnly) frame.getContentPane().add(diff);
		else {
			final JPanel panel = new JPanel();
			frame.getContentPane().add(panel);
			panel.setLayout(new GridBagLayout());
			final GridBagConstraints c = new GridBagConstraints();

			final Font monospaced = new Font("Monospaced", Font.PLAIN, 12);

			c.anchor = GridBagConstraints.NORTHWEST;
			c.gridx = c.gridy = 0;
			c.weightx = c.weighty = 0;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(2, 2, 2, 2);
			panel.add(new JLabel("Subject:"), c);
			c.weightx = c.gridx = 1;
			final JTextField subject = new JTextField();
			subject.setFont(monospaced);
			subject.setColumns(76);
			subject.getDocument().addDocumentListener(new LengthWarner(76, subject));
			panel.add(subject, c);

			c.weightx = c.gridx = 0;
			c.gridy = 1;
			panel.add(new JLabel("Body:"), c);
			c.fill = GridBagConstraints.BOTH;
			c.weightx = c.weighty = c.gridx = 1;
			final JTextArea body = new JTextArea(20, 76);
			body.setFont(monospaced);
			body.setColumns(76);
			body.getDocument().addDocumentListener(new TextWrapper(76));
			panel.add(body, c);

			c.gridy = 2;
			panel.add(diff, c);

			final JPanel buttons = new JPanel();
			c.gridwidth = 2;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1;
			c.weighty = c.gridx = 0;
			c.gridy = 3;
			panel.add(buttons, c);

			final JButton commit = new JButton("Commit");
			buttons.add(commit);
			commit.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					String message = "";
					message = subject.getText();
					final String bodyText = body.getText();
					if (!bodyText.equals("")) message += "\n\n" + bodyText;
					if (message.equals("")) {
						error("Empty commit message");
						return;
					}

					String[] cmdarray = { "git", "commit", "-s", "-F", "-", "--", "." };
					if (config != null) cmdarray = append(cmdarray, config);
					final InputStream stdin =
						new ByteArrayInputStream(message.getBytes());
					SimpleExecuter.LineHandler ijLog = new DiffView.IJLog();
					try {
						final SimpleExecuter executer =
							new SimpleExecuter(cmdarray, stdin, ijLog, ijLog, root);
						if (executer.getExitCode() == 0) frame.dispose();
					}
					catch (final IOException e2) {
						IJ.handleException(e2);
					}
				}
			});
		}
		frame.pack();
		frame.setVisible(true);
	}
	*/

	public class LengthWarner implements DocumentListener {

		protected int width;
		protected JTextComponent component;
		protected Color normal, warn;

		public LengthWarner(final int width, final JTextComponent component) {
			this.width = width;
			this.component = component;
			normal = component.getForeground();
			warn = Color.red;
		}

		@Override
		public void changedUpdate(final DocumentEvent e) {}

		@Override
		public void insertUpdate(final DocumentEvent e) {
			updateColor();
		}

		@Override
		public void removeUpdate(final DocumentEvent e) {
			updateColor();
		}

		public void updateColor() {
			component.setForeground(component.getDocument().getLength() <= width
				? normal : warn);
		}
	}

	public class TextWrapper implements DocumentListener {

		protected int width;

		public TextWrapper(final int width) {
			this.width = width;
		}

		@Override
		public void changedUpdate(final DocumentEvent e) {}

		@Override
		public void insertUpdate(final DocumentEvent e) {
			final Document document = e.getDocument();
			final int offset = e.getOffset() + e.getLength();
			if (offset <= width) return;
			try {
				final String text = document.getText(0, offset);
				int newLine = text.lastIndexOf('\n');
				if (offset - newLine <= width) return;
				while (offset - newLine > width) {
					int remove = 0;
					final int space = text.lastIndexOf(' ', newLine + width);
					if (space < newLine) break;
					if (space > 0) {
						int first = space;
						while (first > newLine + 1 && text.charAt(first - 1) == ' ')
							first--;
						remove = space + 1 - first;
						newLine = first;
					}
					else newLine += width;

					final int removeCount = remove, at = newLine;
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							try {
								if (removeCount > 0) document.remove(at, removeCount);
								document.insertString(at, "\n", null);
							}
							catch (final BadLocationException e2) { /* ignore */}
						}
					});
				}
			}
			catch (final BadLocationException e2) { /* ignore */}
		}

		@Override
		public void removeUpdate(final DocumentEvent e) {}
	}

	/* TODO!
	public class ScreenLineHandler implements SimpleExecuter.LineHandler {

		public void handleLine(final String line) {
			final TextEditor.Tab tab = parent.getTab();
			tab.screen.insert(line + "\n", tab.screen.getDocument().getLength());
		}
	}

	public static class GrepLineHandler implements SimpleExecuter.LineHandler {

		protected static Pattern pattern = Pattern.compile(
			"([A-Za-z]:[^:]*|[^:]+):([1-9][0-9]*):.*", Pattern.DOTALL);

		public ErrorHandler errorHandler;
		protected String directory;

		public GrepLineHandler(final JTextArea textArea, String directory) {
			errorHandler = new ErrorHandler(textArea);
			if (!directory.endsWith("/")) directory += "/";
			this.directory = directory;
		}

		public void handleLine(final String line) {
			final Matcher matcher = pattern.matcher(line);
			if (matcher.matches()) errorHandler.addError(
				directory + matcher.group(1), Integer.parseInt(matcher.group(2)), line);
			else errorHandler.addError(null, -1, line);
		}
	}

	public void gitGrep(final String searchTerm, final File directory) {
		final GrepLineHandler handler =
			new GrepLineHandler(parent.errorScreen, directory.getAbsolutePath());
		parent.getTab().showErrors();
		try {
			final SimpleExecuter executer =
				new SimpleExecuter(new String[] { "git", "grep", "-n", searchTerm },
					handler, handler, directory);
			parent.errorHandler = handler.errorHandler;
		}
		catch (final IOException e) {
			IJ.handleException(e);
		}
	}

	public void openInGitweb(final File file, final File gitDirectory,
		final int line)
	{
		if (file == null || gitDirectory == null) {
			error("No file or git directory");
			return;
		}
		final String url = getGitwebURL(file, gitDirectory, line);
		if (url == null) error("Could not get gitweb URL for " + file);
		else new BrowserLauncher().run(url);
	}

	public String git(final File gitDirectory, final File workingDirectory,
		String... args)
	{
		try {
			args =
				append(gitDirectory == null ? new String[] { "git" } : new String[] {
					"git", "--git-dir=" + gitDirectory.getAbsolutePath() }, args);
			final SimpleExecuter gitConfig =
				new SimpleExecuter(args, workingDirectory);
			if (gitConfig.getExitCode() == 0) return stripSuffix(gitConfig
				.getOutput(), "\n");
			parent.write(gitConfig.getError());
		}
		catch (final IOException e) {
			parent.write(e.getMessage());
		}
		return null;
	}

	public String git(final File gitDirectory, final String... args) {
		return git(gitDirectory, (File) null, args);
	}

	public String gitConfig(final File gitDirectory, final String key) {
		return git(gitDirectory, "config", key);
	}

	public String getGitwebURL(final File file, final File gitDirectory,
		final int line)
	{
		String url = gitConfig(gitDirectory, "remote.origin.url");
		if (url == null) {
			final String remote = gitConfig(gitDirectory, "branch.master.remote");
			if (remote != null) url =
				gitConfig(gitDirectory, "remote." + remote + ".url");
			if (url == null) return null;
		}
		if (url.startsWith("repo.or.cz:") || url.startsWith("ssh://repo.or.cz/")) {
			final int index = url.indexOf("/srv/git/") + "/srv/git/".length();
			url = "http://repo.or.cz/w/" + url.substring(index);
		}
		else if (url.startsWith("git://repo.or.cz/")) url =
			"http://repo.or.cz/w/" + url.substring("git://repo.or.cz/".length());
		else {
			url = stripSuffix(url, "/");
			int slash = url.lastIndexOf('/');
			if (url.endsWith("/.git")) slash = url.lastIndexOf('/', slash - 1);
			String project = url.substring(slash + 1);
			if (!project.endsWith(".git")) project += "/.git";
			if (project.equals("imageja.git")) project = "ImageJA.git";
			url = "http://fiji.sc/cgi-bin/gitweb.cgi?p=" + project;
		}
		final String head =
			git(gitDirectory, "rev-parse", "--symbolic-full-name", "HEAD");
		final String path =
			git(null, // ls-files does not work with --git-dir
			 file.getParentFile(), "ls-files", "--full-name", file.getName());
		if (url == null || head == null || path == null) return null;
		return url + ";a=blob;f=" + path + ";hb=" + head +
			(line < 0 ? "" : "#l" + line);
	}
	*/

	protected String[] append(final String[] array, final String item) {
		final String[] result = new String[array.length + 1];
		System.arraycopy(array, 0, result, 0, array.length);
		result[array.length] = item;
		return result;
	}

	protected String[] append(final String[] array, final String[] append) {
		final String[] result = new String[array.length + append.length];
		System.arraycopy(array, 0, result, 0, array.length);
		System.arraycopy(append, 0, result, array.length, append.length);
		return result;
	}

	/* TODO!
	protected void addChangesActionLink(final DiffView diff, final String text,
		final String plugin, final int verboseLevel)
	{
		diff.link(text, new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				showPluginChangesSinceUpload(plugin, verboseLevel);
			}
		});
	}

	public void showPluginChangesSinceUpload(final String plugin) {
		showPluginChangesSinceUpload(new LogComponentCommits(0, 15, false, null,
			null, "-p", "-M"), plugin, true);
	}

	public void showPluginChangesSinceUpload(final String plugin,
		final int verboseLevel)
	{
		showPluginChangesSinceUpload(new LogComponentCommits(verboseLevel, 15,
			false, null, null, "-p", "-M"), plugin, false);
	}

	public void showPluginChangesSinceUpload(final LogComponentCommits logger,
		final String plugin, final boolean checkReadyForUpload)
	{
		final DiffView diff = new DiffView();
		diff.normal("Verbose level: ");
		addChangesActionLink(diff, "file names", plugin, 0);
		diff.normal(" ");
		addChangesActionLink(diff, "bytecode", plugin, 1);
		diff.normal(" ");
		addChangesActionLink(diff, "verbose bytecode", plugin, 2);
		diff.normal(" ");
		addChangesActionLink(diff, "hexdump", plugin, 3);
		diff.normal("\n");
		logger.setOutput(diff);
		logger.setErrorOutput(diff);

		final Cursor cursor = diff.getCursor();
		diff.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		final Thread thread = new Thread() {

			@Override
			public void run() {
				logger.showChanges(plugin);
				diff.setCursor(cursor);
			}
		};
		final JFrame frame = new JFrame("Changes since last upload " + plugin);
		frame.getContentPane().add(diff);
		frame.pack();
		frame.setSize(640, 640);
		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(final WindowEvent e) {
				thread.stop();
				try {
					thread.join();
				}
				catch (final InterruptedException e2) {
					System.err.println("interrupted");
				}
			}
		});
		frame.setVisible(true);

		if (checkReadyForUpload) {
			// When run from Updater, call ready-for-upload
			diff.normal("Checking whether " + plugin +
				" is ready to be uploaded... \n");
			try {
				final int pos = diff.document.getLength() - 1;
				final ReadyForUpload ready =
					new ReadyForUpload(new PrintStream(diff.getOutputStream()));
				if (ready.check(plugin)) diff.green(pos, "Yes!");
				else diff.red(pos, "Not ready!");
			}
			catch (final Exception e) {
				IJ.handleException(e);
				diff.red("Probably not (see Exception)\n");
			}
		}

		thread.start();
	}
	*/

	protected String stripSuffix(final String string, final String suffix) {
		if (string.endsWith(suffix)) return string.substring(0, string.length() -
			suffix.length());
		return string;
	}

	protected boolean error(final String message) {
		JOptionPane.showMessageDialog(parent, message);
		return false;
	}
}
