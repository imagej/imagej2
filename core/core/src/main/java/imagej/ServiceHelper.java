//
// ServiceHelper.java
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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

/**
 * Helper class for managing available services.
 * 
 * @author Curtis Rueden
 * @see ImageJ
 */
public class ServiceHelper {

	private final ImageJ context;
	private final Map<Class<? extends IService>, IService> services;
	private final Set<Class<? extends IService>> initialized;

	/** Creates a new service helper with the given services. */
	public ServiceHelper(final ImageJ context) {
		this.context = context;
		services = new ConcurrentHashMap<Class<? extends IService>, IService>();
		initialized =
			Collections.synchronizedSet(new HashSet<Class<? extends IService>>());
	}

	// -- ServiceHelper methods --

	/** Loads the service of the given class. */
	public <S extends IService> void loadService(final Class<S> c) {
		final List<Class<? extends IService>> serviceClasses =
			new ArrayList<Class<? extends IService>>();
		serviceClasses.add(c);
		loadServices(serviceClasses);
	}

	/** Loads the services of the given classes. */
	public void loadServices(
		final Collection<Class<? extends IService>> serviceClasses)
	{
		final Collection<Class<? extends IService>> classes;
		if (serviceClasses == null) classes = findServiceClasses();
		else classes = serviceClasses;

		// instantiate service classes
		for (final Class<? extends IService> c : classes) {
			IService service = getService(c);
			if (service == null) service = createService(c);
			if (service == null) Log.error("Invalid service: " + c.getName());
		}

		// initialize service classes
		for (final Class<? extends IService> c : classes) {
			initializeService(c);
		}
	}

	/** Gets the service of the given class. */
	public <S extends IService> S getService(final Class<S> c) {
		@SuppressWarnings("unchecked")
		final S service = (S) services.get(c);
		return service;
	}

	// -- Helper methods --

	/** Discovers services present on the classpath. */
	private List<Class<? extends IService>> findServiceClasses() {
		final List<Class<? extends IService>> serviceList =
			new ArrayList<Class<? extends IService>>();

		// use SezPoz to discover all services
		for (final IndexItem<Service, IService> item : Index.load(Service.class,
			IService.class))
		{
			try {
				@SuppressWarnings("unchecked")
				final Class<? extends IService> c =
					(Class<? extends IService>) item.element();
				serviceList.add(c);
			}
			catch (final InstantiationException e) {
				Log.error("Invalid service: " + item, e);
			}
		}

		return serviceList;
	}

	/** Instantiates a service of the given class. */
	private <S extends IService> S createService(final Class<S> c) {
		Log.debug("Creating service: " + c.getName());
		try {
			final Constructor<S> ctor = getConstructor(c);
			if (ctor == null) return null; // invalid constructor
			final S service = createService(ctor);
			services.put(c, service);
			return service;
		}
		catch (final Exception e) {
			Log.error("Invalid service: " + c.getName(), e);
		}
		return null;
	}

	/** Instantiates a service using the given constructor. */
	private <S extends IService> S createService(final Constructor<S> ctor)
		throws InstantiationException, IllegalAccessException,
		InvocationTargetException
	{
		final Class<?>[] types = ctor.getParameterTypes();
		final Object[] args = new Object[types.length];
		for (int i = 0; i < types.length; i++) {
			final Class<?> type = types[i];
			if (IService.class.isAssignableFrom(type)) {
				@SuppressWarnings("unchecked")
				final Class<IService> c = (Class<IService>) type;
				args[i] = getService(c);
				if (args[i] == null) {
					// recursively create dependent service
					args[i] = createService(c);
				}
			}
			else if (ImageJ.class.isAssignableFrom(type)) {
				args[i] = context;
			}
			else throw new IllegalArgumentException("Invalid constructor: " + ctor);
		}
		return ctor.newInstance(args);
	}

	private <S extends IService> void initializeService(final Class<S> c) {
		if (initialized.contains(c)) return; // already initialized
		initialized.add(c);

		// initialize dependencies first
		final Constructor<S> ctor = getConstructor(c);
		for (final Class<?> type : ctor.getParameterTypes()) {
			if (IService.class.isAssignableFrom(type)) {
				@SuppressWarnings("unchecked")
				final Class<IService> serviceType = (Class<IService>) type;
				initializeService(serviceType);
			}
		}

		// initialize the service
		Log.info("Initializing service: " + c.getName());
		getService(c).initialize();
	}

	private <S extends IService> Constructor<S> getConstructor(final Class<S> c)
	{
		final Constructor<?>[] ctors = c.getConstructors();
		if (ctors == null) return null;
		for (final Constructor<?> ctor : ctors) {
			final Class<?>[] types = ctor.getParameterTypes();
			if (types == null || types.length == 0) continue; // no constructors
			if (!ImageJ.class.isAssignableFrom(types[0])) continue; // wrong one
			@SuppressWarnings("unchecked")
			final Constructor<S> result = (Constructor<S>) ctor;
			return result;
		}
		// no appropriate constructor found
		return null;
	}

}
