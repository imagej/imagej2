package imagej.core.plugins;

import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Plugin;
import imagej.plugin.Parameter;
import imglib.ops.function.p1.UnaryOperatorFunction;
import imglib.ops.operator.UnaryOperator;
import imglib.ops.operator.unary.Log;

/**
 * TODO
 *
 * @author Barry DeZonia
 */
@Plugin(
	menuPath = "PureIJ2>Process>Math>Log"
)
public class LogDataValues implements ImageJPlugin
{
	// ***************  instance variables that are Parameters ***************************************************************

	@Parameter
	private Dataset input;
	
	@Parameter(output=true)
	private Dataset output;
	
	// ***************  constructor ***************************************************************

	public LogDataValues()
	{
	}
	
	// ***************  public interface ***************************************************************

	@Override
	public void run()
	{
		UnaryOperator op = new Log();
		output = new UnaryTransformation(input, output, op).run();
	}
}
