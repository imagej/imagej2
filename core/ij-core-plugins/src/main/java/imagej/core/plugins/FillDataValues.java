package imagej.core.plugins;

import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Plugin;
import imagej.plugin.Parameter;
import imglib.ops.function.p1.UnaryOperatorFunction;
import imglib.ops.operator.UnaryOperator;
import imglib.ops.operator.unary.Constant;

/**
 * Fills an output Dataset with a user defined constant value.
 *
 * @author Barry DeZonia
 */
@Plugin(
	menuPath = "PureIJ2>Edit>Fill"
)
public class FillDataValues implements ImageJPlugin
{
	// ***************  instance variables that are Parameters ***************************************************************

	@Parameter
	private Dataset input;  // TODO - does this make sense? used for dimensions only I think. Or do we want in place changing?
	
	@Parameter(output=true)
	private Dataset output;
	
	@Parameter(label="Enter value to fill each data value with")
	private double constant;
	
	// ***************  constructor ***************************************************************

	public FillDataValues()
	{
	}
	
	// ***************  public interrface ***************************************************************

	@Override
	public void run()
	{
		UnaryOperator op = new Constant(constant);
		output = new UnaryTransformation(input, output, op).run();
	}
}
