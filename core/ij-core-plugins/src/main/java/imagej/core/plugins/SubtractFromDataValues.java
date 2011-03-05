package imagej.core.plugins;

import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Plugin;
import imagej.plugin.Parameter;
import imglib.ops.function.p1.UnaryOperatorFunction;
import imglib.ops.operator.UnaryOperator;
import imglib.ops.operator.unary.SubtractConstant;

/**
 * Fills an output Dataset by subtracting a user defined constant value from an input Dataset.
 *
 * @author Barry DeZonia
 */
@Plugin(
	menuPath = "PureIJ2>Process>Math>Subtract"
)
public class SubtractFromDataValues implements ImageJPlugin
{
	// ***************  instance variables that are Parameters ***************************************************************

	@Parameter
	private Dataset input;
	
	@Parameter(output=true)
	private Dataset output;

	@Parameter(label="Enter value to subtract from each data value")
	private double constant;
	
	// ***************  constructor ***************************************************************

	public SubtractFromDataValues()
	{
	}
	
	// ***************  public interface ***************************************************************

	@Override
	public void run()
	{
		UnaryOperator op = new SubtractConstant(constant);
		output = new UnaryTransformation(input, output, op).run();
	}
}
