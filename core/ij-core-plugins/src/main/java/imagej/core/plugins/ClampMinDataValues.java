package imagej.core.plugins;

import imagej.plugin.Plugin;
import imagej.plugin.Parameter;
import imglib.ops.function.p1.UnaryOperatorFunction;
import imglib.ops.operator.UnaryOperator;
import imglib.ops.operator.unary.Min;

/**
 * TODO
 *
 * @author Barry DeZonia
 */
@Plugin(
	menuPath = "Process>Math2>Min"
)
public class ClampMinDataValues extends NAryOperation
{
	@Parameter(label="Enter minimum clamp value")
	private double constant;
	
	public ClampMinDataValues()
	{
	}
	
	@Override
	public void run()
	{
		UnaryOperator op = new Min(constant);
		UnaryOperatorFunction func = new UnaryOperatorFunction(op);
		setFunction(func);
		super.run();
	}
}
