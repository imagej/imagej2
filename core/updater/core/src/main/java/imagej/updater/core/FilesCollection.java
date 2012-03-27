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
import imagej.updater.util.DependencyAnalyzer;
import imagej.updater.util.Util;
import imagej.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPOutputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;

import org.xml.sax.SAXException;

@SuppressWarnings("serial")
public class FilesCollection extends LinkedHashMap<String, FileObject>
	implements Iterable<FileObject>
{

	public final static String DEFAULT_UPDATE_SITE = "ImageJ";
	protected File imagejRoot;
	protected Set<FileObject> ignoredConflicts = new HashSet<FileObject>();

	public static class UpdateSite implements Cloneable {

		public String url, sshHost, uploadDirectory;
		public long timestamp;

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
			return uploadDirectory != null && !uploadDirectory.equals("");
		}

		@Override
		public String toString() {
			return url + (sshHost != null ? ", " + sshHost : "") +
				(uploadDirectory != null ? ", " + uploadDirectory : "");
		}
	}

	protected Map<String, UpdateSite> updateSites;

	/**
	 * This constructor takes the imagejRoot primarily for testing purposes.
	 * 
	 * @param imagejRoot the ImageJ directory
	 */
	public FilesCollection(final File imagejRoot) {
		this.imagejRoot = imagejRoot;
		updateSites = new LinkedHashMap<String, UpdateSite>();
		addUpdateSite(DEFAULT_UPDATE_SITE, Util.MAIN_URL, null, null,
			imagejRoot == null ? 0 : Util.getTimestamp(prefix(Util.XML_COMPRESSED)));
	}

	public void addUpdateSite(final String name, final String url,
		final String sshHost, final String uploadDirectory, final long timestamp)
	{
		updateSites.put(name, new UpdateSite(url, sshHost, uploadDirectory,
			timestamp));
	}

	public void renameUpdateSite(final String oldName, final String newName) {
		if (getUpdateSite(newName) != null) throw new RuntimeException(
			"Update site " + newName + " exists already!");
		if (getUpdateSite(oldName) == null) throw new RuntimeException(
			"Update site " + oldName + " does not exist!");

		// handle all files
		for (final FileObject file : this)
			if (file.updateSite.equals(oldName)) file.updateSite = newName;

		// preserve order
		final HashMap<String, UpdateSite> newMap =
			new LinkedHashMap<String, UpdateSite>();
		for (final String name : updateSites.keySet())
			if (name.equals(oldName)) newMap.put(newName, getUpdateSite(oldName));
			else newMap.put(name, getUpdateSite(name));

		updateSites = newMap;
	}

	public void removeUpdateSite(final String name) {
		// remove NOT_INSTALLED plugins, mark others LOCAL_ONLY
		for (final FileObject file : forUpdateSite(name))
			if (file.getStatus() == Status.NOT_INSTALLED) {
				remove(file);
			}
			else {
				file.setStatus(Status.LOCAL_ONLY);
			}
		updateSites.remove(name);
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

	public Action[] getActions(final FileObject file) {
		return file.isUploadable(this) ? file.getStatus().getDeveloperActions()
			: file.getStatus().getActions();
	}

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
	}

	public void write() throws IOException, SAXException,
		TransformerConfigurationException, ParserConfigurationException
	{
		new XMLFileWriter(this).write(new GZIPOutputStream(new FileOutputStream(
			prefix(Util.XML_COMPRESSED))), true);
	}

	protected static DependencyAnalyzer dependencyAnalyzer;

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
		return filter(oneOf(new Action[] { Action.INSTALL, Action.UPDATE }));
	}

	public Iterable<FileObject> notHidden() {
		return filter(and(not(is(Status.OBSOLETE_UNINSTALLED)), doesPlatformMatch()));
	}

	public Iterable<FileObject> uninstalled() {
		return filter(is(Status.NOT_INSTALLED));
	}

	public Iterable<FileObject> installed() {
		return filter(not(oneOf(new Status[] { Status.LOCAL_ONLY,
			Status.NOT_INSTALLED })));
	}

	public Iterable<FileObject> locallyModified() {
		return filter(oneOf(new Status[] { Status.MODIFIED,
			Status.OBSOLETE_MODIFIED }));
	}

	public Iterable<FileObject> forUpdateSite(final String name) {
		return filter(isUpdateSite(name));
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
				return file.getAction() != file.getStatus().getActions()[0];
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
				return file.isUpdateablePlatform();
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

	public Filter oneOf(final Action[] actions) {
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

	public Filter oneOf(final Status[] states) {
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

	public Filter startsWith(final String[] prefixes) {
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
			return dependencyAnalyzer.getDependencies(imagejRoot, file.getFilename());
		}
		catch (final IOException e) {
			Log.error(e);
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
		return has(oneOf(new Action[] { Action.UPLOAD, Action.REMOVE }));
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
				return file.isUpdateable(evenForcedOnes) && file.isUpdateablePlatform();
			}
		});
	}

	public void markForUpdate(final boolean evenForcedUpdates) {
		for (final FileObject file : updateable(evenForcedUpdates)) {
			file.setFirstValidAction(this, new Action[] { Action.UPDATE,
				Action.UNINSTALL, Action.INSTALL });
		}
	}

	public String getURL(final FileObject file) {
		final String siteName = file.updateSite;
		assert (siteName != null && !siteName.equals(""));
		final UpdateSite site = getUpdateSite(siteName);
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

	// TODO: for developers, there should be a consistency check:
	// no dependencies on local-only files, no circular dependencies,
	// and no overring circular dependencies.
	void addDependencies(final FileObject file, final DependencyMap map,
		final boolean overriding)
	{
		for (final Dependency dependency : file.getDependencies()) {
			final FileObject other = get(dependency.filename);
			if (other == null || overriding != dependency.overrides ||
				!other.isUpdateablePlatform()) continue;
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
			put(file.filename, file);
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

	public FileObject get(final int index) {
		throw new UnsupportedOperationException();
	}

	public void add(final FileObject file) {
		put(file.filename, file);
	}

	@Override
	public Iterator<FileObject> iterator() {
		final Iterator<String> iterator = keySet().iterator();
		return new Iterator<FileObject>() {

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public FileObject next() {
				return get(iterator.next());
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

		};
	}
}
