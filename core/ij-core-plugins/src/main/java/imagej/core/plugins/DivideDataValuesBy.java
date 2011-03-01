package imagej.core.plugins;

import imagej.plugin.Plugin;
import imagej.plugin.Parameter;
import imglib.ops.function.p1.UnaryOperatorFunction;
import imglib.ops.operator.UnaryOperator;
import imglib.ops.operator.unary.DivideByConstant;

/**
 * TODO
 *
 * @author Barry DeZonia
 */
@Plugin(
	menuPath = "Process>Math2>Divide"
)
public class DivideDataValuesBy extends NAryOperation
{
	@Parameter(label="Enter value to divide each data value by")
	private double constant;
	
	public DivideDataValuesBy()
	{
	}
	
	@Override
	public void run()
	{
		UnaryOperator op = new DivideByConstant(constant);
		UnaryOperatorFunction func = new UnaryOperatorFunction(op);
		setFunction(func);
		super.run();
	}
}
