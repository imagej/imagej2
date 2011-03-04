package imagej.core.plugins;

import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
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
	menuPath = "PureIJ2>Process>Math>Multiply"
)
public class MultiplyDataValuesBy implements ImageJPlugin
{
	@Parameter
	private Dataset input;
	
	@Parameter(output=true)
	private Dataset output;
	
	@Parameter(label="Enter value for scaling constant")
	private double constant;
	
	public MultiplyDataValuesBy()
	{
	}
	
	@Override
	public void run()
	{
		UnaryOperator op = new MultiplyByConstant(constant);
		UnaryOperatorFunction func = new UnaryOperatorFunction(op);
		NAryOperation operation = new NAryOperation(input, func);
		operation.setOutput(output);
		output = operation.run();
	}
}
