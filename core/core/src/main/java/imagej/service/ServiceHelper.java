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

import imagej.AbstractContextual;
import imagej.ImageJ;
import imagej.Priority;
import imagej.event.EventService;
import imagej.ext.InstantiableException;
import imagej.ext.plugin.Plugin;
import imagej.ext.plugin.PluginInfo;
import imagej.log.LogService;
import imagej.service.event.ServicesLoadedEvent;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Helper class for discovering and instantiating available services.
 * 
 * @author Curtis Rueden
 */
public class ServiceHelper extends AbstractContextual {

	/** Classes to scan when searching for dependencies. */
	private final List<Class<? extends Service>> classPool;

	/** Classes to instantiate as services. */
	private final List<Class<? extends Service>> serviceClasses;

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
		final Collection<Class<? extends Service>> serviceClasses)
	{
		setContext(context);
		classPool = findServiceClasses();
		this.serviceClasses = new ArrayList<Class<? extends Service>>();
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
		for (final Class<? extends Service> serviceClass : serviceClasses) {
			loadService(serviceClass);
		}
		EventService eventService = getContext().getService(EventService.class);
		if (eventService != null)
			eventService.publish(new ServicesLoadedEvent());
	}

	/**
	 * Obtains a service compatible with the given class, instantiating it (and
	 * registering it in the index) if necessary.
	 * 
	 * @return an existing compatible service if one is registered, or else the
	 *         newly created service, or null if none can be instantiated
	 * @throws IllegalArgumentException if no suitable service class is found
	 */
	public <S extends Service> S loadService(final Class<S> c) {
		// if a compatible service already exists, return it
		final S service = getContext().getServiceIndex().getService(c);
		if (service != null) return service;

		// scan the class pool for a suitable match
		for (final Class<? extends Service> serviceClass : classPool) {
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
	public <S extends Service> S createExactService(final Class<S> c) {
		final LogService log = getContext().getService(LogService.class);
		if (log != null) log.debug("Creating service: " + c.getName());
		try {
			final Constructor<S> ctor = getConstructor(c);
			final S service = createService(ctor);
			getContext().getServiceIndex().add(service);
			if (log != null) log.info("Created service: " + c.getName());
			return service;
		}
		catch (final Exception e) {
			if (log != null) log.error("Invalid service: " + c.getName(), e);
		}
		return null;
	}

	// -- Utility methods --

	/** Gets the annotated priority of the given {@link Service} class. */
	public static double
		getPriority(final Class<? extends Service> serviceClass)
	{
		final Plugin ann = serviceClass.getAnnotation(Plugin.class);
		if (ann == null) return Priority.NORMAL_PRIORITY;
		return ann.priority();
	}

	// -- Helper methods --

	/** Instantiates a service using the given constructor. */
	private <S extends Service> S createService(final Constructor<S> ctor)
		throws InstantiationException, IllegalAccessException,
		InvocationTargetException
	{
		final Class<?>[] types = ctor.getParameterTypes();
		final Object[] args = new Object[types.length];
		for (int i = 0; i < types.length; i++) {
			final Class<?> type = types[i];
			if (Service.class.isAssignableFrom(type)) {
				@SuppressWarnings("unchecked")
				final Class<Service> c = (Class<Service>) type;
				args[i] = getContext().getServiceIndex().getService(c);
				if (args[i] == null) {
					// recursively obtain needed services
					args[i] = loadService(c);
				}
			}
			else if (ImageJ.class.isAssignableFrom(type)) {
				args[i] = getContext();
			}
			else throw new IllegalArgumentException("Invalid constructor: " + ctor);
		}
		return ctor.newInstance(args);
	}

	/**
	 * Gets a compatible constructor for creating a service of the given type. A
	 * constructor is compatible if all its arguments are assignable to
	 * {@link ImageJ} and {@link Service}. The method uses a greedy approach to
	 * choosing the best constructor, preferring constructors with a larger number
	 * of arguments, to populate the maximum number of services.
	 * 
	 * @return the best constructor to use for instantiating the service
	 * @throws IllegalArgumentException if no compatible constructors exist
	 */
	private <S extends Service> Constructor<S> getConstructor(
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
					!Service.class.isAssignableFrom(type))
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

	/** Asks the plugin index for all available service implementations. */
	private ArrayList<Class<? extends Service>> findServiceClasses() {
		final ArrayList<Class<? extends Service>> serviceList =
			new ArrayList<Class<? extends Service>>();

		// ask the plugin index for the (sorted) list of available services
		final List<PluginInfo<? extends Service>> services =
			getContext().getPluginIndex().getPlugins(Service.class);

		for (final PluginInfo<? extends Service> info : services) {
			try {
				final Class<? extends Service> c = info.loadClass();
				serviceList.add(c);
			}
			catch (final InstantiableException e) {
				final LogService log = getContext().getService(LogService.class);
				if (log != null) {
					log.error("Invalid service: " + info.getClassName(), e);
				}
			}
		}

		return serviceList;
	}

}
