package imagej.core.plugins;

import imagej.plugin.Plugin;
import imagej.plugin.Parameter;
import imglib.ops.function.p1.MultiplyConstFunction;

@Plugin(
	menuPath = "Process>Multiply Data Values By"
)
@SuppressWarnings("rawtypes")
public class MultiplyDataValuesBy extends NAryOperation
{
	@Parameter(label="Enter value to multiply each data value by")
	private double constant;
	
	public MultiplyDataValuesBy()
	{
		super();
		super.setFunction(new MultiplyConstFunction(constant));
	}
}
