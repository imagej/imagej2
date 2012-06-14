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

package imagej.ext;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A path in a hierarchical menu structure, for use with {@link UIDetails}.
 * 
 * @author Curtis Rueden
 */
public class MenuPath extends ArrayList<MenuEntry> {

	/** The separator between elements of a menu path string. */
	public static final String PATH_SEPARATOR = ">";

	/** Creates an empty menu path. */
	public MenuPath() {
		// default constructor
	}

	/**
	 * Creates a menu path with the given entries. Passing a {@link MenuPath} as
	 * the argument will make a copy.
	 */
	public MenuPath(final Collection<? extends MenuEntry> menuEntries) {
		addAll(menuEntries);
	}

	/**
	 * Creates a menu path with entries parsed from the given string. Assumes
	 * "&gt;" as the separator (e.g., "File&gt;New&gt;Image").
	 * 
	 * @see #PATH_SEPARATOR
	 */
	public MenuPath(final String path) {
		if (path != null && !path.isEmpty()) {
			final String[] tokens = path.split(PATH_SEPARATOR);
			for (final String token : tokens) {
				add(new MenuEntry(token.trim()));
			}
		}
	}

	/** Gets the final element of the menu path. */
	public MenuEntry getLeaf() {
		if (size() == 0) return null;
		return get(size() - 1);
	}

	/** Gets the menu path as a string. */
	public String getMenuString() {
		return getMenuString(true);
	}

	/** Gets the menu path as a string, with or without the final element. */
	public String getMenuString(final boolean includeLeaf) {
		final StringBuilder sb = new StringBuilder();
		final int size = size();
		final int last = includeLeaf ? size : size - 1;
		for (int i = 0; i < last; i++) {
			final MenuEntry menu = get(i);
			if (i > 0) sb.append(" " + PATH_SEPARATOR + " ");
			sb.append(menu);
		}
		return sb.toString();
	}

}
