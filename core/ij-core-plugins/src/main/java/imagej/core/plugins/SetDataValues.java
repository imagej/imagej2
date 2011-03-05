package imagej.core.plugins;

import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Plugin;
import imagej.plugin.Parameter;
import imglib.ops.function.p1.UnaryOperatorFunction;
import imglib.ops.operator.UnaryOperator;
import imglib.ops.operator.unary.Constant;

/**
 * TODO
 *
 * @author Barry DeZonia
 */
@Plugin(
	menuPath = "PureIJ2>Process>Math>Set"
)
public class SetDataValues implements ImageJPlugin
{
	// ***************  instance variables that are Parameters ***************************************************************

	@Parameter
	private Dataset input;
	
	@Parameter(output=true)
	private Dataset output;
	
	@Parameter(label="Enter value to fill each value with")
	private double constant;
	
	// ***************  constructor ***************************************************************

	public SetDataValues()
	{
	}
	
	// ***************  public interface ***************************************************************

	@Override
	public void run()
	{
		UnaryOperator op = new Constant(constant);
		output = new UnaryTransformation(input, output, op).run();
	}
}
