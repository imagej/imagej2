//
// PluginCollection.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.updater.core;

import imagej.updater.core.PluginObject.Action;
import imagej.updater.core.PluginObject.Status;
import imagej.updater.util.DependencyAnalyzer;
import imagej.updater.util.Util;

import java.io.File;
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
public class PluginCollection extends ArrayList<PluginObject> {

	public final static String DEFAULT_UPDATE_SITE = "Fiji";

	public static class UpdateSite {

		public String url, sshHost, uploadDirectory;
		public long timestamp;

		public UpdateSite(String url, String sshHost, String uploadDirectory,
			final long timestamp)
		{
			if (url.equals("http://pacific.mpi-cbg.de/update/")) {
				url = Util.MAIN_URL;
				if (sshHost != null && sshHost.equals("pacific.mpi-cbg.de")) sshHost =
					Util.SSH_HOST;
				else if (sshHost != null && sshHost.endsWith("@pacific.mpi-cbg.de")) sshHost =
					sshHost.substring(0, sshHost.length() - 18) + Util.SSH_HOST;
			}
			if (!url.endsWith("/")) url += "/";
			if (uploadDirectory != null && !uploadDirectory.equals("") &&
				!uploadDirectory.endsWith("/")) uploadDirectory += "/";
			this.url = url;
			this.sshHost = sshHost;
			this.uploadDirectory = uploadDirectory;
			this.timestamp = timestamp;
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

	public PluginCollection() {
		updateSites = new LinkedHashMap<String, UpdateSite>();
		addUpdateSite(DEFAULT_UPDATE_SITE, Util.MAIN_URL, Util.isDeveloper
			? Util.SSH_HOST : null, Util.isDeveloper ? Util.UPDATE_DIRECTORY : null,
			Util.getTimestamp(Util.XML_COMPRESSED));
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

		// handle all plugins
		for (final PluginObject plugin : this)
			if (plugin.updateSite.equals(oldName)) plugin.updateSite = newName;

		// preserve order
		final HashMap<String, UpdateSite> newMap =
			new LinkedHashMap<String, UpdateSite>();
		for (final String name : updateSites.keySet())
			if (name.equals(oldName)) newMap.put(newName, getUpdateSite(oldName));
			else newMap.put(name, getUpdateSite(name));

		updateSites = newMap;
	}

	public void removeUpdateSite(final String name) {
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
		for (final PluginObject plugin : toUpload(true))
			set.add(plugin.updateSite);
		for (PluginObject plugin : toRemove())
			set.add(plugin.updateSite);
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

	public Action[] getActions(final PluginObject plugin) {
		return plugin.isUploadable(this) ? plugin.getStatus().getDeveloperActions()
			: plugin.getStatus().getActions();
	}

	public Action[] getActions(final Iterable<PluginObject> plugins) {
		List<Action> result = null;
		for (final PluginObject plugin : plugins) {
			final Action[] actions = getActions(plugin);
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
		new XMLFileReader(this).read(new File(Util.prefix(Util.XML_COMPRESSED)));
	}

	public void write() throws IOException, SAXException,
		TransformerConfigurationException, ParserConfigurationException
	{
		new XMLFileWriter(this).write(new GZIPOutputStream(new FileOutputStream(
			Util.prefix(Util.XML_COMPRESSED))), true);
	}

	protected static DependencyAnalyzer dependencyAnalyzer;

	public interface Filter {

		boolean matches(PluginObject plugin);
	}

	public static PluginCollection clone(final Iterable<PluginObject> iterable) {
		final PluginCollection result = new PluginCollection();
		for (final PluginObject plugin : iterable)
			result.add(plugin);
		return result;
	}

	public Iterable<PluginObject> toUploadOrRemove() {
		return filter(or(is(Action.UPLOAD), is(Action.REMOVE)));
	}

	public Iterable<PluginObject> toUpload() {
		return toUpload(false);
	}

	public Iterable<PluginObject> toUpload(final boolean includeMetadataChanges) {
		if (!includeMetadataChanges) return filter(is(Action.UPLOAD));
		return filter(or(is(Action.UPLOAD), new Filter() {

			@Override
			public boolean matches(final PluginObject plugin) {
				return plugin.metadataChanged;
			}
		}));
	}

	public Iterable<PluginObject> toUpload(final String updateSite) {
		return filter(and(is(Action.UPLOAD), isUpdateSite(updateSite)));
	}

	public Iterable<PluginObject> toUninstall() {
		return filter(is(Action.UNINSTALL));
	}

	public Iterable<PluginObject> toRemove() {
		return filter(is(Action.REMOVE));
	}

	public Iterable<PluginObject> toUpdate() {
		return filter(is(Action.UPDATE));
	}

	public Iterable<PluginObject> upToDate() {
		return filter(is(Action.INSTALLED));
	}

	public Iterable<PluginObject> toInstall() {
		return filter(is(Action.INSTALL));
	}

	public Iterable<PluginObject> toInstallOrUpdate() {
		return filter(oneOf(new Action[] { Action.INSTALL, Action.UPDATE }));
	}

	public Iterable<PluginObject> notHidden() {
		return filter(and(not(is(Status.OBSOLETE_UNINSTALLED)), doesPlatformMatch()));
	}

	public Iterable<PluginObject> uninstalled() {
		return filter(is(Status.NOT_INSTALLED));
	}

	public Iterable<PluginObject> installed() {
		return filter(not(oneOf(new Status[] { Status.LOCAL_ONLY,
			Status.NOT_INSTALLED })));
	}

	public Iterable<PluginObject> locallyModified() {
		return filter(oneOf(new Status[] { Status.MODIFIED,
			Status.OBSOLETE_MODIFIED }));
	}

	public Iterable<PluginObject> forUpdateSite(final String name) {
		return filter(isUpdateSite(name));
	}

	public Iterable<PluginObject> fijiPlugins() {
		return filter(not(is(Status.LOCAL_ONLY)));
	}

	public Iterable<PluginObject> forCurrentTXT() {
		return filter(and(not(oneOf(new Status[] { Status.LOCAL_ONLY,
			Status.OBSOLETE, Status.OBSOLETE_MODIFIED, Status.OBSOLETE_UNINSTALLED
		/* the old updater will only checksum these! */
		})), or(startsWith("fiji-"), and(startsWith(new String[] { "plugins/",
			"jars/", "retro/", "misc/" }), endsWith(".jar")))));
	}

	public Iterable<PluginObject> nonFiji() {
		return filter(is(Status.LOCAL_ONLY));
	}

	public Iterable<PluginObject> shownByDefault() {
		/*
		 * Let's not show the NOT_INSTALLED ones, as the user chose not
		 * to have them.
		 */
		final Status[] oneOf =
			{ Status.UPDATEABLE, Status.NEW, Status.OBSOLETE,
				Status.OBSOLETE_MODIFIED };
		return filter(or(oneOf(oneOf), is(Action.INSTALL)));
	}

	public Iterable<PluginObject> uploadable() {
		return filter(new Filter() {

			@Override
			public boolean matches(final PluginObject plugin) {
				return plugin.isUploadable(PluginCollection.this);
			}
		});
	}

	public Iterable<PluginObject> changes() {
		return filter(new Filter() {

			@Override
			public boolean matches(final PluginObject plugin) {
				return plugin.getAction() != plugin.getStatus().getActions()[0];
			}
		});
	}

	public static class FilteredIterator implements Iterator<PluginObject> {

		Filter filter;
		boolean opposite;
		Iterator<PluginObject> iterator;
		PluginObject next;

		FilteredIterator(final Filter filter, final Iterable<PluginObject> plugins)
		{
			this.filter = filter;
			iterator = plugins.iterator();
			findNext();
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public PluginObject next() {
			final PluginObject plugin = next;
			findNext();
			return plugin;
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

	public static Iterable<PluginObject> filter(final Filter filter,
		final Iterable<PluginObject> plugins)
	{
		return new Iterable<PluginObject>() {

			@Override
			public Iterator<PluginObject> iterator() {
				return new FilteredIterator(filter, plugins);
			}
		};
	}

	public static Iterable<PluginObject> filter(final String search,
		final Iterable<PluginObject> plugins)
	{
		final String keyword = search.trim().toLowerCase();
		return filter(new Filter() {

			@Override
			public boolean matches(final PluginObject plugin) {
				return plugin.getFilename().trim().toLowerCase().indexOf(keyword) >= 0;
			}
		}, plugins);
	}

	public Filter yes() {
		return new Filter() {

			@Override
			public boolean matches(final PluginObject plugin) {
				return true;
			}
		};
	}

	public Filter doesPlatformMatch() {
		// If we're a developer or no platform was specified, return yes
		if (hasUploadableSites()) return yes();
		return new Filter() {

			@Override
			public boolean matches(final PluginObject plugin) {
				return plugin.isUpdateablePlatform();
			}
		};
	}

	public Filter is(final Action action) {
		return new Filter() {

			@Override
			public boolean matches(final PluginObject plugin) {
				return plugin.getAction() == action;
			}
		};
	}

	public Filter isNoAction() {
		return new Filter() {

			@Override
			public boolean matches(final PluginObject plugin) {
				return plugin.getAction() == plugin.getStatus().getNoAction();
			}
		};
	}

	public Filter oneOf(final Action[] actions) {
		final Set<Action> oneOf = new HashSet<Action>();
		for (final Action action : actions)
			oneOf.add(action);
		return new Filter() {

			@Override
			public boolean matches(final PluginObject plugin) {
				return oneOf.contains(plugin.getAction());
			}
		};
	}

	public Filter is(final Status status) {
		return new Filter() {

			@Override
			public boolean matches(final PluginObject plugin) {
				return plugin.getStatus() == status;
			}
		};
	}

	public Filter isUpdateSite(final String updateSite) {
		return new Filter() {

			@Override
			public boolean matches(final PluginObject plugin) {
				return plugin.updateSite != null && // is null for non-Fiji files
					plugin.updateSite.equals(updateSite);
			}
		};
	}

	public Filter oneOf(final Status[] states) {
		final Set<Status> oneOf = new HashSet<Status>();
		for (final Status status : states)
			oneOf.add(status);
		return new Filter() {

			@Override
			public boolean matches(final PluginObject plugin) {
				return oneOf.contains(plugin.getStatus());
			}
		};
	}

	public Filter startsWith(final String prefix) {
		return new Filter() {

			@Override
			public boolean matches(final PluginObject plugin) {
				return plugin.filename.startsWith(prefix);
			}
		};
	}

	public Filter startsWith(final String[] prefixes) {
		return new Filter() {

			@Override
			public boolean matches(final PluginObject plugin) {
				for (final String prefix : prefixes)
					if (plugin.filename.startsWith(prefix)) return true;
				return false;
			}
		};
	}

	public Filter endsWith(final String suffix) {
		return new Filter() {

			@Override
			public boolean matches(final PluginObject plugin) {
				return plugin.filename.endsWith(suffix);
			}
		};
	}

	public Filter not(final Filter filter) {
		return new Filter() {

			@Override
			public boolean matches(final PluginObject plugin) {
				return !filter.matches(plugin);
			}
		};
	}

	public Filter or(final Filter a, final Filter b) {
		return new Filter() {

			@Override
			public boolean matches(final PluginObject plugin) {
				return a.matches(plugin) || b.matches(plugin);
			}
		};
	}

	public Filter and(final Filter a, final Filter b) {
		return new Filter() {

			@Override
			public boolean matches(final PluginObject plugin) {
				return a.matches(plugin) && b.matches(plugin);
			}
		};
	}

	public Iterable<PluginObject> filter(final Filter filter) {
		return filter(filter, this);
	}

	public PluginObject getPlugin(final String filename) {
		for (final PluginObject plugin : this) {
			if (plugin.getFilename().equals(filename)) return plugin;
		}
		return null;
	}

	public PluginObject getPlugin(final String filename, final long timestamp) {
		for (final PluginObject plugin : this)
			if (plugin.getFilename().equals(filename) &&
				plugin.getTimestamp() == timestamp) return plugin;
		return null;
	}

	public PluginObject getPluginFromDigest(final String filename,
		final String digest)
	{
		for (final PluginObject plugin : this)
			if (plugin.getFilename().equals(filename) &&
				plugin.getChecksum().equals(digest)) return plugin;
		return null;
	}

	public Iterable<String> analyzeDependencies(final PluginObject plugin) {
		try {
			if (dependencyAnalyzer == null) dependencyAnalyzer =
				new DependencyAnalyzer();
			final String path = Util.prefix(plugin.getFilename());
			return dependencyAnalyzer.getDependencies(path);
		}
		catch (final IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void updateDependencies(final PluginObject plugin) {
		final Iterable<String> dependencies = analyzeDependencies(plugin);
		if (dependencies == null) return;
		for (final String dependency : dependencies)
			plugin.addDependency(dependency);
	}

	public boolean has(final Filter filter) {
		for (final PluginObject plugin : this)
			if (filter.matches(plugin)) return true;
		return false;
	}

	public boolean hasChanges() {
		return has(not(isNoAction()));
	}

	public boolean hasUploadOrRemove() {
		return has(oneOf(new Action[] { Action.UPLOAD, Action.REMOVE }));
	}

	public boolean hasForcableUpdates() {
		for (final PluginObject plugin : updateable(true))
			if (!plugin.isUpdateable(false)) return true;
		return false;
	}

	public Iterable<PluginObject> updateable(final boolean evenForcedOnes) {
		return filter(new Filter() {

			@Override
			public boolean matches(final PluginObject plugin) {
				return plugin.isUpdateable(evenForcedOnes) &&
					plugin.isUpdateablePlatform();
			}
		});
	}

	public void markForUpdate(final boolean evenForcedUpdates) {
		for (final PluginObject file : updateable(evenForcedUpdates)) {
			plugin.setFirstValidAction(this, new Action[] { Action.UPDATE,
				Action.UNINSTALL, Action.INSTALL });
		}
	}

	public String getURL(final PluginObject plugin) {
		final String siteName = plugin.updateSite;
		assert (siteName != null && !siteName.equals(""));
		final UpdateSite site = getUpdateSite(siteName);
		return site.url + plugin.filename.replace(" ", "%20") + "-" +
			plugin.getTimestamp();
	}

	public static class DependencyMap extends
		HashMap<PluginObject, PluginCollection>
	{

		// returns true when the map did not have the dependency before
		public boolean add(final PluginObject dependency,
			final PluginObject dependencee)
		{
			if (containsKey(dependency)) {
				get(dependency).add(dependencee);
				return false;
			}
			final PluginCollection list = new PluginCollection();
			list.add(dependencee);
			put(dependency, list);
			return true;
		}
	}

	// TODO: for developers, there should be a consistency check:
	// no dependencies on non-Fiji plugins, no circular dependencies,
	// and no overring circular dependencies.
	void addDependencies(final PluginObject plugin, final DependencyMap map,
		final boolean overriding)
	{
		for (final Dependency dependency : plugin.getDependencies()) {
			final PluginObject other = getPlugin(dependency.filename);
			if (other == null || overriding != dependency.overrides ||
				!other.isUpdateablePlatform()) continue;
			if (dependency.overrides) {
				if (other.willNotBeInstalled()) continue;
			}
			else if (other.willBeUpToDate()) continue;
			if (!map.add(other, plugin)) continue;
			// overriding dependencies are not recursive
			if (!overriding) addDependencies(other, map, overriding);
		}
	}

	public DependencyMap getDependencies(final boolean overridingOnes) {
		final DependencyMap result = new DependencyMap();
		for (final PluginObject plugin : toInstallOrUpdate())
			addDependencies(plugin, result, overridingOnes);
		return result;
	}

	public void sort() {
		// first letters in this order: 'f', 'p', 'j', 's', 'i', 'm'
		Collections.sort(this, new Comparator<PluginObject>() {

			@Override
			public int compare(final PluginObject a, final PluginObject b) {
				final int result = firstChar(a) - firstChar(b);
				return result != 0 ? result : a.filename.compareTo(b.filename);
			}

			int firstChar(final PluginObject plugin) {
				final char c = plugin.filename.charAt(0);
				final int index = "fpjsim".indexOf(c);
				return index < 0 ? 0x200 + c : index;
			}
		});
	}

	String checkForCircularDependency(final PluginObject plugin,
		final Set<PluginObject> seen)
	{
		if (seen.contains(plugin)) return "";
		final String result =
			checkForCircularDependency(plugin, seen, new HashSet<PluginObject>());
		if (result == null) return "";

		// Display only the circular dependency
		final int last = result.lastIndexOf(' ');
		final int off = result.lastIndexOf(result.substring(last), last - 1);
		return "Circular dependency detected: " + result.substring(off + 1) + "\n";
	}

	String checkForCircularDependency(final PluginObject plugin,
		final Set<PluginObject> seen, final Set<PluginObject> chain)
	{
		if (seen.contains(plugin)) return null;
		for (final String dependency : plugin.dependencies.keySet()) {
			final PluginObject dep = getPlugin(dependency);
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
		final Set<PluginObject> circularChecked = new HashSet<PluginObject>();
		for (final PluginObject plugin : this) {
			result.append(checkForCircularDependency(plugin, circularChecked));
			// only non-obsolete components can have dependencies
			final Set<String> deps = plugin.dependencies.keySet();
			if (deps.size() > 0 && plugin.isObsolete()) result
				.append("Obsolete plugin " + plugin + "has dependencies: " +
					Util.join(", ", deps) + "!\n");
			for (final String dependency : deps) {
				final PluginObject dep = getPlugin(dependency);
				if (dep == null || dep.current == null) result.append("The plugin " +
					plugin + " has the obsolete/local-only " + "dependency " +
					dependency + "!\n");
			}
		}
		return result.length() > 0 ? result.toString() : null;
	}

	@Override
	public String toString() {
		return Util.join(", ", this);
	}
}
