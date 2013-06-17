/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

package imagej.legacy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.scijava.util.ClassUtils;
import org.scijava.util.FileUtils;

/**
 * A Java agent to help with legacy issues.
 * 
 * @author Johannes Schindelin
 */
public class LegacyJavaAgent implements ClassFileTransformer {

	private static Instrumentation instrumentation;
	private static LegacyJavaAgent agent;

	public static void stop() {
		if (instrumentation != null && agent != null) {
			instrumentation.removeTransformer(agent);
			agent = null;
		}
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
		LegacyJavaAgent.instrumentation = instrumentation;
		if ("help".equals(agentArgs)) {
			usage();
			return;
		} else if ("init".equals(agentArgs) || "preinit".equals(agentArgs) || "pre-init".equals(agentArgs)) {
			preinit();
			return;
		}
		System.err.println("The legacy agent was started with the argument: " + agentArgs);
		agent = new LegacyJavaAgent();
		instrumentation.addTransformer(agent);
	}

	@Override
	public byte[] transform(ClassLoader loader, String className,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {
		if (className.startsWith("ij.") || className.startsWith("ij/")) {
			reportCaller("Loading " + className + " into " + loader + "!");
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
				"\tforce pre-initialization of the ImageJ legacy service\n");
	}

	private static void preinit() {
		DefaultLegacyService.preinit();
	}

	private static void reportCaller(final String message) {
		System.err.println(message);
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		if (trace == null) return;
		int i = 0;
		// skip Thread#getStackTrace, #reportCaller and #transform
		while (i < trace.length && isCoreClass(trace[i].getClassName())) i++;
		for (; i < trace.length; i++) {
			final StackTraceElement element = trace[i];
			System.err.println("\tat " + element.getClassName() + "." + element.getMethodName()
					+ "(" + element.getFileName() + ":" + element.getLineNumber() + ")");
		}
	}

	private final static ClassLoader bootstrapClassLoader = ClassLoader.getSystemClassLoader().getParent();

	private static boolean isCoreClass(final String className) {
		if (LegacyJavaAgent.class.getName().equals(className)) return true;
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

		final String classUrl = ClassUtils.getLocation(LegacyJavaAgent.class).toExternalForm();
		final int slash = classUrl.lastIndexOf('/');
		final URL directory = new URL(classUrl.substring(0,  slash + 1));
		final Collection<URL> urls = FileUtils.listContents(directory);
		final String infix = LegacyJavaAgent.class.getName().replace('.', '/');

		final byte[] buffer = new byte[65536];
		final Manifest manifest = new Manifest();
		final Attributes mainAttrs = manifest.getMainAttributes();
		mainAttrs.put(Attributes.Name.MANIFEST_VERSION, "1.0");
		mainAttrs.putValue("Premain-Class", LegacyJavaAgent.class.getName());
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
