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

import imagej.updater.util.Util;
import imagej.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO
 * 
 * @author Johannes Schindelin
 */
public class FileObject {

	public static class Version implements Comparable<Version> {

		public String checksum;
		// This timestamp is not a Unix epoch!
		// Instead, it is Long.parseLong(Util.timestamp(epoch))
		public long timestamp;

		public String filename; // optional (can be different from FileObject.filename if the version was different

		Version(final String checksum, final long timestamp) {
			this.checksum = checksum;
			this.timestamp = timestamp;
		}

		@Override
		public int compareTo(final Version other) {
			final long diff = timestamp - other.timestamp;
			if (diff != 0) return diff < 0 ? -1 : +1;
			return checksum.compareTo(other.checksum);
		}

		@Override
		public boolean equals(final Object other) {
			return other instanceof Version ? equals((Version) other) : false;
		}

		public boolean equals(final Version other) {
			return timestamp == other.timestamp && checksum.equals(other.checksum);
		}

		@Override
		public int hashCode() {
			return (checksum == null ? 0 : checksum.hashCode()) ^
				new Long(timestamp).hashCode();
		}

		@Override
		public String toString() {
			return "Version(" + checksum + ";" + timestamp + ")";
		}
	}

	public static enum Action {
		// no changes
		LOCAL_ONLY("Local-only"), NOT_INSTALLED("Not installed"), INSTALLED(
				"Up-to-date"), UPDATEABLE("Update available"), MODIFIED(
				"Locally modified"), NEW("New file"), OBSOLETE("Obsolete"),

		// changes
		UNINSTALL("Uninstall it"), INSTALL("Install it"), UPDATE("Update it"),
		// TODO: FORCE_UPDATE

		// developer-only changes
		UPLOAD("Upload it", true), REMOVE("Remove it", true);

		private String label;
		private boolean uploadersOnly;

		Action(final String label) {
			this(label, false);
		}

		Action(final String label, final boolean uploadersOnly) {
			this.label = label;
			this.uploadersOnly = uploadersOnly;
		}

		@Override
		public String toString() {
			return label;
		}
	}

	public static enum Status {
		NOT_INSTALLED(Action.NOT_INSTALLED, Action.INSTALL, Action.REMOVE),
		INSTALLED(Action.INSTALLED, Action.UNINSTALL),
		UPDATEABLE(Action.UPDATEABLE, Action.UNINSTALL, Action.UPDATE, Action.UPLOAD),
		MODIFIED(Action.MODIFIED, Action.UNINSTALL, Action.UPDATE, Action.UPLOAD),
		LOCAL_ONLY(Action.LOCAL_ONLY, Action.UNINSTALL, Action.UPLOAD),
		NEW(Action.NEW, Action.INSTALL, Action.REMOVE),
		OBSOLETE_UNINSTALLED( Action.NOT_INSTALLED),
		OBSOLETE(Action.OBSOLETE, Action.UNINSTALL, Action.UPLOAD),
		OBSOLETE_MODIFIED( Action.MODIFIED, Action.UNINSTALL, Action.UPLOAD);

		private Action[] actions, developerActions;

		Status(final Action... actions) {
			developerActions = actions;
			if (!actions[actions.length - 1].uploadersOnly)
				this.actions = actions;
			else {
				this.actions = new Action[actions.length - 1];
				System.arraycopy(actions, 0, this.actions, 0,
						this.actions.length);
			}
		}

		public Action[] getActions() {
			return actions;
		}

		public Action[] getDeveloperActions() {
			return developerActions;
		}

		public boolean isValid(final Action action) {
			for (final Action a : developerActions)
				if (a.equals(action)) return true;
			return false;
		}

		public Action getNoAction() {
			return actions[0];
		}
	}

	protected List<String> overriddenUpdateSites = new ArrayList<String>();
	private Status status;
	private Action action;
	public String updateSite, filename, description;
	public boolean executable;
	public Version current;
	public Set<Version> previous;
	public long filesize;
	public boolean metadataChanged;
	public boolean descriptionFromPOM;

	public String localFilename, localChecksum;
	public long localTimestamp;

	// These are LinkedHashMaps to retain the order of the entries
	protected Map<String, Dependency> dependencies;
	protected Set<String> links, authors, platforms, categories;

	public FileObject(final String updateSite, final String filename,
		final long filesize, final String checksum, final long timestamp,
		final Status status)
	{
		this.updateSite = updateSite;
		this.filename = filename;
		if (checksum != null) current = new Version(checksum, timestamp);
		previous = new LinkedHashSet<Version>();
		this.status = status;
		dependencies = new LinkedHashMap<String, Dependency>();
		authors = new LinkedHashSet<String>();
		platforms = new LinkedHashSet<String>();
		categories = new LinkedHashSet<String>();
		links = new LinkedHashSet<String>();
		this.filesize = filesize;
		setNoAction();
	}

	public void merge(final FileObject upstream) {
		for (final Version previous : upstream.previous)
			addPreviousVersion(previous.checksum, previous.timestamp, previous.filename);
		if (updateSite == null || updateSite.equals(upstream.updateSite)) {
			updateSite = upstream.updateSite;
			description = upstream.description;
			dependencies = upstream.dependencies;
			authors = upstream.authors;
			platforms = upstream.platforms;
			categories = upstream.categories;
			links = upstream.links;
			filesize = upstream.filesize;
			executable = upstream.executable;
			if (current != null && !upstream.hasPreviousVersion(current.checksum)) addPreviousVersion(
				current.checksum, current.timestamp, current.filename);
			current = upstream.current;
			status = upstream.status;
			action = upstream.action;
		}
		else {
			final Version other = upstream.current;
			if (other != null && !hasPreviousVersion(other.checksum)) addPreviousVersion(
				other.checksum, other.timestamp, other.filename);
		}
	}

	/**
	 * Fill unset metadata with metadata from another object
	 *   
	 * @param other the metadata source
	 */
	public void completeMetadataFrom(final FileObject other) {
		if (description == null || description.length() == 0) description = other.description;
		if (links == null || links.size() == 0) links = other.links;
		if (authors == null || authors.size() == 0) authors = other.authors;
		if (platforms == null || platforms.size() == 0) platforms = other.platforms;
		if (categories == null || categories.size() == 0) categories = other.categories;
	}

	public boolean hasPreviousVersion(final String checksum) {
		if (current != null && current.checksum.equals(checksum)) return true;
		for (final Version version : previous)
			if (version.checksum.equals(checksum)) return true;
		return false;
	}

	public boolean isNewerThan(final long timestamp) {
		if (current != null && current.timestamp <= timestamp) return false;
		for (final Version version : previous)
			if (version.timestamp <= timestamp) return false;
		return true;
	}

	void setVersion(final String checksum, final long timestamp) {
		if (current != null) previous.add(current);
		current = new Version(checksum, timestamp);
		current.filename = filename;
	}

	public void setLocalVersion(final String filename, final String checksum, final long timestamp) {
		localFilename = filename;
		if (current != null && checksum.equals(current.checksum)) {
			if (status != Status.LOCAL_ONLY) status = Status.INSTALLED;
			setNoAction();
			return;
		}
		status =
			hasPreviousVersion(checksum) ? (current == null ? Status.OBSOLETE
				: Status.UPDATEABLE) : (current == null ? Status.OBSOLETE_MODIFIED
				: Status.MODIFIED);
		setNoAction();
		localChecksum = checksum;
		localTimestamp = timestamp;
	}

	public String getDescription() {
		return description;
	}

	public void addDependency(final FilesCollection files,
		final FileObject dependency)
	{
		final String filename = dependency.getFilename();
		addDependency(filename, files.prefix(filename));
	}

	public void addDependency(final String filename, final File file) {
		addDependency(filename, Util.getTimestamp(file), false);
	}

	public void addDependency(final String filename, final long timestamp,
		final boolean overrides)
	{
		addDependency(new Dependency(filename, timestamp, overrides));
	}

	public void addDependency(final Dependency dependency) {
		if (dependency.filename == null || "".equals(dependency.filename.trim())) return;
		// the timestamp should not be changed unnecessarily
		final String key = getFilename(dependency.filename, true);
		if (dependencies.containsKey(key)) {
			final Dependency other = dependencies.get(key);
			if (other.filename.equals(dependency.filename)
					|| other.timestamp >= dependency.timestamp)
				return;
		}
		dependencies.put(key, dependency);
	}

	public void removeDependency(final String other) {
		dependencies.remove(getFilename(other, true));
	}

	public boolean hasDependency(final String filename) {
		return dependencies.containsKey(filename);
	}

	public void addLink(final String link) {
		links.add(link);
	}

	public Iterable<String> getLinks() {
		return links;
	}

	public void addAuthor(final String author) {
		authors.add(author);
	}

	public Iterable<String> getAuthors() {
		return authors;
	}

	public void addPlatform(String platform) {
		if (platform.equals("linux")) platform = "linux32";
		if (platform != null && !platform.trim().equals("")) platforms.add(platform
			.trim());
	}

	public Iterable<String> getPlatforms() {
		return platforms;
	}

	public void addCategory(final String category) {
		categories.add(category);
	}

	public void replaceList(final String tag, final String... list) {
		if (tag.equals("Dependency")) {
			final long now = Long.parseLong(Util.timestamp(new Date().getTime()));
			final Dependency[] newList = new Dependency[list.length];
			for (int i = 0; i < list.length; i++) {
				boolean obsoleted = false;
				String item = list[i].trim();
				if (item.startsWith("obsoletes ")) {
					item = item.substring(10);
					obsoleted = true;
				}
				Dependency dep = dependencies.get(item);
				if (dep == null) dep = new Dependency(item, now, obsoleted);
				else if (dep.overrides != obsoleted) {
					dep.timestamp = now;
					dep.overrides = obsoleted;
				}
				newList[i] = dep;
			}
			dependencies.clear();
			for (final Dependency dep : newList)
				addDependency(dep);
			return;
		}

		final Set<String> map =
			tag.equals("Link") ? links : tag.equals("Author") ? authors : tag
				.equals("Platform") ? platforms : tag.equals("Category") ? categories
				: null;
		map.clear();
		for (final String string : list)
			map.add(string.trim());
	}

	public Iterable<String> getCategories() {
		return categories;
	}

	public Iterable<Version> getPrevious() {
		return previous;
	}

	public void addPreviousVersion(final String checksum, final long timestamp, final String filename) {
		final Version version = new Version(checksum, timestamp);
		if (filename != null && !"".equals(filename)) version.filename = filename;
		if (!previous.contains(version)) previous.add(version);
	}

	public void setNoAction() {
		action = status.getNoAction();
	}

	public void setAction(final FilesCollection files, final Action action) {
		if (!status.isValid(action)) throw new Error(
			"Invalid action requested for file " + filename + "(" + action + ", " +
				status + ")");
		if (action == Action.UPLOAD) {
			if (localFilename != null) {
				current.filename = filename;
				filename = localFilename;
			}
			files.updateDependencies(this);
		}
		this.action = action;
	}

	public boolean setFirstValidAction(final FilesCollection files,
			final Action... actions) {
		for (final Action action : actions)
			if (status.isValid(action)) {
				setAction(files, action);
				return true;
			}
		return false;
	}

	public void setStatus(final Status status) {
		this.status = status;
		setNoAction();
	}

	public void markUploaded() {
		if (isLocalOnly()) {
			status = Status.INSTALLED;
			localChecksum = current.checksum;
			localTimestamp = current.timestamp;
		}
		else if (isObsolete() || status == Status.UPDATEABLE) {
			/* force re-upload */
			status = Status.INSTALLED;
			setVersion(localChecksum, localTimestamp);
		}
		else {
			if (localChecksum == null || localChecksum.equals(current.checksum)) throw new Error(
				filename + " is already uploaded");
			setVersion(localChecksum, localTimestamp);
		}
	}

	public void markRemoved() {
		addPreviousVersion(current.checksum, current.timestamp, current.filename);
		setStatus(Status.OBSOLETE);
		current = null;
	}

	public String getLocalFilename(boolean forDisplay) {
		if (localFilename == null || localFilename.equals(filename)) return filename;
		if (!forDisplay) return localFilename;
		return filename + " (local: " + localFilename + ")";
	}

	public String getFilename() {
		return getFilename(false);
	}

	protected final static Pattern versionPattern = Pattern.compile("(.+?)(-\\d+(\\.\\d+)+[a-z]?(-[A-Za-z0-9.]+|\\.GA)*)(\\.jar)");

	protected static Matcher matchVersion(String filename) {
		if (!filename.endsWith(".jar")) return null;
		final Matcher matcher = versionPattern.matcher(filename);
		return matcher.matches() ? matcher : null;
	}

	public String getFilename(boolean stripVersion) {
		return getFilename(filename, stripVersion);
	}

	public static String getFilename(final String filename, boolean stripVersion) {
		final Matcher matcher;
		if (stripVersion && (matcher = matchVersion(filename)) != null) {
			return matcher.group(1) + matcher.group(5);
		}
		return filename;
	}

	public String getBaseName() {
		final Matcher matcher = matchVersion(filename);
		if (matcher != null) return matcher.group(1);
		final String extension = FileUtils.getExtension(filename);
		return filename.substring(0, filename.length() - extension.length() - 1);
	}

	public String getFileVersion() {
		final Matcher matcher = matchVersion(filename);
		if (matcher != null) return matcher.group(2);
		return "";
	}

	public String getChecksum() {
		return action == Action.UPLOAD ? localChecksum : action == Action.REMOVE ||
			current == null ? null : current.checksum;
	}

	public long getTimestamp() {
		return action == Action.UPLOAD ? (status == Status.LOCAL_ONLY &&
			current != null ? current.timestamp : localTimestamp)
			: (action == Action.REMOVE || current == null ? 0 : current.timestamp);
	}

	public Iterable<Dependency> getDependencies() {
		return dependencies.values();
	}

	public Iterable<FileObject> getFileDependencies(
			final FilesCollection files, final boolean recursive) {
		final Set<FileObject> result = new LinkedHashSet<FileObject>();
		if (recursive) result.add(this);
		final Stack<FileObject> stack = new Stack<FileObject>();
		stack.push(this);
		while (!stack.empty()) {
			final FileObject file = stack.pop();
			for (final Dependency dependency : file.getDependencies()) {
				final FileObject file2 = files.get(dependency.filename);
				if (file2 == null)
					continue;
				if (recursive && !result.contains(file2))
					stack.push(file2);
				result.add(file2);
			}
		}
		return result;
	}

	public Status getStatus() {
		return status;
	}

	public Action getAction() {
		return action;
	}

	public boolean isInstallable() {
		return status.isValid(Action.INSTALL);
	}

	public boolean isUpdateable() {
		return status.isValid(Action.UPDATE);
	}

	public boolean isUninstallable() {
		return status.isValid(Action.UNINSTALL);
	}

	public boolean isLocallyModified() {
		return status.getNoAction() == Action.MODIFIED;
	}

	/**
	 * Tell whether this file can be uploaded to its update site Note: this does
	 * not check whether the file is locally modified.
	 */
	public boolean isUploadable(final FilesCollection files) {
		switch (status) {
			case INSTALLED:
				return false;
		}
		if (updateSite == null) return files.hasUploadableSites();
		final FilesCollection.UpdateSite updateSite =
			files.getUpdateSite(this.updateSite);
		return updateSite != null && updateSite.isUploadable();
	}

	public boolean actionSpecified() {
		return action != status.getNoAction();
	}

	public boolean toUpdate() {
		return action == Action.UPDATE;
	}

	public boolean toUninstall() {
		return action == Action.UNINSTALL;
	}

	public boolean toInstall() {
		return action == Action.INSTALL;
	}

	public boolean toUpload() {
		return action == Action.UPLOAD;
	}

	public boolean isObsolete() {
		switch (status) {
			case OBSOLETE:
			case OBSOLETE_MODIFIED:
			case OBSOLETE_UNINSTALLED:
				return true;
		}
		return false;
	}

	public boolean isForPlatform(final String platform) {
		return platforms.contains(platform);
	}

	public boolean isForThisPlatform() {
		return platforms.size() == 0 || isForPlatform(Util.platform);
	}

	public boolean isUpdateablePlatform() {
		if (platforms.size() == 0) return true;
		for (final String platform : platforms)
			if (Util.isUpdateablePlatform(platform)) return true;
		return false;
	}

	public boolean isLocalOnly() {
		return status == Status.LOCAL_ONLY;
	}

	/* This returns true if the user marked the file for uninstall, too */
	public boolean willNotBeInstalled() {
		switch (action) {
			case NOT_INSTALLED:
			case NEW:
			case UNINSTALL:
			case REMOVE:
				return true;
			case LOCAL_ONLY:
			case INSTALLED:
			case UPDATEABLE:
			case MODIFIED:
			case OBSOLETE:
			case INSTALL:
			case UPDATE:
			case UPLOAD:
				return false;
			default:
				throw new RuntimeException("Unhandled action: " + action);
		}
	}

	/* This returns true if the user marked the file for uninstall, too */
	public boolean willBeUpToDate() {
		switch (action) {
			case OBSOLETE:
			case REMOVE:
			case NOT_INSTALLED:
			case NEW:
			case UPDATEABLE:
			case MODIFIED:
			case UNINSTALL:
				return false;
			case INSTALLED:
			case INSTALL:
			case UPDATE:
			case UPLOAD:
			case LOCAL_ONLY:
				return true;
			default:
				throw new RuntimeException("Unhandled action: " + action);
		}
	}

	// TODO: this needs a better name; something like wantsAction()
	public boolean isUpdateable(final boolean evenForcedUpdates) {
		return action == Action.UPDATE ||
			action == Action.INSTALL ||
			status == Status.UPDATEABLE ||
			status == Status.OBSOLETE ||
			(evenForcedUpdates && (status.isValid(Action.UPDATE) || status == Status.OBSOLETE_MODIFIED));
	}

	public boolean stageForUpdate(final FilesCollection files, boolean evenForcedOnes) {
		if (!evenForcedOnes && status == Status.MODIFIED)
			return false;
		if (!setFirstValidAction(files, Action.UPDATE, Action.INSTALL))
			return false;
		for (final FileObject file : getFileDependencies(files, true)) {
			if (!evenForcedOnes && (file.status == Status.MODIFIED || file.status == Status.OBSOLETE_MODIFIED)) continue;
			file.setFirstValidAction(files, Action.UPDATE, Action.INSTALL);
		}
		return true;
	}

	public void stageForUpload(final FilesCollection files,
		final String updateSite)
	{
		if (status == Status.LOCAL_ONLY) {
			localChecksum = current.checksum;
			localTimestamp = current.timestamp;
		}
		if (status == Status.NOT_INSTALLED) {
			setAction(files, Action.REMOVE);
		}
		else {
			setAction(files, Action.UPLOAD);
		}
		this.updateSite = updateSite;

		String baseName = getBaseName();
		String withoutVersion = getFilename(true);
		List<Dependency> fixup = new ArrayList<Dependency>();
		for (final FileObject file : files.forUpdateSite(updateSite)) {
			if (file.isObsolete()) continue;
			fixup.clear();
			for (final Dependency dependency : file.getDependencies()) {
				if (dependency.overrides) continue;
				if (dependency.filename.startsWith(baseName) && withoutVersion.equals(getFilename(dependency.filename, true)) && !dependency.filename.equals(filename)) {
					fixup.add(dependency);
				}
			}
			for (final Dependency dependency : fixup) {
				file.dependencies.remove(dependency.filename);
				dependency.filename = filename;
				file.dependencies.put(filename, dependency);
			}
		}
	}

	public void stageForUninstall(final FilesCollection files) throws IOException
	{
		final String filename = getLocalFilename(false);
		if (action != Action.UNINSTALL)
			setAction(files, Action.UNINSTALL);
		if (filename.endsWith(".jar")) touch(files.prefixUpdate(filename));
		else {
			String old = filename + ".old";
			if (old.endsWith(".exe.old")) old =
				old.substring(0, old.length() - 8) + ".old.exe";
			files.prefix(filename).renameTo(files.prefix(old));
			touch(files.prefixUpdate(old));
		}
		if (status != Status.LOCAL_ONLY) setStatus(isObsolete()
			? Status.OBSOLETE_UNINSTALLED : Status.NOT_INSTALLED);
	}

	public static void touch(final File file) throws IOException {
		if (file.exists()) {
			final long now = new Date().getTime();
			file.setLastModified(now);
		}
		else {
			final File parent = file.getParentFile();
			if (!parent.exists()) parent.mkdirs();
			file.createNewFile();
		}
	}

	public String toDebug() {
		return filename + "(" + status + ", " + action + ")";
	}

	@Override
	public String toString() {
		return filename;
	}

	/**
	 * For displaying purposes, it is nice to have a file object whose toString()
	 * method shows either the filename or the action.
	 */
	public class LabeledFile {

		String label;

		LabeledFile(final String label) {
			this.label = label;
		}

		public FileObject getFile() {
			return FileObject.this;
		}

		@Override
		public String toString() {
			return label;
		}
	}

	public LabeledFile getLabeledFile(final int column) {
		switch (column) {
			case 0:
				return new LabeledFile(getFilename());
			case 1:
				return new LabeledFile(getAction().toString());
		}
		return null;
	}
}
