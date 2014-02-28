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

import java.awt.GraphicsEnvironment;
import java.lang.reflect.Method;

/**
 * Encapsulates an ImageJ 1.x "instance".
 * <p>
 * This class is a partner to the {@link LegacyClassLoader}, intended to make
 * sure that the ImageJ 1.x contained in a given class loader is patched and can
 * be accessed conveniently.
 * </p>
 * 
 * @author "Johannes Schindelin"
 */
public class LegacyEnvironment {

	final private ClassLoader loader;
	final private Method setOptions, run, runMacro, runPlugIn, main;

	/**
	 * Constructs a new legacy environment.
	 * 
	 * @param loader the {@link ClassLoader} to use for loading the (patched)
	 *          ImageJ 1.x classes; if {@code null}, a {@link LegacyClassLoader}
	 *          is constructed.
	 * @param headless whether to patch in support for headless operation
	 *          (compatible only with "well-behaved" plugins, i.e. plugins that do
	 *          not use graphical components directly)
	 * @throws ClassNotFoundException
	 */
	public LegacyEnvironment(final ClassLoader loader, boolean headless)
		throws ClassNotFoundException
	{
		if (loader != null) {
			new LegacyInjector().injectHooks(loader, headless);
		}
		this.loader = loader != null ? loader : new LegacyClassLoader(headless);
		final Class<?> ij = this.loader.loadClass("ij.IJ");
		final Class<?> imagej = this.loader.loadClass("ij.ImageJ");
		final Class<?> macro = this.loader.loadClass("ij.Macro");
		try {
			setOptions = macro.getMethod("setOptions", String.class);
			run = ij.getMethod("run", String.class, String.class);
			runMacro = ij.getMethod("runMacro", String.class, String.class);
			runPlugIn = ij.getMethod("runPlugIn", String.class, String.class);
			main = imagej.getMethod("main", String[].class);
		}
		catch (Exception e) {
			throw new ClassNotFoundException("Found incompatible ij.IJ class", e);
		}
		// TODO: if we want to allow calling IJ#run(ImagePlus, String, String), we
		// will need a data translator
	}

	/**
	 * Sets the macro options.
	 * <p>
	 * Both {@link #run(String, String)} and {@link #runMacro(String, String)}
	 * take an argument that is typically recorded by the macro recorder. For
	 * {@link #runPlugIn(String, String)}, however, only the {@code arg} parameter
	 * that is to be passed to the plugins {@code run()} or {@code setup()} method
	 * can be specified. For those use cases where one wants to call a plugin
	 * class directly, but still provide macro options, this method is the
	 * solution.
	 * </p>
	 * 
	 * @param options the macro options to use for the next call to
	 *          {@link #runPlugIn(String, String)}
	 */
	public void setMacroOptions(final String options) {
		try {
			setOptions.invoke(null, options);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Runs {@code IJ.run(command, options)} in the legacy environment.
	 * 
	 * @param command the command to run
	 * @param options the options to pass to the command
	 */
	public void run(final String command, final String options) {
		final Thread thread = Thread.currentThread();
		final ClassLoader savedLoader = thread.getContextClassLoader();
		thread.setContextClassLoader(loader);
		try {
			run.invoke(null, command, options);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			thread.setContextClassLoader(savedLoader);
		}
	}

	/**
	 * Runs {@code IJ.runMacro(macro, arg)} in the legacy environment.
	 * 
	 * @param macro the macro code to run
	 * @param arg an optional argument (which can be retrieved in the macro code
	 *          via {@code getArgument()})
	 */
	public void runMacro(final String macro, final String arg) {
		final Thread thread = Thread.currentThread();
		final String savedName = thread.getName();
		thread.setName("Run$_" + savedName);
		final ClassLoader savedLoader = thread.getContextClassLoader();
		thread.setContextClassLoader(loader);
		try {
			runMacro.invoke(null, macro, arg);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			thread.setName(savedName);
			thread.setContextClassLoader(savedLoader);
		}
	}

	/**
	 * Runs {@code IJ.runPlugIn(className, arg)} in the legacy environment.
	 * 
	 * @param className the plugin class to run
	 * @param arg an optional argument (which get passed to the {@code run()} or
	 *          {@code setup()} method of the plugin)
	 */
	public Object runPlugIn(final String className, final String arg) {
		final Thread thread = Thread.currentThread();
		final String savedName = thread.getName();
		thread.setName("Run$_" + savedName);
		final ClassLoader savedLoader = thread.getContextClassLoader();
		thread.setContextClassLoader(loader);
		try {
			return runPlugIn.invoke(null, className, arg);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			thread.setName(savedName);
			thread.setContextClassLoader(savedLoader);
		}
	}

	/**
	 * Runs {@code ImageJ.main(args)} in the legacy environment.
	 * 
	 * @param args the arguments to pass to the main() method
	 */
	public void main(final String... args) {
		Thread.currentThread().setContextClassLoader(loader);
		try {
			main.invoke(null, (Object) args);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Gets the class loader containing the ImageJ 1.x classes used in this legacy
	 * environment.
	 * 
	 * @return the class loader
	 */
	public ClassLoader getClassLoader() {
		return loader;
	}

	/**
	 * Launches a fully-patched, self-contained ImageJ 1.x.
	 * 
	 * @throws ClassNotFoundException
	 */
	public static LegacyEnvironment getPatchedImageJ1()
		throws ClassNotFoundException
	{
		boolean headless = GraphicsEnvironment.isHeadless();
		return new LegacyEnvironment(new LegacyClassLoader(headless), headless);
	}
}
