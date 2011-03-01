package imagej.core.plugins;

import imagej.plugin.Plugin;
import imglib.ops.function.p1.UnaryOperatorFunction;
import imglib.ops.operator.UnaryOperator;
import imglib.ops.operator.unary.Sqrt;

/**
 * TODO
 *
 * @author Barry DeZonia
 */
@Plugin(
	menuPath = "PureIJ2>Process>Math>Square Root"
)
public class SquareRootDataValues extends NAryOperation
{
	public SquareRootDataValues()
	{
		UnaryOperator op = new Sqrt();
		UnaryOperatorFunction func = new UnaryOperatorFunction(op);
		setFunction(func);
	}
}
