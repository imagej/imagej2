package imagej.core.plugins;

import imagej.plugin.Plugin;

/**
 * TODO
 * @author Barry DeZonia
 *
 * @param <T>
 */
@Plugin(
		menuPath = "PureIJ2>Process>Shadows>South"
)
public class ShadowsSouth extends Convolve3x3Operation
{
	public ShadowsSouth()
	{
		super(new double[]{-1,-2,-1,  0,1,0,  1,2,1});
	}
}
