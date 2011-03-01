package imagej.core.plugins;

import imagej.plugin.Plugin;
import imglib.ops.function.p1.UnaryOperatorFunction;
import imglib.ops.operator.UnaryOperator;
import imglib.ops.operator.unary.Reciprocal;

/**
 * TODO
 *
 * @author Barry DeZonia
 */
@Plugin(
	menuPath = "Process>Math2>Reciprocal"
)
public class ReciprocalDataValues extends NAryOperation
{
	public ReciprocalDataValues()
	{
		UnaryOperator op = new Reciprocal();
		UnaryOperatorFunction func = new UnaryOperatorFunction(op);
		setFunction(func);
	}
}
