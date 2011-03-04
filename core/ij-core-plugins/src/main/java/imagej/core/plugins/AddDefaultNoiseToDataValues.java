package imagej.core.plugins;

import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

/**
 * TODO
 *
 * @author Barry DeZonia
 */
@Plugin(
	menuPath = "PureIJ2>Process>Noise>Add Noise"
)
public class AddDefaultNoiseToDataValues implements ImageJPlugin
{
	@Parameter
	Dataset input;
	
	@Parameter(output=true)
	Dataset output;
	
	public AddDefaultNoiseToDataValues()
	{
	}

	@Override
	public void run()
	{
		AddNoiseToDataValues noiseAdder = new AddNoiseToDataValues(input);
		noiseAdder.setOutput(output);
		noiseAdder.setStdDev(25.0);
		output = noiseAdder.run();
	}
}
