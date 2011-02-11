package imagej.plugin.process;

public interface PluginPreprocessor extends IPluginProcessor {
	// PluginPreprocessor trivially extends IPluginProcessor to differentiate
	// preprocessors from postprocessors while sharing the same contract, so
	// that SezPoz can unambiguously discover plugin preprocessors.
}
