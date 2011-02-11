package imagej.plugin.process;

public interface PluginPostprocessor extends IPluginProcessor {
	// PluginPostprocessor trivially extends IPluginProcessor to differentiate
	// preprocessors from postprocessors while sharing the same contract, so
	// that SezPoz can unambiguously discover plugin postprocessors.
}
