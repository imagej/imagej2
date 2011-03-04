package imagej.core.plugins;

import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imglib.ops.function.p1.UnaryOperatorFunction;
import imglib.ops.operator.UnaryOperator;
import imglib.ops.operator.unary.Abs;

/**
 * TODO
 *
 * @author Barry DeZonia
 */
@Plugin(
	menuPath = "PureIJ2>Process>Math>Abs"
)
public class AbsDataValues implements ImageJPlugin
{
	@Parameter
	Dataset input;
	
	@Parameter(output=true)
	Dataset output;
	
	public AbsDataValues()
	{
	}

	@Override
	public void run()
	{
		UnaryOperator op = new Abs();
		
		UnaryOperatorFunction func = new UnaryOperatorFunction(op);
		
		NAryOperation operation = new NAryOperation(input, func);

		operation.setOutput(output);
		
		output = operation.run();
	}
}
