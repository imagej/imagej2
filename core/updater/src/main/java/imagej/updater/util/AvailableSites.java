/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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
 * #L%
 */

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
		int end = text.indexOf("\n|}\n", start);
		if (end < 0) end = text.length();
		if (start < 0) {
			throw new IOException("Could not find table");
		}
		final String[] table = text.substring(start + 1, end).split("\n\\|-");

		final Map<String, UpdateSite> result = new LinkedHashMap<String, UpdateSite>();
		int nameColumn = -1;
		int urlColumn = -1;
		int descriptionColumn = -1;
		int maintainerColumn = -1;
		for (final String row : table) {
			if (row.matches("(?s)(\\{\\||[\\|!](style=\"vertical-align|colspan=\"4\")).*")) continue;
			final String[] columns = row.split("\n[\\|!]");
			if (columns.length > 1 && columns[1].endsWith("|'''Name'''")) {
				nameColumn = urlColumn = descriptionColumn = maintainerColumn = -1;
				int i = 0;
				for (final String column : columns) {
					if (column.endsWith("|'''Name'''")) nameColumn = i;
					else if (column.endsWith("|'''Site'''")) urlColumn = i;
					else if (column.endsWith("|'''URL'''")) urlColumn = i;
					else if (column.endsWith("|'''Description'''")) descriptionColumn = i;
					else if (column.endsWith("|'''Maintainer'''")) maintainerColumn = i;
					i++;
				}
			} else if (nameColumn >= 0 && urlColumn >= 0 && columns.length > nameColumn && columns.length > urlColumn) {
				final UpdateSite info = new UpdateSite(stripWikiMarkup(columns, nameColumn), stripWikiMarkup(columns, urlColumn),
						null, null,
						stripWikiMarkup(columns, descriptionColumn), stripWikiMarkup(columns, maintainerColumn), 0l);
				result.put(info.getURL(), info);
			}
		}

		// Sanity checks
		final Iterator<UpdateSite> iter = result.values().iterator();
		if (!iter.hasNext()) throw new IOException("Invalid page: " + SITE_LIST_PAGE_TITLE);
		UpdateSite site = iter.next();
		if (!site.getName().equals("ImageJ") || !site.getURL().equals("http://update.imagej.net/")) {
			throw new IOException("Invalid page: " + SITE_LIST_PAGE_TITLE);
		}
		if (!iter.hasNext()) throw new IOException("Invalid page: " + SITE_LIST_PAGE_TITLE);
		site = iter.next();
		if (!site.getName().equals("Fiji") || !site.getURL().equals("http://fiji.sc/update/")) {
			throw new IOException("Invalid page: " + SITE_LIST_PAGE_TITLE);
		}

		return result;
	}

	/**
	 * Parses the list of known update sites from the ImageJ wiki.
	 * <p>
	 * <strong>NB:</strong> This method does <em>not</em> add the sites to the
	 * given {@link FilesCollection}! Use
	 * {@link #initializeAndAddSites(FilesCollection)} for that.
	 * </p>
	 */
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

	/**
	 * Initializes the list of update sites,
	 * <em>and<em> adds them to the given {@link FilesCollection}.
	 */
	public static void initializeAndAddSites(final FilesCollection files) {
		for (final UpdateSite site : initializeSites(files)) {
			files.addUpdateSite(site);
		}

	}

	private static String stripWikiMarkup(final String[] columns, int index) {
		if (index < 0 || index >= columns.length) return null;
		final String string = columns[index];
		return string.replaceAll("'''", "").replaceAll("\\[\\[([^\\|\\]]*\\|)?([^\\]]*)\\]\\]", "$2").replaceAll("\\[[^\\[][^ ]*([^\\]]*)\\]", "$1");
	}

}
