package imagej.core.commands.display.interactive.threshold;

import imagej.plugin.ImageJPlugin;


/**
 * @author Barry DeZonia
 */
public interface AutoThresholdMethod extends ImageJPlugin {

	int getThreshold(long[] histogram);
}
