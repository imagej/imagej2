package imagej.core.plugins;

import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Plugin;
import imagej.plugin.Parameter;
import imglib.ops.function.p1.UnaryOperatorFunction;
import imglib.ops.operator.UnaryOperator;
import imglib.ops.operator.unary.OrConstant;

/**
 * Fills an output Dataset by ORing an input Dataset with a user defined constant value.
 *
 * @author Barry DeZonia
 */
@Plugin(
	menuPath = "PureIJ2>Process>Math>OR"
)
public class OrDataValuesWith implements ImageJPlugin
{
	// ***************  instance variables that are Parameters ***************************************************************

	@Parameter
	Dataset input;
	
	@Parameter(output=true)
	Dataset output;
	
	@Parameter(label="Enter value to OR with each data value")
	private long constant;
	
	// ***************  constructor ***************************************************************

	public OrDataValuesWith()
	{
	}

	// ***************  public interface ***************************************************************

	@Override
	public void run()
	{
		UnaryOperator op = new OrConstant(constant);
		output = new UnaryTransformation(input, output, op).run();
	}
}
