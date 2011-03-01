package imagej.core.plugins;

import imagej.plugin.Plugin;
import imagej.plugin.Parameter;
import imglib.ops.function.p1.UnaryOperatorFunction;
import imglib.ops.operator.UnaryOperator;
import imglib.ops.operator.unary.AndConstant;

/**
 * TODO
 *
 * @author Barry DeZonia
 */
@Plugin(
	menuPath = "PureIJ2>Process>Math>AND"
)
public class AndDataValuesWith extends NAryOperation
{
	@Parameter(label="Enter value to AND with each data value")
	private long constant;
	
	public AndDataValuesWith()
	{
	}

	@Override
	public void run()
	{
		UnaryOperator op = new AndConstant(constant);
		UnaryOperatorFunction func = new UnaryOperatorFunction(op);
		setFunction(func);
		super.run();
	}
}
