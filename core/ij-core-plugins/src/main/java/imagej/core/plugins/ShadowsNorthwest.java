package imagej.core.plugins;

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
public class ShadowsNorthwest extends Convolve3x3Operation
{
	public ShadowsNorthwest()
	{
		super(new double[]{2,1,0,  1,1,-1,  0,-1,-2});
	}
}
