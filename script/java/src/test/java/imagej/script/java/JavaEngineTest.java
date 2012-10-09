package imagej.script.java;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.junit.Test;

public class JavaEngineTest {

	@Test
	public void minimalProject() throws Exception {
		final File dir = makeMinimalProject();

		boolean result = false;
		try {
			evalJava(new File(dir, "pom.xml"));
		} catch (ScriptException e) {
			result = e.getCause().getCause().getMessage().equals("success");
		}
		assertTrue(result);

		assertTrue(new File(dir, "target").isDirectory());

		final File jar = new File(dir, "target/MinimalTest-1.0.0.jar");
		assertTrue(jar.exists());

	}

	// -- helper functions

	private File makeMinimalProject() throws IOException {
		final String source = "package minimaven;\n"
			+ "public class MinimalTest {\n"
			+ "\tpublic static void main(final String[] args) throws Exception {\n"
			+ "\t\tthrow new RuntimeException(\"success\");\n"
			+ "\t}\n"
			+ "}\n";
		final File dir = makeProject("minimaven.MinimalTest", "minimaven/MinimalTest.java", source);
		return dir;
	}

	private ScriptEngine evalJava(final File file) throws ScriptException {
		ScriptEngine miniMaven = new JavaEngineFactory().getScriptEngine();
		miniMaven.put(ScriptEngine.FILENAME, file.getPath());
		final ScriptContext context = miniMaven.getContext();
		context.setWriter(new OutputStreamWriter(System.out));
		context.setErrorWriter(new OutputStreamWriter(System.err));
		miniMaven.eval((Reader)null);
		return miniMaven;
	}

	private File makeProject(final String mainClass, final String... args) throws IOException {
		final File dir = File.createTempFile("java-", "");
		assertTrue(dir.delete());
		assertTrue(dir.mkdir());

		final FileWriter pom = new FileWriter(new File(dir, "pom.xml"));
		pom.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n"
			+ " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
			+ " xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0\n"
			+ " http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
			+ " <modelVersion>4.0.0</modelVersion>\n"
			+ " <groupId>net.imagej</groupId>\n"
			+ " <artifactId>MinimalTest</artifactId>\n"
			+ " <version>1.0.0</version>\n"
			+ " <build>\n"
			+ "  <plugins>\n"
			+ "   <plugin>\n"
			+ "    <artifactId>maven-jar-plugin</artifactId>\n"
			+ "    <configuration>\n"
			+ "     <archive>\n"
			+ "      <manifest>\n"
			+ "       <mainClass>" + mainClass + "</mainClass>\n"
			+ "      </manifest>\n"
			+ "     </archive>\n"
			+ "    </configuration>\n"
			+ "   </plugin>\n"
			+ "  </plugins>\n"
			+ " </build>\n"
			+ "</project>\n");
		pom.close();

		writeFiles(dir, args);

		return dir;
	}

	private void writeFiles(final File dir, final String... args)
		throws IOException
	{
		assertTrue("need filename/content pairs", (args.length % 2) == 0);

		for (int i = 0; i < args.length; i += 2) {
			final File src = new File(dir, "src/main/java/" + args[i]);
			final File srcDir = src.getParentFile();
			assertTrue(srcDir.isDirectory() || srcDir.mkdirs());
			final FileWriter writer = new FileWriter(src);
			writer.write(args[i + 1]);
			writer.close();
		}
	}
}

