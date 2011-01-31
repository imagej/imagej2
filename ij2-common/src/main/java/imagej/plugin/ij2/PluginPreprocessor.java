package imagej.plugin.ij2;

public interface PluginPreprocessor extends PluginProcessor {
	// PluginPreprocessor trivially extends PluginProcessor to differentiate
	// preprocessors from postprocessors while sharing the same contract.
}
