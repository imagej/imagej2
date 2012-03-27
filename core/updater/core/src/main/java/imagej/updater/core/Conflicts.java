/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
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

import imagej.updater.core.FileObject.Action;
import imagej.updater.core.FileObject.Status;
import imagej.updater.core.FilesCollection.DependencyMap;
import imagej.updater.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A class describing conflicts in what is selected for updating/uploading
 * 
 * @author Johannes Schindelin
 */

public class Conflicts {

	protected final FilesCollection files;
	protected List<Conflict> conflicts;

	public static class Conflict {

		protected final boolean isError, isCritical;
		protected final String filename, conflict;
		protected final Resolution[] resolutions;

		public Conflict(final FileObject file, final String conflict,
			final Resolution... resolutions)
		{
			this(true, file, conflict, resolutions);
		}

		public Conflict(final boolean isError, final FileObject file,
			final String conflict, final Resolution... resolutions)
		{
			this(isError, false, file, conflict, resolutions);
		}

		public Conflict(final boolean isError, final boolean isCritical,
			final FileObject file, final String conflict,
			final Resolution... resolutions)
		{
			this.isError = isError;
			this.isCritical = isCritical;
			this.filename = file == null ? null : file.getFilename();
			this.conflict = conflict;
			this.resolutions = resolutions;
		}

		public boolean isError() {
			return isError;
		}

		public boolean isCritical() {
			return isCritical;
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
	}

	public abstract static class Resolution {

		protected final String description;

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
			conflicts
				.add(new Conflict(
					null,
					"There are files which need to be updated/installed since other files depend on them",
					actionResolution("Install " + Util.join(", ", automatic), automatic,
						Action.INSTALL, Action.UPDATE)));
		}
	}

	protected Conflict
		bothInstallAndUninstall(final FileObject file,
			final FilesCollection installReasons,
			final FilesCollection obsoleteReasons)
	{
		return new Conflict(file, "Required by \n\n" + installReasons +
			"\nbut made obsolete by\n\n" + obsoleteReasons, ignoreResolution(
			"Ignore this issue", file), actionResolution("Do not update " +
			installReasons, installReasons));
	}

	protected Conflict needUninstall(final FileObject file,
		final FilesCollection obsoleteReasons)
	{
		return new Conflict(file, "Locally modified but made obsolete by\n\n" +
			obsoleteReasons, actionResolution("Uninstall " + file, file,
			Action.UNINSTALL), actionResolution("Do not update " + obsoleteReasons,
			obsoleteReasons));
	}

	protected Conflict locallyModified(final FileObject file,
		final FilesCollection installReasons)
	{
		final boolean toInstall = file.getStatus().isValid(Action.INSTALL);
		return new Conflict(false, file,
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
				if (dep.isInstallable() ||
					(dep.isLocalOnly() && dep.getAction() != Action.UPLOAD) ||
					dep.isObsolete() ||
					(dep.getStatus().isValid(Action.UPLOAD) && dep.getAction() != Action.UPLOAD)) toUpload
					.add(dep, file);
			}
		}
		for (final FileObject file : toUpload.keySet())
			conflicts.add(needUpload(file, toUpload.get(file)));

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
				else {
					if (dependencyObject == null ||
						dependencyObject.getStatus() == Status.LOCAL_ONLY) conflicts
						.add(dependencyNotUploaded(file, dependency.filename));
					else if (dependencyObject.isObsolete() ||
						dependencyObject.getAction() == Action.REMOVE) conflicts
						.add(dependencyRemoved(file, dependency.filename));
				}
			}
		}
	}

	protected Conflict timestampChanged(final FileObject file) {
		return new Conflict(file, "The timestamp of " + file +
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
		return new Conflict(true, localOnly || obsolete, file, (localOnly
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
		return new Conflict(obsoleted, "The file " + obsoleting.getFilename() +
			" overrides the file " + obsoleted.getFilename() + ", but " +
			obsoleted.getFilename() + " was not removed", resolutions
			.toArray(new Resolution[resolutions.size()]));
	}

	protected Conflict dependencyNotUploaded(final FileObject file,
		final String dependency)
	{
		return new Conflict(file, "Depends on " + dependency +
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
		return new Conflict(true, file, "Depends on " + dependency +
			" which is about to be removed.", resolutions
			.toArray(new Resolution[resolutions.size()]));
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

}
