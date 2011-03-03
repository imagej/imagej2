package imagej.core.plugins;

import imagej.plugin.Plugin;

/**
 * TODO
 * @author Barry DeZonia
 *
 * @param <T>
 */
@Plugin(
		menuPath = "PureIJ2>Image>Transform>Flip Horizontally"
)
public class FlipHorizontally extends XYFlipper
{
	@Override
	void calcOutputPosition(int[] inputDimensions, int[] inputPosition, int[] outputPosition)
	{
		outputPosition[0] = inputDimensions[0] - inputPosition[0] - 1;
		outputPosition[1] = inputPosition[1];
	}

	@Override
	int[] calcOutputDimensions(int[] inputDimensions)
	{
		return inputDimensions.clone();
	}
}
