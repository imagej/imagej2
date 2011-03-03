package imagej.core.plugins;

import imagej.plugin.Plugin;

/**
 * TODO
 * @author Barry DeZonia
 *
 * @param <T>
 */
@Plugin(
		menuPath = "PureIJ2>Process>Smooth"
)
public class SmoothDataValues extends Convolve3x3Operation
{
	public SmoothDataValues()
	{
		super(new double[]{1,1,1,
							1,1,1,
							1,1,1});
	}
}
