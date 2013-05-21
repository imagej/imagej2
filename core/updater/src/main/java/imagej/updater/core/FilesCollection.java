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

import imagej.updater.core.Conflicts.Conflict;
import imagej.updater.core.FileObject.Action;
import imagej.updater.core.FileObject.Status;
import imagej.updater.core.action.InstallOrUpdate;
import imagej.updater.core.action.KeepAsIs;
import imagej.updater.core.action.Uninstall;
import imagej.updater.core.action.UploadOrRemove;
import imagej.updater.util.DependencyAnalyzer;
import imagej.updater.util.Progress;
import imagej.updater.util.UpdateCanceledException;
import imagej.updater.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPOutputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;

import org.scijava.log.LogService;
import org.xml.sax.SAXException;

/**
 * This class represents the database of available {@link FileObject}s.
 * 
 * @author Johannes Schindelin
 */
@SuppressWarnings("serial")
public class FilesCollection extends LinkedHashMap<String, FileObject>
	implements Iterable<FileObject>
{

	public final static String DEFAULT_UPDATE_SITE = "ImageJ";
	private File imagejRoot;
	public final LogService log;
	protected Set<FileObject> ignoredConflicts = new HashSet<FileObject>();
	protected List<Conflict> conflicts = new ArrayList<Conflict>();

	public static class UpdateSite implements Cloneable, Comparable<UpdateSite> {

		public String url, sshHost, uploadDirectory;
		public long timestamp;
		public int rank;

		public UpdateSite(String url, final String sshHost, String uploadDirectory,
			final long timestamp)
		{
			if (!url.endsWith("/")) url += "/";
			if (uploadDirectory != null && !uploadDirectory.equals("") &&
				!uploadDirectory.endsWith("/")) uploadDirectory += "/";
			this.url = url;
			this.sshHost = sshHost;
			this.uploadDirectory = uploadDirectory;
			this.timestamp = timestamp;
		}

		@Override
		public Object clone() {
			return new UpdateSite(url, sshHost, uploadDirectory, timestamp);
		}

		public boolean isLastModified(final long lastModified) {
			return timestamp == Long.parseLong(Util.timestamp(lastModified));
		}

		public void setLastModified(final long lastModified) {
			timestamp = Long.parseLong(Util.timestamp(lastModified));
		}

		public boolean isUploadable() {
			return (uploadDirectory != null && !uploadDirectory.equals("")) ||
					(sshHost != null && sshHost.indexOf(':') > 0);
		}

		@Override
		public String toString() {
			return url + (sshHost != null ? ", " + sshHost : "") +
				(uploadDirectory != null ? ", " + uploadDirectory : "");
		}

		@Override
		public int compareTo(UpdateSite other) {
			return rank - other.rank;
		}

		@Override
		public boolean equals(Object other) {
			if (other instanceof UpdateSite)
				return rank == ((UpdateSite)other).rank;
			return false;
		}

		@Override
		public int hashCode() {
			return rank;
		}

		public String getUploadProtocol() {
			if (sshHost == null)
				throw new RuntimeException("Missing upload information for site " + url);
			final int at = sshHost.indexOf('@');
			final int colon = sshHost.indexOf(':');
			if (colon > 0 && (at < 0 || colon < at)) return sshHost.substring(0, colon);
			return "ssh";
		}
	}

	private Map<String, UpdateSite> updateSites;

	private DependencyAnalyzer dependencyAnalyzer;
	public final Util util;

	/**
	 * This constructor takes the imagejRoot primarily for testing purposes.
	 * 
	 * @param imagejRoot the ImageJ directory
	 */
	public FilesCollection(final File imagejRoot) {
		this(Util.getLogService(), imagejRoot);
	}

	/**
	 * This constructor takes the imagejRoot primarily for testing purposes.
	 * 
	 * @param log the log service
	 * @param imagejRoot the ImageJ directory
	 */
	public FilesCollection(final LogService log, final File imagejRoot) {
		this.log = log;
		this.imagejRoot = imagejRoot;
		util = new Util(imagejRoot);
		updateSites = new LinkedHashMap<String, UpdateSite>();
		addUpdateSite(DEFAULT_UPDATE_SITE, Util.MAIN_URL, null, null,
			imagejRoot == null ? 0 : Util.getTimestamp(prefix(Util.XML_COMPRESSED)));
	}

	public void addUpdateSite(final String name, final String url,
		final String sshHost, final String uploadDirectory, final long timestamp)
	{
		addUpdateSite(name, new UpdateSite(url, sshHost, uploadDirectory,
			timestamp));
	}

	protected void addUpdateSite(final String name, final UpdateSite updateSite) {
		UpdateSite already = updateSites.get(name);
		updateSite.rank = already != null ? already.rank : updateSites.size();
		updateSites.put(name,  updateSite);
	}

	public void renameUpdateSite(final String oldName, final String newName) {
		if (getUpdateSite(newName) != null) throw new RuntimeException(
			"Update site " + newName + " exists already!");
		if (getUpdateSite(oldName) == null) throw new RuntimeException(
			"Update site " + oldName + " does not exist!");

		// handle all files
		for (final FileObject file : this)
			if (oldName.equals(file.updateSite)) file.updateSite = newName;

		// preserve order
		final Map<String, UpdateSite> oldMap = updateSites;
		updateSites = new LinkedHashMap<String, UpdateSite>();
		for (final String name : oldMap.keySet()) {
			addUpdateSite(name.equals(oldName) ? newName : name, oldMap.get(name));
		}
	}

	public void removeUpdateSite(final String name) {
		Set<String> toReRead = new HashSet<String>();
		for (final FileObject file : forUpdateSite(name)) {
			toReRead.addAll(file.overriddenUpdateSites.keySet());
			if (file.getStatus() == Status.NOT_INSTALLED) {
				remove(file);
			}
			else {
				file.setStatus(Status.LOCAL_ONLY);
				file.updateSite = null;
			}
		}
		updateSites.remove(name);

		// re-read the overridden sites
		// no need to sort, the XMLFileReader will only override data from higher-ranked sites
		new XMLFileDownloader(this, toReRead).start();

		// update rank
		int counter = 1;
		for (final Map.Entry<String, UpdateSite> entry : updateSites.entrySet()) {
			entry.getValue().rank = counter++;
		}
	}

	public UpdateSite getUpdateSite(final String name) {
		if (name == null) return null;
		return updateSites.get(name);
	}

	public Collection<String> getUpdateSiteNames() {
		return updateSites.keySet();
	}

	public Collection<String> getSiteNamesToUpload() {
		final Collection<String> set = new HashSet<String>();
		for (final FileObject file : toUpload(true))
			set.add(file.updateSite);
		for (final FileObject file : toRemove())
			set.add(file.updateSite);
		// keep the update sites' order
		final List<String> result = new ArrayList<String>();
		for (final String name : getUpdateSiteNames())
			if (set.contains(name)) result.add(name);
		if (result.size() != set.size()) throw new RuntimeException(
			"Unknown update site in " + set.toString() + " (known: " +
				result.toString() + ")");
		return result;
	}

	public boolean hasUploadableSites() {
		for (final String name : updateSites.keySet())
			if (getUpdateSite(name).isUploadable()) return true;
		return false;
	}

	public void reReadUpdateSite(final String name, final Progress progress) throws ParserConfigurationException, IOException, SAXException {
		new XMLFileReader(this).read(name);
		final List<String> filesFromSite = new ArrayList<String>();
		for (final FileObject file : forUpdateSite(name))
			filesFromSite.add(file.localFilename != null ? file.localFilename : file.filename);
		final Checksummer checksummer =
			new Checksummer(this, progress);
		checksummer.updateFromLocal(filesFromSite);
	}

	public String protocolsMissingUploaders(final UploaderService uploaderService, final Progress progress) {
		final Map<String, Set<String>> map = new LinkedHashMap<String, Set<String>>();
		for (final Map.Entry<String, UpdateSite> entry : updateSites.entrySet()) {
			final UpdateSite site = entry.getValue();
			if (!site.isUploadable()) continue;
			final String protocol = site.getUploadProtocol();
			try {
				uploaderService.installUploader(protocol, this, progress);
			} catch (IllegalArgumentException e) {
				Set<String> set = map.get(protocol);
				if (set == null) {
					set = new LinkedHashSet<String>();
					map.put(protocol, set);
				}
				set.add(entry.getKey());
			}
		}
		if (map.size() == 0) return null;
		final StringBuilder builder = new StringBuilder();
		builder.append("Missing uploaders:\n");
		for (final Map.Entry<String, Set<String>> entry : map.entrySet()) {
			final String list = Arrays.toString(entry.getValue().toArray());
			builder.append("'").append(entry.getKey()).append("': ").append(list).append("\n");
		}
		return builder.toString();
	}

	public Set<GroupAction> getValidActions() {
		final Set<GroupAction> actions = new LinkedHashSet<GroupAction>();
		actions.add(new KeepAsIs());
		actions.add(new InstallOrUpdate());
		final Collection<String> siteNames = getSiteNamesToUpload();
		final Map<String, UpdateSite> updateSites;
		if (siteNames.size() == 0) updateSites = this.updateSites;
		else {
			updateSites = new LinkedHashMap<String, UpdateSite>();
			for (final String name : siteNames) {
				updateSites.put(name, getUpdateSite(name));
			}
		}
		for (final Map.Entry<String, UpdateSite> entry : updateSites.entrySet()) {
			final UpdateSite updateSite = entry.getValue();
			if (updateSite.isUploadable()) actions.add(new UploadOrRemove(entry.getKey()));
		}
		actions.add(new Uninstall());
		return actions;
	}

	public Set<GroupAction> getValidActions(final Iterable<FileObject> selected) {
		final Set<GroupAction> actions = getValidActions();
		for (final Iterator<GroupAction> iter = actions.iterator(); iter.hasNext(); ) {
			final GroupAction action = iter.next();
			for (final FileObject file : selected) {
				if (!action.isValid(this, file)) {
					iter.remove();
					break;
				}
			}
		}
		return actions;
	}

	@Deprecated
	public Action[] getActions(final FileObject file) {
		return file.isUploadable(this) ? file.getStatus().getDeveloperActions()
			: file.getStatus().getActions();
	}

	@Deprecated
	public Action[] getActions(final Iterable<FileObject> files) {
		List<Action> result = null;
		for (final FileObject file : files) {
			final Action[] actions = getActions(file);
			if (result == null) {
				result = new ArrayList<Action>();
				for (final Action action : actions)
					result.add(action);
			}
			else {
				final Set<Action> set = new TreeSet<Action>();
				for (final Action action : actions)
					set.add(action);
				final Iterator<Action> iter = result.iterator();
				while (iter.hasNext())
					if (!set.contains(iter.next())) iter.remove();
			}
		}
		if (result == null) {
			return new Action[0];
		}
		return result.toArray(new Action[result.size()]);
	}

	public void read() throws IOException, ParserConfigurationException,
		SAXException
	{
		read(prefix(Util.XML_COMPRESSED));
	}

	public void read(final File file) throws IOException,
		ParserConfigurationException, SAXException
	{
		read(new FileInputStream(file));
	}

	public void read(final FileInputStream in) throws IOException,
		ParserConfigurationException, SAXException
	{
		new XMLFileReader(this).read(in);
		in.close();
	}

	public void write() throws IOException, SAXException,
		TransformerConfigurationException
	{
		new XMLFileWriter(this).write(new GZIPOutputStream(new FileOutputStream(
			prefix(Util.XML_COMPRESSED))), true);
	}

	public interface Filter {

		boolean matches(FileObject file);
	}

	public FilesCollection clone(final Iterable<FileObject> iterable) {
		final FilesCollection result = new FilesCollection(imagejRoot);
		for (final FileObject file : iterable)
			result.add(file);
		for (final String name : updateSites.keySet())
			result.updateSites.put(name, (UpdateSite) updateSites.get(name).clone());
		return result;
	}

	public Iterable<FileObject> toUploadOrRemove() {
		return filter(or(is(Action.UPLOAD), is(Action.REMOVE)));
	}

	public Iterable<FileObject> toUpload() {
		return toUpload(false);
	}

	public Iterable<FileObject> toUpload(final boolean includeMetadataChanges) {
		if (!includeMetadataChanges) return filter(is(Action.UPLOAD));
		return filter(or(is(Action.UPLOAD), new Filter() {

			@Override
			public boolean matches(final FileObject file) {
				return file.metadataChanged;
			}
		}));
	}

	public Iterable<FileObject> toUpload(final String updateSite) {
		return filter(and(is(Action.UPLOAD), isUpdateSite(updateSite)));
	}

	public Iterable<FileObject> toUninstall() {
		return filter(is(Action.UNINSTALL));
	}

	public Iterable<FileObject> toRemove() {
		return filter(is(Action.REMOVE));
	}

	public Iterable<FileObject> toUpdate() {
		return filter(is(Action.UPDATE));
	}

	public Iterable<FileObject> upToDate() {
		return filter(is(Action.INSTALLED));
	}

	public Iterable<FileObject> toInstall() {
		return filter(is(Action.INSTALL));
	}

	public Iterable<FileObject> toInstallOrUpdate() {
		return filter(oneOf(Action.INSTALL, Action.UPDATE));
	}

	public Iterable<FileObject> notHidden() {
		return filter(and(not(is(Status.OBSOLETE_UNINSTALLED)), doesPlatformMatch()));
	}

	public Iterable<FileObject> uninstalled() {
		return filter(is(Status.NOT_INSTALLED));
	}

	public Iterable<FileObject> installed() {
		return filter(not(oneOf(Status.LOCAL_ONLY,
			Status.NOT_INSTALLED)));
	}

	public Iterable<FileObject> locallyModified() {
		return filter(oneOf(Status.MODIFIED,
			Status.OBSOLETE_MODIFIED));
	}

	public Iterable<FileObject> forUpdateSite(final String name) {
		return forUpdateSite(name, false);
	}

	public Iterable<FileObject> forUpdateSite(final String name, boolean includeObsoletes) {
		Filter filter = and(doesPlatformMatch(), isUpdateSite(name));
		if (!includeObsoletes) {
			filter = and(not(is(Status.OBSOLETE_UNINSTALLED)), filter);
			return filter(filter);
		}
		// make sure that overridden records are kept
		List<FileObject> result = new ArrayList<FileObject>();
		for (FileObject file : this) {
			if (filter.matches(file))
				result.add(file);
			else {
				FileObject overridden = file.overriddenUpdateSites.get(name);
				if (overridden != null)
					result.add(overridden);
			}
		}
		return result;
	}

	public Iterable<FileObject> managedFiles() {
		return filter(not(is(Status.LOCAL_ONLY)));
	}

	public Iterable<FileObject> localOnly() {
		return filter(is(Status.LOCAL_ONLY));
	}

	public Iterable<FileObject> shownByDefault() {
		/*
		 * Let's not show the NOT_INSTALLED ones, as the user chose not
		 * to have them.
		 */
		final Status[] oneOf =
			{ Status.UPDATEABLE, Status.NEW, Status.OBSOLETE,
				Status.OBSOLETE_MODIFIED };
		return filter(or(oneOf(oneOf), is(Action.INSTALL)));
	}

	public Iterable<FileObject> uploadable() {
		return filter(new Filter() {

			@Override
			public boolean matches(final FileObject file) {
				return file.isUploadable(FilesCollection.this);
			}
		});
	}

	public Iterable<FileObject> changes() {
		return filter(new Filter() {

			@Override
			public boolean matches(final FileObject file) {
				return file.getAction() != file.getStatus().getNoAction();
			}
		});
	}

	public static class FilteredIterator implements Iterator<FileObject> {

		Filter filter;
		boolean opposite;
		Iterator<FileObject> iterator;
		FileObject next;

		FilteredIterator(final Filter filter, final Iterable<FileObject> files) {
			this.filter = filter;
			iterator = files.iterator();
			findNext();
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public FileObject next() {
			final FileObject file = next;
			findNext();
			return file;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		protected void findNext() {
			while (iterator.hasNext()) {
				next = iterator.next();
				if (filter.matches(next)) return;
			}
			next = null;
		}
	}

	public static Iterable<FileObject> filter(final Filter filter,
		final Iterable<FileObject> files)
	{
		return new Iterable<FileObject>() {

			@Override
			public Iterator<FileObject> iterator() {
				return new FilteredIterator(filter, files);
			}
		};
	}

	public static Iterable<FileObject> filter(final String search,
		final Iterable<FileObject> files)
	{
		final String keyword = search.trim().toLowerCase();
		return filter(new Filter() {

			@Override
			public boolean matches(final FileObject file) {
				return file.getFilename().trim().toLowerCase().indexOf(keyword) >= 0;
			}
		}, files);
	}

	public Filter yes() {
		return new Filter() {

			@Override
			public boolean matches(final FileObject file) {
				return true;
			}
		};
	}

	public Filter doesPlatformMatch() {
		// If we're a developer or no platform was specified, return yes
		if (hasUploadableSites()) return yes();
		return new Filter() {

			@Override
			public boolean matches(final FileObject file) {
				return file.isUpdateablePlatform(FilesCollection.this);
			}
		};
	}

	public Filter is(final Action action) {
		return new Filter() {

			@Override
			public boolean matches(final FileObject file) {
				return file.getAction() == action;
			}
		};
	}

	public Filter isNoAction() {
		return new Filter() {

			@Override
			public boolean matches(final FileObject file) {
				return file.getAction() == file.getStatus().getNoAction();
			}
		};
	}

	public Filter oneOf(final Action... actions) {
		final Set<Action> oneOf = new HashSet<Action>();
		for (final Action action : actions)
			oneOf.add(action);
		return new Filter() {

			@Override
			public boolean matches(final FileObject file) {
				return oneOf.contains(file.getAction());
			}
		};
	}

	public Filter is(final Status status) {
		return new Filter() {

			@Override
			public boolean matches(final FileObject file) {
				return file.getStatus() == status;
			}
		};
	}

	public Filter isUpdateSite(final String updateSite) {
		return new Filter() {

			@Override
			public boolean matches(final FileObject file) {
				return file.updateSite != null && // is null for local-only files
					file.updateSite.equals(updateSite);
			}
		};
	}

	public Filter oneOf(final Status... states) {
		final Set<Status> oneOf = new HashSet<Status>();
		for (final Status status : states)
			oneOf.add(status);
		return new Filter() {

			@Override
			public boolean matches(final FileObject file) {
				return oneOf.contains(file.getStatus());
			}
		};
	}

	public Filter startsWith(final String prefix) {
		return new Filter() {

			@Override
			public boolean matches(final FileObject file) {
				return file.filename.startsWith(prefix);
			}
		};
	}

	public Filter startsWith(final String... prefixes) {
		return new Filter() {

			@Override
			public boolean matches(final FileObject file) {
				for (final String prefix : prefixes)
					if (file.filename.startsWith(prefix)) return true;
				return false;
			}
		};
	}

	public Filter endsWith(final String suffix) {
		return new Filter() {

			@Override
			public boolean matches(final FileObject file) {
				return file.filename.endsWith(suffix);
			}
		};
	}

	public Filter not(final Filter filter) {
		return new Filter() {

			@Override
			public boolean matches(final FileObject file) {
				return !filter.matches(file);
			}
		};
	}

	public Filter or(final Filter a, final Filter b) {
		return new Filter() {

			@Override
			public boolean matches(final FileObject file) {
				return a.matches(file) || b.matches(file);
			}
		};
	}

	public Filter and(final Filter a, final Filter b) {
		return new Filter() {

			@Override
			public boolean matches(final FileObject file) {
				return a.matches(file) && b.matches(file);
			}
		};
	}

	public Iterable<FileObject> filter(final Filter filter) {
		return filter(filter, this);
	}

	public FileObject
		getFileFromDigest(final String filename, final String digest)
	{
		for (final FileObject file : this)
			if (file.getFilename().equals(filename) &&
				file.getChecksum().equals(digest)) return file;
		return null;
	}

	public Iterable<String> analyzeDependencies(final FileObject file) {
		try {
			if (dependencyAnalyzer == null) dependencyAnalyzer =
				new DependencyAnalyzer(imagejRoot);
			return dependencyAnalyzer.getDependencies(imagejRoot, file);
		}
		catch (final IOException e) {
			log.error(e);
			return null;
		}
	}

	public void updateDependencies(final FileObject file) {
		final Iterable<String> dependencies = analyzeDependencies(file);
		if (dependencies == null) return;
		for (final String dependency : dependencies)
			file.addDependency(dependency, prefix(dependency));
	}

	public boolean has(final Filter filter) {
		for (final FileObject file : this)
			if (filter.matches(file)) return true;
		return false;
	}

	public boolean hasChanges() {
		return has(not(isNoAction()));
	}

	public boolean hasUploadOrRemove() {
		return has(oneOf(Action.UPLOAD, Action.REMOVE));
	}

	public boolean hasForcableUpdates() {
		for (final FileObject file : updateable(true))
			if (!file.isUpdateable(false)) return true;
		return false;
	}

	public Iterable<FileObject> updateable(final boolean evenForcedOnes) {
		return filter(new Filter() {

			@Override
			public boolean matches(final FileObject file) {
				return file.isUpdateable(evenForcedOnes) && file.isUpdateablePlatform(FilesCollection.this);
			}
		});
	}

	public void markForUpdate(final boolean evenForcedUpdates) {
		for (final FileObject file : updateable(evenForcedUpdates)) {
			file.setFirstValidAction(this, Action.UPDATE,
				Action.UNINSTALL, Action.INSTALL);
		}
	}

	public String getURL(final FileObject file) {
		final String siteName = file.updateSite;
		assert (siteName != null && !siteName.equals(""));
		final UpdateSite site = getUpdateSite(siteName);
		if (site == null) return null;
		return site.url + file.filename.replace(" ", "%20") + "-" +
			file.getTimestamp();
	}

	public static class DependencyMap extends
		HashMap<FileObject, FilesCollection>
	{

		// returns true when the map did not have the dependency before
		public boolean
			add(final FileObject dependency, final FileObject dependencee)
		{
			if (containsKey(dependency)) {
				get(dependency).add(dependencee);
				return false;
			}
			final FilesCollection list = new FilesCollection(null);
			list.add(dependencee);
			put(dependency, list);
			return true;
		}
	}

	void addDependencies(final FileObject file, final DependencyMap map,
		final boolean overriding)
	{
		for (final Dependency dependency : file.getDependencies()) {
			final FileObject other = get(dependency.filename);
			if (other == null || overriding != dependency.overrides ||
				!other.isUpdateablePlatform(this)) continue;
			if (other.isObsolete() && other.willNotBeInstalled()) {
				log.warn("Ignoring obsolete dependency " + dependency.filename
						+ " of " + file.filename);
				continue;
			}
			if (dependency.overrides) {
				if (other.willNotBeInstalled()) continue;
			}
			else if (other.willBeUpToDate()) continue;
			if (!map.add(other, file)) continue;
			// overriding dependencies are not recursive
			if (!overriding) addDependencies(other, map, overriding);
		}
	}

	public DependencyMap getDependencies(final boolean overridingOnes) {
		final DependencyMap result = new DependencyMap();
		for (final FileObject file : toInstallOrUpdate())
			addDependencies(file, result, overridingOnes);
		return result;
	}

	public void sort() {
		// first letters in this order: 'C', 'I', 'f', 'p', 'j', 's', 'i', 'm', 'l,
		// 'r'
		final ArrayList<FileObject> files = new ArrayList<FileObject>();
		for (final FileObject file : this) {
			files.add(file);
		}
		Collections.sort(files, new Comparator<FileObject>() {

			@Override
			public int compare(final FileObject a, final FileObject b) {
				final int result = firstChar(a) - firstChar(b);
				return result != 0 ? result : a.filename.compareTo(b.filename);
			}

			int firstChar(final FileObject file) {
				final char c = file.filename.charAt(0);
				final int index = "CIfpjsim".indexOf(c);
				return index < 0 ? 0x200 + c : index;
			}
		});
		this.clear();
		for (final FileObject file : files) {
			super.put(file.filename, file);
		}
	}

	String checkForCircularDependency(final FileObject file,
		final Set<FileObject> seen)
	{
		if (seen.contains(file)) return "";
		final String result =
			checkForCircularDependency(file, seen, new HashSet<FileObject>());
		if (result == null) return "";

		// Display only the circular dependency
		final int last = result.lastIndexOf(' ');
		final int off = result.lastIndexOf(result.substring(last), last - 1);
		return "Circular dependency detected: " + result.substring(off + 1) + "\n";
	}

	String checkForCircularDependency(final FileObject file,
		final Set<FileObject> seen, final Set<FileObject> chain)
	{
		if (seen.contains(file)) return null;
		for (final String dependency : file.dependencies.keySet()) {
			final FileObject dep = get(dependency);
			if (dep == null) continue;
			if (chain.contains(dep)) return " " + dependency;
			chain.add(dep);
			final String result = checkForCircularDependency(dep, seen, chain);
			seen.add(dep);
			if (result != null) return " " + dependency + " ->" + result;
			chain.remove(dep);
		}
		return null;
	}

	/* returns null if consistent, error string when not */
	public String checkConsistency() {
		final StringBuilder result = new StringBuilder();
		final Set<FileObject> circularChecked = new HashSet<FileObject>();
		for (final FileObject file : this) {
			result.append(checkForCircularDependency(file, circularChecked));
			// only non-obsolete components can have dependencies
			final Set<String> deps = file.dependencies.keySet();
			if (deps.size() > 0 && file.isObsolete()) result.append("Obsolete file " +
				file + "has dependencies: " + Util.join(", ", deps) + "!\n");
			for (final String dependency : deps) {
				final FileObject dep = get(dependency);
				if (dep == null || dep.current == null) result.append("The file " +
					file + " has the obsolete/local-only " + "dependency " + dependency +
					"!\n");
			}
		}
		return result.length() > 0 ? result.toString() : null;
	}

	public File prefix(final FileObject file) {
		return prefix(file.getFilename());
	}

	public File prefix(final String path) {
		final File file = new File(path);
		if (file.isAbsolute()) return file;
		assert (imagejRoot != null);
		return new File(imagejRoot, path);
	}

	public File prefixUpdate(final String path) {
		return prefix("update/" + path);
	}

	public boolean fileExists(final String filename) {
		return prefix(filename).exists();
	}

	@Override
	public String toString() {
		return Util.join(", ", this);
	}

	@Deprecated
	public FileObject get(final int index) {
		throw new UnsupportedOperationException();
	}

	public void add(final FileObject file) {
		super.put(file.getFilename(true), file);
	}

	@Override
	public FileObject get(final Object filename) {
		return super.get(FileObject.getFilename((String)filename, true));
	}

	@Override
	public FileObject put(final String key, final FileObject file) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<FileObject> iterator() {
		final Iterator<Map.Entry<String, FileObject>> iterator = entrySet().iterator();
		return new Iterator<FileObject>() {

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public FileObject next() {
				return iterator.next().getValue();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

		};
	}

	public String downloadIndexAndChecksum(final Progress progress) throws ParserConfigurationException, SAXException {
		try {
			read();
		}
		catch (final FileNotFoundException e) { /* ignore */}
		catch (final IOException e) { /* ignore */ }

		// clear the files
		clear();

		final XMLFileDownloader downloader = new XMLFileDownloader(this);
		downloader.addProgress(progress);
		try {
			downloader.start(false);
		} catch (final UpdateCanceledException e) {
				downloader.done();
				throw e;
		}
		new Checksummer(this, progress).updateFromLocal();

		// When upstream fixed dependencies, heed them
		for (final FileObject file : upToDate()) {
			for (final FileObject dependency : file.getFileDependencies(this, false)) {
				if (dependency.getAction() == Action.NOT_INSTALLED && dependency.isUpdateablePlatform(this)) {
					dependency.setAction(this, Action.INSTALL);
				}
			}
		}

		return downloader.getWarnings();
	}

	public List<Conflict> getConflicts() {
		return conflicts;
	}

	/**
	 * Utility method for Fiji's Bug Submitter
	 *
	 * @return the list of files known to the Updater, with versions, as a String
	 */
	public static String getInstalledVersions(final File ijDirectory, final Progress progress) {
		final FilesCollection files = new FilesCollection(ijDirectory);
		final Checksummer checksummer = new Checksummer(files, progress);
		try {
			checksummer.updateFromLocal();
		} catch (UpdateCanceledException t) {
			return null;
		}

		final Map<String, FileObject.Version> checksums = checksummer.getCachedChecksums();

		final StringBuilder sb = new StringBuilder();
		for (final Map.Entry<String, FileObject.Version> entry : checksums.entrySet()) {
			String file = entry.getKey();
			if (file.startsWith(":") && file.length() > 8) file = file.substring(0, 8);
			final FileObject.Version version = entry.getValue();
			String checksum = version.checksum;
			if (version.checksum != null && version.checksum.length() > 8) {
				final StringBuilder rebuild = new StringBuilder();
				for (final String element : checksum.split(":")) {
					if (rebuild.length() > 0) rebuild.append(":");
					if (element == null || element.length() <= 8) rebuild.append(element);
					else rebuild.append(element.substring(0, 8));
				}
				checksum = rebuild.toString();
			}
			sb.append("  ").append(checksum).append(" ");
			sb.append(version.timestamp).append(" ");
			sb.append(file).append("\n");
		}
		return sb.toString();
	}

	public Collection<String> getProtocols(Iterable<FileObject> selected) {
		final Set<String> protocols = new LinkedHashSet<String>();
		for (final FileObject file : selected) {
			final UpdateSite site = getUpdateSite(file.updateSite);
			if (site != null) {
				if (site.sshHost == null)
					protocols.add("unknown(" + file.filename + ")");
				else
					protocols.add(site.getUploadProtocol());
			}
		}
		return protocols;
	}
}
