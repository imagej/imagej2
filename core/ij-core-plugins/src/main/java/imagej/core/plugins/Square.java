package imagej.core.plugins;

import imagej.plugin.Plugin;
import imglib.ops.function.p1.SqrFunction;

@Plugin(
	menuPath = "Process>Square"
)
@SuppressWarnings("rawtypes")
public class Square extends NAryOperation
{
	public Square()
	{
		super(new SqrFunction());
	}
}
