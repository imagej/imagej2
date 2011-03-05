package imagej.core.plugins;

import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imglib.ops.function.p1.UnaryOperatorFunction;
import imglib.ops.operator.UnaryOperator;
import imglib.ops.operator.unary.Abs;

/**
 * Fills an output Dataset by applying the absolute value function to an input Dataset.
 *
 * @author Barry DeZonia
 */
@Plugin(
	menuPath = "PureIJ2>Process>Math>Abs"
)
public class AbsDataValues implements ImageJPlugin
{
	// ***************  instance variables that are Parameters ***************************************************************

	@Parameter
	Dataset input;
	
	@Parameter(output=true)
	Dataset output;
	
	// ***************  constructor ***************************************************************

	public AbsDataValues()
	{
	}

	// ***************  public interface ***************************************************************

	@Override
	public void run()
	{
		UnaryOperator op = new Abs();
		output = new UnaryTransformation(input, output, op).run();
	}
}
