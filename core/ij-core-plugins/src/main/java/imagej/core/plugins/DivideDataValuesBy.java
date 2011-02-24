package imagej.core.plugins;

import imagej.plugin.Plugin;
import imagej.plugin.Parameter;
import imglib.ops.function.p1.DivideConstFunction;

@Plugin(
	menuPath = "Process>Divide Data Values By"
)
@SuppressWarnings("rawtypes")
public class DivideDataValuesBy extends NAryOperation
{
	@Parameter(label="Enter value to divide each data value by")
	private double constant;
	
	public DivideDataValuesBy()
	{
		super();
		super.setFunction(new DivideConstFunction(constant));
	}
}
