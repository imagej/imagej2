package imagej.core.plugins;

import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

/**
 * TODO
 *
 * @author Barry DeZonia
 */
@Plugin(
	menuPath = "PureIJ2>Process>Noise>Add Specified Noise"
)
public class AddSpecificNoiseToDataValues extends AddNoiseToDataValues
{
	@Parameter(label="Enter standard deviation of range")
	double stdDev;
	
	public AddSpecificNoiseToDataValues()
	{
	}

	@Override
	public void run()
	{
		setStdDev(stdDev);
		super.run();
	}
}
