package imagej.core.plugins;

import imagej.core.plugins.XYFlipper.FlipCoordinateTransformer;
import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

/**
 * TODO
 * @author Barry DeZonia
 *
 * @param <T>
 */
@Plugin(
		menuPath = "PureIJ2>Image>Transform>Rotate 90 Degrees Left"
)
public class Rotate90DegreesLeft implements ImageJPlugin
{
	@Parameter
	private Dataset input;
	
	@Parameter(output=true)
	private Dataset output;
	
	public Rotate90DegreesLeft()
	{
	}
	
	@Override
	public void run()
	{
		FlipCoordinateTransformer flipTransformer = new NinetyLeftTransformer();
		XYFlipper flipper = new XYFlipper(input, flipTransformer);
		ImglibOutputAlgorithmRunner runner = new ImglibOutputAlgorithmRunner(flipper);
		output = runner.run();
	}
	
	class NinetyLeftTransformer implements FlipCoordinateTransformer
	{
		@Override
		public void calcOutputPosition(int[] inputDimensions, int[] inputPosition, int[] outputPosition)
		{
			outputPosition[1] = inputDimensions[0] - inputPosition[0] - 1;
			outputPosition[0] = inputPosition[1];
		}

		@Override
		public int[] calcOutputDimensions(int[] inputDimensions)
		{
			int[] outputDims = inputDimensions.clone();
			
			outputDims[0] = inputDimensions[1];
			outputDims[1] = inputDimensions[0];
			
			return outputDims;
		}
	}
}
