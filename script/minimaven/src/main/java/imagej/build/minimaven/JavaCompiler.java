package imagej.build.minimaven;
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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;

public class JavaCompiler {
	protected PrintStream err, out;
	protected static Method javac;

	public JavaCompiler(PrintStream err, PrintStream out) {
		this.err = err;
		this.out = out;
	}

	// this function handles the javac singleton
	public void call(String[] arguments,
			boolean verbose) throws CompileError {
		synchronized(this) {
			try {
				if (javac == null) {
					JarClassLoader loader = discoverJavac();
					String className = "com.sun.tools.javac.Main";
					Class<?> main = loader.forceLoadClass(className);
					Class<?>[] argsType = new Class[] {
						arguments.getClass(),
						PrintWriter.class
					};
					javac = main.getMethod("compile", argsType);
				}

				Object result = javac.invoke(null,
						new Object[] { arguments, new PrintWriter(err) });
				if (!result.equals(new Integer(0)))
					throw new CompileError(result);
				return;
			} catch (CompileError e) {
				/* re-throw */
				throw e;
			} catch (Exception e) {
				e.printStackTrace(err);
				err.println("Could not find javac " + e
					+ ", falling back to system javac");
			}
		}

		// fall back to calling javac
		String[] newArguments = new String[arguments.length + 1];
		newArguments[0] = "javac";
		System.arraycopy(arguments, 0, newArguments, 1,
				arguments.length);
		try {
			execute(newArguments, new File("."), verbose);
		} catch (Exception e) {
			throw new RuntimeException("Could not even fall back "
				+ " to javac in the PATH");
		}
	}

	public static class CompileError extends Exception {
		private static final long serialVersionUID = 1L;
		protected Object result;

		public CompileError(Object result) {
			super("Compile error: " + result);
			this.result = result;
		}

		public Object getResult() {
			return result;
		}
	}

	protected void execute(String[] args, File dir, boolean verbose)
			throws IOException {
		if (verbose) {
			String output = "Executing:";
			for (int i = 0; i < args.length; i++)
				output += " '" + args[i] + "'";
			err.println(output);
		}

		/* stupid, stupid Windows... */
		if (System.getProperty("os.name").startsWith("Windows")) {
			for (int i = 0; i < args.length; i++)
				args[i] = quoteArg(args[i]);
			// stupid, stupid, stupid Windows taking all my time!!!
			if (args[0].startsWith("../"))
				args[0] = new File(dir,
						args[0]).getAbsolutePath();
		}

		Process proc = Runtime.getRuntime().exec(args, null, dir);
		new ReadInto(proc.getErrorStream(), err).start();
		new ReadInto(proc.getInputStream(), out).start();
		try {
			proc.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
		int exitValue = proc.exitValue();
		if (exitValue != 0)
			throw new RuntimeException("Failed: " + exitValue);
	}

	private static String quotables = " \"\'";
	public static String quoteArg(String arg) {
		return quoteArg(arg, quotables);
	}

	public static String quoteArg(String arg, String quotables) {
		for (int j = 0; j < arg.length(); j++) {
			char c = arg.charAt(j);
			if (quotables.indexOf(c) >= 0) {
				String replacement;
				if (c == '"') {
					if (System.getenv("MSYSTEM") != null)
						replacement = "\\" + c;
					else
						replacement = "'" + c + "'";
				}
				else
					replacement = "\"" + c + "\"";
				arg = arg.substring(0, j)
					+ replacement
					+ arg.substring(j + 1);
				j += replacement.length() - 1;
			}
		}
		return arg;
	}

	protected static JarClassLoader discoverJavac() throws IOException {
		File ijHome = new File(System.getProperty("ij.dir"));
		File javac = new File(ijHome, "jars/javac.jar");
		if (!javac.exists()) {
			javac = new File(ijHome, "precompiled/javac.jar");
			if (!javac.exists()) {
				System.err.println("No javac.jar found (looked in " + ijHome + ")!");
				return null;
			}
		}
		return new JarClassLoader(javac.getPath());
	}
}
