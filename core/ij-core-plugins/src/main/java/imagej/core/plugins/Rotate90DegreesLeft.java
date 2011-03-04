package imagej.core.plugins;

import imagej.core.plugins.XYFlipper.FlipCoordinateTransformer;
import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

// TODO - IJ1 updates the calibration so that pixel width & depth swap after this operation. Must implement here.

/**
 * Creates an output Dataset that is a duplicate of an input Dataset rotated 90 degrees to the left 
 * 
 * @author Barry DeZonia
 *
 */
@Plugin(
		menuPath = "PureIJ2>Image>Transform>Rotate 90 Degrees Left"
)
public class Rotate90DegreesLeft implements ImageJPlugin
{
	// ***************  instance variables that are Parameters ***************************************************************

	@Parameter
	private Dataset input;
	
	@Parameter(output=true)
	private Dataset output;
	
	// ***************  constructor ***************************************************************

	public Rotate90DegreesLeft()
	{
	}
	
	// ***************  public interface ***************************************************************

	@Override
	public void run()
	{
		FlipCoordinateTransformer flipTransformer = new NinetyLeftTransformer();
		XYFlipper flipper = new XYFlipper(input, flipTransformer);
		ImglibOutputAlgorithmRunner runner = new ImglibOutputAlgorithmRunner(flipper);
		output = runner.run();
	}
	
	// ***************  private interface ***************************************************************

	private class NinetyLeftTransformer implements FlipCoordinateTransformer
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
