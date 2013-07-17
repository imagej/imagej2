package imagej.updater.util;

import imagej.updater.core.FilesCollection;
import imagej.updater.core.UpdateSite;
import imagej.util.MediaWikiClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for parsing the list of available update sites.
 * 
 * @author Johannes Schindelin
 * @author Curtis Rueden
 */
public final class AvailableSites {

  private AvailableSites() {
    // NB: prevent instantiation of utility class
  }

	private static final String SITE_LIST_PAGE_TITLE = "List of update sites";

	public static Map<String, UpdateSite> getAvailableSites() throws IOException {
		final MediaWikiClient wiki = new MediaWikiClient();
		final String text = wiki.getPageSource(SITE_LIST_PAGE_TITLE);

		final int start = text.indexOf("\n{| class=\"wikitable\"\n");
		final int end = text.indexOf("\n|}\n", start);
		if (start < 0 || end < 0) {
			throw new Error("Could not find table");
		}
		final String[] table = text.substring(start + 1, end).split("\n\\|-");

		final Map<String, UpdateSite> result = new LinkedHashMap<String, UpdateSite>();
		for (final String row : table) {
			if (row.matches("(?s)(\\{\\||[\\|!](style=\"vertical-align|colspan=\"4\")).*")) continue;
			final String[] columns = row.split("\n[\\|!]");
			if (columns.length == 5 && !columns[1].endsWith("|'''Name'''")) {
				final UpdateSite info = new UpdateSite(stripWikiMarkup(columns[1]), stripWikiMarkup(columns[2]), null, null, stripWikiMarkup(columns[3]), stripWikiMarkup(columns[4]), 0l);
				result.put(info.getURL(), info);
			}
		}

		// Sanity checks
		final Iterator<UpdateSite> iter = result.values().iterator();
		if (!iter.hasNext()) throw new Error("Invalid page: " + SITE_LIST_PAGE_TITLE);
		UpdateSite site = iter.next();
		if (!site.getName().equals("ImageJ") || !site.getURL().equals("http://update.imagej.net/")) {
			throw new Error("Invalid page: " + SITE_LIST_PAGE_TITLE);
		}
		if (!iter.hasNext()) throw new Error("Invalid page: " + SITE_LIST_PAGE_TITLE);
		site = iter.next();
		if (!site.getName().equals("Fiji") || !site.getURL().equals("http://fiji.sc/update/")) {
			throw new Error("Invalid page: " + SITE_LIST_PAGE_TITLE);
		}

		return result;
	}

	public static List<UpdateSite> initializeSites(final FilesCollection files) {
		final List<UpdateSite> sites = new ArrayList<UpdateSite>();
		final Map<String, Integer> url2index = new HashMap<String, Integer>();

		// make sure that the main update site is the first one.
		final UpdateSite mainSite = new UpdateSite(FilesCollection.DEFAULT_UPDATE_SITE, Util.MAIN_URL, "", "", null, null, 0l);
		sites.add(mainSite);
		url2index.put(mainSite.getURL(), 0);

		// read available sites from the Fiji Wiki
		try {
			for (final UpdateSite site : getAvailableSites().values()) {
				Integer index = url2index.get(site.getURL());
				if (index == null) {
					url2index.put(site.getURL(), sites.size());
					sites.add(site);
				} else {
					sites.set(index.intValue(), site);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// add active / upload information
		final Set<String> names = new HashSet<String>();
		for (final String name : files.getUpdateSiteNames(true)) {
			final UpdateSite site = files.getUpdateSite(name, true);
			Integer index = url2index.get(site.getURL());
			if (index == null) {
				url2index.put(site.getURL(), sites.size());
				sites.add(site);
			} else {
				final UpdateSite listed = sites.get(index.intValue());
				site.setDescription(listed.getDescription());
				site.setMaintainer(listed.getMaintainer());
				sites.set(index.intValue(), site);
			}
		}

		// make sure names are unique
		for (final UpdateSite site : sites) {
			if (site.isActive()) continue;
			if (names.contains(site.getName())) {
				int i = 2;
				while (names.contains(site.getName() + "-" + i))
					i++;
				site.setName(site.getName() + ("-" + i));
			}
			names.add(site.getName());
		}

		return sites;
	}

	private static String stripWikiMarkup(final String string) {
		return string.replaceAll("'''", "").replaceAll("\\[\\[([^\\|\\]]*\\|)?([^\\]]*)\\]\\]", "$2").replaceAll("\\[[^\\[][^ ]*([^\\]]*)\\]", "$1");
	}

}
