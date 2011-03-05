package imagej.core.plugins;

import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imglib.ops.operator.UnaryOperator;
import imglib.ops.operator.unary.DivideByConstant;

/**
 * Fills an output Dataset by dividing an input Dataset by a user defined constant value.
 *
 * @author Barry DeZonia
 */
@Plugin(
	menuPath = "PureIJ2>Process>Math>Divide"
)
public class DivideDataValuesBy implements ImageJPlugin
{
	// ***************  instance variables that are Parameters ***************************************************************

	@Parameter
	Dataset input;
	
	@Parameter(output=true)
	Dataset output;
	
	@Parameter(label="Enter value to divide each data value by")
	private double constant;

	// ***************  public interface ***************************************************************

	@Override
	public void run()
	{
		UnaryOperator op = new DivideByConstant(constant);
		output = new UnaryTransformation(input, output, op).run();
	}
}
