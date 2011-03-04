package imagej.core.plugins;

import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
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
	menuPath = "PureIJ2>Process>Math>Add"
)
public class AddToDataValues implements ImageJPlugin
{
	@Parameter
	private Dataset input;
	
	@Parameter(output=true)
	private Dataset output;

	@Parameter(label="Enter value to add to each data value")
	private double constant;
	
	public AddToDataValues()
	{
	}
	
	@Override
	public void run()
	{
		UnaryOperator op = new AddConstant(constant);
		UnaryOperatorFunction func = new UnaryOperatorFunction(op);
		NAryOperation operation = new NAryOperation(input, func);
		operation.setOutput(output);
		output = operation.run();
	}
}
