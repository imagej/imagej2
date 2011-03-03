package imagej.core.plugins;

import imagej.plugin.Plugin;

/**
 * TODO
 * @author Barry DeZonia
 *
 * @param <T>
 */
@Plugin(
		menuPath = "PureIJ2>Process>Shadows>West"
)
public class ShadowsWest extends Convolve3x3Operation
{
	public ShadowsWest()
	{
		super(new double[]{1,0,-1,  2,1,-2,  1,0,-1});
	}
}
