package imagej.core.plugins;

import imagej.plugin.Plugin;
import imagej.plugin.Parameter;
import imglib.ops.function.p1.UnaryOperatorFunction;
import imglib.ops.operator.UnaryOperator;
import imglib.ops.operator.unary.XorConstant;

/**
 * TODO
 *
 * @author Barry DeZonia
 */
@Plugin(
	menuPath = "Process>Math2>XOR"
)
public class XorDataValuesWith extends NAryOperation
{
	@Parameter(label="Enter value to XOR with each data value")
	private long constant;
	
	public XorDataValuesWith()
	{
	}
	
	@Override
	public void run()
	{
		UnaryOperator op = new XorConstant(constant);
		UnaryOperatorFunction func = new UnaryOperatorFunction(op);
		setFunction(func);
		super.run();
	}
}
