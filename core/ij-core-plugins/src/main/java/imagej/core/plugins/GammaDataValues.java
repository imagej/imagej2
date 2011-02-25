package imagej.core.plugins;

import imagej.plugin.Plugin;
import imagej.plugin.Parameter;
import imglib.ops.function.p1.UnaryOperatorFunction;
import imglib.ops.operator.UnaryOperator;
import imglib.ops.operator.unary.Gamma;

@Plugin(
	menuPath = "Process>Gamma"
)
@SuppressWarnings("rawtypes")
public class GammaDataValues extends NAryOperation
{
	@Parameter(label="Enter value for gamma constant")
	private double constant;
	
	public GammaDataValues()
	{
		UnaryOperator op = new Gamma(constant);
		UnaryOperatorFunction func = new UnaryOperatorFunction(op);
		setFunction(func);
	}
}
