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

println = function(arg) {
	eclipse.console.println(arg);
}

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

var cleanUp = function(file) {
	// File>Refresh
	file.refreshLocal(1, new NullProgressMonitor());

	var compilationUnit = JavaCore.create(file);

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
	var perform = PerformChangeOperation(create);
	// Source>Clean Up...
	eclipse.resources.workspace.run(perform, new NullProgressMonitor());

}

var editedFile = eclipse.editors.file;
var file = editedFile.getLocation().toFile();
var directory = file.getParentFile();

system = function(commandLine, stdin) {
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
	if (typeof(arguments) == 'string') {
		arguments = [ arguments ];
	}
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
	var tree = git('write-tree');
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
  git(['checkout', fixupped])
}

tip = git(['rev-parse', 'HEAD']);
branchName = git(['rev-parse', '--symbolic-full-name', 'HEAD']);
if (branchName == '') throw "Cannot determine current branch name!";
mergeBase = git(['merge-base', 'HEAD', 'origin/master']);
if (mergeBase == '') throw "Cannot determine merge base!";
mergeBase = '1a1bc4be622802b1d2a2292ec77906baca747cd9^';

commits = git(['rev-list', '--reverse', mergeBase + '..']).split("\n");
for (var line in commits) {
	var commit = commits[line];
	git('checkout', commit);
	cleanUp(editedFile);
	git('add', file.getAbsolutePath());
	fixup_commit(commit);
	eclipse.console.println("Cleaned up " + file + " in " + commit.substring(0, 8)
		+ " (-> " + commitMap[commit].substring(0, 8) + ")");
}

git(['update-ref', '-m', 'Updated from ' + tip + ' by code-cleanup.eclipse.js',
     branchName, commitMap[tip], tip]);
git(['checkout', branchName]);