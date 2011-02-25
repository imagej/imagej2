package imagej.core.plugins;

import imagej.plugin.Plugin;
import imagej.plugin.Parameter;
import imglib.ops.function.p1.SubtractConstFunction;

/**
 * TODO
 *
 * @author Barry DeZonia
 */
@Plugin(
	menuPath = "Process>Subtract From Data Values"
)
@SuppressWarnings("rawtypes")
public class SubtractFromDataValues extends NAryOperation
{
	@Parameter(label="Enter value to subtract from each data value")
	private double constant;
	
	public SubtractFromDataValues()
	{
		super();
		super.setFunction(new SubtractConstFunction(constant));
	}
}
