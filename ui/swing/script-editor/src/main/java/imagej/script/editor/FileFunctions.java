package fiji.scripting;

import fiji.SimpleExecuter;

import fiji.build.Fake;
import fiji.build.MiniMaven.POM;
import fiji.build.Parser;
import fiji.build.Rule;
import fiji.build.SubFake;

import ij.IJ;

import ij.gui.GenericDialog;

import ij.plugin.BrowserLauncher;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
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
		if (!dir.endsWith("/"))
			dir += "/";
		ijDir = dir;
	}

	protected TextEditor parent;

	public FileFunctions(TextEditor parent) {
		this.parent = parent;
	}

	public List<String> extractSourceJar(String path) throws IOException {
		String baseName = new File(path).getName();
		if (baseName.endsWith(".jar") || baseName.endsWith(".zip"))
			baseName = baseName.substring(0, baseName.length() - 4);
		String baseDirectory = System.getProperty("ij.dir")
			+ "/src-plugins/" + baseName + "/";

		List<String> result = new ArrayList<String>();
		JarFile jar = new JarFile(path);
		for (JarEntry entry : Collections.list(jar.entries())) {
			String name = entry.getName();
			if (name.endsWith(".class") || name.endsWith("/"))
				continue;
			String destination = baseDirectory + name;
			copyTo(jar.getInputStream(entry), destination);
			result.add(destination);
		}
		return result;
	}

	protected void copyTo(InputStream in, String destination)
			throws IOException {
		File file = new File(destination);
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
	public boolean isBinaryFile(String path) {
		try {
			InputStream in = new FileInputStream(path);
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
		// First, let's try to get the .jar file for said class.
		String result = getJar(className);
		if (result == null)
			return findSourcePath(className);

		// try the simple thing first
		int slash = result.lastIndexOf('/'), backSlash = result.lastIndexOf('\\');
		String baseName = result.substring(Math.max(slash, backSlash) + 1, result.length() - 4);
		String dir = ijDir + "/src-plugins/" + baseName + "/";
		String path = dir + className.replace('.', '/') + ".java";
		if (new File(path).exists())
			return path;
		if (new File(dir).isDirectory())
			for (;;) {
				int dot = className.lastIndexOf('.');
				if (dot < 0)
					break;
				className = className.substring(0, dot);
				path = dir + className.replace('.', '/') + ".java";
			}

		// Try to find it with the help of the Fakefile
		File fakefile = new File(ijDir, "Fakefile");
		if (fakefile.exists()) try {
			Fake fake = new Fake();
			if (parent != null) {
				final JTextAreaOutputStream output = new JTextAreaOutputStream(parent.getTab().screen);
				final JTextAreaOutputStream errors = new JTextAreaOutputStream(parent.errorScreen);
				fake.out = new PrintStream(output);
				fake.err = new PrintStream(errors);
			}
			Parser parser = fake.parse(new FileInputStream(fakefile), new File(ijDir));
			parser.parseRules(null);
			Rule rule = parser.getRule("plugins/" + baseName + ".jar");
			if (rule == null)
				rule = parser.getRule("jars/" + baseName + ".jar");
			if (rule != null) {
				String stripPath = rule.getStripPath();
				dir = ijDir + "/";
				if (rule instanceof SubFake) {
					SubFake subFake = (SubFake)rule;
					stripPath = rule.getLastPrerequisite();
					fakefile = subFake.getFakefile();
					if (fakefile != null) {
						dir += rule.getLastPrerequisite();
						parser = fake.parse(new FileInputStream(fakefile), new File(dir));
						parser.parseRules(null);
						rule = parser.getRule(baseName + ".jar");
						if (rule != null)
							stripPath = rule.getStripPath();
					}
					else {
						POM pom = subFake.getPOM();
						if (pom != null) {
							dir += rule.getLastPrerequisite();
							stripPath = pom.getSourcePath();
						}
					}
				}
				if (stripPath != null) {
					if (!stripPath.endsWith("/"))
						stripPath += "/";
					dir += stripPath;
					path = dir + className.replace('.', '/') + ".java";
					if (new File(path).exists())
						return path;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
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
			findJavaPaths(new File(ijDir), "");
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
			return ijDir + "/" + paths.get(0);
		String[] names = paths.toArray(new String[paths.size()]);
		GenericDialog gd = new GenericDialog("Choose path", parent);
		gd.addChoice("path", names, names[0]);
		gd.showDialog();
		if (gd.wasCanceled())
			return null;
		return ijDir + "/" + gd.getNextChoice();
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
		GenericDialog gd = new GenericDialog("New Plugin");
		gd.addStringField("Plugin_name", "", 30);
		gd.showDialog();
		if (gd.wasCanceled())
			return false;
		String name = gd.getNextString();
		if (!newPlugin(name))
			return false;
		return true;
	}

	public boolean newPlugin(String name) {
		String originalName = name.replace('_', ' ');

		name = name.replace(' ', '_');
		if (name.indexOf('_') < 0)
			name += "_";

		File file = new File(System.getProperty("ij.dir")
			+ "/src-plugins/" + name + "/" + name + ".java");
		File dir = file.getParentFile();
		if ((!dir.exists() && !dir.mkdirs()) || !dir.isDirectory())
			return error("Could not make directory '"
				+ dir.getAbsolutePath() + "'");

		String jar = "plugins/" + name + ".jar";
		addToGitignore(jar);
		addPluginJarToFakefile(jar);

		File pluginsConfig = new File(dir, "plugins.config");
		parent.open(pluginsConfig.getAbsolutePath());
		if (parent.getEditorPane().getDocument().getLength() == 0)
			parent.getEditorPane().insert(
				"# " + originalName + "\n"
				+ "\n"
				+ "# Author: \n"
				+ "\n"
				+ "Plugins, \"" + originalName + "\", " + name + "\n", 0);
		parent.open(file.getAbsolutePath());
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

		File file = new File(System.getProperty("ij.dir"), ".gitignore");
		if (!file.exists())
			return false;

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

	public boolean addPluginJarToFakefile(String name) {
		File file = new File(System.getProperty("ij.dir"), "Fakefile");
		if (!file.exists())
			return false;

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
				IJ.handleException(e);
			}
		}
		else {
			String prefix = IJ.isWindows() ? "file:/" : "file:";
			if (url.startsWith(prefix))
				url = url.substring(prefix.length());
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

	public void showDiff(File file, File gitDirectory) {
		showDiffOrCommit(file, gitDirectory, true);
	}

	public void commit(File file, File gitDirectory) {
		showDiffOrCommit(file, gitDirectory, false);
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

	public void showDiffOrCommit(File file, File gitDirectory, boolean diffOnly) {
		if (file == null || gitDirectory == null)
			return;
		boolean isInFijiGit = gitDirectory.equals(new File(System.getProperty("ij.dir"), ".git"));
		final File root = isInFijiGit ? getPluginRootDirectory(file) : gitDirectory.getParentFile();

		try {
			String[] cmdarray = {
				"git", "ls-files", "--exclude-standard", "--other", "."
			};
			SimpleExecuter e = new SimpleExecuter(cmdarray, root);
			if (e.getExitCode() != 0) {
				error("Could not determine whether there are untracked files");
				return;
			}
			String out = e.getOutput();
			if (!out.equals(""))
				if (JOptionPane.showConfirmDialog(parent,
						"Do you want to commit the following untracked files?\n\n" + firstNLines(out, 10)) == JOptionPane.YES_OPTION) {
					cmdarray = new String[] {
						"git", "add", "-N", "."
					};
					e = new SimpleExecuter(cmdarray, root);
					if (e.getExitCode() != 0) {
						error("Could not add untracked files:\n" + e.getError());
						return;
					}
				}
		} catch (IOException e) {
			IJ.handleException(e);
			return;
		}

		final DiffView diff = new DiffView();
		String configPath = System.getProperty("ij.dir") + "/staged-plugins/"
			+ root.getName() + ".config";
		// only include .config file if gitDirectory is ij.dir/.git
		final String config = isInFijiGit && new File(configPath).exists() ? configPath : null;
		try {
			String[] cmdarray = {
				"git", "diff", "--", "."
			};
			if (config != null)
				cmdarray = append(cmdarray, config);
			SimpleExecuter e = new SimpleExecuter(cmdarray,
				diff, new DiffView.IJLog(), root);
		} catch (IOException e) {
			IJ.handleException(e);
			return;
		}

		if (diff.getChanges() == 0) {
			error("No changes detected for " + root);
			return;
		}

		final JFrame frame = new JFrame((diffOnly ? "Unstaged differences for " : "Commit ") + root);
		frame.setPreferredSize(new Dimension(640, diffOnly ? 480 : 640));
		if (diffOnly)
			frame.getContentPane().add(diff);
		else {
			JPanel panel = new JPanel();
			frame.getContentPane().add(panel);
			panel.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();

			Font monospaced = new Font("Monospaced", Font.PLAIN, 12);

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

			c.weightx = c.gridx = 0; c.gridy = 1;
			panel.add(new JLabel("Body:"), c);
			c.fill = GridBagConstraints.BOTH;
			c.weightx = c.weighty = c.gridx = 1;
			final JTextArea body = new JTextArea(20, 76);
			body.setFont(monospaced);
			body.setColumns(76);
			body.getDocument().addDocumentListener(new TextWrapper(76));
			panel.add(body, c);

			c.gridy= 2;
			panel.add(diff, c);

			JPanel buttons = new JPanel();
			c.gridwidth = 2;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1; c.weighty = c.gridx = 0; c.gridy = 3;
			panel.add(buttons, c);

			JButton commit = new JButton("Commit");
			buttons.add(commit);
			commit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String message = "";
					message = subject.getText();
					String bodyText = body.getText();
					if (!bodyText.equals(""))
						message += "\n\n" + bodyText;
					if (message.equals("")) {
						error("Empty commit message");
						return;
					}

					String[] cmdarray = {
						"git", "commit", "-s", "-F", "-", "--", "."
					};
					if (config != null)
						cmdarray = append(cmdarray, config);
					InputStream stdin = new ByteArrayInputStream(message.getBytes());
					SimpleExecuter.LineHandler ijLog = new DiffView.IJLog();
					try {
						SimpleExecuter executer = new SimpleExecuter(cmdarray,
							stdin, ijLog, ijLog, root);
						if (executer.getExitCode() == 0)
							frame.dispose();
					} catch (IOException e2) {
						IJ.handleException(e2);
					}
				}
			});
		}
		frame.pack();
		frame.setVisible(true);
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

	public class ScreenLineHandler implements SimpleExecuter.LineHandler {
		public void handleLine(String line) {
			TextEditor.Tab tab = parent.getTab();
			tab.screen.insert(line + "\n", tab.screen.getDocument().getLength());
		}
	}

	public static class GrepLineHandler implements SimpleExecuter.LineHandler {
		protected static Pattern pattern = Pattern.compile("([A-Za-z]:[^:]*|[^:]+):([1-9][0-9]*):.*", Pattern.DOTALL);

		public ErrorHandler errorHandler;
		protected String directory;

		public GrepLineHandler(JTextArea textArea, String directory) {
			errorHandler = new ErrorHandler(textArea);
			if (!directory.endsWith("/"))
				directory += "/";
			this.directory = directory;
		}

		public void handleLine(String line) {
			Matcher matcher = pattern.matcher(line);
			if (matcher.matches())
				errorHandler.addError(directory + matcher.group(1), Integer.parseInt(matcher.group(2)), line);
			else
				errorHandler.addError(null, -1, line);
		}
	}

	public void gitGrep(String searchTerm, File directory) {
		GrepLineHandler handler = new GrepLineHandler(parent.errorScreen, directory.getAbsolutePath());
		parent.getTab().showErrors();
		try {
			SimpleExecuter executer = new SimpleExecuter(new String[] {
				"git", "grep", "-n", searchTerm
			}, handler, handler, directory);
			parent.errorHandler = handler.errorHandler;
		} catch (IOException e) {
			IJ.handleException(e);
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
		else
			new BrowserLauncher().run(url);
	}

	public String git(File gitDirectory, File workingDirectory, String... args) {
		try {
			args = append(gitDirectory == null ? new String[] { "git" } :
				new String[] { "git", "--git-dir=" + gitDirectory.getAbsolutePath()}, args);
			SimpleExecuter gitConfig = new SimpleExecuter(args, workingDirectory);
			if (gitConfig.getExitCode() == 0)
				return stripSuffix(gitConfig.getOutput(), "\n");
			parent.write(gitConfig.getError());
		} catch (IOException e) {
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

	protected void addChangesActionLink(DiffView diff, String text, final String plugin, final int verboseLevel) {
		diff.link(text, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showPluginChangesSinceUpload(plugin, verboseLevel);
			}
		});
	}

	public void showPluginChangesSinceUpload(String plugin) {
		showPluginChangesSinceUpload(new LogComponentCommits(0, 15, false, null, null, "-p", "-M"), plugin, true);
	}

	public void showPluginChangesSinceUpload(final String plugin, final int verboseLevel) {
		showPluginChangesSinceUpload(new LogComponentCommits(verboseLevel, 15, false, null, null, "-p", "-M"), plugin, false);
	}

	public void showPluginChangesSinceUpload(final LogComponentCommits logger, final String plugin, final boolean checkReadyForUpload) {
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
			public void windowClosing(WindowEvent e) {
				thread.stop();
				try {
					thread.join();
				} catch (InterruptedException e2) {
					System.err.println("interrupted");
				}
			}
		});
		frame.setVisible(true);

		if (checkReadyForUpload) {
			// When run from Updater, call ready-for-upload
			diff.normal("Checking whether " + plugin + " is ready to be uploaded... \n");
			try {
				int pos = diff.document.getLength() - 1;
				ReadyForUpload ready = new ReadyForUpload(new PrintStream(diff.getOutputStream()));
				if (ready.check(plugin))
					diff.green(pos, "Yes!");
				else
					diff.red(pos, "Not ready!");
			} catch (Exception e) {
				IJ.handleException(e);
				diff.red("Probably not (see Exception)\n");
			}
		}

		thread.start();
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
