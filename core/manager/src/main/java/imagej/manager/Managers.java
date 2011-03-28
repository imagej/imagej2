//
// Managers.java
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

package imagej.manager;

import imagej.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

/**
 * Utility class for working with ImageJ manager components.
 * 
 * @author Curtis Rueden
 */
public final class Managers {

	// TODO - decide if singleton pattern is really best here

	private static Managers instance;

	/** Initializes all available ImageJ manager components. */
	public static void initialize() {
		getInstance();
	}

	public static Managers getInstance() {
		if (instance == null) instance = new Managers();
		return instance;
	}

	public static <T extends ManagerComponent> T get(final Class<T> c) {
		return getInstance().getManager(c);
	}

	public static List<ManagerComponent> loadManagers() {
		// use SezPoz to discover all manager components
		final List<ManagerEntry> entries = new ArrayList<ManagerEntry>();
		for (final IndexItem<Manager, ManagerComponent> item :
			Index.load(Manager.class, ManagerComponent.class))
		{
			try {
				final int priority = item.annotation().priority();
				entries.add(new ManagerEntry(item.instance(), priority));
			}
			catch (final InstantiationException e) {
				Log.warn("Invalid manager component: " + item, e);
			}
		}
		Collections.sort(entries);

		final List<ManagerComponent> managers = new ArrayList<ManagerComponent>();
		for (final ManagerEntry entry : entries) managers.add(entry.manager);
		return managers;
	}

	private final Map<Class<?>, ManagerComponent> managers;

	private Managers() {
		managers = new Hashtable<Class<?>, ManagerComponent>();
		for (ManagerComponent m : Managers.loadManagers()) {
			managers.put(m.getClass(), m);
			Log.debug("Initializing manager component: " + m);
			m.initialize();
		}
		instance = this;
	}

	public <T extends ManagerComponent> T getManager(final Class<T> c) {
		@SuppressWarnings("unchecked")
		final T manager = (T) managers.get(c);
		return manager;
	}

	/** Helper class for sorting managers by priority. */
	private static class ManagerEntry implements Comparable<ManagerEntry> {

		protected ManagerComponent manager;
		protected int priority;

		protected ManagerEntry(final ManagerComponent manager, final int priority)
		{
			this.manager = manager;
			this.priority = priority;
		}

		@Override
		public int compareTo(final ManagerEntry entry) {
			return priority - entry.priority;
		}

	}

}
