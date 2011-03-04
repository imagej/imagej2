package imagej.core.plugins;

import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

/**
 * Fills an output Dataset by applying a user calibrated amount of random noise to an input Dataset.
 *
 * @author Barry DeZonia
 */
@Plugin(
	menuPath = "PureIJ2>Process>Noise>Add Specified Noise"
)
public class AddSpecificNoiseToDataValues implements ImageJPlugin
{
	// ***************  instance variables that are Parameters ***************************************************************

	@Parameter
	Dataset input;
	
	@Parameter(output=true)
	Dataset output;
	
	@Parameter(label="Enter standard deviation of range")
	double stdDev;
	
	// ***************  constructor ***************************************************************

	public AddSpecificNoiseToDataValues()
	{
	}

	// ***************  public interface ***************************************************************

	@Override
	public void run()
	{
		AddNoiseToDataValues noiseAdder = new AddNoiseToDataValues(input);
		noiseAdder.setOutput(output);
		noiseAdder.setStdDev(stdDev);
		output = noiseAdder.run();
	}
}
