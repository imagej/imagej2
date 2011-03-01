package imagej.core.plugins;

import imagej.plugin.Plugin;
import imglib.ops.function.p1.UnaryOperatorFunction;
import imglib.ops.operator.UnaryOperator;
import imglib.ops.operator.unary.Exp;

/**
 * TODO
 *
 * @author Barry DeZonia
 */
@Plugin(
	menuPath = "PureIJ2>Process>Math>Exp"
)
public class ExpDataValues extends NAryOperation
{
	public ExpDataValues()
	{
		UnaryOperator op = new Exp();
		UnaryOperatorFunction func = new UnaryOperatorFunction(op);
		setFunction(func);
	}
}
