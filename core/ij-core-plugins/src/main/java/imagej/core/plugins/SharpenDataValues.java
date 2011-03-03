package imagej.core.plugins;

import imagej.plugin.Plugin;

/**
 * TODO
 * @author Barry DeZonia
 *
 * @param <T>
 */
@Plugin(
		menuPath = "PureIJ2>Process>Sharpen"
)
public class SharpenDataValues extends Convolve3x3Operation
{
	public SharpenDataValues()
	{
		super(new double[]{-1, -1, -1,
							-1, 12, -1,
							-1, -1, -1});
	}
}
