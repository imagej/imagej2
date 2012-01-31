package fiji.scripting;

import fiji.CheckClassVersions;
import fiji.SimpleExecuter;
import fiji.SimpleExecuter.LineHandler;

import fiji.build.ByteCodeAnalyzer;
import fiji.build.Fake;
import fiji.build.FakeException;
import fiji.build.Parser;
import fiji.build.Rule;

import fiji.scripting.FileFunctions;

import ij.IJ;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.net.MalformedURLException;
import java.net.URL;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import java.util.zip.ZipEntry;


public class LogComponentCommits {
	protected int verbose;
	protected int fuzz;
	protected boolean color;
	protected LineHandler out, err;
	protected List<String> extraArgs = new ArrayList<String>();

	protected String ijDir = System.getProperty("ij.dir");
	protected String tempDir;

	// Updater stuff
	protected Object plugins;
	protected Method getPlugin, getURL, getTimestamp;

	// Fiji Build stuff
	protected Parser parser;

	// Date/Time stuff
	protected Calendar calendar = Calendar.getInstance();
	protected DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public LogComponentCommits(int verbose, int fuzz, boolean color, PrintStream out, PrintStream err, String... extraArgs) {
		this(verbose, fuzz, color, out, err, Arrays.asList(extraArgs));
	}

	public LogComponentCommits(int verbose, int fuzz, boolean color, PrintStream out, PrintStream err, List<String> extraArgs) {
		this(verbose, fuzz, color, out == null ? null : getLineHandler(out), err == null ? null : getLineHandler(err), extraArgs);
	}

	public LogComponentCommits(int verbose, int fuzz, boolean color, LineHandler out, LineHandler err, List<String> extraArgs) {
		this.verbose = verbose;
		this.fuzz = fuzz;
		this.color = color;
		this.out = out;
		this.err = err;
		this.extraArgs = extraArgs;
	}

	public static LineHandler getLineHandler(final OutputStream out) {
		final PrintStream printStream = new PrintStream(out);
		return new LineHandler() {
			@Override
			public void handleLine(String line) {
				IJ.log(line);
			}
		};
	}

	public void setOutput(final LineHandler out) {
		this.out = out;
	}

	public void setErrorOutput(final LineHandler err) {
		this.err = err;
	}

	public void showChanges(String[] components) {
		for (String component : components)
			showChanges(component);
	}

	protected boolean wasInterrupted() {
		try {
			Thread.sleep(0);
		} catch (InterruptedException e) {
			return true;
		}
		return false;
	}

	public void showChanges(String component) {
		long timestamp = getTimestampFromUpdater(component);
		if (timestamp < 0) {
			return;
		}

		calendar.set((int)((timestamp / 10000000000l) % 10000),
			(int)((timestamp / 100000000l) % 100) - 1,
			(int)((timestamp / 1000000l) % 100),
			(int)((timestamp / 10000l) % 100),
			(int)((timestamp / 100l) % 100),
			(int)(timestamp % 100));
		String message, since;
		message = since = dateFormat.format(calendar.getTime());
		if (fuzz != 0) {
			calendar.setTimeInMillis(calendar.getTimeInMillis() - 60000l * fuzz);
			since = dateFormat.format(calendar.getTime());
			message += " (-" + fuzz + " minutes) = " + since;
		}

		Rule rule = getRule(component);

		if (wasInterrupted())
			return;

		List<String> extraFiles = new ArrayList<String>();
		if (rule != null && !rule.getClass().getName().endsWith("SubFake")) {
			String baseName = component.substring(component.lastIndexOf('/') + 1);
			if (baseName.endsWith(".jar"))
				baseName = baseName.substring(0, baseName.length() - 4);
			for (String file : new String[] { "config", "Fakefile" })
				extraFiles.add("staged-plugins/" + baseName + "." + file);
		}

		out.handleLine("*** Changes in " + component + " since " + message + " ***");
		try {
			if (!compareToUploaded(component))
				return;
		} catch (IOException e) {
			out.handleLine("Could not compare " + component + " to uploaded version (" + e + ")");
		}

		if (wasInterrupted())
			return;

		since = "--since=" + since + "+0000";
		String[] cmdarray = new String[5 + extraArgs.size() + extraFiles.size()];
		int i = 0;
		cmdarray[i++] = "git";
		cmdarray[i++] = "log";
		cmdarray[i++] = since;
		for (String extra : extraArgs)
			cmdarray[i++] = extra;
		cmdarray[i++] = "--";
		for (String extra : extraFiles)
			cmdarray[i++] = extra;
		cmdarray[i++] = ".";

		if (rule != null && rule.getClass().getName().endsWith("SubFake"))
			SimpleExecuter.exec(new File(ijDir, rule.getLastPrerequisite()), out, cmdarray);
		else if (component.startsWith("precompiled/ImageJ-") || component.startsWith("ImageJ-")) {
			cmdarray[cmdarray.length - 1] = "ImageJ.c";
			SimpleExecuter.exec(new File(ijDir), out, cmdarray);
		}
		else {
			String path = rule == null ? component : rule.getPrerequisiteString();
			int starstar = path.indexOf("**");
			if (starstar >= 0)
				path = path.substring(0, starstar);
			cmdarray[cmdarray.length - 1] = path;
			SimpleExecuter.exec(new File(ijDir), out, cmdarray);
		}
	}

	public long getTimestampFromUpdater(String component) {
		Object plugin = getPluginFromUpdater(component);
		try {
			return plugin == null ? -1 : ((Long)getTimestamp.invoke(plugin, new Object[0])).longValue();
		} catch (Throwable t) {
			IJ.handleException(t);
			out.handleLine("Could not access the Fiji Updater!");
			return -1;
		}
	}

	public String getURLFromUpdater(String component) {
		Object plugin = getPluginFromUpdater(component);
		try {
			return plugin == null ? null : (String)getURL.invoke(plugins, new Object[] { plugin });
		} catch (Throwable t) {
			IJ.handleException(t);
			out.handleLine("Could not access the Fiji Updater!");
			return null;
		}
	}

	protected Object getPluginFromUpdater(String component) {
		try {
			getPluginCollection();
		} catch (Throwable t) {
			IJ.handleException(t);
			out.handleLine("Could not access the Fiji Updater!");
			return null;
		}

		if (component.startsWith("precompiled/"))
			component = component.substring(12);
		try {
			Object p = getPlugin.invoke(plugins, new Object[] { component });
			if (p == null) {
				out.handleLine("Not uploaded: " + component);
				return null;
			}
			return p;
		} catch (Exception e) {
			IJ.handleException(e);
			out.handleLine("Internal error while accessing " + component);
			return null;
		}
	}

	protected void getPluginCollection() throws Throwable {
		if (plugins != null)
			return;
		try {
			Class pluginsClass = getClass().getClassLoader().loadClass("fiji.updater.logic.PluginCollection");
			Method read = pluginsClass.getMethod("read", new Class[0]);
			plugins = pluginsClass.newInstance();
			read.invoke(plugins, new Object[0]);
			getPlugin = pluginsClass.getMethod("getPlugin", new Class[] { String.class });
			Class pluginClass = getClass().getClassLoader().loadClass("fiji.updater.logic.PluginObject");
			getURL = pluginsClass.getMethod("getURL", new Class[] { pluginClass });
			getTimestamp = pluginClass.getMethod("getTimestamp", new Class[0]);
		} catch (Throwable t) {
			IJ.handleException(t);
			plugins = null;
		}
	}

	public Rule getRule(String component) {
		if (parser == null) try {
			if (!new File(ijDir, "Fakefile").exists())
				return null;
			Fake fake = new Fake();
			parser = fake.parse(new FileInputStream(ijDir + "/Fakefile"), new File(ijDir));
			parser.parseRules(new ArrayList<String>());
		} catch (Exception e) {
			parser = null;
			out.handleLine("Could not parse Fakefile");
			return null;
		}

		return parser.getRule(component);
	}

	public static class Outputter extends Thread {
		protected InputStream in;
		protected PrintStream out;

		public Outputter(InputStream in, PrintStream out) {
			this.in = in;
			this.out = out;
		}

		public void run() {
			byte[] buffer = new byte[65536];
			try {
				for (;;) {
					int count = in.read(buffer);
					if (count < 0)
						break;
					out.write(buffer, 0, count);
				}
				in.close();
			} catch (IOException e) { /* ignore */ }
		}
	}

	protected List<ZipEntry> getZipItems(URL url) {
		List<ZipEntry> result = new ArrayList<ZipEntry>();
		try {
			JarInputStream in = new JarInputStream(url.openStream());
			for (;;) {
				ZipEntry entry = in.getNextEntry();
				if (entry == null)
					break;
				result.add(entry);
			}
			in.close();
		} catch (IOException e) {
			return result;
		}
		Collections.sort(result, new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				return ((ZipEntry)o1).getName().compareTo(((ZipEntry)o2).getName());
			}

			@Override
			public boolean equals(Object o) {
				return false;
			}
		});
		return result;
	}

	protected String getTempDir() {
		if (tempDir == null) try {
			File file = File.createTempFile("jar-dir-", "");
			file.delete();
			file.mkdir();
			file.deleteOnExit();
			tempDir = file.getAbsolutePath();
		} catch (IOException e) {
			throw new RuntimeException("Could not make a temporary directory");
		}
		return tempDir;
	}

	protected void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[16384];
		for (;;) {
			int count = in.read(buffer);
			if (count < 0)
				break;
			out.write(buffer, 0, count);
		}
		in.close();
		out.flush();
		out.close();
	}

	protected String[] toStringArray(List<String> list) {
		return list.toArray(new String[list.size()]);
	}

	protected void javap(String jar, String className, String outputFile) {
		List<String> args = new ArrayList(Arrays.asList(new String[] {
			System.getProperty("ij.executable"),
				"--javap", "--", "-classpath", jar, "-c", "-l",
		}));
		if (verbose > 1)
			args.add("-verbose");
		args.add(className);
		try {
			SimpleExecuter.exec(new File(ijDir), new FileOutputStream(outputFile), toStringArray(args));
		} catch (Exception e) {
			out.handleLine("Could not execute " + Arrays.toString(toStringArray(args)));
		}
	}

	// file == outputFile is handled correctly, thanks to the byte array
	protected void hexdump(String file, String outputFile) {
		try {
			OutputStream out = new FileOutputStream(outputFile);
			SimpleExecuter.exec(new File(ijDir), out, "hexdump", "-C", file);
			out.close();
		} catch (Exception e) {
			out.handleLine("Could not execute hexdump " + file);
		}
	}

	protected void analyzeByteCode(String file, String outputFile) {
		try {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			copyFile(new FileInputStream(file), outStream);
			ByteCodeAnalyzer analyzer = new ByteCodeAnalyzer(outStream.toByteArray(), true);
			InputStream inStream = new ByteArrayInputStream(analyzer.toString().getBytes());
			copyFile(inStream, new FileOutputStream(outputFile));
		} catch (Exception e) {
			out.handleLine("Could not analyze bytecode of " + file);
		}
	}

	protected<K, T> String toString(Map<K, T> map, K key) {
		return "'" + key + "' => '" + map.get(key) + "'";
	}

	protected<K, T> void showMapDiff(String label, Map<K, T> remoteMap, Map<K, T> localMap) {
		List<K> remote = new ArrayList<K>(remoteMap.keySet());
		List<K> local = new ArrayList<K>(localMap.keySet());

		int i = 0, j = 0;
		while (i < remote.size() || j < local.size())
			if (i == remote.size() || (j < local.size() && remote.get(i).toString().compareTo(local.get(j).toString()) > 0)) {
				out.handleLine("Local " + label + ": " + toString(localMap, local.get(j)));
				j++;
			} else if (j == local.size() || (i < remote.size() && remote.get(i).toString().compareTo(local.get(j).toString()) < 0)) {
				out.handleLine("Remote " + label + ": " + toString(remoteMap, remote.get(i)));
				i++;
			} else {
				T e1 = remoteMap.get(local.get(j));
				T e2 = localMap.get(local.get(j));
				if (!e1.equals(e2))
					out.handleLine("Differences in " + label + ": '" + local.get(j) + "' => '" + e1 + "' != '" + e2 + "'");
				i++; j++;
			}
	}

	protected void showManifestDiff(String remoteURL, String localURL) throws IOException, MalformedURLException {
		JarInputStream remoteIn = new JarInputStream(new URL(remoteURL).openStream());
		JarInputStream localIn = new JarInputStream(new URL(localURL).openStream());
		Manifest remoteManifest = remoteIn.getManifest();
		Manifest localManifest = localIn.getManifest();
		if (remoteManifest == null)
			remoteManifest = new Manifest();
		if (localManifest == null)
			localManifest = new Manifest();
		showMapDiff("manifest", remoteManifest.getEntries(), localManifest.getEntries());
		showMapDiff("main attribute", remoteManifest.getMainAttributes(), localManifest.getMainAttributes());
		remoteIn.close();
		localIn.close();
	}

	protected void showFileDiff(String remoteURL, String localURL) throws IOException, MalformedURLException {
		if (verbose == 0)
			return;

		String tmp = getTempDir();
		String localFile = tmp + "/local";
		String remoteFile = tmp + "/remote";
		copyFile(new URL(remoteURL).openStream(), new FileOutputStream(remoteFile));
		copyFile(new URL(localURL).openStream(), new FileOutputStream(localFile));
		boolean isClass = remoteURL.endsWith(".class");
		if (isClass) {
			float remoteVersion = CheckClassVersions.getClassVersion(new FileInputStream(remoteFile));
			float localVersion = CheckClassVersions.getClassVersion(new FileInputStream(localFile));
			if (remoteVersion != localVersion)
				out.handleLine("class versions differ! remote: " + remoteVersion + ", local: " + localVersion);
		}
		if (verbose == 3) {
			hexdump(remoteFile, remoteFile);
			hexdump(localFile, localFile);
		}
		else if (isClass) {
			if (verbose > 3) {
				analyzeByteCode(remoteFile, remoteFile);
				analyzeByteCode(localFile, localFile);
			}
			else {
				String remoteJarURL = remoteURL.substring(4, remoteURL.indexOf('!'));
				String tmpJar = tmp + remoteJarURL.substring(remoteJarURL.lastIndexOf('/')) + ".jar";
				if (!new File(tmpJar).exists())
					copyFile(new URL(remoteJarURL).openStream(), new FileOutputStream(tmpJar));
				String className = remoteURL.substring(remoteURL.indexOf("!/") + 2);
				className = className.substring(0, className.length() - 6).replace('/', '.');
				String localPath = localURL.substring(4 + 5, localURL.indexOf("!/"));
				javap(tmpJar, className, remoteFile);
				javap(localPath, className, localFile);
			}
		}
		try {
			SimpleExecuter.exec(null, out, "git", "diff", "--color=" + (color ? "always" : "auto"), "--no-index", remoteFile, localFile);
		} catch (RuntimeException e) {
			// we expect the diff to return 1 if there were differences
			if (!e.getMessage().equals("exit status: 1"))
				IJ.handleException(e);
		}
	}

	protected boolean compareToUploaded(String plugin) throws IOException, MalformedURLException {
		String remoteURL = getURLFromUpdater(plugin);
		if (remoteURL == null) {
			out.handleLine(plugin + " is not a Fiji component");
			return true;
		}
		String remotePath = null;
		String localPath = System.getProperty("ij.dir") + "/" + plugin;
		String localURL = "file:" + localPath;

		out.handleLine("Differences between the local and remote " + plugin);
		if (plugin.matches(".*\\.(js|bsh|ijm|clj|rb|py|java|m)")) {
			try {
				showFileDiff(remoteURL, localURL);
			} catch (IOException e) {
				out.handleLine("Could not diff " + remoteURL + " vs " + localURL + " (" + e + ")");
			}
			return true;
		}

		if (!plugin.matches(".*\\.(jar|zip)"))
			return true;

		List<ZipEntry> remote = getZipItems(new URL(remoteURL));
		List<ZipEntry> local = getZipItems(new URL(localURL));
		showManifestDiff(remoteURL, localURL);

		int i = 0, j = 0;
		while (i < remote.size() || j < local.size()) {
			if (i == remote.size()) {
				out.handleLine("Local file: " + local.get(j));
				j++;
			} else if (j == local.size()) {
				out.handleLine("Remote file: " + remote.get(i));
				i++;
			} else {
				ZipEntry e1 = remote.get(i);
				ZipEntry e2 = local.get(j);
				if (e1.getName().equals(e2.getName())) {
					long s1 = e1.getSize();
					long s2 = e2.getSize();
					long h1 = e1.getCrc();
					long h2 = e2.getCrc();
					if (s1 != s2 || h1 != h2) {
						out.handleLine("Differences in " + e1);
						out.handleLine((s1 != s2 ? ("Size difference " + s1 + " != " + s2) : "")
							+ " " + (h1 != h2 ? ("Hash difference " + h1 + " != " + h2) : ""));
						showFileDiff("jar:" + remoteURL + "!/" + e1.getName(), "jar:" + localURL + "!/" + e1.getName());
					}
					i++; j++;
				} else if (e1.getName().compareTo(e2.getName()) < 0) {
					out.handleLine("Remote file: " + remote.get(i));
					i++;
				} else {
					out.handleLine("Local file: " + local.get(j));
					j++;
				}
			}
			if (wasInterrupted())
				return false;
		}
		return true;
	}
}