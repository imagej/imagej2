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

package imagej.service;

import imagej.ImageJ;
import imagej.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

/**
 * Helper class for discovering and instantiating available services.
 * 
 * @author Curtis Rueden
 */
public class ServiceHelper {

	/** Associated application context. */
	private final ImageJ context;

	/** Classes to scan when searching for dependencies. */
	private final Set<Class<? extends IService>> classPool;

	/** Classes to instantiate as services. */
	private final List<Class<? extends IService>> serviceClasses;

	/**
	 * Creates a new service helper for discovering and instantiating services.
	 * 
	 * @param context The application context for which services should be
	 *          instantiated.
	 */
	public ServiceHelper(final ImageJ context) {
		this(context, null);
	}

	/**
	 * Creates a new service helper for discovering and instantiating services.
	 * 
	 * @param context The application context to which services should be added.
	 * @param serviceClasses The service classes to instantiate.
	 */
	public ServiceHelper(final ImageJ context,
		final Collection<Class<? extends IService>> serviceClasses)
	{
		this.context = context;
		classPool = findServiceClasses();
		this.serviceClasses = new ArrayList<Class<? extends IService>>();
		if (serviceClasses == null) {
			// load all discovered services
			this.serviceClasses.addAll(classPool);
		}
		else {
			// load only the services that were explicitly specified
			this.serviceClasses.addAll(serviceClasses);
		}
	}

	// -- ServiceHelper methods --

	/**
	 * Ensures all candidate service classes are registered in the index, locating
	 * and instantiating compatible services as needed.
	 */
	public void loadServices() {
		for (final Class<? extends IService> serviceClass : serviceClasses) {
			loadService(serviceClass);
		}
	}

	/**
	 * Obtains a service compatible with the given class, instantiating it (and
	 * registering it in the index) if necessary.
	 * 
	 * @return an existing compatible service if one is registered, or else the
	 *         newly created service, or null if none can be instantiated
	 * @throws IllegalArgumentException if no suitable service class is found
	 */
	public <S extends IService> S loadService(final Class<S> c) {
		// if a compatible service already exists, return it
		final S service = context.getServiceIndex().getService(c);
		if (service != null) return service;

		// scan the class pool for a suitable match
		for (final Class<? extends IService> serviceClass : classPool) {
			if (c.isAssignableFrom(serviceClass)) {
				// found a match; now instantiate it
				@SuppressWarnings("unchecked")
				final S result = (S) createExactService(serviceClass);
				return result;
			}
		}

		return createExactService(c);
	}

	/**
	 * Instantiates a service of the given class, registering it in the index.
	 * 
	 * @return the newly created service, or null if the given class cannot be
	 *         instantiated
	 */
	public <S extends IService> S createExactService(final Class<S> c) {
		Log.debug("Creating service: " + c.getName());
		try {
			final Constructor<S> ctor = getConstructor(c);
			final S service = createService(ctor);
			context.getServiceIndex().add(service);
			Log.info("Created service: " + c.getName());
			return service;
		}
		catch (final Exception e) {
			Log.error("Invalid service: " + c.getName(), e);
		}
		return null;
	}

	// -- Helper methods --

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
				args[i] = context.getServiceIndex().getService(c);
				if (args[i] == null) {
					// recursively obtain needed services
					args[i] = loadService(c);
				}
			}
			else if (ImageJ.class.isAssignableFrom(type)) {
				args[i] = context;
			}
			else throw new IllegalArgumentException("Invalid constructor: " + ctor);
		}
		return ctor.newInstance(args);
	}

	/**
	 * Gets a compatible constructor for creating a service of the given type. A
	 * constructor is compatible if all its arguments are assignable to
	 * {@link ImageJ} and {@link IService}. The method uses a greedy approach to
	 * choosing the best constructor, preferring constructors with a larger number
	 * of arguments, to populate the maximum number of services.
	 * 
	 * @return the best constructor to use for instantiating the service
	 * @throws IllegalArgumentException if no compatible constructors exist
	 */
	private <S extends IService> Constructor<S> getConstructor(
		final Class<S> serviceClass)
	{
		final Constructor<?>[] ctors = serviceClass.getConstructors();

		// sort constructors by number of parameters
		Arrays.sort(ctors, new Comparator<Constructor<?>>() {

			@Override
			public int compare(final Constructor<?> c1, final Constructor<?> c2) {
				return c2.getParameterTypes().length - c1.getParameterTypes().length;
			}

		});

		for (final Constructor<?> ctorUntyped : ctors) {
			@SuppressWarnings("unchecked")
			final Constructor<S> ctor = (Constructor<S>) ctorUntyped;

			final Class<?>[] types = ctor.getParameterTypes();
			for (final Class<?> type : types) {
				if (!ImageJ.class.isAssignableFrom(type) &&
					!IService.class.isAssignableFrom(type))
				{
					// constructor has an argument of unknown type
					continue;
				}
			}
			return ctor;
		}
		throw new IllegalArgumentException(
			"No appropriate constructor found for service class: " +
				serviceClass.getName());
	}

	// CTR TODO - Add a level of indirection for usage of SezPoz.
	// That way we can later include additional discovery methods.

	/**
	 * Discovers service implementations that are present on the classpath and
	 * marked with the @{@link Service} annotation.
	 */
	private HashSet<Class<? extends IService>> findServiceClasses() {
		final HashSet<Class<? extends IService>> serviceSet =
			new HashSet<Class<? extends IService>>();

		// use SezPoz to discover available services
		for (final IndexItem<Service, IService> item : Index.load(Service.class,
			IService.class))
		{
			try {
				@SuppressWarnings("unchecked")
				final Class<? extends IService> c =
					(Class<? extends IService>) item.element();
				serviceSet.add(c);
			}
			catch (final InstantiationException e) {
				Log.error("Invalid service: " + item, e);
			}
		}

		return serviceSet;
	}

}
