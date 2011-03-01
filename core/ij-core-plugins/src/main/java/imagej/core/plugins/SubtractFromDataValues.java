package imagej.core.plugins;

import imagej.plugin.Plugin;
import imagej.plugin.Parameter;
import imglib.ops.function.p1.UnaryOperatorFunction;
import imglib.ops.operator.UnaryOperator;
import imglib.ops.operator.unary.SubtractConstant;

/**
 * TODO
 *
 * @author Barry DeZonia
 */
@Plugin(
	menuPath = "PureIJ2>Process>Math>Subtract"
)
public class SubtractFromDataValues extends NAryOperation
{
	@Parameter(label="Enter value to subtract from each data value")
	private double constant;
	
	public SubtractFromDataValues()
	{
	}
	
	@Override
	public void run()
	{
		UnaryOperator op = new SubtractConstant(constant);
		UnaryOperatorFunction func = new UnaryOperatorFunction(op);
		setFunction(func);
		super.run();
	}
}
