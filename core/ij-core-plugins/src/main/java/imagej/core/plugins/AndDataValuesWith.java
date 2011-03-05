package imagej.core.plugins;

import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Plugin;
import imagej.plugin.Parameter;
import imglib.ops.function.p1.UnaryOperatorFunction;
import imglib.ops.operator.UnaryOperator;
import imglib.ops.operator.unary.AndConstant;

/**
 * Fills an output Dataset by ANDing an input Dataset with a user defined constant value.
 *
 * @author Barry DeZonia
 */
@Plugin(
	menuPath = "PureIJ2>Process>Math>AND"
)
public class AndDataValuesWith implements ImageJPlugin
{
	// ***************  instance variables that are Parameters ***************************************************************

	@Parameter
	Dataset input;
	
	@Parameter(output=true)
	Dataset output;
	
	@Parameter(label="Enter value to AND with each data value")
	private long constant;
	
	// ***************  constructor ***************************************************************
	
	public AndDataValuesWith()
	{
	}

	// ***************  public interface ***************************************************************

	@Override
	public void run()
	{
		UnaryOperator op = new AndConstant(constant);
		output = new UnaryTransformation(input, output, op).run();
	}
}
