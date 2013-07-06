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

system = function(commandLine) {
	var process = new Runtime.getRuntime().exec(commandLine);
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
	process.getOutputStream().close();
	stderrDumper.join();
	stdoutDumper.join();
	var exitValue = process.exitValue();
	if (exitValue != 0) {
		throw "Error (exit code: " + exitValue + ") executing:\n\t" + commandLine.join(" ")
			+ "\nOutput so far:\n" + output + "\n";
	}
	return output.toString();
}

git = function(arguments) {
	if (typeof(arguments) == 'string') {
		arguments = [ arguments ];
	}
	arguments.unshift("git");
	return system(arguments);
}

eclipse.console.println("git version: " + git('version'));

var editedFile = eclipse.editors.file;
cleanUp(editedFile);
eclipse.console.println("Cleaned up sources of " + editedFile);
