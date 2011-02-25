package imagej.core.plugins;

import imagej.plugin.Plugin;
import imglib.ops.function.p1.UnaryOperatorFunction;
import imglib.ops.operator.UnaryOperator;
import imglib.ops.operator.unary.Abs;

@Plugin(
	menuPath = "Process>Abs"
)
@SuppressWarnings("rawtypes")
public class AbsDataValues extends NAryOperation
{
	public AbsDataValues()
	{
		UnaryOperator op = new Abs();
		UnaryOperatorFunction func = new UnaryOperatorFunction(op);
		setFunction(func);
	}
}
