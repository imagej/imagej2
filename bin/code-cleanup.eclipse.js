/*
 * Programmatic Source>Clean Up... on the current file.
 *
 * Needs EclipseScript: Help>Install New Software..., Add
 * http://eclipsescript.org/updates/, select "EclipseScript".
 */

importClass(Packages.org.eclipse.core.runtime.NullProgressMonitor);
importClass(Packages.org.eclipse.jdt.core.JavaCore);
importClass(Packages.org.eclipse.jdt.internal.corext.fix.CleanUpRefactoring);
importClass(Packages.org.eclipse.jdt.internal.ui.JavaPlugin);
importClass(Packages.org.eclipse.ltk.core.refactoring.CreateChangeOperation);
importClass(Packages.org.eclipse.ltk.core.refactoring.CheckConditionsOperation);
importClass(Packages.org.eclipse.ltk.core.refactoring.PerformChangeOperation);
importClass(Packages.org.eclipse.ltk.core.refactoring.RefactoringCore);
importClass(Packages.org.eclipse.ltk.core.refactoring.RefactoringStatus);

var editedFile = eclipse.editors.file;
// File>Refresh
editedFile.refreshLocal(1, new NullProgressMonitor());

var compilationUnit = JavaCore.create(editedFile);

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

eclipse.console.println("Cleaned up sources of " + editedFile);
