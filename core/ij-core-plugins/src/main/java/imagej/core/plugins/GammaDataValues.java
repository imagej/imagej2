package imagej.core.plugins;

import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imglib.ops.operator.UnaryOperator;
import imglib.ops.operator.unary.Gamma;

/**
 * TODO
 *
 * @author Barry DeZonia
 */
@Plugin(
	menuPath = "PureIJ2>Process>Math>Gamma"
)
public class GammaDataValues implements ImageJPlugin
{
	// ***************  instance variables that are Parameters ***************************************************************

	@Parameter
	private Dataset input;
	
	@Parameter(output=true)
	private Dataset output;
	
	@Parameter(label="Enter value for gamma constant")
	private double constant;

	// ***************  public interface ***************************************************************

	@Override
	public void run()
	{
		UnaryOperator op = new Gamma(constant);
		output = new UnaryTransformation(input, output, op).run();
	}
}
