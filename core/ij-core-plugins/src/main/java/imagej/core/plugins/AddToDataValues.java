package imagej.core.plugins;

import imagej.plugin.Plugin;
import imagej.plugin.Parameter;
import imglib.ops.function.p1.UnaryOperatorFunction;
import imglib.ops.operator.UnaryOperator;
import imglib.ops.operator.unary.AddConstant;

/**
 * TODO
 *
 * @author Barry DeZonia
 */
@Plugin(
	menuPath = "Process>Add"
)
@SuppressWarnings("rawtypes")
public class AddToDataValues extends NAryOperation
{
	@Parameter(label="Enter value to add to each data value")
	private double constant;
	
	public AddToDataValues()
	{
		UnaryOperator op = new AddConstant(constant);
		UnaryOperatorFunction func = new UnaryOperatorFunction(op);
		setFunction(func);
	}
}
