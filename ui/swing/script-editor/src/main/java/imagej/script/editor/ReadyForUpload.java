package fiji.scripting;

/**
 * This class checks several preconditions of .jar files before uploading:
 *
 * - the source files should be committed
 * - the latest commit touching the source files should be pushed
 * - Fiji Build should report it as up-to-date
 * - no stale leftover files from old versions
 * - no debug information in the .class files
 */

import fiji.SimpleExecuter;

import fiji.build.ByteCodeAnalyzer;
import fiji.build.CompileJar;
import fiji.build.Fake;
import fiji.build.FakeException;
import fiji.build.Parser;
import fiji.build.Rule;
import fiji.build.SubFake;
import fiji.build.Util;

import ij.IJ;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ReadyForUpload {
	protected static String ijDir;

	static {
		String dir = System.getProperty("ij.dir");
		if (IJ.isWindows())
			dir = normalizeWinPath(dir);
		if (!dir.endsWith("/"))
			dir += "/";
		ijDir = dir;
	}

	protected PrintStream out;
	protected Fake fake;
	protected Parser parser;
	protected Rule rule;
	protected String path, fullPath;

	public ReadyForUpload(PrintStream printStream) {
		out = printStream;
	}

	/* helper functions */
	protected void print(String message) {
		out.println(message);
	}

	/* Fiji Build helpers */
	protected void getRule() throws FakeException, FileNotFoundException {
		path = new File(path).getAbsolutePath();
		if (IJ.isWindows())
			path = normalizeWinPath(path);
		if (!path.startsWith(ijDir)) {
			print("Warning: Not in $FIJI_ROOT: " + path);
			return;
		}

		String target = path.substring(ijDir.length());
		if (rule != null && rule.getTarget().equals(target))
			return;

		if (fake == null) {
			fake = new Fake();
			fake.out = fake.err = out;
		}
		if (parser == null) {
			parser = fake.parse(new FileInputStream(ijDir + "/Fakefile"), new File(ijDir));
			parser.parseRules(new ArrayList());
		}

		rule =  parser.getRule(target);
	}

	protected String getSourcePathForTarget(boolean fromSubFakefile) throws FakeException, FileNotFoundException {
		if (rule instanceof SubFake) {
			if (rule.getTarget().equals("plugins/loci_tools.jar"))
				return "modules/bio-formats/components/bio-formats/src/";
			if (rule.getTarget().equals("jars/batik.jar"))
				return "modules/batik/sources/";
			if (fromSubFakefile) {
				if (rule.getLastPrerequisite().equals("mpicbg/"))
					return ijDir + "mpicbg/";
				File fakefile = ((SubFake)rule).getFakefile();
				if (fakefile == null) {
					// TODO: this really needs to go into a new Rule.isClean() method, checking also for resources
					File mavenSrc = new File(rule.getWorkingDirectory(), rule.getLastPrerequisite() + "src/main/java");
					if (mavenSrc.exists())
						return mavenSrc.getAbsolutePath();
					return null;
				}
				Fake fake = new Fake();
				File cwd = new File(ijDir, rule.getLastPrerequisite());
				if (!new File(cwd, ".git").exists()) {
					print(rule.getLastPrerequisite() + " not checked out");
					return null;
				}
				Parser parser = fake.parse(new FileInputStream(fakefile), cwd);
				parser.parseRules(new ArrayList());

				String target = rule.getTarget();
				target = target.substring(target.lastIndexOf('/') + 1);
				Rule subRule = parser.getRule(target);
				if (subRule == null) {
					print("Warning: rule for " + target + " not found in " + rule.getLastPrerequisite());
					return null;
				}
				if (!(subRule instanceof CompileJar)) {
					print("Warning: ignoring rule for " + target);
					return null;
				}

				String prereq = subRule.getPrerequisiteString();
				int star = prereq.indexOf("*");
				if (star >= 0)
					prereq = prereq.substring(0, star);

				return Util.makePath(cwd, prereq);
			}
			return rule.getLastPrerequisite();
		}
		if (!(rule instanceof CompileJar)) {
			print("Warning: ignoring rule for " + rule.getTarget());
			return null;
		}

		String prereq = rule.getPrerequisiteString();
		int starstar = prereq.indexOf("**");
		if (starstar >= 0)
			prereq = prereq.substring(0, starstar);

		return ijDir + prereq;
	}

	protected boolean checkFakeTargetUpToDate() throws FakeException, FileNotFoundException {
		return rule.upToDate();
	}

	protected boolean checkDirtyFiles(File directory, String fileName) throws IOException {
		SimpleExecuter executer = new SimpleExecuter(directory, new String[] {
			"git", "ls-files", "--exclude-standard", "--other", "--modified", fileName
		});
		if (executer.getExitCode() != 0) {
			print("Failed looking for uncommitted/changed files for " + path + "\n" + executer.getError() + "\n" + executer.getOutput());
			return false;
		}
		if (!"".equals(executer.getOutput())) {
			print("Uncommitted files for " + path + ":\n" + executer.getOutput());
			return false;
		}
		return true;
	}

	protected boolean checkDirtyFiles() throws FakeException, IOException {
		// check for uncommitted files
		String sourcePath = getSourcePathForTarget(true);
		if (sourcePath == null)
			print("Warning: could not determine source directory for " + path);
		else if (!checkDirtyFiles(new File(sourcePath), "."))
			return false;

		// check whether submodules are correctly committed
		if (sourcePath == null)
			sourcePath = getSourcePathForTarget(false);
		if (sourcePath != null && new File(sourcePath, ".git").exists()) {
			File dir = new File(sourcePath);
			if (!checkDirtyFiles(dir.getParentFile(), dir.getName()))
				return false;
		}

		return true;
	}

	protected String getSourceFileName(InputStream in) throws IOException {
		byte[] code = readStream(in);
		ByteCodeAnalyzer analyzer = new ByteCodeAnalyzer(code, true);
		return analyzer.getSourceFile();
	}

	protected long getMTime(String path) {
		return new File(path).lastModified();
	}

	protected boolean checkCRLF(InputStream input) throws IOException {
		byte[] buf = new byte[1024];
		boolean result = true;
		for (;;) {
			int count = input.read(buf);
			if (count <= 0)
				break;
			for (int i = 0; i < count; i++)
				if (buf[i] == 0) {
					// assume binary
					result = true;
					break;
				}
				else if (buf[i] == 13)
					// found carriage return
					result = false;
		}
		input.close();
		return result;
	}

	protected boolean fileNameOfBinary(String filename) {
		return filename.matches("(?i).*\\.(class|pdf|jpg|png|gif)$");
	}

	protected boolean checkCRLF() throws IOException {
		boolean result = true;
		if (path.endsWith(".jar")) {
			ZipFile zip = new ZipFile(path);
			for (ZipEntry entry : Collections.list(zip.entries())) {
				String name = entry.getName();
				if (!fileNameOfBinary(name) && !name.startsWith("META-INF/") &&
						!checkCRLF(zip.getInputStream(entry))) {
					print(name + " in " + path + " contains DOS-style line endings");
					result = false;
				}
			}
			zip.close();
		}
		else if (!checkCRLF(new FileInputStream(path))) {
			print(path + " contains DOS-style line endings");
			result = false;
		}
		return result;
	}

	protected boolean checkTimestamps() throws FakeException, IOException {
		String sourcePath = getSourcePathForTarget(true);
		if (sourcePath == null) {
			print("Warning: could not determine source path for " + path);
			return false;
		}
		if (!sourcePath.endsWith(File.separator))
			sourcePath += File.separator;

		boolean result = true;
		ZipFile zip = new ZipFile(fullPath);
		for (ZipEntry entry : Collections.list(zip.entries())) {
			String name = entry.getName();
			if (name.startsWith("META-INF/"))
				continue;
			if (name.endsWith(".class")) {
				int dollar = name.indexOf('$');
				if (dollar >= 0)
					name = name.substring(0, dollar) + ".java";
				else
					name = name.substring(0, name.length() - 5) + "java";
				if (!new File(sourcePath + name).exists()) {
					String sourceName = getSourceFileName(zip.getInputStream(entry));
					if (sourceName == null) {
						print("No source file found for '" + sourcePath + entry.getName() + "'");
						continue;
					}
					name = name.substring(0, name.lastIndexOf('/') + 1) + sourceName;
				}
			}
			if (!new File(sourcePath + name).exists() &&
					(!path.endsWith("/Script_Editor.jar") ||
					 !new File(sourcePath + "templates/Java/" + name).exists()) &&
					(!path.endsWith("/ij.jar") ||
					 (!name.equals("aboutja.jpg") && !name.equals("icon.gif")))) {
				print(entry.getName() + " in " + path + " is missing the source " + name);
				result = false;
			}
			if (entry.getTime() < getMTime(sourcePath + name)) {
				print(entry.getName() + " in " + path + " is not up-to-date");
				result = false;
			}
		}
		zip.close();
		return result;
	}

	protected boolean checkPushed(String directory, String subdirectory) throws IOException {
		String upstreamBranch = "master";
		if (subdirectory.endsWith("/"))
			subdirectory = subdirectory.substring(0, subdirectory.length() - 1);
		SimpleExecuter executer = new SimpleExecuter(new File(directory), new String[] {
			"git", "log", "--oneline", "origin/" + upstreamBranch + "..", "--", subdirectory == null ? "." : subdirectory
		});
		if (executer.getExitCode() != 0) {
			print("Failed looking for unpushed commits for " + path + "\n" + executer.getError() + "\n" + executer.getOutput());
			return false;
		}
		if (!"".equals(executer.getOutput())) {
			print("Unpushed commits for " + path + ":\n" + executer.getOutput());
			return false;
		}
		return true;
	}

	protected boolean checkPushed() throws FakeException, IOException {
		String submodulePath = getSourcePathForTarget(false);
		if (fullPath.startsWith(ijDir) && submodulePath != null && !checkPushed(ijDir, submodulePath))
			return false;

		// check whether submodules are pushed
		if (submodulePath != null && new File(submodulePath, ".git").exists() && !checkPushed(submodulePath, "."))
			return false;

		return true;
	}

	protected boolean isUncheckedOutSubmodule() throws FakeException, FileNotFoundException {
		if (!(rule instanceof SubFake))
			return false;
		String submoduleDir = rule.getLastPrerequisite();
		if (submoduleDir == null)
			return false;
		File dir = new File(ijDir, submoduleDir);
		if (!dir.isDirectory())
			return true;
		for (String name : dir.list())
			if (name.charAt(0) != '.')
				return false;
		return true;
	}

	protected boolean checkPrecompiled() {
		final String baseName = new File(path).getName();
		final File precompiled = new File(new File(ijDir, "precompiled"), baseName);
		if (!precompiled.exists())
			return true;

		// compare .jar contents (timestamps can vary)
		try {
			final ZipFile jar = new ZipFile(fullPath);
			final Set<ZipEntry> files = new HashSet<ZipEntry>(Collections.list(jar.entries()));
			final ZipFile precompiledJar = new ZipFile(precompiled);
			final Set<ZipEntry> precompiledFiles = new HashSet<ZipEntry>(Collections.list(precompiledJar.entries()));

			if (files.size() != precompiledFiles.size()) {
				print(path + " differs from the precompiled one!");
				return false;
			}

			for (ZipEntry entry : files) {
				ZipEntry precompiledEntry = precompiledJar.getEntry(entry.getName());
				if (precompiledEntry == null) {
					print("The precompiled " + path + " lacks the file " + entry.getName() + "!");
					return false;
				}
				InputStream in = jar.getInputStream(entry);
				InputStream precompiledIn = precompiledJar.getInputStream(precompiledEntry);
				if (streamCompare(in, precompiledIn) != 0) {
					print(path + " differs from the precompiled one (" + entry.getName() + ")!");
					return false;
				}
			}
		} catch (IOException e) {
			print("Could not compare " + path + " to the precompiled one!");
			return false;
		}
		return true;
	}

	protected int streamCompare(InputStream in1, InputStream in2) throws IOException {
		byte[] buffer1 = new byte[32768];
		byte[] buffer2 = new byte[32768];

		for (;;) {
			int count1 = in1.read(buffer1);
			if (count1 < 0) {
				in1.close();
				int count2 = in2.read(buffer2);
				in2.close();
				if (count2 >= 0)
					return -1;
				return 0;
			}

			for (int i1 = 0; i1 < count1; ) {
				int count2 = in2.read(buffer2, 0, count1 - i1);
				if (count2 < 0) {
					in1.close();
					in2.close();
					return +1;
				}
				for (int i2 = 0; i2 < count2; i2++)
					if (buffer1[i1 + i2] != buffer2[i2])
						return buffer1[i1 + i2] - buffer2[i2];
				i1 += count2;
			}
		}

	}

	protected static Set<String> textFileExtensions;
	static {
		textFileExtensions = new HashSet<String>(Arrays.asList(new String[] {
			"bsh", "c", "clj", "h", "ijm", "js", "m", "py", "rb", "txt"
		}));
	}

	public synchronized boolean check(String path) {
		if (IJ.isWindows())
			path = normalizeWinPath(path);

		int dot = path.lastIndexOf('.');
		String extension = "";
		if (dot > path.length() - 5)
			extension = path.substring(dot + 1);
		boolean isText = textFileExtensions.contains(extension);

		boolean result = true;
		rule = null;
		try {
			this.path = path;
			fullPath = new File(path).getAbsolutePath();
			if (IJ.isWindows())
				fullPath = normalizeWinPath(fullPath);

			if (!isText && !extension.equals("jar")) {
				print("Warning: ignoring " + path);
				return true;
			}

			if (!checkCRLF()) {
				print("");
				result = false;
			}

			if (isText)
				return result;

			if (containsDebugInfo(fullPath)) {
				if (new File(path).getCanonicalPath().equals(new File(ijDir, "plugins/loci_tools.jar").getCanonicalPath()))
					print("Ignoring debug info in Bio-Formats");
				else {
					print(path + " contains debug information\n");
					result = false;
				}
			}
			getRule();
			if (rule == null)
				print("Warning: " + path + " not made using Fiji Build!\n");
			else if (isUncheckedOutSubmodule())
				print("Warning: source for " + path + " not checked out!\n");
			else {
				if (!checkTimestamps()) {
					print("");
					result = false;
				}
				if (!checkFakeTargetUpToDate()) {
					print(path + " is not up-to-date\n");
					result = false;
				}
				if (!checkDirtyFiles())
					result = false;
				if (!checkPushed())
					result = false;
				if (!checkPrecompiled())
					result = false;
			}
		} catch (Exception e) {
			e.printStackTrace(out);
			print("Error opening " + path + ": " + e);
			return false;
		}
		return result;
	}

	protected static String normalizeWinPath(String path) {
		try {
			return new File(path).getCanonicalPath().replace('\\', '/');
		} catch (IOException e) {
			return path;
		}
	}

	protected static byte[] readStream(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[16384];
		for (;;) {
			int count = in.read(buffer);
			if (count < 0)
				break;
			out.write(buffer, 0, count);
		}
		in.close();
		out.close();
		return out.toByteArray();
	}

	public static boolean containsDebugInfo(String filename) throws IOException {
		if (!filename.endsWith(".jar") || !new File(filename).exists())
			return false;

		final ZipFile jar = new ZipFile(filename);
		for (ZipEntry file : Collections.list(jar.entries())) {
			if (!file.getName().endsWith(".class"))
				continue;

			InputStream input = jar.getInputStream(file);
			byte[] code = readStream(input);
			ByteCodeAnalyzer analyzer = new ByteCodeAnalyzer(code, true);
			if (analyzer.containsDebugInfo())
				return true;
		}
		return false;
	}

	public static void main(String[] args) {
		if (args == null || args.length == 0 || (args.length == 1 && args[0].equals("")))
			args = new String[] { "plugins/Fiji_Plugins.jar" };

		ReadyForUpload checker = new ReadyForUpload(System.err);
		boolean result = true;
		for (String arg : args)
			if (!checker.check(arg))
				result = false;
		if (!result)
			checker.print("FAILED!");
	}
}