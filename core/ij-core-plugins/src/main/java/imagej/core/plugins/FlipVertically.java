package imagej.core.plugins;

import imagej.core.plugins.XYFlipper.FlipCoordinateTransformer;
import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

/**
 * Creates an output Dataset that is a duplicate of an input Dataset flipped vertically 
 *
 * @author Barry DeZonia
 *
 */
@Plugin(
		menuPath = "PureIJ2>Image>Transform>Flip Vertically"
)
public class FlipVertically implements ImageJPlugin
{
	// ***************  instance variables that are Parameters ***************************************************************

	@Parameter
	private Dataset input;
	
	@Parameter(output=true)
	private Dataset output;
	
	// ***************  constructor ***************************************************************

	public FlipVertically()
	{
	}
	
	// ***************  public interface ***************************************************************

	@Override
	public void run()
	{
		FlipCoordinateTransformer flipTransformer = new VertFlipTransformer();
		XYFlipper flipper = new XYFlipper(input, flipTransformer);
		ImglibOutputAlgorithmRunner runner = new ImglibOutputAlgorithmRunner(flipper);
		output = runner.run();
	}
	
	// ***************  private interface ***************************************************************

	private class VertFlipTransformer implements FlipCoordinateTransformer
	{
		@Override
		public void calcOutputPosition(int[] inputDimensions, int[] inputPosition, int[] outputPosition)
		{
			outputPosition[0] = inputPosition[0];
			outputPosition[1] = inputDimensions[1] - inputPosition[1] - 1;
		}

		@Override
		public int[] calcOutputDimensions(int[] inputDimensions)
		{
			return inputDimensions.clone();
		}
	}
}
