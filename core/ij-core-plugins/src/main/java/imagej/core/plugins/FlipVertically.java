package imagej.core.plugins;

import imagej.plugin.Plugin;

/**
 * TODO
 * @author Barry DeZonia
 *
 * @param <T>
 */
@Plugin(
		menuPath = "PureIJ2>Image>Transform>Flip Vertically"
)
public class FlipVertically extends XYFlipper
{
	@Override
	void calcOutputPosition(int[] inputDimensions, int[] inputPosition, int[] outputPosition)
	{
		outputPosition[0] = inputPosition[0];
		outputPosition[1] = inputDimensions[1] - inputPosition[1] - 1;
	}

	@Override
	int[] calcOutputDimensions(int[] inputDimensions)
	{
		return inputDimensions.clone();
	}
}
