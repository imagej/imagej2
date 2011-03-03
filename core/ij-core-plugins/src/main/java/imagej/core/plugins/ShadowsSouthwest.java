package imagej.core.plugins;

import imagej.plugin.Plugin;

/**
 * TODO
 * @author Barry DeZonia
 *
 * @param <T>
 */
@Plugin(
		menuPath = "PureIJ2>Process>Shadows>Southwest"
)
public class ShadowsSouthwest extends Convolve3x3Operation
{
	public ShadowsSouthwest()
	{
		super(new double[]{0,-1,-2,  1,1,-1,  2,1,0});
	}
}
