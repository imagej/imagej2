package imagej.core.plugins;

import imagej.model.Dataset;
import imglib.ops.function.p1.UnaryOperatorFunction;
import imglib.ops.operator.UnaryOperator;

public class UnaryTransformation
{
	// ***************  instance variables ***************************************************************

	private Dataset input;
	private Dataset output;
	private UnaryOperator operator;
	
	// ***************  constructor ***************************************************************

	public UnaryTransformation(Dataset input, Dataset output, UnaryOperator operator)
	{
		this.input = input;
		this.output = output;
		this.operator = operator;
	}
	
	// ***************  public interface ***************************************************************

	public Dataset run()
	{
		UnaryOperatorFunction func = new UnaryOperatorFunction(operator);
		
		NAryOperation operation = new NAryOperation(input, func);

		operation.setOutput(output);
		
		return operation.run();
	}
}
