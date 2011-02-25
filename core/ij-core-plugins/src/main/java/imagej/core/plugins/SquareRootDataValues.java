package imagej.core.plugins;

import imagej.plugin.Plugin;
import imglib.ops.function.p1.UnaryOperatorFunction;
import imglib.ops.operator.UnaryOperator;
import imglib.ops.operator.unary.Sqrt;

@Plugin(
	menuPath = "Process>Square Root"
)
@SuppressWarnings("rawtypes")
public class SquareRootDataValues extends NAryOperation
{
	public SquareRootDataValues()
	{
		UnaryOperator op = new Sqrt();
		UnaryOperatorFunction func = new UnaryOperatorFunction(op);
		setFunction(func);
	}
}
