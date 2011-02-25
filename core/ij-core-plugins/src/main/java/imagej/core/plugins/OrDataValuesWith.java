package imagej.core.plugins;

import imagej.plugin.Plugin;
import imagej.plugin.Parameter;
import imglib.ops.function.p1.UnaryOperatorFunction;
import imglib.ops.operator.UnaryOperator;
import imglib.ops.operator.unary.OrConstant;

@Plugin(
	menuPath = "Process>OR"
)
@SuppressWarnings("rawtypes")
public class OrDataValuesWith extends NAryOperation
{
	@Parameter(label="Enter value to OR with each data value")
	private long constant;
	
	public OrDataValuesWith()
	{
		UnaryOperator op = new OrConstant(constant);
		UnaryOperatorFunction func = new UnaryOperatorFunction(op);
		setFunction(func);
	}
}
