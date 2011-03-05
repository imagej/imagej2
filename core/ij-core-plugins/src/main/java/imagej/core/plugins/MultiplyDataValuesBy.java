package imagej.core.plugins;

import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Plugin;
import imagej.plugin.Parameter;
import imglib.ops.function.p1.UnaryOperatorFunction;
import imglib.ops.operator.UnaryOperator;
import imglib.ops.operator.unary.MultiplyByConstant;

/**
 * Fills an output Dataset by multiplying an input Dataset by a user defined constant value.
 *
 * @author Barry DeZonia
 */
@Plugin(
	menuPath = "PureIJ2>Process>Math>Multiply"
)
public class MultiplyDataValuesBy implements ImageJPlugin
{
	// ***************  instance variables that are Parameters ***************************************************************

	@Parameter
	private Dataset input;
	
	@Parameter(output=true)
	private Dataset output;
	
	@Parameter(label="Enter value for scaling constant")
	private double constant;
	
	// ***************  constructor ***************************************************************

	public MultiplyDataValuesBy()
	{
	}
	
	// ***************  public interface ***************************************************************

	@Override
	public void run()
	{
		UnaryOperator op = new MultiplyByConstant(constant);
		output = new UnaryTransformation(input, output, op).run();
	}
}
