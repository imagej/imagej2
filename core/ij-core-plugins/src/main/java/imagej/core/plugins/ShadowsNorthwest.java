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
		menuPath = "PureIJ2>Process>Shadows>Northwest"
)
public class ShadowsNorthwest implements ImageJPlugin
{
	@Parameter
	private Dataset input;
	
	@Parameter(output=true)
	private Dataset output;
	
	private Convolve3x3Operation operation;
	
	public ShadowsNorthwest()
	{
	}

	@Override
	public void run()
	{
		operation = new Convolve3x3Operation(input, new double[]{2,1,0,  1,1,-1,  0,-1,-2});
		output = operation.run();
	}
}
