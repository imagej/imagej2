package imagej.process.function;

public class ThresholdUnaryFunction implements UnaryFunction
{
	double threshold;
	
	public ThresholdUnaryFunction(double threshold)
	{
		this.threshold = threshold;
	}
	
	public double compute(double input) {
		if (input <= this.threshold)
			return 0;
		else
			return 255;
	}
	
}

