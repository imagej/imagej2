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

package imagej.build.minimaven;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * TODO
 * 
 * @author Johannes Schindelin
 */
public class JarClassLoader extends ClassLoader {
	Map<String, JarFile> jarFilesMap;
	List<String> jarFilesNames;
	List<JarFile> jarFilesObjects;
	HashMap<String, Class<?>> cache;

	JarClassLoader() {
		super(Thread.currentThread().getContextClassLoader());
		jarFilesMap = new HashMap<String, JarFile>();
		jarFilesNames = new ArrayList<String>(10);
		jarFilesObjects = new ArrayList<JarFile>(10);
		cache = new HashMap<String, Class<?>>();
	}

	public JarClassLoader(String... paths) throws IOException {
		this();
		for (String path : paths)
			add(path);
	}

	public synchronized void add(String path) throws IOException {
		if (jarFilesMap.containsKey(path))
			return;
		JarFile jar = new JarFile(path);
		/* n.b. We don't need to synchronize
		   fetching since nothing is ever removed */
		jarFilesMap.put(path, jar);
		jarFilesNames.add(path);
		jarFilesObjects.add(jar);
	}

	@Override
	public URL getResource(String name) {
		int n = jarFilesNames.size();
		for (int i = n - 1; i >= 0; --i) {
			JarFile jar = jarFilesObjects.get(i);
			String file = jarFilesNames.get(i);
			if (jar.getEntry(name) == null)
				continue;
			String url = "file:///"
				+ file.replace('\\', '/')
				+ "!/" + name;
			try {
				return new URL("jar", "", url);
			} catch (MalformedURLException e) {
				// fall through
			}
		}
		return getSystemResource(name);
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		return getResourceAsStream(name, false);
	}

	public InputStream getResourceAsStream(String name,
			boolean nonSystemOnly) {
		int n = jarFilesNames.size();
		for (int i = n - 1; i >= 0; --i) {
			JarFile jar = jarFilesObjects.get(i);
			JarEntry entry = jar.getJarEntry(name);
			if (entry == null)
				continue;
			try {
				return jar.getInputStream(entry);
			} catch (IOException e) {
				// fall through
			}
		}
		if (nonSystemOnly)
			return null;
		return super.getResourceAsStream(name);
	}

	public Class<?> forceLoadClass(String name)
			throws ClassNotFoundException {
		return loadClass(name, true, true);
	}

	@Override
	public Class<?> loadClass(String name)
			throws ClassNotFoundException {
		return loadClass(name, true);
	}

	@Override
	public synchronized Class<?> loadClass(String name,
			boolean resolve) throws ClassNotFoundException {
		return loadClass(name, resolve, false);
	}

	public synchronized Class<?> loadClass(String name,
				boolean resolve, boolean forceReload)
			throws ClassNotFoundException {
		Class<?> cached = forceReload ? null : cache.get(name);
		if (cached != null)
			return cached;
		Class<?> result;
		try {
			if (!forceReload) {
				result = super.loadClass(name, resolve);
				if (result != null)
					return result;
			}
		} catch (Exception e) {
			// fall through
		}
		String path = name.replace('.', '/') + ".class";
		InputStream input = getResourceAsStream(path, !true);
		if (input == null)
			throw new ClassNotFoundException(name);
		try {
			byte[] buffer = readStream(input);
			input.close();
			result = defineClass(name,
					buffer, 0, buffer.length);
			if (result.getPackage() == null) {
				String packageName = name.substring(0, name.lastIndexOf('.'));
				definePackage(packageName, null, null, null, null, null, null, null);
			}
			cache.put(name, result);
			return result;
		} catch (IOException e) {
			result = forceReload ?
				super.loadClass(name, resolve) : null;
			return result;
		}
	}

	protected static byte[] readStream(InputStream in) throws IOException {
		byte[] buffer = new byte[16384];
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (;;) {
			int count = in.read(buffer);
			if (count < 0)
				break;
			out.write(buffer, 0, count);
		}
		in.close();
		out.close();
		return out.toByteArray();
	}
}
