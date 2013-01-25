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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

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

	/** @deprecated Use {@link #ImageJ()} instead. */
	@Deprecated
	public static ImageJ createContext() {
		return new ImageJ();
	}

	/** @deprecated Use {@code new ImageJ(true)} instead. */
	@Deprecated
	public static ImageJ createEmptyContext() {
		return new ImageJ(true);
	}

	/** @deprecated Use {@link #ImageJ(Class...)} instead. */
	@Deprecated
	public static ImageJ createContext(
		final Class<? extends Service> serviceClass)
	{
		return new ImageJ(serviceClass);
	}

	/** @deprecated Use {@link #ImageJ(Class...)} instead. */
	@Deprecated
	public static ImageJ createContext(
		final Class<? extends Service>... serviceClasses)
	{
		return new ImageJ(serviceClasses);
	}

	// TODO - remove this!
	private static ImageJ staticContext;

	/** @deprecated Use {@link #ImageJ(Collection)} instead. */
	@Deprecated
	public static ImageJ createContext(
		final Collection<Class<? extends Service>> serviceClasses)
	{
		return new ImageJ(serviceClasses);
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

	/** Creates a new ImageJ application context with all available services. */
	public ImageJ() {
		this(false);
	}

	/**
	 * Creates a new ImageJ application context.
	 * 
	 * @param empty If true, the context will be empty; otherwise, it will be
	 *          initialized with all available services.
	 */
	public ImageJ(final boolean empty) {
		this(empty ? Collections.<Class<? extends Service>> emptyList() : null);
	}

	/**
	 * Creates a new ImageJ application context with the specified services (and
	 * any required service dependencies).
	 * <p>
	 * <b>Developer's note:</b> This constructor's argument is raw (i.e.,
	 * {@code Class...} instead of {@code Class<? extends Service>...}) because
	 * otherwise, downstream invocations (e.g.,
	 * {@code new ImageJ(DisplayService.class)}) yield the potentially confusing
	 * warning:
	 * </p>
	 * <blockquote>Type safety: A generic array of Class<? extends Service> is
	 * created for a varargs parameter</blockquote>
	 * <p>
	 * To avoid this, we have opted to use raw types and suppress the relevant
	 * warnings here instead.
	 * </p>
	 * 
	 * @param serviceClasses A list of types that implement the {@link Service}
	 *          interface (e.g., {@code DisplayService.class}).
	 * @throws ClassCastException If any of the given arguments do not implement
	 *           the {@link Service} interface.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ImageJ(final Class... serviceClasses) {
		this(serviceClasses != null ? (Collection) Arrays.asList(serviceClasses)
			: null);
	}

	/**
	 * Creates a new ImageJ application context with the specified services (and
	 * any required service dependencies).
	 * 
	 * @param serviceClasses A collection of types that implement the
	 *          {@link Service} interface (e.g., {@code DisplayService.class}).
	 */
	public ImageJ(final Collection<Class<? extends Service>> serviceClasses) {
		if (staticContext == null) {
			// First context! Check that annotations were generated properly.
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
		}
		staticContext = this; // TEMP

		serviceIndex = new ServiceIndex();

		pluginIndex = new PluginIndex();
		pluginIndex.discover();

		pom = POM.getPOM(ImageJ.class, "net.imagej", "ij-core");
		manifest = Manifest.getManifest(ImageJ.class);

		final ServiceHelper serviceHelper =
			new ServiceHelper(this, serviceClasses);
		serviceHelper.loadServices();
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
	 * @return true If the context was successfully injected, or if the object
	 *         already has this context.
	 * @throws IllegalStateException If the object already has a different
	 *           context.
	 */
	public boolean inject(final Object o) {
		if (!(o instanceof Contextual)) return false;
		final Contextual c = (Contextual) o;
		if (c.getContext() == this) return true;
		c.setContext(this);
		return true;
	}

}
