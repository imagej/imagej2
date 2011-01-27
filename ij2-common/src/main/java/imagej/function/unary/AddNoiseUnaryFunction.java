package imagej.function.unary;

import imagej.DoubleRange;
import imagej.function.UnaryFunction;

import java.util.Random;

public class AddNoiseUnaryFunction implements UnaryFunction
{
	private boolean dataIsIntegral;
	private double typeRangeMin;
	private double typeRangeMax;
	private double spread;
	private Random rng;
	
	public AddNoiseUnaryFunction(boolean isIntegral, double typeMin, double typeMax, double spread)
	{
		this.dataIsIntegral = isIntegral;
		this.typeRangeMin = typeMin;
		this.typeRangeMax = typeMax;
		this.spread = spread;
		this.rng = new Random();
		this.rng.setSeed(System.currentTimeMillis());  // TODO - this line not present in original code (by design???)
	}
	
	public double compute(double input)
	{
		double result, ran;
		boolean inRange = false;
		do {
			ran = this.rng.nextGaussian() * this.spread;
			if (this.dataIsIntegral)
				ran = Math.round(ran);
			result = input + ran;
			inRange = DoubleRange.inside(this.typeRangeMin, this.typeRangeMax, result);
		} while (!inRange);
		return result;
	}
}
