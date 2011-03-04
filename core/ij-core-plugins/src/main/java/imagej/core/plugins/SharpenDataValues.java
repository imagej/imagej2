package imagej.core.plugins;

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
		menuPath = "PureIJ2>Process>Sharpen"
)
public class SharpenDataValues implements ImageJPlugin
{
	// ***************  instance variables that are Parameters ***************************************************************

	@Parameter
	private Dataset input;
	
	@Parameter(output=true)
	private Dataset output;
	
	// ***************  other instance variables ***************************************************************

	private Convolve3x3Operation operation;
	
	// ***************  constructor ***************************************************************

	public SharpenDataValues()
	{
	}

	// ***************  public interface ***************************************************************

	@Override
	public void run()
	{
		operation = new Convolve3x3Operation(input, new double[]{-1,-1,-1,  -1,12,-1,  -1,-1,-1});
		output = operation.run();
	}
}
