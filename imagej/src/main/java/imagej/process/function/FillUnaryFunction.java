package imagej.process.function;

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
