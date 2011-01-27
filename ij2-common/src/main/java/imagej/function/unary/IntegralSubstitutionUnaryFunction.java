package imagej.function.unary;

import imagej.function.UnaryFunction;

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
		return this.substitutionTable[(int)input - this.originValue];
	}
}
