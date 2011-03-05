package imagej.core.plugins;

import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Plugin;
import imagej.plugin.Parameter;
import imglib.ops.function.p1.UnaryOperatorFunction;
import imglib.ops.operator.UnaryOperator;
import imglib.ops.operator.unary.AddConstant;

/**
 * Fills an output Dataset by adding a user defined constant value to an input Dataset.
 *
 * @author Barry DeZonia
 */
@Plugin(
	menuPath = "PureIJ2>Process>Math>Add"
)
public class AddToDataValues implements ImageJPlugin
{
	// ***************  instance variables that are Parameters ***************************************************************

	@Parameter
	private Dataset input;
	
	@Parameter(output=true)
	private Dataset output;

	@Parameter(label="Enter value to add to each data value")
	private double constant;
	
	// ***************  constructor ***************************************************************

	public AddToDataValues()
	{
	}
	
	// ***************  public interface ***************************************************************

	@Override
	public void run()
	{
		UnaryOperator op = new AddConstant(constant);
		output = new UnaryTransformation(input, output, op).run();
	}
}
