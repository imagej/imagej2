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
import imagej.plugin.PluginIndex;
import imagej.service.Service;
import imagej.service.ServiceHelper;
import imagej.service.ServiceIndex;
import imagej.util.CheckSezpoz;
import imagej.util.Manifest;
import imagej.util.POM;

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
 * @see Service
 */
public class ImageJ {

	/** @deprecated Use {@link ImageJ#getVersion()} instead. */
	@Deprecated
	public static final String VERSION =
		POM.getPOM(ImageJ.class, "net.imagej", "ij-core").getVersion();

	/** Creates a new ImageJ application context with all available services. */
	public static ImageJ createContext() {
		try {
			if (!CheckSezpoz.check(false)) {
				// SezPoz uses ClassLoader.getResources() which will now pick up the
				// apt-generated annotations.
				System.err.println("SezPoz generated annotations."); // no log service
			}
		}
		catch (final IOException e) {
			e.printStackTrace();
		}
		return createContext((List<Class<? extends Service>>) null);
	}

	/** Creates a new ImageJ application context with no services. */
	public static ImageJ createEmptyContext() {
		return createContext(new ArrayList<Class<? extends Service>>());
	}

	/**
	 * Creates a new ImageJ application context with the specified service (and
	 * any required service dependencies).
	 */
	public static ImageJ createContext(
		final Class<? extends Service> serviceClass)
	{
		// NB: Although the createContext(Class<? extends Service>...) method
		// covers a superset of this case, it results in a warning in client code.
		// Needing a single service (e.g., for unit testing) is common enough to
		// warrant this extra method to avoid the problem for this special case.
		final List<Class<? extends Service>> serviceClassList =
			new ArrayList<Class<? extends Service>>();
		serviceClassList.add(serviceClass);
		return createContext(serviceClassList);
	}

	/**
	 * Creates a new ImageJ application context with the specified services (and
	 * any required service dependencies).
	 */
	public static ImageJ createContext(
		final Class<? extends Service>... serviceClasses)
	{
		final List<Class<? extends Service>> serviceClassList;
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
		final Collection<Class<? extends Service>> serviceClasses)
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
	 * @deprecated Avoid using this method. If you are writing a command, you can
	 *             declare the {@link ImageJ} or {@link Service} you want as a
	 *             parameter. If you are writing a tool, you can obtain the
	 *             {@link ImageJ} context by calling
	 *             {@link ImageJEvent#getContext()}, and then asking that context
	 *             for needed {@link Service} instances by calling
	 *             {@link ImageJ#getService(Class)}. See the classes in
	 *             {@code core/commands} and {@code core/tools} for many examples.
	 */
	@Deprecated
	public static ImageJ getContext() {
		return staticContext;
	}

	/**
	 * Gets the service of the given class for the current ImageJ application
	 * context.
	 * 
	 * @deprecated Avoid using this method. If you are writing a command, you can
	 *             annotate the {@link ImageJ} or {@link Service} you want as a
	 *             parameter. If you are writing a tool, you can obtain the
	 *             {@link ImageJ} context by calling
	 *             {@link ImageJEvent#getContext()}, and then asking that context
	 *             for needed {@link Service} instances by calling
	 *             {@link ImageJ#getService(Class)}. See the classes in
	 *             {@code core/commands} and {@code core/tools} for many examples.
	 */
	@Deprecated
	public static <S extends Service> S get(final Class<S> serviceClass) {
		final ImageJ context = getContext();
		if (context == null) return null; // no context
		return context.getService(serviceClass);
	}

	// -- Fields --

	/** Title of the application context. */
	private String title = "ImageJ";

	/** Index of the application context's services. */
	private final ServiceIndex serviceIndex;

	/** Master index of all plugins known to the application context. */
	private final PluginIndex pluginIndex;

	/** Maven POM with metadata about ImageJ. */
	private final POM pom;

	/** JAR manifest with metadata about ImageJ. */
	private final Manifest manifest;

	/** Creates a new ImageJ context. */
	public ImageJ() {
		serviceIndex = new ServiceIndex();

		pluginIndex = new PluginIndex();
		pluginIndex.discover();

		pom = POM.getPOM(ImageJ.class, "net.imagej", "ij-core");
		manifest = Manifest.getManifest(ImageJ.class);
	}

	// -- ImageJ methods --

	/**
	 * Gets the title of the application context. The default value is "ImageJ"
	 * but it can be overridden by calling {@link #setTitle(String)}.
	 */
	public String getTitle() {
		return title;
	}

	/** Overrides the title of the application context. */
	public void setTitle(final String title) {
		this.title = title;
	}

	/**
	 * Gets the version of the application. ImageJ conforms to the <a
	 * href="http://semver.org/">Semantic Versioning</a> specification.
	 * 
	 * @return The application version, in {@code major.minor.micro} format.
	 */
	public String getVersion() {
		return pom.getVersion();
	}

	/** Gets the Maven POM containing metadata about the application context. */
	public POM getPOM() {
		return pom;
	}

	/**
	 * Gets the manifest containing metadata about the application context.
	 * <p>
	 * NB: This metadata may be null if run in a development environment.
	 * </p>
	 */
	public Manifest getManifest() {
		return manifest;
	}

	/**
	 * Gets a string with information about the application context.
	 * 
	 * @param mem If true, memory usage information is included.
	 */
	public String getInfo(final boolean mem) {
		final String appTitle = getTitle();
		final String appVersion = getVersion();
		final String javaVersion = System.getProperty("java.version");
		final String osArch = System.getProperty("os.arch");
		final long maxMem = Runtime.getRuntime().maxMemory();
		final long totalMem = Runtime.getRuntime().totalMemory();
		final long freeMem = Runtime.getRuntime().freeMemory();
		final long usedMem = totalMem - freeMem;
		final long usedMB = usedMem / 1048576;
		final long maxMB = maxMem / 1048576;
		final StringBuilder sb = new StringBuilder();
		sb.append(appTitle + " " + appVersion);
		sb.append("; Java " + javaVersion + " [" + osArch + "]");
		if (mem) sb.append("; " + usedMB + "MB of " + maxMB + "MB");
		return sb.toString();
	}

	public ServiceIndex getServiceIndex() {
		return serviceIndex;
	}

	public PluginIndex getPluginIndex() {
		return pluginIndex;
	}

	/** Gets the service of the given class. */
	public <S extends Service> S getService(final Class<S> c) {
		return serviceIndex.getService(c);
	}

	/** Gets the service of the given class name (useful for scripts). */
	@SuppressWarnings("unchecked")
	public <S extends Service> S getService(final String className) {
		try {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			return  getService((Class<S>)loader.loadClass(className));
		} catch (ClassNotFoundException exc) {
			return null;
		}
	}

	/**
	 * Injects the application context into the given object. Note that this is
	 * only possible if the given object implements the {@link Contextual}
	 * interface.
	 * 
	 * @param o The object to which the context should be assigned.
	 * @return true If the context was successfully injected.
	 * @throws IllegalStateException If the object already has a context.
	 */
	public boolean inject(final Object o) {
		if (!(o instanceof Contextual)) return false;
		((Contextual) o).setContext(this);
		return true;
	}

}
