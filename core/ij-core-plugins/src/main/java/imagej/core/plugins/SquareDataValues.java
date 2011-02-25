package imagej.core.plugins;

import imagej.plugin.Plugin;
import imglib.ops.function.p1.SqrFunction;

/**
 * TODO
 *
 * @author Barry DeZonia
 */
@Plugin(
	menuPath = "Process>Square Data Values"
)
@SuppressWarnings("rawtypes")
public class SquareDataValues extends NAryOperation
{
	public SquareDataValues()
	{
		super(new SqrFunction());
	}
}
