package imagej.process.function.unary;

import imagej.Utils;

import java.util.Random;

public class AddNoiseUnaryFunction implements UnaryFunction
{
	private boolean dataIsIntegral;
	private double min;
	private double max;
	private double range;
	private Random rnd;
	
	public AddNoiseUnaryFunction(boolean isIntegral, double min, double max, double range)
	{
		this.dataIsIntegral = isIntegral;
		this.min = min;
		this.max = max;
		this.range = range;
		this.rnd = new Random();
		this.rnd.setSeed(System.currentTimeMillis());  // TODO - this line not present in original code (by design???)
	}
	
	public double compute(double input)
	{
		double result, ran;
		boolean inRange = false;
		do {
			ran = this.rnd.nextGaussian() * this.range;
			if (this.dataIsIntegral)
				ran = Math.round(ran);
			result = input + ran;
			inRange = Utils.insideRange(this.min, this.max, result);
		} while (!inRange);
		return result;
	}
}
