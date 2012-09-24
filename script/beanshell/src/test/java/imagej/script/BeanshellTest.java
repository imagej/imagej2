package imagej.script;

import static org.junit.Assert.assertEquals;
import imagej.ImageJ;
import imagej.service.ServiceHelper;

import java.io.StringReader;

import org.junit.Test;

public class BeanshellTest {

	@Test
	public void testBasic() throws Exception {
		final ImageJ context = ImageJ.createContext();
		new ServiceHelper(context).createExactService(DefaultScriptService.class);
		final ScriptService scriptService = context.getService(ScriptService.class);
		new ServiceHelper(context).createExactService(DummyService.class);

		String script =
			"dummy = IJ.getService(imagej.script.DummyService.class);\n" +
			"dummy.context = IJ;\n" +
			"dummy.value = 1234;\n";
		scriptService.eval("hello.bsh", new StringReader(script));

		final DummyService dummy = context.getService(DummyService.class);
		assertEquals(context, dummy.context);
		assertEquals(1234, dummy.value);
	}

}
