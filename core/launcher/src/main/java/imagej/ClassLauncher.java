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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.Arrays;

public class ClassLauncher {
	protected static boolean debug = System.getenv("DEBUG_IJ_LAUNCHER") != null;
	protected static String[] originalArguments;

	/**
	 * Patch ij.jar and launch the class given as first argument passing on the remaining arguments
	 *
	 * @param arguments A list containing the name of the class whose main() method is to be called
	 *        with the remaining arguments.
	 *
	 * @author Johannes Schindelin
	 */
	public static void main(String[] arguments) {
		originalArguments = arguments;
		run(arguments);
	}

	public static void restart() {
		Thread.currentThread().setContextClassLoader(ClassLoader.class.getClassLoader());
		run(originalArguments);
	}

	protected static void run(String[] arguments) {
		boolean retrotranslator = false, jdb = false, passClasspath = false;
		ClassLoaderPlus classLoader = null;
		int i = 0;
		for (; i < arguments.length && arguments[i].charAt(0) == '-'; i++) {
			String option = arguments[i];
			if (option.equals("-cp") || option.equals("-classpath"))
				classLoader = ClassLoaderPlus.get(new File(arguments[++i]));
			else if (option.equals("-ijcp") || option.equals("-ijclasspath"))
				classLoader = ClassLoaderPlus.getInFijiDirectory(arguments[++i]);
			else if (option.equals("-jarpath"))
				classLoader = ClassLoaderPlus.getRecursively(true, new File(arguments[++i]));
			else if (option.equals("-ijjarpath"))
				classLoader = ClassLoaderPlus.getRecursivelyInFijiDirectory(true, arguments[++i]);
			else if (option.equals("-jdb"))
				jdb = true;
			else if (option.equals("-retrotranslator")) {
				classLoader = ClassLoaderPlus.getRecursivelyInFijiDirectory(true, "retro");
				retrotranslator = true;
			}
			else if (option.equals("-pass-classpath"))
				passClasspath = true;
			else if (option.equals("-freeze-classloader"))
				classLoader.freeze();
			else {
				System.err.println("Unknown option: " + option + "!");
				System.exit(1);
			}
		}

		if (i >= arguments.length) {
			System.err.println("Missing argument: main class");
			System.exit(1);
		}

		String mainClass = arguments[i];
		arguments = slice(arguments, i + 1);

		if (!"false".equals(System.getProperty("patch.ij1")) && !mainClass.equals("imagej.Main") && !mainClass.equals("fiji.build.MiniMaven")) {
			classLoader = ClassLoaderPlus.getInFijiDirectory("jars/fiji-compat.jar", "jars/ij.jar", "jars/javassist.jar");
			try {
				patchIJ1(classLoader);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (passClasspath && classLoader != null)
			arguments = prepend(arguments, "-classpath", classLoader.getClassPath());

		if (jdb) {
			arguments = prepend(arguments, mainClass);
			if (classLoader != null)
				arguments = prepend(arguments, "-classpath", classLoader.getClassPath());
			mainClass = "com.sun.tools.example.debug.tty.TTY";
		}

		if (retrotranslator) {
			arguments = prepend(arguments, "-advanced", mainClass);
			mainClass = "net.sf.retrotranslator.transformer.JITRetrotranslator";
		}

		if (debug)
			System.err.println("Launching main class " + mainClass + " with parameters " + Arrays.toString(arguments));

		launch(classLoader, mainClass, arguments);
	}

	protected static void patchIJ1(ClassLoader classLoader) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
		Class<Runnable> clazz = (Class<Runnable>)classLoader.loadClass("fiji.IJ1Patcher");
		Runnable ij1Patcher = clazz.newInstance();
		ij1Patcher.run();
	}

	protected static String[] slice(String[] array, int from) {
		return slice(array, from, array.length);
	}

	protected static String[] slice(String[] array, int from, int to) {
		String[] result = new String[to - from];
		if (result.length > 0)
			System.arraycopy(array, from, result, 0, result.length);
		return result;
	}

	protected static String[] prepend(String[] array, String... before) {
		if (before.length == 0)
			return array;
		String[] result = new String[before.length + array.length];
		System.arraycopy(before, 0, result, 0, before.length);
		System.arraycopy(array, 0, result, before.length, array.length);
		return result;
	}

	protected static void launch(ClassLoader classLoader, String className, String[] arguments) {
		Class main = null;
		if (classLoader == null)
			classLoader = Thread.currentThread().getContextClassLoader();
		try {
			main = classLoader.loadClass(className.replace('/', '.'));
		} catch (ClassNotFoundException e) {
			System.err.println("Class '" + className + "' was not found");
			System.exit(1);
		}
		Class[] argsType = new Class[] { arguments.getClass() };
		Method mainMethod = null;
		try {
			mainMethod = main.getMethod("main", argsType);
		} catch (NoSuchMethodException e) {
			System.err.println("Class '" + className + "' does not have a main() method.");
			System.exit(1);
		}
		Integer result = new Integer(1);
		try {
                        result = (Integer)mainMethod.invoke(null,
                                        new Object[] { arguments });
                } catch (IllegalAccessException e) {
                        System.err.println("The main() method of class '" + className + "' is not public.");
                } catch (InvocationTargetException e) {
                        System.err.println("Error while executing the main() " + "method of class '" + className + "':");
                        e.getTargetException().printStackTrace();
                }
		if (result != null)
			System.exit(result.intValue());
	}
}
