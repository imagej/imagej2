/*
 * Programmatic Source>Clean Up... on the current file.
 *
 * Needs EclipseScript: Help>Install New Software..., Add
 * http://eclipsescript.org/updates/, select "EclipseScript".
 *
 * Then, Ctrl+4 (or on MacOSX, Command+4) -- *not* Ctrl+*F4*!
 * -- opens a command launcher look-alike with which you can
 * launch this script.
 */

importClass(Packages.java.io.BufferedReader);
importClass(Packages.java.io.InputStreamReader);
importClass(Packages.java.io.OutputStreamWriter);
importClass(Packages.java.lang.Runtime);
importClass(Packages.java.lang.StringBuilder);
importClass(Packages.java.lang.Thread);

importClass(Packages.org.eclipse.core.runtime.NullProgressMonitor);
importClass(Packages.org.eclipse.jdt.core.JavaCore);
importClass(Packages.org.eclipse.jdt.internal.corext.fix.CleanUpRefactoring);
importClass(Packages.org.eclipse.jdt.internal.ui.JavaPlugin);
importClass(Packages.org.eclipse.ltk.core.refactoring.CreateChangeOperation);
importClass(Packages.org.eclipse.ltk.core.refactoring.CheckConditionsOperation);
importClass(Packages.org.eclipse.ltk.core.refactoring.PerformChangeOperation);
importClass(Packages.org.eclipse.ltk.core.refactoring.RefactoringCore);
importClass(Packages.org.eclipse.ltk.core.refactoring.RefactoringStatus);

var cleanUp = function(file, monitor) {
	// File>Refresh
	file.getProject().refreshLocal(10, monitor);

	var compilationUnit = JavaCore.create(file);
	if (compilationUnit == null) throw 'Not a Java file? ' + file;

	var refactoring = new CleanUpRefactoring();
	refactoring.setUseOptionsFromProfile(true);
	refactoring.addCompilationUnit(compilationUnit);

	var cleanUps = JavaPlugin.getDefault().getCleanUpRegistry().createCleanUps();
	for (var i = 0; i < cleanUps.length; i++) {
		refactoring.addCleanUp(cleanUps[i]);
	}

	var undoManager = RefactoringCore.getUndoManager();
	var create = new CreateChangeOperation(
		new CheckConditionsOperation(refactoring, CheckConditionsOperation.ALL_CONDITIONS),
		RefactoringStatus.FATAL);
	var perform = new PerformChangeOperation(create);
	// Source>Clean Up...
	try {
		eclipse.resources.workspace.run(perform, monitor);
	} catch (e) {
		eclipse.console.println("Encountered Heisenbug!");
		Thread.currentThread().sleep(500);
		// sleep a bit, try again.
		eclipse.resources.workspace.run(perform, monitor);
	}
}

var editedFile = eclipse.editors.file;
if (JavaCore.create(editedFile) == null) throw 'Not a Java file? ' + editedFile;
var file = editedFile.getLocation().toFile();
var directory = file.getParentFile();

var trace = false;
system = function(commandLine, stdin) {
	if (trace) {
		eclipse.console.println("TRACE: " + commandLine);
	}
	var process = new Runtime.getRuntime().exec(commandLine, null, directory);
	var stderrReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
	var stderrDumper = new Thread({
		run: function() {
			for (;;) {
				var line = stderrReader.readLine();
				if (line == null) break;
				eclipse.console.println(line);
			}
			stderrReader.close();
		}
	})
	var output = new StringBuilder();
	var stdoutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	var stdoutDumper = new Thread({
		run: function() {
			for (;;) {
				var line = stdoutReader.readLine();
				if (line == null) break;
				if (output.length() > 0) output.append("\n");
				output.append(line);
			}
			stdoutReader.close();
		}
	})
	stderrDumper.start();
	stdoutDumper.start();
	var stdinWriter = new OutputStreamWriter(process.getOutputStream());
	if (stdin != undefined) {
		stdinWriter.write(stdin);
	}
	stdinWriter.close();
	process.waitFor();
	stderrDumper.join();
	stdoutDumper.join();
	var exitValue = process.exitValue();
	if (exitValue != 0) {
		throw "Error (exit code: " + exitValue + ") executing:\n\t" + commandLine.join(" ")
			+ "\nOutput so far:\n" + output + "\n";
	}
	return output.toString();
}

git = function(arguments, stdin) {
	arguments.unshift("git");
	return system(arguments, stdin);
}

/* Rhino as shipped with EclipseScript has no startsWith() */
if (!String.prototype.startsWith) {
	Object.defineProperty(String.prototype, 'startsWith', {
		value: function (searchString) {
			return this.indexOf(searchString, 0) === 0;
		}
	});
}

var commitMap = {}
/* Rewrites the original commit with the current tree and the rewritten parents */
fixup_commit = function(originalCommit) {
	var tree = git(['write-tree']);
	lines = git(['cat-file', 'commit', originalCommit]).split('\n');
	for (var i in lines) {
		var line = lines[i];
		if (line.startsWith('tree ')) {
			lines[i] = 'tree ' + tree;
		} else if (line.startsWith('parent ')) {
			var fixupped = commitMap[line.substring(7)];
			if (fixupped != undefined) {
				lines[i] = 'parent ' + fixupped;
			}
		} else if (line.startsWith('committer ')) {
			lines[i] = 'committer ' + git(['var', 'GIT_COMMITTER_IDENT']);
		} else if (lines[i] == '') {
			break;
		}
	}
	var fixupped = git(['hash-object', '-t', 'commit', '-w', '--stdin'], lines.join('\n'));
	commitMap[originalCommit] = fixupped;
	git(['checkout', '-q', fixupped])
}

tip = git(['rev-parse', 'HEAD']);
if (git(['diff-index', '--ignore-submodules', '--cached', 'HEAD', '--']) != '' ||
		git(['diff-files', '--ignore-submodules']) != '')
	throw 'Uncommitted changes!';
branchName = git(['rev-parse', '--symbolic-full-name', 'HEAD']);
if (branchName == '') throw "Cannot determine current branch name!";
if (!branchName.startsWith('refs/heads/')) throw "Refusing to work on funny branch name: " + branchName;
mergeBase = git(['merge-base', 'HEAD', 'origin/master']);
if (mergeBase == '') throw "Cannot determine merge base!";

commits = git(['rev-list', '--reverse', mergeBase + '..']).split("\n");
eclipse.console.println('About to process ' + commits.length + ' commits (' + mergeBase + '..)');
eclipse.runtime.schedule(function run(monitor) {
	for (var line in commits) {
		if (monitor.canceled) return false;
		var commit = commits[line++];
		eclipse.console.println("Processing " + line + "/" + commits.length + ": " + commit);
		git(['checkout', '-q', commit]);
		cleanUp(editedFile, monitor);
		git(['add', file.getAbsolutePath()]);
		fixup_commit(commit);
		eclipse.console.println("Cleaned up " + file + " in " + commit.substring(0, 8)
			+ " (-> " + commitMap[commit].substring(0, 8) + ")");
		monitor.worked(1);
	}

	git(['update-ref', '-m', 'Updated from ' + tip.substring(0, 8) + ' by code-cleanup.eclipse.js',
		branchName, commitMap[tip], tip]);
	git(['checkout', branchName.substring(11)]);
});