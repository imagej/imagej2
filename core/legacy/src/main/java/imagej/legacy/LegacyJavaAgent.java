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
import java.io.UnsupportedEncodingException;
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
		} else if ("noop".equals(agentArgs)) {
			return;
		} else if (agentArgs != null && !"debug".equals(agentArgs)) {
			System.err.println("Unhandled agent option: " + agentArgs);
			usage();
			return;
		}
		agent = new LegacyJavaAgent();
		instrumentation.addTransformer(agent);
		System.err.println("Legacy Java agent initialized");
	}

	@Override
	public byte[] transform(ClassLoader loader, String className,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {
		if (className.startsWith("ij.") || className.startsWith("ij/")) {
			return reportCaller("Loading " + className + " into " + loader + "!", className);
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
				"\tshow where ImageJ 1.x classes are used prematurely\n");
	}

	private static void preinit() {
		DefaultLegacyService.preinit();
	}

	private static List<RuntimeException> exceptions = new ArrayList<RuntimeException>();

	public static void dontCall(int i) {
		throw exceptions.get(i);
	}

	private static byte[] reportCaller(final String message, final String className) throws IllegalClassFormatException {
		final RuntimeException exception = new RuntimeException(message);
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
		 * So we do something much nastier: we write our own Java class here.
		 */
		return new ClassWriter().makeClass(className, exception);
	}

	@SuppressWarnings("unused")
	private static void hexdump(byte[] bytes) {
		System.err.println("\nhexdump of " + bytes.length + " bytes:");
		for (int offset = 0; offset < bytes.length; offset += 0x10) {
			System.err.printf("%08x  ", offset);
			String suffix = " |";
			for (int i = 0; i < 0x10; i++) {
				if (offset + i < bytes.length) {
					int b = bytes[offset + i] & 0xff;
					System.err.printf("%02x ", b);
					suffix += (b >= 0x20 && b < 0x80 ? (char)b : '.');
				} else {
					System.err.print("   ");
					suffix += " ";
				}
				if (i == 7) System.err.print(" ");
			}
			System.err.println(suffix + "|");
		}
	}

	private static class ClassWriter {
		private final static byte RETURN = (byte)0xb1;
		private final static int CODE_REF = 7;

		public byte[] makeClass(final String className, final RuntimeException e) {
			int number;
			synchronized (exceptions) {
				number = exceptions.size();
				exceptions.add(e);
			}
			return concat(new byte[] {
				// magic: 0xcafebabe
				-54, -2, -70, -66,
				// class file format version (JDK 1.4 = 48.0)
				0, 0, 0, 48,
			},
			constantPool(
				// #1 super-class' constructor
				methodRef(4, 9),
				// #12 LegacyJavaAgent#dontCall(int)
				methodRef(10, 11),
				// #3 this.class
				classRef(12),
				// #4 Object.class
				classRef(13),
				// #5 "<init>"
				string("<init>"),
				// #6 "()V"
				string("()V"),
				// #7 "Code"
				string("Code"),
				// #8 "<clinit>"
				string("<clinit>"),
				// #9 constructor's signature
				nameAndType(5, 6),
				// #10 LegacyJavaAgent.class
				classRef(14),
				// #11 signature of LegacyJavaAgent#dontCall(int)
				nameAndType(15, 16),
				// #12 class name
				string(className),
				// #13 "java/lang/Object"
				string("java/lang/Object"),
				// #14 "imagej.legacy.LegacyJavaAgent"
				string(LegacyJavaAgent.class.getName().replace('.', '/')),
				// #15 "dontCall"
				string("dontCall"),
				// #16 "(I)V"
				string("(I)V")
			),
			// class access flags (PUBLIC | SUPER)
			(short)0x21,
			// this / super class
			(short)3, (short)4,
			// interfaces implemented
			(short)0,
			// field count
			(short)0,
			// method count
			(short)2,
			// constructor
			method(1 /* PUBLIC */, 5, 6, concat(aload(0), invokespecial(1), RETURN)),
			// static initializer
			method(8 /* STATIC */, 8, 6, concat(ipush(number), invokestatic(2), RETURN)),
			// class attribute count
			(short)0
			);
		}

		private byte[] constantPool(final byte[]... elements) {
			return concat((short)(elements.length + 1), concat((Object[])elements));
		}

		private static byte[] string(final String string) {
			try {
				final byte[] bytes = string.getBytes("UTF-8");
				return concat((byte)1, (short)bytes.length, bytes);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return null;
			}
		}

		private static byte[] classRef(final int stringRef) {
			return concat((byte)7, (short)stringRef);
		}

		private byte[] nameAndType(final int nameRef, final int typeRef) {
			return concat((byte)12, (short)nameRef, (short)typeRef);
		}

		private byte[] methodRef(final int classRef, final int nameAndTypeRef) {
			return concat((byte)10, (short)classRef, (short)nameAndTypeRef);
		}

		private byte[] method(final int accessFlags, final int nameRef, final int typeRef, final byte[] code) {
			return concat((short)accessFlags,
					(short)nameRef, (short)typeRef,
					// attribute count: 1 (Code)
					(short)1,
					(short)CODE_REF, // "Code"
					code.length + 12, // attribute length
					(short)1, (short)1, // max stack, max local
					code.length,
					code,
					(short)0, // code attribute's exception count
					(short)0 // code attribute's attribute count
				);
		}

		private byte[] aload(int i) {
			if (i == 0) return concat((byte)0x2a);
			throw new IllegalArgumentException("Not yet implemented: i = " + i);
		}

		private byte[] invokespecial(final int methodRef) {
			return concat((byte)0xb7, (short)methodRef);
		}

		private byte[] ipush(final int value) {
			if (value < 0x100) return concat((byte)0x10, (byte)value);
			throw new IllegalArgumentException("Not yet implemented: value = " + value);
		}

		private byte[] invokestatic(final int methodRef) {
			return concat((byte)0xb8, (short)methodRef);
		}

		private static byte[] concat(final Object... list) {
			int length = 0;
			for (final Object element : list) {
				if (element == null) continue;
				if (Byte.class.isInstance(element)) length++;
				else if (Short.class.isInstance(element)) length += 2;
				else if (Integer.class.isInstance(element)) length += 4;
				else if (element instanceof byte[]) {
					final byte[] array = (byte[])element;
					length += array.length;
				} else {
					System.err.println("Cannot handle element of type " + element.getClass());
				}
			}
			final byte[] result = new byte[length];
			int offset =  0;
			for (final Object element : list) {
				if (element == null) continue;
				if (Byte.class.isInstance(element)) {
					result[offset++] = ((Byte)element).byteValue();
				}
				else if (Short.class.isInstance(element)) {
					final short value = ((Short)element).shortValue();
					result[offset++] = (byte)((value & 0xff00) >> 8);
					result[offset++] = (byte)(value & 0xff);
				}
				else if (Integer.class.isInstance(element)) {
					final int value = ((Integer)element).intValue();
					result[offset++] = (byte)((value & 0xff000000l) >> 24);
					result[offset++] = (byte)((value & 0xff0000) >> 16);
					result[offset++] = (byte)((value & 0xff00) >> 8);
					result[offset++] = (byte)(value & 0xff);
				}
				else if (element instanceof byte[]) {
					final byte[] array = (byte[])element;
					length += array.length;
					System.arraycopy(array, 0, result, offset, array.length);
					offset += array.length;
				}
			}
			return result;
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
