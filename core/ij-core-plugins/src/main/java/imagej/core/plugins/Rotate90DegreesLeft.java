package imagej.core.plugins;

import imagej.plugin.Plugin;

// TODO - IJ1 also swaps calibration values pixelWidth/pixelHeight during this operation.
//   Must do so too when calibration stored by Dataset/Img.

/**
 * TODO
 * @author Barry DeZonia
 *
 * @param <T>
 */
@Plugin(
		menuPath = "PureIJ2>Image>Transform>Rotate 90 Degrees Left"
)
public class Rotate90DegreesLeft extends XYFlipper
{
	@Override
	void calcOutputPosition(int[] inputDimensions, int[] inputPosition, int[] outputPosition)
	{
		outputPosition[1] = inputDimensions[0] - inputPosition[0] - 1;
		outputPosition[0] = inputPosition[1];
	}

	@Override
	int[] calcOutputDimensions(int[] inputDimensions)
	{
		int[] outputDims = inputDimensions.clone();
		
		outputDims[0] = inputDimensions[1];
		outputDims[1] = inputDimensions[0];
		
		return outputDims;
	}
}
