//
// TemplateFiller.java
//

package imagej.core.plugins.misc;

import imagej.ext.module.Module;
import imagej.ext.module.ModuleItem;

import java.io.StringWriter;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

public class TemplateFiller {

	private final VelocityEngine engine;

	public TemplateFiller() {
		engine = new VelocityEngine();
		final Properties p = new Properties();
		p.setProperty("resource.loader", "class");
		p.setProperty("class.resource.loader.class",
			"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		engine.init(p);
	}

	/**
	 * Populates the template specified by the given resource, using the input
	 * values from the given module instance.
	 */
	public String fillTemplate(final String resource, final Module module) {
		final VelocityContext context = new VelocityContext();
		for (final ModuleItem<?> input : module.getInfo().inputs()) {
			final String name = input.getName();
			final Object value = input.getValue(module);
			context.put(name, value);
		}

		final Template t = engine.getTemplate(resource);
		final StringWriter writer = new StringWriter();
		t.merge(context, writer);

		return writer.toString();
	}

}
