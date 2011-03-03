package imagej.core.plugins;

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
	menuPath = "PureIJ2>Edit>Fill"
)
public class FillDataValues extends NAryOperation
{
	@Parameter(label="Enter value to fill each data value with")
	private double constant;
	
	public FillDataValues()
	{
	}
	
	@Override
	public void run()
	{
		UnaryOperator op = new Constant(constant);
		UnaryOperatorFunction func = new UnaryOperatorFunction(op);
		setFunction(func);
		super.run();
	}
}
