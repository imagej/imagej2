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

package imagej.plugins.scripting.beanshell;

import bsh.EvalError;
import bsh.Interpreter;
import imagej.script.AbstractScriptEngine;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.scijava.util.LineOutputStream;

/**
 * Beanshell support for ImageJ
 * 
 * @author Johannes Schindelin
 */
public class BeanshellScriptEngine extends AbstractScriptEngine
{

	protected final Interpreter interpreter;

	public BeanshellScriptEngine() {
		interpreter = new Interpreter();
		engineScopeBindings = new BeanshellBindings(interpreter);
	}

	@Override
	public Object eval(final String script) throws ScriptException {
		setup();
		try {
			return interpreter.eval(script);
		}
		catch (final EvalError e) {
			throw new ScriptException(e);
		}
	}

	@Override
	public Object eval(final Reader reader) throws ScriptException {
		setup();
		try {
			final String filename = (String) get(ScriptEngine.FILENAME);
			return interpreter.eval(reader, interpreter.getNameSpace(), filename);
		}
		catch (final EvalError e) {
			throw new ScriptException(e);
		}
	}

	protected void setup() {
		final ScriptContext context = getContext();
		final Reader reader = context.getReader();
		if (reader != null) {
			log().warn("Beanshell does not support redirecting the input");
		}
		final Writer writer = context.getWriter();
		if (writer != null) interpreter.setOut(new PrintStream(
			new WriterOutputStream(writer)));
		final Writer errorWriter = context.getErrorWriter();
		if (errorWriter != null) interpreter.setErr(new PrintStream(
			new WriterOutputStream(errorWriter)));
	}

	private static class WriterOutputStream extends LineOutputStream {

		private Writer writer;

		public WriterOutputStream(final Writer writer) {
			this.writer = writer;
		}

		@Override
		public void println(String line) throws IOException {
			writer.write(line);
			writer.write('\n');
		}

	}
}
