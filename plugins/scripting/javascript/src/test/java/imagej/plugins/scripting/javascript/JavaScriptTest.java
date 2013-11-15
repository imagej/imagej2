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

package imagej.plugins.scripting.javascript;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import imagej.script.ScriptService;

import java.io.StringReader;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

import org.junit.Test;
import org.scijava.Context;
import org.scijava.service.ServiceHelper;

/**
 * JavaScript unit tests.
 * 
 * @author Johannes Schindelin
 * @author Curtis Rueden
 */
public class JavaScriptTest {

	@Test
	public void testBasic() throws Exception {
		final Context context = new Context(ScriptService.class);
		final ScriptService scriptService = context.getService(ScriptService.class);

		String script = "$x = 1 + 2;";
		final Double result =
			(Double) scriptService.eval("add.js", new StringReader(script));
		assertEquals(3.0, result.doubleValue(), 0.0);
	}

	@Test
	public void testLocals() throws Exception {
		final Context context = new Context(ScriptService.class);
		final ScriptService scriptService = context.getService(ScriptService.class);

		final ScriptEngineFactory factory = scriptService.getByFileExtension("js");
		final ScriptEngine engine = factory.getScriptEngine();
		assertTrue(engine.getClass().getName().endsWith(".RhinoScriptEngine"));
		engine.put("$hello", 17);
		assertEquals("17", engine.eval("$hello").toString());
		assertEquals("17", engine.get("$hello").toString());

		Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
		bindings.clear();
		assertNull(engine.get("$hello"));
	}

	@Test
	public void testContext() throws Exception {
		final Context context = new Context(ScriptService.class);
		final ScriptService scriptService = context.getService(ScriptService.class);
		new ServiceHelper(context).createExactService(DummyService.class);

		String script =
			"dummy = IJ.getService('" + DummyService.class.getName() + "');\n" +
			"dummy.context = IJ;\n" +
			"dummy.value = 1234;\n";
		scriptService.eval("hello.js", new StringReader(script));

		final DummyService dummy = context.getService(DummyService.class);
		assertEquals(context, dummy.context);
		assertEquals(1234, dummy.value);
	}

	@Test
	public void testParameters() throws Exception {
		final Context context = new Context(ScriptService.class);
		final ScriptService scriptService = context.getService(ScriptService.class);
		new ServiceHelper(context).createExactService(DummyService.class);

		String script =
				"// @DummyService d\n" +
				"d.value = 4321;\n";
		scriptService.eval("hello.js", new StringReader(script));

		final DummyService dummy = context.getService(DummyService.class);
		assertEquals(4321, dummy.value);
	}

}
