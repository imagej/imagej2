package imagej.plugin.process;

public interface PluginPostprocessor extends PluginProcessor {
	// PluginPostprocessor trivially extends IPluginProcessor to differentiate
	// preprocessors from postprocessors while sharing the same contract.
}
