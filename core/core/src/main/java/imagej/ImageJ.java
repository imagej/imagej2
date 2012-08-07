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

package imagej;

import imagej.event.ImageJEvent;
import imagej.ext.plugin.PluginIndex;
import imagej.service.IService;
import imagej.service.ServiceHelper;
import imagej.service.ServiceIndex;
import imagej.util.CheckSezpoz;
import imagej.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Top-level application context for ImageJ, which initializes and maintains a
 * list of services.
 * 
 * @author Curtis Rueden
 * @see IService
 */
public class ImageJ {

	/** Version of the ImageJ software. */
	public static final String VERSION = "2.0.0-beta4-DEV";

	/** Creates a new ImageJ application context with all available services. */
	public static ImageJ createContext() {
		try {
			if (!CheckSezpoz.check(false)) {
				// SezPoz uses ClassLoader.getResources() which will now pick up the apt-generated annotations.
				Log.info("SezPoz generated annotations.");
			}
		}
		catch (final IOException e) {
			Log.error(e);
		}
		return createContext((List<Class<? extends IService>>) null);
	}

	/** Creates a new ImageJ application context with no services. */
	public static ImageJ createEmptyContext() {
		return createContext(new ArrayList<Class<? extends IService>>());
	}

	/**
	 * Creates a new ImageJ application context with the specified service (and
	 * any required service dependencies).
	 */
	public static ImageJ createContext(
		final Class<? extends IService> serviceClass)
	{
		// NB: Although the createContext(Class<? extends IService>...) method
		// covers a superset of this case, it results in a warning in client code.
		// Needing a single service (e.g., for unit testing) is common enough to
		// warrant this extra method to avoid the problem for this special case.
		final List<Class<? extends IService>> serviceClassList =
			new ArrayList<Class<? extends IService>>();
		serviceClassList.add(serviceClass);
		return createContext(serviceClassList);
	}

	/**
	 * Creates a new ImageJ application context with the specified services (and
	 * any required service dependencies).
	 */
	public static ImageJ createContext(
		final Class<? extends IService>... serviceClasses)
	{
		final List<Class<? extends IService>> serviceClassList;
		if (serviceClasses == null || serviceClasses.length == 0) {
			serviceClassList = null;
		}
		else {
			serviceClassList = Arrays.asList(serviceClasses);
		}
		return createContext(serviceClassList);
	}

	// TODO - remove this!
	private static ImageJ staticContext;

	/**
	 * Creates a new ImageJ application context with the specified services (and
	 * any required service dependencies).
	 */
	public static ImageJ createContext(
		final Collection<Class<? extends IService>> serviceClasses)
	{
		final ImageJ context = new ImageJ();
		staticContext = context; // TEMP
		final ServiceHelper serviceHelper =
			new ServiceHelper(context, serviceClasses);
		serviceHelper.loadServices();
		return context;
	}

	/**
	 * Gets the static ImageJ application context.
	 * 
	 * @deprecated Avoid using this method. If you are writing a plugin, you can
	 *             declare the {@link ImageJ} or {@link IService} you want as a
	 *             parameter, with required=true and persist=false. If you are
	 *             writing a tool, you can obtain the {@link ImageJ} context by
	 *             calling {@link ImageJEvent#getContext()}, and then asking that
	 *             context for needed {@link IService} instances by calling
	 *             {@link ImageJ#getService(Class)}. See the classes in
	 *             core/plugins and core/tools for many examples.
	 */
	@Deprecated
	public static ImageJ getContext() {
		return staticContext;
	}

	/**
	 * Gets the service of the given class for the current ImageJ application
	 * context.
	 * 
	 * @deprecated Avoid using this method. If you are writing a plugin, you can
	 *             annotate the {@link ImageJ} or {@link IService} you want as a
	 *             parameter, with required=true and persist=false. If you are
	 *             writing a tool, you can obtain the {@link ImageJ} context by
	 *             calling {@link ImageJEvent#getContext()}, and then asking that
	 *             context for needed {@link IService} instances by calling
	 *             {@link ImageJ#getService(Class)}. See the classes in
	 *             core/plugins and core/tools for many examples.
	 */
	@Deprecated
	public static <S extends IService> S get(final Class<S> serviceClass) {
		final ImageJ context = getContext();
		if (context == null) return null; // no context
		return context.getService(serviceClass);
	}

	// -- Fields --

	private final ServiceIndex serviceIndex;

	private final PluginIndex pluginIndex;

	/** Creates a new ImageJ context. */
	public ImageJ() {
		serviceIndex = new ServiceIndex();

		pluginIndex = new PluginIndex();
		pluginIndex.discover();
	}

	// -- ImageJ methods --

	public ServiceIndex getServiceIndex() {
		return serviceIndex;
	}

	public PluginIndex getPluginIndex() {
		return pluginIndex;
	}

	/** Gets the service of the given class. */
	public <S extends IService> S getService(final Class<S> c) {
		return serviceIndex.getService(c);
	}

}
