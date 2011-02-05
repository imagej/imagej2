package imagej.plugin.spi;

public interface PluginPostprocessor extends PluginProcessor {
	// PluginPostprocessor trivially extends PluginProcessor to differentiate
	// preprocessors from postprocessors while sharing the same contract.
}
