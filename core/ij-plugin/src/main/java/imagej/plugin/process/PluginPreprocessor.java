package imagej.plugin.process;

/**
 * TODO
 *
 * @author Curtis Rueden
 */
public interface PluginPreprocessor extends PluginProcessor {

	// PluginPreprocessor is a plugin that extends PluginProcessor,
	// discoverable via the plugin discovery mechanism.

	boolean canceled();

}
