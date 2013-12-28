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

package imagej.updater.core;

import imagej.updater.core.Conflicts.Conflict.Severity;
import imagej.updater.core.FileObject.Action;
import imagej.updater.core.FileObject.Status;
import imagej.updater.core.FilesCollection.DependencyMap;
import imagej.updater.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A class describing conflicts in what is selected for updating/uploading.
 * 
 * @author Johannes Schindelin
 */
public class Conflicts {

	private final FilesCollection files;
	protected List<Conflict> conflicts;

	/**
	 * An upload or download conflict requiring user resolution.
	 *
	 * A conflict can be associated with a specific FileObject, can be an
	 * error, can be critical and have an arbitrary number of {@link
	 * Resolution}s.
	 */
	public static class Conflict {

		public enum Severity {
			CRITICAL_ERROR,
			ERROR,
			WARNING,
			NOTICE;

			@Override
			public String toString() {
				return Util.toCamelCase(name());
			}
		}
		private final Severity severity;
		protected final String filename;
		private final String conflict;
		protected final Resolution[] resolutions;

		@Deprecated
		public Conflict(final FileObject file, final String conflict,
			final Resolution... resolutions)
		{
			this(Severity.ERROR, file, conflict, resolutions);
		}

		@Deprecated
		public Conflict(final boolean isError, final FileObject file,
			final String conflict, final Resolution... resolutions)
		{
			this(isError ? Severity.ERROR : Severity.WARNING, file, conflict, resolutions);
		}

		@Deprecated
		public Conflict(final boolean isError, final boolean isCritical,
			final FileObject file, final String conflict,
			final Resolution... resolutions)
		{
			this(isCritical ? Severity.CRITICAL_ERROR : (isError ? Severity.ERROR : Severity.WARNING),
					file == null ? null : file.getFilename(), conflict, resolutions);
		}

		@Deprecated
		public Conflict(final boolean isError, final boolean isCritical,
				final String filename, final String conflict,
				final Resolution... resolutions) {
			this(isCritical ? Severity.CRITICAL_ERROR : (isError ? Severity.ERROR : Severity.WARNING),
					filename, conflict, resolutions);
		}

		public Conflict(final Severity severity,
				final FileObject file, final String conflict,
				final Resolution... resolutions) {
			this(severity, file == null ? null : file.getFilename(), conflict, resolutions);
		}

		public Conflict(final Severity severity,
			final String filename, final String conflict,
			final Resolution... resolutions) {
			this.severity = severity;
			this.filename = filename;
			this.conflict = conflict;
			this.resolutions = resolutions;
		}

		public Severity getSeverity() {
			return severity;
		}

		public boolean isError() {
			return severity.compareTo(Severity.ERROR) <= 0;
		}

		public boolean isCritical() {
			return severity.compareTo(Severity.CRITICAL_ERROR) <= 0;
		}

		public String getFilename() {
			return filename;
		}

		public String getConflict() {
			return conflict;
		}

		public Resolution[] getResolutions() {
			return resolutions;
		}

		@Override
		public String toString() {
			return getFilename() + ": " + getConflict();
		}
	}

	/**
	 * Part of the conflict list, but really only a non-critical notice.
	 */
	public static class Notice extends Conflict {
		public Notice(final String message) {
			super(Severity.NOTICE, (String)null, message);
		}
	}

	public abstract static class Resolution {

		private final String description;

		public Resolution(final String description) {
			this.description = description;
		}

		public String getDescription() {
			return description;
		}

		public abstract void resolve();
	}

	public Conflicts(final FilesCollection files) {
		this.files = files;
	}

	public Iterable<Conflict> getConflicts(final boolean forUpload) {
		conflicts = new ArrayList<Conflict>();
		if (!forUpload) listUpdateIssues();
		else listUploadIssues();
		return conflicts;
	}

	protected void listUpdateIssues() {
		final DependencyMap toInstall = files.getDependencies(false);
		final DependencyMap obsoleted = files.getDependencies(true);
		final Set<FileObject> automatic = new LinkedHashSet<FileObject>();

		for (final FileObject file : toInstall.keySet())
			if (obsoleted.get(file) != null) conflicts.add(bothInstallAndUninstall(
				file, toInstall.get(file), obsoleted.get(file)));
			else if (!file.willBeUpToDate()) {
				if (file.isLocallyModified()) {
					if (!files.ignoredConflicts.contains(file)) conflicts
						.add(locallyModified(file, toInstall.get(file)));
				}
				else automatic.add(file);
			}

		for (final FileObject file : obsoleted.keySet())
			if (toInstall.get(file) != null || // handled above
				!file.willNotBeInstalled()) conflicts.add(needUninstall(file, obsoleted
				.get(file)));

		if (automatic.size() > 0) {
			for (final FileObject file : automatic) {
				file.setFirstValidAction(files, Action.UPDATE, Action.INSTALL);
			}
			conflicts
				.add(new Notice(
					"There are files which need to be updated/installed since other files depend on them:\n"
					+ Util.join(", ", automatic)));
		}
	}

	protected Conflict
		bothInstallAndUninstall(final FileObject file,
			final FilesCollection installReasons,
			final FilesCollection obsoleteReasons)
	{
		return new Conflict(Severity.ERROR, file, "Required by \n\n" + installReasons +
			"\nbut made obsolete by\n\n" + obsoleteReasons, ignoreResolution(
			"Ignore this issue", file), actionResolution("Do not update " +
			installReasons, installReasons));
	}

	protected Conflict needUninstall(final FileObject file,
		final FilesCollection obsoleteReasons)
	{
		return new Conflict(Severity.ERROR, file, "Locally modified but made obsolete by\n\n" +
			obsoleteReasons, actionResolution("Uninstall " + file, file,
			Action.UNINSTALL), actionResolution("Do not update " + obsoleteReasons,
			obsoleteReasons));
	}

	protected Conflict locallyModified(final FileObject file,
		final FilesCollection installReasons)
	{
		final boolean toInstall = file.getStatus().isValid(Action.INSTALL);
		return new Conflict(Severity.WARNING, file,
			"Locally modified and the Updater cannot determine its " +
				"status. A newer version might be required by\n\n" + installReasons,
			ignoreResolution("Keep the local version", file), actionResolution(
				(toInstall ? "Install" : "Update") + " " + file, file, toInstall
					? Action.INSTALL : Action.UPDATE));
	}

	protected void listUploadIssues() {
		final DependencyMap toUpload = new FilesCollection.DependencyMap();
		for (final FileObject file : files.toUpload()) {
			if (file.getTimestamp() != Util.getTimestamp(files.prefix(file))) {
				conflicts.add(timestampChanged(file));
			}
			for (final Dependency dependency : file.getDependencies()) {
				final FileObject dep = files.get(dependency.filename);
				if (dep == null || files.ignoredConflicts.contains(dep)) continue;
				if (dep.getAction() != Action.UPLOAD
						&& (dep.isInstallable() || dep.isLocalOnly()
								|| dep.isObsolete() || dep.getStatus().isValid(
								Action.UPLOAD)))
					toUpload.add(dep, file);
			}
			// test whether there are conflicting versions of the same file
			if (!files.ignoredConflicts.contains(file) && file.filename.endsWith(".jar")) {
				String baseName = file.getBaseName();
				int slash = baseName.lastIndexOf('/');
				String prefix = baseName.substring(0, slash + 1);
				baseName = baseName.substring(slash + 1);
				for (final String name : files.prefix(file).getParentFile().list()) {
					final String prefixed = prefix + name;
					if (name.startsWith(baseName) && !file.filename.equals(prefixed) && file.getFilename(true).equals(FileObject.getFilename(prefixed, true))) {
						conflicts.add(conflictingVersions(file, files.prefix(prefixed), prefixed));
					}
				}
			}
		}
		for (final FileObject file : toUpload.keySet()) {
			conflicts.add(needUpload(file, toUpload.get(file)));
		}

		// Replace dependencies on to-be-removed files
		for (final FileObject file : files.managedFiles()) {
			if (file.getAction() == Action.REMOVE) continue;
			for (final Dependency dependency : file.getDependencies()) {
				final FileObject dependencyObject = files.get(dependency.filename);
				if (dependency.overrides) {
					if (dependencyObject != null && !dependencyObject.isObsolete() &&
						!dependencyObject.willNotBeInstalled()) conflicts
						.add(dependencyObsoleted(file, dependencyObject));
				}
				else if (dependencyObject == null || dependencyObject.getAction() != Action.UPLOAD) {
					if (dependencyObject == null
							|| dependencyObject.getStatus() == Status.LOCAL_ONLY) {
						conflicts.add(dependencyNotUploaded(file,
								dependency.filename));
					} else if (dependencyObject.isObsolete()
							|| (dependencyObject.getAction() == Action.REMOVE && !dependencyObject.overridesOtherUpdateSite())) {
						conflicts.add(dependencyRemoved(file,
								dependency.filename));
					}
				}
			}
		}
	}

	protected Conflict timestampChanged(final FileObject file) {
		return new Conflict(Severity.ERROR, file, "The timestamp of " + file +
			" changed in the meantime", new Resolution(
			"Recalculate checksum and dependencies of " + file)
		{

			@Override
			public void resolve() {
				final Checksummer checksummer = new Checksummer(files, null);
				checksummer.updateFromLocal(Collections.singletonList(file
					.getFilename()));
				files.updateDependencies(file);
			}
		});
	}

	protected Conflict needUpload(final FileObject file,
		final FilesCollection uploadReasons)
	{
		final boolean localOnly = file.isLocalOnly();
		final boolean notInstalled = file.isInstallable();
		final boolean obsolete = file.isObsolete();
		final List<Resolution> resolutions = new ArrayList<Resolution>();
		if (!localOnly && !obsolete) {
			resolutions.add(ignoreResolution("Do not upload " + file, file));
		}
		if (!notInstalled) {
			resolutions.add(actionResolution("Upload " + file +
				(obsolete ? " again" : ""), file, Action.UPLOAD));
		}
		resolutions.add(new Resolution("Break the dependency") {

			@Override
			public void resolve() {
				for (final FileObject other : uploadReasons)
					other.removeDependency(file.getFilename());
			}
		});
		return new Conflict(localOnly || obsolete ? Severity.CRITICAL_ERROR : Severity.ERROR, file, (localOnly
			? "Not uploaded yet" : "Is " +
				(notInstalled ? "not installed" : (obsolete ? "marked obsolete"
					: "locally modified"))) +
			" but a dependency of\n\n" + uploadReasons, resolutions
			.toArray(new Resolution[resolutions.size()]));
	}

	private Conflict dependencyObsoleted(final FileObject obsoleting,
		final FileObject obsoleted)
	{
		final List<Resolution> resolutions = new ArrayList<Resolution>();
		resolutions
			.add(new Resolution("Do not obsolete " + obsoleted.getFilename()) {

				@Override
				public void resolve() {
					obsoleting.removeDependency(obsoleted.getFilename());
				}
			});
		if (obsoleting.updateSite.equals(obsoleted.updateSite)) {
			resolutions.add(new Resolution("Remove " + obsoleted.getFilename() +
				" from the Update Site")
			{

				@Override
				public void resolve() {
					obsoleted.setAction(files, Action.REMOVE);
					files.prefix(obsoleted).delete();
				}
			});
		}
		return new Conflict(Severity.ERROR, obsoleted, "The file " + obsoleting.getFilename() +
			" overrides the file " + obsoleted.getFilename() + ", but " +
			obsoleted.getFilename() + " was not removed", resolutions
			.toArray(new Resolution[resolutions.size()]));
	}

	protected Conflict dependencyNotUploaded(final FileObject file,
		final String dependency)
	{
		return new Conflict(Severity.ERROR, file, "Depends on " + dependency +
			" which was not uploaded.", dependencyResolution("Break the dependency",
			file, dependency, null));
	}

	protected Conflict dependencyRemoved(final FileObject file,
		final String dependency)
	{
		final List<Resolution> resolutions = new ArrayList<Resolution>();
		resolutions.add(dependencyResolution("Break the dependency", file,
			dependency, null));
		for (final FileObject toUpload : files.toUpload()) {
			if (file.hasDependency(toUpload.getFilename())) continue;
			resolutions.add(dependencyResolution("Replace with dependency to " +
				toUpload, file, dependency, toUpload.getFilename()));
			resolutions.add(dependencyResolution("Replace all dependencies on " +
				dependency + " with " + toUpload, null, dependency, toUpload
				.getFilename()));
		}
		return new Conflict(Severity.ERROR, file, "Depends on " + dependency +
			" which is about to be removed.", resolutions
			.toArray(new Resolution[resolutions.size()]));
	}

	protected Conflict conflictingVersions(final FileObject file, final File otherFile, final String otherFileName) {
		return new Conflict(Severity.ERROR, file, "Conflicting version found: " + otherFileName,
				deleteFile("Delete " + otherFileName + " (dangerous!)", otherFile),
				ignoreResolution("Ignore the problem (also dangerous!)", file));
	}

	protected Resolution ignoreResolution(final String description,
		final FileObject file)
	{
		return new Resolution(description) {

			@Override
			public void resolve() {
				files.ignoredConflicts.add(file);
			}
		};
	}

	protected Resolution deleteFile(final String description, final File file) {
		return new Resolution(description) {

			@Override
			public void resolve() {
				file.delete();
			}
		};
	}

	protected Resolution actionResolution(final String description,
		final FileObject file, final Action... actionsToTry)
	{
		return actionResolution(description, Collections.singleton(file),
			actionsToTry);
	}

	protected Resolution actionResolution(final String description,
		final Iterable<FileObject> files, final Action... actionsToTry)
	{
		return new Resolution(description) {

			@Override
			public void resolve() {
				for (final FileObject file : files) {
					file.setFirstValidAction(Conflicts.this.files, actionsToTry);
				}
			}
		};
	}

	protected Resolution dependencyResolution(final String description,
		final FileObject file, final String removeDependency,
		final String addDependency)
	{
		return new Resolution(description) {

			@Override
			public void resolve() {
				if (file != null) replaceDependency(file, removeDependency,
					addDependency);
				else for (final FileObject file : files) {
					if (file.hasDependency(removeDependency)) replaceDependency(file,
						removeDependency, addDependency);
				}
			}
		};
	}

	protected void replaceDependency(final FileObject file,
		final String removeDependency, final String addDependency)
	{
		file.removeDependency(removeDependency);
		if (addDependency != null) file.addDependency(addDependency, files
			.prefix(addDependency));
	}

	public boolean hasDownloadConflicts() {
		conflicts = new ArrayList<Conflict>();
		listUpdateIssues();
		return conflicts.size() > 0;
	}

	public boolean hasUploadConflicts() {
		conflicts = new ArrayList<Conflict>();
		listUploadIssues();
		return conflicts.size() > 0;
	}

	/**
	 * Determine whether a list of conflicts requires user feedback.
	 *
	 * There are {@link Conflict}s which do not require the user to
	 * choose between resolutions, but which are merely notifications.
	 * The upload (or download) should not be stopped by those.
	 */
	public static boolean needsFeedback(Iterable<Conflict> conflicts) {
		if (conflicts == null) return false;
		for (final Conflict conflict : conflicts) {
			if (conflict instanceof Notice) continue;
			return true;
		}
		return false;
	}

}
