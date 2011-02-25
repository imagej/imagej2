package imagej.core.plugins;

import imagej.plugin.Plugin;
import imagej.plugin.Parameter;
import imglib.ops.function.p1.UnaryOperatorFunction;
import imglib.ops.operator.UnaryOperator;
import imglib.ops.operator.unary.MultiplyByConstant;

/**
 * TODO
 *
 * @author Barry DeZonia
 */
@Plugin(
	menuPath = "Process>Multiply"
)
@SuppressWarnings("rawtypes")
public class MultiplyDataValuesBy extends NAryOperation
{
	@Parameter(label="Enter value to multiply each data value by")
	private double constant;
	
	public MultiplyDataValuesBy()
	{
	}
	
	@Override
	public void run()
	{
		UnaryOperator op = new MultiplyByConstant(constant);
		UnaryOperatorFunction func = new UnaryOperatorFunction(op);
		setFunction(func);
		super.run();
	}
}
