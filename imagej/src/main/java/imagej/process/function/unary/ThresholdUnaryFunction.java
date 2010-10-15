package imagej.process.function.unary;

public class ThresholdUnaryFunction implements UnaryFunction
{
	double threshold;
	double lowerValue;
	double upperValue;
	
	public ThresholdUnaryFunction(double threshold, double lowerValue, double upperValue)
	{
		this.threshold = threshold;
		this.lowerValue = lowerValue;
		this.upperValue = upperValue;
	}
	
	public double compute(double input) {
		if (input <= this.threshold)
			return this.lowerValue;
		else
			return this.upperValue;
	}
	
}

