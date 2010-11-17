package imagej2.function.unary;

import imagej2.function.UnaryFunction;

public class IntegralSubstitutionUnaryFunction implements UnaryFunction
{
	private int originValue;
	private int[] substitutionTable;
	
	public IntegralSubstitutionUnaryFunction(int originValue, int[] table)
	{
		this.originValue = originValue;
		this.substitutionTable = table;
	}
	
	public double compute(double input)
	{
		return substitutionTable[(int)input - originValue];
	}
}
