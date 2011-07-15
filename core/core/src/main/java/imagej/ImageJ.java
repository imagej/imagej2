//
// ImageJ.java
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
 * list of services.
 * 
 * @author Curtis Rueden
 * @see IService
 */
public final class ImageJ {

	public static final String VERSION = "2.0.0-alpha3";

	// TODO - decide if singleton pattern is really best here

	private static ImageJ instance;

	/** Initializes all available ImageJ services. */
	public static void initialize() {
		getInstance();
	}

	/** Gets the singleton ImageJ instance. */
	public static ImageJ getInstance() {
		if (instance == null) new ImageJ();
		return instance;
	}

	/** Gets the service of the given class. */
	public static <S extends IService> S get(final Class<S> c) {
		return getInstance().getService(c);
	}

	/** Discovers services present on the classpath. */
	public static List<IService> loadServices() {
		// use SezPoz to discover all services
		final List<ServiceEntry> entries = new ArrayList<ServiceEntry>();
		for (final IndexItem<Service, IService> item : Index.load(Service.class,
			IService.class))
		{
			try {
				final float priority = item.annotation().priority();
				entries.add(new ServiceEntry(item.instance(), priority));
			}
			catch (final InstantiationException e) {
				Log.warn("Invalid service: " + item, e);
			}
		}
		Collections.sort(entries);

		final List<IService> serviceList = new ArrayList<IService>();
		for (final ServiceEntry entry : entries)
			serviceList.add(entry.service);
		return serviceList;
	}

	private final Map<Class<?>, IService> services;
	private final Set<Class<? extends IService>> initializedServices;

	private ImageJ() {
		instance = this;
		services = new ConcurrentHashMap<Class<?>, IService>();
		initializedServices = new HashSet<Class<? extends IService>>();

		// discover available services
		final List<IService> serviceList = loadServices();

		// add services to lookup table
		for (final IService s : serviceList) {
			services.put(s.getClass(), s);
		}

		// initialize services
		for (final IService s : serviceList) {
			Log.info("Initializing service: " + s.getClass().getName());
			s.initialize();
			initializedServices.add(s.getClass());
		}
	}

	/** Gets the service of the given class. */
	public <S extends IService> S getService(final Class<S> c) {
		@SuppressWarnings("unchecked")
		final S service = (S) services.get(c);
		if (!initializedServices.contains(c)) {
			// NB: For now, disallow access to uninitialized services. In the future,
			// there may be a reason to allow it (e.g., services with circular
			// dependencies), but at the moment it is useful for debugging service
			// priorities for this exception to be thrown. Ideally, all services
			// should be initialized in a strict hierarchy. It would probably be
			// worthwhile to think further about the service architecture in general.
			throw new IllegalStateException(
				"Access to uninitialized service: " + c);
		}
		return service;
	}

	/** Helper class for sorting services by priority. */
	private static class ServiceEntry implements Comparable<ServiceEntry> {

		protected IService service;
		protected float priority;

		protected ServiceEntry(final IService service, final float priority) {
			this.service = service;
			this.priority = priority;
		}

		@Override
		public int compareTo(final ServiceEntry entry) {
			if (priority < entry.priority) return -1;
			else if (priority > entry.priority) return 1;
			else return 0;
		}

	}

}
