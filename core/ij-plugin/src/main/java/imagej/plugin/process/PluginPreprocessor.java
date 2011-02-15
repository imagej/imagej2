package imagej.plugin.process;

public interface PluginPreprocessor extends PluginProcessor {
	// PluginPreprocessor trivially extends IPluginProcessor to differentiate
	// preprocessors from postprocessors while sharing the same contract.
}
