package imagej.core.plugins;

import imagej.plugin.Plugin;

/**
 * TODO
 *
 * @author Barry DeZonia
 */
@Plugin(
	menuPath = "PureIJ2>Process>Noise>Add Noise"
)
public class AddDefaultNoiseToDataValues extends AddNoiseToDataValues
{
	public AddDefaultNoiseToDataValues()
	{
		setStdDev(25.0);
	}
}
