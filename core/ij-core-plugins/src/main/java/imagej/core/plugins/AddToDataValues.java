package imagej.core.plugins;

import imagej.plugin.Plugin;
import imagej.plugin.Parameter;
import imglib.ops.function.p1.AddConstFunction;

/**
 * TODO
 *
 * @author Barry DeZonia
 */
@Plugin(
	menuPath = "Process>Add To Data Values"
)
@SuppressWarnings("rawtypes")
public class AddToDataValues extends NAryOperation
{
	@Parameter(label="Enter value to add to each data value")
	private double constant;
	
	public AddToDataValues()
	{
		super();
		super.setFunction(new AddConstFunction(constant));
	}
}
