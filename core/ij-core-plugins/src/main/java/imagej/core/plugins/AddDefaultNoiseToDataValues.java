package imagej.core.plugins;

import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

/**
 * Fills an output Dataset by applying a default amount of random noise to an input Dataset.
 *
 * @author Barry DeZonia
 */
@Plugin(
	menuPath = "PureIJ2>Process>Noise>Add Noise"
)
public class AddDefaultNoiseToDataValues implements ImageJPlugin
{
	// ***************  instance variables that are Parameters ***************************************************************

	@Parameter
	Dataset input;
	
	@Parameter(output=true)
	Dataset output;

	// ***************  public interface ***************************************************************

	@Override
	public void run()
	{
		AddNoiseToDataValues noiseAdder = new AddNoiseToDataValues(input);
		noiseAdder.setOutput(output);
		noiseAdder.setStdDev(25.0);
		output = noiseAdder.run();
	}
}
