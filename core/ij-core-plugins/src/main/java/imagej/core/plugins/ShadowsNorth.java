package imagej.core.plugins;

import imagej.plugin.Plugin;

/**
 * TODO
 * @author Barry DeZonia
 *
 * @param <T>
 */
@Plugin(
		menuPath = "PureIJ2>Process>Shadows>North"
)
public class ShadowsNorth extends Convolve3x3Operation
{
	public ShadowsNorth()
	{
		super(new double[]{1,2,1,  0,1,0,  -1,-2,-1});
	}
}
