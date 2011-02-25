package imagej.core.plugins;

import imagej.plugin.Plugin;
import imglib.ops.function.p1.UnaryOperatorFunction;
import imglib.ops.operator.UnaryOperator;
import imglib.ops.operator.unary.Exp;

@Plugin(
	menuPath = "Process>Exp"
)
@SuppressWarnings("rawtypes")
public class ExpDataValues extends NAryOperation
{
	public ExpDataValues()
	{
		UnaryOperator op = new Exp();
		UnaryOperatorFunction func = new UnaryOperatorFunction(op);
		setFunction(func);
	}
}
