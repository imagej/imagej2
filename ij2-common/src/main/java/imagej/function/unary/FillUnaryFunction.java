package imagej.function.unary;

import imagej.function.UnaryFunction;

public class FillUnaryFunction implements UnaryFunction
{
	private double fillColor;

	public FillUnaryFunction(double fillColor)
	{
		this.fillColor = fillColor;
	}
	
	public double compute(double input)
	{
		return this.fillColor;
	}
}
