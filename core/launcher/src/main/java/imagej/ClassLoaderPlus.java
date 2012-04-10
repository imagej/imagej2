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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * A classloader whose classpath can be augmented after instantiation.
 * 
 * @author Johannes Schindelin
 */
public class ClassLoaderPlus extends URLClassLoader {

	// A frozen ClassLoaderPlus will add only to the urls array
	protected boolean frozen;
	protected List<URL> urls = new ArrayList<URL>();

	public static ClassLoaderPlus getInImageJDirectory(
		final String... relativePaths)
	{
		try {
			final File directory = new File(getImageJDir());
			final URL[] urls = new URL[relativePaths.length];
			for (int i = 0; i < urls.length; i++) {
				urls[i] = new File(directory, relativePaths[i]).toURI().toURL();
			}
			return get(urls);
		}
		catch (final Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Uh oh: " + e.getMessage());
		}
	}

	public static ClassLoaderPlus get(final File... files) {
		try {
			final URL[] urls = new URL[files.length];
			for (int i = 0; i < urls.length; i++) {
				urls[i] = files[i].toURI().toURL();
			}
			return get(urls);
		}
		catch (final Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Uh oh: " + e.getMessage());
		}
	}

	public static ClassLoaderPlus get(final URL... urls) {
		final ClassLoader classLoader =
			Thread.currentThread().getContextClassLoader();
		if (classLoader instanceof ClassLoaderPlus) {
			final ClassLoaderPlus classLoaderPlus = (ClassLoaderPlus) classLoader;
			for (final URL url : urls) {
				classLoaderPlus.add(url);
			}
			return classLoaderPlus;
		}
		return new ClassLoaderPlus(urls);
	}

	public static ClassLoaderPlus getRecursivelyInImageJDirectory(
		final String... relativePaths)
	{
		return getRecursivelyInImageJDirectory(false, relativePaths);
	}

	public static ClassLoaderPlus getRecursivelyInImageJDirectory(
		final boolean onlyJars, final String... relativePaths)
	{
		try {
			final File directory = new File(getImageJDir());
			ClassLoaderPlus classLoader = null;
			final File[] files = new File[relativePaths.length];
			for (int i = 0; i < files.length; i++)
				classLoader =
					getRecursively(onlyJars, new File(directory, relativePaths[i]));
			return classLoader;
		}
		catch (final Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Uh oh: " + e.getMessage());
		}
	}

	public static ClassLoaderPlus getRecursively(final File directory) {
		return getRecursively(false, directory);
	}

	public static ClassLoaderPlus getRecursively(final boolean onlyJars,
		final File directory)
	{
		try {
			ClassLoaderPlus classLoader = onlyJars ? null : get(directory);
			final File[] list = directory.listFiles();
			if (list != null) for (final File file : list)
				if (file.isDirectory()) classLoader = getRecursively(onlyJars, file);
				else if (file.getName().endsWith(".jar")) classLoader = get(file);
			return classLoader;
		}
		catch (final Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Uh oh: " + e.getMessage());
		}
	}

	public ClassLoaderPlus() {
		this(new URL[0]);
	}

	public ClassLoaderPlus(final URL... urls) {
		super(urls, Thread.currentThread().getContextClassLoader());
		Thread.currentThread().setContextClassLoader(this);
	}

	public void addInImageJDirectory(final String relativePath) {
		try {
			add(new File(getImageJDir(), relativePath));
		}
		catch (final Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Uh oh: " + e.getMessage());
		}
	}

	public void add(final String path) throws MalformedURLException {
		add(new File(path));
	}

	public void add(final File file) throws MalformedURLException {
		add(file.toURI().toURL());
	}

	public void add(final URL url) {
		urls.add(url);
		if (!frozen) addURL(url);
	}

	public void freeze() {
		frozen = true;
	}

	public String getClassPath() {
		final StringBuilder builder = new StringBuilder();
		String sep = "";
		for (final URL url : urls)
			if (url.getProtocol().equals("file")) {
				builder.append(sep).append(url.getPath());
				sep = File.pathSeparator;
			}
		return builder.toString();
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append(getClass().getName()).append("(");
		for (final URL url : getURLs()) {
			builder.append(" ").append(url.toString());
		}
		builder.append(" )");
		return builder.toString();
	}

	public static String getImageJDir() throws ClassNotFoundException {
		String path = System.getProperty("ij.dir");
		if (path != null) return path;
		final String prefix = "file:";
		final String suffix = "/jars/ij-launcher.jar!/fiji/ClassLoaderPlus.class";
		path =
			Class.forName("fiji.ClassLoaderPlus")
				.getResource("ClassLoaderPlus.class").getPath();
		if (path.startsWith(prefix)) path = path.substring(prefix.length());
		if (path.endsWith(suffix)) {
			path = path.substring(0, path.length() - suffix.length());
		}
		return path;
	}

	public String getJarPath(final String className) {
		try {
			final Class clazz = loadClass(className);
			String path =
				clazz.getResource("/" + className.replace('.', '/') + ".class")
					.getPath();
			if (path.startsWith("file:")) path = path.substring(5);
			final int bang = path.indexOf("!/");
			if (bang > 0) path = path.substring(0, bang);
			return path;
		}
		catch (final Throwable t) {
			t.printStackTrace();
			return null;
		}
	}

}
