/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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
 * #L%
 */

package imagej.patcher;

import ij.IJ;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The base {@link LegacyHooks} to be used in the patched ImageJ 1.x.
 * <p>
 * This is the minimal implementation of {@link LegacyHooks} and will be
 * installed by default after patching in the extension points into ImageJ 1.x.
 * On its own, it does not allow to override the extension points (such as the
 * editor) with different implementations; one needs to install different hooks using
 * the {@link imagej.legacy.CodeHacker#installHooks(LegacyHooks)} method.
 * </p>
 * </p>
 * This class is also the perfect base class for all implementations of the
 * {@link LegacyHooks} interface, e.g. to offer "real" extension mechanisms such
 * as the SciJava-common plugin framework.
 * </p>
 * 
 * @author Johannes Schindelin
 */
public class EssentialLegacyHooks implements LegacyHooks {

	/** @inherit */
	@Override
	public boolean isLegacyMode() {
		return true;
	}

	/** @inherit */
	@Override
	public Object getContext() {
		return null;
	}

	/** @inherit */
	@Override
	public boolean quit() {
		return true;
	}

	/** @inherit */
	@Override
	public void installed() {
		// ignore
	}

	/** @inherit */
	@Override
	public void dispose() {
		// ignore
	}

	/** @inherit */
	@Override
	public Object interceptRunPlugIn(String className, String arg) {
		return null;
	}

	/** @inherit */
	@Override
	public void showProgress(double progress) {
	}

	/** @inherit */
	@Override
	public void showProgress(int currentIndex, int finalIndex) {
	}

	/** @inherit */
	@Override
	public void showStatus(String status) {
	}

	/** @inherit */
	@Override
	public void log(String message) {
	}

	/** @inherit */
	@Override
	public void registerImage(final Object image) {
	}

	/** @inherit */
	@Override
	public void unregisterImage(final Object image) {
	}

	/** @inherit */
	@Override
	public void debug(String string) {
		System.err.println(string);
	}

	/** @inherit */
	@Override
	public void error(Throwable t) {
		IJ.handleException(t);
	}

	/** @inherit */
	@Override
	public String getAppName() {
		return "ImageJ";
	}

	/** @inherit */
	@Override
	public URL getIconURL() {
		return null;
	}

	/** @inherit */
	@Override
	public boolean openInEditor(String path) {
		return false;
	}

	/** @inherit */
	@Override
	public boolean createInEditor(String fileName, String content) {
		return false;
	}

	/** @inherit */
	@Override
	public void runAfterRefreshMenus() {
		// ignore
	}

	/** @inherit */
	@Override
	public boolean handleNoSuchMethodError(NoSuchMethodError error) {
		String message = error.getMessage();
		int paren = message.indexOf("(");
		if (paren < 0) return false;
		int dot = message.lastIndexOf(".", paren);
		if (dot < 0) return false;
		String path = message.substring(0, dot).replace('.', '/') + ".class";
		Set<String> urls = new LinkedHashSet<String>();
		try {
			Enumeration<URL> e = IJ.getClassLoader().getResources(path);
			while (e.hasMoreElements()) {
				urls.add(e.nextElement().toString());
			}
			e = IJ.getClassLoader().getResources("/" + path);
			while (e.hasMoreElements()) {
				urls.add(e.nextElement().toString());
			}
		} catch (Throwable t) {
			t.printStackTrace();
			return false;
		}

		if (urls.size() == 0) return false;
		StringBuilder buffer = new StringBuilder();
		buffer.append("There was a problem with the class ");
		buffer.append(message.substring(0, dot));
		buffer.append(" which can be found here:\n");
		for (String url : urls) {
			if (url.startsWith("jar:")) url = url.substring(4);
			if (url.startsWith("file:")) url = url.substring(5);
			int bang = url.indexOf("!");
			if (bang < 0) buffer.append(url);
			else buffer.append(url.substring(0, bang));
			buffer.append("\n");
		}
		if (urls.size() > 1) {
			buffer.append("\nWARNING: multiple locations found!\n");
		}

		StringWriter writer = new StringWriter();
		error.printStackTrace(new PrintWriter(writer));
		buffer.append(writer.toString());

		IJ.log(buffer.toString());
		IJ.error("Could not find method " + message + "\n(See Log for details)\n");
		return true;
	}

	/** @inherit */
	@Override
	public List<File> handleExtraPluginJars() {
		final List<File> result = new ArrayList<File>();
		final String extraPluginDirs = System.getProperty("ij1.plugin.dirs");
		if (extraPluginDirs != null) {
			for (final String dir : extraPluginDirs.split(File.pathSeparator)) {
				handleExtraPluginJars(new File(dir), result);
			}
			return result;
		}
		final String userHome = System.getProperty("user.home");
		if (userHome != null) handleExtraPluginJars(new File(userHome, ".plugins"), result);
		return result;
	}

	private void handleExtraPluginJars(final File directory, final List<File> result) {
		final File[] list = directory.listFiles();
		if (list == null) return;
		for (final File file : list) {
			if (file.isDirectory()) handleExtraPluginJars(file, result);
			else if (file.isFile() && file.getName().endsWith(".jar")) {
				result.add(file);
			}
		}
	}

	/** @inherit */
	@Override
	public void newPluginClassLoader(final ClassLoader loader) {
		// do nothing
	}

	/** @inherit */
	@Override
	public void initialized() {
		final String property = System.getProperty("ij1.patcher.initializer");
		try {
			final Class<?> runClass = IJ.getClassLoader().loadClass(property != null ? property
					: "imagej.legacy.plugin.LegacyInitializer");
			final Runnable run = (Runnable)runClass.newInstance();
			run.run();
		} catch (ClassNotFoundException e) {
			// ignore
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
