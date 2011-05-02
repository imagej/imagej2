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

package imagej;

import imagej.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

/**
 * Top-level application context for ImageJ, which initializes and maintains a
 * list of manager components.
 * 
 * @author Curtis Rueden
 */
public final class ImageJ {

	public static final String VERSION = "2.0.0-alpha2";

	// TODO - decide if singleton pattern is really best here

	private static ImageJ instance;

	/** Initializes all available ImageJ manager components. */
	public static void initialize() {
		getInstance();
	}

	/** Gets the singleton ImageJ instance. */
	public static ImageJ getInstance() {
		if (instance == null) new ImageJ();
		return instance;
	}

	/** Gets the manager component of the given class. */
	public static <T extends ManagerComponent> T get(final Class<T> c) {
		return getInstance().getManager(c);
	}

	/** Discovers manager components present on the classpath. */
	public static List<ManagerComponent> loadManagers() {
		// use SezPoz to discover all manager components
		final List<ManagerEntry> entries = new ArrayList<ManagerEntry>();
		for (final IndexItem<Manager, ManagerComponent> item :
			Index.load(Manager.class, ManagerComponent.class))
		{
			try {
				final float priority = item.annotation().priority();
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
	private final Set<Class<? extends ManagerComponent>> initializedManagers;

	private ImageJ() {
		instance = this;
		managers = new ConcurrentHashMap<Class<?>, ManagerComponent>();
		initializedManagers = new HashSet<Class<? extends ManagerComponent>>();

		// discover available managers
		final List<ManagerComponent> managerList = loadManagers();

		// add managers to lookup table
		for (final ManagerComponent m : managerList) {
			managers.put(m.getClass(), m);
		}

		// initialize manager components
		for (final ManagerComponent m : managerList) {
			Log.debug("Initializing manager component: " + m);
			m.initialize();
			initializedManagers.add(m.getClass());
		}
	}

	/** Gets the manager component of the given class. */
	public <T extends ManagerComponent> T getManager(final Class<T> c) {
		@SuppressWarnings("unchecked")
		final T manager = (T) managers.get(c);
		if (!initializedManagers.contains(c)) {
			// NB: For now, disallow access to uninitialized managers. In the future,
			// there may be a reason to allow it (e.g., managers with circular
			// dependencies), but at the moment it is useful for debugging manager
			// priorities for this exception to be thrown. Ideally, all managers
			// should be initialized in a strict hierarchy. It would probably be
			// worthwhile to think further about the manager architecture in general.
			throw new IllegalStateException(
				"Access to uninitialized manager component: " + c);
		}
		return manager;
	}

	/** Helper class for sorting managers by priority. */
	private static class ManagerEntry implements Comparable<ManagerEntry> {

		protected ManagerComponent manager;
		protected float priority;

		protected ManagerEntry(final ManagerComponent manager,
			final float priority)
		{
			this.manager = manager;
			this.priority = priority;
		}

		@Override
		public int compareTo(final ManagerEntry entry) {
			if (priority < entry.priority) return -1;
			else if (priority > entry.priority) return 1;
			else return 0;
		}

	}

}
