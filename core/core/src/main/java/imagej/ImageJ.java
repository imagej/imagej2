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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Top-level application context for ImageJ, which initializes and maintains a
 * list of services.
 * 
 * @author Curtis Rueden
 * @see IService
 */
public class ImageJ {

	/** Version of the ImageJ software. */
	public static final String VERSION = "2.0.0-alpha3";

	/** Table of ImageJ application contexts. */
	private static Map<Integer, ImageJ> contexts = Collections
		.synchronizedMap(new WeakHashMap<Integer, ImageJ>());

	/** The next available application context ID. */
	private static int nextID = 0;

	/** Creates a new ImageJ application context with all available services. */
	public static ImageJ createContext() {
		return createContext((List<Class<? extends IService>>) null);
	}

	/** Creates a new ImageJ application context with the specified services. */
	public static ImageJ createContext(
		final Class<? extends IService>... serviceClasses)
	{
		final List<Class<? extends IService>> serviceList;
		if (serviceClasses == null || serviceClasses.length == 0) {
			serviceList = null;
		}
		else {
			serviceList = Arrays.asList(serviceClasses);
		}
		return createContext(serviceList);
	}

	/** Creates a new ImageJ application context with the specified services. */
	public static ImageJ createContext(
		final Collection<Class<? extends IService>> serviceClasses)
	{
		final InitContextThread t =
			new InitContextThread(nextID++, serviceClasses);
		t.start();
		try {
			t.join();
		}
		catch (final InterruptedException e) {
			Log.error("Error creating application context", e);
			return null;
		}
		return t.getContext();
	}

	/**
	 * Disposes of the ImageJ application context with the given ID.
	 * 
	 * @param id The ID of the ImageJ application context to delete.
	 * @return true if the context was successfully deleted, or false if no such
	 *         context exists.
	 */
	public static boolean disposeContext(final int id) {
		final ImageJ context = contexts.remove(id);
		return context != null;
	}

	/** Gets the ImageJ application context for the current thread. */
	public static ImageJ getContext() {
		final int id = getContextID();
		if (id < 0) {
			throw new IllegalStateException("No application context from thread: " +
				Thread.currentThread().getName());
		}
		return getContext(id);
	}

	private static int getContextID() {
		final String name = Thread.currentThread().getName();
		final String prefix = "ImageJ-";
		if (!name.startsWith(prefix)) return -1;
		final int index = prefix.length();
		final int dash = name.indexOf('-', index);
		if (dash < 0) return -1;
		try {
			return Integer.parseInt(name.substring(index, dash));
		}
		catch (final NumberFormatException exc) {
			return -1;
		}
	}

	/** Gets the ImageJ application context with the given ID. */
	public static ImageJ getContext(final int id) {
		return contexts.get(id);
	}

	/**
	 * Gets the service of the given class for the current ImageJ application
	 * context.
	 */
	public static <S extends IService> S get(final Class<S> serviceClass) {
		return getContext().getService(serviceClass);
	}

	private final int id;
	private final ServiceHelper serviceHelper;

	/** Creates a new ImageJ context with the given ID and services. */
	protected ImageJ(final int id,
		final Collection<Class<? extends IService>> serviceClasses)
	{
		this.id = id;
		contexts.put(id, this);
		serviceHelper = new ServiceHelper(this);
		serviceHelper.loadServices(serviceClasses);
	}

	// -- ImageJ methods --

	/** Gets the ID code for this ImageJ application context. */
	public int getID() {
		return id;
	}

	/** Loads the service of the given class. */
	public <S extends IService> void loadService(final Class<S> c) {
		serviceHelper.loadService(c);
	}

	/** Loads the services of the given classes. */
	public void loadServices(
		final Collection<Class<? extends IService>> serviceClasses)
	{
		serviceHelper.loadServices(serviceClasses);
	}

	/** Gets the service of the given class. */
	public <S extends IService> S getService(final Class<S> c) {
		return serviceHelper.getService(c);
	}

	// -- Helper classes --

	private static class InitContextThread extends Thread {

		private final int id;
		private final Collection<Class<? extends IService>> serviceClasses;
		private ImageJ context;

		public InitContextThread(final int id,
			final Collection<Class<? extends IService>> serviceClasses)
		{
			super("ImageJ-" + id + "-Initialization");
			this.id = id;
			this.serviceClasses = serviceClasses;
		}

		@Override
		public void run() {
			context = new ImageJ(id, serviceClasses);
		}

		public ImageJ getContext() {
			return context;
		}
	}

}
