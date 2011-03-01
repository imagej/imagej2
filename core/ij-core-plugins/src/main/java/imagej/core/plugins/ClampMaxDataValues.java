package imagej.core.plugins;

import imagej.plugin.Plugin;
import imagej.plugin.Parameter;
import imglib.ops.function.p1.UnaryOperatorFunction;
import imglib.ops.operator.UnaryOperator;
import imglib.ops.operator.unary.Max;

/**
 * TODO
 *
 * @author Barry DeZonia
 */
@Plugin(
	menuPath = "PureIJ2>Process>Math>Max"
)
public class ClampMaxDataValues extends NAryOperation
{
	@Parameter(label="Enter maximum clamp value")
	private double constant;
	
	public ClampMaxDataValues()
	{
	}
	
	@Override
	public void run()
	{
		UnaryOperator op = new Max(constant);
		UnaryOperatorFunction func = new UnaryOperatorFunction(op);
		setFunction(func);
		super.run();
	}
}
