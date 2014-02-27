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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtNewConstructor;
import javassist.Modifier;

/**
 * A Java agent to help with legacy issues.
 * 
 * @author Johannes Schindelin
 */
public class JavaAgent implements ClassFileTransformer {

	private static Instrumentation instrumentation;
	private static JavaAgent agent;
	private static Transformer transformer;

	public static void stop() {
		if (instrumentation != null && agent != null) {
			instrumentation.removeTransformer(agent);
			agent = null;
		}
		transformer = null;
	}

	/**
	 * The premain method started at JVM startup.
	 * 
	 * When this class is specified as <i>Premain-Class</i> in the manifest and
	 * the JVM is started with the option
	 * <tt>-javaagent:/path/to/ij-legacy.jar</tt> then this method is called
	 * some time before the <i>main</i> method of the main class is called.
	 * 
	 * @param agentArgs the optional argument passed  via <tt>-javaagent:ij-legacy.jar=ARGUMENT</tt>
	 * @param instrumentation the {@link Instrumentation} instance passed by the JVM
	 */
	public static void premain(final String agentArgs, final Instrumentation instrumentation) {
		JavaAgent.instrumentation = instrumentation;
		if ("help".equals(agentArgs)) {
			usage();
			return;
		} else if ("init".equals(agentArgs) || "preinit".equals(agentArgs) || "pre-init".equals(agentArgs)) {
			preinit();
			return;
		} else if ("noop".equals(agentArgs)) {
			return;
		} else if ("debug".equals(agentArgs)) {
			transformer = new Transformer();
		} else if (agentArgs != null && !"report".equals(agentArgs)) {
			System.err.println("Unhandled agent option: " + agentArgs);
			usage();
			return;
		}
		agent = new JavaAgent();
		instrumentation.addTransformer(agent);
		System.err.println("Legacy Java agent initialized");
	}

	@Override
	public byte[] transform(ClassLoader loader, String className,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {
		if (className.startsWith("ij.") || className.startsWith("ij/")) {
			return reportCaller("Loading " + className + " into " + loader + "!", classfileBuffer);
		}
		return null;
	}

	private static void usage() {
		System.err.println("ImageJ Legacy Agent Usage:\n" +
				"\n" +
				"The ij-legacy agent's purpose is to help with issues arising from ImageJ 1.x\n" +
				"classes being used before the ImageJ2 legacy service's pre-initialization stage\n" +
				"had a chance to run.\n" +
				"\n" +
				"Pre-initialization of the ImageJ2 legacy service is necessary in order to add\n" +
				"the extension points to ImageJ 1.x' classes that are required for the ImageJ2\n" +
				"legacy service to synchronize between the internal states of the legacy and\n" +
				"modern mode, respectively.\n" +
				"\n" +
				"Use the agent via the JVM option -javaagent=/path/to/ij-legacy.jar[=<OPTION>].\n" +
				"The following optional options are available:\n\n" +
				"help\n" +
				"\tshow this description\n" +
				"init\n" +
				"\tforce pre-initialization of the ImageJ legacy service\n" +
				"debug (this is the default)\n" +
				"\tthrow exceptions where ImageJ 1.x classes are used prematurely\n" +
				"report\n" +
				"\treport whenever ImageJ 1.x classes are used prematurely\n");
	}

	private static void preinit() {
		LegacyInjector.preinit();
	}

	private static class ImageJ1ClassLoadedPrematurely extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public ImageJ1ClassLoadedPrematurely(final String message) {
			super(message);
		}
	}

	private static List<RuntimeException> exceptions = new ArrayList<RuntimeException>();

	public static void dontCall(int i) {
		throw exceptions.get(i);
	}

	private static byte[] reportCaller(final String message, final byte[] classfileBuffer) throws IllegalClassFormatException {
		final RuntimeException exception = new ImageJ1ClassLoadedPrematurely(message);
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		if (trace != null) {
			int i = 0;
			// skip Thread#getStackTrace, #reportCaller and #transform
			while (i < trace.length && isCoreClass(trace[i].getClassName())) i++;
			final StackTraceElement[] stackTrace = new StackTraceElement[trace.length - i];
			for (int j = 0; i < trace.length; j++, i++) {
				stackTrace[j] = trace[i];
			}
			exception.setStackTrace(stackTrace);
		}

		/*
		 * We cannot really throw any exception here. That is, we can, but to no
		 * avail: exceptions thrown in ClassFileTransformer#transform() are
		 * simply ignored!
		 * 
		 * So we do something much nastier: we rewrite the class using Javassist here.
		 */
		if (transformer != null) {
			return transformer.transform(classfileBuffer, exception);
		}
		exception.printStackTrace();
		return null;
	}

	private static class Transformer {
		private final ClassPool pool;

		public Transformer() {
			pool = ClassPool.getDefault();
		}

		public byte[] transform(byte[] classfileBuffer, RuntimeException exception) {
			int number;
			synchronized (exceptions) {
				number = exceptions.size();
				exceptions.add(exception);
			}
			final String src = JavaAgent.class.getName() + ".dontCall(" + number + ");";
			try {
				CtClass clazz = pool.makeClass(new ByteArrayInputStream(classfileBuffer), false);
				CtConstructor method = clazz.getClassInitializer();
				if (method != null) {
					method.insertBefore(src);
				} else {
					method = CtNewConstructor.make(new CtClass[0], new CtClass[0], src, clazz);
					method.getMethodInfo().setName("<clinit>");
					method.setModifiers(Modifier.STATIC);
					clazz.addConstructor(method);
				}
				return clazz.toBytecode();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} catch (CannotCompileException e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	private final static ClassLoader bootstrapClassLoader = ClassLoader.getSystemClassLoader().getParent();

	private static boolean isCoreClass(final String className) {
		if (JavaAgent.class.getName().equals(className)) return true;
		try {
			bootstrapClassLoader.loadClass(className);
			return true;
		} catch (Throwable t) {
			return false;
		}
	}

	/**
	 * Writes out a minimal java agent .jar file.
	 * 
	 * <p>
	 * When trying to debug class loading issues in ij-legacy itself, of course
	 * we cannot load the complete ij-legacy.jar as a Java Agent (otherwise it
	 * would hide the classes that we want to test with possibly out-dated
	 * ones).
	 * </p>
	 * 
	 * <p>
	 * This main method can be used to generate a minimal .jar file for use with
	 * <tt>-javaagent</tt> that contains only the LegacyJavaAgent class.
	 * </p>
	 * 
	 * @param args
	 *            the command-line arguments (the first one, if specified,
	 *            refers to the output file name)
	 * @throws IOException
	 */
	public static void main(final String... args) throws IOException {
		boolean overwrite = false;
		final File file;
		int i = 0;
		if ("-f".equals(args[i]) || "--force".equals(args[i])) {
			overwrite = true;
			i++;
		}
		if (args == null || args.length <= i) {
			file = File.createTempFile("ij-legacy-javaagent-", ".jar");
		} else {
			file = new File(args[i]);
			if (!overwrite && file.exists()) {
				System.err.println("File exists: " + file);
				System.exit(1);
			}
		}

		final String classUrl = Utils.getLocation(JavaAgent.class).toExternalForm();
		final int slash = classUrl.lastIndexOf('/');
		final URL directory = new URL(classUrl.substring(0,  slash + 1));
		final Collection<URL> urls = Utils.listContents(directory);
		final String infix = JavaAgent.class.getName().replace('.', '/');

		final byte[] buffer = new byte[65536];
		final Manifest manifest = new Manifest();
		final Attributes mainAttrs = manifest.getMainAttributes();
		mainAttrs.put(Attributes.Name.MANIFEST_VERSION, "1.0");
		mainAttrs.putValue("Premain-Class", JavaAgent.class.getName());
		final JarOutputStream jar = new JarOutputStream(new FileOutputStream(file), manifest);

		for (final URL url2 : urls) {
			final String path = url2.getPath();
			final int offset = path.indexOf(infix);
			if (offset < 0) continue;
			jar.putNextEntry(new JarEntry(path.substring(offset)));
			final InputStream in = url2.openStream();
			for (;;) {
				int count = in.read(buffer);
				if (count < 0) break;
				jar.write(buffer, 0, count);
			}
			in.close();
			jar.closeEntry();
		}
		jar.close();
		System.err.println("Wrote " + file);
	}
}
